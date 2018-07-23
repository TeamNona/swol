package noaleetz.com.swol;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.location.places.ui.SupportPlaceAutocompleteFragment;
import com.google.android.gms.maps.model.LatLng;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONArray;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import noaleetz.com.swol.models.Workout;

import static android.support.constraint.Constraints.TAG;


/**
 * A simple {@link Fragment} subclass.
 */
public class AddFragment extends Fragment{

    // Use butterknife to bind
    @BindView(R.id.btCancel)
    ImageView btCancel;
    @BindView(R.id.btnPost)
    Button postButton;
    @BindView(R.id.btnUpload)
    Button upload;
    @BindView(R.id.btnCapture)
    Button camera;
    @BindView(R.id.btnTime)
    Button addTime;
    @BindView(R.id.btnDate)
    Button addDate;
    @BindView(R.id.etName)
    EditText etName;
    @BindView(R.id.etDescription)
    EditText etDescription;
    @BindView(R.id.tvDate)
    TextView tvDate;
    @BindView(R.id.tvTime)
    TextView tvTime;
    @BindView(R.id.etTags)
    EditText etTags;
    @BindView(R.id.ivMedia)
    ImageView post;


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

    // declare important variables for accessing photo gallery
    private static final int RESULT_OK = -1;
    private static int RESULT_LOAD_IMAGE = 1;
    Bitmap image;
    Bitmap bitmap;
    File photoFile;
    public String photoFileName = "photo.jpg";
    public final String APP_TAG = "Swol";

    private Unbinder unbinder;




    public AddFragment() {
        // Required empty public constructor
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


        // allow user to upload and post a photo for the workout
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(
                        Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                startActivityForResult(i, RESULT_LOAD_IMAGE);
            }
        });


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
                String getTags = etTags.getText().toString();
                String[] gotTags = getTags.split(" ");
                for (int i = 0; i < gotTags.length; i++) {
                    tags.put(gotTags[i]);
                }


                // populate participants
                final JSONArray participants = new JSONArray();
                participants.put(currentUser.getObjectId().toString());


                // get media
                final ParseFile media;
                if (bitmap == null) {
                    Drawable drawable = getResources().getDrawable(R.drawable.ic_directions_run_black_24dp);
                    bitmap = convertToBitmap(drawable, 1000, 1000);
                }
                media = conversionBitmapParseFile(bitmap);

                createNewWorkout(name, description, date, location, media, participants, tags);
                FragmentManager fm = getActivity().getSupportFragmentManager();
                fab.show();
                fm.popBackStackImmediate();

            }
        });

        btCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                FragmentManager fm = getActivity().getSupportFragmentManager();
                fab.show();
                fm.popBackStackImmediate();

            }
        });


    }

    private void createNewWorkout(String name, String description, Date time, ParseGeoPoint location, ParseFile media, JSONArray participants, JSONArray tags) {

        // create a new event
        Workout workout = new Workout();

        // populate all of the fields
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

        }
    }


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

    // When binding a fragment in onCreateView, set the views to null in onDestroyView.
    // ButterKnife returns an Unbinder on the initial binding that has an unbind method to do this automatically.
    @Override public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

}
