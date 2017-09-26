package com.dmsys.airdiskpro;


import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.multidex.MultiDex;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.alibaba.sdk.android.feedback.impl.FeedbackAPI;
import com.dm.baselib.BaseValue;
import com.dmairdisk.aodplayer.api.AodPlayer;
import com.dmairdisk.aodplayer.api.AodPlayer.DecodeLibMode;
import com.dmsys.airdiskpro.filemanager.FileOperationHelper;
import com.dmsys.airdiskpro.view.VaultPasswordDialog;
import com.dmsys.dmsdk.DMSdk;
import com.dmsys.dmsdk.DMSdk.DMSessionConnectListener;
import com.dmsys.libjpeg.LibJpeg;
import com.dmsys.txtviewer.util.PageFactory;
import com.dmsys.txtviewer.view.Config;

import org.litepal.LitePalApplication;

import java.io.File;
import java.io.IOException;

import cn.dm.longsys.library.imageloader.cache.disc.DiskCache;
import cn.dm.longsys.library.imageloader.cache.disc.impl.UnlimitedDiskCache;
import cn.dm.longsys.library.imageloader.cache.disc.impl.ext.LruDiskCache;
import cn.dm.longsys.library.imageloader.cache.disc.naming.Md5FileNameGenerator;
import cn.dm.longsys.library.imageloader.cache.memory.impl.LruMemoryCache;
import cn.dm.longsys.library.imageloader.core.DMImageLoader;
import cn.dm.longsys.library.imageloader.core.ImageLoaderConfiguration;
import cn.dm.longsys.library.imageloader.core.assist.QueueProcessingType;
import cn.dm.longsys.library.imageloader.core.download.OkhttpImageDownloader;
import cn.dm.longsys.library.imageloader.utils.StorageUtils;

public class BrothersApplication extends Application {


    private final static String ALIBAICHUAN_FEEDBACK_APPKEY = "23544916";

    private Handler mHandler;

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        new Runnable() {
            @Override
            public void run() {
                //Your code
                init();
            }
        }.run();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    // 辅助其他工具类获取到Application Context
    private static BrothersApplication mInstance = null;

    public static BrothersApplication getInstance() {
        return mInstance;
    }

    private void init() {
        mHandler = new Handler();
        mInstance = this;

        try {
            FeedbackAPI.mContext = this;
            FeedbackAPI.init(this, ALIBAICHUAN_FEEDBACK_APPKEY);
        } catch (Exception e) {
        }

        AodPlayer.getInstance().init(mInstance);
        AodPlayer.getInstance().setDecodeLibMode(DecodeLibMode.MEDIAPLAYER);
        FileOperationHelper.getInstance().initDownloadFolder();

        DMSdk.getInstance().attachListtener(new DMSessionConnectListener() {

            @Override
            public void onDisConnect(final int type) {
                // TODO Auto-grated method stub
                mHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        Intent mIntent = new Intent(mInstance,
                                VaultPasswordDialog.class);
                        mIntent.putExtra(VaultPasswordDialog.TypeFlag,
                                type);
                        mIntent.putExtra(VaultPasswordDialog.DeviceNameFlag,
                                BaseValue.DeviceName);
                        mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        mInstance.startActivity(mIntent);
                    }
                });
            }

        });

        // 阅读器初始化

        LitePalApplication.initialize(this);
        Config.createConfig(this);
        PageFactory.createPageFactory(this);

        initImageLoader(this);
    }

    private void initImageLoader(Context context) {
        File cache = StorageUtils.getCacheDirectory(this);
        DiskCache diskCache = null;
        try {
            diskCache = new LruDiskCache(cache, new Md5FileNameGenerator(),
                    300 * 1024 * 1024);
        } catch (IOException e) {
            e.printStackTrace();
            diskCache = new UnlimitedDiskCache(cache);
        }

        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics mDisplayMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(mDisplayMetrics);

        ImageLoaderConfiguration configuration = new ImageLoaderConfiguration.Builder(
                this)
                .memoryCacheExtraOptions(mDisplayMetrics.widthPixels,
                        mDisplayMetrics.heightPixels)
                .diskCacheExtraOptions(mDisplayMetrics.widthPixels,
                        mDisplayMetrics.heightPixels, null)
                .threadPoolSize(20)
                // default
                .threadPriority(Thread.NORM_PRIORITY - 2)
                // default
                .tasksProcessingOrder(QueueProcessingType.FIFO)
                // default
                .denyCacheImageMultipleSizesInMemory()
                .memoryCache(new LruMemoryCache(256* 1024 * 1024))
                .memoryCacheSize(256* 1024 * 1024)
                .memoryCacheSizePercentage(13)
                // default
                .diskCacheSize(300* 1024 * 1024 )
                .imageDownloader(new OkhttpImageDownloader(this, 3000, 0))
                .diskCache(diskCache).build();
        DMImageLoader.getInstance().init(configuration);

        LibJpeg.getInstance().initCachePath(
                FileOperationHelper.getInstance().getCachePath());
    }

}
