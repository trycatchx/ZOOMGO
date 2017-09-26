
package com.dmsys.vlcplayer;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.KeyguardManager;
import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothHeadset;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.media.MediaRouter;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.ViewStubCompat;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.GestureDetector;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.ScaleGestureDetector;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLayoutChangeListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.dmsys.vlcplayer.adapter.ChooseSrtAdapter;
import com.dmsys.vlcplayer.db.PlayRecordBean;
import com.dmsys.vlcplayer.db.VlcPlayRecordDB;
import com.dmsys.vlcplayer.interfaces.IPlaybackSettingsController;
import com.dmsys.vlcplayer.subtitle.Caption;
import com.dmsys.vlcplayer.subtitle.ChooseSrtBean;
import com.dmsys.vlcplayer.subtitle.TimedTextObject;
import com.dmsys.vlcplayer.util.AndroidDevices;
import com.dmsys.vlcplayer.util.FileUtils;
import com.dmsys.vlcplayer.util.Preferences;
import com.dmsys.vlcplayer.util.Strings;
import com.dmsys.vlcplayer.util.SubtitleTool;
import com.dmsys.vlcplayer.util.Util;
import com.dmsys.vlcplayer.util.VLCInstance;

import org.videolan.libvlc.IVLCVout;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;
import org.videolan.libvlc.util.AndroidUtil;
import org.videolan.medialibrary.Medialibrary;
import org.videolan.medialibrary.Tools;
import org.videolan.medialibrary.media.MediaWrapper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class VideoPlayerActivity extends AppCompatActivity implements IVLCVout.Callback, IVLCVout.OnNewVideoLayoutListener,
        IPlaybackSettingsController, PlaybackService.Client.Callback, PlaybackService.Callback, OnClickListener, View.OnLongClickListener, ScaleGestureDetector.OnScaleGestureListener {

    public final static String TAG = "VLC/VideoPlayerActivity";
    public CompositeSubscription mCompositeSubscription = new CompositeSubscription();
    // Internal intent identifier to distinguish between internal launch and
    // external intent.
    public final static String PLAY_FROM_VIDEOGRID = Strings.buildPkgString("gui.video.PLAY_FROM_VIDEOGRID");
    public final static String PLAY_FROM_SERVICE = Strings.buildPkgString("gui.video.PLAY_FROM_SERVICE");
    public final static String EXIT_PLAYER = Strings.buildPkgString("gui.video.EXIT_PLAYER");

    public final static String PLAY_EXTRA_ITEM_LOCATION = "item_location";
    public final static String PLAY_EXTRA_ITEMS_LOCATION = "items_location";
    public final static String PLAY_EXTRA_SUBTITLES_LOCATION = "subtitles_location";
    public final static String PLAY_EXTRA_ITEM_TITLE = "title";
    public final static String PLAY_EXTRA_FROM_START = "from_start";
    public final static String PLAY_EXTRA_START_TIME = "position";
    public final static String PLAY_EXTRA_OPENED_POSITION = "opened_position";
    public final static String PLAY_DISABLE_HARDWARE = "disable_hardware";

    public final static String ACTION_RESULT = Strings.buildPkgString("player.result");
    public final static String EXTRA_POSITION = "extra_position";
    public final static String EXTRA_DURATION = "extra_duration";
    public final static String EXTRA_URI = "extra_uri";
    public final static int RESULT_CONNECTION_FAILED = RESULT_FIRST_USER + 1;
    public final static int RESULT_PLAYBACK_ERROR = RESULT_FIRST_USER + 2;
    public final static int RESULT_HARDWARE_ACCELERATION_ERROR = RESULT_FIRST_USER + 3;
    public final static int RESULT_VIDEO_TRACK_LOST = RESULT_FIRST_USER + 4;
    private static final float DEFAULT_FOV = 80f;
    public static final float MIN_FOV = 20f;
    public static final float MAX_FOV = 150f;

    private final PlaybackServiceActivity.Helper mHelper = new PlaybackServiceActivity.Helper(this, this);
    protected PlaybackService mService;
    private Medialibrary mMedialibrary;
    private SurfaceView mSurfaceView = null;
    private SurfaceView mSubtitlesSurfaceView = null;
    private View mRootView;
    private FrameLayout mSurfaceFrame;
    protected MediaRouter mMediaRouter;
    private MediaRouter.SimpleCallback mMediaRouterCallback;
    private int mPresentationDisplayId = -1;
    private Uri mUri;
    private boolean mAskResume = true;
    private ScaleGestureDetector mScaleGestureDetector;
    private GestureDetectorCompat mDetector = null;

    private ImageView mPlaylistNext;
    private ImageView mPlaylistPrevious;

    private static final int SURFACE_BEST_FIT = 0;
    private static final int SURFACE_FIT_SCREEN = 1;
    private static final int SURFACE_FILL = 2;
    private static final int SURFACE_16_9 = 3;
    private static final int SURFACE_4_3 = 4;
    private static final int SURFACE_ORIGINAL = 5;
    private int mCurrentSize;

    private SharedPreferences mSettings;
    private int mTouchControls = 0;

    /**
     * Overlay
     */
    private ActionBar mActionBar;
    private ViewGroup mActionBarView;
    private View mOverlayProgress;
    private View mOverlayBackground;
    private View mOverlayButtons;
    private static final int OVERLAY_TIMEOUT = 4000;
    private static final int OVERLAY_INFINITE = -1;
    private static final int FADE_OUT = 1;
    private static final int SHOW_PROGRESS = 2;
    private static final int FADE_OUT_INFO = 3;
    private static final int START_PLAYBACK = 4;
    private static final int AUDIO_SERVICE_CONNECTION_FAILED = 5;
    private static final int RESET_BACK_LOCK = 6;
    private static final int CHECK_VIDEO_TRACKS = 7;
    private static final int LOADING_ANIMATION = 8;
    private static final int SHOW_INFO = 9;
    private static final int HIDE_INFO = 10;

    private static final int LOADING_ANIMATION_DELAY = 1000;

    private boolean mDragging;
    private boolean mShowing;
    private IPlaybackSettingsController.DelayState mPlaybackSetting = DelayState.OFF;
    private SeekBar mSeekbar;
    private TextView mTitle;
    private ImageView mBack;
    private TextView mSysTime;
    private TextView mBattery;
    private TextView mTime;
    private TextView mLength;
    private TextView mInfo;
    private View mOverlayInfo;
    private View mVerticalBar;
    private View mVerticalBarProgress;
    private boolean mIsLoading;
    private boolean mIsPlaying = false;
    private ImageView mLoading;
    private ImageView mPlayPause;
    private ImageView mPlayPre;
    private ImageView mPlayNext;
    private Button mSubtitle;
    private ImageView mPlaybackSettingPlus;
    private ImageView mPlaybackSettingMinus;
    private View mObjectFocused;
    private boolean mEnableBrightnessGesture;
    protected boolean mEnableCloneMode;
    private boolean mDisplayRemainingTime;
    private int mScreenOrientation;
    private int mScreenOrientationLock;
    private int mCurrentScreenOrientation;
    private ImageView mLock;
    //    private ImageView mSize;
    private ImageView mOrientation;
    private String KEY_REMAINING_TIME_DISPLAY = "remaining_time_display";
    private String KEY_BLUETOOTH_DELAY = "key_bluetooth_delay";
    private long mSpuDelay = 0L;
    private long mAudioDelay = 0L;
    private boolean mRateHasChanged = false;
    private int mCurrentAudioTrack = -2, mCurrentSpuTrack = -2;

    private boolean mIsLocked = false;
    /* -1 is a valid track (Disable) */
    private int mLastAudioTrack = -2;
    private int mLastSpuTrack = -2;
    private int mOverlayTimeout = 0;
    private boolean mLockBackButton = false;
    boolean mWasPaused = false;
    private long mSavedTime = -1;
    private float mSavedRate = 1.f;

    /**
     * For uninterrupted switching between audio and video mode
     */
    private boolean mSwitchingView;
    private boolean mSwitchToPopup;
    private boolean mHasSubItems = false;

    // size of the video
    private int mVideoHeight;
    private int mVideoWidth;
    private int mVideoVisibleHeight;
    private int mVideoVisibleWidth;
    private int mSarNum;
    private int mSarDen;

    //Volume
    private AudioManager mAudioManager;
    private int mAudioMax;
    private boolean mMute = false;
    private int mVolSave;
    private float mVol;

    //Touch Events
    private static final int TOUCH_NONE = 0;
    private static final int TOUCH_VOLUME = 1;
    private static final int TOUCH_BRIGHTNESS = 2;
    private static final int TOUCH_MOVE = 3;
    private static final int TOUCH_SEEK = 4;
    private int mTouchAction = TOUCH_NONE;
    private int mSurfaceYDisplayRange, mSurfaceXDisplayRange;
    private float mFov;
    private float mInitTouchY, mTouchY = -1f, mTouchX = -1f;

    //stick event
    private static final int JOYSTICK_INPUT_DELAY = 300;
    private long mLastMove;

    // Brightness
    private boolean mIsFirstBrightnessGesture = true;
    private float mRestoreAutoBrightness = -1f;

    // Tracks & Subtitles
    private MediaPlayer.TrackDescription[] mAudioTracksList;
    private MediaPlayer.TrackDescription[] mSubtitleTracksList;
    /**
     * Used to store a selected subtitle; see onActivityResult.
     * It is possible to have multiple custom subs in one session
     * (just like desktop VLC allows you as well.)
     */
    private volatile ArrayList<String> mSubtitleSelectedFiles = new ArrayList<>();
    private volatile String mSubTitleDir;
    /**
     * Flag to indicate whether the media should be paused once loaded
     * (e.g. lock screen, or to restore the pause state)
     */
    private boolean mPlaybackStarted = false;

    // Navigation handling (DVD, Blu-Ray...)
    private int mMenuIdx = -1;
    private boolean mIsNavMenu = false;

    /* for getTime and seek */
    private long mForcedTime = -1;
    private long mLastTime = -1;

    private OnLayoutChangeListener mOnLayoutChangeListener;
    private AlertDialog mAlertDialog;

    DisplayMetrics mScreen = new DisplayMetrics();
    Subscription parseSubtitleSubscription;
    Subscription openSrtSubscription;

    private LibVLC LibVLC() {
        return VLCInstance.get(this);
    }

    @Override
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!VLCInstance.testCompatibleCPU(this)) {
            exit(RESULT_CANCELED);
            return;
        }


        mSettings = PreferenceManager.getDefaultSharedPreferences(this);


        mTouchControls = (mSettings.getBoolean("enable_volume_gesture", true) ? 1 : 0)
                + (mSettings.getBoolean("enable_brightness_gesture", true) ? 2 : 0);

        /* Services and miscellaneous */
        mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        mAudioMax = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

        mEnableCloneMode = mSettings.getBoolean("enable_clone_mode", false);
        setContentView(R.layout.player);

        /** initialize Views an their Events */
        mActionBar = getSupportActionBar();


        mActionBar.setHomeButtonEnabled(false);
        mActionBar.setDisplayShowTitleEnabled(false);
        mActionBar.setDisplayShowCustomEnabled(true);
        mActionBar.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        View view = getLayoutInflater().inflate(R.layout.player_action_bar, null);
        android.support.v7.app.ActionBar.LayoutParams layout =
                new android.support.v7.app.ActionBar.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        mActionBar.setCustomView(view, layout);

        mActionBarView = (ViewGroup) mActionBar.getCustomView();
        mActionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);

        Toolbar toolbar=(Toolbar)mActionBarView.getParent();
        toolbar.setContentInsetsAbsolute(0, 0);
        toolbar.getContentInsetEnd();
        toolbar.setPadding(0, 0, 0, 0);
        toolbar.setBackgroundColor(getResources().getColor(R.color.transparent));
        dimStatusBar(false);

        mRootView = findViewById(R.id.player_root);




        mTitle = (TextView) mActionBarView.findViewById(R.id.player_overlay_title);
        mBack = (ImageView) mActionBarView.findViewById(R.id.player_overlay_back);

        mScreenOrientation = Integer.valueOf(
                mSettings.getString("screen_orientation", "99" /*SCREEN ORIENTATION SENSOR*/));

        mSurfaceView = (SurfaceView) findViewById(R.id.player_surface);
        mSubtitlesSurfaceView = (SurfaceView) findViewById(R.id.subtitles_surface);

        mSubtitlesSurfaceView.setZOrderMediaOverlay(true);
        mSubtitlesSurfaceView.getHolder().setFormat(PixelFormat.TRANSLUCENT);

        mSurfaceFrame = (FrameLayout) findViewById(R.id.player_surface_frame);

        /* Loading view */
        mLoading = (ImageView) findViewById(R.id.player_overlay_loading);
        mLock = (ImageView) findViewById(R.id.lock_overlay_button);
        layout_srt = (LinearLayout) findViewById(R.id.layout_srt);
        mTVSrt1 = (TextView) findViewById(R.id.vod_player_tv_srt1);
        mTVSrt2 = (TextView) findViewById(R.id.vod_player_tv_srt2);
        showOverlay();
        mHandler.sendEmptyMessageDelayed(LOADING_ANIMATION, LOADING_ANIMATION_DELAY);

        mSwitchingView = false;

        mAskResume = mSettings.getBoolean("dialog_confirm_resume", false);
        mDisplayRemainingTime = mSettings.getBoolean(KEY_REMAINING_TIME_DISPLAY, false);
        // Clear the resume time, since it is only used for resumes in external
        // videos.
        Editor editor = mSettings.edit();
        editor.putLong(Preferences.VIDEO_RESUME_TIME, -1);
        // Also clear the subs list, because it is supposed to be per session
        // only (like desktop VLC). We don't want the custom subtitle files
        // to persist forever with this video.
        editor.putString(Preferences.VIDEO_SUBTITLE_FILES, null);
        // Paused flag - per session too, like the subs list.
        editor.remove(Preferences.VIDEO_PAUSED);
        editor.apply();

        IntentFilter filter = new IntentFilter();
        if (mBattery != null)
            filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(mReceiver, filter);

        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);

        // 100 is the value for screen_orientation_start_lock
        //setRequestedOrientation(getScreenOrientation(100));

        getWindowManager().getDefaultDisplay().getMetrics(mScreen);
        mSurfaceYDisplayRange = Math.min(mScreen.widthPixels, mScreen.heightPixels);
        mSurfaceXDisplayRange = Math.max(mScreen.widthPixels, mScreen.heightPixels);
        mCurrentScreenOrientation = getResources().getConfiguration().orientation;
        mCurrentSize = mSettings.getInt(Preferences.VIDEO_RATIO, SURFACE_BEST_FIT);

        VLCInstance.get(this); // ensure VLC is loaded before medialibrary
        mMedialibrary = Medialibrary.getInstance(this);

        startListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        /*
         * Set listeners here to avoid NPE when activity is closing
         */
        setHudClickListeners();

        //if (mIsLocked && mScreenOrientation == 99)
        //   setRequestedOrientation(mScreenOrientationLock);
    }

    private void setHudClickListeners() {
        if (mSeekbar != null)
            mSeekbar.setOnSeekBarChangeListener(mSeekListener);

        if (mLock != null) {
            mLock.setOnClickListener(this);
        }
        if (mPlayPause != null) {
            mPlayPause.setOnClickListener(this);
        }
        if (mPlayPre != null) {
            mPlayPre.setOnClickListener(this);
        }
        if (mPlayNext != null) {
            mPlayNext.setOnClickListener(this);
        }
        if (mLength != null) {
            mLength.setOnClickListener(this);
        }
        if (mTime != null) {
            mTime.setOnClickListener(this);
        }
        if (mOrientation != null) {
            mOrientation.setOnClickListener(this);
        }
        if (mBack != null) {
            mBack.setOnClickListener(this);
        }
        if (mSubtitle != null) {
            mSubtitle.setOnClickListener(this);
        }
    }


    @Override
    protected void onNewIntent(Intent intent) {
        System.out.println("onNewIntent");
        setIntent(intent);
        if (mPlaybackStarted && mService.getCurrentMediaWrapper() != null) {
            String[] uris = intent.hasExtra(PLAY_EXTRA_ITEMS_LOCATION) ?
                    intent.getStringArrayExtra(PLAY_EXTRA_ITEMS_LOCATION) : null;
            int position = intent.getIntExtra(PLAY_EXTRA_OPENED_POSITION, -1);
            String tagerPath = null;
            if (uris != null && uris.length > 0
                    && position >= 0) {
                tagerPath = uris[position];
            }

            String curUri = null;
            if (mService != null) {
                curUri = mService.getCurrentMediaLocation();
            }

            if (curUri == null || tagerPath == null) {
                return;
            }

            if (tagerPath != null && curUri != null && curUri.equals(uris[position])) {
                return;
            }

            Uri tagerUri = Uri.parse(tagerPath);


            if (TextUtils.equals("file", tagerUri.getScheme()) && tagerUri.getPath().startsWith("/sdcard")) {
                Uri convertedUri = FileUtils.convertLocalUri(tagerUri);
                if (convertedUri == null || convertedUri.equals(mUri)) {
                    return;
                } else {
                    tagerUri = convertedUri;
                }
            }
            mUri = tagerUri;
            mTitle.setText(mService.getCurrentMediaWrapper().getTitle());

            showTitle();
            initUI();
            setPlaybackParameters();
            mForcedTime = mLastTime = -1;
            setOverlayProgress();
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onPause() {
        super.onPause();
        hideOverlay(true);
        if (mSeekbar != null)
            mSeekbar.setOnSeekBarChangeListener(null);
        if (mLock != null)
            mLock.setOnClickListener(null);
        if (mPlayPause != null)
            mPlayPause.setOnClickListener(null);
        if (mPlayPre != null)
            mPlayPre.setOnClickListener(null);

        if (mPlayNext != null)
            mPlayNext.setOnClickListener(null);

        if (mLength != null)
            mLength.setOnClickListener(null);
        if (mTime != null)
            mTime.setOnClickListener(null);
        if (mOrientation != null)
            mOrientation.setOnClickListener(null);

        /* Stop the earliest possible to avoid vout error */
        if (!isInPictureInPictureMode() && (isFinishing()))
            stopPlayback();
    }

    @Override
    public void onVisibleBehindCanceled() {
        super.onVisibleBehindCanceled();
        stopPlayback();
        exitOK();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Log.d("liutao", "onConfigurationChanged: ");

        super.onConfigurationChanged(newConfig);

        if (!AndroidUtil.isHoneycombOrLater)
            changeSurfaceLayout();

        getWindowManager().getDefaultDisplay().getMetrics(mScreen);
        mCurrentScreenOrientation = newConfig.orientation;
        mSurfaceYDisplayRange = Math.min(mScreen.widthPixels, mScreen.heightPixels);
        mSurfaceXDisplayRange = Math.max(mScreen.widthPixels, mScreen.heightPixels);
    }


    @Override
    protected void onStart() {
        super.onStart();
        mMedialibrary.pauseBackgroundOperations();
        mHelper.onStart();
        if (mSettings.getBoolean("save_brightness", false)) {
            float brightness = mSettings.getFloat("brightness_value", -1f);
            if (brightness != -1f)
                setWindowBrightness(brightness);
        }
        System.out.println("start playback service 22!!!");
        IntentFilter filter = new IntentFilter(PLAY_FROM_SERVICE);
        filter.addAction(EXIT_PLAYER);
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mServiceReceiver, filter);
        if (mBtReceiver != null) {
            IntentFilter btFilter = new IntentFilter(BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED);
            btFilter.addAction(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED);
            registerReceiver(mBtReceiver, btFilter);
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    protected void onStop() {
        super.onStop();
        long time1 = System.currentTimeMillis();

        System.out.println("liutao onStop");
        mMedialibrary.resumeBackgroundOperations();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mServiceReceiver);

        if (mBtReceiver != null)
            unregisterReceiver(mBtReceiver);
        if (mAlertDialog != null && mAlertDialog.isShowing())
            mAlertDialog.dismiss();
        if (!isFinishing() && mService != null && mService.isPlaying() &&
                mSettings.getBoolean(Preferences.VIDEO_BACKGROUND, false)) {
            switchToAudioMode(false);
        }

        stopPlayback();

        Editor editor = mSettings.edit();
        System.out.println("liutao mSavedTime3:" + mSavedTime);
        if (mSavedTime != -1)
            editor.putLong(Preferences.VIDEO_RESUME_TIME, mSavedTime);


        editor.putFloat(Preferences.VIDEO_RATE, mSavedRate);

        // Save selected subtitles
        String subtitleList_serialized = null;
        if (mSubtitleSelectedFiles.size() > 0) {
            Log.d(TAG, "Saving selected subtitle files");
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            try {
                ObjectOutputStream oos = new ObjectOutputStream(bos);
                oos.writeObject(mSubtitleSelectedFiles);
                subtitleList_serialized = bos.toString();
            } catch (IOException e) {
            }
        }
        editor.putString(Preferences.VIDEO_SUBTITLE_FILES, subtitleList_serialized);
        editor.apply();

        restoreBrightness();

        if (mSubtitlesGetTask != null)
            mSubtitlesGetTask.cancel(true);

        if (mService != null)
            mService.removeCallback(this);
        mHelper.onStop();

        System.out.println("stop2" + (System.currentTimeMillis() - time1));
    }

    private void restoreBrightness() {
        if (mRestoreAutoBrightness != -1f) {
            int brightness = (int) (mRestoreAutoBrightness * 255f);
            setWindowBrightness(brightness);
        }
        // Save brightness if user wants to
        if (mSettings.getBoolean("save_brightness", false)) {
            float brightness = getWindow().getAttributes().screenBrightness;
            if (brightness != -1f) {
                Editor editor = mSettings.edit();
                editor.putFloat("brightness_value", brightness);
                editor.apply();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mReceiver != null)
            unregisterReceiver(mReceiver);

        mAudioManager = null;


    }

    /**
     * Add or remove MediaRouter callbacks. This is provided for version targeting.
     *
     * @param add true to add, false to remove
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void mediaRouterAddCallback(boolean add) {
        if (!AndroidUtil.isJellyBeanMR1OrLater || mMediaRouter == null) return;

        if (add)
            mMediaRouter.addCallback(MediaRouter.ROUTE_TYPE_LIVE_VIDEO, mMediaRouterCallback);
        else
            mMediaRouter.removeCallback(mMediaRouterCallback);
    }


    /**
     * 开始播放
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void startPlayback() {
        /* start playback only when audio service and both surfaces are ready */
        if (mPlaybackStarted || mService == null)
            return;

        mSavedRate = 1.0f;
        mSavedTime = -1;
        mPlaybackStarted = true;

        final IVLCVout vlcVout = mService.getVLCVout();
        if (vlcVout.areViewsAttached()) {
            if (mService.isPlayingPopup())
                mService.stop();
            vlcVout.detachViews();
        }
        vlcVout.setVideoView(mSurfaceView);
        vlcVout.setSubtitlesView(mSubtitlesSurfaceView);
        vlcVout.addCallback(this);
        vlcVout.attachViews(this);
        mService.setVideoTrackEnabled(true);

        initUI();
        //加载播放列表
        loadMedia();
        boolean ratePref = mSettings.getBoolean(Preferences.KEY_AUDIO_PLAYBACK_SPEED_PERSIST, true);
        mService.setRate(ratePref || mRateHasChanged ? mSettings.getFloat(Preferences.VIDEO_RATE, 1.0f) : 1.0F, false);

    }

    private void initUI() {

        cleanUI();

        /* Dispatch ActionBar touch events to the Activity */
        mActionBarView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                onTouchEvent(event);
                return true;
            }
        });

        if (AndroidUtil.isHoneycombOrLater) {
            if (mOnLayoutChangeListener == null) {
                mOnLayoutChangeListener = new OnLayoutChangeListener() {
                    private final Runnable mRunnable = new Runnable() {
                        @Override
                        public void run() {
                            changeSurfaceLayout();
                        }
                    };

                    @Override
                    public void onLayoutChange(View v, int left, int top, int right,
                                               int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                        if (left != oldLeft || top != oldTop || right != oldRight || bottom != oldBottom) {
                            /* changeSurfaceLayout need to be called after the layout changed */
                            mHandler.removeCallbacks(mRunnable);
                            mHandler.post(mRunnable);
                        }
                    }
                };
            }
            mSurfaceFrame.addOnLayoutChangeListener(mOnLayoutChangeListener);
        }
        changeSurfaceLayout();

        /* Listen for changes to media routes. */
        if (mMediaRouter != null)
            mediaRouterAddCallback(true);

        if (mRootView != null)
            mRootView.setKeepScreenOn(true);
    }

    private void setPlaybackParameters() {
        if (mAudioDelay != 0L && mAudioDelay != mService.getAudioDelay())
            mService.setAudioDelay(mAudioDelay);
        else if (mBtReceiver != null && (mAudioManager.isBluetoothA2dpOn() || mAudioManager.isBluetoothScoOn()))
            toggleBtDelay(true);
        if (mSpuDelay != 0L && mSpuDelay != mService.getSpuDelay())
            mService.setSpuDelay(mSpuDelay);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void stopPlayback() {
        long time1 = System.currentTimeMillis();

        if (!mPlaybackStarted)
            return;

        mWasPaused = !mService.isPlaying();
        if (!isFinishing()) {
            mCurrentAudioTrack = mService.getAudioTrack();
            mCurrentSpuTrack = mService.getSpuTrack();
        }

        if (mMute)
            mute(false);

        mPlaybackStarted = false;

        mService.setVideoTrackEnabled(false);
        mService.removeCallback(this);

        mHandler.removeCallbacksAndMessages(null);
        final IVLCVout vlcVout = mService.getVLCVout();
        vlcVout.removeCallback(this);
        vlcVout.detachViews();

        if (mSwitchingView && mService != null) {
            Log.d(TAG, "mLocation = \"" + mUri + "\"");
            if (mSwitchToPopup)
                mService.switchToPopup(mService.getCurrentMediaPosition());
            else {
                mService.getCurrentMediaWrapper().addFlags(MediaWrapper.MEDIA_FORCE_AUDIO);
                mService.showWithoutParse(mService.getCurrentMediaPosition());
            }
            return;
        }

        cleanUI();

        System.out.println("liutao isSeekable:" + mService.isSeekable());

        if (mService.isSeekable()) {
            mSavedTime = getTime();
            System.out.println("liutao mSavedTime1:" + mSavedTime);
            long length = mService.getLength();
            //remove saved position if in the last 5 seconds
            if (length - mSavedTime < 5000)
                mSavedTime = 0;
            else
                mSavedTime -= 2000; // go back 2 seconds, to compensate loading time

            System.out.println("liutao 2:" + mSavedTime);
        }

        mSavedRate = mService.getRate();
        mRateHasChanged = mSavedRate != 1.0f;

        mService.setRate(1.0f, false);
        mService.stop();

        System.out.println("stop1" + (System.currentTimeMillis() - time1));
    }

    private void cleanUI() {

        if (mRootView != null)
            mRootView.setKeepScreenOn(false);

        if (mDetector != null) {
            mDetector.setOnDoubleTapListener(null);
            mDetector = null;
        }

        /* Stop listening for changes to media routes. */
        if (mMediaRouter != null)
            mediaRouterAddCallback(false);

        if (mSurfaceFrame != null && AndroidUtil.isHoneycombOrLater && mOnLayoutChangeListener != null)
            mSurfaceFrame.removeOnLayoutChangeListener(mOnLayoutChangeListener);

        if (AndroidUtil.isICSOrLater)
            getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(null);

        mActionBarView.setOnTouchListener(null);
    }


    public static void startOpened(Context context, Uri uri, int openedPosition) {
        start(context, uri, null, false, openedPosition);
    }


    //
    private static void start(Context context, Uri uri, String title, boolean fromStart, int openedPosition) {
        String[] uris = {uri.toString()};

        Intent intent = getIntent(PLAY_FROM_VIDEOGRID, context, uris, fromStart, openedPosition);
        context.startActivity(intent);
    }


    public static void start(Context context, List<String> uris, boolean fromStart, int openedPosition) {
        Intent intent = getIntent(PLAY_FROM_VIDEOGRID, context, uris.toArray(new String[0]), fromStart, openedPosition);
        context.startActivity(intent);
    }


    @NonNull
    public static Intent getIntent(String action, Context context, String[] uris, boolean fromStart, int openedPosition) {
        Intent intent = new Intent(context, VideoPlayerActivity.class);
        intent.setAction(action);
        intent.putExtra(PLAY_EXTRA_ITEMS_LOCATION, uris);
        intent.putExtra(PLAY_EXTRA_FROM_START, fromStart);

        if (openedPosition != -1 || !(context instanceof Activity)) {
            if (openedPosition != -1)
                intent.putExtra(PLAY_EXTRA_OPENED_POSITION, openedPosition);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        return intent;
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equalsIgnoreCase(Intent.ACTION_BATTERY_CHANGED)) {
                if (mBattery == null)
                    return;
                int batteryLevel = intent.getIntExtra("level", 0);
                if (batteryLevel >= 50)
                    mBattery.setTextColor(Color.GREEN);
                else if (batteryLevel >= 30)
                    mBattery.setTextColor(Color.YELLOW);
                else
                    mBattery.setTextColor(Color.RED);
                mBattery.setText(String.format("%d%%", batteryLevel));
            }
        }
    };

    protected void exit(int resultCode) {
        if (isFinishing())
            return;
        Intent resultIntent = new Intent(ACTION_RESULT);
        if (mUri != null && mService != null) {
            if (AndroidUtil.isNougatOrLater)
                resultIntent.putExtra(EXTRA_URI, mUri.toString());
            else
                resultIntent.setData(mUri);
            resultIntent.putExtra(EXTRA_POSITION, mService.getTime());
            resultIntent.putExtra(EXTRA_DURATION, mService.getLength());
        }
        setResult(resultCode, resultIntent);
        finish();
    }

    private void exitOK() {
        exit(RESULT_OK);
    }

    @Override
    public boolean onTrackballEvent(MotionEvent event) {
        if (mIsLoading)
            return false;
        showOverlay();
        return true;
    }

    @TargetApi(12) //only active for Android 3.1+
    public boolean dispatchGenericMotionEvent(MotionEvent event) {
        if (mIsLoading)
            return false;
        //Check for a joystick event
        if ((event.getSource() & InputDevice.SOURCE_JOYSTICK) !=
                InputDevice.SOURCE_JOYSTICK ||
                event.getAction() != MotionEvent.ACTION_MOVE)
            return false;

        InputDevice mInputDevice = event.getDevice();

        float dpadx = event.getAxisValue(MotionEvent.AXIS_HAT_X);
        float dpady = event.getAxisValue(MotionEvent.AXIS_HAT_Y);
        if (mInputDevice == null || Math.abs(dpadx) == 1.0f || Math.abs(dpady) == 1.0f)
            return false;

        float x = AndroidDevices.getCenteredAxis(event, mInputDevice,
                MotionEvent.AXIS_X);
        float y = AndroidDevices.getCenteredAxis(event, mInputDevice,
                MotionEvent.AXIS_Y);
        float rz = AndroidDevices.getCenteredAxis(event, mInputDevice,
                MotionEvent.AXIS_RZ);

        if (System.currentTimeMillis() - mLastMove > JOYSTICK_INPUT_DELAY) {
            if (Math.abs(x) > 0.3) {
                seekDelta(x > 0.0f ? 10000 : -10000);
            } else if (Math.abs(y) > 0.3) {
                if (mIsFirstBrightnessGesture)
                    initBrightnessTouch();
                changeBrightness(-y / 10f);
            } else if (Math.abs(rz) > 0.3) {
                mVol = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                int delta = -(int) ((rz / 7) * mAudioMax);
                int vol = (int) Math.min(Math.max(mVol + delta, 0), mAudioMax);
                setAudioVolume(vol);
            }
            mLastMove = System.currentTimeMillis();
        }
        return true;
    }

    // 点击back键退出播放器时，两次点击间隔3s
    private static final long QuiteInteval = 2000; // 3s
    private long mLastKeyTime = 0;

    @Override
    public void onBackPressed() {
        long curTime = System.currentTimeMillis();

        if (curTime - mLastKeyTime > QuiteInteval) {
            Toast.makeText(this, R.string.DM_Vod_Toast_Key_Back_Quit, Toast.LENGTH_LONG).show();
            mLastKeyTime = curTime;

        } else {
            exitOK();
            super.onBackPressed();
        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_BUTTON_B)
            return super.onKeyDown(keyCode, event);
        if (mPlaybackSetting != IPlaybackSettingsController.DelayState.OFF)
            return false;
        if (mIsLoading) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_S:
                case KeyEvent.KEYCODE_MEDIA_STOP:
                    exitOK();
                    return true;
            }
            return false;
        }

        if (mShowing || (mFov == 0f && keyCode == KeyEvent.KEYCODE_DPAD_DOWN))
            showOverlayTimeout(OVERLAY_TIMEOUT);
        switch (keyCode) {
            case KeyEvent.KEYCODE_F:
            case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD:
                seekDelta(10000);
                return true;
            case KeyEvent.KEYCODE_R:
            case KeyEvent.KEYCODE_MEDIA_REWIND:
                seekDelta(-10000);
                return true;
            case KeyEvent.KEYCODE_BUTTON_R1:
                seekDelta(60000);
                return true;
            case KeyEvent.KEYCODE_BUTTON_L1:
                seekDelta(-60000);
                return true;
            case KeyEvent.KEYCODE_BUTTON_A:
                if (mOverlayProgress != null && mOverlayProgress.getVisibility() == View.VISIBLE)
                    return false;
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
            case KeyEvent.KEYCODE_MEDIA_PLAY:
            case KeyEvent.KEYCODE_MEDIA_PAUSE:
            case KeyEvent.KEYCODE_SPACE:
                if (mIsNavMenu)
                    return navigateDvdMenu(keyCode);
                else if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) //prevent conflict with remote control
                    return super.onKeyDown(keyCode, event);
                else
                    doPlayPause();
                return true;
            case KeyEvent.KEYCODE_O:
            case KeyEvent.KEYCODE_BUTTON_Y:
            case KeyEvent.KEYCODE_MENU:
                return true;
            case KeyEvent.KEYCODE_V:
            case KeyEvent.KEYCODE_MEDIA_AUDIO_TRACK:
            case KeyEvent.KEYCODE_BUTTON_X:
                return true;
            case KeyEvent.KEYCODE_N:
                return true;
            case KeyEvent.KEYCODE_A:
                resizeVideo();
                return true;
            case KeyEvent.KEYCODE_M:
            case KeyEvent.KEYCODE_VOLUME_MUTE:
                updateMute();
                return true;
            case KeyEvent.KEYCODE_S:
            case KeyEvent.KEYCODE_MEDIA_STOP:
                exitOK();
                return true;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                if (!mShowing) {
                    if (mFov == 0f)
                        seekDelta(-10000);
                    else
                        mService.updateViewpoint(-5f, 0f, 0f, 0f, false);
                    return true;
                }
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                if (!mShowing) {
                    if (mFov == 0f)
                        seekDelta(10000);
                    else
                        mService.updateViewpoint(5f, 0f, 0f, 0f, false);
                    return true;
                }

            case KeyEvent.KEYCODE_DPAD_DOWN:
                if (!mShowing && mFov != 0f) {
                    mService.updateViewpoint(0f, 5f, 0f, 0f, false);
                    return true;
                }
            case KeyEvent.KEYCODE_DPAD_CENTER:
                if (!mShowing) {
                    doPlayPause();
                    return true;
                }
            case KeyEvent.KEYCODE_ENTER:
                if (mIsNavMenu)
                    return navigateDvdMenu(keyCode);
                else
                    return super.onKeyDown(keyCode, event);
            case KeyEvent.KEYCODE_VOLUME_DOWN:
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (mMute) {
                    updateMute();
                    return true;
                } else
                    return false;
            case KeyEvent.KEYCODE_CAPTIONS:
                //selectSubtitles();
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private boolean navigateDvdMenu(int keyCode) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_UP:
                mService.navigate(MediaPlayer.Navigate.Up);
                return true;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                mService.navigate(MediaPlayer.Navigate.Down);
                return true;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                mService.navigate(MediaPlayer.Navigate.Left);
                return true;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                mService.navigate(MediaPlayer.Navigate.Right);
                return true;
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
            case KeyEvent.KEYCODE_BUTTON_X:
            case KeyEvent.KEYCODE_BUTTON_A:
                mService.navigate(MediaPlayer.Navigate.Activate);
                return true;
            default:
                return false;
        }
    }

    /**
     * Lock screen rotation
     */
    private void lockScreen() {
        /*if (mScreenOrientation != 100) {
            mScreenOrientationLock = getRequestedOrientation();
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2)
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
            else
                setRequestedOrientation(getScreenOrientation(100));
        }*/
        lockVideoScreenDirection();
        showInfo(R.string.locked, 1000);
        mLock.setImageResource(R.drawable.ic_locked_circle);
        mTime.setEnabled(false);
        mSeekbar.setEnabled(false);
        mLength.setEnabled(false);
        mOrientation.setEnabled(false);
        if (mPlaylistNext != null)
            mPlaylistNext.setEnabled(false);
        if (mPlaylistPrevious != null)
            mPlaylistPrevious.setEnabled(false);
        hideOverlay(true);
        mLockBackButton = true;
        mIsLocked = true;
    }

    /**
     * Remove screen lock
     */
    private void unlockScreen() {
        //if(mScreenOrientation != 100)
        //    setRequestedOrientation(mScreenOrientationLock);
        unLockVideoScreenDirection();
        showInfo(R.string.unlocked, 1000);
        mLock.setImageResource(R.drawable.ic_lock_circle);
        mTime.setEnabled(true);
        mSeekbar.setEnabled(mService == null || mService.isSeekable());
        mLength.setEnabled(true);
        mOrientation.setEnabled(true);
        if (mPlaylistNext != null)
            mPlaylistNext.setEnabled(true);
        if (mPlaylistPrevious != null)
            mPlaylistPrevious.setEnabled(true);
        mShowing = false;
        mIsLocked = false;
        showOverlay();
        mLockBackButton = false;
    }

    /**
     * Show text in the info view and vertical progress bar for "duration" milliseconds
     *
     * @param text
     * @param duration
     * @param barNewValue new volume/brightness value (range: 0 - 15)
     */
    private void showInfoWithVerticalBar(String text, int duration, int barNewValue) {
        showInfo(text, duration);
        if (mVerticalBarProgress == null)
            return;
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) mVerticalBarProgress.getLayoutParams();
        layoutParams.weight = barNewValue;
        mVerticalBarProgress.setLayoutParams(layoutParams);
        mVerticalBar.setVisibility(View.VISIBLE);
    }

    /**
     * Show text in the info view for "duration" milliseconds
     *
     * @param text
     * @param duration
     */
    private void showInfo(String text, int duration) {
        initInfoOverlay();
        mVerticalBar.setVisibility(View.GONE);
        mOverlayInfo.setVisibility(View.VISIBLE);
        mInfo.setText(text);
        mHandler.removeMessages(FADE_OUT_INFO);
        mHandler.sendEmptyMessageDelayed(FADE_OUT_INFO, duration);
    }

    private void initInfoOverlay() {
        ViewStubCompat vsc = (ViewStubCompat) findViewById(R.id.player_info_stub);
        if (vsc != null) {
            vsc.inflate();
            // the info textView is not on the overlay
            mInfo = (TextView) findViewById(R.id.player_overlay_textinfo);
            mOverlayInfo = findViewById(R.id.player_overlay_info);
            mVerticalBar = findViewById(R.id.verticalbar);
            mVerticalBarProgress = findViewById(R.id.verticalbar_progress);
        }
    }

    private void showInfo(int textid, int duration) {
        initInfoOverlay();
        mVerticalBar.setVisibility(View.GONE);
        mOverlayInfo.setVisibility(View.VISIBLE);
        mInfo.setText(textid);
        mHandler.removeMessages(FADE_OUT_INFO);
        mHandler.sendEmptyMessageDelayed(FADE_OUT_INFO, duration);
    }

    /**
     * hide the info view with "delay" milliseconds delay
     *
     * @param delay
     */
    private void hideInfo(int delay) {
        mHandler.sendEmptyMessageDelayed(FADE_OUT_INFO, delay);
    }

    /**
     * hide the info view
     */
    private void hideInfo() {
        hideInfo(0);
    }

    private void fadeOutInfo() {
        if (mOverlayInfo != null && mOverlayInfo.getVisibility() == View.VISIBLE) {
            mOverlayInfo.startAnimation(AnimationUtils.loadAnimation(
                    VideoPlayerActivity.this, android.R.anim.fade_out));
            mOverlayInfo.setVisibility(View.INVISIBLE);
        }
    }

    /* PlaybackService.Callback */

    @Override
    public void update() {
    }

    @Override
    public void updateProgress() {
    }

    @Override
    public void onMediaEvent(Media.Event event) {
        switch (event.type) {
            case Media.Event.ParsedChanged:
                updateNavStatus();
                break;
            case Media.Event.MetaChanged:
                break;
            case Media.Event.SubItemTreeAdded:
                mHasSubItems = true;
                break;
        }
    }

    @Override
    public void onMediaPlayerEvent(MediaPlayer.Event event) {
        switch (event.type) {
            case MediaPlayer.Event.Opening:
                mHasSubItems = false;
                break;
            case MediaPlayer.Event.Playing:
                onPlaying();
                break;
            case MediaPlayer.Event.Paused:
                updateOverlayPausePlay();
                break;
            case MediaPlayer.Event.Stopped:
                exitOK();
                break;
            case MediaPlayer.Event.EndReached:
                /* Don't end the activity if the media has subitems since the next child will be
                 * loaded by the PlaybackService */
                if (!mHasSubItems)
                    endReached();
                break;
            case MediaPlayer.Event.EncounteredError:
                encounteredError();
                break;
            case MediaPlayer.Event.TimeChanged:
                break;
            case MediaPlayer.Event.Vout:
                updateNavStatus();
                if (mMenuIdx == -1)
                    handleVout(event.getVoutCount());
                break;
            case MediaPlayer.Event.ESAdded:
                if (mMenuIdx == -1) {
                    if (event.getEsChangedType() == Media.Track.Type.Audio) {
                        MediaWrapper media = mMedialibrary.findMedia(mService.getCurrentMediaWrapper());
                        setESTrackLists();
                        if (media == null)
                            return;
                        int audioTrack = (int) media.getMetaLong(mMedialibrary, MediaWrapper.META_AUDIOTRACK);
                        if (audioTrack != 0 || mCurrentAudioTrack != -2)
                            mService.setAudioTrack(media.getId() == 0L ? mCurrentAudioTrack : audioTrack);
                    } else if (event.getEsChangedType() == Media.Track.Type.Text) {
                        MediaWrapper media = mMedialibrary.findMedia(mService.getCurrentMediaWrapper());
                        setESTrackLists();
                        int spuTrack = (int) media.getMetaLong(mMedialibrary, MediaWrapper.META_SUBTITLE_TRACK);
                        if (spuTrack != 0 || mCurrentSpuTrack != -2)
                            mService.setSpuTrack(media.getId() == 0L ? mCurrentAudioTrack : spuTrack);
                    }
                }
            case MediaPlayer.Event.ESDeleted:
                if (mMenuIdx == -1 && event.getEsChangedType() == Media.Track.Type.Video) {
                    mHandler.removeMessages(CHECK_VIDEO_TRACKS);
                    mHandler.sendEmptyMessageDelayed(CHECK_VIDEO_TRACKS, 1000);
                }
                invalidateESTracks(event.getEsChangedType());
                break;
            case MediaPlayer.Event.ESSelected:
                if (event.getEsChangedType() == Media.VideoTrack.Type.Video) {
                    Media.VideoTrack vt = mService.getCurrentVideoTrack();
                    changeSurfaceLayout();
                    if (vt != null)
                        mFov = vt.projection == Media.VideoTrack.Projection.Rectangular ? 0f : DEFAULT_FOV;
                }
                break;
            case MediaPlayer.Event.SeekableChanged:
                updateSeekable(event.getSeekable());
                break;
            case MediaPlayer.Event.PausableChanged:
                updatePausable(event.getPausable());
                break;
            case MediaPlayer.Event.Buffering:
                if (!mIsPlaying)
                    break;
                if (event.getBuffering() == 100f)
                    stopLoading();
                else if (!mHandler.hasMessages(LOADING_ANIMATION) && !mIsLoading
                        && mTouchAction != TOUCH_SEEK && !mDragging)
                    mHandler.sendEmptyMessageDelayed(LOADING_ANIMATION, LOADING_ANIMATION_DELAY);
                break;
        }
    }

    /**
     * Handle resize of the surface and the overlay
     */
    private final Handler mHandler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (mService == null)
                return true;

            switch (msg.what) {
                case FADE_OUT:
                    hideOverlay(false);
                    break;
                case SHOW_PROGRESS:
                    mHandler.removeMessages(SHOW_PROGRESS);
                    int pos = setOverlayProgress();
                    if (canShowProgress()) {
                        msg = mHandler.obtainMessage(SHOW_PROGRESS);
                        mHandler.sendMessageDelayed(msg, 1000 - (pos % 1000));
                    }
                    break;
                case FADE_OUT_INFO:
                    fadeOutInfo();
                    break;
                case START_PLAYBACK:
                    Log.d("liutao", "startPlayback:");
                    startPlayback();
                    break;
                case AUDIO_SERVICE_CONNECTION_FAILED:
                    exit(RESULT_CONNECTION_FAILED);
                    break;
                case RESET_BACK_LOCK:
                    mLockBackButton = true;
                    break;
                case CHECK_VIDEO_TRACKS:
                    if (mService.getVideoTracksCount() < 1 && mService.getAudioTracksCount() > 0) {
                        Log.i(TAG, "No video track, open in audio mode");
                        switchToAudioMode(true);
                    }
                    break;
                case LOADING_ANIMATION:
                    startLoading();
                    break;
                case HIDE_INFO:
                    hideOverlay(true);
                    break;
                case SHOW_INFO:
                    showOverlay();
                    break;
            }
            return true;
        }
    });

    private boolean canShowProgress() {
        return !mDragging && mService != null && mService.isPlaying();
    }

    private void onPlaying() {
        String title = null;

        mUri = Uri.parse(mService.getCurrentMediaLocation());
        if (!TextUtils.equals(mUri.getScheme(), "content")) {
            title = mUri.getLastPathSegment();
        }

        if (title != null && !mTitle.getText().equals(title)) {
            mTitle.setText(title);
        }


        mIsPlaying = true;
        setPlaybackParameters();
        stopLoading();
        updateOverlayPausePlay();
        updateNavStatus();
        if (!mService.getCurrentMediaWrapper().hasFlag(MediaWrapper.MEDIA_PAUSED))
            mHandler.sendEmptyMessageDelayed(FADE_OUT, OVERLAY_TIMEOUT);
        else {
            mService.getCurrentMediaWrapper().removeFlags(MediaWrapper.MEDIA_PAUSED);
            mWasPaused = false;
        }
        setESTracks();
    }

    private void endReached() {
        if (mService == null)
            return;
        if (mService.getRepeatType() == PlaybackService.REPEAT_ONE) {
            seek(0);
            return;
        }
        if (mService.expand(false) == 0) {
            mHandler.removeMessages(LOADING_ANIMATION);
            mHandler.sendEmptyMessageDelayed(LOADING_ANIMATION, LOADING_ANIMATION_DELAY);
            Log.d(TAG, "Found a video playlist, expanding it");
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    loadMedia();
                }
            });
        }
        //Ignore repeat 
        if (mService.getRepeatType() == PlaybackService.REPEAT_ALL && mService.getMediaListSize() == 1)
            exitOK();
    }

    private void encounteredError() {
        if (isFinishing())
            return;

        /* Encountered Error, exit player with a message */
        mAlertDialog = new AlertDialog.Builder(VideoPlayerActivity.this)
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        exit(RESULT_PLAYBACK_ERROR);
                    }
                })
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        exit(RESULT_PLAYBACK_ERROR);
                    }
                })
                .setTitle(R.string.encountered_error_title)
                .setMessage(R.string.encountered_error_message)
                .create();
        mAlertDialog.show();
    }

    private final Runnable mSwitchAudioRunnable = new Runnable() {
        @Override
        public void run() {
            if (mService.hasMedia()) {
                Log.i(TAG, "Video track lost, switching to audio");
                mSwitchingView = true;
            }
            exit(RESULT_VIDEO_TRACK_LOST);
        }
    };

    private void handleVout(int voutCount) {
        mHandler.removeCallbacks(mSwitchAudioRunnable);

        final IVLCVout vlcVout = mService.getVLCVout();
        if (vlcVout.areViewsAttached() && voutCount == 0) {
            mHandler.postDelayed(mSwitchAudioRunnable, 4000);
        }
    }

    public void switchToPopupMode() {
        if (mService == null)
            return;
        mSwitchingView = true;
        mSwitchToPopup = true;
        exitOK();
    }

    public void switchToAudioMode(boolean showUI) {
        if (mService == null)
            return;
        mSwitchingView = true;
        mSettings.edit().putBoolean(Preferences.VIDEO_RESTORE, true).apply();
        exitOK();
    }

    private void changeMediaPlayerLayout(int displayW, int displayH) {
        /* Change the video placement using MediaPlayer API */
        switch (mCurrentSize) {
            case SURFACE_BEST_FIT:
                mService.setVideoAspectRatio(null);
                mService.setVideoScale(0);
                break;
            case SURFACE_FIT_SCREEN:
            case SURFACE_FILL: {
                Media.VideoTrack vtrack = mService.getCurrentVideoTrack();
                if (vtrack == null)
                    return;
                final boolean videoSwapped = vtrack.orientation == Media.VideoTrack.Orientation.LeftBottom
                        || vtrack.orientation == Media.VideoTrack.Orientation.RightTop;
                if (mCurrentSize == SURFACE_FIT_SCREEN) {
                    int videoW = vtrack.width;
                    int videoH = vtrack.height;

                    if (videoSwapped) {
                        int swap = videoW;
                        videoW = videoH;
                        videoH = swap;
                    }
                    if (vtrack.sarNum != vtrack.sarDen)
                        videoW = videoW * vtrack.sarNum / vtrack.sarDen;

                    float ar = videoW / (float) videoH;
                    float dar = displayW / (float) displayH;

                    float scale;
                    if (dar >= ar)
                        scale = displayW / (float) videoW; /* horizontal */
                    else
                        scale = displayH / (float) videoH; /* vertical */
                    mService.setVideoScale(scale);
                    mService.setVideoAspectRatio(null);
                } else {
                    mService.setVideoScale(0);
                    mService.setVideoAspectRatio(!videoSwapped ? "" + displayW + ":" + displayH
                            : "" + displayH + ":" + displayW);
                }
                break;
            }
            case SURFACE_16_9:
                mService.setVideoAspectRatio("16:9");
                mService.setVideoScale(0);
                break;
            case SURFACE_4_3:
                mService.setVideoAspectRatio("4:3");
                mService.setVideoScale(0);
                break;
            case SURFACE_ORIGINAL:
                mService.setVideoAspectRatio(null);
                mService.setVideoScale(1);
                break;
        }
    }

    @Override
    public boolean isInPictureInPictureMode() {
        return AndroidUtil.isNougatOrLater && super.isInPictureInPictureMode();
    }

    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode);
        changeSurfaceLayout();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void changeSurfaceLayout() {
        int sw;
        int sh;

        // get screen size
        sw = getWindow().getDecorView().getWidth();
        sh = getWindow().getDecorView().getHeight();

        Log.d("liutao", "changeSurfaceLayout sw:" + sw);
        Log.d("liutao", "changeSurfaceLayout sh" + sw);

        // sanity check
        if (sw * sh == 0) {
            Log.d("liutao", "Invalid surface size");
            return;
        }

        if (mService != null) {
            final IVLCVout vlcVout = mService.getVLCVout();
            vlcVout.setWindowSize(sw, sh);
        }

        SurfaceView surface;
        SurfaceView subtitlesSurface;
        FrameLayout surfaceFrame;
        surface = mSurfaceView;
        subtitlesSurface = mSubtitlesSurfaceView;
        surfaceFrame = mSurfaceFrame;
        LayoutParams lp = surface.getLayoutParams();

        Log.d("liutao", "mVideoWidth:" + mVideoWidth);
        Log.d("liutao", "mVideoHeight:" + mVideoHeight);
        Log.d("liutao", "isInPictureInPictureMode:" + isInPictureInPictureMode());
        if (mVideoWidth * mVideoHeight == 0 || isInPictureInPictureMode()) {
            /* Case of OpenGL vouts: handles the placement of the video using MediaPlayer API */
            lp.width = LayoutParams.MATCH_PARENT;
            lp.height = LayoutParams.MATCH_PARENT;
            surface.setLayoutParams(lp);
            lp = surfaceFrame.getLayoutParams();
            lp.width = LayoutParams.MATCH_PARENT;
            lp.height = LayoutParams.MATCH_PARENT;
            surfaceFrame.setLayoutParams(lp);
            if (mService != null && mVideoWidth * mVideoHeight == 0)
                changeMediaPlayerLayout(sw, sh);
            return;
        }

        if (mService != null && lp.width == lp.height && lp.width == LayoutParams.MATCH_PARENT) {
            /* We handle the placement of the video using Android View LayoutParams */
            mService.setVideoAspectRatio(null);
            mService.setVideoScale(0);
        }

        double dw = sw, dh = sh;
        boolean isPortrait;

        isPortrait = mCurrentScreenOrientation == Configuration.ORIENTATION_PORTRAIT;
        Log.d("liutao", "isPortrait:" + isPortrait);
        if (sw > sh && isPortrait || sw < sh && !isPortrait) {
            dw = sh;
            dh = sw;
        }

        // compute the aspect ratio
        double ar, vw;
        if (mSarDen == mSarNum) {
            /* No indication about the density, assuming 1:1 */
            vw = mVideoVisibleWidth;
            ar = (double) mVideoVisibleWidth / (double) mVideoVisibleHeight;
        } else {
            /* Use the specified aspect ratio */
            vw = mVideoVisibleWidth * (double) mSarNum / mSarDen;
            ar = vw / mVideoVisibleHeight;
        }

        // compute the display aspect ratio
        double dar = dw / dh;

        switch (mCurrentSize) {
            case SURFACE_BEST_FIT:
                if (dar < ar)
                    dh = dw / ar;
                else
                    dw = dh * ar;
                break;
            case SURFACE_FIT_SCREEN:
                if (dar >= ar)
                    dh = dw / ar; /* horizontal */
                else
                    dw = dh * ar; /* vertical */
                break;
            case SURFACE_FILL:
                break;
            case SURFACE_16_9:
                ar = 16.0 / 9.0;
                if (dar < ar)
                    dh = dw / ar;
                else
                    dw = dh * ar;
                break;
            case SURFACE_4_3:
                ar = 4.0 / 3.0;
                if (dar < ar)
                    dh = dw / ar;
                else
                    dw = dh * ar;
                break;
            case SURFACE_ORIGINAL:
                dh = mVideoVisibleHeight;
                dw = vw;
                break;
        }

        // set display size
        lp.width = (int) Math.ceil(dw * mVideoWidth / mVideoVisibleWidth);
        lp.height = (int) Math.ceil(dh * mVideoHeight / mVideoVisibleHeight);
        surface.setLayoutParams(lp);
        subtitlesSurface.setLayoutParams(lp);

        // set frame size (crop if necessary)
        lp = surfaceFrame.getLayoutParams();
        lp.width = (int) Math.floor(dw);
        lp.height = (int) Math.floor(dh);
        surfaceFrame.setLayoutParams(lp);

        surface.invalidate();
        subtitlesSurface.invalidate();

        margin = Util.getScreentH() / 2;
        refreshSrtPosition();

    }

    private void sendMouseEvent(int action, int button, int x, int y) {
        if (mService == null)
            return;
        final IVLCVout vlcVout = mService.getVLCVout();
        vlcVout.sendMouseEvent(action, button, x, y);
    }

    /**
     * show/hide the overlay
     */

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mService == null)
            return false;
        if (mDetector == null) {
            mDetector = new GestureDetectorCompat(this, mGestureListener);
            mDetector.setOnDoubleTapListener(mGestureListener);
        }
        if (mFov != 0f && mScaleGestureDetector == null)
            mScaleGestureDetector = new ScaleGestureDetector(this, this);
        if (mPlaybackSetting != DelayState.OFF) {
            if (event.getAction() == MotionEvent.ACTION_UP)
                endPlaybackSetting();
            return true;
        }
        if (mTouchControls == 0 || mIsLocked) {
            // locked or swipe disabled, only handle show/hide & ignore all actions
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (!mShowing) {
                    showOverlay();
                } else {
                    hideOverlay(true);
                }
            }
            return false;
        }
        if (mFov != 0f && mScaleGestureDetector != null)
            mScaleGestureDetector.onTouchEvent(event);
        if ((mScaleGestureDetector != null && mScaleGestureDetector.isInProgress()) ||
                (mDetector != null && mDetector.onTouchEvent(event)))
            return true;

        float x_changed, y_changed;
        if (mTouchX != -1f && mTouchY != -1f) {
            y_changed = event.getRawY() - mTouchY;
            x_changed = event.getRawX() - mTouchX;
        } else {
            x_changed = 0f;
            y_changed = 0f;
        }

        // coef is the gradient's move to determine a neutral zone
        float coef = Math.abs(y_changed / x_changed);
        float xgesturesize = ((x_changed / mScreen.xdpi) * 2.54f);
        float delta_y = Math.max(1f, (Math.abs(mInitTouchY - event.getRawY()) / mScreen.xdpi + 0.5f) * 2f);

        int xTouch = Math.round(event.getRawX());
        int yTouch = Math.round(event.getRawY());

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                // Audio
                mTouchY = mInitTouchY = event.getRawY();
                mVol = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                mTouchAction = TOUCH_NONE;
                // Seek
                mTouchX = event.getRawX();
                // Mouse events for the core
                sendMouseEvent(MotionEvent.ACTION_DOWN, 0, xTouch, yTouch);
                break;

            case MotionEvent.ACTION_MOVE:
                // Mouse events for the core
                sendMouseEvent(MotionEvent.ACTION_MOVE, 0, xTouch, yTouch);

                if (mFov == 0f) {
                    // No volume/brightness action if coef < 2 or a secondary display is connected
                    //TODO : Volume action when a secondary display is connected
                    if (mTouchAction != TOUCH_SEEK && coef > 2) {
                        if (Math.abs(y_changed / mSurfaceYDisplayRange) < 0.05)
                            return false;
                        mTouchY = event.getRawY();
                        mTouchX = event.getRawX();
                        // Volume (Up or Down - Right side)
                        if (mTouchControls == 1 || (mTouchControls == 3 && (int) mTouchX > (4 * mScreen.widthPixels / 7f))) {
                            doVolumeTouch(y_changed);
                            hideOverlay(true);
                        }
                        // Brightness (Up or Down - Left side)
                        if (mTouchControls == 2 || (mTouchControls == 3 && (int) mTouchX < (3 * mScreen.widthPixels / 7f))) {
                            doBrightnessTouch(y_changed);
                            hideOverlay(true);
                        }
                    } else {
                        // Seek (Right or Left move)
                        doSeekTouch(Math.round(delta_y), xgesturesize, false);
                    }
                } else {
                    mTouchY = event.getRawY();
                    mTouchX = event.getRawX();
                    mTouchAction = TOUCH_MOVE;
                    float yaw = mFov * -x_changed / (float) mSurfaceXDisplayRange;
                    float pitch = mFov * -y_changed / (float) mSurfaceXDisplayRange;
                    mService.updateViewpoint(yaw, pitch, 0, 0, false);
                }
                break;

            case MotionEvent.ACTION_UP:
                // Mouse events for the core
                sendMouseEvent(MotionEvent.ACTION_UP, 0, xTouch, yTouch);
                // Seek
                if (mTouchAction == TOUCH_SEEK)
                    doSeekTouch(Math.round(delta_y), xgesturesize, true);
                mTouchX = -1f;
                mTouchY = -1f;
                break;
        }
        return mTouchAction != TOUCH_NONE;
    }

    private void doSeekTouch(int coef, float gesturesize, boolean seek) {
        if (coef == 0)
            coef = 1;
        // No seek action if coef > 0.5 and gesturesize < 1cm
        if (Math.abs(gesturesize) < 1 || !mService.isSeekable())
            return;

        if (mTouchAction != TOUCH_NONE && mTouchAction != TOUCH_SEEK)
            return;
        mTouchAction = TOUCH_SEEK;

        long length = mService.getLength();
        long time = getTime();

        // Size of the jump, 10 minutes max (600000), with a bi-cubic progression, for a 8cm gesture
        int jump = (int) ((Math.signum(gesturesize) * ((600000 * Math.pow((gesturesize / 8), 4)) + 3000)) / coef);

        // Adjust the jump
        if ((jump > 0) && ((time + jump) > length))
            jump = (int) (length - time);
        if ((jump < 0) && ((time + jump) < 0))
            jump = (int) -time;

        //Jump !
        if (seek && length > 0)
            seek(time + jump, length);

        if (length > 0)
            //Show the jump's size
            showInfo(String.format("%s%s (%s)%s",
                    jump >= 0 ? "+" : "",
                    Tools.millisToString(jump),
                    Tools.millisToString(time + jump),
                    coef > 1 ? String.format(" x%.1g", 1.0 / coef) : ""), 50);
        else
            showInfo(R.string.unseekable_stream, 1000);
    }

    @Override
    public void showAudioDelaySetting() {
        mPlaybackSetting = DelayState.AUDIO;
        //showDelayControls();
    }

    @Override
    public void showSubsDelaySetting() {
        mPlaybackSetting = DelayState.SUBS;
        //showDelayControls();
    }

    @Override
    public void showPlaybackSpeedSetting() {
        mPlaybackSetting = DelayState.SPEED;
        //showDelayControls();
    }

    @Override
    public void endPlaybackSetting() {
        mTouchAction = TOUCH_NONE;
        mService.saveMediaMeta();

        mPlaybackSetting = DelayState.OFF;
        if (mPlaybackSettingMinus != null) {
            mPlaybackSettingMinus.setOnClickListener(null);
            mPlaybackSettingMinus.setVisibility(View.INVISIBLE);
        }
        if (mPlaybackSettingPlus != null) {
            mPlaybackSettingPlus.setOnClickListener(null);
            mPlaybackSettingPlus.setVisibility(View.INVISIBLE);
        }
        mOverlayInfo.setVisibility(View.INVISIBLE);
        mInfo.setText("");
        if (mPlayPause != null)
            mPlayPause.requestFocus();
    }

    private void doVolumeTouch(float y_changed) {
        if (mTouchAction != TOUCH_NONE && mTouchAction != TOUCH_VOLUME)
            return;
        float delta = -((y_changed / (float) mScreen.heightPixels) * mAudioMax);
        mVol += delta;
        int vol = (int) Math.min(Math.max(mVol, 0), mAudioMax);
        if (delta != 0f) {
            setAudioVolume(vol);
        }
    }

    private void setAudioVolume(int vol) {
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, vol, 0);

        /* Since android 4.3, the safe volume warning dialog is displayed only with the FLAG_SHOW_UI flag.
         * We don't want to always show the default UI volume, so show it only when volume is not set. */
        int newVol = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        if (vol != newVol)
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, vol, AudioManager.FLAG_SHOW_UI);

        mTouchAction = TOUCH_VOLUME;
        vol = vol * 100 / mAudioMax;
        showInfoWithVerticalBar(getString(R.string.volume) + "\n" + Integer.toString(vol) + '%', 1000, vol);
    }

    private void mute(boolean mute) {
        mMute = mute;
        if (mMute)
            mVolSave = mService.getVolume();
        mService.setVolume(mMute ? 0 : mVolSave);
    }

    private void updateMute() {
        mute(!mMute);
        showInfo(mMute ? R.string.sound_off : R.string.sound_on, 1000);
    }

    private void initBrightnessTouch() {

        mIsFirstBrightnessGesture = false;
    }

    private void doBrightnessTouch(float y_changed) {
        if (mTouchAction != TOUCH_NONE && mTouchAction != TOUCH_BRIGHTNESS)
            return;
        if (mIsFirstBrightnessGesture) initBrightnessTouch();
        mTouchAction = TOUCH_BRIGHTNESS;

        // Set delta : 2f is arbitrary for now, it possibly will change in the future
        float delta = -y_changed / mSurfaceYDisplayRange;

        changeBrightness(delta);
    }

    private void changeBrightness(float delta) {
        // Estimate and adjust Brightness
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        float brightness = Math.min(Math.max(lp.screenBrightness + delta, 0.01f), 1f);
        setWindowBrightness(brightness);
        brightness = Math.round(brightness * 100);
        showInfoWithVerticalBar(getString(R.string.brightness) + "\n" + (int) brightness + '%', 1000, (int) brightness);
    }

    private void setWindowBrightness(float brightness) {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.screenBrightness = brightness;
        // Set Brightness
        getWindow().setAttributes(lp);
    }

    /**
     * handle changes of the seekbar (slicer)
     */
    private final OnSeekBarChangeListener mSeekListener = new OnSeekBarChangeListener() {

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            mDragging = true;
            showOverlayTimeout(OVERLAY_INFINITE);
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            mDragging = false;
            showOverlay(true);
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (!isFinishing() && fromUser && mService.isSeekable()) {
                seek(progress);
                setOverlayProgress();
                mTime.setText(Tools.millisToString(progress));
                showInfo(Tools.millisToString(progress), 1000);
            }
        }
    };


    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.player_overlay_play) {
            doPlayPause();
        } else if (v.getId() == R.id.lock_overlay_button) {
            if (mIsLocked) {
                unlockScreen();
            } else {
                lockScreen();
            }
        } else if (v.getId() == R.id.player_overlay_orientation) {
            changeVideoScreenDirection();
        } else if (v.getId() == R.id.player_overlay_length || v.getId() == R.id.player_overlay_time) {
            mDisplayRemainingTime = !mDisplayRemainingTime;
            showOverlay();
            mSettings.edit().putBoolean(KEY_REMAINING_TIME_DISPLAY, mDisplayRemainingTime).apply();
        } else if (v.getId() == R.id.player_overlay_back) {

            exitOK();
        } else if (v.getId() == R.id.player_btn_rewind) {
            mService.determinePrevAndNextIndices();
            mService.previous(true);
            closeSrt();
            setSrts(null);
        } else if (v.getId() == R.id.player_btn_forward) {
            mService.determinePrevAndNextIndices();
            mService.next();
            closeSrt();
            setSrts(null);
        } else if (v.getId() == R.id.player_btn_subtitle) {
            if (mService != null && mService.hasMedia()) {
                String path = mService.getCurrentMediaLocation();
                int index = path.lastIndexOf(File.separator);
                String dirPathString = null;
                if (index != -1) {
                    dirPathString = path.substring(0, index);
                }
                showSubtitlePop(dirPathString);

            }


        }
    }

    private PopupWindow popupWindow;
    private View popView;
    private volatile boolean toogle_on = false;
    private LinearLayout layout_srt = null;
    private TextView mTVSrt1 = null;
    private TextView mTVSrt2 = null;
    private TextView emptyTextView;
    private ImageButton ib_toogle;
    private ImageView iv_size_small, iv_size_large;
    private SeekBar vod_player_srt_seekbar;
    private ProgressBar pb_srt_loading;
    private List<ChooseSrtBean> mData = new ArrayList<ChooseSrtBean>();
    private ChooseSrtAdapter mChooseSrtAdapter;
    OnChooseSrtListener onChooseSrtListener;
    private TimedTextObject srts = null;// 字幕列表
    private int margin; // 字幕偏移；
    private int halfOfHeight; // 字幕控件一半的高度
    private int srtDefaultSizeSp = 18;
    private final static int SRT_MIN_SIZE = 8;
    private final static int SRT_MAX_SIZE = 28;

    public interface OnChooseSrtListener {
        public void onClickSrt();
    }

    public void showSubtitlePop(String dirPath) {
        // TODO Auto-generated method stub
        // 底部的框保持不收入
        mHandler.removeMessages(FADE_OUT);

        this.mSubTitleDir = dirPath;
        if (popupWindow == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            // 显示的位置为:屏幕的宽度的一半-PopupWindow的高度的一半
            popView = layoutInflater.inflate(R.layout.vod_pop_subtitle, null);
            popupWindow = new PopupWindow(popView);

            // 使其聚集
            popupWindow.setFocusable(true);
            // 设置允许在外点击消失
            popupWindow.setOutsideTouchable(true);
            popupWindow.setOnDismissListener(popuWindowDismissListener);

            initPopView();

            // 这个是为了点击“返回Back”也能使其消失，并且并不会影响你的背景
            popupWindow.setBackgroundDrawable(new BitmapDrawable());
        }

        // 恢复开关按钮
        if (mService != null && mService.hasMedia()) {

            String pathDb = mService.getCurrentMediaLocation();
            try {
                pathDb = URLDecoder.decode(pathDb, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            int index = pathDb.lastIndexOf("?token");
            if (index >= 0 && index < pathDb.length()) {
                pathDb = pathDb.substring(0, index);
            }

            PlayRecordBean mPlayRecord = VlcPlayRecordDB.getInstance(this).getRecordByUrl(
                    pathDb);

            if (mPlayRecord != null && mPlayRecord.subtitle_onoff) {
                openSrt(dirPath);
            } else {
                closeSrt();
            }
        }

        int screenHeight = Util.getScreentH();
        int screenWidth = Util.getScreentW();


        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // 横屏
            popupWindow.setHeight(screenHeight * 2 / 3);
        } else {
            popupWindow.setHeight(screenHeight / 2);
        }

        popupWindow.setWidth(screenWidth * 2 / 3);
        popupWindow.showAsDropDown(mSubtitle, -200, 0);
    }

    public void closeSrt() {
        toogle_on = false;
        if (popupWindow != null) {
            ib_toogle.setImageResource(R.drawable.togglebutton_close);
            mData.clear();
            emptyTextView.setText(R.string.DM_Srt_close);
            mChooseSrtAdapter.notifyDataSetChanged();
            setSrtControlEnable(false);
        }

        layout_srt.setVisibility(View.GONE);
        mTVSrt1.setText("");
        mTVSrt2.setText("");

    }

    public void openSrt(final String srtDirPath) {

        toogle_on = true;
        layout_srt.setVisibility(View.VISIBLE);
        //说明用户还没有点击popwindow，后面扫描字幕列表无需操作
        if (popupWindow == null)
            return;

        emptyTextView.setText(getString(R.string.DM_Srt_loading));
        pb_srt_loading.setVisibility(View.VISIBLE);
        ib_toogle.setImageResource(R.drawable.togglebutton_open);
        setSrtControlEnable(true);

        if (openSrtSubscription != null && !openSrtSubscription.isUnsubscribed()) {
            return;
        }

        openSrtSubscription = Observable.fromCallable(new Callable<Boolean>() {

            @Override
            public Boolean call() {
                boolean ret = false;
                List<ChooseSrtBean> tmpDataBeans = SubtitleTool
                        .getChooseSrtBeanList(srtDirPath);

                if (tmpDataBeans.size() > 0) {
                    mData.clear();
                    mData.addAll(tmpDataBeans);
                    mChooseSrtAdapter.selectedPosition = -1;

                    String pathDb = mService.getCurrentMediaLocation();
                    try {
                        pathDb = URLDecoder.decode(pathDb, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    int index = pathDb.lastIndexOf("?token");
                    if (index >= 0 && index < pathDb.length()) {
                        pathDb = pathDb.substring(0, index);
                    }


                    //获取出当前字幕的在列表中的 位置
                    PlayRecordBean mPlayRecord = VlcPlayRecordDB.getInstance(VideoPlayerActivity.this).getRecordByUrl(
                            pathDb);
                    if (mPlayRecord != null && mPlayRecord.uri != null) {
                        for (int i = 0; i < tmpDataBeans.size(); i++) {
                            if (tmpDataBeans.get(i).path.equals(mPlayRecord.subtitle_path)) {
                                mChooseSrtAdapter.selectedPosition = i;
                                //当前没有字幕 去恢复字幕
                                parseSubtitle();
                                break;
                            }
                        }
                    } else {
                        mChooseSrtAdapter.selectedPosition = -1;
                    }
                    ret = true;

                }

                return ret;

            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean ret) {
                        if (!ret) {
                            emptyTextView.setText(R.string.DM_Srt_no_file);
                        }

                        mChooseSrtAdapter.notifyDataSetChanged();
                        pb_srt_loading.setVisibility(View.GONE);
                        mCompositeSubscription.remove(openSrtSubscription);
                    }
                });
        mCompositeSubscription.add(openSrtSubscription);

    }


    private void parseSubtitle() {
        // TODO Auto-generated method stub
        /** 解析字幕文件线程 */


        if (mService == null
                || !mService.hasMedia()) return;

        if (parseSubtitleSubscription != null && !parseSubtitleSubscription.isUnsubscribed()) {
            return;
        }

        parseSubtitleSubscription = Observable.fromCallable(new Callable<TimedTextObject>() {

            @Override
            public TimedTextObject call() {


                String pathDb = mService.getCurrentMediaLocation();
                try {
                    pathDb = URLDecoder.decode(pathDb, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                int index = pathDb.lastIndexOf("?token");
                if (index >= 0 && index < pathDb.length()) {
                    pathDb = pathDb.substring(0, index);
                }

                PlayRecordBean mPlayRecord = VlcPlayRecordDB.getInstance(VideoPlayerActivity.this)
                        .getRecordByUrl(pathDb);
                TimedTextObject mTimedTextObject = null;
                if (mPlayRecord != null && mPlayRecord.subtitle_onoff && mPlayRecord.subtitle_path != null) {
                    mTimedTextObject = SubtitleTool.parseCaption(mPlayRecord.subtitle_path);
                }
                return mTimedTextObject;

            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<TimedTextObject>() {
                    @Override
                    public void call(TimedTextObject ret) {
                        if (ret != null) {
                            toogle_on = true;
                            layout_srt.setVisibility(View.VISIBLE);

                            setSrts(ret);
                            refreshSrtPosition();

                        }
                        mCompositeSubscription.remove(parseSubtitleSubscription);
                    }
                });
        mCompositeSubscription.add(parseSubtitleSubscription);


    }

    public void setSrts(TimedTextObject srts) {
        this.srts = srts;
    }

    private void updateTimedText(int currentPosition) {
        // TODO Auto-generated method stub
        if (srts == null || srts.captions == null) {
            return;
        }
        // SRT srtbean;

        Iterator<Map.Entry<Integer, Caption>> entryKeyIterator = srts.captions
                .entrySet().iterator();

        while (entryKeyIterator.hasNext()) {
            Map.Entry<Integer, Caption> e = entryKeyIterator.next();
            Caption value = e.getValue();
            if (currentPosition > value.start.getMseconds()
                    && currentPosition < value.end.getMseconds()) {
                mTVSrt1.setText(value.content);
                mTVSrt2.setText(value.content1);
            } else if ((currentPosition - value.end.getMseconds()) > 3000) {
                mTVSrt1.setText("");
                mTVSrt2.setText("");
            }
        }

    }


    public void refreshSrtPosition() {
        if (toogle_on) {
            int realMargin = 0;
            if (vod_player_srt_seekbar != null) {
                // 一次函数
                realMargin = (-2 * margin
                        * vod_player_srt_seekbar.getProgress() / vod_player_srt_seekbar
                        .getMax()) + margin + halfOfHeight;
            } else {
                realMargin = (-2 * margin
                        * 15 / 100) + margin + halfOfHeight;
            }
            FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) layout_srt.getLayoutParams();
            lp.setMargins(0, realMargin, 0, 0);
            layout_srt.setLayoutParams(lp);
        }
    }

    private void setSrtControlEnable(boolean enabled) {
        iv_size_small.setEnabled(enabled);
        iv_size_large.setEnabled(enabled);
        vod_player_srt_seekbar.setEnabled(enabled);
    }

    ListView lv_pop_srt_choose;

    private void initPopView() {
        // TODO Auto-generated method stub
        iv_size_small = (ImageView) popView.findViewById(R.id.iv_size_small);
        iv_size_large = (ImageView) popView.findViewById(R.id.iv_size_large);

        ib_toogle = (ImageButton) popView.findViewById(R.id.ib_toogle);

        vod_player_srt_seekbar = (SeekBar) popView
                .findViewById(R.id.vod_player_srt_seekbar);
        vod_player_srt_seekbar.setEnabled(false);
        lv_pop_srt_choose = (ListView) popView
                .findViewById(R.id.lv_pop_srt_choose);

		/* emptyView 的创建 */
        ViewGroup viewGroup = (ViewGroup) lv_pop_srt_choose.getParent();
        LayoutInflater li = (LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View emptyView = li.inflate(R.layout.vod_pop_subtitle_empty_view,
                viewGroup, false);
        viewGroup.addView(emptyView);
        lv_pop_srt_choose.setEmptyView(emptyView);

        pb_srt_loading = (ProgressBar) emptyView.findViewById(R.id.pb_srt_onoff);
        pb_srt_loading.setVisibility(View.GONE);
        emptyTextView = (TextView) emptyView.findViewById(R.id.tv_choose_empty);

        mChooseSrtAdapter = new ChooseSrtAdapter(this, mData);
        lv_pop_srt_choose.setAdapter(mChooseSrtAdapter);

        iv_size_large.setOnClickListener(listener);
        iv_size_small.setOnClickListener(listener);
        ib_toogle.setOnClickListener(listener);
        vod_player_srt_seekbar
                .setOnSeekBarChangeListener(mOnSeekBarSrtChangeListener);

        lv_pop_srt_choose
                .setOnItemClickListener(popUpWindowOnItemClickListener);

        halfOfHeight = dip2px(this, 50);
    }


    private AdapterView.OnItemClickListener popUpWindowOnItemClickListener = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            // TODO Auto-generated method stub
            int prePosition = mChooseSrtAdapter.selectedPosition;
            if (prePosition == position)
                return;

            if (prePosition >= 0
                    && prePosition < mChooseSrtAdapter.getCount()
                    && prePosition >= lv_pop_srt_choose
                    .getFirstVisiblePosition()
                    && prePosition <= lv_pop_srt_choose
                    .getLastVisiblePosition()) {

                View v = lv_pop_srt_choose.getChildAt(prePosition);
                if (v != null) {
                    v.setSelected(false);
                }
            }
            mChooseSrtAdapter.selectedPosition = position;
            view.setSelected(true);

            final ChooseSrtBean mChooseSrtBean = mData.get(position);

            // 保存字幕到数据库

            new Thread(new Runnable() {
                public void run() {

                    String pathDb = mService.getCurrentMediaLocation();
                    try {
                        pathDb = URLDecoder.decode(pathDb, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    int index = pathDb.lastIndexOf("?token");
                    if (index >= 0 && index < pathDb.length()) {
                        pathDb = pathDb.substring(0, index);
                    }

                    VlcPlayRecordDB.getInstance(VideoPlayerActivity.this).addRecord(
                            new PlayRecordBean(true, pathDb, mChooseSrtBean.path)
                    );
                    parseSubtitle();
                }
            }).start();
            //
            mHandler.postDelayed(new Runnable() {

                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    popupWindow.dismiss();
                }
            }, 200);

        }
    };

    private OnSeekBarChangeListener mOnSeekBarSrtChangeListener = new OnSeekBarChangeListener() {

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            // 标记
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress,
                                      boolean fromUser) {
            if (fromUser) {
                int realMargin = (-2 * margin * progress / vod_player_srt_seekbar
                        .getMax()) + margin + halfOfHeight;
                FrameLayout.LayoutParams lParams = (FrameLayout.LayoutParams) layout_srt
                        .getLayoutParams();
                lParams.setMargins(0, realMargin, 0, 0);
                layout_srt.setLayoutParams(lParams);
            }
        }
    };


    private OnClickListener listener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            int id = v.getId();
            if (id == R.id.ib_toogle) {


                if (toogle_on) {
                    // 关
                    closeSrt();
                } else {
                    // 开
                    String path = mService.getCurrentMediaLocation();
                    int index = path.lastIndexOf(File.separator);
                    String dirPathString = null;
                    if (index != -1) {
                        dirPathString = path.substring(0, index);
                    }
                    openSrt(path);
                }


                if (mService != null && mService.hasMedia()) {
                    new Thread(new Runnable() {
                        public void run() {
                            String pathDb = mService.getCurrentMediaLocation();
                            try {
                                pathDb = URLDecoder.decode(pathDb, "UTF-8");
                            } catch (UnsupportedEncodingException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                            int index = pathDb.lastIndexOf("?token");
                            if (index >= 0 && index < pathDb.length()) {
                                pathDb = pathDb.substring(0, index);
                            }

                            if (!toogle_on) {
                                // 关
                                VlcPlayRecordDB.getInstance(VideoPlayerActivity.this)
                                        .deleteRecord(pathDb);

                            } else {
                                // 开
                                VlcPlayRecordDB.getInstance(VideoPlayerActivity.this)
                                        .addRecord(
                                                new PlayRecordBean(true, pathDb, null)
                                        );
                            }


                        }
                    }).start();
                }


            } else if (id == R.id.iv_size_small) {
                --srtDefaultSizeSp;
                srtDefaultSizeSp = Math.max(srtDefaultSizeSp, SRT_MIN_SIZE);

                mTVSrt1.setTextSize(TypedValue.COMPLEX_UNIT_SP,
                        srtDefaultSizeSp);
                mTVSrt2.setTextSize(TypedValue.COMPLEX_UNIT_SP,
                        srtDefaultSizeSp);

            } else if (id == R.id.iv_size_large) {

                ++srtDefaultSizeSp;
                srtDefaultSizeSp = Math.min(srtDefaultSizeSp, SRT_MAX_SIZE);

                mTVSrt1.setTextSize(TypedValue.COMPLEX_UNIT_SP,
                        srtDefaultSizeSp);
                mTVSrt2.setTextSize(TypedValue.COMPLEX_UNIT_SP,
                        srtDefaultSizeSp);
            }
        }
    };

    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }


    private PopupWindow.OnDismissListener popuWindowDismissListener = new PopupWindow.OnDismissListener() {

        @Override
        public void onDismiss() {
            // TODO Auto-generated method stub
            mHandler.removeMessages(FADE_OUT);
//            mHandler.removeMessages(SHOW_PROGRESS);
            mHandler.sendEmptyMessageDelayed(FADE_OUT, OVERLAY_TIMEOUT);
        }
    };


    private void changeVideoScreenDirection() {
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        orientationHandler.sendEmptyMessageDelayed(0, 2000);
    }

    private void lockVideoScreenDirection() {
        orientationHandler.removeMessages(0);
        mOrientationListener.disable();

        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    private void unLockVideoScreenDirection () {
        startRotation = -2;
        mOrientationListener.enable();
    }

    /**
     * 2s后恢复重力感应横竖屏切换
     */
    private OrientationEventListener mOrientationListener; // 屏幕方向改变监听器
    private int startRotation;
    Handler orientationHandler = new Handler() {
        public void handleMessage(Message msg) {
            startRotation = -2;
            mOrientationListener.enable();
        }


    };

    private final void startListener() {
        mOrientationListener = new OrientationEventListener(this) {
            @Override
            public void onOrientationChanged(int rotation) {

                if (startRotation == -2) {//初始化角度
                    startRotation = rotation;
                }
                //变化角度大于30时，开启自动旋转，并关闭监听
                int r = Math.abs(startRotation - rotation);
                r = r > 180 ? 360 - r : r;
                if (r > 30) {
                    //开启自动旋转，响应屏幕旋转事件
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
                    this.disable();
                }
            }
        };
    }


    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        float diff = DEFAULT_FOV * (1 - detector.getScaleFactor());
        if (mService.updateViewpoint(0, 0, 0, diff, false)) {
            mFov = Math.min(Math.max(MIN_FOV, mFov + diff), MAX_FOV);
            return true;
        }
        return false;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        return mSurfaceXDisplayRange != 0 && mFov != 0f;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
    }

    @Override
    public boolean onLongClick(View view) {
        return false;
    }

    private interface TrackSelectedListener {
        boolean onTrackSelected(int trackID);
    }

    private void selectTrack(final MediaPlayer.TrackDescription[] tracks, int currentTrack, int titleId,
                             final TrackSelectedListener listener) {
        if (listener == null)
            throw new IllegalArgumentException("listener must not be null");
        if (tracks == null)
            return;
        final String[] nameList = new String[tracks.length];
        final int[] idList = new int[tracks.length];
        int i = 0;
        int listPosition = 0;
        for (MediaPlayer.TrackDescription track : tracks) {
            idList[i] = track.id;
            nameList[i] = track.name;
            // map the track position to the list position
            if (track.id == currentTrack)
                listPosition = i;
            i++;
        }

        if (!isFinishing()) {
            mAlertDialog = new AlertDialog.Builder(VideoPlayerActivity.this)
                    .setTitle(titleId)
                    .setSingleChoiceItems(nameList, listPosition, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int listPosition) {
                            int trackID = -1;
                            // Reverse map search...
                            for (MediaPlayer.TrackDescription track : tracks) {
                                if (idList[listPosition] == track.id) {
                                    trackID = track.id;
                                    break;
                                }
                            }
                            listener.onTrackSelected(trackID);
                            dialog.dismiss();
                        }
                    })
                    .create();
            mAlertDialog.setCanceledOnTouchOutside(true);
            mAlertDialog.setOwnerActivity(VideoPlayerActivity.this);
            mAlertDialog.show();
        }
    }


    private void updateSeekable(boolean seekable) {

        if (!mIsLocked && mSeekbar != null)
            mSeekbar.setEnabled(seekable);
    }

    private void updatePausable(boolean pausable) {
        if (mPlayPause == null)
            return;
        mPlayPause.setEnabled(pausable);
        if (!pausable)
            mPlayPause.setImageResource(R.drawable.ic_play_circle_disable_o);
    }

    private void doPlayPause() {
        if (!mService.isPausable())
            return;

        if (mService.isPlaying()) {
            pause();
        } else {
            play();
        }
    }

    private long getTime() {
        long time = mService.getTime();
        if (mForcedTime != -1 && mLastTime != -1) {
            /* XXX: After a seek, mService.getTime can return the position before or after
             * the seek position. Therefore we return mForcedTime in order to avoid the seekBar
             * to move between seek position and the actual position.
             * We have to wait for a valid position (that is after the seek position).
             * to re-init mLastTime and mForcedTime to -1 and return the actual position.
             */
            if (mLastTime > mForcedTime) {
                if (time <= mLastTime && time > mForcedTime || time > mLastTime)
                    mLastTime = mForcedTime = -1;
            } else {
                if (time > mForcedTime)
                    mLastTime = mForcedTime = -1;
            }
        } else if (time == 0)
            time = (int) mService.getCurrentMediaWrapper().getTime();
        return mForcedTime == -1 ? time : mForcedTime;
    }

    protected void seek(long position) {
        seek(position, mService.getLength());
    }

    private void seek(long position, long length) {
        mForcedTime = position;
        mLastTime = mService.getTime();
        mService.seek(position, length);
    }

    private void seekDelta(int delta) {
        // unseekable stream
        if (mService.getLength() <= 0 || !mService.isSeekable()) return;

        long position = getTime() + delta;
        if (position < 0) position = 0;
        seek(position);
        StringBuilder sb = new StringBuilder();
        if (delta > 0f)
            sb.append('+');
        sb.append((int) (delta / 1000f))
                .append("s (")
                .append(Tools.millisToString(mService.getTime()))
                .append(')');
        showInfo(sb.toString(), 1000);
    }

    private void resizeVideo() {
        if (mCurrentSize < SURFACE_ORIGINAL) {
            mCurrentSize++;
        } else {
            mCurrentSize = 0;
        }
        changeSurfaceLayout();
        switch (mCurrentSize) {
            case SURFACE_BEST_FIT:
                showInfo(R.string.surface_best_fit, 1000);
                break;
            case SURFACE_FIT_SCREEN:
                showInfo(R.string.surface_fit_screen, 1000);
                break;
            case SURFACE_FILL:
                showInfo(R.string.surface_fill, 1000);
                break;
            case SURFACE_16_9:
                showInfo("16:9", 1000);
                break;
            case SURFACE_4_3:
                showInfo("4:3", 1000);
                break;
            case SURFACE_ORIGINAL:
                showInfo(R.string.surface_original, 1000);
                break;
        }
        Editor editor = mSettings.edit();
        editor.putInt(Preferences.VIDEO_RATIO, mCurrentSize);
        editor.apply();
        showOverlay();
    }

    /**
     * show overlay
     *
     * @param forceCheck: adjust the timeout in function of playing state
     */
    private void showOverlay(boolean forceCheck) {
        if (forceCheck)
            mOverlayTimeout = 0;
        showOverlayTimeout(0);
    }

    /**
     * show overlay with the previous timeout value
     */
    private void showOverlay() {
        showOverlay(false);
    }

    /**
     * show overlay
     */
    private void showOverlayTimeout(int timeout) {
        if (mService == null)
            return;
        initOverlay();
        if (timeout != 0)
            mOverlayTimeout = timeout;
        if (mOverlayTimeout == 0)
            mOverlayTimeout = mService.isPlaying() ? OVERLAY_TIMEOUT : OVERLAY_INFINITE;
        if (mIsNavMenu) {
            mShowing = true;
            return;
        }
        mHandler.sendEmptyMessage(SHOW_PROGRESS);
        if (!mShowing) {
            mShowing = true;
            if (!mIsLocked) {
                mPlayPause.setVisibility(View.VISIBLE);
                mPlayNext.setVisibility(View.VISIBLE);
                mPlayPre.setVisibility(View.VISIBLE);
                mOrientation.setVisibility(View.VISIBLE);
                mSubtitle.setVisibility(View.VISIBLE);
            }
            dimStatusBar(false);
            mOverlayProgress.setVisibility(View.VISIBLE);
            mLock.setVisibility(View.VISIBLE);
        }
        mHandler.removeMessages(FADE_OUT);
        if (mOverlayTimeout != OVERLAY_INFINITE)
            mHandler.sendMessageDelayed(mHandler.obtainMessage(FADE_OUT), mOverlayTimeout);
        updateOverlayPausePlay();
        if (mObjectFocused != null)
            mObjectFocused.requestFocus();
        else if (getCurrentFocus() == null)
            mPlayPause.requestFocus();
    }

    private void initOverlay() {
        ViewStubCompat vsc = (ViewStubCompat) findViewById(R.id.player_hud_stub);
        if (vsc != null) {
            vsc.inflate();
            mOverlayProgress = findViewById(R.id.progress_overlay);
            RelativeLayout.LayoutParams layoutParams =
                    (RelativeLayout.LayoutParams) mOverlayProgress.getLayoutParams();
            layoutParams.width = LayoutParams.MATCH_PARENT;
            mOverlayProgress.setLayoutParams(layoutParams);
            // Position and remaining time
            mOverlayButtons = findViewById(R.id.player_overlay_buttons);
            mTime = (TextView) findViewById(R.id.player_overlay_time);
            mLength = (TextView) findViewById(R.id.player_overlay_length);
            mPlayPause = (ImageView) findViewById(R.id.player_overlay_play);
            mPlayPre = (ImageView) findViewById(R.id.player_btn_rewind);
            mPlayNext = (ImageView) findViewById(R.id.player_btn_forward);
            mSubtitle = (Button) findViewById(R.id.player_btn_subtitle);

            mOrientation = (ImageView) findViewById(R.id.player_overlay_orientation);
            mSeekbar = (SeekBar) findViewById(R.id.player_overlay_seekbar);
            resetHudLayout();
            updateOverlayPausePlay();
            updateSeekable(mService.isSeekable());
            updatePausable(mService.isPausable());
            updateNavStatus();
            setHudClickListeners();
        }
    }


    public void resetHudLayout() {
        if (mOverlayButtons == null)
            return;
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mOverlayButtons.getLayoutParams();
        int orientation = getScreenOrientation(100);
        boolean portrait = orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT ||
                orientation == ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
        if (portrait) {
            layoutParams.addRule(RelativeLayout.BELOW, R.id.player_overlay_length);
            layoutParams.addRule(RelativeLayout.RIGHT_OF, 0);
            layoutParams.addRule(RelativeLayout.LEFT_OF, 0);
        } else {
            layoutParams.addRule(RelativeLayout.BELOW, R.id.player_overlay_seekbar);
            layoutParams.addRule(RelativeLayout.RIGHT_OF, R.id.player_overlay_time);
            layoutParams.addRule(RelativeLayout.LEFT_OF, R.id.player_overlay_length);
        }
        mOverlayButtons.setLayoutParams(layoutParams);
    }

    /**
     * hider overlay
     */
    private void hideOverlay(boolean fromUser) {
        if (mShowing) {
            mHandler.removeMessages(FADE_OUT);
//            mHandler.removeMessages(SHOW_PROGRESS);
            Log.i(TAG, "remove View!");
            Log.i(TAG, "fromUser:" + fromUser);
            Log.i(TAG, "mIsLocked:" + mIsLocked);
            mObjectFocused = getCurrentFocus();
            if (!fromUser && !mIsLocked) {
                mOverlayProgress.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_out));
                mPlayPause.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_out));
                mPlayPre.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_out));
                mPlayNext.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_out));
                if (mPlaylistNext != null)
                    mPlaylistNext.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_out));
                if (mPlaylistPrevious != null)
                    mPlaylistPrevious.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_out));
                //mSize.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_out));
                mOrientation.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_out));
            }

            mOverlayProgress.setVisibility(View.INVISIBLE);
            mLock.setVisibility(View.INVISIBLE);
            mPlayPause.setVisibility(View.INVISIBLE);
            mPlayPre.setVisibility(View.INVISIBLE);
            mPlayNext.setVisibility(View.INVISIBLE);
            mSubtitle.setVisibility(View.INVISIBLE);
            mOrientation.setVisibility(View.INVISIBLE);

            mShowing = false;
            dimStatusBar(true);
        } else if (!fromUser) {
            /*
             * Try to hide the Nav Bar again.
             * It seems that you can't hide the Nav Bar if you previously
             * showed it in the last 1-2 seconds.
             */
            dimStatusBar(true);
        }
    }

    /**
     * Dim the status bar and/or navigation icons when needed on Android 3.x.
     * Hide it on Android 4.0 and later
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void dimStatusBar(boolean dim) {
        if (dim || mIsLocked)
            mActionBar.hide();
        else
            mActionBar.show();
        if (!AndroidUtil.isHoneycombOrLater || mIsNavMenu)
            return;
        int visibility = 0;
        int navbar = 0;

        if (AndroidUtil.isJellyBeanOrLater) {
            visibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
            navbar = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
        }
        if (dim || mIsLocked) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            if (AndroidUtil.isICSOrLater)
                navbar |= View.SYSTEM_UI_FLAG_LOW_PROFILE;
            else
                visibility |= View.STATUS_BAR_HIDDEN;
        } else {
            mActionBar.show();
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            if (AndroidUtil.isICSOrLater)
                visibility |= View.SYSTEM_UI_FLAG_VISIBLE;
            else
                visibility |= View.STATUS_BAR_VISIBLE;
        }

        if (AndroidDevices.hasNavBar())
            visibility |= navbar;
        getWindow().getDecorView().setSystemUiVisibility(visibility);
    }

    private void showTitle() {
        if (!AndroidUtil.isHoneycombOrLater || mIsNavMenu)
            return;
        int visibility = 0;
        int navbar = 0;
        mActionBar.show();

        if (AndroidUtil.isJellyBeanOrLater) {
            visibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
            navbar = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
        }
        if (AndroidUtil.isICSOrLater)
            navbar |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;

        if (AndroidDevices.hasNavBar())
            visibility |= navbar;
        getWindow().getDecorView().setSystemUiVisibility(visibility);

    }

    private void updateOverlayPausePlay() {
        if (mService == null || mPlayPause == null)
            return;
        if (mService.isPausable())
            mPlayPause.setImageResource(mService.isPlaying() ? R.drawable.ic_pause_circle
                    : R.drawable.ic_play_circle);
    }

    /**
     * update the overlay
     */
    private int setOverlayProgress() {
        if (mService == null) {
            return 0;
        }
        int time = (int) getTime();
        int length = (int) mService.getLength();
        if (length == 0) {
            MediaWrapper media = mService.getCurrentMediaWrapper();
            if (media.getId() == 0)
                media = mMedialibrary.findMedia(media);
            if (media != null)
                length = (int) media.getLength();
        }

        // Update all view elements
        if (mSeekbar != null) {
            mSeekbar.setMax(length);
            mSeekbar.setProgress(time);
        }
        if (mTime != null && time >= 0) mTime.setText(Tools.millisToString(time));
        if (mLength != null && length >= 0) mLength.setText(mDisplayRemainingTime && length > 0
                ? "-" + '\u00A0' + Tools.millisToString(length - time)
                : Tools.millisToString(length));
        //字幕
        updateTimedText(time);
        return time;
    }

    private void invalidateESTracks(int type) {
        switch (type) {
            case Media.Track.Type.Audio:
                mAudioTracksList = null;
                break;
            case Media.Track.Type.Text:
                mSubtitleTracksList = null;
                break;
        }
    }

    private void setESTracks() {
        if (mLastAudioTrack >= -1) {
            mService.setAudioTrack(mLastAudioTrack);
            mLastAudioTrack = -2;
        }
        if (mLastSpuTrack >= -1) {
            mService.setSpuTrack(mLastSpuTrack);
            mLastSpuTrack = -2;
        }
    }

    private void setESTrackLists() {
        if (mAudioTracksList == null && mService.getAudioTracksCount() > 0)
            mAudioTracksList = mService.getAudioTracks();
        if (mSubtitleTracksList == null && mService.getSpuTracksCount() > 0)
            mSubtitleTracksList = mService.getSpuTracks();
    }


    /**
     *
     */
    private void play() {
        mService.play();
        if (mRootView != null)
            mRootView.setKeepScreenOn(true);
    }

    /**
     *
     */
    private void pause() {
        mService.pause();
        if (mRootView != null)
            mRootView.setKeepScreenOn(false);
    }

    /*
     * Additionnal method to prevent alert dialog to pop up
     */
    @SuppressWarnings({"unchecked"})
    private void loadMedia(boolean fromStart) {
        System.out.println("loadMedia bb");
        mAskResume = false;
        getIntent().putExtra(PLAY_EXTRA_FROM_START, fromStart);
        loadMedia();
    }

    /**
     * External extras:
     * - position (long) - position of the video to start with (in ms)
     * - subtitles_location (String) - location of a subtitles file to load
     * - from_start (boolean) - Whether playback should start from start or from resume point
     * - title (String) - video title, will be guessed from file if not set.
     */
    @TargetApi(12)
    @SuppressWarnings({"unchecked"})
    protected void loadMedia() {
        Log.d("liutao", "loadMedia aa");
        if (mService == null)
            return;

        String mUris[] = null;
        mUri = null;
        mIsPlaying = false;
        String title = null;
        boolean fromStart = false;
        String itemTitle = null;
        int positionInPlaylist = -1;
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        long savedTime = extras != null ? extras.getLong(PLAY_EXTRA_START_TIME) : 0L; // position passed in by intent (ms)
        if (extras != null && savedTime == 0L)
            savedTime = extras.getInt(PLAY_EXTRA_START_TIME);

        final KeyguardManager km = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
        if (km.inKeyguardRestrictedInputMode())
            mWasPaused = true;
        if (mWasPaused)
            Log.d(TAG, "Video was previously paused, resuming in paused mode");

        if (intent.getData() != null)
            mUri = intent.getData();
        if (extras != null) {
            if (intent.hasExtra(PLAY_EXTRA_ITEMS_LOCATION)) {
                mUris = extras.getStringArray(PLAY_EXTRA_ITEMS_LOCATION);
                positionInPlaylist = intent.getIntExtra(PLAY_EXTRA_OPENED_POSITION, -1);
                if (positionInPlaylist >= 0 && mUris != null && mUris.length > 0) {
                    mUri = Uri.parse(mUris[positionInPlaylist]);
                }
            }

            fromStart = extras.getBoolean(PLAY_EXTRA_FROM_START, false);
            mAskResume &= !fromStart;

        }

        if (intent.hasExtra(PLAY_EXTRA_SUBTITLES_LOCATION))
            mSubtitleSelectedFiles.add(extras.getString(PLAY_EXTRA_SUBTITLES_LOCATION));
        if (intent.hasExtra(PLAY_EXTRA_ITEM_TITLE))
            itemTitle = extras.getString(PLAY_EXTRA_ITEM_TITLE);

        MediaWrapper openedMedia = null;

        if (positionInPlaylist != -1 && mService.hasMedia() && positionInPlaylist < mService.getMedias().size()) {
            // Provided externally from AudioService
            Log.d(TAG, "loadMedia Continuing playback from PlaybackService at index " + positionInPlaylist);
            openedMedia = mService.getMedias().get(positionInPlaylist);
            if (openedMedia == null) {
                encounteredError();
                return;
            }
            mUri = openedMedia.getUri();
            itemTitle = openedMedia.getTitle();
            updateSeekable(mService.isSeekable());
            updatePausable(mService.isPausable());

            mService.flush();
        }

        if (mUri != null) {
            if (mService.hasMedia() && !mUri.equals(mService.getCurrentMediaWrapper().getUri()))
                mService.stop();
            // restore last position
            MediaWrapper media;
            if (openedMedia == null || openedMedia.getId() <= 0L) {
                Medialibrary ml = mMedialibrary;
                media = ml.getMedia(mUri);

                if (media == null && TextUtils.equals(mUri.getScheme(), "file") &&
                        mUri.getPath() != null && mUri.getPath().startsWith("/sdcard")) {
                    mUri = FileUtils.convertLocalUri(mUri);
                    media = ml.getMedia(mUri);
                }
                if (media != null && media.getId() != 0L && media.getTime() == 0L) {
                    media.setTime((long) (media.getMetaLong(mMedialibrary,
                            MediaWrapper.META_PROGRESS) * (double) media.getLength()) / 100L);
                }
            } else {
                media = openedMedia;
            }

            if (media != null) {
                // in media library
                System.out.println("loadmedia media 22:");
                if (media.getTime() > 0 && !fromStart && positionInPlaylist == -1) {
                    if (mAskResume) {
                        showConfirmResumeDialog();
                        return;
                    }
                }
                // Consume fromStart option after first use to prevent
                // restarting again when playback is paused.
                intent.putExtra(PLAY_EXTRA_FROM_START, false);
                if (fromStart || mService.isPlaying())
                    media.setTime(0L);
                else if (savedTime <= 0L)
                    savedTime = media.getTime();

                mLastAudioTrack = media.getAudioTrack();
                mLastSpuTrack = media.getSpuTrack();
            } else {
                // not in media library
                Log.d("liutao", "loadMedia meida null:");
                if (savedTime > 0L && mAskResume) {
                    showConfirmResumeDialog();
                    return;
                } else {
                    long rTime = mSettings.getLong(Preferences.VIDEO_RESUME_TIME, -1);
                    System.out.println("liutao rTime:" + rTime);
                    System.out.println("liutao fromStart:" + fromStart);
                    if (rTime > 0 && !fromStart) {
                        if (mAskResume) {
                            showConfirmResumeDialog();
                            return;
                        } else {
                            Editor editor = mSettings.edit();
                            editor.putLong(Preferences.VIDEO_RESUME_TIME, -1);
                            editor.apply();
                            savedTime = rTime;
                            System.out.println("liutao savedTime:" + savedTime);
                        }
                    }
                }
            }


            // Start playback & seek
            mService.addCallback(this);
            /* prepare playback */
            boolean hasMedia = mService.hasMedia();
            System.out.println("loadmedia hasmedia:" + hasMedia);
            if (hasMedia) {
                media = mService.getCurrentMediaWrapper();
            } else if (media == null) {
                media = new MediaWrapper(mUri);
            }


            if (mWasPaused) {
                media.addFlags(MediaWrapper.MEDIA_PAUSED);
            }

            if (intent.hasExtra(PLAY_DISABLE_HARDWARE)) {
                media.addFlags(MediaWrapper.MEDIA_NO_HWACCEL);
            }

            media.removeFlags(MediaWrapper.MEDIA_FORCE_AUDIO);
            media.addFlags(MediaWrapper.MEDIA_VIDEO);

            if (savedTime <= 0L && media.getTime() > 0L) {
                savedTime = media.getTime();
            }
            if (savedTime > 0L && !mService.isPlaying()) {
                mService.saveTimeToSeek(savedTime);
            }
            // Handle playback
            if (!hasMedia) {
                List<MediaWrapper> list = new ArrayList<>();
                if (mUris != null && mUris.length > 0) {
                    for (String uri : mUris) {
                        list.add(new MediaWrapper(Uri.parse(uri)));
                    }
                }
                mService.load(list, positionInPlaylist);
            } else if (!mService.isPlaying()) {
                mService.playIndex(positionInPlaylist);
            } else {
                onPlaying();
            }

            getSubtitles();

            if (itemTitle == null && !TextUtils.equals(mUri.getScheme(), "content"))
                title = mUri.getLastPathSegment();
        }
        if (itemTitle != null)
            title = itemTitle;
        mTitle.setText(title);

        if (mWasPaused) {
            // XXX: Workaround to update the seekbar position
            mForcedTime = savedTime;
            setOverlayProgress();
            mForcedTime = -1;

            showOverlay(true);
        }
    }

    private SubtitlesGetTask mSubtitlesGetTask = null;

    private class SubtitlesGetTask extends AsyncTask<String, Void, ArrayList<String>> {
        @Override
        protected ArrayList<String> doInBackground(String... strings) {
            final String subtitleList_serialized = strings[0];
            ArrayList<String> prefsList = new ArrayList<>();

            if (subtitleList_serialized != null) {
                ByteArrayInputStream bis = new ByteArrayInputStream(subtitleList_serialized.getBytes());
                try {
                    ObjectInputStream ois = new ObjectInputStream(bis);
                    prefsList = (ArrayList<String>) ois.readObject();
                } catch (InterruptedIOException ignored) {
                    return prefsList; /* Task is cancelled */
                } catch (ClassNotFoundException | IOException ignored) {
                }
            }

            return prefsList;
        }

        @Override
        protected void onPostExecute(ArrayList<String> prefsList) {
            // Add any selected subtitle file from the file picker
            if (prefsList.size() > 0) {
                for (String file : prefsList) {
                    if (!mSubtitleSelectedFiles.contains(file))
                        mSubtitleSelectedFiles.add(file);
                    Log.i(TAG, "Adding user-selected subtitle " + file);
                    mService.addSubtitleTrack(file, true);
                }
            }
            mSubtitlesGetTask = null;
        }

        @Override
        protected void onCancelled() {
            mSubtitlesGetTask = null;
        }
    }

    public void getSubtitles() {
        if (mSubtitlesGetTask != null || mService == null)
            return;
        final String subtitleList_serialized = mSettings.getString(Preferences.VIDEO_SUBTITLE_FILES, null);

        mSubtitlesGetTask = new SubtitlesGetTask();
        mSubtitlesGetTask.execute(subtitleList_serialized);
    }

    @SuppressWarnings("deprecation")
    private int getScreenRotation() {
        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        try {
            Method m = display.getClass().getDeclaredMethod("getRotation");
            return (Integer) m.invoke(display);
        } catch (Exception e) {
            return Surface.ROTATION_0;
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private int getScreenOrientation(int mode) {
        switch (mode) {
            case 99: //screen orientation user
                return AndroidUtil.isJellyBeanMR2OrLater ?
                        ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR :
                        ActivityInfo.SCREEN_ORIENTATION_SENSOR;
            case 101: //screen orientation landscape
                return ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE;
            case 102: //screen orientation portrait
                return ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT;
        }
        /*
         mScreenOrientation = 100, we lock screen at its current orientation
         */
        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        int rot = getScreenRotation();
        /*
         * Since getRotation() returns the screen's "natural" orientation,
         * which is not guaranteed to be SCREEN_ORIENTATION_PORTRAIT,
         * we have to invert the SCREEN_ORIENTATION value if it is "naturally"
         * landscape.
         */
        @SuppressWarnings("deprecation")
        boolean defaultWide = display.getWidth() > display.getHeight();
        if (rot == Surface.ROTATION_90 || rot == Surface.ROTATION_270)
            defaultWide = !defaultWide;
        if (defaultWide) {
            switch (rot) {
                case Surface.ROTATION_0:
                    return ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                case Surface.ROTATION_90:
                    return ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                case Surface.ROTATION_180:
                    // SCREEN_ORIENTATION_REVERSE_PORTRAIT only available since API
                    // Level 9+
                    return ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                case Surface.ROTATION_270:
                    // SCREEN_ORIENTATION_REVERSE_LANDSCAPE only available since API
                    // Level 9+
                    return ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
                default:
                    return 0;
            }
        } else {
            switch (rot) {
                case Surface.ROTATION_0:
                    return ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                case Surface.ROTATION_90:
                    return ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                case Surface.ROTATION_180:
                    // SCREEN_ORIENTATION_REVERSE_PORTRAIT only available since API
                    // Level 9+
                    return ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
                case Surface.ROTATION_270:
                    // SCREEN_ORIENTATION_REVERSE_LANDSCAPE only available since API
                    // Level 9+
                    return ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                default:
                    return 0;
            }
        }
    }

    public void showConfirmResumeDialog() {
        if (isFinishing())
            return;
        mService.pause();
        /* Encountered Error, exit player with a message */
        mAlertDialog = new AlertDialog.Builder(VideoPlayerActivity.this)
                .setMessage(R.string.confirm_resume)
                .setPositiveButton(R.string.resume_from_position, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        loadMedia(false);
                    }
                })
                .setNegativeButton(R.string.play_from_start, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        loadMedia(true);
                    }
                })
                .create();
        mAlertDialog.setCancelable(false);
        mAlertDialog.show();
    }


    private BroadcastReceiver mBtReceiver = AndroidUtil.isICSOrLater ? new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED:
                case BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED:
                    long savedDelay = mSettings.getLong(KEY_BLUETOOTH_DELAY, 0l);
                    long currentDelay = mService.getAudioDelay();
                    if (savedDelay != 0l) {
                        boolean connected = intent.getIntExtra(BluetoothA2dp.EXTRA_STATE, -1) == BluetoothA2dp.STATE_CONNECTED;
                        if (connected && currentDelay == 0l)
                            toggleBtDelay(true);
                        else if (!connected && savedDelay == currentDelay)
                            toggleBtDelay(false);
                    }
            }
        }
    } : null;

    private void toggleBtDelay(boolean connected) {
        mService.setAudioDelay(connected ? mSettings.getLong(KEY_BLUETOOTH_DELAY, 0) : 0l);
    }

    private OnClickListener mBtSaveListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            mSettings.edit().putLong(KEY_BLUETOOTH_DELAY, mService.getAudioDelay()).apply();
        }
    };


    /**
     * Start the video loading animation.
     */
    private void startLoading() {
        if (mIsLoading)
            return;
        mIsLoading = true;
        AnimationSet anim = new AnimationSet(true);
        RotateAnimation rotate = new RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotate.setDuration(800);
        rotate.setInterpolator(new DecelerateInterpolator());
        rotate.setRepeatCount(RotateAnimation.INFINITE);
        anim.addAnimation(rotate);
        mLoading.setVisibility(View.VISIBLE);
        mLoading.startAnimation(anim);
    }

    /**
     * Stop the video loading animation.
     */
    private void stopLoading() {
        mHandler.removeMessages(LOADING_ANIMATION);
        if (!mIsLoading)
            return;
        mIsLoading = false;
        mLoading.setVisibility(View.INVISIBLE);
        mLoading.clearAnimation();

    }


    private void updateNavStatus() {
        mIsNavMenu = false;
        mMenuIdx = -1;

        final MediaPlayer.Title[] titles = mService.getTitles();
        if (titles != null) {
            final int currentIdx = mService.getTitleIdx();
            for (int i = 0; i < titles.length; ++i) {
                final MediaPlayer.Title title = titles[i];
                if (title.isMenu()) {
                    mMenuIdx = i;
                    break;
                }
            }
            mIsNavMenu = mMenuIdx == currentIdx;
        }

        if (mIsNavMenu) {
            /*
             * Keep the overlay hidden in order to have touch events directly
             * transmitted to navigation handling.
             */
            hideOverlay(false);
        } else if (mMenuIdx != -1)
            setESTracks();

        supportInvalidateOptionsMenu();
    }

    private GestureDetector.SimpleOnGestureListener mGestureListener = new GestureDetector.SimpleOnGestureListener() {

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            mHandler.sendEmptyMessageDelayed(mShowing ? HIDE_INFO : SHOW_INFO, 200);
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            mHandler.removeMessages(HIDE_INFO);
            mHandler.removeMessages(SHOW_INFO);
            float range = mCurrentScreenOrientation == Configuration.ORIENTATION_LANDSCAPE ? mSurfaceXDisplayRange : mSurfaceYDisplayRange;
            if (mService == null)
                return false;
            if (!mIsLocked) {
                float x = e.getX();
                if (x < range / 4f)
                    seekDelta(-10000);
                else if (x > range * 0.75)
                    seekDelta(10000);
                else
                    doPlayPause();
                return true;
            }
            return false;
        }
    };

    public PlaybackServiceActivity.Helper getHelper() {
        return mHelper;
    }

    @Override
    public void onConnected(PlaybackService service) {
        mService = service;
        Log.d("liutao", "onConnected: " + mSwitchingView);
        if (!mSwitchingView)
            mHandler.sendEmptyMessage(START_PLAYBACK);
        mSwitchingView = false;
        mSettings.edit().putBoolean(Preferences.VIDEO_RESTORE, false).apply();
    }

    @Override
    public void onDisconnected() {
        mService = null;
        mHandler.sendEmptyMessage(AUDIO_SERVICE_CONNECTION_FAILED);
    }

    @Override
    public void onNewVideoLayout(IVLCVout vlcVout, int width, int height, int visibleWidth, int visibleHeight, int sarNum, int sarDen) {
        // store video size
        mVideoWidth = width;
        mVideoHeight = height;
        mVideoVisibleWidth = visibleWidth;
        mVideoVisibleHeight = visibleHeight;
        mSarNum = sarNum;
        mSarDen = sarDen;
        changeSurfaceLayout();
    }

    @Override
    public void onSurfacesCreated(IVLCVout vlcVout) {
    }

    @Override
    public void onSurfacesDestroyed(IVLCVout vlcVout) {
    }

    private BroadcastReceiver mServiceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (TextUtils.equals(intent.getAction(), PLAY_FROM_SERVICE))
                onNewIntent(intent);
            else if (TextUtils.equals(intent.getAction(), EXIT_PLAYER))
                exitOK();
        }
    };
}
