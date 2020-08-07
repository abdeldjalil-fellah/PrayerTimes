package jalil.prayertimes;

import android.content.Context;
import android.os.AsyncTask;

import java.lang.ref.WeakReference;

public class AsyncVersion extends AsyncTask<Void, Void, String> {

    private WeakReference<Context> context;

    AsyncVersion(Context context) {
        this.context = new WeakReference<>(context);
    }

    @Override
    protected String doInBackground(Void... voids) {

        String versionUrl = Utilities.getResponseFromURL(Constants.URL_LATEST_GITHUB, true);

        if (versionUrl.isEmpty()) {
            return String.format("%s%s(للبحث عن أحدث إصدار يرجى الاتصال بالإنترنت)", Constants.ABOUT_VERSION, Constants.NEW_LINE);
        } else {
            String versionName = versionUrl.substring(versionUrl.lastIndexOf("/") + 1);
            if (versionName.equals(BuildConfig.VERSION_NAME)) {
                return String.format("%s (أحدث إصدار)", Constants.ABOUT_VERSION);
            } else {
                return String.format("%s%s%sيرجى تنزيل أحدث إصدار: %s%s", Constants.ABOUT_VERSION, Constants.NEW_LINE, Constants.NEW_LINE, Constants.NEW_LINE, versionUrl);
            }
        }
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);

        if (!result.equals(ActivityMain.prefLatestVersionMessage(context.get()))) {
            ActivityMain.updateLatestVersion(context.get(), result);

            if (context.get() instanceof IAsyncCompleted) {
                IAsyncCompleted iAsyncCompleted = (IAsyncCompleted) context.get();
                iAsyncCompleted.onVersion(result);
            }
        }
    }
}
