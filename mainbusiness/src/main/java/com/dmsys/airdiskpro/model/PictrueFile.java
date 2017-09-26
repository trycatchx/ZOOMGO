package com.dmsys.airdiskpro.model;

import com.dmsys.dmsdk.model.DMFile;

public class PictrueFile extends DMFile {
	public long dateParentId = -1L;

	//新增
	public long parentID;
	
	
	
	public PictrueFile(String name, String path,boolean isDir,long size, long mLastModify) {
		super(name,path,isDir,false,size,mLastModify);
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
}
