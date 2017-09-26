package com.dmsys.airdiskpro.setting;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.dm.baselib.BaseValue;
import com.dm.xunlei.udisk.Network.View.CustomButtonView1;
import com.dm.xunlei.udisk.Network.View.CustomButtonView1.onToogleClickListener;
import com.dmairdisk.aodplayer.util.CommonAsync;
import com.dmsys.airdiskpro.event.DisconnectEvent;
import com.dmsys.dmsdk.DMSdk;
import com.dmsys.dmsdk.model.DMRet;
import com.dmsys.dmsdk.model.DMSupportFunction;
import com.dmsys.mainbusiness.R;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.greenrobot.event.EventBus;

public class MoreSettingsActivity extends Activity implements OnClickListener {

	private View layout_hide,layout_mount;
	private CustomButtonView1 cbv_hide,cbv_mount;
	
	private boolean mShowHideFiles;
	private boolean mMount;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_more);
		initViews();
	}
	
	private void initViews() {
		// TODO Auto-generated method stub
		EventBus.getDefault().register(this);
		((TextView) findViewById(R.id.titlebar_title)).setText(R.string.DM_Setting_More);
		FrameLayout back = (FrameLayout) findViewById(R.id.layout_back);
		ImageView titlebar_left = (ImageView) findViewById(R.id.titlebar_left);
		titlebar_left.setImageResource(R.drawable.dm_lib_wifi_back_btn_bg_selector);
		back.setOnClickListener(this);
		
		layout_hide = findViewById(R.id.layout_hide);
		cbv_hide = (CustomButtonView1) findViewById(R.id.cbv_hide);
		cbv_hide.setToToogle();
		cbv_hide.setOnToogleClickListener(new onToogleClickListener() {
			@Override
			public void onClick(boolean toogleOn) {
				setShowHideFiles();
			}
		});
		cbv_hide.setTitle(getResources().getString(R.string.DM_Setting_Show_Hidden_Files));
		//一开始是显示打开，在onstart去检测真是的状态
		cbv_hide.setToogleState(false,false);
		
		layout_mount = findViewById(R.id.layout_mount);
		cbv_mount = (CustomButtonView1) findViewById(R.id.cbv_mount);
		cbv_mount.setToToogle();
		cbv_mount.setOnToogleClickListener(new onToogleClickListener() {
			
			@Override
			public void onClick(boolean toogleOn) {
				setMountPc();
			}
		});
		cbv_mount.setTitle(getResources().getString(R.string.DM_Setting_PC_USB_Mode));
		cbv_mount.setToogleState(true,false);
		
		if (!DMSupportFunction.isSupportFileHide(BaseValue.supportFucntion)) {
			layout_hide.setVisibility(View.GONE);
		}
		
		if (DMSupportFunction.isSupportAdvanceSetting(BaseValue.supportFucntion)){
			findViewById(R.id.sett_advance_layout).setVisibility(View.VISIBLE);
			findViewById(R.id.sett_advance_layout).setOnClickListener(this);
		}
		
	}


	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		getStatusInfo();
	}


	private void getStatusInfo() {
		// TODO Auto-generated method stub
		CommonAsync.Runnable runnable = new CommonAsync.Runnable() {
			
			@Override
			public void stop() {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public Object run() {
				// TODO Auto-generated method stub
				if (DMSupportFunction.isSupportFileHide(BaseValue.supportFucntion)) {
					mShowHideFiles = DMSdk.getInstance().getHideFilesVisible();
				}
				
				if(DMSupportFunction.isSupportControlMount(BaseValue.supportFucntion)){
					mMount = DMSdk.getInstance().getMountPcEnable();
				}
				
				return 0;
			}
		};
		
		CommonAsync.CommonAsyncListener listener = new CommonAsync.CommonAsyncListener() {
			
			@Override
			public void onResult(Object ret) {
				// TODO Auto-generated method stub
				if (DMSupportFunction.isSupportFileHide(BaseValue.supportFucntion)) {
					layout_hide.setVisibility(View.VISIBLE);
					if (mShowHideFiles) {
						cbv_hide.setToogleState(true,false);
					}else {
						cbv_hide.setToogleState(false,false);
					}
				}else {
					layout_hide.setVisibility(View.GONE);
				}
				
				if(DMSupportFunction.isSupportControlMount(BaseValue.supportFucntion)){
					layout_mount.setVisibility(View.VISIBLE);
					if (mMount) {
						cbv_mount.setToogleState(true,false);
					}else {
						cbv_mount.setToogleState(false,false);
					}
				}else {
					layout_mount.setVisibility(View.GONE);
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
		
		CommonAsync async = new CommonAsync(runnable, listener);
		async.executeOnExecutor((ExecutorService) Executors.newCachedThreadPool());
	}

	private void setShowHideFiles(){
		CommonAsync.Runnable runnable = new CommonAsync.Runnable() {
			
			@Override
			public void stop() {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public Object run() {
				// TODO Auto-generated method stub
				return DMSdk.getInstance().setHideFilesVisible(cbv_hide.isToogle_on());
			}
		};
		
		CommonAsync.CommonAsyncListener listener = new CommonAsync.CommonAsyncListener() {
			
			@Override
			public void onResult(Object result) {
				// TODO Auto-generated method stub
				int ret = (int) result;
				System.out.println("sethide ret:"+ret);
				if (ret != DMRet.ACTION_SUCCESS) {
					if (cbv_hide.isToogle_on()) {
						cbv_hide.setToogleState(false,false);
					}else {
						cbv_hide.setToogleState(true,false);
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
		
		CommonAsync async = new CommonAsync(runnable, listener);
		async.executeOnExecutor((ExecutorService) Executors.newCachedThreadPool());
	}

	private void setMountPc(){
		CommonAsync.Runnable runnable = new CommonAsync.Runnable() {
			
			@Override
			public void stop() {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public Object run() {
				// TODO Auto-generated method stub
				return DMSdk.getInstance().setMountPcEnable(cbv_mount.isToogle_on());
			}
		};
		
		CommonAsync.CommonAsyncListener listener = new CommonAsync.CommonAsyncListener() {
			
			@Override
			public void onResult(Object result) {
				// TODO Auto-generated method stub
				int ret = (int) result;
				System.out.println("cbv_mount ret:"+ret);
				if (ret != DMRet.ACTION_SUCCESS) {
					if (cbv_mount.isToogle_on()) {
						cbv_mount.setToogleState(false,false);
					}else {
						cbv_mount.setToogleState(true,false);
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
		
		CommonAsync async = new CommonAsync(runnable, listener);
		async.executeOnExecutor((ExecutorService) Executors.newCachedThreadPool());
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
		if (i == R.id.sett_advance_layout) {
			String webSite = "http://" + BaseValue.Host + "/a_advanced2.shtml?deviceType=app";
			Uri uri = Uri.parse(webSite);
			startActivity(new Intent(Intent.ACTION_VIEW, uri));

		} else if (i == R.id.layout_back) {
			finish();

		} else {
		}
	}
	
}
