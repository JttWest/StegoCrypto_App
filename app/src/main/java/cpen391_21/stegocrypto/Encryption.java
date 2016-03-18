package cpen391_21.stegocrypto;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;


import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class Encryption extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_encyption);

        // register button to send user to selection location activity
        Button selectLocBtn = (Button) findViewById(R.id.go_select_loc);
        Button sendDataBtn = (Button) findViewById(R.id.send_data);

        selectLocBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), SelectLocation.class);
                startActivity(i);
            }
        });

        sendDataBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                EditText dataET = (EditText) findViewById(R.id.data_for_enc);
                String dataForEnc = dataET.getText().toString();
                postData(dataForEnc);

                Intent i = new Intent(getApplicationContext(), MainMenuActivity.class);
                startActivity(i);
            }
        });


    }

    private void postData(String data) {
        HttpURLConnection conn = null;

        try {
            URL url = new URL("https://stegocrypto-server.herokuapp.com/sendData");
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");

            conn.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded");

            conn.setRequestProperty("Content-Language", "en-US");

            conn.setDoInput(true);
            conn.setDoOutput(true);
            // read the response
            //Send request
            DataOutputStream wr = new DataOutputStream (
                    conn.getOutputStream ());
            wr.writeBytes (data);
            wr.flush ();
            wr.close ();
        }
        catch (Exception e) {
            Log.v("StegoCrypto", "POST to server failed");
         //
        } finally {
            conn.disconnect();
        }
    }
}

