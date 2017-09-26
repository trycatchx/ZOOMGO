package com.dmsys.airdiskpro.model;

public class BackupDMFile extends MediaInfo{
	private String remoteUrl;

	public String getRemoteUrl() {
		return remoteUrl;
	}

	public void setRemoteUrl(String remoteUrl) {
		this.remoteUrl = remoteUrl;
	}
	
	public BackupDMFile () {}
	
	
	public BackupDMFile(MediaInfo m) {
		this.mSize = m.mSize;
		this.mLastModify = m.mLastModify;
		this.isDir = m.isDir;
		this.mName = m.mName;
		this.mPath = m.mPath;
		this.mLocation = m.mLocation;
		this.mType = m.mType;
		this.selected = m.selected;
		this.mPath = m.mPath;
		this.parentID = m.parentID;
	}
	
	
}
