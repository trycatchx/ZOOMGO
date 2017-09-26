package com.dmsys.airdiskpro.db;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.dmsys.airdiskpro.model.LogContactBean;
import com.dmsys.airdiskpro.utils.StreamTool;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * 备份记录数据库管理类
 * 
 */
public class BackupCLogDBManager {
	private static final Object SYNC_OBJECT = new Object();
	private BakCLogDBHelper helper;
	public SQLiteDatabase db;

	private static BackupCLogDBManager instance;

	/**
	 * 获取实例
	 * 
	 * @throws IOException
	 */
	// public static BackupLogDBManager getInstance(Context context) {
	// if (instance == null) {
	// instance = new BackupLogDBManager(context);
	// }
	// return instance;
	// }

	public BackupCLogDBManager(Context context, String dbPath, String dbName) throws IOException {
		synchronized (SYNC_OBJECT) {

			if (!StreamTool.ensureFilePathExist(dbPath))// 带“/”
			{
				throw new IOException("full local storage");
			}
			helper = new BakCLogDBHelper(context, dbPath, dbName);
			db = helper.getWritableDatabase();
		}
	}

	/**
	 * 获取联系人的上次备份点
	 * 
	 * @return
	 */
	public int getContactLastBakNode() {
		return getInfoLastBakNode();
	}

	/**
	 * 向contact表中插入一条联系人备份日志
	 * 
	 * @param contactBean
	 */
	public void addContactLog(LogContactBean contactBean) {
		synchronized (SYNC_OBJECT) {

			try {
				db.beginTransaction(); // 开始事务
				db.execSQL("INSERT INTO " + BakCLogDBHelper.CONTACT_TABLE + " VALUES(null, ?, ?, ?)", new Object[] { contactBean.num,
						contactBean.node, contactBean.time });
				db.setTransactionSuccessful(); // 设置事务成功完成
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				db.endTransaction(); // 结束事务
			}
		}
	}

	/**
	 * 获取联系人的上次备份记录 如果没记录，返回null
	 * 
	 * @return
	 */
	public LogContactBean getContactLastRecord() {
		synchronized (SYNC_OBJECT) {

			String sql = "SELECT * FROM " + BakCLogDBHelper.CONTACT_TABLE + " ORDER BY bak_node DESC LIMIT 1";
			Cursor c = null;
			LogContactBean contactLog = new LogContactBean(0, 0, 0);
			try {
				c = db.rawQuery(sql, null);
				if (c.moveToFirst()) {
					contactLog = new LogContactBean(c.getInt(c.getColumnIndex("bak_num")), c.getInt(c.getColumnIndex("bak_node")),
							c.getLong(c.getColumnIndex("bak_time")));
				} else {
					// empty cursor
					contactLog = null;
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				if (c != null) {
					c.close();
				}
			}
			return contactLog;
		}
	}

	/**
	 * 按备份点降序获取联系人备份日志列表
	 * 
	 * @param type
	 * @return
	 */
	public List<LogContactBean> getContactLogList() {
		synchronized (SYNC_OBJECT) {

			ArrayList<LogContactBean> contactLogList = new ArrayList<LogContactBean>();
			String sql = "SELECT * FROM " + BakCLogDBHelper.CONTACT_TABLE + " ORDER BY bak_node DESC";
			Cursor c = null;
			try {
				c = db.rawQuery(sql, null);
				if (c.moveToFirst()) {
					do {
						LogContactBean contactLog = new LogContactBean(c.getInt(c.getColumnIndex("bak_num")), c.getInt(c
								.getColumnIndex("bak_node")), c.getLong(c.getColumnIndex("bak_time")));

						contactLogList.add(contactLog);
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
			return contactLogList;
		}
	}

	/**
	 * 获取备份信息的上次备份点
	 * 
	 * @return
	 */
	private int getInfoLastBakNode() {
		synchronized (SYNC_OBJECT) {

			String sql = "SELECT * FROM " + BakCLogDBHelper.CONTACT_TABLE + " ORDER BY bak_node DESC LIMIT 1";
			Cursor c = null;
			int ret = 0;
			try {
				c = db.rawQuery(sql, null);
				ret = 0;
				if (c.moveToFirst()) {
					ret = c.getInt(c.getColumnIndex("bak_node"));
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				if (c != null) {
					c.close();
				}
			}
			return ret;
		}
	}

	/**
	 * 删除数据库
	 */
	public void deleteDB() {
		synchronized (SYNC_OBJECT) {

			try {
				db.delete(BakCLogDBHelper.CONTACT_TABLE, null, null);
			} catch (Exception e) {
				return;
			}
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
