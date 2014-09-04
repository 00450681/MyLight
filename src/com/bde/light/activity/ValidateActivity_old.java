package com.bde.light.activity;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.bde.light.adapter.AreaAdapter;
import com.bde.light.mgr.AreaMgr;
import com.bde.light.mgr.LightMgr;
import com.bde.light.model.Area;
import com.bde.light.model.Light;

public class ValidateActivity_old extends Activity implements OnClickListener, OnItemClickListener {
	
	public static final int VALIDATE_RESULT = 1;

	protected static final String TAG = "ValidateActivity";
	
	private Light light;
	private EditText et_password;
	private EditText et_name;
	private EditText et_area;
	//private TextView et_area;
	private ListView areaListView;
	private ArrayList<Area> areaList;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.vallidate_dialog_old);
		
		Bundle bundle = getIntent().getExtras();
		light = (Light) bundle.getSerializable(Light.LIGHT);
		
		et_password = (EditText) findViewById(R.id.input_password);
		et_name = (EditText) findViewById(R.id.input_name);
		//et_area = (EditText) findViewById(R.id.input_area);
		//et_area = (TextView) findViewById(R.id.input_area);
		et_name.setText(light.name);
		et_area.setText(light.area);
		
		areaListView = (ListView) findViewById(R.id.arealist);
		
		Button bt_confirm = (Button) findViewById(R.id.bt_confirm);
		Button bt_cancel = (Button) findViewById(R.id.bt_cancel);
		bt_confirm.setOnClickListener(this);
		bt_cancel.setOnClickListener(this);
		
		/*et_area.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (areaListView.getVisibility() == View.GONE) {
					areaListView.setVisibility(View.VISIBLE);
					areaListView.setFocusable(true);
					areaListView.setFocusableInTouchMode(true);
					areaListView.requestFocus();
					areaListView.requestFocusFromTouch();
				}
				else {
					areaListView.setVisibility(View.GONE);
				}
			}
		});
		AreaMgr areaMgr = new AreaMgr(this);
		areaList = areaMgr.findAll();
		if (areaList == null || areaList.size() == 0) {
			Area area = new Area();
			area.area = getString(R.string.all);
			areaMgr.add(area);
			areaList = areaMgr.findAll();
		}
		AreaAdapter areaAdapter = new AreaAdapter(this, areaList, R.layout.item_area_layout);
		areaListView.setAdapter(areaAdapter);
		areaListView.setOnItemClickListener(this);*/
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch(id) {
		case R.id.bt_confirm:
			String password = et_password.getText().toString().trim();
			String name = et_name.getText().toString().trim();
			String area = et_area.getText().toString().trim();
			if (password != null && password.length() != 0) {
				light.password = password;
				light.name = name;
				light.area = area;
				Intent intent = new Intent();
				intent.putExtra(Light.LIGHT, light);
				setResult(VALIDATE_RESULT,intent);
				finish();
			} else {
				new AlertDialog.Builder(ValidateActivity_old.this)
				.setMessage(R.string.password_empty)
				.setNegativeButton(R.string.confirm, null)
				.create().show();
			}
			break;
		case R.id.bt_cancel:
			Light l = null;
			Intent intent = new Intent();
			intent.putExtra(Light.LIGHT, l);
			setResult(VALIDATE_RESULT,intent);
			finish();
			break;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub
		areaListView.setVisibility(View.GONE);
		Area area = areaList.get(position);
		et_area.setText(area.area);
	}
	
}
