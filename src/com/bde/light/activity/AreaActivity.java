package com.bde.light.activity;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bde.light.adapter.AreaAdapter;
import com.bde.light.mgr.AreaMgr;
import com.bde.light.mgr.LightMgr;
import com.bde.light.model.Area;
import com.bde.light.model.Light;
import com.bde.light.utils.MyActivityUtils;

public class AreaActivity extends Activity implements OnClickListener, OnTouchListener {
	
	private ListView listView;
	private AreaAdapter areaAdapter;
	private ArrayList<Area> areaList;
	private ArrayList<View> deleteBtnView;
	private AreaMgr areaMgr;
	private LightMgr lightMgr;
	private Context context;
	private String currentArea;
	Button btn_delete;
	float downX = 0, downY = 0, upX = 0, upY = 0;
	int p1,p2;
	Area areaToDelete;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.area_activity);
		
		context = this;
		deleteBtnView = new ArrayList<View>();
		//areaToDelete = new
		
		listView = (ListView) findViewById(R.id.list);
		Button bt_back = (Button) findViewById(R.id.bt_back);
		Button add_area = (Button) findViewById(R.id.add_area);
		Button delete_area = (Button) findViewById(R.id.delete_area);
		TextView tv_top_title = (TextView) findViewById(R.id.top_title);
		
		bt_back.setText(R.string.menu);
		tv_top_title.setText(R.string.area_management);
		
		add_area.setOnClickListener(this);
		bt_back.setOnClickListener(this);
		delete_area.setOnClickListener(this);
		
		listView.setOnTouchListener(this);
		
		areaMgr = new AreaMgr(this);
		lightMgr = new LightMgr(this);
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		initData();
	}
	
	/**
	 * 初始化areaList数据
	 */
	private void initData(){
		areaList = areaMgr.findAll();
		if (areaList == null || areaList.size() == 0) {
			Area area = new Area();
			area.area = getString(R.string.all);
			long id = areaMgr.add(area);
			if (id > 0) {
				areaList = areaMgr.findAll();
			}
		}
		Area area = new Area();
		area.area = getString(R.string.all);
		area.id = areaList.get(0).id;
		
		areaList.remove(0);
		areaList.add(0, area);
		
		areaMgr.update(area);
		if (areaAdapter == null) {
			areaAdapter = new AreaAdapter(this, areaList, R.layout.item_area_layout);
			listView.setAdapter(areaAdapter);
		} else {
			areaAdapter.notifyDataSetChanged();
		}
	}
	
	

	public void onClick(View v) {
		int id = v.getId();
		switch(id){
		//返回
		case R.id.bt_back:
			finish();
			break;
		//添加area
		case R.id.add_area:
			View view = getLayoutInflater().inflate(R.layout.input_name, null);
			final EditText nameEt = (EditText) view.findViewById(R.id.input_name);
			new AlertDialog.Builder(this)
			.setTitle(R.string.input_area)
			.setView(view)
			.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					
					String areaname = nameEt.getText().toString();
					
					if (areaname != null && areaname.length() != 0) {
						if (areaMgr.findByName(areaname) == null) {
							Area area = new Area();
							area.area = areaname;
							long result = areaMgr.add(area);
							if (result >= 0) {
								Toast.makeText(AreaActivity.this, R.string.add_success, Toast.LENGTH_SHORT).show();
								areaList.add(area);
								areaAdapter.notifyDataSetChanged();
							}
						}
						else {
							new AlertDialog.Builder(AreaActivity.this)
					    	.setMessage(R.string.areaExsit)
					    	.setNegativeButton(R.string.confirm, null)
							.show();
						}
					}
				}
			})
			.setNegativeButton(R.string.cancel, null)
			.create().show();
			break;
		//删除所有area
		case R.id.delete_area:
			final int size = areaList.size();
			if (areaList != null && size != 0) {
				new AlertDialog.Builder(this)
				.setTitle(R.string.tip)
				.setMessage(R.string.delete_or_not)
				.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
					
					public void onClick(DialogInterface dialog, int which) {
						
						//boolean isDelete = true;
						
						for (int i = 1; i < size; i++) {
							Area area = areaList.get(i);
							int result = areaMgr.delete(area);
							if (result > 0) {
								lightMgr.deleteAll();
								/*ArrayList<Light> list = lightMgr.findAll();
								for (Light light : list) {
									if (light.area.equals(area.area)) {
										light.area = getString(R.string.all);
										lightMgr.update(light);
									}
								}*/
								
							}
							/*else {
								isDelete = false;
							}*/
						}
						//if (isDelete) {
							areaList.clear();
							areaList.addAll(areaMgr.findAll());
							areaAdapter.notifyDataSetChanged();
							//initData();
						//}
					}
				})
				.setNegativeButton(R.string.cancel, null)
				.create().show();
			} else {
				MyActivityUtils.toast(this, R.string.no_area);
			}
			break;
		}
	}
	
	
	public boolean onTouch(View v, MotionEvent event) {
		
		
		
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			downX = event.getX();
			downY = event.getY();
			p1 = ((ListView) v).pointToPosition((int) downX, (int) downY);
		}
		
		if (event.getAction() == MotionEvent.ACTION_UP) {
			upX = event.getX();
			upY = event.getY();
			p2 = ((ListView) v).pointToPosition((int) upX, (int) upY);
			View view = ((ListView) v).getChildAt(p2);
			if (view == null) {
				int FirstVisiblePosition = listView
						.getFirstVisiblePosition();
				view = ((ListView) v).getChildAt(p2 - FirstVisiblePosition);
			}
			if (view != null) {
				btn_delete = (Button) view.findViewById(R.id.btn_delete);
				//TextView area = (TextView) findViewById(R.id.area_name);
				if (p1 == p2 && Math.abs(upX - downX) > 150 && p1 != 0) {
					
					if (btn_delete.getVisibility() == View.GONE) {
						for (View Btnview : deleteBtnView) {
							Btnview.findViewById(R.id.btn_delete).setVisibility(View.GONE);
						}
						deleteBtnView.clear();
						btn_delete.setVisibility(View.VISIBLE);
						deleteBtnView.add(btn_delete);
						//currentArea = (String) area.getText();
						
						/*btn_delete.setOnTouchListener(new View.OnTouchListener() {
							
							@Override
							public boolean onTouch(View v, MotionEvent event) {
								// TODO Auto-generated method stub
								if (event.getAction() == MotionEvent.ACTION_DOWN) {
									Area area = areaList.get(p1);
									int result = areaMgr.delete(area);
									if (result > 0) {
										for (View Btnview : deleteBtnView) {
											Btnview.findViewById(R.id.btn_delete).setVisibility(View.GONE);
										}
										//btn_delete.setVisibility(View.GONE);
										
										areaList.remove(p1);
										//检查light的区域有没有是这个的，有就修改为全部
										ArrayList<Light> lights = lightMgr.findAllByArea(area.area);
										for (Light light : lights) {
											light.area = getString(R.string.all);
											lightMgr.update(light);
										}
										
										areaAdapter.notifyDataSetChanged();
									}
								}
								return false;
							}
						});*/
						btn_delete.setOnTouchListener(new View.OnTouchListener() {
							
							@Override
							public boolean onTouch(View v, MotionEvent event) {
								// TODO Auto-generated method stub
								if (event.getAction() == MotionEvent.ACTION_DOWN) {
									areaToDelete = areaList.get(p1);
									ArrayList<Light> lights = lightMgr.findAllByArea(areaToDelete.area);
									if (lights.size() > 0) {
										MyActivityUtils.makeAlertDialog(context, R.string.warrningTitle,
												R.string.warrningMessage, R.string.confirm, new DialogInterface.OnClickListener() {
													
													@Override
													public void onClick(DialogInterface dialog, int which) {
														// TODO Auto-generated method stub
														int result = areaMgr.delete(areaToDelete);
														//if (result > 0) {
															areaList.remove(p1);
															//检查light的区域有没有是这个的，有就修改为全部
															lightMgr.deleteAllByArea(areaToDelete.area);					
															areaAdapter.notifyDataSetChanged();
														//}
													}
												}, R.string.cancel, new DialogInterface.OnClickListener() {
													
													@Override
													public void onClick(DialogInterface dialog, int which) {
														// TODO Auto-generated method stub
														
													}
												}).show();
									} else {
										int result = areaMgr.delete(areaToDelete);
										if (result > 0) {
											areaList.remove(p1);
											areaAdapter.notifyDataSetChanged();
										}
									}
								}
								return true;
							}
						});
						return true;
					} 
				} else if (p1 == p2 && Math.abs(upX - downX) < 10) {
					if (btn_delete.getVisibility() == View.VISIBLE) {
						btn_delete.setVisibility(View.GONE);
					}
				}
			}
			
		}
		
		return false;
	}

}
