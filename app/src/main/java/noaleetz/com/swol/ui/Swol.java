package noaleetz.com.swol.ui;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseObject;

import noaleetz.com.swol.models.Comments;
import noaleetz.com.swol.models.Likes;
import noaleetz.com.swol.models.Workout;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.parse.facebook.ParseFacebookUtils;


public class Swol extends Application {

    @Override
    public void onCreate() {
        super.onCreate();


        // Use for troubleshooting -- remove this line for production
        Parse.setLogLevel(Parse.LOG_LEVEL_DEBUG);

        // Use for monitoring Parse OkHttp traffic
        // Can be Level.BASIC, Level.HEADERS, or Level.BODY
        // See http://square.github.io/okhttp/3.x/logging-interceptor/ to see the options.
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        builder.networkInterceptors().add(httpLoggingInterceptor);


        // set applicationId, and server server based on the values in the Heroku settings.
        // clientKey is not needed unless explicitly configured
        // any network interceptors must be added with the Configuration Builder given this syntax
        final Parse.Configuration configuration = new Parse.Configuration.Builder(this)
                .applicationId("swol")
                .clientKey("TeamNonaRules")
                .server("http://swol-teamnona.herokuapp.com/parse")
                .build();
        ParseObject.registerSubclass(Workout.class);
        ParseObject.registerSubclass(Comments.class);
        ParseObject.registerSubclass(Likes.class);


        Parse.initialize(configuration);

        ParseFacebookUtils.initialize(this);
    }
}
