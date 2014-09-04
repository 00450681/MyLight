package com.bde.light.activity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.Queue;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bde.light.adapter.TimerAdapter;
import com.bde.light.mgr.TimerMgr;
import com.bde.light.model.Light;
import com.bde.light.model.Timer;
import com.bde.light.myview.DragSortListView;
import com.bde.light.service.BleService;
import com.bde.light.utils.MyActivityUtils;

@SuppressLint("HandlerLeak")
public class TimerActivity extends Activity implements OnClickListener {
	
	public static final String SELECT_DEVICE = "select_device";
	
	
	private ListView timerListView;
	private DragSortListView ddlv;
	private int operation;
	private Button btn_add;
	
	
	private ArrayList<Timer> timerList;
	private TimerAdapter timerAdapter;
	private TimerMgr timerMgr;
	private BleService mService;
	private ServiceConnection onService;
	private BluetoothAdapter mBluetoothAdapter;
	private Queue<Timer> deleteQueue;
	private Timer myTimer;
	private Timer myOldTimer;
	private int action;
	private ProgressDialog dialog;
	boolean operationComplete = false;
	private TextView noTimer;
	
	private Context mContext;
	private final int TIMER_TO_DELETE = 1023;
	private static final String TIMER_TO_DELETE_STR = "TIMER TO DELETE";
	float downX = 0, downY = 0, upX = 0, upY = 0;
	int p1,p2;
	
	private Handler mTimerHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
        	//设置成功了
        	//修改成功后需要删除数据库中原来的定时器
        	Bundle data = msg.getData();
        	byte[] result = (byte[]) data.getSerializable(BleService.RESULT);
        	if (dialog != null)
        		dialog.dismiss();
            switch (msg.what) {
            case BleService.TIMER:
            	for (int i = 0; i < result.length; i++) {
            		System.out.println(result[i]);
            	}
            	
            	int heigh = result[1];
            	int low = result[0];
            	int index;
            	int second = 0;
            	if (myTimer != null) {
            		myTimer.index = Integer.toString((heigh<<8) + low);
                	
                	Handler handler = new Handler();
                	
                	String[] time = myTimer.time.split(":");
        			int setHour = Integer.parseInt(time[0]);
        			int setMinute = Integer.parseInt(time[1]);

        			Calendar calendar = Calendar.getInstance();
        			int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        			int currentMinute = calendar.get(Calendar.MINUTE);
        			int currentSecond = calendar.get(Calendar.SECOND);
        			
        			second = getSeconds(setHour, setMinute, currentHour, currentMinute, currentSecond);
            	}
            	RemoveTimerWhenFinished thread;
            	switch (operation) {
            	
            	case Timer.OPERATION_ADD:   		
                	long a = timerMgr.add(myTimer);
                	if (a > 0) {
                		myTimer.id = a;
                		if (myTimer.operation.equals("开启"))
                    		myTimer.operation = getString(R.string.open);
                    	else if (myTimer.operation.equals("关闭"))
                    		myTimer.operation = getString(R.string.close);
                    	timerList.add(myTimer);
                    	//postdelay
                    	deleteQueue.offer(myTimer);
                    	//handler.postDelayed(removeTimerWhenFinished, (second * 1000));
                    	//thread = new RemoveTimerWhenFinished(mContext, (second * 1000), myTimer);
                    	//thread.start();
                    	mTimerHandler.postDelayed(new RemoveTimerWhenFinished(mContext, myTimer), second * 1000);
                    	MyActivityUtils.toast(TimerActivity.this, R.string.add_success);
                        findViewById(R.id.no_timer).setVisibility(View.GONE);
                        timerListView.setVisibility(View.VISIBLE);
                	}
            		break;
            	case Timer.OPERATION_MODIFY:
            		//要知道timer在List中的位置
                	timerMgr.update(myTimer);
                	index = timerList.indexOf(myOldTimer);
                	timerList.remove(index);
                	timerList.add(index, myTimer);
                	//postdelay
                	//需要删除队列中之前的那个Timer
                	deleteQueue.remove(myOldTimer);
                	deleteQueue.offer(myTimer);
                	//handler.postDelayed(removeTimerWhenFinished, (second * 1000));
                	/*thread = new RemoveTimerWhenFinished(mContext, (second * 1000), myTimer);
                	thread.start();*/
                	mTimerHandler.postDelayed(new RemoveTimerWhenFinished(mContext, myTimer), second * 1000);
                	MyActivityUtils.toast(TimerActivity.this, R.string.update_success);
            		break;
            	case Timer.OPERATION_DELETE:
                	timerMgr.delete(myOldTimer);
                	index = timerList.indexOf(myOldTimer);
                	if (index > -1) {
                		timerList.remove(index);
                	}
                	dialog.dismiss();
                	MyActivityUtils.toast(TimerActivity.this, R.string.delete_success);
            		break;
            	}
            	operationComplete = true;
            	//此处timerList实际上已经指向另一个地方，而不是原来的list了！！！
            	//timerList = timerMgr.findAll();
            	timerAdapter.notifyDataSetChanged();
                break;
            case BleService.DISCONNECTED:
            	dialog.dismiss();
            	break;
            	
            case TIMER_TO_DELETE:
            	Timer timer = (Timer) data.getSerializable(TIMER_TO_DELETE_STR);
            	index = timerList.indexOf(timer);
            	if (index >= 0) {
            		timerList.remove(index);
            	}
				if (timerList.size() == 0) {
					timerAdapter.notifyDataSetChanged();
					timerListView.setVisibility(View.GONE);
					noTimer.setVisibility(View.VISIBLE);
				}
				else {
					timerAdapter.notifyDataSetChanged();
				}
            	break;
            }
            
            
        }
	};
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		//super.onActivityResult(requestCode, resultCode, data);
		if (data != null) {
			
			
			dialog.show();
			
			Bundle bundle = data.getExtras();
			myOldTimer = myTimer;
			myTimer = (Timer) bundle.getSerializable("result");

			String[] time = myTimer.time.split(":");
			int setHour = Integer.parseInt(time[0]);
			int setMinute = Integer.parseInt(time[1]);

			Calendar calendar = Calendar.getInstance();
			int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
			int currentMinute = calendar.get(Calendar.MINUTE);
			int currentSecond = calendar.get(Calendar.SECOND);
			int second = 0;
			second = getSeconds(setHour, setMinute, currentHour, currentMinute, currentSecond);
			byte[] buffer = null;
			if (myTimer.operation.equals("开启")) {
				action = Timer.ACTION_ON;
			} else if (myTimer.operation.equals("关闭")) {
				action = Timer.ACTION_OFF;
			} else {
				new AlertDialog.Builder(TimerActivity.this)
				.setMessage(R.string.operationNotSupport)
				.setNegativeButton(R.string.confirm, null)
				.create().show();
				return;
			}

			switch (resultCode) {
			case Timer.OPERATION_ADD:
				buffer = Timer.addTimer(action, second);
				operation = Timer.OPERATION_ADD;
				break;
			case Timer.OPERATION_MODIFY:
				buffer = Timer.modifyTimer(Integer.parseInt(myTimer.index),
						action, second);
				operation = Timer.OPERATION_MODIFY;
				break;
			default:
				return;
			}
			if (buffer != null) {
				operationComplete = false;
				mService.setTimerData(buffer);
				mService.setOperation(BleService.TIMER);
				mService.setMyLight(myTimer.light);
				System.out.println("myLight.address: " + myTimer.light.address);
				mService.connect(
						mBluetoothAdapter.getRemoteDevice(myTimer.light.address),
						false);
				new Thread(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						try {
							Thread.sleep(4000);
							if (!operationComplete && !timerList.contains(myTimer)) {
								//超时失败
								runOnUiThread(new Runnable() {

									@Override
									public void run() {
										// TODO Auto-generated method stub
										dialog.dismiss();
										MyActivityUtils.toast(TimerActivity.this, R.string.timeout);
									}
									
								});
								
							}
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					
				}).start();
			}
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.timer_activity);
		
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		
		onService = new ServiceConnection() {
            public void onServiceConnected(ComponentName className, IBinder rawBinder) {
                mService = ((BleService.LocalBinder) rawBinder).getService();
                mService.setTimerHandler(mTimerHandler);
                if (mService != null) {
                }
            }
            
            public void onServiceDisconnected(ComponentName classname) {
                mService = null;
            }
        };
        startService(new Intent(this, BleService.class));
        Intent bindIntent = new Intent(this, BleService.class);
        bindService(bindIntent, onService, Context.BIND_AUTO_CREATE);
		
        deleteQueue = new LinkedList<Timer>();
        mContext = this;
        
        
		initView();
		//initDeviceList();
		timerMgr = new TimerMgr(this);
		dialog = new ProgressDialog(this);
		dialog.setTitle(getString(R.string.operating));
	}
	
	private void initView() {
		noTimer = (TextView) findViewById(R.id.no_timer);
		//已经设置好的定时器
		timerListView = (ListView) findViewById(R.id.list);
		//添加按钮
		btn_add = (Button) findViewById(R.id.add_timer);
		//返回按钮
		Button btn_back = (Button) findViewById(R.id.bt_back);
		//顶部中间显示的标题，这里应该显示定时器
		TextView tv_top_title = (TextView) findViewById(R.id.top_title);
		//设置返回按钮的文字
		btn_back.setText(R.string.home);
		//设置顶部中间的文字
		tv_top_title.setText(R.string.timer);
		btn_back.setOnClickListener(this);
		btn_add.setOnClickListener(this);
	}

	/**
	 * 初始化选择设备列表
	 */
	void initDeviceList(){
		//初始化已经设置了的定时器列表
		
		timerList = timerMgr.findAll();
		
		ArrayList<Timer> deleteTimerList = new ArrayList<Timer>();
		for (Timer timer : timerList) {
			String[] time = timer.time.split(":");
			int setHour = Integer.parseInt(time[0]);
			int setMinute = Integer.parseInt(time[1]);

			Calendar calendar = Calendar.getInstance();
			int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
			int currentMinute = calendar.get(Calendar.MINUTE);
			int currentSecond = calendar.get(Calendar.SECOND);
			int second = 0;
			second = getSeconds(setHour, setMinute, currentHour, currentMinute, currentSecond);
			if (second <= 0) {
				timerMgr.delete(timer);
			} else {
				//new RemoveTimerWhenFinished(this, second * 1000, timer).start();
				Log.i("TimerThread", "mTimerHandler.postDelayed sleepTime = " + second * 1000);
				//mTimerHandler.postDelayed(new RemoveTimerWhenFinished(mContext, timer), second * 1000);
			}
		}
		timerList = timerMgr.findAll();
		for (Timer timer : timerList) {
			if (timer.operation.equals("开启")) {
				timer.operation = getString(R.string.open);;
			} else if (timer.operation.equals("关闭")) {
				timer.operation = getString(R.string.close);
			}
			
			String[] time = timer.time.split(":");
			int setHour = Integer.parseInt(time[0]);
			int setMinute = Integer.parseInt(time[1]);

			Calendar calendar = Calendar.getInstance();
			int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
			int currentMinute = calendar.get(Calendar.MINUTE);
			int currentSecond = calendar.get(Calendar.SECOND);
			int second = 0;
			second = getSeconds(setHour, setMinute, currentHour, currentMinute, currentSecond);
			mTimerHandler.postDelayed(new RemoveTimerWhenFinished(mContext, timer), second * 1000);
		}
		timerAdapter = new TimerAdapter(this, timerList);
		timerListView.setAdapter(timerAdapter);
		ddlv = (DragSortListView) timerListView;
        ddlv.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					//listView.setClickable(false);
					downX = event.getX();
					downY = event.getY();
					p1 = ((ListView) v).pointToPosition((int) downX, (int) downY);
				}
				
				if (event.getAction() == MotionEvent.ACTION_UP) {
					upX = event.getX();
					upY = event.getY();
					p2 = ((ListView) v).pointToPosition((int) upX, (int) upY);
					//View view = ((ListView) v).getChildAt(p2);
					//if (view == null) {
						int FirstVisiblePosition = ddlv.getFirstVisiblePosition();
						View view = ((ListView) v).getChildAt(p2 - FirstVisiblePosition);
					//}
					if (view != null) {
						Button btn_delete = (Button) view.findViewById(R.id.btn_delete);
						
						//ImageView iv_signal = (ImageView) view.findViewById(R.id.signal);
						if (p1 == p2 && Math.abs(upX - downX) > 200) {
							//滑动手势成功
							if (btn_delete.getVisibility() == View.GONE) {
								int childCount = ((ListView) v).getChildCount();
								for (int i = 0; i < childCount; i++) {
									View btnView = ((ListView)v).getChildAt(i);
									btnView.findViewById(R.id.btn_delete).setVisibility(View.GONE);
								}
								btn_delete.setVisibility(View.VISIBLE);
								//iv_signal.setVisibility(View.GONE);
								btn_delete.setOnClickListener(new OnClickListener() {
									public void onClick(View v) {
										dialog.show();
										myOldTimer = timerList.get(p1);
										String[] time = myOldTimer.time.split(":");
						    			int setHour = Integer.parseInt(time[0]);
						    			int setMinute = Integer.parseInt(time[1]);

						    			Calendar calendar = Calendar.getInstance();
						    			int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
						    			int currentMinute = calendar.get(Calendar.MINUTE);
						    			int currentSecond = calendar.get(Calendar.SECOND);
						    			
						    			int second = 0;
						    			second = getSeconds(setHour, setMinute, currentHour, currentMinute, currentSecond);
										if (second <= 0) {
											timerMgr.delete(myOldTimer);
						                	int index = timerList.indexOf(myOldTimer);
						                	if (index > -1) {
						                		timerList.remove(index);
						                		MyActivityUtils.toast(TimerActivity.this, R.string.delete_success);
						                		timerAdapter.notifyDataSetChanged();
						                		dialog.dismiss();
						                		if (timerList.size() == 0) {
						        					timerListView.setVisibility(View.GONE);
						        					noTimer.setVisibility(View.VISIBLE);
						        				}
						        				else {
						        					timerAdapter.notifyDataSetChanged();
						        				}
						                	}
										}
										else {
											operation = Timer.OPERATION_DELETE;
											//myOldTimer = timerList.get(p1);
											byte[] buffer = Timer.deleteTimer(Integer.parseInt(myOldTimer.index));
											mService.setTimerData(buffer);
											mService.setOperation(BleService.TIMER);
											mService.setMyLight(myOldTimer.light);
											mService.connect(
													mBluetoothAdapter.getRemoteDevice(myOldTimer.light.address),
													false);
										}
										
										v.setVisibility(View.GONE);
										/*if (lightList.size() != 0) {
											int result = lightMgr.delete(lightList.get(p1).address);
											if (result > 0) {
												lightList.remove(p1);
												MyActivityUtils.toast(MainActivity.this,R.string.delete_success);
												mService.scan(true);
												deviceAdapter.notifyDataSetChanged();
											} else {
												MyActivityUtils.toast(MainActivity.this,R.string.delete_failed);
											}
										}*/
										/*for (int i = 0; i < childCount; i++) {
											View btnView = ((ListView)v).getChildAt(i);
											btnView.findViewById(R.id.btn_delete).setVisibility(View.GONE);
										}*/
									}
								});
								return true;
							} 
						}else if (p1 == p2 && Math.abs(upX - downX) < 10) {
							/*
							 * 定时器可以修改时间与动作
							 * Timer timer = timerList.get(p1);
							Bundle bundle = new Bundle();
							bundle.putSerializable("TIMER_INFO", timer);
							bundle.putInt(Timer.OPERATION, Timer.OPERATION_MODIFY);
							Intent intent = new Intent(TimerActivity.this, TimerDialogActivity.class);
							intent.putExtras(bundle);
							startActivityForResult(intent, Timer.OPERATION_MODIFY);*/
						}
					}
					
				}
				
				return false;
			}
		});
        
        if (timerList.size() == 0) {
        	timerListView.setVisibility(View.GONE);
        	noTimer.setVisibility(View.VISIBLE);
        }
		//初始化选择设备列表
		/*timerListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				Timer timer = timerList.get(position);
				Bundle bundle = new Bundle();
				bundle.putSerializable("TIMER_INFO", timer);
				bundle.putInt(Timer.OPERATION, Timer.OPERATION_MODIFY);
				Intent intent = new Intent(TimerActivity.this, TimerDialogActivity.class);
				intent.putExtras(bundle);
				startActivityForResult(intent, Timer.OPERATION_MODIFY);
			}
		});*/
	}
	
	
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		initDeviceList();
		//for (Timer timer : )
	}

	public void onClick(View v) {
		// TODO Auto-generated method stub
		int id = v.getId();
		switch(id) {
		case R.id.bt_back:
			finish();
			break;
			
		case R.id.add_timer:
			// 添加定时器
			Bundle bundle = getIntent().getExtras();
			if (bundle == null)
				bundle = null;
			bundle.putInt(Timer.OPERATION, Timer.OPERATION_ADD);
			Intent intent = new Intent(TimerActivity.this, TimerDialogActivity.class);
			intent.putExtras(bundle);
			startActivityForResult(intent, Timer.OPERATION_ADD);
			break;
				/*//获取设置时分
				int setHour = timePicker.getCurrentHour();
				int setMinute = timePicker.getCurrentMinute();
				//获取当前时分
				Calendar calendar = Calendar.getInstance();
				int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
				int currentMinute = calendar.get(Calendar.MINUTE);
				int second = 0;
				if ((second = getSeconds(setHour, setMinute, currentHour, currentMinute)) > 0) {
					String device_name = select_device.getText().toString().trim();
					if (!device_name.equals(getString(R.string.click))) {
						//证明选择了设备，而不是初始状态的显示点击
						myTimer.name = device_name;
						//得到定时器设置时间
						myTimer.time = setHour + ":" + setMinute;
						//得到操作类型
						if ((Integer)open_check.getTag() == R.drawable.frame_with_checked) {
							myTimer.operation = "开启";
							action = Timer.ACTION_ON;
						} else {
							myTimer.operation ="关闭";
							action = Timer.ACTION_OFF;
						}
						Bundle bundle = new Bundle();
						byte[] buffer = Timer.addTimer(action, second);
						mService.setTimerData(buffer);
						mService.setOperation(BleService.TIMER);
						mService.setMyLight(myLight);
						System.out.println("myLight.address: " + myLight.address);
						mService.connect(mBluetoothAdapter.getRemoteDevice(myLight.address), false);
						
						}
					} else {
						//ToDo
						new AlertDialog.Builder(TimerActivity.this)
    					.setMessage(R.string.choose_device)
    					.setNegativeButton(R.string.confirm, null)
    					.create().show();
					}*/
		}
	}
	
	
	/**
	 * 获取秒数
	 */
	public static int getSeconds(int setHour, int setMinute, int currentHour,int currentMinute, int currentSecond) {
		return (setHour - currentHour) * 3600 + (setMinute - currentMinute) * 60 - currentSecond;
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		
		super.onDestroy();
		unbindService(onService);
		stopService(new Intent(this, BleService.class));
	}

	/*private class RemoveTimerWhenFinished extends Thread {

		private Context mContext;
		private TimerMgr mTimerMgr;
		private int mSleepTime;
		private Timer mTimer;
		public RemoveTimerWhenFinished(Context context, int sleepTime, Timer timer) {
			mContext = context;
			mTimerMgr = new TimerMgr(mContext);
			mSleepTime = sleepTime;
			mTimer = timer;
			Log.v("TimerThread", "new Thread sleeptime = " + sleepTime);
		}
		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
				Thread.sleep(mSleepTime);
				Timer deleteTimer = null;
				if (!deleteQueue.isEmpty()) {
					for (Timer timer : deleteQueue) {
						String[] time = timer.time.split(":");
		    			int setHour = Integer.parseInt(time[0]);
		    			int setMinute = Integer.parseInt(time[1]);

		    			Calendar calendar = Calendar.getInstance();
		    			int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
		    			int currentMinute = calendar.get(Calendar.MINUTE);
						if (getSeconds(setHour, setMinute, currentHour, currentMinute, 0) <= 0) {
							mTimerMgr.delete(timer);
							deleteTimer = timer;
							Message msg = Message.obtain(mTimerHandler, TIMER_TO_DELETE);
							Bundle data = new Bundle();
							data.putSerializable(TIMER_TO_DELETE_STR, timer);
							msg.setData(data);
							msg.sendToTarget();
							break;
						}
					}
					if (deleteTimer != null) {
						deleteQueue.remove(deleteTimer);
					}
				}
				String[] time = mTimer.time.split(":");
    			int setHour = Integer.parseInt(time[0]);
    			int setMinute = Integer.parseInt(time[1]);

    			Calendar calendar = Calendar.getInstance();
    			int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
    			int currentMinute = calendar.get(Calendar.MINUTE);
				if (getSeconds(setHour, setMinute, currentHour, currentMinute, 0) <= 0) {
					mTimerMgr.delete(mTimer);
					//deleteTimer = mTimer;
					Message msg = Message.obtain(mTimerHandler, TIMER_TO_DELETE);
					Bundle data = new Bundle();
					data.putSerializable(TIMER_TO_DELETE_STR, mTimer);
					msg.setData(data);
					msg.sendToTarget();
				//}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			Log.v("TimerThread", "Exiting Thread");
			
		}
		
	}*/
	
	private class RemoveTimerWhenFinished implements Runnable {

		private Context mContext;
		private TimerMgr mTimerMgr;
		private Timer mTimer;
		public RemoveTimerWhenFinished(Context context, Timer timer) {
			mContext = context;
			mTimerMgr = new TimerMgr(mContext);
			mTimer = timer;
			Log.v("TimerThread", "Entering ");
		}
		@Override
		public void run() {
			// TODO Auto-generated method stub
					mTimerMgr.delete(mTimer);
					//deleteTimer = mTimer;
					Message msg = Message.obtain(mTimerHandler, TIMER_TO_DELETE);
					Bundle data = new Bundle();
					data.putSerializable(TIMER_TO_DELETE_STR, mTimer);
					msg.setData(data);
					msg.sendToTarget();
			Log.v("TimerThread", "Exiting Thread");
		}
		
	}
	
}
