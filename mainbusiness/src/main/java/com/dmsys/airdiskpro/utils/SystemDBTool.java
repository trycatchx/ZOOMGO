package com.dmsys.airdiskpro.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;

import com.dmsys.airdiskpro.model.MediaInfo;
import com.dmsys.airdiskpro.model.MediaInfo.Type;
import com.dmsys.dmsdk.model.DMFile;
import com.dmsys.dmsdk.model.DMFileCategoryType;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;


public class SystemDBTool {
	/**
	 * 获取图片文件
	 * @param context
	 * @return
	 */
	public static ArrayList<MediaInfo> getPictrueFiles(Context context) {
		ArrayList<MediaInfo> phoneFileList = new ArrayList<>();
		ContentResolver resolver = context.getContentResolver();
		Cursor cursor = null;
		//按照降序去获取
		String sortOrder = MediaStore.Images.Media.DATE_MODIFIED + " desc";  
		
		cursor = resolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, null, sortOrder);
		readPictrueCursor(Type.PICTRUE,cursor,phoneFileList);
		
//		cursor = resolver.query(MediaStore.Images.Media.INTERNAL_CONTENT_URI, null, null, null, sortOrder);
//		readCursor(cursor,phoneFileList);
		
		return phoneFileList;
	}
	
	
	/**
	 * 获取图片和视频的文件！用于备份模块
	 * @param cursor
	 * @param phoneFileList
	 */
	
	
	public static ArrayList<MediaInfo> getPhoneMFiles(Context context) {
		ArrayList<MediaInfo> phoneFileList = new ArrayList<>();
		ContentResolver resolver = context.getContentResolver();
		Cursor cursor = null;
		//按照降序去获取
		String sortOrder = MediaStore.Images.Media.DATE_MODIFIED + " desc";  
		
		cursor = resolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, null, sortOrder);
		readPictrueCursor(Type.PICTRUE,cursor,phoneFileList);
		
		cursor = resolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, null, null, null,sortOrder);
		readPictrueCursor(Type.VIDEO,cursor,phoneFileList);
		return phoneFileList;
	}
	
	
	private static void readPictrueCursor(Type type ,Cursor cursor,ArrayList<MediaInfo> phoneFileList)
	{
		if (cursor != null) {
			if (cursor.moveToFirst()) {
				do {
					String fileName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME));
					String filePath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
					if (fileName != null && filePath != null && fileName.equals(filePath)) {
						fileName = filePath.substring(filePath.lastIndexOf("/")+1);
					}
					long parentHash = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_ID));
					String folderName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
					File tempFile = new File(filePath);
					long fileSize = tempFile.length();
					if (fileSize > 1 * 1024) {
						String parentPath = tempFile.getParent();
						MediaInfo mfile = new MediaInfo(fileName, type,filePath, tempFile.isDirectory(), fileSize,tempFile.lastModified());
						mfile.parentID = parentHash;
						mfile.mMediaInfoType = type;
						phoneFileList.add(mfile);
					}
					
				} while (cursor.moveToNext());
			}
		}
		if(cursor != null)
			cursor.close();
	}
	
	
	
	/**
	 * 通过文件夹的Id获取指定文件夹的所有图片
	 */
	public static ArrayList<MediaInfo> getPicFileByBucketId(Context context,long BucketId) {
		ArrayList<MediaInfo> phoneFileList = new ArrayList<MediaInfo>();
		ContentResolver resolver = context.getContentResolver();
		Cursor cursor = null;
		
		//选择指定文件夹
		String selection = MediaStore.Images.Media.BUCKET_ID + "=" + BucketId;
		//按照降序去获取
		String sortOrder = MediaStore.Images.Media.DATE_MODIFIED + " desc";  
		
		cursor = resolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, selection, null, sortOrder);
		readPictrueCursor(Type.PICTRUE,cursor,phoneFileList);
		fillPPE(phoneFileList);
		return phoneFileList;
	}
	
	
	private static void fillPPE(ArrayList<MediaInfo> phoneFileList) {
		long dateParentId = 0L;
		long lastDate = 0L;
		
		Iterator<MediaInfo> iter = phoneFileList.iterator();
		while (iter.hasNext()) {
			MediaInfo p = iter.next();
			//日期不是同一天
			if(p.mLastModify <= 0) {
				p.mLastModify = lastDate; 
			} else if(!TimeTool.isSameDayOfMillis(p.mLastModify, lastDate)) {
				dateParentId++;
				lastDate = p.mLastModify;
			} 
			p.dateParentId = dateParentId;
		}
	}
	
	/**
	 * 获取音乐文件
	 * @param context
	 * @return
	 */
	public static ArrayList<DMFile> getAudioFiles(Context context) {
		ArrayList<DMFile> audioFileList = new ArrayList<DMFile>();
		ContentResolver resolver = context.getContentResolver();
		Cursor cursor = null;
		//按照降序去获取
		String sortOrder = MediaStore.Audio.Media.DATE_MODIFIED + " desc";  
		
		cursor = resolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null, sortOrder);
		readAudioCursor(cursor,audioFileList);
		
//		cursor = resolver.query(MediaStore.Images.Media.INTERNAL_CONTENT_URI, null, null, null, sortOrder);
//		readCursor(cursor,phoneFileList);
		
		return audioFileList;
	}
	/**
	 * 获取视频文件
	 * @param context
	 * @return
	 */
	private static void readAudioCursor(Cursor cursor,ArrayList<DMFile> AudioFileList)
	{
		if (cursor != null) {
			if (cursor.moveToFirst()) {
				do {
					/*String fileName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
					if(fileName == null || fileName.equals("")) {
						fileName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME));
						fileName = getNameByFileName(fileName);
					}*/
					String filePath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
					
					String fileName = filePath.substring(filePath.lastIndexOf("/")+1);
					
					File tempFile = new File(filePath);
					long fileSize = tempFile.length();
					if (fileSize > 100 * 1024) {
						DMFile mfile = new DMFile(fileName, filePath, tempFile.isDirectory(), false,fileSize,tempFile.lastModified());
						mfile.setType(DMFileCategoryType.E_MUSIC_CATEGORY.ordinal());
						AudioFileList.add(mfile);
					}
					
				} while (cursor.moveToNext());
			}
		}
		if(cursor != null)
			cursor.close();
		
	}
	
	private static String getNameByFileName(String fileName) {
		String ret = "";
		if(fileName != null) {
			String arg[] = fileName.split("/");
			if(arg != null) {
				ret = arg[arg.length-1];
			}
		}
		return ret;
	}
	
	/**
	 * 获取视频文件
	 * @param context
	 * @return
	 */
	public static ArrayList<DMFile> getVideoFiles(Context context) {
		ArrayList<DMFile> videoFileList = new ArrayList<DMFile>();
		ContentResolver resolver = context.getContentResolver();
		Cursor cursor = null;
		//按照降序去获取
		String sortOrder = MediaStore.Video.Media.DATE_MODIFIED + " desc";  
		
		cursor = resolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, null, null, null, sortOrder);
		readVideoCursor(cursor,videoFileList);
		
//		cursor = resolver.query(MediaStore.Images.Media.INTERNAL_CONTENT_URI, null, null, null, sortOrder);
//		readCursor(cursor,phoneFileList);
		
		return videoFileList;
	}
	
	private static void readVideoCursor(Cursor cursor,ArrayList<DMFile> videoFileList)
	{
		if (cursor != null) {
			if (cursor.moveToFirst()) {
				do {
					/*String fileName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE));
					if(fileName == null || fileName.equals("")) {
						fileName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME));
						fileName = getNameByFileName(fileName);
					}*/
					String filePath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA));
					String fileName = filePath.substring(filePath.lastIndexOf("/")+1);
					
					File tempFile = new File(filePath);
					long fileSize = tempFile.length();
					DMFile mfile = new DMFile(fileName, filePath, tempFile.isDirectory(),false,fileSize,tempFile.lastModified());
					mfile.setType(DMFileCategoryType.E_VIDEO_CATEGORY.ordinal());
					videoFileList.add(mfile);
				} while (cursor.moveToNext());
			}
		}
		if(cursor != null)
			cursor.close();
		
	}
	
	
	
}
