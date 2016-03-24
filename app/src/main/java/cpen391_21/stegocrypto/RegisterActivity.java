package cpen391_21.stegocrypto;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import cpen391_21.stegocrypto.ServerRequests.UserAccountRequests;
import cpen391_21.stegocrypto.User.GetUserCallback;
import cpen391_21.stegocrypto.User.User;


public class RegisterActivity extends AppCompatActivity implements View.OnClickListener{
    EditText etUsername, etPassword, etConfirmPassword;
    Button bRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etUsername = (EditText) findViewById(R.id.etUsername);
        etPassword = (EditText) findViewById(R.id.etPassword);
        etConfirmPassword = (EditText) findViewById(R.id.etConfirmPassword);
        bRegister = (Button) findViewById(R.id.bRegister);

        bRegister.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bRegister:
                String username = etUsername.getText().toString();
                String password = etPassword.getText().toString();
                String confirmPassword = etConfirmPassword.getText().toString();

                if (!password.equals(confirmPassword)){
                    Toast toast = Toast.makeText(this, "Password does not match!", Toast.LENGTH_SHORT);
                    toast.show();
                    break;
                }

                User user = new User(username, password);
                registerUser(user);
                break;
        }
    }

    private void registerUser(User user) {
        UserAccountRequests registerRequest = new UserAccountRequests(this);
        registerRequest.storeUserDataInBackground(user, new GetUserCallback() {
            @Override
            public void done(User returnedUser) {
                Intent loginIntent = new Intent(RegisterActivity.this, MainMenuActivity.class);
                startActivity(loginIntent);
            }
        });
    }
}
