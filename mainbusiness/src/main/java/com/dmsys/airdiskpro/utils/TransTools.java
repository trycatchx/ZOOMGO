package com.dmsys.airdiskpro.utils;

import android.os.Environment;

import com.dmsys.airdiskpro.model.BackupDMFile;
import com.dmsys.airdiskpro.utils.GetBakLocationTools.MyBoolean;
import com.dmsys.dmsdk.DMSdk;
import com.dmsys.dmsdk.model.DMFile;
import com.dmsys.dmsdk.model.DMFileCategoryType;
import com.dmsys.dmsdk.model.DMStorageInfo;

import java.io.File;
import java.io.IOException;

public class TransTools {
	private static final String TAG = "TransTools";
	public static final String POINT_DB = ".db";
	private static final String dirBackupRemoteFolder = "Disk_backup_dir";
	private static final String contactsBackupRemoteFolder = "Contacts_backup_dir";
	public static final String SDCARD = "sdcard";
	public static final String DCIM_CAMERA = "DCIM" + File.separator + "Camera";
	public static final String DeviceRootUrl = "";
	public static final String MyPicture = "My Pictures";
	public static final String MyVideo = "My Videos";
	
	public static String first_remote_disk;
	public static String SDCARD_PATH = getSDCardPath();
	public static String DISK_BU = "DMAirDiskPro_Backup/";
	public static String CONTACTS_BU_F = "Contacts/";
	public static String CONTACTS_BU_Main_F = "Main/";
	public static String C_CONFIG_DBNAME = "desc.db";
	public static String CONTACTS_DB_FOLDER_PATH = SDCARD_PATH + DISK_BU + CONTACTS_BU_F;
	public static String C_BAKLOG_DBNAME = "baklog.db";
	public static String CONTACTS_DB_MAIN_PATH = SDCARD_PATH + DISK_BU + CONTACTS_BU_F + CONTACTS_BU_Main_F;
	public static final String C_CONFIG_LOCAL_PATH = CONTACTS_DB_MAIN_PATH + C_CONFIG_DBNAME;
	
	
	public static String getC_CONFIG_REMOTE_PATH() throws IOException {
		MyBoolean tempB = new MyBoolean();
		
		String diskName = GetBakLocationTools.getBakDiskName(tempB);
		if(diskName == null)
			throw new IOException("diskName == null");
		return TransTools.DeviceRootUrl + diskName + File.separator + ".dmApp" + File.separator
				+ "ContactsBackup" + File.separator + C_CONFIG_DBNAME;

	}
	
	public static String getC_BACKUP_REMOTE_PARENT_PATH() {
		MyBoolean tempB2 = new MyBoolean();
		String diskName = GetBakLocationTools.getBakDiskName(tempB2);
		return TransTools.DeviceRootUrl + diskName + File.separator + ".dmApp" + File.separator
				+ "ContactsBackup" + File.separator + "data" + File.separator;
	}
	
	public static String getSDCardPath() {
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			return Environment.getExternalStorageDirectory().toString() + "/";
		} else {
			return "/";
		}

	}


	public static String getFirstDiskName() {
		String DiskName = null;
		DMStorageInfo info = DMSdk.getInstance().getStorageInfo();
		if (info != null && info.getStorages() != null && info.getStorages().size() > 0) {
			DiskName  = info.getStorages().get(0).mName;
		}
		return DiskName;	
	}

	private static void ensureRemoteBackupFolder(String disk, String dirBackupRemoteFolder) throws IOException {
		ensureRemoteFolder(DeviceRootUrl + disk + File.separator + dirBackupRemoteFolder);
	}

	private static void ensureRemoteDirStruct_old(String disk, String dirBackupRemoteFolder, String filePath) throws IOException {
		// info.getFilePath()="/storage/emulated/0/airdisk_apk/hidisk_1.1.0_20141217.apk"
		String parentUrl = DeviceRootUrl + disk + File.separator + dirBackupRemoteFolder;
		String[] pathNames = filePath.split("/");
		for (int i = 0; i < pathNames.length; i++) {
			if (i < pathNames.length - 1) {
				String childUrl = buildChildUrl(pathNames, i);
				ensureRemoteFolder(parentUrl + File.separator + childUrl);
			}
		}
	}
	
	
	
	public static boolean existRemoteFile(String url)   {
		boolean res = false;
		res = DMSdk.getInstance().isExisted(url);
		return res;
	}

	public static String DIRECTORY_CAMERA = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath()
			+ "/Camera";
	
	public static String getRemoteBUUrl(DMFile mfile,String diskName,String phoneName) {
		String res = "";
		String pathMd5 = "";
		String shortUrl = "";
		
		if(mfile.getPath() != null) {
			pathMd5 = ContactHandler.makeMD5(mfile.getParent());
			shortUrl = generateShortUrl(pathMd5); 
		} 
	
		String remoteFilePath = mfile.getParentName()+"("+"DM_"+shortUrl+")"+File.separator+mfile.getName();
			
		if(mfile.mType == DMFileCategoryType.E_PICTURE_CATEGORY) {
			res = diskName + File.separator + phoneName 
					+ File.separator + MyPicture  + File.separator
					+ remoteFilePath;		
		} else if(mfile.mType == DMFileCategoryType.E_VIDEO_CATEGORY) {
			res = diskName + File.separator + phoneName 
					+ File.separator + MyVideo + File.separator
					+ remoteFilePath;		
		}
		return res;
	}
	
	
    static String[] chars = new String[] { "a" , "b" , "c" , "d" , "e" , "f" , "g" , "h" ,
        "i" , "j" , "k" , "l" , "m" , "n" , "o" , "p" , "q" , "r" , "s" , "t" ,
        "u" , "v" , "w" , "x" , "y" , "z" , "0" , "1" , "2" , "3" , "4" , "5" ,
        "6" , "7" , "8" , "9" , "A" , "B" , "C" , "D" , "E" , "F" , "G" , "H" ,
        "I" , "J" , "K" , "L" , "M" , "N" , "O" , "P" , "Q" , "R" , "S" , "T" ,
        "U" , "V" , "W" , "X" , "Y" , "Z"}; 
	private static String generateShortUrl(String md5) {
        String hex = md5; 
        String[] resUrl = new String[4];
 
        for ( int i = 0; i < 4; i++) {
            String sTempSubString = hex.substring(i * 8, i * 8 + 8);
            long lHexLong = 0x3FFFFFFF & Long.parseLong (sTempSubString, 16);
            String outChars = "" ;
            for ( int j = 0; j < 6; j++) {
               long index = 0x0000003D & lHexLong;
               outChars += chars[( int ) index];
               lHexLong = lHexLong >> 5;
            }
            resUrl[i] = outChars;
        }
        return resUrl[0]; 
    }
	
	
	/**
	 * 
	 * @param filePath
	 *            远程文件绝对路径，带文件名
	 * @throws IOException
	 */
	public static void ensureRemoteFileDirStruct(String filePath) throws IOException {
		filePath = filePath.substring(DeviceRootUrl.length());
		String[] pathNames = filePath.split("/");
		for (int i = 0; i < pathNames.length; i++) {
			if (i < pathNames.length - 1) {
				String childUrl = buildChildUrl(pathNames, i);
				String url = DeviceRootUrl + childUrl;
//				url = FileInfoUtils.encodeUri(url);
				ensureRemoteFolder(url);
			}
		}
	}
	
	/**
	 * 自带encodeUri
	 * @param filePath
	 * @throws IOException
	 */
	public static void ensureRemoteDirStruct(String filePath) throws InterruptedException   {
		filePath = filePath.substring(DeviceRootUrl.length());
		String[] pathNames = filePath.split("/");
		for (int i = 0; i < pathNames.length; i++) {
			if(isUserStop) throw new InterruptedException();
				String childUrl = buildChildUrl(pathNames, i);
				String url = DeviceRootUrl + childUrl;
				ensureRemoteFolder(url);
		}
	}

	private static String getUrl(String disk, String dirBackupRemoteFolder, String filePath) {
		String res = DeviceRootUrl + disk + File.separator + dirBackupRemoteFolder + File.separator + filePath;
		return res;
	}

	private static String buildChildUrl(String[] pathNames, int i) {
		StringBuffer child = new StringBuffer("");
		for (int j = 0; j <= i; j++) {
			child.append(pathNames[j]).append(File.separator);
		}
		String ret = "";
		if(child.length() > 0) {
			ret = child.toString().substring(0, child.length()-1);
		}
		return ret;
	}

	private static void ensureRemoteFolder(String folderUrl)  {
		
		
		System.out.println("test 123mPath:"+folderUrl);
		boolean res = DMSdk.getInstance().isExisted(folderUrl);
		System.out.println("test 123res:"+res);
		if (!res) {
			int ret = DMSdk.getInstance().creatDir(folderUrl);
		}
	}
	public static String appendTEMP(String str) {
		String[] arrayStr = str.split("\\.");
		arrayStr[arrayStr.length - 2] = arrayStr[arrayStr.length - 2] + "_temp";
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < arrayStr.length; i++) {
			sb.append(arrayStr[i]);
			if (i != arrayStr.length - 1)
				sb.append(".");
		}
		return sb.toString();
	}

	private static String removeTEMP(String str) {
		String tempStr = "_temp";
		String[] arrayStr = str.split("\\.");
		int len = arrayStr[arrayStr.length - 2].length();
		arrayStr[arrayStr.length - 2] = arrayStr[arrayStr.length - 2].substring(0, len - tempStr.length());
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < arrayStr.length; i++) {
			sb.append(arrayStr[i]);
			if (i != arrayStr.length - 1)
				sb.append(".");
		}
		return sb.toString();
	}

	private static String getFileName(String localFilePath) {
		String[] arrayStr = localFilePath.split("/");
		return arrayStr[arrayStr.length - 1];
	}

	

	

	



	public static void delLocalFile(String filePath) {
		File file = new File(filePath);
		if (file.exists())
			file.delete();
	}

	/**
	 * 注意：此方法不会进行递归子文件夹
	 * 
	 * @param filePath
	 * @return
	 */
	public static boolean clearFolder(String filePath) {
		File file = new File(filePath);
		if (!file.exists())
			return false;
		if (!file.isDirectory())
			return false;
		File[] arrayF = file.listFiles();
		for (File f : arrayF) {
			if (f.exists())
				f.delete();
		}
		return true;
	}

	private static void renameLocalFile(String filePath1, String filePath2) {
		File file = new File(filePath1);
		if (file.exists()) {
			file.renameTo(new File(filePath2));
		}
	}
	
	public static boolean isMediaRemoteBackuped(BackupDMFile mfile) {
		BackupDMFile m = new BackupDMFile();
		String remoteUrl = mfile.getRemoteUrl();
//		remoteUrl = FileInfoUtils.encodeUri(remoteUrl);
		boolean ret = DMSdk.getInstance().isExisted(remoteUrl);
		return ret;
	}
	public static boolean isUserStop = false;
	
	


}
