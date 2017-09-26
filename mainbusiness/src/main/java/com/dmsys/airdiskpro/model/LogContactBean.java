package com.dmsys.airdiskpro.model;

/**
 * @ClassName:  LogContactBean   
 * @Description:   通话记录的备份数据库管理类
 * @author: Alan.wang  
 * @date:   2014-10-28 18:32  
 * version:1.0.0
 */ 
public class LogContactBean {

	public int num;
	public int node;
	public long time;
	public LogContactBean(int num, int node, long time) {
		super();
		this.num = num;
		this.node = node;
		this.time = time;
	}
	
	
}
