package nl.waywayway.ahn;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.fragment.app.Fragment;

import java.net.URL;
import java.util.ArrayList;

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
        public void onPostExecute(ArrayList<Double> result);
    }

    private Context context;
    private TaskCallbacks callbacks;
    private DummyTask task;
    private boolean running;

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
            task.cancel(true);
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
    private class DummyTask extends AsyncTask<URL, Integer, ArrayList<Double>>
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
        protected ArrayList<Double> doInBackground(URL... urls)
        {
            //Log.i("HermLog", "doInBackground");
            //Log.i("HermLog", "urls[]: " + Arrays.toString(urls));
            //if (urls[0] == null) return "n/a";

            ArrayList<Double> elevations = new ArrayList<Double>();
            int total = urls.length;
            int i = 0;

            for (URL url : urls)
            {
                if (isCancelled()) break;

                if (url == null)
                {
                    elevations.add(null);
                    continue;
                }

                DownloadJsonString downloader = new DownloadJsonString(url);
                String jsonstring = downloader.download();

                if (jsonstring.equals("Fout in DownloadJsonString!") || jsonstring == null)
                {
                    elevations.add(null);
                    continue;
                }
                else
                {
                    Double elevation = ResultParser.parse(jsonstring);
                    elevations.add(elevation);
                }

                publishProgress((int) ((i / (float) total) * 100));
                i++;
            }

            return elevations;
        }

        @Override
        protected void onProgressUpdate(Integer... percent)
        {
            // Proxy the call to the Activity.
            callbacks.onProgressUpdate(percent[0]);
        }

        @Override
        protected void onCancelled()
        {
            // Proxy the call to the Activity.
            callbacks.onCancelled();
            running = false;
        }

        @Override
        protected void onPostExecute(ArrayList<Double> result)
        {
            //Log.i("HermLog", "TaskFragment");

            // Proxy the call to the Activity
            callbacks.onPostExecute(result);
            running = false;
        }
    }
}
