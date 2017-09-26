package com.dmsys.airdiskpro.ui;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.dm.baselib.BaseValue;
import com.dmairdisk.aodplayer.util.CommonAsync;
import com.dmsys.airdiskpro.adapter.ContactsHistoryRecordAapter;
import com.dmsys.airdiskpro.adapter.ContactsHistoryRecordAapter.BackupOperaListenter;
import com.dmsys.airdiskpro.backup.BackupInfoFactory;
import com.dmsys.airdiskpro.backup.IBackupInfo;
import com.dmsys.airdiskpro.event.BackupRefreshEvent;
import com.dmsys.airdiskpro.event.DisconnectEvent;
import com.dmsys.airdiskpro.model.BackupInfoType;
import com.dmsys.airdiskpro.model.ContactsConfig;
import com.dmsys.airdiskpro.service.BackupService;
import com.dmsys.airdiskpro.service.BackupService.BuckupType;
import com.dmsys.airdiskpro.view.MessageDialog;
import com.dmsys.airdiskpro.view.ProgressDialog;
import com.dmsys.airdiskpro.view.UDiskBaseDialog;
import com.dmsys.mainbusiness.R;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.greenrobot.event.EventBus;

public class ContactsHistoryRecordActivity extends Activity implements View.OnClickListener {
	private Activity mContext;
	private ListView list;
	private TextView titlebar_title;
	private ImageView titlebar_left;
	private CommonAsync mCommonAsync;
	private ExecutorService FULL_TASK_EXECUTOR = (ExecutorService) Executors.newCachedThreadPool();
	private LinearLayout loading, emptyRl;
	ContactsHistoryRecordAapter adapter;
	List<ContactsConfig> contactsConfigList = new ArrayList<ContactsConfig>();
	private BackupService backupService;
	private MessageDialog mMessageDialog;
	private ProgressDialog mProgressDialog;

	private ServiceConnection mConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			// TODO Auto-generated method stub
			backupService = ((BackupService.BackupBinder) service).getService();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			// TODO Auto-generated method stub
			backupService = null;
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_contacts_history_record);
		mContext = this;

		initViews();
		Intent intent = new Intent(this, BackupService.class);
		boolean ret = this.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();

		CommonAsync.Runnable mRunnable = new CommonAsync.Runnable() {

			@Override
			public void stop() {
				// TODO Auto-generated method stub
			}

			@Override
			public Object run() {
				// TODO Auto-generated method stub
				mContext.runOnUiThread(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						loading.setVisibility(View.VISIBLE);
					}
				});

				ArrayList<ContactsConfig> ret = getContactsBuList();
				
				if (ret == null) {
					ret = new ArrayList<>();
				}
				
				return ret;

			}
		};
		CommonAsync.CommonAsyncListener mCommonAsyncListener = new CommonAsync.CommonAsyncListener() {

			@Override
			public void onResult(Object ret) {
				// TODO Auto-generated method stub
				loading.setVisibility(View.GONE);

				ArrayList<ContactsConfig> datas = (ArrayList<ContactsConfig>) ret;

				if (datas != null) {
					if (datas.size() > 0) {

						emptyRl.setVisibility(View.GONE);
						list.setVisibility(View.VISIBLE);

						contactsConfigList.clear();
						contactsConfigList.addAll(((ArrayList<ContactsConfig>) ret));
						adapter.notifyDataSetChanged();

					} else {
						emptyRl.setVisibility(View.VISIBLE);
						list.setVisibility(View.GONE);
					}

				} else {
					emptyRl.setVisibility(View.VISIBLE);
					list.setVisibility(View.GONE);
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
		if (mCommonAsync != null) {
			mCommonAsync.destory();
		}
		mCommonAsync = new CommonAsync(mRunnable, mCommonAsyncListener);
		mCommonAsync.executeOnExecutor(FULL_TASK_EXECUTOR);
	}

	private ArrayList<ContactsConfig> getContactsBuList() {

		ArrayList<ContactsConfig> resList = null;
		IBackupInfo backupInfo = BackupInfoFactory.getInstance(BackupInfoType.CONTACTS, this, null);

		try {
			resList = (ArrayList<ContactsConfig>) backupInfo.getBackupInfoDscreptionList();
		} catch (Exception e) {
		}

		return resList;
	}

	private void initViews() {

		EventBus.getDefault().register(this);

		titlebar_title = (TextView) findViewById(R.id.titlebar_title);
		titlebar_title.setText(getString(R.string.DM_Backup_Address_Records_Title));

		FrameLayout back = (FrameLayout) findViewById(R.id.layout_back);
		titlebar_left = (ImageView) findViewById(R.id.titlebar_left);
		titlebar_left.setImageResource(R.drawable.dm_lib_wifi_back_btn_bg_selector);
		back.setOnClickListener(this);
		loading = (LinearLayout) findViewById(R.id.loading);
		emptyRl = (LinearLayout) findViewById(R.id.emptyRl);
		list = (ListView) findViewById(R.id.list);
		adapter = new ContactsHistoryRecordAapter(this, contactsConfigList);
		adapter.setBackupOperaListenter(new BackupOperaListenter() {

			@Override
			public void recover(ContactsConfig c) {
				// TODO Auto-generated method stub
				System.out.println("test123 huifu");
				if (BaseValue.backing_album || BaseValue.backing_contacts) {
					Toast.makeText(ContactsHistoryRecordActivity.this, R.string.DM_Remind_Operate_Backingup,
							Toast.LENGTH_SHORT).show();
				} else {
					showRecoverContactsDialogTip(c);
				}

			}

			@Override
			public void delete(ContactsConfig c) {
				// TODO Auto-generated method stub
				System.out.println("test123 shanchu");
				if (BaseValue.backing_album || BaseValue.backing_contacts) {
					Toast.makeText(ContactsHistoryRecordActivity.this, R.string.DM_Remind_Operate_Backingup,
							Toast.LENGTH_SHORT).show();
				} else {
					showDeleteContactsDialogTip(c);
				}

			}

		});

		list.setAdapter(adapter);

	}

	/**
	 * 显示恢复通讯录的对话框
	 * 
	 * @param c
	 */
	private void showRecoverContactsDialogTip(final ContactsConfig c) {
		if (mMessageDialog != null && mMessageDialog.isShowing()) {
			mMessageDialog.dismiss();
		}
		mMessageDialog = new MessageDialog(mContext, UDiskBaseDialog.TYPE_TWO_BTN);
		mMessageDialog.setTitleContent(getString(R.string.DM_Backup_Address_Records_Restore_Button));
		mMessageDialog.setRightBtn(getString(R.string.DM_SetUI_Dialog_Button_Sure), new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				showRecoverProDialog();

				Intent mIntent = new Intent(mContext, BackupService.class);
				mIntent.putExtra("backUp_Type", BuckupType.BUTYPE_RECOVER.ordinal());
				BackupService.selectedContactsConfig = c;
				mContext.startService(mIntent);
			}
		});
		mMessageDialog.setLeftBtn(getString(R.string.DM_SetUI_Dialog_Button_Cancel), new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
			}
		});
		mMessageDialog
				.setMessage(String.format(getString(R.string.DM_Remind_Operate_Records_Restore), c.getContactsNum()));
		mMessageDialog.show();
	}

	/**
	 * 显示恢复通讯录的进度框
	 * 
	 * @param c
	 */
	private void showRecoverProDialog() {
		if (mProgressDialog != null && mProgressDialog.isShowing()) {
			mProgressDialog.dismiss();
		}
		mProgressDialog = new ProgressDialog(mContext, UDiskBaseDialog.TYPE_ONE_BTN);
		mProgressDialog.setTitleContent(getString(R.string.DM_Backup_Address_Records_Restore_Button));
		mProgressDialog.setLeftBtn(getString(R.string.DM_SetUI_Dialog_Button_Cancel), new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				backupService.stopBackup();
			}
		});
		mProgressDialog.show();
	}

	private void showRecoverSuccessDialog(long number) {
		if (mMessageDialog != null && mMessageDialog.isShowing()) {
			mMessageDialog.dismiss();
		}
		mMessageDialog = new MessageDialog(mContext, UDiskBaseDialog.TYPE_ONE_BTN);
		mMessageDialog.setTitleContent(getString(R.string.DM_Remind_Operate_Restore_Success));

		mMessageDialog.setLeftBtn(getString(R.string.DM_SetUI_Dialog_Button_Sure), null);
		mMessageDialog.setMessage(String.format(getString(R.string.DM_Remind_Operate_Restore_Done), number));
		mMessageDialog.show();
	}

	/**
	 * 显示删除通讯录的对话框
	 * 
	 * @param v
	 */
	private void showDeleteContactsDialogTip(final ContactsConfig c) {
		if (mMessageDialog != null && mMessageDialog.isShowing()) {
			mMessageDialog.dismiss();
		}
		mMessageDialog = new MessageDialog(mContext, UDiskBaseDialog.TYPE_TWO_BTN);
		mMessageDialog.setTitleContent(getString(R.string.DM_Backup_Address_Records_Delete_Button));
		mMessageDialog.setRightBtn(getString(R.string.DM_SetUI_Dialog_Button_Sure), new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				showDeleteProDialog();

				Intent mIntent = new Intent(mContext, BackupService.class);
				mIntent.putExtra("backUp_Type", BuckupType.BUTYPE_DELETE.ordinal());
				BackupService.selectedContactsConfig = c;
				mContext.startService(mIntent);
			}
		});
		mMessageDialog.setLeftBtn(getString(R.string.DM_SetUI_Dialog_Button_Cancel), new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
			}
		});
		mMessageDialog.setMessage(String.format(getString(R.string.DM_Remind_Operate_Records_Delete), c.getContactsNum()));
		mMessageDialog.show();
	}

	/**
	 * 显示删除联系人的对进度框
	 * 
	 * @param v
	 */
	private void showDeleteProDialog() {
		if (mProgressDialog != null && mProgressDialog.isShowing()) {
			mProgressDialog.dismiss();
		}
		mProgressDialog = new ProgressDialog(mContext, UDiskBaseDialog.TYPE_ONE_BTN);
		mProgressDialog.setTitleContent(getString(R.string.DM_Backup_Address_Records_Delete_Button));
		mProgressDialog.setLeftBtn(getString(R.string.DM_SetUI_Dialog_Button_Cancel), new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				backupService.stopBackup();
			}
		});
		mProgressDialog.show();
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		int i = v.getId();
		if (i == R.id.layout_back) {
			finish();

		} else {
		}
	}

	public void onEventMainThread(DisconnectEvent event) {
		finish();
	}
	
	public void onEventMainThread(BackupRefreshEvent event) {

		if (event.type == 1) {
			Message msg = event.message;
			if (mProgressDialog == null)
				return;

			if (msg.what == BackupService.MSG_BACKUP_PROGRESS) {
				Bundle bundle = msg.getData();
				long tmpProgress = bundle.getLong(BackupService.KEY_PRO);
				long max = bundle.getLong(BackupService.KEY_MAX);
				int progress = (int) ((tmpProgress * 100) / max);
				if (mProgressDialog != null) {
					mProgressDialog.setProgress(progress);
				}
			} else if (msg.what == BackupService.MSG_BACKUP_COMPLETE) {
				if (mProgressDialog != null) {
					mProgressDialog.dismiss();
					mProgressDialog = null;
				}
				// 有数据说明失败了，提示对应的错误信息
				Bundle bundle = msg.getData();
				if (bundle != null) {
					boolean ret = bundle.getBoolean(BackupService.RESULT_BACKUP, false);
					int errorCode = bundle.getInt(BackupService.ERROR_CODE, -1);
					if (ret) {
						if (errorCode == BackupService.CODE_BACKEDUP_DELETE_SUCCESS) {
							Toast.makeText(mContext, getString(R.string.DM_Disk_delete_contacts_success),
									Toast.LENGTH_LONG).show();
							// 刷新数据
							onStart();
						} else if (errorCode == BackupService.CODE_BACKEDUP_RECOVER_SUCCESS) {
							Toast.makeText(mContext, getString(R.string.DM_Remind_Operate_Restore_Success),
									Toast.LENGTH_LONG).show();
							long number = bundle.getLong(BackupService.RESULT_BACKEDUP_NUMBER);
							showRecoverSuccessDialog(number);
						}
					} else {
						switch (errorCode) {
						case BackupService.ERROR_BACKUP_NO_STORAGE:
							Toast.makeText(mContext, getString(R.string.DM_Remind_Operate_No_Disk), Toast.LENGTH_LONG)
									.show();
							break;
						case BackupService.CODE_BACKEDUP_RECOVER_FAILED:
							Toast.makeText(mContext, getString(R.string.DM_Disk_the_contacts_recover_failed),
									Toast.LENGTH_LONG).show();
							break;
						// 已经备好了
						case BackupService.CODE_BACKEDUP_RECOVER_HAVE_NO_CONTACTS:
							Toast.makeText(mContext, getString(R.string.DM_Disk_the_contacts_recover_no_contacts),
									Toast.LENGTH_LONG).show();
							break;
						case BackupService.CODE_BACKEDUP_DELETE_FAILED:
							Toast.makeText(mContext, getString(R.string.DM_Disk_delete_contacts_failed),
									Toast.LENGTH_LONG).show();
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

		if (mConnection != null) {
			this.unbindService(mConnection);
			mConnection = null;

		}
	}

}
