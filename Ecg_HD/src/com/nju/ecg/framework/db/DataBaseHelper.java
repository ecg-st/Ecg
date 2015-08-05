package com.nju.ecg.framework.db;

import com.nju.ecg.service.EcgApp;
import com.nju.ecg.utils.LogUtil;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 数据库轻量级操作封装
 * @author zhuhf
 * @version [ME MTVClient_Handset V100R001C04SPC002, 2012-9-12]
 */
public class DataBaseHelper extends SQLiteOpenHelper
{
    private static final String TAG = "DataBaseHelper";
    /** 数据库名称*/
    private static final String DATABASE_NAME = "ecg.db";
    /** 数据库版本*/
    private static final int DATABASE_VERSION = 1;
    /** 单例*/
    private static DataBaseHelper sInstance;
    /** 数据库操作对象*/
    private SQLiteDatabase db;
    
    public DataBaseHelper(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    
    public static DataBaseHelper getInstance()
    {
        if (sInstance == null)
        {
            sInstance = new DataBaseHelper(EcgApp.getInstance().getContext());
        }
        return sInstance;
    }
    
    /**
     * 打开数据库
     */
    public void open()
    {
        try
        {
            db = getInstance().getWritableDatabase();
        }
        catch (SQLiteException ex)
        {
            db = getInstance().getReadableDatabase();
            LogUtil.e(TAG,
                "open >> 获取数据库失败 " + ex.getMessage());
        }
    }
    
    /**
     * 获取数据库操作对象
     * @return SQLiteDatabase
     */
    public SQLiteDatabase getSQLiteDatabase()
    {
        return db;
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        db.beginTransaction();
        try
        {
            db.execSQL(WaveDataDBHelper.TABLE_CREATE_SQL);
            // 创建数据库表
            db.setTransactionSuccessful();
        }
        catch (Exception e)
        {
            LogUtil.e(TAG, e);
        }
        finally
        {
            db.endTransaction();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        db.execSQL("DROP DATABASE IF EXISTS " + DATABASE_NAME);
        onCreate(db);
    }

}
