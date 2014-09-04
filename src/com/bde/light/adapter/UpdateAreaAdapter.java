package com.bde.light.adapter;

import java.util.ArrayList;
import com.bde.light.activity.R;
import com.bde.light.model.Area;
import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class UpdateAreaAdapter extends BaseAdapter {
	
	private ArrayList<Area> list;
	private Context context;
	
	public UpdateAreaAdapter(Context context,ArrayList<Area> list) {
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
		return list.get(position).id;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		if (convertView == null) {
			convertView = ((Activity) context).getLayoutInflater().inflate(R.layout.item_update_area_list, parent, false);
			
			ViewHolder holder = new ViewHolder();
			holder.tv_area = (TextView) convertView.findViewById(R.id.item_area);
			holder.iv_selected = (ImageView) convertView.findViewById(R.id.item_selected);
			
			convertView.setTag(holder);
		}
		ViewHolder holder = (ViewHolder) convertView.getTag();
		Area area = list.get(position);
		holder.tv_area.setText(area.area);
		holder.iv_selected.setImageResource(area.selected);
		return convertView;
	}
	
	private class ViewHolder {
		public TextView tv_area;
		public ImageView iv_selected;
	}

}
