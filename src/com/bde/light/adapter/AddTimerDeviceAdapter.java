package com.bde.light.adapter;

import java.util.ArrayList;

import com.bde.light.activity.R;
import com.bde.light.model.Light;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class AddTimerDeviceAdapter extends BaseAdapter {
	
	private ArrayList<Light> list;
	private Context context;
	
	public AddTimerDeviceAdapter(Context context,ArrayList<Light> list) {
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
			convertView = ((Activity) context).getLayoutInflater().inflate(R.layout.item_add_timer_device_list, parent, false);
			
			ViewHolder holder = new ViewHolder();
			holder.tv_lightName = (TextView) convertView.findViewById(R.id.light_name);
			
			convertView.setTag(holder);
		}
		
		ViewHolder holder = (ViewHolder) convertView.getTag();
		holder.tv_lightName.setText(getItem(position).name);
		
		return convertView;
	}
	
	private class ViewHolder {
		public TextView tv_lightName;
	}

}
