package com.nju.ecg.wave;

import android.os.Bundle;
import android.view.Window;

import com.nju.ecg.R;
import com.nju.ecg.basic.BasicActivity;
import com.nju.ecg.utils.LogUtil;
/**
 * 心率图放大
 * @author zhuhf
 * @version [ME MTVClient_Handset V100R001C04SPC002, 2012-12-1]
 */
public class ZoomEcgActivity extends BasicActivity
{
    private static final String TAG = "ZoomEcgActivity";
    public static final String DISPLAY_DATA1 = "display_data1";
    public static final String DISPLAY_DATA2 = "display_data2";
    public static final String VALID_DATA_LENGTH = "valid_data_length";
    public static final String SWITCH_SCREEN = "switch_screen";
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.zoom_ecg_draw);
        LogUtil.d(TAG, "onCreate");
        int[] displayDataCh1 = getIntent().getIntArrayExtra(DISPLAY_DATA1);
        int[] displayDataCh2 = getIntent().getIntArrayExtra(DISPLAY_DATA2);
        int updateDataIndex = getIntent().getIntExtra(VALID_DATA_LENGTH, 0);
        boolean switchScreen = getIntent().getBooleanExtra(SWITCH_SCREEN, false);
        EcgBackgroundView bgView = (EcgBackgroundView)findViewById(R.id.bgView);
        bgView.setZoomRate(2);
        bgView.invalidate();
        
        ZoomEcgDrawView zDrawView = (ZoomEcgDrawView)findViewById(R.id.zoomEcgdrawview);
        zDrawView.setSwitchScreen(switchScreen);
        zDrawView.setZoomRate(2);
        zDrawView.setDisplayDataCh1(displayDataCh1);
        zDrawView.setDisplayDataCh2(displayDataCh2);
        zDrawView.setUpdateCh1DataIndex(updateDataIndex);
        zDrawView.invalidate();
    }
}
