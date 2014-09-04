package com.bde.light.mgr;

import java.util.ArrayList;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.bde.light.activity.R;
import com.bde.light.db.LightHelper;
import com.bde.light.model.Light;

/**
 * 增删改查
 * @author lusiyu
 *
 */
public class LightMgr {
	
	LightHelper helper;
	
	public LightMgr(Context context) {
		helper = new LightHelper(context);
	}
	
	public ContentValues getValues(Light light) {
		ContentValues values = new ContentValues();
		//values.put(Light.ID, light.id);
    	values.put(Light.NAME, light.name);
    	values.put(Light.PASSWORD, light.password);
    	values.put(Light.ADDRESS, light.address);
    	values.put(Light.AREA, light.area);
    	values.put(Light.TYPE, light.type);
    	values.put(Light.SIGNAL, R.drawable.no_signal);
    	values.put(Light.PICTURE, light.picture);
    	values.put(Light.VERSION, light.version);
    	values.put(Light.ISVALIDATE, light.isValidate);
    	values.put(Light.SHAKE_OPEN, light.shake_open);
    	values.put(Light.SHAKE_CLOSE, light.shake_close);
    	values.put(Light.CLOSE_OPEN, light.close_open);
    	values.put(Light.CLOSE_CLOSE, light.close_close);
    	values.put(Light.REMOTE_OPEN, light.remote_open);
    	values.put(Light.REMOTE_CLOSE, light.remote_close);
    	values.put(Light.TIMER_OPEN, light.timer_open);
    	values.put(Light.TIMER_CLOSE, light.timer_close);
    	values.put(Light.BRIGHTNESS_CHANGABLE, light.brightnessChangeable);
    	values.put(Light.LAST_SIGNAL, light.lastSignal);
    	return values;
	}
	
	/**
	 * 添加light
	 * @param values
	 * @return id
	 */
	synchronized public long add(Light light) {
		SQLiteDatabase db = helper.getWritableDatabase();
		ContentValues values = getValues(light);
		long id = db.insert(Light.TABLE, null, values);
		db.close();
		return id;
	}
	
	/**
	 * 查询所有
	 * @param name
	 * @param type
	 * @return 返回ArrayList
	 */
	synchronized public ArrayList<Light> findAll() {
		SQLiteDatabase db = helper.getReadableDatabase();
		String selection = null;
		String[] selectionArgs = null;
		Cursor cursor = db.query(Light.TABLE, null, selection, selectionArgs, null, null, null);
		ArrayList<Light> lights = new ArrayList<Light>();
		
		while(cursor.moveToNext()) {
			Light light = new Light();
			light.id = cursor.getInt(cursor.getColumnIndex(Light.ID));
			light.name = cursor.getString(cursor.getColumnIndex(Light.NAME));
			light.password = cursor.getString(cursor.getColumnIndex(Light.PASSWORD));
			light.address = cursor.getString(cursor.getColumnIndex(Light.ADDRESS));
			light.area = cursor.getString(cursor.getColumnIndex(Light.AREA));
			light.type = cursor.getInt(cursor.getColumnIndex(Light.TYPE));
			light.signal = cursor.getInt(cursor.getColumnIndex(Light.SIGNAL));
			light.picture = cursor.getInt(cursor.getColumnIndex(Light.PICTURE));
			light.version = cursor.getInt(cursor.getColumnIndex(Light.VERSION));
			light.isValidate = cursor.getInt(cursor.getColumnIndex(Light.ISVALIDATE));
			light.shake_open = cursor.getInt(cursor.getColumnIndex(Light.SHAKE_OPEN));
			light.shake_close = cursor.getInt(cursor.getColumnIndex(Light.SHAKE_CLOSE));
			light.close_open = cursor.getInt(cursor.getColumnIndex(Light.CLOSE_OPEN));
			light.close_close = cursor.getInt(cursor.getColumnIndex(Light.CLOSE_CLOSE));
			light.remote_open = cursor.getInt(cursor.getColumnIndex(Light.REMOTE_OPEN));
			light.remote_close = cursor.getInt(cursor.getColumnIndex(Light.REMOTE_CLOSE));
			light.timer_open = cursor.getInt(cursor.getColumnIndex(Light.TIMER_OPEN));
			light.timer_close = cursor.getInt(cursor.getColumnIndex(Light.TIMER_CLOSE));
			light.brightnessChangeable = cursor.getInt(cursor.getColumnIndex(Light.BRIGHTNESS_CHANGABLE));
			light.lastSignal = cursor.getInt(cursor.getColumnIndex(Light.LAST_SIGNAL));
			lights.add(light);
		}
		
		cursor.close();
		db.close();
		return lights;
	}
	
	/**
	 * 查询Area所有
	 * @param name
	 * @param type
	 * @return 返回ArrayList
	 */
	synchronized public ArrayList<Light> findAllByArea(String areaName) {
		SQLiteDatabase db = helper.getReadableDatabase();
		String selection = null;
		String[] selectionArgs = null;
		selection = Light.AREA + " = ? ";
		selectionArgs = new String[]{areaName};
		
		Cursor cursor = db.query(Light.TABLE, null, selection, selectionArgs, null, null, null);
		ArrayList<Light> lights = new ArrayList<Light>();
		
		while(cursor.moveToNext()) {
			Light light = new Light();
			light.id = cursor.getInt(cursor.getColumnIndex(Light.ID));
			light.name = cursor.getString(cursor.getColumnIndex(Light.NAME));
			light.password = cursor.getString(cursor.getColumnIndex(Light.PASSWORD));
			light.address = cursor.getString(cursor.getColumnIndex(Light.ADDRESS));
			light.area = cursor.getString(cursor.getColumnIndex(Light.AREA));
			light.type = cursor.getInt(cursor.getColumnIndex(Light.TYPE));
			light.signal = cursor.getInt(cursor.getColumnIndex(Light.SIGNAL));
			light.picture = cursor.getInt(cursor.getColumnIndex(Light.PICTURE));
			light.version = cursor.getInt(cursor.getColumnIndex(Light.VERSION));
			light.isValidate = cursor.getInt(cursor.getColumnIndex(Light.ISVALIDATE));
			light.shake_open = cursor.getInt(cursor.getColumnIndex(Light.SHAKE_OPEN));
			light.shake_close = cursor.getInt(cursor.getColumnIndex(Light.SHAKE_CLOSE));
			light.close_open = cursor.getInt(cursor.getColumnIndex(Light.CLOSE_OPEN));
			light.close_close = cursor.getInt(cursor.getColumnIndex(Light.CLOSE_CLOSE));
			light.remote_open = cursor.getInt(cursor.getColumnIndex(Light.REMOTE_OPEN));
			light.remote_close = cursor.getInt(cursor.getColumnIndex(Light.REMOTE_CLOSE));
			light.timer_open = cursor.getInt(cursor.getColumnIndex(Light.TIMER_OPEN));
			light.timer_close = cursor.getInt(cursor.getColumnIndex(Light.TIMER_CLOSE));
			light.brightnessChangeable = cursor.getInt(cursor.getColumnIndex(Light.BRIGHTNESS_CHANGABLE));
			light.lastSignal = cursor.getInt(cursor.getColumnIndex(Light.LAST_SIGNAL));
			lights.add(light);
		}
		
		cursor.close();
		db.close();
		return lights;
	}
	
	synchronized public ArrayList<Light> find(int shake_open,int shake_close,int close_open,int close_close) {
		SQLiteDatabase db = helper.getReadableDatabase();
		String selection = null;
		String[] selectionArgs = null;
		selection = Light.SHAKE_OPEN + " = ? or " 
			      + Light.SHAKE_CLOSE + " = ? or "
			      + Light.CLOSE_OPEN + " = ? or "
			      + Light.CLOSE_CLOSE + " = ? ";
		selectionArgs = new String[]{shake_open+"", shake_close+"", close_open+"",close_close+""};
		Cursor cursor = db.query(Light.TABLE, null, selection, selectionArgs, null, null, null);
		ArrayList<Light> lights = new ArrayList<Light>();
		
		while(cursor.moveToNext()) {
			Light light = new Light();
			light.id = cursor.getInt(cursor.getColumnIndex(Light.ID));
			light.name = cursor.getString(cursor.getColumnIndex(Light.NAME));
			light.password = cursor.getString(cursor.getColumnIndex(Light.PASSWORD));
			light.address = cursor.getString(cursor.getColumnIndex(Light.ADDRESS));
			light.area = cursor.getString(cursor.getColumnIndex(Light.AREA));
			light.type = cursor.getInt(cursor.getColumnIndex(Light.TYPE));
			light.signal = cursor.getInt(cursor.getColumnIndex(Light.SIGNAL));
			light.picture = cursor.getInt(cursor.getColumnIndex(Light.PICTURE));
			light.version = cursor.getInt(cursor.getColumnIndex(Light.VERSION));
			light.isValidate = cursor.getInt(cursor.getColumnIndex(Light.ISVALIDATE));
			light.shake_open = cursor.getInt(cursor.getColumnIndex(Light.SHAKE_OPEN));
			light.shake_close = cursor.getInt(cursor.getColumnIndex(Light.SHAKE_CLOSE));
			light.close_open = cursor.getInt(cursor.getColumnIndex(Light.CLOSE_OPEN));
			light.close_close = cursor.getInt(cursor.getColumnIndex(Light.CLOSE_CLOSE));
			light.remote_open = cursor.getInt(cursor.getColumnIndex(Light.REMOTE_OPEN));
			light.remote_close = cursor.getInt(cursor.getColumnIndex(Light.REMOTE_CLOSE));
			light.timer_open = cursor.getInt(cursor.getColumnIndex(Light.TIMER_OPEN));
			light.timer_close = cursor.getInt(cursor.getColumnIndex(Light.TIMER_CLOSE));
			light.brightnessChangeable = cursor.getInt(cursor.getColumnIndex(Light.BRIGHTNESS_CHANGABLE));
			light.lastSignal = cursor.getInt(cursor.getColumnIndex(Light.LAST_SIGNAL));
			lights.add(light);
		}
		
		cursor.close();
		db.close();
		return lights;
	}
	
	synchronized public Light findByAddress(String address) {
		SQLiteDatabase db = helper.getReadableDatabase();
		String selection = null;
		String[] selectionArgs = null;
		selection = Light.ADDRESS + " = ? ";
		selectionArgs = new String[]{address};
		Cursor cursor = db.query(Light.TABLE, null, selection, selectionArgs, null, null, null);
		Light light = null;
		while(cursor.moveToNext()) {
			light = new Light();
			light.id = cursor.getInt(cursor.getColumnIndex(Light.ID));
			light.name = cursor.getString(cursor.getColumnIndex(Light.NAME));
			light.password = cursor.getString(cursor.getColumnIndex(Light.PASSWORD));
			light.address = cursor.getString(cursor.getColumnIndex(Light.ADDRESS));
			light.area = cursor.getString(cursor.getColumnIndex(Light.AREA));
			light.type = cursor.getInt(cursor.getColumnIndex(Light.TYPE));
			light.signal = cursor.getInt(cursor.getColumnIndex(Light.SIGNAL));
			light.picture = cursor.getInt(cursor.getColumnIndex(Light.PICTURE));
			light.version = cursor.getInt(cursor.getColumnIndex(Light.VERSION));
			light.isValidate = cursor.getInt(cursor.getColumnIndex(Light.ISVALIDATE));
			light.shake_open = cursor.getInt(cursor.getColumnIndex(Light.SHAKE_OPEN));
			light.shake_close = cursor.getInt(cursor.getColumnIndex(Light.SHAKE_CLOSE));
			light.close_open = cursor.getInt(cursor.getColumnIndex(Light.CLOSE_OPEN));
			light.close_close = cursor.getInt(cursor.getColumnIndex(Light.CLOSE_CLOSE));
			light.remote_open = cursor.getInt(cursor.getColumnIndex(Light.REMOTE_OPEN));
			light.remote_close = cursor.getInt(cursor.getColumnIndex(Light.REMOTE_CLOSE));
			light.timer_open = cursor.getInt(cursor.getColumnIndex(Light.TIMER_OPEN));
			light.timer_close = cursor.getInt(cursor.getColumnIndex(Light.TIMER_CLOSE));
			light.brightnessChangeable = cursor.getInt(cursor.getColumnIndex(Light.BRIGHTNESS_CHANGABLE));
			light.lastSignal = cursor.getInt(cursor.getColumnIndex(Light.LAST_SIGNAL));
		}
		
		cursor.close();
		db.close();
		return light;
	}
	
	/**
	 * 修改
	 * @param values
	 * @return 所影响的记录数，返回1代表修改成功
	 */
	synchronized public int update(Light light) {
		SQLiteDatabase db = helper.getWritableDatabase();
		ContentValues values = getValues(light);
		int result = db.update(Light.TABLE, values, Light.ADDRESS + "=?", new String[]{light.address});
		db.close();
		return result;
	}
	
	/**
	 * 删除
	 * @param address
	 * @return 所影响的记录数，返回1代表删除成功
	 */
	synchronized public int delete(String address) {
		SQLiteDatabase db = helper.getWritableDatabase();
		int result = db.delete(Light.TABLE, Light.ADDRESS + "=?", new String[]{address});
		db.close();
		return result;
	}
	
	/**
	 * 删除该区域全部设备
	 * @param address
	 * @return 所影响的记录数，返回1代表删除成功
	 */
	synchronized public int deleteAllByArea(String areaName) {
		SQLiteDatabase db = helper.getWritableDatabase();
		int result = db.delete(Light.TABLE, Light.AREA + "=?", new String[]{areaName});
		db.close();
		return result;
	}
	/**
	 * 删除全部设备
	 * @param address
	 * @return 所影响的记录数，返回1代表删除成功
	 */
	synchronized public int deleteAll() {
		SQLiteDatabase db = helper.getWritableDatabase();
		int result = db.delete(Light.TABLE, null, null);
		db.close();
		return result;
	}
}
