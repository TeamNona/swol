package noaleetz.com.swol.ui.fragments;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.maps.android.PolyUtil;
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
import butterknife.OnClick;
import butterknife.Unbinder;
import noaleetz.com.swol.ui.adapters.ClusterWindowAdapter;

import noaleetz.com.swol.ui.activities.MainActivity;
import noaleetz.com.swol.R;
import noaleetz.com.swol.models.Workout;

import static android.app.Activity.RESULT_OK;
import static noaleetz.com.swol.ui.activities.MainActivity.REQUEST_LOCATION_PERMISSION;


/**
 * A simple {@link Fragment} subclass.
 */



public class MapFragment extends Fragment implements OnMapReadyCallback,
        GoogleMap.OnMapLongClickListener,
        GoogleMap.OnInfoWindowClickListener,
        GoogleMap.OnMapClickListener,
        ClusterManager.OnClusterItemClickListener<Workout>,
        ClusterManager.OnClusterItemInfoWindowClickListener<Workout>,
        ClusterWindowAdapter.itemClickListener {



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
    private Cluster<Workout> clickedCluster;
    private Workout clickedClusterItem;
    private ClusterInfoWindowAdapter adapter;
    float withinHour = BitmapDescriptorFactory.HUE_RED;
    float withinToday = BitmapDescriptorFactory.HUE_YELLOW;
    float withinForever = BitmapDescriptorFactory.HUE_GREEN;
    WorkoutRenderer wRenderer;
    boolean showWorkout = false;

    @BindView(R.id.fabNearby)
    FloatingActionButton fabNearby;

    @BindView(R.id.fab1mi)
    FloatingActionButton fab1mi;

    @BindView(R.id.fab5mi)
    FloatingActionButton fab5mi;

    @BindView(R.id.fab10mi)
    FloatingActionButton fab10mi;

    FloatingActionButton oldFabAdd;
    FloatingActionButton mapFabAdd;

    @OnClick(R.id.mapFabAdd)
    public void onClick(View view) {
        mapFabAdd.hide();
        AddFragment addfragment = new AddFragment();
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.flContent, addfragment).addToBackStack(null);
        transaction.commit();

    }

    boolean init = true;

    boolean showNew;

    Polyline currentPolyline;

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

    @SuppressLint("RestrictedApi")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        //TODO: dynamically create the mapfragment (ok maybe not we'll see - Omari 7/27)

        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.google_map_fragment);

        oldFabAdd = getActivity().findViewById(R.id.fabAdd);
        oldFabAdd.setVisibility(View.GONE);

        mapFragment.getMapAsync(this);

        hideZoomButtons();

        mapFabAdd = getActivity().findViewById(R.id.mapFabAdd);
        mapFabAdd.show();

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
                "/n ids " + "[" + workoutIDs.size() + "]:" + workoutIDs.toString());


    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        loadMap(googleMap);

        loadTopWorkouts();

        // Make sure we have the permissions
        getLocationPermission();

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();

        // Get the current location of the device and set the position of the map.
        getLocation();

        // Set the slider to the right initial position
        Toast.makeText(getContext(), "Current Zoom: " + map.getCameraPosition().zoom, Toast.LENGTH_SHORT).show();
    }

    protected void loadMap(GoogleMap googleMap) {
        map = googleMap;
        if (map != null) {
            // Attach long click listener to the map here
            map.setOnMapLongClickListener(this);

            //TODO: remove this hackey stuff https://guides.codepath.com/android/Google-Maps-API-v2-Usage#customize-infowindow
            map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {

                @Override
                public boolean onMarkerClick(final Marker m) {
                    m.showInfoWindow();
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            m.showInfoWindow();
                        }
                    }, 200);
                    return true;
                }
            });

            // fire up the cluster manager
            clusterManager = new ClusterManager<>(getContext(), map);
            adapter = new ClusterInfoWindowAdapter();
            clusterManager.getClusterMarkerCollection().setOnInfoWindowAdapter(adapter);
            clusterManager.getMarkerCollection().setOnInfoWindowAdapter(new CustomInfoWindowAdapter(LayoutInflater.from(getContext())));
            final WorkoutRenderer renderer = new WorkoutRenderer();
            wRenderer = renderer;
            clusterManager.setRenderer(renderer);
            map.setOnMapClickListener(this);
            map.setOnCameraIdleListener(clusterManager);
            map.setOnMarkerClickListener(clusterManager);
            map.setOnInfoWindowClickListener(clusterManager);
            map.setInfoWindowAdapter(clusterManager.getMarkerManager());

            clusterManager.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<Workout>() {
                @Override
                public boolean onClusterItemClick(Workout workout) {
                    clickedClusterItem = workout;
                    final Marker m = renderer.getMarker(workout);

                    // TODO: same as the onInfoWindowClick
                    m.showInfoWindow();
                    if (currentPolyline != null) currentPolyline.remove();

                    String polyString = workout.getPolyline();
                    if (polyString != null) {
                        List<LatLng> decodedPath = PolyUtil.decode(polyString);
                        currentPolyline = map.addPolyline(new PolylineOptions().addAll(decodedPath));
                        LatLngBounds bounds = workout.getPolylineLatLngBounds();
                        map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, convertDpToPixel(42)));

                        return true;

                    }

                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            m.showInfoWindow();

                        }
                    }, 400);


                    return false;
                }
            });

            clusterManager.setOnClusterClickListener(new ClusterManager.OnClusterClickListener<Workout>() {
                @Override
                public boolean onClusterClick(Cluster<Workout> cluster) {
                    clickedCluster = cluster;
                    if (currentPolyline != null) currentPolyline.remove();
                    return false;
                }
            });

            clusterManager.setOnClusterItemInfoWindowClickListener(new ClusterManager.OnClusterItemInfoWindowClickListener<Workout>() {
                @Override
                public void onClusterItemInfoWindowClick(Workout workout) {
                    ((MainActivity) getContext()).changeToDetailFragment(workout);
                }
            });


            Log.d("ArrayCheck(loadMap)", "markers " + "[" + workoutMarkers.size() + "]:" + workoutMarkers.toString() +
                    "/n ids " + "[" + workoutIDs.size() + "]:" + workoutIDs.toString());

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
                    LatLngBounds.Builder builder = new LatLngBounds.Builder();
                    map.clear();
                    clusterManager.clearItems();
                    Log.d(TAG, "Number of nearby workouts: " + Integer.toString(objects.size()));
                    Toast.makeText(getContext(), "Workouts Size: " + objects.size(), Toast.LENGTH_SHORT).show();
                    LatLngBounds bounds = LatLngBounds.builder().include(currLoc).build();
                    for (int i = 0; i < objects.size(); i++) {
                        Workout workout = objects.get(i);
//                        if (!workoutIDs.contains(workout.getID())) workoutMarkers.add(createMarker(map, workout));
                        if (!workoutIDs.contains(workout.getID())) {
                            clusterManager.addItem(workout);
                            workoutIDs.add(workout.getObjectId());
                        }
                        builder.include(workout.getLatLng());
                        Log.i("CalculateBounds", "added workout " + i + " to the bounds: " + workout.getLocation().distanceInMilesTo(currentGeoPoint));
                    }
                    clusterManager.cluster();
                    showNearbyWorkouts(builder.build());

                    Log.d("ArrayCheck(calcBounds)", "markers [" + workoutMarkers.size() + "]" +
                            "\nids " + "[" + workoutIDs.size() + "]:" + workoutIDs.toString());

                } else {
                    e.printStackTrace();
                }
            }
        });

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
        if (currentPolyline != null) currentPolyline.remove();
    }

    public void loadTopWorkouts() {
        final Workout.Query postQuery = new Workout.Query();
        postQuery.getTop().withUser().orderByLastCreated();

        postQuery.findInBackground(new FindCallback<Workout>() {
            @Override
            public void done(List<Workout> objects, ParseException e) {
                if (e == null) {
                    Log.d(TAG, "Number of workouts: " + Integer.toString(objects.size()));
                    workoutMarkers.clear();
                    workoutIDs.clear();
                    map.clear();

                    LatLng currLatLng = new LatLng(currentGeoPoint.getLatitude(), currentGeoPoint.getLongitude());

                    workoutBounds = new LatLngBounds(currLatLng, currLatLng);
                    Toast.makeText(getContext(), "Workouts Size: " + objects.size(), Toast.LENGTH_SHORT).show();
                    for (int i = 0; i < objects.size(); i++) {
                        Workout workout = objects.get(i);
                        workoutIDs.add(workout.getObjectId());
                        clusterManager.addItem(workout);
                        if (workout.isInRange(currentGeoPoint, 10)) {
                            workoutBounds = workoutBounds.including(workout.getLatLng());
                            Log.i("AddToBounds", "added workout " + i + " to the bounds: " + workout.getLocation().distanceInMilesTo(currentGeoPoint));
                        }
                    }

                    clusterManager.cluster();


//                    if (showWorkout) viewWorkout(clickedClusterItem);
//                    showWorkout = false;

                } else {
                    e.printStackTrace();
                }
            }
        });

    }

    // this function is to convert from DP to pixels for the padding on the map bounds
    public static int convertDpToPixel(float dp) {
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        float px = dp * ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
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
        } catch (SecurityException e) {
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
                                Log.d(TAG, currentGeoPoint.toString());
                                ParseUser.getCurrentUser().put("currentLocation", currentGeoPoint);
                                ParseUser.getCurrentUser().saveInBackground();
                                // move the map to the current location
                                if (init)
                                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), 15));
                                init = false;
                            } else {

                                // TODO- handle null location

                                Log.d(TAG, "location is found to be null");
                            }
                        }
                    });
        }


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void viewWorkout(Workout workout) {
        final Marker m = wRenderer.getMarker(workout);
        Log.i("MapView", "Showing workout [" + mod + "] @ " + m.getPosition().toString());
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(m.getPosition(), 17), new GoogleMap.CancelableCallback() {
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

    void showNearbyWorkouts(LatLngBounds bounds) {
        getLocation();
        if (currentGeoPoint == null) return;
        Log.i("MapView", "Showing workout bounds: " + bounds.northeast.toString() + " --> " + bounds.southwest.toString());
        map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, convertDpToPixel(42)));
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
        showNew = true;

    }

    public void setClickedClusterItem(Workout clickedClusterItem) {
        this.clickedClusterItem = clickedClusterItem;
        showWorkout = true;
    }

    // TODO: animate this
    @SuppressLint("RestrictedApi")
    void showZoomButtons() {
        fab1mi.setVisibility(View.VISIBLE);
        fab5mi.setVisibility(View.VISIBLE);
        fab10mi.setVisibility(View.VISIBLE);
    }

    // TODO: animate this
    @SuppressLint("RestrictedApi")
    public void hideZoomButtons() {
        fab1mi.setVisibility(View.GONE);
        fab5mi.setVisibility(View.GONE);
        fab10mi.setVisibility(View.GONE);
    }

    float colorInterpolator(long time) {
        float timeUntil = ((float) (time - System.currentTimeMillis())) / 3600000;
        Log.d("ColorInterpolator", timeUntil + "");
        if (timeUntil <= 0) return BitmapDescriptorFactory.HUE_BLUE;
        if (timeUntil <= 1) return withinHour;
        if (timeUntil <= 24) return withinToday;
        return withinForever;
    }

    @Override public void onPause() {
        super.onPause();

//        oldFabAdd.setTranslationY(1200);


    }
    @Override public void onDestroyView() {


        super.onDestroyView();

        unbinder.unbind();
    }

    // INCOMING -- A LOT OF CLUSTER CODE //
    private class WorkoutRenderer extends DefaultClusterRenderer<Workout> {

        private final IconGenerator clusterIconGenerator;

        public WorkoutRenderer() {
            super(getContext(), map, clusterManager);
            clusterIconGenerator = new IconGenerator(getContext());
        }

        @Override
        protected void onBeforeClusterItemRendered(Workout workout, MarkerOptions markerOptions) {
            final BitmapDescriptor markerDescriptor = BitmapDescriptorFactory.defaultMarker(colorInterpolator(workout.getTime().getTime()));

            markerOptions.icon(markerDescriptor).snippet(workout.getName());

        }

        @Override
        protected void onBeforeClusterRendered(Cluster<Workout> cluster, MarkerOptions markerOptions) {
            super.onBeforeClusterRendered(cluster, markerOptions);

            clusterIconGenerator.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.background_circle));
            clusterIconGenerator.setTextAppearance(R.style.AppTheme);
            final Bitmap icon = clusterIconGenerator.makeIcon(String.valueOf(cluster.getSize()));
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));
        }

        @Override
        protected boolean shouldRenderAsCluster(Cluster cluster) {
            // Always render clusters.
            return cluster.getSize() > 1;
        }


    }

    //     alright now to implement the clickable methods for the clusters
    @Override
    public void onInfoWindowClick(Marker marker) {

    }

    @Override
    public boolean onClusterItemClick(Workout workout) {
        return false;
    }

    @Override
    public void onClusterItemInfoWindowClick(Workout workout) {

    }

    // Cluster info window adapter
    public class ClusterInfoWindowAdapter implements GoogleMap.InfoWindowAdapter, ClusterWindowAdapter.itemClickListener {

        //TODO: the butterknives

        private final View myContentsView;
        private ClusterWindowAdapter adapter;
        private RecyclerView rvNearby;
        private List<Workout> workouts;
        private AlertDialog alertDialog;
        private Context context;

        ClusterInfoWindowAdapter() {
            myContentsView = getLayoutInflater().inflate(R.layout.cluster_info_window, null);
            context = getContext();
        }

        @Override
        public View getInfoContents(Marker marker) {
            return null;
        }

        @Override
        public View getInfoWindow(Marker marker) {
            // TODO Auto-generated method stub

            if (clickedCluster != null) showAlertDialogForCluster(clickedCluster);

            return myContentsView;
        }

        public void showAlertDialogForCluster(Cluster<Workout> cluster) {
            // inflate the xml
            View messageView = LayoutInflater.from(getContext()).inflate(R.layout.cluster_info_window, null);
            // create the alert dialog builder
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
            // set the xml to the alert dialog builder
            alertDialogBuilder.setView(messageView);

            // create the alertDialog
            alertDialog = alertDialogBuilder.create();

            workouts = new ArrayList<>();
            rvNearby = messageView.findViewById(R.id.rvNearby);
            adapter = new ClusterWindowAdapter(workouts);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
            rvNearby.setLayoutManager(linearLayoutManager);
            rvNearby.setAdapter(adapter);

            workouts.addAll(cluster.getItems());
            adapter.notifyDataSetChanged();

            // display the dialog
            alertDialog.show();
        }

        @Override
        public void onWorkoutSelected(Workout workout) {
            alertDialog.dismiss();
            ((MainActivity) context).changeToDetailFragment(workout);
        }
    }

    public class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
        private final View v;

        public CustomInfoWindowAdapter(LayoutInflater i) {
            v = getLayoutInflater().inflate(R.layout.custom_info_window, null);
        }

        // This defines the contents within the info window based on the marker
        @Override
        public View getInfoContents(Marker marker) {
            // Getting view from the layout file
            // Populate fields

            Workout assigned_workout = clickedClusterItem;

            TextView tvInfoTitle = v.findViewById(R.id.tvInfoTitle);
            tvInfoTitle.setText(assigned_workout.getName());

            TextView tvCreatedBy = v.findViewById(R.id.tvInfoCreatedBy);

            String user = null;
            try {
                user = assigned_workout.getUser().fetchIfNeeded().getUsername();
                tvCreatedBy.setText("Created By: " + user);
            } catch (ParseException e) {
                e.printStackTrace();
            }


            TextView tvInfoTimeUntil = v.findViewById(R.id.tvInfoTimeUntil);
            tvInfoTimeUntil.setText(assigned_workout.getTimeUntil());

            ImageView ivInfoImage = v.findViewById(R.id.ivInfoImage);
            Glide.with(v).load(assigned_workout.getMedia().getUrl()).into(ivInfoImage);
            // Return info window contents
            return v;
        }

        // This changes the frame of the info window; returning null uses the default frame.
        // This is just the border and arrow surrounding the contents specified above
        @Override
        public View getInfoWindow(Marker marker) {
            return null;
        }
    }

    @Override
    public void onWorkoutSelected(Workout workout) {
        adapter.onWorkoutSelected(workout);
    }
}
