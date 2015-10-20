package com.nju.ecg.wave;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.PathEffect;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff.Mode;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.nju.ecg.R;
import com.nju.ecg.service.EcgApp;
import com.nju.ecg.utils.EcgConst;
import com.nju.ecg.utils.LogUtil;

/**
 * 心率绘制界面
 * @author zhuhf
 * @version [ME MTVClient_Handset V100R001C04SPC002, 2012-9-11]
 */
public class EcgDrawView extends SurfaceView implements SurfaceHolder.Callback
{
    /** 当前设置的导联 */
    public static int mCurentLead = EcgConst.SIMPLE_LIMB_LEAD;
    /** 当前模式, 默认是采集模式*/
    public static int mCurrentMode = 0;;

    private static final String TAG = "EcgDrawView";
    private DrawThread drawThread;
    /** 导联切换空值变量 */
    private boolean switchScreen = false;

    /** 清楚绘制"痕迹" */
    private boolean clear = false;

    private SurfaceHolder sh;
    
    /** 回退的屏数*/
    public final static int BACK_SCREEM_COUNT = 2;
    /** 缓存通道1回退的数据, 只保存最多2整屏数据, 当前屏幕数据在displayDataCh1中, 使用时需要把数据拼凑在一起(每一屏的起始伪造数据需要去除, 长度为EcgConst.WAVE_DEVIATION_VALUE)*/
    public final List<int[]> backWaves1 = new ArrayList<int[]>(2);
    /** 缓存通道2回退的数据, 只保存最多2整屏数据, 当前屏幕数据在displayDataCh2中, 使用时需要把数据拼凑在一起(每一屏的起始伪造数据需要去除, 长度为EcgConst.WAVE_DEVIATION_VALUE)*/
    public final List<int[]> backWaves2 = new ArrayList<int[]>(2);
    private int infoLHeight;
    
    private Paint txt_paint = new Paint();
    public EcgDrawView(Context context)
    {
        super(context);
    }

    public EcgDrawView(Context context, AttributeSet attrs)
    {
        super(context,
            attrs);
        initDiaplayData();
        
        sh = getHolder();
        sh.addCallback(this);
        drawThread = new DrawThread(sh);
        
        // Z轴方向也就是屏幕垂直方向置顶
        setZOrderOnTop(true);
        // 设置背景为全透明
        getHolder().setFormat(PixelFormat.TRANSPARENT);
    }
    
    /**
     * 初始化数据
     */
    private void initDiaplayData()
    {
        for (int i = 0; i < displayDataCh1.length; i++)
        {
            displayDataCh1[i] = -1;
            displayDataCh2[i] = -1;
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
        int height)
    {
        Log.d(TAG, "surfaceChanged");
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder)
    {
        Log.d(TAG, "surfaceCreated");
        if (drawThread.run == false)
        {
            drawThread.startThread();
        }
    }
    
    /**
     * @param infoLHeight the infoLHeight to set
     */
    public void setInfoLHeight(int infoLHeight)
    {
        this.infoLHeight = infoLHeight;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder)
    {
        Log.d(TAG, "surfaceDestroyed");
    }
    
    /**
     * 停止绘图线程
     */
    public void stopDrawThread()
    {
        drawThread.run = false;
    }
    
    // 停止采集时保存未满一屏的数据
    public void saveNotFullWave()
    {
        if (mCurrentMode == 0) // 停止采集时保存未满一屏的数据(value 1 for test)
        {
            if (updateCh1DataIndex < (displayDataCh1.length - EcgConst.WAVE_DEVIATION_VALUE - EcgWaveData.ProcessDataThread.RAW_DATA_BUF_LENGTH/EcgConst.LEADS_NUMBER
                /2/EcgConst.AVERAGE_POINTS) && updateCh1DataIndex > 0)
            {
                int[] ch1Data = new int[displayDataCh1.length];
                int[] ch2Data = new int[displayDataCh2.length];
                System.arraycopy(displayDataCh1, 0, ch1Data, 0, displayDataCh1.length);
                System.arraycopy(displayDataCh2, 0, ch2Data, 0, displayDataCh2.length);
                int updateIndex = updateCh1DataIndex;
                EcgApp.getInstance().getEcgBinder().saveWaveShot(ch1Data, ch2Data, updateIndex, switchScreen, infoLHeight);
            }
        }
    }

    /**
     * 重置SurfaceView
     */
    public void reset()
    {
        updateCh1DataIndex = EcgConst.DISPLAY_LEFT_LEAD_NUMBER_WIDTH;
        clear = true;
        backWaves1.clear();
        backWaves2.clear();
        if (paused)
        {
        	this.paused = false;
        	synchronized (drawThread) 
    		{
    			drawThread.notify();
			}
        }
    }
    
    /**
     * 切换导联
     */
    public void switchScreen()
    {
        switchScreen = !switchScreen;
    }
    
    public boolean isSwitchScreen()
    {
        return switchScreen;
    }
    
    private boolean paused;
    public void pauseDraw(boolean paused, boolean clearOld)
    {
    	this.paused = paused;
    	if (!paused)
    	{
    		if (clearOld)
    		{
    			updateCh1DataIndex = EcgConst.DISPLAY_LEFT_LEAD_NUMBER_WIDTH;
    			clear = true;
    			backWaves1.clear();
    			backWaves2.clear();
    		}
    		synchronized (drawThread) 
    		{
    			drawThread.notify();
			}
    	}
    }

    @SuppressLint("WrongCall")
	class DrawThread extends Thread
    {
        SurfaceHolder mSh;
        Paint p = new Paint();
        int i = 0;
        /** 线程是否正在运行标记 */
        public boolean run = false;
        
        public DrawThread(SurfaceHolder sh)
        {
            this.mSh = sh;
            p.setColor(Color.BLUE);
            initPaint();
        }
        
        public void startThread()
        {
            run = true;
            start();
        }

        @Override
        public void run()
        {
            Canvas c = null;
            while (run)
            {
                try
                {
                    synchronized (mSh)
                    {
                        c = mSh.lockCanvas();
                        if (clear)
                        {
                            initDiaplayData();
                            
                            c.drawColor(Color.TRANSPARENT, Mode.CLEAR);
                            clear = false;
                        }
                        else
                        {
                        	if (paused)
                        	{
                        		synchronized (this) 
                        		{
                        			wait();
								}
                        	}
                            updateData(c);
                            if (c != null)// home键SurfaceView会destory,c==null
                            {
                                onDraw(c);
                            }
                        }
                    }
                }
                catch (Exception e)
                {
                    LogUtil.e(TAG, e);
                }
                finally
                {
                    if (c != null)
                    {
                        mSh.unlockCanvasAndPost(c);
                    }
                }
                try
                {
                    Thread.sleep(15);
                }
                catch (InterruptedException e)
                {
                    LogUtil.e(TAG, e);
                }
            }
        }
    }

    public int[] displayDataCh1 = new int[EcgConst.WAVE_WIDTH];
    public int[] displayDataCh2 = new int[EcgConst.WAVE_WIDTH];
    private int updateCh1DataIndex = EcgConst.DISPLAY_LEFT_LEAD_NUMBER_WIDTH;
    
    public int[] getDisplayDataCh1()
    {
        return displayDataCh1;
    }
    
    public int[] getDisplayDatach2()
    {
        return displayDataCh2;
    }
    
    /**
     * 保存回退数据
     */
    private void saveBackData()
    {
        int[] backCh1Data = new int[updateCh1DataIndex + EcgConst.WAVE_DEVIATION_VALUE];
        System.arraycopy(displayDataCh1, 0, backCh1Data, 0, updateCh1DataIndex + EcgConst.WAVE_DEVIATION_VALUE);
        int[] backCh2Data = new int[updateCh1DataIndex + EcgConst.WAVE_DEVIATION_VALUE];
        System.arraycopy(displayDataCh2, 0, backCh2Data, 0, updateCh1DataIndex + EcgConst.WAVE_DEVIATION_VALUE);
        if (backWaves1.size() >= 2)
        {
            backWaves1.set(0, backWaves1.get(1));
            backWaves1.set(1, backCh1Data);
            backWaves2.set(0, backWaves2.get(1));
            backWaves2.set(1, backCh2Data);
        }
        else
        {
            backWaves1.add(backCh1Data);
            backWaves2.add(backCh2Data);
        }
    }
    
    /**
     * 返回拼凑好的回退数据, 初始值包括伪造数据
     * @return index 0:通道1数据
     *         index 1:通道2数据
     */
    public List<int[]> getBackData()
    {
        List<int[]> backWaveData = new ArrayList<int[]>(2);
        backWaveData.add(processCh1Data());
        backWaveData.add(processCh2Data());
        return backWaveData;
    }
    
    /**
     * 拼凑通道1数据
     * @return
     */
    private int[] processCh1Data()
    {
        int[] tempCh1Data = new int[displayDataCh1.length * 3];
        int length = 0;
        if (backWaves1.size() >= 1)
        {
            int[] backCh1Data1 = backWaves1.get(0);
            System.arraycopy(backCh1Data1, 0, tempCh1Data, 0, backCh1Data1.length);
            length += backCh1Data1.length;
        }
        if (backWaves1.size() >= 2) //backCh1Data2不为空则backCh1Data1一定不为空, 所以直接叠加
        {
            int[] backCh1Data2 = backWaves1.get(1);
            System.arraycopy(backCh1Data2, EcgConst.WAVE_DEVIATION_VALUE, tempCh1Data, length, backCh1Data2.length - EcgConst.WAVE_DEVIATION_VALUE);
            length += backCh1Data2.length - EcgConst.WAVE_DEVIATION_VALUE;
        }
        if (length > 0)
        {
            System.arraycopy(displayDataCh1, EcgConst.WAVE_DEVIATION_VALUE, tempCh1Data, length, updateCh1DataIndex);
            length += updateCh1DataIndex;
        }
        else
        {
            System.arraycopy(displayDataCh1, 0, tempCh1Data, 0, updateCh1DataIndex + EcgConst.WAVE_DEVIATION_VALUE);
            length += updateCh1DataIndex + EcgConst.WAVE_DEVIATION_VALUE;
        }
        int[] ch1Data = new int[length];
        System.arraycopy(tempCh1Data, 0, ch1Data, 0, length);
        return ch1Data;
    }
    
    /**
     * 拼凑通道2数据
     * @return
     */
    private int[] processCh2Data()
    {
        int[] tempCh2Data = new int[displayDataCh2.length * 3];
        int length = 0;
        if (backWaves2.size() >=1)
        {
            int[] backCh2Data1 = backWaves2.get(0);
            System.arraycopy(backCh2Data1, 0, tempCh2Data, 0, backCh2Data1.length);
            length += backCh2Data1.length;
        }
        if (backWaves2.size() >=2) //backCh1Data2不为空则backCh1Data1一定不为空, 所以直接叠加
        {
            int[] backCh2Data2 = backWaves2.get(1);
            System.arraycopy(backCh2Data2, EcgConst.WAVE_DEVIATION_VALUE, tempCh2Data, length, backCh2Data2.length - EcgConst.WAVE_DEVIATION_VALUE);
            length += backCh2Data2.length - EcgConst.WAVE_DEVIATION_VALUE;
        }
        if (length > 0)
        {
            System.arraycopy(displayDataCh2, EcgConst.WAVE_DEVIATION_VALUE, tempCh2Data, length, updateCh1DataIndex);
            length += updateCh1DataIndex;
        }
        else
        {
            System.arraycopy(displayDataCh2, 0, tempCh2Data, 0, updateCh1DataIndex + EcgConst.WAVE_DEVIATION_VALUE);
            length += updateCh1DataIndex + EcgConst.WAVE_DEVIATION_VALUE;
        }
        int[] ch2Data = new int[length];
        System.arraycopy(tempCh2Data, 0, ch2Data, 0, length);
        return ch2Data;
    }
    
    /**
     * @return the updateCh1DataIndex
     */
    public int getUpdateCh1DataIndex()
    {
        return updateCh1DataIndex;
    }

    private void updateData(Canvas c)
    {
        int[] newDataCh1 = null;
        int[] newDataCh2 = null;
        if (EcgWaveData.mWaveData.size() <= 0)
            return;
        synchronized (EcgWaveData.mWaveData.get(0))
        {
            if (EcgWaveData.mWaveData.get(0).size() > 0)
            {
               newDataCh1 = EcgWaveData.mWaveData.get(0).get(0);
            }
        }

        synchronized (EcgWaveData.mWaveData.get(1))
        {
            if (EcgWaveData.mWaveData.get(1).size() > 0)
            {
                newDataCh2 = EcgWaveData.mWaveData.get(1).get(0);
            }
        }

        if ((newDataCh1 != null && newDataCh1.length > 0) && (newDataCh2 != null && newDataCh2.length > 0))
        {
            if (updateCh1DataIndex >= displayDataCh1.length - EcgConst.WAVE_DEVIATION_VALUE - EcgWaveData.ProcessDataThread.RAW_DATA_BUF_LENGTH/EcgConst.LEADS_NUMBER
                /2/EcgConst.AVERAGE_POINTS)
            {
                if (mCurrentMode == 1) // 回放模式才缓存数据
                {
                    saveBackData();
                }
                if (mCurrentMode == 0 && WaveScreen.needsWaveShot) // 采集模式保存截屏(value 1 for test)
                {
                    int[] ch1Data = new int[displayDataCh1.length];
                    int[] ch2Data = new int[displayDataCh2.length];
                    System.arraycopy(displayDataCh1, 0, ch1Data, 0, displayDataCh1.length);
                    System.arraycopy(displayDataCh2, 0, ch2Data, 0, displayDataCh2.length);
                    int updateIndex = updateCh1DataIndex;
                    EcgApp.getInstance().getEcgBinder().saveWaveShot(ch1Data, ch2Data, updateIndex, switchScreen, infoLHeight);
                }
                updateCh1DataIndex = EcgConst.DISPLAY_LEFT_LEAD_NUMBER_WIDTH;
            }
            
            System.arraycopy(newDataCh1,
                0,
                displayDataCh1,
                updateCh1DataIndex - EcgConst.DISPLAY_LEFT_LEAD_NUMBER_WIDTH + EcgConst.WAVE_DEVIATION_VALUE,
                newDataCh1.length);
            System.arraycopy(newDataCh2,
                0,
                displayDataCh2,
                updateCh1DataIndex - EcgConst.DISPLAY_LEFT_LEAD_NUMBER_WIDTH + EcgConst.WAVE_DEVIATION_VALUE,
                newDataCh2.length);
            updateCh1DataIndex = updateCh1DataIndex + newDataCh1.length;
//            LogUtil.d(TAG,
//                "displayDataCh1:" + Arrays.toString(displayDataCh1));
//            LogUtil.d(TAG,
//                "displayDataCh2:" + Arrays.toString(displayDataCh2));
            synchronized (EcgWaveData.mWaveData.get(0))
            {
                if (EcgWaveData.mWaveData.get(0).size() > 0)
                {
                    EcgWaveData.mWaveData.get(0).remove(0);
                }
            }
            synchronized (EcgWaveData.mWaveData.get(1))
            {
                if (EcgWaveData.mWaveData.get(1).size() > 0)
                {
                    EcgWaveData.mWaveData.get(1).remove(0);
                }
            }
        }
    }

    /*
     * private int copiedSrcIndex = 0; private void updateData() { int[]
     * newDataCh1 = null; int[] newDataCh2 = null;
     * if(EcgWaveData.mWaveData.size() <= 0) return; Log.v("leftdata",
     * "EcgWaveData.mWaveData.length = " + EcgWaveData.mWaveData.get(0).size());
     * synchronized (EcgWaveData.mWaveData.get(0)) { newDataCh1 =
     * EcgWaveData.mWaveData.get(0).peekFirst(); } synchronized
     * (EcgWaveData.mWaveData.get(1)) { newDataCh2 =
     * EcgWaveData.mWaveData.get(1).peekFirst(); } if ((newDataCh1 != null) &&
     * (newDataCh2 != null)) { System.arraycopy(newDataCh1, copiedSrcIndex,
     * displayDataCh1, updateCh1DataIndex, 16); System.arraycopy(newDataCh2,
     * copiedSrcIndex, displayDataCh2, updateCh1DataIndex, 16); copiedSrcIndex
     * += 16; updateCh1DataIndex = updateCh1DataIndex + 16; Log.v("draw",
     * "displayDataCh1:" + Arrays.toString(displayDataCh1)); if(copiedSrcIndex
     * >= newDataCh1.length) { synchronized (EcgWaveData.mWaveData.get(0)) {
     * EcgWaveData.mWaveData.get(0).removeFirst(); } synchronized
     * (EcgWaveData.mWaveData.get(1)) {
     * EcgWaveData.mWaveData.get(1).removeFirst(); } copiedSrcIndex = 0; }
     * if(updateCh1DataIndex >= displayDataCh1.length) updateCh1DataIndex = 0; }
     * else { Log.v("draw", "There's no more data"); } }
     */

    /** 波形Y轴基偏移量 */
    public static final int CENTER_Y_CH = 50;
    /** 肢体导联、模拟肢体导联之间的波形间隔*/
    public static int marginBetweenWave = 0;
    /** 文字X轴偏移量 */
    private static final int TEXT_X_OFFSET = 40;
    /** 波形Y轴放大倍数调节系数 (by Huo)*/
    public static final double Y_AMPLIFIER1 = 1.1; //for I, MI
    public static final double Y_AMPLIFIER2 = 1.1; //for II,MII
    public static final double Y_AMPLIFIER3 = 1.1; //for III, MIII
    public static final double Y_AMPLIFIER4 = 1.1; //for aVR, MaVR
    public static final double Y_AMPLIFIER5 = 1.1; //for aVL, MaVL
    public static final double Y_AMPLIFIER6 = 1.1; //for aVF, MaVF
    public static final double Y_AMPLIFIER7 = 1.1; //for MV1
    public static final double Y_AMPLIFIER8 = 1.1; //for MV5
    public static final double Y_AMPLIFIER9 = 1.1; //for simp
    /**波形Y轴偏移量微调 (by Huo)*/
    public static final int Y_TUNING1 = 4; //for I, MI
    public static final int Y_TUNING2 = 27; //for II,MII
    public static final int Y_TUNING3 = 24; //for III, MIII
    public static final int Y_TUNING4 = -14; //for aVR, MaVR
    public static final int Y_TUNING5 = -10; //for aVL, MaVL
    public static final int Y_TUNING6 = 25; //for aVF, MaVF
    public static final int Y_TUNING7 = 24; //for MV1
    public static final int Y_TUNING8 = 27; //for MV5
    public static final int Y_TUNING9 = 27; //for simp
    
    // ----------- show Max/4: Center_c*2 + 2048/4 - max/4
    // ----------- Center_ch show 2048 / 4
    // ----------- show Min /4: Center_c * 2 + 2048/4 - min/4
    private Paint ch0_paint = new Paint();
    private Paint ch1_paint = new Paint();
    private Paint ch2_paint = new Paint();
    private Paint vertical_line_paint = new Paint();
    private Paint textPaint0 = new Paint();
    private Paint textPaint1 = new Paint();
    private Paint textPaint2 = new Paint();

    public void initPaint()
    {
        ch0_paint.setColor(Color.BLACK);
        ch0_paint.setStrokeWidth(2);
        ch0_paint.setPathEffect(null);

        ch1_paint.setColor(Color.RED);
        ch1_paint.setStrokeWidth(2);
        ch1_paint.setPathEffect(null);

        ch2_paint.setColor(Color.BLUE);
        ch2_paint.setStrokeWidth(2);
        ch2_paint.setPathEffect(null);

        vertical_line_paint.setColor(Color.GRAY);
        vertical_line_paint.setStrokeWidth(5);
        PathEffect mEffects1 = new DashPathEffect(new float[] {5, 5, 5, 5 },
            1);
        vertical_line_paint.setPathEffect(mEffects1);
        
        textPaint0.setColor(Color.BLACK);
        textPaint0.setStrokeWidth(2);
        textPaint0.setTextSize(20);
        
        textPaint1.setColor(Color.RED);
        textPaint1.setStrokeWidth(2);
        textPaint1.setTextSize(20);
        
        textPaint2.setColor(Color.BLUE);
        textPaint2.setStrokeWidth(2);
        textPaint2.setTextSize(20);
    }

    public void onDraw(Canvas canvas)
    {
        // 擦背景
        canvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);
        switch (mCurentLead)
        {
            case EcgConst.LIMB_LEAD:
                drawLimbLead(canvas);
                break;
            case EcgConst.MOCK_LIMB_LEAD:
                drawMockLimbLead(canvas);
                break;
            case EcgConst.MOCK_CHEST_LEAD:
                drawChestLead(canvas);
                break;
            case EcgConst.SIMPLE_LIMB_LEAD:
                drawSimpleLimbLead(canvas);
                break;
            default:
                break;
        }
    }
    
    /**
     * 绘制肢体导联
     * 
     * @param canvas 画布
     */
    private void drawLimbLead(Canvas canvas)
    {
        int oldX, oldY, newY;

        if (!switchScreen)
        {
            // Draw I
            oldX = EcgConst.DISPLAY_LEFT_LEAD_NUMBER_WIDTH;
            oldY = (int) (CENTER_Y_CH * 2 - Y_AMPLIFIER1 * (displayDataCh2[0] - displayDataCh1[0]) + Y_TUNING1);
            newY = 0;
            if (displayDataCh1[EcgConst.WAVE_DEVIATION_VALUE] != -1)
            {
                canvas.drawText("Ⅰ", TEXT_X_OFFSET, CENTER_Y_CH, textPaint0);
            }
            for (int i = 0; i < updateCh1DataIndex + EcgConst.WAVE_DEVIATION_VALUE; i++)
            {
                newY = (int) (CENTER_Y_CH * 2 - Y_AMPLIFIER1 * (displayDataCh2[i] - displayDataCh1[i]) + Y_TUNING1);
                
                if ((i >= 0 && i < EcgConst.WAVE_DEVIATION_VALUE_PART1)
                    || (i >= EcgConst.WAVE_DEVIATION_VALUE_PART2 && i < EcgConst.WAVE_DEVIATION_VALUE))
                {
                    newY = CENTER_Y_CH * 2;
                }
                else if (i >= EcgConst.WAVE_DEVIATION_VALUE_PART1
                    && i <= EcgConst.WAVE_DEVIATION_VALUE_PART2)
                {
                    newY = CENTER_Y_CH * 2 - EcgConst.GRID_WIDTH;
                }
                
                if (displayDataCh1[EcgConst.WAVE_DEVIATION_VALUE] == -1 || (i > EcgConst.WAVE_DEVIATION_VALUE && displayDataCh1[i] == -1))
                {
                    newY = -1;
                }
                
                if (newY == -1)
                {
                    // do nothing
                }
                else if (i == EcgConst.WAVE_DEVIATION_VALUE_PART1 || i == EcgConst.WAVE_DEVIATION_VALUE_PART2)
                {
                    canvas.drawLine(oldX,
                        oldY,
                        oldX,
                        newY,
                        ch0_paint);

                }
                else
                {
                    canvas.drawLine(oldX,
                        oldY,
                        oldX + 1,
                        newY,
                        ch0_paint);
                }
                oldX = oldX + 1;
                oldY = newY;
            }

            // Draw II
            oldX = EcgConst.DISPLAY_LEFT_LEAD_NUMBER_WIDTH;
            oldY = (int) (CENTER_Y_CH * 5.5 - marginBetweenWave - Y_AMPLIFIER2 * displayDataCh2[0] + Y_TUNING2);
            newY = 0;
            if (displayDataCh2[EcgConst.WAVE_DEVIATION_VALUE] != -1)
            {
                canvas.drawText("Ⅱ", TEXT_X_OFFSET, (int)(CENTER_Y_CH * 4.5 - marginBetweenWave), textPaint1);
            }
            for (int i = 0; i < updateCh1DataIndex + EcgConst.WAVE_DEVIATION_VALUE; i++)
            {
                newY = (int) (CENTER_Y_CH * 9.5 - marginBetweenWave - Y_AMPLIFIER2 * displayDataCh2[i] + Y_TUNING2);
                
                if ((i >= 0 && i < EcgConst.WAVE_DEVIATION_VALUE_PART1)
                    || (i >= EcgConst.WAVE_DEVIATION_VALUE_PART2 && i < EcgConst.WAVE_DEVIATION_VALUE))
                {
                    newY = (int) (CENTER_Y_CH * 5.5 - marginBetweenWave);
                }
                else if (i >= EcgConst.WAVE_DEVIATION_VALUE_PART1
                    && i <= EcgConst.WAVE_DEVIATION_VALUE_PART2)
                {
                    newY = (int) (CENTER_Y_CH * 5.5 - marginBetweenWave - EcgConst.GRID_WIDTH);
                }
                
                if (displayDataCh2[EcgConst.WAVE_DEVIATION_VALUE] == -1 || (i > EcgConst.WAVE_DEVIATION_VALUE && displayDataCh2[i] == -1))
                {
                    newY = -1;
                }
                
                if (newY == -1)
                {
                    // do nothing
                }
                else if (i == EcgConst.WAVE_DEVIATION_VALUE_PART1 || i == EcgConst.WAVE_DEVIATION_VALUE_PART2)
                {
                    canvas.drawLine(oldX,
                        oldY,
                        oldX,
                        newY,
                        ch1_paint);
                }
                else
                {
                    canvas.drawLine(oldX,
                        oldY,
                        oldX + 1,
                        newY,
                        ch1_paint);
                }
                oldX = oldX + 1;
                oldY = newY;
            }

            // Draw III
            oldX = EcgConst.DISPLAY_LEFT_LEAD_NUMBER_WIDTH;
            oldY = (int) (CENTER_Y_CH * 9 - marginBetweenWave * 2 - Y_AMPLIFIER3 * displayDataCh1[0] + Y_TUNING3);
            newY = 0;
            if (displayDataCh1[EcgConst.WAVE_DEVIATION_VALUE] != -1)
            {
                canvas.drawText("Ⅲ", TEXT_X_OFFSET, CENTER_Y_CH * 8 - marginBetweenWave * 2, textPaint2);
            }
            for (int i = 0; i < updateCh1DataIndex + EcgConst.WAVE_DEVIATION_VALUE; i++)
            {
                newY = (int) (CENTER_Y_CH * 13 - marginBetweenWave * 2 - Y_AMPLIFIER3 * displayDataCh1[i] + Y_TUNING3);

                if ((i >= 0 && i < EcgConst.WAVE_DEVIATION_VALUE_PART1)
                    || (i >= EcgConst.WAVE_DEVIATION_VALUE_PART2 && i < EcgConst.WAVE_DEVIATION_VALUE))
                {
                    newY = CENTER_Y_CH * 9 - marginBetweenWave * 2;
                }
                else if (i >= EcgConst.WAVE_DEVIATION_VALUE_PART1
                    && i <= EcgConst.WAVE_DEVIATION_VALUE_PART2)
                {
                    newY = CENTER_Y_CH * 9 - marginBetweenWave * 2 - EcgConst.GRID_WIDTH;
                }
                
                if (displayDataCh1[EcgConst.WAVE_DEVIATION_VALUE] == -1 || (i > EcgConst.WAVE_DEVIATION_VALUE && displayDataCh1[i] == -1))
                {
                    newY = -1;
                }
                
                if (newY == -1)
                {
                    // do nothing
                }
                else if (i == EcgConst.WAVE_DEVIATION_VALUE_PART1 || i == EcgConst.WAVE_DEVIATION_VALUE_PART2)
                {
                    canvas.drawLine(oldX,
                        oldY,
                        oldX,
                        newY,
                        ch2_paint);
                }
                else
                {
                    canvas.drawLine(oldX,
                        oldY,
                        oldX + 1,
                        newY,
                        ch2_paint);
                }
                oldX = oldX + 1;
                oldY = newY;
            }
        }
        else
        {
            // Draw AVR
            oldX = EcgConst.DISPLAY_LEFT_LEAD_NUMBER_WIDTH;
            oldY = (int) (CENTER_Y_CH * 3 - marginBetweenWave - Y_AMPLIFIER4 * (displayDataCh1[0] - 2 * displayDataCh2[0]) / 2 + Y_TUNING4);
            newY = 0;
            if (displayDataCh1[EcgConst.WAVE_DEVIATION_VALUE] != -1)
            {
                canvas.drawText("aVR", TEXT_X_OFFSET, CENTER_Y_CH * 2 - marginBetweenWave, textPaint0);
            }
            for (int i = 0; i < updateCh1DataIndex + EcgConst.WAVE_DEVIATION_VALUE; i++)
            {
                newY = (int) ( - marginBetweenWave - Y_AMPLIFIER4 * (displayDataCh1[i] - 2 * displayDataCh2[i]) / 2 + Y_TUNING4 + CENTER_Y_CH);

                if ((i >= 0 && i < EcgConst.WAVE_DEVIATION_VALUE_PART1)
                    || (i >= EcgConst.WAVE_DEVIATION_VALUE_PART2 && i < EcgConst.WAVE_DEVIATION_VALUE))
                {
                    newY = CENTER_Y_CH * 3 - marginBetweenWave;
                }
                else if (i >= EcgConst.WAVE_DEVIATION_VALUE_PART1
                    && i <= EcgConst.WAVE_DEVIATION_VALUE_PART2)
                {
                    newY = CENTER_Y_CH * 3 - marginBetweenWave - EcgConst.GRID_WIDTH;
                }
                
                if (displayDataCh1[EcgConst.WAVE_DEVIATION_VALUE] == -1 || (i > EcgConst.WAVE_DEVIATION_VALUE && displayDataCh1[i] == -1))
                {
                    newY = -1;
                }
                
                if (newY == -1)
                {
                    // do nothing
                }
                else if (i == EcgConst.WAVE_DEVIATION_VALUE_PART1 || i == EcgConst.WAVE_DEVIATION_VALUE_PART2)
                {
                    canvas.drawLine(oldX,
                        oldY,
                        oldX,
                        newY,
                        ch0_paint);
                }
                else
                {
                    canvas.drawLine(oldX,
                        oldY,
                        oldX + 1,
                        newY,
                        ch0_paint);
                }
                oldX = oldX + 1;
                oldY = newY;
            }
            // Draw AVL
            oldX = EcgConst.DISPLAY_LEFT_LEAD_NUMBER_WIDTH;
            oldY = (int) (CENTER_Y_CH * 5.5 - marginBetweenWave - Y_AMPLIFIER5 * (displayDataCh2[0] - 2 * displayDataCh1[0]) / 2 + Y_TUNING5);
            newY = 0;
            if (displayDataCh1[EcgConst.WAVE_DEVIATION_VALUE] != -1)
            {
                canvas.drawText("aVL", TEXT_X_OFFSET, (int)(CENTER_Y_CH * 4.5 - marginBetweenWave), textPaint1);
            }
            for (int i = 0; i < updateCh1DataIndex + EcgConst.WAVE_DEVIATION_VALUE; i++)
            {
                newY = (int) (CENTER_Y_CH * 3.5 - marginBetweenWave - Y_AMPLIFIER5 * (displayDataCh2[i] - 2 * displayDataCh1[i]) / 2 + Y_TUNING5);

                if ((i >= 0 && i < EcgConst.WAVE_DEVIATION_VALUE_PART1)
                    || (i >= EcgConst.WAVE_DEVIATION_VALUE_PART2 && i < EcgConst.WAVE_DEVIATION_VALUE))
                {
                    newY = (int) (CENTER_Y_CH * 5.5 - marginBetweenWave);
                }
                else if (i >= EcgConst.WAVE_DEVIATION_VALUE_PART1
                    && i <= EcgConst.WAVE_DEVIATION_VALUE_PART2)
                {
                    newY = (int) (CENTER_Y_CH * 5.5 - marginBetweenWave - EcgConst.GRID_WIDTH);
                }
                
                if (displayDataCh1[EcgConst.WAVE_DEVIATION_VALUE] == -1 || (i > EcgConst.WAVE_DEVIATION_VALUE && displayDataCh1[i] == -1))
                {
                    newY = -1;
                }
                
                if (newY == -1)
                {
                    // do nothing
                }
                else if (i == EcgConst.WAVE_DEVIATION_VALUE_PART1 || i == EcgConst.WAVE_DEVIATION_VALUE_PART2)
                {
                    canvas.drawLine(oldX,
                        oldY,
                        oldX,
                        newY,
                        ch1_paint);
                }
                else
                {
                    canvas.drawLine(oldX,
                        oldY,
                        oldX + 1,
                        newY,
                        ch1_paint);
                }
                oldX = oldX + 1;
                oldY = newY;
            }
            // Draw AVF
            oldX = EcgConst.DISPLAY_LEFT_LEAD_NUMBER_WIDTH;
            oldY = (int) (CENTER_Y_CH * 9 - marginBetweenWave * 2 - Y_AMPLIFIER6 * (displayDataCh1[0] + displayDataCh2[0]) / 2 + Y_TUNING6);
            newY = 0;
            if (displayDataCh1[EcgConst.WAVE_DEVIATION_VALUE] != -1)
            {
                canvas.drawText("aVF", TEXT_X_OFFSET, CENTER_Y_CH * 8 - marginBetweenWave * 2, textPaint2);
            }
            for (int i = 0; i < updateCh1DataIndex + EcgConst.WAVE_DEVIATION_VALUE; i++)
            {
                newY = (int) (CENTER_Y_CH * 13 - marginBetweenWave * 2 - Y_AMPLIFIER6 * (displayDataCh1[i] + displayDataCh2[i]) / 2 + Y_TUNING6);

                if ((i >= 0 && i < EcgConst.WAVE_DEVIATION_VALUE_PART1)
                    || (i >= EcgConst.WAVE_DEVIATION_VALUE_PART2 && i < EcgConst.WAVE_DEVIATION_VALUE))
                {
                    newY = CENTER_Y_CH * 9 - marginBetweenWave * 2;
                }
                else if (i >= EcgConst.WAVE_DEVIATION_VALUE_PART1
                    && i <= EcgConst.WAVE_DEVIATION_VALUE_PART2)
                {
                    newY = CENTER_Y_CH * 9 - marginBetweenWave * 2 - EcgConst.GRID_WIDTH;
                }
                
                if (displayDataCh1[EcgConst.WAVE_DEVIATION_VALUE] == -1 || (i > EcgConst.WAVE_DEVIATION_VALUE && displayDataCh1[i] == -1))
                {
                    newY = -1;
                }
                
                if (newY == -1)
                {
                    // do nothing
                }
                else if (i == EcgConst.WAVE_DEVIATION_VALUE_PART1 || i == EcgConst.WAVE_DEVIATION_VALUE_PART2)
                {
                    canvas.drawLine(oldX,
                        oldY,
                        oldX,
                        newY,
                        ch2_paint);
                }
                else
                {
                    canvas.drawLine(oldX,
                        oldY,
                        oldX + 1,
                        newY,
                        ch2_paint);
                }
                oldX = oldX + 1;
                oldY = newY;
            }
        }
    }
    
    /**
     * 绘制模拟肢体导联
     * 
     * @param canvas 画布
     */
    private void drawMockLimbLead(Canvas canvas)
    {
        int oldX, oldY, newY;

        if (!switchScreen)
        {
            // Draw MI
            oldX = EcgConst.DISPLAY_LEFT_LEAD_NUMBER_WIDTH;
            oldY = (CENTER_Y_CH * 2 - marginBetweenWave - (displayDataCh2[0] - displayDataCh1[0]));
            newY = 0;
            if (displayDataCh1[EcgConst.WAVE_DEVIATION_VALUE] != -1)
            {
                canvas.drawText("MⅠ",
                    TEXT_X_OFFSET,
                    CENTER_Y_CH - marginBetweenWave,
                    textPaint0);
            }
            for (int i = 0; i < updateCh1DataIndex + EcgConst.WAVE_DEVIATION_VALUE; i++)
            {
                newY = (CENTER_Y_CH * 2 - marginBetweenWave - (displayDataCh2[i] - displayDataCh1[i]));

                if ((i >= 0 && i < EcgConst.WAVE_DEVIATION_VALUE_PART1)
                    || (i >= EcgConst.WAVE_DEVIATION_VALUE_PART2 && i < EcgConst.WAVE_DEVIATION_VALUE))
                {
                    newY = CENTER_Y_CH * 2 - marginBetweenWave;
                }
                else if (i >= EcgConst.WAVE_DEVIATION_VALUE_PART1
                    && i <= EcgConst.WAVE_DEVIATION_VALUE_PART2)
                {
                    newY = CENTER_Y_CH * 2 - marginBetweenWave - EcgConst.GRID_WIDTH;
                }

                if (displayDataCh1[EcgConst.WAVE_DEVIATION_VALUE] == -1
                    || (i > EcgConst.WAVE_DEVIATION_VALUE && displayDataCh1[i] == -1))
                {
                    newY = -1;
                }

                if (newY == -1)
                {
                    // do nothing
                }
                else if (i == EcgConst.WAVE_DEVIATION_VALUE_PART1
                    || i == EcgConst.WAVE_DEVIATION_VALUE_PART2)
                {
                    canvas.drawLine(oldX,
                        oldY,
                        oldX,
                        newY,
                        ch0_paint);
                }
                else
                {
                    canvas.drawLine(oldX,
                        oldY,
                        oldX + 1,
                        newY,
                        ch0_paint);
                }
                oldX = oldX + 1;
                oldY = newY;
            }

            // Draw MII
            oldX = EcgConst.DISPLAY_LEFT_LEAD_NUMBER_WIDTH;
            oldY = (CENTER_Y_CH * 6 - marginBetweenWave - displayDataCh2[0]);
            newY = 0;
            if (displayDataCh2[EcgConst.WAVE_DEVIATION_VALUE] != -1)
            {
                canvas.drawText("MⅡ",
                    TEXT_X_OFFSET,
                    CENTER_Y_CH * 5 - marginBetweenWave,
                    textPaint1);
            }
            for (int i = 0; i < updateCh1DataIndex + EcgConst.WAVE_DEVIATION_VALUE; i++)
            {
                newY = (CENTER_Y_CH * 10 - marginBetweenWave - displayDataCh2[i]);

                if ((i >= 0 && i < EcgConst.WAVE_DEVIATION_VALUE_PART1)
                    || (i >= EcgConst.WAVE_DEVIATION_VALUE_PART2 && i < EcgConst.WAVE_DEVIATION_VALUE))
                {
                    newY = CENTER_Y_CH * 6 - marginBetweenWave;
                }
                else if (i >= EcgConst.WAVE_DEVIATION_VALUE_PART1
                    && i <= EcgConst.WAVE_DEVIATION_VALUE_PART2)
                {
                    newY = CENTER_Y_CH * 6 - marginBetweenWave - EcgConst.GRID_WIDTH;
                }

                if (displayDataCh2[EcgConst.WAVE_DEVIATION_VALUE] == -1
                    || (i > EcgConst.WAVE_DEVIATION_VALUE && displayDataCh2[i] == -1))
                {
                    newY = -1;
                }

                if (newY == -1)
                {
                    // do nothing
                }
                else if (i == EcgConst.WAVE_DEVIATION_VALUE_PART1
                    || i == EcgConst.WAVE_DEVIATION_VALUE_PART2)
                {
                    canvas.drawLine(oldX,
                        oldY,
                        oldX,
                        newY,
                        ch1_paint);
                }
                else
                {
                    canvas.drawLine(oldX,
                        oldY,
                        oldX + 1,
                        newY,
                        ch1_paint);
                }
                oldX = oldX + 1;
                oldY = newY;
            }

            // Draw MIII
            oldX = EcgConst.DISPLAY_LEFT_LEAD_NUMBER_WIDTH;
            oldY = (CENTER_Y_CH * 10 - marginBetweenWave * 2 - displayDataCh1[0]);
            newY = 0;
            if (displayDataCh1[EcgConst.WAVE_DEVIATION_VALUE] != -1)
            {
                canvas.drawText("MⅢ",
                    TEXT_X_OFFSET,
                    CENTER_Y_CH * 9 - marginBetweenWave * 2,
                    textPaint2);
            }
            for (int i = 0; i < updateCh1DataIndex + EcgConst.WAVE_DEVIATION_VALUE; i++)
            {
                newY = (CENTER_Y_CH * 14 - marginBetweenWave * 2 - displayDataCh1[i]);

                if ((i >= 0 && i < EcgConst.WAVE_DEVIATION_VALUE_PART1)
                    || (i >= EcgConst.WAVE_DEVIATION_VALUE_PART2 && i < EcgConst.WAVE_DEVIATION_VALUE))
                {
                    newY = CENTER_Y_CH * 10 - marginBetweenWave * 2;
                }
                else if (i >= EcgConst.WAVE_DEVIATION_VALUE_PART1
                    && i <= EcgConst.WAVE_DEVIATION_VALUE_PART2)
                {
                    newY = CENTER_Y_CH * 10 - marginBetweenWave * 2 - EcgConst.GRID_WIDTH;
                }

                if (displayDataCh1[EcgConst.WAVE_DEVIATION_VALUE] == -1
                    || (i > EcgConst.WAVE_DEVIATION_VALUE && displayDataCh1[i] == -1))
                {
                    newY = -1;
                }

                if (newY == -1)
                {
                    // do nothing
                }
                else if (i == EcgConst.WAVE_DEVIATION_VALUE_PART1
                    || i == EcgConst.WAVE_DEVIATION_VALUE_PART2)
                {
                    canvas.drawLine(oldX,
                        oldY,
                        oldX,
                        newY,
                        ch2_paint);
                }
                else
                {
                    canvas.drawLine(oldX,
                        oldY,
                        oldX + 1,
                        newY,
                        ch2_paint);
                }
                oldX = oldX + 1;
                oldY = newY;
            }
        }
        else
        {
            // Draw MaVR
            oldX = EcgConst.DISPLAY_LEFT_LEAD_NUMBER_WIDTH;
            oldY = (CENTER_Y_CH * 2 - marginBetweenWave - (displayDataCh1[0] - 2 * displayDataCh2[0]) / 2);
            newY = 0;
            if (displayDataCh1[EcgConst.WAVE_DEVIATION_VALUE] != -1)
            {
                canvas.drawText("MaVR",
                    TEXT_X_OFFSET,
                    CENTER_Y_CH - marginBetweenWave,
                    textPaint0);
            }
            for (int i = 0; i < updateCh1DataIndex + EcgConst.WAVE_DEVIATION_VALUE; i++)
            {
                newY = ( - (displayDataCh1[i] - marginBetweenWave - 2 * displayDataCh2[i]) / 2);

                if ((i >= 0 && i < EcgConst.WAVE_DEVIATION_VALUE_PART1)
                    || (i >= EcgConst.WAVE_DEVIATION_VALUE_PART2 && i < EcgConst.WAVE_DEVIATION_VALUE))
                {
                    newY = CENTER_Y_CH * 2 - marginBetweenWave;
                }
                else if (i >= EcgConst.WAVE_DEVIATION_VALUE_PART1
                    && i <= EcgConst.WAVE_DEVIATION_VALUE_PART2)
                {
                    newY = CENTER_Y_CH * 2 - marginBetweenWave - EcgConst.GRID_WIDTH;
                }

                if (displayDataCh1[EcgConst.WAVE_DEVIATION_VALUE] == -1
                    || (i > EcgConst.WAVE_DEVIATION_VALUE && displayDataCh1[i] == -1))
                {
                    newY = -1;
                }

                if (newY == -1)
                {
                    // do nothing
                }
                else if (i == EcgConst.WAVE_DEVIATION_VALUE_PART1
                    || i == EcgConst.WAVE_DEVIATION_VALUE_PART2)
                {
                    canvas.drawLine(oldX,
                        oldY,
                        oldX,
                        newY,
                        ch0_paint);
                }
                else
                {
                    canvas.drawLine(oldX,
                        oldY,
                        oldX + 1,
                        newY,
                        ch0_paint);
                }
                oldX = oldX + 1;
                oldY = newY;
            }
            // Draw MaVL
            oldX = EcgConst.DISPLAY_LEFT_LEAD_NUMBER_WIDTH;
            oldY = (CENTER_Y_CH * 6 - marginBetweenWave - (displayDataCh2[0] - 2 * displayDataCh1[0]) / 2);
            newY = 0;
            if (displayDataCh1[EcgConst.WAVE_DEVIATION_VALUE] != -1)
            {
                canvas.drawText("MaVL",
                    TEXT_X_OFFSET,
                    CENTER_Y_CH * 5 - marginBetweenWave,
                    textPaint1);
            }
            for (int i = 0; i < updateCh1DataIndex + EcgConst.WAVE_DEVIATION_VALUE; i++)
            {
                newY = (CENTER_Y_CH * 4 - marginBetweenWave - (displayDataCh2[i] - 2 * displayDataCh1[i]) / 2);

                if ((i >= 0 && i < EcgConst.WAVE_DEVIATION_VALUE_PART1)
                    || (i >= EcgConst.WAVE_DEVIATION_VALUE_PART2 && i < EcgConst.WAVE_DEVIATION_VALUE))
                {
                    newY = CENTER_Y_CH * 6 - marginBetweenWave;
                }
                else if (i >= EcgConst.WAVE_DEVIATION_VALUE_PART1
                    && i <= EcgConst.WAVE_DEVIATION_VALUE_PART2)
                {
                    newY = CENTER_Y_CH * 6 - marginBetweenWave - EcgConst.GRID_WIDTH;
                }

                if (displayDataCh1[EcgConst.WAVE_DEVIATION_VALUE] == -1
                    || (i > EcgConst.WAVE_DEVIATION_VALUE && displayDataCh1[i] == -1))
                {
                    newY = -1;
                }

                if (newY == -1)
                {
                    // do nothing
                }
                else if (i == EcgConst.WAVE_DEVIATION_VALUE_PART1
                    || i == EcgConst.WAVE_DEVIATION_VALUE_PART2)
                {
                    canvas.drawLine(oldX,
                        oldY,
                        oldX,
                        newY,
                        ch1_paint);
                }
                else
                {
                    canvas.drawLine(oldX,
                        oldY,
                        oldX + 1,
                        newY,
                        ch1_paint);
                }
                oldX = oldX + 1;
                oldY = newY;
            }
            // Draw MaVF
            oldX = EcgConst.DISPLAY_LEFT_LEAD_NUMBER_WIDTH;
            oldY = CENTER_Y_CH * 10 - marginBetweenWave * 2 - (displayDataCh1[0] + displayDataCh2[0])
                / 2;
            newY = 0;
            if (displayDataCh1[EcgConst.WAVE_DEVIATION_VALUE] != -1)
            {
                canvas.drawText("MaVF",
                    TEXT_X_OFFSET,
                    CENTER_Y_CH * 9 - marginBetweenWave * 2,
                    textPaint2);
            }
            for (int i = 0; i < updateCh1DataIndex + EcgConst.WAVE_DEVIATION_VALUE; i++)
            {
                newY = (CENTER_Y_CH * 14 - marginBetweenWave * 2 - (displayDataCh1[i] + displayDataCh2[i]) / 2);

                if ((i >= 0 && i < EcgConst.WAVE_DEVIATION_VALUE_PART1)
                    || (i >= EcgConst.WAVE_DEVIATION_VALUE_PART2 && i < EcgConst.WAVE_DEVIATION_VALUE))
                {
                    newY = CENTER_Y_CH * 10 - marginBetweenWave * 2;
                }
                else if (i >= EcgConst.WAVE_DEVIATION_VALUE_PART1
                    && i <= EcgConst.WAVE_DEVIATION_VALUE_PART2)
                {
                    newY = CENTER_Y_CH * 10 - marginBetweenWave * 2 - EcgConst.GRID_WIDTH;
                }

                if (displayDataCh1[EcgConst.WAVE_DEVIATION_VALUE] == -1
                    || (i > EcgConst.WAVE_DEVIATION_VALUE && displayDataCh1[i] == -1))
                {
                    newY = -1;
                }

                if (newY == -1)
                {
                    // do nothing
                }
                else if (i == EcgConst.WAVE_DEVIATION_VALUE_PART1
                    || i == EcgConst.WAVE_DEVIATION_VALUE_PART2)
                {
                    canvas.drawLine(oldX,
                        oldY,
                        oldX,
                        newY,
                        ch2_paint);
                }
                else
                {
                    canvas.drawLine(oldX,
                        oldY,
                        oldX + 1,
                        newY,
                        ch2_paint);
                }
                oldX = oldX + 1;
                oldY = newY;
            }
        }
    }

    /**
     * 绘制模拟胸导联
     * 
     * @param canvas 画布
     */
    private void drawChestLead(Canvas canvas)
    {
        int oldX, oldY, newY;

        // Draw MV1
        oldX = EcgConst.DISPLAY_LEFT_LEAD_NUMBER_WIDTH;
        oldY = (int) (CENTER_Y_CH * 3 - Y_AMPLIFIER7 * displayDataCh1[0] + Y_TUNING7);
        newY = 0;
        if (displayDataCh1[EcgConst.WAVE_DEVIATION_VALUE] != -1)
        {
            canvas.drawText("MV1",
                TEXT_X_OFFSET,
                CENTER_Y_CH * 2,
                textPaint1);
        }
        for (int i = 0; i < updateCh1DataIndex + EcgConst.WAVE_DEVIATION_VALUE; i++)
        {
            newY = (int) (CENTER_Y_CH * 7 - Y_AMPLIFIER7 * displayDataCh1[i] + Y_TUNING7);

            if ((i >= 0 && i < EcgConst.WAVE_DEVIATION_VALUE_PART1)
                || (i >= EcgConst.WAVE_DEVIATION_VALUE_PART2 && i < EcgConst.WAVE_DEVIATION_VALUE))
            {
                newY = CENTER_Y_CH * 3;
            }
            else if (i >= EcgConst.WAVE_DEVIATION_VALUE_PART1
                && i <= EcgConst.WAVE_DEVIATION_VALUE_PART2)
            {
                newY = CENTER_Y_CH * 3 - EcgConst.GRID_WIDTH;
            }

            if (displayDataCh1[EcgConst.WAVE_DEVIATION_VALUE] == -1
                || (i > EcgConst.WAVE_DEVIATION_VALUE && displayDataCh1[i] == -1))
            {
                newY = -1;
            }

            if (newY == -1)
            {
                // do nothing
            }
            else if (i == EcgConst.WAVE_DEVIATION_VALUE_PART1
                || i == EcgConst.WAVE_DEVIATION_VALUE_PART2)
            {
                canvas.drawLine(oldX,
                    oldY,
                    oldX,
                    newY,
                    ch1_paint);
            }
            else
            {
                canvas.drawLine(oldX,
                    oldY,
                    oldX + 1,
                    newY,
                    ch1_paint);
            }
            oldX = oldX + 1;
            oldY = newY;
        }

        // Draw MV5
        oldX = EcgConst.DISPLAY_LEFT_LEAD_NUMBER_WIDTH;
        oldY = (int) (CENTER_Y_CH * 7 - Y_AMPLIFIER8 * displayDataCh2[0] + Y_TUNING8);
        newY = 0;
        if (displayDataCh2[EcgConst.WAVE_DEVIATION_VALUE] != -1)
        {
            canvas.drawText("MV5",
                TEXT_X_OFFSET,
                CENTER_Y_CH * 6,
                textPaint2);
        }
        for (int i = 0; i < updateCh1DataIndex + EcgConst.WAVE_DEVIATION_VALUE; i++)
        {
            newY = (int) (CENTER_Y_CH * 11 - Y_AMPLIFIER8 * displayDataCh2[i] + Y_TUNING8);

            if ((i >= 0 && i < EcgConst.WAVE_DEVIATION_VALUE_PART1)
                || (i >= EcgConst.WAVE_DEVIATION_VALUE_PART2 && i < EcgConst.WAVE_DEVIATION_VALUE))
            {
                newY = CENTER_Y_CH * 7;
            }
            else if (i >= EcgConst.WAVE_DEVIATION_VALUE_PART1
                && i <= EcgConst.WAVE_DEVIATION_VALUE_PART2)
            {
                newY = CENTER_Y_CH * 7 - EcgConst.GRID_WIDTH;
            }

            if (displayDataCh2[EcgConst.WAVE_DEVIATION_VALUE] == -1
                || (i > EcgConst.WAVE_DEVIATION_VALUE && displayDataCh2[i] == -1))
            {
                newY = -1;
            }

            if (newY == -1)
            {
                // do nothing
            }
            else if (i == EcgConst.WAVE_DEVIATION_VALUE_PART1
                || i == EcgConst.WAVE_DEVIATION_VALUE_PART2)
            {
                canvas.drawLine(oldX,
                    oldY,
                    oldX,
                    newY,
                    ch2_paint);
            }
            else
            {
                canvas.drawLine(oldX,
                    oldY,
                    oldX + 1,
                    newY,
                    ch2_paint);
            }
            oldX = oldX + 1;
            oldY = newY;
        }
    }
    
    /**
     * 绘简易肢体导联
     * 
     * @param canvas 画布
     */
    private void drawSimpleLimbLead(Canvas canvas)
    {
        int oldX, oldY, newY;

        // Draw II
        oldX = EcgConst.DISPLAY_LEFT_LEAD_NUMBER_WIDTH;
        oldY = (int) (CENTER_Y_CH * 4 - Y_AMPLIFIER9 * displayDataCh2[0] + Y_TUNING9);
        newY = 0;
        for (int i = 0; i < updateCh1DataIndex + EcgConst.WAVE_DEVIATION_VALUE; i++)
        {
            newY = (int) (CENTER_Y_CH * 8 - Y_AMPLIFIER9 * displayDataCh2[i] + Y_TUNING9);

            if ((i >= 0 && i < EcgConst.WAVE_DEVIATION_VALUE_PART1)
                || (i >= EcgConst.WAVE_DEVIATION_VALUE_PART2 && i < EcgConst.WAVE_DEVIATION_VALUE))
            {
                newY = CENTER_Y_CH * 4;
            }
            else if (i >= EcgConst.WAVE_DEVIATION_VALUE_PART1
                && i <= EcgConst.WAVE_DEVIATION_VALUE_PART2)
            {
                newY = CENTER_Y_CH * 4 - EcgConst.GRID_WIDTH;
            }

            if (displayDataCh2[EcgConst.WAVE_DEVIATION_VALUE] == -1
                || (i > EcgConst.WAVE_DEVIATION_VALUE && displayDataCh2[i] == -1))
            {
                newY = -1;
            }

            if (newY == -1)
            {
                // do nothing
            }
            else if (i == EcgConst.WAVE_DEVIATION_VALUE_PART1
                || i == EcgConst.WAVE_DEVIATION_VALUE_PART2)
            {
                canvas.drawLine(oldX,
                    oldY,
                    oldX,
                    newY,
                    ch1_paint);
            }
            else
            {
                canvas.drawLine(oldX,
                    oldY,
                    oldX + 1,
                    newY,
                    ch1_paint);
            }
            oldX = oldX + 1;
            oldY = newY;
        }
    }
}
