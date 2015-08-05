package com.nju.ecg.framework.net;
/**
 * 网络请求回调处理
 * @author zhuhf
 * @version [ME MTVClient_Handset V100R001C04SPC002, 2012-10-26]
 */
public interface HttpDataListener
{
    public abstract void actionSuccess();
    public abstract void actionFailure();
}
