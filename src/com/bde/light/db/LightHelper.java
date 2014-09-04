package com.bde.light.db;

import com.bde.light.model.Light;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class LightHelper extends SQLiteOpenHelper {
	
	private static final String NAME = "lights.db";
	private static final int VERSION = 2;

	public LightHelper(Context context) {
		super(context, NAME, null, VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		StringBuilder sb = new StringBuilder(" create table ").append(Light.TABLE).append("(")
			.append(Light.ID).append(" integer primary key autoincrement, ")
			.append(Light.NAME).append(" text, ")
			.append(Light.PASSWORD).append(" text, ")
			.append(Light.ADDRESS).append(" text, ")
			.append(Light.AREA).append(" text, ")
			.append(Light.TYPE).append(" integer, ")
			.append(Light.SIGNAL).append(" integer, ")
			.append(Light.PICTURE).append(" integer, ")
			.append(Light.VERSION).append(" integer, ")
			.append(Light.ISVALIDATE).append(" integer, ")
			.append(Light.SHAKE_OPEN).append(" integer, ")
			.append(Light.SHAKE_CLOSE).append(" integer, ")
			.append(Light.CLOSE_OPEN).append(" integer, ")
			.append(Light.CLOSE_CLOSE).append(" integer, ")
			.append(Light.REMOTE_OPEN).append(" integer, ")
			.append(Light.REMOTE_CLOSE).append(" integer, ")
			.append(Light.TIMER_OPEN).append(" integer, ")
			.append(Light.TIMER_CLOSE).append(" integer, ")
			.append(Light.BRIGHTNESS_CHANGABLE).append(" integer, ")
			.append(Light.LAST_SIGNAL).append(" integer); ");
		db.execSQL(sb.toString());
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL(" drop table if exists " + Light.TABLE);
		onCreate(db);
	}

}
