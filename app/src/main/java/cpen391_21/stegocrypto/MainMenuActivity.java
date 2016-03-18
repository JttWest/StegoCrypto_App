package cpen391_21.stegocrypto;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainMenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        Button goToLoginBtn = (Button) findViewById(R.id.goToLogin);
        Button goToSendDataBtn = (Button) findViewById(R.id.goToSendData);
        Button goToDecryptBtn = (Button) findViewById(R.id.goToDecrypt);

        goToLoginBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), LoginActivity.class);
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


}
