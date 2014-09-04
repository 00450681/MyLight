package com.bde.light.db;

import com.bde.light.model.Area;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class AreaHelper extends SQLiteOpenHelper {
	
	private static final String NAME = "areas.db";
	private static final int VERSION = 1;

	public AreaHelper(Context context) {
		super(context, NAME, null, VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		StringBuilder sb = new StringBuilder(" create table ").append(Area.TABLE).append("(")
			.append(Area.ID).append(" integer primary key autoincrement, ")
			.append(Area.AREA).append(" text); ");
		db.execSQL(sb.toString());
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL(" drop table if exists " + Area.TABLE);
		onCreate(db);
	}

}
