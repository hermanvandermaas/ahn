package nl.waywayway.ahn;

import android.util.*;
import com.github.mikephil.charting.data.*;
import com.google.android.gms.maps.model.*;
import java.util.*;

public class LineChartDataMaker
{
	private double lowCap = -10000;
	private double highCap = 10000;
	
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
			if (y == null || y > highCap || y < lowCap) y = 0d;
			//Log.i("HermLog", "x/y: " + x + " / " + y);
			entries.add(new Entry(x.floatValue(), y.floatValue(), p)); 
		}
		
		return entries;
	}

}
