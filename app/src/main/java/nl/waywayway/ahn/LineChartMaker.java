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
	private String label = "Hoogte +/- NAP";
	
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
		Description description = new Description();
		description.setText(title);
		chart.setDescription(description);
		
		LineDataSet dataSet = new LineDataSet(entries, label);
		dataSet.setDrawCircles(false);
		dataSet.setDrawValues(false);
		dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
		dataSet.setLineWidth(3f);
		int lineColor = Color.BLACK;
		//context.getResources().getColor(R.color.accent);
		Log.i("HermLog", "lineColor: " + lineColor);
		dataSet.setFillColor(lineColor);
		dataSet.setDrawHorizontalHighlightIndicator(false);
		dataSet.setDrawVerticalHighlightIndicator(true);
		
		LineData lineData = new LineData(dataSet);
		chart.setData(lineData);
		//chart.invalidate();
		chart.animateY(500, Easing.EasingOption.EaseInOutCubic);
	}
}
