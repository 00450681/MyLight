package com.bde.light.utils;

public class NumConversion {
	
	/**
	 * 验证密码时将密码转换格式
	 * @param password
	 * @param buf
	 * @return
	 */
	public static byte[] stringToBytes(String password,byte[] buf) {
		int len = buf.length;
		int hex = 0;
		for (int i = 0; i < len; i++) {
			if(i < len-1) {
				hex = Integer.parseInt(password.substring(i*2, i*2+2),16);
			}else {
				hex = Integer.parseInt(password.substring(i*2),16);
			}
			buf[i] = (byte) hex;
		}
		
		return buf;
	}
	
	/**
	 * 提交定时器数据时转换数据的格式
	 * @param a
	 * @param len
	 * @return
	 */
	public static byte[] intToBytes(long add_id, int len) {
		String str = add_id + "";
		int num_len = str.length();
		byte[] buffer = new byte[len];
		switch(len) {
		case 2:
			if (num_len == 1) {
				buffer[0] = 0;
				buffer[1] = (byte) Integer.parseInt(str.substring(0));
			} else {
				for (int i = 0; i < len; i++) {
					buffer[i] = (byte) Integer.parseInt(str.substring(i,i+1));
				}
			}
			break;
			
	
		case 4:
			switch(num_len) {
			case 1:
				for (int i = 0; i < len - 1; i++) {
					buffer[i] = 0;
				}
				buffer[3] = (byte) Integer.parseInt(str.substring(0,1));
				break;
				
			case 2:
				buffer[0] = 0;
				buffer[1] = 0;
				for (int i = 2; i < len; i++) {
					buffer[i] = (byte) Integer.parseInt(str.substring(i-2,i-1));
				}
				break;
			case 3:
				
				buffer[0] = 0;
				for (int i = 1; i < len; i++) {
					buffer[i] = (byte) Integer.parseInt(str.substring(i-1,i));
				}
				break;
				
			case 4:
				for (int i = 0; i < len; i++) {
					buffer[i] = (byte) Integer.parseInt(str.substring(i,i+1));
				}
				break;
			}
			break;
		}
		return buffer;
	}
	
	/**
	 * 将int数据转换为小端格式的byte数组
	 * @param num
	 * @return
	 */
	public static byte [] int2LittleEndianByteArray16(int num) {
		byte []ret = new byte[2];
		ret[0] = (byte) (num & 0xFF);
		ret[1] = (byte) ((num & 0xFF00) >> 8);
		return ret;
	}
}
