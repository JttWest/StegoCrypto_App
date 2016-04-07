package cpen391_21.stegocrypto.GCM;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;

import org.json.JSONException;
import org.json.JSONObject;

import cpen391_21.stegocrypto.Decryption;
import cpen391_21.stegocrypto.R;
import cpen391_21.stegocrypto.User.UserLocalStore;

public class MyGcmListenerService extends GcmListenerService {

    private static final String TAG = "StegoCrpyto-GCM";

    /**
     * Called when message is received.
     *
     * @param from SenderID of the sender.
     * @param data Data bundle containing message data as key/value pairs.
     *             For Set of keys use data.keySet().
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(String from, Bundle data) {

        SharedPreferences userLocalStore = getSharedPreferences(UserLocalStore.USER_LOCAL_STORE_SP_NAME,
                                                                Context.MODE_PRIVATE);


        String message = data.getString("data_package");

        // instanceTokenID could get out of sync due to network failure
        // make sure the incoming data is intended for the correct user
        try {
            JSONObject JsonMsg = new JSONObject(message);
            String toUserName = JsonMsg.getString("toUserName");

            String currentLoginUserName = userLocalStore.getString("userName", "");

            // ignore this message if current login user is not intended recipient
            if (!toUserName.equals(currentLoginUserName)) {
                // send request to server to remove instaceIDToken of this device from toUser account
                return;
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }


        Log.d(TAG, "From: " + from);
        Log.d(TAG, "Message: " + message);

        if (from.startsWith("/topics/")) {
            // message received from some topic.
        } else {
            // normal downstream message.
        }

        // [START_EXCLUDE]
        /**
         * Production applications would usually process the message here.
         * Eg: - Syncing with server.
         *     - Store message in local database.
         *     - Update UI.
         */

        /**
         * In some cases it may be useful to show a notification indicating to the user
         * that a message was received.
         */
        sendNotification(message);
        // [END_EXCLUDE]
    }
    // [END receive_message]

    /**
     * Create and show a simple notification containing the received GCM message.
     *
     * @param message GCM message received.
     */
    private void sendNotification(String message) {
        Intent intent = new Intent(this, Decryption.class);

        intent.putExtra("data", message);

        String msgFromUsername = "";
        try{
            JSONObject parsedMsg = new JSONObject(message);
            msgFromUsername = parsedMsg.getString("fromUserName");

        } catch (JSONException e) {e.printStackTrace();}

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);


        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.stegography_logo)
                .setContentTitle("StegoCrpyto")
                .setContentText("You received an encryptic image from " + msgFromUsername)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }
}

