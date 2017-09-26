package com.dmsys.airdiskpro.model;

public class BakSetBean {

	public static final int TRUE = 1;
	public static final int FALSE = 0;

	public String mac;
	public int autoBak;
	public int bakImage;
	public int bakVideo;
	public int bakContacts;
	public String diskName;
	public String media_bak_folder;
	public String allStorage;
	public int bakDisplay;
	//media
	public int bakOnOff;
	public BakSetBean(String mac, int autoBak, int bakImage, int bakVideo,
			int bakContacts, String diskName, String media_bak_folder,
			String allStorage, int bakDisplay,int bakOnOff) {
		this.mac = mac;
		this.autoBak = autoBak;
		this.bakImage = bakImage;
		this.bakVideo = bakVideo;
		this.bakContacts = bakContacts;
		this.diskName = diskName;
		this.media_bak_folder = media_bak_folder;
		this.allStorage = allStorage;
		this.bakDisplay = bakDisplay;
		this.bakOnOff = bakOnOff;
	}
	@Override
	public String toString() {
		return "BakSetBean [mac=" + mac + ", autoBak=" + autoBak
				+ ", bakImage=" + bakImage + ", bakVideo=" + bakVideo
				+ ", bakContacts=" + bakContacts + ", diskName=" + diskName
				+ ", media_bak_folder=" + media_bak_folder + ", allStorage="
				+ allStorage + ", bakDisplay=" + bakDisplay + ", bakOnOff="
				+ bakOnOff + "]";
	}






}
