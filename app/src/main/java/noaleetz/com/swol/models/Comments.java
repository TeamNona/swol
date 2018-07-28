package noaleetz.com.swol.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

@ParseClassName("Comments")
public class Comments extends ParseObject{

    // declare database fields

    private static final String KEY_DESCRIPTION = "description";

    private static final String KEY_POST = "postedTo";

    private static final String KEY_USER = "postedBy";

    // define setters and getters

    public String getComment() {
        return getString(KEY_DESCRIPTION);
    }

    public void setComment(String comment) {
        put(KEY_DESCRIPTION, comment);
    }

    public ParseUser getPostedBy () {
        return getParseUser(KEY_USER);
    }

    public void setPostedBy (ParseUser user) {
        put(KEY_USER, user);
    }

    public ParseObject getPostedTo () {
        return getParseObject(KEY_POST);
    }

    public void setPostedTo(ParseObject workout) {
        put(KEY_POST, workout);
    }


    // define the queries
    public static class Query extends ParseQuery<Comments> {

        public Query() {
            super(Comments.class);
        }

        public Query getPostComments (String post_id) {
            whereEqualTo(KEY_POST, post_id);
            return this;
        }

        public Query getTop () {
            setLimit(10);
            orderByAscending("createdAt");
            return this;
        }
    }
}
