package noaleetz.com.swol;


import android.media.Image;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.parse.ParseException;
import com.parse.ParseUser;

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
        ParseUser user = (ParseUser) bundle.getParcelable("user");

        tvProfileName.setText(user.getString("name"));
        tvProfileUsername.setText("@" + user.getUsername());
        // TODO: fix facebook users
        if (!MainActivity.isFacebookUser(user)) {
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
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}
