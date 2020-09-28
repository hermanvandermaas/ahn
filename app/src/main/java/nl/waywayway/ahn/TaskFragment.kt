package nl.waywayway.ahn

import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import androidx.fragment.app.Fragment
import java.net.URL
import java.util.*

/**
 * TaskFragment manages a single background task and retains itself across
 * configuration changes.
 */
class TaskFragment : Fragment() {
    /**
     * Callback interface through which the fragment can report the task's
     * progress and results back to the Activity.
     */
    interface TaskCallbacks {
        fun onPreExecute()
        fun onProgressUpdate(percent: Int)
        fun onCancelled()
        fun onPostExecute(result: ArrayList<Double?>?)
    }

    private var context: Context? = null
    private var callbacks: TaskCallbacks? = null
    private var task: DummyTask? = null

    /**
     * Returns the current state of the background task.
     */
    var isRunning = false
        private set

    /**
     * Hold a reference to the parent Activity so we can report the task's current
     * progress and results. The Android framework will pass us a reference to the
     * newly created Activity after each configuration change.
     */
    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.context = context
        check(context is TaskCallbacks) { "Activity must implement the TaskCallbacks interface." }

        // Hold a reference to the parent Activity so we can report back the task's
        // current progress and results.
        callbacks = context
    }

    /**
     * This method is called once when the Fragment is first created.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    /**
     * Note that this method is *not* called when the Fragment is being
     * retained across Activity instances. It will, however, be called when its
     * parent Activity is being destroyed for good (such as when the user clicks
     * the back button, etc.).
     */
    override fun onDestroy() {
        super.onDestroy()
        cancel()
    }
    // Task fragment API
    /**
     * Start the background task.
     */
    fun start(urls: ArrayList<URL>) {
        if (!isRunning) {
            task = DummyTask()
            task!!.execute(*urls.toTypedArray())
            isRunning = true
        }
    }

    /**
     * Cancel the background task.
     */
    fun cancel() {
        if (isRunning) {
            task!!.cancel(true)
            task = null
            isRunning = false
        }
    }

    /**
     * A dummy task that performs some (dumb) background work and proxies progress
     * updates and results back to the Activity.
     */
    private inner class DummyTask : AsyncTask<URL?, Int?, ArrayList<Double?>>() {
        override fun onPreExecute() {
            // Proxy the call to the Activity.
            callbacks!!.onPreExecute()
            isRunning = true
        }

        /**
         * Note that we do NOT call the callback object's methods directly from the
         * background thread, as this could result in a race condition.
         */
        protected override fun doInBackground(vararg urls: URL): ArrayList<Double?> {
            //Log.i("HermLog", "doInBackground");
            //Log.i("HermLog", "urls[]: " + Arrays.toString(urls));
            //if (urls[0] == null) return "n/a";
            val elevations = ArrayList<Double?>()
            val total = urls.size
            var i = 0
            for (url in urls) {
                if (isCancelled) break
                if (url == null) {
                    elevations.add(null)
                    continue
                }
                val downloader = DownloadJsonString(url)
                val jsonstring = downloader.download()
                if (jsonstring == "Fout in DownloadJsonString!" || jsonstring == null) {
                    elevations.add(null)
                    continue
                } else {
                    val elevation = ResultParser.parse(jsonstring)
                    elevations.add(elevation)
                }
                publishProgress((i / total.toFloat() * 100).toInt())
                i++
            }
            return elevations
        }

        protected override fun onProgressUpdate(vararg percent: Int) {
            // Proxy the call to the Activity.
            callbacks!!.onProgressUpdate(percent[0])
        }

        override fun onCancelled() {
            // Proxy the call to the Activity.
            callbacks!!.onCancelled()
            isRunning = false
        }

        override fun onPostExecute(result: ArrayList<Double?>) {
            //Log.i("HermLog", "TaskFragment");

            // Proxy the call to the Activity
            callbacks!!.onPostExecute(result)
            isRunning = false
        }
    }
}