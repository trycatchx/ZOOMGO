package com.dmsys.airdiskpro.model;

import com.dmsys.dmsdk.model.DMFile;

import java.util.List;

public class DirViewStateChangeEvent {

	public int state;
	public String currentPath;
	public List<DMFile> fileList;
	
	public DirViewStateChangeEvent(int state, String currentPath, List<DMFile> fileList) {
		this.state = state;
		this.currentPath = currentPath;
		this.fileList = fileList;
	}
	
	
	
}
