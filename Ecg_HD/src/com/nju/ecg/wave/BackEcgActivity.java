package com.nju.ecg.wave;

import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nju.ecg.R;
import com.nju.ecg.basic.BasicActivity;

/**
 * 心率回退界面
 * @author zhuhf
 * @version [ME MTVClient_Handset V100R001C04SPC002, 2012-12-18]
 */
public class BackEcgActivity extends BasicActivity
{
    private static final String TAG = "BackEcgActivity";
    public static final String DISPLAY_DATA1 = "display_data1";
    public static final String DISPLAY_DATA2 = "display_data2";
    public static final String VALID_DATA_LENGTH = "valid_data_length";
    public static final String SWITCH_SCREEN = "switch_screen";
    public static final String HEART_PARAM = "heart_param";
    public static final String HEART_VALUE = "heart_value";
    public static final String HEART_RESULT = "heart_result";
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.back_ecg_draw);
        
        String heartParam = getIntent().getStringExtra(HEART_PARAM);
        String heartValue = getIntent().getStringExtra(HEART_VALUE);
        String heartResult = getIntent().getStringExtra(HEART_RESULT);
        LinearLayout infoLayout = (LinearLayout) findViewById(R.id.info_layout);
        infoLayout.setVisibility(View.VISIBLE);
        TextView paraTxt = (TextView) findViewById(R.id.heart_para);
        paraTxt.setText(heartParam);
        TextView resultTxt = (TextView) findViewById(R.id.ecg_report_text);
        resultTxt.setText(heartResult);
        TextView valueTxt = (TextView) findViewById(R.id.heart_rate);
        valueTxt.setText(heartValue);
        
        int[] displayDataCh1 = getIntent().getIntArrayExtra(DISPLAY_DATA1);
        int[] displayDataCh2 = getIntent().getIntArrayExtra(DISPLAY_DATA2);
        int updateDataIndex = getIntent().getIntExtra(VALID_DATA_LENGTH, 0);
        boolean switchScreen = getIntent().getBooleanExtra(SWITCH_SCREEN, false);
        EcgBackgroundView bgView = (EcgBackgroundView)findViewById(R.id.bgView);
        bgView.invalidate();
        ZoomEcgDrawView zDrawView = (ZoomEcgDrawView)findViewById(R.id.zoomEcgdrawview);
        zDrawView.setSwitchScreen(switchScreen);
        zDrawView.setBackMode(true);
        zDrawView.setDisplayDataCh1(displayDataCh1);
        zDrawView.setDisplayDataCh2(displayDataCh2);
        zDrawView.setUpdateCh1DataIndex(updateDataIndex);
        zDrawView.invalidate();
    }
}
