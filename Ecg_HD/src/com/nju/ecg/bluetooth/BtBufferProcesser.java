package com.nju.ecg.bluetooth;

import java.util.ArrayList;
import java.util.List;

import com.nju.ecg.utils.LogUtil;
import com.nju.ecg.wave.EcgWaveData;
/**
 * 蓝牙缓冲处理器
 * @author zhuhf
 * @version [ME MTVClient_Handset V100R001C04SPC002, 2013-1-12]
 */
public class BtBufferProcesser
{
    private BufferThread bufferThread;
    private final static String TAG = "BtBufferProcesser";
    private static BtBufferProcesser sInstance;
    
    private BtBufferProcesser()
    {
        bufferThread = new BufferThread();
        bufferThread.start();
    }
    
    public static BtBufferProcesser getInstatce()
    {
        if (sInstance == null)
        {
            sInstance = new BtBufferProcesser();
        }
        return sInstance;
    }
    
    public void clear()
    {
        bufferThread.clear();
    }
    
    public void pauseThread()
    {
        bufferThread.pauseThread();
    }
    
    public void resumeThread()
    {
        bufferThread.resumeThread();
    }
    
    public void save(byte[] src)
    {
        bufferThread.log(src);
    }
    
    private class BufferThread extends Thread
    {
        private List<byte[]> btBufferList = new ArrayList<byte[]>();
        private boolean paused;
        
        public BufferThread()
        {
        }
        
        public void clear()
        {
            btBufferList.clear();
            // 初始化数据
            hasFindFirstPH = false;
            btBuffer = new byte[BT_BUFFER_LENGTH];
            btLength = 0;
            lastHeadIndex = -1;
            lastFrameNumber = -1;
        }
        
        public void pauseThread()
        {
            paused = true;
            clear();
        }
        
        public void resumeThread()
        {
            paused = false;
        }
        
        public synchronized void log(byte[] src)
        {
            synchronized (btBufferList)
            {
                btBufferList.add(src);
            }
            notify();
        }
        
        @Override
        public void run()
        {
            while(true)
            {
                byte[] item = null;
                synchronized (btBufferList) {
                    if (btBufferList.size() > 0)
                    {
                        item = btBufferList.get(0);
                    }
                    // btBufferList有时候index==0的位置为Null, 暂时不知道原因, 先这样修复.
                    while (item == null && btBufferList.size() > 0)
                    {
                        btBufferList.remove(0);
                        item = btBufferList.get(0);
                    }
                }

                if (item == null) {
                    //No log item, wait
                    synchronized (this) {
                        try {
                            wait();
                        } catch (InterruptedException e) {
                        }
                    }
                } else {
                    long processBtStart = System.currentTimeMillis();
                    processBtData(item);
                    LogUtil.d(LogUtil.TIME_TAG, "处理蓝牙数据耗时 >>> " + (System.currentTimeMillis() - processBtStart));
                    if (btBufferList.size() > 0)
                    {
                        btBufferList.remove(0);
                    }
                    if (paused)
                    {
                        clear();
                    }
                }
            }
        }
    }
    
    /**
     * 以下是新增功能, 增加对蓝牙数据正确性的校验
     */
    /** 数据包帧头长度*/
    private static final int PACKAGE_HEAD_LENGTH = 3;
    /** 数据包帧号长度*/
    private static final int PACKAGE_NUMBER_LENGTH = 1;
    /** 数据包数据长度*/
    private static final int PACKAGE_DATA_LENGTH = 256;
    /** 数据包校验长度*/
    private static final int PACKAGE_CHECK_LENGTH = 1;
    /** 数据包长度*/
    private static final int PACKAGE_LENGTH = PACKAGE_HEAD_LENGTH + PACKAGE_NUMBER_LENGTH + PACKAGE_DATA_LENGTH + PACKAGE_CHECK_LENGTH;
    /** 是否查找到第一个数据包头标志位*/
    private boolean hasFindFirstPH;
    private static final int BT_BUFFER_LENGTH = 2048;
    /** 蓝牙数据缓冲区*/
    private byte[] btBuffer = new byte[BT_BUFFER_LENGTH];
    /** 收到的蓝牙数据长度*/
    private int btLength;
    /** 上一个包头的下标*/
    private int lastHeadIndex = -1;
    /** 上一帧的帧号, -1标识未同步帧号*/
    private int lastFrameNumber = -1;
    
    /**
     * 处理蓝牙数据
     * @param src 原始数据
     */
    private void processBtData(byte[] src)
    {
        if (!hasFindFirstPH) // 未找到第一个数据包包头
        {
            if (btLength + src.length > BT_BUFFER_LENGTH) // 防止缓冲区溢出
            {
                btBuffer = new byte[BT_BUFFER_LENGTH];
                btLength = 0;
            }
            // 将数据存入缓冲区
            System.arraycopy(src, 0, btBuffer, btLength, src.length);
            btLength += src.length;
//            LogUtil.d(TAG, "查找第一个数据包头");
            int firstHeadIndex = findFirstPH();
            if (firstHeadIndex != -1)
            {
                LogUtil.d(TAG, "找到第一个数据包头 >> firstHeadIndex: " + firstHeadIndex);
                // 取出从第一个包头(包括第一个包头)开始的数据
                byte[] datas = new byte[btLength - firstHeadIndex];
                System.arraycopy(btBuffer, firstHeadIndex, datas, 0, datas.length);
                hasFindFirstPH = true;
                // 数据存入缓冲区
                btBuffer = new byte[BT_BUFFER_LENGTH];
                btLength = 0;
                System.arraycopy(datas, 0, btBuffer, 0, datas.length);
                btLength = datas.length;
                lastHeadIndex = 0;
            }
            else
            {
                LogUtil.d(TAG, "未找到第一个数据包头 >> firstHeadIndex: " + firstHeadIndex);
            }
        }
        else
        {
//            LogUtil.d(TAG, "缓冲区字节数: " + btLength);
            while (btLength >= PACKAGE_LENGTH + PACKAGE_HEAD_LENGTH) // 保证缓冲区不会超过264个字节, 防止缓冲区无限制暴涨溢出
            {
//                LogUtil.d(TAG, "缓冲区超过264个字节");
                int nextHeadIndex = findNextPH();
                if (nextHeadIndex != -1) // 找到下一个包头
                {
                    LogUtil.d(TAG, "找到下一个数据包头 >> nextHeadIndex: " + nextHeadIndex);
                    // 取出两个包头之间的数据
                    byte[] datas = new byte[nextHeadIndex - lastHeadIndex - PACKAGE_HEAD_LENGTH];
                    System.arraycopy(btBuffer, lastHeadIndex + PACKAGE_HEAD_LENGTH, datas, 0, datas.length);
                    LogUtil.d(TAG, "两个包头之间的数据长度：" + datas.length);
                    // 分离帧号、数据、校验字节
                    int frameNumber = datas[0] >= 0 ? datas[0] : datas[0] + 256;
  //                  byte[] data = new byte[datas.length - 2];
  //                  System.arraycopy(datas, 1, data, 0, datas.length - 2);
  //                  byte check = datas[datas.length - 1];
                    byte[] data = new byte[0];
                    if (datas.length - 2 >= 0)
                    {
                        data = new byte[datas.length - 2];
                        System.arraycopy(datas, 1, data, 0, datas.length - 2);
                    }
                    byte check = 0;
                    if (datas.length - 1 >= 0)
                    {
                        check = datas[datas.length - 1];
                    }
//                    LogUtil.d(TAG, "分离两个包头之间的数据 >> 帧号: " + frameNumber + " 数据长度: " + data.length + " 校验位: " + check);
                    // 将从下一个包头(包括当前包头)开始的数据移到btBuffer开头
                    byte[] nextData = new byte[btLength - nextHeadIndex];
                    System.arraycopy(btBuffer, nextHeadIndex, nextData, 0, nextData.length);
                    btBuffer = new byte[BT_BUFFER_LENGTH];
                    System.arraycopy(nextData, 0, btBuffer, 0, nextData.length);
                    btLength = nextData.length;
                    // 判断数据长度是否正确及校验和是否正确
                    if (checkData(data, check)) // 正确
                    {
                        LogUtil.d(TAG, "数据长度及校验和都正确");
                        if (lastFrameNumber != -1) // 本地包序号已同步(0x00≤N≤0xff循环, 跳帧则说明丢帧)
                        {
                            LogUtil.d(TAG, "本地包序号已同步 >> 本地包序号: " + lastFrameNumber + " 当前包序号: " + frameNumber);
                            if ((lastFrameNumber == 255 && frameNumber == 0) || (frameNumber == (lastFrameNumber + 1)))
                            {
                                LogUtil.d(TAG, "包序号正确");
                                // 更新本地包序号
                                lastFrameNumber = frameNumber;
                                // 保存数据
                                EcgWaveData.saveData(data);
                            }
                            else
                            {
                                LogUtil.d(TAG, "包序号不正确, 丢包");
                                // 先生成全0数据包并保存
                                byte[] zeroData = new byte[PACKAGE_DATA_LENGTH];
                                EcgWaveData.saveData(zeroData);
                                // 再生成当前数据包并保存, 更新本地包序号
                                EcgWaveData.saveData(data);
                                // 更新本地包序号
                                lastFrameNumber = frameNumber;
                            }
                        }
                        else
                        {
                            LogUtil.d(TAG, "本地包序号未同步 >> 本地包序号: " + lastFrameNumber + " 当前包序号: " + frameNumber);
                            // 更新本地包序号
                            lastFrameNumber = frameNumber;
                            // 保存数据
                            EcgWaveData.saveData(data);
                        }
                    }
                    else
                    {
                        LogUtil.d(TAG, "数据长度、校验和不正确");
                        // 生成全0数据包并保存
                        byte[] zeroData = new byte[PACKAGE_DATA_LENGTH];
                        EcgWaveData.saveData(zeroData);
                        // 本地包序号变为未同步状态
                        lastFrameNumber = -1;
                    }
                    lastHeadIndex = 0;
                }
                else
                {
                    LogUtil.d(TAG, "未找到下一个数据包头 >> nextHeadIndex: " + nextHeadIndex);
                    btBuffer = new byte[BT_BUFFER_LENGTH];
                    btLength = 0;
                }
//                // 此次的数据存入缓冲区
//                System.arraycopy(src, 0, btBuffer, btLength, src.length);
//                btLength += src.length;
            }
//            LogUtil.d(TAG, "缓冲区少于264个字节");
            System.arraycopy(src, 0, btBuffer, btLength, src.length);
            btLength += src.length;
//            LogUtil.d(TAG, "存入缓冲区>>" + src.length +"<<个字节数");
        }
    }
    
    /**
     * 查找第一个数据包头(0xF0,0x81,0x82), 返回起始位置
     * @param src
     * @return -1表示未找到
     */
    private int findFirstPH()
    {
        for (int i = 0; i <= btLength - PACKAGE_HEAD_LENGTH; i++)
        {
            if (btBuffer[i] == -16 && btBuffer[i + 1] == -127 && btBuffer[i + 2] == -126)
            {
                return i;
            }
        }
        return -1;
    }
    
    /**
     * 查找下一个数据包头(0xF0,0x81,0x82), 返回起始位置
     * @return -1表示未找到
     */
    private int findNextPH()
    {
        for (int i = PACKAGE_HEAD_LENGTH; i <= btLength - PACKAGE_HEAD_LENGTH; i++)
        {
            if (btBuffer[i] == -16 && btBuffer[i + 1] == -127
                && btBuffer[i + 2] == -126)
            {
                return i;
            }
        }
        return -1;
    }
    
    /**
     * 校验数据长度及校验和是否正确
     * @param data 数据
     * @param check 校验位
     * @return
     */
    private boolean checkData(byte[] data, byte check)
    {
        byte dataSum = 0;
        for (byte d : data)
        {
            dataSum += d;
        }
        LogUtil.d(TAG, "检验数据长度及和校验 >> 数据长度: " + data.length + " 数据总和: " + dataSum + " 校验位: " + check);
        if (data.length == PACKAGE_DATA_LENGTH && dataSum == check)
        {
            return true;
        }
        else
        {
            return false;
        }
    }
}
