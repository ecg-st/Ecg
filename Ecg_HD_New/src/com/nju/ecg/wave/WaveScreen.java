package com.nju.ecg.wave;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.onekeyshare.OnekeyShare;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;
import com.nju.ecg.R;
import com.nju.ecg.basic.BasicActivity;
import com.nju.ecg.bluetooth.BluetoothRfcommClient;
import com.nju.ecg.bluetooth.BtBufferProcesser;
import com.nju.ecg.bluetooth.DeviceListActivity;
import com.nju.ecg.framework.db.WaveDataDBHelper;
import com.nju.ecg.location.LocationInfo;
import com.nju.ecg.location.LocationManager;
import com.nju.ecg.location.LocationManager.ErrorTypes;
import com.nju.ecg.location.LocationManager.LocationResultListener;
import com.nju.ecg.model.WaveData;
import com.nju.ecg.service.EcgApp;
import com.nju.ecg.service.EcgService;
import com.nju.ecg.service.ReportReceiver;
import com.nju.ecg.utils.DataStore;
import com.nju.ecg.utils.EcgConst;
import com.nju.ecg.utils.EcgIntent;
import com.nju.ecg.utils.FileUtil;
import com.nju.ecg.utils.LogUtil;
import com.nju.ecg.utils.StringUtil;

/**
 * 心率主界面
 * 
 * @author zhuhf
 * @version [ME MTVClient_Handset V100R001C04SPC002, 2012-9-3]
 */
public class WaveScreen extends BasicActivity implements View.OnClickListener, DemoModeResultListener, CollectedDataResultListener, OnSeekBarChangeListener
{
    private static final String TAG = "WaveScreen";
    private WaitingView waitingView;
    private RelativeLayout waitingLayout;
    private Button skipBtn;
    private TextView mStatusTxt;
    private TextView mHeartRate;
    private TextView mHeartPara;
    private TextView mHeartResult;
    private ImageButton mCollectOrReplayBtn;
    /** 数据管理Btn*/
    private Button historyExampleDataBtn;
    /** 模式切换Btn*/
    private Button modeSwitchBtn;
    /** 回放中暂停和继续按钮*/
    private Button replayPauseBtn;
    /** 放大按钮*/
    private Button zoomBtn;
    /** 回退按钮*/
    private Button backBtn;
    
    /** 模拟数据采集器*/
    private TestDataHandle mTestDataHandle;
    /** 当前是否正在采集心率数据*/
    private boolean isCollectOn = false;
    /** 是否正在回放数据*/
    private boolean isReplayOn = false;
    /** 是否处于暂停状态*/
    private boolean isPause = false;
    /** 是否正在演示数据*/
    private boolean isShowOn = false;
    /** 是否正在播放第1段数据*/
    private boolean isFirstSeg = false;
    
    private int mAbnormalValuse = 0;
    
    /** 选择导联时记录选项*/
    private int selectLeadIndex;
    /** 记录当前选择的模式*/
    private int selectModeIndex;
    /** 显示所有采集的数据列表*/
    private AlertDialog collectedDataDialog;
    /** 显示演示数据的对话框(自定义)*/
    private AlertDialog demoWaveDialog;
    /** 显示心电知识库等相关文本*/
    private AlertDialog richTxtDialog;
    /** 绘制心率SurfaceView*/
    private EcgDrawView drawView;
    
    PowerManager.WakeLock mWakeLock;
    
    /** 模式选择对话框*/
    private Dialog modeChooseDialog;
    
    /** 采集数据的开始和结束时间*/
    private long collectStart = 0;
    private long collectEnd = 0;
    
    /** 心率数据库操作*/
    private WaveDataDBHelper dbHelper;
    /** 所有采集的数据列表*/
    private List<WaveData> dataList;
    /** 数据适配器*/
    private CollectedDataAdapter dataAdapter;
    /** 标识某次的Tag, 防止结束还有分析结果显示*/
    public static String currentTag = "";
    private ContentObserver observer;
    
    /** 异常统计*/
    public static Map<String, Integer> abnormalParameterMap = new HashMap<String, Integer>();
    /** QRS宽度统计*/
    public static List<Integer> qrsValueList = new ArrayList<Integer>();
    /** PR间期统计*/
    public static List<Integer> prValueList = new ArrayList<Integer>();
    /** QT间期统计*/
    public static List<Integer> qtValueList = new ArrayList<Integer>();
    /** ST段高度统计*/
    public static List<Double> stValueList = new ArrayList<Double>();
    
    /** 最近回放的数据*/
    private WaveData lastReplayData;
    /** 进度条*/    
    private ProgressDialog progressDialog;
    /** 本次采集是否已发送紧急短信*/
    private boolean hasSendSMS = false;
    /** 本次采集已收到的分析结果次数*/
    private int analysisnum = 0;
    /** 本次采集两种发短信异常情况的连续出现计数*/
    private int emergentstage1 = 0;
    private int emergentstage2 = 0;
    
    /** 标识是否需求截取心率截屏*/
    public static boolean needsWaveShot = false;
    /** 回放快进与后退*/
    private static SeekBar seekBar;
    private View progressLay;
    public static TextView timeTxt;
    
    // 时长模式 add by zhf2015.09.28
    private int selectTimeInterval = 0; // 默认2分钟
    private int minute = 0; // 时长
    private int seconds;
    private TextView tvTime;
    // 语音合成对象
	private SpeechSynthesizer mTts;
	private Toast mToast;
	// 缓冲进度
	private int mPercentForBuffering = 0;
	// 播放进度
	private int mPercentForPlaying = 0;
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        WindowManager windowManager = getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        int rotation = display.getOrientation();
        EcgConst.DISPLAY_WIDTH = display.getWidth();
        EcgConst.DISPLAY_HEIGH = display.getHeight();
        LogUtil.d(TAG,
        		"Width = " + EcgConst.DISPLAY_WIDTH + " Heigh = "
        		+ EcgConst.DISPLAY_HEIGH + " Rotate = " + rotation);
        EcgConst.WAVE_WIDTH = ((EcgConst.DISPLAY_WIDTH - EcgConst.DISPLAY_LEFT_LEAD_NUMBER_WIDTH) / 16) * 16; 
        if (EcgConst.DISPLAY_HEIGH < 700) // 屏幕高度低于700, 肢体和模拟肢体导联之间隔缩小才能完整显示
        {
        	EcgDrawView.marginBetweenWave = EcgDrawView.CENTER_Y_CH;
        }
        setContentView(R.layout.ecg_draw);
        // 保持屏幕唤醒状态
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        waitingView = (WaitingView)findViewById(R.id.waiting_view);
        waitingLayout = (RelativeLayout)findViewById(R.id.waiting_layout);
        skipBtn = (Button)findViewById(R.id.skip_waiting_btn);
        skipBtn.setOnClickListener(this);
        mStatusTxt = (TextView) findViewById(R.id.record_status);
        mStatusTxt.setText(R.string.record_start);
        mCollectOrReplayBtn = (ImageButton) findViewById(R.id.collect_or_replayBtn);
        mCollectOrReplayBtn.setOnClickListener(this);
        mStatusTxt.setTextColor(getResources()
            .getColor(R.color.record_textEnded));
        mHeartRate = (TextView) findViewById(R.id.heart_rate);
        mHeartPara = (TextView) findViewById(R.id.heart_para);
        mHeartResult = (TextView) findViewById(R.id.ecg_report_text);
        historyExampleDataBtn = (Button) findViewById(R.id.wave_data_btn);
        historyExampleDataBtn.setOnClickListener(this);
        modeSwitchBtn = (Button) findViewById(R.id.mode_switch_btn);
        modeSwitchBtn.setOnClickListener(this);
        replayPauseBtn = (Button) findViewById(R.id.replay_pause_btn);
        replayPauseBtn.setOnClickListener(this);
        zoomBtn = (Button) findViewById(R.id.zoom_btn);
        zoomBtn.setOnClickListener(this);
        backBtn = (Button) findViewById(R.id.back_btn);
        backBtn.setOnClickListener(this);
        drawView = (EcgDrawView)findViewById(R.id.ecgdrawview);
        seekBar = (SeekBar)findViewById(R.id.seek_bar);
        progressLay = findViewById(R.id.progress_lay);
        timeTxt = (TextView)findViewById(R.id.time_txt);
        seekBar.setOnSeekBarChangeListener(this);
        
        observer = new CollectWaitingObserver(new Handler());
        getContentResolver().registerContentObserver(EcgConst.COLLECT_WAITING_COMPLETED_URI, false, observer);
        
        getLeadSystem();
        registerDispatchServiceReceiver();
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "com.nju.ecg");
        
        dbHelper = WaveDataDBHelper.getInstance();
        progressDialog = new ProgressDialog(this);
        // for test
        // EcgApp.getInstance().getEcgBinder().saveDotGraphShot("0123456789", collectEnd - collectStart);
        // for test
        
        autoCollect();
        
        // add by zhf 2015.09.28
        tvTime = (TextView)findViewById(R.id.tv_time);
        // 初始化合成对象
        mToast = Toast.makeText(this,"",Toast.LENGTH_SHORT);
		mTts = SpeechSynthesizer.createSynthesizer(this, mTtsInitListener);
    }
    
    @Override
    public void onWindowFocusChanged(boolean hasFocus)
    {
        super.onWindowFocusChanged(hasFocus);
        drawView.setInfoLHeight(findViewById(R.id.info_layout).getHeight());
    }
    
    @Override
    public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.collect_or_replayBtn://数据采集、回放模式、演示模式
                handleCollectRecordShowBtnClick();
                break;
            case R.id.replay_pause_btn:// 回放中暂停和继续
                replayPauseOrGoon();
                break;
            case R.id.back_btn:
                backBtnClick();
                break;
            case R.id.zoom_btn:
                zoomBtnClick();
                break;
            case R.id.wave_data_btn:
                switch (selectModeIndex)
                {
                    case 1://数据管理
                        createCollectedDataDialog();
                        break;
                    case 2://演示数据
                        createDemoWaveDialog();
                        break;
                    default:
                        break;
                }
                break;
            case R.id.mode_switch_btn://模式切换
                createModeChooseDialog();
                break;
            case R.id.skip_waiting_btn:
                waitingLayout.setVisibility(View.GONE);
                waitingView.setVisibility(View.GONE);
                break;
            default:
                break;
        }
    }
    
    
    @Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		if (isPause)
		{
			mTestDataHandle.openSDFile(lastReplayData.getFilePath(), seekBar.getProgress());
		}
		else
		{
			isFirstSeg = true; //added by Huo
			replayData(lastReplayData.getFilePath(), seekBar.getProgress());
		}
	}

	private static int pausedProgress; // 暂停时的进度
	public static void setReplayProgress(int progress)
	{
		seekBar.setProgress(progress);
		pausedProgress = progress;
	}

	private class CollectWaitingObserver extends ContentObserver
    {
        public CollectWaitingObserver(Handler handler)
        {
            super(handler);
        }
        
        @Override
        public void onChange(boolean selfChange)
        {
            super.onChange(selfChange);
            // 隐藏等待画面
            waitingView.setVisibility(View.GONE);
            waitingLayout.setVisibility(View.GONE);
            
            // 设置本次采集的Tag
            currentTag = String.valueOf(System.currentTimeMillis());
            if (mRfcommClient != null && device != null)
            {
                mRfcommClient.connect(device);
            }
            else if (device != null) // 匹配过蓝牙, 直接连接
            {
                mRfcommClient = new BluetoothRfcommClient(WaveScreen.this,
                    mHandler);
                mRfcommClient.connect(device);
            }
            else
            {
                mCollectOrReplayBtn.setEnabled(true);
            }
        }
    }
	
	private void autoCollect()
	{
	    mCollectOrReplayBtn.setEnabled(false);
	    mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null)
        {
            showToast(getString(R.string.bluetooth_not_availble));
        }
        else
        {
            this.autoConnect = true;
            if (!mBluetoothAdapter.isEnabled())
            {
                Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableIntent,
                    REQUEST_ENABLE_BT);
            }
            // Otherwise, setup the Oscillosope session
            else
            {
                String address = DataStore.getInstance().getBluetoothAddress();
                if (!StringUtil.isNullOrEmpty(address))
                {
                    Intent data = new Intent();
                    Bundle extras = new Bundle();
                    extras.putString(DeviceListActivity.EXTRA_DEVICE_ADDRESS, address);
                    data.putExtras(extras);
                    onActivityResult(REQUEST_CONNECT_DEVICE, RESULT_OK, data);
                }
                else
                {
                    setupBTConnection();
                }
            }
        }
	}
    
    /**
     * 回放最新采集的数据
     */
    private void replayLatestData()
    {
        final WaveData data = dbHelper.getLatestData();
        if (data == null)
        {
            // 未采集过数据
            showToast(getString(R.string.no_latest_data));
            return;
        }
        String filePath = data.getFilePath();
        if (!FileUtil.isExist(filePath))
        {
            new AlertDialog.Builder(this)
            .setTitle(R.string.dialog_title)
            .setMessage(String.format(getString(R.string.latest_data_file_deleted),
                data.getCollectFormatedTime()))
                .setPositiveButton(R.string.ok_btn_txt,
                    new OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog,
                        int which)
                    {
                        dbHelper.delete(data.get_id());
                    }
                })
                .setNegativeButton(R.string.cancel_btn_txt,
                    null)
                    .create()
                    .show();
            return;
        }
        
        // 设置本次的Tag
        currentTag = String.valueOf(System.currentTimeMillis());
        isReplayOn = true;
        mCollectOrReplayBtn.setImageResource(R.drawable.ic_stop);
        mStatusTxt.setText(getString(R.string.stop_btn_txt));
        replayPauseBtn.setText(getString(R.string.pause_btn_txt));
        replayPauseBtn.setVisibility(View.VISIBLE);
        resetScreen(true);
        if (mTestDataHandle == null)
        {
            mTestDataHandle = new TestDataHandle();
            mTestDataHandle.openSDFile(filePath, -1);
            mTestDataHandle.start();
        }
        // 启动数据分析线程
        EcgWaveData.resumeThread();
        progressLay.setVisibility(View.VISIBLE);
        seekBar.setProgress(0);
        timeTxt.setText("");
        lastReplayData = data; // 最近的采集数据回放后认为是最近回放数据
    }
    
    /**
     * 回放中暂停和继续回放
     */
    private void replayPauseOrGoon()
    {
        if (isReplayOn)
        {
            if (!isPause)// 暂停回放
            {
                isPause = true;
                replayPauseBtn.setText(R.string.goon_btn_txt);
                mTestDataHandle.pauseThread();
                EcgWaveData.pauseThread();
//                drawView.pauseDraw(true, false);
                zoomBtn.setVisibility(View.VISIBLE);
                backBtn.setVisibility(View.VISIBLE);
            }
            else // 继续回放
            {
                isPause = false;
                replayPauseBtn.setText(R.string.pause_btn_txt);
                mTestDataHandle.ResumeThread();
                EcgWaveData.resumeThread();
                if (pausedProgress == seekBar.getProgress())
                {
                	drawView.pauseDraw(false, false);
                }
                else
                {
                	drawView.pauseDraw(false, true);
                }
                zoomBtn.setVisibility(View.INVISIBLE);
                backBtn.setVisibility(View.INVISIBLE);
            }
        }
    }
    
    /**
     * 回放中回退
     */
    private void backBtnClick()
    {
        List<int[]> backData = drawView.getBackData();
        int[] ch1Data = backData.get(0);
        int[] ch2Data = backData.get(1);
        // 跳转到回退数据的视图
        ZoomEcgDrawView.mCurentLead = EcgDrawView.mCurentLead;
        Intent intent = new Intent(WaveScreen.this,
            BackEcgActivity.class);
        intent.putExtra(BackEcgActivity.DISPLAY_DATA1,
            ch1Data);
        intent.putExtra(BackEcgActivity.DISPLAY_DATA2,
            ch2Data);
        intent.putExtra(BackEcgActivity.VALID_DATA_LENGTH,
            ch1Data.length - EcgConst.WAVE_DEVIATION_VALUE);
        intent.putExtra(BackEcgActivity.SWITCH_SCREEN,
            drawView.isSwitchScreen());
        intent.putExtra(BackEcgActivity.HEART_PARAM,
            mHeartPara.getText().toString());
        intent.putExtra(BackEcgActivity.HEART_RESULT,
            mHeartResult.getText().toString());
        intent.putExtra(BackEcgActivity.HEART_VALUE,
            mHeartRate.getText().toString());
        startActivity(intent);
    }
    
    /**
     * 回放中放大
     */
    private void zoomBtnClick()
    {
        // 跳转到放大数据的视图
        ZoomEcgDrawView.mCurentLead = EcgDrawView.mCurentLead;
        Intent intent = new Intent(WaveScreen.this,
            ZoomEcgActivity.class);
        intent.putExtra(ZoomEcgActivity.DISPLAY_DATA1,
            drawView.getDisplayDataCh1());
        intent.putExtra(ZoomEcgActivity.DISPLAY_DATA2,
            drawView.getDisplayDatach2());
        intent.putExtra(ZoomEcgActivity.VALID_DATA_LENGTH,
            drawView.getUpdateCh1DataIndex());
        intent.putExtra(ZoomEcgActivity.SWITCH_SCREEN,
            drawView.isSwitchScreen());
        startActivity(intent);
    }
    
    /**
     * 回放数据
     * @param filePath 文件路径
     */
    private void replayData(String filePath, int progress)
    {
        // 设置本次的Tag
        currentTag = String.valueOf(System.currentTimeMillis());
        resetScreen(true);
        if (mTestDataHandle == null)
        {
            mTestDataHandle = new TestDataHandle();
            mTestDataHandle.openSDFile(filePath, progress);
            mTestDataHandle.start();
        }
        // 启动数据分析线程
        EcgWaveData.resumeThread();
        
        isReplayOn = true;
        isPause = false;
        mStatusTxt.setText(R.string.stop_btn_txt);
        replayPauseBtn.setText(R.string.pause_btn_txt);
        replayPauseBtn.setVisibility(View.VISIBLE);
        progressLay.setVisibility(View.VISIBLE);
        seekBar.setProgress(0);
        timeTxt.setText("");
    }
    
    /**
     * 获得当前设置的导联系统
     */
    private void getLeadSystem()
    {
    	  // modified by zhf 20150930
    	int leadSystem = EcgDrawView.mCurentLead;
//        int leadSystem = DataStore.getInstance().getLeadSystem();
//        EcgDrawView.mCurentLead = leadSystem;
        switch (leadSystem)
        {
            case EcgConst.LIMB_LEAD:
                selectLeadIndex = 0;
                break;
            case EcgConst.MOCK_LIMB_LEAD:
                selectLeadIndex = 1;
                break;
            case EcgConst.MOCK_CHEST_LEAD:
                selectLeadIndex = 2;
                break;
            case EcgConst.SIMPLE_LIMB_LEAD:
                selectLeadIndex = 3;
                break;
            default:
                break;
        }
    }
    
    /**
     * 创建历史采集的数据列表对话框
     */
    private void createCollectedDataDialog()
    {
        if (collectedDataDialog == null || !collectedDataDialog.isShowing())
        {
            dataList = dbHelper.getDataList();
            collectedDataDialog = new AlertDialog.Builder(this).create();
            collectedDataDialog.setTitle(getString(R.string.collect_record_list));
            if (dataList != null && dataList.size() > 0)
            {
                ListView listView = (ListView) LayoutInflater
                .from(this)
                .inflate(R.layout.list_view,
                    null);
                dataAdapter = new CollectedDataAdapter(this, dataList, this);
                listView.setAdapter(dataAdapter);
                dataAdapter.notifyDataSetChanged();
                collectedDataDialog.setView(listView);
            }
            else
            {
                collectedDataDialog.setMessage(getString(R.string.no_collect_record));
            }
            collectedDataDialog.show();
        }
    }
    
    /**
     * 创建选择演示数据对话框
     */
    private void createDemoWaveDialog()
    {
        if (demoWaveDialog == null || !demoWaveDialog.isShowing())
        {
            ListView listView = (ListView) LayoutInflater
            .from(this)
            .inflate(R.layout.list_view,
                null);
            DemoWaveDataAdapter demoWaveDataAdapter = new DemoWaveDataAdapter(this, this);
            listView.setAdapter(demoWaveDataAdapter);
            demoWaveDataAdapter.notifyDataSetChanged();
            demoWaveDialog = new AlertDialog.Builder(this).setView(listView).create();
            demoWaveDialog.show();
        }
    }
    
    // added by zhf 20151005
    private void createRichTxtDialog(String title, final String message)
    {
    	createRichTxtDialog(title, message, null);
    }
    
    private void createRichTxtDialog(String title, final String message, final String path)
    {
        if (richTxtDialog == null || !richTxtDialog.isShowing())
        {
            ScrollView scrollView = (ScrollView) LayoutInflater
            .from(this)
            .inflate(R.layout.rich_box_txt_view,
                null);
            // modified by zhf 20151005
            demoWaveDialog = new AlertDialog.Builder(this).setView(scrollView).setPositiveButton("分享图片", new OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					showShare(message, path, true);
				}
			}).setNegativeButton("分享文字", new OnClickListener() {
				@Override
				public void onClick(DialogInterface paramDialogInterface, int paramInt) {
					showShare(message, path, false);
				}
			})
			.create();
            demoWaveDialog.setTitle(title);
            TextView txtView = (TextView)scrollView.findViewById(R.id.txt_view);
            txtView.setText(message);
            demoWaveDialog.show();
            
            // added by zhf 20151005
            demoWaveDialog.setOnDismissListener(new OnDismissListener() {
				@Override
				public void onDismiss(DialogInterface dialog) {
					stopSpeaking();
				}
			});
            startSpeaking(message);
        }
    }
    
    /**
     * 创建选择模式对话框
     */
    private void createModeChooseDialog()
    {
        if (modeChooseDialog == null || !modeChooseDialog.isShowing())
        {
            modeChooseDialog = new AlertDialog.Builder(this)
            .setSingleChoiceItems(R.array.mode_choose_items,
                selectModeIndex,
                new OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    mCollectOrReplayBtn.setImageResource(R.drawable.ic_begin);
                    if (selectModeIndex == 0 && isCollectOn)// 采集模式且正在采集中保存数据
                    {
                        showDiagnoseResult();
                        EcgSaveData.destroy();
                        resetScreen(false);
                        // 不再截屏
                        needsWaveShot = false;
                        // 停止定时器
                        stopAlarm();
                    }
                    else
                    {
                        mRfcommClient = null;
                        resetScreen(true);
                    }
                    selectModeIndex = which;
                    EcgDrawView.mCurrentMode = which;
                    switch (selectModeIndex)
                    {
                        case 0:// 采集模式
                            getLeadSystem();
                            replayPauseBtn.setVisibility(View.INVISIBLE);
                            zoomBtn.setVisibility(View.INVISIBLE);
                            backBtn.setVisibility(View.INVISIBLE);
                            mStatusTxt
                            .setText(getString(R.string.record_start));
                            historyExampleDataBtn
                            .setVisibility(View.INVISIBLE);
                            break;
                        case 1:// 播放最新采集的数据
                            mStatusTxt
                            .setText(getString(R.string.replay_start));
                            historyExampleDataBtn
                            .setVisibility(View.VISIBLE);
                            historyExampleDataBtn
                            .setText(getString(R.string.history_data_btn_txt));
                            break;
                        case 2:// 演示数据
                            getLeadSystem();
                            replayPauseBtn.setVisibility(View.INVISIBLE);
                            zoomBtn.setVisibility(View.INVISIBLE);
                            backBtn.setVisibility(View.INVISIBLE);
                            mStatusTxt
                            .setText(getString(R.string.show_start));
                            historyExampleDataBtn
                            .setVisibility(View.VISIBLE);
                            historyExampleDataBtn
                            .setText(getString(R.string.example_data_btn_txt));
                            break;
                        default:
                            break;
                    }
                    modeChooseDialog.dismiss();
                }
            })
            .create();
            modeChooseDialog.show();
        }
    }
    
    /**
     * 处理采集、回放、演示模式的"开始"和"停止"点击事件
     */
    private void handleCollectRecordShowBtnClick()
    {
        switch (selectModeIndex)
        {
            case 0:
                mCollectOrReplayBtn.setEnabled(false);
                if (!isCollectOn) // 没有正在采集, 则弹出导联设置对话框再进行蓝牙设置
                {
                    createLeadChooseDialog(true);
                }
                else
                {
                    handleCollectBtnClick();
                }
                break;
            case 1:
                if(isReplayOn) //停止回放
                {
                    // 设置本次的Tag
                    currentTag = "";
                    isReplayOn = false;
                    mCollectOrReplayBtn.setImageResource(R.drawable.ic_begin);
                    mStatusTxt.setText(getString(R.string.replay_start));
                    replayPauseBtn.setVisibility(View.INVISIBLE);
                    zoomBtn.setVisibility(View.INVISIBLE);
                    backBtn.setVisibility(View.INVISIBLE);
                    resetScreen(true);
                    return;
                }
                
                if (lastReplayData != null) // 最近有回放数据
                {
                    if (FileUtil.isExist(lastReplayData.getFilePath()))
                    {
                        collectedDataDialog.dismiss();
                        // 回放模式导联系统以采集时的系统为准, 当前设置的暂时"失效", 模式转换时会重新读取当前导联系统(用户数据以所在导联文件夹为准)
                        // modified by zhf 20150930
                        //EcgDrawView.mCurentLead = lastReplayData.getLeadSystem();
                        isFirstSeg = true; //added by Huo
                        replayData(lastReplayData.getFilePath(), -1);
                    }
                    else
                    {
                        lastReplayData = null;
                        showToast(getString(R.string.file_not_exist));
                    }
                }
                else
                {
                    //播放最新采集的数据
                	isFirstSeg = true; //added by Huo
                    replayLatestData();
                }
                break;
            case 2:
                handleShowBtnClick();
                break;
            default:
                break;
        }
        
    }
    
    /**
     * 处理采集模式事件
     */
    private void handleCollectBtnClick()
    {
        if (!isCollectOn)
        {
            // 蓝牙相关设置已经成功
            if (mRfcommClient != null && device != null)
            {
                // 显示等待界面
                showWaitingView();
            }
            else if (device != null) // 匹配过蓝牙, 直接连接
            {
                // 显示等待界面
                showWaitingView();
            }
            else // 配对蓝牙
            {
//                mCollectOrReplayBtn.setEnabled(false);
                matchBluetooth(true);
            }
        }
        else
        {
        	// 不再截屏
        	needsWaveShot = false;
        	// 停止定时器
        	stopAlarm();
            // 设置本次的Tag
            currentTag = "";
            mCollectOrReplayBtn.setImageResource(R.drawable.ic_begin);
            mStatusTxt.setText(R.string.record_start);
            mStatusTxt.setTextColor(getResources()
                .getColor(R.color.record_textEnded));
            // 停止存储数据
            EcgSaveData.destroy();
            // 显示检测报告
            showDiagnoseResult();
            resetScreen(false);
//            isCollectOn = false;
            mCollectOrReplayBtn.setEnabled(true);
        }
    }
    
    
    /**
     * 显示采集结果
     */
    private void showDiagnoseResult()
    {
        collectEnd = System.currentTimeMillis();
        // 显示本次采集结果
        StringBuilder sb = new StringBuilder();
        // modified by zhf 20151005
        sb.append(String.format(getString(R.string.last_time), ((collectEnd - collectStart)/1000f))).append("\n\n");
        sb.append(getString(R.string.abnormal_result) + "\n\n");
        String ab = buildAbnormal();
        if (!TextUtils.isEmpty(ab))
        {
        	sb.append(ab + "\n\n");
        }
        else
        {
        	sb.append("无\n\n");
        }
        sb.append(getString(R.string.conclusion));
        if (abnormalParameterMap.size() > 0)
        {
            sb.append(getString(R.string.diagnose_conclusion));
        }
        else
        {
            sb.append(getString(R.string.none_diagnose_conclusion));
        }
        
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(collectStart);
        final String recordTime = calendar.get(Calendar.YEAR) + "-"
            + (calendar.get(Calendar.MONTH) + 1) + "-"
            + calendar.get(Calendar.DAY_OF_MONTH) + " "
            + calendar.get(Calendar.HOUR_OF_DAY) + "."
            + calendar.get(Calendar.MINUTE) + "."
            + calendar.get(Calendar.SECOND);
        
        LinearLayout diagnoseLayout = (LinearLayout)LayoutInflater.from(this).inflate(R.layout.diagnose_result, null);
        diagnoseLayout.findViewById(R.id.file_name_edt).requestFocus();
        TextView resultTxt = (TextView)diagnoseLayout.findViewById(R.id.result_txt);
        final EditText nameEdt = (EditText)diagnoseLayout.findViewById(R.id.file_name_edt);
        final EditText remarkEdt = (EditText)diagnoseLayout.findViewById(R.id.remark_edt);
        final CheckBox uploadCb = (CheckBox)diagnoseLayout.findViewById(R.id.upload_cb);
        resultTxt.setText(sb.toString());
        nameEdt.setText(recordTime);
        
        // modified by zhf 20151005
        AlertDialog dialog = new AlertDialog.Builder(this)
            .setTitle(R.string.diagnose_result)
                .setView(diagnoseLayout)
                .setPositiveButton(R.string.ok_btn_txt,
                    new OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            String fileName = recordTime;
                            if (!StringUtil.isNullOrEmpty(nameEdt.getText().toString()))
                            {
                                fileName = nameEdt.getText().toString();
                            }
                            if (EcgApp.getInstance().getEcgBinder().fileExist(fileName))
                            {
                                disableDialog(dialog);
                                showToast(getResources().getString(R.string.record_exist));
                            }
                            else
                            {
                            	enableDialog(dialog);
                            	updateWaveData(fileName, remarkEdt.getText().toString());
                            	// 重置显示控件
                            	mHeartRate.setText(R.string.question_mark);
                            	mHeartResult.setText(" ");
                            	EcgApp.getInstance().getEcgBinder().saveDotGraphShot(fileName, collectEnd - collectStart);
                            	if (uploadCb.isChecked())
                            	{
                            		// 上传数据
                            		Intent intent = new Intent(WaveScreen.this, DataTransferActivity.class);
                            		intent.putExtra("autoSend", true);
                            		String path = EcgService.mCurrentFileName.substring(0, EcgService.mCurrentFileName.lastIndexOf("/")) + "/" + fileName + EcgConst.FILE_END_NAME;
                            		intent.putExtra("filePath", path);
                            		startActivity(intent);
                            	}
                            }
                        }
                    })
                .setNegativeButton(R.string.cancel_btn_txt,
                    new OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            try
                            {
                                enableDialog(dialog);
                                // 清除界面显示
                                mHeartRate.setText(R.string.question_mark);
                                mHeartResult.setText(" ");
                                qrsValueList.clear();
                                prValueList.clear();
                                qtValueList.clear();
                                stValueList.clear();
                                abnormalParameterMap.clear();
                                EcgApp.getInstance().getEcgBinder().deleteData(dbHelper);
                            }
                            catch (Exception e)
                            {
                                LogUtil.e(TAG, e);
                            }
                        }
                    })
                .setOnCancelListener(new OnCancelListener()
                {

                    @Override
                    public void onCancel(DialogInterface dialog)
                    {
                        enableDialog(dialog);
                        updateWaveData(recordTime, remarkEdt.getText().toString());

                        // 重置显示控件
                        mHeartRate.setText(R.string.question_mark);
                        mHeartResult.setText(" ");
//                        drawView.saveNotFullWave();
                        EcgApp.getInstance().getEcgBinder().saveDotGraphShot(recordTime, collectEnd - collectStart);
                    }
                })
                .create();
        dialog.show();
        // added by zhf 20151005
        dialog.setOnDismissListener(new OnDismissListener() {
			
			@Override
			public void onDismiss(DialogInterface paramDialogInterface) {
				stopSpeaking();	
			}
		});
        startSpeaking(sb.toString());
    }
    
    private void enableDialog(DialogInterface dialog)
    {
        // 关闭对话框
        try
        {
            Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
            field.setAccessible(true);
            field.set(dialog, true);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    private void disableDialog(DialogInterface dialog)
    {
        // 不关闭对话框
        try
        {
            Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
            field.setAccessible(true);
            field.set(dialog, false);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    /**
     * 保存采集的数据
     */
    private void updateWaveData(String fileName, String remark)
    {
        String heartPara = mHeartRate.getText().toString(); // 心跳详细参数数据
        String abnormalValue = mHeartResult.getText().toString(); // 异常信息
        String diagnoseResult = buildResult(remark); //诊断结果
        WaveData data = new WaveData();
        data.setCollectFormatedTime(fileName);
        data.setStartTime(collectStart);
        data.setEndTime(collectEnd);
        data.setHeartPara(heartPara);
        data.setAbnormalValue(abnormalValue);
        data.setDiagnoseResult(diagnoseResult);
        data.setDesc(remark);
        EcgApp.getInstance().getEcgBinder().updateWaveData(dbHelper, data);
    }
    
    /**
     * 生成分析报告
     * @param remark 备注
     * @return 诊断结果
     */
    private String buildResult(String remark)
    {
       StringBuilder sb = new StringBuilder(100);
       sb.append(buildTime());
       sb.append("\n\n");
       sb.append(buildParameter());
       sb.append("\n\n");
       // modified by zhf 20151005
       String ab = buildAbnormal();
       if (!StringUtil.isNullOrEmpty(ab))
       {
    	   sb.append(ab);
    	   sb.append("\n\n");
       }
       if (!StringUtil.isNullOrEmpty(remark))
       {
           sb.append(buildRemark(remark));
           sb.append("\n\n");
       }
       if (abnormalParameterMap.size() > 0)
       {
           sb.append(getString(R.string.diagnose_conclusion));
       }
       else
       {
           sb.append(getString(R.string.none_diagnose_conclusion));
       }
       return sb.toString();
    }
    
    /**
     * 持续时间
     * @return
     */
    private String buildTime()
    {
        StringBuilder sb = new StringBuilder(100);
        sb.append(String.format(getString(R.string.last_time), ((collectEnd - collectStart)/1000f)));
        return sb.toString();
    }
    
    /**
     * 生成参数值
     * @return
     */
    private String buildParameter()
    {
        StringBuilder sb = new StringBuilder(100);
        DecimalFormat df1 = new DecimalFormat("0.00");
        if (qrsValueList.size() > 0)
        {
            sb.append("QRS宽度(ms):").append(df1.format((double)sumInt(qrsValueList)/qrsValueList.size())).append("\n");
        }
        else
        {
            sb.append("QRS宽度(ms):").append(getString(R.string.unknown)).append("\n");
        }
        
        if (prValueList.size() > 0)
        {
            sb.append("PR间期(ms):").append(df1.format((double)sumInt(prValueList)/prValueList.size())).append("\n");
        }
        else
        {
            sb.append("PR间期(ms):").append(getString(R.string.unknown)).append("\n");
        }
        
        if (qtValueList.size() > 0)
        {
            sb.append("QT间期(ms):").append(df1.format((double)sumInt(qtValueList)/qtValueList.size())).append("\n");
        }
        else
        {
            sb.append("QT间期(ms):").append(getString(R.string.unknown)).append("\n");
        }
        
        if (stValueList.size() > 0)
        {
            sb.append("ST段高度(mV):").append(df1.format((double)sumDouble(stValueList)/stValueList.size())).append("\n");
        }
        else
        {
            sb.append("ST段高度(mV):").append(getString(R.string.unknown)).append("\n");
        }
        return sb.toString();
    }
    
    /**
     * 生成异常信息
     * @return
     */
    private String buildAbnormal()
    {
        StringBuilder sb = new StringBuilder(100);
        Set<String> keys = abnormalParameterMap.keySet();
        Iterator<String> it = keys.iterator();
        while (it.hasNext())
        {
            String abnomalStr = it.next();
            Integer value = abnormalParameterMap.get(abnomalStr);
            if (abnomalStr.equals("心动过缓") || abnomalStr.equals("窦性心动过速"))
            {
                sb.append(abnomalStr).append(":").append(value * 10).append("s\n");
            }
            else
            {
                sb.append(abnomalStr).append(":").append(value).append("次\n");
            }
        }
        return sb.toString();
    }
    
    /**
     * 备注
     * @return
     */
    private String buildRemark(String remark)
    {
        StringBuilder sb = new StringBuilder(100);
        sb.append(getString(R.string.report_remark));
        sb.append(remark);
        return sb.toString();
    }
    
    private int sumInt(List<Integer> nums)
    {
        int n = 0;
        for (Integer i : nums)
        {
            n += i;
        }
        return n;
    }
    
    private double sumDouble(List<Double> nums)
    {
        double n = 0;
        for (Double i : nums)
        {
            n += i;
        }
        return n;
    }
    
    /**
     * 处理演示模式事件
     */
    private void handleShowBtnClick()
    {
        if (!isShowOn)
        {
            createDemoWaveDialog();
        }
        else
        {
            // 设置本次的Tag
            currentTag = "";
            resetScreen(true);
            isShowOn = false;
            mCollectOrReplayBtn.setImageResource(R.drawable.ic_begin);
            mStatusTxt.setText(R.string.show_start);
        }
    }
    
    /**
     * 清空屏幕并停止数据采集和分析线程
     */
    private void resetScreen(boolean clearView)
    {
        if (mTestDataHandle != null)
        {
            // 结束数据采集线程
            mTestDataHandle.stopThread();
            mTestDataHandle = null;
        }
        if (mRfcommClient != null)
        {
            mRfcommClient.stop();
            mRfcommClient = null;
        }
        synchronized (EcgWaveData.mWaveData)
        {
            // 清空缓存数据
            for (int i = 0; i < EcgWaveData.mWaveData.size(); i++)
            {
                EcgWaveData.mWaveData.get(i).clear();
            }
        }
        synchronized (EcgWaveData.leadFirstData)
        {
            EcgWaveData.leadFirstData.clear();
        }
        synchronized (EcgWaveData.leadSecondData)
        {
            EcgWaveData.leadSecondData.clear();
        }
        // 暂停数据处理线程
        EcgWaveData.pauseThread();
        EcgWaveData.clearAnalyseData();
        drawView.reset();
        
//        mHeartRate.setText(R.string.question_mark);
        mHeartPara.setText(" ");
//        mHeartResult.setText(" ");
        mCollectOrReplayBtn.setEnabled(true);
        
//        qrsValueList.clear();
//        prValueList.clear();
//        qtValueList.clear();
//        stValueList.clear();
//        abnormalParameterMap.clear();
        if (clearView)
        {
            mHeartRate.setText(R.string.question_mark);
            mHeartResult.setText(" ");
            qrsValueList.clear();
            prValueList.clear();
            qtValueList.clear();
            stValueList.clear();
            abnormalParameterMap.clear();
        }
        
        // 暂停蓝牙缓冲处理线程
        BtBufferProcesser.getInstatce().pauseThread();
        stopAlarm();
        progressLay.setVisibility(View.GONE);
    }
    
    
    /** 导联系统设置菜单*/
    private MenuItem mLeadSystemMenuItem;
    /** 蓝牙配对菜单*/
    private MenuItem mBluetoothMatchMenuItem;
    /** 心率知识库菜单*/
    private MenuItem mWaveKnowledgeMenuItem;
    /** 数据发送*/
    private MenuItem dataTransferMenuItem;
    /** 紧急电话菜单*/
    private MenuItem mEmergentCallMenuItem;
    /** 退出系统菜单*/
    private MenuItem mQuitMenuItem;
    /** 切换导联菜单, 尽在正在采集肢体导联或者模拟肢体导联时才有*/
    private MenuItem mSwitchLeadMenuItem;

    /**
     * 创建菜单项
     * @param menu
     * @return
     * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        mLeadSystemMenuItem = menu.add(0, 0, 0, R.string.wave_settings_menu);
        mBluetoothMatchMenuItem = menu.add(0, 1, 0, R.string.bluetooth_match_menu);
        mSwitchLeadMenuItem = menu.add(0, 2, 0, R.string.switch_lead_menu);
        mWaveKnowledgeMenuItem = menu.add(0, 3, 0, R.string.wave_knowledge_menu);
        dataTransferMenuItem = menu.add(0, 4, 0, R.string.data_transfer);
        mEmergentCallMenuItem = menu.add(0, 5, 0, R.string.emergent_call_menu);
        mQuitMenuItem = menu.add(0, 6, 0, R.string.quit_menu);
        return true;
    }

    /**
     * 可根据具体要求隐藏或显示部分菜单项
     * @param menu
     * @return
     * @see android.app.Activity#onPrepareOptionsMenu(android.view.Menu)
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
    	// added by zhf 2015.09.28
    	mLeadSystemMenuItem.setVisible(false);
    	
        if (selectModeIndex == 1) //回放模式下不提供蓝牙配对设置、导联系统设置
        {
            mLeadSystemMenuItem.setVisible(false);
            mBluetoothMatchMenuItem.setVisible(false); 
            mSwitchLeadMenuItem.setVisible(true);
            mWaveKnowledgeMenuItem.setVisible(true);
            dataTransferMenuItem.setVisible(false);
            mEmergentCallMenuItem.setVisible(true);
            mQuitMenuItem.setVisible(true);
        }
        else
        {
            mWaveKnowledgeMenuItem.setVisible(true);
            mEmergentCallMenuItem.setVisible(true);
            mQuitMenuItem.setVisible(true);
            
            //采集模式下且正在采集(肢体导联、模拟肢体导联)提供切换导联功能、屏蔽导联系统设置、蓝牙匹配
            if (selectModeIndex == 0 && isCollectOn && (EcgDrawView.mCurentLead == EcgConst.LIMB_LEAD 
                || EcgDrawView.mCurentLead == EcgConst.MOCK_LIMB_LEAD))
            {
                mLeadSystemMenuItem.setVisible(false);
                mBluetoothMatchMenuItem.setVisible(false);
                dataTransferMenuItem.setVisible(false);
                mSwitchLeadMenuItem.setVisible(true);
            }
            else if (selectModeIndex == 0 && (isCollectOn || !mCollectOrReplayBtn.isEnabled())) // 正在采集或者用户点击了采集到正在采集这个阶段屏蔽菜单
            {
//                mLeadSystemMenuItem.setVisible(false);
//                mBluetoothMatchMenuItem.setVisible(false);
                dataTransferMenuItem.setVisible(false);
                mSwitchLeadMenuItem.setVisible(true);
            }
            else
            {
                mLeadSystemMenuItem.setVisible(true);
                mBluetoothMatchMenuItem.setVisible(true);
                mSwitchLeadMenuItem.setVisible(true);
                dataTransferMenuItem.setVisible(true);
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case 0://导联设置
                createLeadChooseDialog(false);
                return true;
            case 1://蓝牙配对
                matchBluetooth(false);
                return true;
            case 2://切换导联
//                EcgDrawView.switchScreen = !EcgDrawView.switchScreen;
                drawView.switchScreen();
                return true;
            case 3://心电知识库
                new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        // 读取心电知识库文档
                        try
                        {
                            final String knowledge = FileUtil.getTxtFileContent(
                                EcgApp.getInstance().getAssets().open("wave_data_knowledge.txt"));
                            runOnUiThread(new Runnable()
                            {
                                
                                @Override
                                public void run()
                                {
                                    createRichTxtDialog(getString(R.string.wave_knowledge_menu), knowledge);
                                }
                            });
                        }
                        catch (IOException e)
                        {
                            LogUtil.e(TAG, e);
                        }
                    }
                }).start();
                return true;
            case 4:
            	startActivity(new Intent(this, DataTransferActivity.class));
            	break;
            case 5://紧急电话
                SharedPreferences sp = getSharedPreferences(TAG, MODE_PRIVATE);
                final Editor editor = sp.edit();
                String phone = sp.getString("EMERGENT_CALL", "");
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("输入紧急短信通知的号码");
                LinearLayout lay = (LinearLayout)LayoutInflater.from(this).inflate(R.layout.phone_input_layout,null);
                final EditText phoneInput = (EditText)lay.findViewById(R.id.phone_input);
                phoneInput.setText(phone);
                builder.setView(lay);
                builder.setPositiveButton(R.string.ok_btn_txt, new OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        editor.putString("EMERGENT_CALL", phoneInput.getText().toString());
                        editor.commit();
                    }
                }).setNegativeButton(R.string.cancel_btn_txt, null).create().show();
                return true;
            case 6://退出
                LogUtil.d(TAG, "onOptionsItemSelected >> 退出");
                finishAll();
                android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(0);
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    
    /**
     * 创建导联选择对话框
     * @param isClikFromCollet 是否是开始采集出发的
     */
    private void createLeadChooseDialog(final boolean isClikFromCollet)
    {
//        new AlertDialog.Builder(this)
//            .setTitle(R.string.wave_settings_menu)
//                .setSingleChoiceItems(R.array.lead_choose_items,
//                    selectLeadIndex,
//                    new OnClickListener()
//                    {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which)
//                        {
//                            selectLeadIndex = which;
//                        }
//                    })
//                .setPositiveButton(getString(R.string.ok_btn_txt),
//                    new OnClickListener()
//                    {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which)
//                        {
//                            switch (selectLeadIndex)
//                            {
//                                case 0:
//                                    DataStore.getInstance().setLeadSystem(EcgConst.LIMB_LEAD);
//                                    EcgDrawView.mCurentLead = EcgConst.LIMB_LEAD;
//                                    break;
//                                case 1:
//                                    DataStore.getInstance().setLeadSystem(EcgConst.MOCK_LIMB_LEAD);
//                                    EcgDrawView.mCurentLead = EcgConst.MOCK_LIMB_LEAD;
//                                    break;
//                                case 2:
//                                    DataStore.getInstance().setLeadSystem(EcgConst.MOCK_CHEST_LEAD);
//                                    EcgDrawView.mCurentLead = EcgConst.MOCK_CHEST_LEAD;
//                                    break;
//                                case 3:
//                                    DataStore.getInstance().setLeadSystem(EcgConst.SIMPLE_LIMB_LEAD);
//                                    EcgDrawView.mCurentLead = EcgConst.SIMPLE_LIMB_LEAD;
//                                    break;
//                                default:
//                                    break;
//                            }
//                            
//                            if (isClikFromCollet)
//                            {
//                                handleCollectBtnClick();
//                            }
//                        }
//                    })
//                .setNegativeButton(getString(R.string.cancel_btn_txt),
//                    new OnClickListener()
//                    {
//
//                        @Override
//                        public void onClick(DialogInterface dialog, int which)
//                        {
//                            getLeadSystem();
//                            if (isClikFromCollet)
//                            {
//                                handleCollectBtnClick();
//                            }
//                        }
//                    })
//                .setOnCancelListener(new OnCancelListener()
//                {
//
//                    @Override
//                    public void onCancel(DialogInterface dialog)
//                    {
//                        getLeadSystem();
//                        mCollectOrReplayBtn.setEnabled(true);
//                    }
//                })
//                .create()
//                .show();
    	
    	// 默认只使用简易肢体导联zhf 2015.09.28
		new AlertDialog.Builder(this)
				.setTitle("请选择测量时长")
				.setSingleChoiceItems(new String[]{"2分钟", "5分钟", "不限时"},
						selectTimeInterval, new OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								selectTimeInterval = which;
							}
						})
				.setPositiveButton(getString(R.string.ok_btn_txt),
						new OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								DataStore.getInstance().setLeadSystem(
										EcgConst.SIMPLE_LIMB_LEAD);
								EcgDrawView.mCurentLead = EcgConst.SIMPLE_LIMB_LEAD;
								if (isClikFromCollet) {
									handleCollectBtnClick();
								}
							}
						})
				.setNegativeButton(getString(R.string.cancel_btn_txt),
						null).create().show();
    }
    
    private boolean autoConnect;
    /**
     * 蓝牙配对
     * @param autoConnect 配对完成后是自动连接蓝牙开始采集
     */
    private void matchBluetooth(boolean autoConnect)
    {
        this.autoConnect = autoConnect;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null)
        {
            showToast(getString(R.string.bluetooth_not_availble));
            mCollectOrReplayBtn.setEnabled(true);
        }
        else
        {
            if (!mBluetoothAdapter.isEnabled())
            {
                Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableIntent,
                    REQUEST_ENABLE_BT);
            }
            // Otherwise, setup the Oscillosope session
            else
            {
                setupBTConnection();
            }
        }
    }
    
    /**
     * 处理选择演示数据回调
     * @param selectedDemo
     * @param action
     * @see com.nju.ecg.wave.DemoModeResultListener#onResult(int, com.nju.ecg.wave.DemoModeResultListener.ModeAction)
     */
    @Override
    public void onResult(int selectedDemo, ModeAction action)
    {
        if (demoWaveDialog != null && demoWaveDialog.isShowing())
        {
            demoWaveDialog.dismiss();
        }
        switch (action)
        {
            case ACTION_SHOW:
                handleDemoShow(selectedDemo);
                break;
            case ACTION_DETAIl:
            	String fileName = "";
            	switch (selectedDemo) {
            	case 0:
                    fileName = "Record_DXXDGS.txt";
                    break;
                case 1:
                	fileName = "Record_DXXXBQ.txt";
                    break;
                case 2:
                	fileName = "Record_FXZB.txt";
                    break;
                case 3:
                	fileName = "Record_SXZB.txt";
                    break;
                case 4:
                	fileName = "Record_SXZB2.txt";
                    break;
                case 5:
                	fileName = "Record_DXXDGH.txt";
                    break;
                case 6:
                	fileName = "Record_ZCDXXL.txt";
                    break;
                default:
                    break;
				}
                try
                {
                    final String knowledge = FileUtil.getTxtFileContent(
                        EcgApp.getInstance().getAssets().open(fileName));
                    runOnUiThread(new Runnable()
                    {
                        
                        @Override
                        public void run()
                        {
                            createRichTxtDialog(getString(R.string.view_detail_btn_txt), knowledge);
                        }
                    });
                }
                catch (IOException e)
                {
                    LogUtil.e(TAG, e);
                }
                break;
            default:
                break;
        }
    }
    
    /**
     * 处理历史采集数据回调
     * @param data
     * @param action
     * @see com.nju.ecg.wave.CollectedDataResultListener#onResult(int, com.nju.ecg.wave.CollectedDataResultListener.Action)
     */
    @Override
    public void onResult(final int index, final WaveData data, final Action action)
    {
        switch (action)
        {
            case ACTION_REPLAY:// 回放
                if (FileUtil.isExist(data.getFilePath()))
                {
                    collectedDataDialog.dismiss();
                    // 回放模式导联系统以采集时的系统为准, 当前设置的暂时"失效", 模式转换时会重新读取当前导联系统(用户数据以所在导联文件夹为准)
                    // modified by zhf 20150930
                    // EcgDrawView.mCurentLead = data.getLeadSystem();
                    isFirstSeg = true; //added by Huo
                    replayData(data.getFilePath(), -1);
                    lastReplayData = data;
                }
                else
                {
                    showToast(getString(R.string.file_not_exist));
                }
                break;
            case ACTION_DIAGNOSE:// 检测报告(标准待定, 暂时显示采集结果)
                if (data.isCustom())
                {
                    final String reportPath = data.getFilePath().substring(0, 
                        data.getFilePath().indexOf(EcgConst.FILE_END_NAME)) + EcgConst.REPORT_FILE_END_NAME;
                    if (!FileUtil.isExist(reportPath))
                    {
                        showToast(getString(R.string.file_not_exist));
                        return;
                    }
                    else
                    {
                        new Thread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                // 读取sdcard检测报告
                                try
                                {
                                    final String report = FileUtil.getTxtFileContent(new FileInputStream(new File(reportPath)));
                                    runOnUiThread(new Runnable()
                                    {
                                        
                                        @Override
                                        public void run()
                                        {
                                            createRichTxtDialog(getString(R.string.diagnose_result), report);
                                        }
                                    });
                                }
                                catch (IOException e)
                                {
                                    LogUtil.e(TAG, e);
                                }
                            }
                        }).start();
                    }
                }
                else
                {
                	// modified by zhf 20151005
                	String reportDir = data.getFilePath().substring(0, 
                            data.getFilePath().indexOf(EcgConst.FILE_END_NAME)) + "_report";
                	File[] files = new File(reportDir).listFiles(new FilenameFilter() {
						
						@Override
						public boolean accept(File dir, String filename) {
							return !filename.endsWith("_dot.png");
						}
					});
                    createRichTxtDialog(getString(R.string.diagnose_result), data.getDiagnoseResult(), files.length > 0? files[(int)(files.length * 0.7)].getAbsolutePath() : null);
                }
                break;
            case ACTION_DOT_GRAPH:// 显示散点图
                // RR间期数据文件路径
                String rrPath = data.getFilePath().substring(0, 
                    data.getFilePath().indexOf(EcgConst.FILE_END_NAME)) + EcgConst.RR_FILE_END_NAME;
                long collectingTime = data.getEndTime() - data.getStartTime();
                if (FileUtil.isExist(rrPath))
                {
                    // 携带路径跳转视图
                    Intent intent = new Intent(this, DotGraphActivity.class);
                    intent.putExtra(DotGraphActivity.RR_FILE_PATH_KEY, rrPath);
                    intent.putExtra(DotGraphActivity.COLLECTING_TIME, collectingTime);
                    startActivity(intent);
                }
                else
                {
                    showToast(getString(R.string.file_not_exist));
                }
                break;
            case ACTION_DELETE:// 删除
                new AlertDialog.Builder(this)
                    .setTitle(R.string.dialog_title)
                        .setMessage(R.string.delete_dialog_message)
                        .setPositiveButton(R.string.ok_btn_txt,
                            new OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialog,
                                    int which)
                                {
                                    if (dataList != null
                                        && dataList.size() > index)
                                    {
                                        dataList.remove(index);
                                    }
                                    dbHelper.delete(data.get_id());
                                    try
                                    {
                                        // 删除对应文件
                                        File file = new File(data.getFilePath());
                                        file.delete();
                                        
                                        // 删除报告文件
                                        File reportFile = new File(data.getFilePath().substring(0, 
                                            data.getFilePath().indexOf(EcgConst.FILE_END_NAME)) + EcgConst.REPORT_FILE_END_NAME);
                                        reportFile.delete();
                                        
                                        // 删除散点图文件
                                        File rrFile = new File(data.getFilePath().substring(0, 
                                            data.getFilePath().indexOf(EcgConst.FILE_END_NAME)) + EcgConst.RR_FILE_END_NAME);
                                        rrFile.delete();
                                    }
                                    catch (Exception e)
                                    {
                                        LogUtil.e(TAG, e);
                                    }
                                    dataAdapter.notifyDataSetChanged();
                                }
                            })
                        .setNegativeButton(R.string.cancel_btn_txt,
                            null)
                        .create()
                        .show();
                break;
            default:
                break;
        }
    }
    
    /**
     * 处理数据演示
     * @param selectedDemo 选择的演示数据
     */
    private void handleDemoShow(int selectedDemo)
    {
        // 设置本次的Tag
        currentTag = String.valueOf(System.currentTimeMillis());
        isShowOn = true;
        mCollectOrReplayBtn.setImageResource(R.drawable.ic_stop);
        mStatusTxt.setText(getString(R.string.stop_btn_txt));
        
        resetScreen(true);
        switch (selectedDemo)
        {
            case 0:
                if (mTestDataHandle == null)
                {
                    mTestDataHandle = new TestDataHandle("Record_DXXDGS.raw");
                    mTestDataHandle.start();
                }
                break;
            case 1:
                if (mTestDataHandle == null)
                {
                    mTestDataHandle = new TestDataHandle("Record_DXXXBQ.raw");
                    mTestDataHandle.start();
                }
                break;
            case 2:
                if (mTestDataHandle == null)
                {
                    mTestDataHandle = new TestDataHandle("Record_FXZB.raw");
                    mTestDataHandle.start();
                }
                break;
            case 3:
                if (mTestDataHandle == null)
                {
                    mTestDataHandle = new TestDataHandle("Record_SXZB.raw");
                    mTestDataHandle.start();
                }
                break;
            case 4:
                if (mTestDataHandle == null)
                {
                    mTestDataHandle = new TestDataHandle("Record_SXZB2.raw");
                    mTestDataHandle.start();
                }
                break;
            case 5:
                if (mTestDataHandle == null)
                {
                    mTestDataHandle = new TestDataHandle("Record_DXXDGH.raw");
                    mTestDataHandle.start();
                }
                break;
            case 6:
                if (mTestDataHandle == null)
                {
                    mTestDataHandle = new TestDataHandle("Record_ZCDXXL.raw");
                    mTestDataHandle.start();
                }
                break;
            default:
                break;
        }
        // 启动数据分析线程
        isFirstSeg = true; //added by Huo
        EcgWaveData.resumeThread();
    }
    
    /**
     * 发送紧急短信
     */
    private void sendEmergentSMS(final int hr)
    {
        SharedPreferences sp = getSharedPreferences(TAG, MODE_PRIVATE);
        final String phone = sp.getString("EMERGENT_CALL", "");
        /**
         * 同时满足以下几个情况则发送紧急短信
         * 1.采集模式且正在采集 
         * 2.本次采集未发送过紧急短信 
         * 3.用户设置了紧急通知号码
         */
        if (!StringUtil.isNullOrEmpty(phone) && !hasSendSMS && (selectModeIndex == 0 && isCollectOn))
        {
        	hasSendSMS = true;
        	LocationManager.getInstance().requestLocation(false, new LocationResultListener() {
				
				@Override
				public void onLocationResult(final LocationInfo locationInfo) {
					new Thread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            String addressInfo = "";
                            if (null != locationInfo && !StringUtil.isNullOrEmpty(locationInfo.getAddressInfo()))
                            {
                                addressInfo = locationInfo.getAddressInfo();
                            }
                            SmsManager smsManager = SmsManager.getDefault();
                            String content;
                            if (hr < 50)
                            	content = "患者心率：" + hr + "，可能有严重心动过缓，建议立即联系患者并确认情况。";
                            else
                            	content = "患者心率：" + hr + "，可能有严重心动过速，建议立即联系患者并确认情况。";
                            if (!StringUtil.isNullOrEmpty(addressInfo))
                            {
                                content += "\n" + "患者现在位置：" + addressInfo;
                            }
                            smsManager.sendTextMessage(phone, null, content, null, null);
                        }
                    }).start();
				}
				
				@Override
				public void locationError(ErrorTypes errorTypes) {
					showToast(getString(R.string.location_error));
					new Thread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            String addressInfo = "";
                            SmsManager smsManager = SmsManager.getDefault();
                            String content;
                            if (hr < 50)
                            	content = "患者心率：" + hr + "，可能有严重心动过缓，建议立即联系患者并确认情况。";
                            else
                            	content = "患者心率：" + hr + "，可能有严重心动过速，建议立即联系患者并确认情况。";
                            if (!StringUtil.isNullOrEmpty(addressInfo))
                            {
                                content += "\n" + "患者现在位置：" + addressInfo;
                            }
                            smsManager.sendTextMessage(phone, null, content, null, null);
                        }
                    }).start();
				}
			}, null);
        }
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_BACK)
        {
            LogUtil.d(TAG, "KEYCODE_BACK");
            finishAll();
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(0);
        }
        return super.onKeyDown(keyCode,
            event);
    }
    
    @Override
    public void onResume()
    {
        super.onResume();
        // 唤醒屏幕
        mWakeLock.acquire();
    }
    
    @Override
    protected void onPause()
    {
        super.onPause();
        //释放唤醒锁
        mWakeLock.release();
    }

    
    @Override
    public void onDestroy()
    {
    	super.onDestroy();
        LogUtil.d(TAG, "onDestroy");
        if (mTestDataHandle != null)
        {
            mTestDataHandle.stopThread();
            mTestDataHandle = null;
        }
        unRegisterDispatchServiceReceiver();
        if (mRfcommClient != null)
        {
            mRfcommClient.stop();
            mRfcommClient = null;
        }
        drawView.stopDrawThread();

        /**
         * 停止数据存储
         */
        EcgSaveData.destroy();
        
        /**
         * 意外停止时, 如果正在采集数据, 则更新数据库记录
         */
        if (isCollectOn)
        {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(collectStart);
            final String recordTime = calendar.get(Calendar.YEAR) + "-"
                + (calendar.get(Calendar.MONTH) + 1) + "-"
                + calendar.get(Calendar.DAY_OF_MONTH) + " "
                + calendar.get(Calendar.HOUR) + "."
                + calendar.get(Calendar.MINUTE) + "."
                + calendar.get(Calendar.SECOND);
            updateWaveData(recordTime, null);
//            drawView.saveNotFullWave();
            EcgApp.getInstance().getEcgBinder().saveDotGraphShot(recordTime, collectEnd - collectStart);
        }
        needsWaveShot = false;
        stopAlarm();
        
        // added by zhf 20151005
        mTts.stopSpeaking();
		// 退出时释放连接
		mTts.destroy();
    }

    private static final int ABNORMAL_BITS_POSITION = 100;
    /**
     * RR间期总数下标
     */
    private static final int TOTAL_RR_INTERVAL_POSITION = 15;
    /**
     * 异常参数总数
     */
    private static final int TOTAL_ABNORMAL_PARAMETER_VALUE = 13;
    private BroadcastReceiver mReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            if (EcgIntent.ECG_HEART_RATE.equals(action) && currentTag.equals(intent.getStringExtra("tag")))
            {
                int raw[] = intent.getIntArrayExtra("ecg_parameter");
                int hr = raw[10]; // heart rate
                if (hr > 0)
                    mHeartRate.setText(Integer.toString(hr));
                analysisnum++;		//记录分析数据段收到的次数（满一定次数后才可以发通知短信）
                double[] p = new double[raw.length];
                for (int i = 0; i < p.length; i++)
                {
                    p[i] = (double) raw[i];
                }
                // QRS
                p[2] = p[2] * 0.8;
                p[3] = p[3] / 1000 / 500;
                p[4] = p[4] / 1000 / 500;
                p[5] = p[5] / 1000 / 500;
                p[6] = p[6] / 1000 / 500;
                p[7] = p[7] / 1000 / 500;
                p[8] = p[8] / 1000 / 500;
                DecimalFormat df1 = new DecimalFormat("0.00");

                if (raw[0] == 0 || raw[13] == 1)
                {
                    mHeartPara.setText(R.string.wave_small_or_exception);
                }
                else
                {
                    StringBuilder builder = new StringBuilder();
//                builder.append(String.format("P波宽度  %s ms, ",
//                    Integer.toString(raw[0])));
                    builder.append(String.format("QRS宽度 %s ms, ",
                        Integer.toString(raw[2])));
                    qrsValueList.add(raw[2]);
//                builder.append(String.format("P波幅度 %s mV, ",
//                    df1.format(p[3])));
//                builder.append(String.format("Q波幅度  %s mV, ",
//                    df1.format(p[4])));
//                builder.append(String.format("R波幅度 %s mV, ",
//                    df1.format(p[5])));
//                builder.append(String.format("S波幅度 %s mV, ",
//                    df1.format(p[6])));
//                builder.append(String.format("T波幅度 %s mV, ",
//                    df1.format(p[7])));
                    builder.append(String.format("ST段高度  %s mV, ",
                        df1.format(p[8])));
                    stValueList.add(Double.valueOf(df1.format(p[8])));
//                builder.append(String.format("RR间期  %s ms, ",
//                    Integer.toString(raw[9])));
                    builder.append(String.format("PR间期  %s ms, ",
                        Integer.toString(raw[11])));
                    prValueList.add(raw[11]);
                    builder.append(String.format("QT间期  %s ms",
                        Integer.toString(raw[13])));
                    qtValueList.add(raw[13]);
                    LogUtil.d(TAG,
                        builder.toString());
                    mHeartPara.setText(builder);
                    mHeartPara.setFocusable(true);
                    
                    if (selectModeIndex == 0 && isCollectOn) // 采集模式且正在采集
                    {
                        // 存储RR间期数据
                        saveRRInterval(raw);
                    }
                    
                    // 发送紧急短信
                    if ((analysisnum > 6) )
                    {
                    	if ((hr > 0 && hr < 40))
                   			emergentstage1++;
                        else if (hr > 160)
                        	emergentstage2++;
                    	else
                    	{
                    		emergentstage1 = 0;
                    		emergentstage2 = 0;
                    	}
                    	if ((emergentstage1==3) || (emergentstage2==3)) //连续出现严重心率异常达3次，发送报警信息
                    		sendEmergentSMS(hr);
                    }
                }
                
                if (raw[0] != 0 && raw[13] != 1 && !isFirstSeg) //增加一个判断，阻止回放时第1段结果的显示，消极避免与前次数据混合造成错误，modified by Huo
                {
                    String[] msgEcgPatinetResult = getResources()
                    .getStringArray(R.array.string_ecg_patient_result);
                    mHeartResult.setVisibility(View.VISIBLE);
                    LogUtil.d(TAG,
                        "abnormalValue = " + raw[ABNORMAL_BITS_POSITION]);
                    mHeartResult.setText("");
                    if (raw[ABNORMAL_BITS_POSITION] != 0)
                    {
                        LogUtil.d(TAG,
                            "abnormalValue = " + raw[ABNORMAL_BITS_POSITION]);
                        String abnormalString = "";
                        for (int j = 0; j < TOTAL_ABNORMAL_PARAMETER_VALUE; j++)
                        {
                            if ((raw[ABNORMAL_BITS_POSITION] & 0x1 << j) > 0)
                            {
                                LogUtil.d(TAG,
                                    j + "bit: " + raw[ABNORMAL_BITS_POSITION]);
                                if (!TextUtils.isEmpty(abnormalString))
                                    abnormalString += ", ";
                                abnormalString += msgEcgPatinetResult[j];
                                
                                abnormalParameterMap
                                    .put(msgEcgPatinetResult[j],
                                        (abnormalParameterMap
                                            .get(msgEcgPatinetResult[j]) == null ? 1
                                            : abnormalParameterMap
                                                .get(msgEcgPatinetResult[j])) + 1);
                            }
                        }
                        mAbnormalValuse |= raw[ABNORMAL_BITS_POSITION];
                        mHeartResult.setText(abnormalString);
                    }
                }
                else
                {
                    mHeartResult.setVisibility(View.INVISIBLE);
                    isFirstSeg = false; //added by Huo
                }
            }
        }
        
        /**
         * 存储RR间期值
         * @param raw 数据源
         */
        private void saveRRInterval(int[] raw)
        {
            // RR间期数据长度
            int rrIntervalNum = raw[TOTAL_RR_INTERVAL_POSITION];
            if (rrIntervalNum > 0)
            {
                List<Integer> rrList = new ArrayList<Integer>();
                for (int i = TOTAL_RR_INTERVAL_POSITION + 1; i <= TOTAL_RR_INTERVAL_POSITION
                    + rrIntervalNum; i++)
                {
                    rrList.add(raw[i]);
                }
                String rrStr = ", " + rrList.toString().replace("[","").replace("]","");
                EcgApp.getInstance().getEcgBinder().saveRRData(rrStr);
            }
        }
    };

    private void registerDispatchServiceReceiver()
    {
        IntentFilter intentFilter = new IntentFilter(EcgIntent.ECG_HEART_RATE);
        registerReceiver(mReceiver,
            intentFilter);
    }

    private void unRegisterDispatchServiceReceiver()
    {
        if (mReceiver != null)
        {
            try
            {
                unregisterReceiver(mReceiver);
                mReceiver = null;
            }
            catch (Exception e)
            {
                LogUtil.e(TAG, e);
            }
        }
    }
    
    //------------心率报告只保存一分钟以内--------------//
    /**
     * 启动定时器
     */
    private void startAlarm()
    {
    	Intent intent = new Intent(this, ReportReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(this, 0, intent, 0);
        //设定一个一分钟后的时间
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.MINUTE, 1);
        AlarmManager alarm = (AlarmManager)getSystemService(ALARM_SERVICE);
        alarm.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), sender);
    }
    
    /**
     * 取消定时器
     */
    private void stopAlarm()
    {
    	Intent intent = new Intent(this, ReportReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(this, 0, intent, 0);
        AlarmManager alarm = (AlarmManager)getSystemService(ALARM_SERVICE);
        alarm.cancel(sender);
    }
    //------------心率报告只保存一分钟以内--------------//
    
    /**
     * 显示提示蓝牙断开Dialog
     * @param strId
     */
    public void showBluetoothNoAccessDialog()
    {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(R.string.dialog_title);
        dialog.setMessage(R.string.title_not_connected);
        dialog.setPositiveButton(R.string.retry_btn_txt, new OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
               isCollectOn = false;
               handleCollectBtnClick();
            }
        });
        dialog.setNegativeButton(R.string.cancel_btn_txt, new OnClickListener()
        {
            
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                device = null;
            }
        });
        dialog.setOnCancelListener(new OnCancelListener()
        {
            
            @Override
            public void onCancel(DialogInterface dialog)
            {
                device = null;
            }
        });
        dialog.create();
        dialog.show();
    }
    
    /**
     * 显示等待界面
     */
    public void showWaitingView()
    {
        progressDialog.setMessage(getString(R.string.title_connecting));
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    // 尝试蓝牙连接, 无法连接则直接提示用户, 不进入等待界面
//                    BluetoothSocket bSocket = device.createRfcommSocketToServiceRecord(BluetoothRfcommClient.MY_UUID);
//                    bSocket.connect();
//                    bSocket.close();
                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            // 关闭对话框
                            if (progressDialog != null && progressDialog.isShowing())
                            {
                                progressDialog.dismiss();
                            }
                            // 显示等待界面
                            waitingView.setVisibility(View.VISIBLE);
                            waitingLayout.setVisibility(View.VISIBLE);
                        }
                    });
                }
                catch (Exception e)
                {
                    LogUtil.e(TAG, e);
                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            if (progressDialog != null && progressDialog.isShowing())
                            {
                                progressDialog.dismiss();
                            }
                            mCollectOrReplayBtn.setEnabled(true);
                            showBluetoothNoAccessDialog();
                        }
                    });
                }
            }
        }).start();
    }

    /******************************* Start Bluetooth configuration ********/
    // BlueTooth
    BluetoothDevice device;

    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        switch (requestCode)
        {
            case REQUEST_CONNECT_DEVICE:
                // 搜索蓝牙设备成功并返回
                if (resultCode == Activity.RESULT_OK)
                {
                    // 蓝牙MAC地址
                    String address = data
                        .getExtras()
                            .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    // 根据MAC地址获取蓝牙设备
                    device = mBluetoothAdapter.getRemoteDevice(address);
                    
                    // 保存address到设置
                    DataStore.getInstance().saveBluetoothAddress(address);
                    if (autoConnect)
                    {
                        // 显示等待界面
                        showWaitingView();
                    }
                    else
                    {
                        showToast(getString(R.string.bluetooth_match_success));
                    }
                }
                else
                {
                    isCollectOn = false;
                    mRfcommClient = null;
                    device = null;
                    mCollectOrReplayBtn.setEnabled(true);
                }
                break;
            case REQUEST_ENABLE_BT:
                // 成功开启蓝牙功能
                if (resultCode == Activity.RESULT_OK)
                {
                    // 启动蓝牙连接
                    setupBTConnection();
                }
                else
                {
                    // 用户没有打开蓝牙设备或者打开蓝牙设备失败
                    showToast(getString(R.string.bt_not_enabled_leaving));
                    isCollectOn = false;
                    mRfcommClient = null;
                    device = null;
                    mCollectOrReplayBtn.setEnabled(true);
                }
                break;
        }
    }

    private void setupBTConnection()
    {
        // 搜索蓝牙设备, 用户选择一个蓝牙设备后会携带MAC地址返回
        Intent serverIntent = new Intent(this,
            DeviceListActivity.class);
        startActivityForResult(serverIntent,
            REQUEST_CONNECT_DEVICE);
        // Initialize the BluetoothRfcommClient to perform bluetooth connections
        mRfcommClient = new BluetoothRfcommClient(this,
            mHandler);
    }

    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;
    // Member object for the RFCOMM services
    private BluetoothRfcommClient mRfcommClient = null;
    // Key names received from the BluetoothRfcommClient Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";
    public static final String CONNECTION_LOST = "connection_lost";
    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    // Above is for Bluetooth
    // Message types sent from the BluetoothRfcommClient Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    public static final int MESSAGE_RECONNECT = 6;

    private static String mConnectedDeviceName;
    // The Handler that gets information back from the BluetoothRfcommClient
    
    private Runnable delayShot = new Runnable() {
		
		@Override
		public void run() {
			needsWaveShot = true;
			startAlarm();
		}
	};
	
	// added by zhf 20151005
	private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable countRunnable = new Runnable() {
		
		@Override
		public void run() {
			seconds++;
			tvTime.setText(formatTime(seconds));
			if (minute != 0) // 不限时长模式
			{
				if (seconds >= minute * 60)
				{
					// 停止采集
					if (isCollectOn)
					{
						handleCollectBtnClick();
					}
				}
			}
			handler.postDelayed(this, 1000);
		}
	};
	
	private String formatTime(int seconds)
    {
    	if (seconds < 60)
    	{
    		return "00:00:" + (seconds < 10? "0" + seconds : seconds);
    	}
    	else if (seconds < 60 * 60)
    	{
    		int minute = seconds / 60;
    		int remainSeconds = seconds % 60;
    		return "00:" + (minute < 10? "0" + minute : minute) + ":" + (remainSeconds < 10? "0" + remainSeconds : remainSeconds);
    	}
    	else
    	{
    		int hour = seconds / (60 * 60);
    		int remainSeconds = seconds % (60 * 60);
    		if (remainSeconds < 60)
        	{
        		return (hour < 10? "0" + hour : hour) + ":00:" + (remainSeconds < 10? "0" + remainSeconds : remainSeconds);
        	}
        	else
        	{
        		int minute = remainSeconds / 60;
        		int remainSeconds2 = remainSeconds % 60;
        		return (hour < 10? "0" + hour : hour) + ":" + (minute < 10? "0" + minute : minute) + ":" + (remainSeconds2 < 10? "0" + remainSeconds2 : remainSeconds2);
        	}
    	}
    }
	
	/**
	 * 初始化监听。
	 */
	private InitListener mTtsInitListener = new InitListener() {
		@Override
		public void onInit(int code) {
			Log.d(TAG, "InitListener init() code = " + code);
			if (code != ErrorCode.SUCCESS) {
        		showTip("初始化失败,错误码："+code);
        	} else {
				// 初始化成功，之后可以调用startSpeaking方法
        		// 注：有的开发者在onCreate方法中创建完合成对象之后马上就调用startSpeaking进行合成，
        		// 正确的做法是将onCreate中的startSpeaking调用移至这里
			}		
		}
	};
	
	/**
	 * 合成回调监听。
	 */
	private SynthesizerListener mTtsListener = new SynthesizerListener() {
		
		@Override
		public void onSpeakBegin() {
//			showTip("开始播放");
		}

		@Override
		public void onSpeakPaused() {
//			showTip("暂停播放");
		}

		@Override
		public void onSpeakResumed() {
//			showTip("继续播放");
		}

		@Override
		public void onBufferProgress(int percent, int beginPos, int endPos,
				String info) {
			// 合成进度
			mPercentForBuffering = percent;
//			showTip(String.format(getString(R.string.tts_toast_format),
//					mPercentForBuffering, mPercentForPlaying));
		}

		@Override
		public void onSpeakProgress(int percent, int beginPos, int endPos) {
			// 播放进度
			mPercentForPlaying = percent;
//			showTip(String.format(getString(R.string.tts_toast_format),
//					mPercentForBuffering, mPercentForPlaying));
		}

		@Override
		public void onCompleted(SpeechError error) {
			if (error == null) {
//				showTip("播放完成");
			} else if (error != null) {
				showTip(error.getPlainDescription(true));
			}
		}

		@Override
		public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
			// 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
			// 若使用本地能力，会话id为null
			//	if (SpeechEvent.EVENT_SESSION_ID == eventType) {
			//		String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
			//		Log.d(TAG, "session id =" + sid);
			//	}
		}
	};
	
	private void showTip(final String str) {
		mToast.setText(str);
		mToast.show();
	}
	/**
	 * 参数设置
	 * @param param
	 * @return 
	 */
	private void setParam(){
		// 清空参数
		mTts.setParameter(SpeechConstant.PARAMS, null);
		// 根据合成引擎设置相应参数
		mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
		// 设置在线合成发音人
		mTts.setParameter(SpeechConstant.VOICE_NAME, "xiaoyan");
		//设置合成语速
		mTts.setParameter(SpeechConstant.SPEED, "50");
		//设置合成音调
		mTts.setParameter(SpeechConstant.PITCH, "50");
		//设置合成音量
		mTts.setParameter(SpeechConstant.VOLUME, "50");
		//设置播放器音频流类型
		mTts.setParameter(SpeechConstant.STREAM_TYPE, "3");
		
		// 设置播放合成音频打断音乐播放，默认为true
		mTts.setParameter(SpeechConstant.KEY_REQUEST_FOCUS, "true");
		
		// 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
		// 注：AUDIO_FORMAT参数语记需要更新版本才能生效
		mTts.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
		mTts.setParameter(SpeechConstant.TTS_AUDIO_PATH, Environment.getExternalStorageDirectory()+"/msc/tts.wav");
	}
	
	public void startSpeaking(String text)
	{
		setParam();
		int code = mTts.startSpeaking(text, mTtsListener);
		if (code != ErrorCode.SUCCESS) {
			showTip("语音合成失败,错误码: " + code);	
		}
	}
	
	/**
	 * 停止语音
	 */
	public void stopSpeaking()
	{
		mTts.stopSpeaking();
	}
	
	/**
	 * 分享
	 * @param text
	 * @param imagePath
	 */
	private void showShare(String text, String imagePath, boolean shareImage) {
		 ShareSDK.initSDK(this);
		 OnekeyShare oks = new OnekeyShare();
		 //关闭sso授权
		 oks.disableSSOWhenAuthorize(); 
		 
		 // title标题，印象笔记、邮箱、信息、微信、人人网和QQ空间使用
		 oks.setTitle(getString(R.string.app_name));
		 if (!shareImage) // 分享文字
		 {
			 // text是分享文本，所有平台都需要这个字段
			 oks.setText(text);
		 }
		 else // 分享图片
		 {
			 // imagePath是图片的本地路径，Linked-In以外的平台都支持此参数
			 if (!TextUtils.isEmpty(imagePath) && new File(imagePath).exists() && new File(imagePath).isFile())
			 {
				 oks.setImagePath(imagePath);//确保SDcard下面存在此张图片
			 }
			 else
			 {
				 oks.setText(text);
			 }
		 }
//		 // url仅在微信（包括好友和朋友圈）中使用
//		 oks.setUrl("http://sharesdk.cn");
		 
		// 启动分享GUI
		 oks.show(this);
	 }
	
    // 蓝牙设备、数据传输状态回调
    private final Handler mHandler = new Handler()
    {
        /** 标识此次连接是否是重连*/
        private boolean isReconnect = false;
        @Override
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case MESSAGE_STATE_CHANGE:
                    switch (msg.arg1)
                    {
                        case BluetoothRfcommClient.STATE_CONNECTED:
                            isReconnect = false;
                            showToast(getString(R.string.title_connected_to)
                                + "\n" + mConnectedDeviceName);
                            break;
                        case BluetoothRfcommClient.STATE_CONNECTING:
                            showToast(getString(R.string.title_connecting));
                            break;
                        case BluetoothRfcommClient.STATE_NONE:
                        	removeCallbacks(delayShot);
                        	// 停止截屏
                        	needsWaveShot = false;
                        	stopAlarm();
                            if (!isCollectOn) // 非正常蓝牙连接断开才提示用户
                            {
//                                showToast(getString(R.string.title_not_connected));
                                // 显示对话框, 防止用户不注意
                                showBluetoothNoAccessDialog();
                            }
                            if (isReconnect)
                            {
                                isReconnect = false;
                                mCollectOrReplayBtn.setEnabled(true);
                            }
                            else
                            {
                                isCollectOn = false;
                                mRfcommClient = null;
//                                device = null;
                                mCollectOrReplayBtn.setEnabled(true);
                            }
                            
                            // add by zhf 2015.09.28
                            tvTime.setVisibility(View.GONE);
                            handler.removeCallbacks(countRunnable);
                            break;
                    }
                    break;
                case MESSAGE_DEVICE_NAME://蓝牙连接建立成功
                	if (selectModeIndex == 0)
                    {
                        hasSendSMS = false;
                    }
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    showToast(getString(R.string.title_connected_to) + mConnectedDeviceName);
                    
                    // 状态设为正在采集数据
                    isCollectOn = true;
                    isReconnect = false;
                    collectStart = System.currentTimeMillis();
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(collectStart);
                    final String recordTime = calendar.get(Calendar.YEAR) + "-"
                        + (calendar.get(Calendar.MONTH) + 1) + "-"
                        + calendar.get(Calendar.DAY_OF_MONTH) + " "
                        + calendar.get(Calendar.HOUR) + "."
                        + calendar.get(Calendar.MINUTE) + "."
                        + calendar.get(Calendar.SECOND);
                    EcgSaveData.init(recordTime);
                    mStatusTxt.setText(R.string.stop_btn_txt);
                    mCollectOrReplayBtn.setEnabled(true);
                    mCollectOrReplayBtn
                        .setImageResource(R.drawable.ic_stop);
                    mStatusTxt.setTextColor(getResources()
                        .getColor(R.color.record_textStart));
                    // 启动数据分析线程
                    isFirstSeg = true; //added by Huo
                    EcgWaveData.resumeThread();
                    
                    /**
                     * 延迟半分钟截屏
                     */
                    postDelayed(delayShot, 1000 * 30);
                    
                    
                    // 测量时长add by zhf 2015.09.28
                    switch (selectTimeInterval) {
					case 0: // 2分钟
						minute = 2;
						break;
					case 1: // 5分钟
						minute = 5;
						break;
					case 2: // 不限时
						minute = 0;
						break;
					default:
						minute = 0;
						break;
					}
                    
                	seconds = 0;
                	tvTime.setVisibility(View.VISIBLE);
                	tvTime.setText("00:00:00");
                	handler.postDelayed(countRunnable, 1000);
                    break;
                case MESSAGE_RECONNECT:
                    showToast(getString(R.string.bt_device_lost));
                    
                    // 延迟5秒尝试重连
                    postDelayed(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            isReconnect = true;
                            if (mRfcommClient == null)
                            {
                                mRfcommClient = new BluetoothRfcommClient(WaveScreen.this,
                                    mHandler);
                            }
                            mRfcommClient.connect(device);
                        }
                    }, 5000);
                    break;
            }
        }
    };
    /******************************* End Bluetooth configuration ********/
}
