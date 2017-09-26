package com.dmsys.airdiskpro.ui.imagereader;

/**
 * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
 */
public final class Constants {

	/**
	 * url
	 * 如果包含中文需要encode
	 */
	public static final String[] IMAGES = new String[] {
		"http://192.168.222.254/webdav/SD-disk-a1/1.jpg",
		"http://192.168.222.254/webdav/SD-disk-a1/2.jpg",
		"http://192.168.222.254/webdav/SD-disk-a1/3.jpg",
		"file:///storage/emulated/0/1.jpg",
		"file:///storage/emulated/0/1/test.jpg",
		"file:///storage/emulated/0/1/test.png",
		"file:///storage/emulated/0/1/dm_lib_image_error.png"
	};

	private Constants() {
	}

	public static class Config {
		public static final boolean DEVELOPER_MODE = false;
	}
	
	public static class Extra {
		public static final String IMAGES = "cn.dm.longsys.library.imagereader.universalimageloader.IMAGES";
		public static final String IMAGE_POSITION = "cn.dm.longsys.library.imagereader.universalimageloader.IMAGE_POSITION";
		public static final String IMAGE_FROM = "cn.dm.longsys.library.imagereader.universalimageloader.IMAGE_FROM";
	}
}
