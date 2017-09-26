package com.dmsys.airdiskpro.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.util.Base64;
import android.util.Log;

import com.dmsys.airdiskpro.model.ContactInfo;
import com.dmsys.airdiskpro.model.TypeNum;

import java.util.ArrayList;
import java.util.List;

public class ReadContact {
	private static final String TAG = "ReadContact";

	Context context;

	public ReadContact(Context context) {
		this.context = context;
	}

	public void readStructuredName(ContactInfo contactInfo, String rawID) {
		ContentResolver cr = context.getContentResolver();
		Cursor cursor = cr.query(ContactsContract.Data.CONTENT_URI, null, ContactsContract.CommonDataKinds.StructuredName.RAW_CONTACT_ID
				+ " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?", new String[] { rawID,
				ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE }, null);
		cursor.moveToFirst();
		// String displayName = getColumnValue(cursor,
		// ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME);
		String familyName = getColumnValue(cursor, ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME);
		String givenName = getColumnValue(cursor, ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME);
		String middleName = getColumnValue(cursor, ContactsContract.CommonDataKinds.StructuredName.MIDDLE_NAME);
		String prefix = getColumnValue(cursor, ContactsContract.CommonDataKinds.StructuredName.PREFIX);
		String suffix = getColumnValue(cursor, ContactsContract.CommonDataKinds.StructuredName.SUFFIX);

		// if (displayName == null)
		// displayName = "";
		if (familyName == null)
			familyName = "";
		if (givenName == null)
			givenName = "";
		if (middleName == null)
			middleName = "";
		if (prefix == null)
			prefix = "";
		if (suffix == null)
			suffix = "";
		// Log.d(TAG, "read name    >>>>     displayName="+displayName);
		ArrayList<String> structNames = new ArrayList<String>();
		structNames.add(familyName);
		structNames.add(givenName);
		structNames.add(middleName);
		structNames.add(prefix);
		structNames.add(suffix);
		contactInfo.setStructName(new ContactInfo.StructName(familyName, givenName, middleName, prefix, suffix));
		cursor.close();
	}

	public void readStructuredName2(ContactInfo contactInfo, Cursor cursor) {
		// String displayName = getColumnValue(cursor,
		// ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME);
		String familyName = getColumnValue(cursor, ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME);
		String givenName = getColumnValue(cursor, ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME);
		String middleName = getColumnValue(cursor, ContactsContract.CommonDataKinds.StructuredName.MIDDLE_NAME);
		String prefix = getColumnValue(cursor, ContactsContract.CommonDataKinds.StructuredName.PREFIX);
		String suffix = getColumnValue(cursor, ContactsContract.CommonDataKinds.StructuredName.SUFFIX);

		// if (displayName == null)
		// displayName = "";
		if (familyName == null)
			familyName = "";
		if (givenName == null)
			givenName = "";
		if (middleName == null)
			middleName = "";
		if (prefix == null)
			prefix = "";
		if (suffix == null)
			suffix = "";
		Log.d(TAG, "222read name    >>>>     familyName=" + familyName + "  givenName=" + givenName + "  middleName=" + middleName + "  prefix="
				+ prefix + "  suffix=" + suffix);
		ArrayList<String> structNames = new ArrayList<String>();
		structNames.add(familyName);
		structNames.add(givenName);
		structNames.add(middleName);
		structNames.add(prefix);
		structNames.add(suffix);
		contactInfo.setStructName(new ContactInfo.StructName(familyName, givenName, middleName, prefix, suffix));
	}

	public void readPostal(ContactInfo contactInfo, String rawID) {
		ContentResolver cr = context.getContentResolver();
		Cursor cursor = cr.query(ContactsContract.Data.CONTENT_URI, null, ContactsContract.CommonDataKinds.StructuredPostal.RAW_CONTACT_ID
				+ " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?", new String[] { rawID,
				ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE }, null);
		// Log.d(TAG, "cursor.getCount()=" + cursor.getCount()
		// + "cursor.getColumnCount()=" + cursor.getColumnCount());
		if (cursor.getCount() == 0) {
			cursor.close();
			return;
		}
		ArrayList<ContactInfo.PostalAddress> resAddresses = new ArrayList<ContactInfo.PostalAddress>();
		cursor.moveToFirst();
		do {
			String city = getColumnValue(cursor, ContactsContract.CommonDataKinds.StructuredPostal.CITY);
			String country = getColumnValue(cursor, ContactsContract.CommonDataKinds.StructuredPostal.COUNTRY);
			String pobox = getColumnValue(cursor, ContactsContract.CommonDataKinds.StructuredPostal.POBOX);
			String postcode = getColumnValue(cursor, ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE);
			String region = getColumnValue(cursor, ContactsContract.CommonDataKinds.StructuredPostal.REGION);
			String street = getColumnValue(cursor, ContactsContract.CommonDataKinds.StructuredPostal.STREET);
			String type = getColumnValue(cursor, ContactsContract.CommonDataKinds.StructuredPostal.TYPE);
			String label = getColumnValue(cursor, ContactsContract.CommonDataKinds.StructuredPostal.LABEL);
			String neighborhood = getColumnValue(cursor, ContactsContract.CommonDataKinds.StructuredPostal.NEIGHBORHOOD);
			// String formatted_address = getColumnValue(cursor,
			// ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS);
			if (city == null)
				city = "";
			if (country == null)
				country = "";
			if (pobox == null)
				pobox = "";
			if (postcode == null)
				postcode = "";
			if (region == null)
				region = "";
			if (street == null)
				street = "";
			if (type == null)
				type = "1";
			if (isNull(label))
				label = null;
			if (neighborhood == null)
				neighborhood = "";
			// if (formatted_address == null)
			// formatted_address = "";
			Log.d(TAG, "postal=" + "  label=" + label + "   type=" + type);
			ContactInfo.PostalAddress postalAddress = new ContactInfo.PostalAddress(Integer.valueOf(type), label, city, country, pobox, postcode,
					region, street, neighborhood);
			resAddresses.add(postalAddress);
		} while (cursor.moveToNext());
		contactInfo.setPostalAddresses(resAddresses);
		cursor.close();
	}

	public void readPostal2(ContactInfo contactInfo, Cursor cursor) {
		String city = getColumnValue(cursor, ContactsContract.CommonDataKinds.StructuredPostal.CITY);
		String country = getColumnValue(cursor, ContactsContract.CommonDataKinds.StructuredPostal.COUNTRY);
		String pobox = getColumnValue(cursor, ContactsContract.CommonDataKinds.StructuredPostal.POBOX);
		String postcode = getColumnValue(cursor, ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE);
		String region = getColumnValue(cursor, ContactsContract.CommonDataKinds.StructuredPostal.REGION);
		String street = getColumnValue(cursor, ContactsContract.CommonDataKinds.StructuredPostal.STREET);
		String type = getColumnValue(cursor, ContactsContract.CommonDataKinds.StructuredPostal.TYPE);
		String label = getColumnValue(cursor, ContactsContract.CommonDataKinds.StructuredPostal.LABEL);
		String neighborhood = getColumnValue(cursor, ContactsContract.CommonDataKinds.StructuredPostal.NEIGHBORHOOD);
		if (city == null)
			city = "";
		if (country == null)
			country = "";
		if (pobox == null)
			pobox = "";
		if (postcode == null)
			postcode = "";
		if (region == null)
			region = "";
		if (street == null)
			street = "";
		if (type == null)
			type = "1";
		if (isNull(label))
			label = null;
		if (neighborhood == null)
			neighborhood = "";
		ContactInfo.PostalAddress postalAddress = new ContactInfo.PostalAddress(Integer.valueOf(type), label, city, country, pobox, postcode, region,
				street, neighborhood);
		if (contactInfo.getPostalAddresses() == null)
			contactInfo.setPostalAddresses(new ArrayList<ContactInfo.PostalAddress>());
		contactInfo.getPostalAddresses().add(postalAddress);
	}

	public void readPhoneticName(ContactInfo contactInfo, String rawID) {
		ContentResolver cr = context.getContentResolver();
		Cursor cursor = cr.query(ContactsContract.Data.CONTENT_URI, null, ContactsContract.CommonDataKinds.StructuredName.RAW_CONTACT_ID
				+ " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?", new String[] { rawID,
				ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE }, null);
		cursor.moveToFirst();

		String phonetic_last_name = getColumnValue(cursor, ContactsContract.CommonDataKinds.StructuredName.PHONETIC_FAMILY_NAME);
		String phonetic_first_name = getColumnValue(cursor, ContactsContract.CommonDataKinds.StructuredName.PHONETIC_GIVEN_NAME);
		String phonetic_middle_name = getColumnValue(cursor, ContactsContract.CommonDataKinds.StructuredName.PHONETIC_MIDDLE_NAME);

		if (phonetic_last_name == null && phonetic_first_name == null && phonetic_middle_name == null) {
			cursor.close();
			return;
		}
		if (phonetic_last_name == null)
			phonetic_last_name = "";
		if (phonetic_first_name == null)
			phonetic_first_name = "";
		if (phonetic_middle_name == null)
			phonetic_middle_name = "";
		ContactInfo.PhoneticThreeName phoneticThreeName = new ContactInfo.PhoneticThreeName(phonetic_first_name, phonetic_middle_name,
				phonetic_last_name);
		contactInfo.setPhoneticThreeName(phoneticThreeName);
		cursor.close();
	}

	public void readPhoneticName2(ContactInfo contactInfo, Cursor cursor) {

		String phonetic_last_name = getColumnValue(cursor, ContactsContract.CommonDataKinds.StructuredName.PHONETIC_FAMILY_NAME);
		String phonetic_first_name = getColumnValue(cursor, ContactsContract.CommonDataKinds.StructuredName.PHONETIC_GIVEN_NAME);
		String phonetic_middle_name = getColumnValue(cursor, ContactsContract.CommonDataKinds.StructuredName.PHONETIC_MIDDLE_NAME);

		if (phonetic_last_name == null && phonetic_first_name == null && phonetic_middle_name == null) {
			return;
		}
		if (phonetic_last_name == null)
			phonetic_last_name = "";
		if (phonetic_first_name == null)
			phonetic_first_name = "";
		if (phonetic_middle_name == null)
			phonetic_middle_name = "";
		ContactInfo.PhoneticThreeName phoneticThreeName = new ContactInfo.PhoneticThreeName(phonetic_first_name, phonetic_middle_name,
				phonetic_last_name);
		contactInfo.setPhoneticThreeName(phoneticThreeName);
	}

	public void readOrganization(ContactInfo contactInfo, String rawID) {
		ContentResolver cr = context.getContentResolver();
		Cursor cursor = cr.query(ContactsContract.Data.CONTENT_URI, null, ContactsContract.CommonDataKinds.StructuredName.RAW_CONTACT_ID
				+ " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?", new String[] { rawID,
				ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE }, null);
		if (cursor.getCount() == 0) {
			cursor.close();
			return;
		}
		cursor.moveToFirst();

		String company = getColumnValue(cursor, ContactsContract.CommonDataKinds.Organization.COMPANY);
		int type = cursor.getInt(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Organization.TYPE));
		String title = getColumnValue(cursor, ContactsContract.CommonDataKinds.Organization.TITLE);
		String label = getColumnValue(cursor, ContactsContract.CommonDataKinds.Organization.LABEL);
		String department = getColumnValue(cursor, ContactsContract.CommonDataKinds.Organization.DEPARTMENT);
		String job_description = getColumnValue(cursor, ContactsContract.CommonDataKinds.Organization.JOB_DESCRIPTION);
		String symbol = getColumnValue(cursor, ContactsContract.CommonDataKinds.Organization.SYMBOL);
		String phonetic_name = getColumnValue(cursor, ContactsContract.CommonDataKinds.Organization.PHONETIC_NAME);
		String office_location = getColumnValue(cursor, ContactsContract.CommonDataKinds.Organization.OFFICE_LOCATION);
		if (isNull(label))
			label = null;
		ContactInfo.Organization organization = new ContactInfo.Organization(company, type, label, title, department, symbol, phonetic_name);
		contactInfo.setOrganization(organization);
		cursor.close();
	}

	public void readOrganization2(ContactInfo contactInfo, Cursor cursor) {
		String company = getColumnValue(cursor, ContactsContract.CommonDataKinds.Organization.COMPANY);
		int type = cursor.getInt(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Organization.TYPE));
		String title = getColumnValue(cursor, ContactsContract.CommonDataKinds.Organization.TITLE);
		String label = getColumnValue(cursor, ContactsContract.CommonDataKinds.Organization.LABEL);
		String department = getColumnValue(cursor, ContactsContract.CommonDataKinds.Organization.DEPARTMENT);
		String job_description = getColumnValue(cursor, ContactsContract.CommonDataKinds.Organization.JOB_DESCRIPTION);
		String symbol = getColumnValue(cursor, ContactsContract.CommonDataKinds.Organization.SYMBOL);
		String phonetic_name = getColumnValue(cursor, ContactsContract.CommonDataKinds.Organization.PHONETIC_NAME);
		String office_location = getColumnValue(cursor, ContactsContract.CommonDataKinds.Organization.OFFICE_LOCATION);
		if (isNull(label))
			label = null;
		ContactInfo.Organization organization = new ContactInfo.Organization(company, type, label, title, department, symbol, phonetic_name);
		contactInfo.setOrganization(organization);
	}

	public void readTel(Cursor cursor, ContactInfo contactInfo, String contactID) {
		// 查看联系人有多少电话号码, 如果没有返回0
		int phoneCount = cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
		if (phoneCount > 0) {
			Cursor phonesCursor = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
					ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + contactID, null, null);
			if (phonesCursor.moveToFirst()) {
				List<ContactInfo.PhoneInfo> phoneNumberList = new ArrayList<ContactInfo.PhoneInfo>();
				do {
					// 遍历所有电话号码
					String phoneNumber = phonesCursor.getString(phonesCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
					// 对应的联系人类型
					int type = phonesCursor.getInt(phonesCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
					String label = phonesCursor.getString(phonesCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.LABEL));
					if (isNull(label))
						label = null;
					// 初始化联系人电话信息
					ContactInfo.PhoneInfo phoneInfo = new ContactInfo.PhoneInfo();
					phoneInfo.type = type;
					phoneInfo.number = phoneNumber;
					phoneInfo.label = label;

					phoneNumberList.add(phoneInfo);
					Log.d(TAG, "phone read >> type=" + type + " phoneNumber=" + phoneNumber + " label=" + label);
				} while (phonesCursor.moveToNext());
				// 设置联系人电话信息
				contactInfo.setPhoneList(phoneNumberList);
			}
			phonesCursor.close();
		}
	}

	public void readTel2(ContactInfo contactInfo, Cursor phonesCursor) {
		// 查看联系人有多少电话号码, 如果没有返回0
		// 遍历所有电话号码
		String phoneNumber = phonesCursor.getString(phonesCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
		// 对应的联系人类型
		int type = phonesCursor.getInt(phonesCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
		String label = phonesCursor.getString(phonesCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.LABEL));
		if (isNull(label))
			label = "";
		// 初始化联系人电话信息
		ContactInfo.PhoneInfo phoneInfo = new ContactInfo.PhoneInfo();
		phoneInfo.type = type;
		phoneInfo.number = phoneNumber;
		phoneInfo.label = label;

		Log.d(TAG, "phone read >> type=" + type + " phoneNumber=" + phoneNumber + " label=" + label);
		// 设置联系人电话信息
		if (contactInfo.getPhoneList() == null) {
			contactInfo.setPhoneList(new ArrayList<ContactInfo.PhoneInfo>());
		}
		contactInfo.getPhoneList().add(phoneInfo);
	}

	public void readEmail(ContactInfo contactInfo, String contactID) {
		Cursor emailCur = context.getContentResolver().query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
				ContactsContract.CommonDataKinds.Email.CONTACT_ID + "=" + contactID, null, null);

		if (emailCur.moveToFirst()) {
			List<ContactInfo.EmailInfo> emailList = new ArrayList<ContactInfo.EmailInfo>();
			do {
				String email = emailCur.getString(emailCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS));
				int type = emailCur.getInt(emailCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.TYPE));
				String label = emailCur.getString(emailCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.LABEL));
				if (isNull(label))
					label = null;
				ContactInfo.EmailInfo emailInfo = new ContactInfo.EmailInfo();
				emailInfo.type = type;
				emailInfo.email = email;
				emailInfo.label = label;
				Log.d(TAG, "email read >> type=" + type + " email=" + email + " label=" + label);
				emailList.add(emailInfo);
			} while (emailCur.moveToNext());
			contactInfo.setEmailList(emailList);
		}
		emailCur.close();
	}

	public void readEmail2(ContactInfo contactInfo, Cursor emailCur) {

		String email = emailCur.getString(emailCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS));
		int type = emailCur.getInt(emailCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.TYPE));
		String label = emailCur.getString(emailCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.LABEL));
		if (isNull(label))
			label = null;
		ContactInfo.EmailInfo emailInfo = new ContactInfo.EmailInfo();
		emailInfo.type = type;
		emailInfo.email = email;
		emailInfo.label = label;
		Log.d(TAG, "email read >> type=" + type + " email=" + email + " label=" + label);
		if (contactInfo.getEmailList() == null)
			contactInfo.setEmailList(new ArrayList<ContactInfo.EmailInfo>());
		contactInfo.getEmailList().add(emailInfo);
	}

	public void readSipAddress(ContactInfo contactInfo, String rawID) {
		ContentResolver cr = context.getContentResolver();
		Cursor cursor = cr.query(ContactsContract.Data.CONTENT_URI, null, ContactsContract.CommonDataKinds.SipAddress.RAW_CONTACT_ID + " = ? AND "
				+ ContactsContract.Data.MIMETYPE + " = ?", new String[] { rawID, ContactsContract.CommonDataKinds.SipAddress.CONTENT_ITEM_TYPE },
				null);
		if (cursor.getCount() == 0) {
			cursor.close();
			return;
		}
		cursor.moveToFirst();
		List<ContactInfo.SipAddressInfo> sipAddressList = new ArrayList<ContactInfo.SipAddressInfo>();
		do {
			String sipAddress = getColumnValue(cursor, ContactsContract.CommonDataKinds.SipAddress.SIP_ADDRESS);
			String label = getColumnValue(cursor, ContactsContract.CommonDataKinds.SipAddress.LABEL);
			int type = cursor.getInt(cursor.getColumnIndex(ContactsContract.CommonDataKinds.SipAddress.TYPE));
			if (isNull(sipAddress))
				sipAddress = "";
			if (isNull(label))
				label = null;
			Log.d(TAG, "sipaddress=" + sipAddress + "   label=" + label + "   type=" + type);
			sipAddressList.add(new ContactInfo.SipAddressInfo(type, sipAddress, label));
		} while (cursor.moveToNext());
		contactInfo.setSipAddressList(sipAddressList);
		cursor.close();
	}

	public void readSipAddress2(ContactInfo contactInfo, Cursor cursor) {
		List<ContactInfo.SipAddressInfo> sipAddressList = new ArrayList<ContactInfo.SipAddressInfo>();
		String sipAddress = getColumnValue(cursor, ContactsContract.CommonDataKinds.SipAddress.SIP_ADDRESS);
		String label = getColumnValue(cursor, ContactsContract.CommonDataKinds.SipAddress.LABEL);
		int type = cursor.getInt(cursor.getColumnIndex(ContactsContract.CommonDataKinds.SipAddress.TYPE));
		if (isNull(sipAddress))
			sipAddress = "";
		if (isNull(label))
			label = null;
		Log.d(TAG, "sipaddress=" + sipAddress + "   label=" + label + "   type=" + type);
		if (contactInfo.getSipAddressList() == null)
			contactInfo.setSipAddressList(new ArrayList<ContactInfo.SipAddressInfo>());
		contactInfo.getSipAddressList().add(new ContactInfo.SipAddressInfo(type, sipAddress, label));
	}

	public void readIM(ContactInfo contactInfo, String rawID) {
		ContentResolver cr = context.getContentResolver();

		Cursor cursor = cr.query(ContactsContract.Data.CONTENT_URI, null, ContactsContract.CommonDataKinds.StructuredName.RAW_CONTACT_ID
				+ " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?",
				new String[] { rawID, ContactsContract.CommonDataKinds.Im.CONTENT_ITEM_TYPE }, null);
		if (cursor.getCount() == 0) {
			cursor.close();
			return;
		}
		ArrayList<ContactInfo.IM> ims = new ArrayList<ContactInfo.IM>();
		cursor.moveToFirst();
		do {
			String customProtocol = getColumnValue(cursor, ContactsContract.CommonDataKinds.Im.CUSTOM_PROTOCOL);
			String protocol = getColumnValue(cursor, ContactsContract.CommonDataKinds.Im.PROTOCOL);
			String data = getColumnValue(cursor, ContactsContract.CommonDataKinds.Im.DATA);
			int type = cursor.getInt(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Im.TYPE));
			String label = getColumnValue(cursor, ContactsContract.CommonDataKinds.Im.LABEL);
			if (isNull(data)) {
				data = "";
				continue;
			}
			if (customProtocol == null)
				customProtocol = "";
			if (protocol == null)
				protocol = "";
			if (isNull(label))
				label = null;
			ContactInfo.IM im = new ContactInfo.IM(type, data, customProtocol, protocol, label);
			ims.add(im);
		} while (cursor.moveToNext());
		contactInfo.setIms(ims);
		cursor.close();
	}

	public void readIM2(ContactInfo contactInfo, Cursor cursor) {
		String customProtocol = getColumnValue(cursor, ContactsContract.CommonDataKinds.Im.CUSTOM_PROTOCOL);
		String protocol = getColumnValue(cursor, ContactsContract.CommonDataKinds.Im.PROTOCOL);
		String data = getColumnValue(cursor, ContactsContract.CommonDataKinds.Im.DATA);
		int type = cursor.getInt(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Im.TYPE));
		String label = getColumnValue(cursor, ContactsContract.CommonDataKinds.Im.LABEL);
		if (isNull(data)) {
			data = "";
		}
		if (customProtocol == null)
			customProtocol = "";
		if (protocol == null)
			protocol = "";
		if (isNull(label))
			label = null;
		ContactInfo.IM im = new ContactInfo.IM(type, data, customProtocol, protocol, label);
		if (contactInfo.getIms() == null)
			contactInfo.setIms(new ArrayList<ContactInfo.IM>());
		contactInfo.getIms().add(im);
	}

	public void readWebsite(ContactInfo contactInfo, String rawID) {
		ContentResolver cr = context.getContentResolver();
		Cursor cursor = cr.query(ContactsContract.Data.CONTENT_URI, null, ContactsContract.CommonDataKinds.StructuredName.RAW_CONTACT_ID
				+ " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?", new String[] { rawID,
				ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE }, null);
		if (cursor.getCount() == 0) {
			cursor.close();
			return;
		}
		ArrayList<ContactInfo.WebSite> websiteList = new ArrayList<ContactInfo.WebSite>();
		cursor.moveToFirst();
		do {
			int type = cursor.getInt(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Website.TYPE));
			String data = getColumnValue(cursor, ContactsContract.CommonDataKinds.Website.URL);
			String label = getColumnValue(cursor, ContactsContract.CommonDataKinds.Website.LABEL);
			if (isNull(data)) {
				data = "";
				continue;
			}
			if (isNull(label))
				label = null;
			ContactInfo.WebSite website = new ContactInfo.WebSite(type, data, label);
			websiteList.add(website);
		} while (cursor.moveToNext());
		contactInfo.setWebsiteList(websiteList);
		cursor.close();
	}

	public void readWebsite2(ContactInfo contactInfo, Cursor cursor) {
		int type = cursor.getInt(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Website.TYPE));
		String data = getColumnValue(cursor, ContactsContract.CommonDataKinds.Website.URL);
		String label = getColumnValue(cursor, ContactsContract.CommonDataKinds.Website.LABEL);
		if (isNull(data)) {
			data = "";
		}
		if (isNull(label))
			label = null;
		ContactInfo.WebSite website = new ContactInfo.WebSite(type, data, label);
		if (contactInfo.getWebsiteList() == null)
			contactInfo.setWebsiteList(new ArrayList<ContactInfo.WebSite>());
		contactInfo.getWebsiteList().add(website);
	}

	public void readNickName(ContactInfo contactInfo, String rawID) {
		ContentResolver cr = context.getContentResolver();
		Cursor cursor = cr.query(ContactsContract.Data.CONTENT_URI, null, ContactsContract.CommonDataKinds.StructuredName.RAW_CONTACT_ID
				+ " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?", new String[] { rawID,
				ContactsContract.CommonDataKinds.Nickname.CONTENT_ITEM_TYPE }, null);
		if (cursor.getCount() == 0) {
			cursor.close();
			return;
		}
		ContactInfo.NickName nickname = null;
		cursor.moveToFirst();
		String data = getColumnValue(cursor, ContactsContract.CommonDataKinds.Nickname.NAME);
		String type = getColumnValue(cursor, ContactsContract.CommonDataKinds.Nickname.TYPE);
		String label = getColumnValue(cursor, ContactsContract.CommonDataKinds.Nickname.LABEL);
		if (isNull(data)) {
			data = "";
			cursor.close();
			return;
		}
		if (isNull(type)) {
			type = String.valueOf(TypeNum.TYPE_NICKNAME_DEFAULT);
		}

		if (isNull(label))
			label = null;
		nickname = new ContactInfo.NickName(Integer.valueOf(type), data, label);
		contactInfo.setNickName(nickname);
		cursor.close();
	}

	public void readNickName2(ContactInfo contactInfo, Cursor cursor) {
		ContactInfo.NickName nickname = null;
		String data = getColumnValue(cursor, ContactsContract.CommonDataKinds.Nickname.NAME);
		String type = getColumnValue(cursor, ContactsContract.CommonDataKinds.Nickname.TYPE);
		String label = getColumnValue(cursor, ContactsContract.CommonDataKinds.Nickname.LABEL);
		if (isNull(data)) {
			data = "";
//			cursor.close();
			return;
		}
		if (isNull(type)) {
			type = String.valueOf(TypeNum.TYPE_NICKNAME_DEFAULT);
		}

		if (isNull(label))
			label = null;
		nickname = new ContactInfo.NickName(Integer.valueOf(type), data, label);
		contactInfo.setNickName(nickname);
	}

	public void readNote(ContactInfo contactInfo, String rawID) {
		ContentResolver cr = context.getContentResolver();
		Cursor cursor = cr.query(ContactsContract.Data.CONTENT_URI, null, ContactsContract.CommonDataKinds.StructuredName.RAW_CONTACT_ID
				+ " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?", new String[] { rawID,
				ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE }, null);
		if (cursor.getCount() == 0) {
			cursor.close();
			return;
		}
		cursor.moveToFirst();
		String data = getColumnValue(cursor, ContactsContract.CommonDataKinds.Note.NOTE);
		if (isNull(data)) {
			data = "";
			cursor.close();
			return;
		}
		contactInfo.setNote(data);
		cursor.close();
	}

	public void readNote2(ContactInfo contactInfo, Cursor cursor) {
		String data = getColumnValue(cursor, ContactsContract.CommonDataKinds.Note.NOTE);
		if (isNull(data)) {
			data = "";
			return;
		}
		contactInfo.setNote(data);
	}

	public void readPhoto(ContactInfo contactInfo, String rawID) {
		ContentResolver cr = context.getContentResolver();
		Cursor cursor = cr.query(ContactsContract.Data.CONTENT_URI, null, ContactsContract.CommonDataKinds.StructuredName.RAW_CONTACT_ID
				+ " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?", new String[] { rawID,
				ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE }, null);
		if (cursor.getCount() == 0) {
			cursor.close();
			return;
		}
		cursor.moveToFirst();
		byte[] bytes = cursor.getBlob(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Photo.PHOTO));
		// String str = cursor
		// .getString(cursor
		// .getColumnIndex(ContactsContract.CommonDataKinds.Photo.PHOTO_FILE_ID));
		String imageStr = "";
		if (bytes != null) {
			try {
				imageStr = Base64.encodeToString(bytes, Base64.DEFAULT);
				contactInfo.setPhoto(imageStr);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		cursor.close();
	}

	public void readPhoto2(ContactInfo contactInfo, Cursor cursor) {
		byte[] bytes = cursor.getBlob(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Photo.PHOTO));
		String imageStr = "";
		if (bytes != null) {
			try {
				imageStr = Base64.encodeToString(bytes, Base64.DEFAULT);
				contactInfo.setPhoto(imageStr);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void readGroup(ContactInfo contactInfo, String rawID) {
		// getAllGroup();
		ContentResolver cr = context.getContentResolver();
		Cursor cursor = cr.query(ContactsContract.Data.CONTENT_URI, null, ContactsContract.CommonDataKinds.StructuredName.RAW_CONTACT_ID
				+ " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?", new String[] { rawID,
				ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE }, null);
		if (cursor.getCount() == 0) {
			cursor.close();
			return;
		}
		cursor.moveToFirst();
		String group_row_id = "-1";
		ArrayList<String> titleList = new ArrayList<String>();
		do {
			group_row_id = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID));
			String title = getGroupTitle(group_row_id);
			titleList.add(title);
		} while (cursor.moveToNext());
		contactInfo.setGroupList(titleList);
		cursor.close();

	}

	public void readGroup2(ContactInfo contactInfo, Cursor cursor) {
		String group_row_id = "-1";
		group_row_id = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID));
		String title = getGroupTitle(group_row_id);
		if(title == null)
			return ;
		if (contactInfo.getGroupList() == null)
			contactInfo.setGroupList(new ArrayList<String>());
		contactInfo.getGroupList().add(title);
	}

	public void readEvent(ContactInfo contactInfo, String rawID) {
		ContentResolver cr = context.getContentResolver();
		Cursor cursor = cr.query(ContactsContract.Data.CONTENT_URI, null, ContactsContract.CommonDataKinds.Event.RAW_CONTACT_ID + " = ? AND "
				+ ContactsContract.Data.MIMETYPE + " = ?", new String[] { rawID, ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE }, null);
		if (cursor.getCount() == 0) {
			cursor.close();
			return;
		}
		cursor.moveToFirst();
		List<ContactInfo.EventInfo> eventList = new ArrayList<ContactInfo.EventInfo>();
		do {
			String start_data = getColumnValue(cursor, ContactsContract.CommonDataKinds.Event.START_DATE);
			String label = getColumnValue(cursor, ContactsContract.CommonDataKinds.Event.LABEL);
			int type = cursor.getInt(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Event.TYPE));
			if (isNull(start_data))
				start_data = "";
			if (isNull(label))
				label = null;
			eventList.add(new ContactInfo.EventInfo(type, start_data, label));
		} while (cursor.moveToNext());
		contactInfo.setEventList(eventList);
		cursor.close();
	}

	public void readEvent2(ContactInfo contactInfo, Cursor cursor) {
		String start_data = getColumnValue(cursor, ContactsContract.CommonDataKinds.Event.START_DATE);
		String label = getColumnValue(cursor, ContactsContract.CommonDataKinds.Event.LABEL);
		int type = cursor.getInt(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Event.TYPE));
		if (isNull(start_data))
			start_data = "";
		if (isNull(label))
			label = null;
		ContactInfo.EventInfo eventInfo = new ContactInfo.EventInfo(type, start_data, label);
		if (contactInfo.getEventList() == null)
			contactInfo.setEventList(new ArrayList<ContactInfo.EventInfo>());
		contactInfo.getEventList().add(eventInfo);
	}

	public void readRelation(ContactInfo contactInfo, String rawID) {
		ContentResolver cr = context.getContentResolver();
		Cursor cursor = cr.query(ContactsContract.Data.CONTENT_URI, null, ContactsContract.CommonDataKinds.StructuredName.RAW_CONTACT_ID
				+ " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?", new String[] { rawID,
				ContactsContract.CommonDataKinds.Relation.CONTENT_ITEM_TYPE }, null);
		if (cursor.getCount() == 0) {
			cursor.close();
			return;
		}
		cursor.moveToFirst();
		List<ContactInfo.RelationInfo> relationList = new ArrayList<ContactInfo.RelationInfo>();
		do {
			String data = getColumnValue(cursor, ContactsContract.CommonDataKinds.Relation.NAME);
			String label = getColumnValue(cursor, ContactsContract.CommonDataKinds.Relation.LABEL);
			int type = cursor.getInt(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Relation.TYPE));
			if (isNull(data))
				data = "";
			if (isNull(label))
				label = null;
			relationList.add(new ContactInfo.RelationInfo(type, data, label));
		} while (cursor.moveToNext());
		contactInfo.setRelationList(relationList);
		cursor.close();
	}

	public void readRelation2(ContactInfo contactInfo, Cursor cursor) {
		String data = getColumnValue(cursor, ContactsContract.CommonDataKinds.Relation.NAME);
		String label = getColumnValue(cursor, ContactsContract.CommonDataKinds.Relation.LABEL);
		int type = cursor.getInt(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Relation.TYPE));
		if (isNull(data))
			data = "";
		if (isNull(label))
			label = null;
		ContactInfo.RelationInfo relationInfo = new ContactInfo.RelationInfo(type, data, label);
		if(contactInfo.getRelationList()==null)
			contactInfo.setRelationList(new ArrayList<ContactInfo.RelationInfo>());
		contactInfo.getRelationList().add(relationInfo);
	}

	private String getColumnValue(Cursor c, String columnName) {
		String res = c.getString(c.getColumnIndex(columnName));
		return res;
	}

	public static boolean isNull(String str) {
		if (str == null) {
			return true;
		}
		if (str.length() == 0) {
			return true;
		}
		return false;
	}

	private String getGroupTitle(String group_raw_id) {
		ContentResolver cr = context.getContentResolver();
		Cursor cursor = cr.query(ContactsContract.Groups.CONTENT_URI, null, ContactsContract.Groups._ID + " = ? ", new String[] { group_raw_id },
				null);
		String title = null;
		if(cursor.moveToFirst())
			title = cursor.getString(cursor.getColumnIndex(ContactsContract.Groups.TITLE));
		cursor.close();
		return title;
	}
	

}
