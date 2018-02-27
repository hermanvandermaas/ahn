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
import java.net.*;

import android.support.v7.app.ActionBar;

public class MainActivity extends AppCompatActivity
implements OnMapReadyCallback
{
	private boolean dialogWasShowed = false;
	private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

		context = this;

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
	public void onMapReady(GoogleMap googleMap)
	{
		// Instellingen basiskaart
		UiSettings uiSettings = googleMap.getUiSettings();
		uiSettings.setCompassEnabled(false);
		
		// Richt camera
        LatLng sydney = new LatLng(-33.852, 151.211);
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
		
		// Maak TileOverlay
		TileOverlay tileOverlay = googleMap.addTileOverlay(new TileOverlayOptions().tileProvider(WMSTileProvider.getTileProvider()));
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
	protected void onResume()
	{
		super.onResume();
		Log.i("HermLog", "onResume()");

		// Check beschikbaarheid Google Play services
		isPlayServicesAvailable();
	}
}
