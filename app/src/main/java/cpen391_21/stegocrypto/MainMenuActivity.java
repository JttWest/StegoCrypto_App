package cpen391_21.stegocrypto;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import cpen391_21.stegocrypto.User.User;
import cpen391_21.stegocrypto.User.UserLocalStore;

public class MainMenuActivity extends AppCompatActivity {
    UserLocalStore userLocalStore;
    Button goToLoginBtn, goToRegisterBtn, logoutBtn, goToSendDataBtn, goToDecryptBtn;
    TextView displayName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        userLocalStore = new UserLocalStore(this);

        goToLoginBtn = (Button) findViewById(R.id.goToLogin);
        goToRegisterBtn = (Button) findViewById(R.id.goToRegister);
        logoutBtn = (Button) findViewById(R.id.logoutBtn);
        goToSendDataBtn = (Button) findViewById(R.id.goToSendData);
        goToDecryptBtn = (Button) findViewById(R.id.goToDecrypt);
        displayName = (TextView) findViewById(R.id.displayName);

        goToLoginBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(i);
            }
        });

        goToRegisterBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(i);
            }
        });

        logoutBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                userLocalStore.clearUserData();
                userLocalStore.setUserLoggedIn(false);
                Intent i = new Intent(getApplicationContext(), MainMenuActivity.class);
                startActivity(i);
            }
        });

        goToSendDataBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), Encryption.class);
                startActivity(i);
            }
        });

        goToDecryptBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), Decryption.class);
                startActivity(i);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (authenticate() == true) {
            goToRegisterBtn.setVisibility(View.GONE);
            goToLoginBtn.setVisibility(View.GONE);
            logoutBtn.setVisibility(View.VISIBLE);

            User currUser = userLocalStore.getLoggedInUser();
            String userName = currUser.getUserName();
            displayName.setText("Welcome: " + userName);
            displayName.setVisibility((View.VISIBLE));
        } else {
            goToRegisterBtn.setVisibility(View.VISIBLE);
            goToLoginBtn.setVisibility(View.VISIBLE);
            logoutBtn.setVisibility(View.GONE);
            displayName.setVisibility((View.GONE));

        }
    }

    private boolean authenticate() {
        if (userLocalStore.getLoggedInUser() == null) {
            //Intent intent = new Intent(this, Login.class);
            //startActivity(intent);
            return false;
        }
        return true;
    }
}
