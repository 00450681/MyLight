package com.bde.light.adapter;

import java.util.ArrayList;
import com.bde.light.activity.R;
import com.bde.light.model.Timer;
import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class TimerAdapter extends BaseAdapter {
	
	private ArrayList<Timer> list;
	private Context context;
	
	public TimerAdapter(Context context,ArrayList<Timer> list) {
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
		//return list.get(position).id;
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		if (convertView == null) {
			convertView = ((Activity) context).getLayoutInflater().inflate(R.layout.item_timer_list, parent, false);
			
			ViewHolder holder = new ViewHolder();
			holder.tv_name = (TextView) convertView.findViewById(R.id.name);
			holder.tv_time = (TextView) convertView.findViewById(R.id.time);
			holder.tv_operation = (TextView) convertView.findViewById(R.id.operation);
			
			convertView.setTag(holder);
		}
		
		ViewHolder holder = (ViewHolder) convertView.getTag();
		holder.tv_name.setText(list.get(position).name);
		holder.tv_time.setText(list.get(position).time);
		holder.tv_operation.setText(list.get(position).operation);
		
		return convertView;
	}
	
	private class ViewHolder {
		public TextView tv_name;
		public TextView tv_time;
		public TextView tv_operation;
	}

}
