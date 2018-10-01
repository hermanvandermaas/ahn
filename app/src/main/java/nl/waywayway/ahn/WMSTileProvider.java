package nl.waywayway.ahn;

import com.google.android.gms.maps.model.TileProvider;
import com.google.android.gms.maps.model.UrlTileProvider;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

// Factory class voor WMS tiles in Web Mercator projectie

public class WMSTileProvider extends UrlTileProvider
{
	private int tileWidth;
	private int tileHeight;
	private String urlFormat;
	// bounding box van de kaart van Nederland
	// in eenheden van het CRS 3857, meters
	// voor check of gevraagde tegel bestaat
	private double minxMap;
	private double minyMap;
	private double maxxMap;
	private double maxyMap;

	// Web Mercator n/w corner of the map.
    private static final double[] TILE_ORIGIN = {-20037508.34789244, 20037508.34789244};

    //array indexes for that data
    private static final int ORIG_X = 0;
    private static final int ORIG_Y = 1;

    // Size of square world map in meters, using WebMerc projection.
    private static final double MAP_SIZE = 20037508.34789244 * 2;

    // array indexes for array to hold bounding boxes.
    protected static final int MINX = 0;
	protected static final int MINY = 1;
    protected static final int MAXX = 2;
    protected static final int MAXY = 3;
	
    // Construct with tile size in pixels, normally 256, see parent class
    private WMSTileProvider(int x, int y, String urlFormat, double minx, double miny, double maxx, double maxy)
	{
    	super(x, y);
		this.tileWidth = x;
		this.tileHeight = y;
		this.urlFormat = urlFormat;
		this.minxMap = minx;
		this.minyMap = miny;
		this.maxxMap = maxx;
		this.maxyMap = maxy;
    }

	// return a wms tile provider
	public static TileProvider getTileProvider(int x, int y, String urlFormat, double minx, double miny, double maxx, double maxy)
	{
		return new WMSTileProvider(x, y, urlFormat, minx, miny, maxx, maxy);
	}

	@Override
	public synchronized URL getTileUrl(int x, int y, int zoom)
	{
		double[] bbox = getBoundingBox(x, y, zoom);
		String s = String.format(
			Locale.US, 
			urlFormat, 
			bbox[MINX],
			bbox[MINY], 
			bbox[MAXX], 
			bbox[MAXY],
			tileWidth,
			tileHeight);

		if (!tileExists(bbox))
		{
			return null;
		}

		try
		{
			//Log.i("HermLog", "Url van tegel: " + new URL(s).toString());
			return new URL(s);
		}
		catch (MalformedURLException e)
		{
			throw new AssertionError(e);
		}
	}

	private boolean tileExists(double[] bbox)
	{
		if ((bbox[MINX] > maxxMap) ||
			(bbox[MAXX] < minxMap) ||
			(bbox[MINY] > maxyMap) ||
			(bbox[MAXY] < minyMap))
			return false;
		else
			return true;
	}

    // Return a web Mercator bounding box given tile x/y indexes and a zoom
    // level.
    protected double[] getBoundingBox(int x, int y, int zoom)
	{
    	double tileSize = MAP_SIZE / Math.pow(2, zoom);
    	double minx = TILE_ORIGIN[ORIG_X] + x * tileSize;
		double miny = TILE_ORIGIN[ORIG_Y] - (y + 1) * tileSize;
    	double maxx = TILE_ORIGIN[ORIG_X] + (x + 1) * tileSize;
    	double maxy = TILE_ORIGIN[ORIG_Y] - y * tileSize;

    	double[] bbox = new double[4];
    	bbox[MINX] = minx;
    	bbox[MINY] = miny;
    	bbox[MAXX] = maxx;
    	bbox[MAXY] = maxy;

		//Log.i("HermLog", "bbox array.toString(): " + Arrays.toString(bbox));
		//Log.i("HermLog", "bbox array per element: " + bbox[MINX] + " > " + MAXX_MAP + " " + bbox[MINY] + " " + bbox[MAXX] + " " + bbox[MAXY]);

    	return bbox;
    }
}
