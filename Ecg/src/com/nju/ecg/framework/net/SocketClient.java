package com.nju.ecg.framework.net;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.net.Socket;

import android.os.Handler;

import com.nju.ecg.service.EcgApp;
import com.nju.ecg.utils.EcgConst;
import com.nju.ecg.utils.LogUtil;

/**
 * @author zhuhf
 * @version [ME MTVClient_Handset V100R001C04SPC002, 2012-10-26]
 */
public class SocketClient
{
    private static final String TAG = "SocketClient";
    private Handler hanlder;
    private HttpDataListener listener;
    public void uploadWaveData(String filePath, double longitude, double latitude, String addressInfo, String imei, HttpDataListener listener) 
    {
        this.listener = listener;
        hanlder = new Handler();
        try
        {
            new UploadThread(imei, longitude, latitude, addressInfo, filePath).start();
        }
        catch (Exception e)
        {
            LogUtil.e(TAG,
                e);
        }
    }
    
    private class UploadThread extends Thread
    {
        private String mImei;
        private double mLongitude;
        private double mLatitude;
        private String mAddressInfo;
        private String mFilePath;
        public UploadThread(String imei, double longitude, double latitude, String addressInfo, String filePath)
        {
            mImei = imei;
            mLongitude = longitude;
            mLatitude = latitude;
            mAddressInfo = addressInfo;
            mFilePath = filePath;
        }
        @Override
        public void run()
        {
            Socket socket = null;
            DataOutputStream dos = null;
            DataInputStream dis = null;
            try
            {
                socket = new Socket(EcgApp.getInstance().getServerIp(), EcgConst.PATIENT_SERVER_PORT);
                dos = new DataOutputStream(socket.getOutputStream());
                dis = new DataInputStream(socket.getInputStream());
                dos.writeUTF(mImei);
                dos.writeDouble(mLongitude);
                dos.writeDouble(mLatitude);
                dos.writeUTF(mAddressInfo);
                byte[] buffer = new byte[1024];
                DataInputStream fis = null;
                try
                {
                    fis = new DataInputStream(new FileInputStream(mFilePath));
                    int len = -1;
                    while ((len = fis.read(buffer)) != -1)
                    {
                        dos.write(buffer,
                            0,
                            len);
                    }
                    
                    hanlder.post(new Runnable()
                    {
                        
                        @Override
                        public void run()
                        {
                            listener.actionSuccess();
                        }
                    });
                }
                catch (Exception e)
                {
                    LogUtil.e(TAG,
                        e);
                    hanlder.post(new Runnable()
                    {
                        
                        @Override
                        public void run()
                        {
                            listener.actionFailure();
                        }
                    });
                }
                finally
                {
                    try
                    {
                        if (null != fis)
                        {
                            fis.close();
                        }
                    }
                    catch (Exception e2)
                    {
                        LogUtil.e(TAG,
                            e2);
                    }
                }
            }
            catch (Exception e)
            {
                LogUtil.e(TAG, e);
                hanlder.post(new Runnable()
                {
                    
                    @Override
                    public void run()
                    {
                        listener.actionFailure();
                    }
                });
            }
            finally
            {
                try
                {
                    if (null != dis)
                    {
                        dis.close();
                    }
                    if (null != dos)
                    {
                        dos.close();
                    }
                    if (null != socket)
                    {
                        socket.close();
                    }
                }
                catch (Exception e2)
                {
                    LogUtil.e(TAG,
                        e2);
                }
            }
        }
    }
}
