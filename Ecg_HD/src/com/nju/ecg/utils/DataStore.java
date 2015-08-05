package com.nju.ecg.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import com.nju.ecg.service.EcgApp;

/**
 * 存储用户设置项
 * @author zhuhf
 * @version [ME MTVClient_Handset V100R001C04SPC002, 2012-9-11]
 */
public class DataStore
{
    /** 配置文件名*/
    private static final String FILE_NAME = "ecg_config";
    /** 单例*/
    private static DataStore sInstance;
    /** 保存配置*/
    private Editor editor;
    /** 获得配置*/
    private SharedPreferences settings;
    
    /**
     * 私有构造函数
     */
    private DataStore()
    {
        settings = EcgApp.getInstance().getContext().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        editor = settings.edit();
    }
    
    /**
     * 获得单例
     * @return 当前对象
     */
    public static DataStore getInstance()
    {
        if (sInstance == null)
        {
            sInstance = new DataStore();
        }
        return sInstance;
    }
    
    /**
     * 设置导联系统
     * @param lead 导联系统
     */
    public void setLeadSystem(int lead)
    {
        editor.putInt("lead_system", lead).commit();
    }
    
    /**
     * 查询当前设置的导联系统
     * @return 默认为"肢体导联"
     */
    public int getLeadSystem()
    {
        return settings.getInt("lead_system", EcgConst.LIMB_LEAD);
    }
    
    /**
     * 保存最近匹配的地址
     * @param address
     */
    public void saveBluetoothAddress(String address)
    {
        editor.putString("BluetoothAddress", address);
        editor.commit();
    }
    
    public String getBluetoothAddress()
    {
        return settings.getString("BluetoothAddress", "");
    }
}
