package cpen391_21.stegocrypto.StegocryptoHardware;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class StegocryptoHardware {
    private static String TAG = "StegocryptoHardware";

    /* Hardcoding our BT dongle's MAC address */
    private static final String BLUETOOTH_MAC_ADDR = "00:06:66:6C:A7:A5";

    /* SPP UUID service - this should work for most devices */
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    /* Used to identify handler message */
    final int handlerState = 0;
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private StringBuilder recDataString = new StringBuilder();
    Handler bluetoothIn;
    private ConnectedThread mConnectedThread;

    private final Context baseContext;
    private final Activity activity;

    public StegocryptoHardware(Activity activity, Context baseContext) {
        this.baseContext = baseContext;
        this.activity = activity;
        bluetoothIn = new BroadcastHandler();
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        checkBTState();
    }

    /**
     * Call this in fini
     */
    public void fini() {
        try
        {
            /* If you close the socket too quickly after sending data, you'll hang the Bluetooth */
            Thread.sleep(1000);
            /* Don't leave Bluetooth sockets open when leaving activity */
            btSocket.close();
            Log.i(TAG, "Successfully closed");
        } catch (IOException e2) {
            Log.e(TAG, "Error closing bluetooth socket in fini: " + e2.getMessage());
        } catch (InterruptedException e) {
            Log.e(TAG, "Thread sleep interrupted");
        }
    }

    /**
     * Call this in onResume
     *
     * This actually establishes the socket connection with the hardware Bluetooth
     */
    public void init() {
        /* Create remote device and set the remote MAC address */
        BluetoothDevice device = btAdapter.getRemoteDevice(BLUETOOTH_MAC_ADDR);

        /* Try to establish a connection */
        try {
            btSocket = device.createRfcommSocketToServiceRecord(BTMODULEUUID);
            btSocket.connect();
            Log.i(TAG, "Successfully created Bluetooth socket");
        } catch (IOException e) {
            Log.e(TAG, "Error creating Bluetooth socket: " + e.getMessage());
            Toast.makeText(baseContext, "Socket creation failed", Toast.LENGTH_SHORT);
        }

        mConnectedThread = new ConnectedThread(btSocket);
        mConnectedThread.start();

        Log.i(TAG, "Connected to Bluetooth");
    }

    public void sendToHardware(String msg) {
        mConnectedThread.write(msg);
        try { Thread.sleep(1); } catch (Exception e) {};
    }

    public String receiveFromHardware() {
        // TODO: implement me!
        return "";
    }


    /**
     * Checks that the Android device Bluetooth is available and prompts to be turned on if off
     */
    private void checkBTState() {
        if (btAdapter == null) {
            Toast.makeText(baseContext, "Device does not support bluetooth", Toast.LENGTH_LONG).show();
            Log.e(TAG, "Device doesn't support Bluetooth???");
        } else {
            if (btAdapter.isEnabled()) {
                Log.v(TAG, "BT Device is enabled");
            } else {
                Log.v(TAG, "Requesting enable...");
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                activity.startActivityForResult(enableBtIntent, 1);
            }
        }
    }

    private class ConnectedThread extends Thread {
        private final BufferedInputStream mmInStream;
        private final BufferedOutputStream mmOutStream;


        //creation of the connect thread
        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                //Create I/O streams for connection
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
                Log.i(TAG, "Successfully got input/output streams for socket");
            } catch (IOException e) {
                Log.e(TAG, "Error getting input/output streams for socket: " + e.getMessage());
            }

            mmInStream = new BufferedInputStream(tmpIn);
            mmOutStream = new BufferedOutputStream(tmpOut, 8);
        }

        public void run() {
            byte[] buffer = new byte[256];

            int bytes = 0;

            // Keep looping to listen for received messages
            while (true) {
                try {
                    StringBuilder read = new StringBuilder();

                    try { Thread.sleep(1); } catch (Exception e) {};

                    //while (mmInStream.available() > 0) {
                        /* read bytes from input buffer */
                        bytes = mmInStream.read(buffer);
                        Log.e("Read Message", "Read " + Integer.toString(bytes) + "bytes");
                        read.append(new String(buffer, 0, bytes));
                    //}


                    //String readMessage = new String(buffer, 0, bytes);
                    String readMessage = read.toString();

                    // Send the obtained bytes to the UI Activity via handler
                    Log.e("Read Message", "Message: " + readMessage);

                    bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }

        /**
         * Send a string over Bluetooth
         * @param input
         */
        public void write(String input) {
            /* Convert String into bytes */
            byte[] msgBuffer = input.getBytes();
            write(msgBuffer);
        }

        /**
         * Send bytes over Bluetooth
         * @param msgBuffer
         */
        public void write(byte[] msgBuffer) {
            try {
                int offset = 0;
                /* Write bytes over BT connection via outstream */
                /* It seems that a rate of 8 bytes then a sleep prevents us from overloading poor DE2 */
                while (offset < msgBuffer.length) {
                    mmOutStream.write(msgBuffer, offset, (offset + 16 < msgBuffer.length ? 16 : msgBuffer.length - offset));
                    Thread.sleep(250);
                    offset += 16;
                }
                //mmOutStream.flush();

                Log.i(TAG, "Sent message!");
            } catch (IOException e) {
                Toast.makeText(baseContext, "Connection Failure", Toast.LENGTH_LONG).show();
                Log.e(TAG, "Bluetooth connection failure on write");
            } catch (InterruptedException e) {
                Log.e(TAG, "Interrupted exception: " + e.getMessage());
            }
        }
    }

    private class BroadcastHandler extends Handler {

        public void handleMessage(android.os.Message msg) {
            /* Check if this message is what we want */
            if (msg.what == handlerState) {
                /* msg.arg1 = bytes from connectThread */
                String readMessage = (String) msg.obj;
                Log.e(TAG, "Received BTMSG " + readMessage);
            }
        }
    }
}
