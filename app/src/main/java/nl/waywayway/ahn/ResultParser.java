package nl.waywayway.ahn;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

// Parse json string naar hoogte in meters +NAP

public class ResultParser
{
	private static final String NODE_1 = "features";
	private static final String NODE_2 = "properties";
	private static final String NODE_3 = "GRAY_INDEX";
	
	// json string verwerken na download
	// geeft null indien geen waarde
	public static Double parse(String result)
	{
		try
		{
			JSONArray jArray = new JSONObject(result).optJSONArray(NODE_1);

			if (jArray.length() == 0) return null;

			double hoogte = jArray
				.optJSONObject(0)
				.optJSONObject(NODE_2)
				.optDouble(NODE_3);
			//Log.i("HermLog", "Hoogte: " + hoogte);

			return hoogte;
		}
		catch (JSONException e)
		{
			Log.i("HermLog", e.getStackTrace().toString());
			return null;
		}
	}
}
