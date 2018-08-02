package nl.waywayway.ahn;

import android.util.*;
import com.google.android.gms.maps.model.*;
import java.util.*;

// Maak hoogteprofiellijst uit puntenlijst

public class ElevationProfileMaker
{
	public static ArrayList<ElevationProfileVertex> make(ArrayList<LatLng> verticesList)
	{
		// Bepaal tussenliggende punten en zet in lijst
		double totalDistance = 0;
		
		for (int i = 0; i < (verticesList.size() - 1); i++)
			totalDistance += SphericalUtil.computeDistanceBetween(verticesList.get(i + 1), verticesList.get(i));
		//Log.i("HermLog", "distance: " +  SphericalUtil.computeDistanceBetween(verticesList.get(i + 1), verticesList.get(i)));
		
		// Download hoogten per punt en zet in lijst
		ArrayList<ElevationProfileVertex> list = new ArrayList<ElevationProfileVertex>();

		return list;
	}
}
