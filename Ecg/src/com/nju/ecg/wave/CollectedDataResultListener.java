package com.nju.ecg.wave;

import com.nju.ecg.model.WaveData;

/**
 * 查看历史数据触发的回调接口
 * @author zhuhf
 * @version [ME MTVClient_Handset V100R001C04SPC002, 2012-9-16]
 */
public interface CollectedDataResultListener
{
    public abstract void onResult(int index, WaveData data, Action action);

    public enum Action
    {
        ACTION_REPLAY, // 回放
        ACTION_DIAGNOSE, // 检测报告
        ACTION_DELETE // 删除
    }
}
