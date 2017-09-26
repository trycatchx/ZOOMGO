package com.dmsys.airdiskpro.ui;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dm.baselib.BaseValue;
import com.dmairdisk.aodplayer.util.CommonAsync;
import com.dmsoftwareupgrade.util.UDiskBaseDialog;
import com.dmsys.airdiskpro.db.BackupCLogDBManager;
import com.dmsys.airdiskpro.event.BackupRefreshEvent;
import com.dmsys.airdiskpro.event.BackupStateEvent;
import com.dmsys.airdiskpro.event.DisconnectEvent;
import com.dmsys.airdiskpro.model.LogContactBean;
import com.dmsys.airdiskpro.service.BackupService;
import com.dmsys.airdiskpro.service.BackupService.BuckupType;
import com.dmsys.airdiskpro.utils.ConvertUtil;
import com.dmsys.airdiskpro.utils.TransTools;
import com.dmsys.airdiskpro.view.MessageDialog;
import com.dmsys.dmsdk.DMSdk;
import com.dmsys.dmsdk.DMSdk.DMDeviceStatusChangeListener;
import com.dmsys.dmsdk.model.DMStatusType;
import com.dmsys.dmsdk.model.DMStorageInfo;
import com.dmsys.mainbusiness.R;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.dm.longsys.library.imageloader.core.DMImageLoader;
import cn.dm.longsys.library.imageloader.core.DisplayImageOptions;
import de.greenrobot.event.EventBus;


public class AutoBackupActivity extends Activity implements OnClickListener, OnCheckedChangeListener {

	private ImageView titlebar_left,iv_backup;
	private RelativeLayout layout_select_album,layout_select_contacts,layout_album_backup,layout_contacts_backup;
	private RelativeLayout layout_img,layout_progress;
	private CheckBox cb_auto, cb_album, cb_contacts;
	private ProgressBar mProgressBar;
	private Button button_start;
	private TextView text_left,text_contacts_time,text_backfolder;
	private LinearLayout layout_auto;
	
	private SharedPreferences preference;
	private Editor editor;
	
	private String BACKUP_SHOWNOMORE = "show_more";
	
	private DMImageLoader mDMImageLoader;
	private DisplayImageOptions mLoaderOptions;
	
	private BackupCLogDBManager manager;
	private SimpleDateFormat sfdDate = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	
	private boolean inited = false;
	
	private long cookie_disk;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_auto_backup);
		initViews();
		
		initContacts();
		attachDiskListener();
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		
		if (preference.getBoolean("ALBUM", false)) {
			cb_album.setChecked(true);
		}else {
			cb_album.setChecked(false);
		}
		
		Set<String> set = getSharedPreferences(AlbumBackupActivity.PREFERENE_NAME, Context.MODE_PRIVATE).getStringSet(AlbumBackupActivity.KEY_FOLDERS, null);
		if (set != null) {
			layout_select_album.setVisibility(View.VISIBLE);
		}else {
			cb_album.setChecked(false);
			layout_select_album.setVisibility(View.GONE);
		}
		
		LogContactBean bean = manager.getContactLastRecord();
		if(bean != null) {
			String lastBakdate = sfdDate.format(new Date(bean.time));
			String lastBakTimeToShow = String.format(getResources().getString(R.string.DM_Backup_Last_C_Bak_Time),lastBakdate);
			text_contacts_time.setText(lastBakTimeToShow);
		}
		
		inited = true;
		
	}
	
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		System.out.println("bbb onStop ");
		
		inited = false;
	}

	private void initViews() {
		
		EventBus.getDefault().register(this);
		
		preference = getSharedPreferences("BACKUP", MODE_PRIVATE);
		editor = preference.edit();

		((TextView) findViewById(R.id.titlebar_title)).setText(R.string.DM_Sidebar_PhoneBackup);

		FrameLayout back = (FrameLayout) findViewById(R.id.layout_back);
		titlebar_left = (ImageView) findViewById(R.id.titlebar_left);
		titlebar_left.setImageResource(R.drawable.dm_lib_wifi_back_btn_bg_selector);
		back.setOnClickListener(this);

		layout_album_backup = (RelativeLayout) findViewById(R.id.layout_album_backup);
		layout_album_backup.setOnClickListener(this);
		
		layout_contacts_backup = (RelativeLayout) findViewById(R.id.layout_contacts_backup);
		layout_contacts_backup.setOnClickListener(this);
		
		layout_select_album = (RelativeLayout) findViewById(R.id.layout_select_album);
		layout_select_album.setOnClickListener(this);
		
		layout_select_contacts = (RelativeLayout) findViewById(R.id.layout_select_contacts);
		layout_select_contacts.setOnClickListener(this);
		
		layout_img = (RelativeLayout) findViewById(R.id.layout_img);
		layout_progress = (RelativeLayout) findViewById(R.id.layout_progress);
		mProgressBar = (ProgressBar) findViewById(R.id.progress);
		iv_backup = (ImageView) findViewById(R.id.iv_backup);
		text_left = (TextView) findViewById(R.id.text_left);
		
		text_backfolder = (TextView) findViewById(R.id.text_backfolder);
		text_backfolder.setText(" airdisk/"+Build.MODEL);
		
		layout_auto = (LinearLayout) findViewById(R.id.layout_auto);
		layout_auto.setOnClickListener(this);
		
		text_contacts_time = (TextView) findViewById(R.id.text_contacts_time);
		
		button_start = (Button) findViewById(R.id.button_start);
		button_start.setClickable(false);
		button_start.setOnClickListener(this);

		cb_auto = (CheckBox) findViewById(R.id.cb_auto);
		if (preference.getBoolean("AUTO", false)) {
			cb_auto.setChecked(true);
		}else {
			cb_auto.setChecked(false);
		}
		
		cb_contacts = (CheckBox) findViewById(R.id.cb_contacts);
		if (preference.getBoolean("CONTACTS", false)) {
			cb_contacts.setChecked(true);
		}
		
		cb_album = (CheckBox) findViewById(R.id.cb_album);

		if (cb_album.isChecked() || cb_contacts.isChecked()) {
			button_start.setEnabled(true);
		}
		
		cb_auto.setOnCheckedChangeListener(this);
		cb_album.setOnCheckedChangeListener(this);
		cb_contacts.setOnCheckedChangeListener(this);
		
		mDMImageLoader = DMImageLoader.getInstance();
		initLoaderOptions();
		
		if (BaseValue.backing_album || BaseValue.backing_contacts) {
			changeUiMode(true);
		}
		
	}
	
	private void initLoaderOptions() {
		/**
		 * imageloader的新包导入
		 */
		mLoaderOptions = new DisplayImageOptions.Builder()
				.cacheInMemory(true)
				.showImageOnFail(R.drawable.filemanager_photo_fail)
				.useThumb(true)
				.cacheOnDisk(true)
				.showImageOnLoading(R.drawable.ready_to_loading_image)
				.showImageForEmptyUri(R.drawable.filemanager_photo_fail)
				.build();
	}
	
	
	private void initContacts() {
		// TODO Auto-generated method stub
		
		boolean isException = false;
		String lastBakTimeToShow = getResources().getString(R.string.DM_Disk_recently_contacts_backup_time_none);

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
				 lastBakTimeToShow = String.format(getResources().getString(R.string.DM_Backup_Last_C_Bak_Time),lastBakdate);
			}
		}
		
		text_contacts_time.setText(lastBakTimeToShow);
	}

	private void attachDiskListener() {
		// TODO Auto-generated method stub
		cookie_disk = DMSdk.getInstance().attachListener(new DMDeviceStatusChangeListener() {

			@Override
			public void onDeviceStatusChanged(int type) {
				// TODO Auto-generated method stub
				System.out.println("cookie_disk:"+type);
				if (DMStatusType.isDiskChange(type)) {
					getDiskInfo();
				}
			}
		});
	}
	
	private void getDiskInfo(){
		
		CommonAsync.Runnable runnable = new CommonAsync.Runnable() {
			
			@Override
			public void stop() {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public Object run() {
				// TODO Auto-generated method stub
				return DMSdk.getInstance().getStorageInfo();
			}
		};
		
		CommonAsync.CommonAsyncListener listener = new CommonAsync.CommonAsyncListener() {
			
			@Override
			public void onResult(Object ret) {
				// TODO Auto-generated method stub
				DMStorageInfo storageInfo = (DMStorageInfo) ret;
				if (storageInfo != null) {
					if (storageInfo.getMountStatus() == 0) {
						stopBackup();
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
		async.executeOnExecutor( (ExecutorService) Executors.newCachedThreadPool());
		
	}

	private void changeUiMode(boolean baking){
		if (baking) {
			layout_img.setVisibility(View.GONE);
			layout_progress.setVisibility(View.VISIBLE);
			button_start.setText(R.string.DM_Backup_Stop_Button);
			iv_backup.setImageResource(R.drawable.backup_default_icon);
			if (BaseValue.backing_contacts) {
				text_left.setText(R.string.DM_Disk_backup_filter_contact);
				iv_backup.setImageResource(R.drawable.contacts_backup_logo);
			}
			
		}else {
			button_start.setText(R.string.DM_Backup_Start_Button);
			iv_backup.setImageResource(R.drawable.backup_default_icon);
			layout_img.setVisibility(View.VISIBLE);
			layout_progress.setVisibility(View.GONE);
			text_left.setText("");
		}
	}
	

	@Override
	public void onClick(View v) {
		int i = v.getId();
		if (i == R.id.button_start) {
			if (button_start.getText().equals(getString(R.string.DM_Backup_Start_Button))) {
				startBackup();
			} else {
				stopBackup();
			}


		} else if (i == R.id.layout_album_backup) {
			if (!BaseValue.backing_album && !BaseValue.backing_contacts) {
				if (cb_album.isChecked()) {
					cb_album.setChecked(false);
				} else {
					cb_album.setChecked(true);
				}
			} else {

				Toast.makeText(AutoBackupActivity.this, R.string.DM_Remind_Operate_Backingup, Toast.LENGTH_SHORT).show();
			}


		} else if (i == R.id.layout_auto) {
			if (!BaseValue.backing_album && !BaseValue.backing_contacts) {
				if (cb_auto.isChecked()) {
					cb_auto.setChecked(false);
				} else {
					cb_auto.setChecked(true);
				}
			} else {
				Toast.makeText(AutoBackupActivity.this, R.string.DM_Remind_Operate_Backingup, Toast.LENGTH_SHORT).show();
			}

		} else if (i == R.id.layout_contacts_backup) {
			if (!BaseValue.backing_album && !BaseValue.backing_contacts) {
				if (cb_contacts.isChecked()) {
					cb_contacts.setChecked(false);
				} else {
					cb_contacts.setChecked(true);
				}
			} else {
				Toast.makeText(AutoBackupActivity.this, R.string.DM_Remind_Operate_Backingup, Toast.LENGTH_SHORT).show();
			}

		} else if (i == R.id.layout_select_album) {
			Intent mIntent = new Intent(this, AlbumBackupActivity.class);
			mIntent.putExtra("BACKUP_TYPE", 1);
			startActivity(mIntent);

		} else if (i == R.id.layout_select_contacts) {
			Intent mIntent1 = new Intent(this, ContactsHistoryRecordActivity.class);
			startActivity(mIntent1);

		} else if (i == R.id.layout_back) {
			finish();

		}
	}


	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		// TODO Auto-generated method stub
		int i = buttonView.getId();
		if (i == R.id.cb_auto) {
			if (!BaseValue.backing_album && !BaseValue.backing_contacts) {
				if (isChecked) {
					showAutoBackupDialog();
				} else {
					editor.putBoolean("AUTO", false).commit();
				}
			} else {
				cb_auto.setChecked(!cb_auto.isChecked());
				Toast.makeText(AutoBackupActivity.this, R.string.DM_Remind_Operate_Backingup, Toast.LENGTH_SHORT).show();
			}

		} else if (i == R.id.cb_album) {
			if (inited) {
				if (!BaseValue.backing_album && !BaseValue.backing_contacts) {
					if (isChecked && layout_select_album.getVisibility() == View.GONE) {
						Intent mIntent = new Intent(this, AlbumBackupActivity.class);
						mIntent.putExtra("BACKUP_TYPE", 1);
						startActivityForResult(mIntent, 1111);
					}

					if (isChecked) {
						layout_select_album.setVisibility(View.VISIBLE);
					}
					editor.putBoolean("ALBUM", isChecked).commit();
				} else {
					cb_album.setChecked(!cb_album.isChecked());
					Toast.makeText(AutoBackupActivity.this, R.string.DM_Remind_Operate_Backingup, Toast.LENGTH_SHORT).show();
				}
			}


		} else if (i == R.id.cb_contacts) {
			if (inited) {
				if (!BaseValue.backing_album && !BaseValue.backing_contacts) {
					editor.putBoolean("CONTACTS", isChecked).commit();
				} else {
					cb_contacts.setChecked(!cb_contacts.isChecked());
					Toast.makeText(AutoBackupActivity.this, R.string.DM_Remind_Operate_Backingup, Toast.LENGTH_SHORT).show();
				}
			}


		} else {
		}
		
		if (cb_album.isChecked() || cb_contacts.isChecked()) {
			if (button_start.isEnabled() == false) {
				button_start.setEnabled(true);
			}
			
		}else if (!cb_album.isChecked() && !cb_contacts.isChecked()) {
			if (button_start.isEnabled() == true) {
				button_start.setEnabled(false);
			}
		}
		
	}
	
	private void showAutoBackupDialog() {
		// TODO Auto-generated method stub
		
		if (preference.getBoolean(BACKUP_SHOWNOMORE, false)) {
			editor.putBoolean("AUTO", true).commit();
			return;
		}
		
		final MessageDialog dialog = new MessageDialog(AutoBackupActivity.this);
		dialog.setMessage(getString(R.string.DM_Backup_Select_Auto_Remind));
		dialog.setTitleContent(getString(R.string.DM_Remind_Tips));
		dialog.setLeftBtn(getString(R.string.DM_Update_No), new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dlg, int which) {
				// TODO Auto-generated method stub
				editor.putBoolean("AUTO", false).commit();
				cb_auto.setChecked(false);
			}
		});

		dialog.setRightBtn(getString(R.string.DM_Update_Sure), new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dlg, int which) {
				// TODO Auto-generated method stub
				editor.putBoolean("AUTO", true).commit();
			}
		});

		dialog.show();
	}

	/*
	 * 更新上传的下一张图片的缩略图
	 */
	private void updateMediaImageUI(Message msg) {
		//跟新剩下多少张
		Bundle bundle = msg.getData();
		String leftFormat = getResources().getString(R.string.DM_Remind_Backup_Bak_Left);
		String leftFinalStr = String.format(leftFormat,String.valueOf(bundle.getLong(BackupService.KEY_TOTAL_LEFT)));
		
		text_left.setText(leftFinalStr);
		//置零
		mProgressBar.setProgress(0);
		//更新图片
		String filePath = bundle.getString(BackupService.KEY_PATH);
		mDMImageLoader.displayImage("file://" + filePath, iv_backup, mLoaderOptions);
	}
	
	private void startBackup() {
		// TODO Auto-generated method stub
		
		System.out.println("startAutoBackup");
		Intent mIntent = new Intent(this, BackupService.class);
		mIntent.putExtra("backUp_Type", BuckupType.BUTYPE_ALL.ordinal());
		startService(mIntent);
		
		changeUiMode(true);
		
		if (!cb_album.isChecked()) {
			text_left.setText(R.string.DM_Disk_backup_filter_contact);
			iv_backup.setImageResource(R.drawable.contacts_backup_logo);
		}else {
			text_left.setText(R.string.DM_Disk_backup_filter_file);
		}
		
	}
	
	private void stopBackup() {
		// TODO Auto-generated method stub
		System.out.println("stopBackup");
		EventBus.getDefault().post(new BackupStateEvent(BackupStateEvent.CANCLE));
		changeUiMode(false);
	}
	
	
	public void onEventMainThread(DisconnectEvent event){
		finish();
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		EventBus.getDefault().unregister(this);
		DMSdk.getInstance().removeListener(cookie_disk);
	}
	
	public void onEventMainThread(BackupRefreshEvent event){
		
		if (inited) {
			if (event.type == 0) {
				dealMediaEvent(event.message);
			}else if (event.type == 1) {
				dealContactEvent(event.message);
			}
		}
		
	}

	private void dealMediaEvent(Message msg) {
		// TODO Auto-generated method stub
		if (msg.what == BackupService.MSG_BACKUP_PROGRESS) {

			Bundle bundle = msg.getData();
			long tmpProgress = bundle.getLong(BackupService.KEY_PRO);
			long max = bundle.getLong(BackupService.KEY_MAX);
			int progress = (int)((tmpProgress *100)/max);
			
			mProgressBar.setProgress(progress);
			
		}else if(msg.what == BackupService.MSG_BACKUP_FILE_CHANGED) {
			updateMediaImageUI(msg);
			
		}else if(msg.what == BackupService.MSG_BACKUP_COMPLETE) {
			
			mProgressBar.setProgress(0);
			//有数据说明失败了，提示对应的错误信息
			Bundle bundle = msg.getData();
			if(bundle != null) {
				boolean ret = bundle.getBoolean(BackupService.RESULT_BACKUP,false);
				if(ret) {
					if (BaseValue.bigFiles.size() > 0) {
						showAlertDialog(1);
					}else {
						Toast.makeText(AutoBackupActivity.this, getString(R.string.DM_Disk_backup_success_Picture), Toast.LENGTH_LONG).show();	
					}
					
					if (cb_contacts.isChecked()) {
						iv_backup.setBackgroundColor(Color.TRANSPARENT);
						iv_backup.setImageResource(R.drawable.contacts_backup_logo);
						text_left.setText(R.string.DM_Disk_backup_filter_contact);
					}else {
						changeUiMode(false);
					}
				} else {
					int errorCode = bundle.getInt(BackupService.ERROR_CODE,-1);
					switch (errorCode) {
					case BackupService.ERROR_BACKUP_NO_STORAGE:
						Toast.makeText(AutoBackupActivity.this, getString(R.string.DM_Remind_Operate_No_Disk), Toast.LENGTH_LONG).show();	
						if (cb_contacts.isChecked()) {
							iv_backup.setBackgroundColor(Color.TRANSPARENT);
							iv_backup.setImageResource(R.drawable.contacts_backup_logo);
							text_left.setText(R.string.DM_Disk_backup_filter_contact);
						}else {
							changeUiMode(false);
						}
						break;
					case BackupService.CODE_BACKUP_EXCEPTION:
						Toast.makeText(AutoBackupActivity.this, getString(R.string.DM_Disk_backup_exception), Toast.LENGTH_LONG).show();	
						if (cb_contacts.isChecked()) {
							iv_backup.setBackgroundColor(Color.TRANSPARENT);
							iv_backup.setImageResource(R.drawable.contacts_backup_logo);
							text_left.setText(R.string.DM_Disk_backup_filter_contact);
						}else {
							changeUiMode(false);
						}
						break;
					case BackupService.CODE_BACKUP_UPLOAD_FAILED:
						long num = bundle.getLong(BackupService.RESULT_BACKEDUP_NUMBER);
						Toast.makeText(AutoBackupActivity.this, String.format(getString(R.string.DM_Disk_backup_fail_with_num),String.valueOf(num)), Toast.LENGTH_LONG).show();
						if (cb_contacts.isChecked()) {
							iv_backup.setBackgroundColor(Color.TRANSPARENT);
							iv_backup.setImageResource(R.drawable.contacts_backup_logo);
							text_left.setText(R.string.DM_Disk_backup_filter_contact);
						}else {
							changeUiMode(false);
						}
						break;
					case BackupService.CODE_BACKEDUP_FILE:
						Toast.makeText(AutoBackupActivity.this, getString(R.string.DM_Disk_the_selected_file_has_been_backup), Toast.LENGTH_LONG).show();	
						if (cb_contacts.isChecked()) {
							iv_backup.setBackgroundColor(Color.TRANSPARENT);
							iv_backup.setImageResource(R.drawable.contacts_backup_logo);
							text_left.setText(R.string.DM_Disk_backup_filter_contact);
						}else {
							changeUiMode(false);
						}
						break;
					case BackupService.CODE_BACKUP_NO_FILE:
						Toast.makeText(AutoBackupActivity.this, getString(R.string.DM_Disk_have_no_new_file), Toast.LENGTH_LONG).show();	
						if (cb_contacts.isChecked()) {
							iv_backup.setBackgroundColor(Color.TRANSPARENT);
							iv_backup.setImageResource(R.drawable.contacts_backup_logo);
							text_left.setText(R.string.DM_Disk_backup_filter_contact);
						}else {
							changeUiMode(false);
						}
						break;
					case BackupService.CODE_BACKUP_IS_USER_STOP:
						Toast.makeText(AutoBackupActivity.this, getString(R.string.DM_Remind_Operate_Stop), Toast.LENGTH_LONG).show();	
						changeUiMode(false);
						break;
					case BackupService.CODE_BACKEDUP__NO_ENOUGH_SPACE:
						//Toast.makeText(AutoBackupActivity.this, getString(R.string.DM_Backup_Remind_No_Space_Stop), Toast.LENGTH_LONG).show();	
						showAlertDialog(2);
						changeUiMode(false);
						break;

					default:
						break;
					}
				}
			} 
		}
	}
	
	private void showAlertDialog(final int type) {
		// TODO Auto-generated method stub
		MessageDialog builder = new MessageDialog(this,UDiskBaseDialog.TYPE_ONE_BTN);
		builder.setTitleContent(getString(R.string.DM_Remind_Tips));
		String message = "";
		
		if (type == 1) {
			StringBuilder builder2 = new StringBuilder();;
			for(String name:BaseValue.bigFiles){
				builder2.append(name).append("\n");
			}
			message = String.format(getString(R.string.DM_Backup_Remind_To_Large_Skip), builder2.toString());
		}else if (type == 2) {
			message = String.format(getString(R.string.DM_Fileexplore_Operation_Warn_Airdisk_No_Space), ConvertUtil.convertFileSize(BaseValue.taskTotalSize, 2),ConvertUtil.convertFileSize(BaseValue.diskFreeSize,2));
		}
		
		builder.setMessage(message);
		builder.setLeftBtn(getString(R.string.DM_Update_Sure), new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				if (type == 1) {
					Toast.makeText(AutoBackupActivity.this, getString(R.string.DM_Disk_backup_success_Picture), Toast.LENGTH_LONG).show();	
				}
			}
		});
		builder.show();
		
	}
	
	private void dealContactEvent(Message msg) {
		// TODO Auto-generated method stub
		if (msg.what == BackupService.MSG_BACKUP_PROGRESS) {
			
			if (text_left.getText().equals(getString(R.string.DM_Disk_backup_filter_contact))) {
				text_left.setText("");
			}
			
			Bundle bundle = msg.getData();
			long tmpProgress = bundle.getLong(BackupService.KEY_PRO);
			long max = bundle.getLong(BackupService.KEY_MAX);
			int progress = (int)((tmpProgress *100)/max);
			mProgressBar.setProgress(progress);
			
		} else if(msg.what == BackupService.MSG_BACKUP_COMPLETE) {
			//有数据说明失败了，提示对应的错误信息
			
			changeUiMode(false);
			mProgressBar.setProgress(0);
			Bundle bundle = msg.getData();
			if(bundle != null) {
				boolean ret = bundle.getBoolean(BackupService.RESULT_BACKUP,false);
				if(ret) {
					int errorCode = bundle.getInt(BackupService.ERROR_CODE,-1);
					if (errorCode != BackupService.CODE_BACKEDUP_DELETE_SUCCESS || errorCode != BackupService.CODE_BACKEDUP_RECOVER_SUCCESS) {
						Toast.makeText(AutoBackupActivity.this, getString(R.string.DM_Disk_backup_success_Contacts), Toast.LENGTH_LONG).show();
						LogContactBean bean = manager.getContactLastRecord();
						if(bean != null) {
							String lastBakdate = sfdDate.format(new Date(bean.time));
							String lastBakTimeToShow = String.format(getResources().getString(R.string.DM_Backup_Last_C_Bak_Time),lastBakdate);
							text_contacts_time.setText(lastBakTimeToShow);
						}
					}
				} else {
					int errorCode = bundle.getInt(BackupService.ERROR_CODE,-1);
					switch (errorCode) {
					case BackupService.ERROR_BACKUP_NO_STORAGE:
						Toast.makeText(AutoBackupActivity.this, getString(R.string.DM_Remind_Operate_No_Disk), Toast.LENGTH_LONG).show();	
						break;
					case BackupService.CODE_BACKUP_EXCEPTION:
						Toast.makeText(AutoBackupActivity.this, getString(R.string.DM_Disk_backup_exception), Toast.LENGTH_LONG).show();	
						break;
					case BackupService.CODE_BACKUP_UPLOAD_FAILED:
						Toast.makeText(AutoBackupActivity.this, getString(R.string.DM_Disk_backup_fail_Contacts), Toast.LENGTH_LONG).show();	
						break;
						//已经备好了
					case BackupService.CODE_BACKEDUP_FILE:
						Toast.makeText(AutoBackupActivity.this, getString(R.string.DM_Disk_contacts_has_been_backup), Toast.LENGTH_LONG).show();	
						break;
					case BackupService.CODE_BACKUP_NO_FILE:
						Toast.makeText(AutoBackupActivity.this, getString(R.string.DM_Disk_have_no_contacts), Toast.LENGTH_LONG).show();	
						break;
					case BackupService.CODE_BACKUP_IS_USER_STOP:
						Toast.makeText(AutoBackupActivity.this, getString(R.string.DM_Remind_Operate_Stop), Toast.LENGTH_LONG).show();	
						break;

					default:
						break;
					}
				}
			} 
			
		}
	}

	
	

}
