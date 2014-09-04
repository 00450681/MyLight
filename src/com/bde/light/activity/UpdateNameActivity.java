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

public class UpdateNameActivity extends Activity implements android.view.View.OnClickListener {
	
	private Light myLight;
	
	private LightMgr lightMgr;
	
	private EditText et_name;
	
	private BleService mService;
	private ServiceConnection onService;
	private BluetoothAdapter mBluetoothAdapter;
	Context context;
	ProgressDialog progressDialog;
	boolean updateOperationDone = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.update_name_activity);
		
		if (mBluetoothAdapter == null) {
			mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
			if (mBluetoothAdapter == null) {
				return;
			}
		}
		context = this;
		progressDialog = new ProgressDialog(this);
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
		
		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			myLight = (Light) bundle.getSerializable(Light.LIGHT);
		}
		
		Button bt_back = (Button) findViewById(R.id.bt_back);
		TextView tv_top_title = (TextView) findViewById(R.id.top_title);
		et_name =  (EditText) findViewById(R.id.update_name_text);
		
		bt_back.setText(R.string.setting);
		bt_back.setOnClickListener(this);
		tv_top_title.setText(R.string.name);
		et_name.setText(myLight.name);
		
	}
	
	@Override
    public void onStart() {
        super.onStart();
        lightMgr = new LightMgr(this);
    }

    @Override
    public void onStop() {
        super.onStop();
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
		progressDialog(R.string.pleaseWait);
		String name = et_name.getText().toString();
		myLight.name = name;
		mService.setMyLight(myLight);
		mService.setOperation(BleService.UPDATE_NAME);
		mService.connect(mBluetoothAdapter.getRemoteDevice(myLight.address), false);
		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					Thread.sleep(4000);
					if (!updateOperationDone) {
						Message msg = mHandler.obtainMessage(BleService.UPDATE_NAME);
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
	
	private void progressDialog(int msg) {
		progressDialog.setMessage(getString(msg));
		progressDialog.setTitle(getString(R.string.operating));
		progressDialog.show();
	}
	private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
        	Bundle data = msg.getData();
        	int result = data.getInt(BleService.RESULT);
            switch (msg.what) {
            case BleService.UPDATE_NAME:
            	progressDialog.cancel();
            	updateOperationDone = true;
                if (result == 0) {
                	lightMgr.update(myLight);
                	showMyDialog(R.string.update_success);
                } else {
                	showMyDialog(R.string.update_failed);
                }
                break;
            }
        }
	};
	
	/**
	 * 显示dialog
	 * @param message
	 */
	public void showMyDialog(int message) {
		new AlertDialog.Builder(UpdateNameActivity.this)
    	.setTitle(message)
    	.setPositiveButton(R.string.confirm, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				finish();
			}
		}).show();
	}

	@Override
	public void onClick(View v) {
		finish();
	}

}
