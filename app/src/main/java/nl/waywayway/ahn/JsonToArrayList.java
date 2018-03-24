package nl.waywayway.ahn;

import android.util.*;
import java.io.*;
import java.util.*;

// Maak ArrayList van json string

public class JsonToArrayList
{
	private JsonToArrayList(){}
	
	public static ArrayList<LayerItem> makeArrayList(InputStream inputStream)
	{
		// Lees json
		// context.getResources().openRawResource(R.raw.layers)
		String jsonLayers = new Scanner(inputStream).useDelimiter("\\A").next();
		Log.i("HermLog", "jsonLayers: " + jsonLayers);
		/*
		try
		{
			JSONArray jArray = new JSONObject(result).optJSONArray("features");

			if (jArray.length() == 0) return null;

			double hoogte = jArray
				.optJSONObject(0)
				.optJSONObject("properties")
				.optDouble("GRAY_INDEX");
			//Log.i("HermLog", "Hoogte: " + hoogte);

			return hoogte;
		}
		catch (JSONException e)
		{
			Log.i("HermLog", e.getStackTrace().toString());
			return null;
		}
		*/
		return new ArrayList<LayerItem>();
	}
	
	
}
