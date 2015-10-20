package com.nju.ecg.wave;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

import com.nju.ecg.service.EcgApp;
import com.nju.ecg.socket.SocketClient;
import com.nju.ecg.utils.LogUtil;

/**
 * 数据存储
 * @author zhuhf
 * @version [ME MTVClient_Handset V100R001C04SPC002, 2012-9-16]
 */
public class EcgSaveData {
    private static final String TAG = "EcgSaveData";
    private static final boolean LOGD = true;


    private static boolean mReady = false;
    private static SaveDataThread mLoggerThread;

    private static BroadcastReceiver mIntentReceiver = null;
    private static volatile boolean mSDCardReay = false;
    private static volatile boolean mGotBootComplete = false;
    private static final int FREE_SPACE_LIMITE = 5*1024*1024; //5MB
    private static SocketClient socketClient;

    public static void init(String fileName) {
        mSDCardReay = isSDCardMounted();
        if (LOGD) Log.d(TAG, "isSDCardMounted " + mSDCardReay);
        if (mSDCardReay && isSDCardFull()) {
            return;
        }
        if (mLoggerThread != null)
        {
            destroy();
        }
        mLoggerThread = new SaveDataThread();
        mLoggerThread.prepareDataFile(fileName);
        mLoggerThread.start();
        
//        mIntentReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            String action = intent.getAction();
//            if (action == null) {
//                return;
//            }
//            String dataStr = intent.getDataString();
//            if (dataStr != null
//                && dataStr.equals("file:///mnt/sdcard")) {//We care internal SD card only
//                if (action.equals(Intent.ACTION_MEDIA_MOUNTED)) {
//                    if (isSDCardFull()) {
//                        destroy();
//                        return;
//                    }
//                    mSDCardReay = true;
//                    synchronized(mLoggerThread) {
//                        mLoggerThread.notify();
//                    }
//                } else if (action.equals(Intent.ACTION_MEDIA_UNMOUNTED)) {
//                    mSDCardReay = false;
//                }
//            }
//        }
//        };
//
//        IntentFilter filter = new IntentFilter();
//        filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
//        filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
//        filter.addDataScheme("file");

        mReady = true;
        SharedPreferences sp = EcgApp.getInstance().getApplicationContext().getSharedPreferences(TAG, EcgApp.getInstance().getApplicationContext().MODE_PRIVATE);
    	String userName = sp.getString("UserName", "");
    	String password = sp.getString("Password", "");
//        socketClient = new SocketClient(userName, password, null);
//        socketClient.login(fileName, true);
    }

    public static void destroy() {
        if (mReady) {
            mReady = false;
            mLoggerThread.close();
            mLoggerThread = null;
            mIntentReceiver = null;
//            socketClient.close();
        }
    }

    public static void saveData(String msg) {
        if (mReady) {
            mLoggerThread.Log(msg);
        }
    }
	
    public static void saveData(byte[] msg) {
        if (mReady) {
            mLoggerThread.Log(msg);
        }
    }
    
    public static void clearTempData()
    {
    	if (mLoggerThread != null)
    	{
    		mLoggerThread.clear();
    	}
    }
    
	    public static void onBootComplete() {
	        //Create new log file only when we got boot complete
	        if (LOGD) Log.d(TAG, "onBootComplete");
	        mGotBootComplete = true;
	    }
	
	    private static boolean isSDCardMounted() {
	        String state = Environment.getExternalStorageState();
	        Log.v(TAG, "storage state is " + state);
	        return Environment.MEDIA_MOUNTED.equals(state);
	    }
	
	    private static boolean isSDCardFull() {
	        Log.d(TAG, "ext path" + Environment.getExternalStorageDirectory().getPath());
	        StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
	        long bytesAvailable = (long)stat.getBlockSize() *(long)stat.getBlockCount();
	        Log.v(TAG, "Free bytes " + bytesAvailable);
	        return bytesAvailable < FREE_SPACE_LIMITE;
	    }
	
	
	
	    private static class SaveDataThread extends Thread {
	        private static final int LOG_OK = 0;
	        private static final int LOG_FAILED_SDCARD_NOT_READY = 1;
	        private static final int LOG_FAILED = -1;
	
	        private boolean closed = false;
	        //private FileWriter mFileWriter = null;
	        private FileOutputStream mFileWriter= null;
	        //private LinkedList<String> mLogBuffer = new LinkedList<String>();
	        private List<byte[]> mData = new ArrayList<byte[]>();
	        public synchronized void close() {
	            closed = true;
	            notify();
	        }

	        public synchronized void Log(String msg) {
	          /*  synchronized (mLogBuffer) {
	                mLogBuffer.add(msg);
	            }
	            notify();*/
	        }
	        
	        public synchronized void Log(byte[] msg) {
	            synchronized (mData) {
	                byte[] data = new byte[msg.length];
	                System.arraycopy(msg, 0, data, 0, msg.length);
	            	mData.add(data);
	            }        	
	            notify();
	        }
	        
	        public void clear()
	        {
	            synchronized (mData)
                {
                    mData.clear();
                }
	        }
	        
	        public void run() {
	            while (true) {
	                byte[] item = null;
	                synchronized (mData) {
	                    if (mData.size() > 0)
	                    {
	                        item = mData.get(0);
	                    }
	                }
	
	                if (item == null) {
	                    //No log item, wait
	                    synchronized (this) {
	                        try {
	                            wait();
	                        } catch (InterruptedException e) {
	                            LogUtil.e(TAG, e);
	                        }
	                    }
	                } else {
	                    switch(saveData2File(item)) {
	                    case LOG_OK:
//	                    	try {
//								socketClient.send(item, item.length);
//							} catch (IOException e1) {
//								e1.printStackTrace();
//							}
	                        //Only removes when the log item is successfully write to file
	                        if (mData.size() > 0)
	                        {
	                            mData.remove(0);
	                        }
	                        break;
	                    case LOG_FAILED_SDCARD_NOT_READY:
	                        // If sdcard is not yet ready, need to wait until it's ready
	                        synchronized (this) {
	                        try {
	                            wait();
	                        } catch (InterruptedException e) {
	                            LogUtil.e(TAG, e);
	                        }
	                        }
	                        break;
	                    case LOG_FAILED:
	                        //Try again later
	                        synchronized (this) {
	                        try {
	                            wait(3000);
	                        } catch (InterruptedException e) {
	                            LogUtil.e(TAG, e);
	                        }
	                        }
	                        break;
	                    }
	                }
	                synchronized (this) {
	                    if (closed) {
	                        if (LOGD) Log.d(TAG, "closed");
	                        break;
	                    }
	                }
	            }
	            if (mFileWriter != null) {
	                try {
	                    mFileWriter.close();
	                } catch (Exception e) {
	                    LogUtil.e(TAG, e);
	                }
	            }
	
	        }
       
	        private int saveData2File(byte[] msg) {
	            if (!mSDCardReay) {
	                if (LOGD) Log.d(TAG, "out for SDCard is not Reay");
	                return LOG_FAILED_SDCARD_NOT_READY;
	            }
	
	            if (mFileWriter == null) {
	               // if (!prepareDataFile()) {
	                    return LOG_FAILED;
	               // }
	            }
	
	            try {
	                mFileWriter.write(msg);
	                mFileWriter.flush();
	            } catch (Exception e) {
	                LogUtil.e(TAG, e);
	                return LOG_FAILED;
	            }
	
	            return LOG_OK;
	        }
	
	        private boolean prepareDataFile(String fileName) {
	            
	            String filePath = EcgApp.getInstance().getEcgBinder().createFileForSaveData(fileName);
	            if(fileName == null) return false;
	            
	            try {
	                //mFileWriter = new FileWriter(COLLECT_DATA_FILE, true);
	            	 mFileWriter=new FileOutputStream(filePath);
	            } catch (Exception e) {
	                LogUtil.e(TAG, e);
	                return false;
	            }
	            return true;
	        }
	    }
}
