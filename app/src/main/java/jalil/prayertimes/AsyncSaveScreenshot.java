package jalil.prayertimes;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;

public class AsyncSaveScreenshot extends AsyncTask<Void, Void, Void> {

    private WeakReference<Activity> context;
    Bitmap bitmap;
    String fileName;

    AsyncSaveScreenshot(Activity context, Bitmap bitmap, String fileName) {
        this.context = new WeakReference<>(context);
        this.bitmap = bitmap;
        this.fileName = fileName;
    }

    @Override
    protected Void doInBackground(Void... voids) {

        Activity activity = context.get();
        FileOutputStream fileOutputStream;

        try {

            File imageFile = new File(Variables.getFullPathShot(activity, fileName + ".png"));

            fileOutputStream = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);

            fileOutputStream.flush();
            fileOutputStream.close();

            Intent intentScanFile = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            intentScanFile.setData(Uri.fromFile(imageFile));
            activity.sendBroadcast(intentScanFile);

            Intent intentSendFile = new Intent(Intent.ACTION_SEND);
            Uri imageUri = FileProvider.getUriForFile(activity, activity.getPackageName() + ".screenshots", imageFile);
            intentSendFile.putExtra(Intent.EXTRA_STREAM, imageUri);
            intentSendFile.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intentSendFile.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intentSendFile.setType("image/png");
            activity.startActivity(Intent.createChooser(intentSendFile, "مشاركة الصورة : " + fileName));

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
