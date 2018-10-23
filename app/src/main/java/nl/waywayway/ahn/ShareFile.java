package nl.waywayway.ahn;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.FileProvider;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

// Class voor delen van afbeelding of tekstbestand tussen apps, zonder toestemming om bestanden te schrijven

public class ShareFile {
    private Context context;
    private Bitmap bitmap;
    private String csvData;
    private String fileName;
    private String fileAuthority;
    private String noFileAvailableMessage;
    private String mimeType;

    private ShareFile(Context context, Bitmap bitmap, String csvData, String fileName, String fileAuthority, String noFileAvailableMessage, String mimeType) {
        this.context = context;
        this.bitmap = bitmap;
        this.csvData = csvData;
        this.fileName = fileName;
        this.fileAuthority = fileAuthority;
        this.noFileAvailableMessage = noFileAvailableMessage;
        this.mimeType = mimeType;
    }

    public static ShareFile getInstance(Context context, Bitmap bitmap, String csvData, String fileName, String fileAuthority, String noFileAvailableMessage, String mimeType) {
        return new ShareFile(context, bitmap, csvData, fileName, fileAuthority, noFileAvailableMessage, mimeType);
    }

    public void share() {
        File file = new File(context.getCacheDir() + fileName);
        FileOutputStream fileOut = null;

        try {
            fileOut = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        if (fileOut != null && bitmap != null) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOut);
        } else if (fileOut != null && csvData != null) {
            try {
                fileOut.write(csvData.getBytes(Charset.defaultCharset()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (fileOut != null && bitmap != null) {
            try {
                fileOut.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        shareImage(file);
    }

    // Deel afbeelding via andere app
    private void shareImage(File imageFile) {

        if (imageFile == null) {
            Toast.makeText(context, noFileAvailableMessage, Toast.LENGTH_SHORT).show();
            return;
        }

        Uri uriToImage = FileProvider.getUriForFile(context, fileAuthority, imageFile);

        Intent shareIntent = ShareCompat.IntentBuilder.from((Activity) context)
                .setStream(uriToImage)
                .getIntent();

        shareIntent.setData(uriToImage);
        shareIntent.setType(mimeType);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        if (shareIntent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(shareIntent);
        }
    }
}
