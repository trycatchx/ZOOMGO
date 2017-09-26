package com.dmsys.airdiskpro.device;

public class DeviceMonitor {

	private static DeviceMonitor monitor = null;
	
	private DeviceMonitor(){
		
	}
	
	public static DeviceMonitor getInstance() {
		if (monitor == null) {
			monitor = new DeviceMonitor();
		}
		return monitor;
	}
	
}
