package com.dmsys.airdiskpro.event;

public class DeviceValutEvent {


	public final static  int DEVICE_PASSWORD = 1;
	public final static  int VAULT_PASSWORD = 0;
	public DeviceValutEvent(int type ,int ret) {
		super();
		this.type = type;
		this.ret = ret;
	}

	public int ret; 
	public int type;
}
