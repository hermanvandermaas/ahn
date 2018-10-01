package nl.waywayway.ahn;

import android.content.Context;
import android.widget.TextView;

import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;
import com.github.mikephil.charting.utils.Utils;

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
