package com.nju.ecg.wave;

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

import com.nju.ecg.utils.EcgConst;
import com.nju.ecg.utils.LogUtil;

/**
 * 心率绘制界面
 * 
 * @author zhuhf
 * @version [ME MTVClient_Handset V100R001C04SPC002, 2012-9-11]
 */
public class EcgDrawView extends SurfaceView implements SurfaceHolder.Callback
{
    /** 当前设置的导联 */
    public static int mCurentLead = EcgConst.MOCK_LIMB_LEAD;

    private static final String TAG = "EcgDrawView";
    private DrawThread drawThread;
    /** 导联切换空值变量 */
    private boolean switchScreen = false;

    /** 清楚绘制"痕迹" */
    private boolean clear = false;

    private SurfaceHolder sh;

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
        // drawThread.start();

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
        Log.d(TAG,
            "surfaceChanged");
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder)
    {
        Log.d(TAG,
            "surfaceCreated");
        // drawThread.start();
        if (drawThread.run == false)
        {
            drawThread.startThread();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder)
    {
        Log.d(TAG,
            "surfaceDestroyed");
        // drawThread.run = false;
    }

    /**
     * 停止绘图线程
     */
    public void stopDrawThread()
    {
        drawThread.run = false;
    }

    /**
     * 重置SurfaceView
     */
    public void reset()
    {
        updateCh1DataIndex = EcgConst.DISPLAY_LEFT_LEAD_NUMBER_WIDTH;
        clear = true;
    }

    /**
     * 切换导联
     */
    public void switchScreen()
    {
        switchScreen = !switchScreen;
    }
    
    private boolean paused;
    public synchronized void pauseDraw(boolean paused)
    {
    	this.paused = paused;
    	if (!paused)
    	{
    		notify();
    	}
    }

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
            // for (int i = 0; i < displayDataCh1.length; i++)
            // {
            // displayDataCh1[i] = 512;
            // displayDataCh2[i] = 512;
            // }
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
                            c.drawColor(Color.TRANSPARENT,
                                Mode.CLEAR);
                            clear = false;
                        }
                        else
                        {
                        	if (paused)
                        	{
                        		wait();
                        	}
                            updateData();
                            if (c != null)// home键SurfaceView会destory,c==null
                            {
                                onDraw(c);
                            }
                        }
                    }
                }
                catch (Exception e)
                {
                    LogUtil.e(TAG,
                        e);
                }
                finally
                {
                    if (c != null)
                        mSh.unlockCanvasAndPost(c);
                }
                try
                {
                    Thread.sleep(15);
                }
                catch (InterruptedException e)
                {
                    LogUtil.e(TAG,
                        e);
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

    private void updateData()
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

        if ((newDataCh1 != null && newDataCh1.length > 0)
            && (newDataCh2 != null && newDataCh2.length > 0))
        {
            if (updateCh1DataIndex >= displayDataCh1.length - EcgConst.WAVE_DEVIATION_VALUE - EcgWaveData.ProcessDataThread.RAW_DATA_BUF_LENGTH/EcgConst.LEADS_NUMBER
                /2/EcgConst.AVERAGE_POINTS)
            {
                updateCh1DataIndex = EcgConst.DISPLAY_LEFT_LEAD_NUMBER_WIDTH;
            }
            System.arraycopy(newDataCh1,
                0,
                displayDataCh1,
                updateCh1DataIndex - EcgConst.DISPLAY_LEFT_LEAD_NUMBER_WIDTH
                    + EcgConst.WAVE_DEVIATION_VALUE,
                newDataCh1.length);
            System.arraycopy(newDataCh2,
                0,
                displayDataCh2,
                updateCh1DataIndex - EcgConst.DISPLAY_LEFT_LEAD_NUMBER_WIDTH
                    + EcgConst.WAVE_DEVIATION_VALUE,
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
    private static final int CENTER_Y_CH = 50;
    /** 文字X轴偏移量 */
    private static final int TEXT_X_OFFSET = 40;
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
        canvas.drawColor(Color.TRANSPARENT,
            Mode.CLEAR);
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
//        if (displayDataCh1[EcgConst.WAVE_DEVIATION_VALUE] != -1)
//        {
//            canvas.drawLine(updateCh1DataIndex + EcgConst.WAVE_DEVIATION_VALUE,
//                0,
//                updateCh1DataIndex + EcgConst.WAVE_DEVIATION_VALUE,
//                EcgConst.DISPLAY_HEIGH,
//                vertical_line_paint);
//        }
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
            oldY = (CENTER_Y_CH * 3 - (displayDataCh2[0] - displayDataCh1[0]));
            newY = 0;
            if (displayDataCh1[EcgConst.WAVE_DEVIATION_VALUE] != -1)
            {
                canvas.drawText("Ⅰ",
                    TEXT_X_OFFSET,
                    CENTER_Y_CH * 2,
                    textPaint0);
            }
            for (int i = 0; i < updateCh1DataIndex + EcgConst.WAVE_DEVIATION_VALUE; i++)
            {
                newY = (CENTER_Y_CH * 3 - (displayDataCh2[i] - displayDataCh1[i]));

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
            oldY = (CENTER_Y_CH * 7 - displayDataCh2[0]);
            newY = 0;
            if (displayDataCh1[EcgConst.WAVE_DEVIATION_VALUE] != -1)
            {
                canvas.drawText("Ⅱ",
                    TEXT_X_OFFSET,
                    CENTER_Y_CH * 6,
                    textPaint1);
            }
            for (int i = 0; i < updateCh1DataIndex + EcgConst.WAVE_DEVIATION_VALUE; i++)
            {
                newY = (CENTER_Y_CH * 11 - displayDataCh2[i]);

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

            // Draw III
            oldX = EcgConst.DISPLAY_LEFT_LEAD_NUMBER_WIDTH;
            oldY = (CENTER_Y_CH * 11 - displayDataCh1[0]);
            newY = 0;
            if (displayDataCh1[EcgConst.WAVE_DEVIATION_VALUE] != -1)
            {
                canvas.drawText("Ⅲ",
                    TEXT_X_OFFSET,
                    CENTER_Y_CH * 10,
                    textPaint2);
            }
            for (int i = 0; i < updateCh1DataIndex + EcgConst.WAVE_DEVIATION_VALUE; i++)
            {
                newY = (CENTER_Y_CH * 15 - displayDataCh1[i]);

                if ((i >= 0 && i < EcgConst.WAVE_DEVIATION_VALUE_PART1)
                    || (i >= EcgConst.WAVE_DEVIATION_VALUE_PART2 && i < EcgConst.WAVE_DEVIATION_VALUE))
                {
                    newY = CENTER_Y_CH * 11;
                }
                else if (i >= EcgConst.WAVE_DEVIATION_VALUE_PART1
                    && i <= EcgConst.WAVE_DEVIATION_VALUE_PART2)
                {
                    newY = CENTER_Y_CH * 11 - EcgConst.GRID_WIDTH;
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
            // Draw AVR
            oldX = EcgConst.DISPLAY_LEFT_LEAD_NUMBER_WIDTH;
            oldY = (CENTER_Y_CH * 3 - (displayDataCh1[0] - 2 * displayDataCh2[0]) / 2);
            newY = 0;
            if (displayDataCh1[EcgConst.WAVE_DEVIATION_VALUE] != -1)
            {
                canvas.drawText("aVR",
                    TEXT_X_OFFSET,
                    CENTER_Y_CH * 2,
                    textPaint0);
            }
            for (int i = 0; i < updateCh1DataIndex + EcgConst.WAVE_DEVIATION_VALUE; i++)
            {
                newY = (CENTER_Y_CH - (displayDataCh1[i] - 2 * displayDataCh2[i]) / 2);

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
            oldY = (CENTER_Y_CH * 7 - (displayDataCh2[0] - 2 * displayDataCh1[0]) / 2);
            newY = 0;
            if (displayDataCh1[EcgConst.WAVE_DEVIATION_VALUE] != -1)
            {
                canvas.drawText("aVL",
                    TEXT_X_OFFSET,
                    CENTER_Y_CH * 6,
                    textPaint1);
            }
            for (int i = 0; i < updateCh1DataIndex + EcgConst.WAVE_DEVIATION_VALUE; i++)
            {
                newY = (CENTER_Y_CH * 5 - (displayDataCh2[i] - 2 * displayDataCh1[i]) / 2);

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
            // Draw AVF
            oldX = EcgConst.DISPLAY_LEFT_LEAD_NUMBER_WIDTH;
            oldY = CENTER_Y_CH * 11 - (displayDataCh1[0] + displayDataCh2[0])
                / 2;
            newY = 0;
            if (displayDataCh1[EcgConst.WAVE_DEVIATION_VALUE] != -1)
            {
                canvas.drawText("aVF",
                    TEXT_X_OFFSET,
                    CENTER_Y_CH * 10,
                    textPaint2);
            }
            for (int i = 0; i < updateCh1DataIndex + EcgConst.WAVE_DEVIATION_VALUE; i++)
            {
                newY = (CENTER_Y_CH * 15 - (displayDataCh1[i] + displayDataCh2[i]) / 2);

                if ((i >= 0 && i < EcgConst.WAVE_DEVIATION_VALUE_PART1)
                    || (i >= EcgConst.WAVE_DEVIATION_VALUE_PART2 && i < EcgConst.WAVE_DEVIATION_VALUE))
                {
                    newY = CENTER_Y_CH * 11;
                }
                else if (i >= EcgConst.WAVE_DEVIATION_VALUE_PART1
                    && i <= EcgConst.WAVE_DEVIATION_VALUE_PART2)
                {
                    newY = CENTER_Y_CH * 11 - EcgConst.GRID_WIDTH;
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
            oldY = (CENTER_Y_CH * 3 - (displayDataCh2[0] - displayDataCh1[0]));
            newY = 0;
            if (displayDataCh1[EcgConst.WAVE_DEVIATION_VALUE] != -1)
            {
                canvas.drawText("MⅠ",
                    TEXT_X_OFFSET,
                    CENTER_Y_CH * 2,
                    textPaint0);
            }
            for (int i = 0; i < updateCh1DataIndex + EcgConst.WAVE_DEVIATION_VALUE; i++)
            {
                newY = (CENTER_Y_CH * 3 - (displayDataCh2[i] - displayDataCh1[i]));

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
            oldY = (CENTER_Y_CH * 7 - displayDataCh2[0]);
            newY = 0;
            if (displayDataCh1[EcgConst.WAVE_DEVIATION_VALUE] != -1)
            {
                canvas.drawText("MⅡ",
                    TEXT_X_OFFSET,
                    CENTER_Y_CH * 6,
                    textPaint1);
            }
            for (int i = 0; i < updateCh1DataIndex + EcgConst.WAVE_DEVIATION_VALUE; i++)
            {
                newY = (CENTER_Y_CH * 11 - displayDataCh2[i]);

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

            // Draw MIII
            oldX = EcgConst.DISPLAY_LEFT_LEAD_NUMBER_WIDTH;
            oldY = (CENTER_Y_CH * 11 - displayDataCh1[0]);
            newY = 0;
            if (displayDataCh1[EcgConst.WAVE_DEVIATION_VALUE] != -1)
            {
                canvas.drawText("MⅢ",
                    TEXT_X_OFFSET,
                    CENTER_Y_CH * 10,
                    textPaint2);
            }
            for (int i = 0; i < updateCh1DataIndex + EcgConst.WAVE_DEVIATION_VALUE; i++)
            {
                newY = (CENTER_Y_CH * 15 - displayDataCh1[i]);

                if ((i >= 0 && i < EcgConst.WAVE_DEVIATION_VALUE_PART1)
                    || (i >= EcgConst.WAVE_DEVIATION_VALUE_PART2 && i < EcgConst.WAVE_DEVIATION_VALUE))
                {
                    newY = CENTER_Y_CH * 11;
                }
                else if (i >= EcgConst.WAVE_DEVIATION_VALUE_PART1
                    && i <= EcgConst.WAVE_DEVIATION_VALUE_PART2)
                {
                    newY = CENTER_Y_CH * 11 - EcgConst.GRID_WIDTH;
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
            oldY = (CENTER_Y_CH * 3 - (displayDataCh1[0] - 2 * displayDataCh2[0]) / 2);
            newY = 0;
            if (displayDataCh1[EcgConst.WAVE_DEVIATION_VALUE] != -1)
            {
                canvas.drawText("MaVR",
                    TEXT_X_OFFSET,
                    CENTER_Y_CH * 2,
                    textPaint0);
            }
            for (int i = 0; i < updateCh1DataIndex + EcgConst.WAVE_DEVIATION_VALUE; i++)
            {
                newY = (CENTER_Y_CH - (displayDataCh1[i] - 2 * displayDataCh2[i]) / 2);

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
            oldY = (CENTER_Y_CH * 7 - (displayDataCh2[0] - 2 * displayDataCh1[0]) / 2);
            newY = 0;
            if (displayDataCh1[EcgConst.WAVE_DEVIATION_VALUE] != -1)
            {
                canvas.drawText("MaVL",
                    TEXT_X_OFFSET,
                    CENTER_Y_CH * 6,
                    textPaint1);
            }
            for (int i = 0; i < updateCh1DataIndex + EcgConst.WAVE_DEVIATION_VALUE; i++)
            {
                newY = (CENTER_Y_CH * 5 - (displayDataCh2[i] - 2 * displayDataCh1[i]) / 2);

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
            oldY = CENTER_Y_CH * 11 - (displayDataCh1[0] + displayDataCh2[0])
                / 2;
            newY = 0;
            if (displayDataCh1[EcgConst.WAVE_DEVIATION_VALUE] != -1)
            {
                canvas.drawText("MaVF",
                    TEXT_X_OFFSET,
                    CENTER_Y_CH * 10,
                    textPaint2);
            }
            for (int i = 0; i < updateCh1DataIndex + EcgConst.WAVE_DEVIATION_VALUE; i++)
            {
                newY = (CENTER_Y_CH * 15 - (displayDataCh1[i] + displayDataCh2[i]) / 2);

                if ((i >= 0 && i < EcgConst.WAVE_DEVIATION_VALUE_PART1)
                    || (i >= EcgConst.WAVE_DEVIATION_VALUE_PART2 && i < EcgConst.WAVE_DEVIATION_VALUE))
                {
                    newY = CENTER_Y_CH * 11;
                }
                else if (i >= EcgConst.WAVE_DEVIATION_VALUE_PART1
                    && i <= EcgConst.WAVE_DEVIATION_VALUE_PART2)
                {
                    newY = CENTER_Y_CH * 11 - EcgConst.GRID_WIDTH;
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
        oldY = (CENTER_Y_CH * 5 - displayDataCh1[0]);
        newY = 0;
        if (displayDataCh1[EcgConst.WAVE_DEVIATION_VALUE] != -1)
        {
            canvas.drawText("MV1",
                TEXT_X_OFFSET,
                CENTER_Y_CH * 4,
                textPaint1);
        }
        for (int i = 0; i < updateCh1DataIndex + EcgConst.WAVE_DEVIATION_VALUE; i++)
        {
            newY = (CENTER_Y_CH * 9 - displayDataCh1[i]);

            if ((i >= 0 && i < EcgConst.WAVE_DEVIATION_VALUE_PART1)
                || (i >= EcgConst.WAVE_DEVIATION_VALUE_PART2 && i < EcgConst.WAVE_DEVIATION_VALUE))
            {
                newY = CENTER_Y_CH * 5;
            }
            else if (i >= EcgConst.WAVE_DEVIATION_VALUE_PART1
                && i <= EcgConst.WAVE_DEVIATION_VALUE_PART2)
            {
                newY = CENTER_Y_CH * 5 - EcgConst.GRID_WIDTH;
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
        oldY = (CENTER_Y_CH * 9 - displayDataCh2[0]);
        newY = 0;
        if (displayDataCh1[EcgConst.WAVE_DEVIATION_VALUE] != -1)
        {
            canvas.drawText("MV5",
                TEXT_X_OFFSET,
                CENTER_Y_CH * 8,
                textPaint2);
        }
        for (int i = 0; i < updateCh1DataIndex + EcgConst.WAVE_DEVIATION_VALUE; i++)
        {
            newY = (CENTER_Y_CH * 13 - displayDataCh2[i]);

            if ((i >= 0 && i < EcgConst.WAVE_DEVIATION_VALUE_PART1)
                || (i >= EcgConst.WAVE_DEVIATION_VALUE_PART2 && i < EcgConst.WAVE_DEVIATION_VALUE))
            {
                newY = CENTER_Y_CH * 9;
            }
            else if (i >= EcgConst.WAVE_DEVIATION_VALUE_PART1
                && i <= EcgConst.WAVE_DEVIATION_VALUE_PART2)
            {
                newY = CENTER_Y_CH * 9 - EcgConst.GRID_WIDTH;
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
        oldY = (CENTER_Y_CH * 7 - displayDataCh2[0]);
        newY = 0;
        for (int i = 0; i < updateCh1DataIndex + EcgConst.WAVE_DEVIATION_VALUE; i++)
        {
            newY = (CENTER_Y_CH * 11 - displayDataCh2[i]);

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
    }

}
