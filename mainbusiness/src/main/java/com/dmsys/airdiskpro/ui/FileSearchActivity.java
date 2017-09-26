package com.dmsys.airdiskpro.ui;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.dm.baselib.BaseValue;
import com.dm.xunlei.udisk.Network.View.EditTextButtonView;
import com.dmairdisk.aodplayer.api.AodPlayer;
import com.dmairdisk.aodplayer.dialog.MusicPlayerDialog;
import com.dmairdisk.aodplayer.impl.MediaPlayerImpl.MusicPlayerListener;
import com.dmairdisk.aodplayer.util.CommonAsync;
import com.dmsys.airdiskpro.event.DisconnectEvent;
import com.dmsys.airdiskpro.event.SearchEndEvent;
import com.dmsys.airdiskpro.filemanager.FileOperationHelper;
import com.dmsys.airdiskpro.utils.ConvertUtil;
import com.dmsys.airdiskpro.utils.DipPixelUtil;
import com.dmsys.airdiskpro.utils.FileInfoUtils;
import com.dmsys.airdiskpro.utils.FileUtil;
import com.dmsys.airdiskpro.view.ProgressDialog;
import com.dmsys.dmsdk.DMSdk;
import com.dmsys.dmsdk.model.DMFile;
import com.dmsys.dmsdk.model.DMFileCategoryType;
import com.dmsys.dmsdk.model.DMSearch;
import com.dmsys.dmsdk.model.DMSearch.OnSearchListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.dmsys.mainbusiness.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.dm.longsys.library.imageloader.core.DMImageLoader;
import cn.dm.longsys.library.imageloader.core.DisplayImageOptions;
import de.greenrobot.event.EventBus;

public class FileSearchActivity<WidgetSearchActivity> extends Activity implements OnClickListener, OnItemClickListener {

	private EditTextButtonView tv_search;
	private ProgressBar progress_search;
	private PullToRefreshListView mList;
	private LinearLayout emptyRl,layout_back,layout_search;
	private String mPath;
	private CommonAsync mTask;
	private List<DMFile> mFileList;
	private List<DMFile> mFolderList;
	private DisplayImageOptions mLoaderOptions;
	private DMImageLoader imageLoader = DMImageLoader.getInstance();
	private AodPlayer mAodPlayer;
	private MyFileAdaper mAdapter;
	private Handler mHandler;
	private boolean mCancelCache = false;
	/** 音乐 **/
	private ImageButton ibtn_music;
	private MusicPlayerDialog mMusicPlayerDialog;
	private long coookie_AodPlayer;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search);
		
		Intent intent = getIntent();
		mPath = intent.getStringExtra("path");
		
		initViews();
	}
	
	private void initViews() {
		// TODO Auto-generated method stub
		EventBus.getDefault().register(this);
		layout_back = (LinearLayout) findViewById(R.id.layout_back);
		layout_back.setOnClickListener(this);
		
		layout_search = (LinearLayout) findViewById(R.id.layout_search);
		layout_search.setOnClickListener(this);
		
		tv_search = (EditTextButtonView) findViewById(R.id.tv_search);
		tv_search.setEditTextHint(getString(R.string.DM_Search_Prompt));
		tv_search.setStyle(EditTextButtonView.EditTextStyle);
		tv_search.setKeyboardStyle(EditorInfo.IME_ACTION_SEARCH);
		tv_search.getEditTextView().setSingleLine();
		tv_search.setOnEditorActionListener(new OnEditorActionListener() {
			
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				// TODO Auto-generated method stub
				
				if (actionId == EditorInfo.IME_ACTION_SEARCH) {
					doSearch();
					return true;    
				}
				if (event != null && event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {                
					doSearch();
					return true;             
				}               
				return false;         
			}
		});
		
		progress_search = (ProgressBar) findViewById(R.id.progress_search);
		mList = (PullToRefreshListView) findViewById(R.id.list);
		emptyRl = (LinearLayout) findViewById(R.id.emptyRl);
		
		mFileList = new ArrayList<>();
		mFolderList = new ArrayList<>();
		mAodPlayer = AodPlayer.getInstance();
		initAudioPlayer();
		mAdapter = new MyFileAdaper(); 
		mList.setAdapter(mAdapter);
		mList.getRefreshableView().setOnItemClickListener(this);
		
		mHandler = new Handler(){
			
			@Override
			public void dispatchMessage(Message msg) {
				// TODO Auto-generated method stub
				super.dispatchMessage(msg);
				switch (msg.what) {
				case 0x1111:
					if (emptyRl.getVisibility() == View.VISIBLE) {
						emptyRl.setVisibility(View.GONE);
						mList.setVisibility(View.VISIBLE);
					}
					mAdapter.notifyDataSetChanged();
					break;
				default:
					break;
				}
			}
		};
		initImageLoader();
		attachAvodListener();
		ibtn_music = (ImageButton) findViewById(R.id.ibtn_music);
		ibtn_music.setOnClickListener(this);
		if (AodPlayer.getInstance().getIsPlaying()) {
			ibtn_music.setVisibility(View.VISIBLE);
		} else {
			ibtn_music.setVisibility(View.GONE);
		}
	}

	private void initAudioPlayer() {
		// TODO Auto-generated method stub
		Intent mIntent = new Intent(this, MainActivity.class);
		mAodPlayer.setIntent(mIntent);// 放在setmOnAodPlayerStatusListener之后
	}

	private void initImageLoader() {
		// TODO Auto-generated method stub
		mLoaderOptions = new DisplayImageOptions.Builder()
				  .cacheInMemory(true)
				  .showImageOnFail(R.drawable.filemanager_photo_fail)
				  .useThumb(true)
				  .cacheOnDisk(true)
				  .showImageOnLoading(R.drawable.bt_download_manager_image)
				  .showImageForEmptyUri(R.drawable.filemanager_photo_fail)
				  .build();
	}
	
	public void attachAvodListener(){
		
		coookie_AodPlayer = mAodPlayer.attachListener(new MusicPlayerListener() {
			
			@Override
			public void onProgressChanged(String filePath, int duration, int position) {
				// TODO Auto-generated method stub
			}
			
			@Override
			public void onPlayStateChanged(int state) {
				// TODO Auto-generated method stub
				System.out.println("mumu onPlayStateChanged:"+state);
				//停止播放，去掉播放标志
				if (state == 4) {
					mAdapter.notifyDataSetChanged();
				}
			}
			
			@Override
			public void onPlayFileChanged(String filePath) {
				// TODO Auto-generated method stub
				System.out.println("mumu onPlayFileChanged:"+filePath);
				mAdapter.notifyDataSetChanged();
			}
		});
	}
	
	public void removeAvodListener(){
		mAodPlayer.removeListener(coookie_AodPlayer);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}
	

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		int i = v.getId();
		if (i == R.id.layout_back) {
			finish();

		} else if (i == R.id.layout_search) {
			doSearch();

		} else if (i == R.id.ibtn_music) {
			showMusicDialog();

		} else {
		}
	}
	
	private void doSearch() {
		// TODO Auto-generated method stub
		System.out.println("ddddo search");
		final String searchText = tv_search.getContentText();
		if (searchText != null && !searchText.equals(" ")) {
			
			mFileList.clear();
			mFolderList.clear();
			progress_search.setVisibility(View.VISIBLE);
			
		    ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);  
			
			CommonAsync.Runnable runnable = new CommonAsync.Runnable() {
				
				@Override
				public void stop() {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public Object run() {
					// TODO Auto-generated method stub
					DMSearch search = new DMSearch(mPath, searchText, new OnSearchListener() {
						
						@Override
						public void onFileSearched(List<DMFile> files) {
							// TODO Auto-generated method stub
							//mFileList.addAll(files);
							
							for (DMFile dmFile : files) {
								if (dmFile.isDir) {
									mFolderList.add(dmFile);
								}else {
									mFileList.add(dmFile);
								}
							}
							
							mHandler.sendEmptyMessage(0x1111);
						}
					});
					System.out.println("DMSdk.getInstance().search");
					return DMSdk.getInstance().search(search);
				}
			};
			
			CommonAsync.CommonAsyncListener listener = new CommonAsync.CommonAsyncListener() {
				
				@Override
				public void onResult(Object ret) {
					// TODO Auto-generated method stub
					progress_search.setVisibility(View.GONE);
					
					if (mFileList.size() > 0) {
						mFolderList.addAll(mFileList);
						mAdapter.notifyDataSetChanged();
					}else if (mFileList.size() == 0 && mFolderList.size() == 0) {
						emptyRl.setVisibility(View.VISIBLE);
						((TextView)findViewById(R.id.emptyTextView)).setText(R.string.DM_Search_Result_No_Files);
						mList.setVisibility(View.GONE);
					}
			
				}
				
				@Override
				public void onError() {
					// TODO Auto-generated method stub
					progress_search.setVisibility(View.GONE);
				}
				
				@Override
				public void onDestory() {
					// TODO Auto-generated method stub
					
				}
			};
			if (mTask != null) {
				mTask.destory();
				mTask = null;
			}		
			mTask = new CommonAsync(runnable, listener);
			ExecutorService FULL_TASK_EXECUTOR = (ExecutorService) Executors.newCachedThreadPool();
			mTask.executeOnExecutor(FULL_TASK_EXECUTOR);
		}
	}
	
	
	public class MyFileAdaper extends BaseAdapter {
		private LayoutInflater layoutInflater;
		private int photoWidth;
		private int photoHeight;

		public MyFileAdaper() {
			layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			photoWidth = DipPixelUtil.dip2px(FileSearchActivity.this, 40);
			photoHeight = DipPixelUtil.dip2px(FileSearchActivity.this, 40);
		}

		@Override
		public int getCount() {
			if (mFolderList == null) {
				return 0;
			}
			
			return mFolderList.size();
		}

		@Override
		public Object getItem(int position) {
			return mFolderList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final ViewHolder holder;
			if (convertView == null) {
				convertView = layoutInflater.inflate(R.layout.search_file_item, null);
				holder = new ViewHolder();
				holder.mPlaying = (ImageView) convertView.findViewById(R.id.iv_playing);
				holder.mFileIcon = (ImageView) convertView.findViewById(R.id.iv_icon);
				holder.mFileName = (TextView) convertView.findViewById(R.id.tv_file_name);
				holder.mFilePath = (TextView) convertView.findViewById(R.id.tv_file_path);
				holder.mFileDate = (TextView) convertView.findViewById(R.id.tv_file_date);
				holder.mGoImage = (ImageView) convertView.findViewById(R.id.img_arrow);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			// 填充data
			final DMFile item = (DMFile) getItem(position);

			// 根据不同类型，设置图标
			DMFileCategoryType type = item.mType;
			
			// 返回上一层消失
			holder.mFileName.setVisibility(View.VISIBLE);
			holder.mFileDate.setVisibility(View.VISIBLE);
			holder.mFilePath.setVisibility(View.VISIBLE);
			if (type == DMFileCategoryType.E_PICTURE_CATEGORY) {
				// 这里显示图片
				updatePicIcons(holder.mFileIcon, item);
			} else {
				//取消之前的引用下载
				imageLoader.cancelDisplayTask(holder.mFileIcon);
				int iconId = FileUtil.getFileLogo(item);
				holder.mFileIcon.setImageResource(iconId);
			}
			
			boolean isPlaying = mAodPlayer.getIsPlaying();
			String playPath = AodPlayer.getInstance().getCurPlayPath();
			if (isPlaying && playPath!= null && playPath.equals(getFullPath(item))) {
				holder.mPlaying.setVisibility(View.VISIBLE);
			}else {
				holder.mPlaying.setVisibility(View.INVISIBLE);
			}
			
			String name = item.getName();
			holder.mFileName.setText(name);
			// 文件(夹)的最后修改时间
			holder.mFileDate.setText(item.getLastModified("yyyy-MM-dd"));
			holder.mFilePath.setText(item.mPath);

			if (item.mHidden) {
				holder.mFileIcon.setAlpha(50);
			}else {
				holder.mFileIcon.setAlpha(255);
			}
			
			/*
			 * 文件大小，如果是文件夹就不显示 *
			 */
			if (!item.isDir()) {
				holder.mGoImage.setVisibility(View.GONE);
				holder.mFileDate.setVisibility(View.VISIBLE);
				String fileSizeStr = ConvertUtil.convertFileSize(item.mSize, 2);
				holder.mFileDate.setText(fileSizeStr + " " + item.getLastModified("yyyy-MM-dd"));
			} else {
				// 是个目录
				holder.mGoImage.setVisibility(View.VISIBLE);
			}
			return convertView;
		}
		/*
		 * 使用第三方库 显示图片
		 */
		private void updatePicIcons(ImageView iconview, DMFile item) {
			String uri = getFullPath(item);
			
			if (item.mLocation == DMFile.LOCATION_UDISK) {
				uri = FileInfoUtils.encodeUri(getFullPath(item));
			}
			System.out.println("search updatePicIcons:"+uri);
			//直接显示图片
			imageLoader.displayImage(uri, item.mSize, iconview, mLoaderOptions, null);
		}
	}
	
	private String getFullPath(DMFile file){
		if (file.mLocation == DMFile.LOCATION_UDISK) {
			return "http://" + BaseValue.Host + File.separator +file.mPath;
		}else {
			return "file://" + file.mPath;
		}
		
	}
	
	public final class ViewHolder {
		public ImageView mPlaying;
		public ImageView mFileIcon;
		public TextView mFileName;
		public TextView mFilePath;
		public TextView mFileDate;
		public ImageView mGoImage;
	}

	public void onEventMainThread(DisconnectEvent event){
		finish();
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		EventBus.getDefault().unregister(this);
		
		removeAvodListener();
		
		closeMusicDialog();
		
		if (mTask != null) {
			mTask.destory();
			mTask = null;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		// TODO Auto-generated method stub
		DMFile item = mFolderList.get(position-1);
		if (!item.isDir()) {
			// 文件：打开
			openFile(item);
		} else {
			EventBus.getDefault().post(new SearchEndEvent(item.mPath));
			finish();
		}
	}
	
	// 打开文件，图片文件需要批量打开
	private void openFile(DMFile file) {
		if (file.mType == DMFileCategoryType.E_PICTURE_CATEGORY) {
			ArrayList<DMFile> fileList = new ArrayList<DMFile>();
			int index = -1;
			DMFile tmp = null;
			
			for (int i = 0; i < mFolderList.size(); i++) {
				tmp = mFolderList.get(i);
				if (tmp.mType == DMFileCategoryType.E_PICTURE_CATEGORY) {
					if (index == -1 && tmp.equals(file)) {
						index = fileList.size();
					}
					
					fileList.add(tmp);
				}
			}
			FileOperationHelper.getInstance().openPicture(this,fileList, index);
		} else if (file.mType == DMFileCategoryType.E_MUSIC_CATEGORY) {
			openMusic(file);
		}else if (file.mType == DMFileCategoryType.E_UNSUPPORT_VIDEO_CATEGORY) {
			FileOperationHelper.getInstance().openUnsupportVideo(file,this);
		}else {
			boolean openOK = FileOperationHelper.getInstance().openFile(file,this);
			System.out.println("openfile2 ok:"+openOK);
			if (!openOK) {
				final ProgressDialog dialog = new ProgressDialog(this);
				dialog.setProgress(0);
				dialog.setTitleContent(getString(R.string.DM_Task_Download));
				dialog.setMessage(getString(R.string.DM_Fileexplore_Loading_File));
				dialog.setLeftBtn(getString(R.string.DM_Control_Cancel), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						mCancelCache = true;
					}
				});
				
				doDownload(file, dialog);
				dialog.show();
			}
		}
	}
	
	public void openMusic(DMFile file){
		List<String> list = new ArrayList<>();
		list.add(getFullPath(file));
		mAodPlayer.setPlayList(list);
		mAodPlayer.startPlay(getFullPath(file));
		
		ibtn_music.setVisibility(View.VISIBLE);
		showMusicDialog();
	}
		
	private double mProgress;
	private void doDownload(final DMFile file, final ProgressDialog dialog) {
		
		File directory = new File(FileOperationHelper.getInstance().getCachePath());  
		if (!directory.exists()) {
			directory.mkdir();
		}
		
		
		final File dstFile = new File(FileOperationHelper.getInstance().getCachePath(), file.getName());
		if (dstFile.exists()) {
			
			mHandler.post(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					dialog.setProgress(100);
					dialog.dismiss();
				}
				
			});
			
			DMFile dstXLFile = new DMFile();
			dstXLFile.mName = dstFile.getName();
			dstXLFile.mPath = dstFile.getPath();
			dstXLFile.mLocation = DMFile.LOCATION_LOCAL;
			FileUtil.thirdPartOpen(dstXLFile,FileSearchActivity.this);
			return;
		}
		
		FileOperationHelper.getInstance().doDownload(file, dstFile.getParent(), new FileOperationHelper.ProgressListener() {
			
			@Override
			public boolean onProgressChange(final double progress) {
				// TODO Auto-generated method stub
				System.out.println("dirdir:"+progress);
				if (progress - mProgress >= 5 || progress == 100) {
					System.out.println("dirdir111:");
					mHandler.post(new Runnable() {

						@Override
						public void run() {
							// TODO Auto-generated method stub
							System.out.println("dirdir222");
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
							
							DMFile dstXLFile = new DMFile();
							dstXLFile.mName = dstFile.getName();
							dstXLFile.mPath = dstFile.getPath();
							dstXLFile.mLocation = DMFile.LOCATION_LOCAL;
							FileUtil.thirdPartOpen(dstXLFile,FileSearchActivity.this);
							
						} else {
							Toast.makeText(FileSearchActivity.this, FileSearchActivity.this.getString(R.string.DM_Disk_Buffer_Fail), Toast.LENGTH_LONG).show();
						}
					}
				});
				
				return 0;
			}
		});
	}
	
	private void showMusicDialog() {

		System.out.println("showMusicDialog main");

		if (mMusicPlayerDialog == null) {
			mMusicPlayerDialog = new MusicPlayerDialog(this);
		}
		mMusicPlayerDialog.setOnDismissListener(new OnDismissListener() {
			
			@Override
			public void onDismiss(DialogInterface dialog) {
				// TODO Auto-generated method stub
				if (!AodPlayer.getInstance().getIsPlaying()) {
					ibtn_music.setVisibility(View.GONE);
				}
			}
		});
		mMusicPlayerDialog.show();
	}

	private void closeMusicDialog() {
		if (mMusicPlayerDialog != null && mMusicPlayerDialog.isShowing()) {
			mMusicPlayerDialog.cancel();
		}
		mMusicPlayerDialog = null;
	}
	
	
}
