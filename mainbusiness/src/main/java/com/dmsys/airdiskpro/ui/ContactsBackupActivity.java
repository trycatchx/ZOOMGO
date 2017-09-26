package com.dmsys.airdiskpro.ui;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Message;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dmsys.airdiskpro.db.BackupCLogDBManager;
import com.dmsys.airdiskpro.event.BackupRefreshEvent;
import com.dmsys.airdiskpro.event.BackupStateEvent;
import com.dmsys.airdiskpro.event.DisconnectEvent;
import com.dmsys.airdiskpro.model.LogContactBean;
import com.dmsys.airdiskpro.service.BackupService;
import com.dmsys.airdiskpro.service.BackupService.BuckupType;
import com.dmsys.airdiskpro.utils.TransTools;
import com.dmsys.airdiskpro.view.BackupProgressDialog;
import com.dmsys.mainbusiness.R;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.greenrobot.event.EventBus;

public class ContactsBackupActivity  extends Activity implements View.OnClickListener {
private Context mContext;
private TextView tv_c_last_bak_time,tv_c_below_text_up,titlebar_title,tv_c_below_text_down;
private ImageView iv_cur_image;
private Button btn_date_pic_upload;
private RelativeLayout rlyt_contacts_backup;
private ImageView titlebar_left;
private BackupCLogDBManager manager;
private SimpleDateFormat sfdDate = new SimpleDateFormat("yyyy-MM-dd HH:mm");


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_backup_contacts);
		mContext= this;
		initViews();
	}
	
	
	private void initViews() {
		
		EventBus.getDefault().register(this);
		
		rlyt_contacts_backup = (RelativeLayout) findViewById(R.id.rlyt_contacts_backup);
		rlyt_contacts_backup.setOnClickListener(this);
		btn_date_pic_upload = (Button) findViewById(R.id.btn_date_pic_upload);
		
		tv_c_last_bak_time = (TextView) findViewById(R.id.tv_c_last_bak_time);
		iv_cur_image = (ImageView) findViewById(R.id.iv_cur_image);
		tv_c_below_text_up = (TextView) findViewById(R.id.tv_c_below_text_up);
		tv_c_below_text_down = (TextView) findViewById(R.id.tv_c_below_text_down);
		titlebar_title = (TextView) findViewById(R.id.titlebar_title);
		titlebar_title.setText(getString(R.string.DM_Backup_Address_Title));
		
		FrameLayout back = (FrameLayout) findViewById(R.id.layout_back);
		titlebar_left = (ImageView) findViewById(R.id.titlebar_left);
		titlebar_left.setImageResource(R.drawable.dm_lib_wifi_back_btn_bg_selector);
		back.setOnClickListener(this);
		btn_date_pic_upload.setOnClickListener(this);
	}
	
	
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		refreshUI();
	}
	
	private void refreshUI() {
		boolean isException = false;
		String lastBakTimeToShow = getResources().getString(R.string.DM_Disk_recently_contacts_backup_time_none);
		String lastBakNumToShow = String.format(getResources().getString(
								R.string.DM_Backup_Last_Bak_C_Num),"0");
		String phoneCNumToShow;
	    try {
			manager = new BackupCLogDBManager(this,TransTools.CONTACTS_DB_FOLDER_PATH,TransTools.C_BAKLOG_DBNAME);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			isException = true;
		}
	    
		if(!isException) {
			LogContactBean bean = manager.getContactLastRecord();
			if(bean != null) {
				String lastBakdate = sfdDate.format(new Date(bean.time));
				 lastBakTimeToShow = String
						.format(getResources().getString(
								R.string.DM_Backup_Last_C_Bak_Time),
								lastBakdate);
				
				 lastBakNumToShow = String
						.format(getResources()
								.getString(
										R.string.DM_Backup_Last_Bak_C_Num),
								String.valueOf(bean.num));
			}
		}
		
		
		int phoneCNum = getPhoneContactsNum();
		phoneCNumToShow = String.format(getResources()
				.getString(R.string.DM_Backup_Phone_C_Num),
				String.valueOf(phoneCNum));
		
		tv_c_last_bak_time.setText(lastBakTimeToShow);
		tv_c_below_text_up.setText(lastBakNumToShow);
		tv_c_below_text_down.setText(phoneCNumToShow);
	}
	
	private int getPhoneContactsNum()
	{
		int res = 0;
		Cursor cur = null;
		try {
			cur = this.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
			if(cur!=null)
				res = cur.getCount();
		} catch (Exception e) {
			// TODO: handle exception
		}finally{
			if(cur!=null)
				cur.close();
		}
		return res;
	}
	
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		int i = v.getId();
		if (i == R.id.btn_date_pic_upload) {
			showContactsBackupDialog();

			Intent mIntent = new Intent(mContext, BackupService.class);
			mIntent.putExtra("backUp_Type", BuckupType.BUTYPE_CONTACTS.ordinal());
			mContext.startService(mIntent);


		} else if (i == R.id.layout_back) {
			finish();

		} else if (i == R.id.rlyt_contacts_backup) {
			Intent mIntent1 = new Intent(mContext, ContactsHistoryRecordActivity.class);
			mContext.startActivity(mIntent1);

		} else {
		}
	}
	BackupProgressDialog mBackupProgressDialog;
	
	private void showContactsBackupDialog() {
		if(mBackupProgressDialog != null && mBackupProgressDialog.isShowing()) {
			mBackupProgressDialog.dismiss();
		}
		mBackupProgressDialog = new BackupProgressDialog(mContext);
		mBackupProgressDialog.setTitleContent(getString(R.string.DM_Remind_Operate_Backingup));
		mBackupProgressDialog.setMessage(getString(R.string.DM_Disk_backup_filter_contact));
		mBackupProgressDialog.setImages(R.drawable.contacts_backup_logo);
		mBackupProgressDialog.setLeftBtn(getString(R.string.DM_Control_Cancel), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				EventBus.getDefault().post(new BackupStateEvent(BackupStateEvent.CANCLE));
			}
		});
		
		mBackupProgressDialog.show();
	}

	
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}


	public void onEventMainThread(DisconnectEvent event){
		finish();
	}
	
	public void onEventMainThread(BackupRefreshEvent event){
		
		if (event.type == 1) {
			Message msg = event.message;
			if(mBackupProgressDialog == null) return;
			
			if (msg.what == BackupService.MSG_BACKUP_PROGRESS) {
				
				if (mBackupProgressDialog.getMessage().equals(getString(R.string.DM_Disk_backup_filter_contact))) {
					mBackupProgressDialog.setMessage("");
				}
				
				Bundle bundle = msg.getData();
				long tmpProgress = bundle.getLong(BackupService.KEY_PRO);
				long max = bundle.getLong(BackupService.KEY_MAX);
				int progress = (int)((tmpProgress *100)/max);
				mBackupProgressDialog.setProgress(progress);
				
			} else if(msg.what == BackupService.MSG_BACKUP_COMPLETE) {
				if(mBackupProgressDialog != null) {
					mBackupProgressDialog.dismiss();
					mBackupProgressDialog = null;
				}
				//有数据说明失败了，提示对应的错误信息
				Bundle bundle = msg.getData();
				if(bundle != null) {
					boolean ret = bundle.getBoolean(BackupService.RESULT_BACKUP,false);
					if(ret) {
						System.out.println("contactsback DM_Disk_backup_success_Contacts");
						Toast.makeText(mContext, getString(R.string.DM_Disk_backup_success_Contacts), Toast.LENGTH_LONG).show();
						refreshUI();
					} else {
						int errorCode = bundle.getInt(BackupService.ERROR_CODE,-1);
						switch (errorCode) {
						case BackupService.ERROR_BACKUP_NO_STORAGE:
							Toast.makeText(mContext, getString(R.string.DM_Remind_Operate_No_Disk), Toast.LENGTH_LONG).show();	
							break;
						case BackupService.CODE_BACKUP_EXCEPTION:
							Toast.makeText(mContext, getString(R.string.DM_Disk_backup_exception), Toast.LENGTH_LONG).show();	
							break;
						case BackupService.CODE_BACKUP_UPLOAD_FAILED:
							Toast.makeText(mContext, getString(R.string.DM_Disk_backup_fail_Contacts), Toast.LENGTH_LONG).show();	
							break;
							//已经备好了
						case BackupService.CODE_BACKEDUP_FILE:
							Toast.makeText(mContext, getString(R.string.DM_Disk_contacts_has_been_backup), Toast.LENGTH_LONG).show();	
							break;
						case BackupService.CODE_BACKUP_NO_FILE:
							Toast.makeText(mContext, getString(R.string.DM_Disk_have_no_contacts), Toast.LENGTH_LONG).show();	
							break;
						case BackupService.CODE_BACKUP_IS_USER_STOP:
							Toast.makeText(mContext, getString(R.string.DM_Disk_backup_fail_Contacts), Toast.LENGTH_LONG).show();	
							break;

						default:
							break;
						}
					}
				} 
				
			}
		}
		
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		EventBus.getDefault().unregister(this);

	}
	
	
	
	

}
