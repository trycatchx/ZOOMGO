package com.dmsys.airdiskpro.filemanager;

import android.os.Handler;

import com.dmsys.airdiskpro.utils.TimeTool;
import com.dmsys.dmsdk.DMSdk;
import com.dmsys.dmsdk.model.DMFile;
import com.dmsys.dmsdk.model.DMFileCategoryType;
import com.dmsys.dmsdk.model.DMFilePage;
import com.dmsys.dmsdk.model.DMFileSort;

import java.util.ArrayList;
import java.util.List;

public class PictrueLoader implements IItemLoader {

	private static final long serialVersionUID = 1L;
	
	private Thread mThread = null;


	@Override
	public List<DMFile> loadItems(final Handler handler,final Object o) {
		if (mThread != null)
		{
			//mThread.stop();
			return null;
		}
		
		mThread = new Thread() {
			public void run() {
				//这里按照时间排序拿出来，显示文件夹位置不会变化
				
				DMSdk.getInstance().setFileSortType(DMFileSort.SORT_TYPE_TIME);
				
				DMSdk.getInstance().setFileSortOrder(DMFileSort.SORT_ORDER_DOWN);
				
				DMFilePage page = DMSdk.getInstance().getFileListByType(DMFileCategoryType.E_PICTURE_CATEGORY);
				List<ArrayList<DMFile>> list =	formatGroup(page);
				handler.obtainMessage(MSG_LOAD_ITEM_COMPLETE, list).sendToTarget();
				
				PictrueLoader.this.mThread = null;
			}
		};
		
		mThread.start();
		return null;
	}
	
	private List<ArrayList<DMFile>> formatGroup(DMFilePage page)  {
		List<ArrayList<DMFile>> res = new ArrayList<>();
		
		if (page != null && page.getFiles() != null) {
			
			int pages = page.getTotalPage();
			List<DMFile> files = page.getFiles();
//	    	if (pages > 1) {  //获取所有文件夹
//
//	    		for(int i = 1;i < pages; i++){
//		    		DMFilePage next = DMSdk.getInstance().getFileListByType(DMFileCategoryType.E_PICTURE_CATEGORY);
//		    		if (next != null && next.getFiles() != null) {
//		    			files.addAll(next.getFiles());
//					}
//		    	}
//			}
			
	    	
//	    	for(DMFile d:files) {
//	    		System.out.println("test123 同一天测试"+TimeTool.formatPicDate(BrothersApplication.getInstance(), d.mLastModify));
//	    	}
//
	    	long curDate = -1;
			ArrayList<DMFile> curList = null;
			for (DMFile pic : files) {  
				
				long date = pic.mLastModify; 
				
				if (curDate == -1) {
					curDate = date;
				}
				if (curList == null) {
					curList = new ArrayList<>();
					res.add(curList);
				}
				
				if (TimeTool.isSameDayOfMillis(date,curDate)) {
					curList.add(pic);
				} else {
					if (curList != null) {
						curList = new ArrayList<>();
						res.add(curList);
					}
					curList.add(pic);
					curDate = date;
				}
				
			}
			
		}
		return res;
	}

}
