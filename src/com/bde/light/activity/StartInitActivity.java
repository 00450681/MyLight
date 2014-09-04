package com.bde.light.activity;

import org.apache.http.util.ByteArrayBuffer;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import com.bde.light.activity.MainActivity.WindowControlThread;
import com.bde.light.model.Light;
import com.bde.light.service.BleService;
import com.bde.light.utils.NumConversion;

public class StartInitActivity extends Activity {

	private BleService mService;
	private ServiceConnection onService;
	private Light myLight;
	private Button upBtn, downBtn, confirmBtn;
	private boolean isConfirm = false, isEnterLongClick = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.start_init_fragment_layout);
		
		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			myLight = (Light) bundle.getSerializable(Light.LIGHT);
		}
		
		Button bt_back = (Button) findViewById(R.id.bt_back);
		bt_back.setText(R.string.setting);
		bt_back.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		
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
        
        /*upBtn = (Button) findViewById(R.id.up);
        upBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				byte []data = NumConversion.int2LittleEndianByteArray16(300);
				ByteArrayBuffer bab = new ByteArrayBuffer(data.length + 1);
				
				bab.append(0x22);
				bab.append(data, 0, data.length);
				if (mService != null) {
					mService.write(bab.buffer(), BleService.MY_TIMER_CHARACTERISTIC);
				}
			}
		});
		downBtn = (Button) findViewById(R.id.down);
		downBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				byte []data = NumConversion.int2LittleEndianByteArray16(300);
				ByteArrayBuffer bab = new ByteArrayBuffer(data.length + 1);
				
				bab.append(0x23);
				bab.append(data, 0, data.length);
				if (mService != null) {
					mService.write(bab.buffer(), BleService.MY_TIMER_CHARACTERISTIC);
				}
			}
		});*/
        upBtn = (Button) findViewById(R.id.up);
        upBtn.setOnLongClickListener(new View.OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View v) {
				// TODO Auto-generated method stub
				System.out.println("long press");
				byte []data = getOpenOrCloseData(true);
				mWindowControlThread = new WindowControlThread(data);
				mWindowControlThread.start();
				downBtn.setEnabled(false);
				isEnterLongClick = true;
				return true;
			}
		});
        upBtn.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				System.out.println("ontouch + action " + event.getAction());
				if (event.getAction() == MotionEvent.ACTION_UP/* && (System.currentTimeMillis() - time) > 300*/ && isEnterLongClick) {
					v.clearFocus();
					v.setPressed(false);
					if (mWindowControlThread != null) {
						mWindowControlThread.setRunning(false);
					}
					downBtn.setEnabled(true);
					isEnterLongClick = false;
					return true;
				}
				return false;
			}
		});
        upBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				downBtn.setEnabled(false);
				System.out.println("窗帘开");
				//System.out.println(mService.write(new byte[]{0x20}, BleService.MY_TIMER_CHARACTERISTIC));
				openOrClose(true);
				
				downBtn.setEnabled(true);
			}
		});
		
        downBtn = (Button) findViewById(R.id.down);
        downBtn.setOnLongClickListener(new View.OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View v) {
				// TODO Auto-generated method stub
				System.out.println("long press");
				byte []data = getOpenOrCloseData(false);
				mWindowControlThread = new WindowControlThread(data);
				mWindowControlThread.start();
				upBtn.setEnabled(false);
				isEnterLongClick = true;
				return true;
			}
		});
        downBtn.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				System.out.println("ontouch + action " + event.getAction());
				if (event.getAction() == MotionEvent.ACTION_UP/* && (System.currentTimeMillis() - time) > 300*/ && isEnterLongClick) {
					// 长按松手
					v.clearFocus();
					v.setPressed(false);
					if (mWindowControlThread != null) {
						mWindowControlThread.setRunning(false);
					}
					upBtn.setEnabled(true);
					isEnterLongClick = false;
					return true;
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					// 短按松手
					//mOpenBtn.setEnabled(true);
					//return true;
				}
				return false;
			}
		});
        downBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				upBtn.setEnabled(false);
				System.out.println("窗帘关");
				//System.out.println(mService.write(new byte[]{0x21}, BleService.MY_TIMER_CHARACTERISTIC));
				openOrClose(false);
				upBtn.setEnabled(true);
			}
		});
		

	confirmBtn = (Button) findViewById(R.id.confirm);
	confirmBtn.setOnClickListener(new View.OnClickListener() {
	
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if (mService != null) {
				mService.write(new byte[]{0x28}, BleService.MY_TIMER_CHARACTERISTIC);
				isConfirm = true;
			}
		}
		});
	
	upBtn.setEnabled(false);
	downBtn.setEnabled(false);
	confirmBtn.setEnabled(false);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		
		mService.disconnect();
        unbindService(onService);
        finish();
	}

	private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case BleService.WINDOW_CONNECT:
            	upBtn.setEnabled(true);
            	downBtn.setEnabled(true);
            	confirmBtn.setEnabled(true);
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
            	break;
            case BleService.WINDOW_FINISH:
            	if (isConfirm) {
            		Intent intent = new Intent();
    				intent.putExtra(Light.LIGHT, myLight);
    				intent.setClass(StartInitActivity.this, EndInitActivity.class);
    				startActivityForResult(intent, 1);
                	//finish();
            	}
            	
            	break;
            }
        }
	};

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		System.out.println("onActivityResult");
		switch (requestCode) {
		case 1:
			finish();
			break;
		}
		
	}
	
	private void openOrClose(boolean isOpen) {
		
		if (mService != null) {
			mService.write(getOpenOrCloseData(isOpen), BleService.MY_TIMER_CHARACTERISTIC);
		}
	}
	private byte []getOpenOrCloseData(boolean isOpen) {
		byte []data = NumConversion.int2LittleEndianByteArray16(300);
		ByteArrayBuffer bab = new ByteArrayBuffer(data.length + 1);
		
		if (isOpen) {
			bab.append(0x22);
		}
		else {
			bab.append(0x23);
		}
		bab.append(data, 0, data.length);
		return bab.buffer();
	}
	WindowControlThread mWindowControlThread;
	public class WindowControlThread extends Thread {

		private boolean mIsRunning = true;
		private byte []mData;
		public WindowControlThread(byte []data) {
			mData = data;
		}
		public boolean isRunning() {
			return mIsRunning;
		}

		public void setRunning(boolean mIsRunning) {
			this.mIsRunning = mIsRunning;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			super.run();
			while (mIsRunning) {
				if (mService != null) {
					mService.write(mData, BleService.MY_TIMER_CHARACTERISTIC);
				}
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		}
		
	}
}
