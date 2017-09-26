package com.dmsys.airdiskpro.model;

import com.dmsys.airdiskpro.backup.AbstractBackupInfoDscreption;

public class ContactsConfig extends AbstractBackupInfoDscreption{
	/**
	 * 备份的时间：毫秒
	 */
	private long c_time;
	/**
	 * 备份的手机型号
	 */
	private String c_phoneModel;
	/**
	 * 联系人个数
	 */
	private int c_Num;

	private String c_MD5;
	public String getMD5() {
		return c_MD5;
	}

	public void setMD5(String c_MD5) {
		this.c_MD5 = c_MD5;
	}

	public long getTime() {
		return c_time;
	}

	public void setTime(long time) {
		this.c_time = time;
	}
	
	

	public String getPhoneModel() {
		return c_phoneModel;
	}

	public void setPhoneModel(String phoneModel) {
		this.c_phoneModel = phoneModel;
	}

	public int getContactsNum() {
		return c_Num;
	}

	public void setContactsNum(int contactsNum) {
		this.c_Num = contactsNum;
	}

	


	public ContactsConfig(long time, String model, int num,String md5) {
		super(BackupInfoType.CONTACTS, null, null, null);
		this.c_time = time;
		this.c_phoneModel = model;
		this.c_Num = num;
		this.c_MD5 = md5;
	}
	
	


	@Override
	public String toString() {
		return "ContactsConfig [c_time=" + c_time + ", c_phoneModel=" + c_phoneModel + ", c_Num=" + c_Num + ", c_MD5=" + c_MD5 + "]";
	}

	@Override
	public String toDscreptionFormat() {
		// TODO Auto-generated method stub
		return null;
	}

}
