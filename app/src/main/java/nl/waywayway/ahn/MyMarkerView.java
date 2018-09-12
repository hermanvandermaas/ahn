package nl.waywayway.ahn;

import android.content.*;
import android.widget.*;
import com.github.mikephil.charting.components.*;
import com.github.mikephil.charting.data.*;
import com.github.mikephil.charting.highlight.*;
import com.github.mikephil.charting.utils.*;

public class MyMarkerView extends MarkerView
{
    private TextView tvContent;

    public MyMarkerView(Context context, int layoutResource)
	{
        super(context, layoutResource);
        tvContent = (TextView) findViewById(R.id.tvContent);
    }

    // callbacks everytime the MarkerView is redrawn, can be used to update the
    // content (user-interface)
    @Override
    public void refreshContent(Entry e, Highlight highlight)
	{
		tvContent.setText("" + Utils.formatNumber(e.getY(), 2, true));
        super.refreshContent(e, highlight);
    }
	
	private MPPointF mOffset; 

    @Override
    public MPPointF getOffset() {

        if(mOffset == null) {
			// center the marker horizontally and vertically
			mOffset = new MPPointF(-(getWidth() / 2), -getHeight() - 6);
        }

        return mOffset;
    }
}
