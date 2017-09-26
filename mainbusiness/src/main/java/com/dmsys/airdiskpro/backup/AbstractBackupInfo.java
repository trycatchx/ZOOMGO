package com.dmsys.airdiskpro.backup;

import android.content.Context;

/**
 * 
 * @ClassName:  AbstractBackup   
 * @Description:   抽象备份类
 * @author: yoy  
 * @date:   2014年9月10日 下午4:51:55   
 * version:1.0.0
 */
public abstract class AbstractBackupInfo implements IBackupInfo{
	/**
	 * 系统上下文
	 */
	private Context context;
	/**
	 * 备份监听器，子类调用get方法获取返回状态
	 */
	private BackupInfoListener backupInfoListener;
	



	public AbstractBackupInfo(Context context,BackupInfoListener backupListener) {
		setContext(context);
		setBackupInfoListener(backupListener);
	}
	public BackupInfoListener getBackupInfoListener() {
		return backupInfoListener;
	}


	public void setBackupInfoListener(BackupInfoListener backupInfoListener) {
		this.backupInfoListener = backupInfoListener;
	}

	public Context getContext() {
		return context;
	}

	public void setContext(Context context) {
		this.context = context;
	}

}
