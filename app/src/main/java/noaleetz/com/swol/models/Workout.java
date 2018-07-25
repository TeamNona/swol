package noaleetz.com.swol.models;

import android.text.format.DateUtils;

import com.google.android.gms.maps.model.LatLng;
import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.parceler.Parcel;

import java.util.Date;

@ParseClassName("exerciseEvent")
public class Workout extends ParseObject{

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

    public void setUser (ParseUser user) {
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

    public String getID() {
        return (String) get(KEY_ID);
    }

    @Override
    public Date getCreatedAt() {
        return super.getCreatedAt();
    }

    public Boolean isInRange(ParseGeoPoint other, double maxRange) {
        return getLocation().distanceInMilesTo(other) < maxRange;
    }

    // helper methods for other functions

    public LatLng getLatLng() { final ParseGeoPoint loc = getLocation(); return new LatLng(loc.getLatitude(), loc.getLongitude());}

    public String getTimeUntil() {
        String relativeDate;
        long dateMillis = getTime().getTime();
        relativeDate = DateUtils.getRelativeTimeSpanString(dateMillis, System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS).toString();
        return relativeDate;
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

        public Query getForUser(ParseUser currentUser) {
            whereEqualTo(KEY_USER, currentUser);
            return this;
        }


        public Query getWithinRange(ParseGeoPoint currentLocation, double maxRange) {
            whereWithinMiles("eventLocation", currentLocation, maxRange);
            return this;
        }
    }

}
