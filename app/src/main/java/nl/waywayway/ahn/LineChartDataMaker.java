package nl.waywayway.ahn;

import android.content.Context;

import com.github.mikephil.charting.data.Entry;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class LineChartDataMaker
{
	private Double lowCap = -10000d;
	private Double highCap = 10000d;
	
	private LineChartDataMaker()
	{}

	public static LineChartDataMaker getDataMaker()
	{
		return new LineChartDataMaker();
	}

	// ArrayList xData, yData en pointsList moeten hetzelfde aantal elementen hebben
	public ArrayList<Entry> makeData(ArrayList<Double> xData, ArrayList<Double> yData, ArrayList<LatLng> pointsList)
	{
		if (xData == null || yData == null || xData.size() != yData.size()) return null;
		ArrayList<Entry> entries = new ArrayList<Entry>();

		for (int i = 0; i < xData.size(); i++)
		{
			Double x = xData.get(i);
			Double y = yData.get(i);
			LatLng p = pointsList.get(i);
			if (y == null || y > highCap || y < lowCap) continue;
			//Log.i("HermLog", "x/y: " + x + " / " + y);
			entries.add(new Entry(x.floatValue(), y.floatValue(), p)); 
		}
		
		return entries;
	}

	public ArrayList<String> getColumnlabels(Context context) {
		ArrayList<String> columnLabels = new ArrayList<String>();
		columnLabels.add(context.getResources().getString(R.string.table_column_label_1));
		columnLabels.add(context.getResources().getString(R.string.table_column_label_2));
		columnLabels.add(context.getResources().getString(R.string.table_column_label_3));
		columnLabels.add(context.getResources().getString(R.string.table_column_label_4));

		return columnLabels;
	}
}
