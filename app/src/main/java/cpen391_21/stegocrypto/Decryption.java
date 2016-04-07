package cpen391_21.stegocrypto;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import cpen391_21.stegocrypto.ServerRequests.DataTransferRequests;
import cpen391_21.stegocrypto.ServerRequests.HTTPCommands;
import cpen391_21.stegocrypto.StegocryptoHardware.StegocryptoHardware;
import cpen391_21.stegocrypto.Utility.ImageUtility;


public class Decryption extends AppCompatActivity implements View.OnClickListener{
    Button decryptedBtn;
    Button browseImagesBtn;
    TextView decryptedDataTV;
    ImageView imageDisplayIV;

    private ProgressDialog progressDialog;
    private StegocryptoHardware bluetooth;
    private byte[] stegoTaskResult = null;
    private boolean stegoTaskDone = false;

    /* Request enums */
    final private static int PICK_IMAGE_REQUEST = 1;

    byteBufContainer resultByteBuffer = new byteBufContainer();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_decryption);

        decryptedBtn = (Button) findViewById(R.id.decryptedBtn);
        decryptedDataTV = (TextView) findViewById(R.id.decrypted_data);
        browseImagesBtn = (Button) findViewById(R.id.browseButton);
        imageDisplayIV = (ImageView) findViewById(R.id.imageDisplay);

        decryptedBtn.setOnClickListener(this);
        browseImagesBtn.setOnClickListener(this);

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
                /* Get the image data */
                imageDisplayIV.setDrawingCacheEnabled(true);
                imageDisplayIV.buildDrawingCache();
                Bitmap imageBitmap = imageDisplayIV.getDrawingCache();
                ByteArrayOutputStream stream = new ByteArrayOutputStream();

                if (imageBitmap == null) {
                    Log.e("Decryption", "imageBitmap was null");
                    Toast.makeText(getBaseContext(), "You must select an image", Toast.LENGTH_SHORT).show();
                    return;
                }

                imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] bytes = stream.toByteArray();

                Log.d("StegoByte",  Arrays.toString(resultByteBuffer.buf));

                Bitmap bitmap = ((BitmapDrawable)imageDisplayIV.getDrawable()).getBitmap();
                Bitmap resizedBitmap = ImageUtility.getResizedBitmap(bitmap, ImageUtility.MAX_IMAGE_SIZE);
                resizedBitmap = ImageUtility.getResizedBitmap(bitmap, 100);
                try {
                    //ByteBuffer imagedatabb = ImageUtility.bitmapToByteBuffer(resizedBitmap);


                /* Send the data to the hardware */
                    new StegoCryptoDecrypt().execute(resultByteBuffer.buf);
                    //new StegoCryptoDecrypt().execute(bytes);



                } catch (Exception e) {
                    Log.e("Decryption", "Could not convert bitmap");
                }
                break;
            case R.id.browseButton:
                Intent intent = new Intent();
                // Show only images, no videos or anything else
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                // Always show the chooser (if there are multiple options available)
                startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST);
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
            case PICK_IMAGE_REQUEST:
                Uri uri = data.getData();
                try {
                    Bitmap imageBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                    Log.v("StegoCrypto-Image", String.valueOf(imageBitmap));

                    imageDisplayIV.setImageBitmap(ImageUtility.getResizedBitmap(imageBitmap, ImageUtility.MAX_IMAGE_SIZE));
                } catch (IOException e) {e.printStackTrace();}
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
                    dataTransferRequests.retrieveDataAsyncTask(requestURL, imageDisplayIV, resultByteBuffer);
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
            publishProgress(1);

            Log.i("Bluetooth", "Sending image data");
            bluetooth.sendToHardware(bytes[0]);
            Log.i("Bluetooth", "Done sending image data");
            publishProgress(2);

            /* We need a longer timeout here to give DE2 some time to decrypt */
            byte[] ret = bluetooth.receiveFromHardware(10000);
            if (ret != null) {
                Log.i("Bluetooth", "Received: " + new String(ret, 0, ret.length));
                totalSize = ret.length;
            }
            publishProgress(3);

            try { Thread.sleep(1000); } catch (Exception e) {};
            bluetooth.fini();

            /* Save the data */
            /* Save the image first */
            // fetching the root directory
            String rootDir = Environment.getExternalStorageDirectory().toString();
            // Creating folders for Image
            String imageFolderPath = rootDir + "/StegoCryptoImages/";
            File imagesFolder = new File(imageFolderPath);
            imagesFolder.mkdirs(); // make directory if if doesn't exist
            // Generating file name
            String imageName = "stegoCryptoEncrypted.bmp";
            ImageUtility.writeToFile(imageFolderPath + imageName, bytes[0]);

            return ret;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            /* Show progressDialog */
            progressDialog.setTitle("Decrypting...");
            progressDialog.setMessage(getString(R.string.loadingEncryption));
            progressDialog.setIndeterminate(false);
            progressDialog.setMax(3);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected void onProgressUpdate(Integer... args) {
            super.onProgressUpdate();
            progressDialog.setProgress(args[0]);
            switch (args[0]) {
                case 1:
                    progressDialog.setMessage("Sending image data...");
                    break;
                case 2:
                    progressDialog.setMessage("Receiving image data...");
                    break;
                default:
                    break;
            }
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

    public class byteBufContainer{
        public byte[] buf;
        /*
        public byteBufContainer(byte[] buf){
            this.buf = buf;
        }*/

    }

}
