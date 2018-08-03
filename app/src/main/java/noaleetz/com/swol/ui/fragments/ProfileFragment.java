package noaleetz.com.swol.ui.fragments;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import noaleetz.com.swol.models.User;
import noaleetz.com.swol.ui.activities.MainActivity;
import noaleetz.com.swol.ui.adapters.ProfileAdapter;
import noaleetz.com.swol.R;
import noaleetz.com.swol.models.Workout;


/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment{


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

    public File photoFile;


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

        tvProfileUsername.setVisibility(View.VISIBLE);

        tvProfileUsername.setText("@" + user.getUsername());
        if (MainActivity.isFacebookUser(user)) {
            String url = "https://graph.facebook.com/" + MainActivity.getFBID(user) + "/picture?type=large";
            Glide.with(view).load(url)
                    .apply(RequestOptions.circleCropTransform()
                            .placeholder(R.drawable.ic_person)
                            .error(R.drawable.ic_person))
                    .into(ivProfileImage);
        } else {
            try {
                Glide.with(view).load(user.getParseFile("profilePicture").getFile())
                        .apply(RequestOptions.circleCropTransform()
                                .placeholder(R.drawable.ic_person)
                                .error(R.drawable.ic_person))
                        .into(ivProfileImage);
            } catch (ParseException e) {
                e.printStackTrace();
            }
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
        String weight = "" + new Random().nextInt(29862) * 5;
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

        // edit the profile
        tvProfileName.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Edit your name.");

                // Set up the input
                final EditText input = new EditText(getActivity());
                // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);

                // Set up the buttons
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String result = input.getText().toString();
                        user.put("name", result);
                        user.saveInBackground();
                        tvProfileName.setText(result);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
                return false;
            }
        });

        tvProfileUsername.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Edit your username.");

                // Set up the input
                final EditText input = new EditText(getActivity());
                // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);

                // Set up the buttons
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String result = input.getText().toString();
                        final User.Query userQuery = new User.Query();
                        if (userQuery.getUsername(result) == null) {
                            user.setUsername(result);
                            user.saveInBackground();
                            tvProfileUsername.setText(result);
                        } else {
                            Toast.makeText(getActivity(), "Username already exists",
                                    Toast.LENGTH_LONG).show();
                        }


                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
                return false;

            }
        });
        ivProfileImage.setOnLongClickListener(new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(View view) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            builder.setTitle("Change Profile Photo");


            // Set up the buttons
            builder.setNeutralButton("Upload", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent i = new Intent(
                            Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                    photoFile = getPhotoFileUri(AddFragment.photoFileName);

                    startActivityForResult(i, AddFragment.RESULT_LOAD_IMAGE);

                }
            });
            builder.setNegativeButton("Capture", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    onLaunchCamera();
                }
            });

            builder.show();
            return false;

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

    public void onLaunchCamera() {
        // create Intent to take a picture and return control to the calling application
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Create a File reference to access to future access
        photoFile = getPhotoFileUri(AddFragment.photoFileName);

        // wrap File object into a content provider
        // required for API >= 24
        // See https://guides.codepath.com/android/Sharing-Content-with-Intents#sharing-files-with-api-24-or-higher
        Uri fileProvider = FileProvider.getUriForFile(getActivity(), "com.swol.fileprovider", photoFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);

        // If you call startActivityForResult() using an intent that no app can handle, your app will crash.
        // So as long as the result is not null, it's safe to use the intent.
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            // Start the image capture intent to take photo
            startActivityForResult(intent, AddFragment.CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // GETTING IMAGE FROM GALLERY
        if (requestCode == AddFragment.RESULT_LOAD_IMAGE && resultCode == AddFragment.RESULT_OK && null != data) {

            Uri selectedImage = data.getData();
            photoFile = getPhotoFileUri(AddFragment.photoFileName);


            try {
                AddFragment.bitmap = MediaStore.Images.Media.getBitmap(getActivity().getApplicationContext().getContentResolver(), selectedImage);
                Glide.with(this).load(AddFragment.bitmap)
                        .apply(RequestOptions.circleCropTransform()
                                .placeholder(R.drawable.ic_person)
                                .error(R.drawable.ic_person))
                        .into(ivProfileImage);
                ivProfileImage.setImageBitmap(AddFragment.bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }

            // GETTING IMAGE FROM CAMERA
        } else if (requestCode == AddFragment.CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == AddFragment.RESULT_OK) {
                // by this point we have the camera photo on disk
                // bitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
                AddFragment.bitmap = AddFragment.rotateBitmapOrientation(photoFile.getPath());
                // RESIZE BITMAP, see section below
                // Load the taken image into a preview
                Glide.with(this).load(AddFragment.bitmap)
                        .apply(RequestOptions.circleCropTransform()
                                .placeholder(R.drawable.ic_person)
                                .error(R.drawable.ic_person))
                        .into(ivProfileImage);
                ivProfileImage.setImageBitmap(AddFragment.bitmap);
            } else { // Result was a failure
                Toast.makeText(getActivity(), "Picture wasn't taken!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Returns the File for a photo stored on disk given the fileName
    public File getPhotoFileUri(String fileName) {
        // Get safe storage directory for photos
        // Use `getExternalFilesDir` on Context to access package-specific directories.
        // This way, we don't need to request external read/write runtime permissions.
        File mediaStorageDir = new File(getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), AddFragment.APP_TAG);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()){
            Log.d(AddFragment.APP_TAG, "failed to create directory");
        }

        // Return the file target for the photo based on filename
        File file = new File(mediaStorageDir.getPath() + File.separator + fileName);

        return file;
    }

}
