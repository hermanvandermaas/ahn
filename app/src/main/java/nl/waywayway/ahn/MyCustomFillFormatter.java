package nl.waywayway.ahn;

import com.github.mikephil.charting.data.*;
import com.github.mikephil.charting.interfaces.dataprovider.*;
import com.github.mikephil.charting.formatter.*;
import com.github.mikephil.charting.interfaces.datasets.*;

public class MyCustomFillFormatter implements IFillFormatter
{

	@Override
	public float getFillLinePosition(ILineDataSet p1, LineDataProvider p2)
	{
		return -10000;
	}
}
