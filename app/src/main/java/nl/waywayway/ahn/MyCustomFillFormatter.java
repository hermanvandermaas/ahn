package nl.waywayway.ahn;

import com.github.mikephil.charting.formatter.IFillFormatter;
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

public class MyCustomFillFormatter implements IFillFormatter
{

	@Override
	public float getFillLinePosition(ILineDataSet p1, LineDataProvider p2)
	{
		return -10000;
	}
}
