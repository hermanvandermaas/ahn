package nl.waywayway.ahn;

import com.github.mikephil.charting.data.*;
import java.util.*;

public class LineChartDataMaker
{
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
			entries.add(new Entry(xData.get(i).floatValue(), yData.get(i).floatValue())); 
		}
		
		return entries;
	}

}
