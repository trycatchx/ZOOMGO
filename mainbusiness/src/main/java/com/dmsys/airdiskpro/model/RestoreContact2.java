package com.dmsys.airdiskpro.model;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.GroupMembership;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

public class RestoreContact2 {
	private Context mContext;
	private static final String KEY_ID = "KEY_ID";
	private static final String KEY_TITLE = "KEY_TITLE";
	private static final String KEY_ACC_NAME = "KEY_ACC_NAME";
	private static final String KEY_ACC_TYPE = "KEY_ACC_TYPE";
	private static final String TAG = "RestoreContact2";

	public RestoreContact2(Context context) {
		this.mContext = context;
	}
	public void restoreStructNameAndPhonetic(ContactInfo info, ArrayList<ContentProviderOperation> ops, int rawContactInsertIndex) {
		ContactInfo.StructName structName = info.getStructName();
		ContactInfo.PhoneticThreeName phoneticName = info.getPhoneticThreeName();
		if (structName == null)
			return;
		ContentProviderOperation.Builder builder = null;
		builder = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI);
		builder.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex);
		builder.withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);
		if (!isNull(structName.familyName))
			builder.withValue(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME, structName.familyName);
		if (!isNull(structName.givenName))
		{
			Log.d("ra_resName", structName.givenName);
			builder.withValue(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, structName.givenName);
		}
		if (!isNull(structName.middleName))
			builder.withValue(ContactsContract.CommonDataKinds.StructuredName.MIDDLE_NAME, structName.middleName);
		if (!isNull(structName.prefix))
			builder.withValue(ContactsContract.CommonDataKinds.StructuredName.PREFIX, structName.prefix);
		if (!isNull(structName.suffix))
			builder.withValue(ContactsContract.CommonDataKinds.StructuredName.SUFFIX, structName.suffix);
		if (phoneticName != null) {
			if (!isNull(phoneticName.Phonetic_first_name))
				builder.withValue(ContactsContract.CommonDataKinds.StructuredName.PHONETIC_GIVEN_NAME, phoneticName.Phonetic_first_name);
			if (!isNull(phoneticName.Phonetic_middle_name))
				builder.withValue(ContactsContract.CommonDataKinds.StructuredName.PHONETIC_MIDDLE_NAME, phoneticName.Phonetic_middle_name);
			if (!isNull(phoneticName.Phonetic_last_name))
				builder.withValue(ContactsContract.CommonDataKinds.StructuredName.PHONETIC_FAMILY_NAME, phoneticName.Phonetic_last_name);
		}
		builder.withYieldAllowed(true);
		ops.add(builder.build());
	}

	public void restoreNickName(ContactInfo info, ArrayList<ContentProviderOperation> ops, int rawContactInsertIndex) {
		ContactInfo.NickName nickName = info.getNickName();
		if (nickName == null)
			return;
		ContentProviderOperation.Builder builder = null;
		builder = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI);
		builder.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex);
		builder.withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Nickname.CONTENT_ITEM_TYPE);
		builder.withValue(ContactsContract.CommonDataKinds.Nickname.TYPE, nickName.type);
		if (!isNull(nickName.label))
			builder.withValue(ContactsContract.CommonDataKinds.Nickname.LABEL, nickName.label);
		if (!isNull(nickName.name))
			builder.withValue(ContactsContract.CommonDataKinds.Nickname.NAME, nickName.name);
		builder.withYieldAllowed(true);
		ops.add(builder.build());
	}

	public void restoreNote(ContactInfo info, ArrayList<ContentProviderOperation> ops, int rawContactInsertIndex) {
		String note = info.getNote();
		if (note == null)
			return;
		ContentProviderOperation.Builder builder = null;
		builder = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI);
		builder.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex);
		builder.withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE);
		builder.withValue(ContactsContract.CommonDataKinds.Note.NOTE, note);
		builder.withYieldAllowed(true);
		ops.add(builder.build());
	}

	public void restorePhoto(ContactInfo info, ArrayList<ContentProviderOperation> ops, int rawContactInsertIndex) {
		String photo = info.getPhoto();
		if (photo == null)
			return;
		ContentProviderOperation.Builder builder = null;
		builder = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI);
		builder.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex);
		builder.withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE);
		byte[] byteArray;
		byteArray = Base64.decode(photo, Base64.DEFAULT);
		Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
		builder.withValue(ContactsContract.CommonDataKinds.Photo.PHOTO, baos.toByteArray());
		builder.withYieldAllowed(true);
		ops.add(builder.build());
	}

	public void restoreOrganization(ContactInfo info, ArrayList<ContentProviderOperation> ops, int rawContactInsertIndex) {
		ContactInfo.Organization organization = info.getOrganization();
		if (organization == null)
			return;
		ContentProviderOperation.Builder builder = null;
		builder = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI);
		builder.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex);
		builder.withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE);
		builder.withValue(ContactsContract.CommonDataKinds.Organization.TYPE, organization.type);
		if (!isNull(organization.label))
			builder.withValue(ContactsContract.CommonDataKinds.Organization.LABEL, organization.label);
		if (!isNull(organization.company))
			builder.withValue(ContactsContract.CommonDataKinds.Organization.COMPANY, organization.company);
		if (!isNull(organization.department))
			builder.withValue(ContactsContract.CommonDataKinds.Organization.DEPARTMENT, organization.department);
		if (!isNull(organization.phonetic_name))
			builder.withValue(ContactsContract.CommonDataKinds.Organization.PHONETIC_NAME, organization.phonetic_name);
		if (!isNull(organization.symbol))
			builder.withValue(ContactsContract.CommonDataKinds.Organization.SYMBOL, organization.symbol);
		if (!isNull(organization.title))
			builder.withValue(ContactsContract.CommonDataKinds.Organization.TITLE, organization.title);
		builder.withYieldAllowed(true);
		ops.add(builder.build());
	}

	public HashMap<String, String> prepareGroups(List<ContactInfo> list) {
		HashMap<String, String> resMap = new HashMap<String, String>();
		String insertId = "-1";
		ArrayList<HashMap<String, String>> groupTitlesIn = getGroupsInContacts(list);
		if (groupTitlesIn == null)
			return null;
		ArrayList<HashMap<String, String>> groups = getAllGroupInfos();
		// Log.d("ra_group", "groups in db-->"+groups.toString());
		for (int i = 0; i < groupTitlesIn.size(); i++) {
			HashMap<String, String> groupTItle = groupTitlesIn.get(i);
			boolean haveTheGroup = false;
			for (int j = 0; j < groups.size(); j++) {
				if ((groups.get(j).get(KEY_TITLE).equals(groupTItle.get(KEY_TITLE)))
						&& (groups.get(j).get(KEY_ACC_NAME).equals(groupTItle.get(KEY_ACC_NAME)))
						&& (groups.get(j).get(KEY_ACC_TYPE).equals(groupTItle.get(KEY_ACC_TYPE)))) {
					insertId = groups.get(j).get(KEY_ID);
					haveTheGroup = true;
				} else {
				}
			}

			if (haveTheGroup == false) {
				insertId = addDIYGroup(groupTItle);
				Log.d("ra_group", "addDIYGroup(groupTItle)-->" + insertId);
			}
			HashMap<String, String> groupStr = groupTitlesIn.get(i);
			resMap.put(groupStr.toString(), insertId);
		}
		return resMap;
	}

	public static int delAllGroups(Context context) {
		Cursor c = context.getContentResolver().query(ContactsContract.Groups.CONTENT_URI, null, null, null, null);
		Log.d(TAG, "pre del--group c.getCount()=" + c.getCount());
		// c.moveToFirst();
		// do{
		// c.getString(c.getColumnIndex(ContactsContract.Groups.DELETED));
		// }while(c.moveToNext());
		c.close();
		context.getContentResolver().delete(ContactsContract.Groups.CONTENT_URI, null, null);
		c = context.getContentResolver().query(ContactsContract.Groups.CONTENT_URI, null, null, null, null);
		Log.d(TAG, "res del--group c.getCount()=" + c.getCount());
		c.close();
		// clearCursor(cur,context);
		// cur.close();
		// cur = queryContact(context, null);
		// 第一次全删除 会有几个未删除
		return 0;
	}

	private ArrayList<HashMap<String, String>> getGroupsInContacts(List<ContactInfo> list) {
		ArrayList<HashMap<String, String>> resList = new ArrayList<HashMap<String, String>>();
		HashSet<HashMap<String, String>> resSet = new HashSet<HashMap<String, String>>();
		for (ContactInfo info : list) {
			if (info.getGroupList() != null) {
				for (String group : info.getGroupList()) {
					HashMap<String, String> groupMap = new HashMap<String, String>();
					groupMap.put(KEY_TITLE, group);
					groupMap.put(KEY_ACC_NAME, info.getAccountName());
					groupMap.put(KEY_ACC_TYPE, info.getAccountType());
					resSet.add(groupMap);
				}
			}
		}
		Iterator<HashMap<String, String>> iterator = resSet.iterator();
		while (iterator.hasNext()) {
			resList.add(iterator.next());
		}
		return resList;
	}

	/**
	 * 
	 * @param info
	 * @param ops
	 * @param rawContactInsertIndex
	 */
	public void restoreGroups(ContactInfo info, ArrayList<ContentProviderOperation> ops, int rawContactInsertIndex,
			HashMap<String, String> groupStringtoID) {
		ArrayList<String> groupinfos = info.getGroupList();
		if (groupinfos == null)
			return;
		for (int i = 0; i < groupinfos.size(); i++) {
			HashMap<String, String> groupMap = new HashMap<String, String>();
			groupMap.put(KEY_TITLE, groupinfos.get(i));
			groupMap.put(KEY_ACC_NAME, info.getAccountName());
			groupMap.put(KEY_ACC_TYPE, info.getAccountName());
			groupinfos.set(i, groupMap.toString());
		}

		for (String groupInfo : groupinfos) {
			ContentProviderOperation.Builder builder = null;
			builder = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI);
			builder.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex);
			builder.withValue(ContactsContract.Data.MIMETYPE, GroupMembership.CONTENT_ITEM_TYPE);

			builder.withValue(GroupMembership.GROUP_ROW_ID, groupStringtoID.get(groupInfo));
			Log.d("ra_group", info.getStructName().givenName + "   GROUP_ROW_ID---->" + groupStringtoID.get(groupInfo));
			ops.add(builder.build());
		}
	}
	public void testRestoreGroups(ContactInfo info, ArrayList<ContentProviderOperation> ops, int rawContactInsertIndex
			,int groupid) {
		ArrayList<String> groupinfos = info.getGroupList();
		if (groupinfos == null)
			return;
		for (int i = 0; i < groupinfos.size(); i++) {
			HashMap<String, String> groupMap = new HashMap<String, String>();
			groupMap.put(KEY_TITLE, groupinfos.get(i));
			groupMap.put(KEY_ACC_NAME, info.getAccountName());
			groupMap.put(KEY_ACC_TYPE, info.getAccountName());
			groupinfos.set(i, groupMap.toString());
		}

		for (String groupInfo : groupinfos) {
			ContentProviderOperation.Builder builder = null;
			builder = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI);
			builder.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex);
			builder.withValue(ContactsContract.Data.MIMETYPE, GroupMembership.CONTENT_ITEM_TYPE);

			builder.withValue(GroupMembership.GROUP_ROW_ID, groupid);
//			Log.d("ra_group", info.getStructName().givenName + "   GROUP_ROW_ID---->" + groupStringtoID.get(groupInfo));
			ops.add(builder.build());
		}
	}
	// private void insertGroupInfo(ArrayList<ContentProviderOperation> ops,
	// ContentProviderOperation.Builder builder, int groupRowId,
	// int rawContactInsertIndex, boolean backRefer) {
	// builder =
	// ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI);
	// builder.withValue(ContactsContract.Contacts.Data.RAW_CONTACT_ID,
	// rawContactInsertIndex);
	// builder.withValue(ContactsContract.Contacts.Data.MIMETYPE,
	// GroupMembership.CONTENT_ITEM_TYPE);
	// if (backRefer)
	// builder.withValueBackReference(GroupMembership.GROUP_ROW_ID, groupRowId);
	// else
	// builder.withValue(GroupMembership.GROUP_ROW_ID, groupRowId);
	// ops.add(builder.build());
	// }

	private String addDIYGroup(HashMap<String, String> newTitle) {
		ContentValues values = new ContentValues();
		values.clear();
		values.put(ContactsContract.Groups.TITLE, newTitle.get(KEY_TITLE));
		values.put(ContactsContract.Groups.ACCOUNT_NAME, newTitle.get(KEY_ACC_NAME));
		values.put(ContactsContract.Groups.ACCOUNT_TYPE, newTitle.get(KEY_ACC_TYPE));
		Log.d("GROUPBUG", "insert--->"+newTitle.get(KEY_TITLE)+"   "+newTitle.get(KEY_ACC_NAME)+"   "+newTitle.get(KEY_ACC_TYPE));
		Uri uri = mContext.getContentResolver().insert(ContactsContract.Groups.CONTENT_URI, values);
		String id = String.valueOf(ContentUris.parseId(uri));
		return id;
	}
	public static  String testAddDIYGroup(Context context ,String title ,String accName ,String accType) {
		ContentValues values = new ContentValues();
		values.clear();
		values.put(ContactsContract.Groups.TITLE, title);
		if(accName!=null)
			values.put(ContactsContract.Groups.ACCOUNT_NAME, accName);
		if(accType!=null)
			values.put(ContactsContract.Groups.ACCOUNT_TYPE, accType);
//		Log.d("GROUPBUG", "insert--->"+newTitle.get(KEY_TITLE)+"   "+newTitle.get(KEY_ACC_NAME)+"   "+newTitle.get(KEY_ACC_TYPE));
		Uri uri = context.getContentResolver().insert(ContactsContract.Groups.CONTENT_URI, values);
		String id = String.valueOf(ContentUris.parseId(uri));
		return id;
	}

	public void restorePhoneNum(ContactInfo info, ArrayList<ContentProviderOperation> ops, int rawContactInsertIndex) {
		List<ContactInfo.PhoneInfo> phoneList = info.getPhoneList();
		if (phoneList == null)
			return;
		for (ContactInfo.PhoneInfo phoneInfo : phoneList) {
			ContentProviderOperation.Builder builder = null;
			builder = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI);
			builder.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex);
			builder.withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
			if (!isNull(phoneInfo.number))
				builder.withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phoneInfo.number);
			if (phoneInfo.type != -1)
				builder.withValue(ContactsContract.CommonDataKinds.Phone.TYPE, phoneInfo.type);
			if (!isNull(phoneInfo.label))
				builder.withValue(ContactsContract.CommonDataKinds.Phone.LABEL, phoneInfo.label);
			builder.withYieldAllowed(true);
			ops.add(builder.build());
		}
	}

	public void restoreEmailList(ContactInfo info, ArrayList<ContentProviderOperation> ops, int rawContactInsertIndex) {
		List<ContactInfo.EmailInfo> emailList = info.getEmailList();
		if (emailList == null)
			return;
		for (ContactInfo.EmailInfo emailInfo : emailList) {
			ContentProviderOperation.Builder builder = null;
			builder = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI);
			builder.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex);
			builder.withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE);
			if (emailInfo.type != -1)
				builder.withValue(ContactsContract.CommonDataKinds.Email.TYPE, emailInfo.type);
			if (!isNull(emailInfo.label))
				builder.withValue(ContactsContract.CommonDataKinds.Email.LABEL, emailInfo.label);
			if (!isNull(emailInfo.email))
				builder.withValue(ContactsContract.CommonDataKinds.Email.ADDRESS, emailInfo.email);
			builder.withYieldAllowed(true);
			ops.add(builder.build());
		}
	}

	public void restoreRelationList(ContactInfo info, ArrayList<ContentProviderOperation> ops, int rawContactInsertIndex) {
		List<ContactInfo.RelationInfo> relationList = info.getRelationList();
		if (relationList == null)
			return;
		for (ContactInfo.RelationInfo relationInfo : relationList) {
			ContentProviderOperation.Builder builder = null;
			builder = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI);
			builder.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex);
			builder.withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Relation.CONTENT_ITEM_TYPE);
			if (relationInfo.type != -1)
				builder.withValue(ContactsContract.CommonDataKinds.Relation.TYPE, relationInfo.type);
			if (!isNull(relationInfo.label))
				builder.withValue(ContactsContract.CommonDataKinds.Relation.LABEL, relationInfo.label);
			if (!isNull(relationInfo.data))
				builder.withValue(ContactsContract.CommonDataKinds.Relation.NAME, relationInfo.data);
			builder.withYieldAllowed(true);
			ops.add(builder.build());
		}
	}

	public void restoreSipAddressList(ContactInfo info, ArrayList<ContentProviderOperation> ops, int rawContactInsertIndex) {
		List<ContactInfo.SipAddressInfo> sipAddressList = info.getSipAddressList();
		if (sipAddressList == null)
			return;
		for (ContactInfo.SipAddressInfo sipAddressInfo : sipAddressList) {
			ContentProviderOperation.Builder builder = null;
			builder = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI);
			builder.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex);
			builder.withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.SipAddress.CONTENT_ITEM_TYPE);
			if (sipAddressInfo.type != -1)
				builder.withValue(ContactsContract.CommonDataKinds.SipAddress.TYPE, sipAddressInfo.type);
			if (!isNull(sipAddressInfo.label))
				builder.withValue(ContactsContract.CommonDataKinds.SipAddress.LABEL, sipAddressInfo.label);
			if (!isNull(sipAddressInfo.sip_address))
				builder.withValue(ContactsContract.CommonDataKinds.SipAddress.SIP_ADDRESS, sipAddressInfo.sip_address);
			builder.withYieldAllowed(true);
			ops.add(builder.build());
		}
	}

	public void restoreURLList(ContactInfo info, ArrayList<ContentProviderOperation> ops, int rawContactInsertIndex) {
		List<ContactInfo.WebSite> websiteList = info.getWebsiteList();
		if (websiteList == null)
			return;
		for (ContactInfo.WebSite websiteInfo : websiteList) {
			ContentProviderOperation.Builder builder = null;
			builder = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI);
			builder.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex);
			builder.withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE);
			if (websiteInfo.type != -1)
				builder.withValue(ContactsContract.CommonDataKinds.Website.TYPE, websiteInfo.type);
			if (!isNull(websiteInfo.label))
				builder.withValue(ContactsContract.CommonDataKinds.Website.LABEL, websiteInfo.label);
			if (!isNull(websiteInfo.data))
				builder.withValue(ContactsContract.CommonDataKinds.Website.URL, websiteInfo.data);
			builder.withYieldAllowed(true);
			ops.add(builder.build());
		}
	}

	public void restoreIMList(ContactInfo info, ArrayList<ContentProviderOperation> ops, int rawContactInsertIndex) {
		List<ContactInfo.IM> imList = info.getIms();
		if (imList == null)
			return;
		for (ContactInfo.IM imInfo : imList) {
			ContentProviderOperation.Builder builder = null;
			builder = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI);
			builder.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex);
			builder.withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Im.CONTENT_ITEM_TYPE);
			if (imInfo.type != -1)
				builder.withValue(ContactsContract.CommonDataKinds.Im.TYPE, imInfo.type);
			if (!isNull(imInfo.label))
				builder.withValue(ContactsContract.CommonDataKinds.Im.LABEL, imInfo.label);
			if (!isNull(imInfo.data))
				builder.withValue(ContactsContract.CommonDataKinds.Im.DATA, imInfo.data);
			if (!isNull(imInfo.protocol))
				builder.withValue(ContactsContract.CommonDataKinds.Im.PROTOCOL, imInfo.protocol);
			if (!isNull(imInfo.customProtocol))
				builder.withValue(ContactsContract.CommonDataKinds.Im.CUSTOM_PROTOCOL, imInfo.customProtocol);
			builder.withYieldAllowed(true);
			ops.add(builder.build());
		}
	}

	public void restoreEventList(ContactInfo info, ArrayList<ContentProviderOperation> ops, int rawContactInsertIndex) {
		List<ContactInfo.EventInfo> eventList = info.getEventList();
		if (eventList == null)
			return;
		for (ContactInfo.EventInfo eventInfo : eventList) {
			ContentProviderOperation.Builder builder = null;
			builder = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI);
			builder.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex);
			builder.withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE);
			builder.withValue(ContactsContract.CommonDataKinds.Event.TYPE, eventInfo.type);
			if (!isNull(eventInfo.label))
				builder.withValue(ContactsContract.CommonDataKinds.Event.LABEL, eventInfo.label);
			if (!isNull(eventInfo.start_date))
				builder.withValue(ContactsContract.CommonDataKinds.Event.START_DATE, eventInfo.start_date);
			builder.withYieldAllowed(true);
			ops.add(builder.build());
		}
	}

	public void restorePostalAddressList(ContactInfo info, ArrayList<ContentProviderOperation> ops, int rawContactInsertIndex) {
		List<ContactInfo.PostalAddress> postalList = info.getPostalAddresses();
		if (postalList == null)
			return;
		for (ContactInfo.PostalAddress postalInfo : postalList) {
			ContentProviderOperation.Builder builder = null;
			builder = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI);
			builder.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex);
			builder.withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE);
			builder.withValue(ContactsContract.CommonDataKinds.StructuredPostal.TYPE, postalInfo.type);
			if (!isNull(postalInfo.label))
				builder.withValue(ContactsContract.CommonDataKinds.StructuredPostal.LABEL, postalInfo.label);
			if (!isNull(postalInfo.city))
				builder.withValue(ContactsContract.CommonDataKinds.StructuredPostal.CITY, postalInfo.city);
			if (!isNull(postalInfo.country))
				builder.withValue(ContactsContract.CommonDataKinds.StructuredPostal.COUNTRY, postalInfo.country);
			if (!isNull(postalInfo.neighborhood))
				builder.withValue(ContactsContract.CommonDataKinds.StructuredPostal.NEIGHBORHOOD, postalInfo.neighborhood);
			if (!isNull(postalInfo.pobox))
				builder.withValue(ContactsContract.CommonDataKinds.StructuredPostal.POBOX, postalInfo.pobox);
			if (!isNull(postalInfo.postcode))
				builder.withValue(ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE, postalInfo.postcode);
			if (!isNull(postalInfo.region))
				builder.withValue(ContactsContract.CommonDataKinds.StructuredPostal.REGION, postalInfo.region);
			if (!isNull(postalInfo.street))
				builder.withValue(ContactsContract.CommonDataKinds.StructuredPostal.STREET, postalInfo.street);
			builder.withYieldAllowed(true);
			ops.add(builder.build());
		}
	}

	public void restoreRawAccInfo(ContactInfo info, ArrayList<ContentProviderOperation> ops) {
		String str_accname;
		String str_acctype = null;
		ContentProviderOperation.Builder builder = null;
		builder = ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI);
		if (!isNull(info.getAccountName())){
			str_accname = info.getAccountName();
		}else{
			str_accname = "default";
		}
		
		str_accname = getAccountNameFromDeviceType(str_accname);
		builder.withValue(ContactsContract.RawContacts.ACCOUNT_NAME, str_accname);
		if (!isNull(info.getAccountType())){
			str_acctype = info.getAccountType();
		}else{
			str_acctype = "com.google";
		}
		
		str_acctype = getAccountTypeFromDeviceType(str_acctype);
		builder.withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, str_acctype);
		builder.withYieldAllowed(true);
		ops.add(builder.build());
	}
	
	private String getAccountNameFromDeviceType(String accountName){
		String brand = android.os.Build.BRAND;
		if (brand.equals("Meizu")) {
			return "2503720";
		}
		return accountName;
	}
	
	private String getAccountTypeFromDeviceType(String accountType){
		String brand = android.os.Build.BRAND;
		if (brand.equals("Meizu")) {
			return "com.meizu.account";
		}
		return accountType;
	}

	private ArrayList<HashMap<String, String>> getAllGroupInfos() {
		ArrayList<HashMap<String, String>> groups = new ArrayList<HashMap<String, String>>();
		ContentResolver cr = mContext.getContentResolver();
		Cursor cursor = cr.query(ContactsContract.Groups.CONTENT_URI, null, ContactsContract.Groups.DELETED + " = ? ", new String[] { "0" }, null);
		cursor.moveToFirst();
		Log.d("GROUPBUG", "cursor.getColumnCount()=" + cursor.getCount());
		if (cursor.getCount() == 0)
			return groups;
		do {
			HashMap<String, String> group = new HashMap<String, String>();
			String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Groups._ID));
			String title = cursor.getString(cursor.getColumnIndex(ContactsContract.Groups.TITLE));
			String accName = cursor.getString(cursor.getColumnIndex(ContactsContract.Groups.ACCOUNT_NAME));
			String accType = cursor.getString(cursor.getColumnIndex(ContactsContract.Groups.ACCOUNT_TYPE));
			group.put(KEY_ID, id);
			group.put(KEY_TITLE, title);
			group.put(KEY_ACC_NAME, accName);
			group.put(KEY_ACC_TYPE, accType);
			Log.d("GROUPBUG", id+"  "+title+"  "+accName+"  "+accType);
			groups.add(group);
		} while (cursor.moveToNext());
		cursor.close();
		return groups;
	}

	private boolean isNull(String str) {
		if (str == null) {
			return true;
		}
		if (str.length() == 0) {
			return true;
		}
		return false;
	}
}
