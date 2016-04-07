package cpen391_21.stegocrypto;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Environment;
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

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

import cpen391_21.stegocrypto.ServerRequests.DataTransferRequests;
import cpen391_21.stegocrypto.ServerRequests.HTTPCommands;
import cpen391_21.stegocrypto.ServerRequests.UserAccountRequests;
import cpen391_21.stegocrypto.StegocryptoHardware.StegocryptoHardware;
import cpen391_21.stegocrypto.Utility.ImageUtility;


public class Decryption extends AppCompatActivity implements View.OnClickListener{
    Button decryptedBtn;
    TextView decryptedDataTV;
    ImageView imageDisplayIV;

    private ProgressDialog progressDialog;
    private StegocryptoHardware bluetooth;
    private byte[] stegoTaskResult = null;
    private boolean stegoTaskDone = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_decryption);

        decryptedBtn = (Button) findViewById(R.id.decryptedBtn);
        decryptedDataTV = (TextView) findViewById(R.id.decrypted_data);
        imageDisplayIV = (ImageView) findViewById(R.id.imageDisplay);

        decryptedBtn.setOnClickListener(this);

        bluetooth = new StegocryptoHardware();
        progressDialog = new ProgressDialog(this);

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
                 /* Get the image data */

                /* TODO: get real image data. For now, import from hardcoded file */
                //String rootDir = Environment.getExternalStorageDirectory().toString();
                //byte[] bytes = ImageUtility.readFromFile(rootDir + "/stegoCrypto1.bmp");
                // Retrieve bytes from current image from display
                imageDisplayIV.setDrawingCacheEnabled(true);
                imageDisplayIV.buildDrawingCache();
                Bitmap imageBitmap = view.getDrawingCache();
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] bytes = stream.toByteArray();

               // Bitmap bitmap = ((BitmapDrawable)imageDisplayIV.getDrawable()).getBitmap();
               // Bitmap resizedBitmap = ImageUtility.getResizedBitmap(bitmap, ImageUtility.MAX_IMAGE_SIZE);
               // resizedBitmap = ImageUtility.getResizedBitmap(bitmap, 50);
                try {
                    //ByteBuffer imagedatabb = ImageUtility.save(resizedBitmap, "current.bmp");

                    if (false) {
                    //if (imagedatabb == null) {
                        Log.e("Decryption", "Bitmap was NULL!");
                    } else {
                        //byte[] imgbyte = imagedatabb.array();
                        //Log.e("Decryption", "Bitmap has size " + imgbyte.length + " bytes");

                        /* Send the data to the hardware */
                        //new StegoCryptoDecrypt().execute(imgbyte);
                        new StegoCryptoDecrypt().execute(bytes);
                    }
                } catch (Exception e) {
                    Log.e("Decryption", "Could not convert bitmap");
                }
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

    private class StegoCryptoDecrypt extends AsyncTask<byte[], Integer, byte[]> {

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

            Log.i("Bluetooth", "Selecting encryption: ");
            bluetooth.selectOption(StegocryptoHardware.OPT.OPT_DECRYPT);

            Log.i("Bluetooth", "Sending image data");
            bluetooth.sendToHardware(bytes[0]);
            Log.i("Bluetooth", "Done sending image data");

            /* We need a longer timeout here to give DE2 some time to decrypt */
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
            progressDialog.setMessage(getString(R.string.loadingEncryption));
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected void onPostExecute(byte[] result) {
            stegoTaskResult = result;

            if (result != null) {
            /* Clean the string output */
                String decrypted = new String(result, 0, result.length);
                int indexOfNulls = decrypted.indexOf('\0');
                if (indexOfNulls > 0 && indexOfNulls < decrypted.length())
                    decrypted = decrypted.substring(0, indexOfNulls);

                decryptedDataTV.setText(decrypted);
            } else {
                decryptedDataTV.setText("(Error decrypting)");
            }

            stegoTaskDone = true;
            progressDialog.dismiss();
        }
    }
}
