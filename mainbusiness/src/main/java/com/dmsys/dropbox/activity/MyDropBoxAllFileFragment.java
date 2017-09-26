package com.dmsys.dropbox.activity;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dmairdisk.aodplayer.api.AodPlayer;
import com.dmairdisk.aodplayer.dialog.MusicPlayerDialog;
import com.dmsys.airdiskpro.adapter.PopupAdapter;
import com.dmsys.airdiskpro.filemanager.FileManager;
import com.dmsys.airdiskpro.filemanager.FileOperationService;
import com.dmsys.airdiskpro.filemanager.FileOperationService.ProgressInfo;
import com.dmsys.airdiskpro.filemanager.FileOperationService.SameNameInfo;
import com.dmsys.airdiskpro.ui.MainActivity;
import com.dmsys.airdiskpro.ui.PictureFolderActivity;
import com.dmsys.airdiskpro.ui.UploadBaseActivity.DestType;
import com.dmsys.airdiskpro.ui.UploadBaseActivity.FileType;
import com.dmsys.airdiskpro.ui.UploadDirActivity;
import com.dmsys.airdiskpro.ui.UploadFileActivity;
import com.dmsys.airdiskpro.utils.FileInfoUtils;
import com.dmsys.airdiskpro.utils.FileUtil;
import com.dmsys.airdiskpro.utils.HandlerUtil;
import com.dmsys.airdiskpro.view.DMPopup;
import com.dmsys.airdiskpro.view.FileBrowseDialog;
import com.dmsys.airdiskpro.view.FileBrowseDialog.FileBrowseDialogOnClickListener;
import com.dmsys.airdiskpro.view.FolderSelector;
import com.dmsys.airdiskpro.view.MessageDialog;
import com.dmsys.airdiskpro.view.UDiskEditTextDialog;
import com.dmsys.dmsdk.model.DMDir;
import com.dmsys.dmsdk.model.DMFile;
import com.dmsys.dropbox.api.DMDropboxAPI;
import com.dmsys.dropbox.api.OnDropBoxDirViewStateChangeListener;
import com.dmsys.dropbox.api.OnDropBoxFileItemClickListener;
import com.dmsys.dropbox.api.OnDropBoxMusicChangeListener;
import com.dmsys.dropbox.utils.DropBoxAttributeTask;
import com.dmsys.dropbox.utils.DropBoxVodUrlConversionHelper;
import com.dmsys.dropbox.view.BaseDirView;
import com.dmsys.dropbox.view.BaseDirView.Onload;
import com.dmsys.dropbox.view.MyDropBoxAllFileView;
import com.viewpagerindicator.TabPageIndicator;
import com.dmsys.mainbusiness.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import de.greenrobot.event.EventBus;

public class MyDropBoxAllFileFragment extends BaseMyDropBoxAllFileFragment
		implements OnClickListener, OnDropBoxDirViewStateChangeListener,
		OnDropBoxFileItemClickListener, OnDropBoxMusicChangeListener {

	private View parent;

	public static final int VIEW_DIR = 0;
	public static final int VIEW_TYPE = 1;

	private int mViewType = VIEW_DIR;

	private MyDropBoxAllFileView mFileListView;
	private FolderSelector mPathView;

	private String[] mFolderArray;
	private String mRootName;

	/** 音乐 **/
	private ImageButton ibtn_music;
	private MusicPlayerDialog mMusicPlayerDialog;

	/** 顶部栏 **/
	private FrameLayout backLayout;
	private ImageView backButton;
	private RelativeLayout layout_newfolder;
	private RelativeLayout normalLayout;
	private RelativeLayout layout_edit;
	private TextView selectAllText;
	private TextView mainText;
	private ImageView newTips;
	private ViewPager mViewPager;
	private TabPageIndicator mTabIndicator;
	private View occupyView;
	private CheckBox mCheckBox;
	private Button mButton;

	private LinearLayout bottom;
	private LinearLayout mUploadLayout;
	
	private RelativeLayout layout_title_more;

	/** 底部action栏 **/
	private TextView downloadText;
	private TextView copyText;
	private TextView deleteText;
	private View moreImage;
	private boolean mEditMode;
	private boolean mBackgroud;
	private HandlerUtil.StaticHandler mHandler;
	private int curFileType = BaseDirView.FILE_TYPE_AIRDISK;
	// 切换allfile、分类的话，设置mFileListview 去加载分类；
	private DMFile newFolder;
	private DMPopup mPopup,mTittlePopup;
	private PopupAdapter mPopupAdapter;
	private WindowManager mWindowManager;

	public MyDropBoxAllFileFragment() {
		super();
		// TODO Auto-generated constructor stub
	}

	public static final String PAGER_TYPE = "PAGER_TYPE";
	private ViewGroup mLoadingView;
	FileBrowseDialog mFileBrowseDialog;
	private Activity mContext;

	public int DOWN_TO_OPEN = 0;
	public int DOWN_TO_SHARE = 1;
	DropBoxFileOperationListener mOpListener;
	
	
	abstract interface OnDropBoxEditableChange {

		public abstract void onChange(boolean show);
	}
	
	OnDropBoxEditableChange onDropBoxEditableChange;


	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
		mContext = getActivity();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		System.out.println("TT onCreateView");
		Bundle bundle = getArguments();
		curFileType = bundle.getInt(PAGER_TYPE, -1);
		parent = (ViewGroup) inflater.inflate(R.layout.dropbox_file_explorer,container, false);
		initViews();
		reloadItems();
		return parent;
	}

	private void initViews() {
		// TODO Auto-generated method stub
		mFileListView = (MyDropBoxAllFileView) parent.findViewById(R.id.lv_file_list);
		mFileListView.init(curFileType);

		mFileListView.setOnDropBoxDirViewStateChangeListener(this);
		mFileListView.setOnFileItemClickListener(this);
		mFileListView.setOnDropBoxDirViewStateChangeListener(this);
		mFileListView.setOnDropBoxMusicChangeListener(this);

		mLoadingView = ((ViewGroup) parent.findViewById(R.id.loading));
		mFileListView.setOnloadListener(new Onload() {

			@Override
			public void begin() {
				// TODO Auto-generated method stub
				mLoadingView.setVisibility(View.VISIBLE);
			}

			@Override
			public void end() {
				// TODO Auto-generated method stub
				mLoadingView.setVisibility(View.GONE);
			}
		});
		mPathView = (FolderSelector) parent.findViewById(R.id.et_navigate);
		mPathView.setOnClickListener(new FolderSelector.ItemOnClickListener() {

			@Override
			public void onClick(int idx) {
				// TODO Auto-generated method stub

				if (mFolderArray != null) {
					int step = mFolderArray.length - idx - 1;
					if (step > 0) {
						mFileListView.toUpperPathByStep(step);
					}
				}

				if (mEditMode) {
					setEditState(DropBoxEditState.STATE_NORMAL);
				}
			}
		});

		backButton = (ImageView) parent.findViewById(R.id.titlebar_left);
		backButton.setVisibility(View.VISIBLE);
		backButton.setImageResource(R.drawable.dropbox_bg_common_back);
		backLayout = (FrameLayout) parent.findViewById(R.id.layout_back);
		backLayout.setOnClickListener(this);

		normalLayout = (RelativeLayout) parent.findViewById(R.id.layout_normal);
		selectAllText = (TextView) parent.findViewById(R.id.text_selectall);
		selectAllText.setOnClickListener(this);

		
		parent.findViewById(R.id.layout_search).setVisibility(View.GONE);;
		
		layout_title_more = (RelativeLayout) parent.findViewById(R.id.layout_title_more);
		layout_title_more.setOnClickListener(this);
		
		mainText = (TextView) parent.findViewById(R.id.titlebar_title);
		ibtn_music = (ImageButton) parent.findViewById(R.id.ibtn_music);
		ibtn_music.setOnClickListener(this);
		if (AodPlayer.getInstance().getIsPlaying()
				&& curFileType != BaseDirView.FILE_TYPE_PATHSELECT) {
			ibtn_music.setVisibility(View.VISIBLE);
		} else {
			ibtn_music.setVisibility(View.GONE);
		}

		bottom = (LinearLayout) parent.findViewById(R.id.bottom);
		bottom.setVisibility(View.VISIBLE);
		downloadText = (TextView) parent.findViewById(R.id.op_download);
		downloadText.setOnClickListener(this);
		copyText = (TextView) parent.findViewById(R.id.op_cpTo);
		copyText.setOnClickListener(this);
		deleteText = (TextView) parent.findViewById(R.id.op_delete);
		deleteText.setOnClickListener(this);
		moreImage = parent.findViewById(R.id.op_more);
		moreImage.setOnClickListener(this);
		
		mUploadLayout = (LinearLayout) parent.findViewById(R.id.layout_upload);
		mUploadLayout.setOnClickListener(this);
		mUploadLayout.setVisibility(View.VISIBLE);
		
		mPopupAdapter = new PopupAdapter(mContext);
		mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);

		setEditState(DropBoxEditState.STATE_NORMAL);
		
		mOpListener = new DropBoxFileOperationListener();
		mHandler = new HandlerUtil.StaticHandler(mOpListener);
		
		initFileBrowseDialog();
	}
	
	private void initFileBrowseDialog() {
		// TODO Auto-generated method stub
		mFileBrowseDialog = new FileBrowseDialog(mContext);
		mFileBrowseDialog.setFileBrowseDialogOnClickListener(new FileBrowseDialogOnClickListener() {
			@Override
			public void FirstImageViewOnClick() {
				// TODO Auto-generated method stub
				Intent mIntent = new Intent(mContext,PictureFolderActivity.class);
				mIntent.putExtra("CurPath", getCurrentPath());
				mIntent.putExtra("DestType", DestType.DropBox.ordinal());
				startActivityForResult(mIntent, 1111);
			}
		
			@Override
			public void SecondImageViewOnClick() {
				// TODO Auto-generated method stub
				Intent mIntent = new Intent(mContext,UploadFileActivity.class);
				mIntent.putExtra("FileType", FileType.VIODE.ordinal());
				mIntent.putExtra("CurPath", getCurrentPath());
				mIntent.putExtra("DestType", DestType.DropBox.ordinal());
				startActivityForResult(mIntent, 1111);
			}
			
			@Override
			public void ThirdImageViewOnClick() {
				// TODO Auto-generated method stub
				Intent mIntent = new Intent(mContext,UploadFileActivity.class);
				mIntent.putExtra("FileType", FileType.AUDIO.ordinal());
				mIntent.putExtra("CurPath", getCurrentPath());
				mIntent.putExtra("DestType", DestType.DropBox.ordinal());
				startActivityForResult(mIntent, 1111);
			}
			
			@Override
			public void FourthImageViewOnClick() {
				// TODO Auto-generated method stub
				Intent mIntent = new Intent(mContext,UploadDirActivity.class);
				mIntent.putExtra("FileType", FileType.AUDIO.ordinal());
				mIntent.putExtra("CurPath", getCurrentPath());
				mIntent.putExtra("DestType", DestType.DropBox.ordinal());
				startActivityForResult(mIntent, 1111);
				
			}
			@Override
			public void CloseOnClick() {
				// TODO Auto-generated method stub
				
			}
		});
		
		mFileBrowseDialog.setOnDismissListener(new OnDismissListener() {
			
			@Override
			public void onDismiss(DialogInterface dialog) {
				// TODO Auto-generated method stub
				parent.findViewById(R.id.bottom_guide_bar).setVisibility(View.VISIBLE);
			}
		});
	}
	private class DropBoxFileOperationListener implements HandlerUtil.MessageListener {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			int msgWhat = msg.what;
			String strOp = null;

			if (msgWhat == FileOperationService.MSG_DELETE_FINISHED) {
				strOp = mContext.getString(R.string.DM_Task_Delete);
			} else if (msgWhat == FileOperationService.MSG_DOWNLOAD_FINISHED) {
				strOp = getString(R.string.DM_Task_Download);
			} else if (msgWhat == FileOperationService.MSG_RENAME_FINISHED) {
				strOp = getString(R.string.DM_Task_Rename);
			} else if (msgWhat == FileOperationService.MSG_NEWFOLDER_FINISHED) {
				strOp = getString(R.string.DM_Task_Build_NewFolder);
			} else if (msgWhat == FileOperationService.MSG_COPY_FINISHED) {
				strOp = getString(R.string.DM_Task_Copy);
				
			} else if (msgWhat == FileOperationService.MSG_PROGRESS_CHANGED) {
				ProgressInfo info = (ProgressInfo) msg.obj;
				if (mProgressDialog != null && mProgressDialog.isShowing()) {
					mProgressDialog.setProgress(info.progress);
					if (!mProgressDialog.getMessage().equals(FileUtil.getFileNameFromPath(info.path))) {
						mProgressDialog.setMessage(FileUtil.getFileNameFromPath(info.path));
					}
				}
				return;
			} else if (msgWhat == FileOperationService.MSG_SAME_FILE) {
				SameNameInfo info = (SameNameInfo) msg.obj;
				onSameFile(info);
				if (info.operation == FileOperationService.FILE_OP_COPYTO) {
					System.out.println("copyto same");
					return;
				}
			} else if (msgWhat == FileOperationService.MSG_ERROR) {
				int err = (Integer) msg.obj;
				onError(err);
			} else if (msgWhat == FileOperationService.MSG_CONTAIN_SPECIAL_SYMBOLS) {
//				String path = (String) msg.obj;
//				 onSpecialSymbols(path);
			} else {
				return;
			}

			if (strOp != null) {
				if (msg.arg1 == FileOperationService.OP_SUCCESSED) {

					if (strOp.equals(mContext.getString(R.string.DM_Task_Download))) {
						if (msg.arg2 != 0) {
							Toast.makeText(mContext, R.string.DM_Remind_Operate_Download_Success, Toast.LENGTH_SHORT).show();
						}
					} else if (strOp.equals(mContext.getString(R.string.DM_Task_Delete))) {
						Toast.makeText(mContext, R.string.DM_Remind_Operate_Delete_Success, Toast.LENGTH_SHORT).show();
					} else if (strOp.equals(mContext.getString(R.string.DM_Task_Rename))) {
						Toast.makeText(mContext, R.string.DM_More_Rename_Updata_Success, Toast.LENGTH_SHORT).show();
					} else if (strOp.equals(mContext.getString(R.string.DM_Task_Build_NewFolder))) {
						Toast.makeText(mContext, R.string.DM_Fileexplore_Operation_NewFolder_Success,Toast.LENGTH_SHORT).show();
						if (curFileType ==  BaseDirView.FILE_TYPE_PATHSELECT) {
							mFileListView.gotoSubPatg(newFolder);
						}
						newFolder = null;
					}else if (strOp.equals(mContext.getString(R.string.DM_Task_Copy))) {
						if (msg.arg2 != 0) {
							Toast.makeText(mContext, R.string.DM_Remind_Operate_Copy_Success, Toast.LENGTH_SHORT).show();
						}
					}

					if (!strOp.equals(mContext.getString(R.string.DM_Task_Rename))) {
						HashMap<String, List> map = (HashMap<String, List>) msg.obj;
						Set set = map.keySet();
						Iterator iter = set.iterator();
						while (iter.hasNext()) {
							String key = (String) iter.next();
							String despath = key;
							List<DMFile> files = map.get(key);
							// 显示dialog 提示用户
							onSuccess(strOp, despath, files, msg.arg2);
							if (strOp != null && strOp.equals(getString(R.string.DM_Task_Copy))) {
								return;
							}
						}
					}
					
					setEditState(DropBoxEditState.STATE_NORMAL);

				} else if (msg.arg1 == FileOperationService.OP_FAILED) {
					if (strOp.equals(mContext.getString(R.string.DM_Task_Download))) {
						Toast.makeText(mContext, R.string.DM_Remind_Operate_Download_Failed, Toast.LENGTH_SHORT).show();
					} else if (strOp.equals(mContext.getString(R.string.DM_Task_Delete))) {
						int skip = (int) msg.obj;
						Toast.makeText(mContext, String.format(getString(R.string.DM_Remind_Operate_Delete_Success_Pass), String.valueOf(skip)), Toast.LENGTH_SHORT).show();
					} else if (strOp.equals(mContext.getString(R.string.DM_Task_Rename))) {
						Toast.makeText(mContext, R.string.DM_More_Rename_Updata_Error, Toast.LENGTH_SHORT).show();
					} else if (strOp.equals(mContext.getString(R.string.DM_Task_Build_NewFolder))) {
						Toast.makeText(mContext, R.string.DM_Task_Build_Failed, Toast.LENGTH_SHORT).show();
					}
				}

				if (mProgressDialog != null) {
					mProgressDialog.cancel();
					mProgressDialog = null;
				}

				if (!strOp.equals(getString(R.string.DM_Task_Download))) {

					// 刷新视图
					mFileListView.reloadItems();
				}
			}

		}
	}

	@Override
	public void onStart() {
		super.onStart();
		
	};

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		init(mContext, mHandler);
		super.onResume();
		mBackgroud = false;
		if (mEditMode) {
			setEditState(DropBoxEditState.STATE_NORMAL);
		}

		mainText.setText(R.string.DM_my_dropbox);

		reloadItems();

	}
	

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		unInit();
		super.onPause();
	}

	@Override
	public void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		mBackgroud = true;
		
		
	}

	@Override
	public void onDestroyView() {
		// TODO Auto-generated method stub
		super.onDestroyView();
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		DropBoxVodUrlConversionHelper.getInstance().stopHttpServer();
		mFileListView.removeAvodListener();
	}

	public void reloadItems() {
		// TODO Auto-generated method stub
			mFileListView.loadFiles();
	}

	public boolean isEditMode() {
		return mEditMode;
	}

	// public IFileExplorer getFileView(){
	// return mFileView;
	// }

	// @Override
	// public void resetPage() {
	// // TODO Auto-generated method stub
	// isLoaded = false;
	// }

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		int i = v.getId();
		if (i == R.id.ibtn_music) {
			showMusicDialog();

		} else if (i == R.id.layout_back) {
			if (mEditMode) {
				unselectAll();
				setEditState(DropBoxEditState.STATE_NORMAL);
			} else {
				if (curFileType == BaseDirView.FILE_TYPE_AIRDISK) {
					getActivity().finish();
				} else {
					EventBus.getDefault().post(new MainActivity.Toggle());
				}
			}

		} else if (i == R.id.layout_title_more) {
			showTittleDialog();

		} else if (i == R.id.item_edit) {
			mTittlePopup.dismiss();
			setEditState(DropBoxEditState.STATE_EDIT);

		} else if (i == R.id.item_newfolder) {
			mTittlePopup.dismiss();
			doFileOperation(FileOperationService.FILE_OP_NEWFOLDER);

		} else if (i == R.id.text_selectall) {
			if (selectAllText.getText().equals(
					getString(R.string.DM_Control_Select))) {
				selectAllText.setText(R.string.DM_Control_Uncheck_All);
				selectAll();
			} else {
				selectAllText.setText(R.string.DM_Control_Select);
				unselectAll();
			}

		} else if (i == R.id.op_download) {
			doFileOperation(FileOperationService.FILE_OP_DOWNLOAD);

		} else if (i == R.id.op_cpTo) {
			final List<DMFile> files = getSelectedFiles();
			if (files.size() == 0) {
				Toast.makeText(mContext, R.string.DM_FileOP_Warn_Select_File,
						Toast.LENGTH_SHORT).show();
				return;
			}

			FileOperationService.selectedList = files;

			Intent cpintent = new Intent(mContext, MyDropBoxPathSelectActivity.class);
			cpintent.putExtra("COPYTO", true);
			startActivity(cpintent);

		} else if (i == R.id.op_delete) {
			doFileOperation(FileOperationService.FILE_OP_DELETE);

		} else if (i == R.id.op_more) {
			showMoreDialog();

		} else if (i == R.id.layout_upload) {
			parent.findViewById(R.id.bottom_guide_bar).setVisibility(View.GONE);
			mFileBrowseDialog.show();

		} else {
		}
	}

	public void unselectAll() {
		// TODO Auto-generated method stub
		mFileListView.unselectAll();
	}

	public void selectAll() {
		// TODO Auto-generated method stub
		mFileListView.selectAll();
	}

	public void setEditState(DropBoxEditState state) {
		// TODO Auto-generated method stub

		mFileListView.switchMode(state);

		// 弹出底部的横条
		switchMode(state);

	}

	public boolean isCanToUpper() {
		return mFileListView.isCanToUpper();
	}

	public void toUpper() {
		mFileListView.toUpperPath();
	}

	public void switchMode(DropBoxEditState mode) {
		// TODO Auto-generated method stub

		if (mode == DropBoxEditState.STATE_EDIT) {

			mEditMode = true;

			selectAllText.setText(R.string.DM_Control_Select);
			selectAllText.setVisibility(View.VISIBLE);

			backButton.setImageResource(R.drawable.sel_upload_close);

			normalLayout.setVisibility(View.GONE);

//			newTips.setVisibility(View.GONE);

			mainText.setText(String
					.format(getResources().getString(
							R.string.DM_Navigation_Upload_Num), "0"));

			if (AodPlayer.getInstance().getIsPlaying()) {
				ibtn_music.setVisibility(View.GONE);
			}
		} else {

			mEditMode = false;

			selectAllText.setVisibility(View.GONE);

			backButton.setImageResource(R.drawable.dropbox_bg_common_back);

			normalLayout.setVisibility(View.VISIBLE);
			
			if (curFileType == BaseDirView.FILE_TYPE_PATHSELECT) {
				backButton.setImageResource(R.drawable.dm_lib_wifi_back_btn_bg_selector);
			}

//			if (BaseValue.dmota != null
//					&& (BaseValue.dmota.flag == 1 || BaseValue.dmota.flag == 2)) {
//				newTips.setVisibility(View.VISIBLE);
//			}

			mainText.setText(R.string.DM_my_dropbox);

			if (AodPlayer.getInstance().getIsPlaying()) {
				ibtn_music.setVisibility(View.VISIBLE);
			}
		}

		onEditModeChange(mEditMode);
	}

	private void onEditModeChange(boolean mode) {
		
		View bottomActionBar = parent.findViewById(R.id.bottom_action_bar);
		View bottomGuideBar = parent.findViewById(R.id.bottom_guide_bar);
		if (mode) {
			bottomGuideBar.setVisibility(View.GONE);
			bottomActionBar.setVisibility(View.VISIBLE);
		}else {
			bottomGuideBar.setVisibility(View.VISIBLE);
			bottomActionBar.setVisibility(View.GONE);
		}
	}

	private List<DMFile> getSelectedFiles() {
		return mFileListView.getSelectedFiles();
	}

	private void showMoreDialog() {
		// TODO Auto-generated method stub

		if (mPopup != null && mPopup.isShowing()) {
			mPopup.dismiss();
			return;
		}

		final List<DMFile> files = mFileListView.getSelectedFiles();
		if (files.size() == 0) {
			Toast.makeText(mContext, R.string.DM_FileOP_Warn_Select_File,
					Toast.LENGTH_SHORT).show();
			return;
		}

		mPopup = new DMPopup(mContext, DMPopup.VERTICAL);

		View contentView = LayoutInflater.from(mContext).inflate(R.layout.popup_content, null);

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
						getString(R.string.DM_Task_Rename))) {
					onClickRename(files.get(0));
				} else if (mdata.get(position).equals(
						getString(R.string.DM_Task_Details))) {
					onClickDetail(files);
				}

				mPopup.dismiss();
			}
		});

		LayoutParams params = new LayoutParams((mWindowManager
				.getDefaultDisplay().getWidth() * 1) / 2,
				LayoutParams.WRAP_CONTENT);
		contentView.setLayoutParams(params);
		mPopup.addView(contentView);

		mPopup.show(bottom);

	}

	private List<String> getPopData(List<DMFile> files) {
		// TODO Auto-generated method stub
		List<String> data = new ArrayList<>();
		if (files.size() == 1) {
			if (files.get(0).isDir == true) {
				data.add(getString(R.string.DM_Task_Rename));
				data.add(getString(R.string.DM_Task_Details));
			} else {
				data.add(getString(R.string.DM_Task_Open_By));
				data.add(getString(R.string.DM_Task_Share));
				data.add(getString(R.string.DM_Task_Rename));
				data.add(getString(R.string.DM_Task_Details));
			}
		} else if (files.size() > 1) {
			data.add(getString(R.string.DM_Task_Details));
		}
		return data;
	}
	
	private void showTittleDialog() {
		// TODO Auto-generated method stub
		
		if (mTittlePopup != null && mTittlePopup.isShowing()) {
			mTittlePopup.dismiss();
			return;
		}
		mTittlePopup = new DMPopup(mContext,DMPopup.VERTICAL);
		View contentView = LayoutInflater.from(mContext).inflate(R.layout.popup_operation, null);
		TextView editText = (TextView) contentView.findViewById(R.id.item_edit);
		TextView newText = (TextView) contentView.findViewById(R.id.item_newfolder);
		TextView sortText = (TextView) contentView.findViewById(R.id.item_sort);
		editText.setOnClickListener(this);
		sortText.setOnClickListener(this);
		newText.setOnClickListener(this);
		
		sortText.setVisibility(View.GONE);
		if (curFileType == BaseDirView.FILE_TYPE_PATHSELECT) {
			editText.setVisibility(View.GONE);
		}
		
		LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		contentView.setLayoutParams(params);
		mTittlePopup.addView(contentView);
		mTittlePopup.show(layout_title_more);
	}

	protected void onClickThirdParty(DMFile file) {

		 downloadFileToDO(mContext,file,DOWN_TO_OPEN);
	}

	protected void onClickShare(DMFile file) {
		// TODO Auto-generated method stub
		 shareFile(file);
	}

	private void onClickRename(DMFile file) {
		// TODO Auto-generated method stub
		 renameFile(file);
	}

	protected void onClickDetail(List<DMFile> files) {
		// TODO Auto-generated method stub
		DropBoxAttributeTask mAttributeTask = null;

		if (files.size() == 1) {
			if (isInPictureType()) {
				mAttributeTask = new DropBoxAttributeTask(mContext, true, files.get(0));
			} else {
				mAttributeTask = new DropBoxAttributeTask(mContext, files.get(0));
			}

		} else {
			mAttributeTask = new DropBoxAttributeTask(mContext, files);
		}
		mAttributeTask.execute();
	}

	public boolean isInPictureType() {
		if (mViewType == VIEW_TYPE && mViewPager.getCurrentItem() == 1) {
			return true;
		}
		return false;
	}

	public void doFileOperation(final int op) {

		final List<DMFile> list = mFileListView.getSelectedFiles();
		if (op != FileOperationService.FILE_OP_NEWFOLDER && op != FileOperationService.FILE_OP_COPYTO && list.size() == 0) {
			Toast.makeText(mContext, R.string.DM_FileOP_Warn_Select_File, Toast.LENGTH_SHORT).show();
			return;
		}
		
     if (op == FileOperationService.FILE_OP_DELETE) {
			
			MessageDialog builder = new MessageDialog(mContext);
			builder.setTitleContent(getString(R.string.DM_Task_Delete));
			builder.setMessage(getString(R.string.DM_Remind_Operate_Delete_File));
			builder.setLeftBtn(getString(R.string.DM_Control_Cancel), new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
				}
			});

			builder.setRightBtn(getString(R.string.DM_Control_Definite), new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
						doFileOperation(op,list);
				}
			});

			builder.show();

		} else if (op == FileOperationService.FILE_OP_NEWFOLDER) {

			final UDiskEditTextDialog builder = new UDiskEditTextDialog(mContext, UDiskEditTextDialog.TYPE_TWO_BTN);
			builder.setTitleContent(getString(R.string.DM_Task_Build_NewFolder));
			builder.setLeftBtn(getString(R.string.DM_Control_Cancel), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					builder.releaseDialog();
				}
			});
			builder.setRightBtn(getString(R.string.DM_Control_Definite), new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					String name = builder.getEditContent().trim();
					if (name == null || name.equals("")) {
						builder.showWarnText(R.string.DM_More_Rename_No_Enpty);
						builder.lockDialog();
					} else if (!FileInfoUtils.isValidFileName(name)) {
						builder.showWarnText(R.string.DM_More_Rename_Name_Error);
						builder.lockDialog();
					} else if (DMDropboxAPI.getInstance().syncCheckExists(mFileListView.getCurrentPath() + File.separator + name)) {
						builder.showWarnText(R.string.DM_More_Rename_BeUsed);
						builder.lockDialog();
					} else {
						builder.releaseDialog();
						DMDir file = new DMDir();
						file.mName = name;
						file.mLocation = DMFile.LOCATION_UDISK;
						file.mPath = mFileListView.getCurrentPath() + File.separator + name;
						newFolder = file;
						doNewFolderOperation(FileOperationService.FILE_OP_NEWFOLDER, file);
					}
				}
			});
			builder.show();
			builder.getEditTextView().setFocusable(true);
			builder.getEditTextView().setFocusableInTouchMode(true);
			builder.getEditTextView().requestFocus();

		} else {
			doFileOperation(op, list);
		}
	}

	private void showMusicDialog() {


		if (mMusicPlayerDialog == null) {
			mMusicPlayerDialog = new MusicPlayerDialog(mContext);
		}
		mMusicPlayerDialog.show();
	}

	private void closeMusicDialog() {
		if (mMusicPlayerDialog != null && mMusicPlayerDialog.isShowing()) {
			mMusicPlayerDialog.cancel();
		}
		mMusicPlayerDialog = null;
	}

	public enum DropBoxEditState {

		STATE_NORMAL, STATE_SHARE, STATE_EDIT

	}

	public interface IDropBoxFileExplorer {

		public void switchMode(DropBoxEditState mode);

		public void selectAll();

		public void unselectAll();

		public List<DMFile> getSelectedFiles();

		public void reloadItems();

	}

	protected void setEditLayoutVisible(boolean visible) {
		// TODO Auto-generated method stub
		if (visible) {
			normalLayout.setVisibility(View.VISIBLE);
			//layout_newfolder.setVisibility(View.VISIBLE);
			//layout_edit.setVisibility(View.VISIBLE);
		} else {
			normalLayout.setVisibility(View.GONE);
		}

	}

	@Override
	public void onChange(DropBoxEditState state, String pathName,
			List<DMFile> fileList) {
		// TODO Auto-generated method stub
		if (fileList == null)
			return;
		if (state != DropBoxEditState.STATE_NORMAL) {
			// 编辑模式下更新title
			int count = FileManager.getSelectedCount(fileList);
			mainText.setText(String
					.format(getResources().getString(
							R.string.DM_Navigation_Upload_Num), String.valueOf(count)));

		} else {
			FileManager.unselectAll(fileList);
			String rPath = mFileListView.getRelativePath(pathName);

			if (rPath.equals("") || rPath.equals("/")) {
				mFolderArray = null;
				if (curFileType == BaseDirView.FILE_TYPE_AIRDISK
						|| curFileType == BaseDirView.FILE_TYPE_PATHSELECT) {
					setEditLayoutVisible(true);
				} else {
					setEditLayoutVisible(true);
					layout_newfolder.setVisibility(View.GONE);
				}
				
				if (onDropBoxEditableChange != null) {
					onDropBoxEditableChange.onChange(true);
				}
			} else {

				String[] array = rPath.split("/");
				mFolderArray = Arrays.copyOfRange(array, 0, array.length);
				setEditLayoutVisible(true);
				
				if (onDropBoxEditableChange != null) {
					onDropBoxEditableChange.onChange(true);
				}
			}
			mRootName = mContext.getString(R.string.DM_dropbox);
			mPathView.setFoder(mRootName, mFolderArray);
		}
	}

	@Override
	public boolean onFileClick(int position) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onFileLongClick(int position) {
		// TODO Auto-generated method stub
		if (!mBackgroud) {
			switchMode(DropBoxEditState.STATE_EDIT);
			mainText.setText(String
					.format(getResources().getString(
							R.string.DM_Navigation_Upload_Num), "1"));
		}
		return true;
	}

	@Override
	public void onMusicChange(int state, boolean show) {
		// TODO Auto-generated method stub
		if (state == 0) {
			ibtn_music.setVisibility(View.GONE);
			closeMusicDialog();
		} else if (state == 1) {
			ibtn_music.setVisibility(View.VISIBLE);
		}

		if (show && !mBackgroud) {
			showMusicDialog();
		}
	}
	public String getCurrentPath() {
		return mFileListView.getCurrentPath();
	}
	public void setTitle(String title){
		 mainText.setText(title);
	}
	
	
	public void setOnEditableChange(OnDropBoxEditableChange onDropBoxEditableChange) {
		this.onDropBoxEditableChange = onDropBoxEditableChange;
	}


}
