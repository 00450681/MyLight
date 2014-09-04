package com.bde.light.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Queue;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.bde.light.adapter.AreaAdapter;
import com.bde.light.adapter.DeviceAdapter;
import com.bde.light.mgr.AreaMgr;
import com.bde.light.mgr.LightMgr;
import com.bde.light.mgr.TimerMgr;
import com.bde.light.model.Area;
import com.bde.light.model.Light;
import com.bde.light.model.Timer;
import com.bde.light.service.BleService;
import com.bde.light.utils.MyActivityUtils;

public class MainActivity extends ListActivity implements SensorEventListener,
		OnTouchListener, OnClickListener, OnItemClickListener {

	public static final String TAG = "MainActivity";
	
	public static final int VALIDATE_REQUEST = 1;

	public static final int REFRESH_LIST = 0;

	private GridView gridView;

	private BluetoothAdapter mBluetoothAdapter;
	private DeviceAdapter deviceAdapter;
	private BleService mService;
	private ServiceConnection onService;
	private SensorManager mSensorManager;
	private Sensor mSensor;
	private RelativeLayout /*dialog,*/ brightnessControl;
	private ProgressDialog dialog;
	private Button cancle, mOpenBtn, mCloseBtn;
	private AlertDialog brightnessControlDialog;
	private AlertDialog windowControlDialog;
	private TextView noDevice;

	//private int signalChangeTimes = 0;

	private Light myLight;
	/** {已经验证的存储在本地数据库中的所有灯} */
	private ArrayList<Light> myList;
	
	private ArrayList<Light> allMyList;
	/** 显示在主界面的搜索到的所有灯 */
	private ArrayList<Light> lightList;
	/** 将要打开或关闭的灯 */
	private ArrayList<Light> onList;
	/** 设置了晃动开关的灯 */
	private ArrayList<Light> sensorLights;
	/** 设置了晃动开关而且是现在搜索到的灯 */
	private ArrayList<Light> shakeList;
	private ArrayList<Light> shakeCopyList;
	/** 以前到现在都设置了接近开关的灯 */
	private ArrayList<Light> closeList;
	private ArrayList<Light> closeOKList;
	private ListView deviceListView;

	private volatile ArrayList<Light> EnglishList;
	private volatile ArrayList<Light> NumberList;
	private volatile ArrayList<Light> ChineseList;
	private volatile ArrayList<Light> NoSignalList;
	
	private LinkedList<Integer> rssiQueue;

	private ArrayList<Light> LightToDeleteList;

	/** 区域列表 */
	private ArrayList<Area> areaList;
	private AreaAdapter areaAdapter;
	private ListView areaListView;
	private LightMgr lightMgr;
	private AreaMgr areaMgr;

	private String areaSelected;

	private ImageView arrows;
	private TextView tv_title;
	private long lastUpdate, lastShakeTime = 0;
	private float x, y, z, last_x = 0, last_y = 0, last_z = 0;
	private static final int SHAKE_THRESHOLD = 500;
	private static final int CHECK_DEVICE_IS_CLOSED = 501;
	private Queue onQueue = new LinkedList();
	private ImageView btn_brightness;
	private SeekBar brightness;
	
	private int hasOperationPerTenSecond = 0;

	Button btn_delete;
	int sleepTime = 0;
	ImageView iv_signal;
	checkDeviceIsCloseThread thread = new checkDeviceIsCloseThread();

	float downX = 0, downY = 0, upX = 0, upY = 0;
	int p1, p2;

	int numberStart = -1, englishStart = -1, chineseStart = -1;
	int numberEnd = -1, englishEnd = -1, chineseEnd = -1;


	private ArrayList<Light> onListCopy;
	// int repeatTimes = 5;
	int currentRepeatTimes;
	//int currentLight = 0;

	private boolean isOperating = false;
	
	private boolean isCloseOperationDone = true;
	
	private boolean isAllOnOrOffOperationDone = true;
	
	private boolean isShakeOperationDone = true;

	private static final int TIMEOUT = 50;
	private static final int SUCCESS = 51;
	private static final int ON = 1;
	private static final int OFF = 0;
	private int onOrOff;

	private ArrayList<View> deleteBtnView;

	private Handler checkHandler;
	boolean isRunning = false;
	int brightnesses = 0;
	boolean isOperationDone = true;
	// 每0.1秒扫描一次

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			Bundle data = msg.getData();
			final Light light = (Light) data.getSerializable(Light.LIGHT);

			switch (msg.what) {
			case BleService.GATT_DEVICE_FOUND_MSG:
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						addDeviceToScreen(light);
						// 通知Activity更新listview
						//deviceAdapter.notifyDataSetChanged();
						// 处理设置了接近开关的情况，能够设置接近开关的，全部都是已经验证过的，所以不需要再看是否验证了
						if (closeList != null && closeList.size() != 0) {
							for (Light l : closeList) {
								if (l.address.equals(light.address)) {
									// 与现在显示的设备是同一个远程设备
									if (light.distance == Light.FAR)
										l.closable = true;
									if (isCloseOperationDone
											&& l.distance == Light.NEAR
											&& light.distance == Light.CLOSE && l.closable
											&& ((l.close_close == 1 && light.state == 1)
													|| (l.close_open == 1 && light.state == 0))) {
										Log.i("close", "l.name = " + l.name);
										Log.i("close", "l.distance = " + l.distance);
										Log.i("close", "light.distance = " + light.distance);
										Log.i("close", "l.closable = " + l.closable);
										l.distance = Light.CLOSE;
										isCloseOperationDone = false;
										//l.oldState = l.state;
										l.state = light.state;
										l.closable = false;
										
										if (light.type == 1 || light.type == 0x11 || light.type == 0x13) {

											//turnOnOrOff(light);
											turnOnOrOff(l);
											sleepTime = 1000;
										} else if (light.type == 2) {
											// onList.clear();
											openLock(l);
											sleepTime = 3500;
										} else if (light.type == 3) {
											turnOnOrOff(l);
											//turnOnOrOffLED(light);
											sleepTime = 1000;
										}
										/*new Thread(new Runnable() {

											@Override
											public void run() {
												// TODO Auto-generated method stub
												try {
													Thread.sleep(sleepTime);
													if (!isCloseOperationDone) {
														//mService.scan(true);
														isCloseOperationDone = true;
													}
												} catch (InterruptedException e) {
													// TODO Auto-generated catch block
													e.printStackTrace();
												}
											}
										}).start();*/
									/*if (light.rssi >= -36) { //远程设备与设备靠得足够近 
										  new Thread(new Runnable() {
										  
										  @Override public void run() { try { if
										  (light.type == 1) {
										  mService.setOperation(BleService.CLOSE);
										  mService
										  .connect(mBluetoothAdapter.getRemoteDevice
										  (light.address), false); turnOnOrOff(light);
										  } else if (light.type == 2) {
										  onList.clear(); openLock(light); } else if
										  (light.type == 3) { turnOnOrOff(light); }
										  
										  //睡眠2秒，免得接近停留的那段时间灯不停地亮灭
										  Thread.sleep(2000);
										  } catch (InterruptedException e) {
										  e.printStackTrace(); } } }).start();*/
									}
									else if ((!isCloseOperationDone)
											&& l.distance == Light.NEAR
											&& light.distance == Light.CLOSE && l.closable){
										break;
									}
									System.out.println("灯" + l.name
											+ "原本的distance为：" + l.distance);
									l.distance = light.distance;
									//l.closable = light.closable;
									System.out.println("灯" + l.name
											+ "现在的distance为：" + l.distance);
									break;
								}
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
		
		
		Light l = findLight(lightList, light);

		
		/*System.out.println("Light " + light.name +
				"进入了addDeviceToScreen......l is " + l);
		System.out.println("findLight(allMyList, light) is " + findLight(allMyList, light));*/
		if (l != null) {

			boolean change = false;
			change = l.signal != light.signal || !l.name.equals(light.name) || !l.area.equals(light.area) || l.picture != light.picture
					|| l.version != light.version;
			//change = l.signal != light.signal || l.name == light.name || l.area == light.area || l.picture != light.picture;
			l.state = light.state;
			l.name = light.name;
			l.picture = light.picture;
			l.type = light.type;
			//l.signal = light.signal;
			l.brightness = light.brightness;
			//l.version = light.version;
			l.rssi = light.rssi;
			l.brightness = light.brightness;
			l.searchTimes++;
			if (!(l.name.equals(light.name)) || l.version != light.version) {
				Light lightToUpdate = findLight(myList, l);
				if (lightToUpdate != null) {
					lightToUpdate.name = l.name;
					lightToUpdate.version = light.version;
					lightMgr.update(lightToUpdate);
				}
			}
			
			if (l.signal == R.drawable.no_signal) {
				//l.signal = light.signal;
				synchronized (lightList) {
					lightList.remove(l);
					NoSignalList.remove(l);
				}
				
				
				//deviceAdapter.notifyDataSetChanged();
				// System.out.println(l.name + "原本是无信号的，删除....");
				
				//return;
			} else {
				l.signal = light.signal;
				if (change) {
					Log.i(TAG, "UpDating View");
					deviceAdapter.updateView(lightList.indexOf(l));
					
					// 如果密码版本改变，需要重新输入
					/*if (l.version != light.version) {
						l.version = light.version;
						Intent intent = new Intent(MainActivity.this,
								ValidateActivity_old.class);
						myLight = l;
						intent.putExtra(Light.LIGHT, myLight);
						startActivityForResult(intent, VALIDATE_REQUEST);
					}*/
				}
				// 之前就已经搜索过的设备，已经显示在界面上了的，现在只是更改一下状态，不需要再排序了
				/*boolean change = false;
				change = l.signal != light.signal || l.name != light.name || l.area != light.area || l.picture != light.picture;
				l.state = light.state;
				l.name = light.name;
				l.picture = light.picture;
				l.type = light.type;
				l.signal = light.signal;
				l.version = light.version;
				l.rssi = light.rssi;
				l.searchTimes++;
				if (change) {
					deviceAdapter.updateView(lightList.indexOf(l));
				}*/
				
				return;
			}

		}
		
		// 先验证是否已经验证
		if (!(areaSelected.equals(getString(R.string.all))) && findLight(findByArea(areaSelected), light) != null) {
			Light validateLightWitArea = findLight(myList, light);
			if (validateLightWitArea != null) {
				if (!(light.area.equals(getString(R.string.all)))
						&& !(tv_title.getText().toString().equals(light.area))) {
					return;
				}
				System.out.println("已经验证的灯");
				System.out.println("叫：" + validateLightWitArea.name);
				System.out.println("插入前NumberList的size是：" + NumberList.size());
				System.out
						.println("插入前EnglishList的size是：" + EnglishList.size());
				System.out
						.println("插入前ChineseList的size是：" + ChineseList.size());
				// 已经验证了的灯，且没有显示在界面上
				light.isValidate = validateLightWitArea.isValidate;
				light.isFound = 0;
				light.area = validateLightWitArea.area;
				light.brightnessChangeable = validateLightWitArea.brightnessChangeable;
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
						synchronized (lightList) {
							lightList.add(index, light);
							NumberList.add(index, light);
							System.out.println("插入到NumberList中...位置为：" + index);
							System.out.println("插入到lightList中...位置为：" + index);
						}
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

						synchronized (lightList) {
							lightList.add(NumberList.size() + index, light);
							EnglishList.add(index, light);
							System.out.println("插入到EnglishList中...位置为：" + index);
							System.out.println("插入到lightList中...位置为："
									+ (NumberList.size() + index));
						}
						
					} else {
						System.out.println("English Index is below 0...");
					}
				} else {
					// 包含中文
					synchronized (lightList) {
						System.out.println("插入到ChineseList中...");
						System.out
								.println("插入到lightList中..."
										+ (NumberList.size() + EnglishList.size() + ChineseList
												.size()));
						lightList.add(NumberList.size() + EnglishList.size()
								+ ChineseList.size(), light);
						ChineseList.add(light);
					}
					if (lightList.size() == 1) {
						noDevice.setVisibility(View.GONE);
						deviceListView.setVisibility(View.VISIBLE);
					}
				}
			} else {
				// 已经验证了，不过不是这个区域的，就不显示了
			}
		} else if (findLight(allMyList, light) == null && areaSelected.equals(getString(R.string.all))) /*if (tv_title.getText().toString().equals(light.area))*/ {
			// 未验证的灯，且之前没有搜索过而且现在没有显示在界面上
			// 之前没有搜索过，也没有显示过在界面上，需要排序
			// 灯的区域与选择的区域一样，可以显示，直接排在最后面
			
			// 灯未验证只能出现在ALL区域中。
			System.out.println("未验证的灯");
			synchronized (lightList) {
				lightList.add(light);
			}
			if (lightList.size() == 1) {
				noDevice.setVisibility(View.GONE);
				deviceListView.setVisibility(View.VISIBLE);
			}
		}
		Log.i("DeviceAdapter", "addToScreen()");
		
		deviceAdapter.notifyDataSetChanged();
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


	boolean isValidating = false;
	private Handler mActivityHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			Bundle data = msg.getData();
			// final Light light = (Light)
			// data.getSerializable(BleService.LIGHT);
			isOperating = false;
			isOperationDone = true;
			isCloseOperationDone = true;
			//mService.scan(true);
			startScanAndCheck(true);
			Light l = findLight(myList, mService.getMyLight());
			if (l != null) {
				l.isOperationDone = true;
			}
			switch (msg.what) {
			case BleService.VALIDDATE:

				isValidating = true;
				int result = data.getInt(BleService.RESULT);
				dialog.dismiss();
				if (result == 0) {
					Light light = mService.getMyLight();
					if (light != null)
						l = findLight(lightList, light);
					System.out.println("after validate...");
					System.out.println("light != null " + light != null);
					System.out.println("l != null " + l!=null);
					System.out.println("!(light.name.equals(l.name)) " + !(light.name.equals(l.name)));
					System.out.println("light.name = " + light.name + " l.name = " + l.name);
					if (light != null && l != null && !(light.name.equals(l.name))) {
						System.out.println("after validate...update name...");
						mService.updateName(light.name);
					}
					else {
						//myLight.name = light.name;
						Message updateMsg = Message.obtain(mActivityHandler, BleService.UPDATE_NAME);
						updateMsg.sendToTarget();
					}
					// 验证成功
					/*myLight.isValidate = 1;
					myLight.isFound = 0;
					myLight.oldState = myLight.state;
					Light light = mService.getMyLight();
					myLight.area = light.area;
					long a = lightMgr.add(myLight);

					if (a > 0) {
						// 将其添加到已验证的列表
						mService.updateName(light.name);
						allMyList.add(myLight);
						if (myLight.area.equals(areaSelected) || areaSelected.equals(getString(R.string.all))) {
							myList.add(myLight);
						}
						synchronized (lightList) {
							lightList.remove(myLight);
							deviceAdapter.notifyDataSetChanged();
						}
						MyActivityUtils.toast(MainActivity.this,
								R.string.validate_success);
					} else {
						lightMgr.delete(myLight.address);
						myLight.area = getString(R.string.all);
						new AlertDialog.Builder(MainActivity.this)
								.setMessage(R.string.validate_failed)
								.setNegativeButton(R.string.confirm, null)
								.create().show();
					}*/
				} else if (result == 1) {
					// 验证失败
					myLight.isValidate = 0;
					myLight.area = getString(R.string.all);
					new AlertDialog.Builder(MainActivity.this)
							.setMessage(R.string.validate_failed)
							.setNegativeButton(R.string.confirm, null).create()
							.show();
				} else {
					myLight.area = getString(R.string.all);
					MyActivityUtils.toast(MainActivity.this, R.string.timeout);
				}
				// 验证完了，可以断开连接了
				
				// mService.scan(true);
				break;
			case BleService.UPDATE_NAME:
				/*MyActivityUtils.toast(MainActivity.this,
						R.string.validate_success);*/
				if (isValidating) {
					myLight.isValidate = 1;
					myLight.isFound = 0;
					myLight.oldState = myLight.state;
					
					Light light = mService.getMyLight();
					myLight.name = light.name;
					myLight.area = light.area;
					long a = lightMgr.add(myLight);

					if (a > 0) {
						// 将其添加到已验证的列表
						allMyList.add(myLight);
						if (myLight.area.equals(areaSelected) || areaSelected.equals(getString(R.string.all))) {
							myList.add(myLight);
						}
						synchronized (lightList) {
							lightList.remove(myLight);
							if (lightList.size() == 0) {
								noDevice.setVisibility(View.VISIBLE);
							}
							deviceAdapter.notifyDataSetChanged();
						}
						MyActivityUtils.toast(MainActivity.this,
								R.string.validate_success);
					} else {
						lightMgr.delete(myLight.address);
						myLight.area = getString(R.string.all);
						new AlertDialog.Builder(MainActivity.this)
								.setMessage(R.string.validate_failed)
								.setNegativeButton(R.string.confirm, null)
								.create().show();
					}
				}
				mService.disconnect();
				break;
			case BleService.SHAKE:
				/*new Thread(new Runnable() {
					public void run() {
						try {
							Thread.sleep(100);
							sendLeftShake(BleService.SHAKE);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}).start();*/
				//isOperating = false;
				//isShakeOperationDone = true;
				Log.v(TAG, "Success once...");
				// mService.scan(true);
				// onList.remove(0);
				if (shakeList.size() > 0) {
					shakeList.remove(0);
					if (shakeList.size() > 0) {
						/*new Thread(new Runnable() {
							public void run() {
								try {
									Thread.sleep(100);
									shakeOpenOrClose(BleService.SHAKE);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
							}
						}).start();*/
						shakeOpenOrClose(BleService.SHAKE);
					} else {

						// onList.clear();
						Log.v(TAG,
								"Turn on or off completed! Now check if some failed...");
						new Thread(new Runnable() {

							@Override
							public void run() {
								// TODO Auto-generated method stub
								// 检查还有没有没亮的，如果有，再做一遍
								try {
									Thread.sleep(2300);
									System.out
											.println("Turn on or off completed! Now check if some failed...");
									// onList.clear();
									if (lightList != null
											&& lightList.size() != 0 && shakeCopyList != null && shakeCopyList.size() != 0) {
										for (Light l : shakeCopyList) {
											Light light = findLight(lightList, l);
											if (light == null) {
												return;
											}
											if (l.oldState == light.state) {
												shakeList.add(light);
											}
										}
										/*for (Light light : lightList) {
											if (light.isValidate == 1
													&& light.signal != R.drawable.no_signal
													&& light.state == onOrOff
													&& (light.type == 1 || light.type == 3)) {
												onList.add(light);
											}
										}*/
									}
									
									//selectQulifyShake();
									if (shakeList.size() > 0) {
										if (currentRepeatTimes < 4) {
											System.out
													.println("There are some lights needed to be done!Repeat...");
											shakeOpenOrClose(BleService.SHAKE);
											currentRepeatTimes++;
										} else {
											mIsCheck = true;
											currentRepeatTimes = 0;
											System.out.println("Time Out!");
											//isOperating = false;
											Bundle mBundle = new Bundle();
											Message msg = Message.obtain(
													mActivityHandler, TIMEOUT);
											msg.setData(mBundle);
											msg.sendToTarget();
											isShakeOperationDone = true;
											mCurOperateAddress = "";
											if (mCurCheckThread != null)
												mCurCheckThread.interrupt();
											// MyActivityUtils.toast(MainActivity.this,
											// R.string.timeout);
										}
									} else {
										// currentRepeatTimes = 0;
										// MyActivityUtils.toast(MainActivity.this,
										// R.string.on_off_success);
										mIsCheck = true;
										System.out
												.println("There is no more lights to be turn on or off!");
										//isOperating = false;
										Bundle mBundle = new Bundle();
										Message msg = Message.obtain(
												mActivityHandler, SUCCESS);
										msg.setData(mBundle);
										msg.sendToTarget();
										currentRepeatTimes = 0;
										isShakeOperationDone = true;
										mCurOperateAddress = "";
										if (mCurCheckThread != null)
											mCurCheckThread.interrupt();
										// mService.scan(true);
									}
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						}).start();

					}
				}
				//sendLeftShake(BleService.SHAKE);
				break;
			case BleService.OnOrOff:
				dialog.dismiss();
				//没有检查功能的代码
				/*new Thread() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						//super.run();
						try {
							Thread.sleep(300);
							runOnUiThread(new Runnable() {

								@Override
								public void run() {
									// TODO Auto-generated method stub
									dialog.dismiss();
									//mService.scan(true);
									//startScanAndCheck(true);
									MyActivityUtils.toast(MainActivity.this,
											R.string.on_off_success);
								}
								
							});
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						
					}
					
				}.start();*/
				
				
				/*dialog.dismiss();
				mService.scan(true);
				MyActivityUtils.toast(MainActivity.this,
						R.string.on_off_success);*/
				/*if (currentRepeatTimes > 3) {
					//超时
					currentRepeatTimes = 0;
					Bundle mBundle = new Bundle();
					msg = Message.obtain(
							mActivityHandler, TIMEOUT);
					msg.setData(mBundle);
					msg.sendToTarget();	
				}
				else {
					new Thread(new Runnable() {

						@Override
						public void run() {
							// TODO Auto-generated method stub
							try {
								Thread.sleep(3000);
								if ((findLight(myList, mService.getMyLight()).oldState) == (findLight(myList, mService.getMyLight()).state)) {
									mService.connect(
											mBluetoothAdapter.getRemoteDevice(mService
													.getMyLight().address), false);
									currentRepeatTimes++;
								}
								else {
									currentRepeatTimes = 0;
									Bundle mBundle = new Bundle();
									Message msg = Message.obtain(
											mActivityHandler, SUCCESS);
									msg.setData(mBundle);
									msg.sendToTarget();
								}
												
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						
					}).start();
				}*/
				break;
			case BleService.ALL_ON_OR_OFF:
				// 成功了一次
				// Light light = (Light) data.getSerializable("DEVICE");
				//isOperating = false;
				isAllOnOrOffOperationDone = false;
				Log.v(TAG, "Success once...");
				// mService.scan(true);
				// onList.remove(0);
				if (onList.size() > 0) {
					onList.remove(0);
					if (onList.size() > 0) {
						/*new Thread() {

							@Override
							public void run() {
								// TODO Auto-generated method stub
								//super.run();
								try {
									Thread.sleep(80);
									startScanAndCheck(false);
									allOnOrOff();
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								
							}
							
						}.start();*/
						allOnOrOff();
					} else {

						// onList.clear();
						Log.v(TAG,
								"Turn on or off completed! Now check if some failed...");
						new Thread(new Runnable() {

							@Override
							public void run() {
								// TODO Auto-generated method stub
								// 检查还有没有没亮的，如果有，再做一遍
								try {
									Thread.sleep(2500);
									System.out
											.println("Turn on or off completed! Now check if some failed...");
									onList.clear();
									if (lightList != null
											&& lightList.size() != 0) {
										synchronized (lightList) {
											for (Light light : lightList) {
												if (light.isValidate == 1
														&& light.signal != R.drawable.no_signal
														&& light.state == onOrOff
														&& (light.type == 1 || light.type == 3|| light.type == 0x11 || light.type == 0x13)) {
													if ((onOrOff == OFF && lightMgr.findByAddress(light.address).remote_open == 1)
															|| (onOrOff == ON && lightMgr.findByAddress(light.address).remote_close == 1)) {
														onList.add(light);
													}
												}
											}
										}
									}
									if (onList.size() > 0) {
										System.out.println("currentRepearTimes is " + currentRepeatTimes);
										if (currentRepeatTimes < 3) {
											System.out
													.println("There are some lights needed to be done!Repeat...");
											allOnOrOff();
											currentRepeatTimes++;
										} else {
											mIsCheck = true;
											currentRepeatTimes = 0;
											System.out.println("Time Out!");
											isAllOnOrOffOperationDone = false;
											Bundle mBundle = new Bundle();
											Message msg = Message.obtain(
													mActivityHandler, TIMEOUT);
											msg.setData(mBundle);
											msg.sendToTarget();
											
											mCurOperateAddress = "";
											if (mCurCheckThread != null)
												mCurCheckThread.interrupt();
											// MyActivityUtils.toast(MainActivity.this,
											// R.string.timeout);
										}
									} else {
										// currentRepeatTimes = 0;
										// MyActivityUtils.toast(MainActivity.this,
										// R.string.on_off_success);
										mIsCheck = true;
										System.out
												.println("There is no more lights to be turn on or off!");
										isAllOnOrOffOperationDone = false;
										Bundle mBundle = new Bundle();
										Message msg = Message.obtain(
												mActivityHandler, SUCCESS);
										msg.setData(mBundle);
										msg.sendToTarget();
										currentRepeatTimes = 0;
										mCurOperateAddress = "";
										if (mCurCheckThread != null)
											mCurCheckThread.interrupt();
										// mService.scan(true);
									}
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						}).start();

					}
					/*
					 * else {
					 * 
					 * //onList.clear(); Log.v(TAG,
					 * "Turn on or off completed! Now check if some failed...");
					 * new Thread (new Runnable() {
					 * 
					 * @Override public void run() { // TODO Auto-generated
					 * method stub //检查还有没有没亮的，如果有，再做一遍 try {
					 * Thread.sleep(2000); System.out.println(
					 * "Turn on or off completed! Now check if some failed...");
					 * //onList.clear(); if (lightList != null &&
					 * lightList.size() != 0) { for (Light light : lightList) {
					 * if (light.isValidate == 1 && light.signal !=
					 * R.drawable.no_signal && light.state == onOrOff &&
					 * (light.type == 1 || light.type == 3)) {
					 * onList.add(light); } } } if (onList.size() > 0) { if
					 * (currentRepeatTimes < 4) { System.out.println(
					 * "There are some lights needed to be done!Repeat...");
					 * allOnOrOff(); currentRepeatTimes++; } else {
					 * currentRepeatTimes = 0; System.out.println("Time Out!");
					 * Bundle mBundle = new Bundle(); Message msg =
					 * Message.obtain(mActivityHandler, TIMEOUT);
					 * msg.setData(mBundle); msg.sendToTarget();
					 * //MyActivityUtils.toast(MainActivity.this,
					 * R.string.timeout); } } else { //currentRepeatTimes = 0;
					 * // MyActivityUtils.toast(MainActivity.this,
					 * R.string.on_off_success); System.out.println(
					 * "There is no more lights to be turn on or off!"); Bundle
					 * mBundle = new Bundle(); Message msg =
					 * Message.obtain(mActivityHandler, SUCCESS);
					 * msg.setData(mBundle); msg.sendToTarget();
					 * currentRepeatTimes = 0; //mService.scan(true); } } catch
					 * (InterruptedException e) { // TODO Auto-generated catch
					 * block e.printStackTrace(); } } }).start();
					 */
					//
					// thread.isAlive();
				}
				break;

			case TIMEOUT:
				isOperating = false;
				dialog.dismiss();
				//mService.scan(true);
				//startScanAndCheck(true);
				MyActivityUtils.toast(MainActivity.this, R.string.timeout);
				break;
			case BleService.OnOrOffWithRedo:
				//dialog.dismiss();
				//mService.scan(true);
				//startScanAndCheck(true);
					new Thread(new Runnable() {

						@Override
						public void run() {
							// TODO Auto-generated method stub
							try {
								Thread.sleep(1800);
								if ((findLight(myList, mService.getMyLight()).oldState) == (findLight(lightList, mService.getMyLight()).state)) {
									//没有跳变
									if (currentRepeatTimes > 0) {
										//超时
										currentRepeatTimes = 0;
										Bundle mBundle = new Bundle();
										Message msg = Message.obtain(
												mActivityHandler, TIMEOUT);
										msg.setData(mBundle);
										msg.sendToTarget();	
									}
									else {
										startScanAndCheck(false);
										currentRepeatTimes++;
										mService.connect(
												mBluetoothAdapter.getRemoteDevice(mService
														.getMyLight().address), false);
										
									}
								}
								else {
									//跳变了
									currentRepeatTimes = 0;
									Bundle mBundle = new Bundle();
									Message msg = Message.obtain(
											mActivityHandler, SUCCESS);
									msg.setData(mBundle);
									msg.sendToTarget();
								}
								/*if (currentRepeatTimes > 2) {
									//超时
									currentRepeatTimes = 0;
									Bundle mBundle = new Bundle();
									Message msg = Message.obtain(
											mActivityHandler, TIMEOUT);
									msg.setData(mBundle);
									msg.sendToTarget();	
								}
								else {
									
									if ((findLight(myList, mService.getMyLight()).oldState) == (findLight(lightList, mService.getMyLight()).state)) {
										System.out.println("turnOnOrOffWithRedo redo");
										mService.connect(
												mBluetoothAdapter.getRemoteDevice(mService
														.getMyLight().address), false);
										currentRepeatTimes++;
									}
									else {
										currentRepeatTimes = 0;
										Bundle mBundle = new Bundle();
										Message msg = Message.obtain(
												mActivityHandler, SUCCESS);
										msg.setData(mBundle);
										msg.sendToTarget();
									}
								}*/
												
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						
					}).start();
				
				break;
			case SUCCESS:
				dialog.dismiss();
				//mService.scan(true);
				//startScanAndCheck(true);
				mIsCheck = true;
				MyActivityUtils.toast(MainActivity.this,
						R.string.on_off_success);
				break;
			/*
			 * else { //检查还有没有没亮的，如果有，再做一遍 if (lightList != null &&
			 * lightList.size() != 0) { for (Light light : lightList) { if
			 * (light.isValidate == 1 && light.state == 0) { onList.add(light);
			 * } }
			 * 
			 * } if (onList.size() > 0 && currentRepeatTimes < 5) {
			 * allOnOrOff(); } else { MyActivityUtils.toast(MainActivity.this,
			 * R.string.on_off_success); mService.scan(true); }
			 * 
			 * }
			 */
			case BleService.LED_CONNECTED:
				dialog.dismiss();
				brightnessControlDialog.show();
				isRunning = true;
				Light light = findLight (lightList, mService.getMyLight());
				if (light != null) {
					brightness.setProgress(light.brightness);
					brightnesses = light.brightness;
				}
				
				new Thread(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						while (isRunning) {
							mService.setBrightness((byte) brightnesses);
							try {
								Thread.sleep(50);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
								break;
							}
						}
					}

				}).start();
				brightness
						.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

							@Override
							public void onProgressChanged(SeekBar seekBar,
									int progress, boolean fromUser) {
								// TODO Auto-generated method stub
								// int brightness =
								// seekBar.getProgress()<<24>>24;
								// mService.setBrightness((byte) brightness);
								// if ((progress % 5) == 0) {
								// mService.setBrightness((byte) progress);
								// }
								brightnesses = progress;

							}

							@Override
							public void onStartTrackingTouch(SeekBar seekBar) {
								// TODO Auto-generated method stub
								// mService.setBrightness((byte) 0x05);
							}

							@Override
							public void onStopTrackingTouch(SeekBar seekBar) {
								// TODO Auto-generated method stub
								// mService.setBrightness((byte) 0x99);
							}

						});
				break;
			case BleService.LED:
				brightnessControlDialog.dismiss();
				myLight = mService.getMyLight();
				myLight = findLight(lightList, myLight);
				myLight.isOperationDone = true;
				myLight.isCheckThisTime = true;
				// mService.scan(true);
				break;
			case DeviceAdapter.DELETE_COMPLETED:
				// isOperating = false;
				myList = findByArea(areaSelected);
				break;
			case DeviceAdapter.BRIGHTNESS_CONTROL:
				dialog.show();
				myLight = (Light) data.getSerializable("DEVICE");
				myLight = findLight(lightList, myLight);
				myLight.isCheckThisTime = false;
				myLight.isOperationDone = false;
				mService.setOperation(mService.LED);
				mService.setMyLight(myLight);
				mService.connect(
						mBluetoothAdapter.getRemoteDevice(myLight.address),
						false);

				new Thread() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						super.run();
						try {
							Thread.sleep(3000);
							if (!isRunning) {
								//dialog.show();
								myLight.isCheckThisTime = true;
								myLight.isOperationDone = true;
								Bundle mBundle = new Bundle();
								Message msg = Message.obtain(
										mActivityHandler, TIMEOUT);
								msg.setData(mBundle);
								msg.sendToTarget();
								mService.disconnect();
							}
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					
				}.start();
				break;

			case DeviceAdapter.WINDOW_CONTROL:
				dialog.show();
				myLight = (Light) data.getSerializable("DEVICE");
				controlWindow(myLight);
				myLight.isCheckThisTime = false;
				myLight.isOperationDone = false;
				break;
			case BleService.WINDOW_CONNECT:
				dialog.dismiss();
				windowControlDialog.show();
				break;
			case BleService.WINDOW:
				windowControlDialog.dismiss();
				myLight.isCheckThisTime = true;
				myLight.isOperationDone = true;
				break;
			case BleService.UNLOCK:
				dialog.dismiss();
				/*new Thread() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						super.run();
						try {
							Thread.sleep(250);
							runOnUiThread(new Runnable() {

								@Override
								public void run() {
									// TODO Auto-generated method stub
									dialog.dismiss();
									MyActivityUtils.toast(MainActivity.this,
											R.string.on_off_success);
								}
								
							});
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					
				}.start();*/
				
				
				/*dialog.dismiss();
				//mService.scan(true);
				MyActivityUtils.toast(MainActivity.this,
						R.string.on_off_success);*/
				break;
			case BleService.LED_OnOrOff:
				dialog.dismiss();
				break;
			case BleService.INIT_PASSWORD:
				dialog.dismiss();
				if (data != null) {
					String initResult = data.getString(BleService.RESULT);
					if (initResult.equals(BleService.SUCCESS)) {
						// showMySuccessDialog(R.string.init_success);
						new AlertDialog.Builder(MainActivity.this)
								.setMessage(R.string.init_success)
								.setPositiveButton(R.string.confirm, null)
								.show();
					} else {
						new AlertDialog.Builder(MainActivity.this)
								.setMessage(R.string.init_failed)
								.setNegativeButton(R.string.confirm, null)
								.show();
					}
				}
				break;
				

			}

			
		}
	};

	private ArrayList<Light> findByArea(String areaName) {
		if (areaName.equals(getString(R.string.all))) {
			//return lightMgr.findAll();
			return new ArrayList<Light>();
		} else {
			return lightMgr.findAllByArea(areaName);
		}
	}

	private AlertDialog createBrightnessControlDialog(Context context, View view) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setView(view);
		builder.setTitle(R.string.brightnessControl);
		return builder.create();
	}
	private AlertDialog createWindowControlDialog(Context context, View view) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setView(view);
		builder.setTitle(R.string.windowControl);
		return builder.create();
	}
	long time = 0;
	WindowControlThread mWindowControlThread;
	boolean isEnterLongClick = false;
	
	private void setUpLastSetting() {
		SharedPreferences sp = getSharedPreferences(LanguageActivity.LANGUAGE, MODE_PRIVATE);
		String language = sp.getString(LanguageActivity.LANGUAGE, "中文");
		if (language.equals("中文") || language.equals("Chinese")) {
			switchLanguage(Locale.CHINESE);
		} else {
			switchLanguage(Locale.ENGLISH);
		}
	}
	private void initBrightnessControl() {
		LayoutInflater li = getLayoutInflater();
		View view = li.inflate(R.layout.brightness_control, null);
		brightnessControlDialog = createBrightnessControlDialog(this, view);
		brightnessControlDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
			
			@Override
			public void onDismiss(DialogInterface dialog) {
				// TODO Auto-generated method stub
				mService.disconnect();
				isRunning = false;
				//brightnessControl.setVisibility(View.GONE);
			}
		});
		brightness = (SeekBar) view.findViewById(R.id.brightness);

		Button cancleBtn = (Button) view.findViewById(R.id.cancle);
		cancleBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				System.out.println("用户点击了取消按钮，LED调光关闭");
				mService.disconnect();
				isRunning = false;
				//brightnessControl.setVisibility(View.GONE);
				brightnessControlDialog.dismiss();
			}
		});
	}
	private void initWindowControl() {
		LayoutInflater li = getLayoutInflater();
		View windowView = li.inflate(R.layout.window_control, null);
		windowControlDialog = createWindowControlDialog(this, windowView);
		windowControlDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
			
			@Override
			public void onDismiss(DialogInterface dialog) {
				// TODO Auto-generated method stub
				mService.disconnect();
				isRunning = false;
				//brightnessControl.setVisibility(View.GONE);
			}
		});
		
		mOpenBtn = (Button) windowView.findViewById(R.id.open);
		mOpenBtn.setOnLongClickListener(new View.OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View v) {
				// TODO Auto-generated method stub
				System.out.println("long press");
				byte []data = {0x20};
				mWindowControlThread = new WindowControlThread(data);
				mWindowControlThread.start();
				mCloseBtn.setEnabled(false);
				isEnterLongClick = true;
				return true;
			}
		});
		mOpenBtn.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				System.out.println("ontouch + action " + event.getAction());
				if (event.getAction() == MotionEvent.ACTION_UP/* && (System.currentTimeMillis() - time) > 300*/ && isEnterLongClick) {
					/*byte []data = {0x20};
					System.out.println("窗帘开");
					System.out.println(mService.write(data, BleService.MY_TIMER_CHARACTERISTIC));
					time = System.currentTimeMillis();*/
					v.clearFocus();
					v.setPressed(false);
					if (mWindowControlThread != null) {
						mWindowControlThread.setRunning(false);
					}
					mCloseBtn.setEnabled(true);
					isEnterLongClick = false;
					return true;
				}/* else if (event.getAction() == MotionEvent.ACTION_UP) {
					// 短按松手
					System.out.println("短按松手，设置mCloseBtn为enable");
					mCloseBtn.setEnabled(true);
					//return true;
				}*/
				return false;
			}
		});
		mOpenBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mCloseBtn.setEnabled(false);
				System.out.println("窗帘开");
				System.out.println(mService.write(new byte[]{0x20}, BleService.MY_TIMER_CHARACTERISTIC));
				mCloseBtn.setEnabled(true);
			}
		});
		
		mCloseBtn = (Button) windowView.findViewById(R.id.close);
		mCloseBtn.setOnLongClickListener(new View.OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View v) {
				// TODO Auto-generated method stub
				System.out.println("long press");
				byte []data = {0x21};
				mWindowControlThread = new WindowControlThread(data);
				mWindowControlThread.start();
				mOpenBtn.setEnabled(false);
				isEnterLongClick = true;
				return true;
			}
		});
		mCloseBtn.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				System.out.println("ontouch + action " + event.getAction());
				if (event.getAction() == MotionEvent.ACTION_UP/* && (System.currentTimeMillis() - time) > 300*/ && isEnterLongClick) {
					// 长按松手
					/*byte []data = {0x20};
					System.out.println("窗帘开");
					System.out.println(mService.write(data, BleService.MY_TIMER_CHARACTERISTIC));
					time = System.currentTimeMillis();*/
					v.clearFocus();
					v.setPressed(false);
					if (mWindowControlThread != null) {
						mWindowControlThread.setRunning(false);
					}
					mOpenBtn.setEnabled(true);
					isEnterLongClick = false;
					return true;
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					// 短按松手
					//mOpenBtn.setEnabled(true);
					//return true;
				}
				return false;
			}
		});
		mCloseBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mOpenBtn.setEnabled(false);
				System.out.println("窗帘关");
				System.out.println(mService.write(new byte[]{0x21}, BleService.MY_TIMER_CHARACTERISTIC));
				mOpenBtn.setEnabled(true);
			}
		});
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		setUpLastSetting();
		initBrightnessControl();
		initWindowControl();
		
		onList = new ArrayList<Light>();
		lightList = new ArrayList<Light>();
		
		deviceListView = getListView();
		deviceAdapter = new DeviceAdapter(this, lightList, mActivityHandler);
		setListAdapter(deviceAdapter);
		deviceAdapter.setListView(deviceListView);

		lightMgr = new LightMgr(this);
		areaMgr = new AreaMgr(this);

		EnglishList = new ArrayList<Light>();
		NumberList = new ArrayList<Light>();
		ChineseList = new ArrayList<Light>();
		NoSignalList = new ArrayList<Light>();
		LightToDeleteList = new ArrayList<Light>();
		deleteBtnView = new ArrayList<View>();
		rssiQueue = new LinkedList<Integer>();
		
		closeOKList = new ArrayList<Light>();

		//ImageView sort = (ImageView) findViewById(R.id.sort_btn);
		
		noDevice = (TextView) findViewById(R.id.no_device);
		
		dialog = new ProgressDialog(this);
		dialog.setTitle(R.string.pleaseWait);
		dialog.setIndeterminate(true);
		dialog.setMessage(getString(R.string.operating));
		dialog.setCanceledOnTouchOutside(false);
		dialog.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss(DialogInterface dialog) {
				// TODO Auto-generated method stub
				Light l = findLight(myList, mService.getMyLight());
				if (l != null) {
					l.isOperationDone = true;
				}
				isShakeOperationDone = true;
				//mService.scan(true);
				
				startScanAndCheck(true);
				
			}
			
		});
		
		//initTabHost()
		initGridView();
		setGridViewListener();
		
		//initTopTitle();
		
		RelativeLayout rllayout = (RelativeLayout) findViewById(R.id.title_layout);
		rllayout.setOnClickListener(this);

		areaListView = (ListView) findViewById(R.id.arealist);
		arrows = (ImageView) findViewById(R.id.arrows);

		tv_title = (TextView) findViewById(R.id.title);
		tv_title.setText(getString(R.string.all));
		
		
		
		shakeCopyList = new ArrayList<Light>();

		/*cancle = (Button) findViewById(R.id.cancleButton);
		//dialog = (RelativeLayout) findViewById(R.id.dialogLayout);
		cancle.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				dialog.dismiss();
				//mService.scan(true);
				startScanAndCheck(true);
			}
		});*/

		deviceListView.setOnTouchListener(this);

		areaSelected = getString(R.string.all);
		
		checkHandler = new Handler();
		checkHandler.postDelayed(thread, 10000);
		
		
		SharedPreferences sp = getSharedPreferences("AreaPreferences", MODE_PRIVATE);
		String areaStr = sp.getString("area", getString(R.string.all));
		System.out.println("area saved is " + areaStr);
		if (areaMgr.findByName(areaStr) != null) {
			//查看记录的area是否已经被删除
			areaSelected = areaStr;
		}
	}


	@Override
	public void onStart() {
		super.onStart();

		if (mBluetoothAdapter == null) {
			mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
			if (mBluetoothAdapter == null) {
				return;
			}
			if (!mBluetoothAdapter.isEnabled()) {
				Intent enableBluetooth = new Intent(
						BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivity(enableBluetooth);
				// 系统会发送ACTION_STATE_CHANGED广播
			}
		}
		
		/*if ("ALL".equals(areaSelected)) {
			areaSelected = "全部";
		}*/
		onService = new ServiceConnection() {
			public void onServiceConnected(ComponentName className,
					IBinder rawBinder) {
				// 当bindService成功返回后调用
				mService = ((BleService.LocalBinder) rawBinder).getService();
				if (mService != null) {
					mService.setDeviceListHandler(mHandler);
					mService.setActivityHandler(mActivityHandler);
					// mScanPerSecond.postDelayed(runnable, 20000);
					// mService.scan(true);
					// 过0.5秒再scan，否则太快了，Activity还没有启动起来
					if (mBluetoothAdapter.isEnabled()) {
						Handler handler = new Handler();
						handler.postDelayed(new Runnable() {
	
							@Override
							public void run() {
								// TODO Auto-generated method stub
								//mService.scan(true);
								startScanAndCheck(true);
								
							}
	
						}, 500);
					}
					// mScanPerSecond.postDelayed(runnable, 20000);
				}
			}

			public void onServiceDisconnected(ComponentName classname) {
				mService = null;
				
				lightList.clear();
				EnglishList.clear();
				NumberList.clear();
				ChineseList.clear();
				NoSignalList.clear();
			}
		};
		startService(new Intent(this, BleService.class));

		lightList.clear();
		EnglishList.clear();
		NumberList.clear();
		ChineseList.clear();
		NoSignalList.clear();
		
		
		myList = findByArea(areaSelected);
		//allMyList = findByArea(getString(R.string.all));
		allMyList = lightMgr.findAll();
		NoSignalList.addAll(myList);
		lightList.clear();
		lightList.addAll(myList);
		for (Light light : lightList) {
			if (light.area.equals("全部") || light.area.equals("ALL")) {
				light.area = getString(R.string.all);
				lightMgr.update(light);
			}
		}
		if (myList != null && myList.size() != 0) {
			sensorLights = new ArrayList<Light>();
			closeList = new ArrayList<Light>();
			boolean isExit = false;
			for (Light l : myList) {
				if (l.shake_open == 1 || l.shake_close == 1) {
					sensorLights.add(l);
					isExit = true;
				}
				if (l.close_open == 1 || l.close_close == 1) {
					closeList.add(l);
				}
				/*
				 * if (l.brightnessChangeable == 1 || l.type == 3) { View view =
				 * getListView().getChildAt(lightList.indexOf(l));
				 * view.findViewById
				 * (R.id.brightnessBtn).setVisibility(View.VISIBLE); }
				 */
			}
			if (isExit) {
				initSensor();
				shakeList = new ArrayList<Light>();
			}
		}

		areaList = areaMgr.findAll();
		if (areaList == null || areaList.size() == 0) {
			Area area = new Area();
			area.area = getString(R.string.all);
			areaMgr.add(area);
			areaList = areaMgr.findAll();
		}
		
		areaList.get(0).area = getString(R.string.all);
		areaMgr.update(areaList.get(0));
		
		//areaList.get(0).area = getString(R.string.all);
		areaAdapter = new AreaAdapter(this, areaList, R.layout.item_area_layout);
		areaListView.setAdapter(areaAdapter);
		areaListView.setOnItemClickListener(this);

		Intent bindIntent = new Intent(this, BleService.class);
		bindService(bindIntent, onService, Context.BIND_AUTO_CREATE);
		IntentFilter filter = new IntentFilter(
				BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
		this.registerReceiver(mReceiver, filter);

		
		
		
		EnglishList.clear();
		NumberList.clear();
		ChineseList.clear();
		if (lightList.size() == 0) {
			noDevice.setVisibility(View.VISIBLE);
			deviceListView.setVisibility(View.GONE);
		}
		deviceAdapter.notifyDataSetChanged();
		
		
		
		
		
		Area area = new Area();
		area.area = areaSelected;
		areaSwitch(area);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mSensorManager != null) {
			mSensorManager.registerListener(this, mSensor,
					SensorManager.SENSOR_DELAY_NORMAL);
		}

	}

	@Override
	protected void onPause() {
		super.onPause();
		if (mSensorManager != null) {
			mSensorManager.unregisterListener(this);
		}
		System.out.println("onPause");
		/*
		 * if (mService != null) { mService.scan(false); }
		 */
	}

	@Override
	public void onStop() {
		super.onStop();
		
		// mScanPerSecond.removeCallbacks(runnable);
		//mService.scan(false);
		startScanAndCheck(false);
		//thread.setStart(false);
		unregisterReceiver(mReceiver);
		unbindService(onService);
		System.out.println("onStop");
		for (Light light : myList) {
			Light l = findLight(lightList, light);
			if (l != null && l.signal != R.drawable.no_signal) {
				light.version = l.version;
				if (light.type == 4) {
					light.picture = R.drawable.curtain_no_signal;
				}
				lightMgr.update(light);
			}
		}

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		stopService(new Intent(this, BleService.class));
		finish();
		System.out.println("onDestroy");
		//thread.setStart(false);
		
		SharedPreferences sp = getSharedPreferences("AreaPreferences", MODE_PRIVATE);
		sp.edit().putString("area", areaSelected).commit();
		//sp.setString(, areaSelected);
		
	}

	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// 验证灯的密码与输入的是否相同
		super.onActivityResult(requestCode, resultCode, data);

		Light light = null;
		//startScanAndCheck(true);
		switch (requestCode) {
		case VALIDATE_REQUEST:
			if (data != null) {
				light = (Light) data.getSerializableExtra(Light.LIGHT);
				if (light != null) {
					// mService.scan(false);
					dialog.show();
					//mService.scan(false);
					
					/*Light l = lightMgr.findByAddress(light.address);
					if (l != null) {
						l.isOperationDone = false;
					}*/
					if (light.password.equals("5335608202")) {
						mService.setMyLight(myLight);
						mService.setOperation(BleService.INIT_PASSWORD);
						mService.connect(mBluetoothAdapter
								.getRemoteDevice(myLight.address), false);
						isOperating = true;
						
						new Thread(new Runnable() {

							@Override
							public void run() {
								// TODO Auto-generated method stub
								try {
									Thread.sleep(5000);
									//Light light = lightMgr.findByAddress(mService.getMyLight().address);
									if (isOperating) {
										Bundle mBundle = new Bundle();
										Message msg = Message.obtain(
												mActivityHandler, TIMEOUT);
										msg.setData(mBundle);
										msg.sendToTarget();
									}

								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}

						}).start();
					} else {

						BluetoothDevice device = mBluetoothAdapter
								.getRemoteDevice(light.address);
						/*Light serviceLight = new Light();
						serviceLight.name = light.name;
						serviceLight.password = light.name;*/
						/*myLight.password = light.password;
						myLight.name = light.name;*/
						//myLight.area = light.area;
						
						mService.setMyLight(light);
						mService.setOperation(BleService.VALIDDATE);
						mService.connect(device, false);

						new Thread(new Runnable() {

							@Override
							public void run() {
								// TODO Auto-generated method stub
								try {
									// for (int i = 0; i < 3; i++) {
									Thread.sleep(5000);
									if (lightMgr.findByAddress(mService
											.getMyLight().address) == null) {
										Log.v(TAG, "skip valide once");
										// mService.connect(mBluetoothAdapter.getRemoteDevice(mService.getMyLight().address),
										// false);
										Message msg = Message.obtain(
												mActivityHandler,
												BleService.VALIDDATE);
										Bundle bundle = new Bundle();
										bundle.putInt(BleService.RESULT, -1);
										msg.setData(bundle);
										msg.sendToTarget();

									}
									/*
									 * else { Message msg =
									 * Message.obtain(mActivityHandler,
									 * BleService.VALIDDATE);
									 * msg.sendToTarget(); //break; }
									 */

									// }
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}

						}).start();
					}

				}
			}
			else {
				//mService.scan(true);
				//startScanAndCheck(true);
			}
		}

	}

	// 初始化传感器
	void initSensor() {
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
	}

	/**
	 * 初始化数据
	 */
	private void initGridView() {
		gridView = (GridView) findViewById(R.id.grid_view);
		// 初始化TabHost
		ArrayList<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();
		int[] grid_text = new int[] { R.string.all_on, R.string.all_off,
				R.string.setting, R.string.timer };
		for (int i = 0; i < 4; i++) {
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("icon", R.drawable.grid_view01 + i);
			map.put("title", getString(grid_text[i]));
			data.add(map);
		}
		SimpleAdapter adapter = new SimpleAdapter(this, data,
				R.layout.item_grid_view, new String[] { "icon", "title" },
				new int[] { R.id.grid_icon, R.id.grid_title });
		gridView.setAdapter(adapter);
	}

	private void setGridViewListener() {
		gridView.setOnItemClickListener(new OnItemClickListener() {

			// TabHost的点击事件
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// mService.scan(false);
				switch (position) {
				case 0:
					mIsCheck = false;
					startScanAndCheck(false);
					currentRepeatTimes = 0;
					isAllOnOrOffOperationDone = true;
					dialog.show();
					onOrOff = OFF;
					if (lightList != null && lightList.size() != 0) {
						if (onList != null && onList.size() != 0) {
							onList.clear();
						}
						synchronized (lightList) {
							for (Light light : lightList) {
								if (light.isValidate == 1
										&& light.signal != R.drawable.no_signal
										&& light.state == 0 && lightMgr.findByAddress(light.address).remote_open == 1
										&& (light.type == 1 || light.type == 3 || light.type == 0x11 || light.type == 0x13)) {
									onList.add(light);
								}
							}
						}
					}
					allOnOrOff();
					break;
				case 1:
					mIsCheck = false;
					startScanAndCheck(false);
					currentRepeatTimes = 0;
					isAllOnOrOffOperationDone = true;
					dialog.show();
					dialog.show();
					onOrOff = ON;
					if (lightList != null && lightList.size() != 0) {
						if (onList != null && onList.size() != 0) {
							onList.clear();
						}
						synchronized (lightList) {
							for (Light light : lightList) {
								if (light.isValidate == 1
										&& light.signal != R.drawable.no_signal
										&& light.state == 1 && lightMgr.findByAddress(light.address).remote_close == 1
										&& (light.type == 1 || light.type == 3|| light.type == 0x11 || light.type == 0x13)) {
									onList.add(light);
								}
							}
						}
						

					}
					allOnOrOff();
					break;
				case 2:
					mService.scan(false);
					Intent setting = new Intent(MainActivity.this,
							SettingActivity.class);
					myList = lightMgr.findAllByArea(areaSelected);
					for (Light l : myList) {
						Light temp = findLight(lightList, l);
						if (temp != null) {
							l.lastSignal = temp.signal;
							l.picture = temp.picture;
							l.state = temp.state;
							lightMgr.update(l);
						}
						
					}
					ArrayList<Light> deviceList = new ArrayList<Light>();
					deviceList.addAll(NumberList);
					deviceList.addAll(EnglishList);
					deviceList.addAll(ChineseList);
					//lightList.removeAll(NoSignalList);
					setting.putExtra(DeviceActivity.DEVICE_LIST, deviceList);
					startActivity(setting);
					finish();
					break;
				case 3:
					mService.scan(false);
					// mScanPerSecond.removeCallbacks(runnable);
					Intent timer = new Intent(MainActivity.this,
							TimerActivity.class);
					ArrayList<Light> list = new ArrayList<Light>();
					
					list.addAll(allMyList);
					timer.putExtra(TimerActivity.SELECT_DEVICE, list);
					startActivity(timer);
					break;
				}
			}
		});
	}
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		// ACTION_DISCOVERY_FINISHED：本地BluetoothAdapter 完成了 the device discovery
		// process
		// 或ACTION_STATE_CHANGED：本地BluetoothAdapter状态改变会调用
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
				// mService.scan(false);
			}
			if (BluetoothAdapter.ACTION_STATE_CHANGED
					.equals(intent.getAction())) {
				// 蓝牙打开了，或者没有打开呢
				if (mBluetoothAdapter.isEnabled()) {
					// 打开了
					//mService.scan(true);
					mActivityHandler.postDelayed(new Runnable() {

						@Override
						public void run() {
							// TODO Auto-generated method stub
							startScanAndCheck(true);
						}
						
					}, 3000);
					
				}
			}
		}
	};

	int mOldSize = 0;
	int waitTime = 0;

	//ThreadLocal<String> mCurOperateAddress = new ThreadLocal<String>();
	String mCurOperateAddress = "";
	CheckLightFinishThread mCurCheckThread = null;
	void allOnOrOff() {
		if (onList != null && onList.size() != 0) {
			mOldSize = onList.size();
			Light light = onList.get(0);
			light.oldState = light.state;

			mCurOperateAddress = light.address;
			/*if (light.type == 1) {
				waitTime = 1000;
			} else if (light.type == 3) {
				waitTime = 5000;
			}*/
			BluetoothDevice device = mBluetoothAdapter
					.getRemoteDevice(light.address);
			mService.setMyLight(light);
			mService.setOperation(BleService.ALL_ON_OR_OFF);
			mService.connect(device, false);

			/*new Thread(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					try {
						Thread.sleep(20000);
						//Thread.sleep(waitTime);
						if (onList.size() > 0 && onList.size() == mOldSize) {
							// 还没有成功，所以没有从onList中移除
							// onList.remove(0);
							// allOnOrOff();
							Log.v(TAG, "skip one light");
							Bundle mBundle = new Bundle();
							Message msg = Message.obtain(mActivityHandler,
									BleService.ALL_ON_OR_OFF);
							msg.setData(mBundle);
							msg.sendToTarget();
						}
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

			}).start();*/
			if (mCurCheckThread != null) {
				mCurCheckThread.interrupt();
			}
			mCurCheckThread =new CheckLightFinishThread(mCurOperateAddress);
			mCurCheckThread.start();
			
			// onList.remove(0);
		} else {
			Bundle mBundle = new Bundle();
			Message msg = Message.obtain(mActivityHandler, SUCCESS);
			msg.setData(mBundle);
			msg.sendToTarget();
		}
	}

	class CheckLightFinishThread extends Thread {
		private String mCheckAddress;
		public CheckLightFinishThread(String curOperateAddress) {
			mCheckAddress = curOperateAddress;
		}
		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
				Thread.sleep(5000);
				//Thread.sleep(waitTime);
				if (mCheckAddress.equals(mCurOperateAddress)) {
					// 还没有成功，所以没有从onList中移除
					Log.v(TAG, "skip one light");
					Bundle mBundle = new Bundle();
					Message msg = Message.obtain(mActivityHandler,
							BleService.ALL_ON_OR_OFF);
					msg.setData(mBundle);
					msg.sendToTarget();
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				Log.v(TAG, "InterruptedException");
				e.printStackTrace();
			}
		}
	}
	/**
	 * 全开全关
	 * 
	 * @param operation
	 * @param state
	 */
	/*
	 * void allOnOrOff(int operation, int state){ if (lightList != null &&
	 * lightList.size() != 0) { if (onList != null && onList.size() != 0) {
	 * onList.clear(); } for (Light light : lightList) { if (light.isValidate ==
	 * 1 && light.state == state) { onList.add(light); } } if (onList != null &&
	 * onList.size() != 0) { Light light = onList.get(0);
	 * mService.setOperation(operation); mService.setMyLight(light);
	 * mService.setSize(0);
	 * mService.connect(mBluetoothAdapter.getRemoteDevice(light.address),
	 * false); } } }
	 * 
	 * void onOrOffMessage(int operation, int state) { int size =
	 * mService.getSize(); if (size < onList.size()){
	 * mService.setOperation(operation);
	 * mService.connect(mBluetoothAdapter.getRemoteDevice
	 * (onList.get(size).address), false); } else if (size == onList.size()) {
	 * boolean isAll = true; for (Light light : onList) { if (light.isValidate
	 * == 1 && light.state == state) { isAll = false; break; } } if (!isAll) {
	 * allOnOrOff(operation, state); } } }
	 */

	void sendLeftShake(int operation) {
		if (shakeList != null && shakeList.size() != 0) {
			mService.setOperation(operation);
			shakeList.remove(0);
			if (shakeList.size() != 0) {
				Light light = shakeList.get(0);
				if (light.type == 1) {
					turnOnOrOff(light);
				}
				else if (light.type == 2){
					openLock(light);
				}
				else if (light.type == 3) {
					turnOnOrOffLED(light);
				}
				/*mService.setMyLight(shakeList.get(0));
				mService.setSize(0);
				mService.connect(mBluetoothAdapter.getRemoteDevice(shakeList
						.get(0).address), false);*/
				
			}
			else {
				dialog.dismiss();
			}
		}
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		long curTime = System.currentTimeMillis();
		if ((curTime - lastUpdate) > 100 && isShakeOperationDone) {
			System.out.println("onSensorChanged");
			long diffTime = (curTime - lastUpdate);
			lastUpdate = curTime;
			// 这里做了简化，没有用z的数据
			x = event.values[SensorManager.DATA_X];
			y = event.values[SensorManager.DATA_Y];
			z = event.values[SensorManager.DATA_Z];
			float acceChangeRate = 0;
			if (last_x != 0)
				acceChangeRate = Math.abs(x + y + z - last_x - last_y - last_z)
						/ diffTime * 10000;
			// 这里设定2个阀值，一个是加速度的，一个是shake的间隔时间的
			if (acceChangeRate > SHAKE_THRESHOLD
					&& curTime - lastShakeTime > 1000 && isShakeOperationDone) {
				
				if (selectQulifyShake().size() > 0) {
					isShakeOperationDone = false;
					lastShakeTime = curTime;
					dialog.show();
					shakeCopyList.clear();
					shakeCopyList.addAll(shakeList);
					mIsCheck = false;
					shakeOpenOrClose(BleService.SHAKE);
				}
			}
			last_x = x;
			last_y = y;
			last_z = z;
		}
	}

	@Override
	/**
	 * 没有用到怎么还重载啊
	 */
	public void onAccuracyChanged(Sensor sensor, int accuracy) {

	}

	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		//mService.scan(true);
		//startScanAndCheck(true);
		//if (dialog.isShowing())
	}

	public ArrayList<Light> selectQulifyShake() {
		shakeList.clear();
		if (lightList != null && lightList.size() != 0 && 
				sensorLights != null && sensorLights.size() != 0) {
			Light light = null;
			for (Light l : sensorLights) {
				//light = null;
				light = findLight(lightList, l);
				if (light != null && light.signal != R.drawable.no_signal &&((l.shake_open == 1 && light.state == 0) || 
						(l.shake_close == 1 && light.state == 1))) {
					shakeList.add(light);
				}
			}
			
		}
		else {
			return null;
		}
		return shakeList;
	}
	/*
	 * 晃动打开或关闭灯具
	 */
	void shakeOpenOrClose(int operation) {
		/*if (lightList != null && lightList.size() != 0) {
			if (sensorLights != null && sensorLights.size() != 0) {
				for (Light light : sensorLights) {
					for (Light l : lightList) {
						if (light.address.equals(l.address)) {
							if (light.shake_open == 1 || light.shake_close == 1) {
								shakeList.add(light);
							}
						}
					}
				}
				if (shakeList != null && shakeList.size() != 0) {
					Light light = shakeList.get(0);
					mService.setOperation(operation);
					mService.setMyLight(shakeList.get(0));
					mService.connect(mBluetoothAdapter
							.getRemoteDevice(shakeList.get(0).address), false);
				}
			}
		}*/
		
		if (shakeList != null && shakeList.size() != 0) {
			mOldSize = shakeList.size();
			Light light = shakeList.get(0);
			Light l = findLight(shakeCopyList, light);
			l.oldState = light.state;

			/*if (light.type == 1) {
				waitTime = 1000;
			} else if (light.type == 3) {
				waitTime = 5000;
			}*/
			BluetoothDevice device = mBluetoothAdapter
					.getRemoteDevice(light.address);
			mService.setMyLight(light);
			mService.setOperation(operation);
			mService.connect(device, false);

			/*new Thread(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					try {
						Thread.sleep(3000);
						//Thread.sleep(waitTime);
						if (shakeList.size() > 0 && shakeList.size() == mOldSize) {
							// 还没有成功，所以没有从onList中移除
							// onList.remove(0);
							// allOnOrOff();
							Log.v(TAG, "skip one light");
							Bundle mBundle = new Bundle();
							Message msg = Message.obtain(mActivityHandler,
									BleService.SHAKE);
							msg.setData(mBundle);
							msg.sendToTarget();
						}
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

			}).start();*/
			// onList.remove(0);
		} else {
			Bundle mBundle = new Bundle();
			Message msg = Message.obtain(mActivityHandler, SUCCESS);
			msg.setData(mBundle);
			msg.sendToTarget();
		}
	}

	/**
	 * 左右滑动兼点击
	 */
	boolean isBtnDeleteClicked = false;
	public boolean onTouch(View v, MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			// listView.setClickable(false);
			downX = event.getX();
			downY = event.getY();
			p1 = ((ListView) v).pointToPosition((int) downX, (int) downY);
		}

		if (event.getAction() == MotionEvent.ACTION_UP) {
			upX = event.getX();
			upY = event.getY();
			p2 = ((ListView) v).pointToPosition((int) upX, (int) upY);
			//View view = ((ListView) v).getChildAt(p2);
			View view = null;
			if (view == null) {
				int FirstVisiblePosition = deviceListView
						.getFirstVisiblePosition();
				view = ((ListView) v).getChildAt(p2 - FirstVisiblePosition);
			}
			if (view != null) {
				btn_delete = (Button) view.findViewById(R.id.btn_delete);
				iv_signal = (ImageView) view.findViewById(R.id.signal);
				if (p1 == p2 && Math.abs(upX - downX) > 200 && Math.abs(upY - downY) < /*60*/view.getHeight()
						&& lightList.get(p1).isValidate == 1) {
					/*因为不知道为什么，当btnDelete按完后，会两次进入这里*/
					if (isBtnDeleteClicked) {
						isBtnDeleteClicked = false;
						return false;
					}
					System.out.println(
							"Math.abs(upX - downX) = " + Math.abs(upX - downX) + " && Math.abs(upY - downY) = " +
									Math.abs(upY - downY));
					System.out.println("downX = " + downX + "downY" + downY + "upX" + upX + "upY" + upY);
					// 滑动事件
					if (btn_delete.getVisibility() == View.GONE) {
						/*for (View deleteview : deleteBtnView) {
							deleteview.findViewById(R.id.btn_delete).setVisibility(View.GONE);
						}
						deleteBtnView.clear();*/
						//int deleteBtnCount = ((ListView)v).getChildCount();
						int deleteBtnCount = deviceListView.getChildCount();
						for (int i = 0; i < deleteBtnCount; i++) {
							((ListView)deviceListView).getChildAt(i).findViewById(R.id.btn_delete).setVisibility(View.GONE);
						}
						System.out.println("btn_delete VISIBLE");
						btn_delete.setVisibility(View.VISIBLE);
						//deleteBtnView.add(view);
						// btn_delete.bringToFront();
						// isOperating = true;
						// mService.scan(false);
						// iv_signal.setVisibility(View.GONE);
						// btn_delete.setFocusable(false);
						
						/*btn_delete.setOnClickListener(new OnClickListener() {
							public void onClick(View v) {
								System.out.println("Delete Button clicked!");
								if (lightList.size() != 0) {
									int result = lightMgr.delete(lightList
											.get(p1).address);
									if (result > 0) {
										myList.remove(lightList.get(p1));
										lightList.remove(p1);
										MyActivityUtils.toast(
												MainActivity.this,
												R.string.delete_success);
										btn_delete.setVisibility(View.GONE);
										iv_signal.setVisibility(View.VISIBLE);
										//myList = lightMgr.findAll(); // mService.scan(true);
										//myList.remove(lightList.get(p1));
										deviceAdapter.notifyDataSetChanged();
									} else {
										MyActivityUtils.toast(
												MainActivity.this,
												R.string.delete_failed);
									}
								}
							}
						});*/
						btn_delete.setOnTouchListener(new OnTouchListener() {
							@Override
							public boolean onTouch(View v, MotionEvent event) {
								// TODO Auto-generated method stub
								if (event.getAction() == MotionEvent.ACTION_DOWN) {
									isBtnDeleteClicked = true;
									// listView.setClickable(false);
									System.out.println("Delete Button clicked!And v is " + v.getId());
									if (lightList.size() != 0) {
										int result = lightMgr.delete(lightList
												.get(p1).address);
										if (result > 0) {
											int deleteBtnCount = deviceListView.getChildCount();
											System.out.println("deviceListView.getChildCount() = " + deleteBtnCount);
											for (int i = 0; i < deleteBtnCount; i++) {
												System.out.println("listview " + i + " GONE");
												((ListView)deviceListView).getChildAt(i).findViewById(R.id.btn_delete).setVisibility(View.GONE);
											}
											Light light = lightList.get(p1); 
											boolean isNumberLight = isNumber(light.name);
											boolean isEnglishLight = isEnglesh(light.name);
											if (isNumberLight) {
												NumberList.remove(light);
											} else if (isEnglishLight) {
												EnglishList.remove(light);
											} else {
												ChineseList.remove(light);
											}
											lightList.remove(p1);
											
											if (lightList.size() == 0) {
												deviceListView.setVisibility(View.GONE);
												noDevice.setVisibility(View.VISIBLE);
											}
											MyActivityUtils.toast(
													MainActivity.this,
													R.string.delete_success);
											
											myList = lightMgr.findAllByArea(areaSelected); // mService.scan(true);
											allMyList = lightMgr.findAll();

											// 删除相应的timer
											TimerMgr timerMgr = new TimerMgr(MainActivity.this);
											ArrayList<Timer> timers = timerMgr.findAllByAddress(light.address);
											if (timers.size() > 0) {
												for (Timer timer : timers) {
													timerMgr.delete(timer);
												}
											}
											
											deviceAdapter.notifyDataSetChanged();
										} else {
											MyActivityUtils.toast(
													MainActivity.this,
													R.string.delete_failed);
										}
									}
									return true;
								}
								return false;
							}
						});
						 
						return false;
					}
					else {
						btn_delete.setVisibility(View.GONE);
						//deleteBtnView.remove(view);
					}
				} else if (p1 == p2 && Math.abs(upX - downX) < 10 && Math.abs(upY - downY) < 60) {
					// 点击事件
					// mService.scan(false);
					System.out.println("点击事件发生了！！！");
					
					//删除滑动出现的按钮
					boolean isReturn = false;
					//int deleteBtnCount = ((ListView)v).getChildCount();
					int deleteBtnCount = deviceListView.getChildCount();
					for (int i = 0; i < deleteBtnCount; i++) {
						int visibility = ((ListView)v).getChildAt(i).findViewById(R.id.btn_delete).getVisibility();
						if (visibility == View.VISIBLE) {
							isReturn = true;
						}
						((ListView)v).getChildAt(i).findViewById(R.id.btn_delete).setVisibility(View.GONE);
					}
					if (isReturn) {
						return false;
					}
					myLight = lightList.get(p1);

					if (myLight.isValidate == 0) {
						// 未验证远程设备
						//mService.scan(false);
						startScanAndCheck(false);
						isOperating = true;
						Intent intent = new Intent(MainActivity.this,
								ValidateActivity.class);
						intent.putExtra(Light.LIGHT, myLight);
						startActivityForResult(intent, VALIDATE_REQUEST);
					} else {
						// 已验证远程设备

						if (btn_delete.getVisibility() == View.VISIBLE) {
							btn_delete.setVisibility(View.GONE);
							// iv_signal.setVisibility(View.VISIBLE);
						} else {
							Light l = lightMgr.findByAddress(myLight.address);
							myLight.remote_close = l.remote_close;
							myLight.remote_open = l.remote_open;
							if (myLight.signal != R.drawable.no_signal
									&& ((myLight.remote_open == 1 && myLight.state == 0)
											|| (myLight.remote_close == 1 && myLight.state == 1))) {
								isOperating = true;
								dialog.show();
								//mService.scan(false);
								startScanAndCheck(false);
								switch (myLight.type) {
								case 0x11:
								case 0x13:
								case 1:
								case 3:
									onList.clear();
									turnOnOrOff(myLight);
									//turnOnOrOffLED(myLight);
									
									/*
									//会重做的
									currentRepeatTimes = 0;
									turnOnOrOffWithRedo(myLight);*/
									
									
									break;
								case 2:
									// 锁
									openLock(myLight);
									break;
								/*case 3:
									// led还没有可以
									// turnOn
									// Light light = lightList.get(p1);
									
									 * mService.setOperation(mService.LED);
									 * mService.setMyLight(myLight);
									 * mService.connect
									 * (mBluetoothAdapter.getRemoteDevice
									 * (myLight.address), false);
									 
									Button btn_brightness = (Button) view
											.findViewById(R.id.brightnessBtn);
										onList.clear();
										turnOnOrOff(myLight);
										//turnOnOrOffLED(myLight);
										//会重做的
										currentRepeatTimes = 0;
										turnOnOrOffWithRedo(myLight);
										
										mService.setOperation(mService.
										LED_OnOrOff);
										mService.setMyLight(myLight);
										mService.connect(mBluetoothAdapter.getRemoteDevice(myLight.address),false);
										 
									//}
									else {
										//dialog.setVisibility(View.GONE);
										
									}
									
									 * else {
									 * mService.setOperation(mService.LED);
									 * mService.setMyLight(myLight);
									 * mService.connect
									 * (mBluetoothAdapter.getRemoteDevice
									 * (myLight.address), false); }
									 

									break;*/
								case 4:
									isOperating = false;
									//dialog.show();
									startScanAndCheck(true);
									dialog.dismiss();
									break;
								}
							}
						}
					}
					/*
					 * mService.scan(true);
					 * deviceAdapter.notifyDataSetChanged();
					 */
				}
			}

		}

		return false;
	}

	private void controlWindow(Light light) {
		BluetoothDevice device = mBluetoothAdapter
				.getRemoteDevice(light.address);
		mService.setMyLight(light);
		mService.setOperation(BleService.WINDOW);
		mService.connect(device, false);
	}
	public void turnOnOrOff(Light light) {
		if (light.type == 1 || light.type == 3) {
			 BluetoothDevice device = mBluetoothAdapter
					.getRemoteDevice(light.address);
			mService.setMyLight(light);
			mService.setOperation(BleService.OnOrOff);
			mService.connect(device, false);
		} else if (light.type == 0x11 || light.type == 0x13) {
			// 连写跳变
			turnOnOrOffLED(light);
		}
	}

	public void turnOnOrOffLED(Light light) {
		mService.setOperation(BleService.
				LED_OnOrOff);
		mService.setMyLight(light);
		mService.connect(mBluetoothAdapter.getRemoteDevice(light.address),false);
	}
	
	public void turnOnOrOffWithRedo(Light light) {
		//isOperationDone = false;
		//mService.scan(true);
		Light l = findLight(myList, light);
		//l.isOperationDone = false;
		l.oldState = light.state;
		BluetoothDevice device = mBluetoothAdapter
				.getRemoteDevice(light.address);
		mService.setMyLight(light);
		mService.setOperation(BleService.OnOrOffWithRedo);
		mService.connect(device, false);

		System.out.println("turnOnOrOffWithRedo");
		/*new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
						Thread.sleep(1000);
						if (!(findLight(myList, mService.getMyLight()).isOperationDone)) {
							Bundle mBundle = new Bundle();
							Message msg = Message.obtain(
									mActivityHandler, TIMEOUT);
							msg.setData(mBundle);
							msg.sendToTarget();
						}
					
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}).start();*/
	}

	public void openLock(Light light) {
		// mService.scan(false);
		//isOperationDone = false;
		Light l = findLight(myList, light);
		if (l != null && (l.remote_open == 1 && light.state == 0)) {
			l.isOperationDone = false;
			BluetoothDevice device = mBluetoothAdapter
					.getRemoteDevice(light.address);
			mService.setMyLight(light);
			// light.oldState = light.state;
			mService.setOperation(BleService.UNLOCK);
			mService.connect(device, false);
			new Thread(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					try {
							Thread.sleep(4500);
							Light light = findLight(myList, mService.getMyLight());
							if (light != null && !light.isOperationDone) {
								Bundle mBundle = new Bundle();
								Message msg = Message.obtain(
										mActivityHandler, TIMEOUT);
								msg.setData(mBundle);
								msg.sendToTarget();
							}
						
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

			}).start();
		}
		
	}


	/**
	 * 点击title区域，显示或隐藏区域列表
	 */
	@Override
	public void onClick(View v) {
		if (areaListView.getVisibility() == View.GONE) {
			areaListView.setVisibility(View.VISIBLE);
			arrows.setImageResource(R.drawable.icon_up);
		} else {
			areaListView.setVisibility(View.GONE);
			arrows.setImageResource(R.drawable.icon_down);
		}
	}

	/**
	 * 选择区域
	 */
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Area area = areaList.get(position);
		
		areaSwitch(area);
		arrows.setImageResource(R.drawable.icon_down);
		/*areaSelected = area.area;
		
		if ("ALL".equals(areaSelected) || "全部".equals(areaSelected)) {
			areaSelected = getString(R.string.all);
		}
		
		tv_title.setText(area.area);
		areaListView.setVisibility(View.GONE);
		
		myList = findByArea(areaSelected);
		NumberList.clear();
		EnglishList.clear();
		ChineseList.clear();
		NoSignalList.clear();
		areaAdapter.notifyDataSetChanged();
		
		if (shakeList != null) {
			shakeList.clear();
		}
		if (shakeCopyList != null) {
			shakeCopyList.clear();
		}
		
		if (sensorLights != null) {
			sensorLights.clear();
		}
		if (closeList != null) {
			closeList.clear();
		}
		if (myList != null && myList.size() != 0 && sensorLights != null && closeList != null) {
			boolean isExit = false;
			for (Light l : myList) {
				if (l.shake_open == 1 || l.shake_close == 1) {
					sensorLights.add(l);
					isExit = true;
				}
				if (l.close_open == 1 || l.close_close == 1) {
					closeList.add(l);
				}
				
				 * if (l.brightnessChangeable == 1 || l.type == 3) { View view =
				 * getListView().getChildAt(lightList.indexOf(l));
				 * view.findViewById
				 * (R.id.brightnessBtn).setVisibility(View.VISIBLE); }
				 
			}
		}
		lightList.clear();
		lightList.addAll(myList);
		NoSignalList.addAll(lightList);
		arrows.setImageResource(R.drawable.icon_up);*/
		
		
	}

	private void areaSwitch(Area area) {
		areaSelected = area.area;
		//arrows.setImageResource(R.drawable.icon_down);
		
		if ("ALL".equals(areaSelected) || "全部".equals(areaSelected)) {
			areaSelected = getString(R.string.all);
		}
		
		tv_title.setText(areaSelected);
		areaListView.setVisibility(View.GONE);
		
		myList = findByArea(areaSelected);
		NumberList.clear();
		EnglishList.clear();
		ChineseList.clear();
		NoSignalList.clear();
		//areaAdapter.notifyDataSetChanged();
		
		if (shakeList != null) {
			shakeList.clear();
		}
		if (shakeCopyList != null) {
			shakeCopyList.clear();
		}
		
		if (sensorLights != null) {
			sensorLights.clear();
		}
		if (closeList != null) {
			closeList.clear();
		}
		if (myList != null && myList.size() != 0 && sensorLights != null && closeList != null) {
			boolean isExit = false;
			for (Light l : myList) {
				if (l.shake_open == 1 || l.shake_close == 1) {
					sensorLights.add(l);
					isExit = true;
				}
				if (l.close_open == 1 || l.close_close == 1) {
					closeList.add(l);
				}
				/*
				 * if (l.brightnessChangeable == 1 || l.type == 3) { View view =
				 * getListView().getChildAt(lightList.indexOf(l));
				 * view.findViewById
				 * (R.id.brightnessBtn).setVisibility(View.VISIBLE); }
				 */
			}
		}
		lightList.clear();
		lightList.addAll(myList);
		NoSignalList.addAll(lightList);
		
		if (lightList.size() > 0) {
			noDevice.setVisibility(View.GONE);
			deviceListView.setVisibility(View.VISIBLE);
			deviceAdapter.notifyDataSetChanged();
		} else {
			noDevice.setVisibility(View.VISIBLE);
			deviceListView.setVisibility(View.GONE);
		}
		
	}
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
		if (newConfig.orientation==Configuration.ORIENTATION_LANDSCAPE) {
	           // Nothing need to be done here
	            
	        } else {
	           // Nothing need to be done here
	        }
	}

	private void startScanAndCheck(boolean isStart) {
		if (mService != null) {
			mService.scan(isStart);
		}
		
		thread.setCheckThisTime(isStart);
	}
	private boolean mIsCheck = true;
	class checkDeviceIsCloseThread extends Thread {

		// LED会因为在控制亮度时超过5秒而导致searchTimes为0
		private boolean isStart = false;
		private boolean isCheckThisTime = true;
		public void setStart(boolean isStart) {
			this.isStart = isStart;
		}
		public void setCheckThisTime(boolean isCheckThisTime) {
			this.isCheckThisTime = isCheckThisTime;
		}
		@Override
		public void run() {
			// TODO Auto-generated method stub
			// super.run();
			/*while (isStart) {
				System.out.println("checking");
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}*/
				if (isCheckThisTime && mIsCheck) {
					System.out.println("Checking if some is close......");
					LightToDeleteList.clear();
					for (Light l : lightList) {
						if (l.signal != R.drawable.no_signal) {
							if (l.searchTimes == 0) {
								if (l.isOperationDone && l.isCheckThisTime) {
									LightToDeleteList.add(l);
									
								}
							} else {
								l.searchTimes = 0;
							}
						}
					}
					for (Light l : LightToDeleteList) {
						lightList.remove(l);
						if (l.isValidate == 1) {
							l.signal = R.drawable.no_signal;
							if (l.type == 4) {
								l.picture = R.drawable.curtain_no_signal;
							}
							
							Light light = findLight(myList, l);
							if (light != null) {
								light.version = l.version;
								lightMgr.update(light);
							}
							
							boolean isNumberLight = isNumber(l.name);
							boolean isEnglishLight = isEnglesh(l.name);
							if (isNumberLight) {
								NumberList.remove(l);
							} else if (isEnglishLight) {
								EnglishList.remove(l);
							} else {
								ChineseList.remove(l);
							}
							int size = NumberList.size() + EnglishList.size() + ChineseList.size() + NoSignalList.size();
							if (size >=0 && size <= lightList.size()) {
								lightList.add(size, l);
							}
							else {
								System.out.println("checkThread 产生严重错误 size is " + size + "lightList.size is " + lightList.size());
							}
								
							//lightList.add(l);
							// NoSignalList = new ArrayList<Light>();
							NoSignalList.add(l);
						}
						
					}
					if (lightList.size() == 0) {
						runOnUiThread(new Runnable() {

							@Override
							public void run() {
								// TODO Auto-generated method stub
								noDevice.setVisibility(View.VISIBLE);
							}
						});
						
					}
					if (LightToDeleteList.size() > 0) {
						System.out.println("Updating UI......");
						deviceAdapter.notifyDataSetChanged();
						/*runOnUiThread(new Runnable () {

							@Override
							public void run() {
								// TODO Auto-generated method stub
								deviceAdapter.notifyDataSetChanged();
							}
						});*/
					}
				}
				checkHandler.postDelayed(thread, 20000);
			}
			//System.out.println("............exiting checkThread");
		//}
	}
	public class WindowControlThread extends Thread {

		private boolean mIsRunning = true;
		private byte []mData;
		public WindowControlThread(byte []data) {
			mData = data;
		}
		public boolean isRunning() {
			return mIsRunning;
		}

		public void setRunning(boolean mIsRunning) {
			this.mIsRunning = mIsRunning;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			super.run();
			while (mIsRunning) {
				if (mService != null) {
					mService.write(mData, BleService.MY_TIMER_CHARACTERISTIC);
				}
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		}
		
	}
	public void switchLanguage(Locale locale) {
        Configuration config = getResources().getConfiguration();// 获得设置对象
        Resources resources = getResources();// 获得res资源对象
        DisplayMetrics dm = resources.getDisplayMetrics();// 获得屏幕参数：主要是分辨率，像素等。
        config.locale = locale; // 简体中文
        resources.updateConfiguration(config, dm);
	}
}
