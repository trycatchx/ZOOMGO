package com.dmsys.airdiskpro.filemanager;

import android.os.Handler;

import com.dmsys.airdiskpro.model.MulPictrueGroup;
import com.dmsys.airdiskpro.model.PictrueGroup;
import com.dmsys.dmsdk.DMSdk;
import com.dmsys.dmsdk.model.DMAlbumInfo;
import com.dmsys.dmsdk.model.DMFile;

import java.util.ArrayList;
import java.util.List;

/**
 * 图片群组的加载器
 * @author Administrator
 *
 */
public class PictrueGroupLoader implements IItemLoader {

	/**
	 * 
	 */
	private static final int SIZE = 2;
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
				//这里不按照时间排序拿出来，显示文件夹位置不会变化
				ArrayList<MulPictrueGroup> l = new ArrayList<>();
				List<DMAlbumInfo> albums = DMSdk.getInstance().getAlbumList();
				if (albums != null) {
					
					ArrayList<PictrueGroup> PictrueGroupList =	formatGroup(albums);
					//两个相册为一组
					int count = 0;
					MulPictrueGroup m = new MulPictrueGroup();
					for(PictrueGroup p:PictrueGroupList) {
						//文件夹里面文件按照时间排下序列，再添加进去
						//FileUtil.sortFileListByTime(p.picGroup);
						m.PictrueGroupList.add(p);
						if(count >= SIZE -1) {
							l.add(m);
							m = new MulPictrueGroup();
							count = 0;
						} else {
							count++;
						}
					}
					if(count ==1) {
						l.add(m);
					}
				}
				
				handler.obtainMessage(MSG_LOAD_ITEM_COMPLETE, l).sendToTarget();
				
				PictrueGroupLoader.this.mThread = null;
			}
		};
		
		mThread.start();
		return null;
	}
	
	private ArrayList<PictrueGroup> formatGroup(List<DMAlbumInfo> albumInfos)  {
		ArrayList<PictrueGroup> res = new ArrayList<>();
		
		for (DMAlbumInfo album : albumInfos) {
			PictrueGroup folder = new PictrueGroup();
			folder.setDir(true);
			folder.setFolderName(album.mName);
			folder.setFolderPath(album.mPath);
			folder.mLastModify = album.mDate;
			folder.mHidden = album.mHidden;
			folder.setCount(album.mCount);
			folder.setFloderSize(album.mSize);
			folder.setPicGroup(album.mShowFiles);
			res.add(folder);
		}
		
		return res;
	}
	
	


}
