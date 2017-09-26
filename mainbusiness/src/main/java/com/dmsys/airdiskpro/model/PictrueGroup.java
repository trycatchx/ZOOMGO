package com.dmsys.airdiskpro.model;

import com.dmsys.dmsdk.model.DMFile;

import java.util.ArrayList;
import java.util.List;

public class PictrueGroup extends DMFile{
	public List<DMFile> picGroup = new ArrayList<>();
	public String folderName;
	public String folderPath;
	public long folderId;
	public long floderSize;
	public int count;
	public boolean selected = false;
	public int mLocation = DMFile.LOCATION_LOCAL;
	
	public PictrueGroup(List<DMFile> picGroup, String folderName,
			String folderPath, long folderId, long floderSize,int count,
			boolean selected, int mLocation) {
		super();
		this.picGroup = picGroup;
		this.folderName = folderName;
		this.folderPath = folderPath;
		this.folderId = folderId;
		this.floderSize = floderSize;
		this.selected = selected;
		this.count = count;
		this.mLocation = mLocation;
	}
	public long getFolderId() {
		return folderId;
	}
	public void setFolderId(long folderId) {
		this.folderId = folderId;
	}
	public List<DMFile> getPicGroup() {
		return picGroup;
	}
	public void setPicGroup(List<DMFile> picGroup) {
		this.picGroup = picGroup;
	}
	public String getFolderName() {
		return folderName;
	}
	public void setFolderName(String folderName) {
		this.folderName = folderName;
	}
	public String getFolderPath() {
		return folderPath;
	}
	public void setFolderPath(String folderPath) {
		this.folderPath = folderPath;
	}
	public long getFloderSize() {
		return floderSize;
	}
	public void setFloderSize(long floderSize) {
		this.floderSize = floderSize;
	}
	public boolean isSelected() {
		return selected;
	}
	public void setSelected(boolean selected) {
		this.selected = selected;
	}
	
	
	
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	public PictrueGroup() {
		super();
		// TODO Auto-generated constructor stub
	}
	@Override
	public String toString() {
		return "PictrueGroup [picGroup=" + picGroup + ", folderName="
				+ folderName + ", folderPath=" + folderPath + ", floderSize="
				+ floderSize + ", selected=" + selected + "]";
	}
	
	@Override
	public int compareTo(DMFile another) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	
	

	
	
	
}
