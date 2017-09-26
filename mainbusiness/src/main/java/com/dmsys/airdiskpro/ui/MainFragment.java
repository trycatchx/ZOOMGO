package com.dmsys.airdiskpro.ui;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dm.baselib.BaseValue;
import com.dmairdisk.aodplayer.api.AodPlayer;
import com.dmairdisk.aodplayer.dialog.MusicPlayerDialog;
import com.dmairdisk.aodplayer.util.CommonAsync;
import com.dmairdisk.aodplayer.util.FileUtil;
import com.dmsoftwareupgrade.util.UDiskBaseDialog;
import com.dmsys.airdiskpro.RxBus;
import com.dmsys.airdiskpro.event.DeviceValutEvent;
import com.dmsys.airdiskpro.event.NewFwEvent;
import com.dmsys.airdiskpro.event.SearchEndEvent;
import com.dmsys.airdiskpro.filemanager.EditState;
import com.dmsys.airdiskpro.filemanager.FileManager;
import com.dmsys.airdiskpro.filemanager.FileOperationService;
import com.dmsys.airdiskpro.filemanager.FileOperationService.ProgressInfo;
import com.dmsys.airdiskpro.filemanager.FileOperationService.SameNameInfo;
import com.dmsys.airdiskpro.model.DirViewStateChangeEvent;
import com.dmsys.airdiskpro.utils.FileInfoUtils;
import com.dmsys.airdiskpro.utils.HandlerUtil;
import com.dmsys.airdiskpro.view.DMPopup;
import com.dmsys.airdiskpro.view.DMSortDialog;
import com.dmsys.airdiskpro.view.FileManagerDirView;
import com.dmsys.airdiskpro.view.FileManagerDirView.LongClickEvent;
import com.dmsys.airdiskpro.view.FileManagerDirView.MusicDialogChange;
import com.dmsys.airdiskpro.view.FileManagerDirView.Onload;
import com.dmsys.airdiskpro.view.FolderSelector;
import com.dmsys.airdiskpro.view.IFileExplorer;
import com.dmsys.airdiskpro.view.MessageDialog;
import com.dmsys.airdiskpro.view.UDiskEditTextDialog;
import com.dmsys.airdiskpro.view.UDiskTextViewDialog;
import com.dmsys.airdiskpro.view.UpgradeFwTaskDialog;
import com.dmsys.dmsdk.DMSdk;
import com.dmsys.dmsdk.DMSdk.DMDeviceStatusChangeListener;
import com.dmsys.dmsdk.model.DMDir;
import com.dmsys.dmsdk.model.DMFile;
import com.dmsys.dmsdk.model.DMFileCategoryType;
import com.dmsys.dmsdk.model.DMOTA;
import com.dmsys.dmsdk.model.DMRet;
import com.dmsys.dmsdk.model.DMStatusType;
import com.dmsys.dmsdk.model.DMStorageInfo;
import com.dmsys.dmsdk.model.DMSupportFunction;
import com.viewpagerindicator.TabPageIndicator;
import com.dmsys.mainbusiness.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.greenrobot.event.EventBus;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;

public class MainFragment extends BaseActionFragment implements OnClickListener {

    private View parent;

    private Activity activity;

    public static final int VIEW_DIR = 0;
    public static final int VIEW_TYPE = 1;

    public int mViewType = VIEW_DIR;

    private FileManagerDirView mFileListView;
    private ViewGroup mLoadingView;
    private FolderSelector mPathView;

    private String[] mFolderArray;
    private String mRootName;

    /**
     * 音乐
     **/
    private ImageButton ibtn_music;
    private MusicPlayerDialog mMusicPlayerDialog;

    /**
     * 顶部栏
     **/
    private FrameLayout backLayout;
    private ImageView backButton;
    private RelativeLayout normalLayout;
    private TextView selectAllText;
    private TextView mainText;
    private ImageView newTips;

    private View layout_dir;
    private View layout_type;
    private ViewPager mViewPager;
    private TabPageIndicator mTabIndicator;

    private View occupyView;

    private RelativeLayout layout_search, layout_title_more;

    private CheckBox mCheckBox;
    private Button mButton;

    private boolean mEditMode;
    private boolean mBackgroud;

    private HandlerUtil.StaticHandler mHandler;
    private FileOperationListener mOpListener;

    // private boolean isDownloadView = false;
    public static int FILE_TYPE_AIRDISK = 20;
    public static int FILE_TYPE_DOWNLOAD = 21;
    public static int FILE_TYPE_PATHSELECT = 22;
    public static int FILE_TYPE_VAULT = 23;

    private int curFileType = FILE_TYPE_AIRDISK;

    private OnEditModeChangeListener mOnEditModeChange;
    private OnEditableChange mOnEditableChange;

    // 切换allfile、分类的话，设置mFileListview 去加载分类；

    private TabsAdapter mTabsAdapter;

    private long cookie_disk;
    private boolean flag_disk;

    private CommonAsync otaTask;

    private DMFile newFolder;

    private boolean exitShown;

    private SharedPreferences preferences;

    private DMPopup mPopup;
    //RXJAVA 的请求框架
    public CompositeSubscription mSubscriptions = new CompositeSubscription();


    public static MainFragment newInstance(int type) {
        MainFragment newFragment = new MainFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("type", type);
        newFragment.setArguments(bundle);
        return newFragment;

    }

    private volatile boolean needToRefresh = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        parent = inflater.inflate(R.layout.fragment_main, null);
        Bundle args = getArguments();
        if (args != null) {
            this.curFileType = args.getInt("type");
            mViewType = VIEW_DIR;
        } else {
            mViewType = VIEW_TYPE;
        }
        initViews();


        RxBus.getDefault().toObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object tmp) {
                        if (tmp != null) {
                            if (tmp instanceof DeviceValutEvent) {
                                if (((DeviceValutEvent) tmp).ret >= 0) {
                                    needToRefresh = true;
                                } else {
                                    needToRefresh = false;
                                }
                            } else if (tmp instanceof LongClickEvent) {
                                if (!mBackgroud) {
                                    switchMode(EditState.STATE_EDIT);
                                    mainText.setText(String
                                            .format(getResources().getString(
                                                    R.string.DM_Navigation_Upload_Num), "1"));
                                }
                            }

                        }
                    }
                });


        return parent;
    }

    @Override
    public void onAttach(Activity activity) {
        // TODO Auto-generated method stub
        super.onAttach(activity);
        this.activity = activity;

    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        mBackgroud = false;
        init(activity, mHandler);

        if (mEditMode) {
            setEditState(EditState.STATE_NORMAL);
        }

        if (curFileType == FILE_TYPE_DOWNLOAD) {
            mainText.setText(R.string.DM_Bottom_Bar_Button_MyDowmloads);
        } else {
            mainText.setText(R.string.DM_APP_Name);
        }

        if (AodPlayer.getInstance().getIsPlaying()) {
            ibtn_music.setVisibility(View.VISIBLE);
        } else {
            ibtn_music.setVisibility(View.GONE);
        }

        if (needToRefresh) {
            reloadItems();
        }
        needToRefresh = true;
    }


    public interface IPager {
        void resetPage();

        IFileExplorer getFileView();
    }

    @Override
    public void onPause() {
        super.onPause();
        unInit();
    }

    ;

    @Override
    public void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
        System.out.println("mmmain onStop");
        mBackgroud = true;
    }

    public void resetFiles() {
        if (isAdded()) {
            mFileListView.goRootDir();
        }
    }

    private void initViews() {

        EventBus.getDefault().register(this);

        initTitleBar();

        initFileList();

        initFileType();

        occupyView = parent.findViewById(R.id.occupyView);
        if (curFileType == FILE_TYPE_DOWNLOAD || curFileType == FILE_TYPE_VAULT) {
            occupyView.setVisibility(View.GONE);
        }

        ibtn_music = (ImageButton) parent.findViewById(R.id.ibtn_music);
        ibtn_music.setOnClickListener(this);
        if (AodPlayer.getInstance().getIsPlaying()
                && curFileType != FILE_TYPE_PATHSELECT) {
            ibtn_music.setVisibility(View.VISIBLE);
        } else {
            ibtn_music.setVisibility(View.GONE);
        }

        mCheckBox = (CheckBox) parent.findViewById(R.id.cb_nomore);
        mCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                // TODO Auto-generated method stub
                if (isChecked) {
                    preferences.edit().putBoolean("SHOW", false).commit();
                } else {
                    preferences.edit().putBoolean("SHOW", true).commit();
                }
            }
        });

        mButton = (Button) parent.findViewById(R.id.btn_ok);
        mButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                parent.findViewById(R.id.layout_guide).setVisibility(View.GONE);
                if (MainActivity.class.isInstance(activity)) {
                    ((MainActivity) activity).setGuideBarVisible(View.VISIBLE);
                }
            }
        });

        mOpListener = new FileOperationListener();
        mHandler = new HandlerUtil.StaticHandler(mOpListener);

        cookie_disk = DMSdk.getInstance().attachListener(
                new DMDeviceStatusChangeListener() {

                    @Override
                    public void onDeviceStatusChanged(int type) {
                        // TODO Auto-generated method stub
                        if (DMStatusType.isDiskChange(type)) {
                            getDiskInfo();
                        }
                    }
                });

        preferences = activity.getSharedPreferences("USER_GUIDE",
                Context.MODE_PRIVATE);

    }

    private void getDiskInfo() {

        CommonAsync.Runnable runnable = new CommonAsync.Runnable() {

            @Override
            public void stop() {
                // TODO Auto-generated method stub

            }

            @Override
            public Object run() {
                // TODO Auto-generated method stub
                return DMSdk.getInstance().getStorageInfo();
            }
        };

        CommonAsync.CommonAsyncListener listener = new CommonAsync.CommonAsyncListener() {

            @Override
            public void onResult(Object ret) {
                // TODO Auto-generated method stub
                DMStorageInfo storageInfo = (DMStorageInfo) ret;
                if (storageInfo != null) {
                    if (storageInfo.getMountStatus() == 0) {

                        Intent intent = new Intent(activity,
                                MountPcActivity.class);
                        startActivity(intent);

                        AodPlayer.getInstance().pause();

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

        CommonAsync async = new CommonAsync(runnable, listener);
        async.executeOnExecutor((ExecutorService) Executors
                .newCachedThreadPool());
    }

    private void initFileType() {
        // TODO Auto-generated method stub
        mViewPager = (ViewPager) parent.findViewById(R.id.tab_pager);
        mViewPager.setOffscreenPageLimit(4);
        mTabsAdapter = new TabsAdapter(getActivity(), mViewPager);
        addPagers();
        mTabIndicator = (TabPageIndicator) parent.findViewById(R.id.indicator);
        mTabIndicator.setViewPager(mViewPager);
        mTabIndicator.setCurrentItem(0);

        mTabIndicator
                .setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                    @Override
                    public void onPageSelected(int index) {
                        // TODO Auto-generated method stub
                        OnCurrentPageChanged();
                    }

                    @Override
                    public void onPageScrolled(int arg0, float arg1, int arg2) {
                        // TODO Auto-generated method stub

                    }

                    @Override
                    public void onPageScrollStateChanged(int arg0) {
                        // TODO Auto-generated method stub

                    }
                });

    }

    private void initTitleBar() {
        // TODO Auto-generated method stub
        backButton = (ImageView) parent.findViewById(R.id.titlebar_left);
        backButton.setVisibility(View.VISIBLE);
        backButton.setImageResource(R.drawable.sliding_menu_button_selector);
        backLayout = (FrameLayout) parent.findViewById(R.id.layout_back);
        backLayout.setOnClickListener(this);

        normalLayout = (RelativeLayout) parent.findViewById(R.id.layout_normal);
        selectAllText = (TextView) parent.findViewById(R.id.text_selectall);
        selectAllText.setOnClickListener(this);

        layout_search = (RelativeLayout) parent
                .findViewById(R.id.layout_search);
        layout_search.setOnClickListener(this);

        layout_title_more = (RelativeLayout) parent
                .findViewById(R.id.layout_title_more);
        layout_title_more.setOnClickListener(this);

        newTips = (ImageView) parent.findViewById(R.id.iv_titlebar_new_tips);

        mainText = (TextView) parent.findViewById(R.id.titlebar_title);

        if (curFileType == FILE_TYPE_DOWNLOAD) {
            layout_search.setVisibility(View.GONE);
            mainText.setText(R.string.DM_Bottom_Bar_Button_MyDowmloads);
            backButton
                    .setImageResource(R.drawable.dm_lib_wifi_back_btn_bg_selector);

            ((ImageView) parent.findViewById(R.id.img_more))
                    .setBackgroundResource(R.drawable.edit_title_btn_selector);

        } else if (curFileType == FILE_TYPE_PATHSELECT) {
            layout_search.setVisibility(View.GONE);
            mainText.setText(R.string.DM_Navigation_Upload_Path);
            backButton
                    .setImageResource(R.drawable.dm_lib_wifi_back_btn_bg_selector);
        }

        layout_dir = parent.findViewById(R.id.layout_dir);
        layout_type = parent.findViewById(R.id.layout_type);

    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        int i = v.getId();
        if (i == R.id.ibtn_music) {
            showMusicDialog();

        } else if (i == R.id.layout_back) {
            if (mEditMode) {
                unselectAll();
                setEditState(EditState.STATE_NORMAL);
            } else {
                if (curFileType != FILE_TYPE_AIRDISK) {
                    getActivity().finish();
                } else {
                    EventBus.getDefault().post(new MainActivity.Toggle());
                }
            }

        } else if (i == R.id.text_selectall) {
            if (selectAllText.getText().equals(
                    getString(R.string.DM_Control_Select))) {
                selectAllText.setText(R.string.DM_Control_Uncheck_All);
                selectAll();
            } else {
                selectAllText.setText(R.string.DM_Control_Select);
                unselectAll();
            }

        } else if (i == R.id.layout_search) {
            Intent intent = new Intent(activity, FileSearchActivity.class);
            intent.putExtra("path", getCurrentPath());
            startActivity(intent);

        } else if (i == R.id.layout_title_more) {
            if (curFileType == FILE_TYPE_DOWNLOAD) {
                setEditState(EditState.STATE_EDIT);
            } else {
                showMoreDialog();
            }

        } else if (i == R.id.item_edit) {
            mPopup.dismiss();
            setEditState(EditState.STATE_EDIT);

        } else if (i == R.id.item_sort) {
            mPopup.dismiss();
            showSortDialog();

        } else if (i == R.id.item_newfolder) {
            mPopup.dismiss();
            doFileOperation(FileOperationService.FILE_OP_NEWFOLDER);

        } else {
        }
    }

    public void onEventMainThread(SearchEndEvent event) {
        String path = event.folderPath;
        System.out.println("path:" + path);
        DMFile file = new DMFile();
        file.mPath = path;
        mFileListView.gotoSubPatg(file);
    }

    public void setTitle(String title) {
        mainText.setText(title);
    }

    public String getCurrentPath() {
        return mFileListView.getCurrentPath();
    }

    public void setEditState(int state) {
        // TODO Auto-generated method stub
        getCurView().switchMode(state);
        // 弹出底部的横条
        switchMode(state);
    }

    private void initFileList() {
        // 如果有U盘显示U盘，否则显示手机
        mFileListView = (FileManagerDirView) parent
                .findViewById(R.id.lv_file_list);
        // mFileListView.setOnFileItemClickListener(mOnFileItemClickListener);
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

        mFileListView.init(curFileType, mSubscriptions);
        mFileListView.attachAvodListener();

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
                    setEditState(EditState.STATE_NORMAL);
                }
            }
        });

    }

    public void setOnEditableChange(OnEditableChange editableChange) {
        this.mOnEditableChange = editableChange;
    }

    abstract interface OnEditableChange {

        public abstract void onChange(boolean show);
    }

    protected void setEditLayoutVisible(boolean visible) {
        // TODO Auto-generated method stub
        if (visible) {
            normalLayout.setVisibility(View.VISIBLE);
        } else {
            normalLayout.setVisibility(View.GONE);
        }
    }

    public boolean isCanToUpper() {
        return mFileListView.isCanToUpper();
    }

    public boolean isEditMode() {
        return mEditMode;
    }

    public void toUpper() {
        mFileListView.toUpperPath();
    }

    public void setExitButtonVisible(boolean visible) {

        exitShown = visible;

        if (visible) {

            showUserGuide();

            Drawable nav_up = getResources().getDrawable(
                    R.drawable.safeexit_btn_selector);
            nav_up.setBounds(0, 0, nav_up.getMinimumWidth(),
                    nav_up.getMinimumHeight());
            mainText.setCompoundDrawables(null, null, nav_up, null);
            mainText.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    showPropDialog();
                }
            });

        } else {
            mainText.setCompoundDrawables(null, null, null, null);
            mainText.setOnClickListener(null);
        }
    }

    private void showUserGuide() {
        // TODO Auto-generated method stub
        boolean show = preferences.getBoolean("SHOW", true);
        if (show) {
            parent.findViewById(R.id.layout_guide).setVisibility(View.GONE);

            if (MainActivity.class.isInstance(activity)) {
                ((MainActivity) activity).setGuideBarVisible(View.GONE);
            }
        }
    }

    protected void showPropDialog() {
        // TODO Auto-generated method stub
        UDiskTextViewDialog dialog = new UDiskTextViewDialog(activity,
                UDiskEditTextDialog.TYPE_TWO_BTN);
        dialog.getTitleLinearLayout().setVisibility(View.VISIBLE);
        dialog.setClickButtonDismiss(true);
        dialog.setContent(activity.getString(R.string.DM_Safe_Exit_Remind));
        dialog.setCancelable(false);
        dialog.setTitleContent(getString(R.string.DM_setting_getotaupgrade_successful_tips));

        // 监听事件
        dialog.setLeftBtn(getString(R.string.DM_Safe_Exit_Remind_No),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub

                    }
                });
        dialog.setRightBtn(getString(R.string.DM_Safe_Exit_Remind_Yes),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        setSafetyExit();
                    }
                });
        dialog.show();
    }

    protected void setSafetyExit() {
        // TODO Auto-generated method stub
        CommonAsync.Runnable runnable = new CommonAsync.Runnable() {

            @Override
            public void stop() {
                // TODO Auto-generated method stub

            }

            @Override
            public Object run() {
                // TODO Auto-generated method stub
                System.out.println("setSafetyExit");
                return DMSdk.getInstance().safetyExit();
            }
        };

        CommonAsync.CommonAsyncListener listener = new CommonAsync.CommonAsyncListener() {

            @Override
            public void onResult(Object ret) {
                // TODO Auto-generated method stub
                int result = (int) ret;
                if (result == DMRet.ACTION_SUCCESS) {
                    Toast.makeText(activity,
                            R.string.DM_SetUI_Success_Operation,
                            Toast.LENGTH_SHORT).show();

                    if (MainActivity.class.isInstance(getActivity())) {
                        ((MainActivity) getActivity()).manualCheckDevice(false);
                    }
                } else {
                    Toast.makeText(activity,
                            R.string.DM_SetUI_Failed_Operation,
                            Toast.LENGTH_SHORT).show();
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

        CommonAsync task = new CommonAsync(runnable, listener);
        ExecutorService FULL_TASK_EXECUTOR = (ExecutorService) Executors
                .newCachedThreadPool();
        task.executeOnExecutor(FULL_TASK_EXECUTOR);
    }

    private void showMusicDialog() {

        System.out.println("showMusicDialog main");

        if (mMusicPlayerDialog == null) {
            mMusicPlayerDialog = new MusicPlayerDialog(activity);
        }
        /*
         * mMusicPlayerDialog.setOnDismissListener(new OnDismissListener() {
		 * 
		 * @Override public void onDismiss(DialogInterface dialog) { // TODO
		 * Auto-generated method stub if
		 * (!AodPlayer.getInstance().getIsPlaying()) {
		 * ibtn_music.setVisibility(View.GONE); } } });
		 */
        mMusicPlayerDialog.show();
    }

    private void closeMusicDialog() {
        if (mMusicPlayerDialog != null && mMusicPlayerDialog.isShowing()) {
            mMusicPlayerDialog.cancel();
        }
        mMusicPlayerDialog = null;
    }

    public void onEventMainThread(MusicDialogChange state) {
        System.out.println("MusicDialogChange:" + state.getState());
        if (state.getState() == 0) {
            ibtn_music.setVisibility(View.GONE);
            closeMusicDialog();
        } else if (state.getState() == 1) {
            ibtn_music.setVisibility(View.VISIBLE);
        }

        if (state.getShown() && !mBackgroud) {
            showMusicDialog();
        }
    }


    public void onEventMainThread(DirViewStateChangeEvent event) {

        if (event == null) {
            return;
        }

        int state = event.state;
        String pathName = event.currentPath;
        List<DMFile> fileList = event.fileList;

        if (fileList == null)
            return;
        if (state != EditState.STATE_NORMAL) {
            // 编辑模式下更新title
            int count = FileManager.getSelectedCount(fileList);
            mainText.setText(String
                    .format(getResources().getString(
                            R.string.DM_Navigation_Upload_Num), String.valueOf(count)));

        } else if (mViewType != VIEW_TYPE) {
            FileManager.unselectAll(fileList);
            String rPath = mFileListView.getRelativePath(pathName);
            System.out.println("rrr rpath:" + rPath);

            if (rPath.equals("") || rPath.equals("/")) {
                mFolderArray = null;
                if (curFileType == FILE_TYPE_AIRDISK
                        || curFileType == FILE_TYPE_PATHSELECT) {
                    setEditLayoutVisible(false);
                } else {
                    setEditLayoutVisible(true);
                }
                if (mOnEditableChange != null) {
                    mOnEditableChange.onChange(false);
                }
            } else {

                String[] array = rPath.split("/");
                mFolderArray = Arrays.copyOfRange(array, 0, array.length);
                setEditLayoutVisible(true);

                if (mOnEditableChange != null) {
                    mOnEditableChange.onChange(true);
                }
            }

            if (curFileType == FILE_TYPE_DOWNLOAD) {
                mRootName = "My Downloads";
            } else if (curFileType == FILE_TYPE_VAULT) {

                mRootName = getString(R.string.DM_Set_SecureVault);

            } else {
                mRootName = getString(R.string.DM_Control_Wireless_Resource);
            }

            mPathView.setFoder(mRootName, mFolderArray);
        }
    }

    abstract interface OnEditModeChangeListener {
        public abstract void onEditModeChange(boolean edit);
    }

    public void setOnEditModeChangeListener(
            OnEditModeChangeListener editModeChange) {
        this.mOnEditModeChange = editModeChange;
    }

    private class FileOperationListener implements HandlerUtil.MessageListener {

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            int msgWhat = msg.what;
            String strOp = null;

            if (msgWhat == FileOperationService.MSG_DELETE_FINISHED) {
                strOp = activity.getString(R.string.DM_Task_Delete);
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
                if (mProgressDialog != null) {
                    mProgressDialog.setProgress(info.progress);
                    //名字
                    if (!mProgressDialog.getMessage().equals(
                            FileUtil.getFileNameFromPath(info.path))) {
                        mProgressDialog.setMessage(FileUtil
                                .getFileNameFromPath(info.path));
                    }
                    //剩余时间
                    if (info.timeLeft != null) {
                        mProgressDialog.setTimeLeft(info.timeLeft);
                    }
                    //剩余项
                    if (info.sizeLeft != null) {
                        mProgressDialog.setNumberLeft(String.format(getString(R.string.DM_File_Operate_Remain_Items),
                                String.valueOf(info.numberLeft), String.valueOf(info.sizeLeft)));
                    } else {
                        mProgressDialog.setNumberLeft(String.valueOf(info.numberLeft));
                    }
                    //速度
                    if (info.speed != null && !info.speed.equals("")) {
                        mProgressDialog.setSpeed(info.speed);
                    }
                }
                return;
            } else if (msgWhat == FileOperationService.MSG_SAME_FILE) {
                SameNameInfo info = (SameNameInfo) msg.obj;
                onSameFile(info);
            } else if (msgWhat == FileOperationService.MSG_ERROR) {
                int err = (Integer) msg.arg1;
                onError(err, msg.obj == null ? null : (List<DMFile>) msg.obj);
            } else if (msgWhat == FileOperationService.MSG_DECRYPTED_FINISHED) {
                strOp = getString(R.string.DM_Access_Vault_Decrypt_Note_Title);
            } else if (msgWhat == FileOperationService.MSG_ENCRYPTED_FINISHED) {
                strOp = getString(R.string.DM_Access_Vault_Encrypt_Note_Title);
            }else if (msgWhat == FileOperationService.MSG_ENCRYPTED_DELETE_FINISHED) {
                strOp = getString(R.string.DM_Encrypt_file_delete);
            }

            if (strOp != null) {
                if (msg.arg1 == FileOperationService.OP_SUCCESSED) {

                    if (strOp.equals(activity
                            .getString(R.string.DM_Task_Download))) {
                        if (msg.arg2 != 0) {
                            Toast.makeText(
                                    activity,
                                    R.string.DM_Remind_Operate_Download_Success,
                                    Toast.LENGTH_SHORT).show();
                        }
                    } else if (strOp.equals(activity
                            .getString(R.string.DM_Task_Delete))) {
                        Toast.makeText(activity,
                                R.string.DM_Remind_Operate_Delete_Success,
                                Toast.LENGTH_SHORT).show();
                    } else if (strOp.equals(activity
                            .getString(R.string.DM_Task_Rename))) {
                        Toast.makeText(activity,
                                R.string.DM_More_Rename_Updata_Success,
                                Toast.LENGTH_SHORT).show();
                    } else if (strOp.equals(activity
                            .getString(R.string.DM_Task_Build_NewFolder))) {
                        Toast.makeText(
                                activity,
                                R.string.DM_Fileexplore_Operation_NewFolder_Success,
                                Toast.LENGTH_SHORT).show();
                        if (curFileType == FILE_TYPE_PATHSELECT) {
                            mFileListView.gotoSubPatg(newFolder);
                        }

                        newFolder = null;
                    } else if (strOp.equals(activity
                            .getString(R.string.DM_Task_Copy))) {
                        if (msg.arg2 != 0) {
                            Toast.makeText(activity,
                                    R.string.DM_Remind_Operate_Copy_Success,
                                    Toast.LENGTH_SHORT).show();
                        }
                    } else if (strOp
                            .equals(getString(R.string.DM_Access_Vault_Encrypt_Note_Title))) {
                        Toast.makeText(activity,
                                R.string.DM_Remind_Operate_Copy_Success,
                                Toast.LENGTH_SHORT).show();
                    } else if (strOp
                            .equals(getString(R.string.DM_Access_Vault_Decrypt_Note_Title))) {
                        Toast.makeText(activity,
                                R.string.DM_Remind_Operate_Copy_Success,
                                Toast.LENGTH_SHORT).show();
                    } else if(strOp
                            .equals(getString(R.string.DM_Encrypt_file_delete))) {
                        Toast.makeText(activity,
                                R.string.DM_Remind_Operate_Delete_Success,
                                Toast.LENGTH_SHORT).show();
                    }



                    if (!strOp.equals(activity
                            .getString(R.string.DM_Task_Rename)) && msg.obj != null) {
                        HashMap<String, List> map = (HashMap<String, List>) msg.obj;
                        Set set = map.keySet();
                        Iterator iter = set.iterator();
                        while (iter.hasNext()) {
                            String key = (String) iter.next();
                            String despath = key;
                            List<DMFile> files = map.get(key);
                            // 显示dialog 提示用户
                            onSuccess(strOp, despath, files, msg.arg2);
                            if (strOp != null
                                    && strOp.equals(getString(R.string.DM_Task_Copy))) {
                                return;
                            }
                        }
                    }

                    setEditState(EditState.STATE_NORMAL);

                } else if (msg.arg1 == FileOperationService.OP_FAILED) {
                    if (strOp.equals(activity
                            .getString(R.string.DM_Task_Download))) {
                        Toast.makeText(activity,
                                R.string.DM_Remind_Operate_Download_Failed,
                                Toast.LENGTH_SHORT).show();
                    } else if (strOp.equals(activity
                            .getString(R.string.DM_Task_Delete))) {
                        int skip = (int) msg.obj;
                        Toast.makeText(
                                activity,
                                String.format(
                                        getString(R.string.DM_Remind_Operate_Delete_Success_Pass),
                                        String.valueOf(skip)), Toast.LENGTH_SHORT).show();
                    } else if (strOp.equals(activity
                            .getString(R.string.DM_Task_Rename))) {
                        Toast.makeText(activity,
                                R.string.DM_More_Rename_Updata_Error,
                                Toast.LENGTH_SHORT).show();
                    } else if (strOp.equals(activity
                            .getString(R.string.DM_Task_Build_NewFolder))) {
                        Toast.makeText(activity, R.string.DM_Task_Build_Failed,
                                Toast.LENGTH_SHORT).show();
                    } else if (strOp
                            .equals(activity
                                    .getString(R.string.DM_Access_Vault_Decrypt_Note_Title))) {
                        Toast.makeText(
                                activity,
                                R.string.DM_Access_Vault_Decrypt_Fail_Note_Title,
                                Toast.LENGTH_SHORT).show();
                    } else if(strOp.equals(activity
                            .getString(R.string.DM_Encrypt_file_delete))) {
                        Toast.makeText(
                                activity,
                                String.format(
                                        getString(R.string.DM_Remind_Operate_Delete_Success_Pass),
                                        String.valueOf((int) msg.obj)), Toast.LENGTH_SHORT).show();
                    }
                }

                if (mProgressDialog != null) {
                    mProgressDialog.cancel();
                    mProgressDialog = null;
                }

                if (!strOp.equals(getString(R.string.DM_Task_Download))) {

                    // 刷新视图
                    getCurView().reloadItems();
                }
            }

        }
    }

    public void doFileOperation(final int op) {

        final List<DMFile> list = getCurView().getSelectedFiles();
        if (op != FileOperationService.FILE_OP_NEWFOLDER
                && op != FileOperationService.FILE_OP_COPYTO
                && list.size() == 0) {
            Toast.makeText(activity, R.string.DM_FileOP_Warn_Select_File,
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (op == FileOperationService.FILE_OP_DELETE || op == FileOperationService.FILE_OP_ENCRYPRED_DELETE) {

            MessageDialog builder = new MessageDialog(activity);
            builder.setTitleContent(getString(R.string.DM_Task_Delete));
            builder.setMessage(getString(R.string.DM_Remind_Operate_Delete_File));
            builder.setLeftBtn(getString(R.string.DM_Control_Cancel),
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // TODO Auto-generated method stub
                        }
                    });

            builder.setRightBtn(getString(R.string.DM_Control_Definite),
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // TODO Auto-generated method stub
                            if (isFileInPictureType(list.get(0))) {
                                doFileOperation(op, true, list);
                            } else {
                                doFileOperation(op, list);
                            }

                        }
                    });

            builder.show();

        } else if (op == FileOperationService.FILE_OP_NEWFOLDER) {

            final UDiskEditTextDialog builder = new UDiskEditTextDialog(
                    activity, UDiskEditTextDialog.TYPE_TWO_BTN);
            builder.setTitleContent(getString(R.string.DM_Task_Build_NewFolder));
            builder.setLeftBtn(getString(R.string.DM_Control_Cancel),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // TODO Auto-generated method stub
                            builder.releaseDialog();
                        }
                    });
            builder.setRightBtn(getString(R.string.DM_Control_Definite),
                    new DialogInterface.OnClickListener() {

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
                            } else if (DMSdk.getInstance().isExisted(
                                    mFileListView.getCurrentPath()
                                            + File.separator + name)) {
                                builder.showWarnText(R.string.DM_More_Rename_BeUsed);
                                builder.lockDialog();
                            } else {
                                builder.releaseDialog();
                                DMDir file = new DMDir();
                                file.mName = name;
                                file.mLocation = DMFile.LOCATION_UDISK;
                                file.mPath = mFileListView.getCurrentPath()
                                        + File.separator + name;
                                newFolder = file;
                                doNewFolderOperation(
                                        FileOperationService.FILE_OP_NEWFOLDER,
                                        file);
                            }
                        }
                    });
            builder.show();
            builder.getEditTextView().setFocusable(true);
            builder.getEditTextView().setFocusableInTouchMode(true);
            builder.getEditTextView().requestFocus();
            builder.getEditTextView().pullUpKeyboard();

        } else  {
            doFileOperation(op, list);
        }
    }

    public boolean isFileInPictureType(DMFile file) {
        return mViewType == VIEW_TYPE && mViewPager.getCurrentItem() == 1
                && file.isDir == true;
    }

    @Override
    public void onDestroyView() {
        // TODO Auto-generated method stub
        super.onDestroyView();

        System.out.println("mainfragment ondestroy");
        mHandler.removeCallbacksAndMessages(null);
        mFileListView.removeAvodListener();
        EventBus.getDefault().unregister(this);
        closeMusicDialog();
        DMSdk.getInstance().removeListener(cookie_disk);
        if (otaTask != null) {
            otaTask.destory();
        }
        mSubscriptions.unsubscribe();
    }

    public void setViewType(int type) {

        mViewType = type;

        if (type == VIEW_DIR) {
            layout_dir.setVisibility(View.VISIBLE);
            layout_type.setVisibility(View.GONE);

            if (DMSupportFunction.isSupportSearch(BaseValue.supportFucntion)) {
                layout_search.setVisibility(View.VISIBLE);
            } else {
                layout_search.setVisibility(View.GONE);
            }

            String pathName = mFileListView.getCurrentPath();
            String rPath = null;
            if (pathName == null) {
                rPath = "/";
            } else {
                rPath = mFileListView.getRelativePath(pathName);
            }
            System.out.println("rrr2 rPath:" + rPath);
            if (rPath.equals("") || rPath.equals("/")) {
                setEditLayoutVisible(false);
            } else {
                setEditLayoutVisible(true);
            }

            reloadItems();

        } else if (type == VIEW_TYPE) {
            layout_dir.setVisibility(View.GONE);
            layout_type.setVisibility(View.VISIBLE);
            layout_search.setVisibility(View.GONE);
            normalLayout.setVisibility(View.VISIBLE);

            try {

                for (int i = 0; i < mTabsAdapter.getCount(); i++) {
                    IPager fr = (IPager) mTabsAdapter.getAt(i);
                    fr.resetPage();
                }
                reloadItems();

            } catch (Exception e) {
                // TODO: handle exception
                // e.printStackTrace();
            }

        }
    }

    public void setViewType(int type, boolean reload) {

        mViewType = type;

        if (type == VIEW_DIR) {
            layout_dir.setVisibility(View.VISIBLE);
            layout_type.setVisibility(View.GONE);

            if (DMSupportFunction.isSupportSearch(BaseValue.supportFucntion)) {
                layout_search.setVisibility(View.VISIBLE);
            } else {
                layout_search.setVisibility(View.GONE);
            }

            String pathName = mFileListView.getCurrentPath();
            String rPath = null;
            if (pathName == null) {
                rPath = "/";
            } else {
                rPath = mFileListView.getRelativePath(pathName);
            }
            System.out.println("rrr2 rPath:" + rPath);
            if (rPath.equals("") || rPath.equals("/")) {
                setEditLayoutVisible(false);
            } else {
                setEditLayoutVisible(true);
            }

            if (reload) {
                reloadItems();
            }

        } else if (type == VIEW_TYPE) {
            layout_dir.setVisibility(View.GONE);
            layout_type.setVisibility(View.VISIBLE);
            layout_search.setVisibility(View.GONE);
            normalLayout.setVisibility(View.VISIBLE);

            try {

                for (int i = 0; i < mTabsAdapter.getCount(); i++) {
                    IPager fr = (IPager) mTabsAdapter.getAt(i);
                    fr.resetPage();
                }
                if (reload) {
                    reloadItems();
                }
            } catch (Exception e) {
                // TODO: handle exception
                // e.printStackTrace();
            }

        }
    }

    private void addPagers() {
        addPager(getString(R.string.DM_Control_Video), FilePager.class,
                DMFileCategoryType.E_VIDEO_CATEGORY);
        addPager(getString(R.string.DM_Control_Image), FilePictruePager.class,
                DMFileCategoryType.E_PICTURE_CATEGORY);
        addPager(getString(R.string.DM_Control_Music), FilePager.class,
                DMFileCategoryType.E_MUSIC_CATEGORY);
        addPager(getString(R.string.DM_Control_Document), FilePager.class,
                DMFileCategoryType.E_BOOK_CATEGORY);
    }

    private void addPager(String title, Class<?> clss, DMFileCategoryType type) {
        Bundle bundle = new Bundle();
        bundle.putInt(FilePager.PAGER_TYPE, type.ordinal());

        mTabsAdapter.addTab(title, clss, bundle);
    }

    public class TabsAdapter extends FragmentPagerAdapter {
        private final Context mContext;
        private final ViewPager mViewPager;
        private Map<Integer, Fragment> mFragmentMap = new HashMap();
        private final ArrayList<TabInfo> mTabs = new ArrayList<TabInfo>();

        final class TabInfo {
            private final Class<?> clss;
            private final Bundle args;
            private final String title;

            TabInfo(String _title, Class<?> _class, Bundle _args) {
                clss = _class;
                args = _args;
                title = _title;
            }
        }

        public TabsAdapter(Context activity, ViewPager pager) {
            super(getChildFragmentManager());
            mContext = activity;
            mViewPager = pager;
            mViewPager.setAdapter(this);
        }

        public void addTab(String title, Class<?> clss, Bundle args) {
            TabInfo info = new TabInfo(title, clss, args);
            mTabs.add(info);

            Fragment fragment = Fragment.instantiate(mContext,
                    info.clss.getName(), info.args);
            mFragmentMap.put(mFragmentMap.size() - 1, fragment);

            notifyDataSetChanged();
        }

        public Fragment getAt(int position) {
            Iterator iterator = mFragmentMap.entrySet().iterator();
            while (iterator.hasNext()) {

                Map.Entry entry = (Map.Entry) iterator.next();
                // // System.out.println("getat:"+entry.getKey() + ":" +
                // // entry.getValue());
                return (Fragment) this.mFragmentMap.get(position);
            }
            return null;
        }

        @Override
        public Fragment getItem(int position) {
            // TODO Auto-generated method stub
            TabInfo info = mTabs.get(position);
            // 如果是图片的分类要特别处理，保留上一次此的UImode
            Fragment fragment = Fragment.instantiate(mContext,
                    info.clss.getName(), info.args);
            mFragmentMap.put(position, fragment);
            return fragment;
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return mTabs.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            TabInfo info = mTabs.get(position);
            return info.title;
        }
    }

    public List<DMFile> getSelectedFiles() {
        // TODO Auto-generated method stub
        return getCurView().getSelectedFiles();
    }

    public int getCurViewType() {
        return mViewType;
    }

    public IFileExplorer getCurView() {
        if (mViewType == VIEW_DIR) {
            return mFileListView;
        } else {
            int i = mViewPager.getCurrentItem();
            IPager fr = (IPager) mTabsAdapter.getAt(i);
            return fr.getFileView();
        }
    }

    public boolean isDiskMountPc() {
        return mFileListView.isDiskMountPc();
    }

    public void switchMode(int mode) {
        // TODO Auto-generated method stub

        if (mode == EditState.STATE_EDIT) {
            mEditMode = true;
            selectAllText.setText(R.string.DM_Control_Select);
            selectAllText.setVisibility(View.VISIBLE);
            backButton.setImageResource(R.drawable.sel_upload_close);
            normalLayout.setVisibility(View.GONE);
            newTips.setVisibility(View.GONE);
            mainText.setText(String
                    .format(getResources().getString(
                            R.string.DM_Navigation_Upload_Num), "0"));
            if (AodPlayer.getInstance().getPlayState() == 1
                    || AodPlayer.getInstance().getPlayState() == 2
                    || AodPlayer.getInstance().getPlayState() == 3) {
                ibtn_music.setVisibility(View.GONE);
            }

            if (exitShown) {
                mainText.setCompoundDrawables(null, null, null, null);
                mainText.setOnClickListener(null);
            }
        } else {

            mEditMode = false;
            String rPath = mFileListView.getRelativePath(getCurrentPath());
            System.out.println("rrr rpath:" + rPath);

            selectAllText.setVisibility(View.GONE);
            backButton
                    .setImageResource(R.drawable.sliding_menu_button_selector);
            if (curFileType == FILE_TYPE_DOWNLOAD) {
                backButton
                        .setImageResource(R.drawable.dm_lib_wifi_back_btn_bg_selector);
            } else if (curFileType == FILE_TYPE_PATHSELECT) {
                backButton
                        .setImageResource(R.drawable.dm_lib_wifi_back_btn_bg_selector);
            }

            normalLayout.setVisibility(View.VISIBLE);
            if (BaseValue.dmota != null
                    && (BaseValue.dmota.flag == 1 || BaseValue.dmota.flag == 2)) {
                newTips.setVisibility(View.VISIBLE);
            }

            if (curFileType == FILE_TYPE_DOWNLOAD) {
                mainText.setText(R.string.DM_Bottom_Bar_Button_MyDowmloads);
            } else {
                mainText.setText(R.string.DM_APP_Name);
            }

            if (AodPlayer.getInstance().getPlayState() == 1
                    || AodPlayer.getInstance().getPlayState() == 2
                    || AodPlayer.getInstance().getPlayState() == 3) {
                ibtn_music.setVisibility(View.VISIBLE);
            }

            if (exitShown) {
                Drawable nav_up = getResources().getDrawable(
                        R.drawable.safeexit_btn_selector);
                nav_up.setBounds(0, 0, nav_up.getMinimumWidth(),
                        nav_up.getMinimumHeight());
                mainText.setCompoundDrawables(null, null, nav_up, null);
                mainText.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                        showPropDialog();
                    }
                });
            }
        }

        if (mOnEditModeChange != null) {
            mOnEditModeChange.onEditModeChange(mEditMode);
        }
    }

    public void selectAll() {
        // TODO Auto-generated method stub
        getCurView().selectAll();
    }

    public void unselectAll() {
        // TODO Auto-generated method stub
        getCurView().unselectAll();
    }

    public void reloadItems() {
        // TODO Auto-generated method stub
        if (mViewType == VIEW_DIR) {
            mFileListView.loadFiles();
        } else if (mViewType == VIEW_TYPE) {

            IFileExplorer curView  = getCurView();
            if(curView != null) {
                curView.reloadItems();
            }

        }
    }

    // 302平台wifi切走后收不到操作失败返回值,需要手动dismiss进度条
    public void dismissPregressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.cancel();
            mProgressDialog = null;
        }
    }

    /*
     * 之前选中的视图和当前视图都要是正常状态
     */
    private void OnCurrentPageChanged() {
        setEditState(EditState.STATE_NORMAL);
    }

    public void getOTAInfo() {
        CommonAsync.Runnable runnable = new CommonAsync.Runnable() {

            @Override
            public void stop() {
                // TODO Auto-generated method stub

            }

            @Override
            public Object run() {
                // TODO Auto-generated method stub
                System.out.println("mmmain check ota:");
                DMOTA ota = DMSdk.getInstance().checkNewFw();
                return ota;
            }
        };

        CommonAsync.CommonAsyncListener listener = new CommonAsync.CommonAsyncListener() {

            @Override
            public void onResult(Object ret) {
                // TODO Auto-generated method stub
                DMOTA ota = (DMOTA) ret;
                if (ota != null) {
                    System.out.println("mmmain ota flg:" + ota.flag);
                    System.out.println("mmmain ota time:" + ota.time);
                    if (ota.flag > 0) {
                        BaseValue.dmota = ota;
                    } else {
                        BaseValue.dmota = null;
                    }
                    EventBus.getDefault().post(new NewFwEvent(BaseValue.dmota));
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

        otaTask = new CommonAsync(runnable, listener);
        ExecutorService FULL_TASK_EXECUTOR = (ExecutorService) Executors
                .newCachedThreadPool();
        otaTask.executeOnExecutor(FULL_TASK_EXECUTOR);
    }

    public void onEventMainThread(NewFwEvent event) {
        if (!mEditMode && event.getOta() != null
                && (event.getOta().flag > 0 && event.getOta().flag < 5)) {
            if (event.getOta().flag < 3) {
                newTips.setVisibility(View.VISIBLE);
            } else {
                UpgradeFwTaskDialog taskDialog = null;
                if (event.getOta().flag == 3) {
                    taskDialog = new UpgradeFwTaskDialog(activity,
                            UpgradeFwTaskDialog.TYPE_FORCE_DOWNLOAD_UPGRADE,
                            event.getOta());
                } else if (event.getOta().flag == 4) {
                    taskDialog = new UpgradeFwTaskDialog(activity,
                            UpgradeFwTaskDialog.TYPE_FORCE_UPGRADE,
                            event.getOta());
                }
                taskDialog.show();
            }
        } else {
            newTips.setVisibility(View.GONE);
        }
    }

    private void showMoreDialog() {
        // TODO Auto-generated method stub

        if (mPopup != null && mPopup.isShowing()) {
            mPopup.dismiss();
            return;
        }
        mPopup = new DMPopup(activity, DMPopup.VERTICAL);
        View contentView = LayoutInflater.from(activity).inflate(
                R.layout.popup_operation, null);
        TextView editText = (TextView) contentView.findViewById(R.id.item_edit);
        TextView newText = (TextView) contentView
                .findViewById(R.id.item_newfolder);
        TextView sortText = (TextView) contentView.findViewById(R.id.item_sort);
        editText.setOnClickListener(this);
        sortText.setOnClickListener(this);
        newText.setOnClickListener(this);

        if (mViewType == VIEW_DIR) {
            newText.setVisibility(View.VISIBLE);
        } else {
            newText.setVisibility(View.GONE);
        }

        if (curFileType == FILE_TYPE_DOWNLOAD) {
            sortText.setVisibility(View.GONE);
            newText.setVisibility(View.GONE);
        } else if (curFileType == FILE_TYPE_PATHSELECT) {
            editText.setVisibility(View.GONE);
        }

        LayoutParams params = new LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        contentView.setLayoutParams(params);
        mPopup.addView(contentView);
        mPopup.show(layout_title_more);
    }

    private void showSortDialog() {
        final DMSortDialog sortDialog = new DMSortDialog(activity,
                UDiskBaseDialog.TYPE_TWO_BTN);
        sortDialog.setTitleContent(activity.getString(R.string.DM_File_Sort));
        sortDialog.setLeftBtn(activity.getString(R.string.DM_SetUI_Cancel),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub

                    }
                });
        sortDialog.setRightBtn(activity.getString(R.string.DM_SetUI_Confirm),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        setFileSortInfo(sortDialog.getCurrentSortType(),
                                sortDialog.getCurrentSortOrder());
                    }
                });
        sortDialog.show();
    }

    protected void setFileSortInfo(final int currentSortType,
                                   final int currentSortOrder) {
        // TODO Auto-generated method stub
        CommonAsync.Runnable runnable = new CommonAsync.Runnable() {

            @Override
            public void stop() {
                // TODO Auto-generated method stub

            }

            @Override
            public Object run() {
                // TODO Auto-generated method stub
                DMSdk.getInstance().setFileSortType(currentSortType);
                DMSdk.getInstance().setFileSortOrder(currentSortOrder);
                return 0;
            }
        };

        CommonAsync.CommonAsyncListener listener = new CommonAsync.CommonAsyncListener() {

            @Override
            public void onResult(Object ret) {
                // TODO Auto-generated method stub
                getCurView().reloadItems();
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

        CommonAsync task = new CommonAsync(runnable, listener);
        ExecutorService FULL_TASK_EXECUTOR = (ExecutorService) Executors
                .newCachedThreadPool();
        task.executeOnExecutor(FULL_TASK_EXECUTOR);
    }

}
