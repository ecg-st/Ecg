package com.nju.ecg.bluetooth;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.nju.ecg.service.EcgApp;
import com.nju.ecg.utils.LogUtil;
import com.nju.ecg.wave.WaveScreen;

/**
 * 
 **/
public class BluetoothRfcommClient 
{
    private static final String TAG = "BluetoothRfcommClient";
    // Unique UUID for this application
    public static final UUID MY_UUID = 
//    	UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
    	UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
//        java.util.UUID.randomUUID();

    // Member fields
    private final BluetoothAdapter mAdapter;
    private final Handler mHandler;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private int mState;

    // Constants that indicate the current connection state
    public static final int STATE_NONE = 0;       // we're doing nothing
    //public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device
    
    private Handler reportHandler = new Handler();

    /**
     * Constructor. Prepares a new BluetoothChat session.
     * - context - The UI Activity Context
     * - handler - A Handler to send messages back to the UI Activity
     */
    public BluetoothRfcommClient(Context context, Handler handler) {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = STATE_NONE;
        mHandler = handler;
    }

    /**
     * Set the current state o
     * */
    private synchronized void setState(int state) {
        mState = state;

        // Give the new state to the Handler so the UI Activity can update
        mHandler.obtainMessage(WaveScreen.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
    }

    /**
     * Return the current connection state. */
    public synchronized int getState() {
        return mState;
    }

    /**
     * Start the Rfcomm client service. 
     * */
    public synchronized void start() {
        // Cancel any thread attempting to make a connection
        if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}
        
        setState(STATE_NONE);
    }

    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     * - device - The BluetoothDevice to connect
     */
    public synchronized void connect(BluetoothDevice device) {

        // Cancel any thread attempting to make a connection
        if (mState == STATE_CONNECTING) {
            if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}

        // Start the thread to connect with the given device
        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
        setState(STATE_CONNECTING);
    }

    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     * - socket - The BluetoothSocket on which the connection was made
     * - device - The BluetoothDevice that has been connected
     */
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {

        // Cancel the thread that completed the connection
        if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();

        // Send the name of the connected device back to the UI Activity
        Message msg = mHandler.obtainMessage(WaveScreen.MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(WaveScreen.DEVICE_NAME, device.getName());
        msg.setData(bundle);
        mHandler.sendMessage(msg);

        setState(STATE_CONNECTED);
    }

    /**
     * Stop all threads
     */
    public synchronized void stop() {
        if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}
        setState(STATE_NONE);
    }

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     * - out - The bytes to write - ConnectedThread#write(byte[])
     */
    public void write(byte[] out) {
        // Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (mState != STATE_CONNECTED) return;
            r = mConnectedThread;
        }
        // Perform the write unsynchronized
        r.write(out);
    }

    /**
     * Indicate that the connection attempt failed and notify the UI Activity.
     */
//    private void connectionFailed() {
//        setState(STATE_NONE);
//        // Send a failure message back to the Activity
//        Message msg = mHandler.obtainMessage(WaveScreen.MESSAGE_TOAST);
//        Bundle bundle = new Bundle();
//        bundle.putString(WaveScreen.TOAST, "无法连接该设备");
//        msg.setData(bundle);
//        mHandler.sendMessage(msg);
//    }

    /**
     * Indicate that the connection was lost and notify the UI Activity.
     */
    private void connectionLost() {
//        setState(STATE_NONE);
        // Send a failure message back to the Activity
        Message msg = mHandler.obtainMessage(WaveScreen.MESSAGE_RECONNECT);
//        Bundle bundle = new Bundle();
//        bundle.putString(WaveScreen.TOAST, "蓝牙设备连接丢失");
//        bundle.putBoolean(WaveScreen.CONNECTION_LOST, true);
//        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }

    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    private class ConnectThread extends Thread {
        private BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        public ConnectThread(BluetoothDevice device) {
            mmDevice = device;
            BluetoothSocket tmp = null;
            // Get a BluetoothSocket for a connection with the given BluetoothDevice
            try {
//                Method m= mmDevice.getClass().getMethod("createRfcommSocket", new Class[]{int.class});
//                tmp = (BluetoothSocket) m.invoke(mmDevice, 1);
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (Exception e) {
                LogUtil.e(TAG, e);
            }
            mmSocket = tmp;
        }

        public void run() {
            setName("ConnectThread");
            // Always cancel discovery because it will slow down a connection
            mAdapter.cancelDiscovery();
            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a  successful connection or an exception
                // 同步块防止正在连接时去关闭, 会导致程序崩溃(android.os.DeadObjectException)
                synchronized (mmSocket)
                {
                    mmSocket.connect();
                }
                // Reset the ConnectThread because we're done
                synchronized (BluetoothRfcommClient.this) {
                    mConnectThread = null;
                }
                // Start the connected thread
                connected(mmSocket, mmDevice);
            } catch (Exception e) {
                LogUtil.e(TAG, e);
//                connectionFailed();
                // Close the socket
//                try {
//                    synchronized (mmSocket)
//                    {
//                        if (mmSocket != null)
//                        {
//                            mmSocket.close();
//                        }
//                    }
//                } catch (Exception e2) {
//                    LogUtil.e(TAG, e2);
//                }
                // Start the service over to restart listening mode
                BluetoothRfcommClient.this.start();
                return;
            }
        }

        public void cancel() {
            try
            {
                synchronized (mmSocket)
                {
                    if (mmSocket != null)
                    {
                        mmSocket.close();
                        mmSocket = null;
                    }
                }
            }
            catch (Exception e)
            {
                LogUtil.e(TAG, e);
            }
        }
    }

    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */
    private class ConnectedThread extends Thread {
        private BluetoothSocket mmSocket;
        private InputStream mmInStream;
        private OutputStream mmOutStream;
        private boolean running = false;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            running = true;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
                
                // 唤醒蓝牙缓冲处理线程
                BtBufferProcesser.getInstatce().resumeThread();
                // 检测报告采集一分钟
                LogUtil.d(TAG, "延迟一分钟执行结束心率数据采集");
                reportHandler.postDelayed(reportRunnable, 1000 * 60);
            } catch (Exception e) {
                LogUtil.e(TAG, e);
            }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }
        
        private Runnable reportRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                LogUtil.d(TAG, "持续采集一分钟, 发出广播通知WaveScreen保存数据");
                Intent intent = new Intent();
                intent.setAction("com.nju.ecg.report.package");
                EcgApp.getInstance().sendBroadcast(intent);
            }
        };

        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;
            // Keep listening to the InputStream while connected
            while (running) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer, 0 , 512);
                    if (bytes != -1)
                    {
                        byte[] src = new byte[bytes];
                        System.arraycopy(buffer, 0, src, 0, bytes);
                        // 处理蓝牙数据
                        BtBufferProcesser.getInstatce().save(src);
                    }
                    Thread.sleep(10);
                } catch (Exception e) {
                    LogUtil.e(TAG, e);
                    if (running) // 正在连接时断开才去尝试重连
                    {
                        connectionLost();
                    }
                    break;
                }
            }
        }

        /**
         * Write to the connected OutStream.
         */
        public void write(byte[] buffer) {
            try {
                mmOutStream.write(buffer);
                // Share the sent message back to the UI Activity
                mHandler.obtainMessage(WaveScreen.MESSAGE_WRITE, -1, -1, buffer)
                        .sendToTarget();
            } catch (Exception e) {
                LogUtil.e(TAG, e);
            }
        }

        public void cancel() {
            running = false;
            if (reportHandler != null)
            {
                LogUtil.d(TAG, "移除一分钟延迟执行Runnable");
                reportHandler.removeCallbacks(reportRunnable);
            }
            try
            {
                synchronized (mmSocket)
                {
                    if (mmSocket != null)
                    {
                        mmSocket.close();
                        mmSocket = null;
                    }
                }
                if (mmInStream != null)
                {
                    mmInStream.close();
                    mmInStream = null;
                }
                if (mmOutStream != null)
                {
                    mmOutStream.close();
                    mmOutStream = null;
                }
            }
            catch (Exception e)
            {
                LogUtil.e(TAG, e);
            }
        }
    }
}
