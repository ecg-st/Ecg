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
import com.nju.ecg.utils.LogUtil;

/**
 * 心率图背景, 由于频繁绘制背景资源消耗太大, 导致采集速度与绘制速度不一致,
 * 所以使用自定义的View至于SurfaceView底部并且SurfaceView设置背景为全透明, 这样达到在SurfaceView里面绘制背景图的效果.
 * 
 * @author zhuhf
 * @version [ME MTVClient_Handset V100R001C04SPC002, 2012-9-5]
 */
public class EcgBackgroundView extends View
{
    private static final String TAG = "EcgBackgroundView";
    /** 背景画笔 */
    private Paint bg_paint = new Paint();
    /** 字体画笔*/
    private Paint txt_paint = new Paint();

    public EcgBackgroundView(Context context, AttributeSet attrs)
    {
        super(context,
            attrs);
    }

    public EcgBackgroundView(Context context)
    {
        super(context);
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        setMeasuredDimension(EcgConst.DISPLAY_WIDTH,
            EcgConst.DISPLAY_HEIGH);
    }
    
    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
        LogUtil.d(TAG,
            "EcgBackgroundView >> onDraw() start: "
                + System.currentTimeMillis());
        canvas.drawColor(Color.WHITE);
        // 绘制字体
        txt_paint.setColor(Color.BLACK);
        txt_paint.setTextSize(20);
        canvas.drawText("1格/mV", 10, 20 , txt_paint);
        canvas.drawText("2.5格/s", EcgConst.GRID_WIDTH * 3, 20 , txt_paint);
        // 绘制背景
        drawBackGround(canvas);
        LogUtil.d(TAG,
            "EcgBackgroundView >> onDraw() end: " + System.currentTimeMillis());
    }
    
    /**
     * 绘制背景
     * 
     * @param canvas 画布
     */
    void drawBackGround(Canvas canvas)
    {
        PathEffect mEffects1= new DashPathEffect(new float[] {5, 5, 5, 5 },
            1);
        PathEffect mEffects2= new DashPathEffect(new float[] {2, 5, 2, 5 },
            1);
        int displayWidth = EcgConst.DISPLAY_WIDTH;
        int displayHeight = EcgConst.DISPLAY_HEIGH;
        int gridWidth = EcgConst.GRID_WIDTH; 
        bg_paint.setColor(Color.RED);

        for (int i = 0; (i * gridWidth) / 10f < displayHeight; i++)
        {
            bg_paint.setStrokeWidth(1);
            bg_paint.setStyle(Paint.Style.STROKE);
            bg_paint.setPathEffect(mEffects1);
            canvas.drawRect(-1,
                (i * 2 + 1) * gridWidth,
                displayWidth,
                (i + 1) * 2 * gridWidth,
                bg_paint);
            
            bg_paint.setStrokeWidth(0);
            bg_paint.setPathEffect(mEffects2);
            canvas.drawLine(0,
                (i + 1) * gridWidth - gridWidth / 2,
                displayWidth,
                (i + 1) * gridWidth - gridWidth / 2,
                bg_paint);
        }
        for (int i = 0; (i * gridWidth) / 10f < displayWidth; i++)
        {
            bg_paint.setStrokeWidth(1);
            bg_paint.setStyle(Paint.Style.STROKE);
            bg_paint.setPathEffect(mEffects1);
            canvas.drawRect((i * 2 + 1) * gridWidth,
                -1,
                (i + 1) * 2 * gridWidth,
                displayHeight,
                bg_paint);
            
            bg_paint.setStrokeWidth(0);
            bg_paint.setPathEffect(mEffects2);
            canvas.drawLine((i + 1) * gridWidth - gridWidth / 2,
                0,
                (i + 1) * gridWidth - gridWidth / 2,
                displayHeight,
                bg_paint);
        }
        
        // 绘制点
        bg_paint.setStrokeWidth(1);
        for (int i = 0; (i * gridWidth) / 10f < displayWidth; i++)
        {
            for (int j = 0; (j * gridWidth) / 10f < displayHeight; j++)
            {
                if (!((((i + 1) * (gridWidth / 10f)) % (gridWidth / 2f) == 0) || ((j + 1) * (gridWidth / 10f)) % (gridWidth / 2f) == 0))
                {
                    canvas.drawPoint((i + 1) * (gridWidth / 10f), (j + 1) * (gridWidth / 10f), bg_paint);
                }
            }
        }
    }

}
