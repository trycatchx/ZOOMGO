package com.dmsys.airdiskpro.utils;

import android.os.Environment;

import java.io.File;
import java.text.SimpleDateFormat;

public class ContactStaticValue {
	public static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	/*public static final String mLocalFolderName = "AirNas2";
	public static final String mRemotePath = "http://192.168.222.254/webdav/disk-a1/";
	public static final String mSDPath = getSDPath();
	public static final String mRemoteFolderName = "AirNasContacts2";*/
	public static final int  ONE_BATCH_COUNT = 100;

	private static String getSDPath() {
		File sdDir = null;
		boolean sdCardExist = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
		if (sdCardExist) {
			sdDir = Environment.getExternalStorageDirectory();
			return sdDir.toString();
		} else
			return null;
	}
}
