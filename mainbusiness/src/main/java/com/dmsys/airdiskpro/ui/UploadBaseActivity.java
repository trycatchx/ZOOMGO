package com.dmsys.airdiskpro.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dmairdisk.aodplayer.util.CommonAsync;
import com.dmsys.airdiskpro.filemanager.FileOperationService;
import com.dmsys.airdiskpro.view.FileActionView;
import com.dmsys.airdiskpro.view.FileActionView.OnBackIconClickListener;
import com.dmsys.dmsdk.DMSdk;
import com.dmsys.dmsdk.model.DMFile;
import com.dmsys.dmsdk.model.DMStorageInfo;
import com.dmsys.dropbox.activity.MyDropBoxPathSelectActivity;
import com.umeng.analytics.MobclickAgent;
import com.dmsys.mainbusiness.R;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UploadBaseActivity extends BaseActionActivity{
	
	private Animation AnimHide,AnimShow;

	public enum UploadType{
		Picture,
		Audio,
		Video,
		File,
	};
	public enum DestType {
		Udisk,
		DropBox
		//...
	}
	
	private TextView text_path;
	private Button btn_upload;
	public LinearLayout llyt_date_pic_upload_to;
	public RelativeLayout layout_path_select;
	
	public CommonAsync mCommonAsync;
	public ExecutorService FULL_TASK_EXECUTOR = (ExecutorService) Executors.newCachedThreadPool(); 
	public FileActionView title_bar;
	public FileType mFileType = FileType.AUDIO;
	public ViewGroup mLoadingView;
	public List<DMFile> mSelectedList = new ArrayList<>();
	public UploadType mUploadType;
	public String mDesPath;
	public DestType mDestType = DestType.Udisk;
	
	public enum  FileType{
		AUDIO,VIODE
	}
	
	public enum Mode {
		MODE_EDIT,MODE_NORMAL
	} 
	
	public interface OnSelectChangeListener {
		public void OnSelectChange();
	}
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		AnimHide = AnimationUtils.loadAnimation(this,
				R.anim.buttom_slide_hide_bar_hide);
		AnimShow = AnimationUtils.loadAnimation(this,
				R.anim.buttom_slide_hide_bar_show);
		AnimHide.setAnimationListener(new MyAnimationListener());
		initViews(this);
		
		
       Intent mIntent = getIntent();
		
		if(mIntent != null) {
			int index = mIntent.getIntExtra("DestType", DestType.Udisk.ordinal());
			if(index < DestType.values().length) {
				mDestType  = DestType.values()[index];
			}
			
		}
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		MobclickAgent.onResume(this);
		init(this);
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		MobclickAgent.onPause(this);
	}
	
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		unInit();
	}
	
	private void initViews(Context context) {
		
		btn_upload = (Button) findViewById(R.id.btn_date_pic_upload);
		btn_upload.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (mSelectedList.size() == 0) {
					Toast.makeText(getBaseContext(), R.string.DM_FileOP_Warn_Select_File, Toast.LENGTH_SHORT).show();
					return;
				}
				
				if (mDesPath == null) {
					Toast.makeText(getBaseContext(), R.string.DM_MDNS_No_Disk, Toast.LENGTH_SHORT).show();
				}else {
					switch(mDestType) {
					case DropBox:
						doFileUploadOperation(FileOperationService.FILE_OP_UPLOAD, mSelectedList,mDesPath,mDestType);
						break;
					case Udisk:
						doFileUploadOperation(FileOperationService.FILE_OP_UPLOAD, mSelectedList,mDesPath);
						break;
					}
					
				}
			}
		});
		
		text_path = (TextView) findViewById(R.id.text_path);
		
		
		llyt_date_pic_upload_to = (LinearLayout) findViewById(R.id.llyt_date_pic_upload_to);
		llyt_date_pic_upload_to.setVisibility(View.GONE);
		
		layout_path_select = (RelativeLayout) findViewById(R.id.rlyt_date_pic_upload_to);
		layout_path_select.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
					switch(mDestType) {
					case DropBox:
						startActivityForResult(new Intent(getBaseContext(), MyDropBoxPathSelectActivity.class), 1111);
						break;
					case Udisk:
						startActivityForResult(new Intent(getBaseContext(), PathSelectActivity.class), 1111);
						break;
				}
			}
		});
		
		
		title_bar = (FileActionView) findViewById(R.id.title_bar);
		title_bar.attachClickListener((FileActionView.OnClickListener) context);
		title_bar.setBackIconClickListener((OnBackIconClickListener) context);	
		mLoadingView = ((ViewGroup) findViewById(R.id.loading));
		showLoadingView();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 1111 && resultCode == RESULT_OK) {
			mDesPath = data.getStringExtra("DES_PATH");
			switch (mDestType) {
			case DropBox:
				String showPath = mDesPath;
				if(showPath.equals("/")) {
					showPath = "DropBox";
				} else {
					showPath = "DropBox"+mDesPath;
				}
				text_path.setText(String.format(getString(R.string.DM_Bottom_Bar_Button_Uploadto), showPath));
				break;
			case Udisk:
				text_path.setText(String.format(getString(R.string.DM_Bottom_Bar_Button_Uploadto), mDesPath));
				break;
			}
		}
	}
	
	public void initUploadType(UploadType type,String curPath){
		mUploadType = type;
		
		
		
		
		switch (mDestType) {
		case DropBox:
			mDesPath = curPath;
			String showPath = curPath;
			if(mDesPath.equals("/")) {
				showPath = "DropBox";
			} else {
				showPath = "DropBox"+curPath;
			}
			text_path.setText(String.format(getString(R.string.DM_Bottom_Bar_Button_Uploadto), showPath));
			break;
		case Udisk:
			DMStorageInfo info = DMSdk.getInstance().getStorageInfo();
			if (info != null && info.getStorages() != null && info.getStorages().size() > 0) {
				if (curPath.equals("CLASSIFY")){
					mDesPath = info.getStorages().get(0).mPath;
				}else {
					
					if (curPath.equals("") || curPath.equals("/")) {
							mDesPath = info.getStorages().get(0).mPath;
					} else {
						mDesPath = curPath;
					}
					
				}
				
				text_path.setText(String.format(getString(R.string.DM_Bottom_Bar_Button_Uploadto), mDesPath));
			}
			break;
		}
		
	}
	
	
	public void showUploadBottomBar() {
		llyt_date_pic_upload_to.setVisibility(View.VISIBLE);
		llyt_date_pic_upload_to.startAnimation(AnimShow);
	}
	public void hideUploadBottomBar() {
		llyt_date_pic_upload_to.setVisibility(View.VISIBLE);
		llyt_date_pic_upload_to.startAnimation(AnimHide);
	}
	
	public void showLoadingView() {
		  mLoadingView.setVisibility(View.VISIBLE);
	}
	
	public void hideLoadingView() {
		mLoadingView.setVisibility(View.GONE);
	}
	
	//显示上传的文件的个数
	public void setSelectedNumber(int numBer) {
		
		title_bar.setTitleText(String.format(getString(R.string.DM_Navigation_Upload_Num), numBer));
		if(llyt_date_pic_upload_to.getVisibility() != View.VISIBLE) {
			showUploadBottomBar();
		}
		btn_upload.setText(String.format(getString(R.string.DM_upload), numBer));
	}
	
	
	/**
	 * 动画结束隐藏控件
	 * @author Administrator
	 *
	 */
	
	class MyAnimationListener implements AnimationListener {

		@Override
		public void onAnimationStart(Animation arg0) {}

		@Override
		public void onAnimationRepeat(Animation arg0) {}

		@Override
		public void onAnimationEnd(Animation arg0) {
			// TODO Auto-generated method stub
			llyt_date_pic_upload_to.setVisibility(View.GONE);
		}
	}


	@Override
	public void onOperationEnd(String opt) {
		// TODO Auto-generated method stub
		
	}

	
}
