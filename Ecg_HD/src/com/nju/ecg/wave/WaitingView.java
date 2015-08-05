package com.nju.ecg.wave;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import com.nju.ecg.R;
import com.nju.ecg.utils.EcgConst;
import com.nju.ecg.utils.LogUtil;

/**
 * 采集等待30秒界面
 * 
 * @author zhuhf
 * @version [ME MTVClient_Handset V100R001C04SPC002, 2012-10-15]
 */
public class WaitingView extends View
{
    private final String TAG = "WaitingView";
    private int count = 30;
    private boolean run = false;
    private Paint textPaint;

    public WaitingView(Context context, AttributeSet attrs)
    {
        super(context,
            attrs);
        init();
    }

    public WaitingView(Context context)
    {
        super(context);
        init();
    }

    private void init()
    {
        textPaint = new Paint();
        textPaint.setTextSize(30);
        textPaint.setColor(Color.RED);
        setBackgroundResource(R.drawable.waiting);
    }
    
    @Override
    public synchronized void setVisibility(int visibility)
    {
        super.setVisibility(visibility);
        switch (visibility)
        {
            case View.VISIBLE:
                if (!run)
                {
                    run = true;
                    count = 30;
                    new DrawThread().start();
                }
                break;
            case View.GONE:
                run = false;
                break;
        }
    }
    
    private class DrawThread extends Thread
    {
        public void run()
        {
            while (run && count >0)
            {
                // 通知刷新
                postInvalidate();
                count--;
                
                // 睡眠
                try
                {
                    Thread.sleep(1000);
                }
                catch (Exception e)
                {
                    LogUtil.e(TAG,
                        e);
                }
            }
            run = false;
            // 通知观察者
            getContext().getContentResolver().notifyChange(EcgConst.COLLECT_WAITING_COMPLETED_URI, null);
        };
    };

    @Override
    protected void onDraw(final Canvas canvas)
    {
//        // 绘制背景
//        setBackgroundResource(R.drawable.waiting);
        
        // 绘制提示语
        canvas.drawText("请耐心等待30秒...",
            10,
            70,
            textPaint);
        
        // 绘制倒计时
        canvas.drawText("倒计时:" + count,
            EcgConst.DISPLAY_WIDTH - 150,
            70,
            textPaint);
    }
}
