package noaleetz.com.swol;


import android.content.Intent;
import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Location;
import android.media.Image;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.tasks.OnSuccessListener;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import noaleetz.com.swol.models.Workout;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "LOCATION";
    private ActionBarDrawerToggle drawerToggle;

    // maps stuff
    private SupportMapFragment mapFragment;
    private GoogleMap map;
    private LocationRequest mLocationRequest;
    Location mCurrentLocation;
    Location mLastLocation;

    private long UPDATE_INTERVAL = 60000;  /* 60 secs */
    private long FASTEST_INTERVAL = 5000; /* 5 secs */
    private final static String KEY_LOCATION = "location";
    private FusedLocationProviderClient mFusedLocationClient;


    public static final int REQUEST_LOCATION_PERMISSION = 1;
    ParseGeoPoint currentGeoPoint;



    @BindView(R.id.fab)
    FloatingActionButton fab;

    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawer;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.nvView)
    NavigationView nvDrawer;


    @OnClick(R.id.fab)
    public void onClick(View view) {
        fab.hide();
        AddFragment addfragment = new AddFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.flContent, addfragment).addToBackStack(null);
        transaction.commit();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        // Let toolbar replace action bar

        setSupportActionBar(toolbar);
        setupDrawerContent(nvDrawer);
        drawerToggle = setupDrawerToggle();


        mDrawer.addDrawerListener(drawerToggle);



        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        getLocation();

        final View hView = nvDrawer.getHeaderView(0);

        // TODO: all of these try catches look gross--fix it

        // sets the full name in the drawer
        TextView navName = hView.findViewById(R.id.tvNavName);
        // sets the username in the drawer
        final TextView navUserame = hView.findViewById(R.id.tvNavUsername);
        // sets the profile pic in the drawer
        final ImageView ivAvatar = hView.findViewById(R.id.ivAvatar);


        try {
            navName.setText(ParseUser.getCurrentUser().fetchIfNeeded().getString("name"));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (isFacebookUser(ParseUser.getCurrentUser())) {
            navUserame.setVisibility(View.GONE);
            // pulls the profile pic
            GraphRequest request = GraphRequest.newGraphPathRequest(
                    AccessToken.getCurrentAccessToken(),
                    "100027668556706/picture?redirect=0&fields=url",
                    new GraphRequest.Callback() {
                        @Override
                        public void onCompleted(GraphResponse response) {
                            try {
                                Log.d("FBPP", response.getJSONObject().optJSONObject("data").get("url").toString());
                                Glide.with(hView).load(response.getJSONObject().optJSONObject("data").get("url").toString())
                                        .apply(RequestOptions.circleCropTransform()
                                                .placeholder(R.drawable.ic_person)
                                                .error(R.drawable.ic_person))
                                        .into(ivAvatar);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });

            request.executeAsync();

        } else {
            navUserame.setText("@" + ParseUser.getCurrentUser().getUsername());
            navUserame.setVisibility(View.VISIBLE);

            try {
                Glide.with(hView).load(ParseUser.getCurrentUser().fetchIfNeeded().getParseFile("profilePicture").getFile())
                        .apply(RequestOptions.circleCropTransform()
                                .placeholder(R.drawable.ic_person)
                                .error(R.drawable.ic_person))
                        .into(ivAvatar);
            } catch (ParseException e) {
                e.printStackTrace();
            }

        }


        // i have no idea what this does but if it ain't broke don't fix it
        if (savedInstanceState != null && savedInstanceState.keySet().contains(KEY_LOCATION)) {
            // Since KEY_LOCATION was found in the Bundle, we can be sure that mCurrentLocation
            // is not null.
            mCurrentLocation = savedInstanceState.getParcelable(KEY_LOCATION);

        }


    }


    private void getLocation() {
        // If user has not yet given permission - ask for permission - dialog box asking for permission pops up
        // Upon return, onRequestPermissionsResult is called
        // getLocation is called AGAIN and this time will skip to the else
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]
                            {Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        } else {
            Log.d(TAG, "getLocation: permissions granted");
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            //location = null;

                            if (location != null) {
                                Log.d(TAG, "Got last known location");
                                // Logic to handle location object
                                mLastLocation = location;
                                currentGeoPoint = new ParseGeoPoint(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                                Log.d(TAG, currentGeoPoint.toString());
                                ParseUser.getCurrentUser().put("currentLocation", currentGeoPoint);
                                ParseUser.getCurrentUser().saveInBackground();
                                Log.d(TAG,"geopoint posted to parse)");


                            } else {


                                // TODO- handle null location

                                Log.d(TAG,"location is found to be null");
                                Toast.makeText(getApplication().getBaseContext(), "We weren't able to identify your location",
                                        Toast.LENGTH_LONG).show();

                            }
                        }
                    });
        }
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.flContent, new FeedFragment()).commit();


    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION_PERMISSION:
                // If the permission is granted, get the location,
                // otherwise, show a Toast
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getLocation();
                } else {
                    Toast.makeText(this,
                            R.string.location_permission_denied,
                            Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private ActionBarDrawerToggle setupDrawerToggle() {
        // NOTE: Make sure you pass in a valid toolbar reference.  ActionBarDrawToggle() does not require it
        // and will not render the hamburger icon without it.
        return new ActionBarDrawerToggle(this, mDrawer, toolbar, R.string.drawer_open, R.string.drawer_close);
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        selectDrawerItem(menuItem);
                        return true;
                    }
                });
    }


    public void selectDrawerItem(MenuItem menuItem) {
        // Create a new fragment and specify the fragment to show based on nav item clicked
        Fragment fragment = null;
        Class fragmentClass;
        switch (menuItem.getItemId()) {
            case R.id.nav_feed_fragment:
                fab.show();
                fragmentClass = FeedFragment.class;
                break;
            case R.id.nav_map_fragment:
                fab.show();
                // if there is no api key, then throw this exception
                if (TextUtils.isEmpty(getResources().getString(R.string.api_key))) {
                    throw new IllegalStateException("You forgot to supply a Google Maps API key");
                }
                fragmentClass = MapFragment.class;
                break;
            case R.id.nav_profile_fragment:
                fab.hide();
                fragmentClass = ProfileFragment.class;
                break;
            case R.id.nav_logout:
                ParseUser.logOut();
                Intent i = new Intent(this, DispatchActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
                finish();
            default:
                fragmentClass = FeedFragment.class;
        }

        try {
            fragment = (Fragment) fragmentClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Insert the fragment by replacing any existing fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.flContent, fragment).commit();

        // Highlight the selected item has been done by NavigationView
        menuItem.setChecked(true);
        // Set action bar title
        setTitle(menuItem.getTitle());
        // Close the navigation drawer
        mDrawer.closeDrawers();
    }


    // ...


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggles
        drawerToggle.onConfigurationChanged(newConfig);
    }

<<<<<<< HEAD

    public void changeToDetailFragment(Workout workout) {
        //TODO that
        DetailFragment detailFragment = new DetailFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable("workout",workout);
        detailFragment.setArguments(bundle);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.flContent,detailFragment).addToBackStack(null);
        transaction.commit();





    }






=======
    public boolean isFacebookUser(ParseUser user) {
        if (user.get("authData") == null) return false;
        JSONObject authData = user.getJSONObject("authData");
        return authData.has("facebook");
    }
>>>>>>> master


}
