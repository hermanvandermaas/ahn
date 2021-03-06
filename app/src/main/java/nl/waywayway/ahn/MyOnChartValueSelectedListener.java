package nl.waywayway.ahn;

import android.content.Context;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.android.gms.maps.model.LatLng;

public class MyOnChartValueSelectedListener
	implements OnChartValueSelectedListener
{
	private LineChart lineChart;
	private Context context;
	private Callbacks callbacks;
	
	// Interface voor uitvoeren van methods in Activity die deze interface implementeert
	public interface Callbacks
	{
		public void drawMarker(LatLng point);
		public void removeMarker();
	}
	
	private MyOnChartValueSelectedListener(Context context, LineChart lineChart)
	{
		this.context = context;
		this.lineChart = lineChart;
		
		if (!(context instanceof Callbacks))
		{
			throw new IllegalStateException("Activity must implement the Callbacks interface.");
		}
		
		callbacks = (Callbacks) context;
	}

	public static MyOnChartValueSelectedListener getListener(Context context, LineChart lineChart)
	{
		return new MyOnChartValueSelectedListener(context, lineChart);
	}
	
	@Override
	public void onValueSelected(Entry entry, Highlight highlight)
	{
		callbacks.removeMarker();
		callbacks.drawMarker((LatLng) entry.getData());
	}

	@Override
	public void onNothingSelected()
	{}
}
