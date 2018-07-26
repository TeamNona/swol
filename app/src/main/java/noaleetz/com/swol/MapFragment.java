package noaleetz.com.swol;


import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.parse.FindCallback;
import com.parse.LocationCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;

import org.parceler.Parcel;
import org.parceler.Parcels;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import noaleetz.com.swol.models.Workout;

import static noaleetz.com.swol.MainActivity.REQUEST_LOCATION_PERMISSION;


/**
 * A simple {@link Fragment} subclass.
 */
public class MapFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener, GoogleMap.OnInfoWindowClickListener {


    ArrayList<Workout> workouts;
    private static final String TAG = "MapFragment";
    private int counter = 0;
    private int mod;

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

    @BindView(R.id.fabNext)
    FloatingActionButton fabNext;

    @BindView(R.id.fabNearby)
    FloatingActionButton fabNearby;

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

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        //TODO: dynamically create the mapfragment

        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map_fragment);

        workouts = new ArrayList<>();

        loadTopWorkouts();

        mapFragment.getMapAsync(this);

        fabNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewNextWorkout();
            }
        });

        fabNearby.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showNearbyWorkouts();
            }
        });


    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        loadMap(googleMap);
        map.setInfoWindowAdapter(new CustomWindowAdapter(getLayoutInflater()));
        //TODO: experiment with the ParseGeoPoint.getLocation thingy

        map.setOnInfoWindowClickListener(this);

        // Make sure we have the permissions
        getLocationPermission();

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();

        // Get the current location of the device and set the position of the map.
        getLocation();

// map = googleMap;
//
//        LatLng dexter = new LatLng(47.6288488, -122.3430076);
//        MarkerOptions option = new MarkerOptions();
//        option.position(dexter).title("Facebook Dexter");
//        map.addMarker(option);
//        map.animateCamera(CameraUpdateFactory.newLatLng(dexter));
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

        }

    }

    @Override
    public void onInfoWindowClick(Marker marker) {
//        FragmentManager fm = getActivity().getSupportFragmentManager();
        // TODO: make the detail fragment class
//        fm.beginTransaction().replace(R.id.flContent, DetailFragment.class).commit();
        Toast.makeText(getContext(), "Go to detailed screen", Toast.LENGTH_SHORT).show();
    }

    // Fires when a long press happens on the map
    @Override
    public void onMapLongClick(final LatLng point) {
        ParseGeoPoint geoLoc = new ParseGeoPoint(point.latitude, point.longitude);
        FragmentManager fm = getActivity().getSupportFragmentManager();
        fm.beginTransaction().replace(R.id.flContent, AddFragment.create(geoLoc)).addToBackStack(null).commit();

    }
    private void createPin(GoogleMap googleMap, Workout workout) {
        map = googleMap;

        Marker pin;

        LatLng loc = workout.getLatLng();
        MarkerOptions option = new MarkerOptions();
        option.position(loc);

        pin = map.addMarker(option);

        try {
            MarkerData data = new MarkerData(workout.getName(),
                    workout.getUser().fetchIfNeeded().getUsername(),
                    workout.getTimeUntil(),
                    workout.getMedia().getFile());

            pin.setTag(Parcels.wrap(data));
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    public void loadTopWorkouts() {
        final Workout.Query postQuery = new Workout.Query();
        postQuery.getTop().withUser().orderByLastCreated();

        postQuery.findInBackground(new FindCallback<Workout>() {
            @Override
            public void done(List<Workout> objects, ParseException e) {
                if (e == null) {
                    Log.d(TAG, Integer.toString(objects.size()));
                    workouts.clear();
                    workouts.addAll(objects);
                    map.clear();

                    LatLng currLatLng = new LatLng(currentGeoPoint.getLatitude(), currentGeoPoint.getLongitude());
                    workoutBounds = new LatLngBounds(currLatLng, currLatLng);
                    Toast.makeText(getContext(), "Workouts Size: " + workouts.size(), Toast.LENGTH_SHORT).show();
                    for (int i = 0; i < workouts.size(); i++) {
                        Workout workout = workouts.get(i);
                        createPin(map, workout);
                        if (workout.isInRange(currentGeoPoint, 10)) {
                            workoutBounds = workoutBounds.including(workout.getLatLng());
                            Log.i("AddToBounds", "added workout " + i + " to the bounds: " + workout.getLocation().distanceInMilesTo(currentGeoPoint));
                        }
                    }
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

    void viewNextWorkout() {
        mod = counter % workouts.size();
            Workout workout = workouts.get(mod);
            Log.i("MapView", "Showing workout [" + mod + "] @ " + workout.getLatLng().toString());
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(workout.getLatLng(), 17));

        counter++;
    }

    void showNearbyWorkouts() {
        if (currentGeoPoint == null) return;
        Log.i("MapView", "Showing workout bounds");
        map.animateCamera(CameraUpdateFactory.newLatLngBounds(workoutBounds,convertDpToPixel(42)));
        Toast.makeText(getContext(), "Showing Nearby Workouts", Toast.LENGTH_SHORT).show();

    }


    @Override public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    //TODO: this class is not necessary. Clean up code by turning the workout into a parcel and just give that to the tag

    // this exists to pass extra data to the window adapter
    @Parcel
    public static class MarkerData {

        String title, createdBy, timeUntil;
        File image;

        public MarkerData() {}

        public MarkerData(String title, String createdBy, String timeUntil, File image){
            this.title = title;
            this.createdBy = createdBy;
            this.timeUntil = timeUntil;
            this.image = image;
        }

        public String getTitle() {
            return title;
        }

        public String getCreatedBy() {
            return createdBy;
        }

        public String getTimeUntil() {
            return timeUntil;
        }

        public File getImage() {
            return image;
        }
    }
}
