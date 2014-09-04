package com.bde.light.adapter;

import java.util.ArrayList;
import java.util.Calendar;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bde.light.activity.R;
import com.bde.light.activity.TimerActivity;
import com.bde.light.mgr.TimerMgr;
import com.bde.light.model.Light;
import com.bde.light.model.Timer;

public class DeviceAdapter extends BaseAdapter {
	public static final int DELETE_COMPLETED = 100;
	public static final int BRIGHTNESS_CONTROL = 101;
	public static final int WINDOW_CONTROL = 102;
	private ArrayList<Light> list;
	private Handler mHandler;
	private Context context;
	
	public DeviceAdapter(Context context,ArrayList<Light> list, Handler handler) {
		this.context = context;
		this.list = list;
		mHandler = handler;
		
	}
	public DeviceAdapter(Context context,ArrayList<Light> list) {
		this.context = context;
		this.list = list;
	}
	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public Light getItem(int position) {
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Log.i("DeviceAdapter", "getView()");
		if (convertView == null) {
			convertView = ((Activity) context).getLayoutInflater().inflate(R.layout.item_device_list, parent, false);
			
			ViewHolder holder = new ViewHolder();
			holder.iv_picture =  (ImageView) convertView.findViewById(R.id.state);
			holder.tv_lightName = (TextView) convertView.findViewById(R.id.light_name);
			holder.tv_areas = (TextView) convertView.findViewById(R.id.areas);
			holder.iv_signal = (ImageView) convertView.findViewById(R.id.signal);
			holder.iv_isFound = (ImageView) convertView.findViewById(R.id.is_found);
			holder.btn_delete = (Button) convertView.findViewById(R.id.btn_delete);
			holder.btn_brightness = (Button) convertView.findViewById(R.id.brightnessBtn);
			holder.timerIcon = (ImageView) convertView.findViewById(R.id.timerIcon);
			holder.btn_window = (Button) convertView.findViewById(R.id.windowBtn);
			
			convertView.setTag(holder);
		}
		
		ViewHolder holder = (ViewHolder) convertView.getTag();
		holder.iv_picture.setBackgroundResource(getItem(position).picture);
		holder.tv_lightName.setText(getItem(position).name);
		holder.tv_areas.setText(getItem(position).area);
		holder.iv_signal.setBackgroundResource(getItem(position).signal);
		holder.iv_isFound.setBackgroundResource(getItem(position).isFound);
		holder.btn_brightness.setTag(position);
		holder.btn_window.setTag(position);
		
		TimerMgr timerMgr = new TimerMgr(context);
		ArrayList<Timer> temp = timerMgr.findAllByAddress(getItem(position).address);
		if (temp.size() != 0 && getItem(position).isValidate == 1) {
				//有设置定时器
			System.out.println("有定时器的灯...地址 = " + getItem(position).address + " name = " + getItem(position).name);
			System.out.println("position为 " + position);
			int count = temp.size();
			Calendar calendar = Calendar.getInstance();
			
			while (count > 0) {
				// 看看定时器是否过期了，如果过期酒删除且不显示了
				Timer timerTemp = temp.get(temp.size() - count);
				String[] time = timerTemp.time.split(":");
				int setHour = Integer.parseInt(time[0]);
				int setMinute = Integer.parseInt(time[1]);

				int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
				int currentMinute = calendar.get(Calendar.MINUTE);
				int currentSecond = calendar.get(Calendar.SECOND);
				int second = 0;
				second = TimerActivity.getSeconds(setHour, setMinute, currentHour, currentMinute, currentSecond);
				if (second <= 0) {
					timerMgr.delete(timerTemp);
					temp.remove(timerTemp);
				}
				count--;
			}
			if (temp.size() > 0)
				holder.timerIcon.setVisibility(View.VISIBLE);
		}
		else {
			holder.timerIcon.setVisibility(View.GONE);
		}
		
		if (getItem(position).isValidate == 0) {
			holder.btn_delete.setVisibility(View.GONE);
			
		}
		if (getItem(position).type == 4 && getItem(position).isValidate == 1) {
			holder.btn_window.setVisibility(View.VISIBLE);
			holder.btn_window.setOnTouchListener(new View.OnTouchListener() {

				@Override
				public boolean onTouch(View v, MotionEvent event) {
					// TODO Auto-generated method stub
					System.out.println("window ontouch is called");
					if (event.getAction() == MotionEvent.ACTION_DOWN) {
						Message msg = mHandler.obtainMessage(WINDOW_CONTROL);
						Bundle bundle = new Bundle();
						bundle.putSerializable("DEVICE", list.get((Integer) v.getTag()));
						msg.setData(bundle);
						msg.sendToTarget();
					}
					return false;
				}
			});
		} else {
			holder.btn_window.setVisibility(View.GONE);
		}
		if (getItem(position).brightnessChangeable == 1 && (getItem(position).type == 3 || getItem(position).type == 0x13 ) && getItem(position).isValidate == 1) {
			holder.btn_brightness.setVisibility(View.VISIBLE);
			/*holder.btn_brightness.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					Message msg = mHandler.obtainMessage(BRIGHTNESS_CONTROL);
					Bundle bundle = new Bundle();
					bundle.putSerializable("DEVICE", list.get((Integer) v.getTag()));
					msg.setData(bundle);
					msg.sendToTarget();
				}
				
			});*/
			holder.btn_brightness.setOnTouchListener(new View.OnTouchListener() {

				@Override
				public boolean onTouch(View v, MotionEvent event) {
					// TODO Auto-generated method stub
					System.out.println("brightness ontouch is called");
					if (event.getAction() == MotionEvent.ACTION_DOWN) {
						Message msg = mHandler.obtainMessage(BRIGHTNESS_CONTROL);
						Bundle bundle = new Bundle();
						bundle.putSerializable("DEVICE", list.get((Integer) v.getTag()));
						msg.setData(bundle);
						msg.sendToTarget();
					}
					return false;
				}
			});
			//holder.btn_brightness.get
		}
		else {
			holder.btn_brightness.setVisibility(View.GONE);
		}
		holder.btn_delete.setTag(position);
		/*holder.btn_delete.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				System.out.println("Delete Button clicked!");
				View layout = ((Activity) context).getLayoutInflater().inflate(R.layout.item_device_list, null, false);
				LightMgr lightMgr = new LightMgr(context);
				int position = (Integer) v.getTag();
				int result = lightMgr.delete(getItem(position).address);
				if (result > 0) {
					list.remove(position);
					v.setVisibility(View.GONE);
					layout.findViewById(R.id.signal).setVisibility(View.VISIBLE);
					Message msg = mHandler.obtainMessage(DELETE_COMPLETED);
					msg.sendToTarget();
				}
			}
		});*/
		/*holder.btn_delete.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					System.out.println("Delete Button clicked!");
					View layout = ((Activity) context).getLayoutInflater().inflate(R.layout.item_device_list, null, false);
					LightMgr lightMgr = new LightMgr(context);
					int position = (Integer) v.getTag();
					int result = lightMgr.delete(getItem(position).address);
					if (result > 0) {
						list.remove(position);
						v.setVisibility(View.GONE);
						//layout.findViewById(R.id.signal).setBackgroundResource(getItem(position).signal);
						//layout.findViewById(R.id.signal).setBackgroundResource(getItem(position).isFound);
						Message msg = mHandler.obtainMessage(DELETE_COMPLETED);
						msg.sendToTarget();
					}
				}
				
				return false;
			}
		});*/
		
		return convertView;
	}
	
	private class ViewHolder {
		public ImageView iv_picture;
		public TextView tv_lightName;
		public TextView tv_areas;
		public ImageView iv_signal;
		public ImageView iv_isFound;
		public Button btn_delete;
		public Button btn_brightness, btn_window;
		public ImageView timerIcon;
	}

	public void remove(Light item) {
		list.remove(item);
	}

	public void insert(Light light, int to) {
		list.add(to, light);
	}

	ListView mListView;
	public void setListView(ListView listview) {
		mListView = listview;
	}
	public void updateView(int position) {
		if (position <0 || position > list.size())
			return;
		Log.i("DeviceAdapter", "updateView()");
		int first = mListView.getFirstVisiblePosition();
		View view = mListView.getChildAt(position - first);
		if (view != null) {
			ViewHolder holder = (ViewHolder) view.getTag();
			if (holder == null) {
				holder = new ViewHolder();
				holder.iv_picture =  (ImageView) view.findViewById(R.id.state);
				holder.tv_lightName = (TextView) view.findViewById(R.id.light_name);
				holder.tv_areas = (TextView) view.findViewById(R.id.areas);
				holder.iv_signal = (ImageView) view.findViewById(R.id.signal);
				view.setTag(holder);
			}
			holder.iv_picture.setBackgroundResource(getItem(position).picture);
			holder.tv_lightName.setText(getItem(position).name);
			holder.tv_areas.setText(getItem(position).area);
			holder.iv_signal.setBackgroundResource(getItem(position).signal);
			return;
		}
		/*System.out.println("DeviceAdapter 发生了严重的问题....view为NULL");
		System.out.println("position is " + position + " and first is " + first);*/
	}
	public void deleteView(int position) {
		if (position <0 || position > list.size())
			return;
		int first = mListView.getFirstVisiblePosition();
		View view = mListView.getChildAt(position - first);
		if (view != null) {
			mListView.removeViewAt(position - first);
			return;
		}
		System.out.println("DeviceAdapter 发生了严重的问题....view为NULL");
		System.out.println("position is " + position + " and first is " + first);
	}
	
}
