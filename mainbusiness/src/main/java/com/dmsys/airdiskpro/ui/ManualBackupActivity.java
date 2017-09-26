package com.dmsys.airdiskpro.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dmsys.airdiskpro.db.BackupSettingDB;
import com.dmsys.airdiskpro.event.DisconnectEvent;
import com.dmsys.airdiskpro.model.BakSetBean;
import com.dmsys.airdiskpro.service.BackupService;
import com.dmsys.airdiskpro.utils.GetBakLocationTools;
import com.dmsys.dmsdk.DMSdk;
import com.dmsys.dmsdk.model.DMStorageInfo;
import com.dmsys.mainbusiness.R;

import de.greenrobot.event.EventBus;


public class ManualBackupActivity extends Activity implements OnClickListener{
 private RelativeLayout rlyt_album_backup,rlyt_contacts_backup;
	private Context mContext;
	private ImageView titlebar_left;	
	private TextView titlebar_title;
	private Intent intent;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_manual_backup);
		mContext= this;
		initViews();
		
		intent = new Intent(this, BackupService.class);
		this.startService(intent);
	}
	
	
	private void initViews() {
		
		EventBus.getDefault().register(this);
		
		rlyt_album_backup = (RelativeLayout) findViewById(R.id.rlyt_album_backup);
		rlyt_contacts_backup = (RelativeLayout) findViewById(R.id.rlyt_contacts_backup);
		rlyt_album_backup.setOnClickListener(this);
		rlyt_contacts_backup.setOnClickListener(this);
		titlebar_left = (ImageView) findViewById(R.id.titlebar_left);
		titlebar_left.setImageResource(R.drawable.dm_lib_wifi_back_btn_bg_selector);
		titlebar_left.setOnClickListener(this);
		
		((TextView)findViewById(R.id.titlebar_title)).setText(R.string.DM_Sidebar_PhoneBackup);
		
	}
	
	

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		//建立一个备份的数据库，保存记录
		
		new Thread(){
			public void run() {
				try {
					checkBackUpDbAndStartBackup();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}.start();
		
	}
	
	private void checkBackUpDbAndStartBackup() throws InterruptedException {
		if (!BackupSettingDB.getInstance().existDiskMac(BackupService.tmpMac)) {
			DMStorageInfo info = DMSdk.getInstance().getStorageInfo();
			String storageName = null;
			long storageByte = -1;
			if(info != null && info.getStorages() != null && info.getStorages().size() >0) {
				storageName = info.getStorages().get(0).getName();
				storageByte = info.getStorages().get(0).total;
			} else {
				return;
			}
			BakSetBean bean = new BakSetBean(
				BackupService.tmpMac,
				BakSetBean.TRUE,
				BakSetBean.TRUE,
				BakSetBean.FALSE,
				BakSetBean.TRUE,
				storageName,
				GetBakLocationTools.getNewMediaBakFolder(this,storageName),
				String.valueOf(storageByte),
				BakSetBean.FALSE, BakSetBean.FALSE);
			BackupSettingDB.getInstance().addDiskSetting(bean);
		}
	}

	
	public void onEventMainThread(DisconnectEvent event){
		finish();
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		
		EventBus.getDefault().unregister(this);
		
		if(intent != null) {
			intent = new Intent(this, BackupService.class);
			this.stopService(intent);	
			intent = null;
		}
		
	}


	@Override
	public void onClick(View v) {
		int i = v.getId();
		if (i == R.id.rlyt_album_backup) {
			Intent mIntent = new Intent(mContext, AlbumBackupActivity.class);
			mIntent.putExtra("BACKUP_TYPE", 0);
			mContext.startActivity(mIntent);

		} else if (i == R.id.rlyt_contacts_backup) {
			Intent mIntent1 = new Intent(mContext, ContactsBackupActivity.class);
			mContext.startActivity(mIntent1);

		} else if (i == R.id.titlebar_left) {
			finish();

		}
	}
 
}
