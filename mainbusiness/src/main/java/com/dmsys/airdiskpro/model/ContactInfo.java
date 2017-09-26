package com.dmsys.airdiskpro.model;

import com.dmsys.airdiskpro.utils.ContactHandler;

import java.util.ArrayList;
import java.util.List;

public class ContactInfo {
	private static final String TAG = "ContactInfo";

	private String accountName = null;
	private String accountType = null;
	private String md5 = null;
	private List<PhoneInfo> phoneList = null;
	private List<EmailInfo> emailList = null;
	private List<EventInfo> eventList = null;
	private List<SipAddressInfo> sipAddressList = null;
	private List<RelationInfo> relationList = null;
	private StructName structName = null;
	private Organization organization = null;
	private ArrayList<PostalAddress> postalAddresses = null;
	private ArrayList<IM> ims = null;
	private ArrayList<WebSite> websiteList = null;
	private NickName nickName = null;
	private String note = null;
	private String photo = null;
	private ArrayList<String> groupList = null;
	private PhoneticThreeName phoneticThreeName = null;

	public void buildMD5() {
		String resmd5 = null;
		StringBuffer sb = new StringBuffer();
		if (structName != null)
			sb.append(structName.toString());
		if (phoneticThreeName != null)
			sb.append(phoneticThreeName.toString());
		if (organization != null)
			sb.append(organization.toString());
		if (postalAddresses != null) {
			for (PostalAddress pa : postalAddresses)
				sb.append(pa.toString());
		}
		if (phoneList != null)
		{
			for (PhoneInfo pi : phoneList)
				sb.append(pi.toString());
		}
		if (emailList != null)
		{
			for(EmailInfo ei:emailList)
				sb.append(ei.toString());
		}
		if (ims != null)
		{
			for(IM im:ims)
				sb.append(im.toString());
		}
		if (websiteList != null)
		{
			for(WebSite ws:websiteList)
				sb.append(ws.toString());
		}
		if (nickName != null)
			sb.append(nickName.toString());
		if (relationList != null)
		{
			for(RelationInfo ri:relationList)
				sb.append(ri.toString());
		}
		// 下面是以前的
		resmd5 = sb.toString();
		resmd5 = ContactHandler.makeMD5(resmd5);
		this.setMd5(resmd5);
	}

	public String getMd5() {
		return md5;
	}

	public void setMd5(String md5) {
		this.md5 = md5;
	}

	public String getAccountName() {
		return accountName;
	}

	public void setAccountName(String accName) {
		accountName = accName;
	}

	public String getAccountType() {
		return accountType;
	}

	public void setAccountType(String accType) {
		accountType = accType;
	}

	public Organization getOrganization() {
		return organization;
	}

	public void setOrganization(Organization organization) {
		this.organization = organization;
	}

	public List<SipAddressInfo> getSipAddressList() {
		return sipAddressList;
	}

	public void setSipAddressList(List<SipAddressInfo> sipAddressList) {
		this.sipAddressList = sipAddressList;
	}

	public ArrayList<String> getGroupList() {
		return groupList;
	}

	public void setGroupList(ArrayList<String> groupList) {
		this.groupList = groupList;
	}

	public String getPhoto() {
		return photo;
	}

	public void setPhoto(String photo) {
		this.photo = photo;
	}

	public List<RelationInfo> getRelationList() {
		return relationList;
	}

	public void setRelationList(List<RelationInfo> relationList) {
		this.relationList = relationList;
	}

	public List<EventInfo> getEventList() {
		return eventList;
	}

	public void setEventList(List<EventInfo> eventList) {
		this.eventList = eventList;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public NickName getNickName() {
		return nickName;
	}

	public void setNickName(NickName nickName) {
		this.nickName = nickName;
	}

	public ArrayList<WebSite> getWebsiteList() {
		return websiteList;
	}

	public void setWebsiteList(ArrayList<WebSite> websiteList) {
		this.websiteList = websiteList;
	}

	public ArrayList<IM> getIms() {
		return ims;
	}

	public void setIms(ArrayList<IM> ims) {
		this.ims = ims;
	}

	public StructName getStructName() {
		return structName;
	}

	public void setStructName(StructName structName) {
		this.structName = structName;
	}

	public ArrayList<PostalAddress> getPostalAddresses() {
		return postalAddresses;
	}

	public void setPostalAddresses(ArrayList<PostalAddress> structAddresses) {
		this.postalAddresses = structAddresses;
	}

	public PhoneticThreeName getPhoneticThreeName() {
		return phoneticThreeName;
	}

	public void setPhoneticThreeName(PhoneticThreeName phoneticThreeName) {
		this.phoneticThreeName = phoneticThreeName;
	}

	public static class PhoneticThreeName {
		public String Phonetic_first_name = null;
		public String Phonetic_middle_name = null;
		public String Phonetic_last_name = null;

		public PhoneticThreeName() {
		}

		public PhoneticThreeName(String phonetic_first_name, String phonetic_middle_name, String phonetic_last_name) {
			super();
			Phonetic_first_name = phonetic_first_name;
			Phonetic_middle_name = phonetic_middle_name;
			Phonetic_last_name = phonetic_last_name;
		}

		@Override
		public String toString() {
			// TODO Auto-generated method stub
			return mkString(Phonetic_first_name) + mkString(Phonetic_middle_name) + mkString(Phonetic_last_name);
		}
	}

	public static class Organization {
		public String company = null;
		public int type;
		public String label = null;
		public String title = null;
		public String department = null;
		public String symbol = null;
		public String phonetic_name = null;

		public Organization() {

		}

		public Organization(String company, int type, String label, String title, String department, String symbol, String phonetic_name) {
			super();
			this.company = company;
			this.type = type;
			this.label = label;
			this.title = title;
			this.department = department;
			this.symbol = symbol;
			this.phonetic_name = phonetic_name;
		}

		@Override
		public String toString() {
			// TODO Auto-generated method stub
			return mkString(company) + mkString(title);
		}

	}

	public static class PostalAddress {
		public int type;
		public String label;
		public String city;
		public String country;
		public String pobox;
		public String postcode;
		public String region;
		public String street;
		public String neighborhood;

		// public String formatted_address;
		public PostalAddress() {
		}

		public PostalAddress(int type, String label, String city, String country, String pobox, String postcode, String region, String street,
				String neighborhood) {
			super();
			this.type = type;
			this.label = label;
			this.city = city;
			this.country = country;
			this.pobox = pobox;
			this.postcode = postcode;
			this.region = region;
			this.street = street;
			this.neighborhood = neighborhood;
			// this.formatted_address = formatted_address;
		}

		@Override
		public String toString() {
			// TODO Auto-generated method stub
			return mkString(city) + mkString(country) + mkString(postcode) + mkString(region);
		}
	}

	public static class WebSite {
		public int type = -2;
		public String data = null;
		public String label = null;

		public WebSite() {
		}

		public WebSite(int type, String data, String label) {
			super();
			this.type = type;
			this.data = data;
			this.label = label;
		}

		@Override
		public String toString() {
			// TODO Auto-generated method stub
			return String.valueOf(type) + data + label;
		}
	}

	public static class IM {
		public int type = 3; // Other
		public String data = null;
		public String customProtocol = null;
		public String protocol = null;
		public String label = null;

		public IM() {
		}

		public IM(int type, String data, String customProtocol, String protocol, String label) {
			super();
			this.type = type;
			this.data = data;
			this.customProtocol = customProtocol;
			this.protocol = protocol;
			this.label = label;
		}

		@Override
		public String toString() {
			// TODO Auto-generated method stub
			return mkString(data);
		}
	}

	public static class PhoneInfo {
		public int type = -1;
		public String number = null;
		public String label = null;

		@Override
		public String toString() {
			// TODO Auto-generated method stub
			return mkString(number);
		}
	}

	public static class EmailInfo {
		public int type = -1;
		public String email = null;
		public String label = null;

		@Override
		public String toString() {
			// TODO Auto-generated method stub
			return mkString(email);
		}
	}

	public static class SipAddressInfo {
		public int type = -1;
		public String sip_address = null;
		public String label = null;

		public SipAddressInfo() {

		}

		public SipAddressInfo(int type, String sip_address, String label) {
			super();
			this.type = type;
			this.sip_address = sip_address;
			this.label = label;
		}

		@Override
		public String toString() {
			// TODO Auto-generated method stub
			return String.valueOf(type) + sip_address + label;
		}
	}

	public static class EventInfo {
		public int type = -1;
		public String start_date = null;
		public String label = null;

		public EventInfo() {
		}

		public EventInfo(int type, String start_date, String label) {
			super();
			this.type = type;
			this.start_date = start_date;
			this.label = label;
		}

		@Override
		public String toString() {
			// TODO Auto-generated method stub
			return String.valueOf(type) + start_date + label;
		}
	}

	public static class RelationInfo {
		public RelationInfo() {
		}

		public RelationInfo(int type, String data, String label) {
			super();
			this.type = type;
			this.data = data;
			this.label = label;
		}

		public int type = -1;
		public String label = null;
		public String data = null;

		@Override
		public String toString() {
			// TODO Auto-generated method stub
			return String.valueOf(type) + data + label;
		}
	}

	public static class StructName {
		// public String displayName = null;
		public String familyName = null;
		public String givenName = null;
		public String middleName = null;
		public String prefix = null;
		public String suffix = null;

		public StructName(String familyName, String givenName, String middleName, String prefix, String suffix) {
			super();
			// this.displayName = displayName;
			this.familyName = familyName;
			this.givenName = givenName;
			this.middleName = middleName;
			this.prefix = prefix;
			this.suffix = suffix;
		}

		public StructName() {

		}

		@Override
		public String toString() {
			// TODO Auto-generated method stub

			return mkString(familyName) + mkString(givenName) + mkString(middleName) + mkString(prefix) + mkString(suffix);
		}
	}

	public static class NickName {
		public NickName() {
		}

		public NickName(int type, String name, String label) {
			super();
			this.type = type;
			this.name = name;
			this.label = label;
		}

		public int type = -1;
		public String name = null;
		public String label = null;

		@Override
		public String toString() {
			// TODO Auto-generated method stub
			return mkString(name);
		}
	}

	public ContactInfo() {

	}

	/** 联系电话信息 */
	public List<PhoneInfo> getPhoneList() {
		return phoneList;
	}

	/** 联系电话信息 */
	public ContactInfo setPhoneList(List<PhoneInfo> phoneList) {
		this.phoneList = phoneList;
		return this;
	}

	/** 邮箱信息 */
	public List<EmailInfo> getEmailList() {
		return emailList;
	}

	/** 邮箱信息 */
	public ContactInfo setEmailList(List<EmailInfo> email) {
		this.emailList = email;
		return this;
	}


	private static String mkString(String str) {
		if (str == null)
			return "";
		else
			return str;
	}
}
