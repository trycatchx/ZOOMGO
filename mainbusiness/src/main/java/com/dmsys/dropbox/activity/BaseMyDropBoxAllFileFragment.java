package com.dmsys.dropbox.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.widget.Toast;

import com.dm.baselib.BaseValue;
import com.dm.share.IntentShare;
import com.dmsys.airdiskpro.filemanager.FileOperationHelper;
import com.dmsys.airdiskpro.filemanager.FileOperationService;
import com.dmsys.airdiskpro.filemanager.FileOperationService.ModuleType;
import com.dmsys.airdiskpro.filemanager.FileOperationService.SameNameInfo;
import com.dmsys.airdiskpro.utils.ConvertUtil;
import com.dmsys.airdiskpro.utils.FileInfoUtils;
import com.dmsys.airdiskpro.view.MessageDialog;
import com.dmsys.airdiskpro.view.MsgWidthCheckBoxDialog;
import com.dmsys.airdiskpro.view.ProgressDialog;
import com.dmsys.airdiskpro.view.UDiskBaseDialog;
import com.dmsys.airdiskpro.view.UDiskEditTextDialog;
import com.dmsys.dmsdk.model.DMFile;
import com.dmsys.dmsdk.model.DMFileCategoryType;
import com.dmsys.dropbox.api.DMDropboxAPI;
import com.dmsys.dropbox.utils.DropBoxFileOperationHelper;
import com.dmsys.mainbusiness.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class BaseMyDropBoxAllFileFragment extends Fragment {

	private Context mContext;
	private FileOperationService mOpService;
	private Handler mHandler;
	public ProgressDialog mProgressDialog;
	public SharedPreferences mPreferences;
	private String PREFERENCE_NAME = "DROPBOX_PREFERENCE_NAME";
	private String KEY_SHOWNOMORE = "SHOWNOMORE";
	public Editor mEditor;
	private boolean mCancelCache = false;
	public int DOWN_TO_OPEN = 0;
	public int DOWN_TO_SHARE = 1;
	DMDropboxAPI mApi;
	long handlerId = -1;
	boolean isbindService = false;

	public void init(Context context, Handler mHandler) {
		this.mContext = context;
		this.mHandler = mHandler;

		this.mPreferences = mContext.getSharedPreferences(PREFERENCE_NAME,
				Context.MODE_PRIVATE);
		this.mEditor = mPreferences.edit();

		Intent intent = new Intent(mContext, FileOperationService.class);
		mContext.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
		isbindService = true;
	}

	private ServiceConnection mConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			// TODO Auto-generated method stub
			mOpService = ((FileOperationService.OperationBinder) service)
					.getService();
			mOpService.setHandler(mHandler);
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			// TODO Auto-generated method stub
			mOpService.setHandler(null);
			mOpService = null;
		}
	};

	public void unInit() {
		if (isbindService) {
			mContext.unbindService(mConnection);
			isbindService = false;
		}

	}

	 @Override
	public void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		closeMsgWidthCheckBoxDialog();
		closeProgressDialog();
		closeUDiskEditTextDialog();
		closeMessageDialog();
	}
	
	private void closeProgressDialog() {
		if (mProgressDialog != null && mProgressDialog.isShowing()) {
			mProgressDialog.dismiss();
			mProgressDialog = null;
		}
	}

	public void doFileOperation(int op, List<DMFile> list) {

		Resources resource = this.getResources();
		String title = opCode2String(op);

		closeProgressDialog();
		mProgressDialog = new ProgressDialog(mContext);
		mProgressDialog.setTitleContent(title);
		mProgressDialog.setLeftBtn(
				resource.getString(R.string.DM_Control_Cancel),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						mOpService.cancelCurOperation();
						Toast.makeText(mContext,
								R.string.DM_Remind_Operate_Stop,
								Toast.LENGTH_SHORT).show();
					}
				});

		mProgressDialog.show();

		// 这里用dropBox 的api 进行操作
		Intent intent = new Intent(getActivity(), FileOperationService.class);
		Bundle bundle = new Bundle();
		bundle.putInt(FileOperationService.EXTRA_OP, op);
		bundle.putInt(FileOperationService.EXTRA_MODULE_TYPE,
				ModuleType.DropBox.ordinal());
		intent.putExtra(FileOperationService.EXTRA_BUNDLE, bundle);
		FileOperationService.selectedList = list;
		mContext.startService(intent);
	}

	public void doFileOperation(final int op, List<DMFile> list, String desPath) {
		Resources resource = this.getResources();

		String title = opCode2String(op);
		closeProgressDialog();
		mProgressDialog = new ProgressDialog(mContext);
		mProgressDialog.setTitleContent(title);
		mProgressDialog.setLeftBtn(
				resource.getString(R.string.DM_Control_Cancel),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						mOpService.cancelCurOperation();
						Toast.makeText(mContext,
								R.string.DM_Remind_Operate_Stop,
								Toast.LENGTH_SHORT).show();

						if (op == FileOperationService.FILE_OP_COPYTO) {
							getActivity().finish();
						}

					}
				});

		mProgressDialog.show();

		Intent intent = new Intent(getActivity(), FileOperationService.class);
		Bundle bundle = new Bundle();
		bundle.putInt(FileOperationService.EXTRA_OP, op);
		bundle.putString(FileOperationService.Rename_Key, desPath);
		bundle.putInt(FileOperationService.EXTRA_MODULE_TYPE,
				ModuleType.DropBox.ordinal());
		intent.putExtra(FileOperationService.EXTRA_BUNDLE, bundle);
		FileOperationService.selectedList = list;
		mContext.startService(intent);

	}

	public void doNewFolderOperation(int op, DMFile file) {

		Intent intent = new Intent(getActivity(), FileOperationService.class);
		Bundle bundle = new Bundle();
		bundle.putInt(FileOperationService.EXTRA_OP, op);
		bundle.putSerializable(FileOperationService.EXTRA_NEWFOLDER, file);
		bundle.putSerializable(FileOperationService.EXTRA_MODULE_TYPE,
				ModuleType.DropBox.ordinal());
		intent.putExtra(FileOperationService.EXTRA_BUNDLE, bundle);
		mContext.startService(intent);

	}

	private void closeMsgWidthCheckBoxDialog() {
		if (mMsgWidthCheckBoxDialog != null && mMsgWidthCheckBoxDialog.isShowing()) {
			mMsgWidthCheckBoxDialog.dismiss();
			mMsgWidthCheckBoxDialog = null;
		}
	}

	MsgWidthCheckBoxDialog mMsgWidthCheckBoxDialog;

	public void onSameFile(final SameNameInfo info) {
		closeMsgWidthCheckBoxDialog();
		mMsgWidthCheckBoxDialog = new MsgWidthCheckBoxDialog(mContext);
		mMsgWidthCheckBoxDialog.setCheckText(getString(R.string.DM_Task_Confirm_Operate));
		mMsgWidthCheckBoxDialog.setMessage(String.format(
				getString(R.string.DM_Remind_Operate_SameFile), info.name));
		mMsgWidthCheckBoxDialog.setTitleContent(getString(R.string.DM_Remind_Tips));
		mMsgWidthCheckBoxDialog.setLeftBtn(getString(R.string.DM_Task_Confirm_Rename),
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dlg, int which) {
						// TODO Auto-generated method stub
						if (mMsgWidthCheckBoxDialog.getChecked()) {
							info.ret = -FileOperationHelper.OP_RENAME;
						} else {
							info.ret = FileOperationHelper.OP_RENAME;
						}
						info.semp.release();
					}
				});

		mMsgWidthCheckBoxDialog.setRightBtn(getString(R.string.DM_Task_Confirm_Jump),
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dlg, int which) {
						// TODO Auto-generated method stub
						// // System.out.println("exist ret0:"+info.ret);
						if (mMsgWidthCheckBoxDialog.getChecked()) {
							info.ret = -FileOperationHelper.OP_SKIP;
						} else {
							info.ret = FileOperationHelper.OP_SKIP;
						}

						info.semp.release();
					}
				});

		mMsgWidthCheckBoxDialog.show();
	}

	MessageDialog mMessageDialog;

	public void onSuccess(final String strOp, String dstPath,
			List<DMFile> list, int size) {
		if (list == null || list.size() == 0
				|| strOp.equals(getString(R.string.DM_Task_Delete))) {
			return;
		}

		boolean show = mPreferences.getBoolean(KEY_SHOWNOMORE, true);
		if (!show) {

			if (strOp.equals(getString(R.string.DM_Task_Copy))) {
				getActivity().finish();
			}

			return;
		}
		closeMessageDialog();
		mMessageDialog = new MessageDialog(mContext,
				UDiskBaseDialog.TYPE_ONE_BTN);

		String message = "";
		if (strOp.equals(getString(R.string.DM_Task_Download))) {
			message = getString(R.string.DM_Remind_Operate_Download_Done);
			message = String.format(message, size, dstPath);
		} else if (strOp.equals(getString(R.string.DM_Task_Copy))) {
			message = getString(R.string.DM_Remind_Operate_Copy_Success);
			message = String.format(message, size, dstPath);
		}

		mMessageDialog.setTitleContent(strOp);
		mMessageDialog.setMessage(message);
		mMessageDialog.setLeftBtn(getString(R.string.DM_Control_Definite),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dlg, int which) {
						// TODO Auto-generated method stub

						if (strOp.equals(getString(R.string.DM_Task_Copy))) {
							getActivity().finish();
						}

					}
				});

		mMessageDialog.show();
	}

	private void closeMessageDialog() {
		if (mMessageDialog != null && mMessageDialog.isShowing()) {
			mMessageDialog.dismiss();
			mMessageDialog = null;
		}
	}

	public void onError(int err) {
		closeMessageDialog();
		mMessageDialog = new MessageDialog(mContext,
				UDiskBaseDialog.TYPE_ONE_BTN);
		mMessageDialog
				.setTitleContent(getString(R.string.DM_Fileexplore_Operation_Warn_Error));
		mMessageDialog.setLeftBtn(
				getString(R.string.DM_SetUI_Dialog_Button_Sure), null);
		mMessageDialog.setMessage(strerr(getActivity(), err));
		mMessageDialog.show();
	}

	private String strerr(Context context, int err) {
		String str = null;
		switch (err) {
		case FileOperationHelper.ERROR_UDISK_NOT_ENOUGH_SPACE:
			str = String
					.format(context
							.getString(R.string.DM_Fileexplore_Operation_Warn_Airdisk_No_Space),
							ConvertUtil.convertFileSize(
									BaseValue.taskTotalSize, 2), ConvertUtil
									.convertFileSize(BaseValue.diskFreeSize, 2));
			break;
		case FileOperationHelper.ERROR_PHONE_NOT_ENOUGH_SPACE:
			str = context.getString(R.string.DM_Remind_Operate_Local_NoSize);
			break;
		case FileOperationHelper.ERROR_NOT_CONNECTED:
			str = context
					.getString(R.string.DM_Fileexplore_Operation_Warn_Connect_Fail);
			break;
		case FileOperationHelper.ERROR_NOT_FOUND_STORAGE:
			str = context.getString(R.string.DM_Remind_Operate_No_Disk);
			break;
		default:
			break;
		}
		return str;
	}

	public String opCode2String(int op) {
		switch (op) {
		case FileOperationService.FILE_OP_DOWNLOAD:
			return getString(R.string.DM_Task_Download);
		case FileOperationService.FILE_OP_DELETE:
			return getString(R.string.DM_Task_Delete);
		case FileOperationService.FILE_OP_UPLOAD:
			return getString(R.string.DM_Task_upload);
		case FileOperationService.FILE_OP_COPYTO:
			return getString(R.string.DM_Task_Copy);
		}
		return null;
	}

	public void downloadFileToDO(Context context, DMFile file, int type) {
		mCancelCache = false;
		closeProgressDialog();
		mProgressDialog = new ProgressDialog(context);
		mProgressDialog.setProgress(0);
		mProgressDialog.setMessage(context
				.getString(R.string.DM_Fileexplore_Loading_File));
		mProgressDialog.setTitleContent(context
				.getString(R.string.DM_Task_Download));
		mProgressDialog.setLeftBtn(
				context.getString(R.string.DM_Control_Cancel),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						mCancelCache = true;
					}
				});
		doDownload(context, file, mProgressDialog, type);
		mProgressDialog.show();
	}

	private double mProgress;

	public void doDownload(final Context context, final DMFile file,
			final ProgressDialog dialog, final int type) {

		File directory = new File(FileOperationHelper.getInstance()
				.getCachePath());
		if (!directory.exists()) {
			directory.mkdir();
		}

		final File dstFile = new File(FileOperationHelper.getInstance()
				.getCachePath(), file.getName());
		if (dstFile.exists()) {

			if (dstFile.length() < file.mSize) {
				dstFile.delete();
			} else {
				mHandler.post(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						if (dialog != null && dialog.isShowing()) {
							dialog.setProgress(100);
							dialog.dismiss();
						}

					}

				});

				if (type == DOWN_TO_OPEN) {
					Uri uri = Uri.fromFile(dstFile);
					FileOperationHelper.getInstance().openDefault(uri,
							file.mType);
				} else {
					int shareType = getIntentShareType(file.getType());
					IntentShare.shareFile(dstFile.getPath(), shareType,
							mContext);
				}
				return;
			}
		}

		DropBoxFileOperationHelper.getInstance().doDownload(file,
				dstFile.getParent(),
				new DropBoxFileOperationHelper.ProgressListener() {

					@Override
					public boolean onProgressChange(final double progress) {
						// TODO Auto-generated method stub
						if (progress - mProgress >= 5 || progress == 100) {
							mHandler.post(new Runnable() {

								@Override
								public void run() {
									// TODO Auto-generated method stub
									mProgress = progress;
									if (dialog != null && dialog.isShowing()) {
										dialog.setProgress(progress);
									}

								}
							});
						}

						return mCancelCache;
					}

					@Override
					public int onFinished(final int err) {
						// TODO Auto-generated method stub
						mHandler.post(new Runnable() {

							@Override
							public void run() {
								// TODO Auto-generated method stub
								dialog.dismiss();

								if (err == 0) {
									if (type == DOWN_TO_OPEN) {
										Uri uri = Uri.fromFile(dstFile);
										FileOperationHelper.getInstance()
												.openDefault(uri, file.mType);
									} else {
										int shareType = getIntentShareType(file
												.getType());
										IntentShare.shareFile(
												dstFile.getPath(), shareType,
												mContext);
									}

								} else {
									Toast.makeText(
											mContext,
											mContext.getString(R.string.DM_Disk_Buffer_Fail),
											Toast.LENGTH_SHORT).show();
								}
							}
						});

						return 0;
					}
				});
	}

	public void shareFile(final DMFile xlfile) {
		if (!shareFormatSupport(xlfile.getType())) {
			Toast.makeText(mContext,
					mContext.getString(R.string.DM_Share_Format_Not_Support),
					1000).show();
			return;
		}
		if (xlfile.mLocation == DMFile.LOCATION_DROPBOX) {
			shareDropBoxFile(xlfile);
		} else {
			shareLocFile(xlfile);
		}

	}

	public void shareLocFile(DMFile xlfile) {

		int shareType = getIntentShareType(xlfile.getType());
		IntentShare.shareFile(xlfile.mPath, shareType, mContext);
	}

	public void shareDropBoxFile(DMFile xlfile) {
		downloadFileToDO(mContext, xlfile, DOWN_TO_SHARE);
	}

	public int getIntentShareType(DMFileCategoryType EType) {

		int shareType;
		if (EType == DMFileCategoryType.E_VIDEO_CATEGORY) {
			shareType = IntentShare.VIDEO;
		} else if (EType == DMFileCategoryType.E_PICTURE_CATEGORY) {
			shareType = IntentShare.IMAGE;
		} else if (EType == DMFileCategoryType.E_BOOK_CATEGORY) {
			shareType = IntentShare.TEXT;
		} else if (EType == DMFileCategoryType.E_MUSIC_CATEGORY) {
			shareType = IntentShare.MUSIC;
		} else {
			shareType = IntentShare.OTHER;
		}
		return shareType;
	}

	public boolean shareFormatSupport(DMFileCategoryType EType) {
		if (EType == DMFileCategoryType.E_VIDEO_CATEGORY) {
			return true;
		} else if (EType == DMFileCategoryType.E_PICTURE_CATEGORY) {
			return true;
		} else if (EType == DMFileCategoryType.E_BOOK_CATEGORY) {
			return false;
		} else if (EType == DMFileCategoryType.E_MUSIC_CATEGORY) {
			return true;
		} else {
			return false;
		}
	}
	private void closeUDiskEditTextDialog() {
		if(editTextDialog != null && editTextDialog.isShowing()) {
			editTextDialog.dismiss();
			editTextDialog = null;
		}
	}
	 UDiskEditTextDialog editTextDialog ;
	public void renameFile(final DMFile file) {

		final String namePart = FileInfoUtils.mainName(file.getName());
		final String extendPart = FileInfoUtils.extension(file.getName());
		closeUDiskEditTextDialog();
		editTextDialog = new UDiskEditTextDialog(
				getActivity(), UDiskEditTextDialog.TYPE_TWO_BTN);
		editTextDialog.setTitleContent(getActivity().getString(
				R.string.DM_Task_Rename));
		editTextDialog.setLeftBtn(getString(R.string.DM_Control_Cancel),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						editTextDialog.releaseDialog();
					}
				});

		editTextDialog.setRightBtn(getString(R.string.DM_Control_Definite),
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						String newNamePart = editTextDialog.getEditContent();
						String newName = null;
						if (file.isDir || extendPart.equals("")) {
							newName = newNamePart;
						} else {
							newName = newNamePart + "." + extendPart;
						}

						if (newName == null || newName.equals("")
								|| newNamePart == null
								|| newNamePart.equals("")) {
							editTextDialog
									.showWarnText(R.string.DM_More_Rename_No_Enpty);
							editTextDialog.setEditContent(namePart);
							editTextDialog.lockDialog();
							return;
						} else if (!FileInfoUtils.isValidFileName(newName)) {
							editTextDialog
									.showWarnText(R.string.DM_More_Rename_Name_Error);
							editTextDialog.lockDialog();
							return;
						} else if (file.getName().equals(newName)) {
							editTextDialog
									.showWarnText(R.string.DM_More_Rename_Placeholder);
							editTextDialog.lockDialog();
							return;
						} else if (DMDropboxAPI.getInstance().syncCheckExists(
								file.getParent() + File.separator + newName)) {
							editTextDialog
									.showWarnText(R.string.DM_More_Rename_BeUsed);
							editTextDialog.lockDialog();
							return;
						} else {
							editTextDialog.releaseDialog();
							ArrayList<DMFile> xlfileList = new ArrayList<>();
							xlfileList.add(file);
							newName = newName.trim();
							doFileRenameOperation(
									FileOperationService.FILE_OP_RENAME,
									xlfileList, newName);
						}
					}
				});
		editTextDialog.setEditContent(namePart);
		editTextDialog.show();
		editTextDialog.getEditTextView().setFocusable(true);
		editTextDialog.getEditTextView().setFocusableInTouchMode(true);
		editTextDialog.getEditTextView().requestFocus();
	}

	public void doFileRenameOperation(int op, List<DMFile> list, String newName) {

		Intent intent = new Intent(getActivity(), FileOperationService.class);
		Bundle bundle = new Bundle();
		bundle.putInt(FileOperationService.EXTRA_OP, op);
		bundle.putString(FileOperationService.Rename_Key, newName);
		bundle.putInt(FileOperationService.EXTRA_MODULE_TYPE,
				ModuleType.DropBox.ordinal());
		intent.putExtra(FileOperationService.EXTRA_BUNDLE, bundle);

		FileOperationService.selectedList = list;
		mContext.startService(intent);

	}

	private static final String LegalChar = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLIMNOPQRSTUVWXYZ(`-=[]\\;',./~!@#$%^&*()_+{}|:<>?\")";

	// ssid暂时不支持"这个字符
	private boolean hasIllegalChar(String str) {
		for (int i = 0; i < str.length(); i++) {
			if (("\"".indexOf(str.charAt(i)) >= 0)) {
				return true;
			}
		}
		return false;
	}
}
