package com.nju.ecg.wave;

import java.io.IOException;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.nju.ecg.R;
import com.nju.ecg.basic.BasicActivity;
import com.nju.ecg.socket.SocketClient;
import com.nju.ecg.socket.SocketClient.ProgressListener;
import com.nju.ecg.utils.LogUtil;
import com.nju.ecg.utils.StringUtil;
/**
 * 数据发送
 * @author haifeng
 *
 */
public class DataTransferActivity extends BasicActivity implements OnClickListener
{
	private final String TAG = "DataTransferActivity";
	private LinearLayout loginLay;
	private EditText userEdt;
	private EditText passwordEdt;
	private Button loginBtn;
	private LinearLayout loginedLay;
	private Button logoutBtn;
	private Button activeBtn;
	private Button sendBtn;
	private SharedPreferences sp;
	private SharedPreferences.Editor editor;
	private String userName;
	private String password;
	private boolean realTimeActived;
	private ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.activity_data_transfer);
    	initViews();
    	initValues();
    	registerListeners();
    }
    
    private void initViews()
    {
    	loginLay = (LinearLayout)findViewById(R.id.login_lay);
    	userEdt = (EditText)findViewById(R.id.user_edt);
    	passwordEdt = (EditText)findViewById(R.id.password_edt);
    	loginBtn = (Button)findViewById(R.id.login_btn);
    	loginedLay = (LinearLayout)findViewById(R.id.logined_lay);
    	logoutBtn = (Button)findViewById(R.id.logout_btn);
    	activeBtn = (Button)findViewById(R.id.active_btn);
    	sendBtn = (Button)findViewById(R.id.send_btn);
    }
    
    private void initValues()
    {
    	sp = getSharedPreferences(TAG, MODE_PRIVATE);
    	editor = sp.edit();
    	userName = sp.getString("UserName", "");
    	password = sp.getString("Password", "");
    	if (StringUtil.isNullOrEmpty(userName) || StringUtil.isNullOrEmpty(password))
    	{
    		loginLay.setVisibility(View.VISIBLE);
    		loginedLay.setVisibility(View.GONE);
    	}
    	else
    	{
    		loginLay.setVisibility(View.GONE);
    		loginedLay.setVisibility(View.VISIBLE);
    	}
    	realTimeActived = sp.getBoolean("RealTimeActived", false);
    	if (realTimeActived)
    	{
    		activeBtn.setText("取消实时");
    	}
    	else
    	{
    		activeBtn.setText("激活实时");
    	}
    	progressDialog = new ProgressDialog(this);
    	progressDialog.setCancelable(false);
    	progressDialog.setCanceledOnTouchOutside(false);
    }
    
    private void registerListeners()
    {
    	loginBtn.setOnClickListener(this);
    	logoutBtn.setOnClickListener(this);
    	activeBtn.setOnClickListener(this);
    	sendBtn.setOnClickListener(this);
    }
    
    @Override
    public void onClick(View v) 
    {
    	switch (v.getId()) {
		case R.id.login_btn:
			if (StringUtil.isNullOrEmpty(userEdt.getText().toString()) || StringUtil.isNullOrEmpty(passwordEdt.getText().toString()))
			{
				showToast("用户名或密码不能为空");
				return;
			}
			if (!progressDialog.isShowing())
			{
				userName = userEdt.getText().toString();
				password = passwordEdt.getText().toString();
				progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
				progressDialog.setMessage("正在登录, 请稍候...");
				progressDialog.show();
				new Thread(new Runnable() {
					
					@Override
					public void run() {
						SocketClient sClient = new SocketClient(userName, password, null);
						final int result = sClient.login(null);
						sClient.close();
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								progressDialog.dismiss();
								if (result > 0)
								{
									editor.putString("UserName", userName);
									editor.putString("Password", password);
									editor.commit();
									loginLay.setVisibility(View.GONE);
						    		loginedLay.setVisibility(View.VISIBLE);
									showToast("登录成功");
								}
								else if (result == 0)
								{
									showToast("用户名密码错误或该用户不存在");
								}
								else if (result == -1)
								{
									showToast("用户登录权限不足");
								}
								else if (result == -2)
								{
									showToast("用户欠费");
								}
								else if (result == -100)
								{
									showToast("服务器连接失败");
								}
								else
								{
									showToast("登录失败");
								}
							}
						});
					}
				}).start();
			}
			else
			{
				showToast("正在登录, 请稍候...");
			}
			break;
		case R.id.logout_btn:
			editor.remove("UserName");
			editor.remove("Password");
			editor.commit();
			loginLay.setVisibility(View.VISIBLE);
    		loginedLay.setVisibility(View.GONE);
			break;
		case R.id.active_btn:
			realTimeActived = !realTimeActived;
			if (realTimeActived)
	    	{
	    		activeBtn.setText("取消实时");
	    	}
	    	else
	    	{
	    		activeBtn.setText("激活实时");
	    	}
			editor.putBoolean("RealTimeActived", realTimeActived);
			editor.commit();
			break;
		case R.id.send_btn:
			startActivityForResult(new Intent(this, FileListActivity.class), 100);
			break;
		default:
			break;
		}
    }
    
    SocketClient sClient = null;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == 100 && resultCode == RESULT_OK)
        {
            final String filePath = data.getStringExtra("FilePath");
            LogUtil.d(TAG, filePath);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setMessage("数据上传中...");
            progressDialog.setProgress(0);
            progressDialog.setMax(100);
            progressDialog.show();
            
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    sClient = new SocketClient(userName, password, new ProgressListener()
                    {
                        @Override
                        public void overSend()
                        {
                            sClient.close();
                            runOnUiThread(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    progressDialog.dismiss();
                                    showToast("发送成功");
                                }
                            });
                        }
                        
                        @Override
                        public void onSend(final int progress)
                        {
                            runOnUiThread(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    progressDialog.setProgress(progress); 
                                }
                            });
                        }
                    });
                    String fileName = filePath.substring(filePath.lastIndexOf("/") + 1, filePath.length());
                    final int result = sClient.login(fileName);
                    if (result > 0)
                    {
                        try
                        {
                            sClient.send(filePath);
                        }
                        catch (IOException e)
                        {
                            e.printStackTrace();
                            sClient.close();
                            runOnUiThread(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    progressDialog.dismiss();
                                    showToast("发送失败");
                                }
                            });
                        }
                    }
                    else
                    {
                        sClient.close();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressDialog.dismiss();
                                editor.remove("UserName");
                                editor.remove("Password");
                                editor.commit();
                                loginLay.setVisibility(View.VISIBLE);
                                loginedLay.setVisibility(View.GONE);
                                if (result == 0)
                                {
                                    showToast("用户名密码错误或该用户不存在");
                                }
                                else if (result == -1)
                                {
                                    showToast("用户登录权限不足");
                                }
                                else if (result == -2)
                                {
                                    showToast("用户欠费");
                                }
                                else if (result == -100)
                                {
                                    showToast("服务器连接失败");
                                }
                                else
                                {
                                    showToast("登录失败");
                                }
                            }
                        });
                    }
                }
            }).start();
        }
    }
}
