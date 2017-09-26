package com.dmsys.airdiskpro.utils;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.dmsys.airdiskpro.backup.BackupInfoListener;
import com.dmsys.airdiskpro.db.ContactDBHelper;
import com.dmsys.airdiskpro.model.ContactInfo;
import com.dmsys.airdiskpro.model.MimetypeData;

import java.io.File;
import java.util.ArrayList;

/**
 * 
 * @ClassName: DBToInfoHandler
 * @Description: 从备份数据库获取联系人数据，封装成ArrayList<ContactInfo>
 * @author: Alan.wang
 * @date: 2014-09-29 17:26 
 * version:1.0.0
 */
public class DBToInfoHandler {

	private ArrayList<ContactInfo> contactInfoList = new ArrayList<ContactInfo>();
	private float dbToContastsProMax = 50;
	private float proIndex = 0;
	private BackupInfoListener backupListener = null;
	String photo ;
	String note ;
	//identity
	String[] DATAS = new String[]{"", ContactDBHelper.DATA.DATA1, ContactDBHelper.DATA.DATA2, 
			ContactDBHelper.DATA.DATA3, ContactDBHelper.DATA.DATA4, ContactDBHelper.DATA.DATA5, 
			ContactDBHelper.DATA.DATA6, ContactDBHelper.DATA.DATA7, ContactDBHelper.DATA.DATA8, 
			ContactDBHelper.DATA.DATA9, ContactDBHelper.DATA.DATA10, ContactDBHelper.DATA.DATA11, 
			ContactDBHelper.DATA.DATA12, ContactDBHelper.DATA.DATA13, ContactDBHelper.DATA.DATA14, 
			ContactDBHelper.DATA.DATA15};
	
	private static DBToInfoHandler _instance = null;
	private SQLiteDatabase database;
	

	/** 获取实例 */
	public static DBToInfoHandler getInstance() {
		if (_instance == null) {
			_instance = new DBToInfoHandler();
		} else {
		}
		return _instance;
	}
	public float getDbToContastsMax() {
		return dbToContastsProMax;
	}

	public void setDbToContastsMax(float dbToContastsMax) {
		this.dbToContastsProMax = dbToContastsMax;
	}

	public float getProIndex() {
		return proIndex;
	}

	public void setProIndex(float proIndex) {
		this.proIndex = proIndex;
	}
	

	
	public BackupInfoListener getBackupListener() {
		return backupListener;
	}

	public void setBackupListener(BackupInfoListener backupListener) {
		this.backupListener = backupListener;
	}
	public ArrayList<ContactInfo> getContactInfos(String dbUrl) {
		contactInfoList = new ArrayList<ContactInfo>();
		if ((new File(dbUrl)).exists()) {
//			new File(dbUrl).delete();
			database = SQLiteDatabase.openDatabase(dbUrl, null, SQLiteDatabase.OPEN_READWRITE);
			Cursor idCursor = database.rawQuery("SELECT * FROM "+ContactDBHelper.CONTACTS_TABLE, null);
			float totalContactNum = idCursor.getCount();
			float oneContactPro = dbToContastsProMax / totalContactNum;
			if (idCursor.moveToFirst()) {
				do {
					if(ContactHandler.getInstance().isUserStop)
					{
						return null;
					}
					ContactInfo contactInfo = new ContactInfo();
					int rawId = idCursor.getInt(idCursor.getColumnIndex(ContactDBHelper.CONTACTS._ID));
					//获取联系人MD5值
					String md5 = idCursor.getString(idCursor.getColumnIndex(ContactDBHelper.CONTACTS.MD5));
					contactInfo.setMd5(md5);
					//Log.i("Tag", "md5>>>>>>>>"+md5);
					//获取accountName
					int nameIndex = idCursor.getColumnIndex(ContactDBHelper.CONTACTS.ACCOUNT_NAME);
					String accountName = null;
					if(nameIndex != -1) {
						 accountName = idCursor.getString(nameIndex);
					}
					contactInfo.setAccountName(accountName);
					//Log.i("Tag", "accountName>>>>>>>>"+accountName);
					//获取accountType
					int typeIndex = idCursor.getColumnIndex(ContactDBHelper.CONTACTS.ACCOUNT_TYPE);
					String accountType = null;
					if(typeIndex != -1) {
						accountType = idCursor.getString(typeIndex);
					}
//					String accountType = idCursor.getString(idCursor.getColumnIndex(ContactDBHelper.CONTACTS.ACCOUNT_TYPE));
					contactInfo.setAccountType(accountType);
					//Log.i("Tag", "accountType>>>>>>>>"+accountType);
					//获取联系人头像
					int idIndex = idCursor.getColumnIndex(ContactDBHelper.CONTACTS.PHOTO_ID);
					int photoId =-1;
					if(idIndex != -1) {
						photoId = idCursor.getInt(idIndex);
					}
//					int photoId = idCursor.getInt(idCursor.getColumnIndex(ContactDBHelper.CONTACTS.PHOTO_ID));
					Cursor photoC = database.rawQuery("SELECT * FROM "+ContactDBHelper.PHOTO_TABLE+" WHERE "+
							ContactDBHelper.PHOTO._ID +" = ?", new String[]{""+photoId});
					if (photoC.moveToFirst()) {
						photo = photoC.getString(photoC.getColumnIndex(ContactDBHelper.PHOTO.PHOTO_VALUE));
					}
					
					if(photoC!=null)
						photoC.close();
					contactInfo.setPhoto(photo);
					//Log.i("Tag", "photo>>>>>>>>"+photo);
					int groupIndex = idCursor.getColumnIndex(ContactDBHelper.CONTACTS.GROUP_ID);
					int groupId  = -1;
					if(groupIndex != -1) {
						groupId = idCursor.getInt(groupIndex);
					}
					//获取联系人所在群组
//					int groupId = idCursor.getInt(idCursor.getColumnIndex(ContactDBHelper.CONTACTS.GROUP_ID));
					String group ="";
					Cursor groupC = database.rawQuery("SELECT * FROM "+ContactDBHelper.GROUP_TABLE+" WHERE "+
							ContactDBHelper.GROUP._ID +" = ?", new String[]{""+groupId});
					if (groupC.moveToFirst()) {
						group = groupC.getString(groupC.getColumnIndex(ContactDBHelper.GROUP.GROUP_NAME));
					}
					
					if(groupC!=null)
						groupC.close();
					
					ArrayList<String> groupList = new ArrayList<String>();
					groupList.add(group);
					contactInfo.setGroupList(groupList);
					//Log.i("Tag", "group>>>>>>>>"+group);
					
					ArrayList<ContactInfo.EmailInfo> emailList = new ArrayList<ContactInfo.EmailInfo>();
					ArrayList<ContactInfo.IM> imList = new ArrayList<ContactInfo.IM>();
					ArrayList<ContactInfo.PostalAddress> posAddrList = new ArrayList<ContactInfo.PostalAddress>();
					ArrayList<ContactInfo.PhoneInfo> phoneList = new ArrayList<ContactInfo.PhoneInfo>();
					ArrayList<ContactInfo.SipAddressInfo> sipAddressList = new ArrayList<ContactInfo.SipAddressInfo>();
					ArrayList<ContactInfo.EventInfo> eventList = new ArrayList<ContactInfo.EventInfo>();
					ArrayList<ContactInfo.WebSite> websiteList = new ArrayList<ContactInfo.WebSite>();
					ArrayList<ContactInfo.RelationInfo> relationList = new ArrayList<ContactInfo.RelationInfo>();
					
					Cursor dataCursor = getContactData(rawId);
					try {
						if (dataCursor.moveToFirst()) {
							do {
								int mimetype = dataCursor.getInt(dataCursor.getColumnIndex(ContactDBHelper.DATA.MIMETYPE));
								switch (mimetype) {
								case MimetypeData.MIMETYPE_CPOTHER://没有获取
									break;
								case MimetypeData.MIMETYPE_EMAIL: 
									setEmailList(dataCursor, contactInfo, emailList); break;
								case MimetypeData.MIMETYPE_IM: 
									setImList(dataCursor, contactInfo, imList); break;
								case MimetypeData.MIMETYPE_POSTAL_ADDRESS:
									setPostalAddressList(dataCursor, contactInfo, posAddrList); break;
								case MimetypeData.MIMETYPE_PHONE:
									setPhoneList(dataCursor, contactInfo, phoneList); break;
								case MimetypeData.MIMETYPE_NAME:
									setName(dataCursor, contactInfo); break;
								case MimetypeData.MIMETYPE_PHOTO:
									//setPhoto(dataCursor, contactInfo); 
									break;
								case MimetypeData.MIMETYPE_GROUP:
									//setGroupMembershipList(dataCursor, contactInfo); 
									break;
								case MimetypeData.MIMETYPE_ORGANIZATION:
									setOrganization(dataCursor, contactInfo); break;
								case MimetypeData.MIMETYPE_NICKNAME:
									setNickname(dataCursor, contactInfo);	break;
								case MimetypeData.MIMETYPE_NOTE:
									setNote(dataCursor, contactInfo);	break;
								case MimetypeData.MIMETYPE_SNS://没有获取
									break;
								case MimetypeData.MIMETYPE_SIP_ADDRESS:
									setSipAddressList(dataCursor, contactInfo, sipAddressList); break;
								case MimetypeData.MIMETYPE_IDENTITY:
									//setIdentity(dataCursor, contactInfo); 
									break;
								case MimetypeData.MIMETYPE_CONTACT_EVENT:
									setContactEventList(dataCursor, contactInfo, eventList); break;
								case MimetypeData.MIMETYPE_WEBSITE:
									setWebsiteList(dataCursor, contactInfo, websiteList); break;
								case MimetypeData.MIMETYPE_RELATION:
									setRelationList(dataCursor, contactInfo, relationList); break;
								default:
									break;
								}
								
							} while (dataCursor.moveToNext());
						}
					} catch (Exception e) {
						// TODO: handle exception
					} finally {
						if(dataCursor!=null)
							dataCursor.close();
					}
					
					contactInfoList.add(contactInfo);
					proIndex += oneContactPro;
					Log.d("proerror", "dbtocontacts proIndex="+proIndex);
					backupListener.onProgress((long) proIndex, 100);
				} while (idCursor.moveToNext());
				if(idCursor!=null)
					idCursor.close();
			}
		}
		
		return contactInfoList;
	}
	
	/**
	 * 封装联系人的EmailList
	 * @param dataArray
	 */
	private void setEmailList(Cursor cursor, ContactInfo contactInfo, ArrayList<ContactInfo.EmailInfo> emailList){
		ContactInfo.EmailInfo emailInfo = new ContactInfo.EmailInfo();
		emailInfo.email = cursor.getString(cursor.getColumnIndex(DATAS[MimetypeData.EMAIL_ADDRESS]));
		String type = cursor.getString(cursor.getColumnIndex(DATAS[MimetypeData.EMAIL_TYPE]));
		String label = cursor.getString(cursor.getColumnIndex(DATAS[MimetypeData.EMAIL_LABLE]));
		if(Integer.valueOf(type) == ContactHandler.OUR_CUS)
		{
			emailInfo.type = ContactHandler.SYS_CUS;
			emailInfo.label =label;
		}else
		{
			emailInfo.type = TypeNumConvert.Email_toInt(label);
			emailInfo.label = "";
		}
		emailList.add(emailInfo);
		contactInfo.setEmailList(emailList);
	}
	
	/**
	 * 封装联系人的ImList
	 * @param dataArray
	 */
	private void setImList(Cursor cursor, ContactInfo contactInfo, ArrayList<ContactInfo.IM> imList){
		ContactInfo.IM imInfo = new ContactInfo.IM();
		imInfo.data = cursor.getString(cursor.getColumnIndex(DATAS[MimetypeData.IM_DATA]));
		String type = cursor.getString(cursor.getColumnIndex(DATAS[MimetypeData.IM_TYPE]));
		
		String label = cursor.getString(cursor.getColumnIndex(DATAS[MimetypeData.IM_LABLE]));
		if(Integer.valueOf(type) == ContactHandler.OUR_CUS)
		{
			imInfo.type = ContactHandler.SYS_CUS;
			imInfo.label =label;
		}else
		{
			imInfo.type = TypeNumConvert.IM_toInt(label);
			imInfo.label = "";
		}
		
		imInfo.protocol = cursor.getString(cursor.getColumnIndex(DATAS[MimetypeData.IM_PROTOCAL]));
		imInfo.customProtocol = cursor.getString(cursor.getColumnIndex(DATAS[MimetypeData.IM_CUSTOM_PROTOCAL]));
		imList.add(imInfo);
		contactInfo.setIms(imList);
	}
	
	/**
	 * 封装联系人的PostalAddressList
	 * @param dataArray
	 */
	private void setPostalAddressList(Cursor cursor, ContactInfo contactInfo, ArrayList<ContactInfo.PostalAddress> posAddrList){
		ContactInfo.PostalAddress posAddrInfo = new ContactInfo.PostalAddress();
		String type = cursor.getString(cursor.getColumnIndex(DATAS[MimetypeData.POST_ADDR_TYPE]));
		
		String label = cursor.getString(cursor.getColumnIndex(DATAS[MimetypeData.POST_ADDR_LABLE]));
		if(Integer.valueOf(type) == ContactHandler.OUR_CUS)
		{
			posAddrInfo.type = ContactHandler.SYS_CUS;
			posAddrInfo.label =label;
		}else
		{
			posAddrInfo.type = TypeNumConvert.Address_toInt(label);
			posAddrInfo.label = "";
		}
		
		
		posAddrInfo.street = cursor.getString(cursor.getColumnIndex(DATAS[MimetypeData.POST_ADDR_STREET]));
		posAddrInfo.pobox = cursor.getString(cursor.getColumnIndex(DATAS[MimetypeData.POST_ADDR_POBOX]));
		posAddrInfo.neighborhood = cursor.getString(cursor.getColumnIndex(DATAS[MimetypeData.POST_ADDR_NEIGHBORHOOD]));
		posAddrInfo.city = cursor.getString(cursor.getColumnIndex(DATAS[MimetypeData.POST_ADDR_CITY]));
		posAddrInfo.region = cursor.getString(cursor.getColumnIndex(DATAS[MimetypeData.POST_ADDR_REGION]));
		posAddrInfo.postcode = cursor.getString(cursor.getColumnIndex(DATAS[MimetypeData.POST_ADDR_POSTCODE]));
		posAddrInfo.country = cursor.getString(cursor.getColumnIndex(DATAS[MimetypeData.POST_ADDR_COUNTRY]));
		posAddrList.add(posAddrInfo);
		contactInfo.setPostalAddresses(posAddrList);
	}
	
	/**
	 * 封装联系人的PhoneList
	 * @param dataArray
	 */
	private void setPhoneList(Cursor cursor, ContactInfo contactInfo, ArrayList<ContactInfo.PhoneInfo> phoneList){
		ContactInfo.PhoneInfo phoneInfo = new ContactInfo.PhoneInfo();
		phoneInfo.number = cursor.getString(cursor.getColumnIndex(DATAS[MimetypeData.PHONE_NUMBER]));
		String type = cursor.getString(cursor.getColumnIndex(DATAS[MimetypeData.PHONE_TYPE]));
		String label = cursor.getString(cursor.getColumnIndex(DATAS[MimetypeData.PHONE_LABLE]));
		if(Integer.valueOf(type) == ContactHandler.OUR_CUS)
		{
			phoneInfo.type = ContactHandler.SYS_CUS;
			phoneInfo.label =label;
		}else
		{
			phoneInfo.type = TypeNumConvert.TEL_toInt(label);
			phoneInfo.label = "";
		}
		
		
		phoneList.add(phoneInfo);
		contactInfo.setPhoneList(phoneList);
	}
	
	/**
	 * 封装联系人的Name
	 * @param dataArray
	 */
	private void setName(Cursor cursor, ContactInfo contactInfo){
		ContactInfo.StructName structName = new ContactInfo.StructName();
		//structName.givenName = dataArray[MimetypeData.NAME_DISPLAY_NAME];
		structName.givenName = cursor.getString(cursor.getColumnIndex(DATAS[MimetypeData.NAME_GIVEN_NAME]));
		structName.familyName = cursor.getString(cursor.getColumnIndex(DATAS[MimetypeData.NAME_FAMILY_NAME]));
		structName.prefix = cursor.getString(cursor.getColumnIndex(DATAS[MimetypeData.NAME_PREFIX]));
		structName.middleName = cursor.getString(cursor.getColumnIndex(DATAS[MimetypeData.NAME_MIDDLE_NAME]));
		structName.suffix = cursor.getString(cursor.getColumnIndex(DATAS[MimetypeData.NAME_SUFFIX]));

		ContactInfo.PhoneticThreeName phoneticName = new ContactInfo.PhoneticThreeName();
		phoneticName.Phonetic_last_name = cursor.getString(cursor.getColumnIndex(DATAS[MimetypeData.NAME_PHONETIC_GIVEN_NAME]));
		phoneticName.Phonetic_middle_name = cursor.getString(cursor.getColumnIndex(DATAS[MimetypeData.NAME_PHONETIC_MIDDLE_NAME]));
		phoneticName.Phonetic_first_name = cursor.getString(cursor.getColumnIndex(DATAS[MimetypeData.NAME_PHONETIC_FAMILY_NAME]));
		contactInfo.setStructName(structName);
		contactInfo.setPhoneticThreeName(phoneticName);
	}
	
	/**
	 * 封装联系人的Photo
	 * @param dataArray
	 */
//	private void setPhoto(String[] dataArray){
//		photo = dataArray[MimetypeData.PHOTO_DIY];
//	}
	
	/**
	 * 封装联系人的GroupMembershipList
	 * @param dataArray
	 */
//	private void setGroupMembershipList(Cursor cursor, ContactInfo contactInfo){
//		//groupList.add(dataArray[MimetypeData.GROUP_DIY]);
//		cursor.getString(cursor.getColumnIndex(DATAS[MimetypeData.GROUP_DIY]));
//	}
	
	/**
	 * 封装联系人的Organization
	 * @param dataArray
	 */
	private void setOrganization(Cursor cursor, ContactInfo contactInfo){
		ContactInfo.Organization organization = new ContactInfo.Organization();
		organization.company = cursor.getString(cursor.getColumnIndex(DATAS[MimetypeData.ORGANIZATION_COMPANY]));
		String type = cursor.getString(cursor.getColumnIndex(DATAS[MimetypeData.ORGANIZATION_TYPE]));
		
		String label = cursor.getString(cursor.getColumnIndex(DATAS[MimetypeData.ORGANIZATION_LABLE]));
		if(Integer.valueOf(type) == ContactHandler.OUR_CUS)
		{
			organization.type = ContactHandler.SYS_CUS;
			organization.label =label;
		}else
		{
			organization.type = TypeNumConvert.Organization_toInt(label);
			organization.label = "";
		}
		
		organization.title = cursor.getString(cursor.getColumnIndex(DATAS[MimetypeData.ORGANIZATION_TITLE]));
		organization.department = cursor.getString(cursor.getColumnIndex(DATAS[MimetypeData.ORGANIZATION_DEPARTMENT]));
		organization.symbol = cursor.getString(cursor.getColumnIndex(DATAS[MimetypeData.ORGANIZATION_SYMBOL]));
		organization.phonetic_name = cursor.getString(cursor.getColumnIndex(DATAS[MimetypeData.ORGANIZATION_PHONETIC_NAME]));
		contactInfo.setOrganization(organization);
	}
	
	/**
	 * 封装联系人的Nickname
	 * @param dataArray
	 */
	private void setNickname(Cursor cursor, ContactInfo contactInfo){
		ContactInfo.NickName nickname = new ContactInfo.NickName();
		nickname.name = cursor.getString(cursor.getColumnIndex(DATAS[MimetypeData.NICKNAME_NAME]));
		String type = cursor.getString(cursor.getColumnIndex(DATAS[MimetypeData.NICKNAME_TYPE]));
		String label = cursor.getString(cursor.getColumnIndex(DATAS[MimetypeData.NICKNAME_LABLE]));
		if(Integer.valueOf(type) == ContactHandler.OUR_CUS)
		{
			nickname.type = ContactHandler.SYS_CUS;
			nickname.label =label;
		}else
		{
			nickname.type = TypeNumConvert.NickName_toInt(label);
			nickname.label = "";
		}
		
		contactInfo.setNickName(nickname);
	}
	
	/**
	 * 封装联系人的Note
	 * @param dataArray
	 */
	private void setNote(Cursor cursor, ContactInfo contactInfo){
		note = cursor.getString(cursor.getColumnIndex(DATAS[MimetypeData.NOTO]));
		contactInfo.setNote(note);
	}
	
	/**
	 * 封装联系人的SipAddressList
	 * @param dataArray
	 */
	private void setSipAddressList(Cursor cursor, ContactInfo 
			contactInfo,ArrayList<ContactInfo.SipAddressInfo> sipAddressList){
		ContactInfo.SipAddressInfo sipAddressInfo = new ContactInfo.SipAddressInfo();
		sipAddressInfo.sip_address = cursor.getString(cursor.getColumnIndex(DATAS[MimetypeData.SIP_ADDR_NAME]));
		String type = cursor.getString(cursor.getColumnIndex(DATAS[MimetypeData.SIP_ADDR_TYPE]));
		
		
		String label = cursor.getString(cursor.getColumnIndex(DATAS[MimetypeData.SIP_ADDR_LABLE]));
		if(Integer.valueOf(type) == ContactHandler.OUR_CUS)
		{
			sipAddressInfo.type = ContactHandler.SYS_CUS;
			sipAddressInfo.label =label;
		}else
		{
			sipAddressInfo.type = TypeNumConvert.Address_toInt(label);
			sipAddressInfo.label = "";
		}
		
		sipAddressList.add(sipAddressInfo);
		contactInfo.setSipAddressList(sipAddressList);
	}
	
	/**
	 * 封装联系人的Identity
	 * @param dataArray
	 */
	private void setIdentity(String[] dataArray){
		//没有该信息
	}
	
	/**
	 * 封装联系人的ContactEventList
	 * @param dataArray
	 */
	private void setContactEventList(Cursor cursor, ContactInfo contactInfo, ArrayList<ContactInfo.EventInfo> eventList){
		ContactInfo.EventInfo eventInfo = new ContactInfo.EventInfo();
		eventInfo.start_date = cursor.getString(cursor.getColumnIndex(DATAS[MimetypeData.EVENT_START_DATE]));
		String type = cursor.getString(cursor.getColumnIndex(DATAS[MimetypeData.EVENT_TYPE]));
		
		String label = cursor.getString(cursor.getColumnIndex(DATAS[MimetypeData.EVENT_LABLE]));
		if(Integer.valueOf(type) == ContactHandler.OUR_CUS)
		{
			eventInfo.type = ContactHandler.SYS_CUS;
			eventInfo.label =label;
		}else
		{
			eventInfo.type = TypeNumConvert.Event_toInt(label);
			eventInfo.label = "";
		}
		
		eventList.add(eventInfo);
		contactInfo.setEventList(eventList);
	}
	
	/**
	 * 封装联系人的WebsiteList
	 * @param dataArray
	 */
	private void setWebsiteList(Cursor cursor, ContactInfo contactInfo, ArrayList<ContactInfo.WebSite> websiteList){
		ContactInfo.WebSite websiteInfo = new ContactInfo.WebSite();
		websiteInfo.data = cursor.getString(cursor.getColumnIndex(DATAS[MimetypeData.WEBSITE_URL]));
		String type = cursor.getString(cursor.getColumnIndex(DATAS[MimetypeData.WEBSITE_TYPE]));
		
		String label = cursor.getString(cursor.getColumnIndex(DATAS[MimetypeData.WEBSITE_LABLE]));
		if(Integer.valueOf(type) == ContactHandler.OUR_CUS)
		{
			websiteInfo.type = ContactHandler.SYS_CUS;
			websiteInfo.label =label;
		}else
		{
			websiteInfo.type = TypeNumConvert.URL_toInt(label);
			websiteInfo.label = "";
		}
		
		websiteList.add(websiteInfo);
		contactInfo.setWebsiteList(websiteList);
	}
	
	/**
	 * 封装联系人的RelationList
	 * @param dataArray
	 */
	private void setRelationList(Cursor cursor, ContactInfo contactInfo, ArrayList<ContactInfo.RelationInfo> relationList){
		ContactInfo.RelationInfo relationInfo = new ContactInfo.RelationInfo();
		relationInfo.data = cursor.getString(cursor.getColumnIndex(DATAS[MimetypeData.RELATION_NAME]));
		String type = cursor.getString(cursor.getColumnIndex(DATAS[MimetypeData.RELATION_TYPE]));
		
		String label = cursor.getString(cursor.getColumnIndex(DATAS[MimetypeData.RELATION_LABLE]));
		if(Integer.valueOf(type) == ContactHandler.OUR_CUS)
		{
			relationInfo.type = ContactHandler.SYS_CUS;
			relationInfo.label =label;
		}else
		{
			relationInfo.type = TypeNumConvert.Relation_toInt(label);
			relationInfo.label = "";
		}
		
		relationList.add(relationInfo);
		contactInfo.setRelationList(relationList);
	}

	
//	private void setContactInfo(ContactInfo contactInfo){
//		
//		//contactInfo.setPhoto(photo);
//		//contactInfo.setGroupList(groupList);
//		contactInfo.setNote(note);
//	}
	
//	private void resetDataList(){
//		imList.clear();
//		posAddrList.clear();
//		phoneList.clear();
//		groupList.clear();
//		sipAddressList.clear();
//		eventList.clear();
//		websiteList.clear();
//		relationList.clear();
//	}
	
	
	/**
	 * 根据contact_id表中的raw_id查询该联系人的所有数据
	 * @param rawId
	 * @return Cursor
	 */
	private Cursor getContactData(int rawId) {
		Cursor cursor = database.rawQuery("SELECT * FROM "+ContactDBHelper.DATA_TABLE+
				" where "+ContactDBHelper.DATA.CONTACT_ID+" = ?", new String[]{""+rawId});
		return cursor;
	}
	
}
