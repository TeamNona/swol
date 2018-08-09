package noaleetz.com.swol.ui.fragments;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
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
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import noaleetz.com.swol.CategoriesDialogFragment;
import noaleetz.com.swol.ui.adapters.FeedAdapter;
import noaleetz.com.swol.R;
import noaleetz.com.swol.models.Workout;


/**
 * A simple {@link Fragment} subclass.
 */
public class FeedFragment extends Fragment implements CategoriesDialogFragment.CategoryDialogListener,View.OnClickListener{

    private static final String TAG = "FeedFragmentTAG";
//    CategoriesDialogFragment.CategoryDialogListener listener = new CategoriesDialogFragment.CategoryDialogListener() {
//        @Override
//        public void onFinishCategoryDialog(String inputText) {
//            svSearch.setQuery(inputText,false);
//        }
//    };



//    private AdapterView.OnItemSelectedListener listener;


    @BindView(R.id.rvPosts)
    RecyclerView rvPosts;
    @BindView(R.id.swipeContainer)
    SwipeRefreshLayout swipeContainer;
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




//    @BindView(R.id.svSearch)
//    android.widget.SearchView svSearch;
//    @BindView(R.id.ivFilterOptions)
//    ImageView ivFilterOptions;

//    @BindView(R.id.search_src_text)
//    android.support.v7.widget.SearchView.SearchAutoComplete categorySearchAutoComplete;
    private FeedAdapter adapter;
    private List<Workout> posts;
    private Unbinder unbinder;
    String maxMileString;
    String maxHourString;
    String tagString;
    ParseGeoPoint currentGeoPoint;
    FloatingActionButton fab;

    String selectedSort;

    String[] sortListItems;


    // to pass into query

    ArrayList<String> TypesToQueryBy = new ArrayList<>();
    Double milesAway;
    Integer timeAwayNumber;
    String timeAwayUnit;
    String sortBy;



    String[] categories;
    private String creatorUsername;
    String[] categoryItems;
    boolean[] checkedItems;
    boolean[] checkedSorts;
    ArrayList<Integer> mUserItems = new ArrayList<>();
    public static int Max;



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

        tvTypeFilter.setText("Categories");
        tvTypeFilter.setBackground(getResources().getDrawable(R.drawable.rect_grey));

        TypesToQueryBy=null;
        milesAway=null;
        timeAwayNumber=null;
        timeAwayUnit=null;
        sortBy=null;

        ivApply.setVisibility(View.INVISIBLE);

    }



    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        this.adapter = new FeedAdapter(posts); // this class implements callback
        fab = (FloatingActionButton) getActivity().findViewById(R.id.fab);
        fab.bringToFront();


        posts = new ArrayList<>();
        adapter = new FeedAdapter(posts);
        Log.d(TAG, "Finished setting the adapter");
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        rvPosts.setLayoutManager(linearLayoutManager);
        rvPosts.setAdapter(adapter);


        categories = new String[]{"Bike", "Cardio","Class","Dance","Game","Gym","High Intensity Interval Training","Hike","Meditation","Run","Swim","Weight"};

        categoryItems = getResources().getStringArray(R.array.workout_types);
        checkedItems = new boolean[categoryItems.length];



        loadTopPosts();

        tvDistanceFilter.setOnClickListener(this);
        tvSortByFilter.setOnClickListener(this);
        tvTimeFilter.setOnClickListener(this);
        tvTypeFilter.setOnClickListener(this);

        FilterUIDefaultState();



        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Your code to refresh the list here.
                // Make sure you call swipeContainer.setRefreshing(false)
                // once the network request has completed successfully.
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
                TalertDialog.show();

                break;


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

                        if(sortBy == null) {
                            sortBy = "Time";

                        }
                        tvSortByFilter.setText("Sorted By: " + sortBy);
                        tvSortByFilter.setBackground(getResources().getDrawable(R.drawable.drawable_rectangle_orange));
                        ivApply.setVisibility(View.VISIBLE);





                    }
                });
                sortBuilder.setNegativeButton("Clear", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                            sortBy = null;
                            dialogInterface.dismiss();

                        tvSortByFilter.setText("Sort By");
                        tvSortByFilter.setBackground(getResources().getDrawable(R.drawable.rect_grey));
                        ivApply.setVisibility(View.VISIBLE);

                    }
                });

                AlertDialog mSortDialog = sortBuilder.create();
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
                mDialog.show();

                break;

        }

    }

//    public static void setForceShowIcon(PopupMenu popupMenu) {
//        try {
//            Field[] fields = popupMenu.getClass().getDeclaredFields();
//            for (Field field : fields) {
//                if ("mPopup".equals(field.getName())) {
//                    field.setAccessible(true);
//                    Object menuPopupHelper = field.get(popupMenu);
//                    Class<?> classPopupHelper = Class.forName(menuPopupHelper
//                            .getClass().getName());
//                    Method setForceIcons = classPopupHelper.getMethod(
//                            "setForceShowIcon", boolean.class);
//                    setForceIcons.invoke(menuPopupHelper, true);
//                    break;
//                }
//            }
//        } catch (Throwable e) {
//            e.printStackTrace();
//        }
//    }

//    public void showTypeDialog(View view) {
//
//        FragmentManager manager = getFragmentManager();
//
//
//        CategoriesDialogFragment dialog = new CategoriesDialogFragment();
////        CategoriesDialogFragment.setTargetFragment(FeedFragment.this, 300);
////        dialog.setCallBack(listener);
//
//        dialog.show(manager,"dialog");
//
//    }









//    public void selectPopupItem(MenuItem menuItem) {
//        ivFilterOptions.setImageDrawable(menuItem.getIcon());
//        svSearch.setSubmitButtonEnabled(true);
//
//
//        switch (menuItem.getItemId()) {
//            case R.id.filterByTag:
//
//                svSearch.setQueryHint("filter by Category");
//                showDialog(getView());
//                svSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//                    @Override
//                    public boolean onQueryTextSubmit(String s) {
////                        tagString = svSearch.getQuery().toString();
//                        tagString = s;
//
//                        // check if category exists
//                        if(Arrays.asList(categories).contains(tagString)){
//                            // user has searched an existing category
//                            QueryByCategory(tagString);
//                        }
//                        else{
//                            CategoryNullOrDoesNotExist();
//                        }
//                        return false;
//                    }
//
//                    @Override
//                    public boolean onQueryTextChange(String s) {
//                        return false;
//                    }
//                });
//
//
//
//                break;
//            case R.id.filterByTime:
//
//                svSearch.setQueryHint("filter by hours away");
//                maxHourString = svSearch.getQuery().toString();
//
//
//                svSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//                    @Override
//                    public boolean onQueryTextSubmit(String s) {
//                        if(maxHourString.isEmpty()) {
//                            NullHourAlert();
//                        }
//                        else {
//                            long maxHourLong = Long.parseLong(maxHourString);
//                            QueryByTime(maxHourLong);
//                        }
//                        return true;
//                    }
//
//                    @Override
//                    public boolean onQueryTextChange(String s) {
//                        return false;
//                    }
//                });
//
//
//                break;
//            case R.id.filterByDistance:
//                svSearch.setQueryHint("filter by number of miles");
//                svSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//                    @Override
//                    public boolean onQueryTextSubmit(String s) {
//                        maxMileString = s;
//                        if(maxMileString.isEmpty()){
//                            maxMileString = "30";
//                        }
//                        else{double maxMileDouble = Double.parseDouble(maxMileString);
//                            QueryByDistance(maxMileDouble);}
//                        return true;
//                    }
//
//                    @Override
//                    public boolean onQueryTextChange(String s) {
//                        return false;
//                    }
//                });
//
//
//                break;
//            case R.id.filterByTitle:
//                svSearch.setQueryHint("filter by Workout Title");
//
//                break;
//            case R.id.filterByUser:
//                svSearch.setQueryHint("filter by Creator");
//
//                svSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//                    @Override
//                    public boolean onQueryTextSubmit(String s) {
//                        creatorUsername = String.valueOf(svSearch.getQuery());
//                        if(creatorUsername.isEmpty()){
//                            NullUserAlert();
//                        }
//                        else{
//                            QueryByUserCreated(creatorUsername);
//
//                        }
//                        return true;
//                    }
//
//                    @Override
//                    public boolean onQueryTextChange(String s) {
//                        return false;
//                    }
//                });
//
//                break;
//            default:
//                return;
//        }
//
//        // Highlight the selected item has been done by NavigationView
//        menuItem.setChecked(true);
//
//        // Close the navigation drawer
//
//    }


//    private void QueryByTime(long maxHourLong) {
//        final Workout.Query postTimeQuery = new Workout.Query();
//        postTimeQuery.withUser().getWithinTimeRange(maxHourLong);
//        postTimeQuery.findInBackground(new FindCallback<Workout>() {
//            @Override
//            public void done(List<Workout> objects, ParseException e) {
//                if(e==null){
//
//                // order objects in time order
//                Collections.sort(objects, new Comparator<Workout>() {
//                    @Override
//                    public int compare(Workout o1, Workout o2) {
//                        return o1.compareToTime(o2);
//                    }
//                });
//
//                posts.clear();
//                posts.addAll(objects);
//                adapter.notifyDataSetChanged();
//                rvPosts.scrollToPosition(0);
//                InputMethodManager mgr = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
//                assert mgr != null;
//                mgr.hideSoftInputFromWindow(svSearch.getWindowToken(), 0);
//
//                // TODO- scroll to bottom option
//            } else {
//                e.printStackTrace();
//            }
//            }
//        });
//
//
//    }
//
//    private void QueryByCategory(String tagString) {
//        final Workout.Query categoryQuery = new Workout.Query();
//        categoryQuery.getTop().orderByLastCreated().whereEqualTo("eventCategory",tagString).findInBackground(new FindCallback<Workout>() {
//            @Override
//            public void done(List<Workout> objects, ParseException e) {
//                posts.clear();
//
//                posts.addAll(objects);
//                adapter.notifyDataSetChanged();
//
//                rvPosts.scrollToPosition(0);
//                InputMethodManager mgr = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
//                assert mgr != null;
//                mgr.hideSoftInputFromWindow(svSearch.getWindowToken(), 0);
//            }
//        });
//
//
//    }
//
//
//    public void QueryByUserCreated(String creatorUsername) {
////        final Workout.Query creatorQuery = new Workout.Query();
////
////
////        ParseUser userObject;
////        creatorQuery.withUser().createdBy(userObject);
//
//    }
//    public void QueryByDistance(double maxMileNumber) {
//
//        final Workout.Query postDistanceQuery = new Workout.Query();
//
//        currentGeoPoint = ParseUser.getCurrentUser().getParseGeoPoint("currentLocation");
//
////        postDistanceQuery.withUser().orderByLastCreated().getWithinRange(currentLocation,maxMileNumber);
//
//        postDistanceQuery.withUser().getWithinRange(currentGeoPoint, maxMileNumber).findInBackground(new FindCallback<Workout>() {
//            @Override
//            public void done(List<Workout> objects, ParseException e) {
//                if (e == null) {
//                    Log.d(TAG, Integer.toString(objects.size()));
//                    for (int i = 0; i < objects.size(); i++) {
////                        Log.d(TAG, "Post [" + i + "] = " + objects.get(i).getDescription()
////                                + "\nusername: " + objects.get(i).getUser().getUsername());
//                    }
//
//                    // order objects in distance order
//                    Collections.sort(objects, new Comparator<Workout>() {
//                        @Override
//                        public int compare(Workout o1, Workout o2) {
//                            return o1.compareToDistance(o2);
//                        }
//                    });
//
//                    posts.clear();
//
//                    posts.addAll(objects);
//                    adapter.notifyDataSetChanged();
//                    rvPosts.scrollToPosition(0);
//                    InputMethodManager mgr = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
//                    assert mgr != null;
//                    mgr.hideSoftInputFromWindow(svSearch.getWindowToken(), 0);
//
//                    // TODO- scroll to bottom option
//                } else {
//                    e.printStackTrace();
//                }
//            }
//        });
//
//
//    }
//


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

        if(tagString.isEmpty()){
            alertDialog.setTitle("Please Enter a Category!");
            alertDialog.setIcon(R.drawable.ic_pencil);
        }
        else{
            // category entered but doesn't exist
            alertDialog.setTitle("Oops!");
            alertDialog.setMessage("This workout category doesn't exist yet");
            alertDialog.setIcon(R.drawable.ic_fitness_center_black_24dp);
        }

        // Setting OK Button
        alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // code here to execute after dialog closed
            }
        });
        alertDialog.show();

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
    public void onFinishCategoryDialog(String inputText) {
        return;
    }



//    @Override
//    public void onFinishCategoryDialog(String inputText) {
//
//
//    }
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

