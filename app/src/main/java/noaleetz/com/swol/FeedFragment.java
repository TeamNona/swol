package noaleetz.com.swol;


import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
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
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

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
    String tagString;
    ParseGeoPoint currentGeoPoint;


//    ArrayList<String> tags = ["Bike", "Cardio","Class","Dance","Game","Gym","High Intensuty Interval Training","Hike","Meditation","Run","Swim","Weight"]




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
                        QueryByString(tagString);

                    }
                });


                break;
            case R.id.filterByTime:

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

    private void QueryByString(String tagString) {


    }


    public void QueryByUser() {
        final Workout.Query postQuery = new Workout.Query();


    }
    public void QueryByDistance(double maxMileNumber) {

        final Workout.Query postDistanceQuery = new Workout.Query();

        currentGeoPoint = ParseUser.getCurrentUser().getParseGeoPoint("currentLocation");

//        postDistanceQuery.withUser().orderByLastCreated().getWithinRange(currentLocation,maxMileNumber);

        postDistanceQuery.withUser().orderByLastCreated().getWithinRange(currentGeoPoint, maxMileNumber);

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
                    adapter.notifyDataSetChanged();
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

