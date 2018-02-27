package nl.waywayway.ahn;

import android.util.*;
import com.google.android.gms.maps.model.*;
import java.net.*;
import java.util.*;

// Factory class voor WMS tiles in Web Mercator projectie

public class WMSTileProvider extends UrlTileProvider
{
	// Web Mercator n/w corner of the map.
    private static final double[] TILE_ORIGIN = {-20037508.34789244, 20037508.34789244};
	
    //array indexes for that data
    private static final int ORIG_X = 0;
    private static final int ORIG_Y = 1;
	
    // Size of square world map in meters, using WebMerc projection.
    private static final double MAP_SIZE = 20037508.34789244 * 2;
	
    // array indexes for array to hold bounding boxes.
    protected static final int MINX = 0;
    protected static final int MAXX = 1;
    protected static final int MINY = 2;
    protected static final int MAXY = 3;
	
	// bounding box van de kaart van Nederland
	// voor check of gevraagde tegel bestaat
	protected static final double MINX_MAP = 361403.7366878665;
	protected static final double MAXX_MAP = 6573443.017669047;
	protected static final double MINY_MAP = 807808.8874921346;
	protected static final double MAXY_MAP = 7082066.659439815;
	
	private static final String URL_FORMAT =
		"https://geodata.nationaalgeoregister.nl/ahn2/ows" +
		"?service=WMS" +
		"&version=1.3.0" +
		"&request=GetMap" +
		"&layers=ahn2_05m_ruw" +
		"&styles=ahn2:ahn2_05m_detail" +
		"&bbox=%f,%f,%f,%f" +
		"&width=256" +
		"&height=256" +
		"&crs=EPSG:3857" +
		"&format=image/png" +
		"&transparent=true";
	
    // Construct with tile size in pixels, normally 256, see parent class
    public WMSTileProvider(int x, int y)
	{
    	super(x, y);
    }

	// return a wms tile layer
	public static TileProvider getTileProvider() {
		return new WMSTileProvider(256,256);
	}
	
	@Override
	public synchronized URL getTileUrl(int x, int y, int zoom) {
		double[] bbox = getBoundingBox(x, y, zoom);
		String s = String.format(Locale.US, URL_FORMAT, bbox[MINX],
								 bbox[MINY], bbox[MAXX], bbox[MAXY]);
								 
		Log.i("HermLog", "tile exists?: " + tileExists(bbox));					 
								 
		if (!tileExists(bbox))
		{
			return null;
		}
		
		try {
			Log.i("HermLog", "Url van tegel: " + new URL(s).toString());
			return new URL(s);
		} catch (MalformedURLException e) {
			throw new AssertionError(e);
		}
	}

	private boolean tileExists(double[] bbox)
	{
		if (bbox[MINX] > MAXX_MAP ||
			bbox[MAXX] < MINX_MAP ||
			bbox[MINY] > MINY_MAP ||
			bbox[MAXY] < MINY_MAP)
			return false;
		
		return true;
	}

    // Return a web Mercator bounding box given tile x/y indexes and a zoom
    // level.
    protected double[] getBoundingBox(int x, int y, int zoom)
	{
    	double tileSize = MAP_SIZE / Math.pow(2, zoom);
    	double minx = TILE_ORIGIN[ORIG_X] + x * tileSize;
    	double maxx = TILE_ORIGIN[ORIG_X] + (x + 1) * tileSize;
    	double miny = TILE_ORIGIN[ORIG_Y] - (y + 1) * tileSize;
    	double maxy = TILE_ORIGIN[ORIG_Y] - y * tileSize;

    	double[] bbox = new double[4];
    	bbox[MINX] = minx;
    	bbox[MINY] = miny;
    	bbox[MAXX] = maxx;
    	bbox[MAXY] = maxy;
		
		Log.i("HermLog", "bbox: " + bbox.toString());

    	return bbox;
    }
}
