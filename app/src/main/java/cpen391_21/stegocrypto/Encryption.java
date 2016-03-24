package cpen391_21.stegocrypto;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;


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
}

