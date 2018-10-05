package nl.waywayway.ahn;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.Arrays;

public class Test
{
	public void testProjection()
	{
		int[] test;

		Log.i("HermLog", "latLngToXYpixels() lat -85.05112878, lon -180, zoom 3: ");
		test = ProjectionWM.latLngToXYpixels(new LatLng(-85.05112878, -180), 3);
		Log.i("HermLog", "getTileCoordinates(): " + Arrays.toString(ProjectionWM.getTileCoordinates(test)));

		Log.i("HermLog", "latLngToXYpixels() lat 85.05112878, lon -180, zoom 3: ");
		test = ProjectionWM.latLngToXYpixels(new LatLng(85.05112878, -180), 3);
		Log.i("HermLog", "getTileCoordinates(): " + Arrays.toString(ProjectionWM.getTileCoordinates(test)));

		Log.i("HermLog", "latLngToXYpixels() lat -85.05112878, lon 179.99999999, zoom 3: ");
		test = ProjectionWM.latLngToXYpixels(new LatLng(-85.05112878, 179.99999999), 3);
		Log.i("HermLog", "getTileCoordinates(): " + Arrays.toString(ProjectionWM.getTileCoordinates(test)));

		Log.i("HermLog", "latLngToXYpixels() lat 0, lon 0, zoom 3: ");
		test = ProjectionWM.latLngToXYpixels(new LatLng(0, 0), 3);
		Log.i("HermLog", "getTileCoordinates(): " + Arrays.toString(ProjectionWM.getTileCoordinates(test)));

		Log.i("HermLog", "latLngToXYpixels() lat 85.05112878, lon 179.99999999, zoom 3: ");
		test = ProjectionWM.latLngToXYpixels(new LatLng(85.05112878, 179.99999999), 3);
		Log.i("HermLog", "getTileCoordinates(): " + Arrays.toString(ProjectionWM.getTileCoordinates(test)));

		Log.i("HermLog", "latLngToXYmeters() lat -85.05112878, lon -180, zoom 3: ");
		ProjectionWM.latLngToXYmeters(new LatLng(-85.05112878, -180), 3);

		Log.i("HermLog", "latLngToXYmeters() lat 85.05112878, lon -180, zoom 3: ");
		ProjectionWM.latLngToXYmeters(new LatLng(85.05112878, -180), 3);

		Log.i("HermLog", "latLngToXYmeters() lat -85.05112878, lon 179.99999999, zoom 3: ");
		ProjectionWM.latLngToXYmeters(new LatLng(-85.05112878, 179.99999999), 3);

		Log.i("HermLog", "latLngToXYmeters() lat 85.05112878, lon 179.99999999, zoom 3: ");
		ProjectionWM.latLngToXYmeters(new LatLng(85.05112878, 179.99999999), 3);

		Log.i("HermLog", "latLngToXYmeters() lat 0, lon 0, zoom 3: ");
		ProjectionWM.latLngToXYmeters(new LatLng(0, 0), 3);
	}
}
