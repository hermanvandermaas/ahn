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
	private LayerItem layerItem;
	
	// Array indexes
    private static final int X = 0;
    private static final int Y = 1;
	
	private static final String URL_TO_BE_REPLACED_FORMAT = 
		"&request=GetMap";
	private static final String URL_REPLACEMENT_FORMAT =
		"&request=GetFeatureInfo";
	private static final String URL_AFFIX_FORMAT_1 =
		"&query_layers=";
	private static final String URL_AFFIX_FORMAT_2 =
		"&i=%d" +
		"&j=%d" +
		"&info_format=application/json";

	private WMSGetMapFeatureUrlMaker(int x, int y, LatLng pointLatLong, double zoom, LayerItem layerItem){
		this.tileWidth = x;
		this.tileHeight = y;
		this.pointLatLong = pointLatLong;
		this.zoom = (int) Math.floor(zoom);
		this.layerItem = layerItem;
	}
	
	public static WMSGetMapFeatureUrlMaker getUrlMaker(int tileWidth, int tileHeight, LatLng cPointLatLong, double cZoom, LayerItem cLayerItem )
	{
		return new WMSGetMapFeatureUrlMaker(tileWidth, tileHeight, cPointLatLong, cZoom, cLayerItem);
	}
	
	// Mag null zijn
	public URL makeUrl()
	{
		int zoomInt = (int) Math.floor(zoom);
		
		int[] pixelCoordinates = ProjectionWM.latLngToXYpixels(pointLatLong, zoomInt);
		int[] tileCoordinates = ProjectionWM.getTileCoordinates(pixelCoordinates);
		int[] pixelOnTileCoordinates = ProjectionWM.getPixelCoordinatesOnTile(pixelCoordinates);
		
		// Eerste deel url kan gemaakt worden door WMSTileProvider
		UrlTileProvider tileProvider = (UrlTileProvider) WMSTileProvider.getTileProvider(
			tileWidth, 
			tileHeight, 
			layerItem.getServiceUrl(), 
			layerItem.getMinx(), 
			layerItem.getMiny(), 
			layerItem.getMaxx(), 
			layerItem.getMaxy());
			
		URL tileUrl = tileProvider.getTileUrl(tileCoordinates[X], tileCoordinates[Y], zoomInt);
		if (tileUrl == null) return null;
		String url = tileUrl.toString();
		//Log.i("HermLog", "GetMap url van tegel met pixel: " + url);
		
		// GetFeatureInfo in plaats van GetMap 
		String regex = "(?i)" + URL_TO_BE_REPLACED_FORMAT;
		url = url.replaceAll(regex, URL_REPLACEMENT_FORMAT);
		//Log.i("HermLog", "GetFeatureMap url van tegel: " + url);
		
		// Voeg parameters toe aan url
		url = url + URL_AFFIX_FORMAT_1 + layerItem.getWMSGetMapFeatureInfoQueryLayer() + URL_AFFIX_FORMAT_2;
		//Log.i("HermLog", "GetFeatureMap url van tegel na toevoegingen: \n" + url);
		
		url = String.format(
			Locale.US, 
			url, 
			pixelOnTileCoordinates[X], 
			pixelOnTileCoordinates[Y]);
			
		Log.i("HermLog", "GetFeatureMap url compleet: \n" + url);
			
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
