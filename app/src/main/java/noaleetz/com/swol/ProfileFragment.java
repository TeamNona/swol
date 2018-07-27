package noaleetz.com.swol;


import android.media.Image;
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
import android.widget.ScrollView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import noaleetz.com.swol.models.Workout;


/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {


    @BindView(R.id.ivProfileImage)
    ImageView ivProfileImage;

    @BindView(R.id.tvProfileName)
    TextView tvProfileName;

    @BindView(R.id.tvProfileUsername)
    TextView tvProfileUsername;

    @BindView(R.id.rvMyPosts)
    RecyclerView rvPosts;

    @BindView(R.id.tvMilesRun)
    TextView tvMilesRun;

    @BindView(R.id.tvWeightLifted)
    TextView tvWeightLifted;

    @BindView(R.id.tvAltitude)
    TextView tvAltitude;

    @BindView(R.id.tvDoneWorkouts)
    TextView tvDoneWorkouts;

    @BindView(R.id.svProfile)
    ScrollView svProfile;

    ParseUser user;

    private ProfileAdapter adapter;
    private List<Workout> posts;
    private Unbinder unbinder;


    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        Bundle bundle = getArguments();
        user = bundle.getParcelable("user");

        tvProfileName.setText(user.getString("name"));
        // TODO: fix facebook users
        if (!isFacebookUser(user)) {
            tvProfileUsername.setVisibility(View.VISIBLE);
            tvProfileUsername.setText("@" + user.getUsername());
            try {
                Glide.with(view).load(user.getParseFile("profilePicture").getFile())
                        .apply(RequestOptions.circleCropTransform()
                                .placeholder(R.drawable.ic_person)
                                .error(R.drawable.ic_person))
                        .into(ivProfileImage);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else {
            Glide.with(view).load(R.drawable.ic_person).into(ivProfileImage);
            tvProfileUsername.setVisibility(View.GONE);
        }

        // now the recycler view stuff
        posts = new ArrayList<>();
        adapter = new ProfileAdapter(posts);
        Log.d("ProfileFragment", "Finished setting the adapter");
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        rvPosts.setLayoutManager(linearLayoutManager);
        rvPosts.setAdapter(adapter);
        loadTopPosts();


        //TODO: extract the miles run, for now, just get a random number
        String milesRun = "" + new Random().nextInt(360);
        tvMilesRun.setText(milesRun);

        //TODO: extract the weight, for now, just get a random number
        String weight = "" + new Random().nextInt(29862)*5;
        tvWeightLifted.setText(weight);

        //TODO: extract the altitude run, for now, just get a random number
        String altitude = "" + new Random().nextInt(12498);
        tvAltitude.setText(altitude);


        svProfile.post(new Runnable() {
            @Override
            public void run() {
                svProfile.fullScroll(ScrollView.FOCUS_UP);
            }
        });

    }

    public void loadTopPosts() {


        final Workout.Query postQuery = new Workout.Query();
        postQuery.getTop().contains(user).withUser().orderByLastCreated();

        postQuery.findInBackground(new FindCallback<Workout>() {
            @Override
            public void done(List<Workout> objects, ParseException e) {
                if (e == null) {
                    Log.d("ProfileFragment", Integer.toString(objects.size()));
                    posts.clear();
                    posts.addAll(objects);
                    tvDoneWorkouts.setText("" + posts.size());
                    adapter.notifyDataSetChanged();
                } else {
                    e.printStackTrace();
                }
            }
        });

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }

    public boolean isFacebookUser(ParseUser user) {
        if (user.get("authData") == null) return false;
        JSONObject authData = user.getJSONObject("authData");
        return authData.has("facebook");
    }

}
