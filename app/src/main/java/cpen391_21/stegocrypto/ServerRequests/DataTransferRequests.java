package cpen391_21.stegocrypto.ServerRequests;


import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Base64;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import cpen391_21.stegocrypto.Decryption;
import cpen391_21.stegocrypto.User.User;

public class DataTransferRequests {
    ProgressDialog progressDialog;
    Context context;

    public DataTransferRequests(Context context) {
        this.context = context;

        progressDialog = new ProgressDialog(context);
        progressDialog.setCancelable(false);
    }

    public void sendDataAsyncTask(HashMap<String, String> params) {
        progressDialog.setTitle("Sending data to server...");
        progressDialog.setMessage("Please wait...");
        progressDialog.show();
        new SendDataAsyncTask().execute(params);
    }

    public void retrieveDataAsyncTask(String url, ImageView imageView, Decryption.byteBufContainer resultBuffer){
        progressDialog.setTitle("Retrieving data from server...");
        progressDialog.setMessage("Please wait...");
        progressDialog.show();
        new RetrieveDataAsyncTask(imageView, resultBuffer).execute(url);
    }

    public class SendDataAsyncTask extends AsyncTask<HashMap<String, String>, Void, String> {

        @Override
        protected String doInBackground(HashMap<String, String>... params) {
            String sendDataURL = HTTPCommands.SERVER_URL + "sendData";

            String rawResponse = HTTPCommands.performPostCall(sendDataURL, params[0]);

            String decodedResponse ="";
            try {
                decodedResponse = (new JSONObject(rawResponse)).getString("response");
            } catch (JSONException e) {e.printStackTrace();}

            return decodedResponse;
        }

        @Override
        protected void onPostExecute(String result) {
            progressDialog.dismiss();

            Toast toast;
            if (result.equals("Data sent sucessfully.")) {
                toast = Toast.makeText(context, "Data has been sent.", Toast.LENGTH_SHORT);
            } else {
                toast = Toast.makeText(context, "Unable to sent data.", Toast.LENGTH_SHORT);
            }

            toast.show();
        }
    }

    public class RetrieveDataAsyncTask extends AsyncTask<String, Void, String> {
        ImageView imageView;
        Decryption.byteBufContainer resultBuffer;

        public RetrieveDataAsyncTask(ImageView imageView, Decryption.byteBufContainer resultBuffer){
            this.resultBuffer = resultBuffer;
            this.imageView = imageView;
        }

        @Override
        protected String doInBackground(String... url) {
            String rawResponse = HTTPCommands.performGetCall(url[0]);
            return rawResponse;
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                JSONObject jsonData = new JSONObject(result);
                String bae64Image = jsonData.getString("data");

                byte[] decodedBytes = Base64.decode(bae64Image, Base64.DEFAULT);

                if (resultBuffer != null)
                    resultBuffer.buf = decodedBytes;

                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, Base64.DEFAULT, decodedBytes.length);

                imageView.setImageBitmap(bitmap);
            } catch (JSONException e) {
                Toast.makeText(context, "Unable to retrive image from server", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }

            progressDialog.dismiss();
        }
    }
}

