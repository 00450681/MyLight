package com.bde.light.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.bde.light.mgr.LightMgr;
import com.bde.light.model.Light;
import com.bde.light.service.BleService;

public class UpdatePasswordActivity extends Activity implements android.view.View.OnClickListener {
	
	private Light myLight;
	private EditText et_old_password;
	private EditText et_new_password;
	private EditText et_repeat_password;
	
	private BleService mService;
	private ServiceConnection onService;
	private BluetoothAdapter mBluetoothAdapter;
	private Context context;
	ProgressDialog progressDialog;
	boolean operationDone = false;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.update_password_activity);
		context = this;
		onService = new ServiceConnection() {
            public void onServiceConnected(ComponentName className, IBinder rawBinder) {
                mService = ((BleService.LocalBinder) rawBinder).getService();
                if (mService != null) {
                	//mService.scan(true);
                    mService.setActivityHandler(mHandler);
                }
            }
            
            public void onServiceDisconnected(ComponentName classname) {
                mService = null;
            }
        };
        progressDialog = new ProgressDialog(this);
        startService(new Intent(this, BleService.class));
        Intent bindIntent = new Intent(this, BleService.class);
        bindService(bindIntent, onService, Context.BIND_AUTO_CREATE);
		
        if (mBluetoothAdapter == null) {
			mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
			if (mBluetoothAdapter == null) {
				return;
			}
        }
        
		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			myLight = (Light) bundle.getSerializable(Light.LIGHT);
		}
		
		Button bt_back = (Button) findViewById(R.id.bt_back);
		TextView top_title = (TextView) findViewById(R.id.top_title);
		et_old_password = (EditText) findViewById(R.id.old_password);
		et_new_password = (EditText) findViewById(R.id.new_password);
		et_repeat_password = (EditText) findViewById(R.id.repeat_password);
		
		bt_back.setText(R.string.device_management);
		top_title.setText(R.string.update_password);
		
		bt_back.setOnClickListener(UpdatePasswordActivity.this);
	}
	
	@Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(onService);
        finish();
    }
	
	/**
	 * 确定按钮
	 * @param v
	 */
	public void confirm(View v) {
		showProgressDialog(getString(R.string.pleaseWait));
		String old_password = et_old_password.getText().toString().trim();
		String new_password = et_new_password.getText().toString().trim();
		String repeat_password = et_repeat_password.getText().toString().trim();
		
		if (old_password != null && old_password.equals("123456")) {
			if (new_password != null && new_password.length() == 6
					&& repeat_password != null && repeat_password.length() == 6
					&& new_password.equals(repeat_password)) {
				
				myLight.password = new_password;
				mService.setMyLight(myLight);
				mService.setOperation(BleService.UPDATE_PASSWORD);
				mService.connect(mBluetoothAdapter.getRemoteDevice(myLight.address), false);
				
			}
			else {
				showMyDialog(R.string.invalidate_pwd);
				progressDialog.cancel();
				operationDone = true;
			}
		}
		else {
			showMyDialog(R.string.invalidate_oldpwd);
			progressDialog.cancel();
			operationDone = true;
		}
		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					Thread.sleep(4000);
					if (!operationDone) {
						Message msg = mHandler.obtainMessage(BleService.UPDATE_PASSWORD);
						Bundle data = new Bundle();
						data.putInt(BleService.RESULT, -1);
						msg.setData(data);
						msg.sendToTarget();
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		}).start();
	}
	
	private void showProgressDialog(String msg) {
		progressDialog.setMessage(msg);
		progressDialog.setTitle(getString(R.string.operating));
		progressDialog.show();
	}
	private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
        	Bundle data = msg.getData();
            switch (msg.what) {
            case BleService.UPDATE_PASSWORD:
            	progressDialog.cancel();
            	operationDone = true;
            	if (data != null) {
            		int result = data.getInt(BleService.RESULT);
            		if (result == 0) {
            			myLight.version = 1;
            			LightMgr lightMgr = new LightMgr(context);
            			if (lightMgr.update(myLight) == 1) {
            				showMySuccessDialog(R.string.update_success);
            			}
            			else {
            				showMyDialog(R.string.dbFail);
            			}
            		} 
            		else if (result == -1) {
            			showMyDialog(R.string.timeout);
            		}
            		else {
            			showMyDialog(R.string.update_failed);
            		}
            	}
                break;
            }
        }
	};
	
	public void showMySuccessDialog(int message) {
		new AlertDialog.Builder(UpdatePasswordActivity.this)
		.setTitle(message)
		.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Intent intent = new Intent();
				intent.putExtra(Light.LIGHT, myLight);
				setResult(DeviceManagerActivity.CHANGE_PASSWORD_RESULT,intent);
				UpdatePasswordActivity.this.finish();
			}
		}).show();
	}
	
	public void showMyDialog(int message) {
		new AlertDialog.Builder(UpdatePasswordActivity.this)
    	.setTitle(message)
    	.setNegativeButton(R.string.confirm, null)
		.show();
	}

	/**
	 * 返回
	 */
	public void onClick(View v) {
		finish();
	}
	
	void flashData(){
		Intent intent = new Intent(this,DeviceManagerActivity.class);
		intent.putExtra(Light.LIGHT, myLight);
		startActivity(intent);
		finish();
	}

}
