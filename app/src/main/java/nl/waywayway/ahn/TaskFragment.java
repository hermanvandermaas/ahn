package nl.waywayway.ahn;

import android.content.*;
import android.os.*;
import android.support.v4.app.*;
import android.util.*;
import java.util.*;

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
		public void onPostExecute(String result);
	}
	
	private Context context;
	private TaskCallbacks callbacks;
	private DummyTask task;
	private boolean running;
	private static final String URL_FORMAT =
	"https://geodata.nationaalgeoregister.nl/ahn2/ows" +
	"?service=WMS" +
	"&version=1.3.0" +
	"&request=GetFeatureInfo" +
	"&layers=ahn2_05m_ruw" +
	"&query_layers=ahn2_05m_ruw" +
	"&styles=ahn2:ahn2_05m_detail" +
	"&bbox=%f,%f,%f,%f" +
	"&width=%d" +
	"&height=%d" +
	"&i=%d" +
	"&j=%d" +
	"&crs=EPSG:3857" +
	"&format=image/png" +
	"&info_format=application/json";
	
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

	/*****************************/
	/***** TASK FRAGMENT API *****/
	/*****************************/

	/**
	 * Start the background task.
	 */
	// parameter false: eerste download
	// parameter true: extra data downoaden bij endless scrolling
	public void start()
	{
		if (!running)
		{
			task = new DummyTask();
			task.execute();
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
	private class DummyTask extends AsyncTask<String, Integer, String>
	{
		public DummyTask()
		{
		}

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
		protected String doInBackground(String... url)
		{
			Log.i("HermLog", "doInBackground");

			DownloadJsonString downloader = new DownloadJsonString("url");
			String jsonstring = downloader.download();
			
			if (jsonstring == "Fout in DownloadJsonString!")
			{
				Log.i("HermLog", "TaskFragment doInBackground jsonstring: " + jsonstring);
			}
			else if (jsonstring != null)
			{
				parseResult(jsonstring);
			}
			
			return jsonstring;
			// Eind asynchrone taak
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
			Log.i("HermLog", "TaskFragment");
			
			// Proxy the call to the Activity
			callbacks.onPostExecute(result);
			running = false;
		}
		
		// json string verwerken na download
		// Zet json string per item in ArrayList<T>
		private void parseResult(String result)
		{
			Log.i("HermLog", "TaskFragment: parseResult()");
		}
	}
}
