package com.dmsys.airdiskpro.model;

import com.dmsys.dmsdk.model.DMFile;

public class MediaInfo extends DMFile {
	public long dateParentId = -1L;

	//新增
	public long parentID;
	public enum Type {
		PICTRUE,VIDEO
	}
	public Type mMediaInfoType = Type.PICTRUE;
	
	public MediaInfo(){}
	public MediaInfo(String name, Type mediaType,String path,boolean isDir,long size, long mLastModify) {
		super(name,path,isDir,false,size,mLastModify);
		mMediaInfoType = mediaType;
	}

	public long getDateParentId() {
		return dateParentId;
	}

	public void setDateParentId(long dataParentId) {
		this.dateParentId = dataParentId;
	}

	
	public long getParentID() {
		return parentID;
	}

	public void setParentID(long parentID) {
		this.parentID = parentID;
	}
	public Type getMediaInfoType() {
		return mMediaInfoType;
	}
	public void setMediaInfoType(Type mMediaInfoType) {
		this.mMediaInfoType = mMediaInfoType;
	}
	
	
	
}
