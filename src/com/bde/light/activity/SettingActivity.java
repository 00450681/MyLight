package com.bde.light.activity;

import java.util.ArrayList;

import com.bde.light.adapter.SettingAdapter;
import com.bde.light.myview.DragDropListView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class SettingActivity extends Activity implements OnItemClickListener, OnClickListener {
	
	private ListView listView;
	private Intent mIntent;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setting_activity);
		
		listView = (ListView) findViewById(R.id.list);
		Button bt_back = (Button) findViewById(R.id.bt_back);
		TextView tv_top_title = (TextView) findViewById(R.id.top_title);
		
		bt_back.setText(R.string.home);
		tv_top_title.setText(R.string.menu);
		mIntent = getIntent(); 
		initData();
		
		listView.setOnItemClickListener(this);
		bt_back.setOnClickListener(this);
	}
	
	/**
	 * 初始化listView数据
	 */
	private void initData() {
		ArrayList<String> list = new ArrayList<String>();
		int[] set = new int[]{R.string.area_manager,R.string.device_manager,R.string.language_manager};
		int len = set.length;
		for (int i = 0; i< len; i++) {
			list.add(getString(set[i]));
		}
		SettingAdapter settingAdapter = new SettingAdapter(this, list);
		listView.setAdapter(settingAdapter);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		switch(position) {
		case 0:
			Intent area = new Intent(this,AreaActivity.class);
			startActivity(area);
			break;
		case 1:
			Intent device = new Intent(this,DeviceActivity.class);
			device.putExtra(DeviceActivity.DEVICE_LIST, mIntent.getSerializableExtra(DeviceActivity.DEVICE_LIST));
			startActivity(device);
			break;
		case 2:
			Intent language = new Intent(this,LanguageActivity.class);
			startActivity(language);
			finish();
			break;
		}
	}

	public void onClick(View v) {
		Intent mainActivity = new Intent(this,MainActivity.class);
		startActivity(mainActivity);
		finish();
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		Intent mainActivity = new Intent(this,MainActivity.class);
		startActivity(mainActivity);
		finish();
	}
	
	

}
