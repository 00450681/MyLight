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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.bde.light.adapter.AreaAdapter;
import com.bde.light.mgr.AreaMgr;
import com.bde.light.mgr.LightMgr;
import com.bde.light.model.Area;
import com.bde.light.model.Light;

public class ValidateActivity extends Activity implements OnClickListener, OnItemClickListener {
	
	public static final int VALIDATE_RESULT = 1;

	protected static final String TAG = "ValidateActivity";
	
	private Light light;
	private EditText et_password;
	private EditText et_name;
	private EditText et_area;
	//private TextView et_area;
	private ListView areaListView;
	private ArrayList<Area> areaList;
	
	private Spinner mInputArea;
	private ArrayAdapter<String> mInputAreaAdapter;
	private ArrayList<String> mAllArea;
	private String mValidateArea;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.vallidate_dialog_with_spinner);
		
		mAllArea = new ArrayList<String>();
		mInputArea = (Spinner) findViewById(R.id.input_area);
		Bundle bundle = getIntent().getExtras();
		light = (Light) bundle.getSerializable(Light.LIGHT);
		
		et_password = (EditText) findViewById(R.id.input_password);
		et_name = (EditText) findViewById(R.id.input_name);
		//et_area = (EditText) findViewById(R.id.input_area);
		//et_area = (TextView) findViewById(R.id.input_area);
		et_name.setText(light.name);
		//et_area.setText(light.area);
		
		areaListView = (ListView) findViewById(R.id.arealist);
		
		Button bt_confirm = (Button) findViewById(R.id.bt_confirm);
		Button bt_cancel = (Button) findViewById(R.id.bt_cancel);
		bt_confirm.setOnClickListener(this);
		bt_cancel.setOnClickListener(this);
		
		AreaMgr areaMgr = new AreaMgr(this);
		areaList = areaMgr.findAll();
		for (Area area : areaList) {
			mAllArea.add(area.area);
		}
		mInputAreaAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, mAllArea);
		mInputAreaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mInputArea.setAdapter(mInputAreaAdapter);
		mInputArea.setSelection(0);
		mValidateArea = getString(R.string.all);
		mInputArea.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				mValidateArea = areaList.get(position).area;
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub
				
			}
			
		});
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
			//String area = et_area.getText().toString().trim();
			String area = mValidateArea;
			if (password != null && password.length() != 0) {
				if (area.equals(getString(R.string.all))) {
					new AlertDialog.Builder(ValidateActivity.this)
					.setTitle(R.string.invalid_area)
					.setMessage(R.string.how_to_add_area)
					.setNegativeButton(R.string.confirm, null)
					.create().show();
					return;
				}
				light.password = password;
				light.name = name;
				light.area = area;
				Intent intent = new Intent();
				intent.putExtra(Light.LIGHT, light);
				setResult(VALIDATE_RESULT,intent);
				finish();
			} else {
				new AlertDialog.Builder(ValidateActivity.this)
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
