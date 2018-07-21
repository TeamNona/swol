package noaleetz.com.swol;


import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.FileProvider;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;

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

import noaleetz.com.swol.models.Workout;

import static android.support.constraint.Constraints.TAG;


/**
 * A simple {@link Fragment} subclass.
 */
public class AddFragment extends Fragment{


    
    // keep track of who is logged on
    private ParseUser currentUser = ParseUser.getCurrentUser();

    // declare fields
    Button addTime;
    Button addDate;
    Button camera;
    Button upload;
    Button postButton;
    EditText etName;
    EditText etDescription;
    EditText etTags;
    TextView tvDate;
    TextView tvTime;
    Date Date;
    ParseGeoPoint postLocation;
    ParseFile media;
    ImageView post;
    int postYear;
    int postMonth;
    int postDay;
    int postHour;
    int postMinute;

    // media
    private static final int RESULT_OK = -1;
    Bitmap image;
    File photoFile;
    public String photoFileName = "photo.jpg";
    public final String APP_TAG = "Swol";
    private static int RESULT_LOAD_IMAGE = 1;
    Bitmap bitmap;


    public AddFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        addTime = view.findViewById(R.id.btnTime);

        addTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTimePicker();
            }
        });

        addDate = view.findViewById(R.id.btnDate);

        addDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePicker();
            }
        });

        etName = view.findViewById(R.id.etName);
        etDescription = view.findViewById(R.id.etDescription);
        tvDate = view.findViewById(R.id.tvDate);
        tvTime = view.findViewById(R.id.tvTime);
        etTags = view.findViewById(R.id.etTags);


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

        post = view.findViewById(R.id.ivMedia);
        upload = view.findViewById(R.id.btnUpload);
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(
                        Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                startActivityForResult(i, RESULT_LOAD_IMAGE);
            }
        });


        postButton = view.findViewById(R.id.btnPost);
        postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // get the final choice of date
                Calendar calendar = Calendar.getInstance();
                calendar.set(postYear, postMonth, postDay, postHour, postMinute);
                Date = calendar.getTime();



                final String name = etName.getText().toString();
                final String description = etDescription.getText().toString();
                final Date date = Date;
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
                // Locate the image in res >
                //Bitmap bitmap = BitmapFactory.decodeFile("picturePath");
                // Convert it to byte
                //ByteArrayOutputStream stream = new ByteArrayOutputStream();
                // Compress image to lower quality scale 1 - 100
                //bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);

                /*Object image = null;
                try {
                    String path = null;
                    image = readInFile(path);
                } catch (Exception e) {
                    e.printStackTrace();
                } */

                // Create the ParseFile
                //ParseFile file = new ParseFile("picturePath", (byte[]) image);
                // Upload the image into Parse Cloud
                //file.saveInBackground();

                // get media
                final File file =  new File(String.valueOf(photoFile));
                final ParseFile media = new ParseFile(file);
                final ParseFile lastshot = conversionBitmapParseFile(bitmap);

                media.saveInBackground();

                createNewWorkout(name, description, date, location, lastshot, participants, tags);

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
                    Log.e("no", "no");
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

        //GETTING IMAGE FROM GALLERY
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            photoFile = getPhotoFileUri(photoFileName);

            try {
                //..bitmap = BitmapFactory.decodeStream(context.getContentResolver().openInputStream(selectedImage));
                bitmap = MediaStore.Images.Media.getBitmap(getActivity().getApplicationContext().getContentResolver(), selectedImage);
                post.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
            /*Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };

            Cursor cursor = getActivity().getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            post.setImageBitmap(BitmapFactory.decodeFile(picturePath)); */

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

    private byte[] readInFile(String path) throws IOException {
        // TODO Auto-generated method stub
        byte[] data = null;
        File file = new File(path);
        InputStream input_stream = new BufferedInputStream(new FileInputStream(
                file));
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        data = new byte[16384]; // 16K
        int bytes_read;
        while ((bytes_read = input_stream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, bytes_read);
        }
        input_stream.close();
        return buffer.toByteArray();

    }

    public ParseFile conversionBitmapParseFile(Bitmap imageBitmap){
        ByteArrayOutputStream byteArrayOutputStream=new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.PNG,100,byteArrayOutputStream);
        byte[] imageByte = byteArrayOutputStream.toByteArray();
        ParseFile parseFile = new ParseFile("image_file.png",imageByte);
        return parseFile;
    }



}
