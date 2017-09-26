package com.dmsys.airdiskpro.model;
/**
 * @ClassName:  DBContactsInfoBean   
 * @Description:   用于携带插入数据库或从数据库取出的数据
 * @author: Alan.wang  
 * @date:   2014-10-21 17:02  
 * version:1.0.0
 */ 
public class DBContactsInfoBean {

	public String md5;
	public String accountName;
	public String accountType;
	public int photoId;
	public int groupId;
	
	public DBContactsInfoBean(String md5, String accountName,
			String accountType, int photoId, int groupId) {
		super();
		this.md5 = md5;
		this.accountName = accountName;
		this.accountType = accountType;
		this.photoId = photoId;
		this.groupId = groupId;
	}
}
