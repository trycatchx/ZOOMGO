package com.dmsys.airdiskpro.utils;

import com.dmsys.airdiskpro.db.ContactDBManager;
import com.dmsys.airdiskpro.model.ContactInfo;
import com.dmsys.airdiskpro.model.ContactInfo.PostalAddress;
import com.dmsys.airdiskpro.model.DBDataBean;
import com.dmsys.airdiskpro.model.MimetypeData;

import java.util.ArrayList;
import java.util.List;


/**
 * 
 * @ClassName: InfoToDBHandler
 * @Description: 处理ContactInfo数据，用于保存到数据库
 * @author: Alan.wang
 * @date: 2014-09-29 11:28 
 * version:1.0.0
 */
public class InfoToDBHandler {
	
	//private ArrayList<String> dataList = new ArrayList<String>(16);//dataList(0)空出来
	private ArrayList<DBDataBean> dataBeanList = new ArrayList<DBDataBean>();
	
	private static InfoToDBHandler _instance = null;

	/** 获取实例 */
	public static InfoToDBHandler getInstance() {
		if (_instance == null) {
			_instance = new InfoToDBHandler();
		} else {
		}
		return _instance;
	}
	
	public void saveInfoToDb(ContactDBManager dbManager, int contactId, ContactInfo info){
		int photoId = dbManager.insertPhotoInfo(contactId, info.getPhoto());
		dbManager.updateContactPhotoId(contactId, photoId);
		
		if (!isListNull(getEmailList(contactId, info))) {
			dbManager.insertDataList(dataBeanList);
		}
		
		if (!isListNull(getImList(contactId, info))) {
			dbManager.insertDataList(dataBeanList);
		}
		if (!isListNull(getPostalAddressList(contactId, info))) {
			dbManager.insertDataList(dataBeanList);
		}
		if (!isListNull(getPhoneList(contactId, info))) {
			dbManager.insertDataList(dataBeanList);
		}
		
		DBDataBean name = getName(contactId, info);
		if (name != null) {
			dbManager.insertData(name);
		}
		//photo已插入photo表
//		DBDataBean photo = getPhoto(contactId, info);
//		if (photo != null) {
//			dbManager.insertData(photo);;
//		}
		
		if (!isListNull(getGroupMembershipList(contactId, info))) {
			dbManager.insertDataList(dataBeanList);
		}
		
		DBDataBean organization = getOrganization(contactId, info);
		if (organization != null) {
			dbManager.insertData(organization);;
		}
		DBDataBean nickname = getNickname(contactId, info);
		if (nickname != null) {
			dbManager.insertData(nickname);
		}
		DBDataBean note = getNote(contactId, info);
		if (note != null) {
			dbManager.insertData(note);
		}
		
		if (!isListNull(getSipAddressList(contactId, info))) {
			dbManager.insertDataList(dataBeanList);
		}
		//identity在ContactInfo中没有获取
//		DBDataBean identity = getIdentity(contactId, info);
//		if (identity != null) {
//			dbManager.insertData(identity);
//		}
		if (!isListNull(getContactEventList(contactId, info))) {
			dbManager.insertDataList(dataBeanList);
		}
		if (!isListNull(getWebsiteList(contactId, info))) {
			dbManager.insertDataList(dataBeanList);
		}
		if (!isListNull(getRelationList(contactId, info))) {
			dbManager.insertDataList(dataBeanList);
		}
		
	}
	
	/**
	 * 获取某一联系人的Email列表
	 * @param info
	 * @return
	 */
	public ArrayList<DBDataBean> getEmailList(int contactId, ContactInfo info) {
		dataBeanList.clear();
		List<ContactInfo.EmailInfo> emailList = info.getEmailList();
		if (emailList != null) {
			for (ContactInfo.EmailInfo email : emailList) {
				ArrayList<String> dataList = new ArrayList<String>();
				resetDataList(dataList);
				dataList.set(MimetypeData.EMAIL_ADDRESS, checkString(email.email));
				dataList.set(MimetypeData.EMAIL_TYPE, ""+checkType(email.type));
				if(email.type == ContactHandler.SYS_CUS)
				{
					//自定义
					dataList.set(MimetypeData.EMAIL_LABLE, email.label);
				}else
				{
					//系统type
					dataList.set(MimetypeData.EMAIL_LABLE, TypeNumConvert.Email_toString(email.type));
				}
				DBDataBean dataBean = new DBDataBean(contactId, MimetypeData.MIMETYPE_EMAIL, dataList);
				dataBeanList.add(dataBean);
			}
		}
		return dataBeanList;
	}
	
	/**
	 * 获取某一联系人的Im列表
	 * @param info
	 * @return
	 */
	public ArrayList<DBDataBean> getImList(int contactId, ContactInfo info) {
		dataBeanList.clear();
		ArrayList<ContactInfo.IM> imList = info.getIms();
		if (imList != null) {
			for (ContactInfo.IM im : imList) {
				ArrayList<String> dataList = new ArrayList<String>();
				resetDataList(dataList);
				dataList.set(MimetypeData.IM_DATA, checkString(im.data));
				dataList.set(MimetypeData.IM_TYPE, ""+checkType(im.type));
				if(im.type == ContactHandler.SYS_CUS)
				{
					//自定义
					dataList.set(MimetypeData.IM_LABLE, im.label);
				}else
				{
					//系统type
					dataList.set(MimetypeData.IM_LABLE, TypeNumConvert.IM_toString(im.type));
				}
				dataList.set(MimetypeData.IM_PROTOCAL, checkString(im.protocol));
				dataList.set(MimetypeData.IM_CUSTOM_PROTOCAL, checkString(im.customProtocol));
				DBDataBean dataBean = new DBDataBean(contactId, MimetypeData.MIMETYPE_IM, dataList);
				dataBeanList.add(dataBean);
			}
		}
		return dataBeanList;
	}
	
	/**
	 * 获取某一联系人的PostalAddress列表
	 * @param info
	 * @return
	 */
	public ArrayList<DBDataBean> getPostalAddressList(int contactId, ContactInfo info) {
		dataBeanList.clear();
		ArrayList<PostalAddress> addresses = info.getPostalAddresses();
		if (addresses != null) {
			for (PostalAddress addr : addresses) {
				ArrayList<String> dataList = new ArrayList<String>();
				resetDataList(dataList);
				//dataList.set(MimetypeData.POST_ADDR_FORMATTED_ADDRESS, checkString(addr.city));
				dataList.set(MimetypeData.POST_ADDR_TYPE, ""+checkType(addr.type));
				if(addr.type == ContactHandler.SYS_CUS)
				{
					//自定义
					dataList.set(MimetypeData.POST_ADDR_LABLE, addr.label);
				}else
				{
					//系统type
					dataList.set(MimetypeData.POST_ADDR_LABLE, TypeNumConvert.Address_toString(addr.type));
				}
				dataList.set(MimetypeData.POST_ADDR_STREET, checkString(addr.street));
				dataList.set(MimetypeData.POST_ADDR_POBOX, checkString(addr.pobox));
				dataList.set(MimetypeData.POST_ADDR_NEIGHBORHOOD, checkString(addr.neighborhood));
				dataList.set(MimetypeData.POST_ADDR_CITY, checkString(addr.city));
				dataList.set(MimetypeData.POST_ADDR_REGION, checkString(addr.region));
				dataList.set(MimetypeData.POST_ADDR_POSTCODE, checkString(addr.postcode));
				dataList.set(MimetypeData.POST_ADDR_COUNTRY, checkString(addr.country));
				DBDataBean dataBean = new DBDataBean(contactId, MimetypeData.MIMETYPE_POSTAL_ADDRESS, dataList);
				dataBeanList.add(dataBean);
			}
		}
		return dataBeanList;
	}
	
	/**
	 * 获取某一联系人的Phone列表
	 * @param info
	 * @return
	 */
	public ArrayList<DBDataBean> getPhoneList(int contactId, ContactInfo info) {
		dataBeanList.clear();
		List<ContactInfo.PhoneInfo> phoneList = info.getPhoneList();
		if (phoneList != null) {
			for (ContactInfo.PhoneInfo phone : phoneList) {
				ArrayList<String> dataList = new ArrayList<String>();
				resetDataList(dataList);
				dataList.set(MimetypeData.PHONE_NUMBER, checkString(phone.number));
				dataList.set(MimetypeData.PHONE_TYPE, ""+checkType(phone.type));
				if(phone.type == ContactHandler.SYS_CUS)
				{
					//自定义
					dataList.set(MimetypeData.PHONE_LABLE, phone.label);
				}else
				{
					//系统type
					dataList.set(MimetypeData.PHONE_LABLE, TypeNumConvert.TEL_toString(phone.type));
				}
				
				//RA:
				DBDataBean dataBean = new DBDataBean(contactId, MimetypeData.MIMETYPE_PHONE, dataList);
				dataBeanList.add(dataBean);
			}
		}
		return dataBeanList;
	}
	
	/**
	 * 获取某一联系人的structname
	 * @param info
	 * @return
	 */
	public DBDataBean getName(int contactId, ContactInfo info) {
		ContactInfo.StructName structName = info.getStructName();
		ContactInfo.PhoneticThreeName pName = info.getPhoneticThreeName();
		ArrayList<String> dataList = new ArrayList<String>();
		resetDataList(dataList);
		if (structName != null) {
			dataList.set(MimetypeData.NAME_DISPLAY_NAME, checkString(structName.givenName));//info中没有
			dataList.set(MimetypeData.NAME_GIVEN_NAME, checkString(structName.givenName));
			dataList.set(MimetypeData.NAME_FAMILY_NAME, checkString(structName.familyName));
			dataList.set(MimetypeData.NAME_PREFIX, checkString(structName.prefix));
			dataList.set(MimetypeData.NAME_MIDDLE_NAME, checkString(structName.middleName));
			dataList.set(MimetypeData.NAME_SUFFIX, checkString(structName.suffix));
		}
		if (pName != null) {
			dataList.set(MimetypeData.NAME_PHONETIC_GIVEN_NAME, checkString(pName.Phonetic_last_name));
			dataList.set(MimetypeData.NAME_PHONETIC_MIDDLE_NAME, checkString(pName.Phonetic_middle_name));
			dataList.set(MimetypeData.NAME_PHONETIC_FAMILY_NAME, checkString(pName.Phonetic_first_name));
		}
		if (structName == null && pName == null) {
			return null;
		}
		DBDataBean dataBean = new DBDataBean(contactId, MimetypeData.MIMETYPE_NAME, dataList);
		return dataBean;
	 }
	
	/**
	 * 获取某一联系人的Photo,此处可以不获取，photo是存放在photo表中
	 * @param info
	 * @return
	 */
	public DBDataBean getPhoto(int contactId, ContactInfo info) {
		ArrayList<String> dataList = new ArrayList<String>();
		resetDataList(dataList);
		//dataList.set(MimetypeData.PHOTO_FILE_ID, checkString(info.getPhoto()));//info中没有
		//dataList.set(MimetypeData.PHOTO, checkString(info.getPhoto()));//info中没有
		dataList.set(MimetypeData.PHOTO_DIY, checkString(info.getPhoto()));//临时自定义的
		DBDataBean dataBean = new DBDataBean(contactId, MimetypeData.MIMETYPE_PHOTO, dataList);
		return dataBean;
	}
	
	/**
	 * 获取某一联系人的GroupMembership，此处有问题，应该是该联系人所属群组而非所有群组
	 * @param info
	 * @return
	 */
	public ArrayList<DBDataBean> getGroupMembershipList(int contactId, ContactInfo info) {
		dataBeanList.clear();
		ArrayList<String> groupList = info.getGroupList();
		if (groupList != null) {
			for (String group : groupList) {
				ArrayList<String> dataList = new ArrayList<String>();
				resetDataList(dataList);
				//dataList.set(MimetypeData.GROUP_ROW_ID, checkString(group));//info中没有
				//dataList.set(MimetypeData.GROUP_SOURCE_ID, checkString(group));//info中没有
				dataList.set(MimetypeData.GROUP_DIY, checkString(group));//临时自定义的
				DBDataBean dataBean = new DBDataBean(contactId, MimetypeData.MIMETYPE_GROUP, dataList);
				dataBeanList.add(dataBean);
			}
		}
		return dataBeanList;
	}
	
	/**
	 * 获取某一联系人的Organization
	 * @param info
	 * @return
	 */
	public DBDataBean getOrganization(int contactId, ContactInfo info) {
		ArrayList<String> dataList = new ArrayList<String>();
		resetDataList(dataList);
		ContactInfo.Organization organization = info.getOrganization();
		if (organization != null) {
			dataList.set(MimetypeData.ORGANIZATION_COMPANY, checkString(organization.company));
			dataList.set(MimetypeData.ORGANIZATION_TYPE, ""+checkType(organization.type));
			if(organization.type == ContactHandler.SYS_CUS)
			{
				//自定义
				dataList.set(MimetypeData.ORGANIZATION_LABLE, organization.label);
			}else
			{
				//系统type
				dataList.set(MimetypeData.ORGANIZATION_LABLE, TypeNumConvert.Organization_toString(organization.type));
			}
			dataList.set(MimetypeData.ORGANIZATION_TITLE, checkString(organization.title));
			dataList.set(MimetypeData.ORGANIZATION_DEPARTMENT, checkString(organization.department));
			//dataList.set(MimetypeData.ORGANIZATION_JOB_DESCRIPTION, checkString(organization.company));
			dataList.set(MimetypeData.ORGANIZATION_SYMBOL, checkString(organization.symbol));
			dataList.set(MimetypeData.ORGANIZATION_PHONETIC_NAME, checkString(organization.phonetic_name));
			//dataList.set(MimetypeData.ORGANIZATION_OFFICE_LOCATION, checkString(organization.company));
			//dataList.set(MimetypeData.ORGANIZATION_PHONETIC_NAME_STYLE, checkString(organization.company));
			DBDataBean dataBean = new DBDataBean(contactId, MimetypeData.MIMETYPE_ORGANIZATION, dataList);
			return dataBean;
		}else {
			return null;
		}
		
	}
	
	/**
	 * 获取某一联系人的Nickname
	 * @param info
	 * @return
	 */
	public DBDataBean getNickname(int contactId, ContactInfo info) {
		ArrayList<String> dataList = new ArrayList<String>();
		resetDataList(dataList);
		ContactInfo.NickName nickname = info.getNickName();
		if (nickname != null) {
			dataList.set(MimetypeData.NICKNAME_NAME, checkString(nickname.name));
			dataList.set(MimetypeData.NICKNAME_TYPE, ""+checkType(nickname.type));
			if(nickname.type == ContactHandler.SYS_CUS)
			{
				//自定义
				dataList.set(MimetypeData.NICKNAME_LABLE, nickname.label);
			}else
			{
				//系统type
				dataList.set(MimetypeData.NICKNAME_LABLE, TypeNumConvert.NickName_toString(nickname.type));
			}
			DBDataBean dataBean = new DBDataBean(contactId, MimetypeData.MIMETYPE_NICKNAME, dataList);
			return dataBean;
		}else {
			return null;
		}
		
	}
	
	/**
	 * 获取某一联系人的Note信息
	 * @param info
	 * @return
	 */
	public DBDataBean getNote(int contactId, ContactInfo info) {
		ArrayList<String> dataList = new ArrayList<String>();
		resetDataList(dataList);
		if (!isStringNull(info.getNote())) {
			dataList.set(MimetypeData.NOTO, checkString(info.getNote()));
			DBDataBean dataBean = new DBDataBean(contactId, MimetypeData.MIMETYPE_NOTE, dataList);
			return dataBean;
		}else {
			return null;
		}
		
	}
	
	/**
	 * 获取某一联系人的SipAddress列表
	 * @param info
	 * @return
	 */
	public ArrayList<DBDataBean> getSipAddressList(int contactId, ContactInfo info) {
		dataBeanList.clear();
		List<ContactInfo.SipAddressInfo> sipAddressList = info.getSipAddressList();
		if (sipAddressList != null) {
			for (ContactInfo.SipAddressInfo sipAddress : sipAddressList) {
				ArrayList<String> dataList = new ArrayList<String>();
				resetDataList(dataList);
				dataList.set(MimetypeData.SIP_ADDR_NAME, checkString(sipAddress.sip_address));
				dataList.set(MimetypeData.SIP_ADDR_TYPE, ""+checkType(sipAddress.type));
				if(sipAddress.type == ContactHandler.SYS_CUS)
				{
					//自定义
					dataList.set(MimetypeData.SIP_ADDR_LABLE, sipAddress.label);
				}else
				{
					//系统type
					dataList.set(MimetypeData.SIP_ADDR_LABLE, TypeNumConvert.SipAddress_toString(sipAddress.type));
				}
				DBDataBean dataBean = new DBDataBean(contactId, MimetypeData.MIMETYPE_SIP_ADDRESS, dataList);
				dataBeanList.add(dataBean);
			}
		}
		return dataBeanList;
	}
	
	/**
	 * 获取某一联系人的Identity信息
	 * @param info
	 * @return
	 */
	public DBDataBean getIdentity(int contactId, ContactInfo info) {//info 中没有获取该信息
		ArrayList<String> dataList = new ArrayList<String>();
		resetDataList(dataList);
		DBDataBean dataBean = new DBDataBean(contactId, MimetypeData.MIMETYPE_IDENTITY, dataList);
		return dataBean;
	}
	
	/**
	 * 获取某一联系人的ContactEvent列表
	 * @param info
	 * @return
	 */
	public ArrayList<DBDataBean> getContactEventList(int contactId, ContactInfo info) {
		dataBeanList.clear();
		List<ContactInfo.EventInfo> eventList = info.getEventList();
		if (eventList != null) {
			for (ContactInfo.EventInfo event : eventList) {
				ArrayList<String> dataList = new ArrayList<String>();
				resetDataList(dataList);
				dataList.set(MimetypeData.EVENT_START_DATE, checkString(event.start_date));
				dataList.set(MimetypeData.EVENT_TYPE, ""+checkType(event.type));
				if(event.type == ContactHandler.SYS_CUS)
				{
					//自定义
					dataList.set(MimetypeData.EVENT_LABLE, event.label);
				}else
				{
					//系统type
					dataList.set(MimetypeData.EVENT_LABLE, TypeNumConvert.Event_toString(event.type));
				}
				DBDataBean dataBean = new DBDataBean(contactId, MimetypeData.MIMETYPE_CONTACT_EVENT, dataList);
				dataBeanList.add(dataBean);
			}
		}
		return dataBeanList;
	}
	
	/**
	 * 获取某一联系人的Website列表
	 * @param info
	 * @return
	 */
	public ArrayList<DBDataBean> getWebsiteList(int contactId, ContactInfo info) {
		dataBeanList.clear();
		List<ContactInfo.WebSite> webList = info.getWebsiteList();
		if (webList != null) {
			for (ContactInfo.WebSite website : webList) {
				ArrayList<String> dataList = new ArrayList<String>();
				resetDataList(dataList);
				dataList.set(MimetypeData.WEBSITE_URL, checkString(website.data));
				dataList.set(MimetypeData.WEBSITE_TYPE, ""+checkType(website.type));
				if(website.type == ContactHandler.SYS_CUS)
				{
					//自定义
					dataList.set(MimetypeData.WEBSITE_LABLE, website.label);
				}else
				{
					//系统type
					dataList.set(MimetypeData.WEBSITE_LABLE, TypeNumConvert.URL_toString(website.type));
				}
				DBDataBean dataBean = new DBDataBean(contactId, MimetypeData.MIMETYPE_WEBSITE, dataList);
				dataBeanList.add(dataBean);
			}
		}
		return dataBeanList;
	}
	
	/**
	 * 获取某一联系人的Relation列表
	 * @param info
	 * @return
	 */
	public ArrayList<DBDataBean> getRelationList(int contactId, ContactInfo info) {
		dataBeanList.clear();
		List<ContactInfo.RelationInfo> relationList = info.getRelationList();
		if (relationList != null) {
			for (ContactInfo.RelationInfo relation : relationList) {
				ArrayList<String> dataList = new ArrayList<String>();
				resetDataList(dataList);
				dataList.set(MimetypeData.RELATION_NAME, checkString(relation.data));
				dataList.set(MimetypeData.RELATION_TYPE, ""+checkType(relation.type));
				if(relation.type == ContactHandler.SYS_CUS)
				{
					//自定义
					dataList.set(MimetypeData.RELATION_LABLE, relation.label);
				}else
				{
					//系统type
					dataList.set(MimetypeData.RELATION_LABLE, TypeNumConvert.Relation_toString(relation.type));
				}
				DBDataBean dataBean = new DBDataBean(contactId, MimetypeData.MIMETYPE_RELATION, dataList);
				dataBeanList.add(dataBean);
			}
		}
		return dataBeanList;
	}
	
	/**
	 * 复位dataVector，准备装入新的数据
	 */
	private void resetDataList(ArrayList<String> dataList){
		dataList.clear();
		for (int i = 0; i < 16; i++) {
			dataList.add("");
		}
	}
	
	private boolean isListNull(ArrayList<DBDataBean> dataBeanList) {
		if (dataBeanList != null && dataBeanList.size()>0) {
			return false;
		}
		return true;
	}
	
	/**
	 * 将data1~data15包装成"data1&data2&data3&data4...&data15"
	 * @return
	 */
//	private String getDataString(){
//		String dataString = " ";
//		for (String data : dataList) {
//			dataString = dataString + "&" + data;
//		}
//		return dataString.substring(2, dataString.length());
//	}
	
	private static boolean isStringNull(String str) {
		if (str == null || str.length() == 0) {
			return true;
		}
		return false;
	}
	
	
//	private static boolean isStringNull(String str) {
//		if (str.equals(" & & & & & & & & & & & & & & ") || str.length()<=28) {
//			return true;
//		}
//		return false;
//	}
	
	
	/**
	 * 存入自定义数据库时根据开发文档自定义标签为1，系统的为0；在系统数据中自定义为0，系统的根据lable为1,2,3...
	 * @param type
	 * @return
	 */
	private static int checkType(int type) {
		if (type == ContactHandler.SYS_CUS) {
			type = ContactHandler.OUR_CUS;
		}else {
			type = ContactHandler.OUR_SYSTYPE;
		}
		return type;
	}
	
	private static String checkString(String str) {
		if (str == null || str.length() == 0) {
			return "";
		}
		return str;
	}
}
