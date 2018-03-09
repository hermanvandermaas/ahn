package nl.waywayway.ahn;

import android.util.*;
import com.google.android.gms.maps.model.*;
import java.net.*;

// Factory class voor url

public class UrlMaker
{
	private int tileWidth;
	private int tileHeight;
	private LatLng pointLatLong;
	private static final String URL_ADDITION_FORMAT =
	"https://geodata.nationaalgeoregister.nl/ahn2/ows" +
	"?service=WMS" +
	"&version=1.3.0" +
	"&request=GetFeatureInfo" +
	"&layers=ahn2_05m_ruw" +
	"&query_layers=ahn2_05m_ruw" +
	"&styles=ahn2:ahn2_05m_detail" +
	"&bbox=%f,%f,%f,%f" +
	"&width=%d" +
	"&height=%d" +
	"&i=%d" +
	"&j=%d" +
	"&crs=EPSG:3857" +
	"&format=image/png" +
	"&info_format=application/json";

	private UrlMaker(int x, int y, LatLng pointLatLong){
		this.tileWidth = x;
		this.tileHeight = y;
		this.pointLatLong = pointLatLong;
	}
	
	public static UrlMaker getUrlMaker(int tileWidth, int tileHeight, LatLng cPointLatLong)
	{
		return new UrlMaker(tileWidth, tileHeight, cPointLatLong);
	}
	
	public URL makeUrl()
	{
		UrlTileProvider tileProvider = (UrlTileProvider) WMSTileProvider.getTileProvider(256, 256);
		// TO DO
		// latlong naar url
		
		URL url = tileProvider.getTileUrl(1, 2, 3);
		Log.i("HermLog", "Url van tegel met pixel: " + url.toString());
		
		return url;
	}
}
