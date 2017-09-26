package com.dmsys.airdiskpro.backup;

import com.dmsys.airdiskpro.service.BackupService.BackupFileListener;


public interface IBackupFile extends IBackup {

	/**as
	 * 设置监听
	 * @param backupFileListener
	 */
	public void setBackupFileListener(BackupFileListener backupFileListener);
	/**
	 * 获取当前备份类型
	 * @return 备份类型
	 */
//	public BackupFileType getBackupFileType();
}
