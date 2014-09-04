package com.bde.light.activity;

import org.apache.http.util.ByteArrayBuffer;

import android.app.Activity;
import android.app.AlertDialog;
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
import android.widget.SeekBar;
import android.widget.TextView;

import com.bde.light.mgr.LightMgr;
import com.bde.light.model.Light;
import com.bde.light.service.BleService;
import com.bde.light.utils.NumConversion;

public class AdjustmentActivity extends Activity implements View.OnClickListener {

	private Light myLight;
	
	private LightMgr lightMgr;
	
	//private EditText et_name;
	
	private BleService mService;
	private ServiceConnection onService;
	private BluetoothAdapter mBluetoothAdapter;
	Context context;
	private SeekBar mAdjustment, mRange;
	private Button open, close, endAdjustment, changeRange;
	private TextView adjustment_tv, rang_tv;
	//ProgressDialog progressDialog;
	boolean updateOperationDone = false;
	private int operationTime = 290, rangTime = 29000;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.adjustment_layout);
		//progressDialog = new ProgressDialog(this);
		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			myLight = (Light) bundle.getSerializable(Light.LIGHT);
		}
		
		context = this;
		onService = new ServiceConnection() {
            public void onServiceConnected(ComponentName className, IBinder rawBinder) {
                mService = ((BleService.LocalBinder) rawBinder).getService();
                if (mService != null) {
                	//mService.scan(true);
                    mService.setActivityHandler(mHandler);
                    if (myLight != null) {
                    	mService.setMyLight(myLight);
                    	mService.setOperation(BleService.WINDOW);
                    	mService.connect(myLight.address);
                    }
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
        
        
		
		//et_init_password = (EditText) findViewById(R.id.init_password);
		Button bt_back = (Button) findViewById(R.id.bt_back);
		TextView top_title = (TextView) findViewById(R.id.top_title);
		
		bt_back.setText(R.string.device_management);
		top_title.setText(R.string.adjustmentAndRange);
		
		bt_back.setOnClickListener(AdjustmentActivity.this);
		
		mAdjustment = (SeekBar) findViewById(R.id.adjustment);
		mRange = (SeekBar) findViewById(R.id.range);
		open = (Button) findViewById(R.id.open);
		close = (Button) findViewById(R.id.close);
		endAdjustment = (Button) findViewById(R.id.endAdjustment);
		changeRange = (Button) findViewById(R.id.rangBtn);
		adjustment_tv = (TextView) findViewById(R.id.adjustmentNum);
		rang_tv = (TextView) findViewById(R.id.rangNum);
		
		mAdjustment.setProgress(operationTime);
		adjustment_tv.setText(operationTime + 10 + "");
		mAdjustment.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				operationTime = seekBar.getProgress()/* + 10*/;
				
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				// TODO Auto-generated method stub
				operationTime = progress;
				adjustment_tv.setText(operationTime + 10 + "");
			}
		});
		mRange.setProgress(rangTime);
		rang_tv.setText(rangTime + 1000 + "");
		mRange.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				rangTime = seekBar.getProgress();
				
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				// TODO Auto-generated method stub
				rangTime = progress;
				rang_tv.setText(rangTime + 1000 + "");
			}
		});
		
		open.setOnClickListener(this);
		close.setOnClickListener(this);
		endAdjustment.setOnClickListener(this);
		changeRange.setOnClickListener(this);
		
		mAdjustment.setEnabled(false);
		mRange.setEnabled(false);
		open.setEnabled(false);
		close.setEnabled(false);
		endAdjustment.setEnabled(false);
		changeRange.setEnabled(false);
	}
	
	@Override
    protected void onDestroy() {
        super.onDestroy();
        if (successDialog != null)
        	successDialog.dismiss();
        if (failedDialog != null)
        	failedDialog.dismiss();
        mService.disconnect();
        unbindService(onService);
        finish();
    }
	
	/*public void confirm(View v) {
		String init_password = et_init_password.getText().toString().trim();
		if (init_password.equals("5335608202")) {
			progressDialog.setMessage(getString(R.string.pleaseWait));
			progressDialog.setTitle(getString(R.string.operating));
			progressDialog.show();
			new Thread(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					try {
						Thread.sleep(4000);
						if (!isOperationDone) {
							Message msg = mHandler.obtainMessage(BleService.INIT_PASSWORD);
							Bundle data = new Bundle();
							data.putString(BleService.RESULT, BleService.FAIL);
							msg.setData(data);
							msg.sendToTarget();
						}
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
			});
			mService.setMyLight(myLight);
			mService.setOperation(BleService.INIT_PASSWORD);
			mService.connect(mBluetoothAdapter.getRemoteDevice(myLight.address), false);
		}
		else {
			showMyFailDialog(R.string.initPwdFail);
		}
	}*/
	
	
	private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case BleService.WINDOW_CONNECT:
            	mAdjustment.setEnabled(true);
            	mRange.setEnabled(true);
        		open.setEnabled(true);
        		close.setEnabled(true);
        		endAdjustment.setEnabled(true);
        		changeRange.setEnabled(true);
                break;
            case BleService.WINDOW:
            	finish();
            	break;
            case BleService.WINDOW_RESULT:
            	byte []buffer = msg.getData().getByteArray(BleService.RESULT);
            	/*switch (buffer[0]) {
            	case 0x24:
            		
            		break;
            	case 0x25:
            		break;
            	}*/
            	if (buffer[1] == 1) {
            		showMySuccessDialog(R.string.success);
            	} else {
            		showMyFailDialog(R.string.failed);
            	}
            	break;
            }
        }
	};
	
	/**
	 * 显示操作成功的dialog
	 * @param message
	 */
	AlertDialog successDialog, failedDialog;
	public void showMySuccessDialog(int message) {
		if (successDialog != null) {
			successDialog.show();
			return;
		}
		successDialog = new AlertDialog.Builder(AdjustmentActivity.this)
    	.setMessage(message)
    	.setPositiveButton(R.string.confirm, new AlertDialog.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				/*Intent intent = new Intent();
				intent.putExtra(Light.LIGHT, myLight);
				setResult(DeviceManagerActivity.CHANGE_PASSWORD_RESULT,intent);
				AdjustmentActivity.this.finish();*/
			}
		}).create();
		successDialog.show();
	}
	
	/**
	 * 显示操作失败的dialog
	 * @param message
	 */
	public void showMyFailDialog(int message) {
		if (failedDialog != null) {
			failedDialog.show();
			return;
		}
		failedDialog = new AlertDialog.Builder(AdjustmentActivity.this)
    	.setMessage(message)
    	.setNegativeButton(R.string.confirm, null)
		.create();
		failedDialog.show();
	}
	@Override
	public void onClick(View v) {
		int command = 0x22;
		byte []data = null;
		ByteArrayBuffer bab = new ByteArrayBuffer(3);
		switch (v.getId()) {
			case R.id.bt_back:
				finish();
				//break;
				return;
			case R.id.open:
				data = NumConversion.int2LittleEndianByteArray16(operationTime + 10);
				break;
			case R.id.close:
				command = 0x23;
				/*ByteArrayBuffer bab = new ByteArrayBuffer(3);
				bab.append(command);*/
				/*byte []*/data = NumConversion.int2LittleEndianByteArray16(operationTime + 10);
				/*bab.append(data, 0, data.length);
				if (mService != null) {
					mService.write(bab.buffer(), BleService.MY_TIMER_CHARACTERISTIC);
				}*/
				break;
			case R.id.endAdjustment:
				command = 0x24;
				/*if (mService != null) {
					mService.write(new byte[]{0x24}, BleService.MY_TIMER_CHARACTERISTIC);
				}*/
				break;
			case R.id.rangBtn:
				command = 0x25;
				data = NumConversion.int2LittleEndianByteArray16(rangTime + 1000);
				bab.append(command);
				bab.append(data, 0, data.length);
				if (mService != null) {
					mService.write(bab.buffer(), BleService.MY_TIMER_CHARACTERISTIC);
				}
				break;
		}
		bab.append(command);
		if (data != null && data.length > 0) {
			bab.append(data, 0, data.length);
		}
		if (mService != null) {
			mService.write(bab.buffer(), BleService.MY_TIMER_CHARACTERISTIC);
		}
	}
}
