package nl.waywayway.ahn;
import android.content.*;
import android.widget.*;
import com.github.mikephil.charting.charts.*;
import com.github.mikephil.charting.data.*;
import com.github.mikephil.charting.highlight.*;
import com.github.mikephil.charting.listener.*;

public class MyOnChartValueSelectedListener
	implements OnChartValueSelectedListener
{
	private LineChart lineChart;
	private Context context;
	
	private MyOnChartValueSelectedListener(Context context, LineChart lineChart)
	{
		this.context = context;
		this.lineChart = lineChart;
	}

	public static MyOnChartValueSelectedListener getListener(Context context, LineChart lineChart)
	{
		return new MyOnChartValueSelectedListener(context, lineChart);
	}
	
	@Override
	public void onValueSelected(Entry entry, Highlight highlight)
	{
		Toast.makeText(context, "Hoogte: " + highlight.getY(), Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onNothingSelected()
	{
		
	}
}
