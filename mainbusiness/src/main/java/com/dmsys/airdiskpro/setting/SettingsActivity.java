package com.dmsys.airdiskpro.setting;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dm.baselib.BaseValue;
import com.dm.xunlei.udisk.wificonnect.UDiskWiFiSSIDSettingsActivity;
import com.dmairdisk.aodplayer.util.CommonAsync;
import com.dmairdisk.aodplayer.util.CommonAsync.CommonAsyncListener;
import com.dmsoftwareupgrade.api.DMSoftwareUpgrade;
import com.dmsoftwareupgrade.api.DMSoftwareUpgrade.UpdateModeType;
import com.dmsys.airdiskpro.event.NewFwEvent;
import com.dmsys.airdiskpro.event.PasswordChangeEvent;
import com.dmsys.airdiskpro.filemanager.FileOperationHelper;
import com.dmsys.airdiskpro.utils.ConvertUtil;
import com.dmsys.airdiskpro.utils.HandlerUtil;
import com.dmsys.airdiskpro.utils.NetHelper;
import com.dmsys.airdiskpro.utils.NetWorkUtil;
import com.dmsys.airdiskpro.view.CircleProgressDialog;
import com.dmsys.airdiskpro.view.MessageDialog;
import com.dmsys.airdiskpro.view.UDiskBaseDialog;
import com.dmsys.airdiskpro.view.UpgradeFwTaskDialog;
import com.dmsys.dmsdk.DMSdk;
import com.dmsys.dmsdk.model.DMOTA;
import com.dmsys.dmsdk.model.DMSupportFunction;
import com.umeng.update.UmengUpdateListener;
import com.umeng.update.UpdateResponse;
import com.umeng.update.UpdateStatus;
import com.dmsys.mainbusiness.R;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.greenrobot.event.EventBus;

public class SettingsActivity extends Activity implements OnClickListener {

	private static final int MSG_CLEAR_CACHE_FINISHED = HandlerUtil.generateId();
	private static final int MSG_GET_CACHE_FINISHED = HandlerUtil.generateId();
	
	private RelativeLayout sett_fw_update;
	private boolean mClearCacheRunning = false;
	private CircleProgressDialog mCircleDialog = null;
	private HandlerUtil.StaticHandler mHandler;
	private CommonAsync task;
	private CommonAsync internetTask;
	private ImageView iv_setting_fw_new_version_notify;
	private DMSoftwareUpgrade mDMSoftwareUpgrade = DMSoftwareUpgrade.getInstance();
	private LinearLayout layout_device,layout_net,layout_more;
	private RelativeLayout layout_password;
//	private TextView sett_enterPassword_state;
	private Activity mActivity;
	private MessageDialog dialog;
	private MessageDialog feedBackDialog;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		mActivity = this;
		initViews();
		getCacheSize();
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		System.out.println("setting resume");
		
		int type = BaseValue.supportFucntion;
		if (type > 0) {
			layout_device.setVisibility(View.VISIBLE);
			sett_fw_update.setVisibility(View.VISIBLE);
			
			if (DMSupportFunction.isSupportSetPassword(type) || DMSupportFunction.isSupportVault(type)) {
				layout_password.setVisibility(View.GONE);
//				getPasswordState();
			} else {
				layout_password.setVisibility(View.GONE);
			}
			
//			if (DMSupportFunction.isSupportFileHide(type)) {
//				layout_more.setVisibility(View.VISIBLE);
//			}else {
				layout_more.setVisibility(View.VISIBLE);
//			}
			
			if (DMSupportFunction.isSupportRemoteApWithIp(type)) {
				findViewById(R.id.bt_sett_info_layout).setVisibility(View.VISIBLE);
			}else {
				findViewById(R.id.bt_sett_info_layout).setVisibility(View.GONE);
			}
		} else {
			layout_device.setVisibility(View.GONE);
			sett_fw_update.setVisibility(View.GONE);
			
		}
	}

	private void initViews() {
		// TODO Auto-generated method stub
		
		EventBus.getDefault().register(this);
		
		((TextView) findViewById(R.id.titlebar_title)).setText(R.string.DM_Sidebar_Set);
		iv_setting_fw_new_version_notify = (ImageView) findViewById(R.id.iv_setting_fw_new_version_notify);
		if (BaseValue.dmota != null && BaseValue.dmota.flag > 0) {
			iv_setting_fw_new_version_notify.setVisibility(View.VISIBLE);
		}else {
			iv_setting_fw_new_version_notify.setVisibility(View.GONE);
		}
		
		findViewById(R.id.layout_wifi).setOnClickListener(this);
		findViewById(R.id.sett_cacheclear_layout).setOnClickListener(this);
		findViewById(R.id.bt_sett_about_check_update_layout).setOnClickListener(this);
		findViewById(R.id.bt_sett_feedback_layout).setOnClickListener(this);
		findViewById(R.id.bt_sett_faq_layout).setOnClickListener(this);
		findViewById(R.id.bt_sett_info_layout).setOnClickListener(this);
		
		layout_net = (LinearLayout) findViewById(R.id.layout_net);
		layout_device = (LinearLayout) findViewById(R.id.layout_device);
		layout_password = (RelativeLayout) findViewById(R.id.layout_password);
//		sett_enterPassword_state = (TextView) findViewById(R.id.sett_enterPassword_state);
		layout_more = (LinearLayout) findViewById(R.id.layout_more);

		FrameLayout back = (FrameLayout) findViewById(R.id.layout_back);
		ImageView titlebar_left = (ImageView) findViewById(R.id.titlebar_left);
		titlebar_left.setImageResource(R.drawable.dm_lib_wifi_back_btn_bg_selector);
		back.setOnClickListener(this);
		
		sett_fw_update = (RelativeLayout)findViewById(R.id.sett_fw_update);
		sett_fw_update.setOnClickListener(this);
		layout_password.setOnClickListener(this);
		layout_more.setOnClickListener(this);
		
		mHandler = new HandlerUtil.StaticHandler(mMessageListener);

	}

	private HandlerUtil.MessageListener mMessageListener = new HandlerUtil.MessageListener() {
		public void handleMessage(Message message) {
			if (message.what == MSG_GET_CACHE_FINISHED) {
				updateCacheSize((Long) message.obj);
			} else if (message.what == MSG_CLEAR_CACHE_FINISHED) {
				mClearCacheRunning = false;
				updateCacheSize(0);
				Toast.makeText(mActivity, getString(R.string.DM_Remind_Clear_Success), Toast.LENGTH_LONG).show();
			}
		}
	};
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		int i = v.getId();
		if (i == R.id.layout_back) {
			finish();

		} else if (i == R.id.layout_wifi) {
			startActivity(new Intent(this, UDiskWiFiSSIDSettingsActivity.class));

		} else if (i == R.id.layout_password) {
			Intent intent = new Intent(this, DeviceVaultActivity.class);
//			intent.putExtra("pwd_state", sett_enterPassword_state.getText().toString());
			startActivity(intent);

		} else if (i == R.id.sett_cacheclear_layout) {
			clearCache();

		} else if (i == R.id.bt_sett_feedback_layout) {//			checkInternet(3);
			showFeedBackDialog();


		} else if (i == R.id.bt_sett_about_check_update_layout) {
			showCircleDialog();
			checkInternet(1);

		} else if (i == R.id.sett_fw_update) {
			showCircleDialog();
			getUpdateInfo();

		} else if (i == R.id.layout_more) {
			startActivity(new Intent(this, MoreSettingsActivity.class));

		} else if (i == R.id.bt_sett_info_layout) {
			startActivity(new Intent(this, DeviceInfoActivity.class));

		} else if (i == R.id.bt_sett_faq_layout) {
			startActivity(new Intent(this, HelpCenterActivity.class));

		} else {
		}
	}
	
	private void updateCacheSize(long size) {
		((TextView) findViewById(R.id.sett_cacheclear_size)).setText(ConvertUtil.byteConvert(size, false));
	}
	
	private void getCacheSize() {
		new Thread() {
			public void run() {
				long size = FileOperationHelper.getInstance().getCacheSize();
				//System.out.println("cache size:" + size);
				mHandler.obtainMessage(MSG_GET_CACHE_FINISHED, 0, 0, Long.valueOf(size)).sendToTarget();
			}
		}.start();
	}
	
	private void clearCache() {
		if (mClearCacheRunning)
			return;
		mClearCacheRunning = true;
		((TextView) findViewById(R.id.sett_cacheclear_size)).setText(R.string.DM_Remind_Clear_Wait);
		new Thread() {
			public void run() {
				FileOperationHelper.getInstance().clearCache();
				mHandler.obtainMessage(MSG_CLEAR_CACHE_FINISHED).sendToTarget();
			}
		}.start();
	}
	
	/**
	 * 检查网络如果网络允许，则进行固件和软件升级
	 * @param position
	 */
	private void checkInternet(final int position) {


		CommonAsync.Runnable mRunnable = new CommonAsync.Runnable() {
			@Override
			public void stop() {
				// TODO Auto-generated method stub
			}

			@Override
			public Object run() {
				// TODO Auto-generated method stub
				NetWorkUtil mNetWorkUtil = new NetWorkUtil();
				List<String> list = new ArrayList(){{
					add("114.114.114.114");
					add("www.microsoft.com");
					add("www.baidu.com");
					//some other add() code......
					}};
				return mNetWorkUtil.manyPing(list);
			}
		};
		// 异步请求的回调
		CommonAsyncListener mCommonAsyncListener = new CommonAsyncListener() {

			@Override
			public void onResult(Object ret) {
				// TODO Auto-generated method stub

				if ((Boolean) ret) {
					if (position == 1) {
						doUpdateChk(mActivity);
					}else if (position == 2) {
						closeCircleDialog();
						Toast.makeText(getBaseContext(), getString(R.string.DM_setting_getotaupgrade_is_the_latest_version),Toast.LENGTH_LONG).show();
					} else if(position == 3) {
						startActivity(new Intent(mActivity, ZoomGoFeedBackActivity.class));
					}
				} else {
					closeCircleDialog();
//					Toast.makeText(getBaseContext(), getString(R.string.DM_setting_getotaupgrade_no_network),Toast.LENGTH_LONG).show();
					showHaveNoNetDialog();
				}
			}

			@Override
			public void onError() {
				// TODO Auto-generated method stub
				closeCircleDialog();
			}

			@Override
			public void onDestory() {
				// TODO Auto-generated method stub
			}

		};
		// 先停止之前的操作
		if (internetTask != null) {
			internetTask.destory();
			internetTask = null;
		}
		// 开始进行异步请求
		internetTask = new CommonAsync(mRunnable, mCommonAsyncListener);
		internetTask.executeOnExecutor((ExecutorService) Executors.newCachedThreadPool());
	}
	
	private void showHaveNoNetDialog() {
		
		  dialog= new MessageDialog(mActivity,
				UDiskBaseDialog.TYPE_ONE_BTN);
		dialog.setTitleContent(getString(R.string.DM_Seeting_no_connected_network));

		String message = getString(R.string.DM_Seeting_no_connected_network_tips);

		// dialog.setTitleContent(strOp);
		dialog.setMessage(message);
		dialog.setLeftBtn(getString(R.string.DM_Control_Definite),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dlg, int which) {
						// TODO Auto-generated method stub
						closeHaveNoNetDialog();
					}
				});

		dialog.show();
	}



	private void showFeedBackDialog() {

		feedBackDialog= new MessageDialog(mActivity,
				UDiskBaseDialog.TYPE_ONE_BTN);
		feedBackDialog.setTitleContent(getString(R.string.DM_Setting_feedback_dialog_tips));

		String message = getString(R.string.DM_Setting_feedback_email);
		String sub_message = getString(R.string.DM_Setting_feedback_email_value);

		// dialog.setTitleContent(strOp);
		feedBackDialog.setMessage(message);
		feedBackDialog.setSubContent(sub_message);
		feedBackDialog.setSubContentColor(R.color.dm_feedback_email_color);
		feedBackDialog.setSubContentUnderLine();
		feedBackDialog.setLeftBtn(getString(R.string.DM_Control_Definite),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dlg, int which) {
						// TODO Auto-generated method stub

						closeFeedBackDialog();
					}
				});
		//邮箱的点击事件
		feedBackDialog.setMessageListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				Intent i = new Intent(Intent.ACTION_SEND);
				i.setType("message/rfc822");
				i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"support@zoomgo.tv"});
				i.putExtra(Intent.EXTRA_SUBJECT, "");
				i.putExtra(Intent.EXTRA_TEXT   , "");
				try {
					startActivity(Intent.createChooser(i, "Send mail..."));
				} catch (android.content.ActivityNotFoundException ex) {
					Toast.makeText(SettingsActivity.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
				}
			}
		});

		feedBackDialog.show();
	}
	
	private void closeHaveNoNetDialog() {
		if(dialog != null && dialog.isShowing()) {
			dialog.dismiss();
			dialog = null;
		}
	}
	private void closeFeedBackDialog() {
		if(feedBackDialog != null && feedBackDialog.isShowing()) {
			feedBackDialog.dismiss();
			feedBackDialog = null;
		}
	}
	
	/**
	 * 获取固件升级的信息
	 */
	private void getUpdateInfo() {
		
		DMOTA ota = BaseValue.dmota;
		
		if (ota != null) {
			
			closeCircleDialog();
			
			if (ota.flag  > 0 && ota.flag < 3) {
				UpgradeFwTaskDialog taskDialog = new UpgradeFwTaskDialog(this, UpgradeFwTaskDialog.TYPE_DOWNLOAD_UPGRADE, ota);
				taskDialog.show();
			}else if (ota.flag  > 2 && ota.flag < 5) {
				UpgradeFwTaskDialog taskDialog = new UpgradeFwTaskDialog(this, UpgradeFwTaskDialog.TYPE_DOWNLOAD_UPGRADE, ota);
				taskDialog.show();
			}else {
				checkInternet(2);
				//Toast.makeText(getBaseContext(), getBaseContext().getString(R.string.DM_setting_getotaupgrade_is_the_latest_version),Toast.LENGTH_LONG).show();
			}
			
		}else {
			new GetOtaTask().execute(); 
		}
		
	}
	
	class GetOtaTask extends AsyncTask<Void, Void, DMOTA>{

		@Override
		protected DMOTA doInBackground(Void... params) {
			// TODO Auto-generated method stub
			return DMSdk.getInstance().checkNewFw();
		}
		
		@Override
		protected void onPostExecute(DMOTA result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			
			BaseValue.dmota = result;
			
			if (result != null) {
				
				closeCircleDialog();
				
				if (result.flag  > 0 && result.flag < 3) {
					UpgradeFwTaskDialog taskDialog = new UpgradeFwTaskDialog(mActivity, UpgradeFwTaskDialog.TYPE_DOWNLOAD_UPGRADE, result);
					taskDialog.show();
				}else if (result.flag  > 2 && result.flag < 5) {
					UpgradeFwTaskDialog taskDialog = new UpgradeFwTaskDialog(mActivity, UpgradeFwTaskDialog.TYPE_DOWNLOAD_UPGRADE, result);
					taskDialog.show();
				}else {
					checkInternet(2);
				}
			}else {
				closeCircleDialog();
				Toast.makeText(getBaseContext(), getBaseContext().getString(R.string.DM_setting_getotaupgrade_is_the_latest_version),Toast.LENGTH_LONG).show();
			}
		}
	}
	
	private void showCircleDialog(){
		if (mCircleDialog == null) {
			mCircleDialog = new CircleProgressDialog(this, R.style.Progress_Dialog);
		}
		mCircleDialog.show();
	}
	
	private void closeCircleDialog(){
		if (mCircleDialog != null && mCircleDialog.isShowing()) {
			mCircleDialog.dismiss();
			mCircleDialog = null;
		}
	}
	
	
	public void doUpdateChk(final Activity aty) {
		if (NetHelper.isNetworkAvailable(aty)) {
			mDMSoftwareUpgrade.setmUpdateModeType(UpdateModeType.DM);
			mDMSoftwareUpgrade.setUpdateOnlyWifi(true);
			mDMSoftwareUpgrade.setUpdateListener(new UmengUpdateListener() {
				@Override
				public void onUpdateReturned(int updateStatus, UpdateResponse updateInfo) {
					closeCircleDialog();
					switch (updateStatus) {
					case UpdateStatus.Yes: // has update
//						Toast.makeText(sApplication, aty.getString(R.string.DM_Disk_Update_Find_New_Ver), Toast.LENGTH_SHORT).show();
						mDMSoftwareUpgrade.showUpdateDialog(aty, updateInfo);
						break;
					case UpdateStatus.No: // has no update
						Toast.makeText(aty, aty.getString(R.string.DM_Remind_Set_NoUpdate), Toast.LENGTH_SHORT).show();
						break;
					case UpdateStatus.NoneWifi: // none wifi
						Toast.makeText(aty, aty.getString(R.string.DM_MDNS_Disconect_WiFi), Toast.LENGTH_SHORT).show();
						break;
					case UpdateStatus.Timeout: // time out
						
						Toast.makeText(aty, aty.getString(R.string.DM_Remind_Set_NoUpdate), Toast.LENGTH_SHORT).show();
						// Toast.makeText(sApplication, "time out",
						// Toast.LENGTH_SHORT).show();
						break;
					}
				}
			});
			mDMSoftwareUpgrade.setUpdateAutoPopup(false);
			mDMSoftwareUpgrade.update(aty);
//			mDMSoftwareUpgrade.forceUpdate(aty);
		} else {
			Toast.makeText(aty, aty.getString(R.string.DM_Remind_Set_NoUpdate), Toast.LENGTH_SHORT).show();
		}

	}
	
	public void onEventMainThread(NewFwEvent event) {
		if (event.ota != null) {
			iv_setting_fw_new_version_notify.setVisibility(View.VISIBLE);
		}else {
			iv_setting_fw_new_version_notify.setVisibility(View.GONE);
		}
	}
	
	public void onEventMainThread(PasswordChangeEvent event){
		finish();
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		closeHaveNoNetDialog();
		closeFeedBackDialog();
		closeCircleDialog();
		EventBus.getDefault().unregister(this);
	}

}
