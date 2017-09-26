/*****************************************************************************
 * PlaybackService.java
 *****************************************************************************
 * Copyright © 2011-2017 VLC authors and VideoLAN
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston MA 02110-1301, USA.
 *****************************************************************************/

package com.dmsys.vlcplayer;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.dmsys.vlcplayer.media.MediaWrapperList;
import com.dmsys.vlcplayer.util.FileUtils;
import com.dmsys.vlcplayer.util.Preferences;
import com.dmsys.vlcplayer.util.Strings;
import com.dmsys.vlcplayer.util.VLCInstance;
import com.dmsys.vlcplayer.util.VLCOptions;
import com.dmsys.vlcplayer.util.WeakHandler;
import com.dmsys.vlcplayer.video.PopupManager;

import org.videolan.libvlc.IVLCVout;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaList;
import org.videolan.libvlc.MediaPlayer;
import org.videolan.medialibrary.Medialibrary;
import org.videolan.medialibrary.media.MediaWrapper;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicBoolean;

public class PlaybackService extends MediaBrowserServiceCompat implements IVLCVout.Callback {

    private static final String TAG = "VLC/PlaybackService";

    private static final int SHOW_PROGRESS = 0;
    private static final int SHOW_TOAST = 1;
    public static final String ACTION_REMOTE_GENERIC =  Strings.buildPkgString("remote.");
    public static final String ACTION_REMOTE_BACKWARD = ACTION_REMOTE_GENERIC+"Backward";
    public static final String ACTION_REMOTE_PLAY = ACTION_REMOTE_GENERIC+"Play";
    public static final String ACTION_REMOTE_PLAYPAUSE = ACTION_REMOTE_GENERIC+"PlayPause";
    public static final String ACTION_REMOTE_PAUSE = ACTION_REMOTE_GENERIC+"Pause";
    public static final String ACTION_REMOTE_STOP = ACTION_REMOTE_GENERIC+"Stop";
    public static final String ACTION_REMOTE_FORWARD = ACTION_REMOTE_GENERIC+"Forward";
    public static final String ACTION_REMOTE_LAST_PLAYLIST = ACTION_REMOTE_GENERIC+"LastPlaylist";
    public static final String ACTION_REMOTE_LAST_VIDEO_PLAYLIST = ACTION_REMOTE_GENERIC+"LastVideoPlaylist";
    public static final String ACTION_REMOTE_SWITCH_VIDEO = ACTION_REMOTE_GENERIC+"SwitchToVideo";
    public static final String ACTION_PLAY_FROM_SEARCH = ACTION_REMOTE_GENERIC+"play_from_search";

    public static final String EXTRA_SEARCH_BUNDLE = ACTION_REMOTE_GENERIC+"extra_search_bundle";

    private static final int DELAY_DOUBLE_CLICK = 800;
    private static final int DELAY_LONG_CLICK = 1000;

    public interface Callback {
        void update();
        void updateProgress();
        void onMediaEvent(Media.Event event);
        void onMediaPlayerEvent(MediaPlayer.Event event);
    }

    private class LocalBinder extends Binder {
        PlaybackService getService() {
            return PlaybackService.this;
        }
    }
    public static PlaybackService getService(IBinder iBinder) {
        LocalBinder binder = (LocalBinder) iBinder;
        return binder.getService();
    }

    private SharedPreferences mSettings;
    private final IBinder mBinder = new LocalBinder();
    private MediaWrapperList mMediaList = new MediaWrapperList();
    private Medialibrary mMedialibrary;
    private MediaPlayer mMediaPlayer;
    private boolean mParsed = false;
    private boolean mSeekable = false;
    private boolean mPausable = false;
    private boolean mSwitchingToVideo = false;
    private boolean mVideoBackground = false;

    final private ArrayList<Callback> mCallbacks = new ArrayList<>();
    private boolean mDetectHeadset = true;
    private PowerManager.WakeLock mWakeLock;
    private final AtomicBoolean mExpanding = new AtomicBoolean(false);

    // Index management
    /**
     * Stack of previously played indexes, used in shuffle mode
     */
    private Stack<Integer> mPrevious;
    private int mCurrentIndex; // Set to -1 if no media is currently loaded
    private int mPrevIndex; // Set to -1 if no previous media
    private int mNextIndex; // Set to -1 if no next media

    // Playback management
    private static final long PLAYBACK_BASE_ACTIONS = PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH
            | PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID | PlaybackStateCompat.ACTION_PLAY_FROM_URI
            | PlaybackStateCompat.ACTION_PLAY_PAUSE;

    public static final int TYPE_AUDIO = 0;
    public static final int TYPE_VIDEO = 1;

    public static final int REPEAT_NONE = 0;
    public static final int REPEAT_ONE = 1;
    public static final int REPEAT_ALL = 2;
    private boolean mShuffling = false;
    private int mRepeating = REPEAT_ALL;
    private Random mRandom = null; // Used in shuffling process
    private long mSavedTime = 0L;
    private boolean mHasAudioFocus = false;

    /**
     * Last widget position update timestamp
     */
    private long mWidgetPositionTimestamp = Calendar.getInstance().getTimeInMillis();
    private PopupManager mPopupManager;

    /* boolean indicating if the player is in benchmark mode */
    private boolean mIsBenchmark = false;
    /* boolenan indication if the player is in hardware mode */
    private boolean mIsHardware = false;

    private LibVLC LibVLC() {
        return VLCInstance.get(this);
    }

    private MediaPlayer newMediaPlayer() {
        final MediaPlayer mp = new MediaPlayer(LibVLC());
        final String aout = VLCOptions.getAout(mSettings);
        if (aout != null)
            mp.setAudioOutput(aout);
        mp.getVLCVout().addCallback(this);

        return mp;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mSettings = PreferenceManager.getDefaultSharedPreferences(this);
        mMediaPlayer = newMediaPlayer();
        mMediaPlayer.setEqualizer(VLCOptions.getEqualizer(this));

        if (!VLCInstance.testCompatibleCPU(this)) {
            stopSelf();
            return;
        }

        VLCInstance.get(this); // ensure VLC is loaded before medialibrary
        mMedialibrary = Medialibrary.getInstance(this);

        mDetectHeadset = mSettings.getBoolean("enable_headset_detection", true);

        mCurrentIndex = -1;
        mPrevIndex = -1;
        mNextIndex = -1;
        mPrevious = new Stack<>();

        // Make sure the audio player will acquire a wake-lock while playing. If we don't do
        // that, the CPU might go to sleep while the song is playing, causing playback to stop.
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);

        IntentFilter filter = new IntentFilter();
        filter.setPriority(Integer.MAX_VALUE);
        filter.addAction(ACTION_REMOTE_BACKWARD);
        filter.addAction(ACTION_REMOTE_PLAYPAUSE);
        filter.addAction(ACTION_REMOTE_PLAY);
        filter.addAction(ACTION_REMOTE_PAUSE);
        filter.addAction(ACTION_REMOTE_STOP);
        filter.addAction(ACTION_REMOTE_FORWARD);
        filter.addAction(ACTION_REMOTE_LAST_PLAYLIST);
        filter.addAction(ACTION_REMOTE_LAST_VIDEO_PLAYLIST);
        filter.addAction(ACTION_REMOTE_SWITCH_VIDEO);
        filter.addAction(Intent.ACTION_HEADSET_PLUG);
        filter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        registerReceiver(mReceiver, filter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        System.out.println("onStartCommand");

        if (intent == null)
            return START_STICKY;
        String action = intent.getAction();

         if (ACTION_REMOTE_PLAY.equals(action)) {
                if (hasCurrentMedia())
                    play();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stop();

        if (mWakeLock.isHeld())
            mWakeLock.release();
        unregisterReceiver(mReceiver);
        mMediaPlayer.release();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return SERVICE_INTERFACE.equals(intent.getAction()) ? super.onBind(intent) : mBinder;
    }

    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        return null;
    }

    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {

    }

    public IVLCVout getVLCVout()  {
        return mMediaPlayer.getVLCVout();
    }

    private final OnAudioFocusChangeListener mAudioFocusListener = createOnAudioFocusChangeListener();

    private OnAudioFocusChangeListener createOnAudioFocusChangeListener() {
        return new OnAudioFocusChangeListener() {
            private boolean mLossTransient = false;
            private boolean wasPlaying = false;

            @Override
            public void onAudioFocusChange(int focusChange) {
                /*
                 * Pause playback during alerts and notifications
                 */
                switch (focusChange) {
                    case AudioManager.AUDIOFOCUS_LOSS:
                        Log.i(TAG, "AUDIOFOCUS_LOSS");
                        // Pause playback
                        changeAudioFocus(false);
                        pause();
                        break;
                    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                        Log.i(TAG, "AUDIOFOCUS_LOSS_TRANSIENT");
                        // Pause playback
                        mLossTransient = true;
                        wasPlaying = isPlaying();
                        if (wasPlaying)
                            pause();
                        break;
                    case AudioManager.AUDIOFOCUS_GAIN:
                        Log.i(TAG, "AUDIOFOCUS_GAIN: ");
                        // Resume playback
                        if (mLossTransient) {
                            if (wasPlaying && mSettings.getBoolean("resume_playback", true))
                                mMediaPlayer.play();
                            mLossTransient = false;
                        }
                        break;
                }
            }
        };
    }

    private void changeAudioFocus(boolean acquire) {
        final AudioManager am = (AudioManager)getSystemService(AUDIO_SERVICE);
        if (am == null)
            return;

        if (acquire) {
            if (!mHasAudioFocus) {
                final int result = am.requestAudioFocus(mAudioFocusListener,
                        AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
                if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                    am.setParameters("bgm_state=true");
                    mHasAudioFocus = true;
                }
            }
        } else {
            if (mHasAudioFocus) {
                am.abandonAudioFocus(mAudioFocusListener);
                am.setParameters("bgm_state=false");
                mHasAudioFocus = false;
            }
        }
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        private boolean wasPlaying = false;
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            int state = intent.getIntExtra("state", 0);
            if( mMediaPlayer == null ) {
                Log.w(TAG, "Intent received, but VLC is not loaded, skipping.");
                return;
            }

            // skip all headsets events if there is a call
            TelephonyManager telManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            if (telManager != null && telManager.getCallState() != TelephonyManager.CALL_STATE_IDLE)
                return;

            /*
             * Launch the activity if needed
             */
            if (action.startsWith(ACTION_REMOTE_GENERIC) && !mMediaPlayer.isPlaying() && !hasCurrentMedia()) {
                context.startActivity(getPackageManager().getLaunchIntentForPackage(getPackageName()));
            }

            /*
             * Remote / headset control events
             */
            if (action.equalsIgnoreCase(ACTION_REMOTE_PLAYPAUSE)) {

                if (mMediaPlayer.isPlaying())
                    pause();
                else
                    play();
            } else if (action.equalsIgnoreCase(ACTION_REMOTE_PLAY)) {
                if (!mMediaPlayer.isPlaying() && hasCurrentMedia())
                    play();
            } else if (action.equalsIgnoreCase(ACTION_REMOTE_PAUSE)) {
                if (hasCurrentMedia())
                    pause();
            } else if (action.equalsIgnoreCase(ACTION_REMOTE_BACKWARD)) {
                previous(false);
            } else if (action.equalsIgnoreCase(ACTION_REMOTE_FORWARD)) {
                next();
            } else if (action.equalsIgnoreCase(ACTION_REMOTE_SWITCH_VIDEO)) {
                removePopup();
                if (hasMedia()) {
                    getCurrentMediaWrapper().removeFlags(MediaWrapper.MEDIA_FORCE_AUDIO);
                    switchToVideo();
                }
            }

            /*
             * headset plug events
             */
            else if (mDetectHeadset) {
                if (action.equalsIgnoreCase(AudioManager.ACTION_AUDIO_BECOMING_NOISY)) {
                    Log.i(TAG, "Becoming noisy");
                    wasPlaying = isPlaying();
                    if (wasPlaying && hasCurrentMedia())
                        pause();
                } else if (action.equalsIgnoreCase(Intent.ACTION_HEADSET_PLUG) && state != 0) {
                    Log.i(TAG, "Headset Inserted.");
                    if (wasPlaying && hasCurrentMedia() && mSettings.getBoolean("enable_play_on_headset_insertion", false))
                        play();
                }
            }
        }
    };

    @Override
    public void onSurfacesCreated(IVLCVout vlcVout) {
    }

    @Override
    public void onSurfacesDestroyed(IVLCVout vlcVout) {
        mSwitchingToVideo = false;
    }

    private final Media.EventListener mMediaListener = new Media.EventListener() {
        @Override
        public void onEvent(Media.Event event) {
            boolean update = true;
            switch (event.type) {
                case Media.Event.MetaChanged:
                    /* Update Meta if file is already parsed */
                    if (mParsed && updateCurrentMeta(event.getMetaId()))
                        executeUpdate();
                    Log.i(TAG, "Media.Event.MetaChanged: " + event.getMetaId());
                    break;
                case Media.Event.ParsedChanged:
                    Log.i(TAG, "Media.Event.ParsedChanged");
                    updateCurrentMeta(-1);
                    mParsed = true;
                    break;
                default:
                    update = false;

            }
            if (update) {
                synchronized (mCallbacks) {
                    for (Callback callback : mCallbacks)
                        callback.onMediaEvent(event);
                }
            }
        }
    };

    public void setBenchmark() { mIsBenchmark = true; }
    public void setHardware() { mIsHardware = true; }

    /**
     * Update current media meta and return true if player needs to be updated
     *
     * @param id of the Meta event received, -1 for none
     * @return true if UI needs to be updated
     */
    private boolean updateCurrentMeta(int id) {
        if (id == Media.Meta.Publisher)
            return false;
        final MediaWrapper mw = getCurrentMedia();
        if (mw != null)
            mw.updateMeta(mMediaPlayer);
        return id != Media.Meta.NowPlaying || getCurrentMedia().getNowPlaying() != null;
    }

    private Media.Stats previousMediaStats = null;

    public Media.Stats getLastStats() {
       return previousMediaStats;
    }

    private final MediaPlayer.EventListener mMediaPlayerListener = new MediaPlayer.EventListener() {
        //KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);

        @Override
        public void onEvent(MediaPlayer.Event event) {
            switch (event.type) {
                case MediaPlayer.Event.Playing:
                    loadMediaMeta();
                    if(mSavedTime != 0L)
                        seek(mSavedTime);
                    mSavedTime = 0L;

                    Log.i(TAG, "MediaPlayer.Event.Playing");
                    executeUpdate();
                    publishState();
                    executeUpdateProgress();
                    mHandler.sendEmptyMessage(SHOW_PROGRESS);
                    changeAudioFocus(true);
                    if (!mWakeLock.isHeld())
                        mWakeLock.acquire();

                    mVideoBackground = false;
                    if (getCurrentMediaWrapper().getType() == MediaWrapper.TYPE_STREAM)
                        mMedialibrary.addToHistory(getCurrentMediaLocation(), getCurrentMediaWrapper().getTitle());
                    break;
                case MediaPlayer.Event.Paused:
                    Log.i(TAG, "MediaPlayer.Event.Paused");
                    executeUpdate();
                    publishState();
                    executeUpdateProgress();

                    mHandler.removeMessages(SHOW_PROGRESS);
                    if (mWakeLock.isHeld())
                        mWakeLock.release();
                    break;
                case MediaPlayer.Event.Stopped:
                    Log.i(TAG, "MediaPlayer.Event.Stopped");
                    saveMediaMeta();
                    executeUpdate();
                    publishState();
                    executeUpdateProgress();
                    if (mWakeLock.isHeld())
                        mWakeLock.release();
                    changeAudioFocus(false);
                    break;
                case MediaPlayer.Event.EndReached:
                    saveMediaMeta();
                    executeUpdateProgress();
                    previousMediaStats = mMediaPlayer.getMedia().getStats();
                    determinePrevAndNextIndices(true);
                    next();
                    if (mWakeLock.isHeld())
                        mWakeLock.release();
                    changeAudioFocus(false);
                    break;
                case MediaPlayer.Event.EncounteredError:
                    showToast("播放s失败", Toast.LENGTH_SHORT);

                    if (mWakeLock.isHeld())
                        mWakeLock.release();
                    break;
                case MediaPlayer.Event.TimeChanged:
                    break;
                case MediaPlayer.Event.PositionChanged:
                    break;
                case MediaPlayer.Event.Vout:
                    break;
                case MediaPlayer.Event.ESAdded:
                    break;
                case MediaPlayer.Event.ESDeleted:
                    break;
                case MediaPlayer.Event.PausableChanged:
                    mPausable = event.getPausable();
                    break;
                case MediaPlayer.Event.SeekableChanged:
                    mSeekable = event.getSeekable();
                    break;
                case MediaPlayer.Event.MediaChanged:
                    Log.d(TAG, "onEvent: MediaChanged");
            }
            synchronized (mCallbacks) {
                for (Callback callback : mCallbacks)
                    callback.onMediaPlayerEvent(event);
            }
        }
    };


    public void saveMediaMeta() {
        MediaWrapper media = mMedialibrary.findMedia(getCurrentMediaWrapper());
        if (media == null || media.getId() == 0)
            return;
        if (canSwitchToVideo()) {
            //Save progress
            long time = getTime();
            float progress = time / (float)media.getLength();
            if (progress > 0.95f)
                progress = 0f;
            media.setTime(progress == 0f ? 0L : time);
            media.setLongMeta(mMedialibrary, MediaWrapper.META_PROGRESS, (long) (progress*100));
            //Save audio delay
            if (mSettings.getBoolean("save_individual_audio_delay", false))
                media.setLongMeta(mMedialibrary, MediaWrapper.META_AUDIODELAY, mMediaPlayer.getAudioDelay());
            media.setLongMeta(mMedialibrary, MediaWrapper.META_SUBTITLE_DELAY, mMediaPlayer.getSpuDelay());
            media.setLongMeta(mMedialibrary, MediaWrapper.META_SUBTITLE_TRACK, mMediaPlayer.getSpuTrack());
        }
    }

    private void loadMediaMeta() {
        MediaWrapper media = mMedialibrary.findMedia(getCurrentMediaWrapper());
        if (media == null || media.getId() == 0)
            return;
        if (canSwitchToVideo()) {
            if (mSettings.getBoolean("save_individual_audio_delay", false))
                mMediaPlayer.setAudioDelay(media.getMetaLong(mMedialibrary, MediaWrapper.META_AUDIODELAY));
            mMediaPlayer.setSpuTrack((int) media.getMetaLong(mMedialibrary, MediaWrapper.META_SUBTITLE_TRACK));
            mMediaPlayer.setSpuDelay(media.getMetaLong(mMedialibrary, MediaWrapper.META_SUBTITLE_DELAY));
        }
    }

    private final MediaWrapperList.EventListener mListEventListener = new MediaWrapperList.EventListener() {

        @Override
        public void onItemAdded(int index, String mrl) {
            Log.i(TAG, "CustomMediaListItemAdded");
            if(mCurrentIndex >= index && !mExpanding.get())
                mCurrentIndex++;

            determinePrevAndNextIndices();
            executeUpdate();
        }

        @Override
        public void onItemRemoved(int index, String mrl) {
            Log.i(TAG, "CustomMediaListItemDeleted");
            if (mCurrentIndex == index && !mExpanding.get()) {
                // The current item has been deleted
                mCurrentIndex--;
                determinePrevAndNextIndices();
                if (mNextIndex != -1)
                    next();
                else if (mCurrentIndex != -1) {
                    playIndex(mCurrentIndex, 0);
                } else
                    stop();
            }

            if(mCurrentIndex > index && !mExpanding.get())
                mCurrentIndex--;
            determinePrevAndNextIndices();
            executeUpdate();
        }

        @Override
        public void onItemMoved(int indexBefore, int indexAfter, String mrl) {
            Log.i(TAG, "CustomMediaListItemMoved");
            if (mCurrentIndex == indexBefore) {
                mCurrentIndex = indexAfter;
                if (indexAfter > indexBefore)
                    mCurrentIndex--;
            } else if (indexBefore > mCurrentIndex
                    && indexAfter <= mCurrentIndex)
                mCurrentIndex++;
            else if (indexBefore < mCurrentIndex
                    && indexAfter > mCurrentIndex)
                mCurrentIndex--;

            // If we are in random mode, we completely reset the stored previous track
            // as their indices changed.
            mPrevious.clear();

            determinePrevAndNextIndices();
            executeUpdate();
        }
    };

    public boolean canSwitchToVideo() {
        return hasCurrentMedia() && mMediaPlayer.getVideoTracksCount() > 0;
    }

    @MainThread
    public boolean switchToVideo() {
        MediaWrapper media = mMediaList.getMedia(mCurrentIndex);
        if (media == null || media.hasFlag(MediaWrapper.MEDIA_FORCE_AUDIO) || !canSwitchToVideo())
            return false;
        mVideoBackground = false;
        if (isVideoPlaying()) {//Player is already running, just send it an intent

        } else if (!mSwitchingToVideo) {//Start the video player
            VideoPlayerActivity.startOpened(this, media.getUri(), mCurrentIndex);
            mSwitchingToVideo = true;
        }
        return true;
    }

    private void executeUpdate() {
        executeUpdate(true);
    }

    private void executeUpdate(Boolean updateWidget) {
        synchronized (mCallbacks) {
            for (Callback callback : mCallbacks) {
                callback.update();
            }
        }
        broadcastMetadata();
    }

    private void executeUpdateProgress() {
        synchronized (mCallbacks) {
            for (Callback callback : mCallbacks) {
                callback.updateProgress();
            }
        }
    }

    /**
     * Return the current media.
     *
     * @return The current media or null if there is not any.
     */
    @Nullable
    private MediaWrapper getCurrentMedia() {
        return mMediaList.getMedia(mCurrentIndex);
    }

    /**
     * Alias for mCurrentIndex >= 0
     *
     * @return True if a media is currently loaded, false otherwise
     */
    private boolean hasCurrentMedia() {
        return isValidIndex(mCurrentIndex);
    }

    private final Handler mHandler = new AudioServiceHandler(this);

    private class AudioServiceHandler extends WeakHandler<PlaybackService> {
        public AudioServiceHandler(PlaybackService fragment) {
            super(fragment);
        }

        @Override
        public void handleMessage(Message msg) {
            PlaybackService service = getOwner();
            if (service == null)
                return;

            switch (msg.what) {
                case SHOW_PROGRESS:
                    synchronized (service.mCallbacks) {
                        if (service.mCallbacks.size() > 0) {
                            removeMessages(SHOW_PROGRESS);
                            service.executeUpdateProgress();
                            sendEmptyMessageDelayed(SHOW_PROGRESS, 1000);
                        }
                    }
                    break;
                case SHOW_TOAST:
                    final Bundle bundle = msg.getData();
                    final String text = bundle.getString("text");
                    Toast.makeText(PlaybackService.this, text, Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

    @NonNull
    private String getMediaDescription(String artist, String album) {
        StringBuilder contentBuilder = new StringBuilder(artist);
        if (contentBuilder.length() > 0 && !TextUtils.isEmpty(album))
            contentBuilder.append(" - ");
        contentBuilder.append(album);
        return contentBuilder.toString();
    }


    @MainThread
    public void pause() {
        if (mPausable) {
            savePosition();
            mMediaPlayer.pause();
        }
    }

    @MainThread
    public void play() {
        if (hasCurrentMedia())
            mMediaPlayer.play();
    }

    @MainThread
    public void stop() {
        removePopup();
        if (mMediaPlayer == null)
            return;
        savePosition();
        final Media media = mMediaPlayer.getMedia();
        if (media != null) {
            saveMediaMeta();
            media.setEventListener(null);
            mMediaPlayer.setEventListener(null);
            mMediaPlayer.stop();
            mMediaPlayer.setMedia(null);
            media.release();
            publishState();
        }
        mMediaList.removeEventListener(mListEventListener);
        mCurrentIndex = -1;
        mPrevious.clear();
        mHandler.removeMessages(SHOW_PROGRESS);
        broadcastMetadata();
        executeUpdate();
        executeUpdateProgress();
        changeAudioFocus(false);
    }

    private synchronized void savePosition(){
        if (getCurrentMedia() == null)
            return;
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putBoolean(Preferences.VIDEO_PAUSED, !isPlaying());
        editor.apply();
    }

    public void determinePrevAndNextIndices() {
        determinePrevAndNextIndices(false);
    }

    private void determinePrevAndNextIndices(boolean expand) {
        if (expand) {
            mExpanding.set(true);
            mNextIndex = expand(getCurrentMedia().getType() == MediaWrapper.TYPE_STREAM);
            mExpanding.set(false);
        } else {
            mNextIndex = -1;
        }
        mPrevIndex = -1;

        if (mNextIndex == -1) {
            // No subitems; play the next item.
            int size = mMediaList.size();
            mShuffling &= size > 2;

            // Repeating once doesn't change the index
            if (mRepeating == REPEAT_ONE) {
                mPrevIndex = mNextIndex = mCurrentIndex;
            } else {

                if(mShuffling) {
                    if(!mPrevious.isEmpty()){
                        mPrevIndex = mPrevious.peek();
                        while (!isValidIndex(mPrevIndex)) {
                            mPrevious.remove(mPrevious.size() - 1);
                            if (mPrevious.isEmpty()) {
                                mPrevIndex = -1;
                                break;
                            }
                            mPrevIndex = mPrevious.peek();
                        }
                    }
                    // If we've played all songs already in shuffle, then either
                    // reshuffle or stop (depending on RepeatType).
                    if(mPrevious.size() + 1 == size) {
                        if(mRepeating == REPEAT_NONE) {
                            mNextIndex = -1;
                            return;
                        } else {
                            mPrevious.clear();
                            mRandom = new Random(System.currentTimeMillis());
                        }
                    }
                    if(mRandom == null) mRandom = new Random(System.currentTimeMillis());
                    // Find a new index not in mPrevious.
                    do
                    {
                        mNextIndex = mRandom.nextInt(size);
                    }
                    while(mNextIndex == mCurrentIndex || mPrevious.contains(mNextIndex));

                } else {
                    // normal playback
                    if(mCurrentIndex > 0)
                        mPrevIndex = mCurrentIndex - 1;
                    if(mCurrentIndex + 1 < size)
                        mNextIndex = mCurrentIndex + 1;
                    else {
                        if(mRepeating == REPEAT_NONE) {
                            mNextIndex = -1;
                        } else {
                            mNextIndex = 0;
                        }
                    }
                }
            }
        }
    }

    private boolean isValidIndex(int position) {
        return position >= 0 && position < mMediaList.size();
    }


    protected void publishState() {

        PlaybackStateCompat.Builder pscb = new PlaybackStateCompat.Builder();
        long actions = PLAYBACK_BASE_ACTIONS;
        if (isPlaying()) {
            actions |= PlaybackStateCompat.ACTION_PAUSE | PlaybackStateCompat.ACTION_STOP;
            pscb.setState(PlaybackStateCompat.STATE_PLAYING, getTime(), getRate());
        } else if (hasMedia()) {
            actions |= PlaybackStateCompat.ACTION_PLAY | PlaybackStateCompat.ACTION_STOP;
            pscb.setState(PlaybackStateCompat.STATE_PAUSED, getTime(), getRate());
        } else {
            actions |= PlaybackStateCompat.ACTION_PLAY;
            pscb.setState(PlaybackStateCompat.STATE_STOPPED, getTime(), getRate());
        }
        if (hasNext())
            actions |= PlaybackStateCompat.ACTION_SKIP_TO_NEXT;
        if (hasPrevious() || isSeekable())
            actions |= PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS;
        if (isSeekable())
            actions |= PlaybackStateCompat.ACTION_FAST_FORWARD | PlaybackStateCompat.ACTION_REWIND;
        actions |= PlaybackStateCompat.ACTION_SKIP_TO_QUEUE_ITEM;
        pscb.setActions(actions);
        int repeatResId = getRepeatType() == REPEAT_ALL ? R.drawable.ic_auto_repeat_pressed : getRepeatType() == REPEAT_ONE ? R.drawable.ic_auto_repeat_one_pressed : R.drawable.ic_auto_repeat_normal;
        if (mMediaList.size() > 2)
            pscb.addCustomAction("shuffle", getString(R.string.shuffle_title), isShuffling() ? R.drawable.ic_auto_shuffle_pressed : R.drawable.ic_auto_shuffle_normal);
        pscb.addCustomAction("repeat", getString(R.string.repeat_title), repeatResId);
    }

    private void notifyTrackChanged() {
        mHandler.sendEmptyMessage(SHOW_PROGRESS);
        broadcastMetadata();
    }

    private void onMediaChanged() {
        notifyTrackChanged();
        saveCurrentMedia();
        determinePrevAndNextIndices();
    }

    private void onMediaListChanged() {
        saveMediaList();
        determinePrevAndNextIndices();
        executeUpdate();
    }

    @MainThread
    public synchronized void next() {
        int size = mMediaList.size();

        mPrevious.push(mCurrentIndex);
        mCurrentIndex = mNextIndex;
        if (size == 0 || mCurrentIndex < 0 || mCurrentIndex >= size) {
            if (mCurrentIndex < 0)
                saveCurrentMedia();
            Log.w(TAG, "Warning: invalid next index, aborted !");
            //Close video player if started
            LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(VideoPlayerActivity.EXIT_PLAYER));
            stop();
            return;
        }
        mVideoBackground = !isVideoPlaying() && canSwitchToVideo();
        playIndex(mCurrentIndex, 0);
        saveCurrentMedia();
        mHandler.sendEmptyMessage(SHOW_PROGRESS);
    }

    @MainThread
    public synchronized void previous(boolean force) {
        if (hasPrevious() && mCurrentIndex > 0 &&
                (force || !mMediaPlayer.isSeekable() || mMediaPlayer.getTime() < 2000l)) {
            int size = mMediaList.size();
            mCurrentIndex = mPrevIndex;
            if (mPrevious.size() > 0)
                mPrevious.pop();
            if (size == 0 || mPrevIndex < 0 || mCurrentIndex >= size) {
                Log.w(TAG, "Warning: invalid previous index, aborted !");
                stop();
                return;
            }
            playIndex(mCurrentIndex, 0);
            saveCurrentMedia();
        } else {
            setPosition(0f);
        }

        mHandler.sendEmptyMessage(SHOW_PROGRESS);
    }

    @MainThread
    public void shuffle() {
        if (mShuffling)
            mPrevious.clear();
        mShuffling = !mShuffling;
        savePosition();
        determinePrevAndNextIndices();
        publishState();
    }

    @MainThread
    public void setRepeatType(int repeatType) {
        mRepeating = repeatType;
        savePosition();
        determinePrevAndNextIndices();
        publishState();
    }


    private void broadcastMetadata() {
        MediaWrapper media = getCurrentMedia();
        if (media == null || media.getType() != MediaWrapper.TYPE_AUDIO)
            return;

        boolean playing = mMediaPlayer.isPlaying();

        Intent broadcast = new Intent("com.android.music.metachanged");
        broadcast.putExtra("track", media.getTitle());
        broadcast.putExtra("artist", media.getArtist());
        broadcast.putExtra("album", media.getAlbum());
        broadcast.putExtra("duration", media.getLength());
        broadcast.putExtra("playing", playing);
        broadcast.putExtra("package", "org.videolan.vlc");

        sendBroadcast(broadcast);
    }


    private synchronized void saveCurrentMedia() {
        boolean audio = true;
        for (int i = 0; i < mMediaList.size(); i++) {
            if (mMediaList.getMedia(i).getType() == MediaWrapper.TYPE_VIDEO)
                audio = false;
        }
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putString(audio ? "current_song" : "current_media", mMediaList.getMRL(Math.max(mCurrentIndex, 0)));
        editor.apply();
    }

    private synchronized void saveMediaList() {
        if (getCurrentMedia() == null)
            return;
        StringBuilder locations = new StringBuilder();
        boolean audio = true;
        for (int i = 0; i < mMediaList.size(); i++) {
            if (mMediaList.getMedia(i).getType() == MediaWrapper.TYPE_VIDEO)
                audio = false;
            locations.append(" ").append(Uri.encode(mMediaList.getMRL(i)));
        }
        //We save a concatenated String because putStringSet is APIv11.
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putString(audio ? "audio_list" : "media_list", locations.toString().trim());
        editor.apply();
    }


    private boolean validateLocation(String location)
    {
        /* Check if the MRL contains a scheme */
        if (!location.matches("\\w+://.+"))
            location = "file://".concat(location);
        if (location.toLowerCase(Locale.ENGLISH).startsWith("file://")) {
            /* Ensure the file exists */
            File f;
            try {
                f = new File(new URI(location));
            } catch (URISyntaxException e) {
                return false;
            } catch (IllegalArgumentException e) {
                return false;
            }
            if (!f.isFile())
                return false;
        }
        return true;
    }

    private void showToast(String text, int duration) {
        Message msg = new Message();
        Bundle bundle = new Bundle();
        bundle.putString("text", text);
        bundle.putInt("duration", duration);
        msg.setData(bundle);
        msg.what = SHOW_TOAST;
        mHandler.sendMessage(msg);
    }

    @MainThread
    public boolean isPlaying() {
        return mMediaPlayer.isPlaying();
    }

    @MainThread
    public boolean isSeekable() {
        return mSeekable;
    }

    @MainThread
    public boolean isPausable() {
        return mPausable;
    }

    @MainThread
    public boolean isShuffling() {
        return mShuffling;
    }

    @MainThread
    public boolean canShuffle()  {
        return getMediaListSize() > 2;
    }

    @MainThread
    public int getRepeatType() {
        return mRepeating;
    }

    @MainThread
    public boolean hasMedia()  {
        return hasCurrentMedia();
    }

    @MainThread
    public boolean hasPlaylist()  {
        return getMediaListSize() > 1;
    }

    @MainThread
    public boolean isVideoPlaying() {
        return mMediaPlayer.getVLCVout().areViewsAttached();
    }


    @MainThread
    public String getTitle() {
        if (hasCurrentMedia())
            return getCurrentMedia().getNowPlaying() != null ? getCurrentMedia().getNowPlaying() : getCurrentMedia().getTitle();
        else
            return null;
    }

    @MainThread
    public String getTitlePrev() {
        if (isValidIndex(mPrevIndex))
            return mMediaList.getMedia(mPrevIndex).getTitle();
        else
            return null;
    }

    @MainThread
    public String getTitleNext() {
        if (isValidIndex(mNextIndex))
            return mMediaList.getMedia(mNextIndex).getTitle();
        else
            return null;
    }

    @MainThread
    public synchronized void addCallback(Callback cb) {
        synchronized (mCallbacks) {
            if (!mCallbacks.contains(cb)) {
                mCallbacks.add(cb);
                if (hasCurrentMedia())
                    mHandler.sendEmptyMessage(SHOW_PROGRESS);
            }
        }
    }

    @MainThread
    public synchronized void removeCallback(Callback cb) {
        synchronized (mCallbacks) {
            mCallbacks.remove(cb);
        }
    }

    @MainThread
    public long getTime() {
        return mMediaPlayer.getTime();
    }

    @MainThread
    public long getLength() {
        return  mMediaPlayer.getLength();
    }

    /**
     * Loads a selection of files (a non-user-supplied collection of media)
     * into the primary or "currently playing" playlist.
     *
     * @param mediaPathList A list of locations to load
     * @param position The position to start playing at
     */
    @MainThread
    public void loadLocations(List<String> mediaPathList, int position) {
        ArrayList<MediaWrapper> mediaList = new ArrayList<>();

        for (int i = 0; i < mediaPathList.size(); i++) {
            String location = mediaPathList.get(i);
            MediaWrapper mediaWrapper = mMedialibrary.getMedia(location);
            if (mediaWrapper == null) {
                if (!validateLocation(location)) {
                    Log.w(TAG, "Invalid location " + location);
                    showToast("播放失败", Toast.LENGTH_SHORT);
                    continue;
                }
                Log.v(TAG, "Creating on-the-fly Media object for " + location);
                mediaWrapper = new MediaWrapper(Uri.parse(location));
            }
            mediaList.add(mediaWrapper);
        }
        load(mediaList, position);
    }

    @MainThread
    public void loadUri(Uri uri) {
        String path = uri.toString();
        if (TextUtils.equals(uri.getScheme(), "content")) {
            path = "file://"+ FileUtils.getPathFromURI(uri,this);
        }
        loadLocation(path);
    }

    @MainThread
    public void loadLocation(String mediaPath) {
        loadLocations(Collections.singletonList(mediaPath), 0);
    }

    @MainThread
    public void load(MediaWrapper[] mediaList, int position) {
        load(Arrays.asList(mediaList), position);
    }

    @MainThread
    public void load(List<MediaWrapper> mediaList, int position) {
        Log.v(TAG, "Loading position " + ((Integer) position).toString() + " in " + mediaList.toString());

        mMediaList.removeEventListener(mListEventListener);
        mMediaList.clear();
        MediaWrapperList currentMediaList = mMediaList;

        mPrevious.clear();

        for (int i = 0; i < mediaList.size(); i++) {
            currentMediaList.add(mediaList.get(i));
        }

        if (mMediaList.size() == 0) {
            Log.w(TAG, "Warning: empty media list, nothing to play !");
            return;
        }
        if (isValidIndex(position)) {
            mCurrentIndex = position;
        } else {
            Log.w(TAG, "Warning: positon " + position + " out of bounds");
            mCurrentIndex = 0;
        }

        // Add handler after loading the list
        mMediaList.addEventListener(mListEventListener);

        playIndex(mCurrentIndex, 0);
        saveMediaList();
        onMediaChanged();
    }

    @MainThread
    public void load(MediaWrapper media) {
        ArrayList<MediaWrapper> arrayList = new ArrayList<>();
        arrayList.add(media);
        load(arrayList, 0);
    }

    /**
     * Play a media from the media list (playlist)
     *
     * @param index The index of the media
     * @param flags LibVLC.MEDIA_* flags
     */
    public void playIndex(int index, int flags) {
        if (mMediaList.size() == 0) {
            Log.w(TAG, "Warning: empty media list, nothing to play !");
            return;
        }
        if (isValidIndex(index)) {
            mCurrentIndex = index;
        } else {
            Log.w(TAG, "Warning: index " + index + " out of bounds");
            mCurrentIndex = 0;
        }

        String mrl = mMediaList.getMRL(index);
        Log.d("liutao","playindex mrl:"+mrl);
        if (mrl == null)
            return;
        final MediaWrapper mw = mMediaList.getMedia(index);
        if (mw == null)
            return;

        boolean isVideoPlaying = mw.getType() == MediaWrapper.TYPE_VIDEO && isVideoPlaying();
        Log.d("liutao","playindex isVideoPlaying:"+isVideoPlaying);
        if (!mVideoBackground && isVideoPlaying)
            mw.addFlags(MediaWrapper.MEDIA_VIDEO);

        if (mVideoBackground)
            mw.addFlags(MediaWrapper.MEDIA_FORCE_AUDIO);

        /* Pausable and seekable are true by default */
        mParsed = false;
        mSwitchingToVideo = false;
        mPausable = mSeekable = true;
        final Media media = new Media(VLCInstance.get(this), mw.getUri());
        VLCOptions.setMediaOptions(media, this, flags | mw.getFlags());

        /* keeping only video during benchmark */
        if (mIsBenchmark) {
            media.addOption(":no-audio");
            media.addOption(":no-spu");
            if (mIsHardware) {
                media.addOption(":codec=mediacodec_ndk,mediacodec_jni,none");
                mIsHardware = false;
            }
        }


        media.setEventListener(mMediaListener);
        mMediaPlayer.setMedia(media);
        media.release();

        if (mw.getType() != MediaWrapper.TYPE_VIDEO || isVideoPlaying || mw.hasFlag(MediaWrapper.MEDIA_FORCE_AUDIO)) {
            mMediaPlayer.setEqualizer(VLCOptions.getEqualizer(this));
            mMediaPlayer.setVideoTitleDisplay(MediaPlayer.Position.Disable, 0);
            changeAudioFocus(true);
            mMediaPlayer.setEventListener(mMediaPlayerListener);
            if (!isVideoPlaying && mMediaPlayer.getRate() == 1.0F && mSettings.getBoolean(Preferences.KEY_AUDIO_PLAYBACK_SPEED_PERSIST, true))
                setRate(mSettings.getFloat(Preferences.KEY_AUDIO_PLAYBACK_RATE, 1.0F), true);
            mMediaPlayer.play();


        } else {//Start VideoPlayer for first video, it will trigger playIndex when ready.
            VideoPlayerActivity.startOpened(this, getCurrentMediaWrapper().getUri(), mCurrentIndex);
        }
    }

    /**
     * Use this function to play a media inside whatever MediaList LibVLC is following.
     *
     * Unlike load(), it does not import anything into the primary list.
     */
    @MainThread
    public void playIndex(int index) {
        playIndex(index, 0);
    }

    @MainThread
    public void flush() {
        /* HACK: flush when activating a video track. This will force an
         * I-Frame to be displayed right away. */
        if (isSeekable()) {
            long time = getTime();
            if (time > 0 )
                setTime(time);
        }
    }

    /**
     * Use this function to show an URI in the audio interface WITHOUT
     * interrupting the stream.
     *
     * Mainly used by VideoPlayerActivity in response to loss of video track.
     */
    @MainThread
    public void showWithoutParse(int index) {
        setVideoTrackEnabled(false);
        MediaWrapper media = mMediaList.getMedia(index);

        if(media == null || !mMediaPlayer.isPlaying())
            return;
        // Show an URI without interrupting/losing the current stream
        Log.v(TAG, "Showing index " + index + " with playing URI " + media.getUri());
        mCurrentIndex = index;

        notifyTrackChanged();
    }

    @MainThread
    public void switchToPopup(int index) {
        showWithoutParse(index);
        showPopup();
    }

    @MainThread
    public void removePopup() {
        if (mPopupManager != null) {
            mPopupManager.removePopup();
        }
        mPopupManager = null;
    }

    @MainThread
    public boolean isPlayingPopup() {
        return mPopupManager != null;
    }

    @MainThread
    public void showPopup() {
        if (mPopupManager == null)
            mPopupManager = new PopupManager(this);
        mPopupManager.showPopup(this);
    }

    public void setVideoTrackEnabled(boolean enabled) {
        if (!hasMedia() || !isPlaying())
            return;
        if (enabled)
            getCurrentMedia().addFlags(MediaWrapper.MEDIA_VIDEO);
        else
            getCurrentMedia().removeFlags(MediaWrapper.MEDIA_VIDEO);
        mMediaPlayer.setVideoTrackEnabled(enabled);
    }

    /**
     * Append to the current existing playlist
     */

    @MainThread
    public void append(MediaWrapper[] mediaList) {
        append(Arrays.asList(mediaList));
    }

    @MainThread
    public void append(List<MediaWrapper> mediaList) {
        if (!hasCurrentMedia())
        {
            load(mediaList, 0);
            return;
        }

        for (int i = 0; i < mediaList.size(); i++) {
            MediaWrapper mediaWrapper = mediaList.get(i);
            mMediaList.add(mediaWrapper);
        }
        onMediaListChanged();
    }

    @MainThread
    public void append(MediaWrapper media) {
        ArrayList<MediaWrapper> arrayList = new ArrayList<>();
        arrayList.add(media);
        append(arrayList);
    }

    /**
     * Insert into the current existing playlist
     */

    @MainThread
    public void insertNext(MediaWrapper[] mediaList) {
        insertNext(Arrays.asList(mediaList));
    }

    @MainThread
    public void insertNext(List<MediaWrapper> mediaList) {
        if (!hasCurrentMedia()) {
            load(mediaList, 0);
            return;
        }

        int startIndex = mCurrentIndex + 1;

        for (int i = 0; i < mediaList.size(); i++) {
            MediaWrapper mediaWrapper = mediaList.get(i);
            mMediaList.insert(startIndex + i, mediaWrapper);
        }
        onMediaListChanged();
    }

    @MainThread
    public void insertNext(MediaWrapper media) {
        ArrayList<MediaWrapper> arrayList = new ArrayList<>();
        arrayList.add(media);
        insertNext(arrayList);
    }

    /**
     * Move an item inside the playlist.
     */
    @MainThread
    public void moveItem(int positionStart, int positionEnd) {
        mMediaList.move(positionStart, positionEnd);
        PlaybackService.this.saveMediaList();
    }

    @MainThread
    public void insertItem(int position, MediaWrapper mw) {
        mMediaList.insert(position, mw);
        saveMediaList();
        determinePrevAndNextIndices();
    }


    @MainThread
    public void remove(int position) {
        mMediaList.remove(position);
        saveMediaList();
        determinePrevAndNextIndices();
    }

    @MainThread
    public void removeLocation(String location) {
        mMediaList.remove(location);
        saveMediaList();
        determinePrevAndNextIndices();
    }

    public int getMediaListSize() {
        return mMediaList.size();
    }

    @MainThread
    public ArrayList<MediaWrapper> getMedias() {
        final ArrayList<MediaWrapper> ml = new ArrayList<>();
        for (int i = 0; i < mMediaList.size(); i++) {
            ml.add(mMediaList.getMedia(i));
        }
        return ml;
    }

    @MainThread
    public List<String> getMediaLocations() {
        ArrayList<String> medias = new ArrayList<>();
        for (int i = 0; i < mMediaList.size(); i++) {
            medias.add(mMediaList.getMRL(i));
        }
        return medias;
    }

    @MainThread
    public String getCurrentMediaLocation() {
        return mMediaList.getMRL(mCurrentIndex);
    }

    @MainThread
    public int getCurrentMediaPosition() {
        return mCurrentIndex;
    }

    @MainThread
    public MediaWrapper getCurrentMediaWrapper() {
        return PlaybackService.this.getCurrentMedia();
    }

    @MainThread
    public void setTime(long time) {
        if (mSeekable)
            mMediaPlayer.setTime(time);
    }

    @MainThread
    public boolean hasNext() {
        return mNextIndex != -1;
    }

    @MainThread
    public boolean hasPrevious() {
        return mPrevIndex != -1;
    }

    @MainThread
    public void detectHeadset(boolean enable)  {
        mDetectHeadset = enable;
    }

    @MainThread
    public float getRate()  {
        return mMediaPlayer.getRate();
    }

    @MainThread
    public void setRate(float rate, boolean save) {
        mMediaPlayer.setRate(rate);
    }

    @MainThread
    public void navigate(int where) {
        mMediaPlayer.navigate(where);
    }

    @MainThread
    public MediaPlayer.Chapter[] getChapters(int title) {
        return mMediaPlayer.getChapters(title);
    }

    @MainThread
    public MediaPlayer.Title[] getTitles() {
        return mMediaPlayer.getTitles();
    }

    @MainThread
    public int getChapterIdx() {
        return mMediaPlayer.getChapter();
    }

    @MainThread
    public void setChapterIdx(int chapter) {
        mMediaPlayer.setChapter(chapter);
    }

    @MainThread
    public int getTitleIdx() {
        return mMediaPlayer.getTitle();
    }

    @MainThread
    public void setTitleIdx(int title) {
        mMediaPlayer.setTitle(title);
    }

    @MainThread
    public int getVolume() {
        return mMediaPlayer.getVolume();
    }

    @MainThread
    public int setVolume(int volume) {
        return mMediaPlayer.setVolume(volume);
    }

    @MainThread
    public void seek(long position) {
        seek(position, getLength());
    }

    @MainThread
    public void seek(long position, double length) {
        if (length > 0.0D)
            setPosition((float) (position/length));
        else
            setTime(position);
    }

    @MainThread
    public boolean updateViewpoint(float yaw, float pitch, float roll, float fov, boolean absolute) {
        return mMediaPlayer.updateViewpoint(yaw, pitch, roll, fov, absolute);
    }

    @MainThread
    public void saveTimeToSeek(long time) {
        mSavedTime = time;
    }

    @MainThread
    public void setPosition(float pos) {
        if (mSeekable)
            mMediaPlayer.setPosition(pos);
    }

    @MainThread
    public int getAudioTracksCount() {
        return mMediaPlayer.getAudioTracksCount();
    }

    @MainThread
    public MediaPlayer.TrackDescription[] getAudioTracks() {
        return mMediaPlayer.getAudioTracks();
    }

    @MainThread
    public int getAudioTrack() {
        return mMediaPlayer.getAudioTrack();
    }

    @MainThread
    public boolean setAudioTrack(int index) {
        return mMediaPlayer.setAudioTrack(index);
    }

    @MainThread
    public int getVideoTracksCount() {
        return mMediaPlayer.getVideoTracksCount();
    }

    @MainThread
    public MediaPlayer.TrackDescription[] getVideoTracks() {
        return mMediaPlayer.getVideoTracks();
    }

    @MainThread
    public Media.VideoTrack getCurrentVideoTrack() {
        return mMediaPlayer.getCurrentVideoTrack();
    }

    @MainThread
    public int getVideoTrack() {
        return mMediaPlayer.getVideoTrack();
    }

    @MainThread
    public boolean addSubtitleTrack(String path, boolean select) {
        return mMediaPlayer.addSlave(Media.Slave.Type.Subtitle, path, select);
    }

    @MainThread
    public boolean addSubtitleTrack(Uri uri,boolean select) {
        return mMediaPlayer.addSlave(Media.Slave.Type.Subtitle, uri, select);
    }

    @MainThread
    public boolean addSubtitleTrack(String path) {
        return addSubtitleTrack(path, false);
    }

    @MainThread
    public boolean addSubtitleTrack(Uri uri) {
        return addSubtitleTrack(uri, false);
    }

    @MainThread
    public MediaPlayer.TrackDescription[] getSpuTracks() {
        return mMediaPlayer.getSpuTracks();
    }

    @MainThread
    public int getSpuTrack() {
        return mMediaPlayer.getSpuTrack();
    }

    @MainThread
    public boolean setSpuTrack(int index) {
        return mMediaPlayer.setSpuTrack(index);
    }

    @MainThread
    public int getSpuTracksCount() {
        return mMediaPlayer.getSpuTracksCount();
    }

    @MainThread
    public boolean setAudioDelay(long delay) {
        return mMediaPlayer.setAudioDelay(delay);
    }

    @MainThread
    public long getAudioDelay() {
        return mMediaPlayer.getAudioDelay();
    }

    @MainThread
    public boolean setSpuDelay(long delay) {
        return mMediaPlayer.setSpuDelay(delay);
    }

    @MainThread
    public long getSpuDelay() {
        return mMediaPlayer.getSpuDelay();
    }

    @MainThread
    public void setEqualizer(MediaPlayer.Equalizer equalizer) {
        mMediaPlayer.setEqualizer(equalizer);
    }

    @MainThread
    public void setVideoScale(float scale) {
        mMediaPlayer.setScale(scale);
    }

    @MainThread
    public void setVideoAspectRatio(String aspect) {
        mMediaPlayer.setAspectRatio(aspect);
    }

    /**
     * Expand the current media.
     * @return the index of the media was expanded, and -1 if no media was expanded
     */
    @MainThread
    public int expand(boolean updateHistory) {
        final Media media = mMediaPlayer.getMedia();
        String mrl = updateHistory ? getCurrentMediaLocation() : null;
        if (media == null)
            return -1;
        final MediaList ml = media.subItems();
        media.release();
        int ret;

        if (ml.getCount() > 0) {
            mMediaList.remove(mCurrentIndex);
            for (int i = ml.getCount() - 1; i >= 0; --i) {
                final Media child = ml.getMediaAt(i);
                child.parse();
                mMediaList.insert(mCurrentIndex, new MediaWrapper(child));
                child.release();
            }
            if (updateHistory && ml.getCount() == 1)
                mMedialibrary.addToHistory(mrl, mMediaList.getMedia(mCurrentIndex).getTitle());
            ret = 0;
        } else {
            ret = -1;
        }
        ml.release();
        return ret;
    }

    public void restartMediaPlayer() {
        stop();
        mMediaPlayer.release();
        mMediaPlayer = newMediaPlayer();
        /* TODO RESUME */
    }




    public static class Client {
        public static final String TAG = "PlaybackService.Client";

        @MainThread
        public interface Callback {
            void onConnected(PlaybackService service);
            void onDisconnected();
        }

        private boolean mBound = false;
        private final Callback mCallback;
        private final Context mContext;

        private final ServiceConnection mServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder iBinder) {
                if (!mBound)
                    return;

                final PlaybackService service = PlaybackService.getService(iBinder);
                if (service != null)
                    mCallback.onConnected(service);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mBound = false;
                mCallback.onDisconnected();
            }
        };

        private static Intent getServiceIntent(Context context) {
            return new Intent(context, PlaybackService.class);
        }

        private static void startService(Context context) {
            context.startService(getServiceIntent(context));
        }

        private static void stopService(Context context) {
            context.stopService(getServiceIntent(context));
        }

        public Client(Context context, Callback callback) {
            if (context == null || callback == null)
                throw new IllegalArgumentException("Context and callback can't be null");
            mContext = context;
            mCallback = callback;
        }

        @MainThread
        public void connect() {
            if (mBound)
                throw new IllegalStateException("already connected");
            startService(mContext);
            System.out.println("start playback service!!!");
            mBound = mContext.bindService(getServiceIntent(mContext), mServiceConnection, BIND_AUTO_CREATE);
        }

        @MainThread
        public void disconnect() {
            if (mBound) {
                mBound = false;
                mContext.unbindService(mServiceConnection);
            }
        }

        public static void restartService(Context context) {
            stopService(context);
            startService(context);
        }
    }


}
