package com.dmsys.airdiskpro.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.dmairdisk.aodplayer.util.CommonAsync;
import com.dmsys.airdiskpro.adapter.UploadFileAdapter;
import com.dmsys.airdiskpro.event.DisconnectEvent;
import com.dmsys.airdiskpro.event.UploadEndEvent;
import com.dmsys.airdiskpro.utils.FileUtil;
import com.dmsys.airdiskpro.utils.SystemDBTool;
import com.dmsys.airdiskpro.view.FileActionView.OnBackIconClickListener;
import com.dmsys.airdiskpro.view.FileActionView.OnClickListener;
import com.dmsys.airdiskpro.view.FileActionView.STATE;
import com.dmsys.dmsdk.model.DMFile;
import com.dmsys.mainbusiness.R;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

public class UploadFileActivity extends UploadBaseActivity implements OnClickListener,OnBackIconClickListener,View.OnClickListener, OnItemClickListener{

	
	private UploadFileAdapter mAdapter;
	private ListView list;
	private List<DMFile> DMFileList = new ArrayList<>();
	private Context mContext;
	private String mCurPath;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		setContentView(R.layout.file_browse_upload_file_activity);
		super.onCreate(savedInstanceState);
		mContext = this;
		Intent tmpIntent = getIntent();
		
		if(tmpIntent != null) {
			int tmp = tmpIntent.getIntExtra("FileType", FileType.AUDIO.ordinal());
			mFileType = FileType.values()[tmp];
			
			mCurPath = tmpIntent.getStringExtra("CurPath");
			
		}
		initView();
		
		switch(mFileType) {
		case AUDIO:
			title_bar.setTitleText(getString(R.string.DM_Navigation_Upload_Music));
			initUploadType(UploadType.Audio,mCurPath);
			break;
		case VIODE:
			title_bar.setTitleText(getString(R.string.DM_Navigation_Upload_Videos));
			initUploadType(UploadType.Video,mCurPath);
			break;
		}
		
		title_bar.setState(STATE.SEL_ALL);
		mAdapter.setmMode(Mode.MODE_EDIT);
		
		loadFiles();
		
	}
	
	protected void loadFiles() {
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
				ArrayList<DMFile> ret = null;
				switch(mFileType) {
				case AUDIO:
					ret = SystemDBTool.getAudioFiles(mContext);
					break;
				case VIODE:
					ret = SystemDBTool.getVideoFiles(mContext);
					break;
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
					ArrayList<DMFile> tmpList  = (ArrayList<DMFile>)ret;
					DMFileList.clear();
					DMFileList.addAll(tmpList);	
					FileUtil.sortFileListByTime(DMFileList);
					mAdapter.notifyDataSetChanged();
					
					setSelectedNumber(0);
					setListSelected(false);
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

	private void initView() {
		
		EventBus.getDefault().register(this);
		
		list = (ListView) findViewById(R.id.list);
		mAdapter = new UploadFileAdapter(DMFileList, this);
		LinearLayout footView = (LinearLayout) LayoutInflater.from(mContext)
                .inflate(R.layout.file_browse_footview, null);
		list.addFooterView(footView, null, false);
		list.setAdapter(mAdapter);
		list.setOnItemClickListener(this);
	}
	
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		
	}
	private void reset() {
		title_bar.setState(STATE.NORMAL);
		mAdapter.setmMode(Mode.MODE_NORMAL);
		setListSelected(false);
		mAdapter.notifyDataSetChanged();
		switch(mFileType) {
		case AUDIO:
			title_bar.setTitleText(getString(R.string.DM_Navigation_Upload_Music));
			break;
		case VIODE:
			title_bar.setTitleText(getString(R.string.DM_Navigation_Upload_Videos));
			break;
		}
		
		hideUploadBottomBar();
	}
	
	public void onEventMainThread(UploadEndEvent event) {  
		finish();
	} 
	
	public void onEventMainThread(DisconnectEvent event){
		finish();
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		EventBus.getDefault().unregister(this);
	}
	

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		int i = v.getId();
		if (i == R.id.titlebar_left) {
			finish();

		}
	}

	/**
	 * 更改所有的图片的selected
	 * @param isChoose
	 */
	private void setListSelected(boolean isChoose) {
		if(DMFileList == null && DMFileList.size() <= 0) return;
		for(int i = 0 ;i<DMFileList.size();i++) {
			DMFile mDMFile = DMFileList.get(i);
			mDMFile.selected = isChoose;
		}
		
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
			setSelectedNumber(DMFileList.size());
			mSelectedList.clear();
			mSelectedList.addAll(DMFileList);
			break;
		case SEL_NONE:
			setListSelected(false);
			setSelectedNumber(0);
			break;
		
		}
		mAdapter.notifyDataSetChanged();
	}

	

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub
		Mode mMode = mAdapter.getmMode();
		if (mMode == Mode.MODE_NORMAL) {
			
		} else {
			mSelectedList.clear();
			boolean selected = !DMFileList.get(position).selected;
			DMFileList.get(position).selected = selected; // 反选
			
			ImageView iv = (ImageView)view.findViewById(R.id.fileexplorer_list_item_operatinobtn);
			iv.setSelected(selected);
			//通知选中的个数已经发生改变
			for (int i = 0; i < DMFileList.size(); i++) {
				if (DMFileList.get(i).selected) {
					mSelectedList.add(DMFileList.get(i));
				}
			}
			setSelectedNumber(mSelectedList.size());
		}
	}

}
