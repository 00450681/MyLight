package com.bde.light.activity;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bde.light.adapter.DeviceManagerAdapter;
import com.bde.light.mgr.LightMgr;
import com.bde.light.model.Light;
import com.util.SlipButton;

public class DeviceManagerActivity extends Activity implements OnItemClickListener {
	
	public static final String UPDATENAME = "update_name";
	public static final String CONTROL_NAME = "control_name";
	public static final String OPEN_SELECT = "open_select";
	public static final String CLOSE_SELECT = "close_select";
	public static final String ENABLE_ITEM = "enable_item";
	
	public static final int UPDATE_NAME_REQUEST = 0;
	public static final int CHANGE_PASSWORD_REQUEST = 1;
	
	public static final int UPDATE_NAME_RESULT = 5;
	public static final int CHANGE_PASSWORD_RESULT = 6;
	
	private ListView listView;
	private Light myLight;
	private DeviceManagerAdapter myAdapter;
	private ArrayList<String> list;
	
	private ListView controlListView;
	private DeviceControlAdapter controlAdapter;
	private ArrayList<HashMap<String, Object>> controlList;
	private SlipButton splitbutton;
	private LightMgr mgr;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.device_manager_activity);
		
		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			myLight = (Light) bundle.getSerializable(Light.LIGHT);
			
		}
		mgr = new LightMgr(this);
		myLight = mgr.findByAddress(myLight.address);
		
		listView = (ListView) findViewById(R.id.list);
		controlListView = (ListView) findViewById(R.id.list_control);
		Button bt_back = (Button) findViewById(R.id.bt_back);
		TextView top_title = (TextView) findViewById(R.id.top_title);
		
		listView.setOnItemClickListener(this);
		bt_back.setText(R.string.device_management);
		top_title.setText(R.string.setting);
		if (myLight.type == 2) {
			
		}
		if (myLight.type == 3 || myLight.type == 0x13) {
			splitbutton = (SlipButton) findViewById(R.id.splitbutton);
			splitbutton.setVisibility(View.VISIBLE);
            //splitbutton.setEnabled(false);
			TextView hint = (TextView) findViewById(R.id.control_light);
			hint.setTextColor(Color.rgb(0, 0, 0));
			boolean brightnessAble = (myLight.brightnessChangeable == 1) ? true : false;
			splitbutton.setCheck(brightnessAble);
			splitbutton.SetOnChangedListener(new SlipButton.OnChangedListener() {
				
				@Override
				public void OnChanged(boolean CheckState) {
					// TODO Auto-generated method stub
					if (myLight != null) {
						myLight.brightnessChangeable = CheckState ? 1 : 0;
					}
				}
			});
		}
		
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		initData();
	}
	
	protected void onStop() {
		super.onStop();
		HashMap<String, Object> map0 = controlList.get(0);
		if ((Boolean) map0.get(OPEN_SELECT)){
			myLight.remote_open = 1;
		} else {
			myLight.remote_open = 0;
		}
		if ((Boolean) map0.get(CLOSE_SELECT)) {
			myLight.remote_close = 1;
		} else {
			myLight.remote_close = 0;
		}
		HashMap<String, Object> map1 = controlList.get(1);
		if ((Boolean) map1.get(OPEN_SELECT)){
			myLight.close_open = 1;
		} else {
			myLight.close_open = 0;
		}
		if ((Boolean) map1.get(CLOSE_SELECT)) {
			myLight.close_close = 1;
		} else {
			myLight.close_close = 0;
		}
		HashMap<String, Object> map2 = controlList.get(2);
		if ((Boolean) map2.get(OPEN_SELECT)){
			myLight.shake_open = 1;
		} else {
			myLight.shake_open = 0;
		}
		if ((Boolean) map2.get(CLOSE_SELECT)) {
			myLight.shake_close = 1;
		} else {
			myLight.shake_close = 0;
		}
		HashMap<String, Object> map3 = controlList.get(3);
		if ((Boolean) map3.get(OPEN_SELECT)){
			myLight.timer_open = 1;
		} else {
			myLight.timer_open = 0;
		}
		if ((Boolean) map3.get(CLOSE_SELECT)) {
			myLight.timer_close = 1;
		} else {
			myLight.timer_close = 0;
		}
		mgr.update(myLight);
	}
	
	/**
	 * 初始化数据
	 */
	void initData() {
		mgr = new LightMgr(this);
		myLight = mgr.findByAddress(myLight.address);
		list = new ArrayList<String>();
		list.add(getString(R.string.update_name));
		list.add(getString(R.string.update_area));
		if (myLight.version == 0) {
			list.add(getString(R.string.update_password));
		} else {
			list.add(getString(R.string.init_password));
		}
        if (myLight.type == 4) {
			//list.add(getString(R.string.adjustmentAndRange));
        	
			list.add(getString(R.string.initialize));
		}
		myAdapter = new DeviceManagerAdapter(this,list);
		listView.setAdapter(myAdapter);
		
		String[] controls = getResources().getStringArray(R.array.control_array);
		controlList = new ArrayList<HashMap<String, Object>>();
		int len = controls.length;
		/*mgr = new LightMgr(this);
		myLight = mgr.findByAddress(myLight.address);*/
		for (int i = 0; i < len; i++) {
			HashMap<String,Object> map = new HashMap<String, Object>();
			map.put(CONTROL_NAME, controls[i]);
			/*if (i == 0 || i == 3) {
				//远程控制和定时器
				map.put(OPEN_SELECT, true);
				map.put(CLOSE_SELECT, true);
			} else if (i == 1) {
				if(myLight.close_open == 1) {
					map.put(OPEN_SELECT, true);
				} else {
					map.put(OPEN_SELECT, false);
				}
				if (myLight.close_close == 1) {
					map.put(CLOSE_SELECT, true);
				} else {
					map.put(CLOSE_SELECT, false);
				}
			} else if (i == 2){
				if(myLight.shake_open == 1) {
					map.put(OPEN_SELECT, true);
				} else {
					map.put(OPEN_SELECT, false);
				}
				if (myLight.shake_close == 1) {
					map.put(CLOSE_SELECT, true);
				} else {
					map.put(CLOSE_SELECT, false);
				}
			} else {
				map.put(OPEN_SELECT, false);
				map.put(CLOSE_SELECT, false);
			}
			if (i < 4) {
				map.put(ENABLE_ITEM, true);
			} else {
				map.put(ENABLE_ITEM, false);
			}*/
			boolean openSelected = false, closeSelected = false, enableItem = true;
			switch (i) {
			case 0:
				openSelected = (myLight.remote_open == 1);
				closeSelected = (myLight.remote_close == 1);
				break;
			case 1:
				openSelected = (myLight.close_open == 1);
				closeSelected = (myLight.close_close == 1);
				break;
			case 2:
				if (myLight.type != 2) {
					openSelected = (myLight.shake_open == 1);
					closeSelected = (myLight.shake_close == 1);
					break;
				} else {
					enableItem = false;
					myLight.shake_open = 0;
					myLight.shake_close = 0;
				}
				break;
			case 3:
				if (myLight.type != 2) {
					openSelected = (myLight.timer_open == 1);
					closeSelected = (myLight.timer_close == 1);
					break;
				} else {
					enableItem = false;
					myLight.timer_open = 0;
					myLight.timer_close = 0;
				}
				break;
			default:
				enableItem = false;
				break;
			}
			map.put(OPEN_SELECT, openSelected);
			map.put(CLOSE_SELECT, closeSelected);
			map.put(ENABLE_ITEM, enableItem);
			controlList.add(map);
		}
		controlAdapter = new DeviceControlAdapter();
		controlListView.setAdapter(controlAdapter);
	}
	
	public void bt_back(View v){
		//finish();
		/*Intent mainActivity = new Intent(this,DeviceActivity.class);
		startActivity(mainActivity);*/
		finish();
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		/*Intent mainActivity = new Intent(this,DeviceActivity.class);
		startActivity(mainActivity);*/
		finish();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		switch(position) {
		case 0:
			Intent name = new Intent(this,UpdateNameActivity.class);
			name.putExtra(Light.LIGHT, myLight);
			startActivity(name);
			break;
			
		case 1:
			Intent area = new Intent(this,UpdateAreaActivity.class);
			area.putExtra(Light.LIGHT, myLight);
			startActivity(area);
			break;
			
		case 2:
			Intent password = null;
			if (myLight.version == 0) {
				password = new Intent(this,UpdatePasswordActivity.class);
			} else {
				password = new Intent(this,InitPasswordActivity.class);
			}
			password.putExtra(Light.LIGHT, myLight);
			startActivityForResult(password, CHANGE_PASSWORD_REQUEST);
			break;
        case 3:
			/*Intent adjustment = new Intent(this,AdjustmentActivity.class);
			adjustment.putExtra(Light.LIGHT, myLight);
			startActivity(adjustment);*/
        	Intent init = new Intent(this,InitializeActivity.class);
        	init.putExtra(Light.LIGHT, myLight);
			startActivity(init);
			break;
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (data != null) {
			Light light = (Light) data.getSerializableExtra(Light.LIGHT);
			if (requestCode == CHANGE_PASSWORD_REQUEST) {
				if (light.version == 0) {
					list.set(2, getString(R.string.update_password));
				} else {
					list.set(2, getString(R.string.init_password));
				}
				myLight.version = light.version;
				mgr.update(myLight);
				myAdapter.notifyDataSetChanged();
			}
		}
	}
	
	
	/**
	 * 自定义适配器
	 */
	public class DeviceControlAdapter extends BaseAdapter {
		
		@Override
		public int getCount() {
			return controlList.size();
		}

		@Override
		public Object getItem(int position) {
			return controlList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = DeviceManagerActivity.this.getLayoutInflater().inflate(R.layout.item_device_control, parent, false);
				ViewHolder holder = new ViewHolder();
				holder.tv_control_name = (TextView) convertView.findViewById(R.id.control_name);
				holder.tv_open = (TextView) convertView.findViewById(R.id.open_tv);
				holder.tv_close = (TextView) convertView.findViewById(R.id.close_tv);
				holder.check_open = (ImageView) convertView.findViewById(R.id.open_check);
				holder.check_close = (ImageView) convertView.findViewById(R.id.close_check);
				convertView.setTag(holder);
			}
			final HashMap<String, Object> map = controlList.get(position);
			ViewHolder holder = (ViewHolder) convertView.getTag();
			holder.tv_control_name.setText(map.get(CONTROL_NAME).toString());
			//if (myLight.type == 1) {
				holder.tv_control_name.setTextColor(DeviceManagerActivity.this.getResources().getColor((Boolean) map.get(ENABLE_ITEM)? R.color.black : R.color.silver));
				holder.tv_open.setTextColor(DeviceManagerActivity.this.getResources().getColor((Boolean) map.get(ENABLE_ITEM)? R.color.black : R.color.silver));
				holder.tv_close.setTextColor(DeviceManagerActivity.this.getResources().getColor((Boolean) map.get(ENABLE_ITEM)? R.color.black : R.color.silver));
				holder.check_open.setImageResource((Boolean) map.get(OPEN_SELECT)? R.drawable.frame_with_checked : R.drawable.frame_no_check);
				holder.check_close.setImageResource((Boolean) map.get(CLOSE_SELECT)? R.drawable.frame_with_checked : R.drawable.frame_no_check);
				holder.check_open.setEnabled((Boolean) map.get(ENABLE_ITEM));
				holder.check_close.setEnabled((Boolean) map.get(ENABLE_ITEM));
				
				holder.check_open.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						map.put(OPEN_SELECT, (Boolean) map.get(OPEN_SELECT)? false : true);
						controlAdapter.notifyDataSetChanged();
					}
				});
				holder.check_close.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						map.put(CLOSE_SELECT, (Boolean) map.get(CLOSE_SELECT)? false : true);
						controlAdapter.notifyDataSetChanged();
					}
				});
			//}
			return convertView;
		}
		
		private class ViewHolder {
			public TextView tv_control_name;
			public TextView tv_open;
			public TextView tv_close;
			public ImageView check_open;
			public ImageView check_close;
		}
	}
}
