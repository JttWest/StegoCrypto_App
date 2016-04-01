package cpen391_21.stegocrypto;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;


import java.io.File;
import java.io.IOException;
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

import cpen391_21.stegocrypto.User.UserLocalStore;

public class Encryption extends AppCompatActivity implements View.OnClickListener{
    Button selectLocBtn, sendDataBtn, cameraBtn, browseImagesBtn, drawBtn;
    TextView geo_key;
    EditText toUsername;
    ImageView selectedImageIV;

    Uri cameraFileUri;

    final private static int SELECT_LOCATION_REQUEST = 1;
    final private static int PICK_IMAGE_REQUEST = 2;
    final private static int CAMERA_REQUEST = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_encyption);

        // register button to send user to selection location activity
        selectLocBtn = (Button) findViewById(R.id.go_select_loc);
        sendDataBtn = (Button) findViewById(R.id.send_data);
        browseImagesBtn = (Button) findViewById(R.id.browseImagesBtn);
        cameraBtn = (Button) findViewById(R.id.cameraBtn);
        drawBtn = (Button) findViewById(R.id.drawBtn);

        selectedImageIV = (ImageView) findViewById(R.id.ivSelectedImage);

        toUsername = (EditText) findViewById(R.id.toUsername);

        geo_key = (TextView) findViewById(R.id.geo_key);

        selectLocBtn.setOnClickListener(this);
        sendDataBtn.setOnClickListener(this);
        browseImagesBtn.setOnClickListener(this);
        cameraBtn.setOnClickListener(this);
        drawBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.go_select_loc:
                Intent i = new Intent(getApplicationContext(), SelectLocation.class);
                startActivityForResult(i, SELECT_LOCATION_REQUEST);
                break;
            case R.id.send_data:
                EditText dataET = (EditText) findViewById(R.id.data_for_enc);
                String dataForEnc = dataET.getText().toString();

                //ServerRequests.HTTPCommands.performPostCall("https://stegocrypto-server.herokuapp.com/sendM")
                postData(dataForEnc);

                Intent backToMenu = new Intent(getApplicationContext(), MainMenuActivity.class);
                startActivity(backToMenu);
                break;
            case R.id.browseImagesBtn:
                Intent intent = new Intent();
                // Show only images, no videos or anything else
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                // Always show the chooser (if there are multiple options available)
                startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST);
                break;
            case R.id.cameraBtn:
                //Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);

                // fetching the root directory
                String rootDir = Environment.getExternalStorageDirectory().toString();

                // Creating folders for Image
                String imageFolderPath = rootDir + "/StegoCryptoImages";
                File imagesFolder = new File(imageFolderPath);
                imagesFolder.mkdirs(); // make directory if if doesn't exist

                // Generating file name
                String imageName = "temp.png";

                // Creating image here

                File image = new File(imageFolderPath, imageName);
                int count = 2;
                while (image.exists()){
                    imageName = String.format("temp%d.png", count);
                    image = new File(imageFolderPath, imageName);
                    count++;
                }

                cameraFileUri = Uri.fromFile(image);

                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraFileUri);

                //startActivityForResult(takePictureIntent, CAMERA_IMAGE_REQUEST);

                startActivityForResult(cameraIntent, CAMERA_REQUEST);

            case R.id.drawBtn:
                Intent drawIntent = new Intent(getApplicationContext(), DrawingActivity.class);
                startActivity(drawIntent);
        }
    }

    // retrieve result from select location activity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_CANCELED)
            return;

        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case SELECT_LOCATION_REQUEST:
                LatLng coordResult = (LatLng) data.getExtras().get("selectedCoord");
                geo_key.setText(coordResult.toString());
                break;
            case PICK_IMAGE_REQUEST:
                Uri uri = data.getData();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                    Log.v("StegoCrypto-Image", String.valueOf(bitmap));

                    selectedImageIV.setImageBitmap(bitmap);
                } catch (IOException e) {e.printStackTrace();}
                break;
            case CAMERA_REQUEST:
                //if (resultCode == RESULT_OK) {
                    //Uri photo = (Uri) data.getExtras().get("data");
                if (cameraFileUri != null)
                    selectedImageIV.setImageURI(cameraFileUri);
                    //SelectedImageIV.setImageBitmap(photo);
                //} else { Log.v("StegoCrypto-Camera", "bad camera result!"); }
                break;
        }
    }


    /* will removing this later */
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
                params.put("toUserName", Uri.encode(toUsername.getText().toString()));

                SharedPreferences userLocalDatabase = getSharedPreferences(UserLocalStore.USER_LOCAL_STORE_SP_NAME, Context.MODE_PRIVATE);
                String from_username = userLocalDatabase.getString("userName", "");

                params.put("fromUserName", Uri.encode(from_username));
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

