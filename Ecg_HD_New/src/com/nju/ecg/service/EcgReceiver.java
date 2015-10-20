package com.nju.ecg.service;

import com.nju.ecg.utils.EcgIntent;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class EcgReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
        if(action == null) return;
        Log.v("ecg", "EcgReceiver, action = " + action);
        
        if(EcgIntent.ECG_SERVICE_STARTED.equals(action)){
        	EcgApp.getInstance().bindEcgService();
        }
	}

}
