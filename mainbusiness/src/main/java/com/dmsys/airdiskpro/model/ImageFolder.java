package com.dmsys.airdiskpro.model;

import java.util.ArrayList;

public class ImageFolder {
	public ArrayList<MediaInfo> list = new ArrayList<MediaInfo>();
	public long dateParentId = -1L;
	public String Date = "";
	
	public ImageFolder(ArrayList<MediaInfo> list, long dateParentId,
			String date) {
		super();
		this.list = list;
		this.dateParentId = dateParentId;
		Date = date;
	}
	
	public long getDateParentId() {
		return dateParentId;
	}


	public void setDateParentId(long dateParentId) {
		this.dateParentId = dateParentId;
	}


	public String getDate() {
		return Date;
	}


	public void setDate(String date) {
		Date = date;
	}


	public ImageFolder() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	
}
