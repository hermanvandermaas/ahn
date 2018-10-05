package nl.waywayway.ahn;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.FileProvider;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

// Class voor maken en delen van kaartafbeelding

public class ShareImage
{
	private GoogleMap gMap;
	private Context context;
	
	public ShareImage(GoogleMap gMap, Context context)
	{
		this.gMap = gMap;
		this.context = context;
	}
	
	public void share()
	{
		gMap.snapshot(new GoogleMap.SnapshotReadyCallback()
			{
				@Override
				public void onSnapshotReady(Bitmap bitmap)
				{
					File file = new File(context.getCacheDir() + context.getResources().getString(R.string.share_image_path));
					FileOutputStream fileOut = null;

					try
					{
						fileOut = new FileOutputStream(file);
					}
					catch (FileNotFoundException e)
					{
						e.printStackTrace();
					}

					if (fileOut != null) bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOut);

					try
					{
						fileOut.close();
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
					
					shareImage(file);
				}
			});
	}

	// Deel afbeelding via andere app
	public void shareImage(File imageFile)
	{

		if (imageFile == null)
		{
			Toast.makeText(context, context.getResources().getString(R.string.no_image_for_sharing_available_message), Toast.LENGTH_SHORT).show();
			return;
		}

		Uri uriToImage = FileProvider.getUriForFile(context, context.getResources().getString(R.string.files_authority), imageFile);

		Intent shareIntent = ShareCompat.IntentBuilder.from((Activity) context)
			.setStream(uriToImage)
			.getIntent();

		shareIntent.setData(uriToImage);
		shareIntent.setType(context.getResources().getString(R.string.share_image_mime_type));
		shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

		if (shareIntent.resolveActivity(context.getPackageManager()) != null)
		{
			context.startActivity(shareIntent);
		}
	}
}
