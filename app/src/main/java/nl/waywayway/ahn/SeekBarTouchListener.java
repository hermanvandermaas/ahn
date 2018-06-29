package nl.waywayway.ahn;

import android.content.*;
import android.graphics.*;
import android.util.*;
import android.view.*;
import android.widget.*;

// NavigationView (drawer), niet laten meeschuiven met SeekBar
// SeekBar niet aanpassen als gebruiker begint te scrollen op SeekBar,
// maar eindigt buiten SeekBar

public class SeekBarTouchListener implements View.OnTouchListener
{
	Context context;
	float oldX = 0;
	float oldY = 0;

	public SeekBarTouchListener(Context context)
	{
		this.context = context;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) 
	{
		int action = event.getAction();

		switch (action)
		{
			case MotionEvent.ACTION_DOWN:
				oldX = event.getX();
				oldY = event.getY();
				//rect = new Rect(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
				break;

			case MotionEvent.ACTION_MOVE:
				float newX = event.getX();
				float newY = event.getY();
				float deltaX = newX - oldX;
				float deltaY = newY - oldY;

				if (Math.abs(deltaY) < Math.abs(deltaX))
				{
					// Motion in X direction
					v.getParent().requestDisallowInterceptTouchEvent(true);

					// Handle seekbar touch events
					v.onTouchEvent(event);

					return true;
				}
				else if (Math.abs(deltaY) >= Math.abs(deltaX))
				{
					// Motion in Y direction
					return false;
				}
				break;
		}

		// Handle seekbar touch events
		v.onTouchEvent(event);

		return true;
	}
}
