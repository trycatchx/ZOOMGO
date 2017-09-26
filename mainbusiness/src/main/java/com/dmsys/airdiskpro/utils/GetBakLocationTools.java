package com.dmsys.airdiskpro.utils;

import android.content.Context;

import com.dmsys.airdiskpro.db.BackupSettingDB;
import com.dmsys.airdiskpro.filemanager.FileOperationHelper;
import com.dmsys.airdiskpro.model.BakSetBean;
import com.dmsys.airdiskpro.service.BackupService;
import com.dmsys.dmsdk.DMSdk;
import com.dmsys.dmsdk.model.DMFile;
import com.dmsys.dmsdk.model.DMStorageInfo;
import com.dmsys.mainbusiness.R;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 本交互参见：《备份交互V2的数据库实现和业务逻辑实现 2015.3.18》
 *
 */
public class GetBakLocationTools {

	public static class MyBoolean {
		public boolean value;
	}

	/**
	 * 获取备份需要使用的盘符名称
	 * 本方法只可以在备份实现类BackupMediaImpl或BackupInfoContactsImpl中调用，因为要保证TransTools
	 * .first_remote_disk不为空
	 * 
	 * @return
	 * @param return_usedNewDisk
	 *            对于媒体备份 获取备份的盘符时是否使用了新盘符
	 */
	public static String getBakDiskName(MyBoolean return_usedNewDisk) {
//数据库获取之前保存的disk 的name
		BakSetBean bean = BackupSettingDB.getInstance().getDiskBakSetting(
				BackupService.tmpMac);

		// BakSetBean bean = manager.getDiskBakSetting(StaticVariate.mac);
		if (bean == null) {
			return null;
		}

		boolean exist = false;
		DMStorageInfo info = DMSdk.getInstance().getStorageInfo();
	
		if(info == null || info.getStorages() == null || info.getStorages().size() <= 0) return null;
		
		for (int i = 0; i < info.getStorages().size(); i++) {
			if (String.valueOf(info.getStorages().get(i).total)
					.equals(bean.allStorage)) {
				exist = true;
				break;
			}
		}

		// 保证bean.diskName确实存在,避免数据库中存在错误数据
		if (exist) {
			boolean innerE = false;
			for (int i = 0; i < info.getStorages().size(); i++) {
				if (info.getStorages().get(i).mName.equals(bean.diskName)) {
					exist = true;
					innerE = true;
					break;
				}
			}
			if (innerE == false) {
				exist = false;
			}
		}

		if (exist) {
			return_usedNewDisk.value = false;
			return bean.diskName;
		} else {
			return_usedNewDisk.value = true;
			bean.diskName = info.getStorages().get(0).mName;
			bean.allStorage = String.valueOf(info.getStorages().get(0).total);
			BackupSettingDB.getInstance().updateDiskMac(bean);
			return bean.diskName;
		}
	}

	/**
	 * 获取备份需要使用的媒体文件夹名字（盘符下的） 本方法默认使用的远端diskName存在
	 * 本方法只可以在备份实现类BackupMediaImpl或BackupInfoContactsImpl中调用
	 * ，因为要保证TransTools.first_remote_disk不为空
	 * 
	 * @param usedNewDisk
	 *            为true意味着 要直接使用方法创建新folder，并更新mac表，返回folder
	 * @throws IOException 
	 * 
	 */
	public static String getBakMediaFolderName(Context context,boolean usedNewDisk) throws InterruptedException {
	
		if (usedNewDisk) {
			// new disk 直接创建新folder
			
			BakSetBean bean = BackupSettingDB.getInstance().getDiskBakSetting(BackupService.tmpMac);
			if (bean == null) {
				return null;
			}
			bean.media_bak_folder = getNewMediaBakFolder(context,bean.diskName);
			if (bean.media_bak_folder == null)
				return null;

			BackupSettingDB.getInstance().updateDiskMac(bean);
			// http://192.168.222.254/wavdav/udisk-4/backup floder/honer 6
			String newFolderUrl = TransTools.DeviceRootUrl + bean.diskName
					+ File.separator + bean.media_bak_folder;
			
			TransTools.ensureRemoteDirStruct(newFolderUrl);
			return bean.media_bak_folder;
		} else {

			
			BakSetBean bean =  BackupSettingDB.getInstance().getDiskBakSetting(BackupService.tmpMac);
			String dbMFolderUrl = TransTools.DeviceRootUrl + bean.diskName
					+ File.separator + bean.media_bak_folder;

			boolean exist = false;
//			dbMFolderUrl = FileInfoUtils.encodeUri(dbMFolderUrl);
			exist = TransTools.existRemoteFile(dbMFolderUrl);
			if (exist) {
				return bean.media_bak_folder;
			} else {
				bean.media_bak_folder = getNewMediaBakFolder(context,bean.diskName);
				if (bean.media_bak_folder == null)
					return null;
				BackupSettingDB.getInstance().updateDiskMac(bean);
				String newFolderUrl = TransTools.DeviceRootUrl + bean.diskName
						+ File.separator + bean.media_bak_folder;
						TransTools.ensureRemoteDirStruct(newFolderUrl);
				return bean.media_bak_folder;
			}

		}

	}

	/**
	 * 默认mac表中的disk必定存在
	 * 
	 * @return 一个在远端folder的位置必定不存在的文件夹名，规则：后面加递增数字的形式
	 * @throws IOException 
	 */
	public static String getNewMediaBakFolder(Context context,String diskName) throws InterruptedException {
//		String dbMFolderUrl = TransTools.DeviceRootUrl + diskName;
		String dbMFolderUrl =  diskName ;
				
		// 建立好目录
		TransTools.ensureRemoteDirStruct(dbMFolderUrl);
	
		List<DMFile> xlfiles = null;
			
		xlfiles = FileOperationHelper.getInstance().getUdiskFolderAllFiles(dbMFolderUrl);
			
		List<String> myNameList = getMyFolderNamesList(xlfiles,String.format(context.getString(R.string.DM_Back_From), android.os.Build.MODEL));
		String myName = buildMyFolderName(context,myNameList);
		return myName;
	}
	
	/**
	 * 在xlfiles中筛选出包含myName字符串的部分
	 * 
	 * @param xlfiles
	 * @param myName
	 * @return
	 */
	private static List<String> getMyFolderNamesList(List<DMFile> xlfiles,
			String myName) {
		List<String> res = new ArrayList<String>();
		if (xlfiles == null)
			return res;
		for (int i = 0; i < xlfiles.size(); i++) {
			if (xlfiles.get(i).getName().contains(myName)) {
				res.add(xlfiles.get(i).getName());
			}
		}
		return res;
	}

	/**
	 * 根据远端包含本机机型的文件夹名 build一个新的文件夹名
	 * 
	 * @param remoteNames
	 * @return
	 */
	private static String buildMyFolderName(Context context,List<String> remoteNames) {
		if (remoteNames == null || remoteNames.size() == 0) {
			return String.format(context.getString(R.string.DM_Back_From), android.os.Build.MODEL);
		}
		boolean hasNum = false;
		for (int i = 0; i < remoteNames.size(); i++) {
			if (remoteNames.get(i).charAt(remoteNames.get(i).length() - 1) == ')') {
				hasNum = true;
				break;
			}
		}
		if (!hasNum) {
			return String.format(context.getString(R.string.DM_Back_From), android.os.Build.MODEL + "(" + 1 + ")");
		}

		int maxNum = 1;
		for (int i = 0; i < remoteNames.size(); i++) {
			int num = getTailNum(remoteNames.get(i));
			if (num > maxNum) {
				maxNum = num;
			}
		}
		maxNum++;
		String res = String.format(context.getString(R.string.DM_Back_From), android.os.Build.MODEL + "(" + maxNum + ")");
		return res;
	}

	private static int getTailNum(String remoteName) {
		int leftB = remoteName.lastIndexOf('(');
		int rightB = remoteName.lastIndexOf(')');
		if (rightB == -1) {
			return 0;
		}
		int res = Integer.valueOf((String) remoteName.subSequence(leftB + 1,
				rightB));
		return res;
	}

}
