package com.bde.light.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import com.bde.light.activity.LanguageActivity;
import com.bde.light.activity.R;
import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class LanguageAdapter extends BaseAdapter {
	
	private ArrayList<HashMap<String,String>> list;
	private Context context;
	
	public LanguageAdapter(Context context,ArrayList<HashMap<String,String>> list) {
		this.context = context;
		this.list = list;
	}
	
	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public HashMap<String,String> getItem(int position) {
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		if (convertView == null) {
			convertView = ((Activity) context).getLayoutInflater().inflate(R.layout.item_language_layout, parent, false);
			
			ViewHolder holder = new ViewHolder();
			holder.nameView = (TextView) convertView.findViewById(R.id.language);
			holder.selectedView = (ImageView) convertView.findViewById(R.id.selected);
			
			convertView.setTag(holder);
		}
		
		ViewHolder holder = (ViewHolder) convertView.getTag();
		HashMap<String,String> map = getItem(position);
		String lan = map.get(LanguageActivity.LANGUAGE).toString();
		holder.nameView.setText(lan);
		holder.selectedView.setBackgroundResource(Integer.valueOf(map.get(LanguageActivity.ISSELECTED)));
		
		return convertView;
	}
	
	private class ViewHolder {
		public TextView nameView;
		public ImageView selectedView;
	}

}
