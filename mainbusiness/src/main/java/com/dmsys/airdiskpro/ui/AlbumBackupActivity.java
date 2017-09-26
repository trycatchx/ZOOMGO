package com.dmsys.airdiskpro.ui;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dm.baselib.BaseValue;
import com.dmairdisk.aodplayer.util.CommonAsync;
import com.dmsoftwareupgrade.util.UDiskBaseDialog;
import com.dmsys.airdiskpro.adapter.AlbumBackupAdapter;
import com.dmsys.airdiskpro.db.BackupSettingDB;
import com.dmsys.airdiskpro.event.BackupRefreshEvent;
import com.dmsys.airdiskpro.event.BackupStateEvent;
import com.dmsys.airdiskpro.event.DisconnectEvent;
import com.dmsys.airdiskpro.model.BackupDMFile;
import com.dmsys.airdiskpro.model.BakSetBean;
import com.dmsys.airdiskpro.model.MediaFolder;
import com.dmsys.airdiskpro.model.MediaInfo;
import com.dmsys.airdiskpro.service.BackupService;
import com.dmsys.airdiskpro.service.BackupService.BuckupType;
import com.dmsys.airdiskpro.utils.ConvertUtil;
import com.dmsys.airdiskpro.utils.GetBakLocationTools;
import com.dmsys.airdiskpro.utils.SystemDBTool;
import com.dmsys.airdiskpro.view.BackupProgressDialog;
import com.dmsys.airdiskpro.view.MessageDialog;
import com.dmsys.dmsdk.DMSdk;
import com.dmsys.dmsdk.model.DMFileCategoryType;
import com.dmsys.dmsdk.model.DMStorageInfo;
import com.dmsys.mainbusiness.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.dm.longsys.library.imageloader.core.DMImageLoader;
import cn.dm.longsys.library.imageloader.core.DisplayImageOptions;
import de.greenrobot.event.EventBus;


public class AlbumBackupActivity extends Activity implements OnItemClickListener,View.OnClickListener {
	
	private GridView grid;
	private RelativeLayout layout_grid;
	private LinearLayout loading,llyt_backup_pictrue,llyt_backup_video,rlyt_date_pic_upload_to;
	private CommonAsync mCommonAsync;
	private ExecutorService FULL_TASK_EXECUTOR = (ExecutorService) Executors.newCachedThreadPool();
	private Activity mContext;
	private ArrayList<MediaFolder> mGroupDatas = new ArrayList<MediaFolder>();
	private int imageRLWIdth;
	private AlbumBackupAdapter adapter;
	private ImageView titlebar_left,iv_pictrue_backup_choose,iv_video_backup_choose;
	private TextView titlebar_title;
	private Button btn_date_pic_upload;
	private BackupProgressDialog mBackupProgressDialog;
	private DMImageLoader mDMImageLoader;
	private DisplayImageOptions mLoaderOptions;
	
	private int mType;  // 0:manual, 1:auto
	private SharedPreferences preference;
	private List<String> mFolders;
	
	public static String PREFERENE_NAME = "ALBUM_BACKUP";
	public static String KEY_IMAGE = "bakImage";
	public static String KEY_VIDEO = "bakVideo";
	public static String KEY_FOLDERS = "bakFolder";
	
	private Handler mHandler;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_album_backup);
		mContext = this;
		
		Intent intent = getIntent();
		mType = intent.getIntExtra("BACKUP_TYPE", 0);
		
		imageRLWIdth = getImageRLWidth(getWindowWidth());
		initViews();
		
		mDMImageLoader = DMImageLoader.getInstance();
		initLoaderOptions();
		
		mHandler = new Handler();
	}
	
	private void initViews() {
		
		EventBus.getDefault().register(this);
		
		grid = (GridView)findViewById(R.id.grid);
		loading = (LinearLayout) findViewById(R.id.loading);
		adapter = new AlbumBackupAdapter(mContext, mGroupDatas, imageRLWIdth);
		grid.setAdapter(adapter);
		grid.setOnItemClickListener(this);
		
		FrameLayout back = (FrameLayout) findViewById(R.id.layout_back);
		titlebar_left = (ImageView) findViewById(R.id.titlebar_left);
		titlebar_left.setImageResource(R.drawable.dm_lib_wifi_back_btn_bg_selector);
		back.setOnClickListener(this);
		
		llyt_backup_video = (LinearLayout) findViewById(R.id.llyt_backup_video);
		llyt_backup_pictrue = (LinearLayout) findViewById(R.id.llyt_backup_pictrue);
		llyt_backup_video.setOnClickListener(this);
		llyt_backup_pictrue.setOnClickListener(this);
		
		layout_grid = (RelativeLayout) findViewById(R.id.layout_grid);
		
		rlyt_date_pic_upload_to = (LinearLayout) findViewById(R.id.rlyt_date_pic_upload_to);
		rlyt_date_pic_upload_to.setVisibility(View.GONE);
		
		btn_date_pic_upload = (Button)findViewById(R.id.btn_date_pic_upload);
		if (mType == 0) {
			btn_date_pic_upload.setText(R.string.DM_Backup_Start_Button);
			btn_date_pic_upload.setEnabled(false);
		}else if (mType == 1) {
			btn_date_pic_upload.setText(R.string.DM_Control_Definite);
		}
		btn_date_pic_upload.setOnClickListener(this);
		
		iv_pictrue_backup_choose = (ImageView)findViewById(R.id.iv_pictrue_backup_choose);
		iv_video_backup_choose = (ImageView)findViewById(R.id.iv_video_backup_choose);
		
		if (mType == 0) {
			
			iv_pictrue_backup_choose.setSelected(true);
			iv_video_backup_choose.setSelected(false);
			
		}else if(mType == 1){
			
			preference = getSharedPreferences(PREFERENE_NAME, MODE_PRIVATE);
			if (preference.getBoolean(KEY_IMAGE, true)) {
				iv_pictrue_backup_choose.setSelected(true);
			}else {
				iv_pictrue_backup_choose.setSelected(false);
			}
			
			if (preference.getBoolean(KEY_VIDEO, false)) {
				iv_video_backup_choose.setSelected(true);
			}else {
				iv_video_backup_choose.setSelected(false);
			}
			
			
			//mFolders = getBacke
			Set<String> set = preference.getStringSet(KEY_FOLDERS, null);
			if (set == null) {
				mFolders = new ArrayList<>();
				
				btn_date_pic_upload.setEnabled(false);
				rlyt_date_pic_upload_to.setVisibility(View.GONE);
				btn_date_pic_upload.setTextColor(Color.BLACK);
				
			}else{
				
				Iterator<String> it = set.iterator();
				while (it.hasNext())
				{
				    System.out.println("aa 1:" + it.next());
				}
				
				mFolders = new ArrayList<>(set);
				
				System.out.println("aa 2: "+mFolders.size());
				
				if (mFolders.size() > 0) {
					btn_date_pic_upload.setEnabled(true);
					rlyt_date_pic_upload_to.setVisibility(View.VISIBLE);
					btn_date_pic_upload.setTextColor(Color.WHITE);
				}else {
					btn_date_pic_upload.setEnabled(false);
					rlyt_date_pic_upload_to.setVisibility(View.GONE);
					btn_date_pic_upload.setTextColor(Color.BLACK);
				}
				
				
			}
			
		}
		
		
		titlebar_title = (TextView) findViewById(R.id.titlebar_title);
		titlebar_title.setText(getString(R.string.DM_Backup_Album_Title));
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
	
	
	/*
	 * 更新上传的下一张图片的缩略图
	 */
	private void updateMediaImageUI(Message msg) {
			//跟新剩下多少张
			Bundle bundle = msg.getData();
			String leftFormat = getResources().getString(
					R.string.DM_Remind_Backup_Bak_Left);
			String leftFinalStr = String.format(leftFormat,
					String.valueOf(bundle.getLong(BackupService.KEY_TOTAL_LEFT)));
			mBackupProgressDialog.setMessage(leftFinalStr);
			//置零
			mBackupProgressDialog.setProgress(0);
			//更新图片
			String filePath = bundle.getString(BackupService.KEY_PATH);
			mBackupProgressDialog.setImages(mDMImageLoader, filePath, mLoaderOptions);
	}
	
	
	
	 

	
	
	public int getImageRLWidth(int screenWith) {
		return (screenWith - dip2px(this, 38)) / 2;
	}
	private int getWindowWidth() {
		DisplayMetrics dpy = new DisplayMetrics();
		this.getWindow().getWindowManager().getDefaultDisplay().getMetrics(dpy);
		return dpy.widthPixels;
	}
	 public int dip2px(Context context, float dpValue) {
	        final float scale = context.getResources().getDisplayMetrics().density;
	        return (int) (dpValue * scale + 0.5f);
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
						layout_grid.setVisibility(View.GONE);
					}
				});
		
				return SystemDBTool.getPhoneMFiles(mContext);
			
			}
		};
		CommonAsync.CommonAsyncListener mCommonAsyncListener = new CommonAsync.CommonAsyncListener() {
			
			@Override
			public void onResult(Object ret) {
				// TODO Auto-generated method stub
				loading.setVisibility(View.GONE);
				layout_grid.setVisibility(View.VISIBLE);
				if(ret != null) {
					ArrayList<MediaInfo> tmp = (ArrayList<MediaInfo>)ret;
					ArrayList<MediaFolder> tmp1 = null;
					try {
						tmp1 = formatList(tmp);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if(tmp1 != null) {
						mGroupDatas.clear();
						mGroupDatas.addAll(tmp1);	
						adapter.notifyDataSetChanged();
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
		if(mCommonAsync != null) {
			mCommonAsync.destory();
		}
		mCommonAsync = new CommonAsync(mRunnable, mCommonAsyncListener);
		mCommonAsync.executeOnExecutor(FULL_TASK_EXECUTOR);
	}

	

	private ArrayList<MediaFolder> formatList(ArrayList<MediaInfo> fileList) throws IOException {
		
		//将fileList存到ArrayList<MediaFolder>的结构中
		ArrayList<MediaFolder> res = new ArrayList<MediaFolder>();
		for (int i = 0; i < fileList.size(); i++) {
			MediaFolder folder = findItemUseHash(res, fileList.get(i).getParentID());
			if (folder == null) {
				ArrayList<MediaInfo> pathList = new ArrayList<MediaInfo>();
				MediaInfo media = fileList.get(i);
				pathList.add(media);
				
				MediaFolder ff = new MediaFolder(pathList, fileList.get(i).getParentName(), false, fileList.get(i).getParentID(),fileList.get(i).getParent());
				
				if (mType == 1) {
					
					for (String folderPath : mFolders) {
						if (ff.getFolderPath().equals(folderPath)) {
							ff.selected = true;
						}
					}
					
				}
				
				res.add(ff);
			} else {
				MediaInfo media = fileList.get(i);
				folder.getMediaInfoList().add(media);
			}
		}
		return res;
	}
	
	
	private MediaFolder findItemUseHash(ArrayList<MediaFolder> folderList, long hash) {
		for (int i = 0; i < folderList.size(); i++) {
			if (folderList.get(i).getParentHash() == hash ) {
				return folderList.get(i);
			} else {
				continue;
			}
		}
		return null;
	}
	
	public void onEventMainThread(DisconnectEvent event){
		finish();
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		
		EventBus.getDefault().unregister(this);
		
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View view, int position, long id) {
		// TODO Auto-generated method stub
		
		if (BaseValue.backing_album || BaseValue.backing_contacts) {
			Toast.makeText(AlbumBackupActivity.this, R.string.DM_Remind_Operate_Backingup, Toast.LENGTH_SHORT).show();
		}else {
			boolean selected = !mGroupDatas.get(position).selected;
			mGroupDatas.get(position).selected = selected; // 反选
			LinearLayout iv = (LinearLayout)view.findViewById(R.id.backup_choose_icon);
			iv.setSelected(selected);
			int SelectedNum = 0;
			for (int i = 0; i < mGroupDatas.size(); i++) {
				if (mGroupDatas.get(i).selected) {
					SelectedNum++;
				}
			}
			
			if (SelectedNum > 0) {
				btn_date_pic_upload.setEnabled(true);
				rlyt_date_pic_upload_to.setVisibility(View.VISIBLE);
				btn_date_pic_upload.setTextColor(Color.WHITE);
			}else {
				btn_date_pic_upload.setEnabled(false);
				rlyt_date_pic_upload_to.setVisibility(View.GONE);
				btn_date_pic_upload.setTextColor(Color.BLACK);
			}
		}
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		int i = v.getId();
		if (i == R.id.llyt_backup_pictrue) {
			boolean setSelected = !iv_pictrue_backup_choose.isSelected();
			iv_pictrue_backup_choose.setSelected(setSelected);

			if (mType == 1) {
				preference.edit().putBoolean(KEY_IMAGE, setSelected).commit();
			}


		} else if (i == R.id.llyt_backup_video) {
			boolean setSelected1 = !iv_video_backup_choose.isSelected();
			iv_video_backup_choose.setSelected(setSelected1);

			if (mType == 1) {
				preference.edit().putBoolean(KEY_VIDEO, setSelected1).commit();
			}


		} else if (i == R.id.layout_back) {
			finish();

		} else if (i == R.id.btn_date_pic_upload) {
			if (mType == 0) {
				if (!checkSelectedStatus()) return;
				showUploadDialog();
				BackupService.selectedBackupFilesList = getSelectedData();

				try {
					checkBackUpDbAndStartBackup();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {

				if (!iv_pictrue_backup_choose.isSelected() && !iv_video_backup_choose.isSelected()) {
					Toast.makeText(mContext, getString(R.string.DM_Disk_choose_backup_type), Toast.LENGTH_SHORT).show();
					return;
				}

				if (getSelectedData() != null) {

					System.out.println("aa:" + mFolders.size());

					preference.edit().putStringSet(KEY_FOLDERS, new HashSet<>(mFolders)).commit();

					//Intent intent = new Intent();
					//Bundle data = new Bundle();
					//data.putSerializable("CHOOSEN", getSelectedData());
					//intent.putExtras(data);
					//AutoBackupActivity.mFiles = getSelectedData();
					//setResult(RESULT_OK, intent);
				}

				finish();
			}


		} else {
		}
	}
	
	private void resetSelectesStatus(){
		if (mGroupDatas != null || mGroupDatas.size() > 0) {
			for (int i = 0; i < mGroupDatas.size(); i++) {
				if(mGroupDatas.get(i).selected) {
					mGroupDatas.get(i).setSelected(false);
				}
			}
			adapter.notifyDataSetChanged();
		}
	}
	
	private boolean checkSelectedStatus() {
		if(!iv_pictrue_backup_choose.isSelected() && !iv_video_backup_choose.isSelected()) {
			Toast.makeText(mContext, getString(R.string.DM_Disk_choose_backup_type), Toast.LENGTH_SHORT).show();
			return false;
		}
		boolean ret = false;
		if (mGroupDatas != null || mGroupDatas.size() > 0) {
			for (int i = 0; i < mGroupDatas.size(); i++) {
				if(mGroupDatas.get(i).selected) {
					ret = true;
					break;
				}
			}
		}
		if(!ret) {
			Toast.makeText(mContext, getString(R.string.DM_Backup_Select_Album_NoSelect), Toast.LENGTH_SHORT).show();
		}
		return ret;
		
	}
	/**
	 * 开启对话上传的dialog 进行进度回调
	 */
	private void showUploadDialog() {
		if(mBackupProgressDialog != null) {
			mBackupProgressDialog.dismiss();
		}
		mBackupProgressDialog = new BackupProgressDialog(mContext);
		mBackupProgressDialog.setTitleContent(getString(R.string.DM_Remind_Operate_Backingup));
		mBackupProgressDialog.setMessage(getString(R.string.DM_Disk_backup_filter_file));
		mBackupProgressDialog.setLeftBtn(getString(R.string.DM_Control_Cancel), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				EventBus.getDefault().post(new BackupStateEvent(BackupStateEvent.CANCLE));
			}
		});
		
		mBackupProgressDialog.show();
	}
		
	private ArrayList<BackupDMFile> getSelectedData() {
		
		if (mType == 1) {
			mFolders.clear();
		}
		
        Map<DMFileCategoryType, Boolean> filterPerms = new HashMap<DMFileCategoryType, Boolean>();
        filterPerms.put(DMFileCategoryType.E_PICTURE_CATEGORY, iv_pictrue_backup_choose.isSelected());
        filterPerms.put(DMFileCategoryType.E_VIDEO_CATEGORY, iv_video_backup_choose.isSelected());
        filterPerms.put(DMFileCategoryType.E_UNSUPPORT_VIDEO_CATEGORY, iv_video_backup_choose.isSelected());
		
        ArrayList<BackupDMFile> mBackupDMFileList = new ArrayList<BackupDMFile>();
		if (mGroupDatas != null || mGroupDatas.size() > 0) {
			for (int i = 0; i < mGroupDatas.size(); i++) {
				MediaFolder m = mGroupDatas.get(i);
				if (m != null && m.isSelected()) {
					
					if (mType == 1) {
						String dirPath = m.getFolderPath();
						mFolders.add(dirPath);
					}
					
					for (int j = 0; j < m.mediaList.size(); j++) {
						DMFileCategoryType type = m.mediaList.get(j).getType();
						try {
							if(filterPerms.get(type)) {
								mBackupDMFileList.add(new BackupDMFile(m.mediaList.get(j)));
							}
						} catch (Exception e) {
							// TODO: handle exception
						}
						
					}
				}
			}
		}

		return mBackupDMFileList;
	}
		
		
		private void checkBackUpDbAndStartBackup() throws InterruptedException {
			if (BackupSettingDB.getInstance().existDiskMac(BackupService.tmpMac)) {
				BakSetBean bean = BackupSettingDB.getInstance().getDiskBakSetting(BackupService.tmpMac);
				bean.bakImage = iv_pictrue_backup_choose.isSelected()?BakSetBean.TRUE:BakSetBean.FALSE;
				bean.bakVideo = iv_video_backup_choose.isSelected()?BakSetBean.TRUE:BakSetBean.FALSE;
				BackupSettingDB.getInstance().updateDiskMac(bean);
			} else {
				DMStorageInfo info = DMSdk.getInstance().getStorageInfo();
				String storageName = null;
				long storageByte = -1;
				if(info != null && info.getStorages() != null && info.getStorages().size() > 0) {
					storageName = info.getStorages().get(0).getName();
					storageByte = info.getStorages().get(0).total;
				} else {
					Message msg = new Message();
					msg.what = BackupService.MSG_BACKUP_COMPLETE;
					Bundle bundle = new Bundle();
					bundle.putInt(BackupService.ERROR_CODE, BackupService.ERROR_BACKUP_NO_STORAGE);
					bundle.putBoolean(BackupService.RESULT_BACKUP, false);
					msg.setData(bundle);
					mHandler.sendMessage(msg);
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
			
			Intent mIntent = new Intent(mContext,BackupService.class);
			mIntent.putExtra("backUp_Type", BuckupType.BUTYPE_MEDIA.ordinal());
			mContext.startService(mIntent);
		}
		
		public void onEventMainThread(BackupRefreshEvent event){
			
			if (event.type == 0 && mType == 0) {
				dealMediaEvent(event.message);
			}
			
		}

		private void dealMediaEvent(Message msg) {
			// TODO Auto-generated method stub
			if(mBackupProgressDialog == null) return;
			
			if (msg.what == BackupService.MSG_BACKUP_PROGRESS) {
				Bundle bundle = msg.getData();
				long tmpProgress = bundle.getLong(BackupService.KEY_PRO);
				long max = bundle.getLong(BackupService.KEY_MAX);
				int progress = (int)((tmpProgress *100)/max);
				mBackupProgressDialog.setProgress(progress);
				
			} else if(msg.what == BackupService.MSG_BACKUP_FILE_CHANGED) {
				updateMediaImageUI(msg);
				
			} else if(msg.what == BackupService.MSG_BACKUP_COMPLETE) {
				if(mBackupProgressDialog != null) {
					mBackupProgressDialog.dismiss();
					mBackupProgressDialog = null;
				}
				//有数据说明失败了，提示对应的错误信息
				Bundle bundle = msg.getData();
				if(bundle != null) {
					resetSelectesStatus();
					boolean ret = bundle.getBoolean(BackupService.RESULT_BACKUP,false);
					if(ret) {
						if (BaseValue.bigFiles.size() > 0) {
							showAlertDialog(1);
						}else {
							Toast.makeText(mContext, getString(R.string.DM_Disk_backup_success_Picture), Toast.LENGTH_LONG).show();	
						}
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
							Toast.makeText(mContext, getString(R.string.DM_Remind_Operate_Stop), Toast.LENGTH_LONG).show();	
							break;
							//已经备好了
						case BackupService.CODE_BACKEDUP_FILE:
							Toast.makeText(mContext, getString(R.string.DM_Disk_the_selected_file_has_been_backup), Toast.LENGTH_LONG).show();	
							break;
						case BackupService.CODE_BACKUP_IS_USER_STOP:
							Toast.makeText(mContext, getString(R.string.DM_Remind_Operate_Stop), Toast.LENGTH_LONG).show();	
							break;
						case BackupService.CODE_BACKEDUP__NO_ENOUGH_SPACE:
							String str = String.format(getString(R.string.DM_Fileexplore_Operation_Warn_Airdisk_No_Space), ConvertUtil.convertFileSize(BaseValue.taskTotalSize, 2),ConvertUtil.convertFileSize(BaseValue.diskFreeSize,2));
							Toast.makeText(mContext, str, Toast.LENGTH_LONG).show();	
							showAlertDialog(2);
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
						Toast.makeText(mContext, getString(R.string.DM_Disk_backup_success_Picture), Toast.LENGTH_LONG).show();	
					}
				}
			});
			builder.show();
			
		}
		

}
