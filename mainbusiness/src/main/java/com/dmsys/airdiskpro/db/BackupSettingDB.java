package com.dmsys.airdiskpro.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.dmsys.airdiskpro.BrothersApplication;
import com.dmsys.airdiskpro.model.BakSetBean;

public class BackupSettingDB extends SQLiteOpenHelper {
	private final static String DB_NAME = "BackupSettingDB.db";
	private final static int VERSION = 1;

	public static final String TABLE_Name = "baksetting";
	public static final String _mac = "mac";
	public static final String _autoBak = "autoBak";
	public static final String _bakImage = "image";
	public static final String _bakVideo = "video";
	public static final String _bakContacts = "contacts";
	public static final String _diskName = "disk_name";
	// 这个是备份到指定disk 的文件夹名字
	public static final String _mediaFolder = "media_folder";
	public static final String _allStorage = "all_storage";
	public static final String _bakDisplay = "bakDisplay";
	public static final String _bakOnOff = "bakOnOff";
	
	public static BackupSettingDB mInstance;
	private static Object mLock = new Object();
	private static final Object SYNC_OBJECT = new Object();

	public static BackupSettingDB getInstance() {

		if (null == mInstance) {
			synchronized (mLock) {
				if (null == mInstance) {
					mInstance = new BackupSettingDB(BrothersApplication.getInstance());
				}
			}
		}
		return mInstance;
	}

	private BackupSettingDB(Context context) {
		super(context, DB_NAME, null, VERSION);
	}

	// 数据库第一次被创建时onCreate会被调用
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_Name);
		db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_Name
				+ "(_id INTEGER PRIMARY KEY AUTOINCREMENT, " + _mac + " TEXT, "
				+ _autoBak + " INTEGER, " + _bakImage + " INTEGER, "
				+ _bakVideo + " INTEGER, " + _bakContacts + " INTEGER, "
				+ _bakDisplay + " INTEGER, " + _bakOnOff + " INTEGER, "
				+ _diskName + " TEXT, " + _mediaFolder + " TEXT ,"
				+ _allStorage + " TEXT " + ")");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		onCreate(db);
	}

	/**
	 * 
	 * 删除数据库
	 */
	public void deleteDB() {
		synchronized (SYNC_OBJECT) {
			SQLiteDatabase db = null;
			try {
				db = getWritableDatabase();
				db.delete(TABLE_Name, null, null);
			} catch (Exception e) {
				return;
			} finally {
				if (null != db && db.isOpen()) {
					db.close();
				}
			}
		}
	}

	/**
	 * 本函数默认表里不存在此mac的数据
	 * 
	 * @param setBean
	 * @return
	 */
	public boolean addDiskSetting(BakSetBean setBean) {
		synchronized (SYNC_OBJECT) {
			  boolean ret = false;
			  SQLiteDatabase db = null;
			try {
				 db = getWritableDatabase();
				db.execSQL("INSERT INTO " + TABLE_Name + " VALUES(null, ?, ?, ?, ?, ?,?,?,?, ?,?)", new Object[] { setBean.mac,
						setBean.autoBak, setBean.bakImage, setBean.bakVideo, setBean.bakContacts,setBean.bakDisplay,setBean.bakOnOff ,setBean.diskName,
						setBean.media_bak_folder,setBean.allStorage });
				ret = true;
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				ret =  false;
			} finally {
				if (null != db && db.isOpen()) {
	                db.close();
	            }
			}
			return ret;
		}
		
		}
	
	public boolean existDiskMac(String mac) {
		synchronized (SYNC_OBJECT) {
			
			boolean res;
			String sql = "SELECT * FROM " + TABLE_Name + " WHERE " + _mac + " = ?";
			Cursor c = null;
			 SQLiteDatabase db = null;
			try {
				 db = getWritableDatabase();
				c = db.rawQuery(sql, new String[] { mac });
				if (c.moveToFirst()) {
					res = true;
				} else {
					res = false;
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				res = false;
			} finally {
				if (c != null) {
					c.close();
				}
				if (null != db && db.isOpen()) {
	                db.close();
	            }

			}
			return res;
		}
	}
	
	

	/**
	 * 本函数默认表里已经存在此mac的数据了
	 * 
	 * @param setBean
	 * @return
	 */
	public void updateDiskMac(BakSetBean setBean) {
		synchronized (SYNC_OBJECT) {
			 SQLiteDatabase db = null;
				try {
					 db = getWritableDatabase();
				ContentValues cv = new ContentValues();
				cv.put(_mac, setBean.mac);
				cv.put(_autoBak, setBean.autoBak);
				cv.put(_bakImage, setBean.bakImage);
				cv.put(_bakVideo, setBean.bakVideo);
				cv.put(_bakContacts, setBean.bakContacts);
				cv.put(_diskName, setBean.diskName);
				cv.put(_mediaFolder, setBean.media_bak_folder);
				cv.put(_allStorage, setBean.allStorage);
				cv.put(_bakDisplay, setBean.bakDisplay);
				cv.put(_bakOnOff, setBean.bakOnOff);
				db.update(TABLE_Name, cv, _mac + " = ?", new String[] { "" + setBean.mac });
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				if (null != db && db.isOpen()) {
	                db.close();
	            }
				
			}
		}
	}
	
	public BakSetBean getDiskBakSetting(String mac) {
		synchronized (SYNC_OBJECT) {

			BakSetBean res = null;
			String sql = "SELECT * FROM " + TABLE_Name + " WHERE " + _mac + " = ?";
			Cursor c = null;
			 SQLiteDatabase db = null;
				try {
				 db = getWritableDatabase();
				c = db.rawQuery(sql, new String[] { mac });
				if (c.moveToFirst()) {
					String tMac = c.getString(c.getColumnIndex(_mac));
					int tAutoBak = c.getInt(c.getColumnIndex(_autoBak));
					int tImage = c.getInt(c.getColumnIndex(_bakImage));
					int tVideo = c.getInt(c.getColumnIndex(_bakVideo));
					int tContacts = c.getInt(c.getColumnIndex(_bakContacts));
					int tDisplay = c.getInt(c.getColumnIndex(_bakDisplay));
					int tOnOff = c.getInt(c.getColumnIndex(_bakOnOff));
					String tDiskName = c.getString(c.getColumnIndex(_diskName));
					String tMediaFolder = c.getString(c.getColumnIndex(_mediaFolder));
					String allStorage = c.getString(c.getColumnIndex(_allStorage));
					res = new BakSetBean(tMac, tAutoBak, tImage, tVideo, tContacts, tDiskName, tMediaFolder,allStorage,tDisplay,tOnOff);
				} else {
					res = null;
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				res = null;
			} finally {
				if (c != null) {
					c.close();
				}
				if (null != db && db.isOpen()) {
	                db.close();
	            }
			}
			return res;
		}
	}
		
		
		
		
		
		
	

}
