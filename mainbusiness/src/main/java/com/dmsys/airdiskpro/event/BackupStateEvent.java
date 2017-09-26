package com.dmsys.airdiskpro.event;

public class BackupStateEvent {

	public static int BACKING = 0;
	public static int FINISHED = 1;
	public static int CANCLE = 2;
	
	public int mState;

	public BackupStateEvent(int mState) {
		this.mState = mState;
	}
	
}
