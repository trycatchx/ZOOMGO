package com.dmsys.airdiskpro.backup;

import com.dmsys.airdiskpro.model.BackupInfoType;

import java.util.Date;
/**
 * 
 * @ClassName:  BackupDscreption   
 * @Description:   记录描述信息
 * @author: yoy  
 * @date:   2014年9月10日 下午5:17:12   
 * version:1.0.0
 */
public abstract class AbstractBackupInfoDscreption {
	/**
	 * 对应备份信息对应的类型，短信，通讯录，通话记录
	 */
	private BackupInfoType backupInfoType;

	/**
	 * 设备名
	 */
	private String deviceName;
	/**
	 * 记录条数
	 */
	private String total;
	/**
	 * 记录的时间
	 */
	private Date date;


	public AbstractBackupInfoDscreption(BackupInfoType backupInfoType, String deviceName,String total, Date date) {
		this.backupInfoType = backupInfoType;
		this.deviceName = deviceName;
		this.total = total;
		this.date = date;
	}
	public BackupInfoType getBackupInfoType() {
		return backupInfoType;
	}

	public void setBackupInfoType(BackupInfoType backupInfoType) {
		this.backupInfoType = backupInfoType;
	}

	public String getDeviceName() {
		return deviceName;
	}
	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}
	public String getTotal() {
		return total;
	}
	public void setTotal(String total) {
		this.total = total;
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	
	/**
	 * 
	 * @return 把对应的数据转化为指定的格式
	 * 例子：<Description DeviceName=”i9192” Total=”900” Date=”1370009900989” />
	 */
	public abstract String toDscreptionFormat();
	
}
