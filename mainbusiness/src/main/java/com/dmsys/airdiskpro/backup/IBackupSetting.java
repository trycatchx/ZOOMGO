package com.dmsys.airdiskpro.backup;

/**
 * 
 * @ClassName:  IBackupFileSetting   
 * @Description:   文件备份的设置信息
 * @author: yoy  
 * @date:   2014年9月22日 上午11:55:26   
 * version:1.0.0
 */
public interface IBackupSetting {
	/**
	 * 获取当前是否需要备份
	 * @return true：需要备份，false：不需要备份
	 */
	public boolean getBackupState();
	/**
	 * 设置当前是否需要备份
	 * @param state  true：需要备份，false：不需要备份
	 */
	public void setBackupState(boolean state);

}
