package com.dmsys.airdiskpro.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.dm.baselib.BaseValue;
import com.dmairdisk.aodplayer.util.CommonAsync;
import com.dmsys.airdiskpro.AttributeTask;
import com.dmsys.airdiskpro.adapter.PopupAdapter;
import com.dmsys.airdiskpro.filemanager.EditState;
import com.dmsys.airdiskpro.filemanager.FileOperationService;
import com.dmsys.airdiskpro.ui.MainFragment.OnEditModeChangeListener;
import com.dmsys.airdiskpro.utils.FileUtil;
import com.dmsys.airdiskpro.view.DMPopup;
import com.dmsys.dmsdk.DMSdk;
import com.dmsys.dmsdk.model.DMFile;
import com.dmsys.dmsdk.model.DMFileCategoryType;
import com.dmsys.dmsdk.model.DMRet;
import com.dmsys.dmsdk.model.DMSupportFunction;
import com.dmsys.mainbusiness.R;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VaultAllFileActivity extends FragmentActivity implements
		OnClickListener {
	private TextView tv_vault_decryption_to, tv_vault_op_delete;
	private LinearLayout op_vault_more;
	private LinearLayout bottom;
	
	MainFragment mMainFragment;
	private DMPopup mPopup;
	private PopupAdapter mPopupAdapter;
	private WindowManager mWindowManager;
	private Activity mContext;
	private Handler mHandler;



	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Basic Android widgets
		setContentView(R.layout.activity_all_file_vault);
		initVars();
		initViews();
	}
	
	private void initVars(){
		mContext = this;
		mHandler= new Handler();
		mPopupAdapter = new PopupAdapter(this);
		mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
	} 

	private void initViews() {

		tv_vault_decryption_to = (TextView) findViewById(R.id.tv_vault_decryption_to);
		tv_vault_op_delete = (TextView) findViewById(R.id.tv_vault_op_delete);
		op_vault_more = (LinearLayout) findViewById(R.id.op_vault_more);

		bottom = (LinearLayout) findViewById(R.id.bottom);
		bottom.setVisibility(View.GONE);

		tv_vault_decryption_to.setOnClickListener(this);
		tv_vault_op_delete.setOnClickListener(this);
		op_vault_more.setOnClickListener(this);

		setVaultAllFileView();

	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
//		mMainFragment.reloadItems();
	}

	@Override
	public void onAttachFragment(Fragment fragment) {
		// TODO Auto-generated method stub
		super.onAttachFragment(fragment);

		if (MainFragment.class.isInstance(fragment)) {
			((MainFragment) fragment)
					.setOnEditModeChangeListener(new OnEditModeChangeListener() {

						@Override
						public void onEditModeChange(boolean edit) {
							// TODO Auto-generated method stub
							if (edit) {
								bottom.setVisibility(View.VISIBLE);
							} else {
								bottom.setVisibility(View.GONE);
							}
						}
					});

		}

	}

	/**
	 * dropBox
	 *
	 */
	private void setVaultAllFileView() {
		FragmentManager fm = this.getSupportFragmentManager();
		FragmentTransaction transaction = fm.beginTransaction();

		if (fm.findFragmentById(R.id.flyt_content) == null) {
			mMainFragment =  MainFragment.newInstance(MainFragment.FILE_TYPE_VAULT);

			transaction.add(R.id.flyt_content, mMainFragment);
			if (!fm.isDestroyed()) {
				transaction.commitAllowingStateLoss();
			}
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub

		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (mMainFragment != null && !mMainFragment.isHidden()
					&& mMainFragment.isEditMode()) {
				mMainFragment.unselectAll();
				mMainFragment.setEditState(EditState.STATE_NORMAL);
				return true;
			}
			if (mMainFragment != null && !mMainFragment.isHidden()) {
				if (mMainFragment.isCanToUpper()) {
					mMainFragment.toUpper();
					return true;
				}
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		int i = v.getId();
		if (i == R.id.tv_vault_decryption_to) {
			final List<DMFile> files = mMainFragment.getSelectedFiles();
			if (files.size() == 0) {
				Toast.makeText(this, R.string.DM_FileOP_Warn_Select_File,
						Toast.LENGTH_SHORT).show();
				return;
			}

			FileOperationService.selectedList = files;

			Intent cpintent = new Intent(this, PathSelectActivity.class);
			cpintent.putExtra(PathSelectActivity.EXTRA_OP,
					PathSelectActivity.DECRYPTEDTO);
			startActivity(cpintent);

		} else if (i == R.id.tv_vault_op_delete) {
			final List<DMFile> files1 = mMainFragment.getSelectedFiles();
			if (files1.size() == 0) {
				Toast.makeText(this, R.string.DM_FileOP_Warn_Select_File,
						Toast.LENGTH_SHORT).show();
				return;
			}
			FileOperationService.selectedList = files1;

			mMainFragment.doFileOperation(FileOperationService.FILE_OP_ENCRYPRED_DELETE);

		} else if (i == R.id.op_vault_more) {
			Toast.makeText(this, "This feature is not open",
					Toast.LENGTH_SHORT).show();
//			showMoreDialog();

		} else {
		}
	}

	private void showMoreDialog() {
		// TODO Auto-generated method stub

		if (mPopup != null && mPopup.isShowing()) {
			mPopup.dismiss();
			return;
		}

		final List<DMFile> files = mMainFragment.getSelectedFiles();
		if (files.size() == 0) {
			Toast.makeText(this, R.string.DM_FileOP_Warn_Select_File,
					Toast.LENGTH_SHORT).show();
			return;
		}

		mPopup = new DMPopup(this, DMPopup.VERTICAL);

		View contentView = LayoutInflater.from(this).inflate(
				R.layout.popup_content, null);

		ListView listView = (ListView) contentView.findViewById(R.id.pop_list);
		final List<String> mdata = getPopData(files);
		mPopupAdapter.setData(mdata);
		listView.setAdapter(mPopupAdapter);
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				if (mdata.get(position).equals(
						getString(R.string.DM_Task_Open_By))) {
					onClickThirdParty(files.get(0));
				} else if (mdata.get(position).equals(
						getString(R.string.DM_Task_Share))) {
					onClickShare(files.get(0));
				} else if (mdata.get(position).equals(
						getString(R.string.DM_Task_Details))) {
					onClickDetail(files);
				} else if (mdata.get(position).equals(
						getString(R.string.DM_Task_File_Hide))) {
					onClickHide(true, files.get(0));
				} else if (mdata.get(position).equals(
						getString(R.string.DM_Task_File_Unhide))) {
					onClickHide(false, files.get(0));
				}

				mPopup.dismiss();
			}
		});

		LayoutParams params = new LayoutParams((mWindowManager
				.getDefaultDisplay().getWidth() * 1) / 2,
				LayoutParams.WRAP_CONTENT);
		contentView.setLayoutParams(params);
		mPopup.addView(contentView);
		mPopup.show(findViewById(R.id.bottom));

	}
	
	private List<String> getPopData(List<DMFile> files) {
		// TODO Auto-generated method stub
		List<String> data = new ArrayList<>();
		if (files.size() == 1) {
			
			if (DMSupportFunction.isSupportFileHide(BaseValue.supportFucntion)) {
				if (files.get(0).mHidden) {
					data.add(getString(R.string.DM_Task_File_Unhide));
				}else {
					data.add(getString(R.string.DM_Task_File_Hide));
				}
			}
			
			if (files.get(0).isDir == true) {
				data.add(getString(R.string.DM_Task_Details));
			}else {
				data.add(getString(R.string.DM_Task_Open_By));
				data.add(getString(R.string.DM_Task_Share));
				data.add(getString(R.string.DM_Task_Details));
			}
			
		}else if (files.size() > 1) {
			data.add(getString(R.string.DM_Task_Details));
		}
		return data;
	}

	protected void onClickThirdParty(DMFile file) {
		// TODO Auto-generated method stub
		if (file.getType() == DMFileCategoryType.E_BOOK_CATEGORY
				&& file.mLocation == DMFile.LOCATION_UDISK
				&& !file.mPath.toLowerCase().endsWith(".txt")) {
			mMainFragment.downloadFileToDO(this, file,
					mMainFragment.DOWN_TO_OPEN);
		} else if (file.getType() == DMFileCategoryType.E_PICTURE_CATEGORY
				&& file.mLocation == DMFile.LOCATION_UDISK) {
			mMainFragment.downloadFileToDO(this, file,
					mMainFragment.DOWN_TO_OPEN);
		} else
			FileUtil.thirdPartOpen(file, this);
	}

	protected void onClickShare(DMFile file) {
		// TODO Auto-generated method stub
		mMainFragment.shareFile(file);
	}

	private void onClickRename(DMFile file) {
		// TODO Auto-generated method stub
		mMainFragment.renameFile(file);
	}

	protected void onClickDetail(List<DMFile> files) {
		// TODO Auto-generated method stub
		AttributeTask mAttributeTask = null;

		if (files.size() == 1) {
			if (mMainFragment.isFileInPictureType(files.get(0))) {
				mAttributeTask = new AttributeTask(this, true, files.get(0));
			} else {
				mAttributeTask = new AttributeTask(this, files.get(0));
			}

		} else {
			mAttributeTask = new AttributeTask(this, files);
		}
		mAttributeTask.execute();
	}

	protected void onClickHide(final boolean hide, final DMFile file) {
		// TODO Auto-generated method stub
		CommonAsync.Runnable runnable = new CommonAsync.Runnable() {

			@Override
			public void stop() {
				// TODO Auto-generated method stub

			}

			@Override
			public Object run() {
				// TODO Auto-generated method stub
				if (mMainFragment.isFileInPictureType(file)) {
					return DMSdk.getInstance().setAlbumHide(file.mPath, hide);
				}
				return DMSdk.getInstance().setFileHide(file.mPath, hide);
			}
		};

		CommonAsync.CommonAsyncListener listener = new CommonAsync.CommonAsyncListener() {

			@Override
			public void onResult(Object result) {
				// TODO Auto-generated method stub
				int ret = (int) result;
				System.out.println("sethide ret:" + ret);
				if (ret == DMRet.ACTION_SUCCESS) {
					mMainFragment.setEditState(EditState.STATE_NORMAL);
					if (mMainFragment.isFileInPictureType(file)) {
						mHandler.postDelayed(new Runnable() {

							@Override
							public void run() {
								// TODO Auto-generated method stub
								mMainFragment.reloadItems();
							}
						}, 1000);
					} else {
						mMainFragment.reloadItems();
					}

				} else {
					if (ret == 10262) {
						Toast.makeText(mContext,
								R.string.DM_Task_Filesystem_Not_Surpport,
								Toast.LENGTH_SHORT).show();
					} else {
						Toast.makeText(mContext,
								R.string.DM_SetUI_Failed_Operation,
								Toast.LENGTH_SHORT).show();
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
		async.executeOnExecutor((ExecutorService) Executors
				.newCachedThreadPool());
	}
}
