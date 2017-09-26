package com.dmsys.airdiskpro.event;

import com.dmsys.dmsdk.model.DMOTA;

public class NewFwEvent {
	
	public DMOTA ota = null;

	public NewFwEvent(DMOTA ota) {
		this.ota = ota;
	}

	public DMOTA getOta() {
		return ota;
	}

	public void setOta(DMOTA ota) {
		this.ota = ota;
	}

	
}
