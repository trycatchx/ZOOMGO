package com.dmsys.airdiskpro.backup;

import android.content.Context;

import com.dm.baselib.BaseValue;
import com.dmsys.airdiskpro.model.BackupDMFile;
import com.dmsys.airdiskpro.service.BackupService;
import com.dmsys.airdiskpro.utils.GetBakLocationTools;
import com.dmsys.airdiskpro.utils.GetBakLocationTools.MyBoolean;
import com.dmsys.airdiskpro.utils.TransTools;
import com.dmsys.dmsdk.DMSdk;
import com.dmsys.dmsdk.model.DMStorage;
import com.dmsys.dmsdk.model.DMStorageInfo;
import com.dmsys.dmsdk.model.DMUpload;
import com.dmsys.dmsdk.model.Request.OnProgressChangeListener;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * 
 * @ClassName: BackupFileAllImpl
 * @Description: 所有文件备份实现类
 * @author: yoy
 * @date: 2014年9月17日 下午7:33:07 version:1.0.0 todo 王健军
 */
public class BackupMediaImpl extends AbstractBackupFile {
	private Context context;
	private long totalFilesSize;
	private long totalProgress;
	private Object controlLock;
	private boolean isCancel = false;
	private boolean isPause = false;
	public static boolean State_Bu_Media = false;
	private DMStorage mStorage;

	// 是否成功上传过一个，只要成功备份过，即使是一张也想baknode表中增加一条记录

	// 最后一个上传成功文件的修改时间，为下次备份查找节约时间

	public BackupMediaImpl(Context context) {
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
		addRemoteUrl(backupFilesList);
		if(backupFilesList == null || backupFilesList.size() <= 0) {
			backupFileListener.onCompleted(false, ret,0L);
			return 0;
		}
		
		if (!checkSpace()) {
			if(backupFileListener != null) {
				backupFileListener.onCompleted(false, BackupService.CODE_BACKEDUP__NO_ENOUGH_SPACE,0L);
				return 0;
			}
		}
		
		
			// 开始上传备份
		long backupNumber = UploadAllFiles(backupFilesList);
		if(backupNumber == -1) {
			backupFileListener.onCompleted(false, BackupService.CODE_BACKEDUP_FILE,backupNumber);
		} else if (backupNumber == backupFilesList.size()) {
			backupFileListener.onCompleted(true, BackupService.CODE_BACKUP_SUCCESS,backupNumber);
		}  else if (backupNumber == -2) {
			backupFileListener.onCompleted(false, BackupService.CODE_BACKUP_IS_USER_STOP,backupNumber);
		} else {
			backupFileListener.onCompleted(false, BackupService.CODE_BACKUP_UPLOAD_FAILED,0);
		}
		
		return 0;
	}
	

	private boolean checkSpace() {
		
		DMStorageInfo info = DMSdk.getInstance().getStorageInfo();
		if (info != null && info.getMountStatus() == 1 &&  info.getStorages() != null) {
			if (info.getStorages().get(0) != null) {
				mStorage = info.getStorages().get(0);
				long free = info.getStorages().get(0).free *1024;
				BaseValue.diskFreeSize = free;
				long total = BaseValue.taskTotalSize = getTotalFilesSize(backupFilesList);
				if (free > total) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * 这里面包括了（创建备份目录名字（如果数据库不存在））
	 * 
	 * @param list
	 * @throws IOException 
	 */
	private void addRemoteUrl(ArrayList<BackupDMFile> list) throws InterruptedException {
		if (list != null) {
			MyBoolean returnB = new MyBoolean();
			// 如果没有变盘符名字 那就是之前的diskName 如果变了就是最新的diskName
//			System.out.println("test123 returnB"+returnB.value);
			String diskName = GetBakLocationTools.getBakDiskName(returnB);
			// 获得要备份到哪一个文件夹的名字eg：Htc-XX（1） Htc-XX（2）
			String folderName = GetBakLocationTools
					.getBakMediaFolderName(context,returnB.value);

			for (BackupDMFile file : list) {
				String remoteUrl = TransTools.getRemoteBUUrl(file, diskName,
						folderName);
				file.setRemoteUrl(remoteUrl);
			}
		}
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
	private int UploadAllFiles(final ArrayList<BackupDMFile> filesList)
			throws InterruptedException {
		totalProgress = 0;
		int backupedFileNum = 0;
		BaseValue.bigFiles.clear();
		totalFilesSize = getTotalFilesSize(filesList);
		long curParentId = -1;
		int isBackupedNumber = 0;
		for (curIndex = 0; curIndex < filesList.size(); curIndex++) {

			if (isCancel) {
				throw new InterruptedException();
			}
			//判断文件是否存在
			final BackupDMFile upLoadFile = filesList.get(curIndex);
			
			boolean isBackuped = TransTools.isMediaRemoteBackuped(upLoadFile);
			
			if(isBackuped) {
				isBackupedNumber++;
				backupedFileNum++;
				continue;
			}
			
			
			backupFileListener.onFileChanged(upLoadFile.getPath(),
					upLoadFile.getSize(), curIndex, filesList.size());
			
			// 先对这个文件之前的目录建立好
			
			if(curParentId != upLoadFile.parentID) {
				int index = upLoadFile.getRemoteUrl().lastIndexOf("/");
				String remoteFolderUrl = upLoadFile.getRemoteUrl().substring(0,
						index + 1);
				//用户中断
				try {
					TransTools.ensureRemoteDirStruct(remoteFolderUrl);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					break;
				}
				//使用同一个目录下的路径只是判断一次
				curParentId = upLoadFile.parentID;
			}
			
		
			if (mStorage.fsType != null && mStorage.fsType.equals("msdos") && upLoadFile.mSize/1024/1024 > 4*1024) {
				BaseValue.bigFiles.add(upLoadFile.mName);
				totalProgress += upLoadFile.mSize;
			}else {
				
				DMUpload task = new DMUpload(upLoadFile.mPath,
						upLoadFile.getRemoteUrl(),
						new OnProgressChangeListener() {

							@Override
							public int onProgressChange(int uid, long total,
									long already) {
								// TODO Auto-generated method stub
								int ret = 0;
								if (getBackupFileListener() != null) {
									long uploadBytes = totalProgress + already;
									mStopped = backupFileListener.onProgress(
											already, total, curIndex, uploadBytes,
											totalFilesSize, filesList.size(),
											upLoadFile.getPath());
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
				 DMSdk.getInstance().upload(task);
			}
			
			
			
			
			if (!mStopped) {
				backupedFileNum++;
			} else {
				return -2;
			}
		}
		
		if(isBackupedNumber == backupedFileNum) {
			//-1代表文件全部都已经备份过了
			backupedFileNum = -1;
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
