
package com.bde.light.service;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.bde.light.activity.R;
import com.bde.light.model.Light;
import com.bde.light.utils.NumConversion;

public class BleService extends Service {

    public static final UUID MYUUID = UUID.fromString("0000FFE0-0000-1000-8000-00805f9b34fb");
    public static final UUID MYPASSWORDCHARACTERISTIC = UUID.fromString("0000ffa1-0000-1000-8000-00805f9b34fb");
    public static final UUID CCC = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    public static final UUID MY_UPDATE_NAME_OR_INIT_PASSWORD_CHARACTERISTIC = UUID.fromString("0000ffe4-0000-1000-8000-00805f9b34fb");
    public static final UUID MY_UPDATE_PASSWORD_CHARACTERISTIC = UUID.fromString("0000ffA0-0000-1000-8000-00805f9b34fb");
    public static final UUID MY_TIMER_CHARACTERISTIC = UUID.fromString("0000ffe4-0000-1000-8000-00805f9b34fb");
    
    /** Source of device entries in the device list */
    static final String TAG = "BleService";
    //验证密码
    public static final int GATT_LIGHT_VALIDATE_MSG = 4;
    //发现设备
    public static final int GATT_DEVICE_FOUND_MSG = 5;
    
    //验证密码
    public static final int VALIDDATE = 6;
    //修改名字
    public static final int UPDATE_NAME = 7;
    //修改区域
    public static final int UPDATE_AREA = 8;
    //修改密码
    public static final int UPDATE_PASSWORD = 9;
    //初始化密码
    public static final int INIT_PASSWORD = 10;
    //连接
    public static final int CONNECTION = 11;
    //全开
    public static final int ALL_ON = 12;
    //全关
    public static final int ALL_OFF = 13;
    //晃动开关
    public static final int SHAKE = 14;
    //接近开关
    public static final int CLOSE = 15;
    //定时器
    public static final int TIMER = 16;
    
    public static final int OnOrOff = 17;
    
    public static final int UNLOCK = 18;
    
    public static final int ALL_ON_OR_OFF = 19;
    
    public static final int LED = 20;
    
    public static final int LED_CONNECTED = 22;
    
    public static final int LED_OnOrOff = 23;
    
    public static final int OnOrOffWithRedo = 24;
    
    public static final int DISCONNECTED = 25;
    
    public static final int WINDOW = 26;
    
    public static final int WINDOW_CONNECT = 27;
    
    public static final int WindowControlReady = 28;
    
    public static final int WINDOW_RESULT = 29;
    
    public static final int WINDOW_FINISH = 30;
    
	public static final String RESULT = "result";
	public static final String SUCCESS = "success";
	public static final String FAIL = "fail";
	
    private BluetoothAdapter mBtAdapter = null;
    public BluetoothGatt mBluetoothGatt = null;
    private Handler mDeviceListHandler = null;
    private Handler mActivityHandler = null;
    private Handler mTimerHandler = null;
    //private Queue rssiQueue = new LinkedList();
    private HashMap<String, Queue> avgRssi;
    
    private String mBluetoothDeviceAddress;
    
    private Light myLight;
    //操作
    private int operation;
    private int size;
    private byte[] timerData;

	public byte[] getTimerData() {
		return timerData;
	}

	public void setTimerData(byte[] timerData) {
		this.timerData = timerData;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public int getOperation() {
		return operation;
	}

	public void setOperation(int operation) {
		this.operation = operation;
	}


    /**
     * Profile service connection listener
     */
    public class LocalBinder extends Binder {
        public BleService getService() {
            return BleService.this;
        }
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return binder;
    }
    

	@Override
	public boolean onUnbind(Intent intent) {
		// TODO Auto-generated method stub
		return super.onUnbind(intent);
	}


	private final IBinder binder = new LocalBinder();
	
	@Override
    public void onCreate() {
        /*if (mBtAdapter == null) {
            mBtAdapter = BluetoothAdapter.getDefaultAdapter();
            if (mBtAdapter == null)
                return;
        }

        if (mBluetoothGatt == null) {
            BluetoothAdapter.getProfileProxy(this, mProfileServiceListener, BluetoothProfile.GATT);
        }*/
		final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		mBtAdapter = bluetoothManager.getAdapter();
        //list = new ArrayList<Light>();
        avgRssi = new HashMap<String, Queue>();
        //restartHandler.postDelayed(thread, 10000);
    }
    
	public void setActivityHandler(Handler mHandler) {
        Log.d(TAG, "Activity Handler set");
        mActivityHandler = mHandler;
    }
	
	public void setDeviceListHandler(Handler mHandler) {
        Log.d(TAG, "Device List Handler set");
        mDeviceListHandler = mHandler;
    }
	public void setTimerHandler(Handler mHandler) {
        Log.d(TAG, "Device List Handler set");
        mTimerHandler = mHandler;
    }
	
	

    public Light getMyLight() {
		return myLight;
	}

	public void setMyLight(Light myLight) {
		this.myLight = myLight;
	}

	@Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy() called");
        /*if (mBtAdapter != null && mBluetoothGatt != null) {
        	BluetoothAdapter.closeProfileProxy(BluetoothAdapter.GATT, mBluetoothGatt);
        }*/
        /*if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;*/
        this.close();
        super.onDestroy();
    }

    /*private BluetoothProfile.ServiceListener mProfileServiceListener = new BluetoothProfile.ServiceListener() {
        @Override
        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            if (profile == BluetoothGattAdapter.GATT) {
                mBluetoothGatt = (BluetoothGatt) proxy;
                mBluetoothGatt.registerApp(mGattCallbacks);
            }
        }

        @Override
        public void onServiceDisconnected(int profile) {
            if (profile == BluetoothGattAdapter.GATT) {
                if (mBluetoothGatt != null)
                    mBluetoothGatt.unregisterApp();

                mBluetoothGatt = null;
            }
        }
    };*/

    private BluetoothGattCallback mGattCallbacks = new BluetoothGattCallback() {

		@Override
		public void onConnectionStateChange(BluetoothGatt gatt, int status,
				int newState) {
			// TODO Auto-generated method stub
			//super.onConnectionStateChange(gatt, status, newState);
			Log.d(TAG, "onConnectionStateChange (" + gatt.getDevice().getAddress() + ")");
            int oper = getOperation();
            if (newState == BluetoothProfile.STATE_CONNECTED && mBluetoothGatt != null) {
            	Log.d(TAG, "connected...");
            	//连接上远程设备了
            	Light light = getMyLight();
            	if ((oper == OnOrOff || ((light.type == 1 || light.type == 3) && oper == ALL_ON_OR_OFF) || (oper == SHAKE && (light.type == 1 || light.type == 3)) || oper == OnOrOffWithRedo) /*&& (getMyLight().type == 1)CONNECTION*/) {
            		//Light light = getMyLight();
            		if (light.isValidate == 1) {
            			disconnect();
                	}
            	}
            	else {
            		//发现服务
            		Log.d(TAG, "Discovering Service...");
                	boolean discoverService = mBluetoothGatt.discoverServices();
                	System.out.println("discoverServices: " + discoverService);
            	}
            }
            if (/*false && */newState == BluetoothProfile.STATE_DISCONNECTED) {
            	//断开连接了
            	Log.d(TAG, "disconnected...");
            	/*mBluetoothGatt.close();
            	mBluetoothGatt = null;*/
            	close();
            	Bundle mBundle =  new Bundle();
            	Message msg = null;
            	switch(oper) {
            	//全开
            	case ALL_ON_OR_OFF:
    				msg = Message.obtain(mActivityHandler, ALL_ON_OR_OFF);
	                msg.setData(mBundle);
	                msg.sendToTarget();
            		break;
            	//摇晃开关操作
            	case SHAKE: 
            		new Thread(new Runnable() {
						public void run() {
							try {
								Thread.sleep(300);
								//setSize(getSize()+1);
			            		Bundle mBundle = new Bundle();
			    				Message msg = Message.obtain(mActivityHandler, SHAKE);
				                msg.setData(mBundle);
				                msg.sendToTarget();
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
					}).start();
            		break;
            	case OnOrOff:
    				msg = Message.obtain(mActivityHandler, OnOrOff);
	                msg.setData(mBundle);
	                msg.sendToTarget();
	                break;
	                
            	case UNLOCK:
            		msg = Message.obtain(mActivityHandler, UNLOCK);
	                msg.setData(mBundle);
	                msg.sendToTarget();
	                break;
            	case LED_OnOrOff:
            		Log.d(TAG, "single LED is disconnected.OK");
            		msg = Message.obtain(mActivityHandler, LED_OnOrOff);
	                msg.setData(mBundle);
	                msg.sendToTarget();
	                break;
            	case LED:
            		msg = Message.obtain(mActivityHandler, LED);
	                msg.setData(mBundle);
	                msg.sendToTarget();
	                break;
	                
            	case OnOrOffWithRedo:
    				msg = Message.obtain(mActivityHandler, OnOrOffWithRedo);
	                msg.setData(mBundle);
	                msg.sendToTarget();
	                break;
            	/*case TIMER:
            		msg = Message.obtain(mActivityHandler, DISCONNECTED);
	                msg.setData(mBundle);
	                msg.sendToTarget();*/
                case WINDOW:
            		msg = Message.obtain(mActivityHandler, WINDOW);
	                msg.setData(mBundle);
	                msg.sendToTarget();
            	}
            }
		}

		@Override
		public void onServicesDiscovered(BluetoothGatt gatt, int status) {
			// TODO Auto-generated method stub
			//super.onServicesDiscovered(gatt, status);
			int oper = getOperation();
        	BluetoothGattCharacteristic mChara = null;
        	byte[] buf = {0x09};
        	Log.v(TAG, "Services Disconverd...");
        	switch(oper) {
        	//验证密码
        	case VALIDDATE:
        		mChara = getBluetoothGattCharacteristic(MYPASSWORDCHARACTERISTIC);
        		if (mChara == null) {
        			Log.v(TAG, "Characteristic is null.EnableNotification stop...");
        			return;
        		}
        		if (getMyLight().isValidate == 0) {
    				if (enableNotification(mChara, true)) {
    					Log.v(TAG, "enableNotification is ok...");
    				}
    				else {
    					Log.v(TAG, "enableNotification failed...");
    				}
    			}
        		/*new Thread() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						//super.run();
						try {
							Thread.sleep(2000);
							Bundle mBundle = new Bundle();
							Message msg = Message.obtain(mActivityHandler, VALIDDATE);
			                mBundle.putInt(RESULT, buffer[0]);
			                msg.setData(mBundle);
			                msg.sendToTarget();
			            	break;
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
        			
        		}.start();*/
        		break;
        	//修改名字
        	case UPDATE_NAME:
        		mChara = getBluetoothGattCharacteristic(MY_UPDATE_NAME_OR_INIT_PASSWORD_CHARACTERISTIC);
        		if (mChara == null) {
        			Log.v(TAG, "Characteristic is null.EnableNotification stop...");
        			return;
        		}
        		if (enableNotification(mChara, true)) {
					Log.v(TAG, "enableNotification is ok...");
				}
        		else {
					Log.v(TAG, "enableNotification failed...");
				}
        		break;
        	//修改密码	
        	case UPDATE_PASSWORD:
        		mChara = getBluetoothGattCharacteristic(MY_UPDATE_PASSWORD_CHARACTERISTIC);
        		if (mChara == null) {
        			Log.v(TAG, "Characteristic is null.EnableNotification stop...");
        			return;
        		}
        		if (enableNotification(mChara, true)) {
					Log.v(TAG, "enableNotification is ok...");
				}
        		else {
					Log.v(TAG, "enableNotification failed...");
				}
        		break;
        	//初始化密码
        	case INIT_PASSWORD:
        		Light light = getMyLight();
        		byte[] b2 = new byte[1];
				b2[0] = 0x04;
				Bundle mBundle = new Bundle();
				Message msg = null;
				if (write(b2, MY_UPDATE_NAME_OR_INIT_PASSWORD_CHARACTERISTIC)) {
					System.out.println("init-password: OK");
					msg = Message.obtain(mActivityHandler, INIT_PASSWORD);
	                mBundle.putString(RESULT, SUCCESS);
	                msg.setData(mBundle);
	                msg.sendToTarget();
				} else {
					System.out.println("init-password: NO");
					msg = Message.obtain(mActivityHandler, INIT_PASSWORD);
	                mBundle.putString(RESULT, FAIL);
	                msg.setData(mBundle);
	                msg.sendToTarget();
				}
        		break;
        	//定时器	
        	case TIMER:
        		mChara = getBluetoothGattCharacteristic(MY_TIMER_CHARACTERISTIC);
        		if (mChara == null) {
        			Log.v(TAG, "Characteristic is null.EnableNotification stop...");
        			return;
        		}
        		if (enableNotification(mChara, true)) {
					Log.v(TAG, "enableNotification is ok...");
				}
        		else {
					Log.v(TAG, "enableNotification failed...");
				}
        		break;
        	case UNLOCK:
				//buf[0] = 0x09;
				write(buf,MY_TIMER_CHARACTERISTIC);
				break;
        	case LED:
        		mChara = getBluetoothGattCharacteristic(MY_TIMER_CHARACTERISTIC);
        		if (mChara == null) {
        			Log.v(TAG, "Characteristic is null.EnableNotification stop...");
        			return;
        		}
        		if (enableNotification(mChara, true)) {
					Log.v(TAG, "LED enableNotification is ok...");
					msg = Message.obtain(mActivityHandler, LED_CONNECTED);
	                msg.sendToTarget();
				}
        		else {
					Log.v(TAG, "enableNotification failed...");
				}
        		break;
        	case SHAKE:
        	case ALL_ON_OR_OFF:
        	case LED_OnOrOff:
        		Log.v(TAG, "single LED is writting 0x0A to new devices");
        		
        		byte brightness = 0;
        		/*if (getMyLight().state > 0)
        			brightness = 0;
        		else if (getMyLight().state == 0)
        			brightness = 99;
        		setBrightness(brightness);*/
        		//disconnect(mBtAdapter.getRemoteDevice(myLight.address));
        		
        		write(new byte[]{0x0A}, MY_TIMER_CHARACTERISTIC);
        		
        		
        		
        		break;
        	/*case ALL_ON_OR_OFF:
        		disconnect();
        		break;*/
        		
        	/*case SHAKE:
        		if (getMyLight().type == 2) {
    				write(mBtAdapter.getRemoteDevice(getMyLight().address), buf,MY_TIMER_CHARACTERISTIC);
    				break;
        		}
        		brightness = 0;
        		if (getMyLight().state > 0)
        			brightness = 0;
        		else if (getMyLight().state == 0)
        			brightness = 99;
        		setBrightness(brightness);
        		//else
                //write(mBtAdapter.getRemoteDevice(getMyLight().address), buf,MY_TIMER_CHARACTERISTIC);
                break;*/
        	case WINDOW:
        		mChara = getBluetoothGattCharacteristic(MY_TIMER_CHARACTERISTIC);
        		if (mChara == null) {
        			Log.v(TAG, "Characteristic is null.EnableNotification stop...");
        			return;
        		}
        		if (enableNotification(mChara, true)) {
					Log.v(TAG, "Window enableNotification is ok...");
					msg = Message.obtain(mActivityHandler, WINDOW_CONNECT);
	                msg.sendToTarget();
				}
        		else {
					Log.v(TAG, "enableNotification failed...");
				}
        		break;
        	}
		}

		@Override
		public void onCharacteristicRead(BluetoothGatt gatt,
				BluetoothGattCharacteristic characteristic, int status) {
			// TODO Auto-generated method stub
			super.onCharacteristicRead(gatt, characteristic, status);
		}

		@Override
		public void onCharacteristicWrite(BluetoothGatt gatt,
				BluetoothGattCharacteristic characteristic, int status) {
			// TODO Auto-generated method stub
			//super.onCharacteristicWrite(gatt, characteristic, status);
			byte[] buffer = characteristic.getValue();
        	//System.out.println("String_buffer" + ": " +  );
        	int len = buffer.length;
        	for(int i = 0; i < len; i++) {
        		System.out.println("write_buffer" + i + ": " + buffer[i]);
        	}
        	int oper = getOperation();
        	switch (oper) {
        	case VALIDDATE:
        		//updateName
        		break;
        	case UNLOCK:
        		//解锁完毕
        		disconnect();
        		break;
        	case LED_OnOrOff:
        		//disconnect();
        		break;
        	case WINDOW:
        		Message msg = Message.obtain(mActivityHandler, WINDOW_FINISH);
                msg.sendToTarget();
        		break;
        	default:
        		//disconnect(mBtAdapter.getRemoteDevice(myLight.address));
        		break;
        	}
		}

		@Override
		public void onCharacteristicChanged(BluetoothGatt gatt,
				BluetoothGattCharacteristic characteristic) {
			// TODO Auto-generated method stub
			//super.onCharacteristicChanged(gatt, characteristic);
			Log.v(TAG, "onCharacteristicChanged is called!");
			int oper = getOperation();
	        if (oper == INIT_PASSWORD) {
	        	Log.e("BleService", "INIT_PASSWORD");
	        	Log.e("BleService", "DATA is ");
	        }
        	byte[] buffer = characteristic.getValue();
            StringBuilder sb = new StringBuilder(buffer.length * 2);
        	for (byte b : buffer) {
        		sb.append(b + " ");
        	}
        	System.out.println(sb.toString());
        	
        	Bundle mBundle = new Bundle();
        	Message msg = null;
        	switch(oper) {
        	case VALIDDATE:
        		//发送验证的结果
        		Log.v(TAG, "验证完毕");
        		System.out.println("buffer[0] is " + buffer[0]);
        		/*if (buffer[0] == 1) {
        			byte[] bb = myLight.name.getBytes();
    				int len = bb.length;
    				byte[] b1 = new byte[2+len];
    				b1[0] = 0x05;
    				b1[1] = (byte) len;
    				for (int i = 2; i < len + 2; i++) {
    					b1[i] = bb[i-2];
    				}
    				write(b1, MY_UPDATE_NAME_OR_INIT_PASSWORD_CHARACTERISTIC);
        		} else {*/
        			msg = Message.obtain(mActivityHandler, VALIDDATE);
                    mBundle.putInt(RESULT, buffer[0]);
                    msg.setData(mBundle);
                    msg.sendToTarget();
        		//}
            	
            	break;
            	
        	case UPDATE_NAME:
                msg = Message.obtain(mActivityHandler, UPDATE_NAME);
                mBundle.putInt(RESULT, buffer[0]);
                msg.setData(mBundle);
                msg.sendToTarget();
        		break;
        		
        	case UPDATE_PASSWORD:
                msg = Message.obtain(mActivityHandler, UPDATE_PASSWORD);
                mBundle.putInt(RESULT, buffer[0]);
                msg.setData(mBundle);
                msg.sendToTarget();
        		break;
        		
        	case TIMER:
        		msg = Message.obtain(mTimerHandler, TIMER);
        		mBundle.putSerializable(RESULT, buffer);
                msg.setData(mBundle);
                msg.sendToTarget();
        		break;
        	case WINDOW:
        		msg = Message.obtain(mActivityHandler, WINDOW_RESULT);
        		mBundle.putSerializable(RESULT, buffer);
                msg.setData(mBundle);
                msg.sendToTarget();
                break;
                

        	}
		}

		@Override
		public void onDescriptorRead(BluetoothGatt gatt,
				BluetoothGattDescriptor descriptor, int status) {
			// TODO Auto-generated method stub
			super.onDescriptorRead(gatt, descriptor, status);
		}

		@Override
		public void onDescriptorWrite(BluetoothGatt gatt,
				BluetoothGattDescriptor descriptor, int status) {
			// TODO Auto-generated method stub
			//super.onDescriptorWrite(gatt, descriptor, status);
			//此回调是Descriptor写完后才调用的，arg1是写了Descriptor后的结果，即开启了notification后调用
        	Log.v(TAG, "writting data to remote device!");
        	Light light = getMyLight();
        	int operation = getOperation();
			switch (operation) {
			case VALIDDATE:
				byte[] buf = new byte[3];
				buf = NumConversion.stringToBytes(light.password,buf);
				//不修改name版本
				write(buf,MYPASSWORDCHARACTERISTIC);
				break;
				
			case UPDATE_NAME:
				byte[] bb = light.name.getBytes();
				int len = bb.length;
				byte[] b1 = new byte[2+len];
				b1[0] = 0x05;
				b1[1] = (byte) len;
				
				for (int i = 2; i < len + 2; i++) {
					b1[i] = bb[i-2];
				}
				write(b1, MY_UPDATE_NAME_OR_INIT_PASSWORD_CHARACTERISTIC);
				break;
				
			case UPDATE_PASSWORD:
				buf = new byte[3];
				buf = NumConversion.stringToBytes(light.password,buf);
				write(buf, MY_UPDATE_PASSWORD_CHARACTERISTIC);
        		break;
        		
			case TIMER:
				write(getTimerData(), MY_TIMER_CHARACTERISTIC);
				break;
			}
		}
    	
    };
    /**
     * GATT client callbacks
     */
    /*private BluetoothGattCallback mGattCallbacks = new BluetoothGattCallback() {
    	
        @Override
        public void onScanResult(BluetoothDevice device, int rssi, byte[] scanRecord) {
        	Light light = selectDevice(scanRecord,device,rssi);
        	if (light != null) {
        		sendMessage(light,GATT_DEVICE_FOUND_MSG);
        	}
        	
        }

    	@Override
        public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
        	//连接或断开连接时被调用
            Log.d(TAG, "onConnectionStateChange (" + device.getAddress() + ")");
            int oper = getOperation();
            if (newState == BluetoothProfile.STATE_CONNECTED && mBluetoothGatt != null) {
            	Log.d(TAG, "connected...");
            	//连接上远程设备了
            	if ((oper == OnOrOff || oper == ALL_ON_OR_OFF || oper == SHAKE || oper == OnOrOffWithRedo) && (getMyLight().type == 1)CONNECTION) {
            		Light light = getMyLight();
            		if (light.isValidate == 1) {
            			disconnect(device);
                	}
            	}
            	else {
            		//发现服务
            		Log.d(TAG, "Discovering Service...");
                	boolean discoverService = mBluetoothGatt.discoverServices(device);
                	System.out.println("discoverServices: " + discoverService);
            	}
            }
            if (newState == BluetoothProfile.STATE_DISCONNECTED && mBluetoothGatt != null) {
            	//断开连接了
            	Log.d(TAG, "disconnected...");
            	Bundle mBundle =  new Bundle();
            	Message msg = null;
            	switch(oper) {
            	//全开
            	case ALL_ON_OR_OFF:
    				msg = Message.obtain(mActivityHandler, ALL_ON_OR_OFF);
	                msg.setData(mBundle);
	                msg.sendToTarget();
            		break;
            	//摇晃开关操作
            	case SHAKE: 
            		new Thread(new Runnable() {
						public void run() {
							try {
								Thread.sleep(300);
								//setSize(getSize()+1);
			            		Bundle mBundle = new Bundle();
			    				Message msg = Message.obtain(mActivityHandler, SHAKE);
				                msg.setData(mBundle);
				                msg.sendToTarget();
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
					}).start();
            		break;
            	case OnOrOff:
    				msg = Message.obtain(mActivityHandler, OnOrOff);
	                msg.setData(mBundle);
	                msg.sendToTarget();
	                break;
	                
            	case UNLOCK:
            		msg = Message.obtain(mActivityHandler, UNLOCK);
	                msg.setData(mBundle);
	                msg.sendToTarget();
	                break;
            	case LED_OnOrOff:
            		Log.d(TAG, "single LED is disconnected.OK");
            		msg = Message.obtain(mActivityHandler, LED_OnOrOff);
	                msg.setData(mBundle);
	                msg.sendToTarget();
	                break;
            	case LED:
            		msg = Message.obtain(mActivityHandler, LED);
	                msg.setData(mBundle);
	                msg.sendToTarget();
	                break;
	                
            	case OnOrOffWithRedo:
    				msg = Message.obtain(mActivityHandler, OnOrOffWithRedo);
	                msg.setData(mBundle);
	                msg.sendToTarget();
	                break;
            	}
            }
            
        }

        @Override
        public void onServicesDiscovered(BluetoothDevice device, int status) {
        	int oper = getOperation();
        	BluetoothGattCharacteristic mChara = null;
        	byte[] buf = {0x09};
        	Log.v(TAG, "Services Disconverd...");
        	switch(oper) {
        	//验证密码
        	case VALIDDATE:
        		mChara = getBluetoothGattCharacteristic(device,MYPASSWORDCHARACTERISTIC);
        		if (getMyLight().isValidate == 0) {
    				if (enableNotification(mChara, true)) {
    					Log.v(TAG, "enableNotification is ok...");
    				}
    				else {
    					Log.v(TAG, "enableNotification failed...");
    				}
    			}
        		new Thread() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						//super.run();
						try {
							Thread.sleep(2000);
							Bundle mBundle = new Bundle();
							Message msg = Message.obtain(mActivityHandler, VALIDDATE);
			                mBundle.putInt(RESULT, buffer[0]);
			                msg.setData(mBundle);
			                msg.sendToTarget();
			            	break;
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
        			
        		}.start();
        		break;
        	//修改名字
        	case UPDATE_NAME:
        		mChara = getBluetoothGattCharacteristic(device,MY_UPDATE_NAME_OR_INIT_PASSWORD_CHARACTERISTIC);
        		if (enableNotification(mChara, true)) {
					Log.v(TAG, "enableNotification is ok...");
				}
        		else {
					Log.v(TAG, "enableNotification failed...");
				}
        		break;
        	//修改密码	
        	case UPDATE_PASSWORD:
        		mChara = getBluetoothGattCharacteristic(device,MY_UPDATE_PASSWORD_CHARACTERISTIC);
        		if (enableNotification(mChara, true)) {
					Log.v(TAG, "enableNotification is ok...");
				}
        		else {
					Log.v(TAG, "enableNotification failed...");
				}
        		break;
        	//初始化密码
        	case INIT_PASSWORD:
        		Light light = getMyLight();
        		byte[] b2 = new byte[1];
				b2[0] = 0x04;
				Bundle mBundle = new Bundle();
				Message msg = null;
				if (write(mBtAdapter.getRemoteDevice(light.address), b2, MY_UPDATE_NAME_OR_INIT_PASSWORD_CHARACTERISTIC)) {
					System.out.println("init-password: OK");
					msg = Message.obtain(mActivityHandler, INIT_PASSWORD);
	                mBundle.putString(RESULT, SUCCESS);
	                msg.setData(mBundle);
	                msg.sendToTarget();
				} else {
					System.out.println("init-password: NO");
					msg = Message.obtain(mActivityHandler, INIT_PASSWORD);
	                mBundle.putString(RESULT, FAIL);
	                msg.setData(mBundle);
	                msg.sendToTarget();
				}
        		break;
        	//定时器	
        	case TIMER:
        		mChara = getBluetoothGattCharacteristic(device,MY_TIMER_CHARACTERISTIC);
        		if (enableNotification(mChara, true)) {
					Log.v(TAG, "enableNotification is ok...");
				}
        		else {
					Log.v(TAG, "enableNotification failed...");
				}
        		break;
        	case UNLOCK:
				//buf[0] = 0x09;
				write(mBtAdapter.getRemoteDevice(getMyLight().address), buf,MY_TIMER_CHARACTERISTIC);
				break;
        	case LED:
        		mChara = getBluetoothGattCharacteristic(device,MY_TIMER_CHARACTERISTIC);
        		if (enableNotification(mChara, true)) {
					Log.v(TAG, "LED enableNotification is ok...");
					msg = Message.obtain(mActivityHandler, LED_CONNECTED);
	                msg.sendToTarget();
				}
        		else {
					Log.v(TAG, "enableNotification failed...");
				}
        		break;
        	case LED_OnOrOff:
        		Log.v(TAG, "single LED is disconnecting");
        		
        		byte brightness = 0;
        		if (getMyLight().state > 0)
        			brightness = 0;
        		else if (getMyLight().state == 0)
        			brightness = 99;
        		setBrightness(brightness);
        		//disconnect(mBtAdapter.getRemoteDevice(myLight.address));
        		break;
        	case ALL_ON_OR_OFF:
        		disconnect(mBtAdapter.getRemoteDevice(myLight.address));
        		break;
        		
        	case SHAKE:
        		if (getMyLight().type == 2) {
    				write(mBtAdapter.getRemoteDevice(getMyLight().address), buf,MY_TIMER_CHARACTERISTIC);
    				break;
        		}
        		brightness = 0;
        		if (getMyLight().state > 0)
        			brightness = 0;
        		else if (getMyLight().state == 0)
        			brightness = 99;
        		setBrightness(brightness);
        		//else
                //write(mBtAdapter.getRemoteDevice(getMyLight().address), buf,MY_TIMER_CHARACTERISTIC);
        	}
			
        }
        
        public void onDescriptorWrite(BluetoothGattDescriptor descriptor, int arg1) {
        	//此回调是Descriptor写完后才调用的，arg1是写了Descriptor后的结果，即开启了notification后调用
        	Log.v(TAG, "writting data to remote device!");
        	Light light = getMyLight();
        	int operation = getOperation();
			switch (operation) {
			case VALIDDATE:
				byte[] buf = new byte[3];
				buf = NumConversion.stringToBytes(light.password,buf);
				write(mBtAdapter.getRemoteDevice(light.address), buf,MYPASSWORDCHARACTERISTIC);
				break;
				
			case UPDATE_NAME:
				byte[] bb = light.name.getBytes();
				int len = bb.length;
				byte[] b1 = new byte[2+len];
				b1[0] = 0x05;
				b1[1] = (byte) len;
				
				for (int i = 2; i < len + 2; i++) {
					b1[i] = bb[i-2];
				}
				write(mBtAdapter.getRemoteDevice(light.address), b1, MY_UPDATE_NAME_OR_INIT_PASSWORD_CHARACTERISTIC);
				break;
				
			case UPDATE_PASSWORD:
				buf = new byte[3];
				buf = NumConversion.stringToBytes(light.password,buf);
				write(mBtAdapter.getRemoteDevice(light.address), buf, MY_UPDATE_PASSWORD_CHARACTERISTIC);
        		break;
        		
			case TIMER:
				write(mBtAdapter.getRemoteDevice(light.address), getTimerData(), MY_TIMER_CHARACTERISTIC);
				break;
			}
        }

        public void onCharacteristicChanged(BluetoothGattCharacteristic characteristic) {
        	Log.v(TAG, "onCharacteristicChanged is called!");
        	byte[] buffer = characteristic.getValue();
        	int oper = getOperation();
        	Bundle mBundle = new Bundle();
        	Message msg = null;
        	switch(oper) {
        	case VALIDDATE:
        		//发送验证的结果
        		Log.v(TAG, "验证完毕");
            	msg = Message.obtain(mActivityHandler, VALIDDATE);
                mBundle.putInt(RESULT, buffer[0]);
                msg.setData(mBundle);
                msg.sendToTarget();
            	break;
            	
        	case UPDATE_NAME:
                msg = Message.obtain(mActivityHandler, UPDATE_NAME);
                mBundle.putInt(RESULT, buffer[0]);
                msg.setData(mBundle);
                msg.sendToTarget();
        		break;
        		
        	case UPDATE_PASSWORD:
                msg = Message.obtain(mActivityHandler, UPDATE_PASSWORD);
                mBundle.putInt(RESULT, buffer[0]);
                msg.setData(mBundle);
                msg.sendToTarget();
        		break;
        		
        	case TIMER:
        		msg = Message.obtain(mTimerHandler, TIMER);
        		mBundle.putSerializable(RESULT, buffer);
                msg.setData(mBundle);
                msg.sendToTarget();
        		break;
        	}
        }
        
        public void onCharacteristicWrite(BluetoothGattCharacteristic characteristic, int arg1) {
        	byte[] buffer = characteristic.getValue();
        	//System.out.println("String_buffer" + ": " +  );
        	int len = buffer.length;
        	for(int i = 0; i < len; i++) {
        		System.out.println("write_buffer" + i + ": " + buffer[i]);
        	}
        	int oper = getOperation();
        	switch (oper) {
        	case UNLOCK:
        		//解锁完毕
        		disconnect(mBtAdapter.getRemoteDevice(myLight.address));
        		break;
        	case LED_OnOrOff:
        		disconnect(mBtAdapter.getRemoteDevice(myLight.address));
        	default:
        		//disconnect(mBtAdapter.getRemoteDevice(myLight.address));
        		break;
        	}
        }
        
        
    };*/

    
    public BluetoothGattCharacteristic getBluetoothGattCharacteristic(UUID uuid) {
        BluetoothGattService mService = mBluetoothGatt.getService(MYUUID);
        if (mService == null) {
            Log.e(TAG, "service not found!");
            return null;
        }
        BluetoothGattCharacteristic mCharac = mService.getCharacteristic(uuid);
        if (mCharac == null) {
            Log.e(TAG, "charateristic not found!");
            return null;
        }
        
        return mCharac;
        
    }
    
    public boolean enableNotification(BluetoothGattCharacteristic characteristic, boolean enable) {
        if (mBluetoothGatt == null)
            return false;
        if (characteristic == null)
        	return false;
        if (!mBluetoothGatt.setCharacteristicNotification(characteristic, enable)){
        	return false;
        }

        BluetoothGattDescriptor clientConfig = characteristic.getDescriptor(CCC);
        
        if (clientConfig == null)
            return false;

        if (enable) {
             Log.i(TAG,"enable notification");
            clientConfig.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        } else {
            Log.i(TAG,"disable notification");
            clientConfig.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
        }
        return mBluetoothGatt.writeDescriptor(clientConfig);
    }
    
    /**
     * 写Characteristic
     * @param device
     * @param buffer
     */
    public boolean write(byte[] buffer,UUID uuid) {
    	if (mBluetoothGatt != null) {
            /*BluetoothGattService writeService = mBluetoothGatt.getService(MYUUID);
            if (writeService == null) {
                return false;
            }
            
            BluetoothGattCharacteristic writeCharacteristic = writeService.getCharacteristic(uuid);
            if (writeCharacteristic == null) {
            	Log.e(TAG, "writeCharacteristic is null");
                return false;
            }*/
    		BluetoothGattCharacteristic writeCharacteristic = getBluetoothGattCharacteristic(uuid);
    		if (writeCharacteristic == null) {
            	Log.e(TAG, "writeCharacteristic is null");
                return false;
            }
            writeCharacteristic.setValue(buffer);
            return mBluetoothGatt.writeCharacteristic(writeCharacteristic);
        }
    	else {
    		Log.e(TAG, "mBluetoothGatt or device is null");
    	}
    	return false;
    }
    
    /**
     *  筛选出提供灯、锁或者LED服务的设备，排除提供心率等设备
     */
    private Light selectDevice(byte[] scanRecord,BluetoothDevice device,int rssi) {
    	Light light = null;
    	int len = scanRecord.length;
    	int nameLen = 0;
    	//for(int i = 0; i < len; i++) {
    		//如果是HeartRate等服务，广播出来的信息不会符合
    		//scanRecord[i] == -1 && scanRecord[i+1] == 11 && scanRecord[i+2] == -34
    	int i = 4;
    		if (scanRecord[i] == -1 && scanRecord[i+1] == 11 && scanRecord[i+2] == -34)
    		{
    			//设备长度名的长度+len以后的、设备名称以前的长度
    			int a = 13 + scanRecord[i+12];
            	if (a == scanRecord[i-1]) {
            		light = new Light();
            		nameLen = scanRecord[i+12];
            		light.type = scanRecord[i+3];
            		light.state = scanRecord[i+4];
            		light.version = scanRecord[i+5];
            		int start = i + 13;
            		int end = start + nameLen - 1;
            		//String deviceName = "";
            		byte[] name = new byte[nameLen];
            		for (int k = start; k <= end; k++) {
            			//获取设备名
            			name[k-start] = scanRecord[k];
            		}
            		String deviceName = new String(name);
            		//根据设备的不同类型而设置不同的图片
            		light.brightnessChangeable = 0;
            		/*System.out.println("Light的类型是" + scanRecord[i+3]);
            		System.out.println("Light的状态是" + scanRecord[i+4]);*/
            		if (scanRecord[i+3] == 1 || scanRecord[i+3] == 0x11) {
            			//开关，也就是light
            			if (scanRecord[i+4] == 0) {
            				light.state = 0;
            				light.picture = R.drawable.switch_off_0;
            			} else {
            				light.state = 1;
            				light.picture = R.drawable.switch_on_0;
            			}
            			//light.brightness = scanRecord[i+4];
            		}else if (scanRecord[i+3] == 2) {
            			//Lock
            			if (scanRecord[i+4] == 0) {
            				light.picture = R.drawable.icon_lock01;
            			} else {
            				light.picture = R.drawable.icon_lock_orange;
            			}
					} else if (scanRecord[i+3] == 3 || scanRecord[i+3] == 0x13) {
						if (scanRecord[i+4] == 0) {
            				light.state = 0;
            				light.picture = R.drawable.icon_light02;
            			} else {
            				light.state = 1;
            				light.picture = R.drawable.icon_light01;
            			}
            			light.brightness = scanRecord[i+4];
					} else if (scanRecord[i + 3] == 4) {
						// Window
						/*if (scanRecord[i + 4] == 0) {
							light.picture = R.drawable.light_off;
							light.state = 0;
						} else {
							light.picture = R.drawable.light_on;
							light.state = 1;
						}*/
						light.picture = R.drawable.curtain_48;
						//light.brightness = scanRecord[i + 4];
						}
            		/*else if (scanRecord[i+3] == 3) {
            			//LED
            			if (scanRecord[i+4] == 0) {
            				light.picture = R.drawable.light_off;
            				light.state = 0;
            			} else {
            				light.picture = R.drawable.light_on;
            				light.state = 1;
            			}
            			light.brightness = scanRecord[i+4];
            		}*/
            		light.name = deviceName;
            		light.address = device.getAddress().trim();
            		light.area = getString(R.string.all);
            		
            		/*if(light.isValidate == 1) {
            			light.isFound = 0;
            		}else {
            			light.isFound = R.drawable.wenhao_2;
            		}*/
            		light.isFound = R.drawable.wenhao_2;
            		
            		
            		light.rssi = rssi;
            		Queue<Integer> lightAvgRssi;
            		int avg = rssi;
            		if ((lightAvgRssi = avgRssi.get(light.address)) != null) {
            			lightAvgRssi.offer(light.rssi);
            		}
            		else {
            			lightAvgRssi = new LinkedList<Integer>();
            			lightAvgRssi.add(light.rssi);
            			avgRssi.put(light.address, lightAvgRssi);
            		}
            		if (lightAvgRssi.size() < 3) {
            			light.distance = Light.NEAR;
            		} else {
            			avg = average(lightAvgRssi);
            			int r = avg/*rssi*/;
            			if (/*avg*/r > -45) {
                			light.distance = Light.CLOSE;
                		}
                		if (/*avg*/r >= -70 && r <= -45) {
                			light.distance = Light.NEAR;
                		}
                		else if (/*avg*/r < -70) {
                			light.distance = Light.FAR;
                			//light.closable = true;
                		}
                		/*else {
                			light.distance = 11;
                		}*/
                		//removeRssi(lightAvgRssi);
                		lightAvgRssi.poll();
            		}
            		if(rssi > -60) {
            			light.signal = R.drawable.stat_sys_signal_4_fully;
            		}else if(rssi > -70) {
            			light.signal = R.drawable.stat_sys_signal_3_fully;
            		}else if(rssi > -90) {
            			light.signal = R.drawable.stat_sys_signal_2_fully;
            		}else if(rssi > -120) {
            			light.signal = R.drawable.stat_sys_signal_1_fully;
            		}else {
            			light.signal = R.drawable.stat_sys_signal_0_fully;
            		}
            		return light;
            	}
    		}
    		//System.out.println("result" + i + " : " + scanRecord[i]);
    	//}
    	return null;
    }
    
    private int average(Queue<Integer> queue) {
		int sum = 0;
		if (queue.size() == 0)
			return 0;
		for (int a : queue) {
			sum += a;
		}
		return (sum / queue.size());
		
	}
    
    /**
     * 去除与平均数相差最大的一个数
     * @param queue
     */
    private void removeRssi (Queue<Integer> queue) {
    	int avg = average(queue);
    	int largest = avg;
    	for (int i : queue) {
    		if ((Math.abs(i - avg)) > (Math.abs(largest - avg))) {
    			largest = i;
    		}
    	}
    	queue.remove(largest);
    }
    
    /**
     * 发送信息给mDeviceListHandler
     * @param light
     * @param type
     */
    public void sendMessage(Light light,int type){
		Bundle mBundle = new Bundle();
        Message msg = Message.obtain(mDeviceListHandler, type);
        mBundle.putSerializable(Light.LIGHT, light);
        msg.setData(mBundle);
        msg.sendToTarget();
    }
    
    public boolean connect(BluetoothDevice device, boolean autoConnect) {
    	Log.d(TAG, "connecting device...");
    	if (device != null) {
    		close();
    		mBluetoothGatt = device.connectGatt(this, autoConnect, mGattCallbacks);
    		if (mBluetoothGatt == null) {
    			Log.d(TAG, "mBluetoothGatt whitch connectGatt returned is null!!!");
    			return false;
    		}
    		return true;
    	}
        /*if (mBluetoothGatt != null) {
            return mBluetoothGatt.connect(device, autoconnect);
        }*/
        return false;
    }
    
    public void close () {
    	if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }
    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     *
     * @return Return true if the connection is initiated successfully. The connection result
     *         is reported asynchronously through the
     *         {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     *         callback.
     */
    public boolean connect(final String address) {
        if (mBtAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        // Previously connected device.  Try to reconnect.
        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                //mConnectionState = STATE_CONNECTING;
                return true;
            } else {
                return false;
            }
        }

        final BluetoothDevice device = mBtAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatt = device.connectGatt(this, false, mGattCallbacks);
        Log.d(TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddress = address;
        //mConnectionState = STATE_CONNECTING;
        return true;
    }

    public void disconnect() {
        if (mBluetoothGatt != null) {
        	System.out.println("disconnect is called");
            //fmBluetoothGatt.cancelConnection(device);
        	mBluetoothGatt.disconnect();
        }

    }

    private BluetoothAdapter.LeScanCallback mScanCallback = new BluetoothAdapter.LeScanCallback() {

		@Override
		public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
			// TODO Auto-generated method stub
			Log.i(TAG, "BleService.onLeScan...Device Address : " + device.getAddress());
			Light light = selectDevice(scanRecord,device,rssi);
        	if (light != null) {
        		sendMessage(light,GATT_DEVICE_FOUND_MSG);
        	}
		}
    	
    };
    public void scan(boolean start) {
        if (mBtAdapter == null)
            return;
        if (start) {
            mBtAdapter.startLeScan(mScanCallback);
            System.out.println("StartScan");
        } else {
        	mBtAdapter.stopLeScan(mScanCallback);
            System.out.println("StopScan");
        }
    }
    
    public void setBrightness(byte brightness) {
    	byte[] buf = new byte[2];
    	buf[0] = 0x06;
    	buf[1] = brightness;
    	write(buf,MY_TIMER_CHARACTERISTIC);
    }
    
    public void updateName(String name) {
    	setOperation(UPDATE_NAME);
    	BluetoothGattCharacteristic mChara = getBluetoothGattCharacteristic(MY_UPDATE_NAME_OR_INIT_PASSWORD_CHARACTERISTIC);
		if (mChara == null) {
			Log.v(TAG, "Characteristic is null.EnableNotification stop...");
			return;
		}
		if (enableNotification(mChara, true)) {
			Log.v(TAG, "enableNotification is ok...");
		}
		else {
			Log.v(TAG, "enableNotification failed...");
		}
    }
    
    
    
    
    public static String bytesToHexString(byte[] src){  
        StringBuilder stringBuilder = new StringBuilder("");  
        if (src == null || src.length <= 0) {  
            return null;  
        }  
        for (int i = 0; i < src.length; i++) {  
            int v = src[i] & 0xFF;  
            String hv = Integer.toHexString(v);  
            if (hv.length() < 2) {  
                stringBuilder.append(0);  
            }  
            stringBuilder.append(hv);  
        }  
        return stringBuilder.toString();  
    }
}

	
