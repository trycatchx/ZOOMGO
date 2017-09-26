package com.dmsys.airdiskpro.backup;

import android.content.Context;

import com.dmsys.airdiskpro.model.BackupInfoType;

/**
 * 
 * @ClassName:  BackupFactory   
 * @Description:   共上层调用，获取实例
 * @author: yoy  
 * @date:   2014年9月10日 下午5:55:29   
 * version:1.0.0
 */
public class BackupInfoFactory {
	

	public static AbstractBackupInfo getInstance(BackupInfoType backupInfoType,Context context,BackupInfoListener backupListener)
	{
		if(backupInfoType == BackupInfoType.SMS)
		{
//			return new BackupInfoSMSImpl(context, backupListener);
		}
		else if(backupInfoType == BackupInfoType.CONTACTS)
		{

			return new BackupInfoContactsImpl(context, backupListener);
		}
		else if(backupInfoType == BackupInfoType.CALL)
		{
//			return new BackupInfoCallImpl(context, backupListener);
		}
		
		return null;
	}
}
