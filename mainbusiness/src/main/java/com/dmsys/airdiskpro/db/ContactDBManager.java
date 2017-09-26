package com.dmsys.airdiskpro.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.dmsys.airdiskpro.model.DBContactsInfoBean;
import com.dmsys.airdiskpro.model.DBDataBean;
import com.dmsys.airdiskpro.model.MimetypeData;
import com.dmsys.airdiskpro.model.TypeNum;
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

public class ContactDBManager {
	private static final Object SYNC_OBJECT = new Object();
	private ContactDBHelper helper;
	public SQLiteDatabase db;

	// private ArrayList<String> dataList = new ArrayList<String>();

	public ContactDBManager(Context context, String dbPath, String dbName) throws IOException {
		synchronized (SYNC_OBJECT) {

			if (!StreamTool.ensureFilePathExist(dbPath))// 带“/”
			{
				throw new IOException("full local storage");
			}
			helper = new ContactDBHelper(context, dbPath, dbName);
			db = helper.getWritableDatabase();
		}
	}

	/**
	 * 
	 * 向版本信息表中插入一条备份信息记录
	 * 
	 * @param versionValue
	 * @param phoneName
	 * @param exportTime
	 */
	public void insertVersionLog(String versionValue, String phoneName, String exportTime) {
		synchronized (SYNC_OBJECT) {

			db.beginTransaction(); // 开始事务
			db.execSQL("INSERT INTO " + ContactDBHelper.VERSION_TABLE + " VALUES(null, ?, ?, ?)", new Object[] { versionValue, phoneName,
					exportTime });
			db.setTransactionSuccessful(); // 设置事务成功完成
			db.endTransaction(); // 结束事务
		}
	}

	/**
	 * 向基本详情表中插入一条联系人记录,并返回一个id，用于向photo表和data表中插入其他数据,插入时没有photoId和groupId
	 * 插入完成后用返回的contactId，将联系人头像插入到photo表中获取photoId，再将photoId更新到此Contact表
	 * 
	 * @param dbInfoBean
	 */
	public int insertContactInfo(DBContactsInfoBean dbInfoBean) {
		synchronized (SYNC_OBJECT) {

			db.beginTransaction(); // 开始事务
			db.execSQL("INSERT INTO " + ContactDBHelper.CONTACTS_TABLE + " VALUES(null, ?, ?, ?, ?, ?)", new Object[] { dbInfoBean.md5,
					dbInfoBean.accountName, dbInfoBean.accountType, dbInfoBean.photoId, dbInfoBean.groupId });
			db.setTransactionSuccessful(); // 设置事务成功完成
			db.endTransaction(); // 结束事务
			Cursor cursor = db.rawQuery("SELECT _id from " + ContactDBHelper.CONTACTS_TABLE + " order by _id desc limit 1", null);
			int id = -1;
			if (cursor.moveToFirst()) {
				id = cursor.getInt(cursor.getColumnIndex("_id"));
			}
			cursor.close();
			return id;
		}
	}

	/**
	 * 更新联系人头像
	 * 
	 * @param contactId
	 * @param photoId
	 */
	public void updateContactPhotoId(int contactId, int photoId) {
		synchronized (SYNC_OBJECT) {

			ContentValues cv = new ContentValues();
			cv.put(ContactDBHelper.CONTACTS.PHOTO_ID, photoId);
			db.update(ContactDBHelper.CONTACTS_TABLE, cv, ContactDBHelper.CONTACTS._ID + " = ?", new String[] { "" + contactId });
		}
	}

	/**
	 * 更新联系人群组
	 * 
	 * @param contactId
	 * @param groupId
	 */
	public void updateContactGroupId(int contactId, int groupId) {
		synchronized (SYNC_OBJECT) {

			ContentValues cv = new ContentValues();
			cv.put(ContactDBHelper.CONTACTS.GROUP_ID, groupId);
			db.update(ContactDBHelper.CONTACTS_TABLE, cv, ContactDBHelper.CONTACTS._ID + " = ?", new String[] { "" + contactId });
		}
	}

	/**
	 * 向photo表中插入一条联系人头像记录
	 * 
	 * @param contactId
	 * @param photoVal
	 */
	public int insertPhotoInfo(int contactId, String photoVal) {
		synchronized (SYNC_OBJECT) {

			db.execSQL("INSERT INTO " + ContactDBHelper.PHOTO_TABLE + " VALUES(null, ?, ?)", new Object[] { contactId, photoVal });
			Cursor cursor = db.rawQuery("SELECT _id from " + ContactDBHelper.PHOTO_TABLE + " order by _id desc limit 1", null);
			int id = -1;
			if (cursor.moveToFirst()) {
				id = cursor.getInt(cursor.getColumnIndex(ContactDBHelper.PHOTO._ID));
			}
			cursor.close();
			return id;
		}
	}

	/**
	 * 向群组表中插入一条记录
	 * 
	 * @param groupName
	 */
	public void insertGroupInfo(String groupName) {
		synchronized (SYNC_OBJECT) {

			db.beginTransaction(); // 开始事务
			db.execSQL("INSERT INTO " + ContactDBHelper.GROUP_TABLE + " VALUES(null, ?)", new Object[] { groupName });
			db.setTransactionSuccessful(); // 设置事务成功完成
			db.endTransaction(); // 结束事务
		}
	}

	/**
	 * 根据群组名获取群组id
	 * 
	 * @param groupName
	 * @return
	 */
	public int getGroupId(String groupName) {
		synchronized (SYNC_OBJECT) {

			Cursor cursor = db.rawQuery("SELECT * FROM " + ContactDBHelper.GROUP_TABLE + " WHERE " + ContactDBHelper.GROUP.GROUP_NAME
					+ " = ?", new String[] { groupName });
			int id = 0;
			if (cursor.moveToFirst()) {
				id = cursor.getInt(cursor.getColumnIndex(ContactDBHelper.GROUP._ID));
			}
			cursor.close();
			return id;
		}
	}

	/**
	 * 向data表中插入一条详细数据
	 * 
	 * @param dataBean
	 */
	public void insertData(DBDataBean dataBean) {
		synchronized (SYNC_OBJECT) {

			db.beginTransaction(); // 开始事务
			ArrayList<String> dataList = dataBean.dataList;
			//Log.i("Tag", "DBDataBean.dataList>>>>>>>>" + dataList.toString());
			String data2 = dataList.get(2);
			if(dataBean.mimetype == MimetypeData.MIMETYPE_PHONE) {
				if(isStandardFormatForData3(dataList.get(3))) {
					data2 = "0";
				} else {
					data2 = "1";
				}
			}
			db.execSQL(
					"INSERT INTO " + ContactDBHelper.DATA_TABLE + " VALUES(null, ?, ?, " + "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
					new Object[] { dataBean.contactId, dataBean.mimetype, dataList.get(1), data2, dataList.get(3),
							dataList.get(4), dataList.get(5), dataList.get(6), dataList.get(7), dataList.get(8), dataList.get(9),
							dataList.get(10), dataList.get(11), dataList.get(12), dataList.get(13), dataList.get(14), dataList.get(15) });
			db.setTransactionSuccessful(); // 设置事务成功完成
			db.endTransaction(); // 结束事务
		}
	}
	
	public boolean isStandardFormatForData3(String data3) {
		boolean ret = false;
		if(data3 != null && data3.equals(TypeNum.String_ADDR_HOME) 
				|| data3.equals(TypeNum.String_ADDR_WORK) 
				|| data3.equals(TypeNum.String_ADDR_OTHER)) {
			ret = true;
		}
		return ret;
	}

	/**
	 * 向data表中插入一组详细数据
	 * 
	 * @param dataBeanList
	 */
	public void insertDataList(ArrayList<DBDataBean> dataBeanList) {
		synchronized (SYNC_OBJECT) {

			db.beginTransaction(); // 开始事务
			//Log.i("Tag", "<<<<<<<<<<<<<>>>>>>>>");
			//Log.i("Tag", "dataBeanList.size()>>>>>>>>" + dataBeanList.size());
			for (DBDataBean dataBean : dataBeanList) {
				ArrayList<String> dataList = dataBean.dataList;
				//Log.i("Tag", "dataBean.mimetype>>>>>>>>" + dataBean.mimetype);
				//Log.i("Tag", "dataBean.dataList>>>>>>>>" + dataList.toString());
				String data2 = dataList.get(2);
				if(dataBean.mimetype == MimetypeData.MIMETYPE_PHONE) {
					if(isStandardFormatForData3(dataList.get(3))) {
						data2 = "0";
					} else {
						data2 = "1";
					}
				}
				db.execSQL(
						"INSERT INTO " + ContactDBHelper.DATA_TABLE + " VALUES(null, ?, ?, "
								+ "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
						new Object[] { dataBean.contactId, dataBean.mimetype, dataList.get(1), data2, dataList.get(3),
								dataList.get(4), dataList.get(5), dataList.get(6), dataList.get(7), dataList.get(8), dataList.get(9),
								dataList.get(10), dataList.get(11), dataList.get(12), dataList.get(13), dataList.get(14), dataList.get(15) });
			}
			//Log.i("Tag", "<<<<<<<<<<<<<>>>>>>>>");
			db.setTransactionSuccessful(); // 设置事务成功完成
			db.endTransaction(); // 结束事务
		}
	}

	/**
	 * 在contact_data表中添加完某一联系人的所有信息后更新该联系人在contact_raw的md5值
	 * 
	 * @param rawContactId
	 * @param md5
	 */
	/*
	 * public void updateMD5(int rawContactId , String md5){ if (rawContactId !=
	 * -1) { Cursor cursor = db.rawQuery("SELECT md5 FROM "+
	 * ContactDBHelper.CONTACT_RAW +" WHERE _id = ?", new
	 * String[]{""+rawContactId}); if (cursor.moveToFirst()) { ContentValues
	 * values = new ContentValues(); values.put("md5", md5);
	 * db.update(ContactDBHelper.CONTACT_RAW , values, "_id = ?", new
	 * String[]{""+rawContactId}); } } }
	 */

	/**
	 * 关闭数据库
	 */
	public void closeDB() {
		synchronized (SYNC_OBJECT) {

			db.close();
		}
	}
}
