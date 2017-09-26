package com.dmsys.airdiskpro.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabaseCorruptException;

import com.dmsys.airdiskpro.db.CConfigDBManager;
import com.dmsys.airdiskpro.model.ContactsConfig;
import com.dmsys.airdiskpro.utils.GetBakLocationTools.MyBoolean;
import com.dmsys.dmsdk.DMSdk;
import com.dmsys.dmsdk.model.DMDownload;
import com.dmsys.dmsdk.model.Request.OnProgressChangeListener;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class CConfigManager {

	public boolean  mStopped = false;
	public CConfigManager()
	{
		
	}
	
	public boolean hasRemoteConfig() throws IOException
	{
		String configPath = getC_CONFIG_REMOTE_PATH(); 
		return TransTools.existRemoteFile(configPath);
	}
	
	public boolean downLoadConfig() throws IOException
	{
		String configUrl = getC_CONFIG_REMOTE_PATH(); 
		//不用加文件名
		String localPath = TransTools.CONTACTS_DB_MAIN_PATH;
		
		//建立好文件夹
		StreamTool.ensureFilePathExist(localPath);
		
		
		File file = new File(TransTools.C_CONFIG_LOCAL_PATH);
		if(file != null && file.exists()) {
			file.delete();
		}
		DMDownload task = new DMDownload(configUrl, localPath, new OnProgressChangeListener() {
			
			@Override
			public int onProgressChange(int uid, long total, long already) {
				// TODO Auto-generated method stub
				int ret = 0;
					if (mStopped) {
						ret = -1;
					}
				return ret;
			}
		});
		
		DMSdk.getInstance().download(task);
		
		return true;
	}
	
	public void buildConfig(Context context) throws IOException
	{
		CConfigDBManager manager= new CConfigDBManager(context,TransTools.CONTACTS_DB_MAIN_PATH,TransTools.C_CONFIG_DBNAME);
		manager.closeDB();
	}
	
	public void insertNewConfig(Context context , ContactsConfig config) throws SQLiteDatabaseCorruptException, IOException
	{
		CConfigDBManager manager= new CConfigDBManager(context,TransTools.CONTACTS_DB_MAIN_PATH,TransTools.C_CONFIG_DBNAME);
		try {
			manager.insertConfigLog(config.getTime(), config.getPhoneModel(), config.getContactsNum(),config.getMD5());
		} catch (SQLiteDatabaseCorruptException e) {
			// TODO: handle exception
			throw new SQLiteDatabaseCorruptException("db error");
		} finally{
			manager.closeDB();
		}
	}
	public void deleteConfig(Context context , ContactsConfig config) throws SQLiteDatabaseCorruptException, IOException
	{
		CConfigDBManager manager= new CConfigDBManager(context,TransTools.CONTACTS_DB_MAIN_PATH,TransTools.C_CONFIG_DBNAME);
		try {
			manager.deleteConfig(config.getMD5());
		} catch (SQLiteDatabaseCorruptException e) {
			// TODO: handle exception
			throw new SQLiteDatabaseCorruptException("db error");
		} finally{
			manager.closeDB();
		}
	}
	
	
	public ArrayList<ContactsConfig> getConfigList(Context context) throws IOException
	{
		ArrayList<ContactsConfig> res = null;
		CConfigDBManager manager= new CConfigDBManager(context,TransTools.CONTACTS_DB_MAIN_PATH,TransTools.C_CONFIG_DBNAME);
		res = manager.selectConfig();
		manager.closeDB();
		return res;
	}

	public static String getC_CONFIG_REMOTE_PATH() throws IOException {
		MyBoolean tempB = new MyBoolean();
		
		String diskName = GetBakLocationTools.getBakDiskName(tempB);
		if(diskName == null)
			throw new IOException("diskName == null");
		return TransTools.DeviceRootUrl + diskName +File.separator + ".dmApp" + File.separator
				+ "ContactsBackup" + File.separator + TransTools.C_CONFIG_DBNAME;

	}
}
