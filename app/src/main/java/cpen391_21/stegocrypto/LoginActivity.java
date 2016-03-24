package cpen391_21.stegocrypto;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import cpen391_21.stegocrypto.R;
import cpen391_21.stegocrypto.RegisterActivity;
import cpen391_21.stegocrypto.ServerRequests.UserAccountRequests;
import cpen391_21.stegocrypto.User.GetUserCallback;
import cpen391_21.stegocrypto.User.User;
import cpen391_21.stegocrypto.User.UserLocalStore;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    Button sign_in_button, register_button;
    EditText etUsername, etPassword;

    UserLocalStore userLocalStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sign_in_button = (Button) findViewById(R.id.sign_in_button);
        register_button = (Button) findViewById(R.id.register_button);
        etUsername = (EditText) findViewById(R.id.etUsername);
        etPassword = (EditText) findViewById(R.id.etPassword);

        sign_in_button.setOnClickListener(this);
        register_button.setOnClickListener(this);

        userLocalStore = new UserLocalStore(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.sign_in_button:
                String username = etUsername.getText().toString();
                String password = etPassword.getText().toString();

                User user = new User(username, password);

                authenticate(user);
                break;
            case R.id.register_button:
                Intent registerIntent = new Intent(this, RegisterActivity.class);
                startActivity(registerIntent);
                break;
        }
    }

    private void authenticate(User user) {
        UserAccountRequests userAccountRequest = new UserAccountRequests(this);
        userAccountRequest.fetchUserDataAsyncTask(user, new GetUserCallback() {
            @Override
            public void done(User returnedUser) {
                if (returnedUser == null) {
                    showErrorMessage();
                } else {
                    logUserIn(returnedUser);
                }
            }
        });
    }

    private void showErrorMessage() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(LoginActivity.this);
        dialogBuilder.setMessage("Incorrect user details");
        dialogBuilder.setPositiveButton("Ok", null);
        dialogBuilder.show();
    }

    private void logUserIn(User returnedUser) {
        userLocalStore.storeUserData(returnedUser);
        userLocalStore.setUserLoggedIn(true);
        startActivity(new Intent(this, MainMenuActivity.class));
    }
}