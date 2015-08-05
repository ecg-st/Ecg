package com.nju.ecg.bluetooth;

import java.util.ArrayList;
import java.util.List;

import com.nju.ecg.utils.LogUtil;
import com.nju.ecg.wave.EcgWaveData;
/**
 * �������崦����
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
            // ��ʼ������
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
                    // btBufferList��ʱ��index==0��λ��ΪNull, ��ʱ��֪��ԭ��, �������޸�.
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
                    processBtData(item);
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
     * ��������������, ���Ӷ�����������ȷ�Ե�У��
     */
    /** ���ݰ�֡ͷ����*/
    private static final int PACKAGE_HEAD_LENGTH = 3;
    /** ���ݰ�֡�ų���*/
    private static final int PACKAGE_NUMBER_LENGTH = 1;
    /** ���ݰ����ݳ���*/
    private static final int PACKAGE_DATA_LENGTH = 256;
    /** ���ݰ�У�鳤��*/
    private static final int PACKAGE_CHECK_LENGTH = 1;
    /** ���ݰ�����*/
    private static final int PACKAGE_LENGTH = PACKAGE_HEAD_LENGTH + PACKAGE_NUMBER_LENGTH + PACKAGE_DATA_LENGTH + PACKAGE_CHECK_LENGTH;
    /** �Ƿ���ҵ���һ�����ݰ�ͷ��־λ*/
    private boolean hasFindFirstPH;
    private static final int BT_BUFFER_LENGTH = 2048;
    /** �������ݻ�����*/
    private byte[] btBuffer = new byte[BT_BUFFER_LENGTH];
    /** �յ����������ݳ���*/
    private int btLength;
    /** ��һ����ͷ���±�*/
    private int lastHeadIndex = -1;
    /** ��һ֡��֡��, -1��ʶδͬ��֡��*/
    private int lastFrameNumber = -1;
    
    /**
     * ������������
     * @param src ԭʼ����
     */
    private void processBtData(byte[] src)
    {
        if (!hasFindFirstPH) // δ�ҵ���һ�����ݰ���ͷ
        {
            if (btLength + src.length > BT_BUFFER_LENGTH) // ��ֹ���������
            {
                btBuffer = new byte[BT_BUFFER_LENGTH];
                btLength = 0;
            }
            // �����ݴ��뻺����
            System.arraycopy(src, 0, btBuffer, btLength, src.length);
            btLength += src.length;
            LogUtil.d(TAG, "���ҵ�һ�����ݰ�ͷ");
            int firstHeadIndex = findFirstPH();
            if (firstHeadIndex != -1)
            {
                LogUtil.d(TAG, "�ҵ���һ�����ݰ�ͷ >> firstHeadIndex: " + firstHeadIndex);
                // ȡ���ӵ�һ����ͷ(������һ����ͷ)��ʼ������
                byte[] datas = new byte[btLength - firstHeadIndex];
                System.arraycopy(btBuffer, firstHeadIndex, datas, 0, datas.length);
                hasFindFirstPH = true;
                // ���ݴ��뻺����
                btBuffer = new byte[BT_BUFFER_LENGTH];
                btLength = 0;
                System.arraycopy(datas, 0, btBuffer, 0, datas.length);
                btLength = datas.length;
                lastHeadIndex = 0;
            }
            else
            {
                LogUtil.d(TAG, "δ�ҵ���һ�����ݰ�ͷ >> firstHeadIndex: " + firstHeadIndex);
            }
        }
        else
        {
            LogUtil.d(TAG, "�������ֽ���: " + btLength);
            while (btLength >= PACKAGE_LENGTH + PACKAGE_HEAD_LENGTH) // ��֤���������ᳬ��264���ֽ�, ��ֹ�����������Ʊ������
            {
                LogUtil.d(TAG, "����������264���ֽ�");
                int nextHeadIndex = findNextPH();
                if (nextHeadIndex != -1) // �ҵ���һ����ͷ
                {
                    LogUtil.d(TAG, "�ҵ���һ�����ݰ�ͷ >> nextHeadIndex: " + nextHeadIndex);
                    // ȡ��������ͷ֮�������
                    byte[] datas = new byte[nextHeadIndex - lastHeadIndex - PACKAGE_HEAD_LENGTH];
                    System.arraycopy(btBuffer, lastHeadIndex + PACKAGE_HEAD_LENGTH, datas, 0, datas.length);
                    LogUtil.d(TAG, "������ͷ֮������ݳ��ȣ�" + datas.length);
                    // ����֡�š����ݡ�У���ֽ�
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
                    LogUtil.d(TAG, "����������ͷ֮������� >> ֡��: " + frameNumber + " ���ݳ���: " + data.length + " У��λ: " + check);
                    // ������һ����ͷ(������ǰ��ͷ)��ʼ�������Ƶ�btBuffer��ͷ
                    byte[] nextData = new byte[btLength - nextHeadIndex];
                    System.arraycopy(btBuffer, nextHeadIndex, nextData, 0, nextData.length);
                    btBuffer = new byte[BT_BUFFER_LENGTH];
                    System.arraycopy(nextData, 0, btBuffer, 0, nextData.length);
                    btLength = nextData.length;
                    // �ж����ݳ����Ƿ���ȷ��У����Ƿ���ȷ
                    if (checkData(data, check)) // ��ȷ
                    {
                        LogUtil.d(TAG, "���ݳ��ȼ�У��Ͷ���ȷ");
                        if (lastFrameNumber != -1) // ���ذ������ͬ��(0x00��N��0xffѭ��, ��֡��˵����֡)
                        {
                            LogUtil.d(TAG, "���ذ������ͬ�� >> ���ذ����: " + lastFrameNumber + " ��ǰ�����: " + frameNumber);
                            if ((lastFrameNumber == 255 && frameNumber == 0) || (frameNumber == (lastFrameNumber + 1)))
                            {
                                LogUtil.d(TAG, "�������ȷ");
                                // ���±��ذ����
                                lastFrameNumber = frameNumber;
                                // ��������
                                EcgWaveData.saveData(data);
                            }
                            else
                            {
                                LogUtil.d(TAG, "����Ų���ȷ, ����");
                                // ������ȫ0���ݰ�������
                                byte[] zeroData = new byte[PACKAGE_DATA_LENGTH];
                                EcgWaveData.saveData(zeroData);
                                // �����ɵ�ǰ���ݰ�������, ���±��ذ����
                                EcgWaveData.saveData(data);
                                // ���±��ذ����
                                lastFrameNumber = frameNumber;
                            }
                        }
                        else
                        {
                            LogUtil.d(TAG, "���ذ����δͬ�� >> ���ذ����: " + lastFrameNumber + " ��ǰ�����: " + frameNumber);
                            // ���±��ذ����
                            lastFrameNumber = frameNumber;
                            // ��������
                            EcgWaveData.saveData(data);
                        }
                    }
                    else
                    {
                        LogUtil.d(TAG, "���ݳ��ȡ�У��Ͳ���ȷ");
                        // ����ȫ0���ݰ�������
                        byte[] zeroData = new byte[PACKAGE_DATA_LENGTH];
                        EcgWaveData.saveData(zeroData);
                        // ���ذ���ű�Ϊδͬ��״̬
                        lastFrameNumber = -1;
                    }
                    lastHeadIndex = 0;
                }
                else
                {
                    LogUtil.d(TAG, "δ�ҵ���һ�����ݰ�ͷ >> nextHeadIndex: " + nextHeadIndex);
                    btBuffer = new byte[BT_BUFFER_LENGTH];
                    btLength = 0;
                }
//                // �˴ε����ݴ��뻺����
//                System.arraycopy(src, 0, btBuffer, btLength, src.length);
//                btLength += src.length;
            }
            LogUtil.d(TAG, "����������264���ֽ�");
            System.arraycopy(src, 0, btBuffer, btLength, src.length);
            btLength += src.length;
            LogUtil.d(TAG, "���뻺����>>" + src.length +"<<���ֽ���");
        }
    }
    
    /**
     * ���ҵ�һ�����ݰ�ͷ(0xF0,0x81,0x82), ������ʼλ��
     * @param src
     * @return -1��ʾδ�ҵ�
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
     * ������һ�����ݰ�ͷ(0xF0,0x81,0x82), ������ʼλ��
     * @return -1��ʾδ�ҵ�
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
     * У�����ݳ��ȼ�У����Ƿ���ȷ
     * @param data ����
     * @param check У��λ
     * @return
     */
    private boolean checkData(byte[] data, byte check)
    {
        byte dataSum = 0;
        for (byte d : data)
        {
            dataSum += d;
        }
        LogUtil.d(TAG, "�������ݳ��ȼ���У�� >> ���ݳ���: " + data.length + " �����ܺ�: " + dataSum + " У��λ: " + check);
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
