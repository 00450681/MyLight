package com.bde.light.adapter;

import java.util.ArrayList;
import com.bde.light.activity.R;
import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class SettingAdapter extends BaseAdapter {
	
	private ArrayList<String> list;
	private Context context;
	
	public SettingAdapter(Context context,ArrayList<String> list) {
		this.context = context;
		this.list = list;
	}
	
	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		if (convertView == null) {
			convertView = ((Activity) context).getLayoutInflater().inflate(R.layout.item_setting_layout, parent, false);
			
			ViewHolder holder = new ViewHolder();
			holder.nameView = (TextView) convertView.findViewById(R.id.setting_option);
			
			convertView.setTag(holder);
		}
		
		ViewHolder holder = (ViewHolder) convertView.getTag();
		holder.nameView.setText(list.get(position));
		
		return convertView;
	}
	
	private class ViewHolder {
		public TextView nameView;
	}

}
