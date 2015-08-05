package com.nju.ecg.wave;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PathEffect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
/**
 * 心率变异散点图
 * @author zhuhf
 * @version [ME MTVClient_Handset V100R001C04SPC002, 2012-12-1]
 */
public class HeartDotGraphView extends View
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
    public HeartDotGraphView(Context context)
    {
        super(context);
        init();
    }
    public HeartDotGraphView(Context context, AttributeSet attrs)
    {
        super(context,
            attrs);
        init();
    }
    public HeartDotGraphView(Context context, AttributeSet attrs, int defStyle)
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
        Log.d("HeartDotGraphView", "viewSide:" + viewSide);
        Log.d("HeartDotGraphView", "squareSide:" + squareSide);
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
        canvas.drawLine(MARGIN, (float)squareSide / 2 + MARGIN, squareSide + MARGIN, (float)squareSide / 2 + MARGIN, linePaint);
        canvas.drawLine((float)squareSide / 2 + MARGIN, MARGIN, (float)squareSide / 2 + MARGIN, squareSide + MARGIN, linePaint);
        PathEffect mEffects= new DashPathEffect(new float[] {2, 5, 2, 5 },1);
        linePaint.setPathEffect(mEffects);
        linePaint.setStyle(Paint.Style.STROKE);
        canvas.drawCircle((float)squareSide / 2 + MARGIN, (float)squareSide / 2 + MARGIN, (float)squareSide / 6, linePaint);
        canvas.drawCircle((float)squareSide / 2 + MARGIN, (float)squareSide / 2 + MARGIN, (float)squareSide / 3, linePaint);
        canvas.drawCircle((float)squareSide / 2 + MARGIN, (float)squareSide / 2 + MARGIN, (float)squareSide / 2, linePaint);
        canvas.drawText("-200", (float)squareSide / 6 + MARGIN - textPaint.measureText("-200") / 2, (float)squareSide / 2 + MARGIN, textPaint);
        canvas.drawText("200(ms)", ((float)squareSide / 6) * 5 + MARGIN - textPaint.measureText("200(ms)") / 2, (float)squareSide / 2 + MARGIN, textPaint);
        canvas.drawText("200(ms)", (float)squareSide / 2 + MARGIN - textPaint.measureText("200(ms)") / 2, (float)squareSide / 6 + MARGIN, textPaint);
        canvas.drawText("-200", (float)squareSide / 2 + MARGIN - textPaint.measureText("-200") / 2, ((float)squareSide / 6) * 5 + MARGIN, textPaint);
        textPaint.setTextSize(30);
        canvas.drawText("心率变异散点图", (float)squareSide / 2 + MARGIN - textPaint.measureText("心率变异散点图") / 2, MARGIN + 30, textPaint);
        textPaint.setTextSize(15);
        textPaint.setColor(Color.BLACK);
        canvas.drawText("采集时间：" + collectingTime / 1000f + "s", MARGIN + (float)squareSide / 2 - textPaint.measureText("采集时间：" + collectingTime / 1000f + "s") / 2, squareSide + MARGIN + 30, textPaint);
    }
    
    /**
     * 绘制散点
     * @param canvas
     */
    private void drawDotGraph(Canvas canvas)
    {
        for (int i = 0;i< x.length && i < y.length; i++)
        {
            canvas.drawPoint((float)squareSide / 2 + MARGIN + (x[i] * squareSide) / 600f, (float)squareSide / 2 + MARGIN + (-(y[i] * squareSide) / 600f), pointPaint);
        }
    }
}
