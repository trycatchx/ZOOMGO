package com.dmsys.airdiskpro.ui;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dmsys.airdiskpro.ui.MainFragment.IPager;
import com.dmsys.airdiskpro.view.FileManagerDirView;
import com.dmsys.airdiskpro.view.FileManagerDirView.Onload;
import com.dmsys.airdiskpro.view.IFileExplorer;
import com.dmsys.dmsdk.util.DMFileTypeUtil;
import com.dmsys.mainbusiness.R;

import java.util.Date;

import rx.subscriptions.CompositeSubscription;

public class FilePager extends LazyLoadFragment implements IPager {

	private int mScreenWidth = 0;
	public static final String PAGER_TYPE = "PAGER_TYPE";
	private int mType;
	
	View parent; 
	private FileManagerDirView mFileView;
	private ViewGroup mLoadingView;
	
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
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		System.out.println("TT onCreateView");
		Bundle bundle = getArguments();
		mType = bundle.getInt(PAGER_TYPE, -1);
		parent = (ViewGroup) inflater.inflate(R.layout.file_explorer, container, false);
		initViews();
		
		isPrepared = true;
        lazyLoad();
        
        ViewGroup view = (ViewGroup)parent.getParent();
        if(view != null) {
        	view.removeView(parent);
        }
		
		return parent;
	}
	public CompositeSubscription mSubscriptions = new CompositeSubscription();

	private void initViews() {
		// TODO Auto-generated method stub
		mFileView = (FileManagerDirView) parent.findViewById(R.id.lv_file_list);
		mFileView.init(mType,mSubscriptions);
		
		mLoadingView = ((ViewGroup) parent.findViewById(R.id.loading));
		mFileView.setOnloadListener(new Onload() {
			
			@Override
			public void begin() {
				// TODO Auto-generated method stub
				System.out.println("start get2:"+new Date(System.currentTimeMillis()));
				mLoadingView.setVisibility(View.VISIBLE);
			}
			
			@Override
			public void end() {
				// TODO Auto-generated method stub
				System.out.println("end get2:"+new Date(System.currentTimeMillis()));
				mLoadingView.setVisibility(View.GONE);
			}
			
		});
		
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		System.out.println("TT onResume");
	}
	
	@Override
	public void onStop() {
		// TODO Auto-generated method stub
		System.out.println("TT onStop");
		super.onStop();
	}
	
	@Override
	public void onDestroyView() {
		// TODO Auto-generated method stub
		super.onDestroyView();
		Log.d("FilePager", "onDestroyView() " + DMFileTypeUtil.getFileCategoryTypeByOrdinal(mType));
		isLoaded = false;
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		mSubscriptions.unsubscribe();
		super.onDestroy();
	}

	public void reloadItems() {
		// TODO Auto-generated method stub
		System.out.println("TT reloadItems:"+isLoaded);
		if (!isLoaded) {
			mFileView.loadFiles();
			isLoaded = true;
		}
		
	}

	@Override
	protected void lazyLoad() {
		// TODO Auto-generated method stub
		System.out.println("TT lazyLoad");
		if (!isVisible || !isPrepared) {
			return;
		}else {
			reloadItems();
		}
	}

	public IFileExplorer getFileView(){
		return mFileView;
	}

	@Override
	public void resetPage() {
		// TODO Auto-generated method stub
		isLoaded = false;
	}

	
}
