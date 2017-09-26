package com.dmsys.airdiskpro.backup;

import android.content.Context;

import com.dmsys.airdiskpro.model.BackupDMFile;
import com.dmsys.airdiskpro.service.BackupService.BackupFileListener;

import java.util.ArrayList;

public abstract class AbstractBackupFile implements IBackupFile {
	private Context context;
	public BackupFileListener backupFileListener;
	public ArrayList<BackupDMFile> backupFilesList;
	
	public ArrayList<BackupDMFile> getBackupFilesList() {
		return backupFilesList;
	}

	public void setBackupFilesList(ArrayList<BackupDMFile> backupFilesList) {
		this.backupFilesList = backupFilesList;
	}

	public AbstractBackupFile(Context context) {
		this.context = context;
	}

	public Context getContext() {
		return context;
	}

	public void setContext(Context context) {
		this.context = context;
	}

	public BackupFileListener getBackupFileListener() {
		return backupFileListener;
	}

	public void setBackupFileListener(BackupFileListener backupFileListener) {
		this.backupFileListener = backupFileListener;
	}
	


}
