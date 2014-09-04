package com.bde.light.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
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

public class InitPasswordActivity extends Activity implements android.view.View.OnClickListener {
	
	private EditText et_init_password;
	
	private BleService mService;
	private ServiceConnection onService;
	private BluetoothAdapter mBluetoothAdapter;
	private Context context;
	private Light myLight;
	ProgressDialog progressDialog;
	boolean isOperationDone = true;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.init_password_activity);
		progressDialog = new ProgressDialog(this);
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
		
		et_init_password = (EditText) findViewById(R.id.init_password);
		Button bt_back = (Button) findViewById(R.id.bt_back);
		TextView top_title = (TextView) findViewById(R.id.top_title);
		
		bt_back.setText(R.string.device_management);
		top_title.setText(R.string.init_password);
		
		bt_back.setOnClickListener(InitPasswordActivity.this);
		
		
	}
	
	@Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(onService);
        finish();
    }
	
	public void confirm(View v) {
		String init_password = et_init_password.getText().toString().trim();
		if (init_password.equals("5335608202")) {
			progressDialog.setMessage(getString(R.string.pleaseWait));
			progressDialog.setTitle(getString(R.string.operating));
			progressDialog.show();
			isOperationDone = false;
			new Thread(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					try {
						Thread.sleep(4000);
						if (!isOperationDone) {
							Message msg = mHandler.obtainMessage(BleService.INIT_PASSWORD);
							/*Bundle data = new Bundle();
							data.putString(BleService.RESULT, BleService.FAIL);
							msg.setData(data);*/
							msg.sendToTarget();
						}
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
			}).start();
			mService.setMyLight(myLight);
			mService.setOperation(BleService.INIT_PASSWORD);
			mService.connect(mBluetoothAdapter.getRemoteDevice(myLight.address), false);
		}
		else {
			showMyFailDialog(R.string.initPwdFail);
		}
	}
	
	
	private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
        	Bundle data = msg.getData();
        	System.out.println("data===: " + data);
            switch (msg.what) {
            case BleService.INIT_PASSWORD:
            	progressDialog.cancel();
            	isOperationDone = true;
            	if (data != null) {
            		String result = data.getString(BleService.RESULT);
            		if (result != null) {
            			if (result.equals(BleService.SUCCESS)) {
                			LightMgr mgr = new LightMgr(context);
                			myLight.version = 0;
                			if (mgr.update(myLight) == 1) {
                				
                			}
                			showMySuccessDialog(R.string.init_success);
                		} else 
                		{
                			showMyFailDialog(R.string.init_failed);
                		}
            		}
            		else {
                		showMyFailDialog(R.string.timeout);
                	}
            	} 
                break;
            }
        }
	};
	
	/**
	 * 显示操作成功的dialog
	 * @param message
	 */
	public void showMySuccessDialog(int message) {
		new AlertDialog.Builder(InitPasswordActivity.this)
    	.setMessage(message)
    	.setPositiveButton(R.string.confirm, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Intent intent = new Intent();
				intent.putExtra(Light.LIGHT, myLight);
				setResult(DeviceManagerActivity.CHANGE_PASSWORD_RESULT,intent);
				InitPasswordActivity.this.finish();
			}
		}).show();
	}
	
	/**
	 * 显示操作失败的dialog
	 * @param message
	 */
	public void showMyFailDialog(int message) {
		new AlertDialog.Builder(InitPasswordActivity.this)
    	.setMessage(message)
    	.setNegativeButton(R.string.confirm, null)
		.show();
	}
	
	public void onClick(View v) {
		finish();
	}
	
}
