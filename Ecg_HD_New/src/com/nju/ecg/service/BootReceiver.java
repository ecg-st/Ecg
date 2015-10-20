package com.nju.ecg.service;

import com.nju.ecg.wave.EcgWelcomeScreen;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
/**
 * 监听开机动作, 自动启动
 * @author zhuhf
 * @version [ME MTVClient_Handset V100R001C04SPC002, 2013-2-21]
 */
public class BootReceiver extends BroadcastReceiver
{

    @Override
    public void onReceive(Context context, Intent intent)
    {
        Intent startIntent = new Intent(context, EcgWelcomeScreen.class);
        startIntent.setAction("android.intent.action.MAIN");
        startIntent.addCategory("android.intent.category.LAUNCHER");
        /* 
         * If activity is not launched in Activity environment, this flag is 
         * mandatory to set 
         */ 
        startIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
        context.startActivity(startIntent);
    }
}
