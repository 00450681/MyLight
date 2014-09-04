package com.bde.light.adapter;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bde.light.activity.R;
import com.bde.light.mgr.LightMgr;
import com.bde.light.model.Light;

public class DeviceSettingAdapter extends BaseAdapter {
	public static final int DELETE_COMPLETED = 100;
	public static final int BRIGHTNESS_CONTROL = 101;
	private ArrayList<Light> list;
	private Handler mHandler;
	private Context context;
	
	public DeviceSettingAdapter(Context context,ArrayList<Light> list, Handler handler) {
		this.context = context;
		this.list = list;
		mHandler = handler;
	}
	public DeviceSettingAdapter(Context context,ArrayList<Light> list) {
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
		
		if (convertView == null) {
			convertView = ((Activity) context).getLayoutInflater().inflate(R.layout.item_device_list, parent, false);
			
			ViewHolder holder = new ViewHolder();
			holder.iv_picture =  (ImageView) convertView.findViewById(R.id.state);
			holder.tv_lightName = (TextView) convertView.findViewById(R.id.light_name);
			holder.tv_areas = (TextView) convertView.findViewById(R.id.areas);
			holder.iv_signal = (ImageView) convertView.findViewById(R.id.signal);
			holder.iv_isFound = (ImageView) convertView.findViewById(R.id.is_found);
			//holder.btn_brightness = (Button) convertView.findViewById(R.id.brightnessBtn);
			
			convertView.setTag(holder);
		}
		
		ViewHolder holder = (ViewHolder) convertView.getTag();
		holder.iv_picture.setBackgroundResource(getItem(position).picture);
		holder.tv_lightName.setText(getItem(position).name);
		holder.tv_areas.setText(getItem(position).area);
		holder.iv_signal.setBackgroundResource(getItem(position).signal);
		holder.iv_isFound.setBackgroundResource(getItem(position).isFound);
		/*if (getItem(position).brightnessChangeable == 1 && getItem(position).type == 3 && getItem(position).isValidate == 1) {
			holder.btn_brightness.setVisibility(View.VISIBLE);
		}
		else {
			holder.btn_brightness.setVisibility(View.GONE);
		}*/
		return convertView;
	}
	
	private class ViewHolder {
		public ImageView iv_picture;
		public TextView tv_lightName;
		public TextView tv_areas;
		public ImageView iv_signal;
		public ImageView iv_isFound;
		public Button btn_brightness;
	}

	public void remove(Light item) {
		list.remove(item);
	}

	public void insert(Light light, int to) {
		list.add(to, light);
	}

}
