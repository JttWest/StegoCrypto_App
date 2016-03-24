package cpen391_21.stegocrypto.ServerRequests;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import cpen391_21.stegocrypto.ServerRequests.HTTPCommands;
import cpen391_21.stegocrypto.User.User;
import cpen391_21.stegocrypto.User.GetUserCallback;

public class UserAccountRequests {
    ProgressDialog progressDialog;
    Context context;
    public static final String SERVER_ADDRESS = "https://stegocrypto-server.herokuapp.com/";

    public UserAccountRequests(Context context) {
        this.context = context;

        progressDialog = new ProgressDialog(context);
        progressDialog.setCancelable(false);
        progressDialog.setTitle("Processing...");
        progressDialog.setMessage("Please wait...");
    }

    public void storeUserDataInBackground(User user, GetUserCallback userCallBack) {
        progressDialog.show();
        new StoreUserDataAsyncTask(user, userCallBack).execute();
    }

    
    public void fetchUserDataAsyncTask(User user, GetUserCallback userCallBack) {
        progressDialog.show();
        new FetchUserDataAsyncTask(user, userCallBack).execute();
    }
    

    /**
     * sends data to our server to register new user
     */
    public class StoreUserDataAsyncTask extends AsyncTask<Void, Void, Void> {
        User user;
        GetUserCallback userCallBack;

        public StoreUserDataAsyncTask(User user, GetUserCallback userCallBack) {
            this.user = user;
            this.userCallBack = userCallBack;
        }

        @Override
        protected Void doInBackground(Void... params) {
            RequestQueue queue = Volley.newRequestQueue(context);
            String postDataURL = SERVER_ADDRESS + "register";

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
                    params.put("userName", Uri.encode(user.getUserName()));
                    params.put("password", Uri.encode(user.getPassword()));
                    //params.put("instanceIDToken", Uri.encode(user.instanceIDToken));
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
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            progressDialog.dismiss();
            userCallBack.done(null);
        }

    }

    
    public class FetchUserDataAsyncTask extends AsyncTask<Void, Void, User> {
        User user;
        GetUserCallback userCallBack;

        public FetchUserDataAsyncTask(User user, GetUserCallback userCallBack) {
            this.user = user;
            this.userCallBack = userCallBack;
        }


        @Override
        protected User doInBackground(Void... params) {
            User returnedUser = null;
            String loginURL = SERVER_ADDRESS + "login";

            HashMap<String, String> loginParams = new HashMap<String, String>();
            loginParams.put("userName", Uri.encode(user.getUserName()));
            loginParams.put("password", Uri.encode(user.getPassword()));

            String rawResponse = HTTPCommands.performPostCall(loginURL, loginParams);

            try {
                String decodedReponse = (new JSONObject(rawResponse)).getString("response");
                if (decodedReponse.equals("Sucessfully login."))
                    returnedUser = new User(user.getUserName(), user.getPassword());
            } catch (JSONException e) {e.printStackTrace();}


            /*
            RequestQueue queue = Volley.newRequestQueue(context);
            String postDataURL = SERVER_ADDRESS + "login";

            StringRequest sr = new StringRequest(Request.Method.POST, postDataURL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    updateUserIfSuccess(response);
                    Log.v("StegoCrypto", "Response from server: " + response);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.v("StegoCrypto", "Login POST data failed: " + error.getMessage());
                }
            }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("userName", Uri.encode(user.getUserName()));
                    params.put("password", Uri.encode(user.getPassword()));
                    //params.put("instanceIDTokens", Uri.encode(user.instanceIDTokens));
                    return params;
                }

                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("Content-Type", "application/x-www-form-urlencoded");
                    return params;
                }
            };
            queue.add(sr);
            */
            return returnedUser;
        }

        @Override
        protected void onPostExecute(User returnedUser) {
            super.onPostExecute(returnedUser);
            progressDialog.dismiss();
            userCallBack.done(returnedUser);
        }

    }

}
