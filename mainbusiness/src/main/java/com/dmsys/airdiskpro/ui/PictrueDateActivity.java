package com.dmsys.airdiskpro.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import com.dmairdisk.aodplayer.util.CommonAsync;
import com.dmsys.airdiskpro.adapter.DatePictrueAdapter;
import com.dmsys.airdiskpro.event.DisconnectEvent;
import com.dmsys.airdiskpro.event.UploadEndEvent;
import com.dmsys.airdiskpro.model.ImageFolder;
import com.dmsys.airdiskpro.model.MediaInfo;
import com.dmsys.airdiskpro.ui.UploadBaseActivity.OnSelectChangeListener;
import com.dmsys.airdiskpro.utils.SystemDBTool;
import com.dmsys.airdiskpro.utils.TimeTool;
import com.dmsys.airdiskpro.view.FileActionView.OnBackIconClickListener;
import com.dmsys.airdiskpro.view.FileActionView.OnClickListener;
import com.dmsys.airdiskpro.view.FileActionView.STATE;
import com.dmsys.mainbusiness.R;

import java.util.ArrayList;

import cn.dm.longsys.library.imageloader.core.DMImageLoader;
import cn.dm.longsys.library.imageloader.core.listener.PauseOnScrollListener;
import de.greenrobot.event.EventBus;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

public class PictrueDateActivity extends UploadBaseActivity implements 
		StickyListHeadersListView.OnStickyHeaderOffsetChangedListener,
		StickyListHeadersListView.OnStickyHeaderChangedListener,
		View.OnTouchListener,View.OnClickListener,
		OnClickListener,OnBackIconClickListener, OnSelectChangeListener{

	private DatePictrueAdapter mAdapter;
	
	private StickyListHeadersListView stickyList;
	
	private ArrayList<MediaInfo> tmpList = new ArrayList<>();
	private ArrayList<ImageFolder> list = new ArrayList<>();
	
	private Context mContext;
	long bucketId = -1l;
	
	private String mCurPath;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.file_browse_date_pic_activity);
		super.onCreate(savedInstanceState);
	
		mContext = this;
		mAdapter = new DatePictrueAdapter(this,list,getImageRLWidth(getWindowWidth()));
		mAdapter.setOnSelectChangeListener(this);
		initViews();
		title_bar.setTitleText(getString(R.string.DM_Navigation_Upload_Pictures));

		loadfiles();
	}
	
	private void initViews() {
		
		EventBus.getDefault().register(this);
		
		stickyList = (StickyListHeadersListView) findViewById(R.id.list);
		LinearLayout footView = (LinearLayout) LayoutInflater.from(mContext).inflate(R.layout.file_browse_footview, null);
		stickyList.addFooterView(footView);
		stickyList.setOnStickyHeaderChangedListener(this);
		stickyList.setOnStickyHeaderOffsetChangedListener(this);
		stickyList.setEmptyView(findViewById(R.id.empty));
		stickyList.setDrawingListUnderStickyHeader(true);
		stickyList.setAreHeadersSticky(true);
		stickyList.setAdapter(mAdapter);
		stickyList.setOnTouchListener(this);
		//去掉滚动条
		stickyList.setFastScrollEnabled(false);
		stickyList.setOnScrollListener(new PauseOnScrollListener(DMImageLoader.getInstance(), false, true));
		
		Intent mIntent  = getIntent();
		if(mIntent != null) {
			bucketId = mIntent.getLongExtra("bucketId", -1L);
			mCurPath = mIntent.getStringExtra("CurPath");
		}
		
		initUploadType(UploadType.Picture,mCurPath);
		
		title_bar.setState(STATE.SEL_ALL);
		mAdapter.setmMode(Mode.MODE_EDIT);
		
	}
	
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		
	}
	
	/**
	 * 更改所有的图片的selected
	 * @param isChoose
	 */
	private void setListSelected(boolean isChoose) {
		if(list == null && list.size() <= 0) return;
		for(int i = 0 ;i<list.size();i++) {
			ImageFolder mPictrueGroup = list.get(i);
			for(int j = 0; j <mPictrueGroup.list.size();j++) {
				mPictrueGroup.list.get(j).selected = isChoose;
			}
		}
		
	}
	
	private int getWindowWidth() {
		DisplayMetrics dpy = new DisplayMetrics();
		this.getWindow().getWindowManager().getDefaultDisplay().getMetrics(dpy);
		return dpy.widthPixels;
	}
	public int getImageRLWidth(int screenWith) {
		return (screenWith - dip2px(mContext, 24)) / 4;
	}
	
	 public int dip2px(Context context, float dpValue) {
	        final float scale = context.getResources().getDisplayMetrics().density;
	        return (int) (dpValue * scale + 0.5f);
	}
	
	
	protected void loadfiles(){
		// TODO Auto-generated method stub
		super.onStart();
		
		if(bucketId == -1) return;
		
		CommonAsync.Runnable mRunnable = new CommonAsync.Runnable() {
			
			@Override
			public void stop() {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public Object run() {
				// TODO Auto-generated method stub
				ArrayList<ImageFolder> ret = null;
				tmpList = SystemDBTool.getPicFileByBucketId(mContext,bucketId);
				if (tmpList != null) {
					return formatList(tmpList);
				}
				return ret;
				
			
			}
		};
		CommonAsync.CommonAsyncListener mCommonAsyncListener = new CommonAsync.CommonAsyncListener() {
			
			@Override
			public void onResult(Object ret) {
				// TODO Auto-generated method stub
				hideLoadingView();
				if(ret != null) {
					ArrayList<ImageFolder> tmp = (ArrayList<ImageFolder>) ret;
					if(tmp != null) {
						list.clear();
						list.addAll(tmp);	
						mAdapter.notifyDataSetChanged();
						
						setSelectedNumber(0);
						setListSelected(false);
						
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
/**
 * 按照日期换行，按照4个作为一个组
 * @param list
 * @return
 */
	
	private ArrayList<ImageFolder> formatList(ArrayList<MediaInfo> list) {
		ArrayList<ImageFolder> pieceList = new ArrayList<ImageFolder>();
		ArrayList<MediaInfo> piece = new ArrayList<MediaInfo>();
		long lastDateParentId  = list.get(0).dateParentId;
		int temp = 0;
		for (int i = 0; i < list.size(); i++) {
			temp ++;
			if(list.get(i).dateParentId != lastDateParentId) {
				lastDateParentId = list.get(i).dateParentId;
				if(piece.size() > 0) {
					pieceList.add(new ImageFolder(piece,
							piece.get(0).dateParentId, TimeTool.formatPicDate(
									mContext, piece.get(0).mLastModify)));
					//日期不一样了，OK 另开一行
					piece = new ArrayList<MediaInfo>();
					temp = 1;
				}
			}
			if (temp % 4 != 0) {
				piece.add(list.get(i));
			} else {
				//满4个了，OK另开一行
				piece.add(list.get(i));
				pieceList.add(new ImageFolder(piece,
						piece.get(0).dateParentId, TimeTool.formatPicDate(
								mContext, piece.get(0).mLastModify)));
				piece = new ArrayList<MediaInfo>();
			}
		}
		if (piece.size() != 0)
			pieceList
					.add(new ImageFolder(piece, piece.get(0).dateParentId,
							TimeTool.formatPicDate(mContext,
									piece.get(0).mLastModify)));
		return pieceList;
	}


	@Override
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void onStickyHeaderOffsetChanged(StickyListHeadersListView l,
			View header, int offset) {
	}

	@Override
	public void onStickyHeaderChanged(StickyListHeadersListView l, View header,
			int itemPosition, long headerId) {
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		v.setOnTouchListener(null);
		return false;
	}
/**
 * 此为选中了的一个回调
 */
	@Override
	public void OnSelectChange() {
		// TODO Auto-generated method stub
		mSelectedList.clear();
		if(list == null && list.size() <= 0) return;
		for(int i = 0 ;i<list.size();i++) {
			ImageFolder mPictrueGroup = list.get(i);
			for(int j = 0; j <mPictrueGroup.list.size();j++) {
				if(mPictrueGroup.list.get(j).selected) {
					mSelectedList.add(mPictrueGroup.list.get(j));
				}
			}
		}
		
		setSelectedNumber(mSelectedList.size());
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		int i = v.getId();
		if (i == R.id.titlebar_left) {
			finish();

		}
	}
	
	public void onEventMainThread(UploadEndEvent event) {  
		finish();
	} 
	
	public void onEventMainThread(DisconnectEvent event){
		finish();
	}
	
	@Override
	public void OnClick(STATE actionId) {
		// TODO Auto-generated method stub
		switch(actionId) {
		case EDIT:
			mAdapter.setmMode(Mode.MODE_EDIT);
			setSelectedNumber(0);
			setListSelected(false);
			break;
		case SEL_ALL:
			setListSelected(true);
			setSelectedNumber(tmpList.size());
			mSelectedList.addAll(tmpList);
			break;
		case SEL_NONE:
			setListSelected(false);
			setSelectedNumber(0);
			break;
		
		}
		mAdapter.notifyDataSetChanged();
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		EventBus.getDefault().unregister(this);
	}
	
}