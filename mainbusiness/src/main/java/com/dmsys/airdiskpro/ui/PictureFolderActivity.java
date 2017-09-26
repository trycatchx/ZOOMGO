package com.dmsys.airdiskpro.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

import com.dmairdisk.aodplayer.util.CommonAsync;
import com.dmsys.airdiskpro.adapter.FolderPictureAdapter;
import com.dmsys.airdiskpro.event.DisconnectEvent;
import com.dmsys.airdiskpro.event.UploadEndEvent;
import com.dmsys.airdiskpro.model.MediaFolder;
import com.dmsys.airdiskpro.model.MediaInfo;
import com.dmsys.airdiskpro.ui.UploadBaseActivity.DestType;
import com.dmsys.airdiskpro.utils.SystemDBTool;
import com.dmsys.airdiskpro.view.FileActionView;
import com.dmsys.airdiskpro.view.FileActionView.OnBackIconClickListener;
import com.dmsys.mainbusiness.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.greenrobot.event.EventBus;

public class PictureFolderActivity extends Activity implements OnItemClickListener{
	private GridView grid;
	private FileActionView title_bar;
	
	private FolderPictureAdapter mFolderPictureAdapter;
	private Context mContext;
	private ArrayList<MediaFolder> mGroupDatas = new ArrayList<MediaFolder>();
	private int imageRLWIdth;
	private CommonAsync mCommonAsync;
	private ExecutorService FULL_TASK_EXECUTOR = (ExecutorService) Executors.newCachedThreadPool(); 
	private ViewGroup mLoadingView;
	boolean imageloaderPaused = false;
	private String mCurPath;

	DestType mDestType;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.file_browse_folder_pic_activity);
		
		mContext= this;
		imageRLWIdth = getImageRLWidth(getWindowWidth());
		initViews();
		
        Intent mIntent = getIntent();
		
		if(mIntent != null) {
			int index = mIntent.getIntExtra("DestType", DestType.Udisk.ordinal());
			mDestType  = DestType.values()[index];
		}
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		CommonAsync.Runnable mRunnable = new CommonAsync.Runnable() {
			
			@Override
			public void stop() {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public Object run() {
				// TODO Auto-generated method stub
				return SystemDBTool.getPictrueFiles(mContext);
			
			}
		};
		CommonAsync.CommonAsyncListener mCommonAsyncListener = new CommonAsync.CommonAsyncListener() {
			
			@Override
			public void onResult(Object ret) {
				// TODO Auto-generated method stub
				mLoadingView.setVisibility(View.GONE);
				if(ret != null) {
					ArrayList<MediaInfo> tmp = (ArrayList<MediaInfo>)ret;
					ArrayList<MediaFolder> tmp1 = null;
					try {
						tmp1 = formatList(tmp);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if(tmp1 != null) {
						mGroupDatas.clear();
						mGroupDatas.addAll(tmp1);	
						mFolderPictureAdapter.notifyDataSetChanged();
					}
					
				}
			}
			
			@Override
			public void onError() {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onDestory() {
				// TODO Auto-generated method stub
				
			}
		};
		if(mCommonAsync != null) {
			mCommonAsync.destory();
		}
		mCommonAsync = new CommonAsync(mRunnable, mCommonAsyncListener);
		mCommonAsync.executeOnExecutor(FULL_TASK_EXECUTOR);
	}



	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}



	private void initViews() {
		
		EventBus.getDefault().register(this);
		
		grid = (GridView) findViewById(R.id.grid);
		mFolderPictureAdapter = new FolderPictureAdapter(mContext, mGroupDatas, imageRLWIdth);
		grid.setAdapter(mFolderPictureAdapter);
		grid.setOnItemClickListener(this);
		grid.setOnScrollListener(new PicScrollListener());
		
		title_bar = (FileActionView) findViewById(R.id.title_bar);
		title_bar.showEditBtn(false);
		title_bar.setTitleText(getString(R.string.DM_Navigation_Upload_Pictures));
		title_bar.setBackIconClickListener(new OnBackIconClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		mLoadingView = ((ViewGroup) findViewById(R.id.loading));
		mLoadingView.setVisibility(View.VISIBLE);
		
		Intent tmpIntent = getIntent();
		
		if(tmpIntent != null) {
			mCurPath = tmpIntent.getStringExtra("CurPath");
		}
		
	}
	
	
	
	private ArrayList<MediaFolder> formatList(ArrayList<MediaInfo> fileList) throws IOException {
		
		//将fileList存到ArrayList<MediaFolder>的结构中
		ArrayList<MediaFolder> res = new ArrayList<MediaFolder>();
		for (int i = 0; i < fileList.size(); i++) {
			MediaFolder folder = findItemUseHash(res, fileList.get(i).getParentID());
			if (folder == null) {
				ArrayList<MediaInfo> pathList = new ArrayList<MediaInfo>();
				MediaInfo media = fileList.get(i);
				pathList.add(media);
				res.add(new MediaFolder(pathList, fileList.get(i).getParentName(), true, fileList.get(i).getParentID(),fileList.get(i).getParent()));
			} else {
				MediaInfo media = fileList.get(i);
				folder.getMediaInfoList().add(media);
			}
		}
		return res;
	}
	
	
	private MediaFolder findItemUseHash(ArrayList<MediaFolder> folderList, long hash) {
		for (int i = 0; i < folderList.size(); i++) {
			if (folderList.get(i).getParentHash() == hash ) {
				return folderList.get(i);
			} else {
				continue;
			}
		}
		return null;
	}
	
	
	public int getImageRLWidth(int screenWith) {
		return (screenWith - dip2px(this, 38)) / 2;
	}
	private int getWindowWidth() {
		DisplayMetrics dpy = new DisplayMetrics();
		this.getWindow().getWindowManager().getDefaultDisplay().getMetrics(dpy);
		return dpy.widthPixels;
	}
	 public int dip2px(Context context, float dpValue) {
	        final float scale = context.getResources().getDisplayMetrics().density;
	        return (int) (dpValue * scale + 0.5f);
	}
	 
	 private class PicScrollListener implements OnScrollListener {

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				// TODO Auto-generated method stub
				
				
				
				if (scrollState == OnScrollListener.SCROLL_STATE_TOUCH_SCROLL
						|| scrollState == OnScrollListener.SCROLL_STATE_FLING) {
					if (imageloaderPaused == false) {
						//DMImageLoader.getInstance().pause();
						imageloaderPaused = true;
					}
				} else {
					if (imageloaderPaused == true) {
						//DMImageLoader.getInstance().resume();
						imageloaderPaused = false;
//						DMImageLoader.getInstance().stop();
//						mediaAdapter.notifyDataSetChanged();
					}
				}
			}

		}
	 
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		EventBus.getDefault().unregister(this);
	}

	
	public void onEventMainThread(UploadEndEvent event) {  
		finish();
	} 
	
	public void onEventMainThread(DisconnectEvent event){
		finish();
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub
		Intent mIntent = new Intent(this,PictrueDateActivity.class);
		mIntent.putExtra("bucketId", mGroupDatas.get(position).getParentHash());
		mIntent.putExtra("CurPath", mCurPath);
		if(mDestType != null) {
			mIntent.putExtra("DestType", mDestType.ordinal());	
		}
		System.out.println("CurPath:"+mCurPath);
		startActivity(mIntent);
	}

}
