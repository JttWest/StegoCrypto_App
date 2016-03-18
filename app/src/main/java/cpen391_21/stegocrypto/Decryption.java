package cpen391_21.stegocrypto;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Decryption extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_decryption);

        Button decryptedBtn = (Button) findViewById(R.id.decryptedBtn);

        decryptedBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String result = getData();

                final TextView decrypted_data = (TextView) findViewById(R.id.decrypted_data);
                decrypted_data.setText(result);
            }
        });
    }

    private String getData(){
        HttpURLConnection conn = null;

        try {
            URL url = new URL("https://stegocrypto-server.herokuapp.com/retrieveData");
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(
                    conn.getInputStream()));
            String inputLine;

            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            return inputLine;
        }
        catch (Exception e) {
            return "";
        } finally {
            conn.disconnect();
        }
    }
}
