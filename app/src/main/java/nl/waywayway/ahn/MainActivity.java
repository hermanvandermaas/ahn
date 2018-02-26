package nl.waywayway.ahn;

import android.app.*;
import android.content.*;
import android.os.*;
import android.support.v7.app.*;
import android.support.v7.widget.*;
import android.util.*;
import android.view.*;
import com.google.android.gms.common.*;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;

import android.support.v7.app.ActionBar;

public class MainActivity extends AppCompatActivity
implements OnMapReadyCallback
{
	private boolean dialogWasShowed = false;
	private Context context;
	private Bundle savedInstanceStateGlobal;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

		context = this;
		savedInstanceStateGlobal = savedInstanceState;

		MapFragment mapFragment = (MapFragment) getFragmentManager()
            .findFragmentById(R.id.map);
		mapFragment.getMapAsync(this);

		makeToolbar();
		setTransparentStatusBar();
    }

	// Maak toolbar
	private void makeToolbar()
	{
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		ActionBar actionBar = getSupportActionBar();
		toolbar.setPadding(0, getStatusBarHeight(), 0, 0);
	}

	@Override
	public void onMapReady(final GoogleMap googleMap)
	{
		// Zoom in op Nederland bij eerste opstart app
		if (savedInstanceStateGlobal == null)
		{
			final LatLngBounds nederland = new LatLngBounds(new LatLng(50.75, 3.2), new LatLng(53.7, 7.22));
			findViewById(R.id.map).post(new Runnable()
				{
					@Override
					public void run()
					{
						googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(nederland, 1));
					}
				});
		}
		
		TileOverlay ahn2 = googleMap.addTileOverlay(new TileOverlayOptions()
													  .tileProvider(tileProvider));
    }
	
	TileProvider tileProvider = new UrlTileProvider(256, 256) {
		@Override
		public synchronized URL getTileUrl(int x, int y, int zoom) {
			// The moon tile coordinate system is reversed.  This is not normal.
			int reversedY = (1 << zoom) - y - 1;
			String s = String.format(Locale.US, MOON_MAP_URL_FORMAT, zoom, x, reversedY);
			URL url = null;
			try {
				url = new URL(s);
			} catch (MalformedURLException e) {
				throw new AssertionError(e);
			}
			return url;
		}
	};
	
	private boolean checkTileExists(int x, int y, int zoom) {
		int minZoom = 12;
		int maxZoom = 16;

		if ((zoom < minZoom || zoom > maxZoom)) {
			return false;
		}

		return true;
	}

	// Check beschikbaarheid Play Services
	protected void isPlayServicesAvailable()
	{
		if (dialogWasShowed) return;

		GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
		int resultCode = apiAvailability.isGooglePlayServicesAvailable(context);

		if (resultCode != ConnectionResult.SUCCESS)
		{
			Log.i("HermLog", "Play Services fout");
			if (apiAvailability.isUserResolvableError(resultCode))
			{
				apiAvailability.getErrorDialog((Activity) context, resultCode, 9000).show();
				dialogWasShowed = true;
			}
		}
	}

	private void setTransparentStatusBar()
	{
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
		{
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
	}

	private int getStatusBarHeight()
	{
		int result = 0;
		int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
		if (resourceId > 0)
		{
			result = getResources().getDimensionPixelSize(resourceId);
		}
		return result;
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		Log.i("HermLog", "onResume()");

		// Check beschikbaarheid Google Play services
		isPlayServicesAvailable();
	}
}
