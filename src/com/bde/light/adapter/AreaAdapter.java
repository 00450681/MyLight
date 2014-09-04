package com.bde.light.adapter;

import java.util.ArrayList;
import com.bde.light.activity.R;
import com.bde.light.model.Area;
import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

public class AreaAdapter extends BaseAdapter {
	
	private ArrayList<Area> list;
	private Context context;
	private int item_layout;
	
	public AreaAdapter(Context context,ArrayList<Area> list, int item_layout) {
		this.context = context;
		this.list = list;
		this.item_layout = item_layout;
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
		return list.get(position).id;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		if (convertView == null) {
			convertView = ((Activity) context).getLayoutInflater().inflate(item_layout, parent, false);
			
			ViewHolder holder = new ViewHolder();
			holder.nameView = (TextView) convertView.findViewById(R.id.area_name);
			holder.deleteBtn = (Button) convertView.findViewById(R.id.btn_delete);
			
			convertView.setTag(holder);
		}
		
		ViewHolder holder = (ViewHolder) convertView.getTag();
		holder.nameView.setText(list.get(position).area);
		holder.deleteBtn.setVisibility(View.GONE);
		
		return convertView;
	}
	
	private class ViewHolder {
		public TextView nameView;
		public Button deleteBtn;
	}

}
