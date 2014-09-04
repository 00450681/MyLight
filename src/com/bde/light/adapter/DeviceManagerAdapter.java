package com.bde.light.adapter;

import java.util.ArrayList;
import com.bde.light.activity.R;
import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class DeviceManagerAdapter extends BaseAdapter {
	
	private Context context;
	private ArrayList<String> list;
	
	public DeviceManagerAdapter(Context context,ArrayList<String> list) {
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
			convertView = ((Activity) context).getLayoutInflater().inflate(R.layout.item_update_setting, parent, false);
			ViewHolder holder = new ViewHolder();
			holder.tv_name = (TextView) convertView.findViewById(R.id.update_option);
			/*holder.tv_open = (TextView) convertView.findViewById(R.id.open_tv);
			holder.tv_close = (TextView) convertView.findViewById(R.id.close_tv);
			holder.check_open = (ImageView) convertView.findViewById(R.id.open_check);
			holder.check_close = (ImageView) convertView.findViewById(R.id.close_check);*/
			convertView.setTag(holder);
		}
		ViewHolder holder = (ViewHolder) convertView.getTag();
		holder.tv_name.setText(list.get(position));
		return convertView;
	}
	
	private class ViewHolder {
		public TextView tv_name;
		/*public TextView tv_open;
		public TextView tv_close;
		public ImageView check_open;
		public ImageView check_close;*/
	}

}
