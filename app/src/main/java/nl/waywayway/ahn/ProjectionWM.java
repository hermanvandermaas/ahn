package nl.waywayway.ahn;

import android.graphics.*;
import android.util.*;
import com.google.android.gms.maps.model.*;

// Projectie Web Mercator
// Converteren tussen breedte/lengte en geprojecteerde pixels
// in de ruimte x[0...256) y[0...256), eerste pixel is 0, bereik is exclusief pixel 256 
// x=0, y=0 is oorsprong in het noordwesten; x toenemend naar oost, y toenemend naar zuid

public class ProjectionWM
{
	// Size of square world map in pixels
    private static final double MAP_SIZE = 256;
	
	// Grenzen kaart
	private static final double minLat = -85.05112878;
	private static final double maxLat = 85.05112878;
	private static final double minLon = -180;
	private static final double maxLon = 180;
	
	// Array indexes
    private static final int X = 0;
    private static final int Y = 1;
	
	// Private constructor
	private ProjectionWM() {}
	
	// Van genormaliseerde x/y coordinaten in pixels x{0...256} y{0...256}
	// naar breedte/lengte coordinaten in decimale graden (wgs84 ellipsoide)
	// in double formaat, niet afgerond, voor eventueel omrekenen van pixels naar meters
	// of projecteren op grotere kaarten dan 256x256 pixels
	public static LatLng xyToLatLng(double[] xyMeters)
	{
		double x = (clip(xyMeters[X], 0, MAP_SIZE - 1) / MAP_SIZE) - 0.5;
		double y = 0.5 - (clip(xyMeters[Y], 0, MAP_SIZE - 1) / MAP_SIZE);

		double latitude = 90 - 360 * Math.atan(Math.exp(-y * 2 * Math.PI)) / Math.PI;
		double longitude = 360 * x;
		
		Log.i("HermLog", "Breedte/lengte: " + latitude + " " + longitude);
		
		return new LatLng(latitude, longitude);
	}
	
	// ... en andersom
	public static double[] latLngToXY(LatLng mLatLong)
	{
		double latitude = clip(mLatLong.latitude, minLat, maxLat);
		double longitude = clip(mLatLong.longitude, minLon, maxLon);

		double x = (longitude + 180) / 360;
		double sinLatitude = Math.sin(latitude * Math.PI / 180);
		double y = 0.5 - Math.log((1 + sinLatitude) / (1 - sinLatitude)) / (4 * Math.PI);

		double pixelX = clip(x * MAP_SIZE + 0.5, 0, MAP_SIZE - 1);
		double pixelY = clip(y * MAP_SIZE + 0.5, 0, MAP_SIZE - 1);
		
		double[] xyArray = new double[]{pixelX, pixelY};
		
		Log.i("HermLog", "Pixelcoordinaten: x: " + xyArray[0] + " y: " + xyArray[1]);
		
		return xyArray;
	}
	
	private static double clip(double n, double minValue, double maxValue)
	{
		return Math.min(Math.max(n, minValue), maxValue);
	}
}
