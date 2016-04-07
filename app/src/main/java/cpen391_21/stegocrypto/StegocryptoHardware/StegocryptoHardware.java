package cpen391_21.stegocrypto.StegocryptoHardware;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
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

    /* Bluetooth functions */
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private StegocryptoDataProtocol stegocryptoProtocol;

    /* Hold the calling Activity in case we need to make Toasts or intents */
    private final Activity activity;

    public StegocryptoHardware(Activity activity) {
        this.activity = activity;
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
            stegocryptoProtocol.close();
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
            Toast.makeText(activity.getBaseContext(), "Socket creation failed", Toast.LENGTH_SHORT);
        }

        stegocryptoProtocol = new StegocryptoDataProtocol(btSocket);

        Log.i(TAG, "Connected to Bluetooth");
    }

    public boolean sendToHardware(String msg) {
        Log.v(TAG, "Trying to send message <" + msg + "> to hardware...");

        Toast.makeText(activity.getBaseContext(), "Sending data to Stegocrypto Hardware...", Toast.LENGTH_SHORT).show();

        /* Initial handshake: Send S */
        stegocryptoProtocol.write("S");

        /* Receive ACK */
        byte[] data = stegocryptoProtocol.read(1);
        if (data[0] != 'R') {
            Log.e(TAG, "Error: Incorrect ACK on initial handshake");
            return false;
        }

        /* Send length of length string */
        String length = Integer.toString(msg.length());
        String lengthOfLength = Integer.toString(length.length());
        if (lengthOfLength.length() > 1) {
            Log.e(TAG, "Error. Send limit size exceeded");
            return false;
        }
        stegocryptoProtocol.write(lengthOfLength);

        /* Receive ACK */
        data = stegocryptoProtocol.read(1);
        if (data[0] != 'R') {
            Log.e(TAG, "Error: Incorrect ACK on size of length size");
            return false;
        }

        /* Send length */
        stegocryptoProtocol.write(length);

        /* Receive ACK */
        data = stegocryptoProtocol.read(1);
        if (data[0] != 'R') {
            Log.e(TAG, "Error: Incorrect ACK on length size");
            return false;
        }

        /* Send message */
        stegocryptoProtocol.write(msg);
        try { Thread.sleep(1); } catch (Exception e) {};

        return true;
    }

    public byte[] receiveFromHardware() {
        Log.v(TAG, "Trying to recv message from hardware...");

        Toast.makeText(activity.getBaseContext(), "Receiving data to Stegocrypto Hardware...", Toast.LENGTH_SHORT).show();

        /* Initial handshake: Send S */
        byte[] data = stegocryptoProtocol.read(1);
        if (data[0] != 'S') {
            Log.e(TAG, "Error: Incorrect ACK on length size");
            return new byte[0];
        }

        /* Initial handshake: Send R */
        stegocryptoProtocol.write("R");

        /* Receive length of length string */
        data = stegocryptoProtocol.read(1);
        int lengthOfLength = Integer.valueOf(new String(data, 0, data.length));

        /* Send ACK: R */
        stegocryptoProtocol.write("R");

        /* Receive length of data */
        data = stegocryptoProtocol.read(lengthOfLength);
        int datalength = Integer.valueOf(new String(data, 0, data.length));

        /* Send ACK: R */
        stegocryptoProtocol.write("R");

        /* Receive data */
        data = stegocryptoProtocol.read(datalength);
        return data;
    }


    /**
     * Checks that the Android device Bluetooth is available and prompts to be turned on if off
     */
    private void checkBTState() {
        if (btAdapter == null) {
            Toast.makeText(activity.getBaseContext(), "Device does not support bluetooth", Toast.LENGTH_LONG).show();
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

    private class StegocryptoDataProtocol {
        private final BufferedInputStream mmInStream;
        private final BufferedOutputStream mmOutStream;

        /* These parameters throttle the sending rate to prevent overwhelming the DE2's lousy buffering rate */
        private static final int BUFFER_SIZE = 16;
        private static final int SLEEP_TIME = 250; /* milliseconds */

        public StegocryptoDataProtocol(BluetoothSocket socket) {
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
            mmOutStream = new BufferedOutputStream(tmpOut, BUFFER_SIZE);
        }

        /**
         * Cleanup is good
         */
        public void close() {
            try {
                mmInStream.close();
                mmOutStream.close();
            } catch (Exception e) {}
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
                    mmOutStream.write(msgBuffer, offset, (offset + BUFFER_SIZE < msgBuffer.length ? BUFFER_SIZE : msgBuffer.length - offset));
                    Thread.sleep(SLEEP_TIME);
                    offset += BUFFER_SIZE;
                }
                mmOutStream.flush();

                Log.v(TAG, "Sent " + msgBuffer.length + " bytes: " + new String(msgBuffer, 0, msgBuffer.length));
            } catch (IOException e) {
                Toast.makeText(activity.getBaseContext(), "Connection Failure", Toast.LENGTH_LONG).show();
                Log.e(TAG, "Bluetooth connection failure on write");
            } catch (InterruptedException e) {
                Log.e(TAG, "Interrupted exception: " + e.getMessage());
            }
        }

        public byte[] read(int numBytes) {
            byte[] buffer = new byte[numBytes];

            int bytes = 0;

            while (bytes < numBytes) {
            /* Keep looping to listen for received messages */
                try {
                /* read bytes from input buffer */
                    bytes += mmInStream.read(buffer, bytes, numBytes - bytes);
                    Log.v("Read Message", "Read " + Integer.toString(bytes) + "bytes");
                } catch (IOException e) {}
            }
            return buffer;
        }
    }
}
