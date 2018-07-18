package noaleetz.com.swol;


import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.ParseException;

import java.util.ArrayList;
import java.util.List;

import noaleetz.com.swol.models.Workout;


/**
 * A simple {@link Fragment} subclass.
 */
public class MapFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMarkerDragListener, GoogleMap.OnMapLongClickListener {


    ArrayList<Workout> workouts;
    private static final String TAG = "MapFragment";
    private int counter;

    GoogleMap map;

    public MapFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map_fragment);
        mapFragment.getMapAsync(this);

        workouts = new ArrayList<>();

        loadTopWorkouts();

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        loadMap(googleMap);

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
//            map.setInfoWindowAdapter(new CustomWindowAdapter(getLayoutInflater()));
        }

    }

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {

        // update the longitude and latitude of the activity

    }

    // Fires when a long press happens on the map
    @Override
    public void onMapLongClick(final LatLng point) {
        counter--;
        if (counter >= 0) {
            Toast.makeText(getContext(), "New Pin [" + counter + "] @ " + workouts.get(counter).getLatLng().toString(), Toast.LENGTH_LONG).show();
            createPin(map, workouts.get(counter));
        } else showAlertDialogForPoint(point);
//         Custom code here...


        // this is a test just for checking if loading the data points work

    }

    // Display the alert that adds the marker
    private void showAlertDialogForPoint(final LatLng point) {
        // inflate message_item.xml view
        View messageView = LayoutInflater.from(getContext()).
                inflate(R.layout.message_item, null);
        // Create alert dialog builder
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
        // set message_item.xml to AlertDialog builder
        alertDialogBuilder.setView(messageView);

        // Create alert dialog
        final AlertDialog alertDialog = alertDialogBuilder.create();

        // Configure dialog button (OK)
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Define color of marker icon
                        BitmapDescriptor defaultMarker =
                                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN);
                        // Extract content from alert dialog
                        String title = ((EditText) alertDialog.findViewById(R.id.etTitle)).
                                getText().toString();
                        String snippet = ((EditText) alertDialog.findViewById(R.id.etSnippet)).
                                getText().toString();
                        // Creates and adds marker to the map
                        Marker marker = map.addMarker(new MarkerOptions()
                                .position(point)
                                .title(title)
                                .snippet(snippet)
                                .icon(defaultMarker));
                    }
                });

        // Configure dialog button (Cancel)
        alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        // Display the dialog
        alertDialog.show();
    }

    private void createPin(GoogleMap googleMap, Workout workout) {
        map = googleMap;

        LatLng loc = workout.getLatLng();
        MarkerOptions option = new MarkerOptions();
        option.position(loc).title(workout.getName()).snippet(workout.getTimeUntil());
        map.addMarker(option);
        map.animateCamera(CameraUpdateFactory.newLatLng(loc));

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
                    counter = objects.size(); // reason this isn't -1 is because there is alread one in the long click
                } else {
                    e.printStackTrace();
                }
            }
        });

    }
}
