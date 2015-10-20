package com.nju.ecg.basic;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.database.ContentObserver;
import android.os.Bundle;
import android.widget.Toast;

/**
 * 所有Activity的基类
 * @author zhuhf
 * @version [ME MTVClient_Handset V100R001C04SPC002, 2012-9-4]
 */
public class BasicActivity extends Activity
{
    /** 保存所有创建的Activity*/
    private static List<Activity> activityStack = new ArrayList<Activity>();
    /** 基类Toast*/
    private Toast mToast;
    /** 观察者*/
    private ContentObserver observer;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        activityStack.add(this);
    }
    
    /**
     * 销毁所有的Activity
     */
    protected void finishAll()
    {
        for (Activity activity : activityStack)
        {
            activity.finish();
        }
    }
    
    /**
     * 
     * 根据字符串 show toast<BR>
     * @param message 字符串
     */
    public void showToast(CharSequence message)
    {
        if (mToast == null)
        {
            mToast = Toast.makeText(this,
                message,
                Toast.LENGTH_SHORT);
        }
        else
        {
//            mToast.cancel(); //4.0若有这句话,则Toast无法显示
            mToast.setText(message);
        }
        mToast.show();
    }
    
    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        if (observer != null)
        {
            getContentResolver().unregisterContentObserver(observer);
        }
    }
}
