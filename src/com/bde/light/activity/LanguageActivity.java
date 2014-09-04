/**
 * @author SiYu Lo
 */
package com.bde.light.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.bde.light.adapter.LanguageAdapter;
import com.bde.light.mgr.LightMgr;
import com.bde.light.model.Light;

public class LanguageActivity extends Activity implements OnItemClickListener, OnClickListener {
	
	public static final String LANGUAGE = "language";
	public static final String ISSELECTED = "isSelected";
	
	private ListView listView;
	private ArrayList<HashMap<String,String>> list;
	private LanguageAdapter languageAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.language_activity);
		
		listView = (ListView) findViewById(R.id.list);
		Button bt_back = (Button) findViewById(R.id.bt_back);
		TextView tv_top_title = (TextView) findViewById(R.id.top_title);
		
		bt_back.setText(R.string.menu);
		tv_top_title.setText(R.string.language_management);
		
		initData();
		
		listView.setOnItemClickListener(this);
		bt_back.setOnClickListener(this);
	}
	
	/**
	 * 初始化listView数据
	 */
	private void initData() {
		list = new ArrayList<HashMap<String,String>>();
		int[] set = new int[]{R.string.chinese,R.string.english};
		int len = set.length;
		SharedPreferences sp = getSharedPreferences(LANGUAGE, MODE_PRIVATE);
		String language = sp.getString(LANGUAGE, "中文");
		for (int i = 0; i< len; i++) {
			HashMap<String,String> map = new HashMap<String,String>();
			String lan = getString(set[i]);
			map.put(LANGUAGE, lan);
			if (lan.equals(language)) {
				map.put(ISSELECTED, R.drawable.icon_selected + "");
			}else {
				map.put(ISSELECTED, "0");
			}
			list.add(map);
		}
		languageAdapter = new LanguageAdapter(this, list);
		listView.setAdapter(languageAdapter);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		//ImageView selected = (ImageView) findViewById(R.id.selected);
		SharedPreferences sp = getSharedPreferences(LANGUAGE, MODE_PRIVATE);
		Editor edit = sp.edit();
		int len = list.size();
		for (int i = 0; i < len; i++) {
			HashMap<String,String> map = list.get(i);
			if (!map.get(ISSELECTED).equals("0")) {
				map.put(ISSELECTED, "0");
			}
		}
		String currentArea = "", orignalArea = getString(R.string.all);
		switch(position) {
		case 0:
			//selected.setBackgroundResource(R.drawable.icon_selected);
			edit.putString(LANGUAGE, "中文");
			switchLanguage(Locale.CHINESE);
			currentArea = "全部";
			break;
		case 1:
			//selected.setBackgroundResource(R.drawable.icon_selected);
			edit.putString(LANGUAGE, "English");
			switchLanguage(Locale.ENGLISH);
			currentArea = "ALL";
			break;
		}
		edit.commit();
		list.get(position).put(ISSELECTED, R.drawable.icon_selected + "");
		languageAdapter.notifyDataSetChanged();
		//finish();
		if (!currentArea.equals(orignalArea)) {
			LightMgr lightMgr = new LightMgr(this);
			ArrayList<Light> lightList = lightMgr.findAll();
			for (Light light : lightList) {
				if (light.area.equals(orignalArea)) {
					light.area = currentArea;
					lightMgr.update(light);
				}
			}
		}
		
		
		
		moveBack();
	}

	public void onClick(View v) {
		moveBack();
		
	}
	
	private void moveBack() {
		Intent language = new Intent(this,SettingActivity.class);
		startActivity(language);
		finish();
	}
	public void switchLanguage(Locale locale) {
        Configuration config = getResources().getConfiguration();// 获得设置对象
        Resources resources = getResources();// 获得res资源对象
        DisplayMetrics dm = resources.getDisplayMetrics();// 获得屏幕参数：主要是分辨率，像素等。
        config.locale = locale; // 简体中文
        resources.updateConfiguration(config, dm);
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		moveBack();
	}

	
}
