package com.bde.light.model;

import java.io.Serializable;

public class Timer implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public static final String TABLE = "t_timer";
	public static final String ID = "id";
	public static final String NAME = "name";
	public static final String TIME = "time";
	public static final String OPERATION = "operation";
	public static final String INDEX = "timerIndex";
	public static final String LIGHT_ADDRESS = "lightAddress";
	
	public static final int ACTION_ON = 1;
	public static final int ACTION_OFF = 0;
	
	public static final int OPERATION_ADD = 11;
	public static final int OPERATION_MODIFY = 12;
	public static final int OPERATION_DELETE = 13;
	
	//数据库字段属性
	//timer的数据库的ID
	public long id;
	//timer的索引
	public String index;
	//设备名字
	public String name;
	//定时的时间
	public String time;
	//操作（打开或关闭）
	public String operation;
	
	public String lightAddress;
	
	public Light light;
	
	public int action;

	public static byte[] addTimer(int action, int second) {
		//创建并填充要发送的包
		
		byte[] message = new byte[8];
		message[0] = 1;
		message[1] = 0;
		message[2] = 0;
		int temp;
		for (int i = 3, j = 3; i <= 6; i++, j--) {
			temp = second;
			message[i] = (byte) (temp<<(j*8)>>24);
		}
		message[7] = (byte)action;

		return message;
	}

	public static byte[] modifyTimer(int index, int action, int second) {
		//创建并填充要发送的包
		byte[] message = new byte[8];
		message[0] = 2;
		int temp;
		for (int i = 1, j = 3; i <= 2; i++, j--) {
			temp = index;
			message[i] = (byte) (temp<<(j*8)>>24);
		}
		for (int i = 3, j = 3; i <= 6; i++, j--) {
			temp = second;
			message[i] = (byte) (temp<<(j*8)>>24);
		}
		message[7] = (byte)action;
		return message;
	}
	
	public static byte[] deleteTimer(int index) {
		//创建并填充要发送的包
		byte[] message = new byte[8];
		message[0] = 3;
		int temp = index;
		for (int i = 1, j = 3; i <= 2; i++, j--) {
			temp = index;
			message[i] = (byte) (temp<<(j*8)>>24);
		}
		for (int i = 3; i <= 7; i++) {
			message[i] = 0;
		}
		return message;
	}
}
