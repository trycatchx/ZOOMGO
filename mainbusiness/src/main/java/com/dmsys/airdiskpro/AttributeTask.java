package com.dmsys.airdiskpro;

import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

import com.dmsys.airdiskpro.filemanager.FileOperationHelper;
import com.dmsys.airdiskpro.utils.FileInfoUtils;
import com.dmsys.airdiskpro.utils.FileUtil;
import com.dmsys.airdiskpro.view.UDiskAttributeDialog;
import com.dmsys.airdiskpro.view.UDiskBaseDialog;
import com.dmsys.dmsdk.DMSdk;
import com.dmsys.dmsdk.model.DMDir;
import com.dmsys.dmsdk.model.DMFile;
import com.dmsys.dmsdk.model.DMFileCategoryType;
import com.dmsys.dmsdk.model.DMFilePage;
import com.dmsys.mainbusiness.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AttributeTask extends AsyncTask<String, String, Boolean>implements DialogInterface.OnClickListener {
	private static final String TAG = "AttributeTask";
	Context mContext;

	int showType;
	private static final int SHOWTYPEISFILE = 0;
	private static final int SHOWTYPEISDIR = 1;
	private static final int SHOWTYPEISLIST = 2;
	String msg;
	String fileName;
	String filePath;
	String absolutePath;
	String sfileBit;
	String sfileDate = "";
	long fileDate = 0;
	long fileSize = 0;
	int fileNum = 0;
	int folderNum = 0;

	String sSize;

	DMFile mfiledata;
	List<DMFile> mlfd;
	boolean taskFlag;
	
	boolean typeFolder;

	public AttributeTask(Context mContext) {
		this.mContext = mContext;
		taskFlag = true;
	}

	public AttributeTask(Context mContext, DMFile mfiledata) {
		this(mContext);
		this.mfiledata = mfiledata;
	}
	
	public AttributeTask(Context mContext, boolean typeFolder ,DMFile mfiledata) {
		this(mContext);
		this.typeFolder = typeFolder;
		this.mfiledata = mfiledata;
	}

	public AttributeTask(Context mContext, List<DMFile> mlfd) {
		this(mContext);
		this.mlfd = new ArrayList<>(mlfd);
	}

	@Override
	protected void onPreExecute() {
		// TODO Auto-generated method stub
		// Log.i(TAG, "onPreExecute");

		// 判断当前显示的方式，多文件显示还是目录信息还是文件信息
		if (mlfd != null) {
			showType = SHOWTYPEISLIST;
			// 初始化基本信息
			File tfile = new File(mlfd.get(0).getPath());
			absolutePath = tfile.getParent();

			fileNum = 0;
			folderNum = 0;
			fileSize = 0;

		} else if (mfiledata != null) {
			filePath = mfiledata.getPath();
			fileName = mfiledata.getName();
			File tfile = new File(mfiledata.getPath());

			// try {
			absolutePath = tfile.getParent();
			sfileDate = mfiledata.getLastModified("yyyy-MM-dd HH:mm:ss");
			fileDate = mfiledata.mLastModify;

			fileSize = 0;
			fileNum = 0;
			folderNum = 0;
			fileSize = 0;

			if (mfiledata.isDir()) {
				showType = SHOWTYPEISDIR;
				// 初始化基本信息
			} else {
				showType = SHOWTYPEISFILE;
				// 初始化基本信息
				fileSize = mfiledata.mSize;
				sSize = FileInfoUtils.getLegibilityFileSize(fileSize);
				sfileBit = FileInfoUtils.changeFileSizeShow(fileSize);
			}
		}

		showAlertDislog();
		super.onPreExecute();
	}

	@Override
	protected void onPostExecute(Boolean result) {
		// TODO Auto-generated method stub
		// Log.i(TAG, "onPostExecute result = " + result);
		// updateMsg();

		taskFlag = false;

		super.onPostExecute(result);
	}

	@Override
	protected void onProgressUpdate(String... values) {
		// TODO Auto-generated method stub
		// Log.i(TAG, "onProgressUpdate values = " + values);

		updateMsg();

		super.onProgressUpdate(values);
	}

	@Override
	protected void onCancelled() {
		// TODO Auto-generated method stub
		// Log.i(TAG, "onCancelled");

		super.onCancelled();
	}

	@Override
	protected Boolean doInBackground(String... params) {
		// TODO Auto-generated method stub
		// Log.i(TAG, "doInBackground params = " + params);

		if (showType == SHOWTYPEISDIR) {
			return showAttributeDialog(mfiledata);
		} else if (showType == SHOWTYPEISLIST) {
			return showAttributeDialog(mlfd);
		}

		return null;
	}

	public boolean showAttributeDialog(List<DMFile> files) {

		for (DMFile file : files) {
			if (taskFlag == false)
				return false;

			if (file.mLocation == DMFile.LOCATION_UDISK) {
				if (file.isDir) {
					WebDirAttribute(file);
				} else {
					fileSize = fileSize + file.getSize();
					fileNum = fileNum + 1;
				}
			} else {
				// 这里是否需要处理文件夹的大小
				getFileLocalInfo(new File(file.getPath()));
			}
			file = null;
		}

		ProgressUpdate(0);
		return true;

	}

	/**
	 * 显示单个文件夹或者文件的属性
	 */
	public boolean showAttributeDialog(DMFile filedata) {


		if (filedata.isDir()) {
			if (filedata.mLocation == DMFile.LOCATION_UDISK) {

				switch (((DMDir) filedata).mDirType) {

				case PICTRUE:
					WebCurLevelDirAttribute(filedata, DMFileCategoryType.E_PICTURE_CATEGORY);
					break;
				default:
					WebDirAttribute(filedata);
					break;
				}
			} else {

				switch (((DMDir) filedata).mDirType) {
				case PICTRUE:
					getCurLevelFileLocalInfo(new File(filedata.getPath()), DMFileCategoryType.E_PICTURE_CATEGORY);
					break;
				default:
					getFileLocalInfo(new File(filedata.getPath()));
					break;
				}
			}

		} else {
			if (filedata.mLocation == DMFile.LOCATION_UDISK) {
				WebFileAttribute(filedata);
			} else {
				getFileLocalInfo(new File(filedata.getPath()));
			}

		}

		ProgressUpdate(0);
		return true;
	}

	private boolean getFileLocalInfo(File mFile) {
		if ((mFile == null) || (taskFlag == false)) {
			return false;
		}

		if (!mFile.exists())
			return false;

		if (mFile.isDirectory()) {// 文件夹
			folderNum += 1;
			List<DMFile> arrfile = FileOperationHelper.getInstance().listLocalFolderAllFiles(mFile.getAbsolutePath(),
					false);
			if (arrfile != null) {
				for (DMFile tf : arrfile) {

					if (taskFlag == false)
						return false;

					if (tf.isDir()) {
						fileSize = fileSize + tf.mSize;

						getFileLocalInfo(new File(tf.getPath()));

					} else {
						fileSize = fileSize + tf.mSize;
						fileNum += 1;
					}
				}
			}
		} else {// 文件
			fileSize += mFile.length();
			fileNum += 1;
		}
		ProgressUpdate(1);
		return true;
	}

	private boolean getCurLevelFileLocalInfo(File mFile, DMFileCategoryType mType) {
		if ((mFile == null) || (taskFlag == false)) {
			return false;
		}

		if (!mFile.exists())
			return false;

		if (mFile.isDirectory()) {// 文件夹
			// folderNum += 1;
			List<DMFile> arrfile = FileOperationHelper.getInstance().listLocalFolderAllFiles(mFile.getAbsolutePath(),
					false);
			if (arrfile != null) {
				for (DMFile tf : arrfile) {

					if (taskFlag == false)
						return false;

					if (tf.isDir()) {
						// fileSize = fileSize + tf.mSize;
						//
						// getFileLocalInfo(new File(tf.getDirPath()));

					} else {
						if (tf.mType == mType) {

							if (mType == DMFileCategoryType.E_PICTURE_CATEGORY) {
								// 如果是图片，那么大小要在10K以上
								// if(tf.mSize >=
								// SDCardScanner.PICTRUE_MIN_SIZE){
								fileSize = fileSize + tf.mSize;
								fileNum += 1;
								// }
							} else if (mType == DMFileCategoryType.E_MUSIC_CATEGORY) {
								// 如果是音频，那么大小要在200K以上
								// if(tf.mSize >= SDCardScanner.AUDIO_MIN_SIZE){
								fileSize = fileSize + tf.mSize;
								fileNum += 1;
								// }
							} else {
								fileSize = fileSize + tf.mSize;
								fileNum += 1;
							}
						}

					}
				}
			}
		}
		ProgressUpdate(1);
		return true;
	}

	public boolean WebDirAttribute(DMFile file) {
		folderNum += 1;

		List<DMFile> fileList = null;
		try {
			if (typeFolder) {
				DMFilePage filePage = DMSdk.getInstance().getFileListInDirByType(DMFileCategoryType.E_PICTURE_CATEGORY, file.mPath, 0);
				if (filePage != null && filePage.getFiles() != null) {
					fileList = filePage.getFiles();
				}
			}else {
				fileList = FileOperationHelper.getInstance().getUdiskFolderAllFiles(file);
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (fileList == null) {
			Log.d("ra_attr_error", "dirpath = " + file.mPath);
			// 此处可能会出现服务端出毛病返回null
			folderNum--;
			return false;
		}
		for (DMFile tfd : fileList) {
			if (taskFlag == false)
				return false;

			if (tfd.isDir()) {

				fileSize = fileSize + tfd.mSize;

				WebDirAttribute(tfd);

			} else {
				// 是文件，判断是否是媒体文件，如果是并保存
				fileSize = fileSize + tfd.mSize;
				fileNum += 1;

			}

		}
		fileList.clear();
		
		ProgressUpdate(1);
		return true;
	}

	// 只是遍历当前层里面的图片文件
	public boolean WebCurLevelDirAttribute(DMFile file, DMFileCategoryType mType) {
		// folderNum += 1;

		List<DMFile> fileList = null;
		try {
			fileList = FileOperationHelper.getInstance().getUdiskFolderAllFiles(file);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (fileList == null) {
			Log.d("ra_attr_error", "dirpath = " + file.mPath);
			// 此处可能会出现服务端出毛病返回null
			folderNum--;
			return false;
		}
		for (DMFile tfd : fileList) {
			if (taskFlag == false)
				return false;

			if (tfd.isDir()) {

				// fileSize = fileSize + tfd.mSize;
				//
				// WebDirAttribute(tfd.getDirPath());

			} else {
				// 是指定的文件，判断是否是媒体文件，如果是并保存

				if (tfd.mType == mType) {

					if (mType == DMFileCategoryType.E_PICTURE_CATEGORY) {
						// 如果是图片，那么大小要在10K以上
						// if(tfd.mSize >= SDCardScanner.PICTRUE_MIN_SIZE){
						fileSize = fileSize + tfd.mSize;
						fileNum += 1;
						// }
					} else if (mType == DMFileCategoryType.E_MUSIC_CATEGORY) {
						// 如果是音频，那么大小要在200K以上
						// if(tfd.mSize >= SDCardScanner.AUDIO_MIN_SIZE){
						fileSize = fileSize + tfd.mSize;
						fileNum += 1;
						// }
					} else {
						fileSize = fileSize + tfd.mSize;
						fileNum += 1;
					}
				}

			}
		}
		
		fileList.clear();

		ProgressUpdate(1);
		return true;
	}

	public boolean WebFileAttribute(DMFile file) {

		fileSize = file.mSize;
		fileNum = 1;
		ProgressUpdate(1);
		return true;
	}

	private void updateMsg() {
		sSize = FileInfoUtils.getLegibilityFileSize(fileSize);
		sfileBit = FileInfoUtils.changeFileSizeShow(fileSize);

		switch (showType) {
		case SHOWTYPEISFILE: {
			String sizeToShow = String.format(mContext.getString(R.string.DM_More_Detail_Size), sSize);
			if ((attributeDialog != null)) {
				attributeDialog.getSizeTextView().setText(sizeToShow);
			}
		}
			break;
		case SHOWTYPEISDIR: {

			String sizeToShow = String.format(mContext.getString(R.string.DM_More_Detail_Size), sSize);
			String containToShow = null;
			
			if (typeFolder) {
				containToShow = String.format(mContext.getString(R.string.DM_More_Detail_Contain_Folder),folderNum);
			}else {
				containToShow = String.format(mContext.getString(R.string.DM_More_Detail_Contain), folderNum,fileNum);
			}

			if ((attributeDialog != null)) {
				attributeDialog.getSizeTextView().setText(sizeToShow);
				attributeDialog.getContainTextView().setText(containToShow);
			}
		}
			break;
		case SHOWTYPEISLIST: {
			String sizeToShow = String.format(mContext.getString(R.string.DM_More_Detail_Size), sSize);
			String containToShow = String.format(mContext.getString(R.string.DM_More_Detail_Contain), folderNum,fileNum);

			if ((attributeDialog != null)) {
				attributeDialog.getSizeTextView().setText(sizeToShow);
				attributeDialog.getContainTextView().setText(containToShow);
			}

		}
			break;
		}

	}

	UDiskAttributeDialog attributeDialog = null;

	public void showAlertDislog() {

		attributeDialog = new UDiskAttributeDialog(mContext, UDiskBaseDialog.TYPE_ONE_BTN);
		attributeDialog.setTitleContent(mContext.getResources().getString(R.string.DM_Task_Details));
		if (showType == SHOWTYPEISLIST) {
			attributeDialog.getTypeImageView().setImageResource(R.drawable.file_type_multi);
			attributeDialog.getNameTextView().setText(R.string.DM_More_Detail_Multiple);
			attributeDialog.getTypeTextView().setText(String.format(mContext.getString(R.string.DM_More_Detail_Species), mContext.getString(R.string.DM_More_Detail_Other)));
			attributeDialog.getPathLayout().setVisibility(View.GONE);
			attributeDialog.getModifyLayout().setVisibility(View.GONE);
		} else {
			attributeDialog.getTypeTextView().setText(String.format(mContext.getString(R.string.DM_More_Detail_Species), FileUtil.getFileTypeString(mfiledata)));
			attributeDialog.getTypeImageView().setImageResource(FileUtil.getFileLogo(mfiledata));
			attributeDialog.getLastModifyTextView().setText(mfiledata.getLastModified("yyyy-MM-dd HH:mm:ss"));
			attributeDialog.getNameTextView().setText(mfiledata.getName());
			attributeDialog.getPathTextView().setText( mfiledata.getPath());
			
			if (mfiledata.isDir())
				attributeDialog.getContainRelativeLayout().setVisibility(View.VISIBLE);
			else
				attributeDialog.getContainRelativeLayout().setVisibility(View.GONE);
		}
		
		updateMsg();
		attributeDialog.setLeftBtn(mContext.getString(R.string.DM_SetUI_Confirm), null);
		attributeDialog.show();

	}

	/*
	 * 选择刷新，方法 0：必须刷新 》0：每隔一段时间刷新一次
	 */
	long lastTime = 0;// 记录上次刷新的时间

	private boolean ProgressUpdate(int level) {
		if (level == 0) {
			// 必须刷新
			lastTime = System.currentTimeMillis();
			publishProgress();
		} else {
			if ((System.currentTimeMillis() - lastTime) > 500) {
				lastTime = System.currentTimeMillis();
				publishProgress();
			}
		}
		return true;
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		// TODO Auto-generated method stub
		// Log.i(TAG, "which = " + which);
		taskFlag = false;

	}

}