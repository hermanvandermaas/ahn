package nl.waywayway.ahn;

import android.content.*;
import android.location.*;
import android.os.*;
import android.util.*;
import com.google.android.gms.common.*;
import com.google.android.gms.common.api.*;
import com.google.android.gms.location.*;
import com.google.android.gms.maps.model.*;

import com.google.android.gms.location.LocationListener;

public class LocationProvider
implements 
LocationListener,
GoogleApiClient.ConnectionCallbacks,
GoogleApiClient.OnConnectionFailedListener
{
	private GoogleApiClient googleApiClient;
	private float zoom;
	private LocationProvider thisInstance;
	private Context context;
	Handler handler;

	public LocationProvider(Context context)
	{
		this.context = context;
		this.googleApiClient = createGoogleApiClient();
		this.thisInstance = this;
	}

	private GoogleApiClient createGoogleApiClient()
	{
		googleApiClient = new GoogleApiClient
			.Builder(context)
			.addConnectionCallbacks(this)
			.addOnConnectionFailedListener(this)
			.addApi(LocationServices.API)
			.build();
			
		return googleApiClient;
	}
	
	// Zoom in op huidige locatie of midden van Nederland bij eerste opstart app
	// timeOut in seconden
	public void zoomToCurrentOrStandardLocation(final LatLng standardLocation, int timeOut, final float zoom)
	{
		//Log.i("HermLog", "zoomToCurrentOrStandardLocation");
		Location currentLocation = getCurrentLocation();

		if (currentLocation != null)
		{
			handleLocation(currentLocation, zoom);
		}
		else
		{
			// wacht x seconden op locatie
			LocationRequest locationRequest = LocationRequest.create()
				.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
				.setNumUpdates(1)
				.setExpirationDuration(1000 * timeOut);

			LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);

			Runnable expiredRunnable = new Runnable()
			{
				@Override
				public void run()
				{
					//Log.i("HermLog", "expiredRunnable");
					LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, thisInstance);
					locationUnavailable(standardLocation, zoom);
				}
			};
			
			handler = new Handler();
			handler.postDelayed(expiredRunnable, 1000 * timeOut);
		}

		//Toast.makeText(this, getResources().getString(R.string.device_location_not_available_message), Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onLocationChanged(Location location)
	{
		//Log.i("HermLog", "onLocationChanged");
		LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
		handler.removeCallbacksAndMessages(null);
        handleLocation(location, zoom);
	}

	public Location getCurrentLocation()
	{
		//Log.i("HermLog", "getCurrentLocation");
		// Vraag huidige locatie op
		Location lastLocation = LocationServices.FusedLocationApi
			.getLastLocation(googleApiClient);

		return lastLocation;
	}
	
	public void connect()
	{
		googleApiClient.connect();
	}
	
	public void disconnect()
	{
		googleApiClient.disconnect();
	}
	
	// Methods bedoeld om te overschrijven bij intantiatie
	@Override
	public void onConnected(Bundle bundle)
	{}

	@Override
	public void onConnectionSuspended(int code)
	{}
	
	@Override
	public void onConnectionFailed(ConnectionResult connectionResult)
	{}
	
	public void handleLocation(Location location, float zoom)
	{}

	public void locationUnavailable(LatLng standardLocation, float zoom)
	{}
}
