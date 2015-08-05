package com.nju.ecg.utils;

import android.net.Uri;
import android.os.Environment;

public class EcgConst {
    /** 屏幕宽度*/
	public static int DISPLAY_WIDTH = 480;
	/** 屏幕高度*/
	public static int DISPLAY_HEIGH = 640;
	
	public static int DISPLAY_LEFT_LEAD_NUMBER_WIDTH = 0;
	/** 心率数组宽度*/
	public static int WAVE_WIDTH = DISPLAY_WIDTH;
	/** 心率数组起始"造"数据的总长度*/
	public static int WAVE_DEVIATION_VALUE = 64;
	/** 伪造数据段1*/
	public static int WAVE_DEVIATION_VALUE_PART1 = 20;
	/** 伪造数据段2*/
	public static int WAVE_DEVIATION_VALUE_PART2 = 40;
	
	/** 对应四种密度的屏幕宫格大小常量*/
	public static final int LMDPI_GRID_WIDTH = 50;
	public static final int HDPI_GRID_WIDTH = 100;
	public static final int XHDPI_GRID_WIDTH = 200;
	
	/** 当前宫格的默认大小*/
	public static int GRID_WIDTH = LMDPI_GRID_WIDTH;
	
	/** 每个格子代表的时间*/
	public static final int ONE_GRID_TIME = 400;
	
	/** 采集时需要等待30秒, 完成监听的URI */
    public static Uri COLLECT_WAITING_COMPLETED_URI = Uri
        .parse("content://com.nju.ecg/collect/waiting/completed");
	
	//更具屏幕分辨率情况每8、4、2个点取平均值默认值
	public static int AVERAGE_POINTS = ONE_GRID_TIME/GRID_WIDTH;
	
	/** 导联数目(通道)*/
	public static final int LEADS_NUMBER = 2;
	
	public static final int ECG_DATA_LENGTH = 1000 * 10;
	public static final int ECG_DATE_FILTER_FOR_DISPALY_LENGTH = 1024;
	
	/** 采集数据保存目录*/
    public static final String COLLECT_DATA_DIR = Environment.getExternalStorageDirectory().getPath() + "/EcgApp/DataDir";
    public static final String LIMB_LEAD_DIR = COLLECT_DATA_DIR + "/LimbLead";
    public static final String MOCK_LIMB_LEAD_DIR = COLLECT_DATA_DIR + "/MockLimbLead";
    public static final String MOCK_CHEST_LEAD_DIR = COLLECT_DATA_DIR + "/MockChestLead";
    public static final String SIMPLE_LIMB_LEAD_DIR = COLLECT_DATA_DIR + "/SimpleLimbLead";
    public static final String FILE_END_NAME = ".raw";
    /** 检测报告文件后缀*/
    public static final String REPORT_FILE_END_NAME = ".txt";
	
	/** 系统设置目录*/
	public static final String SETTING_DIR = Environment.getExternalStorageDirectory().getPath() + "/EcgApp/SettingDir";
	public static final String IP_SETTING_FILE = SETTING_DIR + "/ip.txt";
	
	/** 病人服务器默认ip和端口*/
	public static final String PATIENT_SERVER_IP = "192.168.0.104";
	public static final int PATIENT_SERVER_PORT = 4477;
	
	/** 肢体导联*/
    public static final int LIMB_LEAD = 2000;
    
    /** 模拟肢体导联*/
    public static final int MOCK_LIMB_LEAD = LIMB_LEAD + 1;
    
    /** 模拟胸导联*/
    public static final int MOCK_CHEST_LEAD = LIMB_LEAD + 2;
    
    /** 简易肢体导联*/
    public static final int SIMPLE_LIMB_LEAD = LIMB_LEAD + 3;
}
