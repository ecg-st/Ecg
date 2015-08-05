package com.nju.ecg.wave;

public interface DemoModeResultListener
{
    public abstract void onResult(int selectedDemo, ModeAction action);
    
    public enum ModeAction
    {
        ACTION_SHOW,//演示
        ACTION_DETAIl//查看详情
    }
}
