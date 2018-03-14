package nl.waywayway.ahn;

import android.graphics.*;
import android.util.*;
import com.google.android.gms.maps.model.*;

// Projectie Web Mercator (WM)
// Converteren tussen breedte-/lengtecoordinaten in decimale graden (wgs84 ellipsoide)
// en op een vierkante kaart geprojecteerde pixels in de ruimte x[0...256*2^zoom), y[0...256*2^zoom), eerste pixel is 0, bereik is exclusief laatste pixel
// x=0, y=0 is oorsprong in het noordwesten; x toenemend naar oost, y toenemend naar zuid
// zoom is in double formaat

public class ProjectionWM
{
	// Grootte van 1 tegel
	// Size of square world map in pixels at zoom level 0
    private static final double TILE_SIZE = 256;
	
	// Size of square world map in meters, using WebMerc projection.
    private static final double MAP_SIZE_METERS = 20037508.34789244 * 2;
	
	// Grenzen kaart
	private static final double minLat = -85.05112878;
	private static final double maxLat = 85.05112878;
	private static final double minLon = -180;
	private static final double maxLon = 179.999999999999;
	
	// Array indexes
    private static final int X = 0;
    private static final int Y = 1;
	
	// Lege private constructor, instances niet nodig
	private ProjectionWM() {}
	
	// Van x/y coordinaten in meters naar breedte/lengte coordinaten in decimale graden
	public static LatLng xyToLatLng(double[] xyMeters)
	{
		// TO DO
		// deze method correct maken
		
		double x = (clip(xyMeters[X], 0, TILE_SIZE - 1) / TILE_SIZE) - 0.5;
		double y = 0.5 - (clip(xyMeters[Y], 0, TILE_SIZE - 1) / TILE_SIZE);

		double latitude = 90 - 360 * Math.atan(Math.exp(-y * 2 * Math.PI)) / Math.PI;
		double longitude = 360 * x;
		
		Log.i("HermLog", "Breedte/lengte: " + latitude + " " + longitude);
		
		return new LatLng(latitude, longitude);
	}
	
	// Van breedte/lengte naar x/y weergegeven als fractie [0-1)
	// voor later omrekenen naar pixels of meters
	// Nb: breedte/lengte (latitude/longitude) is y/x, niet x/y
	// Nb2: oorsprong linksboven
	public static double[] latLngToXY(LatLng mLatLong)
	{
		double latitude = clip(mLatLong.latitude, minLat, maxLat);
		double longitude = clip(mLatLong.longitude, minLon, maxLon);

		double x = (longitude + 180) / 360;
		double sinLatitude = Math.sin(latitude * Math.PI / 180);
		double y = 0.5 - Math.log((1 + sinLatitude) / (1 - sinLatitude)) / (4 * Math.PI);

		double[] xyFractionArray = new double[]{x, y};
		
		//Log.i("HermLog", "X/y coordinaten als fractie: x: " + xyFractionArray[X] + " y: " + xyFractionArray[Y]);
		
		return xyFractionArray;
	}
	
	// Van breedte/lengte naar x/y in pixels
	public static int[] latLngToXYpixels(LatLng mLatLong, int zoom)
	{
		// Eerst xy als fractie bepalen
		double[] xyFractionArray = latLngToXY(mLatLong);
		
		// Fractie coordinaten * kaartgrootte gegeven zoomlevel = pixelcoordinaten
		int pixelX = (int) Math.floor(clip(xyFractionArray[X] * mapSize(zoom) + 0.5, 0, mapSize(zoom) - 1));
		int pixelY = (int) Math.floor(clip(xyFractionArray[Y] * mapSize(zoom) + 0.5, 0, mapSize(zoom) - 1));

		int[] xyPixelArray = new int[]{pixelX, pixelY};

		//Log.i("HermLog", "Pixelcoordinaten: x: " + xyPixelArray[X] + " y: " + xyPixelArray[Y]);

		return xyPixelArray;
	}
	
	// Van breedte/lengte naar x/y in meters
	// Nb: oorsprong linksboven
	public static double[] latLngToXYmeters(LatLng mLatLong, int zoom)
	{
		// Eerst xy als fractie bepalen
		double[] xyFractionArray = latLngToXY(mLatLong);

		// Fractie coordinaten * omtrek aarde in WM = metercoordinaten
		double meterX = clip(xyFractionArray[X] * MAP_SIZE_METERS, 0, MAP_SIZE_METERS);
		double meterY = clip(xyFractionArray[Y] * MAP_SIZE_METERS, 0, MAP_SIZE_METERS);

		double[] xyMeterArray = new double[]{meterX, meterY};

		//Log.i("HermLog", "Metercoordinaten: x: " + xyMeterArray[X] + " y: " + xyMeterArray[Y]);

		return xyMeterArray;
	}
	
	// Tegelcoordinaten volgens Google Maps
	// van tegel die de gegeven pixel bevat
	// oorsprong linksboven, begint met x=0, y=0
	public static int[] getTileCoordinates(int[] pixelCoordinates)
	{
		int[] tileCoordinates = new int[]{(int) Math.floor(pixelCoordinates[X] / TILE_SIZE), (int) Math.floor(pixelCoordinates[Y] / TILE_SIZE)};
		//Log.i("HermLog", "tileCoordinates: x=" + tileCoordinates[X] + " y: " + tileCoordinates[Y]);
		return tileCoordinates;
	}
	
	// Pixel coordinaten binnen een tegel
	// voor opvragen wms feature
	// oorsprong linksboven, begint met x=0, y=0
	public static int[] getPixelCoordinatesOnTile(int[] pixelCoordinates)
	{
		int x = (int) Math.floor(pixelCoordinates[X] - getTileCoordinates(pixelCoordinates)[X] * TILE_SIZE);
		int y = (int) Math.floor(pixelCoordinates[Y] - getTileCoordinates(pixelCoordinates)[Y] * TILE_SIZE);
		
		int[] pixelOnTileCoordinates = new int[]{x, y};
		//Log.i("HermLog", "pixelOnTileCoordinates: x=" + pixelOnTileCoordinates[X] + " y: " + pixelOnTileCoordinates[Y]);
		return pixelOnTileCoordinates;
	}
	
	// Kaartgrootte in pixels bij gegeven zoomniveau
	public static int mapSize(double zoomLevel)
	{
		int mapSize = (int) Math.floor(TILE_SIZE * Math.pow(2, zoomLevel));
		//Log.i("HermLog", "mapSize: " + mapSize + " zoomLevel: " + zoomLevel);
		return mapSize;
	}
	
	public static double clip(double n, double minValue, double maxValue)
	{
		double value = Math.min(Math.max(n, minValue), maxValue);
		//if (n != value) Log.i("HermLog", "clip(): n=" + n + " wordt value=" + value);
		return value;
	}
}
