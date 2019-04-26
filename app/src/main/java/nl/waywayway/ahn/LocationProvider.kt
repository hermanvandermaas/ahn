package nl.waywayway.ahn

import android.content.Context
import android.content.IntentSender
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast

import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.Task

// Class voor opvragen van en zoomen naar locatie toestel of standaardlocatie

class LocationProvider(val context: Context, val gMap: GoogleMap, var standardLocation: LatLng) : LocationCallback() {
    var fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
    val standardZoomLevel: Float = context.resources.getInteger(R.integer.standard_zoom_level).toFloat()
    val timeOut: Long = 1000
    val numberOfUpdates: Int = 1
    val priorityCode: Int = LocationRequest.PRIORITY_HIGH_ACCURACY
    var zoomToStandardLocationAsDefault: Boolean = false

    fun zoomToDeviceOrStandardLocation() {
        // Zoom in op huidige locatie of midden van Nederland bij eerste opstart app
        zoomToStandardLocationAsDefault = true
        zoomToDeviceLocation()
    }

    fun zoomToDeviceLocation() {
        requestLocationUpdate()
        /*
        fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        // Locatie beschikbaar
                        Log.i("HermLog", "zoomToDeviceLocation(): laatste locatie beschikbaar");
                        val lat = location.getLatitude()
                        val lon = location.getLongitude()
                        zoomToLocation(LatLng(lat, lon), standardZoomLevel)
                    } else {
                        // Locatie niet beschikbaar, vraag locatie op
                        Log.i("HermLog", "zoomToDeviceLocation(): laatste locatie niet beschikbaar");
                        requestLocationUpdate()
                    }
                }*/
    }

    fun requestLocationUpdate() {
        val locationRequest = LocationRequest.create()?.apply {
            setExpirationDuration(timeOut)
            numUpdates = numberOfUpdates
            priority = priorityCode
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, this, null)
    }

    override fun onLocationResult(locationResult: LocationResult?) {
        fusedLocationClient.removeLocationUpdates(this)

        if (locationResult == null) {
            // Locatie definitief niet beschikbaar
            Log.i("HermLog", "onLocationResult: locatie update opgevraagd maar niet beschikbaar")
            Toast.makeText(context, context.resources.getString(R.string.device_location_not_available_message), Toast.LENGTH_SHORT).show()
            if (zoomToStandardLocationAsDefault) zoomToLocation(standardLocation, standardZoomLevel)
            zoomToStandardLocationAsDefault = false
        } else {
            // Locatie opgevraagd en beschikbaar
            Log.i("HermLog", "onLocationResult: locatie update opgevraagd en beschikbaar")
            val location: Location? = locationResult.locations[0]
            val lat = location?.latitude
            val lon = location?.longitude
            Log.i("HermLog", "onLocationResult: lat: " + lat)
            Log.i("HermLog", "onLocationResult: long: " + lon)
            zoomToLocation(LatLng(lat!!, lon!!), standardZoomLevel)
        }
    }

    fun zoomToLocation(location: LatLng, zoomLevel: Float) {
        gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, zoomLevel))
    }
}