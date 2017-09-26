package com.dmsys.airdiskpro.ui;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.dm.baselib.BaseValue;
import com.dm.xunlei.udisk.Network.Dialog.AlertDmDialogDefault;
import com.dm.xunlei.udisk.Network.View.EditTextButtonView;
import com.dmairdisk.aodplayer.api.AodPlayer;
import com.dmairdisk.aodplayer.util.CommonAsync;
import com.dmairdisk.aodplayer.util.CommonAsync.CommonAsyncListener;
import com.dmsoftwareupgrade.util.UDiskBaseDialog;
import com.dmsys.airdiskpro.AttributeTask;
import com.dmsys.airdiskpro.RxBus;
import com.dmsys.airdiskpro.adapter.DeviceListAdapter;
import com.dmsys.airdiskpro.adapter.PopupAdapter;
import com.dmsys.airdiskpro.db.BackupSettingDB;
import com.dmsys.airdiskpro.event.DeviceValutEvent;
import com.dmsys.airdiskpro.event.DisconnectEvent;
import com.dmsys.airdiskpro.event.FileRefreshEvent;
import com.dmsys.airdiskpro.event.NewFwEvent;
import com.dmsys.airdiskpro.event.PasswordChangeEvent;
import com.dmsys.airdiskpro.event.StorageEvent;
import com.dmsys.airdiskpro.event.SupportFunctionEvent;
import com.dmsys.airdiskpro.filemanager.EditState;
import com.dmsys.airdiskpro.filemanager.FileOperationService;
import com.dmsys.airdiskpro.model.BakSetBean;
import com.dmsys.airdiskpro.service.BackupService;
import com.dmsys.airdiskpro.service.BackupService.BuckupType;
import com.dmsys.airdiskpro.setting.VaultSettingActivity;
import com.dmsys.airdiskpro.ui.MainFragment.OnEditModeChangeListener;
import com.dmsys.airdiskpro.ui.UploadBaseActivity.FileType;
import com.dmsys.airdiskpro.utils.AndroidConfig;
import com.dmsys.airdiskpro.utils.ConvertUtil;
import com.dmsys.airdiskpro.utils.DMLog;
import com.dmsys.airdiskpro.utils.FileInfoUtils;
import com.dmsys.airdiskpro.utils.FileUtil;
import com.dmsys.airdiskpro.utils.GetBakLocationTools;
import com.dmsys.airdiskpro.utils.HandlerUtil;
import com.dmsys.airdiskpro.view.DMPopup;
import com.dmsys.airdiskpro.view.FileBrowseDialog;
import com.dmsys.airdiskpro.view.FileBrowseDialog.FileBrowseDialogOnClickListener;
import com.dmsys.airdiskpro.view.MessageDialog;
import com.dmsys.airdiskpro.view.ProgressDialog;
import com.dmsys.airdiskpro.view.UDiskEditTextDialog;
import com.dmsys.airdiskpro.view.UDiskListViewDialog;
import com.dmsys.airdiskpro.view.UDiskListViewDialog.MyItemClickListener;
import com.dmsys.airdiskpro.view.UDiskTextViewDialog;
import com.dmsys.airdiskpro.view.VaultPasswordDialog;
import com.dmsys.dmsdk.DMSdk;
import com.dmsys.dmsdk.DMSdk.DMDeviceListChangeListener;
import com.dmsys.dmsdk.api.IDMSdk.FwDownloadListener;
import com.dmsys.dmsdk.model.DMBindInfo;
import com.dmsys.dmsdk.model.DMDevice;
import com.dmsys.dmsdk.model.DMFile;
import com.dmsys.dmsdk.model.DMFileCategoryType;
import com.dmsys.dmsdk.model.DMIsOpeningVault;
import com.dmsys.dmsdk.model.DMOTA;
import com.dmsys.dmsdk.model.DMRet;
import com.dmsys.dmsdk.model.DMSupportFunction;
import com.dmsys.dmsdk.model.DMVaultPath;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu.OnOpenListener;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;
import com.umeng.analytics.MobclickAgent;
import com.dmsys.mainbusiness.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import de.greenrobot.event.EventBus;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;


public class MainActivity extends SlidingFragmentActivity implements
        OnClickListener {

    public volatile AtomicBoolean isServiceInited = new AtomicBoolean(false);

    private FrameLayout layout_bottom_connected;
    private LinearLayout layout_bottom_nodevice;

    /**
     * 底部action栏
     **/
    private TextView downloadText;
    private TextView copyText;
    private TextView deleteText;
    private View moreImage;

    /**
     * 底部引导栏
     **/
    private TextView layout_menu;
    private TextView layout_download;
    private LinearLayout mUploadLayout;

    private FrameLayout layout_switch;
    private TextView text_dir;
    private TextView text_type;
    private ImageView img_upload;

    private MenuFragment menuFragment;
    private ConnectFragment connectFragment;
    private MainFragment mainFragment;

    private FileBrowseDialog mFileBrowseDialog;

    private long deviceCookie, connectCookie;

    private HandlerUtil.StaticHandler mHandler;
    private MyMessageListener mListener;

    private static final int SHOW_HOME = HandlerUtil.generateId();
    private static final int SHOW_NODISK = HandlerUtil.generateId();
    private static final int REFRESH_TYPE_LAYOUT = HandlerUtil.generateId();
    private static final int REFRESH_COPYTO_LAYOUT = HandlerUtil.generateId();
    private static final int REFRESH_SAFEEXIT = HandlerUtil.generateId();

    private BroadcastReceiver mReceiver;
    private long mExitTime = 0;

    private DMPopup mPopup;
    private PopupAdapter mPopupAdapter;
    private WindowManager mWindowManager;

    private UDiskTextViewDialog fwUpdateDialog;
    private UDiskTextViewDialog fwDownloadedDialog;
    private ProgressDialog fwProDialog;

    private DMDevice mDevice;

    public static final String TAG_EXIT = "exit";

    private EditText mPassword;
    private UDiskEditTextDialog adPassword;
    private Button okButton;
    private EditTextButtonView mETBV;

    private UDiskListViewDialog devicesSwitchDialog;

    private Dialog promptDialog;
    public boolean passwordBtnOnclickRecored = false;

    public CompositeSubscription mSubscriptions = new CompositeSubscription();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // int a = 1 / 0;

        initViews();

        initScanningDevice();
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        MobclickAgent.onPause(this);
    }

    private void initViews() {
        // TODO Auto-generated method stub
        DMLog.d("main", "initViews");

        EventBus.getDefault().register(this);
        initSlidingMenu();

        setDefaultFragment();

        mListener = new MyMessageListener();

        mHandler = new HandlerUtil.StaticHandler(mListener);

        initBottomBar();

        initActionsBar();

        initFileBrowseDialog();

        mPopupAdapter = new PopupAdapter(this);
        mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);


        //统一用RXbUS

        RxBus.getDefault().toObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object tmp) {
                        if (tmp != null && tmp instanceof DeviceValutEvent) {
                            DeviceValutEvent event = (DeviceValutEvent) tmp;
                            if (event.type == DeviceValutEvent.DEVICE_PASSWORD) {
                                if (event.ret == 0) {
                                    BaseValue.Host = mDevice.getIp();
                                    BaseValue.DeviceName = mDevice.getName();
                                    mHandler.sendEmptyMessage(SHOW_HOME);
                                } else {
                                    mHandler.sendEmptyMessage(SHOW_NODISK);
                                }
                            }

                        }
                    }
                });

    }

    private void initBottomBar() {
        // TODO Auto-generated method stub
        layout_bottom_connected = (FrameLayout) findViewById(R.id.layout_bottom_connectdevice);
        layout_bottom_nodevice = (LinearLayout) findViewById(R.id.layout_bottom_nodevice);

        layout_menu = (TextView) findViewById(R.id.layout_menu);
        layout_menu.setOnClickListener(this);
        layout_download = (TextView) findViewById(R.id.layout_download);
        layout_download.setOnClickListener(this);

        mUploadLayout = (LinearLayout) findViewById(R.id.layout_upload);
        mUploadLayout.setOnClickListener(this);

        layout_switch = (FrameLayout) findViewById(R.id.layout_switch);
        text_dir = (TextView) findViewById(R.id.text_dir);
        text_dir.setOnClickListener(this);
        text_type = (TextView) findViewById(R.id.text_type);
        text_type.setOnClickListener(this);
        img_upload = (ImageView) findViewById(R.id.img_upload);
        img_upload.setOnClickListener(this);
    }

    private void initActionsBar() {
        // TODO Auto-generated method stub
        downloadText = (TextView) findViewById(R.id.op_download);
        downloadText.setOnClickListener(this);
        copyText = (TextView) findViewById(R.id.op_cpTo);
        copyText.setOnClickListener(this);
        deleteText = (TextView) findViewById(R.id.op_delete);
        deleteText.setOnClickListener(this);
        moreImage = findViewById(R.id.op_more);
        moreImage.setOnClickListener(this);
    }

    private void initFileBrowseDialog() {
        // TODO Auto-generated method stub
        mFileBrowseDialog = new FileBrowseDialog(this);
        mFileBrowseDialog
                .setFileBrowseDialogOnClickListener(new FileBrowseDialogOnClickListener() {

                    @Override
                    public void ThirdImageViewOnClick() {
                        // TODO Auto-generated method stub
                        Intent mIntent = new Intent(getBaseContext(),
                                UploadFileActivity.class);
                        mIntent.putExtra("FileType", FileType.AUDIO.ordinal());
                        if (mainFragment.getCurViewType() == MainFragment.VIEW_DIR) {
                            mIntent.putExtra("CurPath",
                                    mainFragment.getCurrentPath());
                        } else {
                            mIntent.putExtra("CurPath", "CLASSIFY");
                        }
                        startActivity(mIntent);
                    }

                    @Override
                    public void SecondImageViewOnClick() {
                        // TODO Auto-generated method stub
                        Intent mIntent = new Intent(getBaseContext(),
                                UploadFileActivity.class);
                        mIntent.putExtra("FileType", FileType.VIODE.ordinal());
                        if (mainFragment.getCurViewType() == MainFragment.VIEW_DIR) {
                            mIntent.putExtra("CurPath",
                                    mainFragment.getCurrentPath());
                        } else {
                            mIntent.putExtra("CurPath", "CLASSIFY");
                        }
                        startActivity(mIntent);
                    }

                    @Override
                    public void FourthImageViewOnClick() {
                        // TODO Auto-generated method stub
                        Intent mIntent = new Intent(getBaseContext(),
                                UploadDirActivity.class);
                        mIntent.putExtra("FileType", FileType.AUDIO.ordinal());
                        if (mainFragment.getCurViewType() == MainFragment.VIEW_DIR) {
                            mIntent.putExtra("CurPath",
                                    mainFragment.getCurrentPath());
                        } else {
                            mIntent.putExtra("CurPath", "CLASSIFY");
                        }
                        startActivity(mIntent);

                    }

                    @Override
                    public void FirstImageViewOnClick() {
                        // TODO Auto-generated method stub
                        Intent mIntent = new Intent(getBaseContext(),
                                PictureFolderActivity.class);
                        if (mainFragment.getCurViewType() == MainFragment.VIEW_DIR) {
                            mIntent.putExtra("CurPath",
                                    mainFragment.getCurrentPath());
                        } else {
                            mIntent.putExtra("CurPath", "CLASSIFY");
                        }
                        startActivity(mIntent);
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
                findViewById(R.id.bottom_guide_bar).setVisibility(View.VISIBLE);
            }
        });
    }

    private class MyMessageListener implements HandlerUtil.MessageListener {
        @Override
        public void handleMessage(Message msg) {

            if (msg.what == SHOW_HOME) {
                showMainFragment();
            } else if (msg.what == SHOW_NODISK) {
                mDevice = null;
                BaseValue.Host = null;
                BaseValue.DeviceName = null;
                showNoDeviceView();
            } else if (msg.what == REFRESH_TYPE_LAYOUT) {
                if (msg.arg1 == 1) {
                    mUploadLayout.setVisibility(View.GONE);
                    layout_switch.setVisibility(View.VISIBLE);
                    text_type.setSelected(true);
                    text_dir.setSelected(false);
                } else {
                    mUploadLayout.setVisibility(View.VISIBLE);
                    layout_switch.setVisibility(View.GONE);
                    text_type.setSelected(false);
                    text_dir.setSelected(false);
                }
                mainFragment.setViewType(MainFragment.VIEW_TYPE, false);
            } else if (msg.what == REFRESH_COPYTO_LAYOUT) {
                if (msg.arg1 == 1) {
                    copyText.setVisibility(View.VISIBLE);
                } else {
                    copyText.setVisibility(View.GONE);
                }
            } else if (msg.what == REFRESH_SAFEEXIT) {
                if (msg.arg1 == 1) {
                    mainFragment.setExitButtonVisible(false);
                } else {
                    mainFragment.setExitButtonVisible(false);
                }
            }
        }
    }

    private void attachDeviceChangeListener() {
        deviceCookie = DMSdk.getInstance().attachListener(
                new DMDeviceListChangeListener() {

                    @Override
                    public void onDeviceListChanged(int type, DMDevice device) {
                        if (type == 0) {
                            mHandler.postDelayed(new Runnable() {

                                @Override
                                public void run() {
                                    // TODO Auto-generated method stub
                                    getDevices();
                                }
                            }, 2000);

                        } else if (type == 1) {

                            if (mDevice != null
                                    && device.getIp().equals(mDevice.getIp())) {
                                disConnectDevice();
                                EventBus.getDefault().post(
                                        new DisconnectEvent());

                                BaseValue.dmota = null;
                                EventBus.getDefault().post(
                                        new NewFwEvent(BaseValue.dmota));

                                mHandler.post(new Runnable() {

                                    @Override
                                    public void run() {
                                        // TODO Auto-generated method stub
                                        if (menuFragment != null) {
                                            menuFragment.resetUI();
                                        }

                                        if (mainFragment != null
                                                && mainFragment.isEditMode()) {
                                            mainFragment.getCurView()
                                                    .unselectAll();
                                            mainFragment
                                                    .setEditState(EditState.STATE_NORMAL);
                                        }
                                    }
                                });
                            }
                        }
                    }
                });
    }

    private void initScanningDevice() {
        // TODO Auto-generated method stub
        initBroadcaseReceiver();
        attachDeviceChangeListener();
        // attachDeviceConnectListener();

        ConnectivityManager connectMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectMgr.getActiveNetworkInfo();
        if (networkInfo != null) {
            if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI
                    || networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                System.out.println("initScanningDevice");
                new InitTask().execute();
            }
        } else {
            mHandler.sendEmptyMessage(SHOW_NODISK);
        }
    }

    private boolean isDeviceConnected() {

        System.out.println("IN isDeviceConnected");
        if (BaseValue.Host == null || BaseValue.Host.equals("")) {
            return false;
        }
        return DMSdk.getInstance().isDeviceConnected(BaseValue.Host);
    }

    protected void getDevices() {

        CommonAsync.Runnable runnable = new CommonAsync.Runnable() {

            @Override
            public void stop() {
                // TODO Auto-generated method stub
            }

            @Override
            public Object run() {
                // TODO Auto-generated method stub
                return DMSdk.getInstance().getDevices();
            }
        };

        CommonAsyncListener listener = new CommonAsyncListener() {

            @Override
            public void onResult(Object result) {
                // TODO Auto-generated method stub
                List<DMDevice> devices = (List<DMDevice>) result;
                if (devices != null) {
                    int size = devices.size();
                    System.out.println("device size:" + size);
                    if (size == 0) {
                        mHandler.sendEmptyMessage(SHOW_NODISK);
                    } else if (size == 1) {
                        connectDevice(devices.get(0));
                    } else if (size > 1) {
                        showDeviceSlectDailog(devices);
                    }

                } else {
                    mHandler.sendEmptyMessage(SHOW_NODISK);
                }
            }

            @Override
            public void onError() {
                // TODO Auto-generated method stub
                mHandler.sendEmptyMessage(SHOW_NODISK);
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

    boolean inConnect = false;

    protected void connectDevice(final DMDevice device) {

        boolean connected = isDeviceConnected();
        System.out.println("connectDevice connected:" + connected);
        if (!connected) {

            CommonAsync.Runnable runnable = new CommonAsync.Runnable() {

                @Override
                public void stop() {
                    // TODO Auto-generated method stub

                }

                @Override
                public Object run() {
                    // TODO Auto-generated method stub

                    return DMSdk.getInstance().connectDevice(device.getIp(),
                            AndroidConfig.getPhoneModel(),
                            AndroidConfig.getIMEI(MainActivity.this));
                }
            };

            CommonAsyncListener listener = new CommonAsyncListener() {

                @Override
                public void onResult(Object result) {
                    // TODO Auto-generated method stub
                    int ret = (int) result;
                    System.out.println("connect ret:" + ret);
                    if (ret == DMRet.ACTION_SUCCESS) {
                        mDevice = device;
                        loginDevice("");
                    } else {
                        mHandler.sendEmptyMessage(SHOW_NODISK);
                    }
                    inConnect = false;
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

            if (!inConnect) {
                inConnect = true;
                CommonAsync async = new CommonAsync(runnable, listener);
                async.executeOnExecutor((ExecutorService) Executors
                        .newCachedThreadPool());
            }

        } else {
            mHandler.sendEmptyMessage(SHOW_HOME);
        }
    }

    protected void loginDevice(final String password) {

        CommonAsync.Runnable runnable = new CommonAsync.Runnable() {

            @Override
            public void stop() {
                // TODO Auto-generated method stub

            }

            @Override
            public Object run() {
                // TODO Auto-generated method stub
                return DMSdk.getInstance().loginDevice(password,
                        AndroidConfig.getPhoneModel(),
                        AndroidConfig.getIMEI(MainActivity.this));
            }
        };

        CommonAsyncListener listener = new CommonAsyncListener() {

            @Override
            public void onResult(Object result) {
                // TODO Auto-generated method stub
                Integer ret = (Integer) result;
                System.out.println("logggg:" + ret);
                if (ret == 0) {
                    // if (adPassword != null) {
                    // adPassword.releaseDialog();
                    // adPassword.dismiss();
                    // adPassword = null;
                    // }
                    BaseValue.Host = mDevice.getIp();
                    BaseValue.DeviceName = mDevice.getName();
                    mHandler.sendEmptyMessage(SHOW_HOME);
                } else if (ret == DMRet.ERROR_SESSTION_INVALID) {

                    // if (adPassword != null && passwordBtnOnclickRecored) {
                    // //防止多次回调，直接显示
                    // passwordBtnOnclickRecored = false;
                    // adPassword
                    // .showWarnText(R.string.DM_SetUI_Connect_Error_Authenticating);
                    // } else {
                    // showPasswordDialog();
                    // }
                    Intent mIntent = new Intent(MainActivity.this,
                            VaultPasswordDialog.class);
                    mIntent.putExtra(VaultPasswordDialog.TypeFlag,
                            VaultPasswordDialog.FLAG_DEVICE_PASSWORD);
                    mIntent.putExtra(VaultPasswordDialog.DeviceNameFlag,
                            mDevice.getName());
                    mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivityForResult(mIntent, 0);
                } else {
                    // adPassword = null;
                    mHandler.sendEmptyMessage(SHOW_NODISK);
                }
            }

            @Override
            public void onError() {
                // TODO Auto-generated method stub
                mHandler.sendEmptyMessage(SHOW_NODISK);
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

    protected void disConnectDevice() {

        CommonAsync.Runnable runnable = new CommonAsync.Runnable() {

            @Override
            public void stop() {
                // TODO Auto-generated method stub

            }

            @Override
            public Object run() {
                // TODO Auto-generated method stub
                return DMSdk.getInstance().disConnectDevice();
            }
        };

        CommonAsyncListener listener = new CommonAsyncListener() {

            @Override
            public void onResult(Object result) {
                // TODO Auto-generated method stub
                mHandler.sendEmptyMessage(SHOW_NODISK);
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

    private void setDefaultFragment() {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();

        if (connectFragment == null) {
            connectFragment = new ConnectFragment();
        }
        transaction.add(R.id.main_content, connectFragment, "CONNECT");
        transaction.commitAllowingStateLoss();
    }

    private void showMainFragment() {
        if (isServiceInited.get()) {

            if (mainFragment != null && !mainFragment.isHidden()) {
                System.out.println("showMainFragment already show");
                return;
            }

            System.out.println("showMainFragment 2");

            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction transaction = fm.beginTransaction();

            transaction.hide(connectFragment);

            if (mainFragment == null) {
                System.out.println("mainFragment null");
                mainFragment = new MainFragment();
                transaction.add(R.id.main_content, mainFragment, "MAIN");
            } else {
                System.out.println("mainFragment not null");
                transaction.show(mainFragment);
            }

            try {
                transaction.commitAllowingStateLoss();
            } catch (Exception e) {
                // TODO: handle exception
            }

            if (menuFragment != null) {
                menuFragment.refreshConnectInfo(true);
            }
            mainFragment.resetFiles();
            mainFragment.getOTAInfo();

            layout_bottom_connected.setVisibility(View.VISIBLE);
            layout_bottom_nodevice.setVisibility(View.GONE);

            adPassword = null;

            getSupportFunctions();
        }
    }

    private void showNoDeviceView() {

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();

        if (connectFragment == null) {
            connectFragment = new ConnectFragment();
            transaction.add(R.id.main_content, connectFragment, "CONNECT");
        }

        if (!connectFragment.isHidden()) {
            connectFragment.showNoDeviceView();
        } else {
            if (mainFragment != null && !mainFragment.isHidden()) {
                transaction.hide(mainFragment);
                transaction.show(connectFragment);
                connectFragment.showNoDeviceView();
            }
        }

        try {
            transaction.commitAllowingStateLoss();
        } catch (Exception e) {
            // TODO: handle exception
        }

        if (AodPlayer.getInstance().getIsPlaying()) {
            AodPlayer.getInstance().stop();
        }

        layout_bottom_connected.setVisibility(View.GONE);
        layout_bottom_nodevice.setVisibility(View.VISIBLE);
        BaseValue.supportFucntion = 0;
        BaseValue.ValutPath = null;
        EventBus.getDefault().post(new SupportFunctionEvent(-1));

    }

    private void showCheckingView() {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();

        if (connectFragment == null) {
            connectFragment = new ConnectFragment();
            transaction.add(R.id.main_content, connectFragment, "CONNECT");
        }

        if (!connectFragment.isHidden()) {
            connectFragment.showCheckingView();
        } else {

            if (!mainFragment.isHidden()) {
                System.out.println("showCheckingView mainFragment visble");
                transaction.hide(mainFragment);
                transaction.show(connectFragment);
                connectFragment.showCheckingView();
            }
        }

        try {
            transaction.commitAllowingStateLoss();
        } catch (Exception e) {
            // TODO: handle exception
        }

        layout_bottom_connected.setVisibility(View.GONE);
        layout_bottom_nodevice.setVisibility(View.VISIBLE);
        BaseValue.supportFucntion = 0;
        BaseValue.ValutPath = null;
        EventBus.getDefault().post(new SupportFunctionEvent(-1));
    }

    private void initSlidingMenu() {
        // customize the SlidingMenu
        setBehindContentView(R.layout.menu_frame);

        if (menuFragment == null) {
            menuFragment = new MenuFragment();
        }

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.menu_frame, menuFragment, "MENU").commit();

        SlidingMenu sm = getSlidingMenu();
        sm.setBehindOffsetRes(R.dimen.slidingmenu_offset);
        sm.setBehindScrollScale(0.2f);
        sm.setFadeEnabled(true);
        sm.setFadeDegree(0.8f);
        sm.setBehindWidth(getWindowManager().getDefaultDisplay().getWidth() * 4 / 5);
        sm.setBackgroundResource(R.drawable.menu_bg);
        sm.setSlidingEnabled(true);
        sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);

        sm.setBehindCanvasTransformer(new SlidingMenu.CanvasTransformer() {
            @Override
            public void transformCanvas(Canvas canvas, float percentOpen) {
                float scale = (float) (percentOpen * 0.2 + 0.8);
                canvas.scale(scale, scale, -canvas.getWidth() / 2,
                        canvas.getHeight() / 2);
            }
        });

        sm.setAboveCanvasTransformer(new SlidingMenu.CanvasTransformer() {
            @Override
            public void transformCanvas(Canvas canvas, float percentOpen) {
                float scale = (float) (1 - percentOpen * 0.2);
                canvas.scale(scale, scale, 0, canvas.getHeight() / 2);
            }
        });

        sm.setOnOpenListener(new OnOpenListener() {

            @Override
            public void onOpen() {
                // TODO Auto-generated method stub
                EventBus.getDefault().post(new StorageEvent());
            }
        });

    }


    public static class Toggle {
    }

    ;

    public void onEventMainThread(Toggle toggle) {
        getSlidingMenu().toggle();
    }

    public void onEventMainThread(FileRefreshEvent event) {
        if (BaseValue.Host != null && !BaseValue.Host.equals("")) {
            mainFragment.reloadItems();
        }
    }

    public void onEventMainThread(PasswordChangeEvent event) {

        if (adPassword == null) {
            BaseValue.Host = null;
            mDevice = null;
            BaseValue.DeviceName = null;


            AodPlayer.getInstance().pause();

            if (getSlidingMenu().isMenuShowing()) {
                getSlidingMenu().toggle();
            }

            showCheckingView();
            getDevices();
            mHandler.postDelayed(new Runnable() {

                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    if (connectFragment.isChecking()) {
                        if (mainFragment != null && !mainFragment.isHidden()) {
                            return;
                        }

                        if (adPassword == null
                                && ((devicesSwitchDialog == null || !devicesSwitchDialog
                                .isShowing()))) {
                            showNoDeviceView();
                        }
                    }
                }
            }, 8000);
        }
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        // TODO Auto-generated method stub
        super.onAttachFragment(fragment);

        if (MainFragment.class.isInstance(fragment)) {
            ((MainFragment) fragment)
                    .setOnEditModeChangeListener(new OnEditModeChangeListener() {

                        @Override
                        public void onEditModeChange(boolean edit) {
                            // TODO Auto-generated method stub
                            View bottomActionBar = findViewById(R.id.bottom_action_bar);
                            View bottomGuideBar = findViewById(R.id.bottom_guide_bar);
                            if (edit) {
                                bottomGuideBar.setVisibility(View.GONE);
                                bottomActionBar.setVisibility(View.VISIBLE);
                            } else {
                                bottomGuideBar.setVisibility(View.VISIBLE);
                                bottomActionBar.setVisibility(View.GONE);
                            }
                        }
                    });

        }

    }

    private void initBroadcaseReceiver() {
        // TODO Auto-generated method stub
        if (mReceiver == null) {
            mReceiver = new BroadcastReceiver() {

                @Override
                public void onReceive(final Context context, Intent intent) {
                    // TODO Auto-generated method stub
                    if (intent.getAction().equals(
                            WifiManager.NETWORK_STATE_CHANGED_ACTION)) {

                        NetworkInfo info = intent
                                .getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                        WifiInfo mWifiInfo0 = intent
                                .getParcelableExtra(WifiManager.EXTRA_WIFI_INFO);
                        State wifi = info.getState();
                        if (mWifiInfo0 != null) {
                            DMLog.d("onReceive",
                                    "wifi name:" + mWifiInfo0.getSSID());
                        }

                        if (wifi != null) {
                            //转屏

                            if (wifi == State.DISCONNECTED) {

                                DMLog.d("wifi change", "DISCONNECTED");

                                showNoDeviceView();

                                if (menuFragment != null) {
                                    menuFragment.refreshSSID(null);
                                }

                                BaseValue.Host = "";
                                BaseValue.dmota = null;
                                BaseValue.DeviceName = null;

                                EventBus.getDefault().post(
                                        new DisconnectEvent());

                                BaseValue.dmota = null;
                                EventBus.getDefault().post(
                                        new NewFwEvent(BaseValue.dmota));

                                if (mainFragment != null) {
                                    mainFragment.dismissPregressDialog();
                                }

                            } else if (wifi == State.CONNECTING) {

                                DMLog.d("wifi change", "CONNECTING");

                            } else if (wifi == State.CONNECTED) {

                                DMLog.d("wifi change", "CONNECTED");

                                if (mWifiInfo0 != null) {

                                    String ssid = mWifiInfo0.getSSID().replace(
                                            "\"", "");

                                    if (isDeviceConnected()) {
                                        return;
                                    }

                                    if (menuFragment != null) {
                                        menuFragment.refreshSSID(ssid);
                                    }

                                    showCheckingView();


                                    startPrivateServiceWapper();

                                    mHandler.postDelayed(new Runnable() {

                                        @Override
                                        public void run() {
                                            // TODO Auto-generated method stub
                                            if (connectFragment.isChecking()) {
                                                if (mainFragment != null
                                                        && !mainFragment
                                                        .isHidden()) {
                                                    return;
                                                }

                                                if (adPassword == null
                                                        && ((devicesSwitchDialog == null || !devicesSwitchDialog
                                                        .isShowing()))) {
                                                    showNoDeviceView();
                                                }
                                            }
                                        }
                                    }, 8000);

                                }
                            }
                        }
                    }
                }
            };

            IntentFilter filter = new IntentFilter();
            filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
            registerReceiver(mReceiver, filter);
        }

    }

    class InitTask extends AsyncTask<Void, Void, Integer> {

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
            closeFwUpdateDialog();
        }

        @Override
        protected Integer doInBackground(Void... params) {
            // TODO Auto-generated method stub

            // connectRemote();

            if (mainFragment != null && !mainFragment.isHidden()) {
                System.out.println("connect main show");
                return -1;
            }

            stopPrivateServiceWapper();

            System.out.println("InitTask startPrivateService");

            return startPrivateServiceWapper();
        }

        @Override
        protected void onPostExecute(Integer result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            System.out.println("InitTask:" + result);
            if (result == DMRet.ACTION_SUCCESS) {
                

                mHandler.postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        if (mainFragment != null && !mainFragment.isHidden()) {
                            return;
                        }

                        if (adPassword == null
                                && ((devicesSwitchDialog == null || !devicesSwitchDialog
                                .isShowing()))) {
                            showNoDeviceView();
                            new OTATask().execute();
                        }
                    }
                }, 12000);
            }
        }
    }

    class OTATask extends AsyncTask<Void, Void, DMOTA> {

        @Override
        protected DMOTA doInBackground(Void... params) {
            // TODO Auto-generated method stub
            return DMSdk.getInstance().checkNewFw();
        }

        @Override
        protected void onPostExecute(DMOTA ota) {
            // TODO Auto-generated method stub
            super.onPostExecute(ota);

            if (!connectFragment.isHidden()) {
                if (ota != null) {
                    System.out.println("ota:" + ota.flag);
                    if (ota.flag == 2) { // 服务器
                        showFwDownloadDialog(ota);
                    } else if (ota.flag == 4) {// 服务器,强制升级
                        showFwForceDownloadDialog(ota);
                    }
                }
            }
        }
    }

    protected void showDeviceSlectDailog(final List<DMDevice> devices) {
        // TODO Auto-generated method stub
        if (devicesSwitchDialog != null && devicesSwitchDialog.isShowing()) {
            return;
        }

        DeviceListAdapter mAdapter = new DeviceListAdapter(this, devices);
        devicesSwitchDialog = new UDiskListViewDialog(this,
                UDiskBaseDialog.TYPE_ONE_BTN, mAdapter,
                new MyItemClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // TODO Auto-generated method stub
                        devicesSwitchDialog.dismiss();
                        mDevice = devices.get(id);
                        connectDevice(mDevice);

                    }
                });
        devicesSwitchDialog
                .setTitleContent(getString(R.string.DM_Disk_Dialog_Select_Device));
        devicesSwitchDialog.setLeftBtn(getString(R.string.DM_SetUI_Cancel),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        mHandler.sendEmptyMessage(SHOW_NODISK);
                        if (menuFragment != null) {
                            menuFragment.refreshSSID("");
                        }
                        devicesSwitchDialog = null;
                    }

                });

        devicesSwitchDialog.setOnKeyListener(new OnKeyListener() {

            @Override
            public boolean onKey(DialogInterface dialog, int keyCode,
                                 KeyEvent event) {
                // TODO Auto-generated method stub
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    mHandler.sendEmptyMessage(SHOW_NODISK);
                    if (menuFragment != null) {
                        menuFragment.refreshSSID("");
                    }
                    devicesSwitchDialog = null;
                }
                return false;
            }
        });

        devicesSwitchDialog.show();
    }

    protected void showPasswordDialog() {
        // TODO Auto-generated method stub
        System.out.println("showPasswordDialog");
        String message = String.format(
                getString(R.string.DM_SetUI_Input_Password_Dialog),
                mDevice.getName());

        adPassword = new UDiskEditTextDialog(this,
                UDiskEditTextDialog.TYPE_TWO_BTN);
        adPassword.setTitleContent(message);
        adPassword.setToPassword();
        adPassword.setCanceledOnTouchOutside(false);
        adPassword.hideWarnText();
        adPassword.setTipText(getString(R.string.DM_Access_Password_Forgot),
                new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                        showTipDiaog();
                    }
                });

        adPassword.setLeftBtn(getString(R.string.DM_Control_Cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        adPassword.releaseDialog();
                        mHandler.sendEmptyMessage(SHOW_NODISK);
                        adPassword = null;
                    }
                });
        adPassword.setRightBtn(getString(R.string.DM_Control_Definite),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        String password = adPassword.getEditContent().trim();
                        if (password == null || password.equals("")) {
                            adPassword
                                    .showWarnText(R.string.DM_More_Rename_No_Enpty);
                            adPassword.lockDialog();
                        } else if (password.length() < 8) {
                            adPassword
                                    .showWarnText(R.string.DM_Error_PWD_Short);
                            adPassword.lockDialog();
                        } else if (password.length() > 32) {
                            adPassword
                                    .showWarnText(R.string.DM_SetUI_Credentials_Password_Too_Long);
                            adPassword.lockDialog();
                        } else if (!FileInfoUtils.isValidFileName(password)) {
                            adPassword
                                    .showWarnText(R.string.DM_More_Rename_Name_Error);
                            adPassword.lockDialog();
                        } else {
                            passwordBtnOnclickRecored = true;
                            adPassword.lockDialog();
                            loginDevice(password);

                        }
                    }
                });

        adPassword.setOnKeyListener(new OnKeyListener() {

            @Override
            public boolean onKey(DialogInterface dialog, int keyCode,
                                 KeyEvent event) {
                // TODO Auto-generated method stub
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    mHandler.sendEmptyMessage(SHOW_NODISK);
                    adPassword.releaseDialog();
                    devicesSwitchDialog = null;
                }
                return false;
            }
        });

        adPassword.show();
        adPassword.getEditTextView().setFocusable(true);
        adPassword.getEditTextView().setFocusableInTouchMode(true);
        adPassword.getEditTextView().requestFocus();
        adPassword.getEditTextView().pullUpKeyboard();
    }

    protected void showTipDiaog() {
        // TODO Auto-generated method stub
        final MessageDialog dialog = new MessageDialog(this,
                UDiskBaseDialog.TYPE_ONE_BTN);
        dialog.setTitleContent(getString(R.string.DM_setting_getotaupgrade_successful_tips));
        dialog.setMessage(getString(R.string.DM_Access_Password_Forgot_Caption));
        dialog.setLeftBtn(getString(R.string.DM_Control_Know),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dlg, int which) {
                        // TODO Auto-generated method stub
                    }
                });

        dialog.show();
    }

    private void showFwDownloadDialog(final DMOTA ota) {
        // TODO Auto-generated method stub
        // 增加不是wifi环境下的提示
        String content = String.format(
                getString(R.string.DM_Remind_Update_Download_ask), ota.name);

        String content1 = String.format(
                getString(R.string.DM_setting_update_content1), ota.version);
        String content2 = String.format(
                getString(R.string.DM_setting_update_content2),
                ConvertUtil.convertFileSize(ota.size, 2));

        String content3 = "";
        String lan = Locale.getDefault().getLanguage();
        if ("zh".equals(lan)) {
            content3 = String.format(
                    getString(R.string.DM_setting_update_content3),
                    ota.description);
        } else {
            content3 = String.format(
                    getString(R.string.DM_setting_update_content3),
                    ota.description_en);
        }

        content1 = "\n" + content1 + "\n" + content2 + "\n" + content3;

        // 关闭之前的dialog
        closeFwUpdateDialog();
        fwUpdateDialog = new UDiskTextViewDialog(this,
                UDiskEditTextDialog.TYPE_TWO_BTN);
        fwUpdateDialog.getTitleLinearLayout().setVisibility(View.VISIBLE);
        fwUpdateDialog.setClickButtonDismiss(true);
        fwUpdateDialog.setContent(content1);
        fwUpdateDialog.setCancelable(false);
        fwUpdateDialog.setTitleContent(content);

        // 监听事件
        fwUpdateDialog.setLeftBtn(getString(R.string.DM_Control_Cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub

                    }
                });
        fwUpdateDialog.setRightBtn(getString(R.string.DM_Control_Definite),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        ConnectivityManager mConnectivity = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                        // 检查网络连接
                        NetworkInfo info = mConnectivity.getActiveNetworkInfo();
                        if (info != null
                                && info.getType() != ConnectivityManager.TYPE_WIFI) {
                            initFwUpdateDialog1(ota.size);
                        } else {
                            // 弹框进度条
                            CreateProgressWindow();
                            // 开始下载
                            downloadFW();
                        }
                    }
                });
        fwUpdateDialog.show();
    }

    private void showFwForceDownloadDialog(final DMOTA ota) {
        // TODO Auto-generated method stub
        // 增加不是wifi环境下的提示
        String content = String.format(
                getString(R.string.DM_setting_mandatory_update_found_newFW1),
                ota.name);

        String content1 = String.format(
                getString(R.string.DM_setting_update_content1), ota.version);
        String content2 = String.format(
                getString(R.string.DM_setting_update_content2),
                ConvertUtil.convertFileSize(ota.size, 2));
        String content3 = String
                .format(getString(R.string.DM_setting_update_content3),
                        ota.description);

        content1 = "\n" + content1 + "\n" + content2 + "\n" + content3;

        // 关闭之前的dialog
        closeFwUpdateDialog();
        fwUpdateDialog = new UDiskTextViewDialog(this,
                UDiskEditTextDialog.TYPE_ONE_BTN);
        fwUpdateDialog.getTitleLinearLayout().setVisibility(View.VISIBLE);
        fwUpdateDialog.setClickButtonDismiss(true);
        fwUpdateDialog.setContent(content1);
        fwUpdateDialog.setCancelable(false);
        fwUpdateDialog.setTitleContent(content);

        // 监听事件
        fwUpdateDialog.setLeftBtn(
                getString(R.string.DM_setting_mandatory_update_download),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        ConnectivityManager mConnectivity = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                        // 检查网络连接
                        NetworkInfo info = mConnectivity.getActiveNetworkInfo();
                        if (info != null
                                && info.getType() != ConnectivityManager.TYPE_WIFI) {
                            initFwUpdateDialog1(ota.size);
                        } else {
                            // 弹框进度条
                            CreateProgressWindow();
                            // 开始下载
                            downloadFW();
                        }
                    }
                });
        fwUpdateDialog.show();
    }

    private void initFwUpdateDialog1(long size) {
        // 关闭之前的dialog
        closeFwUpdateDialog();
        String content = String.format(
                getString(R.string.DM_Remind_Update_Mobile_ask),
                ConvertUtil.byteConvert(size, false));
        fwUpdateDialog = new UDiskTextViewDialog(this,
                UDiskEditTextDialog.TYPE_TWO_BTN);
        fwUpdateDialog.getTitleLinearLayout().setVisibility(View.VISIBLE);
        fwUpdateDialog.setClickButtonDismiss(true);
        fwUpdateDialog.setContent(content);
        fwUpdateDialog.setCancelable(false);
        fwUpdateDialog
                .setTitleContent(getString(R.string.DM_setting_getotaupgrade_successful_tips));

        // 监听事件
        fwUpdateDialog.setLeftBtn(
                getString(R.string.DM_setting_getotaupgrede_no),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                    }
                });

        fwUpdateDialog.setRightBtn(
                getString(R.string.DM_setting_getotaupgrede_yes),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        // // 弹框进度条
                        CreateProgressWindow();
                        // 开始下载
                        downloadFW();
                    }
                });
        fwUpdateDialog.show();
    }

    protected void downloadFW() {
        // TODO Auto-generated method stub
        new Thread(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                int ret = DMSdk.getInstance().downloadFw(
                        new FwDownloadListener() {

                            @Override
                            public void onProgressChange(final long total,
                                                         final long already) {
                                // TODO Auto-generated method stub
                                runOnUiThread(new Runnable() {

                                    @Override
                                    public void run() {
                                        // TODO Auto-generated method stub
                                        int progresses = (int) (already * 100 / total);
                                        System.out.println("propro:"
                                                + progresses);
                                        if (progresses > 0) {
                                            fwProDialog.setProgress(progresses);
                                        } else {
                                            fwProDialog.setProgress(0);
                                        }
                                        if (progresses == 100) {
                                            cancelProgressWindow();
                                            createDownloadFinishWindow(getString(R.string.DM_setting_mandatory_update_download_done));
                                        }
                                    }
                                });
                            }
                        });

                if (ret != DMRet.ACTION_SUCCESS) {
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            // TODO Auto-generated method stub
                            cancelProgressWindow();
                            createDownloadFinishWindow(getString(R.string.DM_Remind_Upgrade_Downloading_failed));
                        }
                    });
                }
            }
        }).start();

    }

    // 关闭FW升级的dialog
    private void closeFwUpdateDialog() {
        if (fwUpdateDialog != null) {
            if (fwUpdateDialog.isShowing()) {
                fwUpdateDialog.dismiss();
            }
            fwUpdateDialog = null;
        }
    }

    // 产生下载进度的进度条
    protected void CreateProgressWindow() {
        cancelProgressWindow();
        fwProDialog = new ProgressDialog(MainActivity.this);
        fwProDialog.setTitleContent(getBaseContext().getString(
                R.string.DM_Remind_Upgrade_downloading));
        fwProDialog.setProgress(0);
        fwProDialog.setLeftBtn(getString(R.string.DM_Control_Cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        DMSdk.getInstance().cancelDownloadFw();
                    }
                });

        fwProDialog.show();
    }

    // 关闭下载进度的进度条
    protected void cancelProgressWindow() {
        if (fwProDialog != null) {
            if (fwProDialog.isShowing()) {
                fwProDialog.dismiss();
            }
            fwProDialog = null;
        }
    }

    // 产生下载进度的进度条
    protected void createDownloadFinishWindow(String content) {
        closeDownloadFinishWindow();
        fwDownloadedDialog = new UDiskTextViewDialog(this,
                UDiskEditTextDialog.TYPE_ONE_BTN);
        fwDownloadedDialog.getTitleLinearLayout().setVisibility(View.VISIBLE);
        fwDownloadedDialog.setClickButtonDismiss(true);
        fwDownloadedDialog.setContent(content);
        fwDownloadedDialog.setCancelable(false);
        fwDownloadedDialog
                .setTitleContent(getString(R.string.DM_setting_getotaupgrade_successful_tips));
        fwDownloadedDialog.setLeftBtn(getString(R.string.DM_Control_Definite),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                    }
                });

        fwDownloadedDialog.show();
    }

    // 关闭下载进度的进度条
    protected void closeDownloadFinishWindow() {
        if (fwDownloadedDialog != null) {
            if (fwDownloadedDialog.isShowing()) {
                fwDownloadedDialog.dismiss();
            }
            fwDownloadedDialog = null;
        }
    }

    public void manualCheckDevice(final boolean withota) {
        System.out.println("manualCheckDevice");
        showCheckingView();
        CommonAsync.Runnable runnable = new CommonAsync.Runnable() {

            @Override
            public void stop() {
                // TODO Auto-generated method stub

            }

            @Override
            public Object run() {
                // TODO Auto-generated method stub

                stopPrivateServiceWapper();

                System.out.println("manual startPrivateService");


                return startPrivateServiceWapper();
            }
        };

        CommonAsyncListener listener = new CommonAsyncListener() {

            @Override
            public void onResult(Object result) {
                // TODO Auto-generated method stub

                int res = (int) result;
                if (res == DMRet.ACTION_SUCCESS) {
                    mHandler.postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            // TODO Auto-generated method stub
                            if (mainFragment != null
                                    && !mainFragment.isHidden()) {
                                return;
                            }
                            if (adPassword == null
                                    && ((devicesSwitchDialog == null || !devicesSwitchDialog
                                    .isShowing()))) {
                                showNoDeviceView();
                            }
                        }
                    }, 8000);
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

    public void unregisterReceiver() {
        if (mReceiver != null) {
            System.out.println("ununununregister!!!");
            unregisterReceiver(mReceiver);
            mReceiver = null;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub

        if (keyCode == KeyEvent.KEYCODE_BACK) {

            if (getSlidingMenu().isMenuShowing()) {
                getSlidingMenu().toggle();
                return true;
            }

            if (mainFragment != null && !mainFragment.isHidden()
                    && mainFragment.isEditMode()) {
                mainFragment.getCurView().unselectAll();
                mainFragment.setEditState(EditState.STATE_NORMAL);
                return true;
            }

            if (mainFragment != null && !mainFragment.isHidden()) {

                if (mainFragment.getCurViewType() != MainFragment.VIEW_TYPE) {
                    if (mainFragment.isCanToUpper()) {
                        mainFragment.toUpper();
                        return true;
                    }
                }
            }

            if (System.currentTimeMillis() - mExitTime > 2000) {
                Toast.makeText(this, R.string.DM_MainAc_Toast_Key_Back_Quit,
                        Toast.LENGTH_SHORT).show();
                mExitTime = System.currentTimeMillis();
                return false;
            } else {
                this.finish();
            }

        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // TODO Auto-generated method stub
        // super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();

        System.out.println("mainactivity ondes");

        mHandler.removeCallbacksAndMessages(null);

        unregisterReceiver();

        try {
            AodPlayer.getInstance().exit();
        } catch (Exception e) {
            // TODO: handle exception
        }

        DMSdk.getInstance().removeListener(deviceCookie);
        DMSdk.getInstance().logoutVault();


        if (isDeviceConnected()) {
            System.out.println("ddd disConnectDevice:");
            DMSdk.getInstance().disConnectDevice();
        }
        stopPrivateServiceWapper();

        // 停止服务
        Intent intent = new Intent();
        intent.setAction(BackupService.ACTION_STOP_BACKUP);
        sendBroadcast(intent);

        mSubscriptions.unsubscribe();

    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        int i = v.getId();
        if (i == R.id.op_download) {
            mainFragment.doFileOperation(FileOperationService.FILE_OP_DOWNLOAD);

        } else if (i == R.id.op_cpTo) {
            final List<DMFile> files = mainFragment.getSelectedFiles();
            if (files.size() == 0) {
                Toast.makeText(this, R.string.DM_FileOP_Warn_Select_File,
                        Toast.LENGTH_SHORT).show();
                return;
            }

            FileOperationService.selectedList = files;

            Intent cpintent = new Intent(this, PathSelectActivity.class);
            cpintent.putExtra(PathSelectActivity.EXTRA_OP,
                    PathSelectActivity.COPYTO);
            startActivity(cpintent);

        } else if (i == R.id.op_delete) {
            mainFragment.doFileOperation(FileOperationService.FILE_OP_DELETE);

        } else if (i == R.id.op_more) {
            showMoreDialog();

        } else if (i == R.id.layout_menu) {
            getSlidingMenu().toggle();

        } else if (i == R.id.layout_download) {
            Intent intent = new Intent(this, MyDownloadActivity.class);
            startActivity(intent);

        } else if (i == R.id.layout_upload || i == R.id.img_upload) {
            if (!mainFragment.isDiskMountPc()) {
                findViewById(R.id.bottom_guide_bar).setVisibility(View.GONE);
                mFileBrowseDialog.show();
            } else {
                Toast.makeText(MainActivity.this,
                        R.string.DM_MDNS_Disk_Connect_PC_2, Toast.LENGTH_SHORT)
                        .show();
            }


        } else if (i == R.id.text_dir) {
            if (!text_dir.isSelected()) {
                text_dir.setSelected(true);
                text_type.setSelected(false);
                mainFragment.setViewType(MainFragment.VIEW_DIR);
            }

        } else if (i == R.id.text_type) {
            if (!text_type.isSelected()) {
                text_dir.setSelected(false);
                text_type.setSelected(true);
                mainFragment.setViewType(MainFragment.VIEW_TYPE);
            }

        } else {
        }
    }

    private void showMoreDialog() {
        // TODO Auto-generated method stub

        if (mPopup != null && mPopup.isShowing()) {
            mPopup.dismiss();
            return;
        }

        final List<DMFile> files = mainFragment.getSelectedFiles();
        if (files.size() == 0) {
            Toast.makeText(this, R.string.DM_FileOP_Warn_Select_File,
                    Toast.LENGTH_SHORT).show();
            return;
        }

        mPopup = new DMPopup(this, DMPopup.VERTICAL);

        View contentView = LayoutInflater.from(this).inflate(
                R.layout.popup_content, null);

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
                } else if (mdata.get(position).equals(
                        getString(R.string.DM_Task_File_Hide))) {
                    onClickHide(true, files.get(0));
                } else if (mdata.get(position).equals(
                        getString(R.string.DM_Task_File_Unhide))) {
                    onClickHide(false, files.get(0));
                } else if (mdata.get(position).equals(
                        getString(R.string.DM_Task_File_Encrypted))) {
                    for (DMFile file : files) {
                        if (file.isDir) {
                            Toast.makeText(MainActivity.this, "Encrypted folders are not supported", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                    encryptedFiles(files);
                    //
                }

                mPopup.dismiss();
            }
        });

        LayoutParams params = new LayoutParams((mWindowManager
                .getDefaultDisplay().getWidth() * 1) / 2,
                LayoutParams.WRAP_CONTENT);
        contentView.setLayoutParams(params);
        mPopup.addView(contentView);
        mPopup.show(findViewById(R.id.bottom));

    }

    protected void encryptedFiles(final List<DMFile> files) {

        Subscription subscription = Observable.fromCallable(new Callable<DMIsOpeningVault>() {
            @Override
            public DMIsOpeningVault call() {
                return DMSdk.getInstance().isOpeningVault();
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<DMIsOpeningVault>() {
                    @Override
                    public void call(DMIsOpeningVault ret) {
                        if (ret != null && ret.errorCode == DMRet.ACTION_SUCCESS
                                && ret.isOpen) {
                            mainFragment.doFileOperation(
                                    FileOperationService.FILE_OP_ENCRYPTED, files,
                                    BaseValue.ValutPath);
                        } else {
                            showVaultSwitchDialog();
                        }
                    }
                });


        mSubscriptions.add(subscription);

    }

    protected void showVaultSwitchDialog() {

        String continueStr = getString(R.string.DM_Access_Vault_Notset_Open_Toset);
        String cancelStr = getString(R.string.DM_Access_Vault_Notset_Open_Giveup);
        String[] array = new String[]{cancelStr, continueStr};

        String message = getString(R.string.DM_Access_Vault_Notset_Open);

        promptDialog = AlertDmDialogDefault.prompt(this, message, null,
                new AlertDmDialogDefault.OnPromptListener() {

                    @Override
                    public void onPromptPositive() {
                        // TODO Auto-generated method stub
                        promptDialog.cancel();
                        Intent mIntent = new Intent(MainActivity.this,
                                VaultSettingActivity.class);
                        MainActivity.this.startActivity(mIntent);
                    }

                    @Override
                    public void onPromptNegative() {
                        // TODO Auto-generated method stub
                        promptDialog.cancel();
                    }

                    @Override
                    public void onPromptMid() {
                        // TODO Auto-generated method stub
                    }
                }, array, 2);
    }

    protected void onClickHide(final boolean hide, final DMFile file) {
        // TODO Auto-generated method stub
        CommonAsync.Runnable runnable = new CommonAsync.Runnable() {

            @Override
            public void stop() {
                // TODO Auto-generated method stub

            }

            @Override
            public Object run() {
                // TODO Auto-generated method stub
                if (mainFragment.isFileInPictureType(file)) {
                    return DMSdk.getInstance().setAlbumHide(file.mPath, hide);
                }
                return DMSdk.getInstance().setFileHide(file.mPath, hide);
            }
        };

        CommonAsync.CommonAsyncListener listener = new CommonAsync.CommonAsyncListener() {

            @Override
            public void onResult(Object result) {
                // TODO Auto-generated method stub
                int ret = (int) result;
                System.out.println("sethide ret:" + ret);
                if (ret == DMRet.ACTION_SUCCESS) {
                    mainFragment.setEditState(EditState.STATE_NORMAL);
                    if (mainFragment.isFileInPictureType(file)) {
                        mHandler.postDelayed(new Runnable() {

                            @Override
                            public void run() {
                                // TODO Auto-generated method stub
                                mainFragment.reloadItems();
                            }
                        }, 1000);
                    } else {
                        mainFragment.reloadItems();
                    }

                } else {
                    if (ret == 10262) {
                        Toast.makeText(MainActivity.this,
                                R.string.DM_Task_Filesystem_Not_Surpport,
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.this,
                                R.string.DM_SetUI_Failed_Operation,
                                Toast.LENGTH_SHORT).show();
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

    protected void onClickThirdParty(DMFile file) {
        // TODO Auto-generated method stub
        if (file.getType() == DMFileCategoryType.E_BOOK_CATEGORY
                && file.mLocation == DMFile.LOCATION_UDISK
                && !file.mPath.toLowerCase().endsWith(".txt")) {
            mainFragment.downloadFileToDO(MainActivity.this, file,
                    mainFragment.DOWN_TO_OPEN);
        } else if (file.getType() == DMFileCategoryType.E_PICTURE_CATEGORY
                && file.mLocation == DMFile.LOCATION_UDISK) {
            mainFragment.downloadFileToDO(MainActivity.this, file,
                    mainFragment.DOWN_TO_OPEN);
        } else
            FileUtil.thirdPartOpen(file, MainActivity.this);
    }

    protected void onClickShare(DMFile file) {
        // TODO Auto-generated method stub
        mainFragment.shareFile(file);
    }

    private void onClickRename(DMFile file) {
        // TODO Auto-generated method stub
        mainFragment.renameFile(file);
    }

    protected void onClickDetail(List<DMFile> files) {
        // TODO Auto-generated method stub
        AttributeTask mAttributeTask = null;

        if (files.size() == 1) {
            if (mainFragment.isFileInPictureType(files.get(0))) {
                mAttributeTask = new AttributeTask(this, true, files.get(0));
            } else {
                mAttributeTask = new AttributeTask(this, files.get(0));
            }

        } else {
            mAttributeTask = new AttributeTask(this, files);
        }
        mAttributeTask.execute();
    }

    private List<String> getPopData(List<DMFile> files) {
        // TODO Auto-generated method stub
        List<String> data = new ArrayList<>();
//        data.add(getString(R.string.DM_Task_File_Encrypted));
        if (files.size() == 1) {

            if (DMSupportFunction.isSupportFileHide(BaseValue.supportFucntion)) {
                if (files.get(0).mHidden) {
                    data.add(getString(R.string.DM_Task_File_Unhide));
                } else {
                    data.add(getString(R.string.DM_Task_File_Hide));
                }
            }

            if (files.get(0).isDir == true) {
                if (!mainFragment.isFileInPictureType(files.get(0))) {
                    data.add(getString(R.string.DM_Task_Rename));
                }
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

    public void setGuideBarVisible(int show) {
        // TODO Auto-generated method stub
        findViewById(R.id.bottom).setVisibility(show);
    }

    private void getSupportFunctions() {
        // TODO Auto-generated method stub
        new Thread(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                int type = BaseValue.supportFucntion = DMSdk.getInstance()
                        .getSupportFunction();

                if (DMSupportFunction.isSupportVault(type)) {
                    DMVaultPath tmpDMVaultPath = DMSdk.getInstance().getVaultPath();
                    if (tmpDMVaultPath != null && tmpDMVaultPath.errorCode == DMRet.ACTION_SUCCESS) {
                        BaseValue.ValutPath = tmpDMVaultPath.path.replace("/tmp/mnt/", "");
                    }
                }

                EventBus.getDefault().post(new SupportFunctionEvent(type));

                Message msg = new Message();
                msg.what = REFRESH_TYPE_LAYOUT;

                DMLog.d("getSupportFunctions", "SupportFunction:" + type);

                if (DMSupportFunction.isSupportClassify(type)) {
                    msg.arg1 = 1;
                    System.out.println("Support");
                } else {
                    msg.arg1 = 0;
                    System.out.println("Not Support");
                }

                mHandler.sendMessage(msg);

                if (DMSupportFunction.isSupportBackup(type)) {
                    DMLog.d("getSupportFunctions", "SupportBackup");
                    DMBindInfo info = DMSdk.getInstance().getBindInfo();

                    if (info != null) {

                        try {
                            if (info.mStatus == DMBindInfo.BINDED_MOUNTED) {

                                BackupService.tmpMac = mDevice.getMac();
                                if (!BackupSettingDB.getInstance()
                                        .existDiskMac(BackupService.tmpMac)) {
                                    BakSetBean bean = new BakSetBean(
                                            BackupService.tmpMac,
                                            BakSetBean.TRUE,
                                            BakSetBean.TRUE,
                                            BakSetBean.FALSE,
                                            BakSetBean.TRUE,
                                            DMSdk.getInstance().getBindInfo().mId,
                                            GetBakLocationTools
                                                    .getNewMediaBakFolder(
                                                            MainActivity.this,
                                                            DMSdk.getInstance()
                                                                    .getBindInfo().mId),
                                            String.valueOf(DMSdk.getInstance()
                                                    .getBindInfo().mTotalSize),
                                            BakSetBean.FALSE, BakSetBean.FALSE);
                                    BackupSettingDB.getInstance()
                                            .addDiskSetting(bean);
                                }

                                if (getSharedPreferences("BACKUP",
                                        Context.MODE_PRIVATE).getBoolean(
                                        "AUTO", false)) {
                                    startAutoBackup();
                                }
                            }
                        } catch (Exception e) {
                            // TODO: handle exception
                        }
                    }

                }

                Message msg2 = new Message();
                msg2.what = REFRESH_COPYTO_LAYOUT;

                if (DMSupportFunction.isSupportCopyto(type)) {
                    msg2.arg1 = 1;
                } else {
                    msg2.arg1 = 0;
                }
                mHandler.sendMessage(msg2);

                Message msg3 = new Message();
                msg3.what = REFRESH_SAFEEXIT;

                if (DMSupportFunction.isSupportSafetyExit(type)) {
                    msg3.arg1 = 1;
                } else {
                    msg3.arg1 = 0;
                }
                mHandler.sendMessage(msg3);

            }
        }).start();
    }

    protected void startAutoBackup() {
        // TODO Auto-generated method stub
        System.out.println("startAutoBackup");
        Intent mIntent = new Intent(MainActivity.this, BackupService.class);
        mIntent.putExtra("backUp_Type", BuckupType.BUTYPE_ALL.ordinal());
        startService(mIntent);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        // TODO Auto-generated method stub
        super.onNewIntent(intent);
        if (intent != null) {
            boolean isExit = intent.getBooleanExtra(TAG_EXIT, false);
            if (isExit) {
                this.finish();
            }
        }
    }

    // 关闭服务
    private int stopPrivateServiceWapper() {
        int ret = 0;
        if (isServiceInited.compareAndSet(true, false)) {
            ret = DMSdk.getInstance().stopPrivateService();

        }
        return ret;
    }

    // 开启服务
    private int startPrivateServiceWapper() {
        int ret = 0;
        if (isServiceInited.compareAndSet(false, true)) {
            ret = DMSdk.getInstance().startPrivateService(this);

        }
        return ret;
    }


}
