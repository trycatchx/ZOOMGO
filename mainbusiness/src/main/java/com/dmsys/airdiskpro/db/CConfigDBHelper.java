package com.dmsys.airdiskpro.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * @ClassName:  ContactDBHelper   
 * @Description:   创建或打开联系人的备份数据库contacts.db，该数据库中有contact_raw和contact_data两张表
 * @author: Alan.wang  
 * @date:   2014-09-28 19:31  
 * version:1.0.0
 */ 
public class CConfigDBHelper extends SQLiteOpenHelper{

	public static final String CONFIG_TABLE = "cconfig";
	private static final int DATABASE_VERSION = 1;
	public class CONFIG{
		public static final String TIME = "time";
		public static final String PHONE_MODEL = "phone_model";
		public static final String NUM = "contacts_num";
		public static final String MD5 = "contacts_md5";
	}
	
	
	public CConfigDBHelper(Context context ,String dbPath, String dbName) {
		//CursorFactory设置为null,使用默认值
		super(context, dbPath+dbName, null, DATABASE_VERSION);
	}

	//数据库第一次被创建时onCreate会被调用
	@Override
	public void onCreate(SQLiteDatabase db) {
		//建立1.	版本信息表：version
		db.execSQL("CREATE TABLE IF NOT EXISTS " + CONFIG_TABLE +
				"(_id INTEGER PRIMARY KEY AUTOINCREMENT, "+CONFIG.TIME+" LONG, " +
				CONFIG.PHONE_MODEL+" VARCHAR, "+CONFIG.NUM+" INTEGER, "+CONFIG.MD5+" VARCHAR)");
	}

	//如果DATABASE_VERSION值被改为2,系统发现现有数据库版本不同,即会调用onUpgrade
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		//db.execSQL("ALTER TABLE calllogs ADD COLUMN other STRING");
	}

}
