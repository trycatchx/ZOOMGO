package com.dmsys.airdiskpro.model;

import java.util.ArrayList;

public class MediaFolder {
	public ArrayList<MediaInfo> mediaList ;
	String folderName;
	long parentHash;
	String folderPath;
	public boolean selected;

	public long getParentHash() {
		return parentHash;
	}


	public String getFolderPath() {
		return folderPath;
	}
	public void setFolderPath(String folderPath) {
		this.folderPath = folderPath;
	}

	public void setParentHash(long parentHash) {
		this.parentHash = parentHash;
	}

	public MediaFolder(ArrayList<MediaInfo> mediaList, String folderName, boolean selected,long parentHash,String folderPath) {
		super();
		this.mediaList = mediaList;
		this.folderName = folderName;
		this.selected = selected;
		this.parentHash = parentHash;
		this.folderPath = folderPath;
	}




	@Override
	public String toString() {
		return "MediaFolder [mediaList=" + mediaList + ", folderName=" + folderName + ", parentHash=" + parentHash + ", folderPath="
				+ folderPath + ", selected=" + selected + "]";
	}


	public String getFolderName() {
		return folderName;
	}

	public void setFolderName(String folderName) {
		this.folderName = folderName;
	}


	public ArrayList<MediaInfo> getMediaInfoList() {
		return mediaList;
	}

	public void setMediaInfoList(ArrayList<MediaInfo> pathList) {
		this.mediaList = pathList;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}
}
