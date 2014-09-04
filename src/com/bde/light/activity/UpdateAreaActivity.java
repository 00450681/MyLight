package com.bde.light.activity;

import java.util.ArrayList;

import com.bde.light.adapter.UpdateAreaAdapter;
import com.bde.light.mgr.AreaMgr;
import com.bde.light.mgr.LightMgr;
import com.bde.light.model.Area;
import com.bde.light.model.Light;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class UpdateAreaActivity extends Activity implements OnClickListener, OnItemClickListener {
	
	public static final String SELECTED_AREA = "selected_area";
	
	private ArrayList<Area> list;
	private UpdateAreaAdapter myAdapter;
	private Light myLight;
	private ListView listView;
	private LightMgr lightMgr;
	private AreaMgr areaMgr;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.update_area_activity);
		
		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			myLight = (Light) bundle.getSerializable(Light.LIGHT);
		}
		
		lightMgr = new LightMgr(this);
		areaMgr = new AreaMgr(this);
		
		Button bt_back = (Button) findViewById(R.id.bt_back);
		TextView tv_top_title = (TextView) findViewById(R.id.top_title);
		
		bt_back.setText(R.string.setting);
		tv_top_title.setText(R.string.area);
		
		bt_back.setOnClickListener(this);
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		init();
	}

	@Override
	public void onClick(View v) {
		finish();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		int size = list.size();
		for (int i = 0; i < size; i++) {
			Area myArea = list.get(i);
			if (myArea.selected != 0) {
				myArea.selected = 0;
			}
		}
		Area selected_Area = list.get(position);
		selected_Area.selected = R.drawable.icon_selected;
		Light light = lightMgr.findByAddress(myLight.address);
		light.area = selected_Area.area;
		int result = lightMgr.update(light);
		if (result > 0) {
			myAdapter.notifyDataSetChanged();
		}
	}
	
	public void init() {
		list = areaMgr.findAll();
		Area area = new Area();
		area.area = getString(R.string.all);
		area.id = list.get(0).id;
		
		list.remove(0);
		list.add(0, area);
		
		areaMgr.update(area);
		
		myAdapter = new UpdateAreaAdapter(this,list);
		listView = (ListView) findViewById(R.id.list);
		listView.setAdapter(myAdapter);
		listView.setOnItemClickListener(this);
		
		int size = list.size();
		Light light = lightMgr.findByAddress(myLight.address);
		for (int i = 0; i < size; i++) {
			Area myArea = list.get(i);
			if (myArea.area.equals(light.area)) {
				myArea.selected = R.drawable.icon_selected;
			}
		}
	}

}
