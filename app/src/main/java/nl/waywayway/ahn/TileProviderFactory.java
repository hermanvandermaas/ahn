package nl.waywayway.ahn;

import com.google.android.gms.maps.model.*;
import java.net.*;
import java.util.*;

public class TileProviderFactory
{
	public static getTileProvider()
	{



		TileProvider tileProvider = new UrlTileProvider(256, 256) {
			@Override
			public synchronized URL getTileUrl(int x, int y, int zoom)
			{
				// The moon tile coordinate system is reversed.  This is not normal.
				int reversedY = (1 << zoom) - y - 1;
				String s = String.format(Locale.US, MOON_MAP_URL_FORMAT, zoom, x, reversedY);
				URL url = null;
				try
				{
					url = new URL(s);
				}
				catch (MalformedURLException e)
				{
					throw new AssertionError(e);
				}
				return url;
			}
		};
	}

	private boolean checkTileExists(int x, int y, int zoom)
	{
		int minZoom = 12;
		int maxZoom = 16;

		if ((zoom < minZoom || zoom > maxZoom))
		{
			return false;
		}

		return true;
	}



}
