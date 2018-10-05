package nl.waywayway.ahn;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

public class ConnectionUtils
{
	// Netwerkverbinding ja/nee
	public static boolean isNetworkConnected(Context context)
	{
		ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		return networkInfo != null && networkInfo.isConnected();
	}
	
	public static boolean showMessageOnlyIfNotConnected(Context context, String message, Boolean flagWasShowed)
	{
		if (!isNetworkConnected(context) && !flagWasShowed)
		{
			showMessage(context, message);
			return true;
		}
		else
			return flagWasShowed;
	}
	
	public static void showMessage(Context context, String message)
	{
			Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
	}
}
