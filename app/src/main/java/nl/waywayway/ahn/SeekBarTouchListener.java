package nl.waywayway.ahn;

import android.app.*;
import android.content.*;
import android.view.*;

// NavigationView (drawer), niet laten meeschuiven met SeekBar
// SeekBar niet aanpassen als gebruiker verticaal begint te scrollen op SeekBar,
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
			// ACTION_DOWN eenmalig, start gesture
			case MotionEvent.ACTION_DOWN:
				oldX = event.getX();
				oldY = event.getY();
				break;

			// ACTION_MOVE doorlopend, per bewogen pixel
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
				else
				{
					// Motion in Y direction
					return false;
				}
		}

		// Handle seekbar touch events
		v.onTouchEvent(event);

		return true;
	}
}
