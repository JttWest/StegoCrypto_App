package cpen391_21.stegocrypto;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.model.LatLng;

public class Encryption extends AppCompatActivity {
    Button selectLocBtn, sendDataBtn;
    TextView geo_key;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_encyption);

        // register button to send user to selection location activity
        selectLocBtn = (Button) findViewById(R.id.go_select_loc);
        sendDataBtn = (Button) findViewById(R.id.send_data);
        geo_key = (TextView) findViewById(R.id.geo_key);

        selectLocBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), SelectLocation.class);
                startActivityForResult(i, 1);


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

    private void postData(final String data) {
            RequestQueue queue = Volley.newRequestQueue(this);
            String postDataURL = "https://stegocrypto-server.herokuapp.com/sendData";

            StringRequest sr = new StringRequest(Request.Method.POST,postDataURL , new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.v("StegoCrypto", "Response from server: " + response);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.v("StegoCrypto", "POST data failed: " + error.getMessage());
                }
            }){
                @Override
                protected Map<String,String> getParams(){
                    Map<String,String> params = new HashMap<String, String>();
                    params.put("data", Uri.encode(data));
                    return params;
                }

                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String,String> params = new HashMap<String, String>();
                    params.put("Content-Type","application/x-www-form-urlencoded");
                    return params;
                }
            };

            queue.add(sr);
    }

    // Function to read the result from select location activity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == 1){
            LatLng coordResult = (LatLng) data.getExtras().get("selectedCoord");

            geo_key.setText(coordResult.toString());
        }

    }
}

