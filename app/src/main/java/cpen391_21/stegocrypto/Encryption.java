package cpen391_21.stegocrypto;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import cpen391_21.stegocrypto.StegocryptoHardware.StegocryptoHardware;
import cpen391_21.stegocrypto.ServerRequests.DataTransferRequests;
import cpen391_21.stegocrypto.User.UserLocalStore;
import cpen391_21.stegocrypto.Utility.ImageUtility;

public class Encryption extends AppCompatActivity implements View.OnClickListener{
    Button selectLocBtn, enc_data, sendDataBtn, cameraBtn, browseImagesBtn, drawBtn;
    TextView geo_key;
    EditText toUsername;
    ImageView selectedImageIV;

    Uri cameraFileUri;

    private ProgressDialog progressDialog;
    private StegocryptoHardware bluetooth;
    private byte[] stegoTaskResult;
    private boolean stegoTaskDone;

    final private static int SELECT_LOCATION_REQUEST = 1;
    final private static int PICK_IMAGE_REQUEST = 2;
    final private static int CAMERA_REQUEST = 3;
    final private static int DRAW_REQUEST = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_encyption);

        // register button to send user to selection location activity
        selectLocBtn = (Button) findViewById(R.id.go_select_loc);
        sendDataBtn = (Button) findViewById(R.id.send_data);
        enc_data = (Button) findViewById(R.id.enc_data);
        browseImagesBtn = (Button) findViewById(R.id.browseImagesBtn);
        cameraBtn = (Button) findViewById(R.id.cameraBtn);
        drawBtn = (Button) findViewById(R.id.drawBtn);

        selectedImageIV = (ImageView) findViewById(R.id.ivSelectedImage);

        toUsername = (EditText) findViewById(R.id.toUsername);

        geo_key = (TextView) findViewById(R.id.geo_key);

        selectLocBtn.setOnClickListener(this);
        sendDataBtn.setOnClickListener(this);
        enc_data.setOnClickListener(this);
        browseImagesBtn.setOnClickListener(this);
        cameraBtn.setOnClickListener(this);
        drawBtn.setOnClickListener(this);

        bluetooth = new StegocryptoHardware();
        progressDialog = new ProgressDialog(this);
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

                HashMap<String,String> params = new HashMap<String, String>();
                SharedPreferences userLocalDatabase = getSharedPreferences(UserLocalStore.USER_LOCAL_STORE_SP_NAME, Context.MODE_PRIVATE);
                String from_username = userLocalDatabase.getString("userName", "");

                params.put("fromUserName", Uri.encode(from_username));
                params.put("toUserName", Uri.encode(toUsername.getText().toString()));


                // grab the current selected bitmap and convert it to base64 string
                //Bitmap bitmap = ((BitmapDrawable)selectedImageIV.getDrawable()).getBitmap();
                //Bitmap resizedBitmap = ImageUtility.getResizedBitmap(bitmap, ImageUtility.MAX_IMAGE_SIZE);

                // grab the image from local storage now instead
                String root = Environment.getExternalStorageDirectory().toString();
//                File imgFile = new  File(root + "/stegoCrypto1.bmp");
//                if(imgFile.exists()) {
//                    Bitmap encImgBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
//                    ByteArrayOutputStream byteArrayOS  = new ByteArrayOutputStream();
//                    encImgBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOS);
//                    String imageBase64 = Base64.encodeToString(byteArrayOS.toByteArray(), Base64.DEFAULT);
//
//                    params.put("data", imageBase64);
//                }
                if (stegoTaskResult != null) {
                    String imageBase64 = Base64.encodeToString(stegoTaskResult, Base64.DEFAULT);
                    params.put("data", imageBase64);
                    Log.i("Derek", imageBase64);

                    Log.d("StegoByte", Arrays.toString(stegoTaskResult));
                }


                DataTransferRequests dataTransferRequest = new DataTransferRequests(this);
                dataTransferRequest.sendDataAsyncTask(params);
                //postData(dataForEnc);

                Intent backToMenu = new Intent(getApplicationContext(), MainMenuActivity.class);
                startActivity(backToMenu);
                break;
            case R.id.enc_data:
                /* Get the data */
                dataET = (EditText) findViewById(R.id.data_for_enc);
                String data = dataET.getText().toString();
                if (data.equals("")) {
                    data = "(no data)";
                }

                /* TODO: take values from edittext when GoogleMaps API works */

                String[] latAndLongArray = geo_key.getText().toString().split(",");
                if (latAndLongArray.length < 2) {
                    Log.e("Encryption", "No entry in geo_key. Cannot proceed");
                    Toast.makeText(getBaseContext(), "Please select a Geo Key", Toast.LENGTH_SHORT).show();
                    break;
                }
                String latitude = latAndLongArray[0]; //"49.261";
                String longitude = latAndLongArray[1]; //"-123.251";

                /* Get the image data */
                Bitmap bitmap = ((BitmapDrawable)selectedImageIV.getDrawable()).getBitmap();
                Bitmap resizedBitmap = ImageUtility.getResizedBitmap(bitmap, ImageUtility.MAX_IMAGE_SIZE);
                try {
                    ByteBuffer imagedatabb = ImageUtility.bitmapToByteBuffer(resizedBitmap);

                    if (imagedatabb == null) {
                        Log.e("Encryption", "Bitmap was NULL!");
                    } else {
//                        byte[] imgbyte = new byte[imagedatabb.remaining()];
//                        imagedatabb.get(imgbyte, 0, imgbyte.length);
                        byte[] imgbyte = imagedatabb.array();
                        Log.e("Encryption", "Bitmap has size " + imgbyte.length + " bytes");

                        /* Save the image first */
                        // fetching the root directory
                        String rootDir = Environment.getExternalStorageDirectory().toString();
                        // Creating folders for Image
                        String imageFolderPath = rootDir + "/StegoCryptoImages";
                        File imagesFolder = new File(imageFolderPath);
                        imagesFolder.mkdirs(); // make directory if if doesn't exist
                        // Generating file name
                        String imageName = "/stegoCryptoUnncrypted.bmp";
                        ImageUtility.writeToFile(imageFolderPath + imageName, imgbyte);

                        /* Send the data to the hardware */
                        new StegoCryptoEncrypt().execute(data.getBytes(), longitude.getBytes(), latitude.getBytes(), imgbyte);
                    }
                } catch (Exception e) {
                    Log.e("Encryption", "Could not convert bitmap");
                }


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
                break;

            case R.id.drawBtn:
                Intent drawIntent = new Intent(getApplicationContext(), DrawingActivity.class);
                startActivityForResult(drawIntent, DRAW_REQUEST);
                break;
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
                String coordResult = (String) data.getExtras().get("selectedCoord");
                geo_key.setText(coordResult);
                break;
            case PICK_IMAGE_REQUEST:
                Uri uri = data.getData();
                try {
                    Bitmap imageBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                    Log.v("StegoCrypto-Image", String.valueOf(imageBitmap));

                    selectedImageIV.setImageBitmap(ImageUtility.getResizedBitmap(imageBitmap, ImageUtility.MAX_IMAGE_SIZE));
                } catch (IOException e) {e.printStackTrace();}
                break;
            case CAMERA_REQUEST:
                if (cameraFileUri != null) {
                    try {
                        Bitmap cameraBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), cameraFileUri);
                        selectedImageIV.setImageBitmap(ImageUtility.getResizedBitmap(cameraBitmap, ImageUtility.MAX_IMAGE_SIZE));
                    } catch (IOException e) {e.printStackTrace();}
                }
                break;
            case DRAW_REQUEST:
                Bitmap drawBitmap = data.getParcelableExtra("drawingBitmap");
                selectedImageIV.setImageBitmap(ImageUtility.getResizedBitmap(drawBitmap, ImageUtility.MAX_IMAGE_SIZE));
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


    private class StegoCryptoEncrypt extends AsyncTask<byte[], Integer, byte[]> {

        /**
         *
         * @param bytes bytes[0]: text data
         *              bytes[1]: longitude
         *              bytes[2]: latitude
         *              bytes[3]: image
         * @return
         */
        @Override
        protected byte[] doInBackground(byte[]... bytes) {
            long totalSize = 0;

            bluetooth.init();

            publishProgress(1);
            Log.i("Bluetooth", "Selecting encryption: ");
            bluetooth.selectOption(StegocryptoHardware.OPT.OPT_ENCRYPT);

            publishProgress(2);
            Log.i("Bluetooth", "Sending text data: " + bytes[0]);
            bluetooth.sendToHardware(bytes[0]);

            publishProgress(3);
            Log.i("Bluetooth", "Sending longitude data");
            bluetooth.sendToHardware(bytes[1]);
            Log.i("Bluetooth", "Done sending longitude data");

            publishProgress(4);
            Log.i("Bluetooth", "Sending latitude data");
            bluetooth.sendToHardware(bytes[2]);
            Log.i("Bluetooth", "Done sending latitude data");

            publishProgress(5);
            Log.i("Bluetooth", "Sending image data");
            bluetooth.sendToHardware(bytes[3]);
            Log.i("Bluetooth", "Done sending image data");

            publishProgress(6);
            byte[] ret = bluetooth.receiveFromHardware(10000);
            if (ret != null) {
                Log.i("Bluetooth", "Received: " + new String(ret, 0, ret.length));
                totalSize = ret.length;
            }

            try { Thread.sleep(1000); } catch (Exception e) {};
            bluetooth.fini();

            return ret;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            /* Show progressDialog */
            progressDialog.setTitle("Encrypting...");
            progressDialog.setMessage(getString(R.string.loadingEncryption));
            progressDialog.setIndeterminate(false);
            progressDialog.setMax(6);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected void onProgressUpdate(Integer... args) {
            super.onProgressUpdate();
            progressDialog.setProgress(args[0]);
            switch (args[0]) {
                case 0:
                    progressDialog.setMessage(getString(R.string.loadingEncryption));
                    break;
                case 1:
                    progressDialog.setMessage("Initializing...");
                    break;
                case 2:
                    progressDialog.setMessage("Sending text data...");
                    break;
                case 3:
                    progressDialog.setMessage("Sending GPS data...");
                    break;
                case 4:
                    progressDialog.setMessage("Sending GPS data...");
                    break;
                case 5:
                    progressDialog.setMessage("Sending image data... (this might take a while)");
                    break;
                case 6:
                    progressDialog.setMessage("Receiving image data... (this might take a while)");
                    break;
                default:
                    break;
            }
        }

        @Override
        protected void onPostExecute(byte[] result) {
            stegoTaskResult = result;

            if (result != null) {
                Toast.makeText(getBaseContext(), "Success", Toast.LENGTH_LONG).show();
            }

            stegoTaskDone = true;
            progressDialog.dismiss();
        }
    }
}

