package nl.waywayway.ahn;

import android.view.*;
import android.widget.*;

public class SwipeRightGestureListener extends GestureDetector.SimpleOnGestureListener
{
	private static final int SWIPE_THRESHOLD = 200;
	private static final int SWIPE_VELOCITY_THRESHOLD = 100;
	
	@Override
	public boolean onDown(MotionEvent event)
	{
		return true;
	}

	@Override
	public boolean onFling(MotionEvent event1, MotionEvent event2, float velocityX, float velocityY)
	{
		boolean result = false;

		try
		{
			float diffY = event2.getY() - event1.getY();
			float diffX = event2.getX() - event1.getX();
			
			if (Math.abs(diffX) > Math.abs(diffY))
			{
				if (true /*Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD*/)
				{
					if (diffX > 0)
					{
						onSwipeRight();
					}
					else
					{
						onSwipeLeft();
					}
					result = true;
				}
			}
			else if (true /*Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD*/)
			{
				if (diffY > 0)
				{
					onSwipeBottom();
				}
				else
				{
					onSwipeTop();
				}
				result = true;
			}
		}
		catch (Exception exception)
		{
			exception.printStackTrace();
		}
		
		
		
		return result;
	}

	public void onSwipeTop()
	{
		// TODO: Implement this method
	}

	public void onSwipeBottom()
	{
		// TODO: Implement this method
	}

	public void onSwipeLeft()
	{
		// TODO: Implement this method
	}

	public void onSwipeRight()
	{
		// TODO: Implement this method
	}
}
