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

public class MainMenuActivity extends AppCompatActivity implements View.OnClickListener{
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

        goToLoginBtn.setOnClickListener(this);
        goToRegisterBtn.setOnClickListener(this);
        logoutBtn.setOnClickListener(this);
        goToSendDataBtn.setOnClickListener(this);
        goToDecryptBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.goToLogin:
                Intent i = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(i);
                break;
            case R.id.goToRegister:
                Intent registerIntent = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(registerIntent);
                break;
            case R.id.logoutBtn:
                userLocalStore.clearUserData();
                userLocalStore.setUserLoggedIn(false);
                Intent restartMenu = new Intent(getApplicationContext(), MainMenuActivity.class);
                startActivity(restartMenu);
                break;
            case R.id.goToSendData:
                Intent encrypt = new Intent(getApplicationContext(), Encryption.class);
                startActivity(encrypt);
                break;
            case R.id.goToDecrypt:
                Intent decrypt = new Intent(getApplicationContext(), Decryption.class);
                startActivity(decrypt);
                break;
        }
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
