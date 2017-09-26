package com.dmsys.airdiskpro.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Stream 的工具类
 * 
*@author alan.wang
*@date 2014-08-29
*
*/

public class StreamTool {

	private final static int BUFFER_SIZE = 1024;
    
    /**
     *功能：将留保存为本地文件
     *
     *@param is ：需导入的文件的InputStream，可以是本地其他目录下文件流，也可以是网络加载的文件流
     *@param urlFile :导入文件所存放的名字(绝对路径)，如/sdcard/calllog/calllog.db
     *@return localFile :返回保存的文件
    */
    public static File convertIsToFile(InputStream is ,String urlFile){
    	
    	if (urlFile==null || urlFile.equals("")) {
    		return null;
		}
    	if(!ensureFilePathExist(urlFile))
    		return null;
    	File localFile = new File(urlFile);
    	if (is != null) {
    		writeIsToOs(is ,localFile);
		}
    	
    	return localFile;
    }
    
    
    /**
     *功能：将本本地文件转化成InputStream
     *
     *@param urlFile :需导出的文件的名字（带路径），如data/data/com.netcom.calllog/database/callogs.db
     *@return fileInputStream :可以使用返回的InputStream来保存为本地文件，也可以通过网络上传
    */
    public static InputStream convertFileToIs(String urlFile){
    	InputStream fileInputStream = null;
    	if (urlFile==null || urlFile.equals("")) {
    		//Log.i(StaticVariate.TAG,"文件null");
    		return null;
		}
    	
    	try {
    		fileInputStream = new FileInputStream(urlFile);
		} catch (FileNotFoundException e) {
			//Log.i(StaticVariate.TAG,"文件不存在");
			e.printStackTrace();
			return null;
		}
    	
    	return fileInputStream;
    }
    
    /**
     *功能：将InputStream写入到file存到本地
     *
     *@param fileInputStream :需写入的fileInputStream
     *@return urlFile :保存的文件的url，如/sdcard/calllog/calllog.db
     * @throws IOException 
    */
    public static void savedAsLocalFile(InputStream fileInputStream ,String urlFile) throws IOException{
    	if(!ensureFilePathExist(urlFile))
    	{
    		throw new IOException("full phone storage");
    	}
    	//Log.i(StaticVariate.TAG,"urlFile>>"+urlFile);
    	File localFile = new File(urlFile);
    	writeIsToOs(fileInputStream ,localFile);
    	/*if (localFile.exists()) {
    		writeIsToOs(fileInputStream ,localFile);
    	}else {
    		//Log.i(StaticVariate.TAG,">>文件不存在");
		}*/
    }
    
    
    /**
     *功能：拷贝文件
     *
     *@param srcUrlFile :需拷贝的源文件
     *@return objUrlFile :保存的文件，如/sdcard/calllog/calllog.db
    */
    public static void copyFile(String srcUrlFile ,String objUrlFile){
    	StreamTool.convertIsToFile(StreamTool.convertFileToIs(srcUrlFile), objUrlFile);
    }
    
//    /**
//     * 判断文件存放的路径是否存在，如果不存在就新建其目录
//     * @param urlFile
//     */
//    public static boolean ensureFilePathExist(String urlFile){
//    	int lastIndex = urlFile.lastIndexOf("/");
//    	if(lastIndex == (urlFile.length() - 1))
//    	{
//    		urlFile = urlFile+"temp.txt";
//    	}
////    	String filePath = urlFile.substring(0, lastIndex);
//    	File fileDir = new File(urlFile);
//    	if(fileDir.isDirectory())
//    	{
//    		Log.d("streamT", "");
//    	}
//    	if (!fileDir.exists()) {
//    		return fileDir.mkdirs();//建立多级级目录，建立一级目录是：fileDir.mkdir()
//		}
//    	return true;
//    }
    
    public static boolean ensureFilePathExist(String urlFile){
    	boolean res;
    	int lastIndex = urlFile.lastIndexOf("/");
    	String filePath = urlFile.substring(0, lastIndex);
    	File fileDir = new File(filePath);
    	if (!fileDir.exists()) {
    		res =  fileDir.mkdirs();//建立多级级目录，建立一级目录是：fileDir.mkdir()
		}else
		{
			res = true;
		}
    	
    	return res;
    }
    
    /**
     *功能：将InputStream写入到file中
     *
     *@param fileInputStream :需写入的fileInputStream
     *@return file :保存的文件
    */
    private static void writeIsToOs(InputStream fileInputStream ,File file){
    	try {
    		FileOutputStream outputStream = new FileOutputStream(file);
			byte[] buffer = new byte[BUFFER_SIZE];
			int len = -1;
			while ((len = fileInputStream.read(buffer)) > 0) {
				outputStream.write(buffer, 0, len);
            }
			outputStream.close();
			fileInputStream.close();
            
		} catch (FileNotFoundException e) {
			//Log.i(StaticVariate.TAG,"FileNotFound>>>>>");
			e.printStackTrace();
		} catch (IOException e) {
			//Log.i(StaticVariate.TAG,"IOException>>>>>");
			e.printStackTrace();
		}
    }
}
