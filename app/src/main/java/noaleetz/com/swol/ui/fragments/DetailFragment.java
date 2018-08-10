package noaleetz.com.swol.ui.fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import noaleetz.com.swol.ui.adapters.CommentAdapter;
import noaleetz.com.swol.ui.adapters.ParticipantAdapter;
import noaleetz.com.swol.R;
import noaleetz.com.swol.models.Comments;
import noaleetz.com.swol.models.Workout;


/**
 * A simple {@link Fragment} subclass.
 */
public class DetailFragment extends Fragment {

    FloatingActionButton fab;


    private static final String TAG = "TAGDetailFragment";
    @BindView(R.id.tvWorkoutTitle)
    TextView tvWorkoutTitle;
    @BindView(R.id.tvBeginsIn)
    TextView tvBeginsIn;
    @BindView(R.id.tvDetailLocation)
    TextView tvDetailLocation;
    @BindView(R.id.ivDetailAvatar)
    ImageView ivAvatar;
    @BindView(R.id.tvUsername)
    TextView tvUsername;
    @BindView(R.id.ivDetailImage)
    ImageView ivImage;
    @BindView(R.id.rvParticipants)
    RecyclerView rvParticipants;
    @BindView(R.id.rvComments)
    RecyclerView rvComments;
    @BindView(R.id.tvDescription)
    TextView tvDescription;
    @BindView(R.id.cvJoin)
    CardView cvJoin;
    @BindView(R.id.tvJoin)
    TextView tvJoin;
    // Add Comment holders
    @BindView(R.id.tvCommentUsername)
    TextView tvCommentUsername;
    @BindView(R.id.ivAddCommentAvatar)
    ImageView ivAddCommentAvatar;
    @BindView(R.id.btAddComment)
    ImageView btAddComment;
    @BindView(R.id.tvComment)
    EditText tvComment;

    Workout workout;

    String url;
    String url_post;


    private ParticipantAdapter participantAdapter;
    private List<ParseUser> participants;

    private CommentAdapter commentAdapter;
    private List<Comments> comments;

    private Unbinder unbinder;

    public JSONArray participant_list;
    public JSONArray comment_list;

    private GoToMapListener listener;
    private Context context;


    public DetailFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment


        View view = inflater.inflate(R.layout.fragment_detail, container, false);
        unbinder = ButterKnife.bind(this, view);
        context = getContext();
        if (context instanceof GoToMapListener) {
            listener = (GoToMapListener) context;
        } else {
            throw new ClassCastException(context.toString() + " must implement DetailFragment.GoToMapListener");
        }


        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fab = (FloatingActionButton) getActivity().findViewById(R.id.fabAdd);
        fab.hide();


        final RoundedCornersTransformation roundedCornersTransformation = new RoundedCornersTransformation(30, 30);
        final RequestOptions requestOptions = RequestOptions.bitmapTransform(roundedCornersTransformation);

        Bundle bundle = getArguments();
        workout = (Workout) bundle.getParcelable("workout");

        tvWorkoutTitle.setText(workout.getName());
        tvBeginsIn.setText(workout.getTimeUntil());
        try {
            tvUsername.setText("@" + workout.getUser().fetchIfNeeded().getUsername());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        tvDescription.setText(workout.getDescription());

        participant_list = new JSONArray();
        comment_list = new JSONArray();

        participant_list = workout.getParticipants();

        Log.d(TAG, "Tag 1" + workout.get("eventParticipants").toString());

        // Load user avatar
        try {
            url = workout.getUser()
                    .fetchIfNeeded()
                    .getParseFile("profilePicture")
                    .getUrl();

            Glide.with(DetailFragment.this)
                    .load(url)
                    .apply(requestOptions)
                    .into(ivAvatar);

            Glide.with(DetailFragment.this)
                    .load(url)
                    .into(ivAddCommentAvatar);
        } catch (ParseException e) {
            e.printStackTrace();
            Log.d(TAG, "AvatarImage did not load");
        } catch (NullPointerException e) {
            Log.d(TAG, "there is no profile picture in the parse server, using the temp one");
        }

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
                .into(ivImage);

        tvCommentUsername.setText(ParseUser.getCurrentUser().getUsername());

        // get Likes Count from Parse

//        getLikesCount(workout);


        // set up and populate data for  participant adapter

        participants = new ArrayList<>();

        this.participantAdapter = new ParticipantAdapter(participants);
        Log.d(TAG, "finished setting up participant adapter");
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        rvParticipants.setLayoutManager(linearLayoutManager);
        rvParticipants.setAdapter(participantAdapter);

        loadParticipants(workout.getParticipants());
        loadComments(workout);

        // set up comment adapter

        comments = new ArrayList<>();
        this.commentAdapter = new CommentAdapter(comments);
        Log.d(TAG, "finished setting up comment adapter");
        LinearLayoutManager commentLinearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, true);
        rvComments.setLayoutManager(commentLinearLayoutManager);
        rvComments.setAdapter(commentAdapter);


        if (didUserJoin(participant_list, ParseUser.getCurrentUser().getObjectId())) {
            tvJoin.setText("Leave Workout");
            cvJoin.setCardBackgroundColor(21);
        } else {
            tvJoin.setText("Join Workout");
            cvJoin.setCardBackgroundColor(121);
        }


        btAddComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addComment();
            }
        });

        // if the current user created the workout then they can't leave
        if (workout.getUser().getObjectId().equals(ParseUser.getCurrentUser().getObjectId())) cvJoin.setVisibility(View.GONE);
        else cvJoin.setVisibility(View.VISIBLE);

        cvJoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // user clicks join workout to add themselves to participant list

                Log.d(TAG, "reset participant list" + participant_list.toString());
                String UserIdToAdd = ParseUser.getCurrentUser().getObjectId().toString();

                // check if user has already joined
                if (didUserJoin(participant_list, UserIdToAdd)) {
                    Log.d(TAG, "is user there?" + String.valueOf(participant_list.equals(UserIdToAdd)));

                    ParseQuery<ParseObject> update_query = ParseQuery.getQuery("exerciseEvent");

                    // find self
                    int self = 0;
                    for (int i = 0; i < participant_list.length(); i++) {
                        try {
                            if (participant_list.get(i).equals(ParseUser.getCurrentUser().getObjectId())) self = i;
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    if (self != 0) participant_list.remove(self);

                            // Retrieve the object by id
                            update_query.getInBackground(workout.getObjectId(), new GetCallback<ParseObject>() {
                                public void done(ParseObject exerciseEvent, ParseException e) {
                                    if (e == null) {
                                        // Now let's update with some new data

                                        exerciseEvent.put("eventParticipants", participant_list);
                                        exerciseEvent.saveInBackground();
                                        participantAdapter.notifyDataSetChanged();
                                        loadParticipants(participant_list);
                                        tvJoin.setText("Join Workout");

                                    }
                                }
                            });

                } else {
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


                                tvJoin.setText("Leave Workout");

                            }
                        }
                    });


                }


            }
        });

        tvDetailLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onLinkClicked(workout);
            }
        });
    }

    public boolean didUserJoin(JSONArray participantListToCheck, String userIdToCheck) {
        for (int i = 0; i < participantListToCheck.length(); i++) {
            try {
//                if(participantListToCheck.get(i).equals(userIdToCheck)){
//                if(userIdToCheck.equals(participantListToCheck.get(i))){

                if (userIdToCheck.equals(participantListToCheck.getString(i))) {

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

//    public void getLikesCount(Workout workout_event) {
//        String workoutId = workout_event.getObjectId();
//
//        ParseQuery<ParseObject> query = ParseQuery.getQuery("Likes");
//
//        query.whereEqualTo("likedPost", workout_event).countInBackground(new CountCallback() {
//            @Override
//            public void done(int count, ParseException e) {
//                if (e == null) {
//                    // no error
//                    Log.d(TAG, "like count:" + String.valueOf(count));
//                    tvLikesCt.setText(String.valueOf(count));
//
//                } else {
//                    // something went wrong
//                    Log.d(TAG, "unable to find like count");
//                    tvLikesCt.setText(String.valueOf(0));
//                }
//            }
//        });
//    }

    // get comment data

    public void loadComments(Workout workout_event) {


        final JSONArray comment_ids = new JSONArray();

//         get list of comment object ids
        Comments.Query commentQuery = new Comments.Query();

        commentQuery.getTop().whereEqualTo("postedTo", workout_event).findInBackground(new FindCallback<Comments>() {
            @Override
            public void done(List<Comments> objects, ParseException e) {
                for (int i = 0; i < objects.size(); i++) {
                    Comments comments1 = objects.get(i);
                    String commentId = comments1.getObjectId();
                    Log.d(TAG, "comment ID to add to JSONArray" + commentId);
                    comment_ids.put(commentId);
                    Log.d(TAG, "updated list of comment ids" + comment_ids);

                }
                Log.d(TAG, "full JSON Array of comment IDs" + comment_ids);
//                comments.clear();

                for (int i = 0; i < comment_ids.length(); i++) {

                    try {
                        Comments.Query getCommentQuery = new Comments.Query();

                        getCommentQuery.getTop().whereEqualTo("objectId", comment_ids.get(i));

                        getCommentQuery.findInBackground(new FindCallback<Comments>() {
                            public void done(List<Comments> object, ParseException e) {
                                if (e == null) {
//                                    Comments comment = (Comments) object.get(0);

                                    comments.add(object.get(0));
                                    commentAdapter.notifyDataSetChanged();
//                                    Log.d(TAG, "comment ID:" + comment.get("objectID") + "," + "comment text" + comment.get("description"));

                                } else {
                                    // something went wrong
                                    Log.d(TAG, "comment object not added");
                                }
                            }

                        });


                    } catch (JSONException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });


//        for(int i=0; i<)
    }

    public void addComment() {
        final String commentText = tvComment.getText().toString();
        Comments newComment = new Comments();
        newComment.setPostedBy(ParseUser.getCurrentUser());
        newComment.setPostedTo(workout);
        newComment.setComment(commentText);
        newComment.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    comments.clear();
                    loadComments(workout);
                    rvComments.scrollToPosition(comments.size() - 1);
                    tvComment.setText("");
                } else {
                    Log.d(TAG, "there was an error saving the comment to parse");
                    e.printStackTrace();
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
                query.whereEqualTo("objectId", user_ids.get(i));


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

    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    public interface GoToMapListener {
        public void onLinkClicked(Workout workout);
    }


}
