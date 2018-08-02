package noaleetz.com.swol.models;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONException;
import org.json.JSONObject;

@ParseClassName("User")
public class User extends ParseObject {

    // declare database fields

    private static final String KEY_USERNAME = "username";

    private static final String KEY_PASSWORD = "password";

    private static final String KEY_EMAIL = "email";

    private static final String KEY_NAME = "name";

    private static final String KEY_PROFILE_PICTURE = "profilePicture";

    private static final String KEY_CURRENTLOCATION= "currentLocation";


    // define setters and getters

    public String getUsername() {
        return getString(KEY_USERNAME);
    }

    public void setUsername(String username) {
        put(KEY_USERNAME, username);
    }

    public String getPassword() {
        return getString(KEY_PASSWORD);
    }

    public void setPassword(String password) {
        put(KEY_PASSWORD, password);
    }

    public String getEmail() {
        return getString(KEY_EMAIL);
    }

    public void setEmail(String email) {
        put(KEY_EMAIL, email);
    }

    public String getName() {
        return getString(KEY_NAME);
    }

    public void setName(String name) {
        put(KEY_NAME, name);
    }


    public ParseFile getProfilePicture () {
        return getParseFile(KEY_PROFILE_PICTURE);
    }

    public void setProfilePicture (ParseFile profilePicture) {
        put(KEY_PROFILE_PICTURE, profilePicture);
    }

    public void setCurrentLocation(ParseGeoPoint currentLocation) { put(KEY_CURRENTLOCATION,currentLocation); }

    public ParseGeoPoint getCurrentLocation() {return getParseGeoPoint(KEY_CURRENTLOCATION);}

    public int getFBID (ParseUser user) throws JSONException {
        JSONObject test = user.getJSONObject("authData");
        JSONObject test1 = test.getJSONObject("facebook");

        String test2 = test1.getString("id");

        return Integer.parseInt(test2);

    }



}
