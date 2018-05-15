package nl.waywayway.ahn;

import android.graphics.*;
import android.view.*;
import android.widget.*;

public class ViewToImage
{
	private ViewToImage(){}
	
	public static final ViewToImage getInstance()
	{
		return new ViewToImage();
	}
	
	public Bitmap viewToImage(View v)
	{
        v.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
														  RelativeLayout.LayoutParams.WRAP_CONTENT));
        v.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
				  View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        v.layout(0, 0, v.getMeasuredWidth(), v.getMeasuredHeight());
        Bitmap bitmap = Bitmap.createBitmap(v.getMeasuredWidth(),
											v.getMeasuredHeight(),
											Bitmap.Config.ARGB_8888);

        Canvas c = new Canvas(bitmap);
        v.layout(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
        v.draw(c);
		
        return bitmap;
    }
}
