package jalil.prayertimes;

import android.app.ProgressDialog;
import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;

import java.lang.ref.WeakReference;

class AsyncLocationByCoordinates extends AsyncTask<Void, Void, String[]> {

    private ProgressDialog progressDialog;
    private WeakReference<Context> context;
    private Location location;
    private int method;
    private String language;

    AsyncLocationByCoordinates(Context context, Location location, int method, String language) {
        this.context = new WeakReference<>(context);
        this.location = location;
        this.method = method;
        this.language = language;
    }

    @Override
    protected void onPreExecute() {
        progressDialog = new ProgressDialog(context.get()) {
            @Override
            public void onBackPressed() {
                progressDialog.dismiss();
                AsyncLocationByCoordinates.this.cancel(true);
            }
        };
        progressDialog.setMessage("يرجى الانتظار...");
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    @Override
    protected String[] doInBackground(Void... voids) {
        return Utilities.getLocationDetails(context.get(), method, language, this.location.getLatitude(), this.location.getLongitude());
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
