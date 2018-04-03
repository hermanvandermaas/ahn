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
import com.google.android.gms.common.api.*;
import com.google.android.gms.location.places.*;
import com.google.android.gms.location.places.ui.*;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import java.net.*;
import java.util.*;

import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;

public class MainActivity extends AppCompatActivity implements 
GoogleMap.OnCameraIdleListener, 
OnMapReadyCallback,
GoogleMap.OnMapClickListener,
GoogleApiClient.OnConnectionFailedListener,
TaskFragment.TaskCallbacks
{
	private static final String TAG_TASK_FRAGMENT = "task_fragment";
	private boolean dialogWasShowed = false;
	private Context context;
	private Bundle savedInstanceStateGlobal;
	private GoogleMap gMap;
	private GoogleApiClient googleApiClient;
	private ArrayList<Marker> markerList = new ArrayList<Marker>();
	private ArrayList<LayerItem> layerList;
	private final LatLngBounds nederland = new LatLngBounds(new LatLng(50.75, 3.2), new LatLng(53.7, 7.22));
	private TaskFragment taskFragment;
	private float zoomLevel;
	private DrawerLayout drawerLayout;
	private View searchBar;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

		// Initialiseer
		context = this;
		savedInstanceStateGlobal = savedInstanceState;
		drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		searchBar = findViewById(R.id.place_autocomplete_fragment);
		layerList = JsonToArrayList.makeArrayList(context.getResources().openRawResource(R.raw.layers));
		//testLayerSettings();

		if (!isNetworkConnected()) Toast.makeText(context, "Geen netwerkverbinding: sommige functies werken niet", Toast.LENGTH_SHORT).show();

		// Handler voor worker fragment
		FragmentManager fm = getSupportFragmentManager();
		taskFragment = (TaskFragment) fm.findFragmentByTag(TAG_TASK_FRAGMENT);

		// If the Fragment is non-null, it is being retained
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
			case R.id.action_layer_menu:
				if (drawerLayout.isDrawerOpen(Gravity.RIGHT))
					drawerLayout.closeDrawer(Gravity.RIGHT);
				else
					drawerLayout.openDrawer(Gravity.RIGHT);

                return true;

			case R.id.action_search:
				if (searchBar.getVisibility() == View.VISIBLE)
					showSearchBar(View.GONE);
				else
					showSearchBar(View.VISIBLE);
					
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
			googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(nederland.getCenter(), 7));
		}

		createLayers();
		createLayerMenu();
		createGoogleApiClient();
		createPlaceSearch();

		gMap.setOnMapClickListener(this);
    }

	private void createPlaceSearch()
	{
		PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
			getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
			
		// Alleen in Nederland zoeken
		AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
			.setCountry("NL")
			.build();

		autocompleteFragment.setFilter(typeFilter);

		// Zoom naar gekozen Place
		autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener()
			{
				@Override
				public void onPlaceSelected(Place place)
				{
					showSearchBar(View.GONE);
					gMap.animateCamera(CameraUpdateFactory.newLatLngBounds(place.getViewport(), 0));
					//Toast.makeText(context, "Place: " + place.getName(), Toast.LENGTH_SHORT).show();
					//Log.i("HermLog", "Place: " + place.getName());
				}

				@Override
				public void onError(Status status)
				{
					//Toast.makeText(context, "An error occurred: " + status, Toast.LENGTH_SHORT).show();
					Log.i("HermLog", "PlaceSelectionListener fout: " + status);
				}
			});
	}

	private void createGoogleApiClient()
	{

		googleApiClient = new GoogleApiClient
			.Builder(this)
			.addApi(Places.GEO_DATA_API)
			.addApi(Places.PLACE_DETECTION_API)
			.enableAutoManage(this, this)
			.build();
	}

	// Maak kaartlagen en zet in ArrayList
	private void createLayers()
	{
		for (LayerItem layerItem : layerList)
		{
			// Maak TileOverlay
			TileOverlay tileOverlay = gMap.
				addTileOverlay(new TileOverlayOptions().tileProvider(
					WMSTileProvider.getTileProvider(
						256, 
						256, 
						layerItem.getServiceUrl(), 
						layerItem.getMinx(), 
						layerItem.getMiny(), 
						layerItem.getMaxx(), 
						layerItem.getMaxy()
				)));

			layerItem.setLayerObject(tileOverlay);
		}
	}

	// RecyclerView
	private void createLayerMenu()
	{
		RecyclerView recyclerView = (RecyclerView) findViewById(R.id.layers_recycler_view);
		//Log.i("HermLog", "recyclerView: " + recyclerView);
		LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
		recyclerView.setLayoutManager(linearLayoutManager);
		LayersRecyclerViewAdapter adapter = new LayersRecyclerViewAdapter(context, layerList);
		recyclerView.setAdapter(adapter);

		// Plaats titel van lagenmenu onder status bar
		TextView layersTitle = (TextView) findViewById(R.id.layers_title);
		LinearLayout.LayoutParams layoutParams =  (LinearLayout.LayoutParams) layersTitle.getLayoutParams();
		layoutParams.topMargin = getStatusBarHeight();
		layersTitle.setLayoutParams(layoutParams);
	}

	@Override
	public void onConnectionFailed(ConnectionResult p1)
	{
		Toast.makeText(context, "Geen verbinding met locatiezoeker, functie werkt momenteel niet", Toast.LENGTH_SHORT).show();
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
		URL url = WMSGetMapFeatureUrlMaker.getUrlMaker(256, 256, pointLatLong, zoomLevel, layerList.get(0)).makeUrl();
		URL[] urls = new URL[]{url};
		taskFragment.start(urls);
	}

	// int visibility is View.VISIBLE, View.GONE of View.INVISIBLE
	private void showProgressBar(int visibility)
	{
		View progressbar = findViewById(R.id.progressbar);
		progressbar.setVisibility(visibility);
	}

	// int visibility is View.VISIBLE, View.GONE of View.INVISIBLE
	private void showSearchBar(int visibility)
	{
		View searchBar = findViewById(R.id.place_autocomplete_fragment);
		searchBar.setVisibility(visibility);
	}
	
	@Override
	public void onNewIntent(Intent intent)
	{
		setIntent(intent);
		if (Intent.ACTION_SEARCH.equals(intent.getAction()))
		{
			String query = intent.getStringExtra(SearchManager.QUERY);
			//now you can display the results
		}  
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

	@Override
	protected void onStart()
	{
		super.onStart();
		Log.i("HermLog", "onStart()");
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

	private void testLayerSettings()
	{
		LayerItem item = layerList.get(0);
		LayersSaveAndRestore.getInstance(context, item.getID()).save(0, 23);
		LayersSaveAndRestore.getInstance(context, item.getID()).restore();
	}
}
