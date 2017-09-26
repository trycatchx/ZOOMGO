package com.dmsys.airdiskpro.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabaseCorruptException;

import com.dmsys.airdiskpro.model.ContactsConfig;
import com.dmsys.airdiskpro.utils.StreamTool;

import java.io.IOException;
import java.util.ArrayList;


/**
 * 通讯录的备份数据库管理类，提供插入、查询通讯录的方法 可根据需要封装一些常用的方法
 * 
 * @author Alan.wang
 * @date 2014-09-09
 * 
 */

public class CConfigDBManager {
	private static final Object SYNC_OBJECT = new Object();

	private CConfigDBHelper helper;
	public SQLiteDatabase db;

	// private ArrayList<String> dataList = new ArrayList<String>();

	public CConfigDBManager(Context context, String dbPath, String dbName) throws IOException {
		synchronized (SYNC_OBJECT) {

			if (!StreamTool.ensureFilePathExist(dbPath))// 带“/”
			{
				throw new IOException("full local storage");
			}
			helper = new CConfigDBHelper(context, dbPath, dbName);
			db = helper.getWritableDatabase();
		}
	}

	public void insertConfigLog(long time, String phoneModel, int contactsNum, String md5) throws SQLiteDatabaseCorruptException {
		synchronized (SYNC_OBJECT) {

			db.beginTransaction(); // 开始事务
			db.execSQL("INSERT INTO " + CConfigDBHelper.CONFIG_TABLE + " VALUES(null, ?, ?, ?, ?)", new Object[] { time, phoneModel,
					contactsNum, md5 });
			db.setTransactionSuccessful(); // 设置事务成功完成
			db.endTransaction(); // 结束事务
		}
	}
	
	public void deleteConfig(String md5) throws SQLiteDatabaseCorruptException {
		synchronized (SYNC_OBJECT) {
			 db.delete(CConfigDBHelper.CONFIG_TABLE, CConfigDBHelper.CONFIG.MD5 + "=? ", new String[]{ md5 });
		}
	}

	public ArrayList<ContactsConfig> selectConfig() {
		synchronized (SYNC_OBJECT) {

			ArrayList<ContactsConfig> res = new ArrayList<ContactsConfig>();
			String sql = "SELECT * FROM " + CConfigDBHelper.CONFIG_TABLE + " ORDER BY _id DESC";
			Cursor c = null;
			try {
				c = db.rawQuery(sql, null);
				if (c.moveToFirst()) {
					do {
						ContactsConfig config = new ContactsConfig(c.getLong(c.getColumnIndex(CConfigDBHelper.CONFIG.TIME)), c.getString(c
								.getColumnIndex(CConfigDBHelper.CONFIG.PHONE_MODEL)),
								c.getInt(c.getColumnIndex(CConfigDBHelper.CONFIG.NUM)), c.getString(c
										.getColumnIndex(CConfigDBHelper.CONFIG.MD5)));

						res.add(config);
					} while (c.moveToNext());
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				if (c != null) {
					c.close();
				}
			}
			return res;
		}
	}

	/**
	 * 关闭数据库
	 */
	public void closeDB() {
		synchronized (SYNC_OBJECT) {

			db.close();
		}
	}
}
