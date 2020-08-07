package jalil.prayertimes;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import java.lang.ref.WeakReference;

class AsyncLocationByName extends AsyncTask<Void, Void, String[]> {

    private ProgressDialog progressDialog;
    private WeakReference<Context> context;
    private int method;
    private String locationName;
    private String countryCode;
    private String language;

    AsyncLocationByName(Context context, String locationName, String countryCode, int method, String language) {
        this.context = new WeakReference<>(context);
        this.locationName = locationName;
        this.countryCode = countryCode;
        this.method = method;
        this.language = language;
    }

    @Override
    protected void onPreExecute() {
        progressDialog = new ProgressDialog(context.get()) {
            @Override
            public void onBackPressed() {
                progressDialog.dismiss();
                AsyncLocationByName.this.cancel(true);
            }
        };
        progressDialog.setMessage("يرجى الانتظار...");
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    @Override
    protected String[] doInBackground(Void... voids) {
        return Utilities.getLocationDetails(context.get(), method, language, locationName, countryCode);
    }

    @Override
    protected void onPostExecute(String[] result) {
        super.onPostExecute(result);

        IAsyncCompleted iAsyncCompleted = (IAsyncCompleted)context.get();

        if (iAsyncCompleted != null) {
            if (result != null) {
                iAsyncCompleted.onLocation(result);
            }

            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        }
    }
}