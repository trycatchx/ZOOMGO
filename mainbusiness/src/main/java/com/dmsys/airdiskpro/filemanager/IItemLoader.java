package com.dmsys.airdiskpro.filemanager;

import android.os.Handler;

import com.dmsys.airdiskpro.utils.HandlerUtil;
import com.dmsys.dmsdk.model.DMFile;

import java.io.Serializable;
import java.util.List;

public interface IItemLoader extends Serializable{
	
	int MSG_LOAD_ITEM_COMPLETE = HandlerUtil.generateId();
	
	public List<DMFile> loadItems(Handler handler, Object o);
}
