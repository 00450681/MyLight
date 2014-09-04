package com.bde.light.mgr;

import java.util.ArrayList;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.bde.light.db.AreaHelper;
import com.bde.light.model.Area;
import com.bde.light.model.Light;

/**
 * Area区域的增删改查操作
 * @author lusiyu
 *
 */
public class AreaMgr {
	
	AreaHelper helper;
	
	public AreaMgr(Context context) {
		helper = new AreaHelper(context);
	}
	
	public ContentValues getValues(Area area) {
		ContentValues values = new ContentValues();
		//values.put(Light.ID, light.id);
    	values.put(Area.AREA, area.area);
    	return values;
	}
	
	/**
	 * 添加area
	 * @param values
	 * @return ����
	 */
	synchronized public long add(Area area) {
		SQLiteDatabase db = helper.getWritableDatabase();
		ContentValues values = getValues(area);
		long id = db.insert(Area.TABLE, null, values);
		db.close();
		return id;
	}
	
	/**
	 * 查找所有area
	 * @param name
	 * @param type
	 * @return 返回ArrayList
	 */
	synchronized public ArrayList<Area> findAll() {
		SQLiteDatabase db = helper.getReadableDatabase();
		String selection = null;
		String[] selectionArgs = null;
		Cursor cursor = db.query(Area.TABLE, null, selection, selectionArgs, null, null, null);
		ArrayList<Area> areas = new ArrayList<Area>();
		
		while(cursor.moveToNext()) {
			Area area = new Area();
			area.id = cursor.getInt(cursor.getColumnIndex(Area.ID));
			area.area = cursor.getString(cursor.getColumnIndex(Area.AREA));
			
			areas.add(area);
		}
		
		cursor.close();
		db.close();
		return areas;
	}
	
	/**
	 * 查找一个area
	 * @param name
	 * @return 返回ArrayList
	 */
	synchronized public Area findByName(String name) {
		SQLiteDatabase db = helper.getReadableDatabase();
		String selection = null;
		String[] selectionArgs = null;
		selection = Area.AREA + " = ?";
		selectionArgs = new String[] {name};
		Cursor cursor = db.query(Area.TABLE, null, selection, selectionArgs, null, null, null);
		Area area = null;
		while(cursor.moveToNext()) {
			area = new Area();
			area.id = cursor.getInt(cursor.getColumnIndex(Area.ID));
			area.area = cursor.getString(cursor.getColumnIndex(Area.AREA));
		}
		cursor.close();
		db.close();
		return area;
	}
	
	/**
	 * 修改
	 * @param values
	 * @return 所影响的记录数，返回1代表删除成功
	 */
	synchronized public int update(Area area) {
		SQLiteDatabase db = helper.getWritableDatabase();
		ContentValues values = getValues(area);
		int result = db.update(Area.TABLE, values, Area.ID + "=?", new String[]{Integer.toString(area.id)});
		
		db.close();
		return result;
	}
	
	/**
	 * 删除area
	 * @return
	 */
	synchronized public int delete(Area area) {
		SQLiteDatabase db = helper.getWritableDatabase();
		int result = db.delete(Area.TABLE, Area.AREA + "=?", new String[]{area.area});
		db.close();
		return result;
	}
}
