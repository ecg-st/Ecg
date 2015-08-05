package com.nju.ecg.framework.db;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.nju.ecg.model.WaveData;
import com.nju.ecg.utils.EcgConst;
import com.nju.ecg.utils.LogUtil;

/**
 * 心率数据库操作
 * @author zhuhf
 * @version [ME MTVClient_Handset V100R001C04SPC002, 2012-9-12]
 */
public class WaveDataDBHelper
{
    public static final String TAG = "WaveDataDBHelper";
    /** 表名*/
    private static final String TABLE_NAME = "wave_data";
    /** 主键*/
    private static final String COLUMN_ID = "_id";
    /** 文件名, 默认是采集时间*/
    private static final String COLUMN_FILE_NAME = "file_name";
    /** 存储采集 的数据的文件完整路径*/
    private static final String COLUMN_FILE_PATH = "file_path";
    /** 采集数据时选择的导联系统*/
    private static final String COLUMN_LEAD_SYSTEM = "lead_system";//使用EcgConst定义的4个常量
    /** 心跳数据*/
    private static final String COLUMN_HEART_PARA = "heart_para";
    /** 异常信息*/
    private static final String COLUMN_ABNORMAL_VALUE = "abnormal_value";
    /** 诊断结论*/
    private static final String COLUMN_DIAGNOSE_RESULT = "diagnose_result";
    /** 开始时间*/
    private static final String COLUMN_TIME_START = "start_time";
    /** 其他描述信息*/
    private static final String COLUMN_DESC = "desc";
    /** 结束时间*/
    private static final String COLUMN_TIME_END = "end_time";
    /** 建表语句*/
    public static final String TABLE_CREATE_SQL = new StringBuilder()
            .append("CREATE TABLE ")
            .append(TABLE_NAME)
            .append("(")
            .append(COLUMN_ID)
            .append(" INTEGER PRIMARY KEY AUTOINCREMENT,")
            .append(COLUMN_FILE_NAME)
            .append(" TEXT,")
            .append(COLUMN_FILE_PATH)
            .append(" TEXT,")
            .append(COLUMN_LEAD_SYSTEM)
            .append(" INTEGER,")
            .append(COLUMN_HEART_PARA)
            .append(" TEXT,")
            .append(COLUMN_ABNORMAL_VALUE)
            .append(" TEXT,")
            .append(COLUMN_DIAGNOSE_RESULT)
            .append(" TEXT,")
            .append(COLUMN_TIME_START)
            .append(" Long,")
            .append(COLUMN_TIME_END)
            .append(" Long,")
            .append(COLUMN_DESC)
            .append(" TEXT)")
            .toString();
    /** 单例*/
    private static WaveDataDBHelper sInstance = null;
    /** 数据库操作对象*/
    private SQLiteDatabase db = null;
    
    /**
     * 无参构造
     */
    private WaveDataDBHelper()
    {
        db = DataBaseHelper.getInstance().getSQLiteDatabase();
    }
    
    /**
     * @return 单例对象
     */
    public static WaveDataDBHelper getInstance()
    {
        if (sInstance == null)
        {
            sInstance = new WaveDataDBHelper();
        }
        return sInstance;
    }
    
    /**
     * 插入一条数据
     * @param data
     */
    public void insert(WaveData data)
    {
        try
        {
            ContentValues values = new ContentValues();
            values.put(COLUMN_FILE_NAME, data.getCollectFormatedTime());
            values.put(COLUMN_FILE_PATH, data.getFilePath());
            values.put(COLUMN_LEAD_SYSTEM, data.getLeadSystem());
            values.put(COLUMN_HEART_PARA, data.getHeartPara());
            values.put(COLUMN_ABNORMAL_VALUE, data.getAbnormalValue());
            values.put(COLUMN_DIAGNOSE_RESULT, data.getDiagnoseResult());
            values.put(COLUMN_TIME_START, data.getStartTime());
            values.put(COLUMN_TIME_END, data.getEndTime());
            values.put(COLUMN_DESC, data.getDesc());
            db.insert(TABLE_NAME, null, values);
        }
        catch (Exception e)
        {
            LogUtil.e(TAG, e);
        }
    }
    
    /**
     * 根据文件Path来更新一条记录
     * @param filePath 路径
     * @param data 数据
     */
    public void update(String filePath, WaveData data)
    {
        ContentValues values = new ContentValues();
        values.put(COLUMN_FILE_NAME, data.getCollectFormatedTime());
        values.put(COLUMN_FILE_PATH, filePath.substring(0, filePath.lastIndexOf("/")) + "/" + data.getCollectFormatedTime() + EcgConst.FILE_END_NAME);
        values.put(COLUMN_TIME_START, data.getStartTime());
        values.put(COLUMN_TIME_END, data.getEndTime());
        values.put(COLUMN_HEART_PARA, data.getHeartPara());
        values.put(COLUMN_ABNORMAL_VALUE, data.getAbnormalValue());
        values.put(COLUMN_DIAGNOSE_RESULT, data.getDiagnoseResult());
        values.put(COLUMN_DESC, data.getDesc());
        db.update(TABLE_NAME, values, COLUMN_FILE_PATH + "=?", new String[]{filePath});
    }
    
    /**
     * 根据主键id删除记录
     * @param id
     */
    public void delete(int id)
    {
        try
        {
            db.delete(TABLE_NAME, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
        }
        catch (Exception e)
        {
            LogUtil.e(TAG, e);
        }
    }
    
    /**
     * 根据文件路径删除记录
     * @param filePath
     */
    public void delete(String filePath)
    {
        try
        {
            db.delete(TABLE_NAME, COLUMN_FILE_PATH + "=?", new String[]{filePath});
        }
        catch (Exception e)
        {
            LogUtil.e(TAG, e);
        }  
    }
    
    /**
     * 获取所有心率数据, 其中包括DataDir中用户手动加进去的数据
     * @return WaveData集合
     */
    public List<WaveData> getDataList()
    {
        final List<String> databaseFilePaths = new ArrayList<String>();
        List<WaveData> dataList = new ArrayList<WaveData>();
        Cursor c = null;
        try
        {
            c = db.query(TABLE_NAME, null, null, null, null, null, COLUMN_TIME_START + " desc");
            if (null != c && c.getCount() > 0)
            {
                while(c.moveToNext())
                {
                    WaveData data = parseCursor(c);
                    databaseFilePaths.add(data.getFilePath());
                    dataList.add(data);
                }
            }
        }
        catch (Exception e)
        {
            LogUtil.e(TAG, e);
        }
        finally
        {
            if (null != c)
            {
                c.close();
            }
        }

     // 过滤数据库中已经存在的文件路径
        // 肢体导联
        File limbDir = new File(EcgConst.LIMB_LEAD_DIR);
        if (limbDir.exists())
        {
            File[] files= limbDir.listFiles(new FileFilter()
            {
                
                @Override
                public boolean accept(File file)
                {
                    return ((!databaseFilePaths.contains(file.getAbsolutePath())) 
                    && (file.getAbsolutePath().endsWith(EcgConst.FILE_END_NAME)) && file.getAbsolutePath().endsWith(EcgConst.FILE_END_NAME));
                }
            });
            
            for(File f : files)
            {
                WaveData data = new WaveData();
                data.setCustom(true);
                data.setLeadSystem(EcgConst.LIMB_LEAD);
                data.setFilePath(f.getAbsolutePath());
                dataList.add(data);
            }
        }
        // 模拟肢体导联
        File mockLimbDir = new File(EcgConst.MOCK_LIMB_LEAD_DIR);
        if (mockLimbDir.exists())
        {
            File[] files= mockLimbDir.listFiles(new FileFilter()
            {
                
                @Override
                public boolean accept(File file)
                {
                    return ((!databaseFilePaths.contains(file.getAbsolutePath())) 
                    && (file.getAbsolutePath().endsWith(EcgConst.FILE_END_NAME)) && file.getAbsolutePath().endsWith(EcgConst.FILE_END_NAME));
                }
            });
            
            for(File f : files)
            {
                WaveData data = new WaveData();
                data.setCustom(true);
                data.setLeadSystem(EcgConst.MOCK_LIMB_LEAD);
                data.setFilePath(f.getAbsolutePath());
                dataList.add(data);
            }
        }
        // 模拟胸导联
        File chestDir = new File(EcgConst.MOCK_CHEST_LEAD_DIR);
        if (chestDir.exists())
        {
            File[] files= chestDir.listFiles(new FileFilter()
            {
                
                @Override
                public boolean accept(File file)
                {
                    return ((!databaseFilePaths.contains(file.getAbsolutePath())) 
                    && (file.getAbsolutePath().endsWith(EcgConst.FILE_END_NAME)) && file.getAbsolutePath().endsWith(EcgConst.FILE_END_NAME));
                }
            });
            
            for(File f : files)
            {
                WaveData data = new WaveData();
                data.setCustom(true);
                data.setLeadSystem(EcgConst.MOCK_CHEST_LEAD);
                data.setFilePath(f.getAbsolutePath());
                dataList.add(data);
            }
        }
        // 简易肢体导联
        File simpleDir = new File(EcgConst.SIMPLE_LIMB_LEAD_DIR);
        if (simpleDir.exists())
        {
            File[] files= simpleDir.listFiles(new FileFilter()
            {
                
                @Override
                public boolean accept(File file)
                {
                    return ((!databaseFilePaths.contains(file.getAbsolutePath())) 
                    && (file.getAbsolutePath().endsWith(EcgConst.FILE_END_NAME)) && file.getAbsolutePath().endsWith(EcgConst.FILE_END_NAME));
                }
            });
            
            for(File f : files)
            {
                WaveData data = new WaveData();
                data.setCustom(true);
                data.setLeadSystem(EcgConst.SIMPLE_LIMB_LEAD);
                data.setFilePath(f.getAbsolutePath());
                dataList.add(data);
            }
        }
        
        return dataList;
    }
    
    /**
     * 查询最新采集的数据
     * @return 心率数据
     */
    public WaveData getLatestData()
    {
        WaveData data = null;
        Cursor c = null;
        try
        {
            c = db.query(TABLE_NAME, null, null, null, null, null, COLUMN_TIME_START + " desc", "0,1");
            if (null != c && c.getCount() > 0)
            {
                while(c.moveToNext())
                {
                    data = parseCursor(c);
                }
            }
        }
        catch (Exception e)
        {
            LogUtil.e(TAG, e);
        }
        finally
        {
            if (null != c)
            {
                c.close();
            }
        }
        return data;
    }
    
    /**
     * 解析游标
     * @param c 游标
     * @return WaveData
     */
    private WaveData parseCursor(Cursor c)
    {
        WaveData data = new WaveData();
        try
        {
            data.set_id(c.getInt(c.getColumnIndex(COLUMN_ID)));
            data.setFilePath(c.getString(c.getColumnIndex(COLUMN_FILE_PATH)));
            data.setLeadSystem(c.getInt(c.getColumnIndex(COLUMN_LEAD_SYSTEM)));
            data.setHeartPara(c.getString(c.getColumnIndex(COLUMN_HEART_PARA)));
            data.setAbnormalValue(c.getString(c
                .getColumnIndex(COLUMN_ABNORMAL_VALUE)));
            data.setDiagnoseResult(c.getString(c
                .getColumnIndex(COLUMN_DIAGNOSE_RESULT)));
            data.setStartTime(c.getLong(c.getColumnIndex(COLUMN_TIME_START)));
            data.setCollectFormatedTime(c.getString(c.getColumnIndex(COLUMN_FILE_NAME)));
            data.setEndTime(c.getLong(c.getColumnIndex(COLUMN_TIME_END)));
            data.setDesc(c.getString(c.getColumnIndex(COLUMN_DESC)));
        }
        catch (Exception e)
        {
            LogUtil.e(TAG,
                e);
        }
        return data;
    }
}
