package com.nju.ecg.utils;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.view.View;
import android.view.Window;

/**
 * 截屏工具类
 * 
 * @author zhuhf
 * @version [ME MTVClient_Handset V100R001C04SPC002, 2012-12-19]
 */
public class ScreenShot
{
    private final static String TAG = "ScreenShot";
    
    /**
     * 截屏并生成Bitmap
     * @param activity 需要截屏的Activity
     * @return
     */
    public static Bitmap takeScreenShot(Activity activity)
    {
        // View是你需要截图的View
        View view = activity.getWindow().getDecorView();
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap b1 = view.getDrawingCache();
        return b1;
    }
    
    /**
     * 去除状态栏
     * @param b
     * @param activity
     * @return
     */
    public static Bitmap delStatusBar(Bitmap b, Activity activity)
    {
        // 获取状态栏高度
        Rect frame = new Rect();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
        int statusBarHeight = frame.top;
        LogUtil.d(TAG, "statusBarHeight:" + statusBarHeight);

        // 获取屏幕长和高
        int width = activity.getWindowManager().getDefaultDisplay().getWidth();
        int height = activity
            .getWindowManager()
                .getDefaultDisplay()
                .getHeight();
        // 去掉状态栏
        Bitmap bitmap = Bitmap.createBitmap(b,
            0,
            statusBarHeight,
            width,
            height - statusBarHeight);
        return bitmap;
    }
    
    /**
     * 去除状态栏和标题栏
     * @param b
     * @param activity
     * @return
     */
    public static Bitmap delStatusAndTitleBar(Bitmap b, Activity activity)
    {
        int contentTop = activity.getWindow().findViewById(Window.ID_ANDROID_CONTENT).getTop();  
        // 获取屏幕长和高
        int width = activity.getWindowManager().getDefaultDisplay().getWidth();
        int height = activity
            .getWindowManager()
                .getDefaultDisplay()
                .getHeight();
        // 去掉标题栏
        Bitmap bitmap = Bitmap.createBitmap(b,
            0,
            contentTop,
            width,
            height - contentTop);
        return bitmap;
    }
    
    /**
     * 去除指定高度
     * @param b
     * @param activity
     * @param h
     * @return
     */
    public static Bitmap delDisplayHeight(Bitmap b, Activity activity, int h)
    {
        // 获取屏幕长和高
        int width = activity.getWindowManager().getDefaultDisplay().getWidth();
        int height = activity
            .getWindowManager()
                .getDefaultDisplay()
                .getHeight();
        // 去掉标题栏
        Bitmap bitmap = Bitmap.createBitmap(b,
            0,
            h,
            width,
            height - h);
        return bitmap;
    }

    /**
     * 保存图片到sdcard
     * @param b
     * @param strFileName
     */
    public static void savePic(Bitmap b, String strFileName)
    {
        FileOutputStream fos = null;
        try
        {
            fos = new FileOutputStream(strFileName);
            if (null != fos)
            {
                b.compress(Bitmap.CompressFormat.PNG,
                    90,
                    fos);
                fos.flush();
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
        finally
        {
            try
            {
                if (b != null && !b.isRecycled())
                {
                    b.recycle();
                    b = null;
                    System.gc();
                }
                if (fos != null)
                {
                    fos.close();
                }
            }
            catch (Exception e2)
            {
                LogUtil.e(TAG, e2);
            }
        }
    }
}
