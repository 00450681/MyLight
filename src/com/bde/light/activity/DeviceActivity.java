package com.bde.light.activity;

import java.util.ArrayList;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.bde.light.adapter.DeviceAdapter;
import com.bde.light.adapter.DeviceSettingAdapter;
import com.bde.light.mgr.LightMgr;
import com.bde.light.model.Light;
import com.bde.light.service.BleService;

public class DeviceActivity extends Activity implements OnClickListener, OnItemClickListener {
	
	private ListView listView;
	private ArrayList<Light> myList;
	private BleService mService;
	private ServiceConnection onService;
	private DeviceSettingAdapter myAdapter;
	private LightMgr lightMgr;
	public final static String DEVICE_LIST = "device_list";
	private float upX, upY, downX, downY;
	private int p1, p2;
	
	private ArrayList<Light> NumberList = new ArrayList<Light>();
	private ArrayList<Light> EnglishList = new ArrayList<Light>();
	private ArrayList<Light> ChineseList = new ArrayList<Light>();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.device_activity);
		
		onService = new ServiceConnection() {
            public void onServiceConnected(ComponentName className, IBinder rawBinder) {
                mService = ((BleService.LocalBinder) rawBinder).getService();
                if (mService != null) {
                	mService.scan(true);
                    mService.setDeviceListHandler(mHandler);
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {

						@Override
						public void run() {
							// TODO Auto-generated method stub
							mService.scan(true);
						}
                    	
                    }, 500);
                }
            }
            
            public void onServiceDisconnected(ComponentName classname) {
                mService = null;
            }
        };
        startService(new Intent(this, BleService.class));
        Intent bindIntent = new Intent(this, BleService.class);
        bindService(bindIntent, onService, Context.BIND_AUTO_CREATE);
		
        lightMgr = new LightMgr(this);
        
        listView = (ListView) findViewById(R.id.list);
		Button bt_back = (Button) findViewById(R.id.bt_back);
		TextView tv_top_title = (TextView) findViewById(R.id.top_title);
		
		bt_back.setText(R.string.menu);
		tv_top_title.setText(R.string.device_management);
		
		
		bt_back.setOnClickListener(this);
		listView.setOnItemClickListener(this);
		/*listView.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					downX = event.getX();
					downY = event.getY();
					p1 = ((ListView) v).pointToPosition((int) event.getX(), (int) event.getY());
					//View view = ((ListView) v).getChildAt(position);
					
					//}
				}
				if (event.getAction() == MotionEvent.ACTION_UP) {
					upX = event.getX();
					upY = event.getY();
					p2 = ((ListView) v).pointToPosition((int) event.getX(), (int) event.getY());
					if (p1 == p2) {
						View view = null;
						if (view == null) {
							int FirstVisiblePosition = listView.getFirstVisiblePosition();
							view = ((ListView) v).getChildAt(p1 - FirstVisiblePosition);
						}
						//if (view != null) {
						Light light = myList.get(p1);
						Intent intent = new Intent(DeviceActivity.this,DeviceManagerActivity.class);
						intent.putExtra(Light.LIGHT, light);
						startActivity(intent);
						finish();
					}
				}
				return false;
			}
		});*/
		
	}
	
	@Override
    public void onStart() {
        super.onStart();
        /*if (myList != null && myList.size() != 0) {
        	myList.clear();
        }
		myList = lightMgr.findAll();
		if (myList != null) {
			myAdapter = new DeviceAdapter(this,myList);
			listView.setAdapter(myAdapter);
		}
        if (mService != null) {
        	mService.scan(true);
        }*/
        
    }

	
    @Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if (isReturnFromDMActivity) {
			mService.scan(true);
			return;
		}
		if (myList != null && myList.size() != 0) {
        	myList.clear();
        }
		myList = lightMgr.findAll();
		if (myList != null) {
			myAdapter = new DeviceSettingAdapter(this,myList);
			listView.setAdapter(myAdapter);
		}
		if (myList.size() == 0) {
			findViewById(R.id.no_device).setVisibility(View.VISIBLE);
		}
        if (mService != null) {
        	mService.scan(true);
        }
        NumberList.clear();
        EnglishList.clear();
        ChineseList.clear();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		if (mService != null) {
			mService.scan(false);
		}
	}

	@Override
    public void onStop() {
        super.onStop();
        //mService.scan(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(onService);
        finish();
    }

	public void onClick(View v) {
		finish();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Light light = myList.get(position);
		Intent intent = new Intent(this,DeviceManagerActivity.class);
		intent.putExtra(Light.LIGHT, light);
		startActivityForResult(intent, 1);
		//finish();
	}
	
	private boolean isReturnFromDMActivity = false;
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		isReturnFromDMActivity = true;
	}


	private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
        	Bundle data = msg.getData();
        	final Light light = (Light) data.getSerializable(Light.LIGHT);
    		//findViewById(R.id.no_device).setVisibility(View.GONE);
            switch (msg.what) {
            case BleService.GATT_DEVICE_FOUND_MSG:
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                    	int size = myList.size();
                    	boolean isChanged = false;
                    	if (myList != null && size != 0) {
                    		Light lightToDelete = null;
                    		for(Light l : myList) {
                    			if (l.address.equals(light.address) && l.signal == R.drawable.no_signal) {
                    				isChanged = true;
                    				l.name = light.name;
                    				l.picture = light.picture;
                    				l.type = light.type;
                    				l.state = light.state;
                    				l.signal = light.signal;
                    				l.version = light.version;
                    				lightToDelete = l;
                    				break;
                    			}
                    		}
                    		if (isChanged) {
                    			myList.remove(lightToDelete);
                    			addDeviceToScreen(lightToDelete);
                    			myAdapter.notifyDataSetChanged();
                    		}
                    		
                    	}
                    }
                });
                break;
            }
        }
	};

	private void addDeviceToScreen(Light light) {
		// TODO Auto-generated method stub
		// 进行数字、英文、中文的排序
		boolean isNumber = isNumber(light.name);
		boolean isEnglish = isEnglesh(light.name);
		int index = 0;
		boolean isLargest = true;
		if (isNumber) {
			for (Light numberLight : NumberList) {
				if (Integer.parseInt(light.name) < Integer
						.parseInt(numberLight.name)) {
					index = NumberList.indexOf(numberLight);
					isLargest = false;
					break;
				}
			}
			if (isLargest) {
				index = NumberList.size();
			}
			if (index >= 0) {

				myList.add(index, light);
				NumberList.add(index, light);
				System.out.println("插入到NumberList中...位置为：" + index);
				System.out.println("插入到lightList中...位置为：" + index);

			} else {
				System.out.println("Number Index is below 0...");
			}
		} else if (isEnglish) {
			for (Light englishLight : EnglishList) {
				if (light.name.compareTo(englishLight.name) < 0) {
					index = EnglishList.indexOf(englishLight);
					isLargest = false;
					break;
				}
			}
			if (isLargest) {
				index = EnglishList.size();
			}
			if (index >= 0) {

				myList.add(NumberList.size() + index, light);
				EnglishList.add(index, light);
				System.out.println("插入到EnglishList中...位置为：" + index);
				System.out.println("插入到lightList中...位置为："
						+ (NumberList.size() + index));
			} else {
				System.out.println("English Index is below 0...");
			}
		} else {
			// 包含中文
			System.out.println("插入到ChineseList中...");
			System.out.println("插入到lightList中..."
					+ (NumberList.size() + EnglishList.size() + ChineseList
							.size()));
			myList.add(
					NumberList.size() + EnglishList.size() + ChineseList.size(),
					light);
			ChineseList.add(light);
		}
	}

	private boolean isEnglesh(String name) {
		// TODO Auto-generated method stub
		char temp;
		for (int i = 0; i < name.length(); i++) {
			temp = name.charAt(i);
			if (!((temp >= 'A' && temp <= 'Z') || (temp >= 'a' && temp <= 'z'))) {
				if (!(temp >= '0' && temp <= '9'))
					return false;
			}
		}
		return true;
	}

	private boolean isNumber(String name) {
		// TODO Auto-generated method stub
		try {
			Integer.parseInt(name);
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	/**
	 * 在list中查找light，如果找到，则返回list中找到的项；如果没有找到，则返回null
	 * 
	 * @return 把找到的灯返回。如果找到，则返回list中找到的项；如果没有找到，则返回null
	 * @param list
	 *            要在其中查找的容器
	 * @param light
	 *            要查找的light
	 */
	synchronized Light findLight(ArrayList<Light> list, Light light) {
		Light result = null;
		if (light == null)
			return result;
		if (list != null && list.size() != 0) {
			for (Light l : list) {
				if (l.address.equals(light.address)) {
					// 与之前验证过的已经存储了的灯是同一个设备
					// 这里是检查搜索到的设备是否已经验证过了
					result = l;
					break;
				}
			}
		}
		return result;
	}
}
/*public class DeviceActivity extends Activity implements OnItemClickListener, OnClickListener{

	private ListView listView;
	private ArrayList<Light> myList;
	private DeviceSettingAdapter myAdapter;
	private LightMgr lightMgr;
	public final static String DEVICE_LIST = "device_list";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.device_activity);
		
		listView = (ListView) findViewById(R.id.list);
		Button bt_back = (Button) findViewById(R.id.bt_back);
		TextView tv_top_title = (TextView) findViewById(R.id.top_title);
		
		lightMgr = new LightMgr(this);
		
		bt_back.setText(R.string.menu);
		tv_top_title.setText(R.string.device_management);
		
		
		bt_back.setOnClickListener(this);
		listView.setOnItemClickListener(this);
	}
	
	
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		
		if (myList != null && myList.size() != 0) {
        	myList.clear();
        }
		ArrayList<Light> noSignal = new ArrayList<Light>();
		myList = lightMgr.findAll();
		for (Light l : myList) {
			l.signal = l.lastSignal;
			if (l.signal == R.drawable.no_signal) {
				noSignal.add(l);
			}
		}
		if (noSignal.size() > 0) {
			myList.removeAll(noSignal);
			myList.addAll(myList.size(), noSignal);
		}
		Intent intent = getIntent();
		myList = (ArrayList<Light>) intent.getSerializableExtra(DeviceActivity.DEVICE_LIST);
		if (myList != null) {
			myAdapter = new DeviceSettingAdapter(this,myList);
			listView.setAdapter(myAdapter);
		}
		if (myList.size() == 0) {
			listView.setVisibility(View.GONE);
			findViewById(R.id.no_device).setVisibility(View.VISIBLE);
		}
	}


	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
    public void onStop() {
        super.onStop();
        //mService.scan(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        finish();
    }
    
	public void onClick(View v) {
		finish();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Light light = myList.get(position);
		Intent intent = new Intent(this,DeviceManagerActivity.class);
		intent.putExtra(Light.LIGHT, light);
		//intent.putExtra(DEVICE_LIST, myList);
		startActivity(intent);
		finish();
	}
}*/