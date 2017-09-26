package com.dmsys.airdiskpro.utils;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.os.Build;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Groups;
import android.provider.ContactsContract.RawContacts;
import android.util.Log;

import com.dmsys.airdiskpro.backup.BackupInfoListener;
import com.dmsys.airdiskpro.db.ContactDBManager;
import com.dmsys.airdiskpro.model.ContactInfo;
import com.dmsys.airdiskpro.model.DBContactsInfoBean;
import com.dmsys.airdiskpro.model.RestoreContact2;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class ContactHandler {
	private static final String TAG = "ContactHandler";
	public static String mSDPath = null;
	private float pullFromPhoneMax = 40;
	public static final int SYS_CUS = 0;	//系统联系人自定义类型时的type值
	public static final int OUR_CUS = 1;	//我们本地数据库自定义类型时的type值
	public static final int OUR_SYSTYPE = 0;	//我们本地数据库系统有的类型时的type值
	public boolean isUserStop = false;
	public float getPullFromPhoneMax() {
		return pullFromPhoneMax;
	}
	public void setPullFromPhoneMax(float num) {
		pullFromPhoneMax = num;
	}

	private final float contactsToDBMax = 80;
	private float proIndex = 0;
	private final float DelPreProMax = 20;
	private final float DelPreProAfter = 50;
	private BackupInfoListener backupListener = null;
	public static String curToken = null; // unlock获取的备份文件url
	
	public static final int COPY_COED_HANE_NO_CONTACTS = 0;
	public static final int COPY_COED_SUCCESS = 1;
	

	public interface ExceptionListener {
		public void onError(String error);
	}

	public ArrayList<ArrayList<String>> mAccStrucList = null; // 0: id 1:accName
																// 2:accType
	private static ContactHandler instance_ = null;

	/** 获取实例 */
	public static ContactHandler getInstance() {
		if (instance_ == null) {
			instance_ = new ContactHandler();
		} else {
		}
		return instance_;
	}


	public BackupInfoListener getBackupListener() {
		return backupListener;
	}

	public void setBackupListener(BackupInfoListener backupProgressListener) {
		this.backupListener = backupProgressListener;
	}

	
	public float getProIndex() {
		return proIndex;
	}

	public void setProIndex(float proIndex) {
		this.proIndex = proIndex;
	}
	/**
	 * 获取联系人指定信息
	 * 
	 * @param projection
	 *            指定要获取的列数组, 获取全部列则设置为null
	 * @return
	 * @throws Exception
	 */
	public Cursor queryContact(Context context, String[] projection) {
		// 获取联系人的所需信息
		Cursor cur = context.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, projection, null, null, null);
		return cur;
	}

	public String getRawContactIDAccInfo(Context context, String contactID, ContactInfo info) {
		String rawContactsId = "";
		// 读取rawContactsId
		Cursor rawContactsCur = null;
		try {
			Log.d("wuxiujie", "contactID="+contactID);
			rawContactsCur = context.getContentResolver().query(RawContacts.CONTENT_URI, null, RawContacts.CONTACT_ID + " = ?",
					new String[] { contactID }, null);
			if (rawContactsCur.moveToFirst()) {
				Log.d("wuxiujie", "if (rawContactsCur.moveToFirst())  ="+rawContactsCur);
				String accountName = rawContactsCur.getString(rawContactsCur.getColumnIndex(RawContacts.ACCOUNT_NAME));
				String accountType = rawContactsCur.getString(rawContactsCur.getColumnIndex(RawContacts.ACCOUNT_TYPE));
				info.setAccountName(accountName);
				info.setAccountType(accountType);
				// 该查询结果一般只返回一条记录，所以我们直接让游标指向第一条记录

				// 读取第一条记录的RawContacts._ID列的值
				rawContactsId = rawContactsCur.getString(rawContactsCur.getColumnIndex(RawContacts._ID));
			}
		} catch (Exception e) {
			// TODO: handle exception
			Log.d("wuxiujie", "Exception");
		}finally{
			rawContactsCur.close();
			Log.d("wuxiujie", "close");
		}
		return rawContactsId;
	}

	public String getRawContactID(Context context, String contactID) {
		String rawContactsId = "";
		// 读取rawContactsId
		Cursor rawContactsCur = context.getContentResolver().query(RawContacts.CONTENT_URI, null, RawContacts.CONTACT_ID + " = ?",
				new String[] { contactID }, null);
		// 该查询结果一般只返回一条记录，所以我们直接让游标指向第一条记录
		if (rawContactsCur.moveToFirst()) {
			// 读取第一条记录的RawContacts._ID列的值
			rawContactsId = rawContactsCur.getString(rawContactsCur.getColumnIndex(RawContacts._ID));
		}
		rawContactsCur.close();
		return rawContactsId;
	}

	/**
	 * 从手机通讯录获取联系人信息
	 * 
	 * @param context
	 * @return
	 */
	public ArrayList<ContactInfo> getContactInfo(Context context,boolean showPro)throws InterruptedException {

		ReadContact readContact = new ReadContact(context);
		ArrayList<ContactInfo> infoList = new ArrayList<ContactInfo>();
		String rawID;
		Cursor cur = queryContact(context, null);
		float totalContactNum = cur.getCount();
		float oneContactPro = pullFromPhoneMax / totalContactNum;
		if (cur.moveToFirst()) {
			do {
				//用户中断
				if(isUserStop)  {
					if(cur != null) {
						cur.close();
					}
					throw new InterruptedException();
				}
				
				String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
				ContactInfo info = new ContactInfo();// 初始化联系人信息
				rawID = getRawContactIDAccInfo(context, id, info);
				// 查raw表获取rawid==rawID的cursor
				// 遍历cursor 如果得到的mimetype == phonetype 就进入函数 带入info和cursor
				// 在函数里info.setlist 或 getlist.add
				Cursor cursor = context.getContentResolver().query(ContactsContract.Data.CONTENT_URI, null,
						ContactsContract.CommonDataKinds.StructuredName.RAW_CONTACT_ID + " = ? ", new String[] { rawID }, null);
				if(cursor.moveToFirst() == false)
				{
					cursor.close();
					continue;
				}
				do {
					//用户中断
					if(isUserStop) {
						if( cursor != null) {
							cursor.close();
						}
						if(cur != null) {
							cur.close();
						}
						throw new InterruptedException();
					}
					
					String mimeType = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.MIMETYPE));
					if (mimeType.equals(ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)) {
						readContact.readStructuredName2(info, cursor);
						readContact.readPhoneticName2(info, cursor);
					} else if (mimeType.equals(ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE)) {
						readContact.readOrganization2(info, cursor);
					} else if (mimeType.equals(ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE)) {
						readContact.readPostal2(info, cursor);
					} else if (mimeType.equals(ContactsContract.CommonDataKinds.SipAddress.CONTENT_ITEM_TYPE)) {
						readContact.readSipAddress2(info, cursor);
					} else if (mimeType.equals(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)) {
						readContact.readTel2(info, cursor);
					} else if (mimeType.equals(ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)) {
						readContact.readEmail2(info, cursor);
					} else if (mimeType.equals(ContactsContract.CommonDataKinds.Im.CONTENT_ITEM_TYPE)) {
						readContact.readIM2(info, cursor);
					} else if (mimeType.equals(ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE)) {
						readContact.readWebsite2(info, cursor);
					} else if (mimeType.equals(ContactsContract.CommonDataKinds.Nickname.CONTENT_ITEM_TYPE)) {
						readContact.readNickName2(info, cursor);
					} else if (mimeType.equals(ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE)) {
						readContact.readNote2(info, cursor);
					} else if (mimeType.equals(ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE)) {
						readContact.readPhoto2(info, cursor);
					} else if (mimeType.equals(ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE)) {
						readContact.readGroup2(info, cursor);
					} else if (mimeType.equals(ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE)) {
						readContact.readEvent2(info, cursor);
					} else if (mimeType.equals(ContactsContract.CommonDataKinds.Relation.CONTENT_ITEM_TYPE)) {
						readContact.readRelation2(info, cursor);
					}
				} while (cursor.moveToNext());
				
				if(cursor!=null)
					cursor.close();
				//算一下md5值 保存到本身info中
				info.buildMD5();
				
				infoList.add(info);
				if(showPro == true)
				{
					proIndex += oneContactPro;
					showProgress(backupListener, proIndex);
				}
			} while (cur.moveToNext());
		}
		if(cur!=null)
			cur.close();
		return infoList;
	}





	private boolean alreadyIn(ContactInfo restoreInfo, Context context, ArrayList<String> localMd5List) {
		boolean resIN = false;
		for (String localMD5 : localMd5List) {
			if (restoreInfo.getMd5().equals(localMD5)) {
				resIN = true;
				break;
			}
		}
		return resIN;

	}


	/**
	 * 恢复所有联系人到手机通讯录中
	 * 
	 * @param restoreList
	 * @param context
	 * @param handler
	 * @throws InterruptedException 
	 */
	public int CopyAll2Phone(List<ContactInfo> restoreList, Context context, boolean isCheckMode,int startPro,int endPro) throws InterruptedException {

		RestoreContact2 restoreContact2 = new RestoreContact2(context);
		int rawContactInsertIndex;
		// HashMap<String, String> groupStringtoID =
		// restoreContact2.prepareGroups(list);
		// Log.d("ra_group", "groupStringtoID-->"+groupStringtoID);
		ArrayList<String> md5List = new ArrayList<String>();
		if (isCheckMode == true) {
			ArrayList<ContactInfo> infosLocalPhone = getContactInfo(context,false);
			
			if (infosLocalPhone != null) {
				for (ContactInfo info : infosLocalPhone) {
					
					if (isUserStop) throw new InterruptedException();
					if(info != null) {
						md5List.add(info.getMd5());
					}
				}
			}
		}
		ArrayList<ContactInfo> infoList = leachRestoreList(context, restoreList, md5List, isCheckMode);
		if(infoList.size() == 0)
		{
			showProgress(getBackupListener(), 100);
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return COPY_COED_HANE_NO_CONTACTS;
		}
		int temp_num = 0;
//		ArrayList<ContactInfo> infoList = (ArrayList<ContactInfo>) restoreList;
		ArrayList<ArrayList<ContentProviderOperation>> opsList = new ArrayList<ArrayList<ContentProviderOperation>>();
		ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
		float pro = startPro;
		float onePro = (((80 - startPro) *1.0f)/infoList.size());
		
		for (int i = 0; i < infoList.size(); i++) {
			
			if (isUserStop) throw new InterruptedException();
			
			temp_num++;
			rawContactInsertIndex = ops.size();
			ContactInfo info = infoList.get(i);
			
			/*try {
				System.out.println("i=" + i + " ," +info.getStructName().familyName + " " + info.getStructName().givenName);
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}*/
			
			restoreContact2.restoreRawAccInfo(info, ops);
			restoreContact2.restorePhoneNum(info, ops, rawContactInsertIndex);
			restoreContact2.restoreStructNameAndPhonetic(info, ops, rawContactInsertIndex);
			restoreContact2.restoreOrganization(info, ops, rawContactInsertIndex);
			restoreContact2.restoreEventList(info, ops, rawContactInsertIndex);
			restoreContact2.restorePostalAddressList(info, ops, rawContactInsertIndex);
			restoreContact2.restoreEmailList(info, ops, rawContactInsertIndex);
			restoreContact2.restoreIMList(info, ops, rawContactInsertIndex);
			restoreContact2.restoreURLList(info, ops, rawContactInsertIndex);
			restoreContact2.restoreNickName(info, ops, rawContactInsertIndex);
			restoreContact2.restoreNote(info, ops, rawContactInsertIndex);
			restoreContact2.restorePhoto(info, ops, rawContactInsertIndex);
			restoreContact2.restoreRelationList(info, ops, rawContactInsertIndex);
			restoreContact2.restoreSipAddressList(info, ops, rawContactInsertIndex);
			if (i != 0) {
				if ((i % (ContactStaticValue.ONE_BATCH_COUNT - 1)) == 0) {
					opsList.add(ops);
					ops = new ArrayList<ContentProviderOperation>();
				}
				if ((i == infoList.size() - 1) && (i % (ContactStaticValue.ONE_BATCH_COUNT - 1) != 0)) {
					opsList.add(ops);
				}
			}
			if ((i == infoList.size() - 1) && (i == 0))
				opsList.add(ops);
			
			pro += onePro;
			showProgress(getBackupListener(), pro);
		}
		
		for (int i = 0;i<opsList.size();i++) {
			
			ArrayList<ContentProviderOperation> operation = opsList.get(i);
			
			if (isUserStop) throw new InterruptedException();
			
			try {
				// 这里才调用的批量添加
				ContentProviderResult[] results = context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, operation);
				
			}catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
			
			System.out.println("i="+i+",opsList:"+opsList.size());
			int progress = 80 + (i+1)*20/opsList.size();
			showProgress(getBackupListener(), progress);
			
		}
		showProgress(getBackupListener(), 100);
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return COPY_COED_SUCCESS;
//		getBackupListener().onCompleted(true, 0,opsList.size());

	}

	private ArrayList<ContactInfo> leachRestoreList(Context context, List<ContactInfo> originalList, ArrayList<String> md5List, boolean isCheckMode) {
		ArrayList<ContactInfo> resList = new ArrayList<ContactInfo>();
		for (ContactInfo info : originalList) {
			if (isCheckMode == true) {
				if (alreadyIn(info, context, md5List) == true)
					continue;
			}
			resList.add(info);
		}
		return resList;
	}




	/**
	 * 删除所有联系人
	 * 
	 * @param context
	 * @return
	 * @throws InterruptedException 
	 */
	public int delAllContacts(Context context,int startPro,int endPro) throws InterruptedException {
		// 第一次全删除 会有几个未删除
		int num = 0;
		boolean firstDel = true;
		do {
			if (isUserStop){
				throw new InterruptedException();
			} 
			Cursor cur = queryContact(context, null);
			num = cur.getCount();
			if (firstDel == true) {
				clearCursor(cur, context, true,startPro,endPro);
			} else {
				clearCursor(cur, context, false,startPro,endPro);
			}
			cur.close();
			firstDel = false;
		} while (num > 0);
		return num;
	}

	private void clearCursor(Cursor cur, Context context, boolean showPro,int startPro,int endPro) throws InterruptedException {
		if (cur.moveToFirst()) {
			float totalContactNum = 0;
			float oneContactPro = 0;
			float proIndex = 0;
			if (showPro == true) {
				totalContactNum = cur.getCount();
				oneContactPro = (endPro - startPro) / totalContactNum;
				proIndex = startPro;
			}
			do {
				if (isUserStop){
					throw new InterruptedException();
				} 
				String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
				String rawID = getRawContactID(context, id);
				context.getContentResolver().delete(ContentUris.withAppendedId(RawContacts.CONTENT_URI, Long.valueOf(rawID)), null, null);
				if (showPro == true) {
					proIndex += oneContactPro;
					showProgress(getBackupListener(), proIndex);
				}
			} while (cur.moveToNext());
		}
	}



	private static boolean isNull(String str) {
		if (str == null) {
			return true;
		}
		if (str.length() == 0) {
			return true;
		}
		return false;
	}


	public void backUpContactDataToDb(Context context, List<ContactInfo> infos, String backupFilePath, String fileName, String versionValue) throws InterruptedException, IOException {
		//Log.i("Tag", "infos>>>>>>>>" + infos.toString());
		ContactDBManager contactDBManager = new ContactDBManager(context, backupFilePath, fileName);
		InfoToDBHandler cHandler = InfoToDBHandler.getInstance();
		// 获取所有群组
		Cursor cursor = context.getContentResolver().query(Groups.CONTENT_URI,
				new String[] { Groups._ID, Groups.TITLE, }, null, null, null);
		while (cursor.moveToNext()) {
			if(isUserStop) {
				File file = new File(backupFilePath+fileName);
				if(file != null && file.exists()) {
					file.delete();
				}
				throw new InterruptedException();
			}
			String groupName = cursor.getString(cursor.getColumnIndex(Groups.TITLE));
			contactDBManager.insertGroupInfo(groupName);
		}
		float totalContactNum = infos.size();
		float oneContactPro = contactsToDBMax / totalContactNum;

		
		for (ContactInfo contactInfo : infos) {
			if(isUserStop) {
				File file = new File(backupFilePath+fileName);
				if(file != null && file.exists()) {
					file.delete();
				}
				throw new InterruptedException();
			}
			int groupId = 0;
			DBContactsInfoBean dbInfoBean = new DBContactsInfoBean(contactInfo.getMd5(), contactInfo.getAccountName(), contactInfo.getAccountType(),
					0, groupId);
			int raw_id = contactDBManager.insertContactInfo(dbInfoBean);
			if (raw_id == 216) {
				System.out.println("raw_id:"+raw_id);
			}
			
			cHandler.saveInfoToDb(contactDBManager, raw_id, contactInfo);
			proIndex += oneContactPro;
			showProgress(backupListener, proIndex);
			
		}
		contactDBManager.insertVersionLog(versionValue, Build.MODEL, "" + System.currentTimeMillis());
	}
	




	public InputStream getFileInputStream(String filePath) {
		File file = new File(filePath);
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return fis;
	}


	
	private void showProgress(BackupInfoListener listener,float pro)
	{
		if(listener!=null) {
			listener.onProgress((long) pro, 100);
		}
			
	}
	
	public static String makeMD5(String string) {
		byte[] hash;
		try {
			hash = MessageDigest.getInstance("MD5").digest(string.getBytes("UTF-8"));
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
		StringBuilder hex = new StringBuilder(hash.length * 2);
		for (byte b : hash) {
			if ((b & 0xFF) < 0x10)
				hex.append("0");
			hex.append(Integer.toHexString(b & 0xFF));

		}
		return hex.toString();// 32位
	}
	
	public static String makeTotalMD5(List<ContactInfo> contactInfos)
	{
		String resMD5 = null;
		StringBuffer sb = new StringBuffer();
		if(contactInfos != null)
		{
			for(ContactInfo info:contactInfos)
			{
				sb.append(info.getMd5());
			}
		}
		resMD5 = makeMD5(sb.toString());
		return resMD5;
	}
	
}
