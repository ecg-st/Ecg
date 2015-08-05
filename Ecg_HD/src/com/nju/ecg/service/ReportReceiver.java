package com.nju.ecg.service;

import com.nju.ecg.utils.LogUtil;
import com.nju.ecg.wave.WaveScreen;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
/**
 * 心率定时截屏广播
 * @author zhuhf
 */
public class ReportReceiver extends BroadcastReceiver {
    private static final String TAG = "ReportReceiver";
	@Override
	public void onReceive(Context context, Intent intent) {
		LogUtil.d(TAG, "收到广播 >>> 持续采集一分钟, 停止保存心率截屏");
        WaveScreen.needsWaveShot = false;
	}
}
