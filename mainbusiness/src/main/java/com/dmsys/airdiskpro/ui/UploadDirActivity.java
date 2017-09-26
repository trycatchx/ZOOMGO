package com.dmsys.airdiskpro.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;

import com.dmsys.airdiskpro.event.DisconnectEvent;
import com.dmsys.airdiskpro.event.UploadEndEvent;
import com.dmsys.airdiskpro.view.FileActionView.OnBackIconClickListener;
import com.dmsys.airdiskpro.view.FileActionView.OnClickListener;
import com.dmsys.airdiskpro.view.FileActionView.STATE;
import com.dmsys.airdiskpro.view.FolderSelector;
import com.dmsys.airdiskpro.view.UploadDirView;
import com.dmsys.airdiskpro.view.UploadDirView.LongPressEvent;
import com.dmsys.airdiskpro.view.UploadDirView.OnDirViewStateChangeListener;
import com.dmsys.airdiskpro.view.UploadDirView.Onload;
import com.dmsys.dmsdk.model.DMFile;
import com.dmsys.mainbusiness.R;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import de.greenrobot.event.EventBus;

public class UploadDirActivity extends UploadBaseActivity implements OnClickListener,OnBackIconClickListener,View.OnClickListener{
	FolderSelector mPathView;

	private String[] mFolderArray;
	private String mRootName;
	UploadDirView  lv_file_list;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		setContentView(R.layout.activity_uploaddir);
		super.onCreate(savedInstanceState);
		initViews();
		title_bar.setTitleText(getString(R.string.DM_Navigation_Upload_Files));
		title_bar.setEditBtnText(getString(R.string.DM_Select));
	}

	private void initViews() {
		EventBus.getDefault().register(this);
		mPathView = (FolderSelector) findViewById(R.id.et_navigate);
		mPathView.setOnClickListener(new FolderSelector.ItemOnClickListener() {

			@Override
			public void onClick(int idx) {
				// TODO Auto-generated method stub
				if (mFolderArray != null) {
					int step = mFolderArray.length - idx - 1;
					if (step > 0) {
						lv_file_list.toUpperPathByStep(step);
					}
				}
			}

		});
		lv_file_list = (UploadDirView) findViewById(R.id.lv_file_list);
		
		
		lv_file_list.setmOnSelectChangeListener(new OnSelectChangeListener() {
			
			@Override
			public void OnSelectChange() {
				// TODO Auto-generated method stub
				mSelectedList.clear();
				mSelectedList.addAll(lv_file_list.getSelectFiles());
				setSelectedNumber(mSelectedList.size());
			}
		});
			
		
		lv_file_list.setOnloadListener(new Onload() {

			@Override
			public void begin() {
				// TODO Auto-generated method stub
				showLoadingView();
			}

			@Override
			public void end() {
				// TODO Auto-generated method stub
				hideLoadingView();
			}

		});
		
		lv_file_list.setOnDirViewStateChangeListener(new OnDirViewStateChangeListener() {

			public void onChange(Mode state, String currentPath,List<DMFile> fileList) {
				if (fileList == null)
					return;
				String pathName = currentPath;
				if (state == Mode.MODE_NORMAL) {
					unselectAll(fileList);
					String rPath = lv_file_list.getRelativePath(pathName);
					System.out.println("rrr rpath:"+rPath);
					
					if (rPath.equals("") || rPath.equals("/")) {
						mFolderArray = null;
						title_bar.showEditBtn(false);
					} else {
						String[] array = rPath.split("/");
						mFolderArray = Arrays.copyOfRange(array, 1,array.length);
						title_bar.showEditBtn(true);
					}

					int location = lv_file_list.getLocation();
					try {
						if (location == DMFile.LOCATION_LOCAL) { 
							mRootName = getString(R.string.DM_Control_MobileResource);
						} 
					} catch (Exception e) {
						// TODO: handle exception
					}

					mPathView.setFoder(mRootName, mFolderArray);
				}
			}

		});
		
		View emptyView = findViewById(R.id.emptyRl);
		lv_file_list.init(this);
		
		Intent tmpIntent = getIntent();
		
		if(tmpIntent != null) {
			String mCurPath = tmpIntent.getStringExtra("CurPath");
			initUploadType(UploadType.File,mCurPath);
		}
		
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// System.out.println("back keyCode:"+keyCode +
		// ",back :"+mFileListView.isCanToUpper());
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			//取消編輯状态
			if(lv_file_list != null && lv_file_list.getMode() == Mode.MODE_EDIT) {
				title_bar.setState(STATE.NORMAL);
				title_bar.setEditBtnText(getString(R.string.DM_Select));
				lv_file_list.setMode(Mode.MODE_NORMAL);
				lv_file_list.unselectAll();
				title_bar.setTitleText(getString(R.string.DM_Navigation_Upload_Files));
				hideUploadBottomBar();
			} else {
				boolean canToUpper = lv_file_list.isCanToUpper();
				System.out.println("test123:"+canToUpper);
				if (canToUpper) {
					//返回上一层
					lv_file_list.toUpperPath();
					return true;
				} else {
					//退出
					finish();
				}
			}
			
		}
		return false;
	}
	
	public static void unselectAll(Collection collection) {
		if(null == collection) {
			return;
		}
		Iterator<DMFile> iter = collection.iterator();
		while (iter.hasNext()) {
			iter.next().selected = false;
		}
	}
	
	
	public void onEventMainThread(LongPressEvent event) {  
		title_bar.setState(STATE.SEL_ALL);
		setSelectedNumber(1);
		
		mSelectedList.clear();
		mSelectedList.addAll(lv_file_list.getSelectedFiles());
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

	@Override
	public void OnClick(STATE actionId) {
		// TODO Auto-generated method stub
		switch(actionId) {
		case EDIT:
			lv_file_list.setMode(Mode.MODE_EDIT);
			lv_file_list.unselectAll();
			setSelectedNumber(0);
			break;
		case SEL_ALL:
			lv_file_list.selectAll();
			setSelectedNumber(lv_file_list.getmFileListSize());
			mSelectedList.clear();
			mSelectedList.addAll(lv_file_list.getSelectedFiles());
			break;
		case SEL_NONE:
			lv_file_list.unselectAll();
			setSelectedNumber(0);
			break;
		}
	}

}
