package nl.waywayway.ahn;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.data.Entry;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.location.places.ui.SupportPlaceAutocompleteFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;

import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements
        GoogleMap.OnCameraIdleListener,
        OnMapReadyCallback,
        GoogleMap.OnMapClickListener,
        GoogleMap.OnMyLocationButtonClickListener,
        ActivityCompat.OnRequestPermissionsResultCallback,
        TaskFragment.TaskCallbacks,
        CancelOrProceedDialogFragment.YesNoDialog,
        LayersRecyclerViewAdapter.AdapterCallbacks,
        MyOnChartValueSelectedListener.Callbacks {
    private static final String TAG_TASK_FRAGMENT = "task_fragment";
    private static final String TAG_DELETE_LINE_DIALOG = "delete_line";
    private boolean dialogPlayServicesWasShowed = false;
    private boolean notConnectedMessageWasShowed = false;
    private Context context;
    private Toolbar toolbar;
    private Bundle savedInstanceStateGlobal;
    private GoogleMap gMap;
    private GeoDataClient geoDataClient;
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
    private Chart chart;
    private View chartContainer;
    private GestureDetectorCompat swipeRightGestureDetector;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private boolean permissionAsked = false;
    private static final String PERMISSION_ASKED_STATE_KEY = "permission_asked_state_key";
    private static final String NOT_CONNECTED_STATE_KEY = "not_connected_state_key";
    private boolean myLocationIconVisible;
    private boolean searchBarVisible = true;
    private boolean legendVisible = false;
    private boolean chartVisible = false;
    private boolean elevationProfileMenuVisible = false;
    private ArrayList<Entry> entries;
    private static final String SEARCHBAR_VISIBLE_KEY = "search_bar_visible_key";
    private static final String LEGEND_VISIBLE_KEY = "legend_visible_key";
    private static final String CHART_VISIBLE_KEY = "chart_visible_key";
    private static final String ELEVATION_PROFILE_MENU_VISIBLE_KEY = "elevation_profile_menu_visible_key";
    private static final String MODE_KEY = "mode_key";
    private static final String LINE_VERTICES_LIST_KEY = "line_vertices_list_key";
    private static final String MARKER_LATLNG_LIST_KEY = "marker_list_key";
    private static final String POINTS_LIST_KEY = "points_list_key";
    private static final String MARKER_SNIPPET_KEY = "marker_info_window_key";
    private static final String SHORT_TITLES_KEY = "short_titles_key";
    private static final String SHORT_TITLE_KEY = "short_title_key";
    private static final String DISTANCE_FROM_ORIGIN_LIST_KEY = "distance_from_origin_list_key";
    private static final String ELEVATION_LIST_KEY = "elevation_list_key";
    private static final String ENTRIES_LIST_KEY = "entries_list_key";
    private static final float LINE_Z_INDEX = 500;
    private static final float LINE_WIDTH = 5f;
    private static final float DOT_Z_INDEX = 600;
    private static final String IS_DOT = "isDot";
    private String snippet;
    private ArrayList<String> shortTitles = new ArrayList<String>();
    private String shortTitle;
    private ArrayList<LatLng> userMadePoints = new ArrayList<LatLng>();
    private ArrayList<Marker> dotsList = new ArrayList<Marker>();
    private ArrayList<LatLng> markersLatLngList = new ArrayList<LatLng>();
    private ArrayList<LatLng> pointsList = new ArrayList<LatLng>();
    private ArrayList<Double> distanceFromOriginList = new ArrayList<Double>();
    private ArrayList<Double> elevationList = new ArrayList<Double>();
    private int totalPoints = 30;

    // Mode.POINT: klik op kaart geeft hoogte van punt, Mode.LINE: klik maakt lijn voor hoogteprofiel
    public enum Mode {
        POINT, LINE
    }

    ;
    private Mode mode = Mode.POINT;
    Polyline line;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
        chart = findViewById(R.id.chart_line);
        chartContainer = findViewById(R.id.chart_container);

        showOnboardingScreenAtFirstRun();

        // Herstel savedInstanceState
        if (savedInstanceState != null) {
            permissionAsked = savedInstanceState.getBoolean(PERMISSION_ASKED_STATE_KEY);
            searchBarVisible = savedInstanceState.getBoolean(SEARCHBAR_VISIBLE_KEY);
            notConnectedMessageWasShowed = savedInstanceState.getBoolean(NOT_CONNECTED_STATE_KEY);
            legendVisible = savedInstanceState.getBoolean(LEGEND_VISIBLE_KEY);
            chartVisible = savedInstanceState.getBoolean(CHART_VISIBLE_KEY);
            mode = (Mode) savedInstanceState.getSerializable(MODE_KEY);
            userMadePoints = savedInstanceState.getParcelableArrayList(LINE_VERTICES_LIST_KEY);
            snippet = savedInstanceState.getString(MARKER_SNIPPET_KEY);
            snippet = (snippet == null) ? "" : snippet;
            markersLatLngList = savedInstanceState.getParcelableArrayList(MARKER_LATLNG_LIST_KEY);
            pointsList = savedInstanceState.getParcelableArrayList(POINTS_LIST_KEY);
            shortTitles = savedInstanceState.getStringArrayList(SHORT_TITLES_KEY);
            shortTitle = savedInstanceState.getString(SHORT_TITLE_KEY);
            distanceFromOriginList = (ArrayList<Double>) savedInstanceState.getSerializable(DISTANCE_FROM_ORIGIN_LIST_KEY);
            elevationList = (ArrayList<Double>) savedInstanceState.getSerializable(ELEVATION_LIST_KEY);
            entries = savedInstanceState.getParcelableArrayList(ENTRIES_LIST_KEY);
        }

        // Handler voor worker fragment
        FragmentManager fm = getSupportFragmentManager();
        taskFragment = (TaskFragment) fm.findFragmentByTag(TAG_TASK_FRAGMENT);
        if (taskFragment == null) {
            taskFragment = new TaskFragment();
            fm.beginTransaction().add(taskFragment, TAG_TASK_FRAGMENT).commit();
        }

        if (taskFragment.isRunning() && mode == Mode.POINT)
            showProgressBarIndeterminate(View.VISIBLE);
        if (taskFragment.isRunning() && mode == Mode.LINE) showProgressBarDeterminate(View.VISIBLE);

        initializeLegend();
        makeAndShowChart(false);
        initializeElevationProfileMenu();
        createGoogleApi();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        makeToolbar();
    }

    // Legenda
    private void initializeLegend() {
        if (legendVisible) {
            legendVisible = toggleViewVisibility(
                    legend,
                    AnimationUtils.loadAnimation(this, R.anim.legend_slide_left),
                    AnimationUtils.loadAnimation(this, R.anim.legend_slide_right),
                    true, null);
        }

        swipeRightGestureDetector = new GestureDetectorCompat(this, new SwipeGestureListener() {
            @Override
            public void onSwipeRight() {
                legendVisible = toggleViewVisibility(
                        legend,
                        AnimationUtils.loadAnimation(context, R.anim.legend_slide_left),
                        AnimationUtils.loadAnimation(context, R.anim.legend_slide_right),
                        false, null);
            }
        });

        legend.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                swipeRightGestureDetector.onTouchEvent(motionEvent);
                return false;
            }
        });
    }

    // Grafiek
    private void makeAndShowChart(final boolean newData) {
        //Log.i("HermLog", "chartVisible: " + chartVisible);
        if (chartVisible) {
            chartVisible = toggleViewVisibility(
                    chartContainer,
                    AnimationUtils.loadAnimation(context, R.anim.chart_slide_up),
                    AnimationUtils.loadAnimation(context, R.anim.chart_slide_down),
                    true,
                    new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation p1) {
                        }

                        @Override
                        public void onAnimationRepeat(Animation p1) {
                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            // Deel afbeelding grafiek met andere app
                            ((ImageView) findViewById(R.id.share_chart_image))
                                    .setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Bitmap chartBitmap = chart.getChartBitmap();
                                            String fileName = context.getResources().getString(R.string.share_chart_image_path);
                                            String noImageAvailableMessage = context.getResources().getString(R.string.no_image_for_sharing_available_message);
                                            String fileAuthority = context.getResources().getString(R.string.files_authority);
                                            String mimeType = context.getResources().getString(R.string.share_image_mime_type);
                                            ShareFile.getInstance(context, chartBitmap, null, fileName, fileAuthority, noImageAvailableMessage, mimeType).share();
                                        }
                                    });

                            // Deel tabel met andere app
                            ((ImageView) findViewById(R.id.share_chart_data))
                                    .setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            ArrayList<String> columnlabels = LineChartDataMaker.getDataMaker().getColumnlabels(context);
                                            String csvData = CsvStringMaker.getInstance(entries, columnlabels, shortTitle, pointsList).getCsvString();
                                            String fileName = context.getResources().getString(R.string.share_table_path);
                                            String noDataAvailableMessage = context.getResources().getString(R.string.no_data_for_sharing_available_message);
                                            String fileAuthority = context.getResources().getString(R.string.files_authority);
                                            String mimeType = context.getResources().getString(R.string.share_data_mime_type);
                                            ShareFile.getInstance(context, null, csvData, fileName, fileAuthority, noDataAvailableMessage, mimeType).share();
                                        }
                                    });

                            // Toon grafiek
                            LineChartMaker.getChartMaker(context).makeChart(chart, entries, shortTitle);
                            setProgressBarDeterminate(0, false);
                            showProgressBarDeterminate(View.GONE);

                            // Verschuif kaart van onder grafiek stukje naar links, bij liggend scherm
                            int orientation = context.getResources().getConfiguration().orientation;
                            if (orientation == Configuration.ORIENTATION_LANDSCAPE && newData) {
                                SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
                                int mapViewWidth = mapFragment.getView().getWidth();
                                int newX = Math.round(mapViewWidth * 0.75f);
                                int mapViewHeight = mapFragment.getView().getHeight();
                                int newY = Math.round(mapViewHeight * 0.5f);
                                Point point = new Point(newX, newY);
                                double newLongitude = gMap.getProjection().fromScreenLocation(point).longitude;
                                double newLatitude = gMap.getProjection().fromScreenLocation(point).latitude;
                                float currentZoom = gMap.getCameraPosition().zoom;
                                gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(newLatitude, newLongitude), currentZoom));
                            }
                        }
                    });
        }
    }

    // Menu hoogteprofiel
    private void initializeElevationProfileMenu() {
        if (mode == Mode.LINE)
            elevationProfileMenuVisible = toggleViewVisibility(elevationProfileMenu, null, null, true, null);

        // Sluit menu
        ((ImageView) findViewById(R.id.elevation_profile_close))
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Log.i("HermLog", "taskFragment.isRunning(): " + taskFragment.isRunning());
                        if (taskFragment.isRunning()) taskFragment.cancel();
                        setProgressBarDeterminate(0, false);
                        showProgressBarDeterminate(View.GONE);
                        removeMarker();

                        if (chartVisible)
                            chartVisible = toggleViewVisibility(
                                    chartContainer,
                                    AnimationUtils.loadAnimation(context, R.anim.chart_slide_up),
                                    AnimationUtils.loadAnimation(context, R.anim.chart_slide_down),
                                    false, null);

                        elevationProfileMenuVisible = toggleViewVisibility(
                                elevationProfileMenu,
                                AnimationUtils.loadAnimation(context, R.anim.elevation_profile_menu_slide_right),
                                AnimationUtils.loadAnimation(context, R.anim.elevation_profile_menu_slide_left),
                                false, null);

                        switchMode(elevationProfileMenuVisible);
                    }
                });

        // Verwijder laatste punt
        ((ImageView) findViewById(R.id.elevation_profile_delete_last_point))
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (taskFragment.isRunning()) taskFragment.cancel();
                        setProgressBarDeterminate(0, false);
                        showProgressBarDeterminate(View.GONE);
                        removeMarker();
                        deleteLastPoint();
                    }
                });

        // Verwijder alle punten
        ((ImageView) findViewById(R.id.elevation_profile_delete_all_points))
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
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
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Log.i("HermLog", "maak hoogteprofiel");
                        if (ConnectionUtils.showMessageOnlyIfNotConnected(context, getResources().getString(R.string.not_connected_message), false))
                            return;
                        //Log.i("HermLog", "userMadePoints.size(): " + userMadePoints.size());
                        if (userMadePoints.size() < 2) return;
                        if (LayerSelector.getLayerSelector(layerList, context).showMessageNoVisibleQueryableLayers())
                            return;
                        if (taskFragment.isRunning()) taskFragment.cancel();
                        setProgressBarDeterminate(0, false);
                        showProgressBarDeterminate(View.GONE);
                        removeMarker();

                        if (chartVisible)
                            chartVisible = toggleViewVisibility(
                                    chartContainer,
                                    AnimationUtils.loadAnimation(context, R.anim.chart_slide_up),
                                    AnimationUtils.loadAnimation(context, R.anim.chart_slide_down),
                                    false, null);

                        //Log.i("HermLog", "layerList.size(): " + layerList.size());
                        LayerItem topElevationLayer = LayerSelector.getLayerSelector(layerList, context).getTopVisibleLayer();
                        shortTitle = topElevationLayer.getShortTitle();
                        //Log.i("HermLog", "topElevationLayer: " + topElevationLayer.getShortTitle());
                        pointsList = ElevationProfile.makePointsList(userMadePoints, totalPoints);
                        distanceFromOriginList = ElevationProfile.makeDistanceFromOriginList(pointsList);
                        ArrayList<URL> urlList = ElevationProfile.makeUrlList(topElevationLayer, zoomLevel, pointsList);
                        //Log.i("HermLog", "lst.size(): " + lst.size());
                        taskFragment.start(urlList);
                    }
                });
    }

    @Override
    public void onYes(DialogInterface dialog, int id) {
        if (taskFragment.isRunning()) taskFragment.cancel();
        setProgressBarDeterminate(0, false);
        showProgressBarDeterminate(View.GONE);
        removeMarker();

        removeLineAndDots();
        drawLineAndDots();

        if (chartVisible) {
            chartVisible = toggleViewVisibility(
                    chartContainer,
                    AnimationUtils.loadAnimation(context, R.anim.chart_slide_up),
                    AnimationUtils.loadAnimation(context, R.anim.chart_slide_down),
                    false, null);

            chart.clear();
        }
    }

    @Override
    public void onNo(DialogInterface dialog, int id) {
    }

    // Maak toolbar
    private void makeToolbar() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
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
                        false, null);

                return true;

            case R.id.action_share_map:
                gMap.snapshot(new GoogleMap.SnapshotReadyCallback() {
                    @Override
                    public void onSnapshotReady(Bitmap bitmap) {
                        String fileName = context.getResources().getString(R.string.share_map_image_path);
                        String noImageAvailableMessage = context.getResources().getString(R.string.no_image_for_sharing_available_message);
                        String fileAuthority = context.getResources().getString(R.string.files_authority);
                        String mimeType = context.getResources().getString(R.string.share_image_mime_type);
                        ShareFile.getInstance(context, bitmap, null, fileName, fileAuthority, noImageAvailableMessage, mimeType).share();
                    }
                });

                return true;

            case R.id.action_info:
                Intent intent = new Intent(context, InformationActivity.class);
                context.startActivity(intent);

                return true;

            case R.id.action_search:
                searchBarVisible = toggleViewVisibility(
                        searchBar,
                        AnimationUtils.loadAnimation(this, R.anim.search_bar_slide_down),
                        AnimationUtils.loadAnimation(this, R.anim.search_bar_slide_up),
                        false, null);

                return true;

            case R.id.action_elevation_profile:
                if (searchBarVisible)
                    searchBarVisible = toggleViewVisibility(
                            searchBar,
                            AnimationUtils.loadAnimation(this, R.anim.search_bar_slide_down),
                            AnimationUtils.loadAnimation(this, R.anim.search_bar_slide_up),
                            false, null);

                if (chartVisible)
                    chartVisible = toggleViewVisibility(
                            chartContainer,
                            AnimationUtils.loadAnimation(context, R.anim.chart_slide_up),
                            AnimationUtils.loadAnimation(context, R.anim.chart_slide_down),
                            false, null);

                elevationProfileMenuVisible = toggleViewVisibility(
                        elevationProfileMenu,
                        AnimationUtils.loadAnimation(context, R.anim.elevation_profile_menu_slide_right),
                        AnimationUtils.loadAnimation(context, R.anim.elevation_profile_menu_slide_left),
                        false, null);

                if (taskFragment.isRunning()) taskFragment.cancel();
                showProgressBarIndeterminate(View.GONE);
                setProgressBarDeterminate(0, false);
                showProgressBarDeterminate(View.GONE);
                removeMarker();
                switchMode(elevationProfileMenuVisible);

                return true;


            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
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
        if (markerList.size() > 0 && !snippet.equals(""))
            setMarkerInfoWindow(markerList.get(0), snippet);

        gMap.setOnCameraIdleListener(this);
        gMap.setOnMapClickListener(this);
        gMap.setOnMarkerClickListener(CustomOnMarkerClickListener.getListener(IS_DOT));
        //gMap.setOnMyLocationButtonClickListener(this);
        enableMyLocation(false);
        switchMode(elevationProfileMenuVisible);
    }

    // Wissel tussen punt- en lijnmodus
    private void switchMode(boolean menuVisible) {
        if (menuVisible) {
            mode = Mode.LINE;
            // Verwijder markers
            removeMarker();
            drawLineAndDots();
        } else {
            mode = Mode.POINT;
            removeLineAndDots();
        }
    }

    private void drawLineAndDots() {
        // Voeg lijn toe
        line = gMap.addPolyline(new PolylineOptions()
                .zIndex(LINE_Z_INDEX)
                .startCap(new RoundCap())
                .endCap(new RoundCap())
                .jointType(JointType.ROUND)
                .width(LINE_WIDTH)
                .geodesic(true)
                .addAll(userMadePoints));
        // Voeg stippen toe
        for (LatLng point : userMadePoints)
            dotsList.add(drawDot(point));
    }

    private void removeLineAndDots() {
        // Verwijder lijn
        if (line != null) line.remove();
        userMadePoints.clear();
        // Verwijder stippen
        for (Marker dot : dotsList) dot.remove();
        dotsList.clear();
    }

    // Voeg punt toe aan lijn
    private void addPointToLine(LatLng point) {
        // Werk lijn bij
        userMadePoints.add(point);
        line.setPoints(userMadePoints);
        // Werk stippen bij
        dotsList.add(drawDot(point));
    }

    // Verwijder laatste punt van lijn
    private void deleteLastPoint() {
        // Werk lijn bij
        if (userMadePoints == null) return;
        if (userMadePoints.size() < 1) return;
        int lastElementIndexLine = userMadePoints.size() - 1;
        userMadePoints.remove(lastElementIndexLine);
        line.setPoints(userMadePoints);
        // Werk stippen bij
        if (dotsList == null) return;
        if (dotsList.size() < 1) return;
        int lastElementIndexDots = dotsList.size() - 1;
        // Verwijder stip van scherm
        dotsList.get(lastElementIndexDots).remove();
        // ... en uit de lijst
        dotsList.remove(lastElementIndexDots);

        // Wis en verberg grafiek
        if (chartVisible) {
            chartVisible = toggleViewVisibility(
                    chartContainer,
                    AnimationUtils.loadAnimation(context, R.anim.chart_slide_up),
                    AnimationUtils.loadAnimation(context, R.anim.chart_slide_down),
                    false, null);

            chart.clear();
        }
    }

    // Zet stip
    private Marker drawDot(LatLng point) {
        Marker dot = gMap.addMarker(new MarkerOptions()
                .position(point)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.circle_black_8x8))
                .zIndex(DOT_Z_INDEX)
                .anchor(0.5f, 0.5f));
        // Zet tag dat dit een stip is, voor bepalen van actie bij aanklikken
        dot.setTag(IS_DOT);

        return dot;
    }

    private LocationProvider initializeZoomToLocation(boolean zoomToStandardIfLocationNotAvailable) {
        LocationProvider locationProvider = new LocationProvider(context) {
            @Override
            public void handleLocation(Location location, float zoom) {
                //Log.i("HermLog", "handleLocation, this.getCurrentLocation(): " + this.getCurrentLocation());
                //if (this.getCurrentLocation() != null) showMyLocationIcon(true);
                zoomToLocation(location, zoom);
            }

            @Override
            public void locationUnavailableZoomToStandardLocation(LatLng standardLocation, float zoom) {
                //Log.i("HermLog", "locationUnavailable");
                Toast.makeText(context, getResources().getString(R.string.device_location_not_available_message), Toast.LENGTH_SHORT).show();
                gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(amersfoort, zoom));
            }

            @Override
            public void locationUnavailable() {
                Toast.makeText(context, getResources().getString(R.string.device_location_not_available_message), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onConnected(Bundle bundle) {
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
    public boolean onMyLocationButtonClick() {
        //Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();

        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }

    private void zoomToLocation(Location lastLocation, float zoom) {
        if (lastLocation != null) {
            double lat = lastLocation.getLatitude();
            double lon = lastLocation.getLongitude();
            gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lon), zoom));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults, Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation(true);
        } else {
            // geen toestemming, zoom naar standaard locatie
            locationProvider.locationUnavailableZoomToStandardLocation(amersfoort, 15);
        }
    }

    // Blauwe stip op huidige locatie, my location layer
    // zoomAction: moet ingezoomd worden of niet
    private void enableMyLocation(boolean zoomAction) {
        //Log.i("HermLog", "enableMyLocation()");

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Niet nog eens toestemming vragen na schermrotatie
            if (permissionAsked) return;

            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE, Manifest.permission.ACCESS_FINE_LOCATION, false);
            permissionAsked = true;
        } else if (gMap != null) {
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

    private void createPlaceSearch() {
        SupportPlaceAutocompleteFragment autocompleteFragment = (SupportPlaceAutocompleteFragment)
                getSupportFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        if (!searchBarVisible) showSearchBar(View.GONE);

        // Alleen in Nederland zoeken
        AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
                .setCountry(getResources().getString(R.string.country_code))
                .build();

        autocompleteFragment.setFilter(typeFilter);

        // Zoom naar gekozen Place
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                showSearchBar(View.GONE);
                searchBarVisible = false;
                gMap.animateCamera(CameraUpdateFactory.newLatLngBounds(place.getViewport(), 0));
                //Toast.makeText(context, "Place: " + place.getName(), Toast.LENGTH_SHORT).show();
                //Log.i("HermLog", "Place: " + place.getName());
            }

            @Override
            public void onError(Status status) {
                //Toast.makeText(context, "An error occurred: " + status, Toast.LENGTH_SHORT).show();
                //Log.i("HermLog", "PlaceSelectionListener fout: " + status);
            }
        });
    }

    private void createGoogleApi() {
        geoDataClient = Places.getGeoDataClient(this, null);
    }

    @Override
    public TileOverlay createLayer(LayerItem layerItem, int opacity) {
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
    private void createLayerMenu() {
        RecyclerView recyclerView = findViewById(R.id.layers_recycler_view);
        //Log.i("HermLog", "recyclerView: " + recyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(linearLayoutManager);
        LayersRecyclerViewAdapter adapter = new LayersRecyclerViewAdapter(context, layerList);
        recyclerView.setAdapter(adapter);

        // Plaats titel van lagenmenu beneden status bar
        TextView layersTitle = findViewById(R.id.layers_title);
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) layersTitle.getLayoutParams();
        layoutParams.topMargin = getStatusBarHeight();
        layersTitle.setLayoutParams(layoutParams);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(Gravity.RIGHT)) {
            drawerLayout.closeDrawer(Gravity.RIGHT);
        } else if (legend.getVisibility() == View.VISIBLE) {
            showLegend(View.INVISIBLE);
            legendVisible = false;
            Animation slideRight = AnimationUtils.loadAnimation(this, R.anim.legend_slide_right);
            legend.startAnimation(slideRight);
        } else {
            super.onBackPressed();
        }
    }

    // Check beschikbaarheid Play Services
    protected void isPlayServicesAvailable() {
        if (dialogPlayServicesWasShowed) return;

        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(context);

        if (resultCode != ConnectionResult.SUCCESS) {
            //Log.i("HermLog", "Play Services fout");
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog((Activity) context, resultCode, 9000).show();
                dialogPlayServicesWasShowed = true;
            }
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
    public void onCameraIdle() {
        zoomLevel = gMap.getCameraPosition().zoom;
        //Log.i("HermLog", "Zoom: " + zoomLevel);
    }

    @Override
    public void onMapClick(LatLng point) {
        //Toast.makeText(context, "Point: " + point.toString(), Toast.LENGTH_SHORT).show();
        if (mode == Mode.POINT) {
            putMarker(point);
        } else {
            addPointToLine(point);
        }
    }

    // Plaats marker na klik op kaart
    private void putMarker(LatLng pointLatLong) {
        removeMarker();
        ArrayList<LayerItem> visibleLayers = LayerSelector.getLayerSelector(layerList, context).getVisibleQueryableLayers();
        LayerSelector.getLayerSelector(layerList, context).showMessageNoVisibleQueryableLayers();

        drawMarker(pointLatLong);

        // Vraag hoogte op voor punt
        if (ConnectionUtils.showMessageOnlyIfNotConnected(context, getResources().getString(R.string.not_connected_message), false))
            return;
        getElevationFromLatLong(pointLatLong, visibleLayers);
    }

    private void setMarkerInfoWindow(Marker marker, String snippet) {
        marker.setTitle(getResources().getString(R.string.marker_title));
        marker.setSnippet(snippet);
        marker.showInfoWindow();
    }

    // Teken marker op de kaart
    @Override
    public void drawMarker(LatLng point) {
        // Plaats nieuwe marker op kaart en in de lijst
        markerList.add(markerList.size(), gMap.addMarker(new MarkerOptions().position(point)));
    }

    @Override
    // Verwijder huidige marker
    public void removeMarker() {
        if (markerList.size() > 0) {
            // Verwijder van kaart
            markerList.get(0).remove();
            // Verwijder uit de lijst met markers
            markerList.remove(0);
        }
    }

    private void getElevationFromLatLong(LatLng pointLatLong, ArrayList<LayerItem> visibleLayers) {
        if (taskFragment.isRunning()) taskFragment.cancel();

        ArrayList<URL> urls = new ArrayList<URL>();
        shortTitles.clear();

        for (LayerItem layerItem : visibleLayers) {
            URL url = WMSGetMapFeatureUrlMaker.getUrlMaker(256, 256, pointLatLong, zoomLevel, layerItem).makeUrl();
            //Log.i("HermLog", "url: " + url);
            urls.add(url);
            shortTitles.add(layerItem.getShortTitle());
        }

        taskFragment.start(urls);
    }

    // int visibility is View.VISIBLE, View.GONE of View.INVISIBLE
    private void showProgressBarIndeterminate(int visibility) {
        ProgressBar progressbar = findViewById(R.id.progressbar);
        progressbar.setVisibility(visibility);
    }

    // int visibility is View.VISIBLE, View.GONE of View.INVISIBLE
    private void showProgressBarDeterminate(int visibility) {
        ProgressBar progressbar = findViewById(R.id.progressbar_determinate);
        progressbar.setVisibility(visibility);
    }

    private void setProgressBarDeterminate(int percent, boolean animate) {
        ProgressBar progressbar = findViewById(R.id.progressbar_determinate);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            progressbar.setProgress(percent, animate);
        else
            progressbar.setProgress(percent);
    }

    // int visibility is View.VISIBLE, View.GONE of View.INVISIBLE
    private void showSearchBar(int visibility) {
        View searchBarCard = findViewById(R.id.card_place_autocomplete_fragment);
        searchBarCard.setVisibility(visibility);
    }

    // int visibility is View.VISIBLE, View.GONE of View.INVISIBLE
    private void showMyLocationIcon(boolean showIcon) {
        myLocationIconVisible = showIcon;
        invalidateOptionsMenu();
    }

    // int visibility is View.VISIBLE, View.GONE of View.INVISIBLE
    private void showLegend(int visibility) {
        View legend = findViewById(R.id.legend_scrollview);
        legend.setVisibility(visibility);
    }

    // Wissel tussen zichtbare en onzichtbare View
    // eventueel met animatie
    // geeft true als zichtbaar gemaakt
    // als boolean show == true: niet wisselen maar altijd menu zichtbaar maken
    private boolean toggleViewVisibility(View view, Animation showAnimation, Animation hideAnimation, boolean show, Animation.AnimationListener callback) {
        if (view.getVisibility() == View.VISIBLE && !show) {
            view.setVisibility(View.GONE);
            if (callback != null) hideAnimation.setAnimationListener(callback);
            if (hideAnimation != null) view.startAnimation(hideAnimation);
            return false;
        } else {
            view.setVisibility(View.VISIBLE);
            if (callback != null) showAnimation.setAnimationListener(callback);
            if (showAnimation != null) view.startAnimation(showAnimation);
            return true;
        }
    }

    private void showOnboardingScreenAtFirstRun() {
        SharedPreferences sharedPref = context.getSharedPreferences(getResources().getString(R.string.SHARED_PREFERENCES_FILENAME), context.MODE_PRIVATE);
        boolean prefDefault = true;
        boolean showOnBoardingScreen = sharedPref.getBoolean(getResources().getString(R.string.PREFERENCES_KEY_SHOW_ONBOARDING_SCREEN), prefDefault);

        if (showOnBoardingScreen) {
            // Start activity
            Intent mIntent = new Intent(context, OnBoardingScreenActivity.class);
            context.startActivity(mIntent);
            this.finish();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(PERMISSION_ASKED_STATE_KEY, permissionAsked);
        outState.putBoolean(SEARCHBAR_VISIBLE_KEY, searchBarVisible);
        outState.putBoolean(NOT_CONNECTED_STATE_KEY, notConnectedMessageWasShowed);
        outState.putBoolean(LEGEND_VISIBLE_KEY, legendVisible);
        outState.putBoolean(CHART_VISIBLE_KEY, chartVisible);
        outState.putBoolean(ELEVATION_PROFILE_MENU_VISIBLE_KEY, elevationProfileMenuVisible);
        outState.putSerializable(MODE_KEY, mode);
        outState.putParcelableArrayList(LINE_VERTICES_LIST_KEY, userMadePoints);
        outState.putParcelableArrayList(MARKER_LATLNG_LIST_KEY, MarkersListToLatLngList.markersToLatLng(markerList));
        if (markerList.size() > 0)
            outState.putString(MARKER_SNIPPET_KEY, markerList.get(0).getSnippet());
        outState.putParcelableArrayList(POINTS_LIST_KEY, pointsList);
        outState.putStringArrayList(SHORT_TITLES_KEY, shortTitles);
        outState.putString(SHORT_TITLE_KEY, shortTitle);
        outState.putSerializable(DISTANCE_FROM_ORIGIN_LIST_KEY, distanceFromOriginList);
        outState.putSerializable(ELEVATION_LIST_KEY, elevationList);
        outState.putParcelableArrayList(ENTRIES_LIST_KEY, entries);

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //if (taskFragment.isRunning()) taskFragment.cancel();
        //toolbar.getMenu().close();
        //closeOptionsMenu();
        //Log.i("HermLog", "onDestroy()");
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Log.i("HermLog", "onResume()");

        isPlayServicesAvailable();
        locationProvider = initializeZoomToLocation(savedInstanceStateGlobal == null);
    }

    @Override
    protected void onStart() {
        super.onStart();
        //Log.i("HermLog", "onStart()");
        notConnectedMessageWasShowed = ConnectionUtils.showMessageOnlyIfNotConnected(context, getResources().getString(R.string.not_connected_message), notConnectedMessageWasShowed);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //Log.i("HermLog", "onPause()");
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (locationProvider.isConnected()) locationProvider.disconnect();
    }

    /*
    TASK CALLBACK METHODS
    */

    @Override
    public void onPreExecute() {
        //Log.i("HermLog", "onPreExecute()");
        if (mode == Mode.POINT) showProgressBarIndeterminate(View.VISIBLE);
        else {
            showProgressBarDeterminate(View.VISIBLE);
            setProgressBarDeterminate(0, false);
        }
    }

    @Override
    public void onProgressUpdate(int percent) {
        setProgressBarDeterminate(percent, true);
    }

    @Override
    public void onCancelled() {
        //Log.i("HermLog", "onCancelled()");
        setProgressBarDeterminate(0, false);
        showProgressBarDeterminate(View.GONE);
        showProgressBarIndeterminate(View.GONE);
    }

    @Override
    public void onPostExecute(ArrayList<Double> result) {
        if (mode == Mode.POINT) {
            showProgressBarIndeterminate(View.GONE);

            // Toon hoogte bij marker
            if (result == null) {
                Toast.makeText(context, getResources().getString(R.string.download_error_message), Toast.LENGTH_SHORT).show();
            } else {
                String snippetText = ElevationListToText.toText(context, result, shortTitles);

                // Als GoogleMap nog niet geladen is, stel alleen tekst in, InfoWindow wordt dan later gevuld
                if (markerList.size() == 0) {
                    snippet = snippetText;
                    return;
                }

                Marker marker = markerList.get(0);
                setMarkerInfoWindow(marker, snippetText);
            }
        } else {
            //Log.i("HermLog", "result: " + result);

            setProgressBarDeterminate(100, true);
            showProgressBarDeterminate(View.GONE);

            entries = LineChartDataMaker.getDataMaker().makeData(distanceFromOriginList, result, pointsList);

            // Bericht als er hoogten ontbreken
            if (entries.size() == 0) {
                Toast.makeText(context, getResources().getString(R.string.no_elevation_points), Toast.LENGTH_SHORT).show();
                return;
            }

            if (result.size() != entries.size())
                Toast.makeText(context, getResources().getString(R.string.missing_elevation_points), Toast.LENGTH_SHORT).show();

            // Toon grafiek
            chartVisible = true;
            makeAndShowChart(true);
        }
    }
}
