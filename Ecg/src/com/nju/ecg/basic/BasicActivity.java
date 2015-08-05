package com.nju.ecg.basic;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
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
    protected ContentObserver collectObserver;
    private ProgressDialog progressDialog;
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
    
    public void showProgress(String title, String message)
    {
        if (progressDialog == null)
        {
            progressDialog = new ProgressDialog(this);
            progressDialog.setCanceledOnTouchOutside(false);
        }
        else if (progressDialog.isShowing())
        {
            progressDialog.dismiss();
        }
        progressDialog.setTitle(title);
        progressDialog.setMessage(message);
        progressDialog.show();
    }
    
    public void hideProgress()
    {
        if (progressDialog != null && progressDialog.isShowing())
        {
            progressDialog.dismiss();
        }
    }
    
    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        if (collectObserver != null)
        {
            getContentResolver().unregisterContentObserver(collectObserver);
        }
        hideProgress();
    }
}
