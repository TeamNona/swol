package noaleetz.com.swol;


import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

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
    int postYear;
    int postMonth;
    int postDay;
    int postHour;
    int postMinute;


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

        String test = etTags.getText().toString();
        String[] tags = test.split(" ");


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

                createNewWorkout(name, description, date, location, participants, tags);


            }
        });


    }

    private void createNewWorkout(String name, String description, Date time, ParseGeoPoint location, JSONArray participants, JSONArray tags) {

        // create a new event
        Workout workout = new Workout();

        // populate all of the fields
        workout.setName(name);
        workout.setDescription(description);
        workout.setLocation(location);
        // workout.setMedia(media);
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





}
