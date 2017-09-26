package com.dmsys.dropbox.utils;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;

/**
 * 一个代理服务器的转换工具类
 * 
 * @author Administrator
 *
 */
public class DropBoxVodUrlConversionHelper {

	private HashMap<String, String> urlDictionary = new HashMap<String, String>();
	private long id = -1;
	public final static int port = 6789;
	public final static String VIDEO_PREFIX = "dropbox-vod-";

	public static DropBoxHttpServer httpServer = null;

	/**
	 * 
	 * @author zhang 效率最高的 单例写法，不过也有弊端。
	 */
	static class DropBoxVodUrlConversionHelperHolder {
		public static DropBoxVodUrlConversionHelper instance = new DropBoxVodUrlConversionHelper();
	}

	public static DropBoxVodUrlConversionHelper getInstance() {
		return DropBoxVodUrlConversionHelperHolder.instance;
	}

	private synchronized long getAutoIncId() {
		return ++id;
	}

	/**
	 * 这里是映射 url 的转换规则
	 * 
	 * @param context
	 * @param preUrl
	 * @return
	 */
	public String getConvertedUrl(Context context, String preUrl,String dest_name) {
		

		String id = dest_name;
		urlDictionary.put(id, preUrl);
		//需要编码，不然中文在网络传输会变乱码
		try {
			id = java.net.URLEncoder.encode(id,"utf-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return "http://127.0.0.1:" + port + "/" + id;
	}

	public String getRealUrlById(String id) {
		return urlDictionary.get(id);
	}

	public InetAddress getLocalIpInetAddress(Context ctx) {
		WifiManager wifiManager = (WifiManager) ctx
				.getSystemService(Context.WIFI_SERVICE);
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		int ipAddress = wifiInfo.getIpAddress();
		try {
			return InetAddress.getByName(String.format("%d.%d.%d.%d",
					(ipAddress & 0xff), (ipAddress >> 8 & 0xff),
					(ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff)));
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public final static Object mOprHttpService = new Object();

	public void startHttpServer() {
		synchronized (mOprHttpService) {
			if (httpServer == null) {
				try {
					httpServer = new DropBoxHttpServer(port);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	public void stopHttpServer() {
		synchronized (mOprHttpService) {
			if (httpServer != null) {
				httpServer.stop();
				httpServer = null;
			}
			if(urlDictionary != null) {
				urlDictionary.clear();
			}
		}
	}

}
