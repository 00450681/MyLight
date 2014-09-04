package com.bde.light.mgr;

import java.util.ArrayList;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.bde.light.db.TimerHelper;
import com.bde.light.model.Timer;

/**
 * 定时器增删改查
 * @author lusiyu
 *
 */
public class TimerMgr {
	
	TimerHelper helper;
	Context mContext;
	private static String TAG = "TimerMgr";
	
	public TimerMgr(Context context) {
		helper = new TimerHelper(context);
		mContext = context;
	}
	
	public ContentValues getValues(Timer timer) {
		ContentValues values = new ContentValues();
		//values.put(Timer.ID, Timer.id);
		values.put(Timer.INDEX, timer.index);
    	values.put(Timer.NAME, timer.name);
    	values.put(Timer.LIGHT_ADDRESS, timer.lightAddress);
    	values.put(Timer.TIME, timer.time);
    	values.put(Timer.OPERATION, timer.operation);
    	return values;
	}
	
	/**
	 * 添加定时器
	 * @param values
	 * @return id
	 */
	synchronized public long add(Timer timer) {
		Log.i(TAG, "Timer.add() is called!!!");
		SQLiteDatabase db = helper.getWritableDatabase();
		ContentValues values = getValues(timer);
		long id = db.insert(Timer.TABLE, null, values);
		db.close();
		return id;
	}
	
	/**
	 * 查询所有定时器
	 * @param name
	 * @param type
	 * @return ArrayList
	 */
	synchronized public ArrayList<Timer> findAll() {
		Log.i(TAG, "Timer.findAll() is called!!!");
		SQLiteDatabase db = helper.getReadableDatabase();
		String selection = null;
		String[] selectionArgs = null;
		Cursor cursor = db.query(Timer.TABLE, null, selection, selectionArgs, null, null, null);
		ArrayList<Timer> timers = new ArrayList<Timer>();
		
		while(cursor.moveToNext()) {
			Timer timer = new Timer();
			timer.id = cursor.getLong(cursor.getColumnIndex(Timer.ID));
			timer.index = cursor.getString(cursor.getColumnIndex(Timer.INDEX));
			timer.name = cursor.getString(cursor.getColumnIndex(Timer.NAME));
			timer.lightAddress = cursor.getString(cursor.getColumnIndex(Timer.LIGHT_ADDRESS));
			timer.time = cursor.getString(cursor.getColumnIndex(Timer.TIME));
			timer.operation = cursor.getString(cursor.getColumnIndex(Timer.OPERATION));
			LightMgr lightMgr = new LightMgr(mContext);
			timer.light = lightMgr.findByAddress(timer.lightAddress);
			timers.add(timer);
			Log.i(TAG, timer.light.name + " " + timer.time + " " + timer.operation);
		}
		
		cursor.close();
		db.close();
		return timers;
	}
	
	
	/**
	 * 查询所有定时器
	 * @param name
	 * @param type
	 * @return ArrayList
	 */
	synchronized public ArrayList<Timer> findAllByAddress(String address) {
		Log.i(TAG, "Timer.findAllByAddress() is called!!!");
		SQLiteDatabase db = helper.getReadableDatabase();
		String selection = null;
		String[] selectionArgs = null;
		selection = Timer.LIGHT_ADDRESS + " = ?";
		selectionArgs = new String[]{address};
		Cursor cursor = db.query(Timer.TABLE, null, selection, selectionArgs, null, null, null);
		ArrayList<Timer> timers = new ArrayList<Timer>();
		
		while(cursor.moveToNext()) {
			Timer timer = new Timer();
			timer.id = cursor.getLong(cursor.getColumnIndex(Timer.ID));
			timer.index = cursor.getString(cursor.getColumnIndex(Timer.INDEX));
			timer.name = cursor.getString(cursor.getColumnIndex(Timer.NAME));
			timer.lightAddress = cursor.getString(cursor.getColumnIndex(Timer.LIGHT_ADDRESS));
			timer.time = cursor.getString(cursor.getColumnIndex(Timer.TIME));
			timer.operation = cursor.getString(cursor.getColumnIndex(Timer.OPERATION));
			LightMgr lightMgr = new LightMgr(mContext);
			timer.light = lightMgr.findByAddress(timer.lightAddress);
			timers.add(timer);
			Log.i(TAG, timer.light.name + " " + timer.time + " " + timer.operation);
		}
		
		cursor.close();
		db.close();
		return timers;
	}
	
	
	/**
	 * 修改
	 * @param values
	 * @return 
	 */
	synchronized public int update(Timer timer) {
		Log.i(TAG, "Timer.update() is called!!!");
		SQLiteDatabase db = helper.getWritableDatabase();
		ContentValues values = getValues(timer);
		int result = db.update(Timer.TABLE, values, Timer.ID + "=?", new String[]{timer.id+""});
		db.close();
		return result;
	}
	
	/**
	 * 删除
	 * @return
	 */
	synchronized public int delete(Timer timer) {
		Log.i(TAG, "Timer.delete() is called!!!");
		SQLiteDatabase db = helper.getWritableDatabase();
		int result = db.delete(Timer.TABLE, Timer.ID + "=?", new String[]{timer.id+""});
		db.close();
		return result;
	}
}
