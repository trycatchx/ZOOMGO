package com.dmsys.airdiskpro.view;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;

import com.adupgrade.api.IFWUpgrade;
import com.adupgrade.api.IFWUpgrade.ErrorCode;
import com.adupgrade.api.IFWUpgrade.IFWUpgradeListener;
import com.adupgrade.api.IFWUpgrade.UpgradeState;
import com.adupgrade.impl.FWUpgradeImpl;
import com.dm.baselib.BaseValue;
import com.dmsys.airdiskpro.event.NewFwEvent;
import com.dmsys.airdiskpro.utils.ConvertUtil;
import com.dmsys.dmsdk.model.DMOTA;
import com.dmsys.mainbusiness.R;

import java.util.Locale;

import de.greenrobot.event.EventBus;

public class UpgradeFwTaskDialog {

	private Context mContext;
	
	private DMOTA mOta;
	
	private UDiskTextViewDialog fwUpdateDialog;
	
	private ProgressDialog fwProDialog;
	
	private IFWUpgrade mIFWUpgrade;
	
	private int mType;
	
	private Handler mHandler;
	
	public static final int TYPE_DOWNLOAD= 1;
	public static final int TYPE_DOWNLOAD_UPGRADE = 2;
	public static final int TYPE_FORCE_DOWNLOAD = 3;
	public static final int TYPE_FORCE_UPGRADE= 4;
	public static final int TYPE_FORCE_DOWNLOAD_UPGRADE = 5;
	
	private static final int MESSAGE_PROGRESS = 1000;
	
	private static final int MESSAGE_SUCCESS= 1001;
	
	private static final int MESSAGE_FAIL= 1002;
	

	public UpgradeFwTaskDialog(Context context,int type,DMOTA dmota) {
		this.mContext = context;
		this.mOta = dmota;
		mType = type;
		
		mIFWUpgrade = new FWUpgradeImpl();
		mHandler = new Handler(){

			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				switch (msg.what) {
				case MESSAGE_PROGRESS:
					int progresses = msg.arg1;
					if(progresses > 0) {
						fwProDialog.setProgress(progresses);
					} else {
						fwProDialog.setProgress(0);
					}
					break;
				case MESSAGE_SUCCESS:
					fwProDialog.dismiss();
					//隐藏小红点
					BaseValue.dmota = null;
					EventBus.getDefault().post(new NewFwEvent(null));
					
					initFwTipsDialog(mContext.getString(R.string.DM_setting_getotaupgrade_successful_tips),
							mContext.getString(R.string.DM_setting_getotaupgrade_successful_tips_content),new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									// TODO Auto-generated method stub
								}
							});
					break;
					
				case MESSAGE_FAIL:
					fwProDialog.dismiss();
					initFwTipsDialog(mContext.getString(R.string.DM_setting_upgrade_warn),
							mContext.getString(R.string.DM_setting_upgrade_fail),null);
					break;

				default:
					break;
				}
			}
			
		};
	}
	
	public void show(){
		
		switch (mType) {
		case TYPE_FORCE_DOWNLOAD:
		case TYPE_FORCE_UPGRADE:
		case TYPE_FORCE_DOWNLOAD_UPGRADE:
			showFwForceDownloadDialog();
			break;
			
		case TYPE_DOWNLOAD:
		case TYPE_DOWNLOAD_UPGRADE:
			showFwDownloadDialog();
			break;

		default:
			break;
		}
		
	}
	
	private void showFwDownloadDialog() {
		// TODO Auto-generated method stub
		//增加不是wifi环境下的提示
		String content = String.format(mContext.getString(R.string.DM_Remind_Update_Download_ask),mOta.name);
		
		String content1 = String.format(mContext.getString(R.string.DM_setting_update_content1),mOta.version);
		String content2 = String.format(mContext.getString(R.string.DM_setting_update_content2), ConvertUtil.convertFileSize(mOta.size, 2));
		
		String content3 = "";
		String lan = Locale.getDefault().getLanguage();  
		if ("zh".equals(lan)) {  
			content3 = String.format(mContext.getString(R.string.DM_setting_update_content3),mOta.description);
		}else {
			content3 = String.format(mContext.getString(R.string.DM_setting_update_content3),mOta.description_en);
		}
		
		
		content1 = "\n" + content1 + "\n" + content2 + "\n" + content3;
		
		//关闭之前的dialog
		closeFwUpdateDialog();
		fwUpdateDialog = new UDiskTextViewDialog(mContext, UDiskEditTextDialog.TYPE_TWO_BTN);
		fwUpdateDialog.getTitleLinearLayout().setVisibility(View.VISIBLE);
		fwUpdateDialog.setClickButtonDismiss(true);
		fwUpdateDialog.setContent(content1);
		fwUpdateDialog.setCancelable(false);
		fwUpdateDialog.setTitleContent(content);
		
		//监听事件
		fwUpdateDialog.setLeftBtn(mContext.getString(R.string.DM_Control_Cancel), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				
			}
		});
		fwUpdateDialog.setRightBtn(mContext.getString(R.string.DM_Control_Definite), new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				initFwUpdateDialog1();
			}
		});
		fwUpdateDialog.show();
	}
	
	private void showFwForceDownloadDialog() {
		// TODO Auto-generated method stub
		//增加不是wifi环境下的提示
		String content = String.format(mContext.getString(R.string.DM_setting_mandatory_update_found_newFW2),mOta.name);
		
		String content1 = String.format(mContext.getString(R.string.DM_setting_update_content1),mOta.version);
		String content2 = String.format(mContext.getString(R.string.DM_setting_update_content2),ConvertUtil.convertFileSize(mOta.size, 2));
		
		String content3 = "";
		String lan = Locale.getDefault().getLanguage();  
		if ("zh".equals(lan)) {  
			content3 = String.format(mContext.getString(R.string.DM_setting_update_content3),mOta.description);
		}else {
			content3 = String.format(mContext.getString(R.string.DM_setting_update_content3),mOta.description_en);
		}
		
		content1 = "\n" + content1 + "\n" + content2 + "\n" + content3;
		
		//关闭之前的dialog
		closeFwUpdateDialog();
		fwUpdateDialog = new UDiskTextViewDialog(mContext, UDiskEditTextDialog.TYPE_ONE_BTN);
		fwUpdateDialog.getTitleLinearLayout().setVisibility(View.VISIBLE);
		fwUpdateDialog.setClickButtonDismiss(true);
		fwUpdateDialog.setContent(content1);
		fwUpdateDialog.setCancelable(false);
		fwUpdateDialog.setTitleContent(content);
		
		//监听事件
		fwUpdateDialog.setLeftBtn(mContext.getString(R.string.DM_setting_mandatory_update_sure), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				initFwUpdateDialog1();
			}
		});
		fwUpdateDialog.show();
	}
	
	//关闭FW升级的dialog
	private void closeFwUpdateDialog() {
		if(fwUpdateDialog != null) {
			if(fwUpdateDialog.isShowing()) {
				fwUpdateDialog.dismiss();
			}
			fwUpdateDialog = null;
		}
	}
	
	private void initFwUpdateDialog1() {
		//关闭之前的dialog
		closeFwUpdateDialog();
		fwUpdateDialog = new UDiskTextViewDialog(mContext,UDiskEditTextDialog.TYPE_TWO_BTN);
		fwUpdateDialog.getTitleLinearLayout().setVisibility(View.VISIBLE);
		fwUpdateDialog.setClickButtonDismiss(true);
		fwUpdateDialog.setContent(mContext.getString(R.string.DM_setting_getotaupgrade_tips_content));
		fwUpdateDialog.setTitleContent(mContext.getString(R.string.DM_setting_upgrade_warn));
		
		//监听事件
		fwUpdateDialog.setLeftBtn(mContext.getString(R.string.DM_setting_getotaupgrede_no),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						// 什么都不做，对话框会自动取消
					}
				});
		fwUpdateDialog.setRightBtn(mContext.getString(R.string.DM_setting_getotaupgrede_yes),
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						 // 弹框进度条
						 CreateProgressWindow();
						 //开始下载
						 updateFW();
					}
				});
		fwUpdateDialog.show();
	}
	
	//产生下载进度的进度条
	protected void CreateProgressWindow()
	{
		cancelProgressWindow();
		fwProDialog = new ProgressDialog(mContext,UDiskBaseDialog.TYPE_NO_BTN);  
		fwProDialog.setTitleContent(mContext.getString(R.string.DM_setting_upgrade_downloading));  
		fwProDialog.setProgress(0);

		fwProDialog.show();
	}
	
	//关闭下载进度的进度条
	protected void cancelProgressWindow() {
		if(fwProDialog != null) {
			if(fwProDialog.isShowing()) {
				fwProDialog.dismiss();
			}
			fwProDialog = null;
		}
	}
	
	/**
	 * 下载bin文件和上传，并发送升级指令
	 */
	private void updateFW() {
		IFWUpgradeListener onUpgradeListener =new IFWUpgradeListener() {
			@Override
			public void onProgresschanaged(UpgradeState mUpgradeState,
					final int progresses, ErrorCode errorcode) {
				
				if(mUpgradeState.equals(UpgradeState.INPROGRESS)) {
					Log.d("upgrade", "update>> INPROGRESS:"+progresses);
					Message msg = new Message();
					msg.what = MESSAGE_PROGRESS;
					msg.arg1 = progresses;
					mHandler.sendMessage(msg);
					
				}else if(mUpgradeState.equals(UpgradeState.SUCCESS)) {
					mHandler.sendEmptyMessage(MESSAGE_SUCCESS);
				} else if(mUpgradeState.equals(UpgradeState.FAIL)) {
					mHandler.sendEmptyMessage(MESSAGE_FAIL);
				}
			}
		};
		
		mIFWUpgrade.upgradeTask(BaseValue.dmota,onUpgradeListener);	
	}
	
	/*
	 * 提示框，提示失败
	 */
	private void initFwTipsDialog(String title,String content,DialogInterface.OnClickListener listener) {
		//关闭之前的dialog
		closeFwUpdateDialog();
		
		fwUpdateDialog = new UDiskTextViewDialog(mContext, UDiskEditTextDialog.TYPE_ONE_BTN);
		fwUpdateDialog.getTitleLinearLayout().setVisibility(View.VISIBLE);
		fwUpdateDialog.setClickButtonDismiss(true);
		fwUpdateDialog.setContent(content);
		fwUpdateDialog.setTitleContent(title);
		fwUpdateDialog.setLeftBtn(mContext.getString(R.string.DM_Control_Definite), listener);
		fwUpdateDialog.show();
	}
	
}
