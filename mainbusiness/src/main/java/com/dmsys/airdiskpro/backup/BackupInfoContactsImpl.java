package com.dmsys.airdiskpro.backup;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabaseCorruptException;

import com.dmsys.airdiskpro.db.BackupCLogDBManager;
import com.dmsys.airdiskpro.model.ContactInfo;
import com.dmsys.airdiskpro.model.ContactsConfig;
import com.dmsys.airdiskpro.model.LogContactBean;
import com.dmsys.airdiskpro.service.BackupService;
import com.dmsys.airdiskpro.utils.CConfigManager;
import com.dmsys.airdiskpro.utils.ContactHandler;
import com.dmsys.airdiskpro.utils.DBToInfoHandler;
import com.dmsys.airdiskpro.utils.TransTools;
import com.dmsys.dmsdk.DMSdk;
import com.dmsys.dmsdk.model.DMDelete;
import com.dmsys.dmsdk.model.DMDownload;
import com.dmsys.dmsdk.model.DMRet;
import com.dmsys.dmsdk.model.DMUpload;
import com.dmsys.dmsdk.model.Request.OnProgressChangeListener;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @ClassName: BackupContactsImpl
 * @Description: 联系人备份
 * @author: yoy
 * @date: 2014年9月10日 下午5:05:46 version:1.0.0
 */
public class BackupInfoContactsImpl extends AbstractBackupInfo {
	private static final String TAG = "BackupContactsImpl";
	String mRemotePath = "";
	String mRemoteFolderName = "";
	private Context mContext;
	private String fileName = null;
	private boolean isBakToDB = true;
	private boolean throughNas = true;
//	private BackupInfoInputStream bakInputStream = null;
	private InputStream recoverInputStream = null;
	private String backupFilePath;
	private ContactsConfig mCConfig = null;
	public static boolean State_Bu_Contacts = false;
	private boolean mStopped = false;

	public BackupInfoContactsImpl(Context context, BackupInfoListener backupListener) {
		super(context, backupListener);
		// TODO Auto-generated constructor stub
		this.mContext = context;
		final ContactHandler contactHandler = ContactHandler.getInstance();
		contactHandler.setBackupListener(getBackupInfoListener());
	}

	/**
	 * 函数概要：完整的备份
	 * 
	 * 实现：读取手机数据库 - 生成xml - 备份文件输出到本地 - 获取本次备份的基础信息 -
	 * 使用备份文件的InputStream和基础信息调用网络层saveBackup函数
	 * @throws IOException 
	 */
	@Override
	public int backup() throws Exception {
		// TODO Auto-generated method stub
		
		ContactHandler ch = ContactHandler.getInstance();
		ch.setProIndex(0);
		ch.isUserStop = false;
		/**
		 *  从手机通讯录获取联系人信息
		 */
		
		List<ContactInfo> infos = ch.getContactInfo(getContext(), false);
		
		if (infos == null || infos.size() <= 0) {
			getBackupInfoListener().onCompleted(false, BackupService.CODE_BACKUP_NO_FILE,0);
			return 0;
		}
		//这一个手机的通讯录的MD5值
		String totalContactsMD5 = ch.makeTotalMD5(infos);
		//检查disk 里面的desc.db 配置文件 看下里面的MD5值 和本手机的通讯录算出来的md5值是否一样
		if (isMd5OnRemote(totalContactsMD5)) {
			System.out.println("liutao exist");
			getBackupInfoListener().onCompleted(false, BackupService.CODE_BACKEDUP_FILE,0);
			return 0;
		}
		//存储手机的名字 当前的时间 md5 联系人的数目  
		mCConfig = buildConfig(infos.size(), totalContactsMD5);
		fileName = mCConfig.getTime() + TransTools.POINT_DB;
		backupFilePath = TransTools.CONTACTS_DB_MAIN_PATH;
		//存储配置信息（手机的名字 当前的时间 md5 联系人的数目 ） 到disk下载下来的desc.DB上，等下准备上传
		addConfigToDB(mContext, mCConfig);
		//把手机上的通讯录联系人数据全部打包到一个数据库.db上去 放在指定的sd卡位置 ，这里跑了进度
		ch.backUpContactDataToDb(mContext, infos, backupFilePath, fileName, "1");
		/**
		 * 上传通讯录的db库
		 */
		String remoteParentPath = TransTools.getC_BACKUP_REMOTE_PARENT_PATH();
		int index1 = remoteParentPath.lastIndexOf("/");
		String remoteFolderUrl1 = remoteParentPath.substring(0,index1 + 1);
		//用户中断
		TransTools.ensureRemoteDirStruct(remoteFolderUrl1);
		
		DMUpload task1 = new DMUpload(backupFilePath + fileName,
				remoteParentPath + fileName, 
				new OnProgressChangeListener() {

					@Override
					public int onProgressChange(int uid, long total,
							long already) {
						// TODO Auto-generated method stub
						int ret = 0;
						if (getBackupInfoListener() != null) {
							long progress = (80 + (int) ((already*1.0/total)*(90-80)));
							mStopped = getBackupInfoListener().onProgress(progress, 100L);
							if (mStopped) {
								ret = -1;
							}
						}
						return ret;
					}
				});
		int up = DMSdk.getInstance().upload(task1);
		int ret = -1;
		if(!mStopped && up == DMRet.ACTION_SUCCESS) {
			/**
			 * 上传配置文件 ,建立disk 的文件夹
			 */
			uploadDescDB(90,100);
			
			if(mStopped)  {
				ret = BackupService.CODE_BACKUP_UPLOAD_FAILED;
			} else {
				saveSummaryInfo(mContext, infos.size());
				ret = BackupService.CODE_BACKUP_SUCCESS;
			}
		} else {
			ret = BackupService.CODE_BACKUP_UPLOAD_FAILED;
		} 
		getBackupInfoListener().onCompleted(ret == BackupService.CODE_BACKUP_SUCCESS, ret,infos.size());
		return 0;
	}

	
	private void uploadDescDB(final int startPos,final int endPos) throws Exception {
		String remotePath = TransTools.getC_CONFIG_REMOTE_PATH();
		int index = remotePath.lastIndexOf("/");
		String remoteFolderUrl = remotePath.substring(0,index + 1);
		TransTools.ensureRemoteDirStruct(remoteFolderUrl);
		
		DMUpload task = new DMUpload(TransTools.C_CONFIG_LOCAL_PATH,remotePath,
				new OnProgressChangeListener() {

					@Override
					public int onProgressChange(int uid, long total,
							long already) {
						// TODO Auto-generated method stub
						int ret = 0;
						if (getBackupInfoListener() != null) {
							long progress = (startPos + (int) ((already*1.0/total)*(endPos-startPos)));
							mStopped = getBackupInfoListener().onProgress(progress, 100L);
							if (mStopped) {
								ret = -1;
							}
						}
						return ret;
					}
				});
		DMSdk.getInstance().upload(task);
	}
	
	/**
	 * 保存备份的概要信息
	 */
	private boolean saveSummaryInfo(Context context, int num) {
		// 将本次备份信息保存到备份记录数据库
		BackupCLogDBManager logDBManager = null;
		try {
			logDBManager = new BackupCLogDBManager(context, TransTools.CONTACTS_DB_FOLDER_PATH, TransTools.C_BAKLOG_DBNAME);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		LogContactBean contactBean = new LogContactBean(num, logDBManager.getContactLastBakNode() + 1, System.currentTimeMillis());
		logDBManager.addContactLog(contactBean);
		logDBManager.closeDB();
		return true;
		/*
		 * logDBManager.addCallLog(new LogCallBean(bakup_totalnum, callin_num,
		 * callout_num, missedcall_num, firstRecordTime, lastRecordTime,
		 * lastLogCallBean.node+1, System.currentTimeMillis()));
		 */
	}
	

	private ContactsConfig buildConfig(int num, String md5) {
		long time = System.currentTimeMillis();
		String model = android.os.Build.MODEL;
		return new ContactsConfig(time, model, num, md5);
	}



	@Override
	public void pause() {
		// TODO Auto-generated method stub

	}

	@Override
	public void cancel() {
		// TODO Auto-generated method stub
		ContactHandler.getInstance().isUserStop = true;
	}

	private boolean initLocalConfigDB() throws IOException   {
		CConfigManager manager = new CConfigManager();
		if (manager.hasRemoteConfig()) {
				if (!manager.downLoadConfig())
					return false;
		} else {
			manager.buildConfig(mContext);
		}
		return true;
	}

	private void addConfigToDB(Context context, ContactsConfig config) throws SQLiteDatabaseCorruptException, IOException {
		CConfigManager manager = new CConfigManager();
		manager.insertNewConfig(context, config);
	}
	
	private void deleteConfigToDB(Context context, ContactsConfig config) throws SQLiteDatabaseCorruptException, IOException {
		CConfigManager manager = new CConfigManager();
		manager.deleteConfig(context, config);
	}

	@Override
	public BackupInfoStatus getBackupInfoStatus() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * 函数概要：点击BackupDscreptionList一项之后，完整的恢复
	 * 
	 * 调用者状态：点击了BackupDscreptionList获取一项
	 * 实现：调用网络层getInputStreamOfBackup函数获取备份文件InputStream - 用逻辑输出到本地 - 解析 - 恢复到手机
	 * @throws InterruptedException 
	 */
	@Override
	public void recover(final RecoverMode recoverMode, final AbstractBackupInfoDscreption backupDscreption) throws InterruptedException {
		// TODO Auto-generated method stub
		ContactsConfig cConfig = (ContactsConfig) backupDscreption;
		String remoteBUName = cConfig.getTime() + TransTools.POINT_DB;
		ContactHandler ch = ContactHandler.getInstance();
		ch.setProIndex(0);
		ch.isUserStop = false;
		/**
		 * 下载对应的time.db 
		 */
		String remoteDbUrl = TransTools.getC_BACKUP_REMOTE_PARENT_PATH() + remoteBUName;
		String localDbParentPath = TransTools.CONTACTS_DB_MAIN_PATH ;
		String localDbPath = localDbParentPath+remoteBUName;
		DMDownload task = new DMDownload(remoteDbUrl, localDbParentPath,  new OnProgressChangeListener() {
			
			@Override
			public int onProgressChange(int uid, long total, long already) {
				// TODO Auto-generated method stub
					int ret = 0;
					long progress =  (int) ((already*1.0/total)*(20-0));
					mStopped = getBackupInfoListener().onProgress(progress, 100L);
					if (mStopped) {
						ret = -1;
					}
				return ret;
			}
		});
		
		DMSdk.getInstance().download(task);
			
		//把刚刚下载DB里面的数据（联系人）取出来
		ArrayList<ContactInfo> contacts = null;
		DBToInfoHandler dbToInfoHandler = DBToInfoHandler.getInstance();
		dbToInfoHandler.setBackupListener(getBackupInfoListener());
		dbToInfoHandler.setDbToContastsMax(30);
		dbToInfoHandler.setProIndex(20);
		contacts = dbToInfoHandler.getContactInfos(localDbPath);
		int ret = BackupService.CODE_BACKUP_RECOVER_NONE;
		if (contacts == null) {
			ret = BackupService.CODE_BACKEDUP_RECOVER_HAVE_NO_CONTACTS;
		} else {
			if (recoverMode == RecoverMode.COVER) {
				
				int status = ch.CopyAll2Phone(contacts, getContext(), true, 50, 100);
				
				if(status == ContactHandler.COPY_COED_HANE_NO_CONTACTS) {
					ret = BackupService.CODE_BACKEDUP_RECOVER_HAVE_NO_CONTACTS;
				} else {
					ret = BackupService.CODE_BACKEDUP_RECOVER_SUCCESS;
				}
				
			} 
		}
		if(ret == BackupService.CODE_BACKEDUP_RECOVER_SUCCESS) {
			getBackupInfoListener().onCompleted(true,ret, contacts.size());
		} else {
			getBackupInfoListener().onCompleted(false,ret, 0);
		}
	}
	
	@Override
	public void delete(AbstractBackupInfoDscreption backupDscreption)
			throws Exception {
		// TODO Auto-generated method stub
		
		
		//删除本地的desc.db文件
		ContactsConfig cConfig = (ContactsConfig) backupDscreption;
		String remoteBUName = cConfig.getTime() + TransTools.POINT_DB;
		
		String localPath = TransTools.C_CONFIG_LOCAL_PATH;
		File file = new File(localPath);
		if(file == null || !file.exists()) {
			getBackupInfoListener().onCompleted(false, BackupService.CODE_BACKEDUP_DELETE_FAILED, 0);
			return;
		} 
		//删除已经下载在本地的desc.db文件里面的数据 
		deleteConfigToDB(mContext,cConfig);
		//再上传上去
		uploadDescDB(0,80);
		if(!mStopped) {
			String remoteDbUrl = TransTools.getC_BACKUP_REMOTE_PARENT_PATH() + remoteBUName;
			//bOK = DMSdk.getInstance().delete(remoteDbUrl);
			int bOK = DMSdk.getInstance().delete(new DMDelete(remoteDbUrl,new OnProgressChangeListener() {
				
				@Override
				public int onProgressChange(int uid, long total, long already) {
					// TODO Auto-generated method stub
					return 0;
				}
			}));
			if(bOK == DMRet.ACTION_SUCCESS) {
				getBackupInfoListener().onProgress(100, 100L);
				getBackupInfoListener().onCompleted(true,BackupService.CODE_BACKEDUP_DELETE_SUCCESS,cConfig.getContactsNum());
			} else {
				getBackupInfoListener().onCompleted(false,BackupService.CODE_BACKEDUP_DELETE_FAILED,0);
			}
		} else {
			getBackupInfoListener().onCompleted(false,BackupService.CODE_BACKEDUP_DELETE_FAILED,0);
		}
	}
	

	@Override
	public List<? extends AbstractBackupInfoDscreption> getBackupInfoDscreptionList() throws Exception {
		// TODO Auto-generated method stub
		CConfigManager manager = new CConfigManager();
		if (manager.hasRemoteConfig()) {
			if (!manager.downLoadConfig())
				return new ArrayList<ContactsConfig>();
		} else {
			
			return new ArrayList<ContactsConfig>();
		}

		return manager.getConfigList(mContext);
	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub

	}

	private void closeInputStream(InputStream inputStream) {
		try {
			if (inputStream != null)
				inputStream.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	@Override
	public boolean getBackupState() {
		// TODO 王健军
		// return GlobalFiled.settingInfo.getBakContactStatus();
		return true;
	}

	@Override
	public void setBackupState(boolean state) {
		// TODO 王健军
		// GlobalFiled.settingInfo.setBakContactStatus(state);
	}

	private boolean isMd5OnRemote(String md5) {
		List<ContactsConfig> ccList = null;
		try {
			ccList = (List<ContactsConfig>) getBackupInfoDscreptionList();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (ccList != null) {
			for (ContactsConfig cc : ccList) {
				if (cc.getMD5().equals(md5)) {
					return true;
				}
			}
		}
		return false;
	}

	

	private void sendUpdateBUStateBroadcast() {
		Intent intent = new Intent(BackupService.State_Update_Action);
		mContext.sendBroadcast(intent);
	}


}
