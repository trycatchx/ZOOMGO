package com.dmsys.airdiskpro.ui;

import android.app.Activity;
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
import android.os.IBinder;
import android.os.Message;
import android.widget.Toast;

import com.dm.baselib.BaseValue;
import com.dm.share.IntentShare;
import com.dmairdisk.aodplayer.util.FileUtil;
import com.dmsys.airdiskpro.event.UploadEndEvent;
import com.dmsys.airdiskpro.filemanager.FileOperationHelper;
import com.dmsys.airdiskpro.filemanager.FileOperationService;
import com.dmsys.airdiskpro.filemanager.FileOperationService.ProgressInfo;
import com.dmsys.airdiskpro.filemanager.FileOperationService.SameNameInfo;
import com.dmsys.airdiskpro.ui.UploadBaseActivity.DestType;
import com.dmsys.airdiskpro.utils.ConvertUtil;
import com.dmsys.airdiskpro.utils.FileInfoUtils;
import com.dmsys.airdiskpro.utils.HandlerUtil;
import com.dmsys.airdiskpro.view.MessageDialog;
import com.dmsys.airdiskpro.view.MsgWidthCheckBoxDialog;
import com.dmsys.airdiskpro.view.ProgressDialog;
import com.dmsys.airdiskpro.view.UDiskBaseDialog;
import com.dmsys.airdiskpro.view.UDiskEditTextDialog;
import com.dmsys.dmsdk.DMSdk;
import com.dmsys.dmsdk.model.DMFile;
import com.dmsys.dmsdk.model.DMFileCategoryType;
import com.dmsys.mainbusiness.R;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import de.greenrobot.event.EventBus;


/**
 * 由于加入了DropBox 的代码导致此处的 代码多了一个 P， 也就是一个View 对应两个P 后面重构的时候可以整合为一个P 两个Mode
 * 
 * @author Administrator
 *
 */
public abstract class BaseActionActivity extends Activity {

	private Context mContext;
	private FileOperationService mOpService;
	public ProgressDialog mProgressDialog;
	public SharedPreferences mPreferences;
	public Editor mEditor;
	private String PREFERENCE_NAME = "PREFERENCE_NAME";
	private String KEY_SHOWNOMORE = "SHOWNOMORE";
	public HandlerUtil.StaticHandler mHandler;
	private FileOperationListener mOpListener;
	public int DOWN_TO_OPEN = 0;
	public int DOWN_TO_SHARE = 1;
	private boolean mCancelCache = false;

	public abstract void onOperationEnd(String opt);

	public void init(Context context) {
		this.mContext = context;
		this.mPreferences = getSharedPreferences(PREFERENCE_NAME,
				Context.MODE_PRIVATE);
		this.mEditor = mPreferences.edit();

		mOpListener = new FileOperationListener();
		mHandler = new HandlerUtil.StaticHandler(mOpListener);

		Intent intent = new Intent(this, FileOperationService.class);
		mContext.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
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
		if (mOpService != null)
			mContext.unbindService(mConnection);
	}

	public void doFileOperation(int op, List<DMFile> list) {

		FileOperationService.selectedList = list;

		Resources resource = this.getResources();

		Intent intent = new Intent(mContext, FileOperationService.class);
		Bundle bundle = new Bundle();
		bundle.putInt(FileOperationService.EXTRA_OP, op);
		intent.putExtra(FileOperationService.EXTRA_BUNDLE, bundle);
		mContext.startService(intent);

		String message = "";
		String title = opCode2String(op);
		String opName = opCode2String(op);
		mProgressDialog = new ProgressDialog(mContext);
		mProgressDialog.setTitleContent(title);
		mProgressDialog.setLeftBtn(
				resource.getString(R.string.DM_Control_Cancel),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						mOpService.cancelCurOperation();
						FileOperationHelper.getInstance().stop();
						Toast.makeText(mContext,
								R.string.DM_Remind_Operate_Stop,
								Toast.LENGTH_SHORT).show();
					}
				});

		mProgressDialog.show();

	}

	public void doNewFolderOperation(int op, List<DMFile> list) {

		Intent intent = new Intent(this, FileOperationService.class);
		Bundle bundle = new Bundle();
		bundle.putInt(FileOperationService.EXTRA_OP, op);
		FileOperationService.selectedList = list;
		intent.putExtra(FileOperationService.EXTRA_BUNDLE, bundle);
		startService(intent);
	}

	public void doFileUploadOperation(int op, List<DMFile> list,
			String desPath, DestType mDestType) {

		FileOperationService.selectedList = list;

		Intent intent = new Intent(this, FileOperationService.class);
		Bundle bundle = new Bundle();
		bundle.putInt(FileOperationService.EXTRA_OP, op);
		if (mDestType != null) {
			bundle.putInt(FileOperationService.EXTRA_MODULE_TYPE,
					mDestType.ordinal());
		}
		bundle.putString(FileOperationService.Rename_Key, desPath);
		intent.putExtra(FileOperationService.EXTRA_BUNDLE, bundle);
		this.startService(intent);

		Resources resource = this.getResources();

		String message = "";
		String title = opCode2String(op);
		String opName = opCode2String(op);

		mProgressDialog = new ProgressDialog.Builder(mContext)
				.setNumberVisiable(true)
				.setSpeedVisiable(true)
				.setTimeVisiable(true)
				.setSum("(" + list.size() + ")")
				.setTitler(title)
				.setLeftNumber(String.valueOf(list.size()))
				.setFileName(
						getString(R.string.DM_File_Operate_Remain_Time_unknow))
				.setLeftTime(
						getString(R.string.DM_File_Operate_Remain_Time_calc))
				.setSpeed(getString(R.string.DM_File_Operate_Remain_Time_calc))
				.setLeftBtn(resource.getString(R.string.DM_Control_Cancel),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// TODO Auto-generated method stub
								mOpService.cancelCurOperation();
								FileOperationHelper.getInstance().stop();
								Toast.makeText(mContext,
										R.string.DM_Remind_Operate_Stop,
										Toast.LENGTH_SHORT).show();
							}
						}).build();

		mProgressDialog.show();

	}

	public void doFileUploadOperation(int op, List<DMFile> list, String desPath) {

		doFileUploadOperation(op, list, desPath, null);

	}

	public void onSameFile(final SameNameInfo info) {
		final MsgWidthCheckBoxDialog dialog = new MsgWidthCheckBoxDialog(this);
		dialog.setCheckText(getString(R.string.DM_Task_Confirm_Operate));
		dialog.setMessage(String.format(
				getString(R.string.DM_Remind_Operate_SameFile), info.name));
		dialog.setTitleContent(getString(R.string.DM_Remind_Tips));
		dialog.setLeftBtn(getString(R.string.DM_Task_Confirm_Rename),
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dlg, int which) {
						// TODO Auto-generated method stub
						if (dialog.getChecked()) {
							info.ret = -FileOperationHelper.OP_RENAME;
						} else {
							info.ret = FileOperationHelper.OP_RENAME;
						}
						info.semp.release();
					}
				});

		dialog.setRightBtn(getString(R.string.DM_Task_Confirm_Jump),
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dlg, int which) {
						// TODO Auto-generated method stub
						// //System.out.println("exist ret0:"+info.ret);
						if (dialog.getChecked()) {
							info.ret = -FileOperationHelper.OP_SKIP;
						} else {
							info.ret = FileOperationHelper.OP_SKIP;
						}

						info.semp.release();
					}
				});

		dialog.show();
	}

	public void onFileSytemUnSupport(final SameNameInfo info) {
		// TODO Auto-generated method stub
		final MessageDialog dialog = new MessageDialog(this,
				UDiskBaseDialog.TYPE_ONE_BTN);

		dialog.setTitleContent(getString(R.string.DM_Remind_Tips));
		dialog.setMessage(String.format(
				getString(R.string.DM_Remind_Operate_Upload_Failed_Fat32),
				info.name));
		dialog.setLeftBtn(getString(R.string.DM_Task_Confirm_Jump),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dlg, int which) {
						// TODO Auto-generated method stub
						info.semp.release();
					}
				});

		dialog.show();
	}

	public void onSuccess(final String strOp, String desPath,
			List<DMFile> list, int size) {
		if (list == null || list.size() == 0
				|| strOp.equals(getString(R.string.DM_Task_Delete))) {
			return;
		}

		boolean show = mPreferences.getBoolean(KEY_SHOWNOMORE, true);
		if (!show) {
			return;
		}

		final MessageDialog dialog = new MessageDialog(this,
				UDiskBaseDialog.TYPE_ONE_BTN);

		String message = "";
		if (strOp.equals(getString(R.string.DM_Task_Download))) {
			message = getString(R.string.DM_Remind_Operate_Download_Done);
			message = String.format(message, String.valueOf(size), desPath);

		} else if (strOp.equals(getString(R.string.DM_Task_upload))) {
			message = getString(R.string.DM_Remind_Operate_Upload_Done);
			message = String.format(message, String.valueOf(size), desPath);
		}

		dialog.setTitleContent(opToString(strOp));
		dialog.setMessage(message);
		dialog.setLeftBtn(getString(R.string.DM_Control_Definite),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dlg, int which) {
						// TODO Auto-generated method stub

						if (strOp.equals(getString(R.string.DM_Task_upload))) {
							EventBus.getDefault().post(new UploadEndEvent());
						}

					}
				});

		dialog.show();
	}

	private String opToString(String strOp) {
		String retString = null;
		if (strOp.equals(getString(R.string.DM_Task_Download))) {
			retString = getString(R.string.DM_Remind_Operate_Download_Success);
		} else if (strOp.equals(getString(R.string.DM_Task_upload))) {
			retString = getString(R.string.DM_Remind_Operate_Upload_Success);
		}
		return retString;
	}

	public void onError(int err) {

		MessageDialog builder = new MessageDialog(this,
				UDiskBaseDialog.TYPE_ONE_BTN);
		builder.setTitleContent(getString(R.string.DM_Fileexplore_Operation_Warn_Error));
		builder.setLeftBtn(getString(R.string.DM_SetUI_Dialog_Button_Sure),
				null);
		builder.setMessage(strerr(this, err));
		builder.show();
	}

	private String strerr(Context context, int err) {
		String str = null;
		switch (err) {
		case FileOperationHelper.ERROR_UDISK_NOT_ENOUGH_SPACE:
			System.out.println("taskTotalSize22:" + BaseValue.taskTotalSize
					+ ",free:" + BaseValue.diskFreeSize);
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
		}
		return null;
	}

	private class FileOperationListener implements HandlerUtil.MessageListener {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			int msgWhat = msg.what;
			String strOp = null;

			if (msgWhat == FileOperationService.MSG_DELETE_FINISHED) {
				strOp = mContext.getString(R.string.DM_Task_Delete);
			} else if (msgWhat == FileOperationService.MSG_DOWNLOAD_FINISHED) {
				strOp = getString(R.string.DM_Task_Download);
			} else if (msgWhat == FileOperationService.MSG_UPLOAD_FINISHED) {
				strOp = getString(R.string.DM_Task_upload);
			} else if (msgWhat == FileOperationService.MSG_RENAME_FINISHED) {
				strOp = getString(R.string.DM_Task_Rename);
			} else if (msgWhat == FileOperationService.MSG_NEWFOLDER_FINISHED) {
				strOp = getString(R.string.DM_Task_Build_NewFolder);
			} else if (msgWhat == FileOperationService.MSG_PROGRESS_CHANGED) {
				// double progress = (Double)msg.obj;
				ProgressInfo info = (ProgressInfo) msg.obj;
				if (mProgressDialog != null) {
					mProgressDialog.setProgress(info.progress);
					// 名字
					if (!mProgressDialog.getMessage().equals(
							FileUtil.getFileNameFromPath(info.path))) {
						mProgressDialog.setMessage(FileUtil
								.getFileNameFromPath(info.path));
					}
					// 剩余时间
					if (info.timeLeft != null) {
						mProgressDialog.setTimeLeft(info.timeLeft);
					}
					// 剩余项
					if (info.sizeLeft != null) {
						mProgressDialog
								.setNumberLeft(String
										.format(getString(R.string.DM_File_Operate_Remain_Items),
												String.valueOf(info.numberLeft), String.valueOf(info.sizeLeft)));
					} else {
						mProgressDialog.setNumberLeft(String
								.valueOf(info.numberLeft));
					}
					// 速度
					if (info.speed != null && !info.speed.equals("")) {
						mProgressDialog.setSpeed(info.speed);
					}
				}
				return;
			} else if (msgWhat == FileOperationService.MSG_SAME_FILE) {
				onSameFile((SameNameInfo) msg.obj);
			} else if (msgWhat == FileOperationService.MSG_FILESYSTEM_UNSUPPORT) {
				onFileSytemUnSupport((SameNameInfo) msg.obj);
			} else if (msgWhat == FileOperationService.MSG_ERROR) {
				int err = (Integer) msg.obj;
				onError(err);
			} else if (msgWhat == FileOperationService.MSG_CONTAIN_SPECIAL_SYMBOLS) {
				String path = (String) msg.obj;
				// onSpecialSymbols(path);
			} else {
				return;
			}

			if (strOp != null) {
				if (msg.arg1 == FileOperationService.OP_SUCCESSED) {

					if (strOp.equals(mContext
							.getString(R.string.DM_Task_Download))) {
						if (msg.arg2 != 0) {
							Toast.makeText(
									mContext,
									R.string.DM_Remind_Operate_Download_Success,
									Toast.LENGTH_SHORT).show();
						}
					} else if (strOp.equals(mContext
							.getString(R.string.DM_Task_upload))) {
						if (msg.arg2 != 0) {
							Toast.makeText(mContext,
									R.string.DM_Remind_Operate_Upload_Success,
									Toast.LENGTH_SHORT).show();
							// EventBus.getDefault().post(new UploadEndEvent());
						}
					} else if (strOp.equals(mContext
							.getString(R.string.DM_Task_Delete))) {
						Toast.makeText(mContext,
								R.string.DM_Remind_Operate_Delete_Success,
								Toast.LENGTH_SHORT).show();
					} else if (strOp.equals(mContext
							.getString(R.string.DM_Task_Rename))) {
						Toast.makeText(mContext,
								R.string.DM_More_Rename_Updata_Success,
								Toast.LENGTH_SHORT).show();
					} else if (strOp.equals(mContext
							.getString(R.string.DM_Task_Build_NewFolder))) {
						Toast.makeText(
								mContext,
								R.string.DM_Fileexplore_Operation_NewFolder_Success,
								Toast.LENGTH_SHORT).show();
					}

					if (!strOp.equals(mContext
							.getString(R.string.DM_Task_Rename))) {
						HashMap<String, List> map = (HashMap<String, List>) msg.obj;
						Set set = map.keySet();
						Iterator iter = set.iterator();
						while (iter.hasNext()) {
							String key = (String) iter.next();
							String desPath = key;
							List<DMFile> files = map.get(key);
							// 显示dialog 提示用户
							onSuccess(strOp, desPath, files, msg.arg2);
						}
					}

				} else if (msg.arg1 == FileOperationService.OP_FAILED) {
					if (strOp.equals(mContext
							.getString(R.string.DM_Task_Download))) {
						Toast.makeText(mContext,
								R.string.DM_Remind_Operate_Download_Failed,
								Toast.LENGTH_SHORT).show();
					} else if (strOp.equals(mContext
							.getString(R.string.DM_Task_upload))) {
						Toast.makeText(mContext,
								R.string.DM_Remind_Operate_Upload_Failed,
								Toast.LENGTH_SHORT).show();
					} else if (strOp.equals(mContext
							.getString(R.string.DM_Task_Delete))) {
						Toast.makeText(mContext,
								R.string.DM_Remind_Operate_Delete_Failed,
								Toast.LENGTH_SHORT).show();
					} else if (strOp.equals(mContext
							.getString(R.string.DM_Task_Rename))) {
						Toast.makeText(mContext,
								R.string.DM_More_Rename_Updata_Error,
								Toast.LENGTH_SHORT).show();
					} else if (strOp.equals(mContext
							.getString(R.string.DM_Task_Build_NewFolder))) {
						Toast.makeText(mContext, R.string.DM_Task_Build_Failed,
								Toast.LENGTH_SHORT).show();
					}

					// onError(FileOperationHelper.ERROR_NOT_CONNECTED);
				}

				if (mProgressDialog != null) {
					mProgressDialog.cancel();
					mProgressDialog = null;
				}
			}

			onOperationEnd(strOp);
		}
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

	public void shareFile(final DMFile xlfile) {
		if (!shareFormatSupport(xlfile.getType())) {
			Toast.makeText(mContext,
					mContext.getString(R.string.DM_Share_Format_Not_Support), Toast.LENGTH_SHORT).show();
			return;
		}
		if (xlfile.mLocation == DMFile.LOCATION_LOCAL) {
			shareLocFile(xlfile);
		} else {
			shareUdiskFile(xlfile);
		}

	}

	public void shareLocFile(DMFile xlfile) {

		int shareType = getIntentShareType(xlfile.getType());
		IntentShare.shareFile(xlfile.mPath, shareType, mContext);
	}

	public void shareUdiskFile(DMFile xlfile) {
		downloadFileToDO(mContext, xlfile, DOWN_TO_SHARE);
	}

	public void downloadFileToDO(Context context, DMFile file, int type) {
		mCancelCache = false;

		final ProgressDialog dialog = new ProgressDialog(context);
		dialog.setProgress(0);
		dialog.setMessage(context
				.getString(R.string.DM_Fileexplore_Loading_File));
		dialog.setTitleContent(context.getString(R.string.DM_Task_Download));
		dialog.setLeftBtn(context.getString(R.string.DM_Control_Cancel),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						mCancelCache = true;
					}
				});
		doDownload(context, file, dialog, type);
		dialog.show();
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
			System.out.println("loca:" + dstFile.length());
			System.out.println("rem:" + file.mSize);
			if (dstFile.length() < file.mSize) {
				dstFile.delete();
			} else {
				mHandler.post(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						dialog.setProgress(100);
						dialog.dismiss();
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

		FileOperationHelper.getInstance().doDownload(file, dstFile.getParent(),
				new FileOperationHelper.ProgressListener() {

					@Override
					public boolean onProgressChange(final double progress) {
						// TODO Auto-generated method stub
						if (progress - mProgress >= 5 || progress == 100) {
							mHandler.post(new Runnable() {

								@Override
								public void run() {
									// TODO Auto-generated method stub
									mProgress = progress;
									dialog.setProgress(progress);
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
											Toast.LENGTH_LONG).show();
								}
							}
						});

						return 0;
					}
				});
	}

	public void renameFile(final DMFile file) {

		final String namePart = FileInfoUtils.mainName(file.getName());
		final String extendPart = FileInfoUtils.extension(file.getName());
		final UDiskEditTextDialog editTextDialog = new UDiskEditTextDialog(
				this, UDiskEditTextDialog.TYPE_TWO_BTN);
		editTextDialog.setTitleContent(getString(R.string.DM_Task_Rename));
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
						} else if (DMSdk.getInstance().isExisted(
								file.getParent() + File.separator + newName)) {
							editTextDialog
									.showWarnText(R.string.DM_More_Rename_BeUsed);
							editTextDialog.lockDialog();
							return;
						} else {
							editTextDialog.releaseDialog();
							ArrayList<DMFile> xlfileList = new ArrayList<>();
							xlfileList.add(file);
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
		editTextDialog.getEditTextView().pullUpKeyboard();
	}

	public void doFileRenameOperation(int op, List<DMFile> list, String newName) {

		FileOperationService.selectedList = list;

		Intent intent = new Intent(this, FileOperationService.class);
		Bundle bundle = new Bundle();
		bundle.putInt(FileOperationService.EXTRA_OP, op);
		bundle.putString(FileOperationService.Rename_Key, newName);
		intent.putExtra(FileOperationService.EXTRA_BUNDLE, bundle);
		startService(intent);

	}
}
