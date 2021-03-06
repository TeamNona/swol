package noaleetz.com.swol.ui.fragments;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.facebook.shimmer.ShimmerFrameLayout;
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
import noaleetz.com.swol.ui.activities.MainActivity;
import noaleetz.com.swol.ui.adapters.FeedAdapter;
import noaleetz.com.swol.R;
import noaleetz.com.swol.models.Workout;

import static android.view.View.GONE;


/**
 * A simple {@link Fragment} subclass.
 */
public class FeedFragment extends Fragment implements View.OnClickListener{

    public static final String ARG_REVEAL_SETTINGS = "arg_reveal_settings";

    private static final String TAG = "FeedFragmentTAG";



    @BindView(R.id.rvPosts)
    RecyclerView rvPosts;
    @BindView(R.id.swipeContainer)
    SwipeRefreshLayout swipeContainer;


    ImageView ivFilterOptions;
    @BindView(R.id.shimmer_view_container)
    ShimmerFrameLayout mShimmerViewContainer;

    @BindView(R.id.tvTimeFilter)
    TextView tvTimeFilter;
    @BindView(R.id.tvDistanceFilter)
    TextView tvDistanceFilter;
    @BindView(R.id.tvSortByFilter)
    TextView tvSortByFilter;
    @BindView(R.id.tvTypeFilter)
    TextView tvTypeFilter;
    @BindView(R.id.ivApply)
    ImageView ivApply;


    private FeedAdapter adapter;
    private List<Workout> posts;
    private Unbinder unbinder;
    String maxMileString;
    String maxHourString;
    String tagString;
    ParseGeoPoint currentGeoPoint;
    FloatingActionButton fabAdd;

    String selectedSort;

    String[] sortListItems;


    // to pass into query

    ArrayList<String> TypesToQueryBy = new ArrayList<>();
    Double milesAway;
    Integer timeAwayNumber;
    String timeAwayUnit;
    String sortBy;



    String[] categories;
    String[] categoryItems;
    boolean[] checkedItems;
    ArrayList<Integer> mUserItems = new ArrayList<>();



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

        if (swipeContainer.isRefreshing()) {
            swipeContainer.setRefreshing(false);
        }


    }

    public void FilterUIDefaultState(){
        tvDistanceFilter.setText("Distance");
        tvDistanceFilter.setBackground(getResources().getDrawable(R.drawable.rect_grey));

        tvTimeFilter.setText("Time");
        tvTimeFilter.setBackground(getResources().getDrawable(R.drawable.rect_grey));

        tvSortByFilter.setText("Sort By");
        tvSortByFilter.setBackground(getResources().getDrawable(R.drawable.rect_grey));

        tvTypeFilter.setText("Type");
        tvTypeFilter.setBackground(getResources().getDrawable(R.drawable.rect_grey));

        TypesToQueryBy=null;
        milesAway=null;
        timeAwayNumber=null;
        timeAwayUnit=null;
        sortBy="";

        ivApply.setVisibility(GONE);

    }



    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        this.adapter = new FeedAdapter(posts); // this class implements callback

        fabAdd = getActivity().findViewById(R.id.fabAdd);
        fabAdd.show();


        posts = new ArrayList<>();
        adapter = new FeedAdapter(posts);
        Log.d(TAG, "Finished setting the adapter");
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        rvPosts.setLayoutManager(linearLayoutManager);
        rvPosts.setAdapter(adapter);


        categories = new String[]{"Bike", "Cardio","Class","Dance","Game","Gym","High Intensity Interval Training","Hike","Meditation","Run","Swim","Weight"};

        categoryItems = getResources().getStringArray(R.array.workout_types);
//        categoryItems = getResources().getStringArray(R.array.workout_categories);
        checkedItems = new boolean[categoryItems.length];



        loadTopPosts();

        tvDistanceFilter.setOnClickListener(this);
        tvSortByFilter.setOnClickListener(this);
        tvTimeFilter.setOnClickListener(this);
        tvTypeFilter.setOnClickListener(this);

        ivApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // apply the filters selected by user

                filter(TypesToQueryBy,milesAway,timeAwayNumber,sortBy);
                ivApply.setVisibility(GONE);

            }
        });

        FilterUIDefaultState();




        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Your code to refresh the list here.
                // Make sure you call swipeContainer.setRefreshing(false)
                // once the network request has completed successfully.
                mShimmerViewContainer.setVisibility(View.VISIBLE);

                mShimmerViewContainer.startShimmerAnimation();

                loadTopPosts();
//                svSearch.setQuery("",false);
//                ivFilterOptions.setImageResource(R.drawable.ic_arrow);
                FilterUIDefaultState();

                fetchTimelineAsync(0);
//                svSearch.setQuery("",false);
//                ivFilterOptions.setImageResource(R.drawable.ic_arrow);



                swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                        android.R.color.holo_green_light,
                        android.R.color.holo_orange_light,
                        android.R.color.holo_red_light);

            }


        });
//        ivFilterOptions.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//                //Creating the instance of PopupMenu
//                final PopupMenu popup = new PopupMenu(view.getContext(), ivFilterOptions);
//                setForceShowIcon(popup);
//                //Inflating the Popup using xml file
//                popup.getMenuInflater()
//                        .inflate(R.menu.popup_icon, popup.getMenu());
//
//
//                //registering popup with OnMenuItemClickListener
//
//                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
//                    public boolean onMenuItemClick(MenuItem item) {
//                        // toast on item click
////                        Toast.makeText(
////                                getContext(),
////                                "You Clicked : " + item.getTitle(),
////                                Toast.LENGTH_SHORT
////                        ).show();
//                        // select icon
//                        selectPopupItem(item);
//                        popup.dismiss();
//                        return true;
//                    }
//                });
//
//                popup.show(); //showing popup menu
//            }
//
//
//        });


    }

    // methods for handling tag click


    @Override
    public void onClick(View view) {



        switch(view.getId()){

            case R.id.tvDistanceFilter:


                Context mContext = getContext();

                RelativeLayout relative = new RelativeLayout(mContext);
                final NumberPicker aNumberPicker = new NumberPicker(mContext);
                aNumberPicker.setMaxValue(10);
                aNumberPicker.setMinValue(1);


                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 50);

                RelativeLayout.LayoutParams numPicerParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);


                numPicerParams.addRule(RelativeLayout.CENTER_IN_PARENT);
                relative.setLayoutParams(params);
                relative.addView(aNumberPicker,numPicerParams);


                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);
                alertDialogBuilder.setTitle("Filter Your Search By a Max Number of Miles");
                alertDialogBuilder.setCancelable(true);

                alertDialogBuilder.setView(relative);
                alertDialogBuilder


                        .setPositiveButton("Ok",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int id) {
                                        tvDistanceFilter.setText((String.valueOf(aNumberPicker.getValue()) + " mile(s) away"));
                                        tvDistanceFilter.setBackground(getResources().getDrawable(R.drawable.drawable_rectangle_blue));
                                        ivApply.setVisibility(View.VISIBLE);


                                        // store milesAway for filter function

                                        milesAway = Double.parseDouble(String.valueOf(aNumberPicker.getValue()));


                                    }
                                })
                        .setNegativeButton("Clear",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int id) {

                                        tvDistanceFilter.setText("Distance");
                                        tvDistanceFilter.setBackground(getResources().getDrawable(R.drawable.rect_grey));
                                        ivApply.setVisibility(View.VISIBLE);



                                        // TODO- if milesAway = 0- filter function knows that no distance max is wanted

                                        milesAway = null;

                                        dialog.cancel();

                                    }
                                });
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation_2;
                alertDialog.show();




                break;


            case R.id.tvTimeFilter:
                Context TmContext = getContext();

                RelativeLayout Trelative = new RelativeLayout(TmContext);
                final NumberPicker timeValuePicker = new NumberPicker(TmContext);
                timeValuePicker.setMaxValue(10);
                timeValuePicker.setMinValue(1);
                final NumberPicker unitTypePicker = new NumberPicker(TmContext);
                unitTypePicker.setMaxValue(2);
                unitTypePicker.setMinValue(1);
                unitTypePicker.setDisplayedValues(new String[] { "Day(s)","Hour(s)"});

                RelativeLayout.LayoutParams Tparams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 50);

                RelativeLayout.LayoutParams TnumPicerParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                RelativeLayout.LayoutParams TqPicerParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);


                TnumPicerParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                TqPicerParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                Trelative.setLayoutParams(Tparams);
                Trelative.addView(timeValuePicker,TnumPicerParams);
                Trelative.addView(unitTypePicker,TqPicerParams);


                AlertDialog.Builder TalertDialogBuilder = new AlertDialog.Builder(TmContext);
                TalertDialogBuilder.setTitle("Begins in Less Than");
                TalertDialogBuilder.setView(Trelative);
                TalertDialogBuilder.setCancelable(true);
                TalertDialogBuilder
                        .setCancelable(true)
                        .setPositiveButton("Ok",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int id) {
                                        tvTimeFilter.setText("In " + timeValuePicker.getValue() + " " + unitTypePicker.getDisplayedValues()[unitTypePicker.getValue() - 1]);
                                        tvTimeFilter.setBackground(getResources().getDrawable(R.drawable.drawable_rectangle_red));

                                        // store timeAway for filter function
                                        timeAwayUnit = unitTypePicker.getDisplayedValues()[unitTypePicker.getValue() - 1];
                                        timeAwayNumber = timeValuePicker.getValue();
                                        ivApply.setVisibility(View.VISIBLE);
//                svSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//                    @Override
//                    public boolean onQueryTextSubmit(String s) {
//                        if(s.isEmpty()) {
//                            NullHourAlert();
//                        }
//                        else {
//                            int maxHour = Integer.parseInt(s);
//                            filter(null, null, maxHour, "time");
//                        }
//                        return true;
//                    }



                                    }
                                })
                        .setNegativeButton("Clear",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int id) {

                                        tvTimeFilter.setText("Time");
                                        tvTimeFilter.setBackground(getResources().getDrawable(R.drawable.rect_grey));
                                        ivApply.setVisibility(View.VISIBLE);


                                        //TODO- if timeAwayNumber = 0 - filter knows that no time filter applied

                                        timeAwayUnit = null;
                                        timeAwayNumber = null;
                                        dialog.cancel();
                                    }
                                });
                AlertDialog TalertDialog = TalertDialogBuilder.create();
                TalertDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation_2;

                TalertDialog.show();

                break;

//            case R.id.filterByDistance:
//                svSearch.setQueryHint("filter by number of miles");
//                svSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//                    @Override
//                    public boolean onQueryTextSubmit(String s) {
//                        maxMileString = s;
//                        if(maxMileString.isEmpty()){
//                            maxMileString = "30";
//                        }
//                        else{
//                            double maxMileDouble = Double.parseDouble(maxMileString);
//                            filter(null, maxMileDouble, null, "distance");
//                        }
//                        return true;
//                    }


            case R.id.tvSortByFilter:

                // create list of items
                sortListItems = new String[]{"Time","Distance"};
                AlertDialog.Builder sortBuilder = new AlertDialog.Builder(getContext());
                sortBuilder.setTitle("Sort By");
                sortBuilder.setIcon(R.drawable.ic_filter);
                sortBuilder.setCancelable(true);

                sortBuilder.setSingleChoiceItems(sortListItems, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        selectedSort = sortListItems[i];

                    }
                });

                sortBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {


                        // user clicked OK, so save the mSelectedItems results somewhere
                        // or return them to the component that opened the dialog

                        sortBy = selectedSort;
                        dialogInterface.dismiss();

                        if(sortBy.equals("")) {
                            sortBy = "Time";

                        }
                        tvSortByFilter.setText("Sorted By: " + sortBy);
                        tvSortByFilter.setBackground(getResources().getDrawable(R.drawable.drawable_rectangle_orange));
                        ivApply.setVisibility(View.VISIBLE);


//                svSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//                    @Override
//                    public boolean onQueryTextSubmit(String s) {
//                        creatorUsername = String.valueOf(svSearch.getQuery());
//                        if(creatorUsername.isEmpty()){
//                            NullUserAlert();
//                        }
//                        return true;
//                    }



                    }
                });
                sortBuilder.setNegativeButton("Clear", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                            sortBy = "";
                            dialogInterface.dismiss();

                        tvSortByFilter.setText("Sort By");
                        tvSortByFilter.setBackground(getResources().getDrawable(R.drawable.rect_grey));
                        ivApply.setVisibility(View.VISIBLE);

                    }
                });

                AlertDialog mSortDialog = sortBuilder.create();
                mSortDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation_2;

                mSortDialog.show();


                break;


            case R.id.tvTypeFilter:

                AlertDialog.Builder mTypeBuilder = new AlertDialog.Builder(this.getContext());
                mTypeBuilder.setTitle("Filter by Categories");
                mTypeBuilder.setMultiChoiceItems(categoryItems, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int position, boolean isChecked) {
                        if(isChecked){
                            if(!mUserItems.contains(position)){
                                mUserItems.add(position);
                            }
                            else{
                                mUserItems.remove(position);
                            }
                        }
                    }
                });

                mTypeBuilder.setCancelable(true);
                mTypeBuilder.setPositiveButton("Apply", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        String item = "";
                        for(int i=0;i<mUserItems.size();i++){
                            TypesToQueryBy = new ArrayList<>();
                            TypesToQueryBy.add(categoryItems[mUserItems.get(i)]);

                            Log.d(TAG,"added to type query array:" + categoryItems[mUserItems.get(i)]);
                            Log.d(TAG,"full query array list:" + TypesToQueryBy.toString());

                            item = item + categoryItems[mUserItems.get(i)];
                            if(i != mUserItems.size() -1 ){
                                item = item + ", ";
                            }
                        }
                        if(item.isEmpty()){
                            tvTypeFilter.setText("Categories");
                            tvTypeFilter.setBackground(getResources().getDrawable(R.drawable.rect_grey));
                        }
                        else {
                            tvTypeFilter.setText(item);
                            tvTypeFilter.setBackground(getResources().getDrawable(R.drawable.drawable_rectangle_purple));
                        }
                        ivApply.setVisibility(View.VISIBLE);

                    }
                });
                mTypeBuilder.setNeutralButton("Clear All", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        if(mUserItems == null || TypesToQueryBy == null) {
                                dialogInterface.dismiss();
                        }
                        else{

                            for (int i=0;i< checkedItems.length;i++){

                            checkedItems[i] = false;

                            mUserItems.clear();

                            TypesToQueryBy.clear();
                            Log.d(TAG,"clear all category filters");

                            tvTypeFilter.setText("Categories");
                            tvTypeFilter.setBackground(getResources().getDrawable(R.drawable.rect_grey));
                            ivApply.setVisibility(View.VISIBLE);
                        }

                        }
                    }
                });
                AlertDialog mDialog = mTypeBuilder.create();
                mDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation_2;

                mDialog.show();

                break;

        }

    }



    /**
     * This function does all of the filtering/sorting for you. This should be run when you click the "apply" wherever you have the filters to apply them.
     * @param categories This is an ArrayList of Strings that represents all of the categories
     *                   that the user would like to see. If the user does not want to filter
     *                   by category, simply pass in null for this value.
     * @param milesAway This is a double that represents the distance radius that should be
     *                  included in the filter. If the user does not want to filter by miles
     *                  away, simply pass in null for this value.
     * @param timeAway This is an integer that represents the time frame that you would like
     *                 the workouts to be filtered by. If the user does not want to filter by
     *                 time, simply pass in null for this value.
     * @param sortBy This is a string that represents how the user wants the information to be
     *               sorted. Only accepted values: "time", "distance". If it is anything else
     *               (including null), the list will not be sorted.
     */
    public void filter(List<String> categories, Double milesAway, Integer timeAway, final String sortBy) {

        final Workout.Query query = new Workout.Query().withUser();

        currentGeoPoint = ParseUser.getCurrentUser().getParseGeoPoint("currentLocation");

        if (categories != null) for (String category : categories) query.whereEqualTo("eventCategory", category);

        if (milesAway != null) query.getWithinRange(currentGeoPoint, milesAway);

        if (timeAway != null) query.getWithinTimeRange(timeAway);

        final Comparator<Workout> comparator;

        switch (sortBy) {
            case "time":
                comparator = new Comparator<Workout>() {
                    @Override
                    public int compare(Workout o1, Workout o2) {
                        return o1.compareToTime(o2);
                    }
                };
                break;
            case "distance":
                comparator = new Comparator<Workout>() {
                    @Override
                    public int compare(Workout o1, Workout o2) {
                        return o1.compareToDistance(o2);
                    }
                };
                break;
            default:
                comparator = new Comparator<Workout>() {
                    @Override
                    public int compare(Workout o1, Workout o2) {
                        return 0;
                    }
                };

        }

        query.findInBackground(new FindCallback<Workout>() {
            @Override
            public void done(List<Workout> objects, ParseException e) {

                if (e == null) {
                    Collections.sort(objects, comparator);

                    posts.clear();

                    posts.addAll(objects);
                    adapter.notifyDataSetChanged();
                    rvPosts.scrollToPosition(0);




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
            public void done(final List<Workout> objects, ParseException e) {
                if (e == null) {
                    Log.d(TAG, Integer.toString(objects.size()));
                    for (int i = 0; i < objects.size(); i++) {
//                        Log.d(TAG, "Post [" + i + "] = " + objects.get(i).getDescription()
//                                + "\nusername: " + objects.get(i).getUser().getUsername());
                    }
                    posts.clear();

                    // Stopping Shimmer Effect's animation after data is loaded to ListView

                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            // Do something after 5s = 5000ms
                            mShimmerViewContainer.stopShimmerAnimation();
                            mShimmerViewContainer.setVisibility(View.GONE);

//                            posts.addAll(objects);
//                            adapter.notifyDataSetChanged();
                        }
                    }, 3000);


                    posts.addAll(objects);
                    adapter.notifyDataSetChanged();

                    if (swipeContainer.isRefreshing()) {
                        swipeContainer.setRefreshing(false);
                    }
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





    public void NullHourAlert(){
        AlertDialog alertDialog = new AlertDialog.Builder(
                this.getContext()).create();
        alertDialog.setTitle("Wait a second!");
        alertDialog.setMessage("Please enter a limit for how many hours away your available workouts should be");
        alertDialog.setIcon(R.drawable.ic_pencil);
        alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // Write your code here to execute after dialog closed
//                            Toast.makeText(getApplicationContext(), "You clicked on OK", Toast.LENGTH_SHORT).show();
            }
        });
        // Showing Alert Message
        alertDialog.show();
    }
    public void NullUserAlert(){
        AlertDialog alertDialog = new AlertDialog.Builder(
                this.getContext()).create();
        alertDialog.setTitle("Wait a second!");
        alertDialog.setMessage("Please enter a user to filter available workouts");
        alertDialog.setIcon(R.drawable.ic_pencil);
        alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // Write your code here to execute after dialog closed
//                            Toast.makeText(getApplicationContext(), "You clicked on OK", Toast.LENGTH_SHORT).show();
            }
        });
        // Showing Alert Message
        alertDialog.show();
    }


    @Override
    public void onResume() {
        super.onResume();
        mShimmerViewContainer.setVisibility(View.VISIBLE);

        mShimmerViewContainer.startShimmerAnimation();
    }




}

