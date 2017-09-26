package com.dmsys.airdiskpro.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.dm.baselib.BaseValue;
import com.dmsys.airdiskpro.backup.AbstractBackupFile;
import com.dmsys.airdiskpro.backup.AbstractBackupInfo;
import com.dmsys.airdiskpro.backup.AutoMediaImpl;
import com.dmsys.airdiskpro.backup.BackupInfoFactory;
import com.dmsys.airdiskpro.backup.BackupInfoListener;
import com.dmsys.airdiskpro.backup.BackupMediaImpl;
import com.dmsys.airdiskpro.backup.IBackupInfo.RecoverMode;
import com.dmsys.airdiskpro.db.BackupSettingDB;
import com.dmsys.airdiskpro.event.BackupRefreshEvent;
import com.dmsys.airdiskpro.event.BackupStateEvent;
import com.dmsys.airdiskpro.event.DisconnectEvent;
import com.dmsys.airdiskpro.filemanager.FileOperationHelper;
import com.dmsys.airdiskpro.model.BackupDMFile;
import com.dmsys.airdiskpro.model.BackupInfoType;
import com.dmsys.airdiskpro.model.BakSetBean;
import com.dmsys.airdiskpro.model.ContactsConfig;
import com.dmsys.airdiskpro.ui.AlbumBackupActivity;
import com.dmsys.airdiskpro.utils.TransTools;
import com.dmsys.dmsdk.DMSdk;
import com.dmsys.dmsdk.model.DMFile;
import com.dmsys.dmsdk.model.DMFileCategoryType;
import com.dmsys.dmsdk.util.DMFileTypeUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import de.greenrobot.event.EventBus;


public class BackupService extends Service {
	private Handler mediaHandler = null;
	private Handler contactsHandler = null;
	private static final String Tag = "BackupService";
	public static final String State_Update_Action = "State_Update_Action";
	public static final String ACTION_STOP_BACKUP = "ACTION_STOP_BACKUP";

	public static final int PRO_TYPE_MEDIA = 0;
	public static final int PRO_TYPE_CONTACTS = 1;

	public static final int BUTYPE_MEDIA = 0;
	public static final int BUTYPE_BU_CONTACTS = 1;

	public static final String KEY_PRO = "KEY_PRO";
	public static final String KEY_MAX = "KEY_MAX";
	public static final String KEY_TOTAL_LEFT = "KEY_TOTAL_LEFT";
	public static final String KEY_PATH = "KEY_PATH";
	public static final String KEY_TOTAL_FINISH = "KEY_TOTAL_FINISH";
	public static final String BUCKUP_TYPE = "BUCKUP_TYPE";
	

	public static final boolean DEFAULT_BAK_IMAGE = false;
	public static final boolean DEFAULT_BAK_VIDEO = false;
	public static final boolean DEFAULT_BAK_CONTACTS = true;
	private int curBUType = BUTYPE_MEDIA;
	private BUThread buThread;
	private final IBinder mBinder = new BackupBinder();
	private LinkedList<Integer> butaskList = new LinkedList<Integer>();

	private AbstractBackupFile mediaBackup = null;
	private AbstractBackupInfo backupInfo = null;
	private AbstractBackupFile autoBackup = null;
	private boolean buContacts = true;
	private boolean buMedia = true;
	public static Message mediaProMes = null;
	public static Object syncObject;

	public static final int MSG_BACKUP_PROGRESS = 0;
	public static final int MSG_BACKUP_FILE_CHANGED = MSG_BACKUP_PROGRESS+1;
	public static final int MSG_BACKUP_COMPLETE = MSG_BACKUP_FILE_CHANGED+1;
	
	public static final String ERROR_CODE = "ERROR_CODE";
	public static final int ERROR_BACKUP_NO_STORAGE = 0;
	public static final String RESULT_BACKUP = "RESULT_BACKUP";
	public static final String RESULT_BACKEDUP_NUMBER = "RESULT_BACKEDUP_NUMBER";
	
	
	public static String tmpMac = "11:12:12:12:12";
	
	public static final int CODE_BACKUP_NONE = 9;
	public static final int CODE_BACKUP_SUCCESS = 10;
	public static final int CODE_BACKUP_NOTNEED = 11;
	public static final int CODE_BACKUP_CANCEL = 12;
	public static final int CODE_BACKUP_UPLOAD_FAILED = 13;
	public static final int CODE_BACKUP_EXCEPTION = 14;
	public static final int CODE_BACKUP_NO_PERMISSION = 15;
	public static final int CODE_BACKUP_NO_FILE = 16;
	public static final int CODE_BACKUP_IS_USER_STOP = 17;
	public static final int CODE_BACKEDUP_FILE = 18;
	
	public static final int CODE_BACKUP_RECOVER_NONE = 19;
	public static final int CODE_BACKEDUP_RECOVER_FAILED = 20;
	public static final int CODE_BACKEDUP_RECOVER_HAVE_NO_CONTACTS = 21;
	public static final int CODE_BACKEDUP_RECOVER_SUCCESS = 22;
	
	public static final int CODE_BACKEDUP_DELETE_SUCCESS = 23;
	public static final int CODE_BACKEDUP_DELETE_FAILED = 24;
	
	public static final int CODE_BACKEDUP__NO_ENOUGH_SPACE = 25;
	
	
	boolean isUserStop = false;
	
	boolean isBaking = false;
	
	//被选中要恢复的通讯录
	public static ContactsConfig selectedContactsConfig = null;
	

	public enum BuckupType {
		BUTYPE_MEDIA, BUTYPE_CONTACTS,BUTYPE_RECOVER,BUTYPE_DELETE,BUTYPE_AUTO,BUTYPE_ALL
	}
			
	public static ArrayList<BackupDMFile> selectedBackupFilesList;

	public interface BackupFileListener {

		/**
		 * 备份进度获取
		 * 
		 * @param progress
		 *            当前文件进度
		 * @param max
		 *            当前文件总进度值
		 * @param current
		 *            当前文件是第几个文件
		 * @param totalProgress
		 *            总进度
		 * @param totalMax
		 *            总进度值
		 * @param total
		 *            备份的总文件数
		 * @param fileName
		 *            当前备份的文件名
		 */
		public boolean onProgress(long progress, long max, int current,
                                  long totalProgress, long totalMax, int total, String filePath);

		/**
		 * 备份完成后调用
		 * 
		 * @param result
		 *            结果，ture:备份成功，false:未成功
		 * @param errorCode
		 *            错误码，为BackupErrorCode中定义的值
		 * @param msg
		 *            错误信息，一般调试使用，提示细节错误定位
		 * @param msg
		 *            备份了多少个文件
		 *            
		 */
		public void onCompleted(boolean result, int errorCode, long total);
		
		public void onFileChanged(String filePath, long max, int current, long total);
	}
	
	/**
	 * 用这个而回调到activity 里面去
	 */
	private BackupFileListener backupFileListener = new BackupFileListener() {

		@Override
		public boolean onProgress(long progress, long max, int current,
				long totalProgress, long totalMax, int total, String filePath) {
			//if (mediaHandler != null) {
				Message mes = new Message();
				mes.what = MSG_BACKUP_PROGRESS;
				Bundle bundle = new Bundle();
				bundle.putInt(KEY_TOTAL_LEFT, total - current);
				bundle.putLong(KEY_PRO, progress);
				//System.out.println("mediaHandler progress:"+progress);
				bundle.putLong(KEY_MAX, max);
				bundle.putString(KEY_PATH, filePath);
				bundle.putBoolean(KEY_TOTAL_FINISH, false);
				mes.setData(bundle);
				//mediaHandler.sendMessage(mes);
				
				EventBus.getDefault().post(new BackupRefreshEvent(0, mes));
				
			//}else {
			//	System.out.println("mediaHandler null");
			//}
			return isUserStop;
		}

		@Override
		public void onCompleted(boolean result, int errorCode ,long total) {
			// TODO Auto-generated method stub
			//if (mediaHandler != null) {
				Message mes = new Message();
				mes.what = MSG_BACKUP_COMPLETE;
				Bundle bundle = new Bundle();
				bundle.putInt(ERROR_CODE, errorCode);
				bundle.putBoolean(RESULT_BACKUP, result);
				bundle.putLong(RESULT_BACKEDUP_NUMBER, total);
				mes.setData(bundle);
				//mediaHandler.sendMessage(mes);
				EventBus.getDefault().post(new BackupRefreshEvent(0, mes));
			//}
		}

		@Override
		public void onFileChanged(String filePath, long max,int current,long total) {
			// TODO Auto-generated method stub
			//if (mediaHandler != null) {
				Message mes = new Message();
				mes.what = MSG_BACKUP_FILE_CHANGED;
				Bundle bundle = new Bundle();
				bundle.putLong(KEY_MAX, max);
				bundle.putString(KEY_PATH, filePath);
				bundle.putLong(KEY_TOTAL_LEFT, total - current);
				mes.setData(bundle);
			//	mediaHandler.sendMessage(mes);
				EventBus.getDefault().post(new BackupRefreshEvent(0, mes));
			//}
		}

	
	};
	
	//备份通讯录的回调
	private BackupInfoListener backupInfoListener = new BackupInfoListener (){

		@Override
		public boolean onProgress(long progress, long max) {
			// TODO Auto-generated method stub
			System.out.println("test123 progress:"+progress);
			//if (contactsHandler != null) {
				Message mes = new Message();
				mes.what = MSG_BACKUP_PROGRESS;
				Bundle bundle = new Bundle();
				bundle.putLong(KEY_PRO, progress);
				bundle.putLong(KEY_MAX, max);
				mes.setData(bundle);
				//contactsHandler.sendMessage(mes);
				EventBus.getDefault().post(new BackupRefreshEvent(1, mes));
			//}
			return isUserStop;
		}

		@Override
		public void onCompleted(boolean result, int errorCode ,
				long total) {
			// TODO Auto-generated method stub
			//if (contactsHandler != null) {
				Message mes = new Message();
				mes.what = MSG_BACKUP_COMPLETE;
				Bundle bundle = new Bundle();
				bundle.putInt(ERROR_CODE, errorCode);
				bundle.putBoolean(RESULT_BACKUP, result);
				bundle.putLong(RESULT_BACKEDUP_NUMBER, total);
				mes.setData(bundle);
				//contactsHandler.sendMessage(mes);
				EventBus.getDefault().post(new BackupRefreshEvent(1, mes));
			//}
		}
		
	};
	
	
	private class BUThread extends Thread {
		private BuckupType mBuckupType;

		public BUThread(BuckupType type) {
			this.mBuckupType = type;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			switch (mBuckupType) {
			case BUTYPE_CONTACTS:
				 backupCONTACTInfo();
				break;
			case BUTYPE_MEDIA:
				backupMediaDir();
				break;
			case BUTYPE_AUTO:
				autoBackupMediaDir();
				break;
			case BUTYPE_RECOVER:
				recoverCONTACTInfo();
				break;
			case BUTYPE_DELETE:
				deleteCONTACTInfo();
				break;
			case BUTYPE_ALL:
				if (!isBaking) {
					
					isBaking = true;
					EventBus.getDefault().post(new BackupStateEvent(BackupStateEvent.BACKING));
					
					autoBackupAll();
					
					isBaking = false;
					EventBus.getDefault().post(new BackupStateEvent(BackupStateEvent.FINISHED));
				}
				break;
			}
			DMSdk.getInstance().syncSystem();
		}
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		Log.d("BackupService", "onCreate");
		butaskList.offer(BUTYPE_BU_CONTACTS);
		butaskList.offer(BUTYPE_MEDIA);
		syncObject = new Object();
		EventBus.getDefault().register(this);
	}


	public class BackupBinder extends Binder {
		public BackupService getService() {
			return BackupService.this;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return mBinder;
	}

	@Override
	public void unbindService(ServiceConnection conn) {
		// TODO Auto-generated method stub
		super.unbindService(conn);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		Log.d(Tag, "onstartCommand");
		isUserStop = false;
		TransTools.isUserStop = false;
		if(intent != null && intent.getExtras() != null) {
			int type = intent.getExtras().getInt("backUp_Type", -1);

			if (type != -1) {
				buThread = new BUThread(BuckupType.values()[type]);
				buThread.start();
			}
		}
		return Service.START_NOT_STICKY;
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		EventBus.getDefault().unregister(this);
		stopBackup();
		Log.d(Tag, "onDestroy");
	}

	
	private void deleteCONTACTInfo() {
		BakSetBean bsBean = BackupSettingDB.getInstance().getDiskBakSetting(tmpMac);
		if(bsBean == null) {
			backupInfoListener.onCompleted(false,ERROR_BACKUP_NO_STORAGE, 0);
			return;
		}
		backupInfo = BackupInfoFactory.getInstance(
				BackupInfoType.CONTACTS, BackupService.this,
				backupInfoListener);
		
		try {
			backupInfo.delete(selectedContactsConfig);
		} catch (Exception e) {
			backupInfoListener.onCompleted(false,CODE_BACKEDUP_DELETE_FAILED, 0);
		}
		
	}
	
	private void recoverCONTACTInfo() {
		BakSetBean bsBean = BackupSettingDB.getInstance().getDiskBakSetting(tmpMac);
		if(bsBean == null) {
			backupInfoListener.onCompleted(false,ERROR_BACKUP_NO_STORAGE, 0);
			return;
		}
		backupInfo = BackupInfoFactory.getInstance(
				BackupInfoType.CONTACTS, BackupService.this,
				backupInfoListener);
		
		try {
			backupInfo.recover(RecoverMode.COVER, selectedContactsConfig);
		} catch (Exception e) {
			backupInfoListener.onCompleted(false,CODE_BACKUP_UPLOAD_FAILED, 0);
		}
		
	}
	
	private void backupCONTACTInfo() {
		BakSetBean bsBean = BackupSettingDB.getInstance().getDiskBakSetting(tmpMac);
		if(bsBean == null) {
			backupInfoListener.onCompleted(false,ERROR_BACKUP_NO_STORAGE, 0);
			return;
		}
		backupInfo = BackupInfoFactory.getInstance(BackupInfoType.CONTACTS, BackupService.this,backupInfoListener);
		
		try {
			 backupInfo.backup();
		} catch (Exception e) {
			backupInfoListener.onCompleted(false,CODE_BACKUP_UPLOAD_FAILED, 0);
		}
		
	}

	/**
	 * 备份相册和视频
	 * 
	 * @return
	 */
	private void backupMediaDir() {
		BackupSettingDB mBackupSettingDB = BackupSettingDB.getInstance();
		BakSetBean bsBean = mBackupSettingDB.getDiskBakSetting(tmpMac);
		if (bsBean == null || selectedBackupFilesList == null 
				|| selectedBackupFilesList.size() <= 0) {
			backupFileListener.onCompleted(false, -1,0);
			return;
		}
		mediaBackup = new BackupMediaImpl(BackupService.this);
		mediaBackup.setBackupFileListener(backupFileListener);
		mediaBackup.setBackupFilesList(selectedBackupFilesList);
		try {
			mediaBackup.backup();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private int autoBackupMediaDir() {
		if (selectedBackupFilesList == null || selectedBackupFilesList.size() <= 0) {
			backupFileListener.onCompleted(false, CODE_BACKUP_NO_FILE,0);
			return 0;
		}
		
		autoBackup = new AutoMediaImpl(BackupService.this);
		autoBackup.setBackupFileListener(backupFileListener);
		autoBackup.setBackupFilesList(selectedBackupFilesList);
		try {
			return autoBackup.backup();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}
	
	public void autoBackupAll() {
		
		boolean backup_album = getSharedPreferences("BACKUP", MODE_PRIVATE).getBoolean("ALBUM", false);
		boolean backup_contact = getSharedPreferences("BACKUP", MODE_PRIVATE).getBoolean("CONTACTS", false);
		
		int ret_album = 0;
		
		if (backup_album ) {
			
			BaseValue.backing_album = true;
			
			List<String> mFolders = getBackupFodlers();
			
			if (mFolders.size() > 0) {
				boolean bakImage = getSharedPreferences(AlbumBackupActivity.PREFERENE_NAME, Context.MODE_PRIVATE).getBoolean(AlbumBackupActivity.KEY_IMAGE, true);
				boolean bakVideo = getSharedPreferences(AlbumBackupActivity.PREFERENE_NAME, Context.MODE_PRIVATE).getBoolean(AlbumBackupActivity.KEY_VIDEO, false);
				
				selectedBackupFilesList = getBackupFiles(mFolders,bakImage,bakVideo);
				
				ret_album = autoBackupMediaDir();
			}
			
			BaseValue.backing_album = false;
		}
		
		if (backup_contact && !isUserStop && ret_album != -2) {
			
			BaseValue.backing_contacts = true;
			backupCONTACTInfo();
			BaseValue.backing_contacts = false;
		}
	}
	
	public void onEventMainThread(BackupStateEvent event){
		if (event.mState == BackupStateEvent.CANCLE) {
			stopBackup();
		}
	}
	
	public void onEventMainThread(DisconnectEvent event){
		stopBackup();
	}
	
	public void stopBackup() {
	
		System.out.println("stopBackup stopBackup");
		
		if(buThread != null && buThread.isAlive()) {
			buThread.interrupt();
			buThread = null;
		}
		
		if (autoBackup != null) {
			autoBackup.cancel();
		}
		
		if(mediaBackup != null) {
			mediaBackup.cancel();
		}
		if(backupInfo != null) {
			backupInfo.cancel();
		}
		//停止 這個後期可以做成統一的一個接口
		isUserStop = true;
		TransTools.isUserStop = false;
	}
	
	private List<String> getBackupFodlers(){
		
		List<String> mFolders;
		Set<String> set = getSharedPreferences(AlbumBackupActivity.PREFERENE_NAME, Context.MODE_PRIVATE).getStringSet(AlbumBackupActivity.KEY_FOLDERS, null);
		if (set == null) {
			mFolders = new ArrayList<>();
		}else{
			
			Iterator<String> it = set.iterator();
			while (it.hasNext())
			{
			    System.out.println("aa 1:" + it.next());
			}
			
			mFolders = new ArrayList<>(set);
			
			System.out.println("aa 2: "+mFolders.size());
		}
		
		return mFolders;
	}
	
	private ArrayList<BackupDMFile> getBackupFiles(List<String> folders, boolean bakImage, boolean bakVideo) {
		// TODO Auto-generated method stub
		ArrayList<BackupDMFile> files = new ArrayList<>();
		
		for (String path : folders) {
			
			File dir = new File(path);
			
			File[] tmp = dir.listFiles();
			if (tmp != null && tmp.length > 0) {
				for (File file : tmp) {
					DMFileCategoryType type = DMFileTypeUtil.getFileCategoryTypeByName(file.getName());
					if (type == DMFileCategoryType.E_PICTURE_CATEGORY) {
						if (bakImage) {
							BackupDMFile back = new BackupDMFile();
							back.mName = file.getName();
							back.mPath = file.getPath();
							back.mLocation = DMFile.LOCATION_LOCAL;
							back.mLastModify = file.lastModified();
							back.mSize = file.length();
							back.mType = type;
							if (back.mSize > 1 * 1024) {
								files.add(back);
							}
							
						}
					}else if (type == DMFileCategoryType.E_VIDEO_CATEGORY) {
						if (bakVideo) {
							BackupDMFile back = new BackupDMFile();
							back.mName = file.getName();
							back.mPath = file.getPath();
							back.mLocation = DMFile.LOCATION_LOCAL;
							back.mLastModify = file.lastModified();
							back.mSize = file.length();
							back.mType = type;
							files.add(back);
						}
					}
				}
			}
		}
		return filterBackupFiles(files);
	}

	private ArrayList<BackupDMFile> filterBackupFiles(ArrayList<BackupDMFile> files) {
		// TODO Auto-generated method stub
		ArrayList<BackupDMFile> ret = new ArrayList<>();
		ArrayList<String> infos = new ArrayList<>();
		
		for (BackupDMFile file : files) {
			String uuid = FileOperationHelper.getInstance().generateFileId(file);
			//System.out.println("filterBackupFiles:"+uuid);
			infos.add(uuid);
		}
		
		List<String>uuids = DMSdk.getInstance().filterBackupFiles((String[]) infos.toArray(new String[infos.size()]));
		if (uuids != null) {
			System.out.println("uuid size:"+uuids.size());
			
			for (BackupDMFile file:files) {
				
				String id = FileOperationHelper.getInstance().generateFileId(file);
				if (uuids.contains(id)) {
					ret.add(file);
				}
			}
		}
		
		return ret;
	}

	

}
