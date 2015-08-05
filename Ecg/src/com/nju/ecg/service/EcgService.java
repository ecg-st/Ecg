package com.nju.ecg.service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import android.app.Service;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.nju.ecg.framework.db.WaveDataDBHelper;
import com.nju.ecg.model.WaveData;
import com.nju.ecg.utils.EcgConst;
import com.nju.ecg.utils.EcgIntent;
import com.nju.ecg.utils.FileUtil;
import com.nju.ecg.utils.LogUtil;
import com.nju.ecg.utils.StringUtil;
import com.nju.ecg.wave.EcgDrawView;
import com.nju.ecg.wave.EcgWaveData;
import com.nju.ecg.wave.WaveScreen;

public class EcgService extends Service {
	private final static String TAG = "EcgService";
	private Context mContext;
	
	private static final int MSG_START_BT_THREAD = 0;
	private static final int MSG_START_READ_PHONE_DATA = 1;
	
	private String mCurrentFileName ="";
    public static BluetoothSocket mBtSocket;
   
	public EcgService() {
	}
	   
    public void onCreate() {
        super.onCreate();
        LogUtil.d(TAG, "+onCreate");
        mContext = this;
    	Intent intent = new Intent(EcgIntent.ECG_SERVICE_STARTED);
    	sendBroadcast(intent);
    	EcgWaveData.init();
    }

    @Override
    public void onDestroy() {
        LogUtil.e(TAG, "Service >> onDestroy");
        super.onDestroy();
        EcgWaveData.destroy();
        EcgWaveData.clearAnalyseData();
    }

    
    private final IBinder mBinder = new EcgBinder();
    public class EcgBinder extends Binder {
    	public void setBtSocket(BluetoothSocket btSocket) {
    		mBtSocket = btSocket;
	    	Message msg = mHandler.obtainMessage(MSG_START_BT_THREAD);
	    	mHandler.sendMessage(msg);
    	}
    	
    	public void startReadDataFromPhone() {
        		Message msg = mHandler.obtainMessage(MSG_START_READ_PHONE_DATA);
        		mHandler.sendMessage(msg);
    	}
    	
    	public int[] getEcgDataArray() {
    		return ecgdata;
    	}
    	
    	public void setDataToCalHeartRate(int[] data, String tag) {
    		System.arraycopy(data, 0, ecgdata, 0, data.length);
    		//Message msg = mHandler.obtainMessage(GET_HEART_RATE);
    		Message msg = new Message();
    		msg.obj = tag;
    		mHandler.sendMessage(msg);    		
    	}
    	
    	public int[] filterEcgData(int[] data) {
    	    return ecgFilter(data, data.length);
    	}
    	
    	public String createFileForSaveData(String fileName) {       
            int leadSystem = EcgDrawView.mCurentLead;
            String leadPath = null;
            switch (leadSystem)
            {
                case EcgConst.LIMB_LEAD:
                    leadPath = EcgConst.LIMB_LEAD_DIR;
                    break;
                case EcgConst.MOCK_LIMB_LEAD:
                    leadPath = EcgConst.MOCK_LIMB_LEAD_DIR;
                    break;
                case EcgConst.MOCK_CHEST_LEAD:
                    leadPath = EcgConst.MOCK_CHEST_LEAD_DIR;
                    break;
                case EcgConst.SIMPLE_LIMB_LEAD:
                    leadPath = EcgConst.SIMPLE_LIMB_LEAD_DIR;
                    break;
            }
            File leadDir = new File(leadPath);
            if (!leadDir.exists()) {
                try {
                    if (!leadDir.mkdirs()) {
                        LogUtil.e(TAG, "make dir fail");
                        return null;
                    }
                } catch (Exception e) {
                    LogUtil.e(TAG, e);
                    return null;
                }

            }
            String filePath = leadPath + "/" + fileName + EcgConst.FILE_END_NAME;
            WaveData data = new WaveData();
            data.setFilePath(filePath);
            data.setLeadSystem(leadSystem);
            WaveDataDBHelper dbHelper = WaveDataDBHelper.getInstance();
            dbHelper.insert(data);
            mCurrentFileName = filePath;
            return filePath;
        }
    	
    	/**
         * 检测是否有同名的raw、txt、rr及检测报告文件夹
         * @param fileName
         * @return
         */
        public boolean fileExist(String fileName)
        {
            String resultPath = mCurrentFileName.substring(0, mCurrentFileName.lastIndexOf("/")) + "/" + fileName + EcgConst.REPORT_FILE_END_NAME;
            if (FileUtil.isExist(resultPath))
            {
                return true;
            }
            return false;
        }
        
        /**
         * 更新数据
         * @param dbHelper
         * @param data
         */
        public void updateWaveData(WaveDataDBHelper dbHelper, WaveData data)
        {
            dbHelper.update(mCurrentFileName, data);
            if (!mCurrentFileName.endsWith(data.getCollectFormatedTime() + EcgConst.FILE_END_NAME)) // 文件名有变更则重命名采集数据和散点图数据文件
            {
                if (FileUtil.isExist(mCurrentFileName)) // 重命名采集数据文件
                {
                    File file = new File(mCurrentFileName);
                    file.renameTo(new File(mCurrentFileName.substring(0, mCurrentFileName.lastIndexOf("/")) + "/" + data.getCollectFormatedTime() + EcgConst.FILE_END_NAME));
                }
            }
            BufferedWriter bw = null;
            try
            {
                // 检测报告存文件
                File reportFile = new File(mCurrentFileName.substring(0, mCurrentFileName.lastIndexOf("/")) + "/" + data.getCollectFormatedTime() + EcgConst.REPORT_FILE_END_NAME);
                if (!reportFile.exists())
                {
                    reportFile.createNewFile();
                }
                bw = new BufferedWriter(new FileWriter(reportFile));
                bw.write(data.getDiagnoseResult());
                bw.flush();
            }
            catch (Exception e)
            {
                LogUtil.e(TAG, e);
            }
            finally
            {
                try
                {
                    if (bw != null)
                    {
                        bw.close();
                    }
                }
                catch (Exception e2)
                {
                    LogUtil.e(TAG, e2);
                }
            }
        }
        
        /**
         * 用户不保存采集的数据则删除
         * @param dbHelper
         */
        public void deleteData(WaveDataDBHelper dbHelper)
        {
            // 删除数据库记录
            dbHelper.delete(mCurrentFileName);
            
            // 删除采集数据文件
            if (FileUtil.isExist(mCurrentFileName))
            {
                File file = new File(mCurrentFileName);
                file.delete();
            }
        }
    	
    	public String getMCurrentFileName()
    	{
    	    return mCurrentFileName;
    	}
    }
    
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return mBinder;
	}
	
    private Handler mHandler = new Handler()
    {
        public void handleMessage(Message msg)
        {

            LogUtil.d(TAG,
                "GET_HEART_RATE");
            // 避免停止采集情况、切换数据源等情况下继续分析数据的问题
            if (!StringUtil.isNullOrEmpty(WaveScreen.currentTag)
                && WaveScreen.currentTag.equals(((String) msg.obj)))
            {
                new HandleEcgParameterThread(new String(WaveScreen.currentTag))
                    .start();
            }
        }
    };
	
	 private class HandleEcgParameterThread extends Thread
    {
        /** 标识每次处理的标记*/
        private String currentHandleTag;
        public HandleEcgParameterThread(String tag)
        {
            currentHandleTag = tag;
        }
        @Override
        public void run()
        {
            try
            {
                int[] b = getEcgParameter(ecgdata);
                if (ecgRRCount >= 0)
                {
                    ecgRRCount = 0;
                    Intent hrIntent = new Intent(EcgIntent.ECG_HEART_RATE);
                    hrIntent.putExtra("ecg_parameter",
                        b);
                    hrIntent.putExtra("tag", currentHandleTag);
                    sendBroadcast(hrIntent);
                }
            }
            catch (Exception e)
            {
                LogUtil.e(TAG, e);
            }
        }
    }

    int ecgRRCount = 0;
    int totalHrv = 0;
    private int sumHRV(int[] data) {
    	int hrv = 0;
    	for(int i = 1; i < data.length - 1; i++) { //data[0] is length
    		hrv += data[i+1] - data[i];
    		Log.v("hello", "hrv = " + (data[i+1] - data[i]));
    		ecgRRCount++;
    	}
    	return hrv;
    }
    private int caculateHeartRateByHRV(int hrv, int ecgRRCount) {
    	int ret = 0;

    	ret = (60 * 1000 * ecgRRCount) / hrv;
    	return ret;
    }

	int[] ecgdata = new int[EcgConst.ECG_DATA_LENGTH];
	private static final int GET_HEART_RATE = 3;
	
	int[] ecgRawDataForFilter = new int[EcgConst.ECG_DATE_FILTER_FOR_DISPALY_LENGTH];

	public native int[] ecgFilter(int[] arr, int length);
	public native int[] setEcgData(int[] arr);
	public native int[]getEcgParameter(int[] arr);
	static {
        System.loadLibrary("hello-jni");
}
}
