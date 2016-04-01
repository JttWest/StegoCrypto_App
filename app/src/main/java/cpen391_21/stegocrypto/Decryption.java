package cpen391_21.stegocrypto;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;


public class Decryption extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_decryption);

        Button decryptedBtn = (Button) findViewById(R.id.decryptedBtn);

        decryptedBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final TextView decryptedDataTV = (TextView) findViewById(R.id.decrypted_data);
                displayData(decryptedDataTV);
                //decrypted_data.setText(result);
            }
        });
    }

    private void displayData(final TextView decryptedDataTV) {

        Intent i = getIntent();
        String data = i.getStringExtra("data");

        if (data != null){
            decryptedDataTV.setText(data);
        }

        /*
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://stegocrypto-server.herokuapp.com/retrieveData";

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
            new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    // Display the first 500 characters of the response string.
                    decryptedDataTV.setText(response);
                    //Log.v("StegoCrypto", "Response from server is: " + response.substring(0, 500));
                }
            }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.v("StegoCrypto", "Failed to retrieve data: " + error.getMessage());
            }
        });

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
        */
    }
}
