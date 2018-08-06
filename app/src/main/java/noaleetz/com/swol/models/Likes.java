package noaleetz.com.swol.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

@ParseClassName("Likes")
public class Likes extends ParseObject {

    // declare database fields
    private static final String KEY_USER = "likedBy";

    private static final String KEY_POST = "likedPost";

    // define setters and getters
    public ParseUser getLikedBy() {
        return getParseUser(KEY_USER);
    }

    public void setLikedBy(ParseUser user) {
        put(KEY_USER, user);
    }

    public ParseObject getLikedPost() {
        return getParseObject(KEY_POST);
    }

    public void setLikedPost(ParseObject workout) {
        put(KEY_POST, workout);
    }

    // define the queries
    public static class Query extends ParseQuery<Likes> {

        public Query() {
            super(Likes.class);
        }

        public Query getLikesNumber(ParseObject post) {
            whereEqualTo(KEY_POST, post);
            return this;
        }
    }
}
