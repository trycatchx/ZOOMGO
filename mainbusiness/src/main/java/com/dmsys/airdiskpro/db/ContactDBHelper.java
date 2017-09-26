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
public class ContactDBHelper extends SQLiteOpenHelper{

	public static final String DATABASE_NAME = "contact";
	public static final String VERSION_TABLE = "version";
	public static final String CONTACTS_TABLE = "contacts";
	public static final String PHOTO_TABLE = "photo";
	public static final String GROUP_TABLE = "groups";
	public static final String DATA_TABLE = "data";
	private static final int DATABASE_VERSION = 1;
	//version表中的字段名
	public class VERSION{
		public static final String _ID = "_id";
		public static final String VERSION_VALUE = "version_value";
		public static final String PHONE_NAME = "phone_name";
		public static final String EXPORT_TIME = "export_time";
	}
	//contacts表中的字段名
	public class CONTACTS{
		public static final String _ID = "_id";
		public static final String MD5 = "md5";
		public static final String ACCOUNT_NAME = "account_name";
		public static final String ACCOUNT_TYPE = "account_type";
		public static final String PHOTO_ID = "photo_id";
		public static final String GROUP_ID = "group_id";
	}
	//photo表中的字段名
	public class PHOTO{
		public static final String _ID = "_id";
		public static final String CONTACT_ID = "contact_id";
		public static final String PHOTO_VALUE = "photo_value";
	}
	//group表中的字段名
	public class GROUP{
		public static final String _ID = "_id";
		public static final String GROUP_NAME = "group_name";
	}
	//data表中的字段名
	public class DATA{
		public static final String _ID = "_id";
		public static final String CONTACT_ID = "contact_id";
		public static final String MIMETYPE = "mimetype";
		public static final String DATA1  = "data1";
		public static final String DATA2  = "data2";
		public static final String DATA3  = "data3";
		public static final String DATA4  = "data4";
		public static final String DATA5  = "data5";
		public static final String DATA6  = "data6";
		public static final String DATA7  = "data7";
		public static final String DATA8  = "data8";
		public static final String DATA9  = "data9";
		public static final String DATA10 = "data10";
		public static final String DATA11 = "data11";
		public static final String DATA12 = "data12";
		public static final String DATA13 = "data13";
		public static final String DATA14 = "data14";
		public static final String DATA15 = "data15";
	}
	
	
	public ContactDBHelper(Context context ,String dbPath, String dbName) {
		//CursorFactory设置为null,使用默认值
		super(context, dbPath+dbName, null, DATABASE_VERSION);
	}

	//数据库第一次被创建时onCreate会被调用
	@Override
	public void onCreate(SQLiteDatabase db) {
		//建立1.	版本信息表：version
		db.execSQL("CREATE TABLE IF NOT EXISTS " + VERSION_TABLE +
				"(_id INTEGER PRIMARY KEY AUTOINCREMENT, "+VERSION.VERSION_VALUE+" VARCHAR, " +
				VERSION.PHONE_NAME+" VARCHAR, "+VERSION.EXPORT_TIME+" LONG)");
		//建立2.	联系人基本详情表：contacts
		db.execSQL("CREATE TABLE IF NOT EXISTS " + CONTACTS_TABLE +
				"(_id INTEGER PRIMARY KEY AUTOINCREMENT, "+CONTACTS.MD5+" VARCHAR, "+CONTACTS.ACCOUNT_NAME+
				" VARCHAR, "+CONTACTS.ACCOUNT_TYPE+" VARCHAR, "+CONTACTS.PHOTO_ID+" INTEGER, "+
				CONTACTS.GROUP_ID+" INTEGER)");
		//建立3.	联系人头像表：photo
		db.execSQL("CREATE TABLE IF NOT EXISTS " + PHOTO_TABLE +
				"(_id INTEGER PRIMARY KEY AUTOINCREMENT, "+PHOTO.CONTACT_ID+" INTEGER, "+PHOTO.PHOTO_VALUE+" TEXT)");
		//建立4.	群组表：group
		db.execSQL("CREATE TABLE IF NOT EXISTS " + GROUP_TABLE +
				"(_id INTEGER PRIMARY KEY AUTOINCREMENT, "+GROUP.GROUP_NAME+" VARCHAR)");
		//建立5.	详细数据表：data
		db.execSQL("CREATE TABLE IF NOT EXISTS " + DATA_TABLE +
				"(_id INTEGER PRIMARY KEY AUTOINCREMENT, "+DATA.CONTACT_ID+" INTEGER, "+DATA.MIMETYPE+" INTEGER, "+
				DATA.DATA1+" VARCHAR, "+DATA.DATA2+" VARCHAR, "+DATA.DATA3+" VARCHAR, "+DATA.DATA4+" VARCHAR, "+
				DATA.DATA5+" VARCHAR, "+DATA.DATA6+" VARCHAR, "+DATA.DATA7+" VARCHAR, "+DATA.DATA8+" VARCHAR, "+
				DATA.DATA9+" VARCHAR, "+DATA.DATA10+" VARCHAR, "+DATA.DATA11+" VARCHAR, "+DATA.DATA12+" VARCHAR, "+
				DATA.DATA13+" VARCHAR, "+DATA.DATA14+" VARCHAR, "+DATA.DATA15+" VARCHAR)");
	}

	//如果DATABASE_VERSION值被改为2,系统发现现有数据库版本不同,即会调用onUpgrade
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		//db.execSQL("ALTER TABLE calllogs ADD COLUMN other STRING");
	}

}
