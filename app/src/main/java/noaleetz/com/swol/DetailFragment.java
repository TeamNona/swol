package noaleetz.com.swol;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.parse.CountCallback;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import bolts.Task;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import noaleetz.com.swol.models.User;
import noaleetz.com.swol.models.Workout;


/**
 * A simple {@link Fragment} subclass.
 */
public class DetailFragment extends Fragment {

    FloatingActionButton fab;



    private static final String TAG = "TAGDetailFragment";
    @BindView(R.id.btBack)
    ImageView btBack;
    @BindView(R.id.tvWorkoutTitle)
    TextView tvWorkoutTitle;
    @BindView(R.id.tvBeginsIn)
    TextView tvBeginsIn;
    @BindView(R.id.ivTimeIcon)
    ImageView ivTimeIcon;
    @BindView(R.id.ivAvatar)
    ImageView ivAvatar;
    @BindView(R.id.tvFullName)
    TextView tvFullName;
    @BindView(R.id.tvUsername)
    TextView tvUsername;
    @BindView(R.id.ivImage)
    ImageView ivImage;
    @BindView(R.id.rvParticipants)
    RecyclerView rvParticipants;
    @BindView(R.id.tvDescription)
    TextView tvDescription;
    @BindView(R.id.tvLikesCt)
    TextView tvLikesCt;
    @BindView(R.id.ivHeartIcon)
    ImageView ivHeartIcon;
    @BindView(R.id.ivCommentIcon)
    ImageView ivCommentIcon;
    @BindView(R.id.ivJoin)
    ImageView ivJoin;

    Workout workout;

    String url;
    String url_post;


    private ParticipantAdapter adapter;
    private List<ParseUser> participants;

    private Unbinder unbinder;

    public DetailFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment


        View view = inflater.inflate(R.layout.fragment_detail, container, false);
        unbinder = ButterKnife.bind(this, view);


        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fab = (FloatingActionButton) getActivity().findViewById(R.id.fab);
        fab.hide();


        final RoundedCornersTransformation roundedCornersTransformation = new RoundedCornersTransformation(30, 30);
        final RequestOptions requestOptions = RequestOptions.bitmapTransform(roundedCornersTransformation);

        Bundle bundle = getArguments();
        workout = (Workout) bundle.getParcelable("workout");

        tvWorkoutTitle.setText(workout.getName().toString());
        tvBeginsIn.setText(workout.getTimeUntil());
        tvFullName.setText(workout.getUser().getString("name"));
        tvUsername.setText(workout.getUser().getUsername());
        tvDescription.setText(workout.getDescription());

        Log.d(TAG, "Tag 1" + workout.get("eventParticipants").toString());

        // Load user avatar
        try {
            url = workout.getUser()
                    .fetchIfNeeded()
                    .getParseFile("profilePicture")
                    .getUrl();
        } catch (ParseException e) {
            e.printStackTrace();
            Log.d(TAG, "AvatarImage did not load");
        }

        Glide.with(DetailFragment.this)
                .load(url)
                .apply(requestOptions)
                .into(ivAvatar);

        // load workout image

        try {
            url_post = workout
                    .fetchIfNeeded()
                    .getParseFile("media")
                    .getUrl();
        } catch (ParseException e) {
            e.printStackTrace();
            Log.d(TAG, "WorkoutImage did not load");
        }

        Glide.with(DetailFragment.this)
                .load(url_post)
                .apply(requestOptions)
                .into(ivImage);

        getLikesCount(workout);

        // set up and populate data for adapter

        participants = new ArrayList<>();

        this.adapter = new ParticipantAdapter(participants);
        Log.d(TAG, "finished setting up participant adapter");
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        rvParticipants.setLayoutManager(linearLayoutManager);
        rvParticipants.setAdapter(adapter);

        loadParticipants(workout.getParticipants());


        btBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                FragmentManager fm = getActivity().getSupportFragmentManager();
                fab.show();
                fm.popBackStackImmediate();

            }


        });

        ivJoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // user clicks join workout to add themselves to participant list

                JSONArray old_list = new JSONArray();
                old_list = workout.getParticipants();
                Log.d(TAG, old_list.toString());
                String UserIdToAdd = ParseUser.getCurrentUser().getObjectId();
                JSONArray new_list = old_list.put(UserIdToAdd);
                Log.d(TAG,new_list.toString());




            }
        });
    }

    public void getLikesCount(Workout workout_event){
        String workoutId = workout_event.getObjectId();



        ParseQuery<ParseObject> exerciseEvent = ParseQuery.getQuery("exerciseEvent");

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Likes");

        query.whereEqualTo("likedPost",workout_event).countInBackground(new CountCallback() {
            @Override
            public void done(int count, ParseException e) {
                if (e == null){
                    // no error
                    Log.d(TAG, "like count:" + String.valueOf(count));
                    tvLikesCt.setText(String.valueOf(count));

                }
                else{
                    // something went wrong
                    Log.d(TAG, "unable to find like count");
                    tvLikesCt.setText(String.valueOf(0));
                }
            }
        });

    }






    // get participant data and add it to list to assemble adapter

    public void loadParticipants(final JSONArray user_ids) {

        participants.clear();


        Log.d(TAG, Integer.toString(user_ids.length()));

        final List<ParseUser> participant_list = new ArrayList<>();


        for (int i = 0; i < user_ids.length(); i++) {
            try {

                ParseQuery<ParseUser> query = ParseQuery.getQuery("_User");
                query.whereEqualTo("objectId",user_ids.get(i));



                query.findInBackground(new FindCallback<ParseUser>() {
                    public void done(List<ParseUser> object, ParseException e) {
                        if (e == null) {
                            ParseUser user = object.get(0);
                            participants.add(user);
                            adapter.notifyDataSetChanged();

                            // object will be your User
//                            participant_list.add((ParseUser) object);
                            Log.d(TAG, "user object added:" + user.getUsername());

                        } else {
                            // something went wrong
                            Log.d(TAG, "user object not added");
                        }
                    }

                });



//                participant_list.add((ParseUser) user_ids.get(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        Log.d(TAG, "participant list of objects" + participant_list);

//        participants.clear();
//        participants.addAll(participant_list);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }


}
