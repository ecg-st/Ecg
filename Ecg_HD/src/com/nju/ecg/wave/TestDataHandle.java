package com.nju.ecg.wave;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import android.os.Handler;
import android.os.Message;

import com.nju.ecg.service.EcgApp;
import com.nju.ecg.utils.LogUtil;

public class TestDataHandle extends Thread {
	private static final String TAG = "TestDataHandle";
	private boolean closed;
	private boolean paused = false;
	private boolean mIsFileOpened = false;
	private Handler handler = new Handler(new Handler.Callback() {
		@Override
		public boolean handleMessage(Message msg) {
			int progress = (int)(((float)totalBytes / fileLength) * 100);
			if (progress > 100)
			{
				progress = 100;
			}
			WaveScreen.seekBar.setProgress(progress);
			WaveScreen.timeTxt.setText(calculateTimeProgress());
			return false;
		}
	});
	
	private String calculateTimeProgress()
	{
		// 已经播放的秒数
		int playedSeconds = (int)(totalBytes / (1024 * 4));
		return calculateTimeProgress(playedSeconds) + " / " + calculateTimeProgress(seconds);
	}
	
	private String calculateTimeProgress(int seconds)
	{
		if (seconds < 60)
		{
			if (seconds < 10)
			{
				return "00:00:0" + seconds;
			}
			else
			{
				return "00:00:" + seconds;
			}
		}
		else if (seconds >= 60 && seconds < 60 * 60)
		{
			String str = null;
			if (seconds / 60 < 10)
			{
				str = "00:0" + seconds / 60;
			}
			else
			{
				str = "00:" + seconds / 60;
			}
			if (seconds % 60 < 10)
			{
				str += ":0" + seconds % 60;
			}
			else
			{
				str += ":" + seconds % 60;
			}
			return str;
		}
		else
		{
			String str = null;
			if (seconds / 3600 < 10)
			{
				str = "0" + seconds / 3600;
			}
			else
			{
				str = "" + seconds / 3600;
			}
			if ((seconds % 3600) / 60 < 10)
			{
				str += ":0" + (seconds % 3600) / 60;
			}
			else
			{
				str += ":" + (seconds % 3600) / 60;
			}
			if ((seconds % 3600) % 60 < 10)
			{
				str += ":0" + (seconds % 3600) % 60;
			}
			else
			{
				str += ":" + (seconds % 3600) % 60;
			}
			return str;
		}
	}
	 
	public TestDataHandle() {
		closed = false;
	}

	public TestDataHandle(String name) {
		closed = false;
		openFile(name);
	}
	
	
	
    public void run() {
        boolean hasData = mIsFileOpened;
        while (hasData) {
            if (paused)
            {
                try
                {
                    synchronized (this)
                    {
                        wait();
                    }
                }
                catch (InterruptedException e)
                {
                    LogUtil.e(TAG, e);
                }
            }
            try {                
                int random = 256;
                byte[] temp = new byte[random];
                int len = readFileFromSD(random, temp);
                if(len < random) hasData = false;//donot read again
				byte[] src = new byte[len];
                System.arraycopy(temp, 0, src, 0, len);
                EcgWaveData.saveData(src);
                Thread.sleep(50);
            } catch (Exception e) {
                LogUtil.e(TAG, e);
            }
            synchronized (this) {
                if (closed) {
                    LogUtil.d(TAG, "Thread stoped");
                    break;
                }
            }
        }
    }	
    
    public synchronized void ResumeThread() {
    	paused = false;
        notify();
    }
    public synchronized void pauseThread() {
        notify();
        paused = true;
    }
    
    
    public synchronized void stopThread() {
        closed = true;
        notify();
        try {
        	sleep(100);
        	closeFile();
        }catch (Exception e) {
            LogUtil.e(TAG, e);
        }
        
    }
    
    public static String bytes2HexString(byte[] b) {
  	  String ret = "";
  	  for (int i = 0; i < b.length; i++) {
  	   String hex = Integer.toHexString(b[ i ] & 0xFF);
  	   if (hex.length() == 1) {
  	    hex = '0' + hex;
  	   }
  	   ret += hex.toUpperCase();
  	  }
  	  return ret;
  	}
    
    static int offset = 0;
    BufferedInputStream inputStream;
    
	public void openFile(String name) {
		try {
			inputStream = new BufferedInputStream(EcgApp.getInstance().getAssets().open(name));
			mIsFileOpened = true;
			byte[] buffer = new byte[400];
			inputStream.read(buffer, 0, 400);
		} catch (IOException e) {
			mIsFileOpened = false;
			LogUtil.e(TAG, e);
		}
	}
	
	private long fileLength;
	private int seconds;
	/**
	 * 打开SDCard上的文件
	 * @param name
	 */
	public void openSDFile(String name, int progress)
	{
	    try {
	    	File file = new File(name);
	    	fileLength = file.length();
	    	// 计算文件总计时间
	    	seconds = (int)(fileLength / (1024 * 4));
            inputStream = new BufferedInputStream(new FileInputStream(file));
            if (progress > -1)
            {
            	// 256为一包完整数据, 所以跳跃读取必须整包跳跃
            	long skip = (((fileLength/256 * progress) / 100) * 256);
            	totalBytes += skip;
            	inputStream.skip(skip);
            }
            mIsFileOpened = true;
        } catch (IOException e) {
            mIsFileOpened = false;
            LogUtil.e(TAG, e);
        }
	}
	
	long totalBytes;
    public int readFileFromSD(int length, byte[] buffer) {
        int bytesNumber = -1;
        try {     	
        	bytesNumber = inputStream.read(buffer, 0, length);
        	totalBytes = totalBytes + bytesNumber;
        	handler.sendEmptyMessage(0);
        } catch (IOException e) {
            LogUtil.e(TAG, e);
        }
        return bytesNumber;
    }
    
	public void closeFile() {
		if(!mIsFileOpened) return;
		try { 
			inputStream.close();
		} catch (IOException e) {
		    LogUtil.e(TAG, e);
		}
	}
}
