package com.bde.light.db;

import com.bde.light.model.Timer;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class TimerHelper extends SQLiteOpenHelper {
	
	private static final String NAME = "timers.db";
	private static final int VERSION = 1;

	public TimerHelper(Context context) {
		super(context, NAME, null, VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		StringBuilder sb = new StringBuilder(" create table ").append(Timer.TABLE).append("(")
			.append(Timer.ID).append(" integer primary key autoincrement, ")
			.append(Timer.INDEX).append(" text, ")
			.append(Timer.NAME).append(" text, ")
			.append(Timer.LIGHT_ADDRESS).append(" text, ")
			.append(Timer.TIME).append(" text, ")
			.append(Timer.OPERATION).append(" text); ");
		db.execSQL(sb.toString());
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL(" drop table if exists " + Timer.TABLE);
		onCreate(db);
	}

}
