package nl.waywayway.ahn;

import android.content.*;
import android.os.*;
import android.support.v4.app.*;
import android.util.*;
import java.net.*;
import java.util.*;
import org.json.*;

/**
 * TaskFragment manages a single background task and retains itself across
 * configuration changes.
 */

public class TaskFragment extends Fragment
{
	/**
	 * Callback interface through which the fragment can report the task's
	 * progress and results back to the Activity.
	 */
	public interface TaskCallbacks
	{
		public void onPreExecute();
		public void onProgressUpdate(int percent);
		public void onCancelled();
		public void onPostExecute(String result, String layerInfo);
	}

	private Context context;
	private TaskCallbacks callbacks;
	private DummyTask task;
	private boolean running;
	private String layerInfo;
	
	public void setLayerInfo(String layerInfo)
	{
		this.layerInfo = layerInfo;
	}

	public String getLayerInfo()
	{
		return layerInfo;
	}

	/**
	 * Hold a reference to the parent Activity so we can report the task's current
	 * progress and results. The Android framework will pass us a reference to the
	 * newly created Activity after each configuration change.
	 */
	@Override
	public void onAttach(Context context)
	{
		super.onAttach(context);
		this.context = context;

		if (!(context instanceof TaskCallbacks))
		{
			throw new IllegalStateException("Activity must implement the TaskCallbacks interface.");
		}

		// Hold a reference to the parent Activity so we can report back the task's
		// current progress and results.
		callbacks = (TaskCallbacks) context;
	}

	/**
	 * This method is called once when the Fragment is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
	}

	/**
	 * Note that this method is <em>not</em> called when the Fragment is being
	 * retained across Activity instances. It will, however, be called when its
	 * parent Activity is being destroyed for good (such as when the user clicks
	 * the back button, etc.).
	 */
	@Override
	public void onDestroy()
	{
		super.onDestroy();
		cancel();
	}

	// Task fragment API

	/**
	 * Start the background task.
	 */
	// parameter false: eerste download
	// parameter true: extra data downoaden bij endless scrolling
	public void start(URL[] urls)
	{
		if (!running)
		{
			task = new DummyTask();
			task.execute(urls);
			running = true;
		}
	}

	/**
	 * Cancel the background task.
	 */
	public void cancel()
	{
		if (running)
		{
			task.cancel(false);
			task = null;
			running = false;
		}
	}

	/**
	 * Returns the current state of the background task.
	 */
	public boolean isRunning()
	{
		return running;
	}

	/**
	 * A dummy task that performs some (dumb) background work and proxies progress
	 * updates and results back to the Activity.
	 */
	private class DummyTask extends AsyncTask<URL, Integer, String>
	{
		public DummyTask()
		{}

		@Override
		protected void onPreExecute()
		{
			// Proxy the call to the Activity.
			callbacks.onPreExecute();
			running = true;
		}

		/**
		 * Note that we do NOT call the callback object's methods directly from the
		 * background thread, as this could result in a race condition.
		 */
		@Override
		protected String doInBackground(URL... urls)
		{
			//Log.i("HermLog", "doInBackground");
			//Log.i("HermLog", "urls[]: " + Arrays.toString(urls));

			if (urls[0] == null) return "n/a";
			DownloadJsonString downloader = new DownloadJsonString(urls[0]);
			String jsonstring = downloader.download();

			if (jsonstring.equals("Fout in DownloadJsonString!") || jsonstring == null)
			{
				//Log.i("HermLog", "doInBackground: fout");
				return "Download niet gelukt";
			}
			else
			{
				//Log.i("HermLog", "doInBackground: jsonstring: " + jsonstring);
				Double hoogte = parseResult(jsonstring);
				if (hoogte == null || hoogte > 10000d || hoogte < -10000d) return "n/a";
				String hoogteAfgerond = String.format("%.2f", hoogte);
				if (hoogte > 0) hoogteAfgerond = "+" + hoogteAfgerond;

				return hoogteAfgerond;
			}
		}

		@Override
		protected void onProgressUpdate(Integer... percent)
		{
			// Proxy the call to the Activity.
			// mCallbacks.onProgressUpdate(percent[0]);
		}

		@Override
		protected void onCancelled()
		{
			// Proxy the call to the Activity.
			callbacks.onCancelled();
			running = false;
		}

		@Override
		protected void onPostExecute(String result)
		{	
			//Log.i("HermLog", "TaskFragment");

			// Proxy the call to the Activity
			callbacks.onPostExecute(result, layerInfo);
			running = false;
		}

		// json string verwerken na download
		// geeft null indien geen waarde
		private Double parseResult(String result)
		{
			//Log.i("HermLog", "TaskFragment: parseResult()");

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
		}
	}
}
