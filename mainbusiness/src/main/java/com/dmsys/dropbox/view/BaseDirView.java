package com.dmsys.dropbox.view;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.dmairdisk.aodplayer.api.AodPlayer;
import com.dmairdisk.aodplayer.api.AodPlayer.OnAodPlayerStatusListener;
import com.dmairdisk.aodplayer.impl.MediaPlayerImpl.MusicPlayerListener;
import com.dmsys.airdiskpro.filemanager.FileManager;
import com.dmsys.airdiskpro.filemanager.FileOperationHelper;
import com.dmsys.airdiskpro.ui.MainActivity;
import com.dmsys.airdiskpro.ui.MainFragment;
import com.dmsys.airdiskpro.ui.MountPcActivity;
import com.dmsys.airdiskpro.utils.ConvertUtil;
import com.dmsys.airdiskpro.utils.DipPixelUtil;
import com.dmsys.airdiskpro.utils.FileUtil;
import com.dmsys.airdiskpro.utils.HandlerUtil;
import com.dmsys.airdiskpro.utils.HandlerUtil.StaticHandler;
import com.dmsys.airdiskpro.utils.XLLog;
import com.dmsys.airdiskpro.view.ProgressDialog;
import com.dmsys.dmsdk.model.DMDir;
import com.dmsys.dmsdk.model.DMFile;
import com.dmsys.dmsdk.model.DMFileCategoryType;
import com.dmsys.dmsdk.model.DMStorage;
import com.dmsys.dmsdk.util.DMFileTypeUtil;
import com.dmsys.dropbox.activity.MyDropBoxAllFileFragment.DropBoxEditState;
import com.dmsys.dropbox.activity.MyDropBoxAllFileFragment.IDropBoxFileExplorer;
import com.dmsys.dropbox.api.OnDropBoxDirViewStateChangeListener;
import com.dmsys.dropbox.api.OnDropBoxFileItemClickListener;
import com.dmsys.dropbox.api.OnDropBoxMusicChangeListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.dmsys.mainbusiness.R;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.dm.longsys.library.imageloader.core.DMImageLoader;
import cn.dm.longsys.library.imageloader.core.DisplayImageOptions;
import cn.dm.longsys.library.imageloader.core.listener.PauseOnScrollListener;


public abstract class BaseDirView extends FrameLayout implements
        AdapterView.OnItemClickListener,
        OnItemLongClickListener, IDropBoxFileExplorer {







    public abstract interface Onload {
        public abstract void begin();

        public abstract void end();
    }

    private OnDropBoxFileItemClickListener mOnFileItemClickListener;
    private Onload mOnLoad;
    public OnDropBoxDirViewStateChangeListener onDropBoxDirViewStateChangeListener;
    public OnDropBoxMusicChangeListener onDropBoxMusicChangeListener;

    private DropBoxEditState mState = DropBoxEditState.STATE_NORMAL;

    private Activity mContext;
    public List<DMFile> mFileList;
    private MyFileAdaper mAdapter;

    private final String TAG = getClass().getSimpleName();

    private String mRootPath;

    public AodPlayer mAodPlayer;
    private long coookie_AodPlayer, cookie_status;

    private PullToRefreshListView mList;

    public StaticHandler mHandler;
    private MyMessageListener mMessageListener = new MyMessageListener();

    private LayoutInflater mInflater;
    private final int MSG_LOAD_FILELIST = HandlerUtil.generateId();
    private DMImageLoader imageLoader = DMImageLoader.getInstance();
    public DisplayImageOptions mLoaderOptions;
    private BrowserStack mDropBoxtackTrace = new BrowserStack();
    private View mEmptyLayout;
    private TextView mEmptyText;
    private ImageView mEmptyImageView;

    // private boolean mountPc = false;

    private int lastBrowseIndex = 0;

    public boolean mCancelCache = false;

    private int curFileType = 0;

    public static int FILE_TYPE_DOWNLOAD = 0;
    public static int FILE_TYPE_AIRDISK = 1;
    public static int FILE_TYPE_PATHSELECT = 2;
    public boolean mountPc = false;
    HashMap<String, String> headers = new HashMap<String, String>();

    public class LoadResult {
        String path;
        List<DMFile> list;
    }

    public BaseDirView(Context context) {
        super(context);
        mContext = (Activity) context;
        initView();
    }

    public BaseDirView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = (Activity) context;
        initView();
    }

    public BaseDirView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = (Activity) context;
        initView();
    }

    private void initView() {
        mInflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = mInflater.inflate(
                R.layout.dropbox_filemanager_typer_dir_explorer_view, null);
        mEmptyLayout = view.findViewById(R.id.emptyRl);
        mEmptyText = (TextView) view.findViewById(R.id.emptyTextView);
        mEmptyImageView = (ImageView) view.findViewById(R.id.emptyImageView);

        mList = (PullToRefreshListView) view.findViewById(R.id.list);

        mList.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {

            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                // TODO Auto-generated method stub
                fillDataToList(false);
            }
        });

        mList.setOnScrollListener(new PauseOnScrollListener(imageLoader, false,
                true));

        mAodPlayer = AodPlayer.getInstance();

        addView(view);

    }

    public void setOnFileItemClickListener(
            OnDropBoxFileItemClickListener listener) {
        mOnFileItemClickListener = listener;
    }

    public void setOnloadListener(Onload onload) {
        mOnLoad = onload;
    }

    public OnDropBoxDirViewStateChangeListener getOnDropBoxDirViewStateChangeListener() {
        return onDropBoxDirViewStateChangeListener;
    }

    public void setOnDropBoxDirViewStateChangeListener(
            OnDropBoxDirViewStateChangeListener onDropBoxDirViewStateChangeListener) {
        this.onDropBoxDirViewStateChangeListener = onDropBoxDirViewStateChangeListener;
    }

    public OnDropBoxMusicChangeListener getOnDropBoxMusicChangeListener() {
        return onDropBoxMusicChangeListener;
    }

    public void setOnDropBoxMusicChangeListener(
            OnDropBoxMusicChangeListener onDropBoxMusicChangeListener) {
        this.onDropBoxMusicChangeListener = onDropBoxMusicChangeListener;
    }

    private class MyMessageListener implements HandlerUtil.MessageListener {
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            if (mOnLoad != null) {
                mOnLoad.end();
            }

            if (msg.what == MSG_LOAD_FILELIST) {
                mList.onRefreshComplete();
                LoadResult result = (LoadResult) msg.obj;
                String path = mDropBoxtackTrace.getLastBrowserRecordPath();
                if (path == null)
                    return;
                mFileList = result.list;
                if (msg.arg1 == 0) {
                    // 加载成功
                    refreshFileListView();
                }
            }
        }
    }

    /*
     * 刷新文件列表界面 *
     */
    public void refreshFileListView() {
        DMImageLoader.getInstance().resume();
        notifyDataSetChanged();
        String path = getLastBrowserRecordPath();
        if (mFileList != null && mFileList.size() != 0) {
            mEmptyLayout.setVisibility(View.GONE);
            // 定位
            mList.getRefreshableView().setSelection(lastBrowseIndex);
        } else {

            mEmptyLayout.setVisibility(View.VISIBLE);
            //
            if (mountPc) {
                Intent intent = new Intent(mContext, MountPcActivity.class);
                mContext.startActivity(intent);
            } else {
                if (path == null || (path != null && path.equals("/"))) {
                    if (curFileType != DMFileCategoryType.E_VIDEO_CATEGORY
                            .ordinal()
                            && curFileType != DMFileCategoryType.E_PICTURE_CATEGORY
                            .ordinal()
                            && curFileType != DMFileCategoryType.E_MUSIC_CATEGORY
                            .ordinal()
                            && curFileType != DMFileCategoryType.E_BOOK_CATEGORY
                            .ordinal()) {
                        mEmptyImageView
                                .setImageResource(R.drawable.connect_guide_nodisk);
                        mEmptyText.setText(R.string.DM_MDNS_No_Disk);
                    } else {
                        mEmptyImageView
                                .setImageResource(R.drawable.list_empty_icon);
                        mEmptyText.setText(R.string.DM_No_File);
                    }

                } else {
                    mEmptyImageView
                            .setImageResource(R.drawable.list_empty_icon);
                    if (curFileType == FILE_TYPE_PATHSELECT) {
                        mEmptyText.setText(R.string.DM_No_Sub_File);
                    } else {
                        mEmptyText.setText(R.string.DM_No_File);
                    }
                }
            }
        }

        if (onDropBoxDirViewStateChangeListener != null) {
            onDropBoxDirViewStateChangeListener.onChange(mState, path,
                    mFileList);
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
            // mList.setSelection(position);
            mList.getRefreshableView().setSelection(position);
        }
    }

    public void fillDataToList(boolean showLoadingView) {

        final String curPath = mDropBoxtackTrace.getLastBrowserRecordPath();

        if (showLoadingView) {
            if (mOnLoad != null) {
                mOnLoad.begin();
            }
        }

        new Thread(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub

                List<DMFile> list = new ArrayList<>();

                LoadResult result = new LoadResult();
                result.path = curPath;

                try {
                    list = listFile(curFileType, curPath);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                result.list = list;
                mHandler.obtainMessage(MSG_LOAD_FILELIST, 0, 0, result)
                        .sendToTarget();

            }

        }).start();
    }

    protected List<DMFile> getUdiskFolderFromList(List<DMFile> list) {
        // TODO Auto-generated method stub
        List<DMFile> folders = new ArrayList<>();
        for (DMFile dmFile : list) {
            if (dmFile.isDir) {
                folders.add(dmFile);
            }
        }
        return folders;
    }

    /*
     * 获取本地目录下的文件
     */
    public List<DMFile> getDownloadFileData(String path) {
        List<DMFile> fileItemList = new ArrayList<DMFile>();
        // 获取当前栈中，栈顶元素的表示的文件路 ?
        String lastPath = path;

        // 如果栈顶的路径是 ?个正确的文件路径，则显示该路径下的文 ?
        File fileRoot = new File(lastPath);
        FileFilter filter = new FileFilter() {

            @Override
            public boolean accept(File pathname) {
                if (pathname.getName().startsWith(".")) {
                    return false;
                }
                return true;
            }
        };

        File[] files = fileRoot.listFiles(filter);

        if (files != null) {
            for (File file : files) {
                DMFile item = null;
                if (file.isDirectory()) {
                    item = new DMDir();
                    item.isDir = true;
                } else {
                    item = new DMFile();
                    item.mType = DMFileTypeUtil.getFileCategoryTypeByName(file
                            .getName());
                }
                item.mLastModify = file.lastModified();
                item.mPath = file.getAbsolutePath();
                item.mSize = file.length();
                item.mName = file.getName();
                item.mLocation = DMFile.LOCATION_LOCAL;
                fileItemList.add(item);
            }
        }
        return fileItemList;
    }

    public void notifyDataSetChanged() {
        mAdapter.notifyDataSetChanged();
    }

    /*
     * 初始化adapter *
     */
    public void init(int fileType) {
        this.curFileType = fileType;
        mHandler = new StaticHandler(mMessageListener);
        mAdapter = new MyFileAdaper();
        mList.setAdapter(mAdapter);
        mList.getRefreshableView().setOnItemClickListener(this);
        mList.getRefreshableView().setOnItemLongClickListener(this);
        attachAvodListener();

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
        /**
         * 这里的onFileClick由外部的return 决定是否响应item 事件，外面返回是false，说明要响应点击事件，ctrl+T 追踪
         */
        if (null != mOnFileItemClickListener
                && mOnFileItemClickListener.onFileClick(position)) {
            return;
        }

        // 如果是文件夹打开，是文件的话打开 ?
        DMFile item = mFileList.get(position - 1);
        if (mState == DropBoxEditState.STATE_EDIT) { // 删除模式
            if (item.mType == DMFileCategoryType.E_XLFILE_UPPER) {
                return;
            }
            item.selected = !item.selected;
            mAdapter.notifyDataSetChanged();

        } else if (mState == DropBoxEditState.STATE_SHARE) { // 分享模式
            if (item.mType == DMFileCategoryType.E_XLFILE_UPPER) {
                // 返回上一层
                toUpperPath();
                return;
            }

            if (item.isDir()) {
                // 目录,前往这个目录
                gotoSubPatg(item);
            } else {
                // 文件
                item.selected = !item.selected;
                refreshFileListView();
            }

        } else {// 非编辑模式下
            if (item.mType == DMFileCategoryType.E_XLFILE_UPPER) {
                // 返回上一层：返回上级目录
                toUpperPath();
                return;
            }

            if (!item.isDir()) {
                // 文件：打 ?
                openFile(item);
            } else {
                // 目录：进入目 ?
                gotoSubPatg(item);
            }
        }
    }

    // 打开文件，图片文件需要批量打 ?
    private void openFile(final DMFile file) {

        if (file.mType == DMFileCategoryType.E_PICTURE_CATEGORY) {
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
            openPicture(mContext, fileList, index);
        } else if (file.mType == DMFileCategoryType.E_MUSIC_CATEGORY) {
            openMusic(file);
        } else if (file.mType == DMFileCategoryType.E_UNSUPPORT_VIDEO_CATEGORY) {
            FileOperationHelper.getInstance()
                    .openUnsupportVideo(file, mContext);
        } else if (file.mType == DMFileCategoryType.E_VIDEO_CATEGORY) {
            boolean openOK = openFile(file, mContext);
        } else {
            boolean openOK = openFile(file, mContext);
            System.out.println("openfile2 ok:" + openOK);
            if (!openOK) {
                final ProgressDialog dialog = new ProgressDialog(mContext);
                dialog.setProgress(0);
                dialog.setTitleContent(mContext
                        .getString(R.string.DM_Task_Download));
                dialog.setMessage(mContext
                        .getString(R.string.DM_Fileexplore_Loading_File));
                dialog.setLeftBtn(
                        mContext.getString(R.string.DM_Control_Cancel),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                // TODO Auto-generated method stub
                                mCancelCache = true;
                            }
                        });

                doDownload(file, dialog);
                dialog.show();
            }
        }
    }

    public List<String> getCurrentMusicFiles() {

        List<String> list = new ArrayList<String>();
        for (int i = 0; i < mFileList.size(); i++) {
            if (mFileList.get(i).mType == DMFileCategoryType.E_MUSIC_CATEGORY) {
                list.add(getFullPath(mFileList.get(i)));
            }
        }
        return list;
    }

    public List<DMFile> getData() {
        return mFileList;
    }

    public void toUpperPath() {
        // 如果栈中只有个元素了。则不能继续 ? ?

        if (mDropBoxtackTrace.size() <= 1) {
            // 不能返回时的操作
            return;
        }
        // 移除栈顶
        BrowserRecord removeBrowse = removeLastBrowserRecord();
        lastBrowseIndex = removeBrowse.mSelection;

        // 刷新数据
        fillDataToList(true);
    }

    /**
     * 返回到历史浏览记录的弟index ?
     *
     * @param index 浏览记录
     */
    public void toUpperPath(int index) {
        if (index < 0 || index >= mDropBoxtackTrace.size()) {
            XLLog.log(TAG, "无效的SD卡浏览历史记 ?");
            return;
        }
        while (mDropBoxtackTrace.size() - 1 > index) {
            // 移除栈顶
            removeLastBrowserRecord();
        }
        // 刷新数据
        fillDataToList(true);
    }

    public void toUpperPathByStep(int step) {
        toUpperPath(mDropBoxtackTrace.size() - step - 1);
    }

    public String getParent(File file) {
        String parent = file.getParent();
        return parent.endsWith(File.separator) ? parent : parent
                + File.separator;
    }

    public String getCurrentPath() {
        return getLastBrowserRecord() != null ? getLastBrowserRecord().mPath
                : null;
    }

    /*
     * 得到被 ? 中的文 ? *
     */
    public List<DMFile> getSelectFiles() {
        List<DMFile> mPathList = new ArrayList<DMFile>();
        if (mFileList == null)
            return mPathList;
        for (DMFile item : mFileList) {
            if (item.selected
                    && item.mType != DMFileCategoryType.E_XLFILE_UPPER) {
                mPathList.add(item);
            }
        }
        return mPathList;
    }

    public List<BrowserRecord> getBrowserRecords() {
        return mDropBoxtackTrace.getTrace();
    }

    public void selectAllIfNeed() {
        if (mState == DropBoxEditState.STATE_EDIT) { // 删除模式下，文件和文件夹都可 ?
            for (int i = 0; i < mFileList.size(); i++) {
                DMFile file = mFileList.get(i);
                if (file.mType != DMFileCategoryType.E_XLFILE_UPPER) {
                    file.selected = true;
                }
            }
        } else if (mState == DropBoxEditState.STATE_SHARE) { // 分享模式下，只有文件可 ? 中，文件夹不可选中
            for (int i = 0; i < mFileList.size(); i++) {
                DMFile file = mFileList.get(i);
                if (!file.isDir()
                        && file.mType != DMFileCategoryType.E_XLFILE_UPPER) {
                    file.selected = true;
                }
            }
        } else { // 非编辑模式下，XXXXXX没得 ?

        }
    }

    public boolean isSdCardPage() {

        List<BrowserRecord> records = getBrowserRecords();
        String str = "";
        if (!records.isEmpty())
            str = ((BrowserRecord) records.get(0)).mPath;
        return str == "#*multsdcard@!~";
    }

    public boolean isAllSelected() {
        if (mState == DropBoxEditState.STATE_EDIT) { // 删除模式下， ?有文件和文件夹都选中了，才叫全 ? 了
            for (int i = 0; i < mFileList.size(); i++) {
                DMFile file = mFileList.get(i);
                if (!file.selected
                        && file.mType != DMFileCategoryType.E_XLFILE_UPPER) { //
                    return false;
                }
            }
            return true;
        } else if (mState == DropBoxEditState.STATE_SHARE) { // 分享模式下，只要 ?有文件全部 ? 中了，就算全 ? 了；因为目录不可 ?  ??
            for (int i = 0; i < mFileList.size(); i++) {
                DMFile file = mFileList.get(i);
                if (!file.isDir() && !file.selected
                        && file.mType != DMFileCategoryType.E_XLFILE_UPPER) { // 目录则跳过判定；如果是文件，判定是否选中，如果没有 ? 中，则直接返回false;
                    return false;
                }
            }
            return true;
        } else { // 非编辑模式下，XXXXXX没得 ?
            return false;
        }
    }

    public boolean isCanToUpper() {
        return mDropBoxtackTrace.size() > 1;
    }

    /**
     * 执行获取这一层目录的文件
     *
     * @param item
     */
    public void gotoSubPatg(DMFile item) {
        // 保存当前目录下，屏幕顶部显示的文件的position，用户点“返回上级 ? 的时 ? ，用户恢复状 ?  ??
        int pos = 0;
        int firstVisiblePosition = mList.getRefreshableView()
                .getFirstVisiblePosition();
        addBrowserRecord(item.getPath(), firstVisiblePosition);
        // 刷新视图
        fillDataToList(true);
        mList.getRefreshableView().setSelection(0);
    }

    private static class BrowserRecord {
        public String mPath;
        public int mSelection;
    }

    private static class BrowserStack {
        private final ArrayList<BrowserRecord> mDropBoxtackTrace = new ArrayList<BrowserRecord>();

        public void addBrowserRecord(String path, int y) {
            BrowserRecord br = new BrowserRecord();
            br.mPath = path;
            br.mSelection = y;
            mDropBoxtackTrace.add(br);
        }

        private BrowserRecord removeLastBrowserRecord() {
            if (mDropBoxtackTrace.size() > 0) {
                return mDropBoxtackTrace.remove(mDropBoxtackTrace.size() - 1);
            } else {
                return null;
            }
        }

        private BrowserRecord getLastBrowserRecord() {
            if (mDropBoxtackTrace.size() > 0) {
                return mDropBoxtackTrace.get(mDropBoxtackTrace.size() - 1);
            } else {
                return null;
            }
        }

        private String getLastBrowserRecordPath() {
            if (mDropBoxtackTrace.size() > 0) {
                BrowserRecord rec = mDropBoxtackTrace.get(mDropBoxtackTrace
                        .size() - 1);
                String path = null;
                if (rec != null) {
                    path = rec.mPath.endsWith(File.separator) ? rec.mPath
                            : rec.mPath + File.separator;
                }
                return path;
            } else {
                return null;
            }
        }

        public void clearAllBrowserRecord() {
            mDropBoxtackTrace.clear();
        }

        public int size() {
            return mDropBoxtackTrace.size();
        }

        public ArrayList<BrowserRecord> getTrace() {
            return mDropBoxtackTrace;
        }
    }

    public void addBrowserRecord(String path, int y) {
        mDropBoxtackTrace.addBrowserRecord(path, y);
    }

    private BrowserRecord removeLastBrowserRecord() {
        return mDropBoxtackTrace.removeLastBrowserRecord();
    }

    private BrowserRecord getLastBrowserRecord() {
        return mDropBoxtackTrace.getLastBrowserRecord();
    }

    private String getLastBrowserRecordPath() {
        return mDropBoxtackTrace.getLastBrowserRecordPath();
    }

    public void clearAllBrowserRecord() {
        mDropBoxtackTrace.clearAllBrowserRecord();
    }

    public final class ViewHolder {
        public ImageView mPlaying;
        public ImageView mFileIcon;
        public TextView mFileName;
        public TextView mFileSize;
        public TextView mFileDate;
        public ImageView mSelectedButton;
        public ImageView mGoImage;
    }

    /*
     * 文件列表的adapter *
     */
    public class MyFileAdaper extends BaseAdapter {
        private LayoutInflater layoutInflater;
        private int photoWidth;
        private int photoHeight;

        public MyFileAdaper() {
            layoutInflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            photoWidth = DipPixelUtil.dip2px(getContext(), 40);
            photoHeight = DipPixelUtil.dip2px(getContext(), 40);
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
                convertView = layoutInflater.inflate(R.layout.file_item, null);
                holder = new ViewHolder();
                holder.mPlaying = (ImageView) convertView
                        .findViewById(R.id.iv_playing);
                holder.mFileIcon = (ImageView) convertView
                        .findViewById(R.id.iv_icon);
                holder.mFileName = (TextView) convertView
                        .findViewById(R.id.tv_file_name);
                holder.mFileSize = (TextView) convertView
                        .findViewById(R.id.tv_file_size);
                holder.mFileDate = (TextView) convertView
                        .findViewById(R.id.tv_file_date);
                holder.mSelectedButton = (ImageView) convertView
                        .findViewById(R.id.cb_file);
                holder.mGoImage = (ImageView) convertView
                        .findViewById(R.id.img_arrow);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            TextView mUpperText = (TextView) convertView
                    .findViewById(R.id.tv_upper_name);
            // 填充data
            final DMFile item = (DMFile) getItem(position);

            // 根据不同类型，设置图 ?
            DMFileCategoryType type = item.mType;
            if (type == DMFileCategoryType.E_XLFILE_UPPER) {
                // 返回上一 ?
                mUpperText.setVisibility(View.VISIBLE);
                holder.mFileName.setVisibility(View.GONE);
                holder.mFileDate.setVisibility(View.GONE);

                mUpperText.setText(item.mPath);
                holder.mFileIcon.setImageResource(R.drawable.file_manage_up);
                holder.mFileName.setText(item.mPath);
                holder.mSelectedButton.setVisibility(View.GONE);
                holder.mFileDate.setVisibility(GONE);
                holder.mFileSize.setVisibility(GONE);
            } else {
                // 返回上一层消 ?
                mUpperText.setVisibility(View.GONE);
                holder.mFileName.setVisibility(View.VISIBLE);
                holder.mSelectedButton.setVisibility(View.VISIBLE);
                holder.mFileDate.setVisibility(VISIBLE);
                holder.mFileSize.setVisibility(VISIBLE);
                if (type == DMFileCategoryType.E_PICTURE_CATEGORY) {
                    // 这里显示图片
                    updatePicIcons(holder.mFileIcon, item);
                } else {
                    // 取消之前的引用下 ?
                    imageLoader.cancelDisplayTask(holder.mFileIcon);
                    int iconId = FileUtil.getFileLogo(item);
                    holder.mFileIcon.setImageResource(iconId);
                }

                boolean isPlaying = mAodPlayer.getIsPlaying();
                String playPath = AodPlayer.getInstance().getCurPlayPath();
                if (isPlaying && playPath != null
                        && playPath.equals(getFullPath(item))) {
                    holder.mPlaying.setVisibility(View.VISIBLE);
                } else {
                    holder.mPlaying.setVisibility(View.INVISIBLE);
                }

                String name = item.getName();
                holder.mFileName.setText(name);
                // 文件( ?)的最后修改时
                holder.mFileDate.setText(item.getLastModified("yyyy-MM-dd"));

				/*
                 * 文件大小，如果是文件夹就不显 ? *
				 */
                if (!item.isDir()) {
                    holder.mGoImage.setVisibility(View.GONE);
                    holder.mFileSize.setVisibility(View.VISIBLE);
                    String fileSizeStr = ConvertUtil.convertFileSize(
                            item.mSize, 2);
                    holder.mFileSize.setText(fileSizeStr);
                } else {
                    holder.mGoImage.setVisibility(View.VISIBLE);
                    // 是个目录
                    if (item instanceof DMDir) {
                        DMDir dir = (DMDir) item;
                        if (dir.mIsSDCardPath) { // 是SD卡目录,则要显示SD卡的可用空间和总空间
                            DMStorage storage = (DMStorage) item;
                            holder.mFileSize.setVisibility(View.VISIBLE);
                            holder.mFileDate.setVisibility(View.GONE); // 不显示最后修改时间

                            holder.mFileSize
                                    .setText(mContext
                                            .getString(R.string.DM_Capacity_Sum_Size)
                                            + String.format(
                                            "%.2f",
                                            (storage.total * 1.0) / 1024 / 1024)
                                            + "GB，"
                                            + mContext
                                            .getString(R.string.DM_Capacity_Free_Size)
                                            + String.format(
                                            "%.2f",
                                            (storage.free * 1.00) / 1024 / 1024)
                                            + "GB");

                            holder.mFileIcon
                                    .setImageResource(R.drawable.storage_internal);
                        }
                    } else {
                        holder.mFileSize.setVisibility(View.GONE); // 不显示文件大小
                        holder.mFileDate.setVisibility(View.VISIBLE); // 显示最后修改时间
                    }
                }

                // 判断是否是编辑模 ?, 发 ? 文件时文件夹禁止 ? 中
                if (mState == DropBoxEditState.STATE_EDIT
                        || (mState == DropBoxEditState.STATE_SHARE && !item
                        .isDir())) {
                    holder.mSelectedButton.setVisibility(View.VISIBLE);
                    holder.mSelectedButton.setSelected(item.selected);
                    holder.mGoImage.setVisibility(View.GONE);
                } else {
                    holder.mSelectedButton.setVisibility(View.GONE);
                }
            }

            return convertView;
        }

        /*
         * 使用第三方库 显示图片
         */
        private void updatePicIcons(ImageView iconview, DMFile item) {
            String uri = getFullPath(item);

            // if (item.mLocation == DMFile.LOCATION_UDISK) {
            // uri = FileInfoUtils.encodeUri(getFullPath(item));
            // }
            // 直接显示图片
            imageLoader.displayImage(uri, item.mSize, iconview, mLoaderOptions,
                    null);
        }
    }

    public void loadFiles() {
        // TODO Auto-generated method stub

        String path = mDropBoxtackTrace.getLastBrowserRecordPath();
        if (path == null) {
            path = "/";
            mDropBoxtackTrace.addBrowserRecord(path, 0);
            mRootPath = path;
        }
        fillDataToList(true);
    }

    public String getRelativePath(String path) {

        List<BrowserRecord> record = getBrowserRecords();
        String root = mRootPath;
        if (!record.isEmpty()) {
            root = record.get(0).mPath;
            // //System.out.println("rrr 22:"+root);
        }

        if (root != null && root.length() > 0) {

            if (root.charAt(root.length() - 1) == '/') {
                root = root.substring(0, root.length() - 1);
            }
            path = path.replaceFirst(root, "");
        }

        if (path.length() > 1 && path.startsWith("/")) {
            path = path.substring(1);
        }

        return path;
    }

    @Override
    public List<DMFile> getSelectedFiles() {
        // TODO Auto-generated method stub
        return getSelectFiles();
    }

    @Override
    public void reloadItems() {
        // TODO Auto-generated method stub
        // refreshFileListView();
        this.fillDataToList(true);
    }

    @Override
    public void selectAll() {
        FileManager.selectAll(mFileList, null);
        notifyDataSetChanged();
        if (onDropBoxDirViewStateChangeListener != null) {
            onDropBoxDirViewStateChangeListener.onChange(mState,
                    getLastBrowserRecordPath(), mFileList);
        }
    }

    @Override
    public void unselectAll() {
        FileManager.unselectAll(mFileList);
        notifyDataSetChanged();
        if (onDropBoxDirViewStateChangeListener != null) {
            onDropBoxDirViewStateChangeListener.onChange(mState,
                    getLastBrowserRecordPath(), mFileList);
        }
    }

    @Override
    public void switchMode(DropBoxEditState state) {
        // TODO Auto-generated method stub
        mState = state;
        notifyDataSetChanged();
    }

    public void initAudioPlayer() {
        // TODO Auto-generated method stub
        Intent mIntent = new Intent(mContext, MainActivity.class);
        mAodPlayer.setIntent(mIntent);// 放在setmOnAodPlayerStatusListener之后
    }

    public void attachAvodListener() {

        coookie_AodPlayer = mAodPlayer
                .attachListener(new MusicPlayerListener() {

                    @Override
                    public void onProgressChanged(String filePath,
                                                  int duration, int position) {
                        // TODO Auto-generated method stub
                    }

                    @Override
                    public void onPlayStateChanged(int state) {
                        // TODO Auto-generated method stub
                        System.out.println("mumu onPlayStateChanged:" + state);
                        // 停止播放，去掉播放标 ?
                        if (state == 4) {
                            notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onPlayFileChanged(String filePath) {
                        // TODO Auto-generated method stub
                        System.out
                                .println("mumu onPlayFileChanged:" + filePath);
                        notifyDataSetChanged();
                    }
                });

        cookie_status = mAodPlayer
                .addAodPlayerStatusListener(new OnAodPlayerStatusListener() {

                    @Override
                    public void status(int status) {
                        // TODO Auto-generated method stub
                        if (onDropBoxMusicChangeListener != null) {
                            onDropBoxMusicChangeListener.onMusicChange(status,
                                    false);
                        }
                    }
                });
    }

    public void removeAvodListener() {
        mAodPlayer.removeListener(coookie_AodPlayer);
        mAodPlayer.removeAodPlayerStatusListener(cookie_status);
    }

    public class MusicDialogChange {
        private int state = -1;
        private boolean shown;

        public MusicDialogChange(int state, boolean shown) {
            super();
            this.state = state;
            this.shown = shown;
        }

        public int getState() {
            return state;
        }

        public void setState(int state) {
            this.state = state;
        }

        public boolean getShown() {
            return shown;
        }

        public void setShown(boolean shown) {
            this.shown = shown;
        }

    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view,
                                   int position, long id) {
        // TODO Auto-generated method stub
        if (mState != DropBoxEditState.STATE_NORMAL) { // 普 ? 不是下长按才有反应 ?
            // 编辑模式下不能长 ?
            return false;
        }

        if (mRootPath.equals(getLastBrowserRecord().mPath)
                && curFileType != MainFragment.FILE_TYPE_DOWNLOAD
                && curFileType != DMFileCategoryType.E_VIDEO_CATEGORY.ordinal()
                && curFileType != DMFileCategoryType.E_PICTURE_CATEGORY
                .ordinal()
                && curFileType != DMFileCategoryType.E_MUSIC_CATEGORY.ordinal()
                && curFileType != DMFileCategoryType.E_BOOK_CATEGORY.ordinal()) {
            return false;
        }

        switchMode(DropBoxEditState.STATE_EDIT);

        DMFile item = mFileList.get(position - 1);
        item.setSelected(true);

        if (mOnFileItemClickListener != null) {
            mOnFileItemClickListener.onFileLongClick(position);
        }
        return true;
    }

    /**
     * 这里还是抽象出来的公用接口，由不同的子类去实现
     */

    public abstract List<DMFile> listFile(int curFileType, String curPath)
            throws Exception;

    public abstract String getFullPath(DMFile file);

    public abstract void doDownload(DMFile file, ProgressDialog dialog);

    public abstract boolean openFile(DMFile file, Activity context);

    public abstract void openPicture(Context context,
                                     ArrayList<DMFile> fileList, int index);

    public abstract void openMusic(final DMFile file);

}

