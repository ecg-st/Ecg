package com.nju.ecg.wave;

import java.io.IOException;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
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
	private ProgressDialog sendDialog;
	
	private boolean autoSend;
	private String filePath;
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
    		activeBtn.setText(R.string.cancel_active);
    	}
    	else
    	{
    		activeBtn.setText(R.string.start_active);
    	}
    	progressDialog = new ProgressDialog(this);
    	progressDialog.setCancelable(false);
    	progressDialog.setCanceledOnTouchOutside(false);
    	
    	sendDialog = new ProgressDialog(this);
    	sendDialog.setCancelable(false);
    	sendDialog.setCanceledOnTouchOutside(false);
    	
    	autoSend = getIntent().getBooleanExtra("autoSend", false);
    	if (autoSend)
    	{
    		filePath = getIntent().getStringExtra("filePath");
    		if (!StringUtil.isNullOrEmpty(userName) && !StringUtil.isNullOrEmpty(password))
        	{
    			sendData(filePath);
        	}
    		else
    		{
    			showToast(getString(R.string.login_please));
    		}
    	}
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
				showToast(getString(R.string.name_pass_empty));
				return;
			}
			if (autoSend)
			{
				userName = userEdt.getText().toString();
				password = passwordEdt.getText().toString();
				sendData(filePath);
			}
			else
			{
				if (!progressDialog.isShowing())
				{
					userName = userEdt.getText().toString();
					password = passwordEdt.getText().toString();
					progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
					progressDialog.setMessage(getString(R.string.logining_wait));
					progressDialog.show();
					new Thread(new Runnable() {
						
						@Override
						public void run() {
							SocketClient sClient = new SocketClient(userName, password, null);
							final int result = sClient.login(null, false);
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
										showToast(getString(R.string.login_success));
									}
									else if (result == 0)
									{
										showToast(getString(R.string.account_pass_error));
									}
									else if (result == -1)
									{
										showToast(getString(R.string.power_disbale));
									}
									else if (result == -2)
									{
										showToast(getString(R.string.no_sufficent_funds));
									}
									else if (result == -100)
									{
										showToast(getString(R.string.server_connect_failure));
									}
									else
									{
										showToast(getString(R.string.login_faliure));
									}
								}
							});
						}
					}).start();
				}
				else
				{
					showToast(getString(R.string.logining_wait));
				}
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
	    		activeBtn.setText(R.string.cancel_active);
	    	}
	    	else
	    	{
	    		activeBtn.setText(R.string.start_active);
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
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == 100 && resultCode == RESULT_OK)
        {
            final String filePath = data.getStringExtra("FilePath");
            sendData(filePath);
        }
    }
    
    SocketClient sClient = null;
    private void sendData(final String filePath)
    {
    	new AlertDialog.Builder(this)
    	.setTitle(getString(R.string.send_mode))
    	.setItems(new String[]{getString(R.string.real_time_display), getString(R.string.time_display)}, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, final int which) {
				dialog.dismiss();
				LogUtil.d(TAG, filePath);
		        sendDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		        sendDialog.setMessage(getString(R.string.data_uploading));
		        sendDialog.setProgress(0);
		        sendDialog.setMax(100);
		        sendDialog.show();
		        
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
		                            	sendDialog.dismiss();
		                                showToast(getString(R.string.send_success));
		                                if (autoSend)
		                                {
		                                	finish();
		                                }
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
		                            	sendDialog.setProgress(progress); 
		                            }
		                        });
		                    }
		                });
		                String fileName = filePath.substring(filePath.lastIndexOf("/") + 1, filePath.length());
		                if (filePath.contains("/EcgApp/DataDir/LimbLead/"))
		                {
		                	fileName = "ta_" + fileName;
		                }
		                else if (filePath.contains("/EcgApp/DataDir/MockLimbLead/"))
		                {
		                	fileName = "tb_" + fileName;
		                }
		                else if (filePath.contains("/EcgApp/DataDir/MockChestLead/"))
		                {
		                	fileName = "tc_" + fileName;
		                }
		                else if (filePath.contains("/EcgApp/DataDir/SimpleLimbLead/"))
		                {
		                	fileName = "td_" + fileName;
		                }
		                final int result = sClient.login(fileName, which == 0? true : false);
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
		                            	sendDialog.dismiss();
		                                showToast(getString(R.string.send_failure));
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
		                        	sendDialog.dismiss();
		                            editor.remove("UserName");
		                            editor.remove("Password");
		                            editor.commit();
		                            loginLay.setVisibility(View.VISIBLE);
		                            loginedLay.setVisibility(View.GONE);
		                            if (result == 0)
									{
										showToast(getString(R.string.account_pass_error));
									}
									else if (result == -1)
									{
										showToast(getString(R.string.power_disbale));
									}
									else if (result == -2)
									{
										showToast(getString(R.string.no_sufficent_funds));
									}
									else if (result == -100)
									{
										showToast(getString(R.string.server_connect_failure));
									}
		                            else
		                            {
		                            	showToast(getString(R.string.login_faliure));
		                            }
		                        }
		                    });
		                }
		            }
		        }).start();
			}
		}).create().show();
    }
}
