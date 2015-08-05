package com.nju.ecg.wave;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import android.util.Log;

import com.nju.ecg.service.EcgApp;
import com.nju.ecg.utils.EcgConst;
import com.nju.ecg.utils.LogUtil;


public class EcgWaveData {
    private static final String TAG = "EcgWaveData";
    private static boolean mReady = false;
    private static ProcessDataThread mProcessThread;
    
	public static LinkedList<int[]> mWaveDataCh1 = new LinkedList<int[]>();
	public static LinkedList<int[]>	mWaveDataCh2 = new LinkedList<int[]>();
    public static ArrayList<ArrayList<int[]>> mWaveData = new ArrayList<ArrayList<int[]>>();
    
    /** 第一通道数据*/
    public static List<int[]> leadFirstData = new ArrayList<int[]>();
    /** 第二通道数据*/
    public static List<int[]> leadSecondData = new ArrayList<int[]>();
    
    public static void init() { 
        mProcessThread = new ProcessDataThread();
        mProcessThread.start();
        mReady = true;
    }

    public static void destroy() {
        if (mReady) {
            mReady = false;
            mProcessThread.stopThread();
            mProcessThread = null;
        }
    }
	
    public static void saveData(byte[] msg) {
        if (mReady) {
            mProcessThread.Log(msg);
        }
    }
    
    public static void pauseThread() {
        mProcessThread.firstFindLead = false;
//        for(int i=0; i < mProcessThread.rawData.length; i++) {
//        	mProcessThread.rawData[i] = 0;
//        }
        mProcessThread.pauseThread();
    }
    
    public static void clearAnalyseData()
    {
        mProcessThread.clearAnalyseData();
    }
    
    public static void resumeThread() {
        mProcessThread.resumeThread();
    }
    
    public static void clear()
    {
        mProcessThread.clear();
    }

	public static class ProcessDataThread extends Thread {
		private static final int LOG_OK = 0;
		private static final int LOG_FAILED = -1;
		
		private boolean paused = false;
		private boolean closed = false;
		private List<byte[]> mData = new ArrayList<byte[]>();
        private String tag;
		
		public ProcessDataThread() {
			for(int i = 0; i < CHANNEL_NUMBER; i++) {
				mWaveData.add(i, new ArrayList<int[]>());
			}
		}
		public synchronized void stopThread() {
		   closed = true;
		   notify();
		}
		
		public void clear()
        {
            synchronized (mData) {
                mData.clear();
            } 
        }
		
		public synchronized void pauseThread()
		{
		    notify();
		    paused = true;
		}
	
        public synchronized void resumeThread()
        {
            paused = false;
            notify();
        }
		public synchronized void Log(byte[] msg) {
			synchronized (mData) {
				mData.add(msg);
			}  	
			notify();
		}
		  
		public void run() {
			while (true) {
            	android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_LOWEST);
				synchronized (this) {
					if (paused) {
//						continue;
					    try {
                            wait();
                        } catch (InterruptedException e) {
                            LogUtil.e(TAG, e);
                        }
					}
                    tag = new String(WaveScreen.currentTag);
				}
				
				byte[] item = null;
				synchronized (mData) {
				    if (mData.size() > 0)
				    {
				        item = mData.get(0);
				        // mData有时候index==0的位置为Null, 暂时不知道原因, 先这样修复.
                        while (item == null && mData.size() > 0)
                        {
                            mData.remove(0);
                            item = mData.get(0);
                        }
				    }
				}
		
				if (item == null) {
					//No log item, wait
					synchronized (this) {
						try {
							wait();
						} catch (InterruptedException e) {
						    LogUtil.e(TAG, e);
						}
					}
				} else {
					switch(processData(item)) {
					case LOG_OK:
						//Only removes when the log item is successfully write to file
					    if (mData.size() > 0)
					    {
					        mData.remove(0);
					    }
						 
						break;
					case LOG_FAILED:
						//Try again later
						synchronized (this) {
							try {
								wait(3000);
							} catch (InterruptedException e) {
							    LogUtil.e(TAG, e);
							}
						}
						break;
					}
				}

				synchronized (this) {
	                if (closed) {
	                    LogUtil.d(TAG, "Thread stoped");
	                    break;
	                }
	            }
			}
		
		}
		  
		byte[] rawData = new byte[RAW_DATA_BUF_LENGTH];
		public boolean firstFindLead = false; //TODO:
		int rawDataIndex = 0;
		synchronized private int processData(byte[] src) {
			int i;			
			int len = src.length;
			//First time, we need to find the lead number
//			if(!firstFindLead) {
//				for(i = 0; i < src.length; i++) {
//					if((int)src[i] < 0) {
//						firstFindLead = true;
//						len = src.length - i;
//						Log.v("raw", "firstFindLead:" + firstFindLead + " start:" + i);
//						break;
//					} 
//				}
//			}
			
			//Log.v("raw", "src data = " + TestDataHandle.bytes2HexString(src));
			while(len>0) {
				int copyLen = Math.min(len, RAW_DATA_BUF_LENGTH-rawDataIndex);
				System.arraycopy(src, src.length - len, rawData, rawDataIndex, copyLen);
				len = len - copyLen;
				rawDataIndex = rawDataIndex + copyLen;
				if(rawDataIndex >= RAW_DATA_BUF_LENGTH) {
					rawDataIndex = 0;
//					if(!checkLeadId(rawData)) break;
					
					//Save data to file 演示和回放数据时, 内部方法实际得不到执行
					EcgSaveData.saveData(rawData);
					for(i = 0; i < CHANNEL_NUMBER; i++){
						synchronized (mWaveData.get(i)) {
							int[] data = convertRawData2Int(rawData, i);
							if (data != null)
							{
							    mWaveData.get(i).add(data);
							}
						}
					}					
				} else {
					//rawData is not full
					break;
				}
			}
			
			return LOG_OK;
		}
		
		public void clearAnalyseData()
        {
//            ecgdata = new int[EcgConst.ECG_DATA_LENGTH];
            ecgdataIndex = 0;
            mData.clear();
        }

		final static int CHANNEL_NUMBER = EcgConst.LEADS_NUMBER; //Total number of channels
		public final static int RAW_DATA_BUF_LENGTH = 128 * CHANNEL_NUMBER;
		int[] ecgdata = new int[EcgConst.ECG_DATA_LENGTH];//EcgApp.getInstance().getEcgBinder().getEcgDataArray();
		static int ecgdataIndex = 0;
		
//		int[] ecgForFilterData = new int[EcgConst.ECG_DATE_FILTER_FOR_DISPALY_LENGTH];
//		static int ecgdataFilterIndex = 0;

		private int[] convertRawData2Int(byte[] rawData, int channel) {
			int[] ch = new int[RAW_DATA_BUF_LENGTH/CHANNEL_NUMBER/2];
			int chIndex = 0;
			//Log.v("raw", "rawData = " + TestDataHandle.bytes2HexString(rawData));
			//Log.v("raw", "channel = " + channel);
			for(int i = 0; i < rawData.length;) {
//				ch[chIndex++] = ((rawData[i + channel*2] & 0x7F) << 7 | (rawData[i+ channel*2+1]  & 0xFF));
			    // 新的蓝牙数据算法
			    int n1 = rawData[i + channel*2] & 0x0f; //去除高4位的通道号,by Huo
			    int n2 = rawData[i+ channel*2+1];
			    ch[chIndex++] = ((( n1>= 0 ? n1 : (n1 + 256))  << 8) | (n2 >= 0 ? n2 : (n2 + 256)));
				//Do not move 8 bits here, because the Msbit in the low byte is 0
				//Log.v("raw", "ch = " + Integer.toHexString(rawData[i] & 0x7F) + "  "  +Integer.toHexString(rawData[i+1]  & 0xFF)
				//		+ ", 0x" + Integer.toHexString(ch[chIndex -1]));												
				i = i + 2 * (CHANNEL_NUMBER);
			}
			
			// 判断如果含有数据0, 则废弃此段数据, 不进行分析同时清空滤波队列(必须保证原始数据没有0)
			boolean hasZero = false;
			
			for (int i=0; i<ch.length-4; i++) //modified by Huo
			{
			    if ( ch[i] + ch[i+1] + ch[i+2] + ch[i+3] == 0)
			    {
			    	hasZero = true;
			        break;
			    }
			}
			
			if(channel == 1) {
			    if (hasZero)
                {
                    ecgdataIndex = 0;
//                    ecgdata = new int[EcgConst.ECG_DATA_LENGTH];
                }
			    else
			    {
			        int leftLen = ch.length;
			        while(leftLen > 0) {
			            int copyLen = Math.min(leftLen, EcgConst.ECG_DATA_LENGTH-ecgdataIndex);
			            System.arraycopy(ch, ch.length - leftLen, ecgdata, ecgdataIndex, copyLen);
			            leftLen = leftLen - copyLen;
			            ecgdataIndex = ecgdataIndex + copyLen;
			            if(ecgdataIndex >= EcgConst.ECG_DATA_LENGTH) {
			                EcgApp.getInstance().getEcgBinder().setDataToCalHeartRate(ecgdata, new String(tag));
			                ecgdataIndex = 0;
//			                ecgdata = new int[EcgConst.ECG_DATA_LENGTH];
			            }
			        }	
			    }
			}
			//return ch;
			//int[] filterData = 
			//EcgApp.getInstance().getEcgBinder().filterEcgData(ch);
			//return avarageData(filterData);
			
			if (hasZero)
            {
				// 清空显示滤波队列，滤波输出直接设为0(modified by Huo)
			    leadFirstData.clear();
			    leadSecondData.clear();
			    int[] avaData = new int[ch.length / AVARAGE_POINTS];
			    for (int i = 0; i < avaData.length; i++)
			    {
			        avaData[i] = 0; //modified by Huo
			    }
			    return avaData;
            }
			
			// 对原始数据进行滤波处理
            if (channel == 0)
            {
                synchronized (leadFirstData)
                {
                    leadFirstData.add(ch);
                    if (leadFirstData.size() > 4)
                    {
                        int shortsrc[] = new int[ch.length * 5];
                        int shortdata_filter[] = new int[ch.length * 5];
                        int data_filter[] = new int[ch.length];
                        
                        int[] src1 = leadFirstData.get(0);
                        int[] src2 = leadFirstData.get(1);
                        int[] src3 = leadFirstData.get(2);
                        int[] src4 = leadFirstData.get(3);
                        int[] src5 = leadFirstData.get(4);
                        leadFirstData.remove(0);
                        System.arraycopy(src1,
                            0,
                            shortsrc,
                            0,
                            ch.length);
                        System.arraycopy(src2,
                            0,
                            shortsrc,
                            ch.length,
                            src2.length);
                        System.arraycopy(src3,
                            0,
                            shortsrc,
                            ch.length * 2,
                            src3.length);
                        System.arraycopy(src4,
                            0,
                            shortsrc,
                            ch.length * 3,
                            src4.length);
                        System.arraycopy(src5,
                            0,
                            shortsrc,
                            ch.length * 4,
                            src5.length);
                        DisplayFilter.my_filter_bp(shortsrc, shortdata_filter, shortdata_filter.length);
                        System.arraycopy(shortdata_filter,
                            ch.length * 4,
                            data_filter,
                            0,
                            ch.length);
                        return avarageData(data_filter);
                    }
                }
            }
            else if (channel == 1)
            {
                synchronized (leadSecondData)
                {
                    leadSecondData.add(ch);
                    if (leadSecondData.size() > 4)
                    {
                        int shortsrc[] = new int[ch.length * 5];
                        int shortdata_filter[] = new int[ch.length * 5];
                        int data_filter[] = new int[ch.length];
                        
                        int[] src1 = leadSecondData.get(0);
                        int[] src2 = leadSecondData.get(1);
                        int[] src3 = leadSecondData.get(2);
                        int[] src4 = leadSecondData.get(3);
                        int[] src5 = leadSecondData.get(4);
                        leadSecondData.remove(0);
                        System.arraycopy(src1,
                            0,
                            shortsrc,
                            0,
                            ch.length);
                        System.arraycopy(src2,
                            0,
                            shortsrc,
                            ch.length,
                            src2.length);
                        System.arraycopy(src3,
                            0,
                            shortsrc,
                            ch.length * 2,
                            src3.length);
                        System.arraycopy(src4,
                            0,
                            shortsrc,
                            ch.length * 3,
                            src4.length);
                        System.arraycopy(src5,
                            0,
                            shortsrc,
                            ch.length * 4,
                            src5.length);
                        DisplayFilter.my_filter_bp(shortsrc, shortdata_filter, shortdata_filter.length);
                        System.arraycopy(shortdata_filter,
                            ch.length * 4,
                            data_filter,
                            0,
                            ch.length);
                        return avarageData(data_filter);
                    }
                }
            }
			return null;
		}
		
		final static int AVARAGE_POINTS = EcgConst.AVERAGE_POINTS;
		private int[] avarageData(int[] src) {
			int[] avaData = new int[src.length / AVARAGE_POINTS];
			int sum = 0;
			for(int i = 0; i < src.length;) {
				for(int j = 0; j < AVARAGE_POINTS; j ++) {
					sum = sum + src[i + j];
				}
				avaData[i/AVARAGE_POINTS] = (sum / AVARAGE_POINTS) / 10;//>> 3; 1000point means 2mv (2*50)
				i = i + AVARAGE_POINTS;
				sum = 0;
			}
			return avaData;
		}
	
	private boolean checkLeadId(byte[] data) {
		boolean ret = true;
		for(int i = 0; i < data.length;) {
			if((int)data[i] >= 0) {
				firstFindLead = false;
				ret = false;
			}
			i = i + CHANNEL_NUMBER * 2;
		}
		return ret;
	}
	
	}
}

