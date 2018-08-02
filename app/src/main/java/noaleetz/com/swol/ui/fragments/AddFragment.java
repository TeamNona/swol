package noaleetz.com.swol.ui.fragments;


import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.location.places.ui.SupportPlaceAutocompleteFragment;
import com.google.android.gms.maps.model.LatLng;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONArray;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import noaleetz.com.swol.R;
import noaleetz.com.swol.models.Workout;

import static android.support.constraint.Constraints.TAG;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;


/**
 * A simple {@link Fragment} subclass.
 */
public class AddFragment extends Fragment{

    // Use butterknife to bind
    @BindView(R.id.btnPost)
    Button postButton;
    @BindView(R.id.btnUpload)
    Button upload;
    @BindView(R.id.btnCapture)
    Button capture;
    /*@BindView(R.id.btnUploadVideo)
    Button uploadVideo;
    @BindView(R.id.btnCaptureVideo)
    Button captureVideo; */
    @BindView(R.id.ibTime)
    ImageButton addTime;
    @BindView(R.id.ibDate)
    ImageButton addDate;
    @BindView(R.id.etName)
    EditText etName;
    @BindView(R.id.etDescription)
    EditText etDescription;
    @BindView(R.id.tvDate)
    TextView tvDate;
    @BindView(R.id.tvTime)
    TextView tvTime;
    @BindView(R.id.spTags)
    Spinner spTags;
    @BindView(R.id.ivMedia)
    ImageView post;
    @BindView(R.id.spCategory)
    Spinner workoutCategory;
    @BindView(R.id.pbLoading)
    ProgressBar pbPost;




    // declare other variables
    Date Date;
    ParseGeoPoint postLocation;
    // initialize time to midnight of current date
    int postYear = Calendar.getInstance().get(Calendar.YEAR);
    int postMonth = Calendar.getInstance().get(Calendar.MONTH);
    int postDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
    int postHour = 23;
    int postMinute = 59;
    FloatingActionButton fab;
    
    // keep track of who is logged on
    private ParseUser currentUser = ParseUser.getCurrentUser();

    // declare important variables for accessing photo gallery and for accessing camerq
    private static final int RESULT_OK = -1;
    public final static int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1034;
    private static int RESULT_LOAD_IMAGE = 1;
    private static final int RESULT_LOAD_VIDEO = 101;
    static final int REQUEST_VIDEO_CAPTURE = 100;
    Bitmap image;
    Bitmap bitmap;
    File photoFile;
    File videoFile;
    public String photoFileName = "photo.jpg";
    public final String APP_TAG = "Swol";

    private NewMapItemListener listener;

    private Unbinder unbinder;

    // declare variables for spinners
    String workoutCategoryPrompt = "Choose a Workout Category";
    String tagsPrompt = "Choose up to 5 tags";




    public AddFragment() {
        // Required empty public constructor
    }

    public static AddFragment create(ParseGeoPoint point) {
        AddFragment fragment = new AddFragment();
        Bundle args = new Bundle();
        args.putParcelable("geoLoc", point);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof NewMapItemListener) {
            listener = (NewMapItemListener) context;
        } else {
            throw new ClassCastException(context.toString() + " must implement AddFragment.NewMapItemListener");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            postLocation = getArguments().getParcelable("geoLoc");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add, container, false);
        unbinder = ButterKnife.bind(this, view);

        // Inflate the layout for this fragment
        return view;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        fab = (FloatingActionButton) getActivity().findViewById(R.id.fab);

        // create Array of workout categories
        final String[] workoutCategories;
        workoutCategories = getResources().getStringArray(R.array.workout_categories);

        ArrayAdapter<CharSequence> categoryAdapter = new ArrayAdapter<CharSequence>(getActivity(),
                android.R.layout.simple_spinner_dropdown_item, workoutCategories ) {
            // Disable click item
            @Override
            public boolean isEnabled(int position) {
                // TODO Auto-generated method stub
                if (position == 0) {
                    return false;
                }
                return true;
            }

            // Change color item
            @Override
            public View getDropDownView(int position, View convertView,
                                        ViewGroup parent) {
                // TODO Auto-generated method stub
                View mView = super.getDropDownView(position, convertView, parent);
                TextView mTextView = (TextView) mView;
                if (position == 0) {
                    mTextView.setTextColor(Color.GRAY);
                } else {
                    mTextView.setTextColor(Color.BLACK);
                }
                return mView;
            }

        };



//        // Create an ArrayAdapter using the string array and a default spinner layout
//        ArrayAdapter<CharSequence> categoryAdapter = ArrayAdapter.createFromResource(getActivity(),
//                R.array.workout_categories, android.R.layout.simple_spinner_item) ;
        // Specify the layout to use when the list of choices appears
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) ;
        // Apply the adapter to the spinner
        workoutCategory.setAdapter(categoryAdapter);

        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                upload.setVisibility(VISIBLE);
                capture.setVisibility(VISIBLE);
            }
        });




        // set on click listener for user to add time
        addTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTimePicker();
            }
        });


        // set on click listener for user to add date
        addDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePicker();
            }
        });


        // utilize the Google Places API to autocomplete the location for the workout
        SupportPlaceAutocompleteFragment autocompleteFragment = (SupportPlaceAutocompleteFragment)
                getChildFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        ((EditText)autocompleteFragment.getView().findViewById(R.id.place_autocomplete_search_input)).setHint("Choose Location");
        ((EditText)autocompleteFragment.getView().findViewById(R.id.place_autocomplete_search_input)).setTextSize(18.0f);

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                LatLng latlng = place.getLatLng();
                postLocation = new ParseGeoPoint();
                postLocation.setLatitude(latlng.latitude);
                postLocation.setLongitude(latlng.longitude);

                Log.i(TAG, "Place: " + place.getName());
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });

        // create Array of workout categories
        String[] tagCategories;
        tagCategories = getResources().getStringArray(R.array.tags);

        ArrayAdapter<CharSequence> tagsAdapter = new ArrayAdapter<CharSequence>(getActivity(),
                android.R.layout.simple_spinner_dropdown_item, tagCategories ) {
            // Disable click item
            @Override
            public boolean isEnabled(int position) {
                // TODO Auto-generated method stub
                if (position == 0) {
                    return false;
                }
                return true;
            }

            // Change color item
            @Override
            public View getDropDownView(int position, View convertView,
                                        ViewGroup parent) {
                // TODO Auto-generated method stub
                View mView = super.getDropDownView(position, convertView, parent);
                TextView mTextView = (TextView) mView;
                if (position == 0) {
                    mTextView.setTextColor(Color.GRAY);
                } else {
                    mTextView.setTextColor(Color.BLACK);
                }
                return mView;
            }

        };

        // Specify the layout to use when the list of choices appears
        tagsAdapter.setDropDownViewResource(android.R.layout.simple_list_item_multiple_choice);
        // Apply the adapter to the spinner
        spTags.setAdapter(tagsAdapter);


                // allow user to upload and post a photo for the workout
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                upload.setVisibility(INVISIBLE);
                capture.setVisibility(INVISIBLE);

                Intent i = new Intent(
                        Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                startActivityForResult(i, RESULT_LOAD_IMAGE);
            }
        });

        /*
        // allow user to upload a video for the workout
        uploadVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(
                        Intent.ACTION_PICK,
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI);

                startActivityForResult(i, RESULT_LOAD_VIDEO);
            }
        });
        */

        // allow user to take a photo for the workout directly from the app
        capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                upload.setVisibility(INVISIBLE);
                capture.setVisibility(INVISIBLE);
                onLaunchCamera(view);
            }
        });

        /*
        captureVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakeVideoIntent();
            }
        });
        */

        // send new workout info to Parse
        postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                // ensure user enters event name
                final String name = etName.getText().toString();
                if (name.length() == 0) {
                    Toast.makeText(getActivity(), "Your workout must have a name.", Toast.LENGTH_SHORT).show();
                    return;
                }

                final String category;
                if (workoutCategoryPrompt.equals((String) workoutCategory.getSelectedItem())) {
                    Toast.makeText(getActivity(), "Please categorize your workout", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    category = (String) workoutCategory.getSelectedItem();
                }

                // on some click or some loading we need to wait for...
                pbPost.setVisibility(ProgressBar.VISIBLE);


                final String description = etDescription.getText().toString();


                // get the final choice of date, if no date or time is chosen, default to current instance
                Calendar calendar = Calendar.getInstance();
                calendar.set(postYear, postMonth, postDay, postHour, postMinute);
                Date = calendar.getTime();
                final Date date = Date;


                // get final location, with default location as current location
                if (postLocation == null) {
                    postLocation = currentUser.getParseGeoPoint("currentLocation");
                }
                final ParseGeoPoint location = postLocation;


                // get the final tags
                final JSONArray tags = new JSONArray();
                if (tagsPrompt.equals((String) spTags.getSelectedItem())) {
                    Toast.makeText(getActivity(), "Please add a least one tag to your workout.", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    tags.put(spTags.getSelectedItem());
                }



                // populate participants
                final JSONArray participants = new JSONArray();
                participants.put(currentUser.getObjectId().toString());

                if (bitmap == null) {
                    Drawable drawable = getResources().getDrawable(R.drawable.ic_directions_run_black_24dp);
                    bitmap = convertToBitmap(drawable, 1000, 1000);
                }

                final ParseFile media;
                media = conversionBitmapParseFile(bitmap);


                createNewWorkout(category, name, description, date, location, media, participants, tags);
//                FragmentManager fm = getActivity().getSupportFragmentManager();
//                fab.show();
//                fm.popBackStackImmediate();

                // run a background job and once complete
                //pbPost.setVisibility(ProgressBar.INVISIBLE);


            }
        });


    }


    private void createNewWorkout(String category, String name, String description, Date time, ParseGeoPoint location, ParseFile media, JSONArray participants, JSONArray tags) {


        // create a new event
        final Workout workout = new Workout();

        // populate all of the fields
        workout.setCategory(category);
        workout.setName(name);
        workout.setDescription(description);
        workout.setLocation(location);
        workout.setMedia(media);
        workout.setParticipants(participants);
        workout.setTime(time);
        workout.setTags(tags);
        workout.setUser(currentUser);

        workout.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Log.d("AddFragment", "Create post successful");

                    // if the user made the post from the map fragment, send the workout back to the map
                    FragmentManager fm = getActivity().getSupportFragmentManager();
                    fab.show();
                    if (fm.getBackStackEntryAt(0).getName() == "map") {
                        fm.popBackStackImmediate();
                        listener.updateMap();
                    } else fm.popBackStackImmediate();

                } else {
                    e.printStackTrace();
                    Log.e("AddFragment", "Create post was not successful");
                }
            }
        });

    }


    private void showTimePicker() {
        TimePickerFragment time = new TimePickerFragment();

        // set current time
        Calendar calender = Calendar.getInstance();
        Bundle args = new Bundle();
        args.putInt("hour", calender.get(Calendar.HOUR_OF_DAY));
        args.putInt("minute", calender.get(Calendar.MINUTE));
        time.setArguments(args);

        time.setCallBack(ontime);
        time.show(getChildFragmentManager(), "Time Picker");
    }

    TimePickerDialog.OnTimeSetListener ontime = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker timePicker, int hour, int minute) {
            postHour = hour;
            postMinute = minute;
            if (minute < 10) {
                tvTime.setText(String.valueOf(hour) + ":0" + String.valueOf(minute));
            } else {
                tvTime.setText(String.valueOf(hour) + ":" + String.valueOf(minute));
            }


        }
    };

    private void showDatePicker() {
        DatePickerFragment date = new DatePickerFragment();

        // set date of today
        Calendar calender = Calendar.getInstance();
        Bundle args = new Bundle();
        args.putInt("year", calender.get(Calendar.YEAR));
        args.putInt("month", calender.get(Calendar.MONTH));
        args.putInt("day", calender.get(Calendar.DAY_OF_MONTH));
        date.setArguments(args);

        date.setCallBack(ondate);
        date.show(getChildFragmentManager(), "Date Picker");
    }

    DatePickerDialog.OnDateSetListener ondate = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            postYear = year;
            postMonth = monthOfYear;
            postDay = dayOfMonth;
            tvDate.setText(String.valueOf(monthOfYear + 1) + "/" + String.valueOf(dayOfMonth) + "/" + String.valueOf(year));
        }
    };


    public void onLaunchCamera(View view) {
        // create Intent to take a picture and return control to the calling application
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Create a File reference to access to future access
        photoFile = getPhotoFileUri(photoFileName);

        // wrap File object into a content provider
        // required for API >= 24
        // See https://guides.codepath.com/android/Sharing-Content-with-Intents#sharing-files-with-api-24-or-higher
        Uri fileProvider = FileProvider.getUriForFile(getActivity(), "com.swol.fileprovider", photoFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);

        // If you call startActivityForResult() using an intent that no app can handle, your app will crash.
        // So as long as the result is not null, it's safe to use the intent.
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            // Start the image capture intent to take photo
            startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        }
    }

   /* private void dispatchTakeVideoIntent() {
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (takeVideoIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);
        }
    } */

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // GETTING IMAGE FROM GALLERY
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {

            Uri selectedImage = data.getData();
            photoFile = getPhotoFileUri(photoFileName);


            try {
                bitmap = MediaStore.Images.Media.getBitmap(getActivity().getApplicationContext().getContentResolver(), selectedImage);
                post.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }

            // GETTING IMAGE FROM CAMERA
        } else if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // by this point we have the camera photo on disk
                // bitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
                bitmap = rotateBitmapOrientation(photoFile.getPath());
                // RESIZE BITMAP, see section below
                // Load the taken image into a preview
                post.setImageBitmap(bitmap);
            } else { // Result was a failure
                Toast.makeText(getActivity(), "Picture wasn't taken!", Toast.LENGTH_SHORT).show();
            }
        }
    }
//
//        } else if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK) {
//            Uri videoUri = data.getData();
//            vvPost.setVideoURI(videoUri);
//            vvPost.start();
//            videoFile = new File(videoUri.getPath());
//            // GETTING VIDEO FROM GALLERY
//        } else if (requestCode == RESULT_LOAD_VIDEO && resultCode == RESULT_OK) {
//            Uri videoUri = data.getData();
//            vvPost.setVideoURI(videoUri);
//
//            videoFile = getVideoFileUri(photoFileName);
//
//            post.setVisibility(View.INVISIBLE);
//            vvPost.setVisibility(View.VISIBLE);
//
//            vvPost.start();
//
//        }
//    }


    // Returns the File for a photo stored on disk given the fileName
    public File getPhotoFileUri(String fileName) {
        // Get safe storage directory for photos
        // Use `getExternalFilesDir` on Context to access package-specific directories.
        // This way, we don't need to request external read/write runtime permissions.
        File mediaStorageDir = new File(getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), APP_TAG);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()){
            Log.d(APP_TAG, "failed to create directory");
        }

        // Return the file target for the photo based on filename
        File file = new File(mediaStorageDir.getPath() + File.separator + fileName);

        return file;
    }

    // Returns the File for a photo stored on disk given the fileName
    public File getVideoFileUri(String fileName) {
        // Get safe storage directory for photos
        // Use `getExternalFilesDir` on Context to access package-specific directories.
        // This way, we don't need to request external read/write runtime permissions.
        File mediaStorageDir = new File(getContext().getExternalFilesDir(Environment.DIRECTORY_DCIM), APP_TAG);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()){
            Log.d(APP_TAG, "failed to create directory");
        }

        // Return the file target for the photo based on filename
        File file = new File(mediaStorageDir.getPath() + File.separator + fileName);

        return file;
    }


    public ParseFile conversionBitmapParseFile(Bitmap imageBitmap){
        ByteArrayOutputStream byteArrayOutputStream=new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.PNG,100,byteArrayOutputStream);
        byte[] imageByte = byteArrayOutputStream.toByteArray();
        ParseFile parseFile = new ParseFile("image_file.png",imageByte);
        return parseFile;
    }


    public Bitmap convertToBitmap(Drawable drawable, int widthPixels, int heightPixels) {
        Bitmap mutableBitmap = Bitmap.createBitmap(widthPixels, heightPixels, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(mutableBitmap);
        drawable.setBounds(0, 0, widthPixels, heightPixels);
        drawable.draw(canvas);

        return mutableBitmap;
    }

    public Bitmap rotateBitmapOrientation(String photoFilePath) {
        // Create and configure BitmapFactory
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(photoFilePath, bounds);
        BitmapFactory.Options opts = new BitmapFactory.Options();
        Bitmap bm = BitmapFactory.decodeFile(photoFilePath, opts);
        // Read EXIF Data
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(photoFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String orientString = exif.getAttribute(ExifInterface.TAG_ORIENTATION);
        int orientation = orientString != null ? Integer.parseInt(orientString) : ExifInterface.ORIENTATION_NORMAL;
        int rotationAngle = 0;
        if (orientation == ExifInterface.ORIENTATION_ROTATE_90) rotationAngle = 90;
        if (orientation == ExifInterface.ORIENTATION_ROTATE_180) rotationAngle = 180;
        if (orientation == ExifInterface.ORIENTATION_ROTATE_270) rotationAngle = 270;
        // Rotate Bitmap
        Matrix matrix = new Matrix();
        matrix.setRotate(rotationAngle, (float) bm.getWidth() / 2, (float) bm.getHeight() / 2);
        Bitmap rotatedBitmap = Bitmap.createBitmap(bm, 0, 0, bounds.outWidth, bounds.outHeight, matrix, true);
        // Return result
        return rotatedBitmap;
    }


    // When binding a fragment in onCreateView, set the views to null in onDestroyView.
    // ButterKnife returns an Unbinder on the initial binding that has an unbind method to do this automatically.
    @Override public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    public static Bitmap rotate(Bitmap bitmap, int degree) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        Matrix mtx = new Matrix();
        mtx.postRotate(degree);

        return Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, true);
    }

    // this interface is so that when a new workout is created, it can send it to the map to update it
    public interface NewMapItemListener {
        public void updateMap();
    }

}
