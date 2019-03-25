package nl.waywayway.ahn;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Scanner;

// Maak ArrayList van json string

public class JsonToArrayList
{
	private JsonToArrayList()
	{}

	public static ArrayList<LayerItem> makeArrayList(InputStream inputStream)
	{
		ArrayList<LayerItem> layerList = new ArrayList<LayerItem>();

		// Lees json
		// context.getResources().openRawResource(R.raw.layers)
		String jsonLayers = new Scanner(inputStream).useDelimiter("\\A").next();
		//Log.i("HermLog", "jsonLayers: " + jsonLayers);

		try
		{
			JSONArray jArray = new JSONObject(jsonLayers).optJSONArray("layers");
			if (jArray.length() == 0) return null;

			for (int i=0; i < jArray.length(); i++)
			{
				JSONObject jObject = jArray.optJSONObject(i);
				LayerItem layerItem = new LayerItem();
				
				layerItem.setID(jObject.optString("ID"));
				layerItem.setTitle(jObject.optString("title"));
				layerItem.setShortTitle(jObject.optString("shortTitle"));
				layerItem.setServiceType(jObject.optString("serviceType"));
				layerItem.setServiceUrl(jObject.optString("serviceUrl"));
				layerItem.setWMSGetMapFeatureInfoQueryLayer(jObject.optString("WMSGetMapFeatureInfoQueryLayer"));
				layerItem.setMinx(jObject.optDouble("minx"));
				layerItem.setMaxx(jObject.optDouble("miny"));
				layerItem.setMiny(jObject.optDouble("maxx"));
				layerItem.setMaxy(jObject.optDouble("maxy"));
				layerItem.setMinZoom(jObject.optInt("minZoom"));
				layerItem.setMaxZoom(jObject.optInt("maxZoom"));
				layerItem.setVisibleByDefault(jObject.optBoolean("visibleByDefault"));
				layerItem.setOpacityDefault(jObject.optInt("opacityDefault"));
				layerItem.setQueryable(jObject.optBoolean("queryable"));
				layerItem.setBaseMap(jObject.optBoolean("isBaseMap"));
				
				layerList.add(layerItem);
			}
			
			//Log.i("HermLog", "jArray.length(): " + jArray.length());
			//Log.i("HermLog", "layerList: " + layerList.size());
			//arrayListTest(layerList);
		}
		catch (JSONException e)
		{
			e.printStackTrace();
			return null;
		}

		return layerList;
	}

	private static void arrayListTest(ArrayList<LayerItem> layerList)
	{
		for (LayerItem layerItem : layerList)
		{
			Log.i("HermLog", "arrayListTest():");
			Log.i("HermLog", layerItem.getTitle());
			Log.i("HermLog", layerItem.getServiceUrl());
			Log.i("HermLog", layerItem.getWMSGetMapFeatureInfoQueryLayer());
			Log.i("HermLog", String.valueOf(layerItem.getMinx()));
			Log.i("HermLog", String.valueOf(layerItem.getMaxx()));
			Log.i("HermLog", String.valueOf(layerItem.getMiny()));
			Log.i("HermLog", String.valueOf(layerItem.getMaxy()));
		}
	}
}
