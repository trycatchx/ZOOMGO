package com.dmsys.airdiskpro.view;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
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
import android.widget.Toast;

import com.dm.baselib.BaseValue;
import com.dmairdisk.aodplayer.api.AodPlayer;
import com.dmairdisk.aodplayer.api.AodPlayer.OnAodPlayerStatusListener;
import com.dmairdisk.aodplayer.impl.MediaPlayerImpl.MusicPlayerListener;
import com.dmsys.airdiskpro.RxBus;
import com.dmsys.airdiskpro.filemanager.EditState;
import com.dmsys.airdiskpro.filemanager.FileManager;
import com.dmsys.airdiskpro.filemanager.FileOperationHelper;
import com.dmsys.airdiskpro.model.DirViewStateChangeEvent;
import com.dmsys.airdiskpro.ui.MainActivity;
import com.dmsys.airdiskpro.ui.MainFragment;
import com.dmsys.airdiskpro.ui.MountPcActivity;
import com.dmsys.airdiskpro.utils.ConvertUtil;
import com.dmsys.airdiskpro.utils.DipPixelUtil;
import com.dmsys.airdiskpro.utils.FileInfoUtils;
import com.dmsys.airdiskpro.utils.FileUtil;
import com.dmsys.airdiskpro.utils.HandlerUtil;
import com.dmsys.airdiskpro.utils.XLLog;
import com.dmsys.dmsdk.DMSdk;
import com.dmsys.dmsdk.model.DMDir;
import com.dmsys.dmsdk.model.DMFile;
import com.dmsys.dmsdk.model.DMFileCategoryType;
import com.dmsys.dmsdk.model.DMFilePage;
import com.dmsys.dmsdk.model.DMRet;
import com.dmsys.dmsdk.model.DMStorage;
import com.dmsys.dmsdk.model.DMStorageInfo;
import com.dmsys.dmsdk.model.DMSupportFunction;
import com.dmsys.dmsdk.model.DMVaultPath;
import com.dmsys.dmsdk.util.DMFileTypeUtil;
import com.dmsys.mainbusiness.R;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import cn.dm.longsys.library.imageloader.core.DMImageLoader;
import cn.dm.longsys.library.imageloader.core.DisplayImageOptions;
import cn.dm.longsys.library.imageloader.core.assist.FailReason;
import cn.dm.longsys.library.imageloader.core.listener.ImageLoadingListener;
import cn.dm.longsys.library.imageloader.core.listener.PauseOnScrollListener;
import cn.dm.longsys.library.imageloader.db.DMImageLoderVideoInfoDB;
import cn.dm.longsys.library.imageloader.db.VideoInfoBean;
import de.greenrobot.event.EventBus;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class FileManagerDirView extends FrameLayout implements
        AdapterView.OnItemClickListener,
        OnItemLongClickListener, IFileExplorer {

    public interface OnFileItemClickListener {
        public boolean onFileClick(int position);

        public boolean onFileLongClick(int position);
    }

    public interface OnDirViewStateChangeListener {
        public void onChange(int state, String currentPath,
                             List<DMFile> fileList);
    }

    public abstract interface Onload {
        public abstract void begin();

        public abstract void end();
    }

    private OnFileItemClickListener mOnFileItemClickListener;
    private Onload mOnLoad;

    private int mState = EditState.STATE_NORMAL;

    private Activity mContext;
    private List<DMFile> mFileList;
    private MyFileAdaper mAdapter;

    private final String TAG = getClass().getSimpleName();

    private String mRootPath;

    private AodPlayer mAodPlayer;
    private long coookie_AodPlayer, cookie_status;

    private PullToRefreshListView mList;

    private HandlerUtil.StaticHandler mHandler;
    private MyMessageListener mMessageListener = new MyMessageListener();

    private LayoutInflater mInflater;
    private final int MSG_LOAD_FILELIST = HandlerUtil.generateId();
    private final int MSG_LOAD_MORE = HandlerUtil.generateId();
    private DMImageLoader imageLoader = DMImageLoader.getInstance();
    private DisplayImageOptions mLoaderOptions, mVideoLoaderOptions;
    private BrowserStack mUdiskStackTrace = new BrowserStack();
    private View mEmptyLayout;
    private TextView mEmptyText;
    private ImageView mEmptyImageView;
    private DMStorageInfo mStorageInfo;

    private boolean mountPc = false;

    private int lastBrowseIndex = 0;

    private boolean mCancelCache = false;

    private int curFileType = 0;
    private CompositeSubscription mCompositeSubscription;


    private class LoadResult {
        String path;
        List<DMFile> list;
    }

    public FileManagerDirView(Context context) {
        super(context);
        mContext = (Activity) context;
        initView();
    }

    public FileManagerDirView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = (Activity) context;
        initView();
    }

    public FileManagerDirView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = (Activity) context;
        initView();
    }

    private void initView() {
        mInflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = mInflater.inflate(
                R.layout.filemanager_typer_dir_explorer_view, null);
        mEmptyLayout = view.findViewById(R.id.emptyRl);
        mEmptyText = (TextView) view.findViewById(R.id.emptyTextView);
        mEmptyImageView = (ImageView) view.findViewById(R.id.emptyImageView);

        mList = (PullToRefreshListView) view.findViewById(R.id.list);

        mList.setOnRefreshListener(new OnRefreshListener<ListView>() {

            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                // TODO Auto-generated method stub
                fillDataToList(false);
            }
        });

        mList.setOnScrollListener(new PauseOnScrollListener(imageLoader, false,
                true));

        mAodPlayer = AodPlayer.getInstance();

        initImageLoader();
        initVideoLoader();
        addView(view);
    }

    public void setOnFileItemClickListener(OnFileItemClickListener listener) {
        mOnFileItemClickListener = listener;
    }

    public void setOnloadListener(Onload onload) {
        mOnLoad = onload;
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
                String path = mUdiskStackTrace.getLastBrowserRecordPath();
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

            if (mountPc) {
                Intent intent = new Intent(mContext, MountPcActivity.class);
                mContext.startActivity(intent);
            } else {
                if (path == null || path.equals("/")) {
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
                    if (curFileType == MainFragment.FILE_TYPE_PATHSELECT) {
                        mEmptyText.setText(R.string.DM_No_Sub_File);
                    } else {
                        mEmptyText.setText(R.string.DM_No_File);
                    }
                }

            }
        }

        EventBus.getDefault().post(
                new DirViewStateChangeEvent(mState, path, mFileList));

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

        final String curPath = mUdiskStackTrace.getLastBrowserRecordPath();
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
                System.out.println("liu cur:" + curPath);

                LoadResult result = new LoadResult();
                result.path = curPath;

                if (curFileType == MainFragment.FILE_TYPE_DOWNLOAD) {

                    list = getDownloadFileData(curPath);

                    if (list != null && list.size() > 1) {
                        FileUtil.sortFileListByName(list);
                    }

                } else if (curFileType == MainFragment.FILE_TYPE_AIRDISK
                        || curFileType == MainFragment.FILE_TYPE_PATHSELECT
                        ) {

                    if (curPath.equals("/")) {

                        DMStorageInfo info;
                        int reTry = 0;
                        do {
                            info = mStorageInfo = DMSdk.getInstance()
                                    .getStorageInfo();
                        } while (info != null
                                && info.errorCode == DMRet.ERROR_NETWORK
                                && ++reTry <= 3);

                        if (info != null) {
                            if (info.getMountStatus() == 1) {
                                System.out.println("not mountPc");
                                mountPc = false;
                                if (info != null && info.getStorages() != null
                                        && info.getStorages().size() > 0) {
                                    for (DMStorage storage : info.getStorages()) {
                                        list.add(storage);
                                    }
                                }
                            } else if (info.getMountStatus() == 0) {
                                System.out.println("mountPc");
                                mountPc = true;
                            }
                        }

                    } else {

                        DMFilePage mDMFilePage;
                        int reTry = 0;
                        do {
                            mDMFilePage = DMSdk.getInstance().getFilePage(
                                    curPath);
                        } while (mDMFilePage != null
                                && mDMFilePage.errorCode == DMRet.ERROR_NETWORK
                                && ++reTry <= 3);

                        if (mDMFilePage != null) {
                            list = mDMFilePage.getFiles();
                        }

                        if (list == null) {
                            list = new ArrayList<>();
                        } else if (curFileType == MainFragment.FILE_TYPE_PATHSELECT) {
                            list = getUdiskFolderFromList(list);
                        }

                        long time1 = System.currentTimeMillis();
                        //隐藏保险库
                        if (list != null && list.size() > 0 && DMSupportFunction.isSupportVault(DMSdk.getInstance()
                                .getSupportFunction())) {
                            for (DMFile d : list) {
                                if (d.isDir && d.getPath().equals(BaseValue.ValutPath)) {
                                    list.remove(d);
                                    break;
                                }
                            }
                        }
                        System.out.println(" time diffrent" + (System.currentTimeMillis() - time1));
                    }

                } else if (curFileType == MainFragment.FILE_TYPE_VAULT) {
                    DMFilePage mDMFilePage;
                    int reTry = 0;
                    do {
                        mDMFilePage = DMSdk.getInstance().getEncryptFileList(
                                curPath);
                    } while (mDMFilePage != null
                            && mDMFilePage.errorCode == DMRet.ERROR_NETWORK
                            && ++reTry <= 3);

                    if (mDMFilePage != null) {
                        list = mDMFilePage.getFiles();

                    }

                } else {

                    DMFileCategoryType type = DMFileTypeUtil
                            .getFileCategoryTypeByOrdinal(curFileType);
                    list = FileOperationHelper.getInstance()
                            .getUdiskAllFilesByType(type);
                    if (list == null) {
                        list = new ArrayList<>();
                    }

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
    private List<DMFile> getDownloadFileData(String path) {
        List<DMFile> fileItemList = new ArrayList<DMFile>();
        // 获取当前栈中，栈顶元素的表示的文件路径
        String lastPath = path;

        // 如果栈顶的路径是一个正确的文件路径，则显示该路径下的文件
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
    public void init(int fileType, CompositeSubscription m) {

        this.curFileType = fileType;

        mHandler = new HandlerUtil.StaticHandler(mMessageListener);
        mAdapter = new MyFileAdaper();
        mList.setAdapter(mAdapter);
        mList.getRefreshableView().setOnItemClickListener(this);
        mList.getRefreshableView().setOnItemLongClickListener(this);
        mCompositeSubscription = m;
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
        /**
         * 这里的onFileClick由外部的return 决定是否响应item 事件，外面返回是true，说明要消耗点击事件，ctrl+T 追踪
         */
        if (null != mOnFileItemClickListener
                && mOnFileItemClickListener.onFileClick(position)) {
            return;
        }

        // 如果是文件夹打开，是文件的话打开？
        DMFile item = mFileList.get(position - 1);
        if (mState == EditState.STATE_EDIT) { // 删除模式下
            if (item.mType == DMFileCategoryType.E_XLFILE_UPPER) { // “返回上一层”点击无效
                return;
            }
            item.selected = !item.selected;
            View cb_file = view.findViewById(R.id.cb_file);
            if (cb_file != null) {
                cb_file.setSelected(item.selected);
            }
            EventBus.getDefault().post(
                    new DirViewStateChangeEvent(mState,
                            getLastBrowserRecordPath(), mFileList));

        } else if (mState == EditState.STATE_SHARE) { // 分享模式下
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
                // 文件：打开
                openFile(item, mFileList);
            } else {
                // 目录：进入目录
                gotoSubPatg(item);
            }
        }
    }

    // 打开文件，图片文件需要批量打开
    private void openFile(DMFile file) {
        openFile(file, null);
    }

    private void openFile(DMFile file, List<DMFile> list) {
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
            FileOperationHelper.getInstance().openPicture(mContext, fileList,
                    index);
        } else if (file.mType == DMFileCategoryType.E_MUSIC_CATEGORY) {
            openMusic(file);
        } else if (file.mType == DMFileCategoryType.E_UNSUPPORT_VIDEO_CATEGORY) {
            FileOperationHelper.getInstance()
                    .openUnsupportVideo(file, mContext);
        } else {
            boolean openOK = FileOperationHelper.getInstance().openFile(file,
                    mContext, list);
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

    List<String> getCurrentMusicFiles() {

        List<String> list = new ArrayList<String>();
        for (int i = 0; i < mFileList.size(); i++) {
            if (mFileList.get(i).mType == DMFileCategoryType.E_MUSIC_CATEGORY) {
                list.add(getFullPath(mFileList.get(i)));
            }
        }
        return list;
    }

    public void openMusic(DMFile file) {

        initAudioPlayer();

        List<String> list = getCurrentMusicFiles();
        mAodPlayer.setPlayList(list);
        mAodPlayer.startPlay(getFullPath(file));
        EventBus.getDefault().post(new MusicDialogChange(-1, true));
    }

    public List<DMFile> getData() {
        return mFileList;
    }

    public void toUpperPath() {
        // 如果栈中只有个元素了。则不能继续往上

        if (mUdiskStackTrace.size() <= 1) {
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
     * 返回到历史浏览记录的弟index条
     *
     * @param index 浏览记录
     */
    public void toUpperPath(int index) {
        if (index < 0 || index >= mUdiskStackTrace.size()) {
            XLLog.log(TAG, "无效的SD卡浏览历史记录");
            return;
        }
        while (mUdiskStackTrace.size() - 1 > index) {
            // 移除栈顶
            removeLastBrowserRecord();
        }
        // 刷新数据
        fillDataToList(true);
    }

    public void toUpperPathByStep(int step) {
        toUpperPath(mUdiskStackTrace.size() - step - 1);
    }

    public String getParent(File file) {
        String parent = file.getParent();
        return parent.endsWith(File.separator) ? parent : parent
                + File.separator;
    }

    public int getState() {
        return mState;
    }

    public String getCurrentPath() {
        return getLastBrowserRecord() != null ? getLastBrowserRecord().mPath
                : null;
    }

    /*
     * 得到被选中的文件 *
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
        return mUdiskStackTrace.getTrace();
    }

    public void selectAllIfNeed() {
        if (mState == EditState.STATE_EDIT) { // 删除模式下，文件和文件夹都可选
            for (int i = 0; i < mFileList.size(); i++) {
                DMFile file = mFileList.get(i);
                if (file.mType != DMFileCategoryType.E_XLFILE_UPPER) {
                    file.selected = true;
                }
            }
        } else if (mState == EditState.STATE_SHARE) { // 分享模式下，只有文件可选中，文件夹不可选中
            for (int i = 0; i < mFileList.size(); i++) {
                DMFile file = mFileList.get(i);
                if (!file.isDir()
                        && file.mType != DMFileCategoryType.E_XLFILE_UPPER) {
                    file.selected = true;
                }
            }
        } else { // 非编辑模式下，XXXXXX没得选

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
        if (mState == EditState.STATE_EDIT) { // 删除模式下，所有文件和文件夹都选中了，才叫全选了
            for (int i = 0; i < mFileList.size(); i++) {
                DMFile file = mFileList.get(i);
                if (!file.selected
                        && file.mType != DMFileCategoryType.E_XLFILE_UPPER) { //
                    return false;
                }
            }
            return true;
        } else if (mState == EditState.STATE_SHARE) { // 分享模式下，只要所有文件全部选中了，就算全选了；因为目录不可选。
            for (int i = 0; i < mFileList.size(); i++) {
                DMFile file = mFileList.get(i);
                if (!file.isDir() && !file.selected
                        && file.mType != DMFileCategoryType.E_XLFILE_UPPER) { // 目录则跳过判定；如果是文件，判定是否选中，如果没有选中，则直接返回false;
                    return false;
                }
            }
            return true;
        } else { // 非编辑模式下，XXXXXX没得选
            return false;
        }
    }

    public boolean isCanToUpper() {
        return mUdiskStackTrace.size() > 1;
    }

    /**
     * 执行获取这一层目录的文件
     *
     * @param item
     */
    public void gotoSubPatg(DMFile item) {
        // 保存当前目录下，屏幕顶部显示的文件的position，用户点“返回上级”的时候，用户恢复状态。
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
        private final ArrayList<BrowserRecord> mUdiskStackTrace = new ArrayList<BrowserRecord>();

        private void addBrowserRecord(String path, int y, int index) {
            BrowserRecord br = new BrowserRecord();
            br.mPath = path;
            br.mSelection = y;
            mUdiskStackTrace.add(index, br);
        }

        public void addBrowserRecord(String path, int y) {
            BrowserRecord br = new BrowserRecord();
            br.mPath = path;
            br.mSelection = y;
            mUdiskStackTrace.add(br);
        }

        private BrowserRecord removeLastBrowserRecord() {
            if (mUdiskStackTrace.size() > 0) {
                return mUdiskStackTrace.remove(mUdiskStackTrace.size() - 1);
            } else {
                return null;
            }
        }

        private void saveCurrentRecodeStatu(int y) {
            if (mUdiskStackTrace.size() > 0) {
                mUdiskStackTrace.get(mUdiskStackTrace.size() - 1).mSelection = y;
            }
        }

        private BrowserRecord getLastBrowserRecord() {
            if (mUdiskStackTrace.size() > 0) {
                return mUdiskStackTrace.get(mUdiskStackTrace.size() - 1);
            } else {
                return null;
            }
        }

        private String getLastBrowserRecordPath() {
            if (mUdiskStackTrace.size() > 0) {
                BrowserRecord rec = mUdiskStackTrace.get(mUdiskStackTrace
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
            mUdiskStackTrace.clear();
        }

        public int size() {
            return mUdiskStackTrace.size();
        }

        public ArrayList<BrowserRecord> getTrace() {
            return mUdiskStackTrace;
        }
    }

    private void addBrowserRecord(String path, int y, int index) {
        mUdiskStackTrace.addBrowserRecord(path, y, index);
    }

    public void addBrowserRecord(String path, int y) {
        mUdiskStackTrace.addBrowserRecord(path, y);
    }

    private BrowserRecord removeLastBrowserRecord() {
        return mUdiskStackTrace.removeLastBrowserRecord();
    }

    private void saveCurrentRecodeStatu(int y) {
        mUdiskStackTrace.saveCurrentRecodeStatu(y);
    }

    private BrowserRecord getLastBrowserRecord() {
        return mUdiskStackTrace.getLastBrowserRecord();
    }

    private String getLastBrowserRecordPath() {
        return mUdiskStackTrace.getLastBrowserRecordPath();
    }

    public void clearAllBrowserRecord() {
        mUdiskStackTrace.clearAllBrowserRecord();
    }

    public final class ViewHolder {
        public ImageView mPlaying;
        public ImageView mFileIcon;
        public TextView mFileName;
        public TextView mFileSize;
        public TextView mFileDate;
        public ImageView mSelectedButton;
        public ImageView mGoImage;
        //xinzeng
        public TextView mFileComment;
        public TextView tv_upper_name;

        public ViewHolder(View convertView) {
            mPlaying = (ImageView) convertView
                    .findViewById(R.id.iv_playing);
            mFileIcon = (ImageView) convertView
                    .findViewById(R.id.iv_icon);
            mFileName = (TextView) convertView
                    .findViewById(R.id.tv_file_name);
            mFileSize = (TextView) convertView
                    .findViewById(R.id.tv_file_size);
            mFileDate = (TextView) convertView
                    .findViewById(R.id.tv_file_date);
            mSelectedButton = (ImageView) convertView
                    .findViewById(R.id.cb_file);
            mGoImage = (ImageView) convertView
                    .findViewById(R.id.img_arrow);
            tv_upper_name = (TextView) convertView
                    .findViewById(R.id.tv_upper_name);

            mFileComment = (TextView) convertView
                    .findViewById(R.id.tv_file_comment);

        }
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

                convertView = layoutInflater.inflate(R.layout.file_item, parent, false);

                holder = new ViewHolder(convertView);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.mFileComment.setTag(position);
            holder.mFileComment.setVisibility(View.GONE);
            TextView mUpperText = holder.tv_upper_name;
            // 填充data
            final DMFile item = (DMFile) getItem(position);

            // 根据不同类型，设置图标
            DMFileCategoryType type = item.mType;
            if (type == DMFileCategoryType.E_XLFILE_UPPER) {
                // 返回上一级
                mUpperText.setVisibility(View.GONE);
                holder.mFileName.setVisibility(View.GONE);
                holder.mFileDate.setVisibility(View.GONE);

                mUpperText.setText(item.mPath);
                holder.mFileIcon.setImageResource(R.drawable.file_manage_up);
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
                if (type == DMFileCategoryType.E_PICTURE_CATEGORY || type == DMFileCategoryType.E_VIDEO_CATEGORY) {
                    // 这里显示图片
                    updatePicIcons(position, holder.mFileIcon, holder.mFileComment, item, type);
                } else {
                    // 取消之前的引用下载
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
                // 文件(夹)的最后修改时间
                holder.mFileDate.setText(item.getLastModified("yyyy-MM-dd"));

                if (item.mHidden) {
                    holder.mFileIcon.setAlpha(50);
                } else {
                    holder.mFileIcon.setAlpha(255);
                }

				/*
                 * 文件大小，如果是文件夹就不显示 *
				 */
                if (!item.isDir()) {
                    holder.mGoImage.setVisibility(View.GONE);
                    holder.mFileSize.setVisibility(View.VISIBLE);
                    String fileSizeStr = ConvertUtil.convertFileSize(
                            item.mSize, 2);
                    holder.mFileSize.setText(fileSizeStr);
                } else {

                    // 是个目录
                    DMDir dir = (DMDir) item;
                    holder.mGoImage.setVisibility(View.VISIBLE);
                    // System.out.println("liu path2:"+path);
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

                    } else { // 非SD卡目录
                        holder.mFileSize.setVisibility(View.GONE); // 不显示文件大小
                        holder.mFileDate.setVisibility(View.VISIBLE); // 显示最后修改时间
                    }
                }

                // 判断是否是编辑模式, 发送文件时文件夹禁止选中
                if (mState == EditState.STATE_EDIT
                        || (mState == EditState.STATE_SHARE && !item.isDir())) {
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
        private void updatePicIcons(final int position, ImageView iconview, final TextView mFileComment, final DMFile item, DMFileCategoryType type) {
            String uri = getFullPath(item);

            if (item.mLocation == DMFile.LOCATION_UDISK) {
                uri = FileInfoUtils.encodeUri(getFullPath(item));
            }
            // 直接显示图片
            if (type == DMFileCategoryType.E_PICTURE_CATEGORY) {
                imageLoader.displayImage(uri, item.mSize, iconview, mLoaderOptions,
                        null);
            } else {
                imageLoader.displayImage(uri, item.mSize, iconview, mVideoLoaderOptions,
                        new ImageLoadingListener() {
                            @Override
                            public void onLoadingStarted(String imageUri, View view) {

                            }

                            @Override
                            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {

                            }

                            @Override
                            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                                getAndSetComment(position, imageUri, mFileComment, item.mSize);

                            }

                            @Override
                            public void onLoadingCancelled(String imageUri, View view) {

                            }
                        });
            }
        }
    }


    private void getAndSetComment(final int position, final String imageUri, final TextView mFileComment, final long size) {
        Subscription subscription = Observable.fromCallable(new Callable<VideoInfoBean>() {

            @Override
            public VideoInfoBean call() {


                return DMImageLoderVideoInfoDB.getInstance().getVideoInfo(imageUri, size);

            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<VideoInfoBean>() {
                    @Override
                    public void call(VideoInfoBean tmp) {

                        if (tmp != null && (int) mFileComment.getTag() == position) {
                            mFileComment.setText(tmp.comment);
                            mFileComment.setVisibility(View.VISIBLE);
                        }


                    }
                });
        mCompositeSubscription.add(subscription);
    }

    private String getFullPath(DMFile file) {
        if (file.mLocation == DMFile.LOCATION_UDISK) {
            return "http://" + BaseValue.Host + File.separator + file.mPath;
        } else {
            return "file://" + file.mPath;
        }

    }

    public void goRootDir() {
        mUdiskStackTrace.clearAllBrowserRecord();
        loadFiles();
    }

    public void loadFiles() {
        // TODO Auto-generated method stub

        String path = mUdiskStackTrace.getLastBrowserRecordPath();
        System.out.println("loadFiles path:" + path);
        //先loading
        if (mOnLoad != null) {
            mOnLoad.begin();
        }
        if (path == null) {
            if (curFileType == MainFragment.FILE_TYPE_DOWNLOAD) {
                path = FileOperationHelper.getInstance().getDownloadPath();
            } else if (curFileType == MainFragment.FILE_TYPE_VAULT) {
                loadingVaultRootFile();
                return;
            } else {
                path = "/";
            }
            mUdiskStackTrace.addBrowserRecord(path, 0);
            mRootPath = path;
        }
        fillDataToList(true);
    }


    private void loadingVaultRootFile() {


        Subscription subscription = Observable.fromCallable(new Callable<DMVaultPath>() {

            @Override
            public DMVaultPath call() {
                return DMSdk.getInstance().getVaultPath();
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<DMVaultPath>() {
                    @Override
                    public void call(DMVaultPath tmp) {
                        if (tmp != null && tmp.errorCode == DMRet.ACTION_SUCCESS) {
                            String path = tmp.path.replace("/tmp/mnt/", "");

                            mUdiskStackTrace.addBrowserRecord(path, 0);
                            mRootPath = path;
                        }
                        fillDataToList(true);

                    }
                });
        mCompositeSubscription.add(subscription);
    }


    public String getRelativePath(String path) {

        List<BrowserRecord> record = getBrowserRecords();
        String root = mRootPath;
        System.out.println("rrr 11:" + root);
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

        if (curFileType == MainFragment.FILE_TYPE_DOWNLOAD
                || curFileType == MainFragment.FILE_TYPE_VAULT) {
            if (path.length() > 1 && path.startsWith("/")) {
                path = path.substring(1);
            }
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
        this.fillDataToList(true);
    }

    @Override
    public void selectAll() {
        FileManager.selectAll(mFileList, null);
        notifyDataSetChanged();
        EventBus.getDefault().post(
                new DirViewStateChangeEvent(mState, getLastBrowserRecordPath(),
                        mFileList));
    }

    @Override
    public void unselectAll() {
        FileManager.unselectAll(mFileList);
        notifyDataSetChanged();
        EventBus.getDefault().post(
                new DirViewStateChangeEvent(mState, getLastBrowserRecordPath(),
                        mFileList));
    }

    @Override
    public void switchMode(int state) {
        // TODO Auto-generated method stub
        if(mState == state) return;
        mState = state;
        notifyDataSetChanged();
    }

    private void initImageLoader() {
        mLoaderOptions = new DisplayImageOptions.Builder().cacheInMemory(true)
                .showImageOnFail(R.drawable.filemanager_photo_fail)
                .useThumb(true).cacheOnDisk(true)
                .showImageOnLoading(R.drawable.bt_download_manager_image)
                .showImageForEmptyUri(R.drawable.filemanager_photo_fail)
                .build();
    }

    private void initVideoLoader() {
        mVideoLoaderOptions = new DisplayImageOptions.Builder().cacheInMemory(true)
                .showImageOnFail(R.drawable.bt_download_manager_video)
                .useThumb(false)
                .cacheOnDisk(true)
                .cacheOriginal(true)
                .showImageOnLoading(R.drawable.bt_download_manager_video)
                .showImageForEmptyUri(R.drawable.bt_download_manager_video)
                .build();
    }

    private void initAudioPlayer() {
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
                        // 停止播放，去掉播放标志
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

        /**
         * 监听音乐模块有没有 存在 或者 退出
         */
        cookie_status = mAodPlayer
                .addAodPlayerStatusListener(new OnAodPlayerStatusListener() {

                    @Override
                    public void status(int status) {
                        // TODO Auto-generated method stub
                        System.out.println("mMUSIC STATUS:" + status);

                        EventBus.getDefault().post(
                                new MusicDialogChange(status, false));
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

    private double mProgress;

    private void doDownload(final DMFile file, final ProgressDialog dialog) {

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
                FileOperationHelper.getInstance().openFile(dstXLFile, mContext);
            } else {
                FileUtil.thirdPartOpen(dstXLFile, mContext);
            }

            return;
        }

        FileOperationHelper.getInstance().doDownload(file, dstFile.getParent(),
                new FileOperationHelper.ProgressListener() {

                    @Override
                    public boolean onProgressChange(final double progress) {
                        // TODO Auto-generated method stub
                        System.out.println("dirdir:" + progress);
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

                                    DMFile dstXLFile = new DMFile();
                                    dstXLFile.mName = dstFile.getName();
                                    dstXLFile.mPath = dstFile.getPath();
                                    dstXLFile.mLocation = DMFile.LOCATION_LOCAL;
                                    if (file.getType() == DMFileCategoryType.E_BOOK_CATEGORY) {
                                        FileOperationHelper.getInstance().openFile(dstXLFile, mContext);
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

    public class LongClickEvent {
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view,
                                   int position, long id) {
        // TODO Auto-generated method stub
        if (mState != EditState.STATE_NORMAL) { // 普通不是下长按才有反应， 编辑模式下不能长按
            return false;
        }

        if (mRootPath.equals(getLastBrowserRecord().mPath)
                && curFileType != MainFragment.FILE_TYPE_DOWNLOAD
                && curFileType != MainFragment.FILE_TYPE_VAULT
                && curFileType != DMFileCategoryType.E_VIDEO_CATEGORY.ordinal()
                && curFileType != DMFileCategoryType.E_PICTURE_CATEGORY
                .ordinal()
                && curFileType != DMFileCategoryType.E_MUSIC_CATEGORY.ordinal()
                && curFileType != DMFileCategoryType.E_BOOK_CATEGORY.ordinal()) {
            return false;
        }

        switchMode(EditState.STATE_EDIT);

        DMFile item = mFileList.get(position - 1);
        item.setSelected(true);
        RxBus.getDefault().send(new LongClickEvent());
        return true;
    }

    public boolean isDiskMountPc() {
        // TODO Auto-generated method stub
        if (mStorageInfo != null && mStorageInfo.getMountStatus() == 0) {
            return true;
        }
        return false;
    }

}
