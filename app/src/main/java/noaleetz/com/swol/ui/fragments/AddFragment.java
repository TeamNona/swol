package noaleetz.com.swol.ui.fragments;


import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
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
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import noaleetz.com.swol.R;
import noaleetz.com.swol.models.Workout;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.support.constraint.Constraints.TAG;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;


/**
 * A simple {@link Fragment} subclass.
 */
public class AddFragment extends Fragment implements AdapterView.OnItemSelectedListener {

    // Bind variables
    @BindView(R.id.btnPost)
    Button postButton;
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
    SupportPlaceAutocompleteFragment pafBegin;
    SupportPlaceAutocompleteFragment pafEnd;

    // keep track of who is logged on
    private ParseUser currentUser = ParseUser.getCurrentUser();

    // workout variables
    Date Date;
    ParseGeoPoint postLocation;
    ParseGeoPoint endLocation;

    // initialize time to midnight of current date
    int postYear = Calendar.getInstance().get(Calendar.YEAR);
    int postMonth = Calendar.getInstance().get(Calendar.MONTH);
    int postDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
    int postHour = 23;
    int postMinute = 59;

    FloatingActionButton fab;

    // declare important variables for accessing photo gallery and for accessing camerq
    public static final int RESULT_OK = -1;
    public final static int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1034;
    public static int RESULT_LOAD_IMAGE = 1;
    public static final int RESULT_LOAD_VIDEO = 101;
    public static final int REQUEST_VIDEO_CAPTURE = 100;
    public static Bitmap bitmap;
    public static Object test = null;
    public File photoFile;
    public static String photoFileName = "photo.jpg";
    public static final String APP_TAG = "Swol";

    // variables for permissions
    public static final int MY_PERMISSIONS_REQUEST_CAMERA = 99;
    public static final int MY_PERMISSIONS_REQUEST_GALLERY = 98;

    private NewMapItemListener listener;

    // variables for spinners
    String workoutCategoryPrompt = "Choose a Workout Category";
    String tagsPrompt = "Choose a tag";

    // maps api request stuff
    String modeOfTransit = "walking";
    String polyline;

    private Unbinder unbinder;

    File resizedFile;

    // event location variables
    String locationName;
    String locationAddress;


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
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        String category = (String) adapterView.getItemAtPosition(i);
        String[] distanceCategories = getResources().getStringArray(R.array.distance_categories);
        endLocationShower:
        for (String item : distanceCategories) {
            Log.d("ItemSelector", "item: " + item + "\tcategory: " + category);
            if (item.equals(category)) {
                if (item.equals("bike")) modeOfTransit = "bicycling";
                pafEnd.getView().setVisibility(View.VISIBLE);
                break endLocationShower;
            } else {
                pafEnd.getView().setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        fab = (FloatingActionButton) getActivity().findViewById(R.id.fab);

        // show the post button
        postButton.setVisibility(View.VISIBLE);

        // hide the end places input


        // create Array of workout categories
        final String[] workoutCategories;
        workoutCategories = getResources().getStringArray(R.array.workout_categories);

        // declare Adapter to populate workout category spinner
        ArrayAdapter<CharSequence> categoryAdapter = new ArrayAdapter<CharSequence>(getActivity(),
                android.R.layout.simple_spinner_dropdown_item, workoutCategories) {
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
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        workoutCategory.setAdapter(categoryAdapter);


        // create Array of tag categories
        String[] tagCategories;
        tagCategories = getResources().getStringArray(R.array.tags);

        // declare Adapter to populate tag category spinner
        ArrayAdapter<CharSequence> tagsAdapter = new ArrayAdapter<CharSequence>(getActivity(),
                android.R.layout.simple_spinner_dropdown_item, tagCategories) {
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
        pafBegin = (SupportPlaceAutocompleteFragment)
                getChildFragmentManager().findFragmentById(R.id.pafBegin);

        ((EditText) pafBegin.getView().findViewById(R.id.place_autocomplete_search_input)).setHint("Choose Location");
        ((EditText) pafBegin.getView().findViewById(R.id.place_autocomplete_search_input)).setTextSize(18.0f);

        pafBegin.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                LatLng latlng = place.getLatLng();
                postLocation = new ParseGeoPoint();
                postLocation.setLatitude(latlng.latitude);
                postLocation.setLongitude(latlng.longitude);

                locationAddress = place.getAddress().toString();
                locationName = place.getName().toString();

                Log.i(TAG, "startLocation: " + place.getName());
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });

        // utilize the Google Places API to autocomplete the location for the workout
        pafEnd = (SupportPlaceAutocompleteFragment)
                getChildFragmentManager().findFragmentById(R.id.pafEnd);

        ((EditText) pafEnd.getView().findViewById(R.id.place_autocomplete_search_input)).setHint("Choose Ending Location");
        ((EditText) pafEnd.getView().findViewById(R.id.place_autocomplete_search_input)).setTextSize(18.0f);

        pafEnd.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                LatLng latlng = place.getLatLng();
                endLocation = new ParseGeoPoint();
                endLocation.setLatitude(latlng.latitude);
                endLocation.setLongitude(latlng.longitude);

                Log.i(TAG, "endLocation: " + place.getName());
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });


        // allow user to upload or capture a video
        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                upload.setVisibility(VISIBLE);
//                capture.setVisibility(VISIBLE);
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                //builder.setTitle("Change Profile Photo");


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
            }
        });

        post.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setNegativeButton("Rotate", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        rotateNneka(bitmap, 90);
                        post.setImageBitmap(bitmap);
                    }
                });
                builder.show();
                return true;
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

        /*
        captureVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakeVideoIntent();
            }
        });
        */


        workoutCategory.setOnItemSelectedListener(this);

        // send new workout info to Parse
        postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // on some click or some loading we need to wait for...
                pbPost.setVisibility(ProgressBar.VISIBLE);


                // ensure user enters event name
                final String name = etName.getText().toString();
                if (name.length() == 0) {
                    Toast.makeText(getActivity(), "Your workout must have a name.", Toast.LENGTH_SHORT).show();

                    // hide the progress bar
                    pbPost.setVisibility(ProgressBar.INVISIBLE);
                    return;
                }

                final String category;
                if (workoutCategoryPrompt.equals((String) workoutCategory.getSelectedItem())) {
                    Toast.makeText(getActivity(), "Please categorize your workout.", Toast.LENGTH_SHORT).show();

                    // hide the progress bar
                    pbPost.setVisibility(ProgressBar.INVISIBLE);
                    return;
                } else {
                    category = (String) workoutCategory.getSelectedItem();
                }

                // on some click or some loading we need to wait for...
                pbPost.setVisibility(ProgressBar.VISIBLE);
                postButton.setVisibility(View.GONE);

                // get the final tags
                final JSONArray tags = new JSONArray();
                String help = (String) spTags.getSelectedItem();
                if (tagsPrompt.equals((String) spTags.getSelectedItem())) {
                    Toast.makeText(getActivity(), "Please add a least one tag to your workout.", Toast.LENGTH_SHORT).show();
                    // hide the progress bar
                    pbPost.setVisibility(ProgressBar.INVISIBLE);
                    return;
                } else {
                    tags.put(spTags.getSelectedItem());
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

                    locationAddress = "Facebook Dexter";
                    locationName = "1101 Dexter Ave N, Seattle, WA 98109";
                }
                final ParseGeoPoint location = postLocation;


                // populate participants
                final JSONArray participants = new JSONArray();
                participants.put(currentUser.getObjectId().toString());

                if (bitmap == null) {
                    Drawable drawable = getResources().getDrawable(R.drawable.ic_directions_run_black_24dp);
                    bitmap = convertToBitmap(drawable, 10, 10);
                }

                final ParseFile media;
                //media = new ParseFile(resizedFile);
                media = conversionBitmapParseFile(bitmap);
                media.saveInBackground(new SaveCallback() {
                    public void done(ParseException e) {
                        if (null == e) {
                            Toast.makeText(getActivity(), "Picture post saved", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getActivity(), "Picture post not saved", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                if (endLocation != null && pafEnd.getView().getVisibility() == View.VISIBLE) {
                    RequestQueue queue = Volley.newRequestQueue(getContext());
                    String url = "https://maps.googleapis.com/maps/api/directions/json?origin=" +
                            postLocation.getLatitude() + "," + postLocation.getLongitude() + "&destination=" +
                            endLocation.getLatitude() + "," + endLocation.getLongitude() + "&mode=" + modeOfTransit +
                            "&key=" + getResources().getString(R.string.api_key);

                    Log.d("API Hit", "url: " + url);

                    JsonObjectRequest polylineRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            JSONObject southwest;
                            JSONObject northeast;
                            String boundsString = null;
                            try {
                                polyline = response.getJSONArray("routes").getJSONObject(0).getJSONObject("overview_polyline").getString("points");
                                southwest = response.getJSONArray("routes").getJSONObject(0).getJSONObject("bounds").getJSONObject("southwest");
                                northeast = response.getJSONArray("routes").getJSONObject(0).getJSONObject("bounds").getJSONObject("northeast");
                                boundsString = southwest.getDouble("lat") + "," + southwest.getDouble("lng") + "," + northeast.getDouble("lat") + "," + northeast.getDouble("lng");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            Log.d("PolylineRequest", "Polyline: " + polyline);
                            createNewWorkout(category, name, description, date, location, locationAddress, locationName, media, participants, tags, polyline, boundsString);
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            error.printStackTrace();
                            Log.d("PolylineRequest", "Request failed :(");
                            createNewWorkout(category, name, description, date, location, locationAddress, locationName, media, participants, tags, null, null);
                        }
                    });

                    queue.add(polylineRequest);

                } else {
                    createNewWorkout(category, name, description, date, location, locationAddress, locationName, media, participants, tags, null, null);
                }

            }
        });


    }


    private void createNewWorkout(String category, String name, String description, Date time, ParseGeoPoint location, String address, String addressName, ParseFile media, JSONArray participants, JSONArray tags, String polyline, String boundsString) {


        // create a new event
        final Workout workout = new Workout();

        // populate all of the fields
        workout.setCategory(category);
        workout.setName(name);
        workout.setDescription(description);
        workout.setLocation(location);
        workout.setAddress(address);
        workout.setLocationName(locationName);
        //workout.setMedia(media);
        workout.setParticipants(participants);
        workout.setTime(time);
        workout.setTags(tags);
        workout.setUser(currentUser);
        if (polyline != null) {
            workout.setPolyline(polyline);
            workout.setPolylineBounds(boundsString);
        }

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
                    fab.show();
                    ;

                } else {
                    e.printStackTrace();
                    // show the button on failure
                    postButton.setVisibility(View.VISIBLE);
                    pbPost.setVisibility(View.GONE);
                    Log.e("AddFragment", "Create post was not successful");
                }
            }
        });

    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode,
//                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
//        switch (requestCode) {
//            case MY_PERMISSIONS_REQUEST_GALLERY:
//                // If the permission is granted, get the location,
//                // otherwise, show a Toast
//                if (grantResults.length > 0
//                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    Intent i = new Intent(
//                            Intent.ACTION_PICK,
//                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//
//                    startActivityForResult(i, RESULT_LOAD_IMAGE);
//                } else {
//                    Toast.makeText(getActivity(),
//                            R.string.camera_permission_denied,
//                            Toast.LENGTH_SHORT).show();
//                }
//                break;
//                default:
//                    Toast.makeText(getActivity(),
//                            R.string.camera_permission_denied,
//                            Toast.LENGTH_SHORT).show();
//        }
//    }


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
            String hourofday = "AM";
            if (hour > 12) {
                hour = hour - 12;
                hourofday = "PM";
            } else if (hour == 0) {
                hour = 12;
            }
            if (minute < 10) {
                tvTime.setText(String.valueOf(hour) + ":0" + String.valueOf(minute) + " " + hourofday);
            } else {
                tvTime.setText(String.valueOf(hour) + ":" + String.valueOf(minute) + " " + hourofday);
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


    public void onLaunchCamera() {
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
                bitmap = getResizedBitmap(bitmap, MapFragment.convertDpToPixel(350), MapFragment.convertDpToPixel(350));
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
//                // RESIZE BITMAP, see section below
//                bitmap = BitmapScaler.scaleToFitWidth(original_bitmap, 250);
//                // Configure byte output stream
//                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
//                // Compress the image further
//                bitmap.compress(Bitmap.CompressFormat.JPEG, 40, bytes);
//                // Create a new file for the resized bitmap (`getPhotoFileUri` defined above)
//                resizedFile = getPhotoFileUri(photoFileName + "_resized");
//                try {
//                    resizedFile.createNewFile();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                FileOutputStream fos = null;
//                try {
//                    fos = new FileOutputStream(resizedFile);
//                } catch (FileNotFoundException e) {
//                    e.printStackTrace();
//                }
//                // Write the bytes of the bitmap to file
//                try {
//                    fos.write(bytes.toByteArray());
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                try {
//                    fos.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                // Load the taken image into a preview
                // RESIZE BITMAP, see section below
                // Load the taken image into a preview
                bitmap = getResizedBitmap(bitmap, MapFragment.convertDpToPixel(350), MapFragment.convertDpToPixel(350));
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
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()) {
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
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()) {
            Log.d(APP_TAG, "failed to create directory");
        }

        // Return the file target for the photo based on filename
        File file = new File(mediaStorageDir.getPath() + File.separator + fileName);

        return file;
    }


    public static ParseFile conversionBitmapParseFile(Bitmap imageBitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        try {
            String path = null;
            test = readInFile(path);
        } catch (Exception e) {
            e.printStackTrace();
        }
        byte[] imageByte = byteArrayOutputStream.toByteArray();
        ParseFile parseFile = new ParseFile("image_file.png", imageByte);
        return parseFile;
    }

    private static byte[] readInFile(String path) throws IOException {
        // TODO Auto-generated method stub
        byte[] data = null;
        File file = new File(path);
        InputStream input_stream = new BufferedInputStream(new FileInputStream(
                file));
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        data = new byte[163840]; // 16K
        int bytes_read;
        while ((bytes_read = input_stream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, bytes_read);
        }
        input_stream.close();
        return buffer.toByteArray();

    }


    public Bitmap convertToBitmap(Drawable drawable, int widthPixels, int heightPixels) {
        Bitmap mutableBitmap = Bitmap.createBitmap(widthPixels, heightPixels, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(mutableBitmap);
        drawable.setBounds(0, 0, widthPixels, heightPixels);
        drawable.draw(canvas);

        return mutableBitmap;
    }

    public static Bitmap rotateBitmapOrientation(String photoFilePath) {
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
    @Override
    public void onDestroyView() {
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

    public static Bitmap rotateNneka(Bitmap bitmap, int degree) {
        // Rotate Bitmap
        Matrix matrix = new Matrix();
        matrix.setRotate(180);
        //matrix.setRotate(degree, (float) bitmap.getWidth() / 2, (float) bitmap.getHeight() / 2);
        Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

        return rotatedBitmap;
    }


    public Bitmap rotateBitmap(Bitmap original, float degrees) {
        int width = original.getWidth();
        int height = original.getHeight();

        Matrix matrix = new Matrix();
        matrix.preRotate(degrees);

        Bitmap rotatedBitmap = Bitmap.createBitmap(original, 0, 0, width, height, matrix, true);
        Canvas canvas = new Canvas(rotatedBitmap);
        canvas.drawBitmap(original, 5.0f, 0.0f, null);

        return rotatedBitmap;
    }

    // this interface is so that when a new workout is created, it can send it to the map to update it
    public interface NewMapItemListener {
        public void updateMap();
    }

    public static class BitmapScaler {
        // scale and keep aspect ratio
        public static Bitmap scaleToFitWidth(Bitmap b, int width) {
            float factor = width / (float) b.getWidth();
            return Bitmap.createScaledBitmap(b, width, (int) (b.getHeight() * factor), true);
        }


        // scale and keep aspect ratio
        public static Bitmap scaleToFitHeight(Bitmap b, int height) {
            float factor = height / (float) b.getHeight();
            return Bitmap.createScaledBitmap(b, (int) (b.getWidth() * factor), height, true);
        }


        // scale and keep aspect ratio
        public static Bitmap scaleToFill(Bitmap b, int width, int height) {
            float factorH = height / (float) b.getWidth();
            float factorW = width / (float) b.getWidth();
            float factorToUse = (factorH > factorW) ? factorW : factorH;
            return Bitmap.createScaledBitmap(b, (int) (b.getWidth() * factorToUse),
                    (int) (b.getHeight() * factorToUse), true);
        }


        // scale and don't keep aspect ratio
        public static Bitmap strechToFill(Bitmap b, int width, int height) {
            float factorH = height / (float) b.getHeight();
            float factorW = width / (float) b.getWidth();
            return Bitmap.createScaledBitmap(b, (int) (b.getWidth() * factorW),
                    (int) (b.getHeight() * factorH), true);
        }

    }

        public Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
            int width = bm.getWidth();
            int height = bm.getHeight();
            float scaleWidth = ((float) newWidth) / width;
            float scaleHeight = ((float) newHeight) / height;
            // CREATE A MATRIX FOR THE MANIPULATION
            Matrix matrix = new Matrix();
            // RESIZE THE BIT MAP
            matrix.postScale(scaleWidth, scaleHeight);

            // "RECREATE" THE NEW BITMAP
            Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
            return resizedBitmap;
        }

}
