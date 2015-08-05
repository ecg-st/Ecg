package com.nju.ecg.wave;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PathEffect;
import android.util.AttributeSet;
import android.view.View;
/**
 * RR间期散点图
 * @author zhuhf
 * @version [ME MTVClient_Handset V100R001C04SPC002, 2012-12-1]
 */
public class RRDotGraphView extends View
{
    /** 上下左右间距*/
    private static final int MARGIN = 50;
    /** 坐标*/
    private int[] x;
    private int[] y;
    /** 采集时间*/
    private long collectingTime;
    /** 点画笔*/
    private Paint pointPaint;
    /** 线画笔*/
    private Paint linePaint;
    /** 文字画笔*/
    private Paint textPaint;
    /** 屏幕宽度和高度*/
    private int viewWidth;
    private int viewHeight;
    /** 视图边长*/
    private int viewSide;
    /** 正方形边长*/
    private int squareSide;
    public RRDotGraphView(Context context)
    {
        super(context);
        init();
    }

    public RRDotGraphView(Context context, AttributeSet attrs)
    {
        super(context,
            attrs);
        init();
    }

    public RRDotGraphView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context,
            attrs,
            defStyle);
        init();
    }
    
    /**
     * 初始化
     */
    private void init()
    {
        pointPaint = new Paint();
        pointPaint.setStyle(Style.STROKE);
        pointPaint.setStrokeWidth(3);
        pointPaint.setColor(Color.RED);
        linePaint = new Paint();
        linePaint.setColor(Color.DKGRAY);
        linePaint.setStyle(Style.STROKE);
        textPaint = new Paint();
        textPaint.setColor(Color.BLUE);
    }
    
    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus)
    {
        super.onWindowFocusChanged(hasWindowFocus);
        viewWidth = getWidth();
        viewHeight = getHeight();
        viewSide = Math.min(viewWidth, viewHeight);
        squareSide = viewSide - MARGIN * 2;
        invalidate();
    }
    
    /**
     * 设置值
     * @param x 坐标
     * @param y 坐标
     * @param collectingTime 采集时间
     */
    public void setValue(int[] x, int[] y, long collectingTime)
    {
        this.x = x;
        this.y = y;
        this.collectingTime = collectingTime;
    }
    
    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
        // 擦背景
        canvas.drawColor(Color.WHITE);
        // 绘制背景
        drawBackground(canvas);
        if (x != null && y != null)
        {
            // 绘制散点
            drawDotGraph(canvas);
        }
    }
    
    /**
     * 绘制背景
     * @param canvas
     */
    private void drawBackground(Canvas canvas)
    {
        // 外框
        canvas.drawRect(MARGIN - 5, MARGIN - 5, viewSide - MARGIN + 5, viewSide - MARGIN + 5, linePaint);
        // 内框
        canvas.drawRect(MARGIN, MARGIN, viewSide - MARGIN, viewSide - MARGIN, linePaint);
        // 绘制横线
        for (int i = 0; i < 9; i++)
        {
            canvas.drawLine(MARGIN, MARGIN + (squareSide / 10f) * (i + 1), squareSide + MARGIN, MARGIN + (squareSide / 10f) * (i + 1), linePaint);
        }
        // 绘制竖线
        for (int i = 0; i < 9; i++)
        {
            canvas.drawLine(MARGIN + (squareSide / 10f) * (i + 1), MARGIN, MARGIN + (squareSide / 10f) * (i + 1), squareSide + MARGIN, linePaint);
        }
        // 绘制交叉线
        canvas.drawLine(MARGIN, squareSide + MARGIN, squareSide + MARGIN, MARGIN, linePaint);
        // 绘制斜线
        for (int i = 0; i < 19; i++)
        {
           if (i <= 9)
           {
               if (i == 5)
               {
                   linePaint.setStyle(Style.FILL);
                   linePaint.setPathEffect(null);
                   linePaint.setColor(Color.RED);
               }
               else if (i == 9)
               {
                   linePaint.setStyle(Style.FILL);
                   linePaint.setPathEffect(null);
                   linePaint.setColor(Color.RED);
               } 
               else
               {
                   PathEffect mEffects= new DashPathEffect(new float[] {15, 5, 15, 5 },1);
                   linePaint.setStyle(Style.STROKE);
                   linePaint.setPathEffect(mEffects);
                   linePaint.setColor(Color.GREEN);
               }
               canvas.drawLine(MARGIN, squareSide + MARGIN - (squareSide / 10f) * (i + 1), MARGIN + (squareSide / 10f) * (i + 1), squareSide + MARGIN, linePaint);
           }
           else
           {
               PathEffect mEffects= new DashPathEffect(new float[] {15, 5, 15, 5 },1);
               linePaint.setStyle(Style.STROKE);
               linePaint.setPathEffect(mEffects);
               linePaint.setColor(Color.GREEN);
               canvas.drawLine(MARGIN + (squareSide / 10f) * (i - 10 + 1), MARGIN, squareSide + MARGIN, squareSide + MARGIN - (squareSide / 10f) * (i - 10 + 1), linePaint);
           }
        }
        PathEffect mEffects= new DashPathEffect(new float[] {2, 3, 2, 3 },1);
        linePaint.setStyle(Style.STROKE);
        linePaint.setPathEffect(mEffects);
        linePaint.setColor(Color.GRAY);
        // 绘制红线之间的虚线(横向)
        for (int i = 0; i < 10; i++)
        {
            if (i < 4)
            {
                canvas.drawLine(MARGIN, MARGIN + (squareSide / 20f) * ( 2 * i + 1), MARGIN + (squareSide / 20f) * ( 2 * i + 1), MARGIN + (squareSide / 20f) * ( 2 * i + 1), linePaint);
            }
            else
            {
                canvas.drawLine(MARGIN + (squareSide / 20f) * ( 2 * (i - 4) + 1), MARGIN + (squareSide / 20f) * ( 2 * i + 1), MARGIN + (squareSide / 20f) * ( 2 * i + 1), MARGIN + (squareSide / 20f) * ( 2 * i + 1), linePaint);
            }
        }
        // 绘制红线之间的虚线(竖向)
        for (int i = 0; i < 10; i++)
        {
            if (i < 6)
            {
                canvas.drawLine(MARGIN + (squareSide / 20f) * ( 2 * i + 1), MARGIN + (squareSide / 20f) * ( 2 * i + 1), MARGIN + (squareSide / 20f) * ( 2 * i + 1), MARGIN + (squareSide / 10f) * 4 + (squareSide / 20f) * ( 2 * i + 1), linePaint);
            }
            else
            {
                canvas.drawLine(MARGIN + (squareSide / 20f) * ( 2 * i + 1), MARGIN + (squareSide / 20f) * ( 2 * i + 1), MARGIN + (squareSide / 20f) * ( 2 * i + 1), squareSide + MARGIN, linePaint);
            }
        }
        // 绘制x,y坐标轴刻度
        canvas.drawText("0", MARGIN - 10 - textPaint.measureText("0"), squareSide + MARGIN + 20, textPaint);
        canvas.drawText("(ms)", MARGIN - 10 - textPaint.measureText("(ms)"), squareSide + MARGIN + 5 - squareSide / 20f, textPaint);
        String[] str1 = {"400", "800", "1200", "1600", "2000"};
        for (int i = 0; i < str1.length; i++)
        {
            canvas.drawText(str1[i], MARGIN + (squareSide / 10f) * (i + 1) * 2 - textPaint.measureText(str1[i]) / 2f, squareSide + MARGIN + 20, textPaint);
        }
        for (int i = 0; i < str1.length; i++)
        {
            canvas.drawText(str1[i], MARGIN - 10 - textPaint.measureText(str1[i]), squareSide + MARGIN - (squareSide / 10f) * (i + 1) * 2, textPaint);
        }
        // 绘制心率标尺刻度
        String[] str2 = {"600", "300", "200", "150", "120", "100", "86", "75", "67", "60", "55", "50", "46", "43", "40", "38", "35", "33", "32"};
        for (int i = 0; i < str2.length; i++)
        {
            canvas.drawText(str2[i], MARGIN + (squareSide / 20f) * (i + 1), squareSide + MARGIN - (squareSide / 20f) * (i + 1) + textPaint.measureText(str2[2]) / 2, textPaint);
        }
        // 绘制"心率标尺"文字
        String[] str3 = {"心", "率", "标", "尺"};
        textPaint.setTextSize(20);
        for (int i = 0; i < str3.length; i++)
        {
            canvas.drawText(str3[i], squareSide / 2f + MARGIN + (squareSide / 10f) * (i + 1) - textPaint.measureText(str3[i]), squareSide / 2f + MARGIN - (squareSide / 10f) * (i + 1), textPaint);
        }
        // 绘制"心动过速界限"
        canvas.save();
        canvas.rotate(45, MARGIN, MARGIN + (squareSide / 10f) * 4);
        String[] str4 = {"心", "动", "过"};
        for (int i = 0; i < str4.length; i++)
        {
            canvas.drawText(str4[i], MARGIN + (squareSide / 10f) * (i + 1) - textPaint.measureText(str4[i]), MARGIN + (squareSide / 10f) * 4, textPaint);
        }
        String[] str5 = {"速", "界", "线"};
        for (int i = 0; i < str5.length; i++)
        {
            canvas.drawText(str5[i], MARGIN + (squareSide / 10f) * 5 + (squareSide / 10f) * (i + 1) - textPaint.measureText(str5[i]), MARGIN + (squareSide / 10f) * 4, textPaint);
        }
        canvas.restore();
        canvas.save();
        // 绘制"心动过缓界限"
        canvas.rotate(45, MARGIN, MARGIN);
        String[] str6 = {"心", "动", "过"};
        for (int i = 0; i < str6.length; i++)
        {
            canvas.drawText(str6[i], MARGIN + (squareSide / 10f) * (i + 1) * 2 - textPaint.measureText(str6[i]), MARGIN, textPaint);
        }
        String[] str7 = {"缓", "界", "线"};
        for (int i = 0; i < str7.length; i++)
        {
            canvas.drawText(str7[i], MARGIN + (squareSide / 10f) * 7 + (squareSide / 10f) * (i + 1) * 2 - textPaint.measureText(str7[i]), MARGIN, textPaint);
        }
        canvas.restore();
        // 绘制标题
        textPaint.setTextSize(30);
        canvas.drawText("RR间期散点图", (float)squareSide / 2 + MARGIN + 10 - textPaint.measureText("RR间期散点图") / 2, MARGIN + 40, textPaint);
        textPaint.setTextSize(15);
        textPaint.setColor(Color.BLACK);
        canvas.drawText("采集时间：" + collectingTime / 1000f + "s", MARGIN + (float)squareSide / 2 + 10 - textPaint.measureText("采集时间：" + collectingTime / 1000f + "s") / 2, squareSide + MARGIN + 60, textPaint);
    }
    
    /**
     * 绘制散点
     * @param canvas
     */
    private void drawDotGraph(Canvas canvas)
    {
        for (int i = 0;i< x.length && i < y.length; i++)
        {
            canvas.drawPoint(MARGIN + (x[i] * squareSide) / 2000f, squareSide + MARGIN - (y[i] * squareSide) / 2000f, pointPaint);
        }
    }

}
