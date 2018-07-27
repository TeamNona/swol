package noaleetz.com.swol;


import android.app.AlertDialog;
import android.content.DialogInterface;
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
import android.widget.Toast;

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
import noaleetz.com.swol.models.Comments;
import noaleetz.com.swol.models.User;
import noaleetz.com.swol.models.Workout;

import static com.parse.Parse.getApplicationContext;


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
    @BindView(R.id.rvComments)
    RecyclerView rvComments;
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


    private ParticipantAdapter participantAdapter;
    private List<ParseUser> participants;

    private CommentAdapter commentAdapter;
    private List<Comments> comments;

    private Unbinder unbinder;

    public JSONArray participant_list;


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

        tvWorkoutTitle.setText(workout.getName());
        tvBeginsIn.setText(workout.getTimeUntil());
        tvFullName.setText(workout.getUser().getString("name"));
        tvUsername.setText(workout.getUser().getUsername());
        tvDescription.setText(workout.getDescription());

        participant_list = new JSONArray();
        participant_list = workout.getParticipants();

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

        // set up and populate data for  participant adapter

        participants = new ArrayList<>();

        this.participantAdapter = new ParticipantAdapter(participants);
        Log.d(TAG, "finished setting up participant adapter");
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        rvParticipants.setLayoutManager(linearLayoutManager);
        rvParticipants.setAdapter(participantAdapter);

        loadParticipants(workout.getParticipants());

        // set up comment adapter

        comments = new ArrayList<>();
        this.commentAdapter = new CommentAdapter(comments);
        Log.d(TAG, "finished setting up comment adapter");
        LinearLayoutManager commentLinearLayoutManager = new LinearLayoutManager(getContext());
        rvComments.setLayoutManager(commentLinearLayoutManager);
        rvComments.setAdapter(commentAdapter);







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

                Log.d(TAG, "reset participant list" + participant_list.toString());
                String UserIdToAdd = ParseUser.getCurrentUser().getObjectId().toString();

                // check if user has already joined
                if(didUserJoin(participant_list,UserIdToAdd)){
                    Log.d(TAG, "is user there?" + String.valueOf(participant_list.equals(UserIdToAdd)));


                    AlertDialog alertDialog = new AlertDialog.Builder(
                            view.getContext()).create();

                    //Setting Dialog Title
                    alertDialog.setTitle("You Have Already Joined!");

                    // Setting Dialog Message
//                    alertDialog.setMessage("You Have Already Joined!");

                    // Setting Icon to Dialog
                    alertDialog.setIcon(R.drawable.ic_noun_add_group_782192);

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
                else {
                    participant_list.put(UserIdToAdd);

                    Log.d(TAG, participant_list.toString());

                    ParseQuery<ParseObject> update_query = ParseQuery.getQuery("exerciseEvent");

                    // Retrieve the object by id
                    update_query.getInBackground(workout.getObjectId(), new GetCallback<ParseObject>() {
                        public void done(ParseObject exerciseEvent, ParseException e) {
                            if (e == null) {
                                // Now let's update with some new data

                                exerciseEvent.put("eventParticipants", participant_list);
                                exerciseEvent.saveInBackground();
                                participantAdapter.notifyDataSetChanged();
                                loadParticipants(participant_list);
                                Toast.makeText(getApplicationContext(), "Workout Joined", Toast.LENGTH_SHORT).show();

                            }
                        }
                    });




                }


            }
        });
    }

    public boolean didUserJoin(JSONArray participantListToCheck, String userIdToCheck) {
        for(int i=0;i<participantListToCheck.length();i++){
            try {
//                if(participantListToCheck.get(i).equals(userIdToCheck)){
//                if(userIdToCheck.equals(participantListToCheck.get(i))){

                if(userIdToCheck.equals(participantListToCheck.getString(i))){

                    Log.d(TAG, "participantListToCheck.getString(i):" + participantListToCheck.getString(i));
                    Log.d(TAG, "user ID found in participantListToCheck");
                    return true;
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Log.d(TAG, "unable to check if userID is in participant array");
            }
        }
        return false;
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

    // get comment data

    public void loadComments(Workout workout_event){

        comments.clear();

        final JSONArray comment_ids = new JSONArray();

        final List<Comments> comment_list = new ArrayList<>();

        // get list of comment object ids
        ParseQuery<ParseObject> commentQuery = ParseQuery.getQuery("Comments");

        commentQuery.whereEqualTo("postedTo",workout_event).findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                for(int i =0;i< objects.size();i++) {
                    String commentId = (String) objects.get(i).get("objectId");
                    Log.d(TAG, "comment ID to add to JSONArray" + commentId);
                    comment_ids.put(commentId);
                    Log.d(TAG, "updated list of comment ids" + comment_ids);

                }
            }
        });


//        for(int i=0; i<)






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
                            participantAdapter.notifyDataSetChanged();

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
