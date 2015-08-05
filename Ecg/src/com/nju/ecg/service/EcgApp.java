package com.nju.ecg.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

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
import com.nju.ecg.utils.StringUtil;
import com.nju.ecg.wave.EcgSaveData;
import com.nju.ecg.wave.EcgWaveData;

public class EcgApp extends Application {
    private static final String TAG = "EcgApp";
    private EcgBinder mEcgBinder;
    
    private static EcgApp sInstance;
    public final static int MODE_RECORD = 0;
    public final static int MODE_REPLAY = 1;
    public int mode = MODE_RECORD;
    
	public void onCreate() {
	    LogUtil.d(TAG, "onCreate");
		sInstance = this;
		DataBaseHelper.getInstance().open();
		Thread.setDefaultUncaughtExceptionHandler(new GlobalExceptionHandler());
        Intent serviceIntent = new Intent(this, EcgService.class);
        serviceIntent.setAction(Intent.ACTION_BOOT_COMPLETED);
        this.startService(serviceIntent);
        
        BufferedWriter bw = null;
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
            
            File settingDir = new File(EcgConst.SETTING_DIR);
            if (!settingDir.exists())
            {
                settingDir.mkdirs();
            }
            
            File ipFile = new File(EcgConst.IP_SETTING_FILE);
            if (!ipFile.exists())
            {
                ipFile.createNewFile();
                bw = new BufferedWriter(new FileWriter(ipFile));
                bw.write(EcgConst.PATIENT_SERVER_IP);
                bw.close();
            }
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
     * 读取配置文件, 获得服务器ip地址, 若不存在该文件或者获取Ip地址失败则使用默认IP
     * @return
     */
    public String getServerIp()
    {
        File ipFile = new File(EcgConst.IP_SETTING_FILE);
        if (!ipFile.exists())
        {
            return EcgConst.PATIENT_SERVER_IP;
        }
        try
        {
            BufferedReader br = new BufferedReader(new FileReader(ipFile));
            String host = br.readLine();
            if (StringUtil.isNullOrEmpty(host))
            {
                return EcgConst.PATIENT_SERVER_IP;
            }
            else
            {
                String ip = null;
                try
                {
                    InetAddress ia = InetAddress.getByName(host);
                    ip = ia.getHostAddress();
                }
                catch (UnknownHostException e)
                {
                    LogUtil.e(TAG, e);
                }
                if (StringUtil.isNullOrEmpty(ip))
                {
                    return EcgConst.PATIENT_SERVER_IP;
                }
                else
                {
                    return ip;
                }
            }
        }
        catch (FileNotFoundException e)
        {
            LogUtil.e(TAG, e);
        }
        catch (IOException e)
        {
            LogUtil.e(TAG, e);
        }
        return EcgConst.PATIENT_SERVER_IP;
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

