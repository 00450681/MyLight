package com.bde.light.activity;

import com.bde.light.activity.MainActivity.WindowControlThread;
import com.bde.light.model.Light;
import com.bde.light.service.BleService;

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

public class EndInitActivity extends Activity {

	private BleService mService;
	private ServiceConnection onService;
	private Light myLight;
	private Button upBtn, downBtn, confirmBtn;
	private boolean isConfirm = false, isEnterLongClick = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.end_init_fragment_layout);
		
		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			myLight = (Light) bundle.getSerializable(Light.LIGHT);
		}
		
		onService = new ServiceConnection() {
            public void onServiceConnected(ComponentName className, IBinder rawBinder) {
                mService = ((BleService.LocalBinder) rawBinder).getService();
                if (mService != null) {
                	//mService.scan(true);
                    mService.setActivityHandler(mHandler);
                    /*if (myLight != null) {
                    	mService.setMyLight(myLight);
                    	mService.setOperation(BleService.WINDOW);
                    	mService.connect(myLight.address);
                    }*/
                    
                }
            }
            
            public void onServiceDisconnected(ComponentName classname) {
                mService = null;
            }
        };
        //startService(new Intent(this, BleService.class));
        Intent bindIntent = new Intent(this, BleService.class);
        bindService(bindIntent, onService, Context.BIND_AUTO_CREATE);
        
        /*upBtn = (Button) findViewById(R.id.up);
        upBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (mService != null) {
					mService.write(new byte[]{0x20}, BleService.MY_TIMER_CHARACTERISTIC);
				}
			}
		});
		downBtn = (Button) findViewById(R.id.down);
		downBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (mService != null) {
					mService.write(new byte[]{0x21}, BleService.MY_TIMER_CHARACTERISTIC);
				}
			}
		});*/
        upBtn = (Button) findViewById(R.id.up);
        upBtn.setOnLongClickListener(new View.OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View v) {
				// TODO Auto-generated method stub
				System.out.println("long press");
				byte []data = {0x20};
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
					/*byte []data = {0x20};
					System.out.println("窗帘开");
					System.out.println(mService.write(data, BleService.MY_TIMER_CHARACTERISTIC));
					time = System.currentTimeMillis();*/
					v.clearFocus();
					v.setPressed(false);
					if (mWindowControlThread != null) {
						mWindowControlThread.setRunning(false);
					}
					downBtn.setEnabled(true);
					isEnterLongClick = false;
					return true;
				}/* else if (event.getAction() == MotionEvent.ACTION_UP) {
					// 短按松手
					System.out.println("短按松手，设置mCloseBtn为enable");
					mCloseBtn.setEnabled(true);
					//return true;
				}*/
				return false;
			}
		});
        upBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				downBtn.setEnabled(false);
				System.out.println("窗帘开");
				System.out.println(mService.write(new byte[]{0x20}, BleService.MY_TIMER_CHARACTERISTIC));
				downBtn.setEnabled(true);
			}
		});
		
		downBtn = (Button) findViewById(R.id.down);
		downBtn.setOnLongClickListener(new View.OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View v) {
				// TODO Auto-generated method stub
				System.out.println("long press");
				byte []data = {0x21};
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
					/*byte []data = {0x20};
					System.out.println("窗帘开");
					System.out.println(mService.write(data, BleService.MY_TIMER_CHARACTERISTIC));
					time = System.currentTimeMillis();*/
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
				System.out.println(mService.write(new byte[]{0x21}, BleService.MY_TIMER_CHARACTERISTIC));
				upBtn.setEnabled(true);
			}
		});
		

		confirmBtn = (Button) findViewById(R.id.confirm);
		confirmBtn.setOnClickListener(new View.OnClickListener() {
		
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (mService != null) {
					mService.write(new byte[]{0x29}, BleService.MY_TIMER_CHARACTERISTIC);
					isConfirm = true;
				}
			}
			});
		
		Button bt_back = (Button) findViewById(R.id.bt_back);
		bt_back.setText(R.string.setting);
		bt_back.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});

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
            	/*upBtn.setEnabled(true);
            	downBtn.setEnabled(true);ni 
            	confirmBtn.setEnabled(true);*/
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
            		finish();
            	}
            	break;
            }
        }
	};
	
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