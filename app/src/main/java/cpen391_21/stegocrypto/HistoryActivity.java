package cpen391_21.stegocrypto;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cpen391_21.stegocrypto.DataTransferHistory.DataTransferHistoryArrayAdapter;
import cpen391_21.stegocrypto.DataTransferHistory.DataTransferHistoryItem;
import cpen391_21.stegocrypto.ServerRequests.DataTransferHistoryRequests;
import cpen391_21.stegocrypto.ServerRequests.DataTransferRequests;
import cpen391_21.stegocrypto.ServerRequests.HTTPCommands;
import cpen391_21.stegocrypto.User.UserLocalStore;

public class HistoryActivity extends AppCompatActivity {
    private DataTransferHistoryArrayAdapter arrayAdapter;
    private Decryption.byteBufContainer resultByteBuffer;

    // need to populate this with call to server at run time
    private ArrayList<DataTransferHistoryItem> historyArrayList = new ArrayList<DataTransferHistoryItem>();

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        arrayAdapter = new DataTransferHistoryArrayAdapter(this, android.R.layout.simple_list_item_1, historyArrayList);

        ListView historyListView = (ListView) findViewById(R.id.historyList);

        historyListView.setAdapter(arrayAdapter);

        historyListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                DataTransferHistoryItem historyItem = historyArrayList.get(position);
                String packageID = historyItem.dataPackageID;


                AlertDialog.Builder adb = new AlertDialog.Builder(HistoryActivity.this);
                adb.setTitle("Image " + historyItem.action.toLowerCase() + historyItem.username);
                //adb.setMessage(" selected Item is=" + parent.getItemAtPosition(position));
                adb.setPositiveButton("Ok", null);

                LinearLayout imageDialogLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.image_dialog, null);
                ImageView dialogImage = (ImageView) imageDialogLayout.findViewById(R.id.dialogPopupImage);

                DataTransferRequests dataTransferRequests = new DataTransferRequests(HistoryActivity.this);
                dataTransferRequests.retrieveDataAsyncTask(HTTPCommands.SERVER_URL + "retrieveDataFromPackage?packageID=" +
                        packageID, dialogImage, resultByteBuffer);

                adb.setView(imageDialogLayout);
                adb.show();
            }
        });

            // get ID of current user
            SharedPreferences userSP = getSharedPreferences(UserLocalStore.USER_LOCAL_STORE_SP_NAME,
                    Context.MODE_PRIVATE);
            String currentLoginUserName = userSP.getString("userName", "");

            // populate ListView with data transfer history
            DataTransferHistoryRequests getDataTransferHistory = new DataTransferHistoryRequests(this);
            getDataTransferHistory.retrieveHistoryAsyncTask(HTTPCommands.SERVER_URL +
                            "getDataTransferHistory?username=" + currentLoginUserName,
                    historyArrayList, arrayAdapter, currentLoginUserName);
        }
    }
