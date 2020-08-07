package jalil.prayertimes;

import android.app.Application;

import com.parse.Parse;

public class ApplicationMain extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId(Secured.parseApplicationId)
                .clientKey(Secured.parseClientKey)
                .server("https://parseapi.back4app.com")
                .build()
        );
    }
}