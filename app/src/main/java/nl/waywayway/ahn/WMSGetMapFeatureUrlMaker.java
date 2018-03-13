package nl.waywayway.ahn;

import android.util.*;
import com.google.android.gms.maps.model.*;
import java.net.*;
import java.util.*;

// Factory class voor url

public class WMSGetMapFeatureUrlMaker
{
	private int tileWidth;
	private int tileHeight;
	private LatLng pointLatLong;
	private float zoom;
	
	// Array indexes
    private static final int X = 0;
    private static final int Y = 1;
	
	private static final String URL_REPLACEMENT_FORMAT =
	"&request=GetFeatureInfo";
	
	private static final String URL_AFFIX_FORMAT =
	"&query_layers=ahn2_05m_ruw" +
	"&i=%d" +
	"&j=%d" +
	"&info_format=application/json";

	private WMSGetMapFeatureUrlMaker(int x, int y, LatLng pointLatLong, double zoom){
		this.tileWidth = x;
		this.tileHeight = y;
		this.pointLatLong = pointLatLong;
		this.zoom = (int) Math.floor(zoom);
	}
	
	public static WMSGetMapFeatureUrlMaker getUrlMaker(int tileWidth, int tileHeight, LatLng cPointLatLong, double cZoom)
	{
		return new WMSGetMapFeatureUrlMaker(tileWidth, tileHeight, cPointLatLong, cZoom);
	}
	
	// Mag null zijn
	public URL makeUrl()
	{
		int zoomInt = (int) Math.floor(zoom);
		
		int[] pixelCoordinates = ProjectionWM.latLngToXYpixels(pointLatLong, zoomInt);
		int[] tileCoordinates = ProjectionWM.getTileCoordinates(pixelCoordinates);
		int[] pixelOnTileCoordinates = ProjectionWM.getPixelCoordinatesOnTile(pixelCoordinates);
		
		// Eerste deel url kan gemaakt worden door WMSTileProvider
		UrlTileProvider tileProvider = (UrlTileProvider) WMSTileProvider.getTileProvider(tileWidth, tileHeight);
		String url = tileProvider.getTileUrl(tileCoordinates[X], tileCoordinates[Y], zoomInt).toString();
		//Log.i("HermLog", "GetMap url van tegel met pixel: " + url);
		if (url == null) return null;
		
		// GetFeatureInfo in plaats van GetMap 
		String regex = "(?i)&request=GetMap";
		url = url.replaceAll(regex, "&request=GetFeatureInfo");
		//Log.i("HermLog", "GetFeatureMap url van tegel: " + url);
		
		// Voeg parameters toe aan url
		url = url + URL_AFFIX_FORMAT;
		//Log.i("HermLog", "GetFeatureMap url van tegel na toevoeging: " + url);
		
		url = String.format(
			Locale.US, 
			url, 
			pixelOnTileCoordinates[X], 
			pixelOnTileCoordinates[Y]);
			
		Log.i("HermLog", "GetFeatureMap url: " + url);
			
		try
		{
			return new URL(url);
		}
		catch (MalformedURLException e)
		{
			throw new AssertionError(e);
		}
	}
}
