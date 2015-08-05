package com.nju.ecg.service;

import java.io.File;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.nju.ecg.bluetooth.BtBufferProcesser;
import com.nju.ecg.framework.db.DataBaseHelper;
import com.nju.ecg.service.EcgService.EcgBinder;
import com.nju.ecg.utils.EcgConst;
import com.nju.ecg.utils.GlobalExceptionHandler;
import com.nju.ecg.utils.LogUtil;
import com.nju.ecg.wave.EcgSaveData;
import com.nju.ecg.wave.EcgWaveData;
/**
 * 应用程序Application
 * @author zhuhf
 * @version [ME MTVClient_Handset V100R001C04SPC002, 2012-10-24]
 */
public class EcgApp extends Application {
    private static final String TAG = "EcgApp";
    private EcgBinder mEcgBinder;
    
    private static EcgApp sInstance;
    public final static int MODE_RECORD = 0;
    public final static int MODE_REPLAY = 1;
    
	public void onCreate() {
	    LogUtil.d(TAG, "onCreate");
	    // 删除滤波算法测试文件夹
//	    File filterDir = new File(EcgConst.FILTER_DIR);
//	    if (filterDir.exists())
//	    {
//	        File[] files = filterDir.listFiles();
//	        try
//            {
//                for (File f : files)
//                {
//                    f.delete();
//                }
//                filterDir.delete();
//            }
//            catch (Exception e)
//            {
//                LogUtil.e(TAG, e);
//            }
//	    }
		sInstance = this;
		DataBaseHelper.getInstance().open();
		Thread.setDefaultUncaughtExceptionHandler(new GlobalExceptionHandler());
        Intent serviceIntent = new Intent(this, EcgService.class);
        serviceIntent.setAction(Intent.ACTION_BOOT_COMPLETED);
        this.startService(serviceIntent);
        
        init();
	}
	
	/**
	 * 初始化客户端配置
	 */
	private void init()
	{
	    try
        {
	        // 创建心率数据保存文件夹
	        File limbDir = new File(EcgConst.LIMB_LEAD_DIR);
	        if (!limbDir.exists())
	        {
	            limbDir.mkdirs();
	        }
	        File mockLimbDir = new File(EcgConst.MOCK_LIMB_LEAD_DIR);
            if (!mockLimbDir.exists())
            {
                mockLimbDir.mkdirs();
            }
            File mockChestDir = new File(EcgConst.MOCK_CHEST_LEAD_DIR);
            if (!mockChestDir.exists())
            {
                mockChestDir.mkdirs();
            }
            File simpleDir = new File(EcgConst.SIMPLE_LIMB_LEAD_DIR);
            if (!simpleDir.exists())
            {
                simpleDir.mkdirs();
            }
        }
        catch (Exception e)
        {
            LogUtil.e(TAG, e);
        }
	}
	
	static public EcgApp getInstance() {
		return sInstance;
	}
	
	@Override
	public void onTerminate()
	{
	    super.onTerminate();
	    LogUtil.d(TAG, "EcgApp >> onTerminate()");
	}
	
	@Override
	public void onLowMemory()
	{
	    super.onLowMemory();
	    LogUtil.e(TAG, "EcgApp >> onLowMemory()");
	    EcgSaveData.clearTempData();
	    synchronized (EcgWaveData.mWaveData)
        {
            // 清空缓存数据
            for (int i = 0; i < EcgWaveData.mWaveData.size(); i++)
            {
                EcgWaveData.mWaveData.get(i).clear();
            }
        }
	    BtBufferProcesser.getInstatce().clear();
	    EcgWaveData.clear();
	    System.gc();
	}
	
	public Context getContext()
	{
	    return getApplicationContext();
	}
	
    public EcgBinder getEcgBinder(){
        return mEcgBinder;
    }
    
    public void bindEcgService(){
		bindService(new Intent(this, EcgService.class), mConnection, Context.BIND_AUTO_CREATE);
    }
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            LogUtil.d(TAG, "onServiceConnected, got Ecg binder");
            mEcgBinder = ((EcgBinder)service);
        }

        public void onServiceDisconnected(ComponentName className) {
            LogUtil.d(TAG,
                "onServiceDisconnected");
        	mEcgBinder = null;
        }
    };

}

