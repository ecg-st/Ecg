package com.nju.ecg.wave;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import android.os.Bundle;
import android.view.Window;

import com.nju.ecg.R;
import com.nju.ecg.basic.BasicActivity;
import com.nju.ecg.utils.LogUtil;
/**
 * 绘制散点图界面
 * @author zhuhf
 * @since 2012-12-01
 * @version [ME MTVClient_Handset V100R001C04SPC002, 2012-12-1]
 */
public class DotGraphActivity extends BasicActivity
{
    /** RR间期文件路径Intent key*/
    public static final String RR_FILE_PATH_KEY = "rr_file_path";
    /** 持续采集时间key*/
    public static final String COLLECTING_TIME = "collecting_time";
    /** TAG*/
    private static final String TAG = "DotGraphActivity";
    private int rrLength = 0;
    private int[] rrX = null;
    private int[] rrY = null;
    private int[] heartX = null;
    private int[] heartY = null;
    private long collectingTime;
    private RRDotGraphView rrDotGraphView;
    private HeartDotGraphView heartDotGraphView;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dot_graph);
        rrDotGraphView = (RRDotGraphView) findViewById(R.id.rr_dot_view);
        heartDotGraphView = (HeartDotGraphView) findViewById(R.id.heart_dot_view);
    }
    
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
    	super.onWindowFocusChanged(hasFocus);
    	new Thread(new Runnable()
    	{
    		@Override
    		public void run()
    		{
    			init();
    		}
    	}).start();
    }
    
    /**
     * 初始化数据
     */
    private void init()
    {
        String filePath = getIntent().getStringExtra(RR_FILE_PATH_KEY);
//        collectingTime = getIntent().getLongExtra(COLLECTING_TIME, 0);
        BufferedReader br = null;
        try
        {
            br = new BufferedReader(new FileReader(new File(filePath)));
            StringBuilder rrBuild = new StringBuilder(100);
            String str;
            while ((str = br.readLine()) != null)
            {
                rrBuild.append(str);
            }
            String rrStr = rrBuild.toString();
            rrStr = rrStr.substring(1, rrStr.length());
            String[] rrs = rrStr.split(",");
            int[] rr = new int[rrs.length];
            for (int i = 0;i < rrs.length; i++)
            {
                rr[i] = Integer.parseInt(rrs[i].trim());
                collectingTime += rr[i];
            }
            rrLength = rrs.length;
            // 初始化坐标
            initRRPoint(rr);
            initHeartPoint(rr);
            
            // 更新界面
            runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    updateUI();
                }
            });
        }
        catch (Exception e)
        {
            LogUtil.e(TAG, e);
        }
        finally
        {
            try
            {
                if (br != null)
                {
                    br.close();
                }
            }
            catch (Exception e2)
            {
                LogUtil.e(TAG, e2);
            }
        }
    }
    
    /**
     * 初始化RR间期散点图坐标
     * @param rr
     */
    private void initRRPoint(int[] rr)
    {
        rrX = rr;
        rrY = new int[rrLength - 1];
        for (int i = 0; i < rrLength - 1; i++)
        {
            rrY[i] = rr[i + 1];
        }
    }
    
    /**
     * 初始化心率变异散点图坐标
     * @param rr
     */
    private void initHeartPoint(int[] rr)
    {
        heartX = new int[rrLength - 1];
        for (int i = 0; i < rrLength - 1; i++)
        {
            heartX[i] = rr[i + 1] - rr[i];
        }
        heartY = new int[rrLength - 2];
        for (int i = 0; i < rrLength - 2; i++)
        {
            heartY[i] = heartX[i + 1];
        }
    }
    
    /**
     * 更新界面
     */
    private void updateUI()
    {
        rrDotGraphView.setValue(rrX, rrY, collectingTime);
        rrDotGraphView.invalidate();
        heartDotGraphView.setValue(heartX, heartY, collectingTime);
        heartDotGraphView.invalidate();
    }
}
