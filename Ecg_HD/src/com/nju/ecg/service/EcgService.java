package com.nju.ecg.service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import android.app.Service;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.nju.ecg.framework.db.WaveDataDBHelper;
import com.nju.ecg.model.WaveData;
import com.nju.ecg.utils.EcgConst;
import com.nju.ecg.utils.EcgIntent;
import com.nju.ecg.utils.FileUtil;
import com.nju.ecg.utils.LogUtil;
import com.nju.ecg.utils.ReportUtil;
import com.nju.ecg.utils.ScreenShot;
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
//    		Message msg = mHandler.obtainMessage(GET_HEART_RATE);
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
    	        
    	        if (FileUtil.isExist(mCurrentFileName.substring(0, 
                    mCurrentFileName.indexOf(EcgConst.FILE_END_NAME)) + EcgConst.RR_FILE_END_NAME)) // 重命名散点图数据文件
                {
    	            File rrFile = new File(mCurrentFileName.substring(0, 
                        mCurrentFileName.indexOf(EcgConst.FILE_END_NAME)) + EcgConst.RR_FILE_END_NAME);
                    rrFile.renameTo(new File(mCurrentFileName.substring(0, mCurrentFileName.lastIndexOf("/")) + "/" + data.getCollectFormatedTime() + EcgConst.RR_FILE_END_NAME));
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
            
            // 删除散点图数据文件
            if (FileUtil.isExist(mCurrentFileName.substring(0,
                mCurrentFileName.indexOf(EcgConst.FILE_END_NAME))
                + EcgConst.RR_FILE_END_NAME))
            {
                File rrFile = new File(mCurrentFileName.substring(0,
                    mCurrentFileName.indexOf(EcgConst.FILE_END_NAME))
                    + EcgConst.RR_FILE_END_NAME);
                rrFile.delete();
            }
            
            // 删除报告文件夹
            File reportDir = new File(mCurrentFileName.substring(0, mCurrentFileName.lastIndexOf(".")) + "_report");
            if (reportDir.exists())
            {
                File[] files = reportDir.listFiles();
                for (File file : files)
                {
                    file.delete();
                }
                reportDir.delete();
            }
        }
    	
    	/**
    	 * 保存RR数据
    	 * @param rrData 以","相隔的整型数据
    	 */
    	public void saveRRData(String rrData)
    	{
            BufferedWriter bw = null;
            try
            {
                // RR间期数据存文件
                File rrFile = new File(mCurrentFileName.substring(0, 
                    mCurrentFileName.indexOf(EcgConst.FILE_END_NAME)) + EcgConst.RR_FILE_END_NAME);
                if (!rrFile.exists())
                {
                    rrFile.createNewFile();
                }
                bw = new BufferedWriter(new FileWriter(rrFile, true));
                bw.write(rrData);
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
    	 * 保存心率截屏
    	 * @param ch1Data
    	 * @param ch2Data
    	 * @param updateIndex
    	 * @param switchScreen
    	 * @param infoHeight
    	 */
    	public void saveWaveShot(final int[] ch1Data, final int[] ch2Data, final int updateIndex, final boolean switchScreen, final int infoHeight)
    	{
    	    new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    final ReportUtil reportUtil = ReportUtil.getInstance();
                    Bitmap bitmap = reportUtil.drawWaveScreen(ch1Data,
                        ch2Data,
                        updateIndex,
                        switchScreen,
                        infoHeight);
                    // file path for test
                    // mCurrentFileName = EcgConst.LIMB_LEAD_DIR + "/" + "0123456789" + EcgConst.FILE_END_NAME;
                    // file path for test
                    
                    String dataDir = mCurrentFileName.substring(0,
                        mCurrentFileName.lastIndexOf("/"));
                    String dataName = mCurrentFileName.substring(mCurrentFileName
                        .lastIndexOf("/") + 1,
                        mCurrentFileName.lastIndexOf(EcgConst.FILE_END_NAME));
                    File reportDir = new File(dataDir + "/" + dataName + "_report");
                    if (!reportDir.exists())
                    {
                        reportDir.mkdirs();
                    }
                    String picPath = reportDir.getAbsolutePath() + "/" + System.currentTimeMillis()
                        + dataName + "_wave" + ".png";
                    if (!FileUtil.isExist(picPath))
                    {
                        try
                        {
                            new File(picPath).createNewFile();
                        }
                        catch (Exception e)
                        {
                            LogUtil.e(TAG,
                                e);
                        }
                    }
                    if (!FileUtil.isExist(picPath))
                    {
                        try
                        {
                            new File(picPath).createNewFile();
                        }
                        catch (Exception e)
                        {
                            LogUtil.e(TAG,
                                e);
                        }
                    }
                    ScreenShot.savePic(bitmap, picPath);
                }
            }).start();
    	}
    	
    	/**
    	 * 检测是否有同名的txt、rr及检测报告文件夹
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
    	 * 保存散点图截屏
    	 * @param data
    	 */
    	public void saveDotGraphShot(final String fileName, final long collectingTime)
    	{
    	    new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    String path = mCurrentFileName.substring(0, mCurrentFileName.lastIndexOf("/")) + "/" + fileName + EcgConst.RR_FILE_END_NAME;
                    
                    // file path for test
                    // String path = EcgConst.LIMB_LEAD_DIR + "/" + "test" + EcgConst.RR_FILE_END_NAME;
                    // mCurrentFileName = EcgConst.LIMB_LEAD_DIR + "/" + "0123456789" + EcgConst.FILE_END_NAME;
                    // file path for test
                    
                    Bitmap bitmap = ReportUtil.getInstance().drawDotGraph(path, collectingTime);
                    String dataDir = mCurrentFileName.substring(0,
                        mCurrentFileName.lastIndexOf("/"));
                    String dataName = mCurrentFileName.substring(mCurrentFileName
                        .lastIndexOf("/") + 1,
                        mCurrentFileName.lastIndexOf(EcgConst.FILE_END_NAME));
                    File reportDir = new File(dataDir + "/" + dataName + "_report");
                    if (!reportDir.exists())
                    {
                        reportDir.mkdirs();
                    }
                    String picPath = reportDir.getAbsolutePath() + "/" + System.currentTimeMillis()
                        + dataName + "_dot" + ".png";
                    if (!FileUtil.isExist(picPath))
                    {
                        try
                        {
                            new File(picPath).createNewFile();
                        }
                        catch (Exception e)
                        {
                            LogUtil.e(TAG,
                                e);
                        }
                    }
                    if (!FileUtil.isExist(picPath))
                    {
                        try
                        {
                            new File(picPath).createNewFile();
                        }
                        catch (Exception e)
                        {
                            LogUtil.e(TAG,
                                e);
                        }
                    }
                    ScreenShot.savePic(bitmap, picPath);
                    // 更新检测报告文件夹
                    File reportDirOld = new File(mCurrentFileName.substring(0, mCurrentFileName.lastIndexOf(".")) + "_report");
                    if (reportDirOld.exists() && !mCurrentFileName.substring(0, mCurrentFileName.lastIndexOf(".")).equals(mCurrentFileName.substring(0, mCurrentFileName.lastIndexOf("/")) + "/" + fileName))
                    {
                        File reportDirNew = new File(mCurrentFileName.substring(0, mCurrentFileName.lastIndexOf("/")) + "/" + fileName + "_report");
                        reportDirOld.renameTo(reportDirNew);
                    }
                    // 生成检测报告
                    String filePath = mCurrentFileName.substring(0, mCurrentFileName.lastIndexOf("/")) + "/" + fileName + EcgConst.FILE_END_NAME;
                    ReportUtil.getInstance().packageReport(filePath);
                    
                    // 清除WaveScreen界面的缓存数据
                    WaveScreen.qrsValueList.clear();
                    WaveScreen.prValueList.clear();
                    WaveScreen.qtValueList.clear();
                    WaveScreen.stValueList.clear();
                    WaveScreen.abnormalParameterMap.clear();
                }
            }).start();
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
            LogUtil.d(TAG, "currentTag =" + WaveScreen.currentTag);
            LogUtil.d(TAG, "lastTag =" + (String) msg.obj);
            // 避免停止采集情况、切换数据源等情况下继续分析数据的问题
            if (!StringUtil.isNullOrEmpty(WaveScreen.currentTag)
                && WaveScreen.currentTag.equals(((String) msg.obj)))
            {
                LogUtil.d(TAG, "currentTag = lastTag");
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
                Toast testToast = Toast.makeText(mContext, "捕获到c++层异常", Toast.LENGTH_LONG);
                testToast.show();
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
