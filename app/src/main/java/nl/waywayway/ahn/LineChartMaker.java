package nl.waywayway.ahn;

import android.content.*;
import android.graphics.*;
import android.util.*;
import com.github.mikephil.charting.charts.*;
import com.github.mikephil.charting.components.*;
import com.github.mikephil.charting.data.*;
import java.util.*;
import com.github.mikephil.charting.animation.*;

// Voegt data en opmaak toe aan bestaande lege lijngrafiek instance

public class LineChartMaker
{
	private Context context;
	private String noDataText = "";
	private String label = "Hoogte in meters +/- NAP";
	private float lineWidth = 3f;
	private float highLightLineWidth = 1f;
	
	private LineChartMaker(Context context)
	{
		this.context = context;
	}

	public static LineChartMaker getChartMaker(Context context)
	{
		return new LineChartMaker(context);
	}

	public void makeChart(Chart chart, List<Entry> entries, String title)
	{
		LineChart lineChart = (LineChart) chart;
		
		lineChart.setNoDataText(noDataText);
		Description description = new Description();
		description.setText(title);
		lineChart.setDescription(description);
		
		lineChart.setPinchZoom(true);
		
		lineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
		lineChart.getAxisRight().setEnabled(false);
		
		LineDataSet dataSet = new LineDataSet(entries, label);
		dataSet.setDrawCircles(false);
		dataSet.setDrawValues(false);
		
		dataSet.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
		
		int lineColor = context.getResources().getColor(R.color.accent);
		int highLightLineColor = context.getResources().getColor(R.color.black);
		
		dataSet.setLineWidth(lineWidth);
		dataSet.setDrawFilled(true);
		dataSet.setFillFormatter(new MyCustomFillFormatter());
		dataSet.setFillColor(lineColor);
		dataSet.setColor(lineColor);
		
		dataSet.setDrawHorizontalHighlightIndicator(false);
		dataSet.setDrawVerticalHighlightIndicator(true);
		dataSet.setHighlightLineWidth(highLightLineWidth);
		dataSet.setHighLightColor(highLightLineColor);
		
		IMarker marker = new MyMarkerView(context, R.layout.line_chart_highlight_marker);
		chart.setMarker(marker);
		
		LineData lineData = new LineData(dataSet);
		lineChart.setData(lineData);
		lineChart.setOnChartValueSelectedListener(MyOnChartValueSelectedListener.getListener(context, lineChart));
		
		//chart.invalidate();
		lineChart.animateY(300, Easing.EasingOption.EaseInOutCubic);
	}
}
