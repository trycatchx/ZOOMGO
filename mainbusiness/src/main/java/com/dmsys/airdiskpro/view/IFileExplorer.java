package com.dmsys.airdiskpro.view;

import com.dmsys.dmsdk.model.DMFile;

import java.util.List;

public interface IFileExplorer {

	public void switchMode(int mode);

	public void selectAll();

	public void unselectAll();

	public List<DMFile> getSelectedFiles();
	
	public void reloadItems();
	
}
