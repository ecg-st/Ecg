package com.nju.ecg.wave;

import java.io.File;
import java.util.Calendar;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Window;

import com.nju.ecg.R;
import com.nju.ecg.basic.BasicActivity;
import com.nju.ecg.service.EcgApp;
import com.nju.ecg.utils.EcgConst;

/***
 * 欢迎界面
 * 
 * @author zhuhf
 * @version [ME MTVClient_Handset V100R001C04SPC002, 2012-9-3]
 */
public class EcgWelcomeScreen extends BasicActivity
{
    private static final String TAG = "EcgWelcomeScreen";

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.welcome_screen);
        /**
         *  启动画面延迟2秒进入主界面
         */
        new Handler().postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
//                Calendar calendar = Calendar.getInstance();
//                calendar.set(Calendar.YEAR, 2014);
//                calendar.set(Calendar.MONTH, 8);
//                calendar.set(Calendar.DAY_OF_MONTH, 30);
//                if (System.currentTimeMillis() > calendar.getTimeInMillis())
//                {
//                    showDialog("提示", "产品试用期已经截止, 请联系我们！");
//                    return;
//                }
//                String deviceId = getDeviceId();
//                if (TextUtils.isEmpty(deviceId))
//                {
//                    showDialog("提示", "未检测到设备序列号, 请联系生产厂商！");
//                }
//                else if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
//                {
//                    showToast("设备存储卡不可用, 请检查后再试！");
//                    return;
//                }
//                else
//                {
//                    File dirFile = new File(EcgConst.AUTHORIZED_DOCUMENT_DIR);
//                    if (!dirFile.exists())
//                    {
//                        dirFile.mkdirs();
//                    }
//                    File authorizedFile = new File(dirFile, deviceId);
//                    if (!authorizedFile.exists())
//                    {
//                        showDialog("设备序列号：" + deviceId, dirFile.getAbsolutePath() + "目录下未找到对应的授权文件, 请确认后再使用本软件！");
//                    }
//                    else if (!EcgApp.getInstance().isDeviceAuthorized(deviceId, authorizedFile.getAbsolutePath()))
//                    {
//                        showDialog("设备序列号：" + deviceId, "授权文件非法！ 请确认" + 
//                                dirFile.getAbsolutePath() + "目录下的" + deviceId + "授权文件为当前设备的合法授权文件, 请确认后再使用本软件！");
//                    }
//                    else
//                    {
                        Intent intent = new Intent(EcgWelcomeScreen.this,
                                WaveScreen.class);
                        intent.putExtra("mode",
                                0);
                        startActivity(intent);
                        finish();
//                    }
//                }
            }
        }, 2000);
    }
    
    public void showDialog(String title, String message)
    {
        new AlertDialog.Builder(this).setTitle(title).setMessage(message)
        .setPositiveButton("确认", new OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                finish();
            }
        }).setNegativeButton("取消", new OnClickListener()
        {
            
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                finish();
            }
        }).create().show();
    }
    
    /**
     * 获取IMEI
     * 
     * @return
     */
    public String getDeviceId() {
        try {
            TelephonyManager tm = (TelephonyManager) this
                    .getSystemService(Context.TELEPHONY_SERVICE);
            return tm.getDeviceId();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_BACK)
        {
            finishAll();
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(0);
        }
        return super.onKeyDown(keyCode,
            event);
    }

}
