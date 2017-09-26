package com.dmsys.airdiskpro.backup;

import android.content.Context;

import com.dm.baselib.BaseValue;
import com.dmsys.airdiskpro.filemanager.FileOperationHelper;
import com.dmsys.airdiskpro.model.BackupDMFile;
import com.dmsys.airdiskpro.service.BackupService;
import com.dmsys.dmsdk.DMSdk;
import com.dmsys.dmsdk.model.DMBackup;
import com.dmsys.dmsdk.model.DMStorage;
import com.dmsys.dmsdk.model.DMStorageInfo;

import java.io.File;
import java.util.ArrayList;


public class AutoMediaImpl extends AbstractBackupFile {
	private Context context;
	private long totalFilesSize;
	private long totalProgress;
	private Object controlLock;
	private boolean isCancel = false;
	private boolean isPause = false;
	public static boolean State_Bu_Media = false;

	// 是否成功上传过一个，只要成功备份过，即使是一张也想baknode表中增加一条记录

	// 最后一个上传成功文件的修改时间，为下次备份查找节约时间

	public AutoMediaImpl(Context context) {
		super(context);
		this.context = context;
	}

	/**
	 * 进行备份
	 */
	@Override
	public int backup() throws Exception {
		isCancel = false;
		int ret = BackupService.CODE_BACKUP_NONE;
		if(backupFilesList == null || backupFilesList.size() <= 0) {
			backupFileListener.onCompleted(false, ret,0L);
			return 0;
		}
		
			// 开始上传备份
		long backupNumber = backupAllFiles(backupFilesList);
		if(backupNumber == -1) {
			backupFileListener.onCompleted(false, BackupService.CODE_BACKEDUP_FILE,backupNumber);
		} else if(backupNumber == -2) {
			backupFileListener.onCompleted(false, BackupService.CODE_BACKEDUP__NO_ENOUGH_SPACE,backupNumber);
			return -2;
		}else if (backupNumber == backupFilesList.size()) {
			backupFileListener.onCompleted(true, BackupService.CODE_BACKUP_SUCCESS,backupNumber);
		} else {
			if (mStopped) {
				backupFileListener.onCompleted(false, BackupService.CODE_BACKUP_IS_USER_STOP,backupNumber);
			}else {
				backupFileListener.onCompleted(false, BackupService.CODE_BACKUP_UPLOAD_FAILED,backupFilesList.size() - backupNumber);
			}
		}
		return 0;
	}
	
	@Override
	public void pause() {
//		isPause = true;
	}

	@Override
	public void resume() {
//		isPause = false;
//		resumeThread();
	}

	@Override
	public void cancel() {
		isCancel = true;
	}

	int curIndex;
	boolean mStopped = false;

	// 备份所有的文件，包括文件进度的回调
	private int backupAllFiles(final ArrayList<BackupDMFile> filesList)throws InterruptedException {
		totalProgress = 0;
		int backupedFileNum = 0,ret = 0;
		DMStorage mStorage = null;
		BaseValue.bigFiles.clear();
		totalFilesSize = getTotalFilesSize(filesList);
		
		DMStorageInfo info = DMSdk.getInstance().getStorageInfo();
		if (info != null && info.getStorages() != null && info.getStorages().size() > 0) {
			mStorage = info.getStorages().get(0);
		}
		
		for (curIndex = 0; curIndex < filesList.size(); curIndex++) {

			if (isCancel) {
				backupFileListener.onCompleted(false, BackupService.CODE_BACKUP_IS_USER_STOP,backupedFileNum);
				throw new InterruptedException();
			}
			//判断文件是否存在
			final BackupDMFile file = filesList.get(curIndex);
			
			String uuid = FileOperationHelper.getInstance().generateFileId(file);
			System.out.println("backservice uuid:"+uuid);
			System.out.println("backservice parent:"+file.getParentName());
			backupFileListener.onFileChanged(file.getPath(),file.getSize(), curIndex, filesList.size());
			
			if (mStorage.fsType != null && mStorage.fsType.equals("msdos") && file.mSize/1024/1024 > 4*1024) {
				BaseValue.bigFiles.add(file.mName);
				totalProgress += file.mSize;
			}else {
				
				
				System.out.println("test ParentName()"+file.getParentName());
				System.out.println("test path"+file.mPath);
				DMBackup backup = new DMBackup(uuid,file.getParentName(),file.mPath,new DMBackup.OnProgressChangeListener() {
					
					@Override
					public int onProgressChange(int uid, long total, long already) {
						// TODO Auto-generated method stub
						int ret = 0;
						if (getBackupFileListener() != null) {
							long uploadBytes = totalProgress + already;
							mStopped = backupFileListener.onProgress(already, total, curIndex, uploadBytes,totalFilesSize, filesList.size(),file.getPath());
							if (mStopped) {
								ret = -1;
							}
						}

						if (already >= total) {
							totalProgress += total;
						}
						return ret;
					}
				});
				ret = DMSdk.getInstance().backupFile(backup);
				System.out.println("backkk ret:"+ret);
			}
			
			
			if (!mStopped) {
				if (ret == 0) {
					backupedFileNum++;
				}else if (ret == 10216) {
					return -2;
				}
				
			} else {
				break;
			}
		}
		
		return backupedFileNum;
	}

	private long getTotalFilesSize(ArrayList<BackupDMFile> filesList) {
		long total = 0;
		if (filesList == null)
			return 0;
		for (int i = 0; i < filesList.size(); i++) {
			File backupFile = new File(filesList.get(i).getPath());
			if (backupFile.exists()) {
				total += backupFile.length();
			}
		}
		return total;
	}

	

	
}
