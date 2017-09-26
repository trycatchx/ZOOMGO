package com.dmsys.airdiskpro.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * @ClassName:  BackupLogDBHelper   
 * @Description:   创建或打开备份记录表
 * @author: Alan.wang  
 * @date:   2014-10-11 16:31  
 * version:1.0.0
 */ 
public class BakCLogDBHelper extends SQLiteOpenHelper {
	//public static final String DATABASE_PATH = StaticVariate.DATABASE_PATH;
	public static final String CONTACT_TABLE = "contact";
	private static final int DATABASE_VERSION = 2;
	
	public BakCLogDBHelper(Context context ,String dbPath, String dbName) {
		//CursorFactory设置为null,使用默认值
		super(context, dbPath+dbName, null, DATABASE_VERSION);
	}
	
	

	//数据库第一次被创建时onCreate会被调用
	@Override
	public void onCreate(SQLiteDatabase db) {
		//建立2.	联系人备份记录表contact
		db.execSQL("CREATE TABLE IF NOT EXISTS " + CONTACT_TABLE +
				"(_id INTEGER PRIMARY KEY AUTOINCREMENT, bak_num INTEGER, " +
				"bak_node INTEGER, bak_time LONG)");
	}

	//如果DATABASE_VERSION值被改为2,系统发现现有数据库版本不同,即会调用onUpgrade
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//		if (oldVersion < 2) {
//			String sql = "ALTER TABLE "+BAKNODE_TABLE+" ADD COLUMN last_rcd_time LONG DEFAULT 0";
//			db.execSQL(sql);
//		}
	}
	
}
