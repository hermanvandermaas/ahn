package nl.waywayway.ahn;

import android.content.*;
import android.util.*;
import java.util.*;
import org.json.*;

// Opslaan en ophalen voorkeuren

public class LayersSaveAndRestore
{
	private static final String PREFERENCES_FILENAME = "ahn_preferences";
	private Context context;
	private String preferenceKey;

	private LayersSaveAndRestore(Context context, String key)
	{
		this.context = context;
		this.preferenceKey = key;
	}
	
	public static LayersSaveAndRestore getInstance(Context cContext, String cKey)
	{
		return new LayersSaveAndRestore(cContext, cKey);
	}

	// Opslaan in SharedPreferences
	// als json integer array met indeling: 
	// [laag zichtbaar: 0 of 1, dekkendheid laag: 0-100]
	public Boolean save(int visible, int opacity)
	{
		int[] array = new int[]{visible, opacity};
		
		SharedPreferences pref = context.getSharedPreferences(PREFERENCES_FILENAME, context.MODE_PRIVATE);
		SharedPreferences.Editor edit = pref.edit();
		edit.putString(preferenceKey, Arrays.toString(array));
		Log.i("HermLog", "save: " + Arrays.toString(array));

		return edit.commit();
	}
	
	// Ophalen uit SharedPreferences
	// kan null zijn
	public int[] restore()
	{
		SharedPreferences sharedPref = context.getSharedPreferences(PREFERENCES_FILENAME, context.MODE_PRIVATE);
		String prefDefault = "";
		String savedString = sharedPref.getString(preferenceKey, prefDefault);
		
		if (savedString.isEmpty()) return null;
		
		JSONArray jsonArray;
		
		try
		{
			jsonArray = new JSONArray(savedString);
		}
		catch (JSONException e)
		{
			e.printStackTrace();
			return null;
		}
		
		int[] savedValues = new int[jsonArray.length()];
		
		for (int i = 0; i < jsonArray.length(); i++)
			savedValues[i] = jsonArray.optInt(i);
			
		Log.i("HermLog", "restore: " + Arrays.toString(savedValues));
		
		return savedValues;
	}
}
