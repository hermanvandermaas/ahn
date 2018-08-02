package nl.waywayway.ahn;

import android.*;
import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.location.*;
import android.os.*;
import android.support.v4.app.*;
import android.support.v4.content.*;
import android.support.v4.view.*;
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
import com.google.android.gms.location.places.*;
import com.google.android.gms.location.places.ui.*;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import java.net.*;
import java.util.*;

import android.support.v4.app.FragmentManager;
import android.support.v7.widget.Toolbar;

public class MainActivity extends AppCompatActivity
implements 
GoogleMap.OnCameraIdleListener, 
OnMapReadyCallback,
GoogleMap.OnMapClickListener,
GoogleApiClient.ConnectionCallbacks,
GoogleApiClient.OnConnectionFailedListener,
GoogleMap.OnMyLocationButtonClickListener,
ActivityCompat.OnRequestPermissionsResultCallback,
TaskFragment.TaskCallbacks,
CancelOrProceedDialogFragment.YesNoDialog,
LayersRecyclerViewAdapter.AdapterCallbacks
{
	private static final String TAG_TASK_FRAGMENT = "task_fragment";
	private static final String TAG_DELETE_LINE_DIALOG = "delete_line";
	private boolean dialogPlayServicesWasShowed = false;
	private boolean notConnectedMessageWasShowed = false;
	private Context context;
	private Toolbar toolbar;
	private Bundle savedInstanceStateGlobal;
	private GoogleMap gMap;
	private GoogleApiClient googleApiClient;
	private LocationProvider locationProvider;
	private ArrayList<Marker> markerList = new ArrayList<Marker>();
	private ArrayList<LayerItem> layerList;
	private final LatLngBounds nederland = new LatLngBounds(new LatLng(50.75, 3.2), new LatLng(53.7, 7.22));
	private LatLng amersfoort = new LatLng(52.155240, 5.387218);
	private TaskFragment taskFragment;
	private float zoomLevel;
	private DrawerLayout drawerLayout;
	private View searchBar;
	private View legend;
	private View elevationProfileMenu;
	private GestureDetectorCompat swipeRightGestureDetector;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private boolean permissionAsked = false;
	private static final String PERMISSION_ASKED_STATE_KEY = "permission_asked_state_key";
	private static final String NOT_CONNECTED_STATE_KEY = "not_connected_state_key";
	private boolean myLocationIconVisible;
	private boolean searchBarVisible = true;
	private boolean legendVisible = false;
	private boolean elevationProfileMenuVisible = false;
	private String SEARCHBAR_VISIBLE_KEY = "search_bar_visible_key";
	private String LEGEND_VISIBLE_KEY = "legend_visible_key";
	private String ELEVATION_PROFILE_MENU_VISIBLE_KEY = "elevation_profile_menu_visible_key";
	private String MODE_KEY = "mode_key";
	private String LINE_VERTICES_LIST_KEY = "line_vertices_list_key";
	private String MARKER_LATLNG_LIST_KEY = "marker_list_key";
	private String MARKER_SNIPPET_KEY = "marker_info_window_key";
	private String SHORT_TITLES_KEY = "short_titles_key";
	private float LINE_Z_INDEX = 500;
	private float DOT_Z_INDEX = 600;
	private String IS_DOT = "isDot";
	private String snippet;
	ArrayList<String> shortTitles = new ArrayList<String>();
	private ArrayList<LatLng> verticesList = new ArrayList<LatLng>();
	private ArrayList<Marker> dotsList = new ArrayList<Marker>();
	private ArrayList<LatLng> markersLatLngList = new ArrayList<LatLng>();
	// Mode.POINT: klik op kaart geeft hoogte van punt, Mode.LINE: klik maakt lijn voor hoogteprofiel
	public enum Mode
	{POINT, LINE};
	private Mode mode = Mode.POINT;
	Polyline line;

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
		elevationProfileMenu = findViewById(R.id.card_elevation_profile_menu);
		layerList = JsonToArrayList.makeArrayList(context.getResources().openRawResource(R.raw.layers));
		locationProvider = initializeZoomToLocation(savedInstanceStateGlobal == null);

		showOnboardingScreenAtFirstRun();

		// Herstel savedInstanceState
		if (savedInstanceState != null)
		{
			permissionAsked = savedInstanceState.getBoolean(PERMISSION_ASKED_STATE_KEY);
			searchBarVisible = savedInstanceState.getBoolean(SEARCHBAR_VISIBLE_KEY);
			notConnectedMessageWasShowed = savedInstanceState.getBoolean(NOT_CONNECTED_STATE_KEY);
			legendVisible = savedInstanceState.getBoolean(LEGEND_VISIBLE_KEY);
			mode = (Mode) savedInstanceState.getSerializable(MODE_KEY);
			verticesList = savedInstanceState.getParcelableArrayList(LINE_VERTICES_LIST_KEY);
			snippet = savedInstanceState.getString(MARKER_SNIPPET_KEY);
			snippet = (snippet == null) ? "" : snippet;
			markersLatLngList = savedInstanceState.getParcelableArrayList(MARKER_LATLNG_LIST_KEY);
			shortTitles = savedInstanceState.getStringArrayList(SHORT_TITLES_KEY);
			//Log.i("HermLog", "savedInstanceState, markersLatLngList: " +  markersLatLngList);
		}

		// Handler voor worker fragment
		FragmentManager fm = getSupportFragmentManager();
		taskFragment = (TaskFragment) fm.findFragmentByTag(TAG_TASK_FRAGMENT);
		if (taskFragment == null)
		{
			taskFragment = new TaskFragment();
			fm.beginTransaction().add(taskFragment, TAG_TASK_FRAGMENT).commit();
		}

		if (taskFragment.isRunning()) showProgressBar(View.VISIBLE);
		initializeLegend();
		initializeElevationProfileMenu();
		createGoogleApiClient();

		MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
		mapFragment.getMapAsync(this);

		makeToolbar();
    }

	// Legenda
	private void initializeLegend()
	{
		if (legendVisible)
		{
			legendVisible = toggleViewVisibility(
				legend,
				AnimationUtils.loadAnimation(this, R.anim.legend_slide_left),
				AnimationUtils.loadAnimation(this, R.anim.legend_slide_right),
				true);
		}

		swipeRightGestureDetector = new GestureDetectorCompat(this, new SwipeGestureListener()
			{
				@Override
				public void onSwipeRight()
				{
					legendVisible = toggleViewVisibility(
						legend,
						AnimationUtils.loadAnimation(context, R.anim.legend_slide_left),
						AnimationUtils.loadAnimation(context, R.anim.legend_slide_right),
						false);
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

	// Menu hoogteprofiel
	private void initializeElevationProfileMenu()
	{
		if (mode == Mode.LINE) elevationProfileMenuVisible = toggleViewVisibility(elevationProfileMenu, null, null, true);

		// Sluit menu
		((ImageView) findViewById(R.id.elevation_profile_close))
			.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					elevationProfileMenuVisible = toggleViewVisibility(
						elevationProfileMenu,
						AnimationUtils.loadAnimation(context, R.anim.elevation_profile_menu_slide_right),
						AnimationUtils.loadAnimation(context, R.anim.elevation_profile_menu_slide_left),
						false);

					switchMode(elevationProfileMenuVisible);
				}
			});

		// Verwijder laatste punt
		((ImageView) findViewById(R.id.elevation_profile_delete_last_point))
			.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					deleteLastPoint();
				}
			});

		// Verwijder alle punten
		((ImageView) findViewById(R.id.elevation_profile_delete_all_points))
			.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					if (dotsList.size() == 0) return;
					CancelOrProceedDialogFragment.newInstance(
						R.string.dialog_confirm_action_body_text,
						R.string.dialog_confirm_action_yes,
						R.string.dialog_confirm_action_no)
						.show(getSupportFragmentManager(), TAG_DELETE_LINE_DIALOG);
				}
			});

		// Maak hoogteprofiel
		((ImageView) findViewById(R.id.elevation_profile_make_profile))
			.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					ElevationProfileMaker.make(verticesList);
				}
			});
	}
	
	@Override
	public void onYes(DialogInterface dialog, int id)
	{
		removeLineAndDots();
		drawLineAndDots();
	}
	
	@Override
	public void onNo(DialogInterface dialog, int id)
	{}
	
	// Maak toolbar
	private void makeToolbar()
	{
		toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar();
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
		switch (item.getItemId())
		{
			case R.id.action_layer_menu:
				if (drawerLayout.isDrawerOpen(Gravity.RIGHT))
					drawerLayout.closeDrawer(Gravity.RIGHT);
				else
					drawerLayout.openDrawer(Gravity.RIGHT);

                return true;

			case R.id.action_myposition:
				permissionAsked = false;
				locationProvider = initializeZoomToLocation(false);
				enableMyLocation(true);

				return true;

			case R.id.action_legend:
				legendVisible = toggleViewVisibility(
					legend,
					AnimationUtils.loadAnimation(this, R.anim.legend_slide_left),
					AnimationUtils.loadAnimation(this, R.anim.legend_slide_right),
					false);

				return true;

			case R.id.action_share_map:
				ShareImage shareImage = new ShareImage(gMap, context);
				shareImage.share();

				return true;

			case R.id.action_info:
				Intent intent = new Intent(context, InformationActivity.class);
				context.startActivity(intent);

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

			case R.id.action_elevation_profile:
				elevationProfileMenuVisible = toggleViewVisibility(
					elevationProfileMenu,
					AnimationUtils.loadAnimation(context, R.anim.elevation_profile_menu_slide_right),
					AnimationUtils.loadAnimation(context, R.anim.elevation_profile_menu_slide_left),
					false);

				if (taskFragment.isRunning()) taskFragment.cancel();
				switchMode(elevationProfileMenuVisible);

				return true;


			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onMapReady(GoogleMap googleMap)
	{
		//Log.i("HermLog", "onMapReady()");
		gMap = googleMap;

		// Instellingen basiskaart
		UiSettings uiSettings = googleMap.getUiSettings();
		uiSettings.setCompassEnabled(false);
		uiSettings.setRotateGesturesEnabled(false);
		uiSettings.setMapToolbarEnabled(true);
		//uiSettings.setTiltGesturesEnabled(true);

		// Kaartlagen worden in RecyclerView adapter toegevoegd
		//createLayers();
		createLayerMenu();
		createPlaceSearch();

		// Herstel markers
		gMap.setInfoWindowAdapter(new CustomInfoWindowAdapter(context));
		markerList = MarkersListToLatLngList.restoreMarkers(markersLatLngList, gMap);
		if (markerList.size() > 0 && !snippet.equals("")) setMarkerInfoWindow(markerList.get(0), snippet);
		
		gMap.setOnCameraIdleListener(this);
		gMap.setOnMapClickListener(this);
		gMap.setOnMarkerClickListener(CustomOnMarkerClickListener.getListener(IS_DOT));
		//gMap.setOnMyLocationButtonClickListener(this);
        enableMyLocation(false);
		switchMode(elevationProfileMenuVisible);
    }

	// Wissel tussen punt- en lijnmodus
	private void switchMode(boolean menuVisible)
	{
		if (menuVisible)
		{
			mode = Mode.LINE;
			// Verwijder markers
			removeMarker();
			drawLineAndDots();
		}
		else
		{
			mode = Mode.POINT;
			removeLineAndDots();
		}
	}

	private void drawLineAndDots()
	{
		// Voeg lijn toe
		line = gMap.addPolyline(new PolylineOptions()
								.zIndex(LINE_Z_INDEX)
								.startCap(new RoundCap())
								.endCap(new RoundCap())
								.jointType(JointType.ROUND)
								.geodesic(true)
								.addAll(verticesList));
		// Voeg stippen toe
		for (LatLng point : verticesList)
			dotsList.add(drawDot(point));
	}

	private void removeLineAndDots()
	{
		// Verwijder lijn
		if (line != null) line.remove();
		verticesList.clear();
		// Verwijder stippen
		for (Marker dot : dotsList) dot.remove();
		dotsList.clear();
	}

	// Voeg punt toe aan lijn
	private void addPointToLine(LatLng point)
	{
		// Werk lijn bij
		verticesList.add(point);
		line.setPoints(verticesList);
		// Werk stippen bij
		dotsList.add(drawDot(point));
	}

	// Verwijder laatste punt van lijn
	private void deleteLastPoint()
	{
		// Werk lijn bij
		if (verticesList == null) return;
		if (verticesList.size() < 1) return;
		int lastElementIndexLine = verticesList.size() - 1;
		verticesList.remove(lastElementIndexLine);
		line.setPoints(verticesList);
		// Werk stippen bij
		if (dotsList == null) return;
		if (dotsList.size() < 1) return;
		int lastElementIndexDots = dotsList.size() - 1;
		// Verwijder stip van scherm
		dotsList.get(lastElementIndexDots).remove();
		// ... en uit de lijst
		dotsList.remove(lastElementIndexDots);
	}

	// Zet stip
	private Marker drawDot(LatLng point)
	{
		Marker dot = gMap.addMarker(new MarkerOptions()
									.position(point)
									.icon(BitmapDescriptorFactory.fromResource(R.drawable.circle_black_8x8))
									.zIndex(DOT_Z_INDEX)
									.anchor(0.5f, 0.5f));
		// Zet tag dat dit een stip is, voor bepalen van actie bij aanklikken
		dot.setTag(IS_DOT);

		return dot;
	}

	private LocationProvider initializeZoomToLocation(boolean zoomToStandardIfLocationNotAvailable)
	{
		LocationProvider locationProvider = new LocationProvider(context)
		{
			@Override
			public void handleLocation(Location location, float zoom)
			{
				//Log.i("HermLog", "handleLocation, this.getCurrentLocation(): " + this.getCurrentLocation());
				//if (this.getCurrentLocation() != null) showMyLocationIcon(true);
				zoomToLocation(location, zoom);
			}

			@Override
			public void locationUnavailableZoomToStandardLocation(LatLng standardLocation, float zoom)
			{
				//Log.i("HermLog", "locationUnavailable");
				Toast.makeText(context, getResources().getString(R.string.device_location_not_available_message), Toast.LENGTH_SHORT).show();
				gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(amersfoort, zoom));
			}

			@Override
			public void locationUnavailable()
			{
				Toast.makeText(context, getResources().getString(R.string.device_location_not_available_message), Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onConnected(Bundle bundle)
			{
				//Log.i("HermLog", "onConnected");

				if (isZoomToStandardIfLocationNotAvailable())
					zoomToCurrentOrStandardLocation(amersfoort, 1, 15);
				else
					zoomToCurrentLocation(1, 15);
			}
		};

		locationProvider.setZoomToStandardIfLocationNotAvailable(zoomToStandardIfLocationNotAvailable);

		return locationProvider;
	}

	@Override
    public boolean onMyLocationButtonClick()
	{
        //Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();

        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }

	private void zoomToLocation(Location lastLocation, float zoom)
	{
		if (lastLocation != null)
		{
			double lat = lastLocation.getLatitude();
			double lon = lastLocation.getLongitude();
			gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lon), zoom));
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
            enableMyLocation(true);
        }
		else
		{
			// geen toestemming, zoom naar standaard locatie
			locationProvider.locationUnavailableZoomToStandardLocation(amersfoort, 15);
		}
    }

	// Blauwe stip op huidige locatie, my location layer
	// zoomAction: moet ingezoomd worden of niet
    private void enableMyLocation(boolean zoomAction)
	{
		//Log.i("HermLog", "enableMyLocation()");

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

			// Zoom naar huidige of standaardlocatie bij eerste opstart app
			// of bij menukeuze 'Zoom naar mijn locatie'
			//Log.i("HermLog", "savedInstanceStateGlobal: " + savedInstanceStateGlobal);
			if (savedInstanceStateGlobal == null || zoomAction) locationProvider.connect();
			//|| !locationProvider.isZoomToStandardIfLocationNotAvailable())
        }
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
			.enableAutoManage(this, this)
			.build();
	}

	@Override
	public void onConnectionFailed(ConnectionResult p1)
	{
	}

	@Override
	public void onConnected(Bundle p1)
	{
	}

	@Override
	public void onConnectionSuspended(int p1)
	{
	}

	@Override
	public TileOverlay createLayer(LayerItem layerItem, int opacity)
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

		tileOverlay.setTransparency(1f - opacity / 100f);

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
		//Log.i("HermLog", "Zoom: " + zoomLevel);
    }

	@Override
    public void onMapClick(LatLng point)
	{
		//Toast.makeText(context, "Point: " + point.toString(), Toast.LENGTH_SHORT).show();
		if (mode == Mode.POINT)
		{
			putMarker(point);
		}
		else
		{
			addPointToLine(point);
		}
    }

	// Plaats marker na klik op kaart
	private void putMarker(LatLng pointLatLong)
	{
		removeMarker();
		ArrayList<LayerItem> visibleLayers = LayerSelector.getLayerSelector(layerList, context).getVisibleQueryableLayers();

		if (visibleLayers.size() == 0)
		{
			Toast.makeText(context, getResources().getString(R.string.make_layer_with_altitude_visible_message), Toast.LENGTH_LONG).show();
			return;
		}

		drawMarker(pointLatLong);
		
		// Vraag hoogte op voor punt
		if (!ConnectionUtils.isNetworkConnected(context))
		{
			ConnectionUtils.showMessage(context, getResources().getString(R.string.not_connected_message));
			return;
		}
		
		getElevationFromLatLong(pointLatLong, visibleLayers);
	}
	
	// Teken marker op de kaart
	private void drawMarker(LatLng point)
	{
		// Plaats nieuwe marker op kaart en in de lijst
		markerList.add(markerList.size(), gMap.addMarker(new MarkerOptions().position(point)));
	}
	
	private void setMarkerInfoWindow(Marker marker, String snippet)
	{
		marker.setTitle(getResources().getString(R.string.marker_title));
		marker.setSnippet(snippet);
		marker.showInfoWindow();
	}

	// Verwijder huidige marker
	private void removeMarker()
	{
		if (markerList.size() > 0)
		{
			// Verwijder van kaart 
			markerList.get(0).remove();
			// Verwijder uit de lijst met markers
			markerList.remove(0);
		}
	}

	private void getElevationFromLatLong(LatLng pointLatLong, ArrayList<LayerItem> visibleLayers)
	{
		if (taskFragment.isRunning()) taskFragment.cancel();

		ArrayList<URL> urls = new ArrayList<URL>();
		shortTitles.clear();

		for (LayerItem layerItem : visibleLayers)
		{
			URL url = WMSGetMapFeatureUrlMaker.getUrlMaker(256, 256, pointLatLong, zoomLevel, layerItem).makeUrl();
			//Log.i("HermLog", "url: " + url);
			urls.add(url);
			shortTitles.add(layerItem.getShortTitle());
		}

		taskFragment.start(urls);
	}

	// int visibility is View.VISIBLE, View.GONE of View.INVISIBLE
	private void showProgressBar(int visibility)
	{
		View progressbar = findViewById(R.id.progressbar);
		progressbar.setVisibility(visibility);
	}
	
	// int visibility is View.VISIBLE, View.GONE of View.INVISIBLE
	private void showProgressBarDeterminate(int visibility)
	{
		View progressbar = findViewById(R.id.progressbar_determinate);
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

	// Wissel tussen zichtbare en onzichtbare View
	// eventueel met animatie
	// geeft true als zichtbaar gemaakt
	// als boolean show == true: niet wisselen maar altijd menu zichtbaar maken
	private boolean toggleViewVisibility(View view, Animation showAnimation, Animation hideAnimation, boolean show)
	{
		if (view.getVisibility() == View.VISIBLE && !show)
		{
			view.setVisibility(View.INVISIBLE);
			if (hideAnimation != null) view.startAnimation(hideAnimation);
			return false;
		}
		else
		{
			view.setVisibility(View.VISIBLE);
			if (showAnimation != null) view.startAnimation(showAnimation);
			return true;
		}
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
		outState.putBoolean(NOT_CONNECTED_STATE_KEY, notConnectedMessageWasShowed);
		outState.putBoolean(LEGEND_VISIBLE_KEY, legendVisible);
		outState.putBoolean(ELEVATION_PROFILE_MENU_VISIBLE_KEY, elevationProfileMenuVisible);
		outState.putSerializable(MODE_KEY, mode);
		outState.putParcelableArrayList(LINE_VERTICES_LIST_KEY, verticesList);
		outState.putParcelableArrayList(MARKER_LATLNG_LIST_KEY, MarkersListToLatLngList.markersToLatLng(markerList));
		if (markerList.size() > 0) outState.putString(MARKER_SNIPPET_KEY, markerList.get(0).getSnippet());
		outState.putStringArrayList(SHORT_TITLES_KEY, shortTitles);

		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		//if (taskFragment.isRunning()) taskFragment.cancel();
		//toolbar.getMenu().close();
		//closeOptionsMenu();
		//Log.i("HermLog", "onDestroy()");
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		//Log.i("HermLog", "onResume()");

		isPlayServicesAvailable();
		locationProvider = initializeZoomToLocation(savedInstanceStateGlobal == null);
	}

	@Override
	protected void onStart()
	{
		super.onStart();
		//Log.i("HermLog", "onStart()");
		notConnectedMessageWasShowed = ConnectionUtils.showMessageOnlyIfNotConnected(context, getResources().getString(R.string.not_connected_message), notConnectedMessageWasShowed);
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		//Log.i("HermLog", "onPause()");
	}

	@Override
	protected void onStop()
	{
		super.onStop();
		if (locationProvider.isConnected()) locationProvider.disconnect();
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
	public void onPostExecute(ArrayList<Double> result)
	{
		if (mode == Mode.POINT)
		{
			showProgressBar(View.GONE);

			// Toon hoogte bij marker
			if (result == null)
			{
				Toast.makeText(context, getResources().getString(R.string.download_error_message), Toast.LENGTH_SHORT).show();
			}
			else
			{
				String snippetText = ElevationListToText.toText(context, result, shortTitles);
				
				// Als GoogleMap nog niet geladen is, stel alleen tekst in, InfoWindow wordt dan later gevuld
				if (markerList.size() == 0)
				{
					snippet = snippetText;
					return;
				}
				
				Marker marker = markerList.get(0);
				setMarkerInfoWindow(marker, snippetText);
			}
		}
		else
		{
			Log.i("HermLog", "onPostExecute(), mode == Mode.LINE, result: " + result);
		}
	}
}
