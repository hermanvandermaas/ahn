package nl.waywayway.ahn;

import android.util.*;
import com.google.android.gms.maps.model.*;
import java.util.*;
import java.net.*;

// Maak URL lijst uit LatLng puntenlijst, voor downloaden hoogten per punt

public class ElevationProfile
{
	// totalPoints is maximum aantal punten voor opvragen hoogte
	public static ArrayList<LatLng> makePointsList(ArrayList<LatLng> userMadePoints, int totalPoints)
	{
		// Bepaal tussenliggende punten en zet in lijst
		double totalDistance = 0;
		ArrayList<LatLng> pointsList = new ArrayList<LatLng>();

		for (int i = 0; i < userMadePoints.size(); i++)
		{
			if (i < (userMadePoints.size() - 1)) 
				totalDistance += SphericalUtil.computeDistanceBetween(userMadePoints.get(i), userMadePoints.get(i + 1));
				pointsList.add(userMadePoints.get(i));
		}
		
		//Log.i("HermLog", "pointsList: " + pointsList);

		double distance;
		//Log.i("HermLog", "averageDistance: " + averageDistance);
		
		// Invoegen tussenliggende punten, optellend tot ingesteld maximum
		for (int j = 0; j < (userMadePoints.size() - 1); j++)
		{
			distance = SphericalUtil.computeDistanceBetween(userMadePoints.get(j), userMadePoints.get(j + 1));
			int numberOfPointsIncludingEndpoints = (int) Math.round(totalPoints * ( distance / totalDistance ));
			//Log.i("HermLog", "distance / totalDistance: " + distance + " / " + totalDistance);
			//Log.i("HermLog", "numberOfPointsIncludingEndpoints: " + numberOfPointsIncludingEndpoints);
			ArrayList<LatLng> pointsBetween = getPointsBetween(userMadePoints.get(j), userMadePoints.get(j + 1), numberOfPointsIncludingEndpoints);
			pointsList.addAll(j + 1, pointsBetween);
			//Log.i("HermLog", "pointsList.size(): " + pointsList.size());
		}
		
		return pointsList;
	}
	
	public static ArrayList<URL> makeUrlList(LayerItem topElevationLayer, double zoom, ArrayList<LatLng> pointsList)
	{
		ArrayList<URL> list = new ArrayList<URL>();
		
		// Maak URL per punt en zet in lijst
		for (LatLng latLng : pointsList)
		{
			URL url = WMSGetMapFeatureUrlMaker.getUrlMaker(256, 256, latLng, zoom, topElevationLayer).makeUrl();
			//Log.i("HermLog", "URL: " + url);
			list.add(url);
		}
		
		return list;
	}
	
	// Maak lijst met tussenliggende punten, exclusief eerste en laatste punt
	public static ArrayList<LatLng> getPointsBetween(LatLng from, LatLng to, int numberOfPointsIncludingEndpoints)
	{
		ArrayList<LatLng> pointsBetween = new ArrayList<LatLng>();
		double fraction = 1d / (numberOfPointsIncludingEndpoints - 1);
		//Log.i("HermLog", "fraction: " + fraction);
		double i = fraction;
		
		for (int j = 0; j < (numberOfPointsIncludingEndpoints - 2); j++)
		{
			//Log.i("HermLog", "i: " + i);
			pointsBetween.add(SphericalUtil.interpolate(from, to, i));
			i += fraction;
		}
		
		//Log.i("HermLog", "pointsBetween: " + pointsBetween);
		//Log.i("HermLog", "pointsBetween.size(): " + pointsBetween.size());
		
		return pointsBetween;
	}
	
	public static ArrayList<Double> makeDistanceFromOriginList(ArrayList<LatLng> pointsList)
	{
		ArrayList<Double> distanceList = new ArrayList<Double>();
		distanceList.add(0d);
		Double distance;
		Double distancePrevious;
		
		for (int j = 1; j < pointsList.size(); j++)
		{
			distancePrevious = distanceList.get(j - 1);
			distance = SphericalUtil.computeDistanceBetween(pointsList.get(j - 1), pointsList.get(j));
			distanceList.add(distancePrevious + distance);
		}
		
		return distanceList;
	}
}
