package com.dmsys.airdiskpro.ui;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;

import com.dmsys.airdiskpro.filemanager.IItemLoader;
import com.dmsys.airdiskpro.filemanager.PictrueGroupLoader;
import com.dmsys.airdiskpro.ui.MainFragment.IPager;
import com.dmsys.airdiskpro.view.FilePictrueView;
import com.dmsys.airdiskpro.view.IFileExplorer;
import com.dmsys.dmsdk.model.DMFileCategoryType;
import com.dmsys.dmsdk.util.DMFileTypeUtil;
import com.dmsys.mainbusiness.R;

public class FilePictruePager extends LazyLoadFragment implements IPager{
	private int mScreenWidth = 0;
	public static final String PAGER_MODE = "PAGER_MODE";
	public static final String PAGER_TYPE = "PAGER_TYPE";
	public static final String PAGER_LOCATION = "PAGER_LOCATION";
	private FilePictrueView mFileView;
	private int mType;
	private Activity mContext;
	
	private boolean isPrepared;
	private boolean isLoaded;
	
	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
		
		DisplayMetrics dpy = new DisplayMetrics();
		activity.getWindow().getWindowManager().getDefaultDisplay().getMetrics(dpy);
		mScreenWidth = dpy.widthPixels;
		
		mContext = getActivity();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		Bundle bundle = getArguments();
		//这个是保存图片分类fragment切换的时候UImode，只能用这种static的形式去记录
		int type = bundle.getInt(PAGER_TYPE, -1);
		
		ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.file_pager_container, container, false);		
		mFileView = new FilePictrueView(mContext);
		mFileView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		
		DMFileCategoryType categoryType = DMFileTypeUtil.getFileCategoryTypeByOrdinal(type);
		if (categoryType == DMFileCategoryType.E_PICTURE_CATEGORY) {
			IItemLoader itemloader = new PictrueGroupLoader();
			mFileView.initialize(categoryType, itemloader, mScreenWidth);
		}
		
		mType = type;
		System.out.println("FilePictruePager onCreateView");
		
		isPrepared = true;
        lazyLoad();
		
		rootView.addView(mFileView);
		return rootView;
	}
	
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		System.out.println("FilePictruePager onResume");
	}
	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		System.out.println("FilePictruePager onPause");
	}
	
	public IFileExplorer getFileView() {
		return mFileView;
		// TODO Auto-generated method stub
	}
	
	@Override
	public void onDestroyView() {
		// TODO Auto-generated method stub
		super.onDestroyView();
		System.out.println("FilePictruePager onDestroyView");
	}

	public void reloadItems() {
		// TODO Auto-generated method stub
		if (!isLoaded) {
			mFileView.reloadItems(true);
			isLoaded = true;
		}
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		isLoaded = false;
		System.out.println("FilePictruePager onDestroy");
	}

	@Override
	protected void lazyLoad() {
		// TODO Auto-generated method stub
		System.out.println("FilePictruePager lazyLoad");
		if (!isVisible || !isPrepared) {
			return;
		}else {
			reloadItems();
		}
	}

	@Override
	public void resetPage() {
		// TODO Auto-generated method stub
		isLoaded = false;
	}


}
