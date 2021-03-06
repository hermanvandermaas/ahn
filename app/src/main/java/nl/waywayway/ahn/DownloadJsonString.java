package nl.waywayway.ahn;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DownloadJsonString
{
	private URL url;

	public DownloadJsonString(URL url)
	{
		this.url = url;
	}

	public String download()
	{
		OkHttpClient mClient = new OkHttpClient.Builder()
			.readTimeout(30, TimeUnit.SECONDS)
			.build();

		Request mRequest = new Request.Builder()
			.url(url)
			.build();

		try
		{
			Response mResponse = mClient
				.newCall(mRequest)
				.execute();

			if (!mResponse.isSuccessful())
			{
				throw new IOException("Unexpected code in DownloadJsonString: " + mResponse);
			}

			//Log.i("HermLog", "Gedownload");

			return mResponse.body().string();
		}
		catch (IOException e)
		{
			//Log.i("HermLog", "Exception in DownloadJsonString");
		}

		return "Fout in DownloadJsonString!";
	}
}
