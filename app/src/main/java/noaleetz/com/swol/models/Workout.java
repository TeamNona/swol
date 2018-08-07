package noaleetz.com.swol.models;

import android.support.annotation.NonNull;
import android.text.format.DateUtils;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.android.clustering.ClusterItem;
import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.parceler.Parcel;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

@ParseClassName("exerciseEvent")
public class Workout extends ParseObject implements ClusterItem {
    // the reason it is implementing cluster item is that now it can be used as a marker for clusters
    // declare database fields

    private static final String KEY_NAME = "eventName";

    private static final String KEY_DESCRIPTION = "eventDescription";

    private static final String KEY_MEDIA = "media";

    private static final String KEY_TIME = "eventTime";

    private static final String KEY_LOCATION = "eventLocation";

    private static final String KEY_USER = "user";



    private static final String KEY_PARTICIPANTS = "eventParticipants";

    private static final String KEY_TAGS = "tags";

    private static final String KEY_ID = "objectId";

    private static final String KEY_CATEGORY = "eventCategory";

    private static final String KEY_POLYLINE = "polyline";

    private static final String KEY_POLYLINE_BOUNDS = "polylineBounds";

    // define setters and getters

    public String getName() {
        return getString(KEY_NAME);
    }

    public void setName(String name) {
        put(KEY_NAME, name);
    }

    public String getDescription() {
        return getString(KEY_DESCRIPTION);
    }

    public void setDescription(String description) {
        put(KEY_DESCRIPTION, description);
    }

    public ParseFile getMedia() {
        return getParseFile(KEY_MEDIA);
    }

    public void setMedia(ParseFile media) {
        put(KEY_MEDIA, media);
    }

    public Date getTime() {
        return getDate(KEY_TIME);
    }

    public void setTime(Date time) {
        put(KEY_TIME, time);
    }

    public ParseGeoPoint getLocation() {
        return getParseGeoPoint(KEY_LOCATION);
    }

    public void setLocation(ParseGeoPoint location) {
        put(KEY_LOCATION, location);
    }

    public ParseUser getUser() {
        return getParseUser(KEY_USER);
    }

    public void setUser(ParseUser user) {
        put(KEY_USER, user);
    }

    public JSONArray getParticipants() {
        return getJSONArray(KEY_PARTICIPANTS);
    }

    public void setParticipants(JSONArray participants) {
        put(KEY_PARTICIPANTS, participants);
    }

    public JSONArray getTags() {
        return getJSONArray(KEY_TAGS);
    }

    public void setTags(JSONArray tags) {
        put(KEY_TAGS, tags);
    }

    public String getCategory() {
        return getString(KEY_CATEGORY);
    }

    public void setCategory(String category) {
        put(KEY_CATEGORY, category);
    }

    public String getID() {
        return (String) get(KEY_ID);
    }

    public void setPolyline(String polyline) { put(KEY_POLYLINE, polyline); }

    public String getPolyline() {return getString(KEY_POLYLINE); }

    public void setPolylineBounds(String bounds) { put(KEY_POLYLINE_BOUNDS, bounds); }

    public String getPolylineBounds() {return getString(KEY_POLYLINE_BOUNDS); }

    @Override
    public Date getCreatedAt() {
        return super.getCreatedAt();
    }

    public Boolean isInRange(ParseGeoPoint other, double maxRange) {
        return getLocation().distanceInMilesTo(other) < maxRange;
    }

    public Boolean isInTimeRange(long maxTimeRange) {
        return getHoursUntil() < maxTimeRange;
    }

    public double getDistance(ParseGeoPoint user) {
        return getLocation().distanceInMilesTo(user);
    }

    // helper methods for other functions

    public LatLng getLatLng() {
        final ParseGeoPoint loc = getLocation();
        return new LatLng(loc.getLatitude(), loc.getLongitude());
    }

    public LatLngBounds getPolylineLatLngBounds () {
        if (getPolyline() == null) return null;
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        String[] stringNums = getPolylineBounds().split(",");
        double[] nums = new double[4];
        for (int i = 0; i < 4; i ++) nums[i] = Double.parseDouble(stringNums[i]);
        LatLng southwest = new LatLng(nums[0], nums[1]);
        LatLng northeast = new LatLng(nums[2], nums[3]);
        builder.include(southwest).include(northeast);
        return builder.build();
    }

    public String getTimeUntil() {
        String relativeDate;
        long dateMillis = getTime().getTime();
        relativeDate = DateUtils.getRelativeTimeSpanString(dateMillis, System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS).toString();
        return relativeDate;
    }

    public long getHoursUntil(){
        long current = System.currentTimeMillis();
        long workout = getTime().getTime();
        long diffInMillisec = workout - current;
        long diffInHours = TimeUnit.MILLISECONDS.toHours(diffInMillisec);

        return diffInHours;
    }


    public int compareToDistance(@NonNull Workout workoutToCompare) {
        double otherWorkoutDistance = workoutToCompare.getDistance(ParseUser.getCurrentUser().getParseGeoPoint("currentLocation"));
        double currentWorkoutDistance = this.getDistance(ParseUser.getCurrentUser().getParseGeoPoint("currentLocation"));
        int difference = (int) (currentWorkoutDistance - otherWorkoutDistance);

        return difference;
    }

    public int compareToTime(@NonNull Workout workoutToCompare) {
        double otherWorkoutTime = workoutToCompare.getHoursUntil();
        double currentWorkoutTime = this.getHoursUntil();
        int difference = (int) (currentWorkoutTime - otherWorkoutTime);

        return difference;
    }


    public static class Query extends ParseQuery<Workout> {
        public Query() {
            super(Workout.class);
        }


        public Query getTop() {
            setLimit(20);
            return this;
        }

        public Query withUser() {
            include("User");
            return this;
        }

        public Query orderByLastCreated() {
            orderByDescending("createdAt");
            return this;
        }

        public Query createdBy(ParseUser user) {
            whereEqualTo(KEY_USER, user);
            return this;
        }

        public Query contains(ParseUser user) {
            whereContains(KEY_PARTICIPANTS, user.getObjectId());
            return this;
        }


        public Query getWithinRange(ParseGeoPoint currentLocation, double maxRange) {
            whereWithinMiles("eventLocation", currentLocation, maxRange);
            return this;
        }
        public Query getWithinTimeRange(long maxHours){
//            workout.isInTimeRange(maxHours);
            int maxHoursInt = (int) maxHours;
            Date currentTime = Calendar.getInstance().getTime();
            whereGreaterThan(KEY_TIME,currentTime);

            Calendar currentInstance = Calendar.getInstance();
            currentInstance.setTime(new Date());
            currentInstance.add(Calendar.HOUR_OF_DAY,maxHoursInt);
            Date maxDate = currentInstance.getTime();
            whereLessThanOrEqualTo(KEY_TIME,maxDate);
            return this;
        }

        public Query getwithTags() {
            whereContains(KEY_TAGS, "High Intensity");
            return this;
        }
        public Query orderByRange(){


            return this;
        }

    }


    // cluster item stuff


    @Override
    public LatLng getPosition() {
        return getLatLng();
    }

    @Override
    public String getTitle() {
        return getName();
    }

    @Override
    public String getSnippet() {
        return getDescription();
    }
}
