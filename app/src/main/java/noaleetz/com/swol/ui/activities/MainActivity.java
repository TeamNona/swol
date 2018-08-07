package noaleetz.com.swol.ui.activities;


import android.content.Intent;
import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import noaleetz.com.swol.R;
import noaleetz.com.swol.models.Workout;
import noaleetz.com.swol.ui.adapters.ClusterWindowAdapter;
import noaleetz.com.swol.ui.fragments.AddFragment;
import noaleetz.com.swol.ui.fragments.DetailFragment;
import noaleetz.com.swol.ui.fragments.FeedFragment;
import noaleetz.com.swol.ui.fragments.MapFragment;
import noaleetz.com.swol.ui.fragments.ProfileFragment;

public class MainActivity extends AppCompatActivity implements AddFragment.NewMapItemListener, ClusterWindowAdapter.itemClickListener {

    private static final String TAG = "LOCATION";
    private ActionBarDrawerToggle drawerToggle;

    // maps stuff
    Location mCurrentLocation;
    Location mLastLocation;

    private long UPDATE_INTERVAL = 60000;  /* 60 secs */
    private long FASTEST_INTERVAL = 5000; /* 5 secs */
    private final static String KEY_LOCATION = "location";
    private FusedLocationProviderClient mFusedLocationClient;


    public static final int REQUEST_LOCATION_PERMISSION = 1;
    ParseGeoPoint currentGeoPoint;

    MapFragment mapFragment = new MapFragment();

    @BindView(R.id.fab)
    FloatingActionButton fab;

    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawer;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.navigation_drawer)
    NavigationView nvDrawer;
    @BindView(R.id.navigation_drawer_bottom)
    NavigationView nvDrawerBottom;

    ImageView ivAvatar;

    Bitmap bitmapProfilePicture;

    public static final int MY_PERMISSIONS_REQUEST_GALLERY = 98;
    public static final int MY_PERMISSIONS_REQUEST_CAMERA = 99;
    private static int RESULT_LOAD_IMAGE = 1;


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
        setupDrawerContent(nvDrawerBottom);
        drawerToggle = setupDrawerToggle();


        mDrawer.addDrawerListener(drawerToggle);


        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        getLocation();

        ParseUser currentUser = ParseUser.getCurrentUser();

        final View hView = nvDrawer.getHeaderView(0);

        // TODO: all of these try catches look gross--fix it

        // sets the full name in the drawer
        TextView navName = hView.findViewById(R.id.tvNavName);
        // sets the username in the drawer
        final TextView navUserame = hView.findViewById(R.id.tvNavUsername);

        navUserame.setText("@" + currentUser.getUsername());
        navUserame.setVisibility(View.VISIBLE);

        // sets the profile pic in the drawer
        // final ImageView ivAvatar = hView.findViewById(R.id.ivAvatar);
        ivAvatar = hView.findViewById(R.id.ivAvatar);


        try {
            navName.setText(currentUser.fetchIfNeeded().getString("name"));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (isFacebookUser(currentUser) && currentUser.getParseFile("profilePicture") == null) {


            // pulls the profile pic
            String url = "https://graph.facebook.com/" + getFBID(currentUser) + "/picture?type=large";


            Glide.with(this).load(url)
                    .apply(RequestOptions.circleCropTransform()
                            .placeholder(R.drawable.ic_person)
                            .error(R.drawable.ic_person))
                    .into(ivAvatar);



            // save profile image onto parse
//                Drawable drawable = ivAvatar.getDrawable();
//                Bitmap bitmap = convertToBitmap(drawable, 500, 500);
//                ParseFile parseFile = conversionBitmapParseFile(bitmap);
//                currentUser.put("profilePicture", parseFile);
//                currentUser.saveInBackground(new SaveCallback() {
//                    @Override
//                    public void done(ParseException e) {
//                        if (e == null) {
//                            Log.d("MainFragment", "Save ProfilePicture successful");
//
//                        } else {
//                            e.printStackTrace();
//                            Log.e("AddFragment", "Save ProfilePicture was not successful");
//                        }
//                    }
//                });

                new DownloadImageTask(ivAvatar)
                        .execute(url);


            if (currentUser.get("username").toString().length() >= 25) {
                String name = currentUser.get("name").toString().toLowerCase();
                String[] splitName = name.split(" ");
                name = splitName[0];
                for (int i = 1; i < splitName.length; i++) {
                    name = name + "_" + splitName[i];
                }
                currentUser.setUsername(name);
            }


//            GraphRequest request = GraphRequest.newGraphPathRequest(
//                    AccessToken.getCurrentAccessToken(),
//                    "100027668556706/picture?redirect=0&fields=url",
//                    new GraphRequest.Callback() {
//                        @Override
//                        public void onCompleted(GraphResponse response) {
//                            try {
//                                Log.d("FBPP", response.getJSONObject().getJSONObject("picture").optJSONObject("data").get("url").toString());
//                                Glide.with(hView).load("https://graph.facebook.com/" + getFBID() + "/picture?type=large")
//                                //Glide.with(hView).load(response.getJSONObject().getJSONObject("picture").optJSONObject("url").get("url").toString())
//                                        .apply(RequestOptions.circleCropTransform()
//                                                .placeholder(R.drawable.ic_person)
//                                                .error(R.drawable.ic_person))
//                                        .into(ivAvatar);
//                            } catch (JSONException e) {
//                                e.printStackTrace();
//                                Log.e("No work", "no work");
//                            }
//                        }
//                    });
//
//            request.executeAsync();

        } else {

            if (currentUser.getParseFile("profilePicture") == null) {
                Glide.with(hView).load(R.drawable.ic_person)
                        .apply(RequestOptions.circleCropTransform()
                                .placeholder(R.drawable.ic_person)
                                .error(R.drawable.ic_person))
                        .into(ivAvatar);
            } else {
                try {
                    Glide.with(hView).load(currentUser.fetchIfNeeded().getParseFile("profilePicture").getFile())
                            .apply(RequestOptions.circleCropTransform()
                                    .placeholder(R.drawable.ic_person)
                                    .error(R.drawable.ic_person))
                            .into(ivAvatar);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
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
                                Log.d(TAG, "geopoint posted to parse)");


                            } else {


                                // TODO- handle null location

                                Log.d(TAG, "location is found to be null");
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
            case MY_PERMISSIONS_REQUEST_GALLERY:
                // If the permission is granted, get the location,
                // otherwise, show a Toast
                if (!(grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    Toast.makeText(this,
                            R.string.gallery_permission_denied,
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case MY_PERMISSIONS_REQUEST_CAMERA:
                // If the permission is granted, get the location,
                // otherwise, show a Toast
                if (!(grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {

                    Toast.makeText(this,
                            R.string.camera_permission_denied,
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
        navigationView.getBackground().setAlpha(150);
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
        FragmentManager fragmentManager = getSupportFragmentManager();
        switch (menuItem.getItemId()) {
            case R.id.nav_feed_fragment:
                fab.show();
                fragmentManager.beginTransaction().replace(R.id.flContent, new FeedFragment()).addToBackStack(null).commit();
                break;
            case R.id.nav_map_fragment:
                fab.show();
                // if there is no api key, then throw this exception
                if (TextUtils.isEmpty(getResources().getString(R.string.api_key))) {
                    throw new IllegalStateException("You forgot to supply a Google Maps API key");
                }
                // TODO: pass through the current location here so we don't have to find it twice
                fragmentManager.beginTransaction().replace(R.id.flContent, mapFragment).addToBackStack("map").commit();
                break;
            case R.id.nav_profile_fragment:
                fab.hide();
                changeToProfileFragment(ParseUser.getCurrentUser());
                mDrawer.closeDrawers();
                break;
            case R.id.nav_rate_us_fragment:
                return;
            case R.id.nav_logout:
                ParseUser.logOut();
                Intent i = new Intent(this, DispatchActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
                finish();
            default:
                return;
        }

        // Highlight the selected item has been done by NavigationView
        menuItem.setChecked(true);
        // Set action bar title
        setTitle(menuItem.getTitle());
        // Close the navigation drawer
        mDrawer.closeDrawers();
    }

    @Override
    public void onWorkoutSelected(Workout workout) {
        mapFragment.onWorkoutSelected(workout);
    }

    @Override
    public void updateMap() {
        mapFragment.addMarker();
    }

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

    public void changeToDetailFragment(Workout workout) {
        DetailFragment detailFragment = new DetailFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable("workout", workout);
        detailFragment.setArguments(bundle);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.flContent, detailFragment).addToBackStack(null);
        transaction.commit();
    }

    public void changeToProfileFragment(ParseUser user) {
        ProfileFragment profileFragment = new ProfileFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable("user", user);
        profileFragment.setArguments(bundle);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.flContent, profileFragment).addToBackStack(null);
        transaction.commit();
    }

// TODO - BEFORE PUSHING- ran an error when not static- should change back to static?

    public static boolean isFacebookUser(ParseUser user) {
        if (user.get("authData") == null) return false;
        JSONObject authData = user.getJSONObject("authData");
        return authData.has("facebook");
    }


    public static String getFBID(ParseUser user) {
        JSONObject authData = ParseUser.getCurrentUser().getJSONObject("authData");
        JSONObject facebook = null;
        try {
            facebook = authData.getJSONObject("facebook");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String fbID = null;

        try {
            fbID = facebook.getString("id");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return fbID;
    }

    public ParseFile conversionBitmapParseFile(Bitmap imageBitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] imageByte = byteArrayOutputStream.toByteArray();
        ParseFile parseFile = new ParseFile("image_file.png", imageByte);
        return parseFile;
    }

    public Bitmap convertToBitmap(Drawable drawable, int widthPixels, int heightPixels) {
        Bitmap mutableBitmap = Bitmap.createBitmap(widthPixels, heightPixels, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(mutableBitmap);
        drawable.setBounds(0, 0, widthPixels, heightPixels);
        drawable.draw(canvas);

        return mutableBitmap;
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap bmp = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                bmp = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            bitmapProfilePicture = bmp;
            return bmp;
        }

        protected void onPostExecute(Bitmap result) {
//            ivAvatar.setImageBitmap(bitmapProfilePicture);
//            bitmapProfilePicture = ((BitmapDrawable)ivAvatar.getDrawable()).getBitmap();

//            // Initialize a new ByteArrayStream
//            ByteArrayOutputStream stream = new ByteArrayOutputStream();
//            // Compress the bitmap with JPEG format and quality 50%
//            result.compress(Bitmap.CompressFormat.JPEG,10,stream);
//
//            byte[] byteArray = stream.toByteArray();
//            Bitmap compressedBitmap = BitmapFactory.decodeByteArray(byteArray,0,byteArray.length);
//
//            Bitmap resizedBitmap = getResizedBitmap(result, 10, 10);

            final ParseFile parseFile = conversionBitmapParseFile(result);

            parseFile.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null) {
                        ParseUser.getCurrentUser().put("profilePicture", parseFile);
                        Log.d("MainFragment", "Save ProfilePicture successful");

                    } else {
                        e.printStackTrace();
                        Log.e("AddFragment", "Save ProfilePicture was not successful");
                    }
                }
            });

            ParseUser.getCurrentUser().put("profilePicture", parseFile);
;
            ParseUser.getCurrentUser().saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null) {
                        Log.d("MainFragment", "Profile Successfully Saved");

                    } else {
                        e.printStackTrace();
                        Log.e("AddFragment", "Profile not saved successfully");

                    }
                }
            });
            }



    }

//    public static class BitmapScaler
//    {
//        // scale and keep aspect ratio
//        public static Bitmap scaleToFitWidth(Bitmap b, int width)
//        {
//            float factor = width / (float) b.getWidth();
//            return Bitmap.createScaledBitmap(b, width, (int) (b.getHeight() * factor), true);
//        }
//
//
//        // scale and keep aspect ratio
//        public static Bitmap scaleToFitHeight(Bitmap b, int height)
//        {
//            float factor = height / (float) b.getHeight();
//            return Bitmap.createScaledBitmap(b, (int) (b.getWidth() * factor), height, true);
//        }
//
//
//        // scale and keep aspect ratio
//        public static Bitmap scaleToFill(Bitmap b, int width, int height)
//        {
//            float factorH = height / (float) b.getWidth();
//            float factorW = width / (float) b.getWidth();
//            float factorToUse = (factorH > factorW) ? factorW : factorH;
//            return Bitmap.createScaledBitmap(b, (int) (b.getWidth() * factorToUse),
//                    (int) (b.getHeight() * factorToUse), true);
//        }
//
//
//        // scale and don't keep aspect ratio
//        public static Bitmap strechToFill(Bitmap b, int width, int height)
//        {
//            float factorH = height / (float) b.getHeight();
//            float factorW = width / (float) b.getWidth();
//            return Bitmap.createScaledBitmap(b, (int) (b.getWidth() * factorW),
//                    (int) (b.getHeight() * factorH), true);
//        }
//    }

//    public Bitmap getResizedBitmap(Bitmap image, int maxSize) {
//        int width = image.getWidth();
//        int height = image.getHeight();
//
//        float bitmapRatio = (float) width / (float) height;
//        if (bitmapRatio > 1) {
//            width = maxSize;
//            height = (int) (width / bitmapRatio);
//        } else {
//            height = maxSize;
//            width = (int) (height * bitmapRatio);
//        }
//
//        return Bitmap.createScaledBitmap(image, width, height, true);
//    }

//    public static Bitmap getResizedBitmap(Bitmap image, int newHeight, int newWidth) {
//        int width = image.getWidth();
//        int height = image.getHeight();
//        float scaleWidth = ((float) newWidth) / width;
//        float scaleHeight = ((float) newHeight) / height;
//        // create a matrix for the manipulation
//        Matrix matrix = new Matrix();
//        // resize the bit map
//        matrix.postScale(scaleWidth, scaleHeight);
//        // recreate the new Bitmap
//        Bitmap resizedBitmap = Bitmap.createBitmap(image, 0, 0, width, height,
//                matrix, false);
//        return resizedBitmap;
//    }

}
