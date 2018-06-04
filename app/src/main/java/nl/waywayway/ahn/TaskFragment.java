package nl.waywayway.ahn;

import android.app.*;
import android.content.*;
import android.os.*;
import android.support.v4.app.*;
import android.util.*;
import java.net.*;
import java.util.*;
import org.json.*;

import android.support.v4.app.Fragment;

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
		public void onPostExecute(ArrayList<String> result, ArrayList<String> layerInfo);
	}

	private Context context;
	private TaskCallbacks callbacks;
	private DummyTask task;
	private boolean running;
	ArrayList<String> layerInfoList = new ArrayList<String>();

	public void setLayerInfoList(ArrayList<String> layerInfoList)
	{
		this.layerInfoList = layerInfoList;
	}

	public ArrayList<String> getLayerInfoList()
	{
		return layerInfoList;
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
	public void start(ArrayList<URL> urls)
	{
		if (!running)
		{
			task = new DummyTask();
			task.execute(urls.toArray(new URL[0]));
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
	private class DummyTask extends AsyncTask<URL, Integer, ArrayList<String>>
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
		protected ArrayList<String> doInBackground(URL... urls)
		{
			//Log.i("HermLog", "doInBackground");
			//Log.i("HermLog", "urls[]: " + Arrays.toString(urls));
			//if (urls[0] == null) return "n/a";

			ArrayList<String> hoogtes = new ArrayList<String>();

			for (URL url : urls)
			{
				if (url == null)
				{
					hoogtes.add(context.getResources().getString(R.string.not_available_UI));
					continue;
				}

				DownloadJsonString downloader = new DownloadJsonString(url);
				String jsonstring = downloader.download();

				if (jsonstring.equals("Fout in DownloadJsonString!") || jsonstring == null)
				{
					//Log.i("HermLog", "doInBackground: fout");
					return null;
				}
				else
				{
					String hoogteAfgerond;
					//Log.i("HermLog", "doInBackground: jsonstring: " + jsonstring);
					Double hoogte = parseResult(jsonstring);

					if (hoogte == null || hoogte > 10000d || hoogte < -10000d)
					{
						hoogteAfgerond = context.getResources().getString(R.string.not_available_UI);
					}
					else
					{
						hoogteAfgerond = String.format("%.2f", hoogte);
						if (hoogte > 0) hoogteAfgerond = "+" + hoogteAfgerond;
					}

					hoogtes.add(hoogteAfgerond);
				}
			}

			return hoogtes;
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
		protected void onPostExecute(ArrayList<String> result)
		{	
			//Log.i("HermLog", "TaskFragment");

			// Proxy the call to the Activity
			callbacks.onPostExecute(result, layerInfoList);
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
