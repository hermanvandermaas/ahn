package nl.waywayway.ahn;

import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import java.util.*;

// Markers omzetten naar (parcelable) LatLng
// voor opslaan en herstellen van Marker posities bij schermrotatie

public class MarkersListToLatLngList
{
	public static ArrayList<LatLng> markersToLatLng(ArrayList<Marker> markerList)
	{
		ArrayList<LatLng> latLngList = new ArrayList<LatLng>();
		
		for (Marker marker : markerList)
		{
			latLngList.add(marker.getPosition());
		}
		
		return latLngList;
	}
	
	// Tekent de Markers ook op de kaart
	// referentie naar Marker krijg je alleen bij toevoegen aan de kaart
	public static ArrayList<Marker> restoreMarkers(ArrayList<LatLng> latLngList, GoogleMap gMap)
	{
		ArrayList<Marker> markerList = new ArrayList<Marker>();

		for (LatLng latLng : latLngList)
		{
			markerList.add(markerList.size(), gMap.addMarker(new MarkerOptions().position(latLng)));
		}

		return markerList;
	}
}
