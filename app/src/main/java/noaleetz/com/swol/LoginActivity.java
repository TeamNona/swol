package noaleetz.com.swol;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LoginActivity extends AppCompatActivity {

    @BindView(R.id.etUsername) EditText usernameInput;
    @BindView(R.id.etPassword) EditText passwordInput;
    @BindView(R.id.etName) EditText nameInput;
    @BindView(R.id.etEmail) EditText emailInput;
    @BindView(R.id.btLogin) Button loginButton;
    @BindView(R.id.btSignup) Button signupButton;
    @BindView(R.id.btRegister) Button registerButton;
    @BindView(R.id.btCancel) Button cancelButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // find references to views
        ButterKnife.bind(this);

        //hide the register stuff
        nameInput.setVisibility(View.GONE);
        emailInput.setVisibility(View.GONE);
        registerButton.setVisibility(View.GONE);
        cancelButton.setVisibility(View.GONE);

        //show the sign up stuff
        loginButton.setVisibility(View.VISIBLE);
        loginButton.bringToFront();
        signupButton.setVisibility(View.VISIBLE);
        signupButton.bringToFront();
        usernameInput.setVisibility(View.VISIBLE);
        usernameInput.bringToFront();
        passwordInput.setVisibility(View.VISIBLE);
        passwordInput.bringToFront();

        //persistence
//        if (ParseUser.getCurrentUser() != null) {
//            final Intent i = new Intent(LoginActivity.this, MainActivity.class);
//            startActivity(i);
//            finish();
//        }

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String username = usernameInput.getText().toString();
                final String password = passwordInput.getText().toString();

                login(username, password);
            }
        });

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // TODO: use the transition manager
                loginButton.setVisibility(View.GONE);
                signupButton.setVisibility(View.GONE);
                registerButton.setVisibility(View.VISIBLE);
                registerButton.bringToFront();
                cancelButton.setVisibility(View.VISIBLE);
                cancelButton.bringToFront();
                emailInput.setVisibility(View.VISIBLE);
                nameInput.setVisibility(View.VISIBLE);


            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String username = usernameInput.getText().toString();
                final String password = passwordInput.getText().toString();
                final String email = emailInput.getText().toString();
                final String name = nameInput.getText().toString();

                createNewUser(username, name, password, email);

            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // TODO: use the transition manager
                loginButton.setVisibility(View.VISIBLE);
                loginButton.bringToFront();
                signupButton.setVisibility(View.VISIBLE);
                signupButton.bringToFront();
                registerButton.setVisibility(View.GONE);
                cancelButton.setVisibility(View.GONE);
                emailInput.setVisibility(View.GONE);
                nameInput.setVisibility(View.GONE);

            }
        });
    }

    private void login(String username, String password) {
        ParseUser.logInInBackground(username, password, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException e) {
                if (e == null) {
                    Log.d("LoginActivity", "login successful!");

                    // probably go to another intent
                    final Intent i = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(i);
                    finish();
                }
                else {
                    Log.e("LoginActivity", "login failure :(");
                    e.printStackTrace();
                }
            }
        });
    }

    private void createNewUser(String username, String name, String password, String email) {
        // create the new user
        ParseUser user = new ParseUser();
        // Set properties
        user.setUsername(username);
        user.setPassword(password);
        user.setEmail(email);
        user.put("name", name);
        // Invoke signUpInBackground
        user.signUpInBackground(new SignUpCallback() {
            public void done(ParseException e) {
                if (e == null) {
                    Log.d("LoginActivity", "login successful!");

                    // probably go to another intent
                    final Intent i = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(i);
                    finish();
                } else {
                    Log.e("LoginActivity", "signup failure :(");
                    e.printStackTrace();
                }
            }
        });
    }
}
