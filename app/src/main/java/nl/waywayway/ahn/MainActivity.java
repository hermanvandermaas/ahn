package nl.waywayway.ahn;

import android.app.*;
import android.content.*;
import android.net.*;
import android.os.*;
import android.support.v4.app.*;
import android.support.v4.widget.*;
import android.support.v7.app.*;
import android.support.v7.widget.*;
import android.util.*;
import android.view.*;
import android.widget.*;
import com.google.android.gms.common.*;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import java.net.*;
import java.util.*;

import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;

public class MainActivity extends AppCompatActivity implements 
	GoogleMap.OnCameraIdleListener, 
	OnMapReadyCallback,
	GoogleMap.OnMapClickListener,
	TaskFragment.TaskCallbacks
{
	private static final String TAG_TASK_FRAGMENT = "task_fragment";
	private boolean dialogWasShowed = false;
	private Context context;
	private Bundle savedInstanceStateGlobal;
	private GoogleMap gMap;
	private ArrayList<Marker> markerList = new ArrayList<Marker>();
	private static final String MARKER_LIST = "marker_list";
	private final LatLngBounds nederland = new LatLngBounds(new LatLng(50.75, 3.2), new LatLng(53.7, 7.22));
	private TaskFragment taskFragment;
	private float zoomLevel;
	private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

		context = this;
		savedInstanceStateGlobal = savedInstanceState;

		if (!isNetworkConnected()) Toast.makeText(context, "Geen netwerkverbinding: sommige functies werken niet", Toast.LENGTH_SHORT).show();
		
		// Handler voor worker fragment
		FragmentManager fm = getSupportFragmentManager();
		taskFragment = (TaskFragment) fm.findFragmentByTag(TAG_TASK_FRAGMENT);

		// If the Fragment is non-null, then it is being retained
		// over a configuration change.
		if (taskFragment == null)
		{
			taskFragment = new TaskFragment();
			fm.beginTransaction().add(taskFragment, TAG_TASK_FRAGMENT).commit();
		}
		
		if (taskFragment.isRunning()) taskFragment.cancel();
		
		MapFragment mapFragment = (MapFragment) getFragmentManager()
            .findFragmentById(R.id.map);
		mapFragment.getMapAsync(this);

		makeToolbar();
		//setTransparentStatusBar();
    }

	// Maak toolbar
	private void makeToolbar()
	{
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		ActionBar actionBar = getSupportActionBar();
		// AppCompatResources.getDrawable(context, R.drawable.ic_menu_black_24dp)
		//toolbar.setPadding(0, getStatusBarHeight(), 0, 0);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.

		switch (item.getItemId())
		{
			case R.id.action_layers:
				View itemView = findViewById(R.id.action_layers);
				PopupMenu popup = new PopupMenu(this, itemView);
				popup.inflate(R.menu.menu_layers);
				popup.show();
				return true;
				
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onMapReady(GoogleMap googleMap)
	{
		gMap = googleMap;
		
		// Instellingen basiskaart
		UiSettings uiSettings = googleMap.getUiSettings();
		uiSettings.setCompassEnabled(false);
		uiSettings.setRotateGesturesEnabled(false);
		googleMap.setOnCameraIdleListener(this);
		
		// Zoom in op Nederland bij eerste opstart app
		if (savedInstanceStateGlobal == null)
		{
			googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(nederland.getCenter(), 7));
		}
		
		// Maak TileOverlay
		TileOverlay tileOverlay = googleMap.addTileOverlay(new TileOverlayOptions().tileProvider(WMSTileProvider.getTileProvider(256, 256)));
		
		gMap.setOnMapClickListener(this);
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
	
	private int getStatusBarHeight() {
		int result = 0;
		int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
		if (resourceId > 0) {
			result = getResources().getDimensionPixelSize(resourceId);
		}
		return result;
	}
	
	@Override
    public void onCameraIdle()
	{
		zoomLevel = gMap.getCameraPosition().zoom;
		Log.i("HermLog", "Zoom: " + zoomLevel);
    }
	
	@Override
    public void onMapClick(LatLng point)
	{
		//Toast.makeText(context, "Point: " + point.toString(), Toast.LENGTH_SHORT).show();
		putMarker(point);
    }
	
	private void putMarker(LatLng pointLatLong)
	{
		//Log.i("HermLog", "markerList size(): " + markerList.size());
		
		// Verwijder huidige marker van kaart en uit de lijst met markers
		if (markerList.size() > 0)
		{
			markerList.get(0).remove();
			markerList.remove(0);
		}
		
		// Plaats nieuwe marker op kaart en in de lijst
		markerList.add(markerList.size(), gMap.addMarker(new MarkerOptions().position(pointLatLong)));
		
		// Vraag hoogte op voor punt
		if (!isNetworkConnected())
		{
			Toast.makeText(context, "Geen netwerkverbinding: sommige functies werken niet", Toast.LENGTH_SHORT).show();
			return;
		}
		
		getElevationFromLatLong(pointLatLong);
		
		//testProjection();
		//Toast.makeText(context, "Lat/lon: " + ProjectionWM.xyToLatLng(new double[]{256,256}).toString(), Toast.LENGTH_SHORT).show();
	}
	
	// Netwerkverbinding ja/nee
	private boolean isNetworkConnected()
	{
		ConnectivityManager connMgr = (ConnectivityManager)
			getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		return networkInfo != null && networkInfo.isConnected();
	}
	
	private void getElevationFromLatLong(LatLng pointLatLong)
	{
		if (taskFragment.isRunning()) taskFragment.cancel();
		URL url = WMSGetMapFeatureUrlMaker.getUrlMaker(256, 256, pointLatLong, zoomLevel).makeUrl();
		URL[] urls = new URL[]{url};
		taskFragment.start(urls);
	}
	
	// int showOrNot is View.VISIBLE, View.GONE of View.INVISIBLE
	private void showProgressBar(int visibility)
	{
		View progressbar = findViewById(R.id.progressbar);
		progressbar.setVisibility(visibility);
	}
	
	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		if (taskFragment.isRunning()) taskFragment.cancel();
		Log.i("HermLog", "onDestroy()");
	}
	
	@Override
	protected void onResume()
	{
		super.onResume();
		Log.i("HermLog", "onResume()");

		// Check beschikbaarheid Google Play services
		isPlayServicesAvailable();
	}
	
	/*********************************/
	/***** TASK CALLBACK METHODS *****/
	/*********************************/

	@Override
	public void onPreExecute()
	{
		//Log.i("HermLog", "onPreExecute()");
		showProgressBar(View.VISIBLE);
	}

	@Override
	public void onProgressUpdate(int percent)
	{
	}

	@Override
	public void onCancelled()
	{
		Log.i("HermLog", "onCancelled()");
		showProgressBar(View.GONE);
	}

	@Override
	public void onPostExecute(String result)
	{
		//Log.i("HermLog", "onPostExecute() result: " + result);
		showProgressBar(View.GONE);
		
		// Toon hoogte bij marker
		if (result.equals("Download niet gelukt"))
		{
			Toast.makeText(context, result, Toast.LENGTH_SHORT).show();
		}
		else
		{
			String affix = result.equals("n/a") ? "" : " meter NAP";
			String info = result + affix;
			Marker marker = markerList.get(0);
			marker.setTitle("Hoogte");
			marker.setSnippet(info);
			marker.showInfoWindow();
		}
	}
	
	private void testProjection()
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
