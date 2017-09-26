package com.dmsys.dropbox.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dmsys.dropbox.activity.MyDropBoxAllFileFragment.DropBoxEditState;
import com.dmsys.dropbox.api.DMDropboxAPI;
import com.dmsys.dropbox.view.BaseDirView;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dmsys.mainbusiness.R;

import de.greenrobot.event.EventBus;


/**
 * @function
 *
 * 
 * @author ZHANG
 *
 */
public class MyDropBoxActivity extends FragmentActivity   {

	
	private RelativeLayout rtly_content; 
//	private MyDropBoxAuthFragment mMyDropBoxAuthFragment;
	private MyDropBoxAllFileFragment mMyDropBoxAllFileFragment;
	private DMDropboxAPI mApi;
	
	private TextView tv_center;
	private ImageView iv_left,iv_right;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Basic Android widgets
		setContentView(R.layout.activity_my_dropbox);
		initVars();
		initViews();
		initRegister();
	}
	
	@Override
	protected void onRestart() {
		super.onRestart();
		 AndroidAuthSession session = (AndroidAuthSession)(mApi.getSession());
		 //home 键
		 if(session.isLinked()) {
			 return;
		 }
		 //认证成功的回调界面
		 if(session.authenticationSuccessful()) {
			 session.finishAuthentication();
			 mApi.storeAccessToken(session.getOAuth2AccessToken());
			 setMyDropBoxAllFileView();
		 } else {
			 finish();
			 //Toast.makeText(this, "auth fail", Toast.LENGTH_LONG).show();
		 }
	}
	
	
	private void initVars() {
		DMDropboxAPI.mApplication = getApplicationContext();
	    mApi = DMDropboxAPI.getInstance();
	}
	
	private void initViews() {
		 AndroidAuthSession session = (AndroidAuthSession)(mApi.getSession());
		 if(session.isLinked()) {
			 setMyDropBoxAllFileView();
		 } else {
			 session.startOAuth2Authentication(this);
		 }
	}
	private void initRegister() {
		//退出时发个通知，关闭一些服务
		EventBus.getDefault().register(this);
	}
	/**
	 * dropBox
	 *
	 */
	private void setMyDropBoxAllFileView() {
		FragmentManager fm = this.getSupportFragmentManager();
		FragmentTransaction transaction = fm.beginTransaction();
		
		
		if (fm.findFragmentByTag("MyDropBoxAllFileFragment") == null) {
			mMyDropBoxAllFileFragment = new MyDropBoxAllFileFragment();
			
			Bundle bundle = new Bundle();
			bundle.putInt(MyDropBoxAllFileFragment.PAGER_TYPE, 
					BaseDirView.FILE_TYPE_AIRDISK);
			mMyDropBoxAllFileFragment.setArguments(bundle);
			
			transaction.replace(R.id.rtly_content, mMyDropBoxAllFileFragment,
					"MyDropBoxAllFileFragment");
			if (!fm.isDestroyed()) {
				transaction.commitAllowingStateLoss();
			}
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (mMyDropBoxAllFileFragment != null && !mMyDropBoxAllFileFragment.isHidden() && mMyDropBoxAllFileFragment.isEditMode()) {
				mMyDropBoxAllFileFragment.unselectAll();
				mMyDropBoxAllFileFragment.setEditState(DropBoxEditState.STATE_NORMAL);
				return true;
			}
			if (mMyDropBoxAllFileFragment != null && !mMyDropBoxAllFileFragment.isHidden()) {
					if (mMyDropBoxAllFileFragment.isCanToUpper()) {
						mMyDropBoxAllFileFragment.toUpper();
						return true;
					}
			}
		}
		return super.onKeyDown(keyCode, event);
	}
	
	
//	public void onEventAsync(ExitVodplayer event) {
//		DropBoxVodUrlConversionHelper.getInstance().stopHttpServer();
//	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		EventBus.getDefault().unregister(this);
		super.onDestroy();
	}
	
	
	
	
	
	
}
