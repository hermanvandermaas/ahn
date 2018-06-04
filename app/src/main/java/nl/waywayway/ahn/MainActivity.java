package nl.waywayway.ahn;

import android.*;
import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.graphics.*;
import android.location.*;
import android.net.*;
import android.os.*;
import android.support.v4.app.*;
import android.support.v4.content.*;
import android.support.v4.widget.*;
import android.support.v7.app.*;
import android.support.v7.widget.*;
import android.util.*;
import android.view.*;
import android.view.View.*;
import android.view.animation.*;
import android.widget.*;
import com.google.android.gms.common.*;
import com.google.android.gms.common.api.*;
import com.google.android.gms.location.*;
import com.google.android.gms.location.places.*;
import com.google.android.gms.location.places.ui.*;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import java.io.*;
import java.net.*;
import java.util.*;

import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.support.v4.view.*;

public class MainActivity extends AppCompatActivity
implements 
GoogleMap.OnCameraIdleListener, 
OnMapReadyCallback,
GoogleMap.OnMapClickListener,
GoogleApiClient.OnConnectionFailedListener,
GoogleMap.OnMyLocationButtonClickListener,
ActivityCompat.OnRequestPermissionsResultCallback,
TaskFragment.TaskCallbacks,
LayersRecyclerViewAdapter.AdapterCallbacks
{
	private static final String TAG_TASK_FRAGMENT = "task_fragment";
	private static final String FILES_AUTHORITY = "nl.waywayway.ahn.fileprovider";
	private static final String SHARE_IMAGE_PATH = "/hoogte.png";
	private boolean dialogPlayServicesWasShowed = false;
	private boolean dialogWelcomeWasShowed = false;
	private boolean notConnectedMessageWasShowed = false;
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
	private View legend;
	private GestureDetectorCompat swipeRightGestureDetector;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private boolean permissionAsked = false;
	private static final String PERMISSION_ASKED_STATE_KEY = "permission_asked_state_key";
	private static final String WELCOME_DIALOG_SHOWED_STATE_KEY = "welcome_dialog_showed_state_key";
	private static final String NOT_CONNECTED_STATE_KEY = "not_connected_state_key";
	private boolean myLocationIconVisible;
	private boolean searchBarVisible = true;
	private boolean legendVisible = false;
	private String SEARCHBAR_VISIBLE_KEY = "search_bar_visible_key";
	private String LEGEND_VISIBLE_KEY = "legend_visible_key";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

		// Initialiseer
		context = this;
		savedInstanceStateGlobal = savedInstanceState;
		drawerLayout = findViewById(R.id.drawer_layout);
		searchBar = findViewById(R.id.card_place_autocomplete_fragment);
		legend = findViewById(R.id.legend_scrollview);
		layerList = JsonToArrayList.makeArrayList(context.getResources().openRawResource(R.raw.layers));
		//testLayerSettings();

		showOnboardingScreenAtFirstRun();

		// Herstel savedInstanceState
		if (savedInstanceState != null)
		{
			permissionAsked = savedInstanceState.getBoolean(PERMISSION_ASKED_STATE_KEY);
			searchBarVisible = savedInstanceState.getBoolean(SEARCHBAR_VISIBLE_KEY);
			dialogWelcomeWasShowed = savedInstanceState.getBoolean(WELCOME_DIALOG_SHOWED_STATE_KEY);
			notConnectedMessageWasShowed = savedInstanceState.getBoolean(NOT_CONNECTED_STATE_KEY);
			legendVisible = savedInstanceState.getBoolean(LEGEND_VISIBLE_KEY);
		}

		initializeLegend();

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

	private void showNotConnectedMessage()
	{
		if (!isNetworkConnected() && !notConnectedMessageWasShowed)
		{
			Toast.makeText(context, getResources().getString(R.string.not_connected_message), Toast.LENGTH_SHORT).show();
			notConnectedMessageWasShowed = true;
		}
	}

	private void initializeLegend()
	{
		if (legendVisible)
		{
			showLegend(View.VISIBLE);
			legendVisible = true;
		}

		swipeRightGestureDetector = new GestureDetectorCompat(this, new SwipeRightGestureListener()
			{
				@Override
				public void onSwipeRight()
				{
					if (legend.getVisibility() == View.VISIBLE)
					{
						showLegend(View.INVISIBLE);
						legendVisible = false;
						Animation slideRight = AnimationUtils.loadAnimation(context, R.anim.legend_slide_right);
						legend.startAnimation(slideRight);
					}
				}
			});

		legend.setOnTouchListener(new OnTouchListener()
			{
				@Override
				public boolean onTouch(View view, MotionEvent motionEvent)
				{
					swipeRightGestureDetector.onTouchEvent(motionEvent);
					return false;
				}
			});
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

		// Toon icoon alleen als nodig
		MenuItem myLocationIcon = menu.findItem(R.id.action_myposition);
		myLocationIcon.setVisible(myLocationIconVisible);

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

			case R.id.action_myposition:
				zoomToCurrentLocation();

				return true;

			case R.id.action_legend:
				if (legend.getVisibility() == View.VISIBLE)
				{
					showLegend(View.INVISIBLE);
					legendVisible = false;
					Animation slideRight = AnimationUtils.loadAnimation(this, R.anim.legend_slide_right);
					legend.startAnimation(slideRight);
				}
				else
				{
					showLegend(View.VISIBLE);
					legendVisible = true;
					Animation slideLeft = AnimationUtils.loadAnimation(this, R.anim.legend_slide_left);
					legend.startAnimation(slideLeft);
				}

				return true;

			case R.id.action_share_map:
				makeImage();

				return true;

			case R.id.action_search:
				if (searchBar.getVisibility() == View.VISIBLE)
				{
					showSearchBar(View.GONE);
					searchBarVisible = false;
				}
				else
				{
					showSearchBar(View.VISIBLE);
					searchBarVisible = true;
				}

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
		uiSettings.setMapToolbarEnabled(true);
		//uiSettings.setTiltGesturesEnabled(true);

		// Zoom in op Nederland bij eerste opstart app
		if (savedInstanceStateGlobal == null)
			googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(nederland.getCenter(), 7));

		// Kaartlagen worden in RecyclerView adapter toegevoegd
		//createLayers();
		createLayerMenu();
		createGoogleApiClient();
		createPlaceSearch();

		googleMap.setOnCameraIdleListener(this);
		gMap.setOnMapClickListener(this);
		gMap.setInfoWindowAdapter(new CustomInfoWindowAdapter(context));
		//gMap.setOnMyLocationButtonClickListener(this);
        enableMyLocation();
    }

	@Override
    public boolean onMyLocationButtonClick()
	{
        //Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();

        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }

	private void zoomToCurrentLocation()
	{
		Location lastLocation = LocationServices.FusedLocationApi
			.getLastLocation(googleApiClient);

		if (lastLocation != null)
		{
			double lat = lastLocation.getLatitude();
			double lon = lastLocation.getLongitude();
			gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lon), 15));
		}
		else
			Toast.makeText(this, getResources().getString(R.string.device_location_not_available_message), Toast.LENGTH_SHORT).show();
	}

	private void makeImage()
	{
		gMap.snapshot(new GoogleMap.SnapshotReadyCallback()
			{
				@Override
				public void onSnapshotReady(Bitmap bitmap)
				{

					File file = new File(context.getCacheDir() + SHARE_IMAGE_PATH);
					FileOutputStream fileOut = null;

					try
					{
						fileOut = new FileOutputStream(file);
					}
					catch (FileNotFoundException e)
					{
						e.printStackTrace();
					}

					if (fileOut != null) bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOut);

					try
					{
						fileOut.close();
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}

					shareImage(file);
				}
			});
	}

	// Deel afbeelding via andere app
	private void shareImage(File imageFile)
	{

		if (imageFile == null)
		{
			Toast.makeText(context, getResources().getString(R.string.no_image_for_sharing_available_message), Toast.LENGTH_SHORT).show();
			return;
		}

		Uri uriToImage = FileProvider.getUriForFile(context, FILES_AUTHORITY, imageFile);

		Intent shareIntent = ShareCompat.IntentBuilder.from(MainActivity.this)
			.setStream(uriToImage)
			.getIntent();

		shareIntent.setData(uriToImage);
		shareIntent.setType(getResources().getString(R.string.share_image_mime_type));
		shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

		if (shareIntent.resolveActivity(getPackageManager()) != null)
		{
			startActivity(shareIntent);
		}
	}

	@Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
	{
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE)
		{
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults, Manifest.permission.ACCESS_FINE_LOCATION))
		{
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
        }
    }

    private void enableMyLocation()
	{
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
			!= PackageManager.PERMISSION_GRANTED)
		{
			// Niet nog eens toestemming vragen na schermrotatie
			if (permissionAsked) return;

            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE, Manifest.permission.ACCESS_FINE_LOCATION, false);
			permissionAsked = true;
        }
		else if (gMap != null)
		{
            // Access to the location has been granted to the app.
            gMap.setMyLocationEnabled(true);
			//setMapPadding();
			gMap.getUiSettings().setMyLocationButtonEnabled(false);
			showMyLocationIcon(true);
        }
    }

	private void setMapPadding()
	{
		final View toolbarAndSearchFragment = findViewById(R.id.toolbar_and_progressbar);

		toolbarAndSearchFragment.post(new Runnable()
			{
				@Override
				public void run()
				{
					toolbarAndSearchFragment.getHeight();
					int paddingTop = toolbarAndSearchFragment.getHeight();
					// setPadding: left, top, right, bottom
					gMap.setPadding(0, paddingTop, 0, 0);
					//Log.i("HermLog", "paddingTop: " + paddingTop);
				}
			});
	}

	private void createPlaceSearch()
	{
		PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
			getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

		if (!searchBarVisible) showSearchBar(View.GONE);

		// Alleen in Nederland zoeken
		AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
			.setCountry(getResources().getString(R.string.country_code))
			.build();

		autocompleteFragment.setFilter(typeFilter);

		// Zoom naar gekozen Place
		autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener()
			{
				@Override
				public void onPlaceSelected(Place place)
				{
					showSearchBar(View.GONE);
					searchBarVisible = false;
					gMap.animateCamera(CameraUpdateFactory.newLatLngBounds(place.getViewport(), 0));
					//Toast.makeText(context, "Place: " + place.getName(), Toast.LENGTH_SHORT).show();
					//Log.i("HermLog", "Place: " + place.getName());
				}

				@Override
				public void onError(Status status)
				{
					//Toast.makeText(context, "An error occurred: " + status, Toast.LENGTH_SHORT).show();
					//Log.i("HermLog", "PlaceSelectionListener fout: " + status);
				}
			});
	}

	private void createGoogleApiClient()
	{

		googleApiClient = new GoogleApiClient
			.Builder(this)
			.addApi(Places.GEO_DATA_API)
			.addApi(Places.PLACE_DETECTION_API)
			.addApi(LocationServices.API)
			.enableAutoManage(this, this)
			.build();
	}

	public TileOverlay createLayer(LayerItem layerItem)
	{
		// Maak TileOverlay, 
		// zIndex is gelijk aan ID van de laag
		// hoogste zIndex ligt bovenop
		float zIndex = Float.parseFloat(layerItem.getID());
		TileOverlay tileOverlay = gMap.
			addTileOverlay(new TileOverlayOptions().zIndex(zIndex).tileProvider(
							   WMSTileProvider.getTileProvider(
								   256, 
								   256, 
								   layerItem.getServiceUrl(), 
								   layerItem.getMinx(), 
								   layerItem.getMiny(), 
								   layerItem.getMaxx(), 
								   layerItem.getMaxy()
							   )));

		// Zet referentie naar kaartlaag in lijst
		//Log.i("HermLog", "getServiceUrl: " + layerItem.getServiceUrl());

		layerItem.setLayerObject(tileOverlay);

		return tileOverlay;
	}

	// RecyclerView
	private void createLayerMenu()
	{
		RecyclerView recyclerView = findViewById(R.id.layers_recycler_view);
		//Log.i("HermLog", "recyclerView: " + recyclerView);
		LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
		recyclerView.setLayoutManager(linearLayoutManager);
		LayersRecyclerViewAdapter adapter = new LayersRecyclerViewAdapter(context, layerList);
		recyclerView.setAdapter(adapter);

		// Plaats titel van lagenmenu beneden status bar
		TextView layersTitle = findViewById(R.id.layers_title);
		LinearLayout.LayoutParams layoutParams =  (LinearLayout.LayoutParams) layersTitle.getLayoutParams();
		layoutParams.topMargin = getStatusBarHeight();
		layersTitle.setLayoutParams(layoutParams);
	}

	@Override
	public void onBackPressed()
	{
		if (drawerLayout.isDrawerOpen(Gravity.RIGHT))
		{
			drawerLayout.closeDrawer(Gravity.RIGHT);
		}
		else if (legend.getVisibility() == View.VISIBLE)
		{
			showLegend(View.INVISIBLE);
			legendVisible = false;
			Animation slideRight = AnimationUtils.loadAnimation(this, R.anim.legend_slide_right);
			legend.startAnimation(slideRight);
		}
		else
		{
			super.onBackPressed();
		}
	}

	@Override
	public void onConnectionFailed(ConnectionResult p1)
	{
		Toast.makeText(context, getResources().getString(R.string.place_search_not_available_message), Toast.LENGTH_SHORT).show();
	}

	// Check beschikbaarheid Play Services
	protected void isPlayServicesAvailable()
	{
		if (dialogPlayServicesWasShowed) return;

		GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
		int resultCode = apiAvailability.isGooglePlayServicesAvailable(context);

		if (resultCode != ConnectionResult.SUCCESS)
		{
			//Log.i("HermLog", "Play Services fout");
			if (apiAvailability.isUserResolvableError(resultCode))
			{
				apiAvailability.getErrorDialog((Activity) context, resultCode, 9000).show();
				dialogPlayServicesWasShowed = true;
			}
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
			Toast.makeText(context, getResources().getString(R.string.not_connected_message), Toast.LENGTH_SHORT).show();
			return;
		}

		// Download van data wordt uitgevoerd in TaskFragment instance
		// verwerking van data in de marker infowindow wordt gedaan in
		// Task Callback Methods in MainActivity
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
		ArrayList<URL> urls = new ArrayList<URL>();
		ArrayList<String> shortTitles = new ArrayList<String>();

		for (LayerItem layerItem : layerList)
		{
			if (layerItem.isQueryable())
			{
				URL url = WMSGetMapFeatureUrlMaker.getUrlMaker(256, 256, pointLatLong, zoomLevel, layerItem).makeUrl();
				//Log.i("HermLog", "url: " + url);
				urls.add(url);
				shortTitles.add(layerItem.getShortTitle());
			}
		}

		taskFragment.setLayerInfoList(shortTitles);
		taskFragment.start(urls);
	}

	// Hoogste zichtbare laag
	private LayerItem getTopVisibleLayer()
	{
		LayerItem returnLayerItem = null;

		for (LayerItem layerItem : layerList)
		{
			if (layerItem.isQueryable() == false) continue;

			int[] preferences = LayersSaveAndRestore.getInstance(context, layerItem.getID()).restore();

			if (preferences == null)
			{
				boolean visible = layerItem.isVisibleByDefault();
				int opacity = layerItem.getOpacityDefault();
				if (visible && opacity > 0) returnLayerItem = layerItem;
			}
			else
			{
				boolean visible = preferences[0] == 1 ? true : false;
				int opacity = preferences[1];
				if (visible && opacity > 0) returnLayerItem = layerItem;
			}
		}

		return returnLayerItem;
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
		View searchBarCard = findViewById(R.id.card_place_autocomplete_fragment);
		searchBarCard.setVisibility(visibility);
	}

	// int visibility is View.VISIBLE, View.GONE of View.INVISIBLE
	private void showMyLocationIcon(boolean showIcon)
	{
		myLocationIconVisible = showIcon;
		invalidateOptionsMenu();
	}

	// int visibility is View.VISIBLE, View.GONE of View.INVISIBLE
	private void showLegend(int visibility)
	{
		View legend = findViewById(R.id.legend_scrollview);
		legend.setVisibility(visibility);
	}

	private void showOnboardingScreenAtFirstRun()
	{
		SharedPreferences sharedPref = context.getSharedPreferences(getResources().getString(R.string.SHARED_PREFERENCES_FILENAME), context.MODE_PRIVATE);
		boolean prefDefault = true;
		boolean showOnBoardingScreen = sharedPref.getBoolean(getResources().getString(R.string.PREFERENCES_KEY_SHOW_ONBOARDING_SCREEN), prefDefault);

		if (showOnBoardingScreen)
		{
			// Start activity
			Intent mIntent = new Intent(context, OnBoardingScreenActivity.class);
			context.startActivity(mIntent);
			this.finish();
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		outState.putBoolean(PERMISSION_ASKED_STATE_KEY, permissionAsked);
		outState.putBoolean(SEARCHBAR_VISIBLE_KEY, searchBarVisible);
		outState.putBoolean(WELCOME_DIALOG_SHOWED_STATE_KEY, dialogWelcomeWasShowed);
		outState.putBoolean(NOT_CONNECTED_STATE_KEY, notConnectedMessageWasShowed);
		outState.putBoolean(LEGEND_VISIBLE_KEY, legendVisible);

		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		if (taskFragment.isRunning()) taskFragment.cancel();
		//Log.i("HermLog", "onDestroy()");
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		//Log.i("HermLog", "onResume()");

		isPlayServicesAvailable();
	}

	@Override
	protected void onStart()
	{
		super.onStart();
		//Log.i("HermLog", "onStart()");
		showNotConnectedMessage();
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
		//Log.i("HermLog", "onCancelled()");
		showProgressBar(View.GONE);
	}

	@Override
	public void onPostExecute(ArrayList<String> result, ArrayList<String> layerInfo)
	{
		showProgressBar(View.GONE);

		// Toon hoogte bij marker
		if (result == null)
		{
			Toast.makeText(context, getResources().getString(R.string.download_error_message), Toast.LENGTH_SHORT).show();
		}
		else
		{

			String snippet = "";

			for (int i = 0; i < result.size(); i++)
			{
				String affix = result.get(i).equals(getResources().getString(R.string.not_available_UI)) ? "" : " " + getResources().getString(R.string.unit_of_measurement_UI);
				String newLine = (i == (result.size() - 1)) ? "" : "\n";
				String line = layerInfo.get(i) + ": " + result.get(i) + affix + newLine;
				snippet = snippet + line;
			}

			Marker marker = markerList.get(0);
			marker.setTitle("Hoogte");
			marker.setSnippet(snippet);
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
