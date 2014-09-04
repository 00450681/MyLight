package com.bde.light.model;

import java.io.Serializable;
import java.util.ArrayList;

public class Light implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public static final String TABLE = "t_light";
	public static final String ID = "_id";
	public static final String NAME = "name";
	public static final String PASSWORD = "password";
	public static final String ADDRESS = "address";
	public static final String AREA = "area";
	public static final String TYPE = "type";
	public static final String STATE = "state";
	public static final String VERSION = "version";
	public static final String SIGNAL = "signal";
	public static final String ISFOUND = "isFound";
	public static final String ISVALIDATE = "isValidate";
	public static final String PICTURE = "picture";
	public static final String SHAKE_OPEN = "shake_open";
	public static final String SHAKE_CLOSE = "shake_close";
	public static final String CLOSE_OPEN = "close_open";
	public static final String CLOSE_CLOSE = "close_close";
	public static final String REMOTE_OPEN = "remote_open";
	public static final String REMOTE_CLOSE = "remote_close";
	public static final String TIMER_OPEN = "timer_open";
	public static final String TIMER_CLOSE = "timer_close";
	public static final String RSSI = "rssi";
	public static final String LIGHT = "light";
	public static final String BRIGHTNESS_CHANGABLE = "brightness_changeable";
	public static final String LAST_SIGNAL = "last_signal";
	
	public static final int FAR = 100;
	public static final int NEAR = 10;
	public static final int CLOSE = 1;
	
	public int id;
	public String name;
	public String password;
	public String address;
	public String area;
	public int picture;
	//设备类型
	public int type;
	//设备状态
	public int state;
	//密码版本
	public int version;
	//信号-保存信号的drawable
	public int signal;
	//是否找到设备-保存设备是否验证的drawable
	public int isFound;
	//是否已经验证密码
	public int isValidate;
	//晃动打开设备
	public int shake_open;
	//晃动关闭设备
	public int shake_close;
	//接近打开设备
	public int close_open;
	//接近关闭
	public int close_close;
	
	public int remote_open = 1;
	public int remote_close = 1;
	
	public int timer_open = 1;
	public int timer_close = 1;
	//设备的rssi值
	public int rssi;
	//设备的旧状态
	public int oldState;
	
	public int brightness;
	public int brightnessChangeable;
	
	public boolean isCheckThisTime = true;
	public int searchTimes = 0;
	
	public int distance =  FAR;
	
	public ArrayList RssiBuff;
	public int averageRssi;
	
	public int lastSignal;
	//可以接近么？
	public boolean closable = true;

	public boolean isOperationDone = true;

	@Override
	public boolean equals(Object o) {
		// TODO Auto-generated method stub
		//return super.equals(o);
		if (o instanceof Light) {
			if (((Light) o).address.equals(address))
				return true;
		}
		return false;
	}

	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return address.hashCode();
	}
	
}
