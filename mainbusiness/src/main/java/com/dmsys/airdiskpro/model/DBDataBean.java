package com.dmsys.airdiskpro.model;

import java.util.ArrayList;

/**
 * @ClassName:  DBDataBean   
 * @Description:   用于携带插入数据库或从数据库取出的数据
 * @author: Alan.wang  
 * @date:   2014-10-21 17:14  
 * version:1.0.0
 */ 
public class DBDataBean {

	public int contactId;
	public int mimetype;
	public ArrayList<String> dataList = new ArrayList<String>();
	
	public DBDataBean(int contactId, int mimetype, ArrayList<String> dataList) {
		super();
		this.contactId = contactId;
		this.mimetype = mimetype;
		this.dataList = dataList;
	}
	

}
