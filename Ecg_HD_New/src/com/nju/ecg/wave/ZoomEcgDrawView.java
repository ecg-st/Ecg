package com.nju.ecg.wave;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.PathEffect;
import android.util.AttributeSet;
import android.view.View;

import com.nju.ecg.utils.EcgConst;

/**
 * 放大、回退心率绘制界面
 * @author zhuhf
 * @version [ME MTVClient_Handset V100R001C04SPC002, 2012-9-11]
 */
public class ZoomEcgDrawView extends View
{
    /** 当前设置的导联 */
    public static int mCurentLead = EcgConst.MOCK_LIMB_LEAD;
    private static final String TAG = "EcgDrawView";
    /** 导联切换空值变量 */
    private boolean switchScreen = false;
    /** 放大倍数*/
    private int zoomRate = 1;
    /** 两个导联数据*/
    private int[] displayDataCh1;
    private int[] displayDataCh2;
    private int updateCh1DataIndex;
    /** 是否是回退模式*/
    private boolean isBackMode;
    
    public ZoomEcgDrawView(Context context)
    {
        super(context);
        initPaint();
    }

    public ZoomEcgDrawView(Context context, AttributeSet attrs)
    {
        super(context,
            attrs);
        initPaint();
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        if (!isBackMode)
        {
            setMeasuredDimension(EcgConst.DISPLAY_WIDTH * zoomRate, EcgConst.DISPLAY_HEIGH * zoomRate);
        }
        else
        {
            setMeasuredDimension(EcgConst.DISPLAY_WIDTH * (EcgDrawView.BACK_SCREEM_COUNT + 1), EcgConst.DISPLAY_HEIGH);
        }
    }
    
    /**
     * @param zoomRate the zoomRate to set
     */
    public void setZoomRate(int zoomRate)
    {
        this.zoomRate = zoomRate;
    }
    
    /**
     * @param isBackMode the isBackMode to set
     */
    public void setBackMode(boolean isBackMode)
    {
        this.isBackMode = isBackMode;
    }

    /**
     * @param displayDataCh1 the displayDataCh1 to set
     */
    public void setDisplayDataCh1(int[] displayDataCh1)
    {
        this.displayDataCh1 = displayDataCh1;
    }

    /**
     * @param displayDataCh2 the displayDataCh2 to set
     */
    public void setDisplayDataCh2(int[] displayDataCh2)
    {
        this.displayDataCh2 = displayDataCh2;
    }

    /**
     * @param updateCh1DataIndex the updateCh1DataIndex to set
     */
    public void setUpdateCh1DataIndex(int updateCh1DataIndex)
    {
        this.updateCh1DataIndex = updateCh1DataIndex;
    }

    /**
     * @param switchScreen the switchScreen to set
     */
    public void setSwitchScreen(boolean switchScreen)
    {
        this.switchScreen = switchScreen;
    }

    /** 波形Y轴基偏移量 */
    private final int CENTER_Y_CH = 50;
    /** 文字X轴偏移量 */
    private final int TEXT_X_OFFSET = 40 * zoomRate;
    private Paint ch0_paint = new Paint();
    private Paint ch1_paint = new Paint();
    private Paint ch2_paint = new Paint();
    private Paint vertical_line_paint = new Paint();
    private Paint textPaint0 = new Paint();
    private Paint textPaint1 = new Paint();
    private Paint textPaint2 = new Paint();
    /** 波形Y轴放大倍数调节系数 (by Huo)*/
    public static final double Y_AMPLIFIER1 = 0.75; //for I, MI
    public static final double Y_AMPLIFIER2 = 0.75; //for II,MII
    public static final double Y_AMPLIFIER3 = 0.75; //for III, MIII
    public static final double Y_AMPLIFIER4 = 0.75; //for aVR, MaVR
    public static final double Y_AMPLIFIER5 = 0.75; //for aVL, MaVL
    public static final double Y_AMPLIFIER6 = 0.75; //for aVF, MaVF
    public static final double Y_AMPLIFIER7 = 0.75; //for MV1
    public static final double Y_AMPLIFIER8 = 0.75; //for MV5
    public static final double Y_AMPLIFIER9 = 0.75; //for simp
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
            oldY = (int) ((CENTER_Y_CH * 2 - Y_AMPLIFIER1 * (displayDataCh2[0] - displayDataCh1[0]) + Y_TUNING1) * zoomRate);
            newY = 0;
            if (displayDataCh1[EcgConst.WAVE_DEVIATION_VALUE] != -1)
            {
                canvas.drawText("Ⅰ", TEXT_X_OFFSET, CENTER_Y_CH * zoomRate, textPaint0);
            }
            for (int i = 0; i < updateCh1DataIndex + EcgConst.WAVE_DEVIATION_VALUE; i++)
            {
                newY = (int) ((CENTER_Y_CH * 2 - Y_AMPLIFIER1 * (displayDataCh2[i] - displayDataCh1[i]) + Y_TUNING1) * zoomRate);
                
                if ((i >= 0 && i < EcgConst.WAVE_DEVIATION_VALUE_PART1)
                    || (i >= EcgConst.WAVE_DEVIATION_VALUE_PART2 && i < EcgConst.WAVE_DEVIATION_VALUE))
                {
                    newY = CENTER_Y_CH * 2 * zoomRate;
                }
                else if (i >= EcgConst.WAVE_DEVIATION_VALUE_PART1
                    && i <= EcgConst.WAVE_DEVIATION_VALUE_PART2)
                {
                    newY = (CENTER_Y_CH * 2 - EcgConst.GRID_WIDTH) * zoomRate;
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
                        oldX + 1 * zoomRate,
                        newY,
                        ch0_paint);
                }
                oldX = oldX + 1 * zoomRate;
                oldY = newY;
            }

            // Draw II
            oldX = EcgConst.DISPLAY_LEFT_LEAD_NUMBER_WIDTH;
            oldY = (int) ((CENTER_Y_CH * 5.5 - Y_AMPLIFIER2 * displayDataCh2[0] + Y_TUNING2) * zoomRate);
            newY = 0;
            if (displayDataCh1[EcgConst.WAVE_DEVIATION_VALUE] != -1)
            {
                canvas.drawText("Ⅱ", TEXT_X_OFFSET, (int)(CENTER_Y_CH * 4.5 * zoomRate), textPaint1);
            }
            for (int i = 0; i < updateCh1DataIndex + EcgConst.WAVE_DEVIATION_VALUE; i++)
            {
                newY = (int) ((CENTER_Y_CH * 9.5 - Y_AMPLIFIER2 * displayDataCh2[i] + Y_TUNING2) * zoomRate);
                
                if ((i >= 0 && i < EcgConst.WAVE_DEVIATION_VALUE_PART1)
                    || (i >= EcgConst.WAVE_DEVIATION_VALUE_PART2 && i < EcgConst.WAVE_DEVIATION_VALUE))
                {
                    newY = (int)(CENTER_Y_CH * 5.5 * zoomRate);
                }
                else if (i >= EcgConst.WAVE_DEVIATION_VALUE_PART1
                    && i <= EcgConst.WAVE_DEVIATION_VALUE_PART2)
                {
                    newY = (int)((CENTER_Y_CH * 5.5 - EcgConst.GRID_WIDTH) * zoomRate);
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
                        oldX + 1 * zoomRate,
                        newY,
                        ch1_paint);
                }
                oldX = oldX + 1 * zoomRate;
                oldY = newY;
            }

            // Draw III
            oldX = EcgConst.DISPLAY_LEFT_LEAD_NUMBER_WIDTH;
            oldY = (int) ((CENTER_Y_CH * 9 - Y_AMPLIFIER3 * displayDataCh1[0] + Y_TUNING3) * zoomRate);
            newY = 0;
            if (displayDataCh1[EcgConst.WAVE_DEVIATION_VALUE] != -1)
            {
                canvas.drawText("Ⅲ", TEXT_X_OFFSET, CENTER_Y_CH * 8 * zoomRate, textPaint2);
            }
            for (int i = 0; i < updateCh1DataIndex + EcgConst.WAVE_DEVIATION_VALUE; i++)
            {
                newY = (int) ((CENTER_Y_CH * 13 - Y_AMPLIFIER3 * displayDataCh1[i] + Y_TUNING3) * zoomRate);

                if ((i >= 0 && i < EcgConst.WAVE_DEVIATION_VALUE_PART1)
                    || (i >= EcgConst.WAVE_DEVIATION_VALUE_PART2 && i < EcgConst.WAVE_DEVIATION_VALUE))
                {
                    newY = CENTER_Y_CH * 9 * zoomRate;
                }
                else if (i >= EcgConst.WAVE_DEVIATION_VALUE_PART1
                    && i <= EcgConst.WAVE_DEVIATION_VALUE_PART2)
                {
                    newY = (CENTER_Y_CH * 9 - EcgConst.GRID_WIDTH) * zoomRate;
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
                        oldX + 1 * zoomRate,
                        newY,
                        ch2_paint);
                }
                oldX = oldX + 1 * zoomRate;
                oldY = newY;
            }
        }
        else
        {
            // Draw AVR
            oldX = EcgConst.DISPLAY_LEFT_LEAD_NUMBER_WIDTH;
            oldY = (int) ((CENTER_Y_CH * 3 - Y_AMPLIFIER4 * (displayDataCh1[0] - 2 * displayDataCh2[0]) / 2 + Y_TUNING4) * zoomRate);
            newY = 0;
            if (displayDataCh1[EcgConst.WAVE_DEVIATION_VALUE] != -1)
            {
                canvas.drawText("aVR", TEXT_X_OFFSET, CENTER_Y_CH * 2 * zoomRate, textPaint0);
            }
            for (int i = 0; i < updateCh1DataIndex + EcgConst.WAVE_DEVIATION_VALUE; i++)
            {
                newY = (int) ((-1 * Y_AMPLIFIER4 * (displayDataCh1[i] - 2 * displayDataCh2[i]) / 2 + Y_TUNING4) * zoomRate);

                if ((i >= 0 && i < EcgConst.WAVE_DEVIATION_VALUE_PART1)
                    || (i >= EcgConst.WAVE_DEVIATION_VALUE_PART2 && i < EcgConst.WAVE_DEVIATION_VALUE))
                {
                    newY = CENTER_Y_CH * 3 * zoomRate;
                }
                else if (i >= EcgConst.WAVE_DEVIATION_VALUE_PART1
                    && i <= EcgConst.WAVE_DEVIATION_VALUE_PART2)
                {
                    newY = (CENTER_Y_CH * 3 - EcgConst.GRID_WIDTH) * zoomRate;
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
                        oldX + 1 * zoomRate,
                        newY,
                        ch0_paint);
                }
                oldX = oldX + 1 * zoomRate;
                oldY = newY;
            }
            // Draw AVL
            oldX = EcgConst.DISPLAY_LEFT_LEAD_NUMBER_WIDTH;
            oldY = (int) ((CENTER_Y_CH * 5.5 - Y_AMPLIFIER5 * (displayDataCh2[0] - 2 * displayDataCh1[0]) / 2 + Y_TUNING5) * zoomRate);
            newY = 0;
            if (displayDataCh1[EcgConst.WAVE_DEVIATION_VALUE] != -1)
            {
                canvas.drawText("aVL", TEXT_X_OFFSET, (int)(CENTER_Y_CH * 4.5 * zoomRate), textPaint1);
            }
            for (int i = 0; i < updateCh1DataIndex + EcgConst.WAVE_DEVIATION_VALUE; i++)
            {
                newY = (int) ((CENTER_Y_CH * 3.5 - Y_AMPLIFIER5 * (displayDataCh2[i] - 2 * displayDataCh1[i]) / 2 + Y_TUNING5) * zoomRate);

                if ((i >= 0 && i < EcgConst.WAVE_DEVIATION_VALUE_PART1)
                    || (i >= EcgConst.WAVE_DEVIATION_VALUE_PART2 && i < EcgConst.WAVE_DEVIATION_VALUE))
                {
                    newY = (int)(CENTER_Y_CH * 5.5 * zoomRate);
                }
                else if (i >= EcgConst.WAVE_DEVIATION_VALUE_PART1
                    && i <= EcgConst.WAVE_DEVIATION_VALUE_PART2)
                {
                    newY = (int)((CENTER_Y_CH * 5.5 - EcgConst.GRID_WIDTH) * zoomRate);
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
                        oldX + 1 * zoomRate,
                        newY,
                        ch1_paint);
                }
                oldX = oldX + 1 * zoomRate;
                oldY = newY;
            }
            // Draw AVF
            oldX = EcgConst.DISPLAY_LEFT_LEAD_NUMBER_WIDTH;
            oldY = (int) ((CENTER_Y_CH * 9 - Y_AMPLIFIER6 * (displayDataCh1[0] + displayDataCh2[0]) / 2 + Y_TUNING6) * zoomRate);
            newY = 0;
            if (displayDataCh1[EcgConst.WAVE_DEVIATION_VALUE] != -1)
            {
                canvas.drawText("aVF", TEXT_X_OFFSET, CENTER_Y_CH * 8 * zoomRate, textPaint2);
            }
            for (int i = 0; i < updateCh1DataIndex + EcgConst.WAVE_DEVIATION_VALUE; i++)
            {
                newY = (int) ((CENTER_Y_CH * 13 - Y_AMPLIFIER6 * (displayDataCh1[i] + displayDataCh2[i]) / 2 + Y_TUNING6) * zoomRate);

                if ((i >= 0 && i < EcgConst.WAVE_DEVIATION_VALUE_PART1)
                    || (i >= EcgConst.WAVE_DEVIATION_VALUE_PART2 && i < EcgConst.WAVE_DEVIATION_VALUE))
                {
                    newY = CENTER_Y_CH * 9 * zoomRate;
                }
                else if (i >= EcgConst.WAVE_DEVIATION_VALUE_PART1
                    && i <= EcgConst.WAVE_DEVIATION_VALUE_PART2)
                {
                    newY = (CENTER_Y_CH * 9 - EcgConst.GRID_WIDTH) * zoomRate;
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
                        oldX + 1 * zoomRate,
                        newY,
                        ch2_paint);
                }
                oldX = oldX + 1 * zoomRate;
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
            oldY = (CENTER_Y_CH * 2 - (displayDataCh2[0] - displayDataCh1[0])) * zoomRate;
            newY = 0;
            if (displayDataCh1[EcgConst.WAVE_DEVIATION_VALUE] != -1)
            {
                canvas.drawText("MⅠ",
                    TEXT_X_OFFSET,
                    CENTER_Y_CH * zoomRate,
                    textPaint0);
            }
            for (int i = 0; i < updateCh1DataIndex + EcgConst.WAVE_DEVIATION_VALUE; i++)
            {
                newY = (CENTER_Y_CH * 2 - (displayDataCh2[i] - displayDataCh1[i])) * zoomRate;

                if ((i >= 0 && i < EcgConst.WAVE_DEVIATION_VALUE_PART1)
                    || (i >= EcgConst.WAVE_DEVIATION_VALUE_PART2 && i < EcgConst.WAVE_DEVIATION_VALUE))
                {
                    newY = CENTER_Y_CH * 2 * zoomRate;
                }
                else if (i >= EcgConst.WAVE_DEVIATION_VALUE_PART1
                    && i <= EcgConst.WAVE_DEVIATION_VALUE_PART2)
                {
                    newY = (CENTER_Y_CH * 2 - EcgConst.GRID_WIDTH) * zoomRate;
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
                        oldX + 1 * zoomRate,
                        newY,
                        ch0_paint);
                }
                oldX = oldX + 1 * zoomRate;
                oldY = newY;
            }

            // Draw MII
            oldX = EcgConst.DISPLAY_LEFT_LEAD_NUMBER_WIDTH;
            oldY = (CENTER_Y_CH * 6 - displayDataCh2[0]) * zoomRate;
            newY = 0;
            if (displayDataCh1[EcgConst.WAVE_DEVIATION_VALUE] != -1)
            {
                canvas.drawText("MⅡ",
                    TEXT_X_OFFSET,
                    CENTER_Y_CH * 5 * zoomRate,
                    textPaint1);
            }
            for (int i = 0; i < updateCh1DataIndex + EcgConst.WAVE_DEVIATION_VALUE; i++)
            {
                newY = (CENTER_Y_CH * 10 - displayDataCh2[i]) * zoomRate;

                if ((i >= 0 && i < EcgConst.WAVE_DEVIATION_VALUE_PART1)
                    || (i >= EcgConst.WAVE_DEVIATION_VALUE_PART2 && i < EcgConst.WAVE_DEVIATION_VALUE))
                {
                    newY = CENTER_Y_CH * 6 * zoomRate;
                }
                else if (i >= EcgConst.WAVE_DEVIATION_VALUE_PART1
                    && i <= EcgConst.WAVE_DEVIATION_VALUE_PART2)
                {
                    newY = (CENTER_Y_CH * 6 - EcgConst.GRID_WIDTH) * zoomRate;
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
                        oldX + 1 * zoomRate,
                        newY,
                        ch1_paint);
                }
                oldX = oldX + 1 * zoomRate;
                oldY = newY;
            }

            // Draw MIII
            oldX = EcgConst.DISPLAY_LEFT_LEAD_NUMBER_WIDTH;
            oldY = (CENTER_Y_CH * 10 - displayDataCh1[0]) * zoomRate;
            newY = 0;
            if (displayDataCh1[EcgConst.WAVE_DEVIATION_VALUE] != -1)
            {
                canvas.drawText("MⅢ",
                    TEXT_X_OFFSET,
                    CENTER_Y_CH * 9 * zoomRate,
                    textPaint2);
            }
            for (int i = 0; i < updateCh1DataIndex + EcgConst.WAVE_DEVIATION_VALUE; i++)
            {
                newY = (CENTER_Y_CH * 14 - displayDataCh1[i]) * zoomRate;

                if ((i >= 0 && i < EcgConst.WAVE_DEVIATION_VALUE_PART1)
                    || (i >= EcgConst.WAVE_DEVIATION_VALUE_PART2 && i < EcgConst.WAVE_DEVIATION_VALUE))
                {
                    newY = CENTER_Y_CH * 10 * zoomRate;
                }
                else if (i >= EcgConst.WAVE_DEVIATION_VALUE_PART1
                    && i <= EcgConst.WAVE_DEVIATION_VALUE_PART2)
                {
                    newY = (CENTER_Y_CH * 10 - EcgConst.GRID_WIDTH) * zoomRate;
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
                        oldX + 1 * zoomRate,
                        newY,
                        ch2_paint);
                }
                oldX = oldX + 1 * zoomRate;
                oldY = newY;
            }
        }
        else
        {
            // Draw MaVR
            oldX = EcgConst.DISPLAY_LEFT_LEAD_NUMBER_WIDTH;
            oldY = (CENTER_Y_CH * 2 - (displayDataCh1[0] - 2 * displayDataCh2[0]) / 2) * zoomRate;
            newY = 0;
            if (displayDataCh1[EcgConst.WAVE_DEVIATION_VALUE] != -1)
            {
                canvas.drawText("MaVR",
                    TEXT_X_OFFSET,
                    CENTER_Y_CH * zoomRate,
                    textPaint0);
            }
            for (int i = 0; i < updateCh1DataIndex + EcgConst.WAVE_DEVIATION_VALUE; i++)
            {
                newY = (- (displayDataCh1[i] - 2 * displayDataCh2[i]) / 2) * zoomRate;

                if ((i >= 0 && i < EcgConst.WAVE_DEVIATION_VALUE_PART1)
                    || (i >= EcgConst.WAVE_DEVIATION_VALUE_PART2 && i < EcgConst.WAVE_DEVIATION_VALUE))
                {
                    newY = CENTER_Y_CH * 2 * zoomRate;
                }
                else if (i >= EcgConst.WAVE_DEVIATION_VALUE_PART1
                    && i <= EcgConst.WAVE_DEVIATION_VALUE_PART2)
                {
                    newY = (CENTER_Y_CH * 2 - EcgConst.GRID_WIDTH) * zoomRate;
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
                        oldX + 1 * zoomRate,
                        newY,
                        ch0_paint);
                }
                oldX = oldX + 1 * zoomRate;
                oldY = newY;
            }
            // Draw MaVL
            oldX = EcgConst.DISPLAY_LEFT_LEAD_NUMBER_WIDTH;
            oldY = (CENTER_Y_CH * 6 - (displayDataCh2[0] - 2 * displayDataCh1[0]) / 2) * zoomRate;
            newY = 0;
            if (displayDataCh1[EcgConst.WAVE_DEVIATION_VALUE] != -1)
            {
                canvas.drawText("MaVL",
                    TEXT_X_OFFSET,
                    CENTER_Y_CH * 5 * zoomRate,
                    textPaint1);
            }
            for (int i = 0; i < updateCh1DataIndex + EcgConst.WAVE_DEVIATION_VALUE; i++)
            {
                newY = (CENTER_Y_CH * 4 - (displayDataCh2[i] - 2 * displayDataCh1[i]) / 2) * zoomRate;

                if ((i >= 0 && i < EcgConst.WAVE_DEVIATION_VALUE_PART1)
                    || (i >= EcgConst.WAVE_DEVIATION_VALUE_PART2 && i < EcgConst.WAVE_DEVIATION_VALUE))
                {
                    newY = CENTER_Y_CH * 6 * zoomRate;
                }
                else if (i >= EcgConst.WAVE_DEVIATION_VALUE_PART1
                    && i <= EcgConst.WAVE_DEVIATION_VALUE_PART2)
                {
                    newY = (CENTER_Y_CH * 6 - EcgConst.GRID_WIDTH) * zoomRate;
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
                        oldX + 1 * zoomRate,
                        newY,
                        ch1_paint);
                }
                oldX = oldX + 1 * zoomRate;
                oldY = newY;
            }
            // Draw MaVF
            oldX = EcgConst.DISPLAY_LEFT_LEAD_NUMBER_WIDTH;
            oldY = (CENTER_Y_CH * 10 - (displayDataCh1[0] + displayDataCh2[0])
                / 2) * zoomRate;
            newY = 0;
            if (displayDataCh1[EcgConst.WAVE_DEVIATION_VALUE] != -1)
            {
                canvas.drawText("MaVF",
                    TEXT_X_OFFSET,
                    CENTER_Y_CH * 9 * zoomRate,
                    textPaint2);
            }
            for (int i = 0; i < updateCh1DataIndex + EcgConst.WAVE_DEVIATION_VALUE; i++)
            {
                newY = (CENTER_Y_CH * 14 - (displayDataCh1[i] + displayDataCh2[i]) / 2) * zoomRate;

                if ((i >= 0 && i < EcgConst.WAVE_DEVIATION_VALUE_PART1)
                    || (i >= EcgConst.WAVE_DEVIATION_VALUE_PART2 && i < EcgConst.WAVE_DEVIATION_VALUE))
                {
                    newY = CENTER_Y_CH * 10 * zoomRate;
                }
                else if (i >= EcgConst.WAVE_DEVIATION_VALUE_PART1
                    && i <= EcgConst.WAVE_DEVIATION_VALUE_PART2)
                {
                    newY = (CENTER_Y_CH * 10 - EcgConst.GRID_WIDTH) * zoomRate;
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
                        oldX + 1 * zoomRate,
                        newY,
                        ch2_paint);
                }
                oldX = oldX + 1 * zoomRate;
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
        oldY = (int) ((CENTER_Y_CH * 3 - Y_AMPLIFIER7 * displayDataCh1[0] + Y_TUNING7) * zoomRate);
        newY = 0;
        if (displayDataCh1[EcgConst.WAVE_DEVIATION_VALUE] != -1)
        {
            canvas.drawText("MV1",
                TEXT_X_OFFSET,
                CENTER_Y_CH * 2 * zoomRate,
                textPaint1);
        }
        for (int i = 0; i < updateCh1DataIndex + EcgConst.WAVE_DEVIATION_VALUE; i++)
        {
            newY = (int) ((CENTER_Y_CH * 7 - Y_AMPLIFIER7 * displayDataCh1[i] + Y_TUNING7) * zoomRate);

            if ((i >= 0 && i < EcgConst.WAVE_DEVIATION_VALUE_PART1)
                || (i >= EcgConst.WAVE_DEVIATION_VALUE_PART2 && i < EcgConst.WAVE_DEVIATION_VALUE))
            {
                newY = CENTER_Y_CH * 3 * zoomRate;
            }
            else if (i >= EcgConst.WAVE_DEVIATION_VALUE_PART1
                && i <= EcgConst.WAVE_DEVIATION_VALUE_PART2)
            {
                newY = (CENTER_Y_CH * 3 - EcgConst.GRID_WIDTH) * zoomRate;
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
                    oldX + 1 * zoomRate,
                    newY,
                    ch1_paint);
            }
            oldX = oldX + 1 * zoomRate;
            oldY = newY;
        }

        // Draw MV5
        oldX = EcgConst.DISPLAY_LEFT_LEAD_NUMBER_WIDTH;
        oldY = (int) ((CENTER_Y_CH * 7 - Y_AMPLIFIER8 * displayDataCh2[0] + Y_TUNING8) * zoomRate);
        newY = 0;
        if (displayDataCh1[EcgConst.WAVE_DEVIATION_VALUE] != -1)
        {
            canvas.drawText("MV5",
                TEXT_X_OFFSET,
                CENTER_Y_CH * 6 * zoomRate,
                textPaint2);
        }
        for (int i = 0; i < updateCh1DataIndex + EcgConst.WAVE_DEVIATION_VALUE; i++)
        {
            newY = (int) ((CENTER_Y_CH * 11 - Y_AMPLIFIER8 * displayDataCh2[i] + Y_TUNING8) * zoomRate);

            if ((i >= 0 && i < EcgConst.WAVE_DEVIATION_VALUE_PART1)
                || (i >= EcgConst.WAVE_DEVIATION_VALUE_PART2 && i < EcgConst.WAVE_DEVIATION_VALUE))
            {
                newY = CENTER_Y_CH * 7 * zoomRate;
            }
            else if (i >= EcgConst.WAVE_DEVIATION_VALUE_PART1
                && i <= EcgConst.WAVE_DEVIATION_VALUE_PART2)
            {
                newY = (CENTER_Y_CH * 7 - EcgConst.GRID_WIDTH) * zoomRate;
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
                    oldX + 1 * zoomRate,
                    newY,
                    ch2_paint);
            }
            oldX = oldX + 1 * zoomRate;
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
        oldY = (int) ((CENTER_Y_CH * 3.5 - Y_AMPLIFIER9 * displayDataCh2[0] + Y_TUNING9) * zoomRate);
        newY = 0;
        for (int i = 0; i < updateCh1DataIndex + EcgConst.WAVE_DEVIATION_VALUE; i++)
        {
            newY = (int) ((CENTER_Y_CH * 6.5 - Y_AMPLIFIER9 * displayDataCh2[i] + Y_TUNING9) * zoomRate);

            if ((i >= 0 && i < EcgConst.WAVE_DEVIATION_VALUE_PART1)
                || (i >= EcgConst.WAVE_DEVIATION_VALUE_PART2 && i < EcgConst.WAVE_DEVIATION_VALUE))
            {
                newY = CENTER_Y_CH * 4 * zoomRate;
            }
            else if (i >= EcgConst.WAVE_DEVIATION_VALUE_PART1
                && i <= EcgConst.WAVE_DEVIATION_VALUE_PART2)
            {
                newY = (CENTER_Y_CH * 4 - EcgConst.GRID_WIDTH) * zoomRate;
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
                    oldX + 1 * zoomRate,
                    newY,
                    ch1_paint);
            }
            oldX = oldX + 1 * zoomRate;
            oldY = newY;
        }
    }

}
