package com.dmsys.airdiskpro.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.dmsys.airdiskpro.GlobalImageLRUCacher;
import com.dmsys.airdiskpro.GlobalImageLRUCacher.DecodeBitmapCallBack;
import com.dmsys.airdiskpro.ui.UploadBaseActivity.Mode;
import com.dmsys.airdiskpro.ui.UploadBaseActivity.OnSelectChangeListener;
import com.dmsys.airdiskpro.utils.ConvertUtil;
import com.dmsys.airdiskpro.utils.FileUtil;
import com.dmsys.airdiskpro.utils.SDCardUtil;
import com.dmsys.dmsdk.model.DMDir;
import com.dmsys.dmsdk.model.DMFile;
import com.dmsys.dmsdk.model.DMFileCategoryType;
import com.dmsys.dmsdk.util.DMFileTypeUtil;
import com.dmsys.mainbusiness.R;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import cn.dm.longsys.library.imageloader.core.DMImageLoader;
import cn.dm.longsys.library.imageloader.core.DisplayImageOptions;
import cn.dm.longsys.library.imageloader.core.listener.PauseOnScrollListener;
import de.greenrobot.event.EventBus;

public class UploadDirView extends FrameLayout
	implements AdapterView.OnItemClickListener, OnItemLongClickListener {

	public interface OnDirViewStateChangeListener {
		public void onChange(Mode state, String currentPath, List<DMFile> fileList);
	}

	public static abstract interface Onload{
	    public abstract void begin();

	    public abstract void end();
	}
	
	private OnDirViewStateChangeListener mOnDirViewStateChangeListener;
	private Onload mOnLoad;

	private Mode mState = Mode.MODE_NORMAL;

	private Context mContext;
	private List<DMFile> mFileList = new ArrayList<DMFile>();
	private MyFileAdaper mAdapter;
	public static final String mMultSDCardChooserFlag = "#*sdcard.choose@!~";
	public static final String mMultSDCardFlag = "/multisdcard/";

	private String mPrimarySdcard;
	private String mSavedSdcard;

	private final String TAG = getClass().getSimpleName();
	
	private String mRootPath;
	
	private DMFile mPlayFile;

	private boolean mJustShowDir = false;
	private int mLocation = DMFile.LOCATION_LOCAL;
	private ListView mList;
	private LayoutInflater mInflater;
	private static final int MSG_LOAD_FILELIST = 0;
	private static final int MSG_NOTIFY_DATA_SET_CHANGED = MSG_LOAD_FILELIST+1;
	private DMImageLoader imageLoader = DMImageLoader.getInstance();
	private DisplayImageOptions mLoaderOptions;
	private BrowserStack mBrowserStackTrace = new BrowserStack();
	private View mEmptyLayout;
	
	private boolean mCancelCache = false;
	private OnSelectChangeListener mOnSelectChangeListener;
	
	

	public void setmOnSelectChangeListener(
			OnSelectChangeListener mOnSelectChangeListener) {
		this.mOnSelectChangeListener = mOnSelectChangeListener;
	}



	private Handler mHandler  = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			if (mOnLoad != null) {
				mOnLoad.end();
			}
			if (msg.what == MSG_LOAD_FILELIST) {
				LoadResult result = (LoadResult)msg.obj;
				String path = mBrowserStackTrace.getLastBrowserRecordPath();
				if(path == null)
					return;
				if (result.location != mLocation || !result.path.equals(path)) {
					// 当前位置已经改变
					return;
				}
				mFileList = result.list;
				if (msg.arg1 == 0) {
					// 加载成功
					refreshFileListView();
				} else {
					// 文件过多加载失败
					showNoSupportTooManyFilesView();
				}
			}
		}
	};
	
	private class LoadResult {
		int location;
		String path;
		List<DMFile> list;
	}

	
	

	public UploadDirView(Context context) {
		super(context);
		mContext = context;
		initSdcardStr();
		initView();
	}

	public UploadDirView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		initSdcardStr();
		initView();
	}

	public UploadDirView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		initSdcardStr();
		initView();
	}
	
	private void initView() {    
		mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = mInflater.inflate(R.layout.upload_dir_explorer_view, null);
		mEmptyLayout = view.findViewById(R.id.emptyRl);
		mList = (ListView) view.findViewById(R.id.list);
		
		mList.setOnScrollListener(new PauseOnScrollListener(imageLoader, false, true));
		initImageLoader();
		addView(view);
	}

	public void setOnDirViewStateChangeListener(OnDirViewStateChangeListener listener) {
		mOnDirViewStateChangeListener = listener;
	}
	
	public void setOnloadListener(Onload onload){
	    mOnLoad = onload;
	}

	/*
	 * 设置当前的路径 *
	 */
	public void setRootPath(String path) {
		if (mMultSDCardChooserFlag.endsWith(path)) {
			setRootPathToSDCardChooser();
		} else {
			addBrowserRecord(path, 0);
		}
	}

	public void setCurrentPath(String path) {
		int index = -1;
		path = path.endsWith(File.separator) ? path : path + File.separator;
		
		StringBuffer sb = new StringBuffer();
		if(isPrimary(mContext,path)) {
			addBrowserRecord(mPrimarySdcard, 0);
			path = path.substring(mPrimarySdcard.length());
			sb.append(mPrimarySdcard);
		} else {
			addBrowserRecord(mSavedSdcard, 0);
			path = path.substring(mSavedSdcard.length());
			sb.append(mSavedSdcard);
		}
		
		while ((index = path.indexOf(File.separator)) != -1) {
			String name = path.substring(0, index + 1);
			path = path.substring(index + 1);
			sb.append(name);
			addBrowserRecord(sb.toString(), 0);
		}
	}

	public void setJustShowDir(boolean justShowDir) {
		mJustShowDir = justShowDir;
	}

	public void setRootPathToSDCardChooser() {
		addBrowserRecord(mMultSDCardChooserFlag, 0);
	}

	/*
	 * 刷新文件列表界面 *
	 */
	public void refreshFileListView() {
//		DMImageLoader.getInstance().resume();
		notifyDataSetChanged();
		if(mFileList!=null) {
			if(mFileList.size()!=0) {
				mEmptyLayout.setVisibility(View.GONE);
				mList.setVisibility(View.VISIBLE);
				//定位
				mList.setSelection(getLastBrowserRecord().mSelection);
			} else {
				mEmptyLayout.setVisibility(View.VISIBLE);
				mList.setVisibility(View.GONE);
			}
		} else {
			mEmptyLayout.setVisibility(View.VISIBLE);
			mList.setVisibility(View.GONE);
		}
		String path = getLastBrowserRecordPath();
		//System.out.println("last path:"+path);
		if (null != mOnDirViewStateChangeListener && path != null) {
			mOnDirViewStateChangeListener.onChange(mState, path, mFileList);
		}
	}
	
	/*
	 * 添加不支持过多的文件的view
	 */
	public void showNoSupportTooManyFilesView() {
		notifyDataSetChanged();
		mEmptyLayout.setVisibility(View.GONE);
		mList.setVisibility(View.GONE);
		String path = getLastBrowserRecordPath();
		//System.out.println("last path:"+path);
		if (null != mOnDirViewStateChangeListener && path != null) {
			mOnDirViewStateChangeListener.onChange(mState, path, mFileList);
		}
	}
	
	
	
	
	
	

	public void setSelection(String pathname) {
		int position = -1;
		for (int i = 0; i < mFileList.size(); i++) {
			DMFile file = mFileList.get(i);
			if (pathname.equals(file.getPath())) {
				position = i;
				break;
			}
		}
		if (position != -1) {
			mList.setSelection(position);
		}
	}

	public void fillDataToList(boolean showLoadingView) {
		final int location = mLocation;
		final String curPath = mBrowserStackTrace.getLastBrowserRecordPath();
		final boolean hasParent = (mBrowserStackTrace.size() > 1);
		if(showLoadingView)
		{
			if (mOnLoad != null) {
				mOnLoad.begin();
			}
		}
		
		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				boolean isError = false;
				List<DMFile> list = null;
				if (location == DMFile.LOCATION_LOCAL) {
					list = getFileDatabyRootPath(curPath);
				} 

				FileUtil.sortFileListByName(list);
				LoadResult result = new LoadResult();
				result.location = location;
				result.path = curPath;
				result.list = list;
				if (isError) {
					mHandler.obtainMessage(MSG_LOAD_FILELIST, -1, 0, result)
							.sendToTarget();
				} else {
					mHandler.obtainMessage(MSG_LOAD_FILELIST, 0, 0, result)
							.sendToTarget();
				}
					
			}
			
		}).start();
	}

	public void notifyDataSetChanged() {
		mAdapter.notifyDataSetChanged();
	}

	/*
	 * 初始化adapter *
	 */
	public void init(Context mContext) {
	
		initSdcardStr();
		//fillDataToList();
		mAdapter = new MyFileAdaper();
		LinearLayout footView = (LinearLayout) LayoutInflater.from(mContext)
                .inflate(R.layout.file_browse_footview, null);
		mList.addFooterView(footView, null, false);
		
		mList.setAdapter(mAdapter);
		mList.setOnItemClickListener(this);
		mList.setOnItemLongClickListener(this);
		mList.setDivider(null);
	
		String path = mBrowserStackTrace.getLastBrowserRecordPath();
		if (path == null) {
			List<String> sdList = getAllSdcard(mContext);
			path = mMultSDCardFlag;
		}
		mBrowserStackTrace.addBrowserRecord(path, 0);
		mRootPath = path;
		fillDataToList(true);
	}

	public class LongPressEvent{}
	
	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
//		position--;
		if (mState != Mode.MODE_NORMAL) { // 普通不是下长按才有反应， 编辑模式下不能长按
			return false;
		}
		
		if (mRootPath.equals(getLastBrowserRecord().mPath)) {
			return false;
		}
	
		setMode(Mode.MODE_EDIT);
		
		DMFile item = mFileList.get(position);
		item.setSelected(true);
		notifyDataSetChanged();
		EventBus.getDefault().post(new LongPressEvent());
		return true;
	}

	private void longclick(DMFile file) {
		if (file.mType == DMFileCategoryType.E_VIDEO_CATEGORY) {
			longclickVideo(file);
		}
	}

	private void longclickVideo(DMFile file) {
		// TODO Auto-generated method stub
		
		String filePath = file.mPath;
//		
//		if(file.mLocation == DMFile.LOCATION_WIFI_UDISK) {
//			filePath = com.dm.baselib.BaseValue.Host + filePath;
//			//filePath = FileInfoUtils.encodeUri(filePath);
//		} else {
			filePath = "file://" + filePath;
//		}
		
		Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("oneshot", 0);
        intent.putExtra("configchange", 0);
        Uri uri = Uri.parse(filePath);
        intent.setDataAndType(uri, "video/*");
		mContext.startActivity(intent);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		
		DMFile item = mFileList.get(position);
		// 正常模式下模式下
		if (mState == Mode.MODE_NORMAL) { 
			
			if (item.mType == DMFileCategoryType.E_XLFILE_UPPER) {
				// 返回上一层：返回上级目录
				toUpperPath();
				return;
			}
			if (!item.isDir()) {
				// 文件：打开
				openFile(item);
			} else {
				// 目录：进入目录
				gotoSubPatg(item);
			}
		} else {
			//编辑模式下
			boolean selected = !item.isSelected();
			item.setSelected(selected);
			ImageView tv = (ImageView)view.findViewById(R.id.cb_file);
			tv.setSelected(selected);
			if(mOnSelectChangeListener != null) {
				mOnSelectChangeListener.OnSelectChange();
			}
		}
	}
	
	// 打开文件，图片文件需要批量打开
	private void openFile(DMFile file) {
		/*if (file.mType == DMFileCategoryType.E_PICTURE_CATEGORY) {
			ArrayList<DMFile> fileList = new ArrayList<DMFile>();
			int index = -1;
			DMFile tmp = null;
			
			for (int i = 0; i < mFileList.size(); i++) {
				tmp = mFileList.get(i);
				if (tmp.mType == DMFileCategoryType.E_PICTURE_CATEGORY) {
					if (index == -1 && tmp.equals(file)) {
						index = fileList.size();
					}
					
					fileList.add(tmp);
				}
			}
			FileOperationHelper.openPicture(mContext,fileList, index);
		} else if (file.mType == DMFileCategoryType.E_MUSIC_CATEGORY) {
			openMusic(file);
		}else {
			boolean openOK = FileOperationHelper.openFile(file,mContext);
			//System.out.println("openfile2 ok:"+openOK);
			if (!openOK) {
				final ProgressDialog dialog = new ProgressDialog(mContext);
				dialog.setProgress(0);
				dialog.setTitleContent(mContext.getString(R.string.DM_Fileexplore_Operation_Titleoperation));
				dialog.setMessage(mContext.getString(R.string.DM_Fileexplore_Loading_File));
				dialog.setLeftBtn(mContext.getString(R.string.DM_Control_Cancel), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						mCancelCache = true;
					}
				});
				
				doDownload(file, dialog);
				dialog.show();
			}
		}*/
	}
	
	public void openMusic(DMFile file)
	{
	   /* MusicPlayerProxy proxy = MusicPlayerProxy.getInstance();
	    proxy.init();
	    if (proxy.getPlayListLocation() != file.mLocation)
	    {
	    	mPlayFile = file;
	    	FileByTypeLoader loader = new FileByTypeLoader(DMFileTypeUtil.DMFileCategoryType.E_MUSIC_CATEGORY);
	    	loader.setLocation(file.mLocation);
	    	loader.loadItems(mHandler,null);
	    }

		
		 Intent intent = new Intent(mContext, MainTabsPager.class);
		 intent.putExtra(MainTabsPager.EXTRA_MESSAGE, MainTabsPager.type2index(file.mType));
		 intent.putExtra(MainTabsPager.EXTRA_SEL_NAME, file.getName());
		 intent.putExtra(MainTabsPager.EXTRA_LOCATION, file.mLocation);
	     mContext.startActivity(intent);
	     
	     proxy.playFile(file);
		*/
	      
	}
	

	public void toUpperPath() {
		// 如果栈中只有个元素了。则不能继续往上
		if (mBrowserStackTrace.size() <= 1) {
			// 不能返回时的操作
			return;
		}
		// 移除栈顶
		BrowserRecord removeBrowse = removeLastBrowserRecord();
		// 刷新数据
		fillDataToList(true);
	}

	/**
	 * 返回到历史浏览记录的弟index条
	 * 
	 * @param index
	 *            浏览记录
	 */
	public void toUpperPath(int index) {
		if (index < 0 || index >= mBrowserStackTrace.size()) {
			return;
		}
		while (mBrowserStackTrace.size() - 1 > index) {
			// 移除栈顶
			removeLastBrowserRecord();
		}
		// 刷新数据
		fillDataToList(true);
		//refreshFileListView();
		// 仅当返回上一级目录的时候，才需要回复页面的状态。
		BrowserRecord br = getLastBrowserRecord();
		if(br!=null)
			mList.setSelection(br.mSelection);
	}
	
	public void toUpperPathByStep(int step) {
		toUpperPath(mBrowserStackTrace.size() - step -1); 
	}

	/**
	 * 设置不能倒退的路径
	 * */
	public void setLimitPath(String limitPath) {
		// String initPath= getLastBrowserRecordPath();
		// limitPath = limitPath.endsWith(File.separator) ? limitPath :
		// limitPath + File.separator;
		//
		// if(limitPath.equals(initPath) ||
		// initPath.startsWith(mMultSDCardChooserFlag)){
		// return;
		// }
		//
		// addBrowserRecord(limitPath, 0, 0);
		//
		// File file = new File(initPath);
		// String parent = null;
		// int index = 1;
		// while(!(parent = getParent(file)).equals(limitPath)) {
		// addBrowserRecord(parent, 0, index);
		// index ++;
		// file = new File(parent);
		// }

	}

	public String getParent(File file) {
		String parent = file.getParent();
		return parent.endsWith(File.separator) ? parent : parent + File.separator;
	}



	public void selectAll(boolean isAll) {
		Iterator<DMFile> iter = mFileList.iterator();
		while (iter.hasNext()) {
			iter.next().selected = true;
		}
		refreshFileListView();
	}

	

	public String getCurrentPath() {
		return getLastBrowserRecord() != null ? getLastBrowserRecord().mPath : null;
	}

	/*
	 * 得到被选中的文件 *
	 */
	public List<DMFile> getSelectFiles() {
		List<DMFile> mPathList = new ArrayList<DMFile>();
		if(mFileList == null) return mPathList;
		for (DMFile item : mFileList) {
			if (item.selected && item.mType != DMFileCategoryType.E_XLFILE_UPPER) {
				mPathList.add(item);
			}
		}
		return mPathList;
	}
	
	public int getmFileListSize() {
		if(mFileList == null ) return 0;
		return this.mFileList.size();
	}
	

	public List<BrowserRecord> getBrowserRecords() {
		return mBrowserStackTrace.getTrace();
	}

	
	
	public int getLocation(){

		return mLocation;
	}

	public boolean isSdCardPage(){
		
		List<BrowserRecord> records = getBrowserRecords();
	    String str = "";
	    if (!records.isEmpty())
	      str = ((BrowserRecord)records.get(0)).mPath;
	    return str == "#*multsdcard@!~";
	  }
	
	public boolean isAllSelected() {
		if (mState == Mode.MODE_EDIT) { 
			for (int i = 0; i < mFileList.size(); i++) {
				DMFile file = mFileList.get(i);
				if (!file.selected && file.mType != DMFileCategoryType.E_XLFILE_UPPER) { 
					return false;
				}
			}
			return true;
		} else { 
			return false;
		}
	}

	public boolean isCanToUpper() {
		return mBrowserStackTrace.size() > 1;
	}

	/*
	 * 获取当前目录下的文件
	 */
	private List<DMFile> getFileDatabyRootPath(String path) {
		List<DMFile> fileItemList = new ArrayList<DMFile>();
		// 获取当前栈中，栈顶元素的表示的文件路径
		String lastPath = path;
		// 如果栈顶的路径是SD卡选择路径，则显示sd卡
		
		if (null != lastPath && lastPath.equals(mMultSDCardFlag)) {
			if (!TextUtils.isEmpty(mPrimarySdcard)) {
				DMDir inner = new DMDir();
				inner.mIsSDCardPath = true;
				inner.initLocalFileByPath(mPrimarySdcard);
				fileItemList.add(inner);
			}
			if (!TextUtils.isEmpty(mSavedSdcard)) {
				DMDir outter = new DMDir();
				outter.mIsSDCardPath = true;
				outter.initLocalFileByPath(mSavedSdcard);
				fileItemList.add(outter);
			}
			return fileItemList;
		}

		// 如果栈顶的路径是一个正确的文件路径，则显示该路径下的文件
		File fileRoot = new File(lastPath);
		FileFilter filter = new FileFilter() {

			@Override
			public boolean accept(File pathname) {
				if (pathname.getName().startsWith(".")) {
					return false;
				}
				if (mJustShowDir && !pathname.isDirectory()) {
					return false;
				}
				return true;
			}
		};

		File[] files = fileRoot.listFiles(filter);
		/**
		 * 防止没有sdcard时取不到文件的崩溃
		 */
		if (files != null) {
			for (File file : files) {
				DMFile item = null;
				if (file.isDirectory()) {
					item = new DMDir();
					item.mType = DMFileCategoryType.E_XLDIR_CATEGORY;
				} else {
					item = new DMFile();
					item.mType = DMFileTypeUtil.getFileCategoryTypeByName(file.getName());
				}
				item.mLastModify = file.lastModified();
				item.mPath = file.getAbsolutePath();
				item.mSize = file.length();
				item.mName = file.getName();
				fileItemList.add(item);
			}
		}

		return fileItemList;
	}

	/**
	 * 执行获取这一层目录的文件
	 * @param item
	 */
	private void gotoSubPatg(DMFile item) {
		// 保存当前目录下，屏幕顶部显示的文件的position，用户点“返回上级”的时候，用户恢复状态。
		int pos = 0;
		int firstVisiblePosition = mList.getFirstVisiblePosition();

		saveCurrentRecodeStatu(firstVisiblePosition);
		
		// 设置栈顶目录
		for (int i = 0; i < mFileList.size(); i++) {
			DMFile f = mFileList.get(i);
			if (item.getPath().equals(f.getPath())) {
				pos = i;
				break;
			}
		}
		
		//System.out.println("Upper pos2:"+pos);
		addBrowserRecord(item.mPath, pos);
		////System.out.println("filePathclick = " + item.mPath);
		// 刷新视图
		fillDataToList(true);
		//refreshFileListView();
		mList.setSelection(0);
	}

	/**
	 * 将路径中的根路径转化为内部存储卡、外部存储卡
	 * */
	public String changePathToName(String path) {

		if(path == null) {
			return "";
		}
		if (null != path && path.startsWith(mMultSDCardChooserFlag)) {
			return "";
		}
		if (isPrimary(mContext,path)) {
			// 内置sdcard
			return path.replace(mPrimarySdcard, "/" + getContext().getString(R.string.DM_Public_Primary_Sdcard_Name) + "/");
		} else {
			// 外置sdcard
			return path.replace(mSavedSdcard, "/" + getContext().getString(R.string.DM_Public_Saved_Sdcard_Name) + "/");
		}
	}

	private void initSdcardStr() {
		mPrimarySdcard = SDCardUtil.getPrimarySDCard() + "";
		mSavedSdcard = SDCardUtil.getSlaverSDCard(mContext) + "";
		
		Log.d(TAG, mPrimarySdcard + ", " + mPrimarySdcard);
	}
	
	private static class BrowserRecord {
		public String mPath;
		public int mSelection;
	}
	
	private static class BrowserStack {
		private final ArrayList<BrowserRecord> mBrowserStackTrace = new ArrayList<BrowserRecord>();

		private void addBrowserRecord(String path, int y, int index) {
			BrowserRecord br = new BrowserRecord();
			br.mPath = path;
			br.mSelection = y;
			mBrowserStackTrace.add(index, br);
		}

		public void addBrowserRecord(String path, int y) {
			BrowserRecord br = new BrowserRecord();
			br.mPath = path;
			br.mSelection = y;
			mBrowserStackTrace.add(br);
		}

		private BrowserRecord removeLastBrowserRecord() {
			if (mBrowserStackTrace.size() > 0) {
				return mBrowserStackTrace.remove(mBrowserStackTrace.size() - 1);
			} else {
				return null;
			}
		}

		private void saveCurrentRecodeStatu(int y) {
			if (mBrowserStackTrace.size() > 0) {
				mBrowserStackTrace.get(mBrowserStackTrace.size() - 1).mSelection = y;
			}
		}

		private BrowserRecord getLastBrowserRecord() {
			if (mBrowserStackTrace.size() > 0) {
				return mBrowserStackTrace.get(mBrowserStackTrace.size() - 1);
			} else {
				return null;
			}
		}

		private String getLastBrowserRecordPath() {
			if (mBrowserStackTrace.size() > 0) {
				BrowserRecord rec = mBrowserStackTrace.get(mBrowserStackTrace.size() - 1);
				String path = null;
				if (rec != null) {
					path = rec.mPath.endsWith(File.separator) ? rec.mPath : rec.mPath + File.separator;
				}
				return path;
			} else {
				return null;
			}
		}
		
		public void clearAllBrowserRecord() {
			mBrowserStackTrace.clear();
		}
		
		public int size() {
			return mBrowserStackTrace.size();
		}
		
		public ArrayList<BrowserRecord> getTrace() {
			return mBrowserStackTrace;
		}
	}
	
	private void addBrowserRecord(String path, int y, int index) {
		mBrowserStackTrace.addBrowserRecord(path, y, index);
	}

	public void addBrowserRecord(String path, int y) {
		mBrowserStackTrace.addBrowserRecord(path, y);
	}

	private BrowserRecord removeLastBrowserRecord() {
		return mBrowserStackTrace.removeLastBrowserRecord();
	}

	private void saveCurrentRecodeStatu(int y) {
		mBrowserStackTrace.saveCurrentRecodeStatu(y);
	}

	private BrowserRecord getLastBrowserRecord() {
		return mBrowserStackTrace.getLastBrowserRecord();
	}

	private String getLastBrowserRecordPath() {
		return mBrowserStackTrace.getLastBrowserRecordPath();
	}
	
	public void clearAllBrowserRecord() {
		mBrowserStackTrace.clearAllBrowserRecord();
	}
	
	public final class ViewHolder {
		public ImageView mFileIcon;
		public TextView mFileName;
		public TextView mFileSize;
		public TextView mFileDate;
		public ImageView mSelectedButton;
	}

	/*
	 * 文件列表的adapter *
	 */
	public class MyFileAdaper extends BaseAdapter {
		private LayoutInflater layoutInflater;
		private int curLocation = DMFile.LOCATION_LOCAL;

		public MyFileAdaper() {
			layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public int getCount() {
			if (mFileList == null) {
				return 0;
			}
			
			return mFileList.size();
		}

		@Override
		public Object getItem(int position) {
			return mFileList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final ViewHolder holder;
			if (convertView == null) {
				convertView = layoutInflater.inflate(R.layout.upload_dir_item, null);
				holder = new ViewHolder();
				holder.mFileIcon = (ImageView) convertView.findViewById(R.id.iv_icon);
				holder.mFileName = (TextView) convertView.findViewById(R.id.tv_file_name);
				holder.mFileSize = (TextView) convertView.findViewById(R.id.tv_file_size);
				holder.mFileDate = (TextView) convertView.findViewById(R.id.tv_file_date);
				holder.mSelectedButton = (ImageView) convertView.findViewById(R.id.cb_file);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			TextView mUpperText = (TextView) convertView.findViewById(R.id.tv_upper_name);
			// 填充data
			final DMFile item = (DMFile) getItem(position);

			// 根据不同类型，设置图标
			DMFileCategoryType type = item.mType;
			if (type == DMFileCategoryType.E_XLFILE_UPPER) {
				// 返回上一级
				mUpperText.setVisibility(View.VISIBLE);
				holder.mFileName.setVisibility(View.GONE);
				holder.mFileDate.setVisibility(View.GONE);

				mUpperText.setText(item.mPath);
//				holder.mFileIcon.setImageResource(R.drawable.file_manage_up);
				holder.mFileName.setText(item.mPath);
				holder.mSelectedButton.setVisibility(View.GONE);
				holder.mFileDate.setVisibility(GONE);
				holder.mFileSize.setVisibility(GONE);
			} else {
				// 返回上一层消失
				mUpperText.setVisibility(View.GONE);
				holder.mFileName.setVisibility(View.VISIBLE);
				holder.mSelectedButton.setVisibility(View.VISIBLE);
				holder.mFileDate.setVisibility(VISIBLE);
				holder.mFileSize.setVisibility(VISIBLE);
				if (type == DMFileCategoryType.E_PICTURE_CATEGORY || type == DMFileCategoryType.E_VIDEO_CATEGORY ) {
					// 这里显示图片
					updatePicIcons(holder.mFileIcon, item);
				} else {
					//取消之前的引用下载
					imageLoader.cancelDisplayTask(holder.mFileIcon);
					int iconId = FileUtil.getFileLogo(item);
					holder.mFileIcon.setImageResource(iconId);
				}
				String name = null;
				if (mPrimarySdcard.equals(item.mPath)) {
					name = getContext().getString(R.string.DM_Public_Primary_Sdcard_Name);
				} else if (mSavedSdcard.equals(item.mPath)) {
					name = getContext().getString(R.string.DM_Public_Saved_Sdcard_Name);
				} else {
					name = item.getName();
				}
				holder.mFileName.setText(name);
				// 文件(夹)的最后修改时间
				holder.mFileDate.setText(item.getLastModified("yyyy-MM-dd"));

				/*
				 * 文件大小，如果是文件夹就不显示 *
				 */
				if (!item.isDir()) {
					holder.mFileSize.setVisibility(View.VISIBLE);
					String fileSizeStr = ConvertUtil.convertFileSize(item.mSize, 2);
					holder.mFileSize.setText(fileSizeStr);
				} else {

					// 是个目录
					DMDir dir = (DMDir) item;
					String path = dir.getPath();
					//System.out.println("liu path2:"+path);
					if (dir.mIsSDCardPath) { // 是SD卡目录,则要显示SD卡的可用空间和总空间
						holder.mFileSize.setVisibility(View.VISIBLE);
						if (dir.mLocation == DMFile.LOCATION_LOCAL) {
							curLocation = DMFile.LOCATION_LOCAL;
							long total = SDCardUtil.getTotalSizeOf(path);
							long available = SDCardUtil.getAvailableSizeOf(path);
							String size = mContext.getString(R.string.DM_Capacity_Sum_Size) + ConvertUtil.convertFileSize(total, 2) + " , "+mContext.getString(R.string.DM_Capacity_Free_Size) + ConvertUtil.convertFileSize(available, 2);
							holder.mFileSize.setText(size);
						}
						holder.mFileDate.setVisibility(View.GONE); // 不显示最后修改时间
					} else { // 非SD卡目录
						holder.mFileSize.setVisibility(View.GONE); // 不显示文件大小
						holder.mFileDate.setVisibility(View.VISIBLE); // 显示最后修改时间
					}
				}

				// 判断是否是编辑模式, 发送文件时文件夹禁止选中
				if (mState == Mode.MODE_EDIT) {
					holder.mSelectedButton.setVisibility(View.VISIBLE);
					holder.mSelectedButton.setSelected(item.selected);
				} else {
					holder.mSelectedButton.setVisibility(View.INVISIBLE);
				}
			}

			return convertView;
		}
		/*
		 * 使用第三方库 显示图片
		 */
		
		private void updatePicIcons(ImageView iconview, DMFile item) {
			
			if (item.mType == DMFileCategoryType.E_PICTURE_CATEGORY) {
				String uri = null;
				
				//本地用decode
				uri = "file://" + item.mPath;
				//直接显示图片
				imageLoader.displayImage(uri, iconview, mLoaderOptions, null);
			}else {
				
				Bitmap b = GlobalImageLRUCacher.getInstance(mContext).getVideoThumbnail(item.mPath, iconview, new DecodeBitmapCallBack() {
						public void callback(Bitmap bmp, Object flag) {
							if (null != bmp) {
								//mHandler.obtainMessage(MSG_NOTIFY_DATA_SET_CHANGED).sendToTarget();
								mHandler.post(new Runnable() {
									
									@Override
									public void run() {
										// TODO Auto-generated method stub
										notifyDataSetChanged();
									}
								});
							}
						}
					});
				
				if (b == null) {
					iconview.setImageResource(FileUtil.getFileLogo(item.getType()));
				}else {
					iconview.setImageBitmap(b);
				}
				
			}
			
			
		}
	}

	
	public void setLocation(int location) {
		// TODO Auto-generated method stub
		
}
	
	public String getRelativePath(String path)
	{
		 List<BrowserRecord> record = getBrowserRecords();
		 String root = mRootPath;
		 System.out.println("rrr 11:"+root);
		 if(!record.isEmpty())
		 {
			 root = record.get(0).mPath;
			 ////System.out.println("rrr 22:"+root);
		 }
		 if(root.charAt(root.length()-1) == '/')
		 {
			 root = root.substring(0, root.length() - 1);
		 }
		 
		 path = path.replaceFirst(root, "");
		
		 
		 if (mMultSDCardFlag.contains(root)) {
			 
			 path = strokePath(path);   //将path进行切割，留下倒数第二个/到路径最后一位，解决路径显示栏进入各级sd卡显示问题；
			
		 }
		 
		// //System.out.println("rrr path:"+path);
		 
		return path;
	}
	private String strokePath(String path) {
		 
		 List<String> list = getAllSdcard(mContext);
		 for (int i = 0; i < list.size(); i++) {
			 
			 String re = list.get(i);
			 
			 String tmp1 = re.substring(0,  re.length() - 1);
			 
			 String tmp2 = re.substring(0,tmp1.lastIndexOf("/"));
			 
			 if (path.contains(tmp2)) {
				 path = path.replace(re.substring(0,  tmp2.length()), "");
				 return path;
			}
		}
		return path;
	}


	public void setMode(Mode mode) {
		// TODO Auto-generated method stub
		this.mState = mode;
//		if (mode == IFileExplorer.MODE_EDIT) {
//			this.setCanEditable(EditState.STATE_DELETE);
//		} else if (mode == IFileExplorer.MODE_NORMAL) {
//			this.setCanEditable(EditState.STATE_NORMAL);
//		}
	}
	public Mode getMode() {
		return this.mState;
	}


	public void selectAll() {
		// TODO Auto-generated method stub
		selectAllIfNeed();
		notifyDataSetChanged();
	}
	
	public void selectAllIfNeed() {
		if (mState == Mode.MODE_EDIT) { 
			for (int i = 0; i < mFileList.size(); i++) {
				DMFile file = mFileList.get(i);
				if (file.mType != DMFileCategoryType.E_XLFILE_UPPER) {
					file.selected = true;
				}
			}
		} 
	}
	
	public void unselectAll() {
		
		Iterator<DMFile> iter = mFileList.iterator();
		while (iter.hasNext()) {
			iter.next().selected = false;
		}
		notifyDataSetChanged();
	}


	public void smoothScrollToPosition(final int position) {
		// TODO Auto-generated method stub
		  mList.smoothScrollToPosition(position);
	       new Handler().postDelayed(new Runnable()
	        {
	            @Override
	            public void run()
	            {
	                if (mList.getFirstVisiblePosition() > position)
	                {
	                	mList.setSelection(position);
	                }
	            }
	        }, 200);
	}
	

	public List<DMFile> getSelectedFiles() {
		// TODO Auto-generated method stub
		return getSelectFiles();
	}


	public void reloadItems(boolean showLoadingView) {
		// TODO Auto-generated method stub
		//refreshFileListView();
		this.fillDataToList(true);
	}
	private void initImageLoader() {
		mLoaderOptions = new DisplayImageOptions.Builder()
		  .cacheInMemory(true)
		  .showImageOnFail(R.drawable.filemanager_photo_fail)
		  .useThumb(true)
		  .cacheOnDisk(true)
		  .showImageOnLoading(R.drawable.bt_download_manager_image)
		  .showImageForEmptyUri(R.drawable.filemanager_photo_fail)
		  .build();
		
		
	}
	/*@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		//System.out.println("back keyCode:"+keyCode + ",back :"+isCanToUpper());
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			
			boolean canToUpper = isCanToUpper();
			if (canToUpper) {
				this.toUpperPath();
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}*/


	
	/*
	 *EventBus的使用详情百度下
	 *载imagePagerAvtivity每一张图片的切换都会回调这个
	 */
//	public void onEventMainThread(FileManagerDirViewCallBackPosition event) {  
//		final int position = event.position;
//		if(position >= 0 && mList != null 
//				&& mAdapter != null && position < mAdapter.getCount()) {
//			if(!itemPositionVisable(position)) {
//				mList.setSelection(position);
//			}
//		}
//	} 
	/*
	 * 完全露出整个item 返回true 被遮住一点就返回false
	 */
		private boolean itemPositionVisable(int posi) {
			boolean ret = false;
			int firstPosition = mList.getFirstVisiblePosition();
			int lastPosition = mList.getLastVisiblePosition();
			
			if (posi >= firstPosition && posi <= lastPosition) {
				View v = mList.getChildAt(posi-firstPosition);
				v.measure(0, 0);
				mList.measure(0, 0);
				if (v.getTop() >= 0 && (mList.getHeight() - v.getBottom() >= 0)) {
					ret = true;
				} else {
					ret = false;
				}
			} 
			return ret;
				
		}

//		@Override
//		public void resetUiMode() {
//			// TODO Auto-generated method stub
//			
//		}
		
		
		public static boolean isPrimary(Context context,String path) {

			if(null == path) {
				return false;
			}

			String mSavedSdcard = SDCardUtil.getSlaveSDCard(context);
			String mPrimarySdcard = SDCardUtil.getPrimarySDCard();

			/**
			 * 这里做逻辑判断是内置sdcard 还是 外置 sdcard
			 *
			 * 注：三星手机的外置SD卡是挂载在内部SD卡目录下面的：/mnt/internal_sd/external_sd,
			 * 所以想单纯地根据startWitdh来判断该路径是内外SD卡是不保险的。
			 * 但是，先去验证该路径是否为外置SD卡，然后才去验证是否为内置SD卡的话，就可以绕过上面描述的这个问题。
			 * */
			if (!TextUtils.isEmpty(mSavedSdcard) && path.startsWith(mSavedSdcard)) {
				return false;
			} else if (path.startsWith(mPrimarySdcard + "")) {
				return true;
			} else {
				return false;
			}

		}
		public static List<String> getAllSdcard(Context context) {
			
			String innerSDCardPath = SDCardUtil.getPrimarySDCard();
			String outerSDCardPath = SDCardUtil.getSlaveSDCard(context);
			List<String> mSdcardPaths = new ArrayList<String>();
			if (null != innerSDCardPath && innerSDCardPath.trim().length() > 1) {
				mSdcardPaths.add(innerSDCardPath);
			}
			if (null != outerSDCardPath && outerSDCardPath.trim().length() > 1) {
				mSdcardPaths.add(outerSDCardPath);
			}
			return mSdcardPaths;
		}
		
		

}
