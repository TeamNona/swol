package noaleetz.com.swol.ui.fragments;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.view.menu.MenuPopupHelper;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;


import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import noaleetz.com.swol.CategoriesDialogFragment;
import noaleetz.com.swol.ui.adapters.FeedAdapter;
import noaleetz.com.swol.R;
import noaleetz.com.swol.models.Workout;

import static android.content.Context.SEARCH_SERVICE;
import static android.support.v4.content.ContextCompat.getSystemService;


/**
 * A simple {@link Fragment} subclass.
 */
public class FeedFragment extends Fragment implements CategoriesDialogFragment.CategoryDialogListener{

    private static final String TAG = "FeedFragmentTAG";
    CategoriesDialogFragment.CategoryDialogListener listener = new CategoriesDialogFragment.CategoryDialogListener() {
        @Override
        public void onFinishCategoryDialog(String inputText) {
            svSearch.setQuery(inputText,false);
        }
    };



//    private AdapterView.OnItemSelectedListener listener;


    @BindView(R.id.rvPosts)
    RecyclerView rvPosts;
    @BindView(R.id.swipeContainer)
    SwipeRefreshLayout swipeContainer;
    @BindView(R.id.svSearch)
    android.widget.SearchView svSearch;
    @BindView(R.id.ivFilterOptions)
    ImageView ivFilterOptions;

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





    String[] categories;
    private String creatorUsername;



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
        fab = (FloatingActionButton) getActivity().findViewById(R.id.fab);
        fab.bringToFront();


        posts = new ArrayList<>();
        adapter = new FeedAdapter(posts);
        Log.d(TAG, "Finished setting the adapter");
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        rvPosts.setLayoutManager(linearLayoutManager);
        rvPosts.setAdapter(adapter);



        categories = new String[]{"Bike", "Cardio","Class","Dance","Game","Gym","High Intensity Interval Training","Hike","Meditation","Run","Swim","Weight"};

        loadTopPosts();





        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Your code to refresh the list here.
                // Make sure you call swipeContainer.setRefreshing(false)
                // once the network request has completed successfully.
                fetchTimelineAsync(0);
                svSearch.setQuery("",false);
                ivFilterOptions.setImageResource(R.drawable.ic_arrow);



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
                setForceShowIcon(popup);
                //Inflating the Popup using xml file
                popup.getMenuInflater()
                        .inflate(R.menu.popup_icon, popup.getMenu());


                //registering popup with OnMenuItemClickListener

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        // toast on item click
//                        Toast.makeText(
//                                getContext(),
//                                "You Clicked : " + item.getTitle(),
//                                Toast.LENGTH_SHORT
//                        ).show();
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
    public static void setForceShowIcon(PopupMenu popupMenu) {
        try {
            Field[] fields = popupMenu.getClass().getDeclaredFields();
            for (Field field : fields) {
                if ("mPopup".equals(field.getName())) {
                    field.setAccessible(true);
                    Object menuPopupHelper = field.get(popupMenu);
                    Class<?> classPopupHelper = Class.forName(menuPopupHelper
                            .getClass().getName());
                    Method setForceIcons = classPopupHelper.getMethod(
                            "setForceShowIcon", boolean.class);
                    setForceIcons.invoke(menuPopupHelper, true);
                    break;
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public void showDialog(View view) {

        FragmentManager manager = getFragmentManager();


        CategoriesDialogFragment dialog = new CategoriesDialogFragment();
//        CategoriesDialogFragment.setTargetFragment(FeedFragment.this, 300);
        dialog.setCallBack(listener);

        dialog.show(manager,"dialog");

    }







    public void selectPopupItem(MenuItem menuItem) {
        ivFilterOptions.setImageDrawable(menuItem.getIcon());
        svSearch.setSubmitButtonEnabled(true);


        switch (menuItem.getItemId()) {
            case R.id.filterByTag:

                svSearch.setQueryHint("filter by Category");
                showDialog(getView());
                svSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String s) {
//                        tagString = svSearch.getQuery().toString();
                        tagString = s;

                        // check if category exists
                        if(Arrays.asList(categories).contains(tagString)){
                            // user has searched an existing category
                            filter(Arrays.asList(new String[]{tagString}), null, null, null);
                        }
                        else{
                            CategoryNullOrDoesNotExist();
                        }
                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String s) {
                        return false;
                    }
                });



                break;
            case R.id.filterByTime:

                svSearch.setQueryHint("filter by hours away");
                maxHourString = svSearch.getQuery().toString();


                svSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String s) {
                        if(s.isEmpty()) {
                            NullHourAlert();
                        }
                        else {
                            int maxHour = Integer.parseInt(s);
                            filter(null, null, maxHour, "time");
                        }
                        return true;
                    }

                    @Override
                    public boolean onQueryTextChange(String s) {
                        return false;
                    }
                });


                break;
            case R.id.filterByDistance:
                svSearch.setQueryHint("filter by number of miles");
                svSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String s) {
                        maxMileString = s;
                        if(maxMileString.isEmpty()){
                            maxMileString = "30";
                        }
                        else{
                            double maxMileDouble = Double.parseDouble(maxMileString);
                            filter(null, maxMileDouble, null, "distance");
                        }
                        return true;
                    }

                    @Override
                    public boolean onQueryTextChange(String s) {
                        return false;
                    }
                });


                break;
            case R.id.filterByTitle:
                svSearch.setQueryHint("filter by Workout Title");

                break;
            case R.id.filterByUser:
                svSearch.setQueryHint("filter by Creator");

                svSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String s) {
                        creatorUsername = String.valueOf(svSearch.getQuery());
                        if(creatorUsername.isEmpty()){
                            NullUserAlert();
                        }
                        return true;
                    }

                    @Override
                    public boolean onQueryTextChange(String s) {
                        return false;
                    }
                });

                break;
            default:
                return;
        }

        // Highlight the selected item has been done by NavigationView
        menuItem.setChecked(true);

        // Close the navigation drawer

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
                    InputMethodManager mgr = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    assert mgr != null;
                    mgr.hideSoftInputFromWindow(svSearch.getWindowToken(), 0);

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

