package cpen391_21.stegocrypto;

import android.content.Intent;
import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import cpen391_21.stegocrypto.ServerRequests.DataTransferRequests;
import cpen391_21.stegocrypto.ServerRequests.HTTPCommands;
import cpen391_21.stegocrypto.ServerRequests.UserAccountRequests;


public class Decryption extends AppCompatActivity implements View.OnClickListener{
    Button decryptedBtn;
    TextView decryptedDataTV;
    ImageView imageDisplayIV;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_decryption);

        decryptedBtn = (Button) findViewById(R.id.decryptedBtn);
        decryptedDataTV = (TextView) findViewById(R.id.decrypted_data);
        imageDisplayIV = (ImageView) findViewById(R.id.imageDisplay);

        decryptedBtn.setOnClickListener(this);

        tryRetrieveImageFromServer();

    }
    /*
    @Override
    protected void onStart(){
        super.onStart();

        if (getIntent().getExtras() != null) {
            Bundle b = getIntent().getExtras();
            String rawData = b.getString("data");

            if (rawData != null) {
                try {
                    JSONObject jsonData = new JSONObject(rawData);
                    String packageID = jsonData.getString("package_id");

                    String requestURL = HTTPCommands.SERVER_URL + "retrieveDataFromPackage" +
                            "?packageID=" + packageID;

                    DataTransferRequests dataTransferRequests = new DataTransferRequests(this);
                    dataTransferRequests.retrieveDataAsyncTask(requestURL, imageDisplayIV);
                } catch (JSONException e) {e.printStackTrace();}
            }
        }
    }*/


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.decryptedBtn:
                decryptedDataTV.setText("Decrpyted data appears here");
                break;
        }
    }

    private void tryRetrieveImageFromServer(){
        if (getIntent().getExtras() != null) {
            Bundle b = getIntent().getExtras();
            String rawData = b.getString("data");

            if (rawData != null) {
                try {
                    JSONObject jsonData = new JSONObject(rawData);
                    String packageID = jsonData.getString("package_id");

                    String requestURL = HTTPCommands.SERVER_URL + "retrieveDataFromPackage" +
                            "?packageID=" + packageID;

                    DataTransferRequests dataTransferRequests = new DataTransferRequests(this);
                    dataTransferRequests.retrieveDataAsyncTask(requestURL, imageDisplayIV);
                } catch (JSONException e) {e.printStackTrace();}
            }
        }
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
