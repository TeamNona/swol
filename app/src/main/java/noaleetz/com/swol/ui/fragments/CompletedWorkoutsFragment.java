package noaleetz.com.swol.ui.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import noaleetz.com.swol.R;
import noaleetz.com.swol.models.Workout;
import noaleetz.com.swol.ui.adapters.ProfileAdapter;

public class CompletedWorkoutsFragment extends Fragment {

    @BindView(R.id.rvCompletedWorkouts)
    RecyclerView rvCompletedWorkouts;

    ParseUser user;

    private ProfileAdapter adapter;
    private List<Workout> posts;
    private Unbinder unbinder;

    public CompletedWorkoutsFragment() {
        // Required empty public constructor
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_completed_workouts, container, false);
        unbinder = ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // get the user's profile
        user = ParseUser.getCurrentUser();

        // now the recycler view stuff
        rvCompletedWorkouts.setNestedScrollingEnabled(false);
        posts = new ArrayList<>();
        this.adapter = new ProfileAdapter(posts);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        rvCompletedWorkouts.setLayoutManager(linearLayoutManager);
        rvCompletedWorkouts.setAdapter(adapter);
        loadCompletedWorkouts();

        rvCompletedWorkouts.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView recyclerView, @NonNull MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN || motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    rvCompletedWorkouts.getParent().requestDisallowInterceptTouchEvent(true);
                    rvCompletedWorkouts.setNestedScrollingEnabled(false);
                }
                return false;
            }

            @Override
            public void onTouchEvent(@NonNull RecyclerView recyclerView, @NonNull MotionEvent motionEvent) {

            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean b) {

            }
        });
    }

    public void loadCompletedWorkouts() {


        final Workout.Query postQuery = new Workout.Query();
        postQuery.getTop().contains(user).withUser().completed().orderByLastCreated();

        postQuery.findInBackground(new FindCallback<Workout>() {
            @Override
            public void done(List<Workout> objects, ParseException e) {
                if (e == null) {
                    Log.d("ProfileFragment", Integer.toString(objects.size()));
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
    public void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }

}
