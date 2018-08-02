package noaleetz.com.swol;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import noaleetz.com.swol.models.Workout;

import static com.parse.ParseUser.getCurrentUser;
import static noaleetz.com.swol.MainActivity.REQUEST_LOCATION_PERMISSION;


/**
 * A simple {@link Fragment} subclass.
 */
public class FeedFragment extends Fragment {

    private static final String TAG = "FeedFragmentTAG";

//    private AdapterView.OnItemSelectedListener listener;


    @BindView(R.id.rvPosts)
    RecyclerView rvPosts;
    @BindView(R.id.swipeContainer)
    SwipeRefreshLayout swipeContainer;
    @BindView(R.id.svSearch)
    android.widget.SearchView svSearch;
    @BindView(R.id.ivFilterOptions)
    ImageView ivFilterOptions;
    @BindView(R.id.ivSubmit)
    ImageView ivSubmit;
    private FeedAdapter adapter;
    private List<Workout> posts;
    private Unbinder unbinder;
    String maxMileString;
    String maxHourString;
    String tagString;
    ParseGeoPoint currentGeoPoint;



    String[] categories = new String[]{"Bike", "Cardio","Class","Dance","Game","Gym","High Intensity Interval Training","Hike","Meditation","Run","Swim","Weight"};









    public FeedFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_feed, container, false);
        unbinder = ButterKnife.bind(this, view);


        // Inflate the layout for this fragment
        return view;
    }

    public void fetchTimelineAsync(int page) {

        loadTopPosts();

        // TODO - refresh stops early

        if (swipeContainer.isRefreshing()) {
            swipeContainer.setRefreshing(false);
        }


    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        this.adapter = new FeedAdapter(posts); // this class implements callback


        posts = new ArrayList<>();
        adapter = new FeedAdapter(posts);
        Log.d(TAG, "Finished setting the adapter");
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        rvPosts.setLayoutManager(linearLayoutManager);
        rvPosts.setAdapter(adapter);



        loadTopPosts();


        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Your code to refresh the list here.
                // Make sure you call swipeContainer.setRefreshing(false)
                // once the network request has completed successfully.
                fetchTimelineAsync(0);

                swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                        android.R.color.holo_green_light,
                        android.R.color.holo_orange_light,
                        android.R.color.holo_red_light);

            }


        });
        ivFilterOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Creating the instance of PopupMenu
                final PopupMenu popup = new PopupMenu(view.getContext(), ivFilterOptions);
                //Inflating the Popup using xml file
                popup.getMenuInflater()
                        .inflate(R.menu.popup_icon, popup.getMenu());

                //registering popup with OnMenuItemClickListener

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        // toast on item click
                        Toast.makeText(
                                getContext(),
                                "You Clicked : " + item.getTitle(),
                                Toast.LENGTH_SHORT
                        ).show();
                        // select icon
                        selectPopupItem(item);
                        popup.dismiss();
                        return true;
                    }
                });

                popup.show(); //showing popup menu
            }


        });


    }

    public void selectPopupItem(MenuItem menuItem) {
        ivFilterOptions.setImageDrawable(menuItem.getIcon());

        switch (menuItem.getItemId()) {
            case R.id.filterByTag:
                svSearch.setQueryHint("filter by a workout category");
                tagString = svSearch.getQuery().toString();

                ivSubmit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // check if category exists
                        if(categories.equals(tagString)){
                            // user has searched an existing category
                            QueryByCategory(tagString);
                        }
                        else{
                            CategoryNullOrDoesNotExist();
                        }
                    }
                });
                break;
            case R.id.filterByTime:
                svSearch.setQueryHint("filter by number of hours");

                ivSubmit.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {

                        maxHourString = String.valueOf(svSearch.getQuery());


                        double maxHourDouble = Double.parseDouble(maxHourString);
                        QueryByTime(maxHourDouble);

                    }
                });
                break;
            case R.id.filterByDistance:
                svSearch.setQueryHint("filter by number of miles");

                ivSubmit.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {

                        maxMileString = String.valueOf(svSearch.getQuery());


                        double maxMileDouble = Double.parseDouble(maxMileString);
                        QueryByDistance(maxMileDouble);

                    }
                });

                break;
            case R.id.filterByTitle:

                break;
            case R.id.filterByUser:

                break;
            default:
                return;
        }

        // Highlight the selected item has been done by NavigationView
        menuItem.setChecked(true);

        // Close the navigation drawer

    }

    private void QueryByTime(double maxHourDouble) {


    }

    private void QueryByCategory(String tagString) {
        final Workout.Query categoryQuery = new Workout.Query();
        categoryQuery.getTop().orderByLastCreated().whereEqualTo("eventCategory",tagString).findInBackground(new FindCallback<Workout>() {
            @Override
            public void done(List<Workout> objects, ParseException e) {
                posts.clear();

                posts.addAll(objects);
                rvPosts.scrollToPosition(0);
                InputMethodManager mgr = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                assert mgr != null;
                mgr.hideSoftInputFromWindow(svSearch.getWindowToken(), 0);
            }
        });


    }


    public void QueryByUserCreated() {
        final Workout.Query postQuery = new Workout.Query();


    }
    public void QueryByDistance(double maxMileNumber) {
        // user didn't input max range - sort by distance
        if(maxMileNumber!=0){
            maxMileNumber = 30.0;
        }

        final Workout.Query postDistanceQuery = new Workout.Query();

        currentGeoPoint = ParseUser.getCurrentUser().getParseGeoPoint("currentLocation");

//        postDistanceQuery.withUser().orderByLastCreated().getWithinRange(currentLocation,maxMileNumber);

        postDistanceQuery.withUser().getWithinRange(currentGeoPoint, maxMileNumber);

        postDistanceQuery.findInBackground(new FindCallback<Workout>() {
            @Override
            public void done(List<Workout> objects, ParseException e) {
                if (e == null) {
                    Log.d(TAG, Integer.toString(objects.size()));
                    for (int i = 0; i < objects.size(); i++) {
//                        Log.d(TAG, "Post [" + i + "] = " + objects.get(i).getDescription()
//                                + "\nusername: " + objects.get(i).getUser().getUsername());
                    }

                    // order objects in distance order
                    Collections.sort(objects, new Comparator<Workout>() {
                        @Override
                        public int compare(Workout o1, Workout o2) {
                            return o1.compareTo(o2);
                        }
                    });

                    posts.clear();

                    posts.addAll(objects);
//                    adapter.notifyDataSetChanged();
                    rvPosts.scrollToPosition(0);
                    InputMethodManager mgr = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    assert mgr != null;
                    mgr.hideSoftInputFromWindow(svSearch.getWindowToken(), 0);

                    // TODO- scroll to bottom option
                } else {
                    e.printStackTrace();
                }
            }
        });


    }



    public void loadTopPosts() {


        final Workout.Query postQuery = new Workout.Query();
        postQuery.getTop().withUser().orderByLastCreated();

        postQuery.findInBackground(new FindCallback<Workout>() {
            @Override
            public void done(List<Workout> objects, ParseException e) {
                if (e == null) {
                    Log.d(TAG, Integer.toString(objects.size()));
                    for (int i = 0; i < objects.size(); i++) {
//                        Log.d(TAG, "Post [" + i + "] = " + objects.get(i).getDescription()
//                                + "\nusername: " + objects.get(i).getUser().getUsername());
                    }
                    posts.clear();
                    posts.addAll(objects);
                    adapter.notifyDataSetChanged();
                } else {
                    e.printStackTrace();
                }
            }
        });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    public void CategoryNullOrDoesNotExist(){
        AlertDialog alertDialog = new AlertDialog.Builder(
                this.getContext()).create();

        if(tagString != null){
            // category entered but doesn't exist
            //Setting Dialog Title
            alertDialog.setTitle("Oops!");

            // Setting Dialog Message
            alertDialog.setMessage("This workout category doesn't exist yet");

            // Setting Icon to Dialog
            alertDialog.setIcon(R.drawable.ic_fitness_center_black_24dp);
        }
        else{

            //Setting Dialog Title
            alertDialog.setTitle("Please Enter a Category!");

            // Setting Dialog Message
//            alertDialog.setMessage("This workout category doesn't exist yet");

            // Setting Icon to Dialog
            alertDialog.setIcon(R.drawable.ic_pencil);
        }

        // Setting OK Button
        alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // Write your code here to execute after dialog closed
//                            Toast.makeText(getApplicationContext(), "You clicked on OK", Toast.LENGTH_SHORT).show();
            }
        });
        // Showing Alert Message
        alertDialog.show();

    }
//    @Override
//    public void onResume() {
//        super.onResume();
////        MainActivity mainActivity = (MainActivity) getActivity();
////        mainActivity.getLocation();



//    }







//    @Override
//    public void onMethodCallback(int position) {
//        // TODO- define actions to occur upon callback
//        // Now, switch fragments via regular listener process
//
//
//
//
//
//    }
}

