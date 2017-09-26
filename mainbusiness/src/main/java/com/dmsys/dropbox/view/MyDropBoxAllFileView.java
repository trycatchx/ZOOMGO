package com.dmsys.dropbox.view;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.Toast;

import com.dmsys.airdiskpro.filemanager.FileOperationHelper;
import com.dmsys.airdiskpro.ui.imagereader.ImagePagerActivity;
import com.dmsys.airdiskpro.utils.FileUtil;
import com.dmsys.airdiskpro.view.ProgressDialog;
import com.dmsys.dmsdk.model.DMFile;
import com.dmsys.dmsdk.model.DMFileCategoryType;
import com.dmsys.dropbox.api.DMDropboxAPI;
import com.dmsys.dropbox.utils.DropBoxFileOperationHelper;
import com.dropbox.client2.exception.DropboxException;
import com.dmsys.mainbusiness.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.dm.longsys.library.imageloader.core.DisplayImageOptions;

public class MyDropBoxAllFileView extends BaseDirView {

	DMDropboxAPI mApi;
	Activity mContext;


	public MyDropBoxAllFileView(Context context) {
		super(context);
		mContext = (Activity)context;
		initView();
	}

	public MyDropBoxAllFileView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		mContext = (Activity)context;
		initView();
	}

	public MyDropBoxAllFileView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = (Activity)context;
		initView();
	}

	private void initView() {
		mApi = DMDropboxAPI.getInstance();
		initImageLoader();
		
	}
	


	private void initImageLoader() {
		// 获取DropBox 的头部信息，有认证的token 不然访问不到文件
		mApi.getSession().sign(headers);
		mLoaderOptions = new DisplayImageOptions.Builder().cacheInMemory(true)
				.showImageOnFail(R.drawable.filemanager_photo_fail)
				.useThumb(true).cacheOnDisk(true)
				.showImageOnLoading(R.drawable.bt_download_manager_image)
				.showImageForEmptyUri(R.drawable.filemanager_photo_fail)
				.extraForDownloader(headers).build();
	}

	@Override
	public List<DMFile> listFile(int curFileType, String curPath)
			throws Exception {
		// TODO Auto-generated method stub
		List<DMFile> list = new ArrayList<>();

		LoadResult result = new LoadResult();
		result.path = curPath;

		if (curFileType == FILE_TYPE_AIRDISK
				|| curFileType == FILE_TYPE_PATHSELECT) {
			try {
				list = mApi.listFileDropBox(curPath, -1, null, true, null);
			} catch (DropboxException e) {
				e.printStackTrace();
			}

		}
		if (list != null && list.size() > 1) {
			FileUtil.sortFileListByName(list);
		}
		return list;
	}

	@Override
	public String getFullPath(DMFile file) {
		// TODO Auto-generated method stub
		String ret = null;
		try {
			ret = mApi.getRequestUrlUnencode(file.getPath(), null);
		} catch (DropboxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ret;
	}

	private double mProgress;

	@Override
	public void doDownload(final DMFile file, final ProgressDialog dialog) {
		// TODO Auto-generated method stub
		File directory = new File(FileOperationHelper.getInstance()
				.getCachePath());
		if (!directory.exists()) {
			directory.mkdir();
		}

		final File dstFile = new File(FileOperationHelper.getInstance()
				.getCachePath(), file.getName());
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
			
			if (file.getType() == DMFileCategoryType.E_BOOK_CATEGORY) {
				DropBoxFileOperationHelper.getInstance().openFile(dstXLFile, mContext);
			} else {
				FileUtil.thirdPartOpen(dstXLFile, mContext);
			}
			return;
		}

		DropBoxFileOperationHelper.getInstance().doDownload(file,
				dstFile.getParent(),
				new DropBoxFileOperationHelper.ProgressListener() {

					@Override
					public boolean onProgressChange(final double progress) {
						// TODO Auto-generated method stub
						System.out.println("dirdir:" + progress);
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
									
									if (file.getType() == DMFileCategoryType.E_BOOK_CATEGORY) {
										DropBoxFileOperationHelper.getInstance().openFile(dstXLFile, mContext);
									} else {
										FileUtil.thirdPartOpen(dstXLFile, mContext);
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

	@Override
	public boolean openFile(DMFile file, Activity context) {
		// TODO Auto-generated method stub
		return DropBoxFileOperationHelper.getInstance().openFile(
				file, context);
	}

	@Override
	public void openPicture(Context context, ArrayList<DMFile> fileList,
			int index) {
		// TODO Auto-generated method stub
		FileOperationHelper.getInstance().openPicture(mContext, fileList,
				index, ImagePagerActivity.IS_FROM_DropBox);
	}
	
	public void openMusic(final DMFile file) {

		initAudioPlayer();

		List<String> list = getCurrentMusicFiles();

		mAodPlayer.setPlayListAndHeaders(list, headers);
		mAodPlayer.startPlay(getFullPath(file));
		if (onDropBoxMusicChangeListener != null) {
			onDropBoxMusicChangeListener.onMusicChange(-1, true);
		}
	}

}
