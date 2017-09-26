package com.dmsys.airdiskpro.event;

import android.os.Message;

public class BackupRefreshEvent {

	public int type;
	public Message message;
	
	
	public BackupRefreshEvent(int type, Message message) {
		this.type = type;
		this.message = message;
	}
	
	
}
