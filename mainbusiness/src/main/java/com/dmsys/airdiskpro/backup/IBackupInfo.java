package com.dmsys.airdiskpro.backup;



/**
 * 
 * @ClassName:  IBackup   
 * @Description:  备份功能模块接口定义
 * @author: yoy  
 * @date:   2014年9月11日 上午11:56:06   
 * version:1.0.1
 */

public interface IBackupInfo extends IBackup,IBackupInfoSetting{
	
	public enum RecoverMode {
		/**
		 * 替换模式，用导入的记录替换，先机器中的记录信息，不保存原有记录
		 */
		REPLACE,
		/**
		 * 覆盖策略，遇到相同的记录，不重复导入
		 */
		COVER,
	}
	
	public enum BackupInfoStatus {
		/**
		 * 空闲的，没有启动备份
		 */
		IDLE,
		/**
		 * 运行中
		 */
		RUNNING,
		/**
		 * 暂停状态
		 */
		PAUSE,
		/**
		 * 完成
		 */
		COMPLETED
	}
	/**
	 * 开始恢复
	 */
	public void recover(RecoverMode recoverMode, AbstractBackupInfoDscreption backupDscreption) throws InterruptedException ;
//	/**
//	 * 获取备份列表信息
//	 */
//	public List<? extends AbstractBackupInfoDscreption> getBackupInfoDscreptionList();
	
	public void delete(AbstractBackupInfoDscreption backupDscreption) throws Exception;

	
	public void setBackupInfoListener(BackupInfoListener backupInfoListener);
	/**
	 * 获取当前备份/恢复状态
	 * @return 备份/恢复状态
	 */
	public BackupInfoStatus getBackupInfoStatus();
}
