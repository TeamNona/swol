package noaleetz.com.swol.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;

@ParseClassName("User")
public class User extends ParseObject {

    // declare database fields

    public String KEY_USERNAME = "username";

    public String KEY_PASSWORD = "password";

    public String KEY_EMAIL = "email";

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



}
