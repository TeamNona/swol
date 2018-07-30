package noaleetz.com.swol;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import noaleetz.com.swol.models.Workout;

import static android.app.Activity.RESULT_OK;
import static noaleetz.com.swol.MainActivity.REQUEST_LOCATION_PERMISSION;


/**
 * A simple {@link Fragment} subclass.
 */

public class MapFragment extends Fragment implements OnMapReadyCallback,
                                                     GoogleMap.OnMapLongClickListener,
                                                     GoogleMap.OnInfoWindowClickListener,
                                                     GoogleMap.OnMapClickListener,
                                                     ClusterManager.OnClusterClickListener<Workout>,
                                                     ClusterManager.OnClusterInfoWindowClickListener<Workout>,
                                                     ClusterManager.OnClusterItemClickListener<Workout>,
                                                     ClusterManager.OnClusterItemInfoWindowClickListener<Workout> {



    ArrayList<Marker> workoutMarkers;
    ArrayList<String> workoutIDs;
    private static final String TAG = "MapFragment";
    private int counter = 0;
    private int mod;
    private final int REQUEST_CODE = 10;

    // map stuff
    GoogleMap map;
    private boolean mPermissionDenied = false;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 2;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private boolean mLocationPermissionGranted = true;
    Location mLastKnownLocation;
    LatLngBounds workoutBounds;
    ParseGeoPoint currentGeoPoint;
    // cluster stuff
    private ClusterManager<Workout> clusterManager;

    @BindView(R.id.fabNext)
    FloatingActionButton fabNext;

    @BindView(R.id.fabNearby)
    FloatingActionButton fabNearby;

    @BindView(R.id.fab1mi)
    FloatingActionButton fab1mi;

    @BindView(R.id.fab5mi)
    FloatingActionButton fab5mi;

    @BindView(R.id.fab10mi)
    FloatingActionButton fab10mi;

    boolean showLast;

    Unbinder unbinder;

    public MapFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        unbinder = ButterKnife.bind(this, view);

        workoutMarkers = new ArrayList<>();
        workoutIDs = new ArrayList<>();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        //TODO: dynamically create the mapfragment (ok maybe not we'll see - Omari 7/27)

        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.google_map_fragment);

        mapFragment.getMapAsync(this);

        hideZoomButtons();

        fabNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewNextWorkout();
                hideZoomButtons();
            }
        });

        fabNearby.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (fab1mi.getVisibility() == View.GONE) showZoomButtons();
                else hideZoomButtons();
            }
        });

        fab1mi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showViewBounds(1);
                hideZoomButtons();
            }
        });

        fab5mi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showViewBounds(5);
                hideZoomButtons();
            }
        });

        fab10mi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showViewBounds(10);
                hideZoomButtons();
            }
        });



        Log.d("ArrayCheck(onViewCreat)", "markers " + "[" + workoutMarkers.size() + "]:" + workoutMarkers.toString() +
                "/n ids " + "[" + workoutIDs.size() + "]:"  + workoutIDs.toString());


    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        loadMap(googleMap);

        loadTopWorkouts();

        map.setInfoWindowAdapter(new CustomWindowAdapter(getLayoutInflater()));

        map.setOnInfoWindowClickListener(this);

        map.setOnMapClickListener(this);

        // Make sure we have the permissions
        getLocationPermission();

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();

        // Get the current location of the device and set the position of the map.
        getLocation();

        // Set the slider to the right initial position
        Toast.makeText(getContext(), "Current Zoom: " + map.getCameraPosition().zoom, Toast.LENGTH_SHORT).show();

        // fire up the cluster manager
        clusterManager = new ClusterManager<>(getContext(), map);
        clusterManager.setRenderer(new WorkoutRenderer());
        map.setOnCameraIdleListener(clusterManager);
        map.setOnMarkerClickListener(clusterManager);
        map.setOnInfoWindowClickListener(clusterManager);
        clusterManager.setOnClusterClickListener(this);
        clusterManager.setOnClusterInfoWindowClickListener(this);
        clusterManager.setOnClusterItemClickListener(this);
        clusterManager.setOnClusterItemInfoWindowClickListener(this);
    }

    protected void loadMap(GoogleMap googleMap) {
        map = googleMap;
        if (map != null) {
            // Attach long click listener to the map here
            map.setOnMapLongClickListener(this);

            //TODO: remove this hackey stuff https://guides.codepath.com/android/Google-Maps-API-v2-Usage#customize-infowindow
            map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {

                @Override
                public boolean onMarkerClick(final Marker mark) {


                    mark.showInfoWindow();

                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mark.showInfoWindow();

                        }
                    }, 200);

                    return true;
                }
            });


            Log.d("ArrayCheck(loadMap)", "markers " + "[" + workoutMarkers.size() + "]:" + workoutMarkers.toString() +
                    "/n ids " + "[" + workoutIDs.size() + "]:"  + workoutIDs.toString());

        }

    }

    public void showViewBounds(int r) {
        final int range = r;
        final LatLng currLoc = new LatLng(currentGeoPoint.getLatitude(), currentGeoPoint.getLongitude());

        final Workout.Query postQuery = new Workout.Query();
        postQuery.withUser().orderByLastCreated().getWithinRange(currentGeoPoint, range);

        postQuery.findInBackground(new FindCallback<Workout>() {
            @Override
            public void done(List<Workout> objects, ParseException e) {
                if (e == null) {
                    map.clear();
                    workoutMarkers.clear();
                    workoutIDs.clear();
                    Log.d(TAG, "Number of nearby workouts: " + Integer.toString(objects.size()));
                    Toast.makeText(getContext(), "Workouts Size: " + objects.size(), Toast.LENGTH_SHORT).show();
                    LatLngBounds bounds = LatLngBounds.builder().include(currLoc).build();
                    for (int i = 0; i < objects.size(); i++) {
                        Workout workout = objects.get(i);
//                        if (!workoutIDs.contains(workout.getID())) workoutMarkers.add(createMarker(map, workout));
                        bounds = bounds.including(workout.getLatLng());
                        Log.i("CalculateBounds", "added workout " + i + " to the bounds: " + workout.getLocation().distanceInMilesTo(currentGeoPoint));
                        clusterManager.addItem(workout);
                    }
                    workoutBounds = bounds;
                    showNearbyWorkouts(range);

                    Log.d("ArrayCheck(calcBounds)", "markers [" + workoutMarkers.size() + "]" +
                            "\nids " + "[" + workoutIDs.size() + "]:"  + workoutIDs.toString());

                } else {
                    e.printStackTrace();
                }
            }
        });

    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        Workout assigned_workout = (Workout) Parcels.unwrap((Parcelable) marker.getTag());
        ((MainActivity) getContext()).changeToDetailFragment(assigned_workout);

        Toast.makeText(getContext(), "Go to detailed screen", Toast.LENGTH_SHORT).show();
    }

    // Fires when a long press happens on the map
    @Override
    public void onMapLongClick(final LatLng point) {
        ParseGeoPoint geoLoc = new ParseGeoPoint(point.latitude, point.longitude);
        FragmentManager fm = getActivity().getSupportFragmentManager();
        fm.beginTransaction().replace(R.id.flContent, AddFragment.create(geoLoc)).addToBackStack(null).commit();

    }

    @Override
    public void onMapClick(LatLng latLng) {
        hideZoomButtons();
    }

    private Marker createMarker(GoogleMap googleMap, Workout workout) {
        map = googleMap;
        Marker marker;

        LatLng loc = workout.getLatLng();
        MarkerOptions option = new MarkerOptions();
        float withinHour = BitmapDescriptorFactory.HUE_RED;
        float withinToday = BitmapDescriptorFactory.HUE_YELLOW;
        float withinForever = BitmapDescriptorFactory.HUE_GREEN;
        option.position(loc).icon(BitmapDescriptorFactory.defaultMarker(colorInterpolator(workout.getTime().getTime(), withinHour, withinToday, withinForever)));

        marker = map.addMarker(option);
        workoutIDs.add(workout.getObjectId());
        marker.setTag(Parcels.wrap(workout));

        return marker;

    }

    public void loadTopWorkouts() {
        final Workout.Query postQuery = new Workout.Query();
        postQuery.getTop().withUser().orderByLastCreated();

        postQuery.findInBackground(new FindCallback<Workout>() {
            @Override
            public void done(List<Workout> objects, ParseException e) {
                if (e == null) {
                    Log.d(TAG, "Number of workouts: "+ Integer.toString(objects.size()));
                    workoutMarkers.clear();
                    workoutIDs.clear();
                    map.clear();

                    LatLng currLatLng = new LatLng(currentGeoPoint.getLatitude(), currentGeoPoint.getLongitude());
                    workoutBounds = new LatLngBounds(currLatLng, currLatLng);
                    Toast.makeText(getContext(), "Workouts Size: " + objects.size(), Toast.LENGTH_SHORT).show();
                    for (int i = 0; i < objects.size(); i++) {
                        Workout workout = objects.get(i);
//                        workoutMarkers.add(createMarker(map, workout));
                        clusterManager.addItem(workout);
                        if (workout.isInRange(currentGeoPoint, 10)) {
                            workoutBounds = workoutBounds.including(workout.getLatLng());
                            Log.i("AddToBounds", "added workout " + i + " to the bounds: " + workout.getLocation().distanceInMilesTo(currentGeoPoint));
                        }
                    }

                    if (showLast) viewWorkout(workoutMarkers.get(0));
                    showLast = false;


                    clusterManager.cluster();
                } else {
                    e.printStackTrace();
                }
            }
        });

    }

    // this function is to convert from DP to pixels for the padding on the map bounds
    public static int convertDpToPixel(float dp){
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        float px = dp * ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return (int) px;
    }

    private void updateLocationUI() {
        if (map == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                map.setMyLocationEnabled(true);
                map.getUiSettings().setMyLocationButtonEnabled(true);
                map.getUiSettings().setMapToolbarEnabled(true);
            } else {
                map.setMyLocationEnabled(false);
                map.getUiSettings().setMyLocationButtonEnabled(false);
                mLastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
        updateLocationUI();
    }

    private void getLocation() {
        // If user has not yet given permission - ask for permission - dialog box asking for permission pops up
        // Upon return, onRequestPermissionsResult is called
        // getLocation is called AGAIN and this time will skip to the else
        if (ActivityCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]
                            {Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        } else {
            Log.d(TAG, "getLocation: permissions granted");
            mFusedLocationProviderClient.getLastLocation()
                    .addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.

                            if (location != null) {
                                Log.d(TAG, "Got last known location");
                                // Logic to handle location object
                                mLastKnownLocation = location;
                                currentGeoPoint = new ParseGeoPoint(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude());
                                Log.d(TAG,currentGeoPoint.toString());
                                ParseUser.getCurrentUser().put("currentLocation",currentGeoPoint);
                                ParseUser.getCurrentUser().saveInBackground();
                                // move the map to the current location
                                map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), 15));

                            }
                            else{

                                // TODO- handle null location

                                Log.d(TAG,"location is found to be null");
                            }
                        }
                    });
        }



    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }

    void viewWorkout(Marker marker) {
        final Marker m = marker;
        Workout workout = Parcels.unwrap((Parcelable) marker.getTag());
        Log.i("MapView", "Showing workout [" + mod + "] @ " + workout.getLatLng().toString());
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(workout.getLatLng(), 17), new GoogleMap.CancelableCallback() {
            @Override
            public void onFinish() {
                // TODO: same as the onInfoWindowClick
                m.showInfoWindow();

                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        m.showInfoWindow();

                    }
                }, 200);

            }

            @Override
            public void onCancel() {

            }
        });
    }

    void viewNextWorkout() {
        mod = counter % workoutMarkers.size();
            final Marker marker = workoutMarkers.get(mod);
            Workout workout = Parcels.unwrap((Parcelable) marker.getTag());
            Log.i("MapView", "Showing workout [" + mod + "] @ " + workout.getLatLng().toString());
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(workout.getLatLng(), 17), new GoogleMap.CancelableCallback() {
                @Override
                public void onFinish() {
                    // TODO: same as the onInfoWindowClick
                    marker.showInfoWindow();

                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            marker.showInfoWindow();

                        }
                    }, 200);

                }

                @Override
                public void onCancel() {

                }
            });
        counter++;
    }

    void showNearbyWorkouts(int range) {
        getLocation();
        if (currentGeoPoint == null) return;
        Log.i("MapView", "Showing workout bounds");
        map.animateCamera(CameraUpdateFactory.newLatLngBounds(workoutBounds,convertDpToPixel(42)));
        Toast.makeText(getContext(), "Showing Nearby Workouts", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE) {
            Workout workout = Parcels.unwrap(data.getExtras().getParcelable("workout"));

        }

    }

    public void addMarker() {
        showLast = true;
    }

    // TODO: animate this
    void showZoomButtons() {
        fab1mi.setVisibility(View.VISIBLE);
        fab5mi.setVisibility(View.VISIBLE);
        fab10mi.setVisibility(View.VISIBLE);
    }

    // TODO: animate this
    void hideZoomButtons() {
        fab1mi.setVisibility(View.GONE);
        fab5mi.setVisibility(View.GONE);
        fab10mi.setVisibility(View.GONE);
    }

    float colorInterpolator(long time, float withinHour, float withinToday, float withinForever) {
        float timeUntil = ((float )(time - System.currentTimeMillis())) / 3600000;
        Log.d("ColorInterpolator", timeUntil + "");
        if (timeUntil <= 0) return BitmapDescriptorFactory.HUE_BLUE;
        if (timeUntil <= 1) return withinHour;
        if (timeUntil <= 24) return withinToday;
        return withinForever;
    }

    // INCOMING -- A LOT OF GOOGLE CODE //
    // new class for rendering the clusters -- yes i copy and pasted this code i understand none of it yet
    private class WorkoutRenderer extends DefaultClusterRenderer<Workout> {
        private final IconGenerator mIconGenerator = new IconGenerator(getContext());
        private final IconGenerator mClusterIconGenerator = new IconGenerator(getContext());
        private final ImageView mImageView;
        private final ImageView mClusterImageView;
        private final int mDimension;

        public WorkoutRenderer() {
            super(getContext(), map, clusterManager);

            View multiProfile = getLayoutInflater().inflate(R.layout.multi_profile, null);
            mClusterIconGenerator.setContentView(multiProfile);
            mClusterImageView = (ImageView) multiProfile.findViewById(R.id.image);

            mImageView = new ImageView(getContext());
            mDimension = (int) getResources().getDimension(R.dimen.custom_profile_image);
            mImageView.setLayoutParams(new ViewGroup.LayoutParams(mDimension, mDimension));
            int padding = (int) getResources().getDimension(R.dimen.custom_profile_padding);
            mImageView.setPadding(padding, padding, padding, padding);
            mIconGenerator.setContentView(mImageView);
        }

        @Override
        protected void onBeforeClusterItemRendered(Workout workout, MarkerOptions markerOptions) {
            // Draw a single person.
            // Set the info window to show their name.
            try {
                Glide.with(getContext()).load(workout.getMedia().getFile()).into(mImageView);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            Bitmap icon = mIconGenerator.makeIcon();
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon)).title(workout.getName());
        }

        @Override
        protected void onBeforeClusterRendered(Cluster<Workout> cluster, MarkerOptions markerOptions) {
            // Draw multiple people.
            // Note: this method runs on the UI thread. Don't spend too much time in here (like in this example).
            List<Drawable> profilePhotos = new ArrayList<Drawable>(Math.min(4, cluster.getSize()));
            int width = mDimension;
            int height = mDimension;

            for (Workout w : cluster.getItems()) {
                // Draw 4 at most.
                if (profilePhotos.size() == 4) break;
                Drawable drawable = null;
                try {
                    drawable = new BitmapDrawable(getResources(), BitmapFactory.decodeFile(w.getMedia().getFile().getAbsolutePath()));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                drawable.setBounds(0, 0, width, height);
                profilePhotos.add(drawable);
            }
            MultiDrawable multiDrawable = new MultiDrawable(profilePhotos);
            multiDrawable.setBounds(0, 0, width, height);

            mClusterImageView.setImageDrawable(multiDrawable);
            Bitmap icon = mClusterIconGenerator.makeIcon(String.valueOf(cluster.getSize()));
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));
        }

        @Override
        protected boolean shouldRenderAsCluster(Cluster cluster) {
            // Always render clusters.
            return cluster.getSize() > 1;
        }
    }

    // alright now to implement the clickable methods for the clusters


    @Override
    public boolean onClusterClick(Cluster<Workout> cluster) {
        Toast.makeText(getContext(), "You clicked the cluster!", Toast.LENGTH_SHORT).show();
        return false;
    }

    @Override
    public void onClusterInfoWindowClick(Cluster<Workout> cluster) {

    }

    @Override
    public boolean onClusterItemClick(Workout workout) {
        return false;
    }

    @Override
    public void onClusterItemInfoWindowClick(Workout workout) {

    }

    @Override public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
