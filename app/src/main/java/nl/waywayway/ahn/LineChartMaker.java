package nl.waywayway.ahn;

import com.github.mikephil.charting.charts.*;
import com.github.mikephil.charting.data.*;
import java.util.*;

// Voegt data en opmaak toe aan bestaande lege lijngrafiek instance

public class LineChartMaker
{
	private String label = "Hoogte +/- NAP";
	
	private LineChartMaker()
	{}

	public static LineChartMaker getChartMaker()
	{
		return new LineChartMaker();
	}

	public void makeChart(Chart chart, List<Entry> entries)
	{
		LineDataSet dataSet = new LineDataSet(entries, label);
		LineData lineData = new LineData(dataSet);
		chart.setData(lineData);
		chart.invalidate();
	}
}
