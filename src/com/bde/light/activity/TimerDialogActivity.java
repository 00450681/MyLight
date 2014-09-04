package com.bde.light.activity;

import java.util.ArrayList;
import java.util.Calendar;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;

import com.bde.light.adapter.AddTimerDeviceAdapter;
import com.bde.light.model.Light;
import com.bde.light.model.Timer;

public class TimerDialogActivity extends Activity {

	public static final String SELECT_DEVICE = "select_device";
	
	private TimePicker timePicker;
	private TextView select_device;
	private ImageView open_check;
	private ImageView close_check;
	private ListView deviceListView;
	private Button btn_save;
	private ArrayList<Light> deviceList;
	private AddTimerDeviceAdapter lightAdapter;
	private ImageView btn_close;
	private Light myLight;
	private Timer timer;
	private int operation;
	private TextView open_tv, close_tv;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.time_layout);
		
		timer = new Timer();
		
		findViews();
		setListeners();
		initData();
		
	}

	private void findViews() {
		// 可以选择的远程设备列表
		deviceListView = (ListView) findViewById(R.id.device_list);
		// 添加按钮
		btn_save = (Button) findViewById(R.id.save_timer);
		// 选择设备列表
		select_device = (TextView) findViewById(R.id.select_device);
		open_check = (ImageView) findViewById(R.id.open_check);
		close_check = (ImageView) findViewById(R.id.close_check);
		//Button btn_back = (Button) findViewById(R.id.bt_back);
		// 关闭addTimer界面
		btn_close = (ImageView) findViewById(R.id.close);
		timePicker = (TimePicker) findViewById(R.id.timepicker);

		open_check.setTag(R.drawable.frame_no_check);
		close_check.setTag(R.drawable.frame_no_check);
		
		open_tv = (TextView) findViewById(R.id.open_tv);
		close_tv = (TextView) findViewById(R.id.close_tv);
		
		
		
	}
	private void initData() {
		timePicker.setIs24HourView(true);
		timePicker.setDescendantFocusability(TimePicker.FOCUS_BLOCK_DESCENDANTS);
		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			operation = bundle.getInt(Timer.OPERATION);
			if (operation == Timer.OPERATION_ADD) {
				int currentHour, currentMinute;
				Calendar calendar = Calendar.getInstance();
				currentHour = calendar.get(Calendar.HOUR_OF_DAY);
				currentMinute = calendar.get(Calendar.MINUTE);
				timePicker.setCurrentHour(currentHour);
				timePicker.setCurrentMinute(currentMinute);
				deviceList = (ArrayList<Light>) bundle.getSerializable(SELECT_DEVICE);
				lightAdapter = new AddTimerDeviceAdapter(this, deviceList);
				deviceListView.setAdapter(lightAdapter);
				//新增定时器默认的动作默认是开启的
				//timer.operation = "开启";
				
				close_check.setClickable(false);
				close_tv.setClickable(false);
				open_check.setClickable(false);
				open_tv.setClickable(false);
				
				close_tv.setTextColor(TimerDialogActivity.this.getResources().getColor(R.color.silver));
				open_tv.setTextColor(TimerDialogActivity.this.getResources().getColor(R.color.silver));
				close_check.setImageAlpha(90);
				open_check.setImageAlpha(90);
			}
			else if (operation == Timer.OPERATION_MODIFY ) {
				timer = (Timer) bundle.getSerializable("TIMER_INFO");
				select_device.setText(timer.name);
				String[] time = timer.time.split(":");
				timePicker.setCurrentHour(Integer.parseInt(time[0]));
				timePicker.setCurrentMinute(Integer.parseInt(time[1]));
				if (timer.operation.equals("关闭") || timer.operation.equals(getString(R.string.close))) {
					open_check.setImageResource(R.drawable.frame_no_check);
					open_check.setTag(R.drawable.frame_no_check);
					close_check.setImageResource(R.drawable.frame_with_checked);
					close_check.setTag(R.drawable.frame_with_checked);
				}
			}
				
		}
		
	}
	private void setListeners() {
		btn_close.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		select_device.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (deviceListView.getVisibility() == View.GONE) {
					deviceListView.setVisibility(View.VISIBLE);
				} else {
					deviceListView.setVisibility(View.GONE);
				}
			}
		});
		
		open_tv.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				int a = (Integer) open_check.getTag();
				if (a == R.drawable.frame_no_check) {
					//设置开启为选择状态
					open_check.setImageResource(R.drawable.frame_with_checked);
					open_check.setTag(R.drawable.frame_with_checked);
					close_check.setImageResource(R.drawable.frame_no_check);
					close_check.setTag(R.drawable.frame_no_check);
					timer.operation = "开启";
				}
			}
		});
		
		close_tv.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				int b = (Integer) close_check.getTag();
				if (b == R.drawable.frame_no_check) {
					//设置关闭为选择状态
					open_check.setImageResource(R.drawable.frame_no_check);
					open_check.setTag(R.drawable.frame_no_check);
					close_check.setImageResource(R.drawable.frame_with_checked);
					close_check.setTag(R.drawable.frame_with_checked);
					timer.operation = "关闭";
				}
			}
		});
		
		open_check.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				int a = (Integer) open_check.getTag();
				if (a == R.drawable.frame_no_check) {
					//设置开启为选择状态
					open_check.setImageResource(R.drawable.frame_with_checked);
					open_check.setTag(R.drawable.frame_with_checked);
					close_check.setImageResource(R.drawable.frame_no_check);
					close_check.setTag(R.drawable.frame_no_check);
					timer.operation = "开启";
				}
			}
		});
		
		close_check.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				int b = (Integer) close_check.getTag();
				if (b == R.drawable.frame_no_check) {
					//设置关闭为选择状态
					open_check.setImageResource(R.drawable.frame_no_check);
					open_check.setTag(R.drawable.frame_no_check);
					close_check.setImageResource(R.drawable.frame_with_checked);
					close_check.setTag(R.drawable.frame_with_checked);
					timer.operation = "关闭";
				}
			}
		});
	
		deviceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				//注意如果是修改定时器，则不响应他
				// TODO Auto-generated method stub
				/*myLight = deviceList.get(position);
				select_device.setText(myLight.name);
				timer.name = myLight.name;
				timer.lightAddress = myLight.address;
				deviceListView.setVisibility(View.GONE);*/
				timer.light = deviceList.get(position);
				select_device.setText(timer.light.name);
				timer.name = timer.light.name;
				timer.lightAddress = timer.light.address;
				deviceListView.setVisibility(View.GONE);
				
				
				if (timer.light.timer_open == 1) {
					open_check.setClickable(true);
					open_tv.setClickable(true);
					open_tv.setTextColor(TimerDialogActivity.this.getResources().getColor(R.color.white));
					open_check.setImageAlpha(1000);
				}
				else {
					open_check.setClickable(false);
					open_tv.setClickable(false);
					open_tv.setTextColor(TimerDialogActivity.this.getResources().getColor(R.color.silver));
					open_check.setImageAlpha(90);
				}
				if (timer.light.timer_close == 1) {
					close_check.setClickable(true);
					close_tv.setClickable(true);
					close_tv.setTextColor(TimerDialogActivity.this.getResources().getColor(R.color.white));
					close_check.setImageAlpha(1000);
				}
				else {
					close_check.setClickable(false);
					close_tv.setClickable(false);
					
					close_tv.setTextColor(TimerDialogActivity.this.getResources().getColor(R.color.silver));
					close_check.setImageAlpha(90);
				}
			}
		});
		
		btn_save.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (timer.light != null && timer.operation != null) {
					if ((timer.light.timer_open == 1 && timer.operation.equals("开启"))
							|| (timer.light.timer_close == 1 && timer.operation.equals("关闭"))) {
						
						Calendar calendar = Calendar.getInstance();
						int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
						int currentMinute = calendar.get(Calendar.MINUTE);
						if (getSeconds(timePicker.getCurrentHour(), timePicker.getCurrentMinute(), currentHour, currentMinute) > 0) {
							String setHour = timePicker.getCurrentHour() + "";
							String setMinute = timePicker.getCurrentMinute() + "";
							timer.time = (setHour.length() == 1 ? ("0" + setHour) : setHour) + ":" +
									(setMinute.length() == 1 ? ("0" + setMinute) : setMinute);
							Intent intent = new Intent(TimerDialogActivity.this, TimerActivity.class);
							Bundle bundle = new Bundle();
							bundle.putSerializable("result", timer);
							intent.putExtras(bundle);
							setResult(operation, intent);
							finish();
						}
						else {
							new AlertDialog.Builder(TimerDialogActivity.this)
							.setMessage(R.string.over_time)
							.setNegativeButton(R.string.confirm, null)
							.create().show();
						}		
					}
					else {
						new AlertDialog.Builder(TimerDialogActivity.this)
						.setMessage(R.string.operationNotSupport)
						.setNegativeButton(R.string.confirm, null)
						.create().show();
					}
					
				}
				else {
					new AlertDialog.Builder(TimerDialogActivity.this)
					.setMessage(R.string.choose_device)
					.setNegativeButton(R.string.confirm, null)
					.create().show();
				}
			}
		});
	}
	
	/**
	 * 获取秒数
	 */
	int getSeconds(int setHour, int setMinute, int currentHour,int currentMinute) {
		return (setHour - currentHour) * 3600 + (setMinute - currentMinute) * 60;
	}
}
