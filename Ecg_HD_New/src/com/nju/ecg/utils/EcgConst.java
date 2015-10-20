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
	
	/** 当前宫格的默认大小*/
    public static int GRID_WIDTH = 50;
    
    /** 采集时需要等待30秒, 完成监听的URI */
    public static Uri COLLECT_WAITING_COMPLETED_URI = Uri
        .parse("content://com.nju.ecg/collect/waiting/completed");
	
	//Average number
	public static final int AVERAGE_POINTS = 8;//4; 400ms ->50px 
	
	/** 导联数目(通道)*/
	public static final int LEADS_NUMBER = 2;
	
	/** 最少的分析数据的缓冲区长度*/
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
	/** RR间期数据文件后缀*/
	public static final String RR_FILE_END_NAME = ".rr";
	
	/** 测试滤波算法数据保存目录*/
	public static final String FILTER_DIR = Environment.getExternalStorageDirectory().getPath() + "/EcgApp/TestFilterDir";
	
	/** 肢体导联*/
    public static final int LIMB_LEAD = 2000;
    
    /** 模拟肢体导联*/
    public static final int MOCK_LIMB_LEAD = LIMB_LEAD + 1;
    
    /** 模拟胸导联*/
    public static final int MOCK_CHEST_LEAD = LIMB_LEAD + 2;
    
    /** 简易肢体导联*/
    public static final int SIMPLE_LIMB_LEAD = LIMB_LEAD + 3;
    
    /** 授权文件存放目录*/
    public static final String AUTHORIZED_DOCUMENT_DIR = Environment.getExternalStorageDirectory().getPath() + "/EcgApp";
    
    public static final String IFYTEK_APPID = "560fd84c";
}
