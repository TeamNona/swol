package noaleetz.com.swol.ui.fragments;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.FileProvider;
import android.support.v4.view.ViewPager;
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
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

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
import noaleetz.com.swol.ui.adapters.CommentAdapter;
import noaleetz.com.swol.ui.adapters.ProfileAdapter;
import noaleetz.com.swol.R;
import noaleetz.com.swol.models.Workout;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;


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
//
//    @BindView(R.id.rvMyPosts)
//    RecyclerView rvPosts;

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

    @BindView(R.id.tbWorkouts)
    TabLayout tbWorkouts;

    @BindView(R.id.vpWorkouts)
    ViewPager vpWorkouts;

//    private TabLayout tabLayout;
//    public ViewPager viewPager;

//    @BindView(R.id.tbWorkout)
//    TableLayout tbWorkout;

    ParseUser user;

    private ProfileAdapter adapter;
    private List<Workout> posts;
    private Unbinder unbinder;

    public File photoFile;

    FloatingActionButton fabAdd;


    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        unbinder = ButterKnife.bind(this, view);

        setupViewPager(vpWorkouts);
        tbWorkouts.setupWithViewPager(vpWorkouts);


        return view;
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getChildFragmentManager());
//        UpcomingWorkoutsFragment upcomingWorkoutsFragment = new UpcomingWorkoutsFragment();
//        CompletedWorkoutsFragment completedWorkoutsFragment = new CompletedWorkoutsFragment();
//        Bundle bundle = new Bundle();
//        bundle.putParcelable("user", user);
//        upcomingWorkoutsFragment.setArguments(bundle);
//        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
//        transaction.replace(R.id.flContent, profileFragment).addToBackStack(null);
//        transaction.commit();
        adapter.addFragment(new CompletedWorkoutsFragment(), "Completed Workouts");
        adapter.addFragment(new UpcomingWorkoutsFragment(), "Upcoming Workouts");
        viewPager.setAdapter(adapter);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }


    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // hide the oldFabAdd
        fabAdd = getActivity().findViewById(R.id.fabAdd);
        fabAdd.hide();

        // get the user's profile
        Bundle bundle = getArguments();
        user = bundle.getParcelable("user");

        // get user's name
        tvProfileName.setText(user.getString("name"));

        // get user's username
        tvProfileUsername.setText("@" + user.getUsername());

        // get the user's profile picture
        if (user.getParseFile("profilePicture") != null) {
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
            Glide.with(view).load(R.drawable.ic_person)
                    .apply(RequestOptions.circleCropTransform()
                            .placeholder(R.drawable.ic_person)
                            .error(R.drawable.ic_person))
                    .into(ivProfileImage);
        }

        // get total number of posts
        posts = new ArrayList<>();
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
                                           } else {
                                               e.printStackTrace();
                                           }
                                       }
                                   });




//        // now the recycler view stuff
//        rvPosts.setNestedScrollingEnabled(false);
//        posts = new ArrayList<>();
//        this.adapter = new ProfileAdapter(posts);
//        Log.d("ProfileFragment", "Finished setting the adapter");
//        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
//        rvPosts.setLayoutManager(linearLayoutManager);
//        rvPosts.setAdapter(adapter);
//        loadTopPosts();


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
                svProfile.scrollTo(0,0);
            }
        });

        // if the current user is on his or her profile, allow user to edit the profile
        ParseUser currentuser = ParseUser.getCurrentUser();
        if (user.getObjectId().equals(currentuser.getObjectId())) {
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

                            if (ActivityCompat.checkSelfPermission(getActivity(), READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                                // Permission is not granted, so request permission
                                ActivityCompat.requestPermissions(getActivity(),
                                        new String[]{READ_EXTERNAL_STORAGE},
                                        AddFragment.MY_PERMISSIONS_REQUEST_GALLERY);
                            } else {
                                // Permission has already been granted
                                Intent i = new Intent(
                                        Intent.ACTION_PICK,
                                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                                startActivityForResult(i, AddFragment.RESULT_LOAD_IMAGE);
                            }

                        }
                    });
                    builder.setNegativeButton("Capture", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (ActivityCompat.checkSelfPermission(getActivity(), CAMERA) != PackageManager.PERMISSION_GRANTED) {

                                // Permission is not granted, so request permission
                                ActivityCompat.requestPermissions(getActivity(),
                                        new String[]{CAMERA},
                                        AddFragment.MY_PERMISSIONS_REQUEST_CAMERA);
                            } else {
                                // Permission has already been granted
                                onLaunchCamera();
                            }
                        }
                    });

                    builder.show();
                    return false;

                }
            });
        }

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
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
                // load bitmap into profile picture view
                AddFragment.bitmap = MediaStore.Images.Media.getBitmap(getActivity().getApplicationContext().getContentResolver(), selectedImage);
                Glide.with(this).load(AddFragment.bitmap)
                        .apply(RequestOptions.circleCropTransform()
                                .placeholder(R.drawable.ic_person)
                                .error(R.drawable.ic_person))
                        .into(ivProfileImage);
                ivProfileImage.setImageBitmap(AddFragment.bitmap);

                // save profile image onto Parse
                final ParseFile parseFile = AddFragment.conversionBitmapParseFile(AddFragment.bitmap);
                parseFile.saveInBackground();
                user.put("profilePicture", parseFile);
                user.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null) {
                            Log.d("ProfileFragment", "Change profile picture successful");
                            Toast.makeText(getActivity(), "Profile picture successfully saved.", Toast.LENGTH_SHORT).show();

                        } else {
                            e.printStackTrace();
                            Log.e("AddFragment", "Change profile picture was not successful");
                            Toast.makeText(getActivity(), "Sorry, profile picture could not be saved.", Toast.LENGTH_SHORT).show();

                        }
                    }
                });

            } catch (IOException e) {
                e.printStackTrace();
            }


            // GETTING IMAGE FROM CAMERA
        } else if (requestCode == AddFragment.CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == AddFragment.RESULT_OK) {
                // load the bitmap into the profile view
                // by this point we have the camera photo on disk
                AddFragment.bitmap = AddFragment.rotateBitmapOrientation(photoFile.getPath());
                // RESIZE BITMAP, see section below
                // Load the taken image into a preview
                Glide.with(this).load(AddFragment.bitmap)
                        .apply(RequestOptions.circleCropTransform()
                                .placeholder(R.drawable.ic_person)
                                .error(R.drawable.ic_person))
                        .into(ivProfileImage);
                ivProfileImage.setImageBitmap(AddFragment.bitmap);
                // save the picture to parse
                final ParseFile parseFile = AddFragment.conversionBitmapParseFile(AddFragment.bitmap);
                parseFile.saveInBackground();
                user.put("profilePicture", parseFile);
                user.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null) {
                            Log.d("ProfileFragment", "Change profile picture successful");
                            Toast.makeText(getActivity(), "Profile picture successfully saved.", Toast.LENGTH_SHORT).show();

                        } else {
                            e.printStackTrace();
                            Log.e("AddFragment", "Change profile picture was not successful");
                            Toast.makeText(getActivity(), "Sorry, profile picture could not be saved.", Toast.LENGTH_SHORT).show();

                        }
                    }
                });

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

