package cpen391_21.stegocrypto.ServerRequests;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

import cpen391_21.stegocrypto.DataTransferHistory.DataTransferHistoryArrayAdapter;
import cpen391_21.stegocrypto.DataTransferHistory.DataTransferHistoryItem;
import cpen391_21.stegocrypto.User.User;
import cpen391_21.stegocrypto.User.UserLocalStore;

public class DataTransferHistoryRequests {
    ProgressDialog progressDialog;
    Context context;

    public DataTransferHistoryRequests(Context context) {
        this.context = context;

        progressDialog = new ProgressDialog(context);
        progressDialog.setCancelable(false);
    }

    public void retrieveHistoryAsyncTask(String requestURL, ArrayList<DataTransferHistoryItem> historyArrayList,
                                         DataTransferHistoryArrayAdapter arrayAdapter, String currLoginUsername) {
        progressDialog.setTitle("Retrieving history from server...");
        progressDialog.setMessage("Please wait...");
        progressDialog.show();
        new RetrieveHistoryAsyncTask(historyArrayList, arrayAdapter, currLoginUsername).execute(requestURL);
    }

    public class RetrieveHistoryAsyncTask extends AsyncTask<String, Void, Void> {
        String currLoginUsername;

        // this is the list we are populating with the result from this call
        ArrayList<DataTransferHistoryItem> historyArrayList;

        // notify this adapter after historyArrayList is populated
        DataTransferHistoryArrayAdapter arrayAdapter;

        public RetrieveHistoryAsyncTask(ArrayList<DataTransferHistoryItem> historyArrayList,
                                        DataTransferHistoryArrayAdapter arrayAdapter, String currLoginUsername){
            this.historyArrayList = historyArrayList;
            this.arrayAdapter = arrayAdapter;
            this.currLoginUsername = currLoginUsername;
        }


        @Override
        protected Void doInBackground(String... requestURL) {
            String rawResponse = HTTPCommands.performGetCall(requestURL[0]);

            // result is an array of JSON objects
            try {
                JSONArray parsedArray = new JSONArray(rawResponse);

                for (int i = 0; i < parsedArray.length(); i++) {
                    JSONObject JSONHistoryItem = parsedArray.getJSONObject(i);
                    DataTransferHistoryItem dataTransHistoryItem;

                    String toUserName = JSONHistoryItem.getString("toUserName");
                    String fromUserName = JSONHistoryItem.getString("fromUserName");
                    String packageID = JSONHistoryItem.getString("packageID");
                    String date = JSONHistoryItem.getString("date").replace("GMT+0000", "");

                    // message was sent from current user to anther user
                    if (currLoginUsername.equals(fromUserName)) {
                        dataTransHistoryItem = new DataTransferHistoryItem("TO: ", toUserName, date, packageID);

                        // data transfer was from another user to current user
                    } else {
                        dataTransHistoryItem = new DataTransferHistoryItem("FROM: ", fromUserName, date, packageID);
                    }

                    historyArrayList.add(dataTransHistoryItem);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            arrayAdapter.notifyDataSetChanged();
            progressDialog.dismiss();
        }


    }

}
