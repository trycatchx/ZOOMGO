package com.dmsys.airdiskpro.utils;

import java.util.ArrayList;
import java.util.List;


/**
 * 文件操作帮助类
 * 
 * @author mayh
 * 
 */
public class FileHelper {

	// 设置当前支持的文件格式
	public static List<String> allowTypeList = new ArrayList<String>() {
		{
			add("txt");
			// add("xml");
			// add("png");
			// add("bmp");
			// add("jpeg");
			// add("jpg");
			// add("gif");
			// add("ico");
		}
	};

	

	/*
	 * 获取给定文件名的扩展名部分
	 * 
	 * @param filename 源文件名
	 * 
	 * @return 源文件名的扩展名部分(不含小数点)
	 */
	public static String getFileSuffix(String filename) {
		int start = filename.lastIndexOf("/");
		int stop = filename.lastIndexOf(".");
		if (stop < start || stop >= filename.length() - 1) {
			return "";
		} else if (stop == -1) {
			return "";
		} else {
			return filename.substring(stop + 1, filename.length());
		}
	}


    
}
