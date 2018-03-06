package nl.waywayway.ahn;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.os.*;
import android.support.v7.app.*;
import android.support.v7.widget.*;
import android.util.*;
import android.view.*;
import android.widget.*;
import com.google.android.gms.common.*;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import java.util.*;

import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;

public class MainActivity extends AppCompatActivity implements 
	GoogleMap.OnCameraIdleListener, 
	OnMapReadyCallback,
	GoogleMap.OnMapClickListener
{
	private boolean dialogWasShowed = false;
	private Context context;
	private Bundle savedInstanceStateGlobal;
	private GoogleMap gMap;
	private ArrayList<Marker> markerList = new ArrayList<Marker>();
	private final LatLngBounds nederland = new LatLngBounds(new LatLng(50.75, 3.2), new LatLng(53.7, 7.22));

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
		//setTransparentStatusBar();
    }

	// Maak toolbar
	private void makeToolbar()
	{
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		ActionBar actionBar = getSupportActionBar();
		//toolbar.setPadding(0, getStatusBarHeight(), 0, 0);
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
    public void onCameraIdle() {
        Log.i("HermLog", "Zoom: " + gMap.getCameraPosition().zoom);
    }
	
	@Override
    public void onMapClick(LatLng point)
	{
		//Toast.makeText(context, "Point: " + point.toString(), Toast.LENGTH_SHORT).show();
		putMarker(point);
    }
	
	private void putMarker(LatLng point)
	{
		//Log.i("HermLog", "markerList size(): " + markerList.size());
		
		// Verwijder huidige marker van kaart en uit de lijst met markers
		if (markerList.size() > 0)
		{
			markerList.get(0).remove();
			markerList.remove(0);
		}
		
		// Plaats nieuwe marker op kaart en in de lijst
		markerList.add(markerList.size(), gMap.addMarker(new MarkerOptions().position(point)));
		
		//Toast.makeText(context, "Lat/lon: " + ProjectionWM.xyToLatLng(new double[]{256,256}).toString(), Toast.LENGTH_SHORT).show();
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
