package com.dmsys.airdiskpro.view;

import android.content.Context;
import android.content.DialogInterface;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.dmsys.mainbusiness.R;

import cn.dm.longsys.library.imageloader.core.DMImageLoader;
import cn.dm.longsys.library.imageloader.core.DisplayImageOptions;

public class BackupProgressDialog extends UDiskBaseDialog {
	
	private WakeLock mWakeLock;
	
	public BackupProgressDialog(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		initView(TYPE_ONE_BTN, R.layout.dialog_progress_backup);
		this.setCancelable(false);
		bindListener(context);
	}
	public BackupProgressDialog(Context context,int style) {
		super(context);
		// TODO Auto-generated constructor stub
		initView(style, R.layout.dialog_progress_backup);
		this.setCancelable(false);
		bindListener(context);
	}
	
	private void bindListener(final Context context) {
		// TODO Auto-generated method stub
		setOnShowListener(new OnShowListener() {
			
			@Override
			public void onShow(DialogInterface dialog) {
				// TODO Auto-generated method stub
				mWakeLock = ((PowerManager) context.getSystemService(Context.POWER_SERVICE))
		                .newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK
		                        | PowerManager.ON_AFTER_RELEASE, "wakelock");
				mWakeLock.acquire();
			}
		});
		
		setOnDismissListener(new OnDismissListener() {
			
			@Override
			public void onDismiss(DialogInterface dialog) {
				// TODO Auto-generated method stub
				if (mWakeLock != null) {
					mWakeLock.release();
					mWakeLock = null;
				}
			}
		});
	}
	
	public void setProgress(int progress) {
		View customView = this.getCustomView();
		ProgressBar progressBar = (ProgressBar)customView.findViewById(R.id.dialog_progress);
		progressBar.setProgress(progress);
	}
	
	public void setMessage(CharSequence text) {
		View customView = this.getCustomView();
		TextView textView = (TextView)customView.findViewById(R.id.dialog_msg);
		textView.setText(text);
	}
	
	public String getMessage(){
		View customView = this.getCustomView();
		TextView textView = (TextView)customView.findViewById(R.id.dialog_msg);
		return textView.getText().toString();
	}
	
	public void setImages(DMImageLoader imageLoader,String url,DisplayImageOptions options) {
		View customView = this.getCustomView();
		ImageView imgeShow = (ImageView) customView.findViewById(R.id.iv_dialog_backup_data_display);
		String uriToUse = "file://" + url;
		imageLoader.displayImage(uriToUse, imgeShow, options);
	}
	public void setImages(int id) {
		View customView = this.getCustomView();
		ImageView imgeShow = (ImageView) customView.findViewById(R.id.iv_dialog_backup_data_display);
		imgeShow.setImageResource(id);
	}
	
	
}
