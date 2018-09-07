package nl.waywayway.ahn;

import android.util.*;
import com.github.mikephil.charting.data.*;
import java.util.*;

public class LineChartDataMaker
{
	private double low = -10000;
	private double high = 10000;
	
	private LineChartDataMaker()
	{}

	public static LineChartDataMaker getDataMaker()
	{
		return new LineChartDataMaker();
	}

	// ArrayList xData en yData moeten hetzelfde aantal elementen hebben
	public ArrayList<Entry> makeData(ArrayList<Double> xData, ArrayList<Double> yData)
	{
		if (xData == null || yData == null || xData.size() != yData.size()) return null;
		ArrayList<Entry> entries = new ArrayList<Entry>();

		for (int i = 0; i < xData.size(); i++)
		{
			float x = xData.get(i).floatValue();
			float y = yData.get(i).floatValue();
			if (y > high || y < low) y = 0;
			Log.i("HermLog", "y: " + y);
			//if (y == null) y = 0;
			entries.add(new Entry(x, y)); 
		}
		
		return entries;
	}

}
