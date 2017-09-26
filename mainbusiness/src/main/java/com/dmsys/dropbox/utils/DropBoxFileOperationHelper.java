package com.dmsys.dropbox.utils;

import android.app.Activity;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import com.dm.baselib.BaseValue;
import com.dmairdisk.aodplayer.api.AodPlayer;
import com.dmsys.airdiskpro.BrothersApplication;
import com.dmsys.airdiskpro.filemanager.FileOperationService.IProgressListener;
import com.dmsys.airdiskpro.filemanager.FileOperationService.OperationResult;
import com.dmsys.airdiskpro.ui.imagereader.Constants.Extra;
import com.dmsys.airdiskpro.ui.imagereader.ImagePagerActivity;
import com.dmsys.airdiskpro.utils.AndroidConfig;
import com.dmsys.airdiskpro.utils.FileHelper;
import com.dmsys.airdiskpro.utils.FileInfoUtils;
import com.dmsys.airdiskpro.utils.FileUtil;
import com.dmsys.airdiskpro.utils.SDCardUtil;
import com.dmsys.airdiskpro.utils.TimeTool;
import com.dmsys.dmsdk.DMSdk;
import com.dmsys.dmsdk.model.DMDir;
import com.dmsys.dmsdk.model.DMFile;
import com.dmsys.dmsdk.model.DMFileCategoryType;
import com.dmsys.dmsdk.model.DMFilePage;
import com.dmsys.dmsdk.model.DMStorageInfo;
import com.dmsys.dmsdk.util.DMFileTypeUtil;
import com.dmsys.dropbox.api.DMDropboxAPI;
import com.dmsys.txtviewer.ReadActivity;
import com.dmsys.txtviewer.db.BookList;
import com.dmsys.vlcplayer.VideoPlayerActivity;
import com.dmsys.vlcplayer.util.UrilTools;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.exception.DropboxException;
import com.tencent.weibo.sdk.android.component.sso.tools.MD5Tools;
import com.dmsys.mainbusiness.R;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

;

public class DropBoxFileOperationHelper {

    private static final String TAG = DropBoxFileOperationHelper.class.getSimpleName();

    public static final int OP_SKIP = 1;
    public static final int OP_OVERWRITE = 2;
    public static final int OP_RENAME = 3;

    public static final int ERROR_SUCESS = 0;
    public static final int ERROR_UDISK_NOT_ENOUGH_SPACE = 1;
    public static final int ERROR_PHONE_NOT_ENOUGH_SPACE = 2;
    public static final int ERROR_NOT_CONNECTED = 3;
    public static final int ERROR_NOT_FOUND_STORAGE = 4;
    public static final int ERROR_RENAME_NAME_EXIST = 5;
    public static final int ERROR_CONTAIN_SPECIALSYMBOLS = 6;

    public static final int RET_AFTER_SKIP = 10;
    public static final int RET_AFTER_SUCCESS = 11;
    public static final int RET_AFTER_FAIL = 12;

    public static final String MAP_KEY_RET = "RET";
    public static final String MAP_KEY_SKIP = "SKIP";

    public static boolean isUserStop = false;

    private static DropBoxFileOperationHelper mHelper;

    private static IProgressListener mListener;

    public static List<ArrayList<DMFile>> mGroupDatas;
    public static int imgPostionInAll = -1;

    public class DownloadInfo {

        public long curTime = 0;
        public long downloadBytes = 0;

        public DownloadInfo(long c, long d) {
            curTime = c;
            downloadBytes = d;
        }
    }

    private DropBoxFileOperationHelper() {

    }

    public synchronized static DropBoxFileOperationHelper getInstance() {
        if (mHelper == null) {
            mHelper = new DropBoxFileOperationHelper();
        }

        return mHelper;
    }

    public void setProgressListener(IProgressListener listener) {
        mListener = listener;
    }

    public boolean openFile(DMFile file, Activity context) {

        switch (file.getType()) {
            case E_VIDEO_CATEGORY:
                openVideo(file, context);
                //openVideoByQQ(context,file);
                break;
            case E_BOOK_CATEGORY:
                if (file.mLocation == DMFile.LOCATION_DROPBOX) {
                    return false;
                } else {
                    openDoc(file, context);
                }
                break;
            default:

                if (FileUtil.getFileSuffix(file.mName) == null) {
                    //Toast.makeText(context, R.string.DM_File_Not_Support, Toast.LENGTH_SHORT).show();
                    return true;
                }

                if (file.mLocation != DMFile.LOCATION_DROPBOX) {
                    FileUtil.thirdPartOpen(file, context);
                } else {
                    return false;
                }
        }

        return true;
    }

    public void openVideo(DMFile file, Context context) {
        // 如果正在播放音乐，暂停播放
        if (AodPlayer.getInstance().getIsPlaying()) {
            AodPlayer.getInstance().pause();
        }

        String filePath = getFullPath(file);
        String castUrl = DropBoxVodUrlConversionHelper.getInstance().getConvertedUrl(context, filePath, file.getName());
        DropBoxVodUrlConversionHelper.getInstance().startHttpServer();




//		VodPlayer.getInstance().startVodPlayView(PlaySource.NETWORK, castUrl);

        List<String> fileList = new ArrayList<>();

        castUrl =  UrilTools.encodeUri(castUrl);
        fileList.add(castUrl);

        VideoPlayerActivity.start(context, fileList, false, 0);

    }

    public void openUnsupportVideo(DMFile file, Context context) {
        FileUtil.thirdPartOpen(file, context);
    }

    public void openVideoByQQ(Context mContext, DMFile file) {

        try {
            ComponentName toActivity = new ComponentName("com.tencent.mtt", "com.tencent.mtt.browser.video.H5VideoThrdcallActivity");

            String filePath = file.mPath;

            if (file.mLocation == DMFile.LOCATION_UDISK) {
                filePath = "http://" + BaseValue.Host + File.separator + filePath;
                filePath = FileInfoUtils.encodeUri(filePath);
            } else {
                filePath = "file://" + filePath;
            }

            Intent intent = new Intent();
            intent.setComponent(toActivity);
            intent.setAction("android.intent.action.VIEW");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("oneshot", 0);
            intent.putExtra("configchange", 0);
            Uri uri = Uri.parse(filePath);
            System.out.println("thirdparty:" + uri);
            String type = FileUtil.getMIMEType(file.getName());
            intent.setDataAndType(uri, type);
            mContext.startActivity(intent);
        } catch (Exception e) {
            Log.v("go to apk error", "------>" + e.toString());
        }

    }


    public void openPicture(Context mContext, ArrayList<DMFile> list, int index) {
        openPicture(mContext, list, index, ImagePagerActivity.IS_FROM_FileExplorerDirView);
    }

    public void openPicture(Context mContext, ArrayList<DMFile> list, int index, int isFrom) {
        List<ArrayList<DMFile>> l = new ArrayList<ArrayList<DMFile>>();
        l.add(list);
        openPicture(mContext, l, index, isFrom);
    }

    public void openPicture(Context mContext, List<ArrayList<DMFile>> list, int index, int isFrom) {
        mGroupDatas = list;
        imgPostionInAll = index;
        Intent intent = new Intent(mContext, ImagePagerActivity.class);
        intent.putExtra(Extra.IMAGE_FROM, isFrom);
        mContext.startActivity(intent);
    }

    public boolean openDoc(DMFile file, Activity context) {
        // 将不支持的格式过滤掉
        String fileType = FileHelper.getFileSuffix(file.getName());
        if (!FileHelper.allowTypeList.contains(fileType)) {
            // Toast.makeText(this, "暂不支持该格式", Toast.LENGTH_SHORT).show();
            FileUtil.thirdPartOpen(file, context);
            // openDefault(file);
            return false;
        }

//		Context ctx = BrothersApplication.getInstance();
//		Intent intent = new Intent(ctx, SimpleTextReaderActivity.class);
//		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//
//		String url = null;
//		if (file.mLocation == DMFile.LOCATION_DROPBOX) {
//			url = getFullPath(file);
//			URL mURL = null;
//			try {
//				mURL = new URL(url);
//				URI uri = new URI(mURL.getProtocol(), mURL.getUserInfo(),
//						mURL.getHost(), mURL.getPort(), mURL.getPath(),
//						mURL.getQuery(), mURL.getRef());
//				mURL = uri.toURL();
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			if(mURL == null) {
//				return false;
//			}
//			intent.putExtra("filePath", mURL.toString());
//			HashMap<String, String> headers = new HashMap<String, String>();
//			DMDropboxAPI.getInstance().getSession().sign(headers);
//			intent.putExtra("headers",headers);
//		} else {
//			intent.putExtra("filePath", file.getPath());
//		}
//		
//		ctx.startActivity(intent);

        BookList bookList = new BookList();

        bookList.setBookname(file.getName());
        bookList.setBookpath(file.getPath());

        bookList.setId(file.hashCode());

        ReadActivity.openBook(bookList, context);


        return true;
    }

    public void openDefault(Uri uri, DMFileCategoryType type) {
        Context ctx = BrothersApplication.getInstance();
        if (type == DMFileCategoryType.E_SOFTWARE_CATEGORY) {
            Intent intent_apk = new Intent(Intent.ACTION_VIEW, uri);
            intent_apk.setDataAndType(uri, "application/vnd.android.package-archive");
            intent_apk.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ctx.startActivity(intent_apk);
            return;
        }

        PackageManager packageManager = ctx.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        if (type == DMFileCategoryType.E_VIDEO_CATEGORY) {
            intent.setDataAndType(uri, "video/*");
        } else if (type == DMFileCategoryType.E_MUSIC_CATEGORY) {
            intent.setDataAndType(uri, "audio/*");
        } else if (type == DMFileCategoryType.E_BOOK_CATEGORY) {
            if (uri.toString().toLowerCase().endsWith("pdf")) {
                intent.setDataAndType(uri, "application/pdf");
            } else if (uri.toString().toLowerCase().endsWith("doc") || uri.toString().toLowerCase().endsWith("docx")) {
                intent.setDataAndType(uri, "application/msword");
            } else if (uri.toString().toLowerCase().endsWith("xls") || uri.toString().toLowerCase().endsWith("xlsx")) {
                intent.setDataAndType(uri, "application/vnd.ms-excel");
            } else if (uri.toString().toLowerCase().endsWith("ppt")) {
                intent.setDataAndType(uri, "application/vnd.ms-powerpoint");
            } else {
                intent.setDataAndType(uri, "text/*");
            }
        } else if (type == DMFileCategoryType.E_PICTURE_CATEGORY) {
            intent.setDataAndType(uri, "image/*");
        }


        List<ResolveInfo> activities = packageManager.queryIntentActivities(intent, 0);
        boolean isIntentSafe = activities.size() > 0;
        if (isIntentSafe) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ctx.startActivity(intent);
        } else {
            Context context = BrothersApplication.getInstance();
            Toast.makeText(context, "无相应软件，无法打开", Toast.LENGTH_SHORT).show();
        }
    }

    private String getFullPath(DMFile file) {
        String ret = null;
        try {
            ret = DMDropboxAPI.getInstance().getRequestUrlUnencode(file.getPath(), null);
        } catch (DropboxException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return ret;

    }


    private String getFileName(String dir, String fileName, FileExistsTest test) throws IOException {
        Log.d(TAG, "createFile " + dir + ", " + fileName);

        if (!test.test(dir, fileName)) {
            return fileName;
        }

        String suffix;
        String name;

        int index = fileName.lastIndexOf(".");
        if (index != -1) {
            name = fileName.substring(0, index);
            suffix = fileName.substring(index + 1);
        } else {
            name = fileName;
            suffix = "";
        }

        Log.d(TAG, "name: " + name + ", suffix: " + suffix);
        index = 0;
        String newName = null;
        do {
            index++;
            if (suffix.equals("")) {
                newName = name + "(" + index + ")";
            } else {
                newName = name + "(" + index + ")." + suffix;
            }

        } while (test.test(dir, newName));

        return newName;
    }


    public int deleteFile(boolean type, List<DMFile> list) {
        DeleteTask deleteTask = new DeleteTask(type, list);
        deleteTask.setProgressListener(mListener);
        int ret = deleteTask.run();
        return ret;
    }

    private class DeleteTask {
        private boolean mtype;
        private List<DMFile> mList;
        private IProgressListener mListener;

        public DeleteTask(boolean type, List<DMFile> list) {
            mtype = type;
            mList = list;
        }

        public void setProgressListener(IProgressListener listener) {
            mListener = listener;
        }

        public int run() {
            return deleteFile(mList);
        }

        int deleteFile(List<DMFile> list) {
            boolean bOK = true;
            List<DMFile> finishedList = new ArrayList<DMFile>();

            int count = 0;
            for (DMFile file : list) {

                try {
                    bOK = deleteFile(file);
                } catch (Exception e) {
                    e.printStackTrace();
                    if (mListener.onProgressChanged(null, -1, -1, -1, -1, null, null)) {
                        break;
                    }
                }
                if (bOK) {

                    if (file.mType == DMFileCategoryType.E_MUSIC_CATEGORY) {
                        if (AodPlayer.getInstance().getIsPlaying() && getFullPath(file).equals(AodPlayer.getInstance().getCurPlayPath())) {

                            List<String> playlist = AodPlayer.getInstance().getFileList();

                            if (playlist.size() > 1) {

                                int index = playlist.indexOf(getFullPath(file));
                                index = index % (playlist.size() - 1);

                                playlist.remove(getFullPath(file));

                                AodPlayer.getInstance().setPlayList(playlist);

                                AodPlayer.getInstance().startPlay(playlist.get(index));

                            } else {
                                AodPlayer.getInstance().stop();
                            }
                        }
                    } else if (file.mType == DMFileCategoryType.E_XLDIR_CATEGORY) {

                        if (AodPlayer.getInstance().getIsPlaying()) {

                            String full = getFullPath(file);
                            String cur = AodPlayer.getInstance().getCurPlayPath();
                            String parent = cur.substring(0, cur.lastIndexOf("/"));
                            if (full.equals(parent)) {
                                AodPlayer.getInstance().stop();
                            }
                        }
                    }

                    finishedList.add(file);
                    count++;
                }

                if (mListener != null) {
                    System.out.println("delete count:" + count);

                    boolean bStop = mListener.onProgressChanged(file.mPath, (double) count * 100 / list.size(),
                            list.size(), list.size() - count, -1, null, null);
                    if (count == list.size()) {
                        mListener.onFinished(ERROR_SUCESS, null);
                    }
                    if (bStop) {
                        break;
                    }
                }

            }

            if (finishedList.size() == list.size()) {
                return 0;
            } else {
                return list.size() - finishedList.size();
            }

        }

        private boolean deleteFile(DMFile file) throws Exception {
            Log.d(TAG, "deleteFile " + file.getPath());

            DMDropboxAPI.getInstance().delete(file.mPath);

            return true;
        }

    }

    ;


    // 删除文件或文件夹
    private boolean deleteLocalFile(File file) throws IOException {
        if (mListener.onProgressChanged(null, -1, -1, -1, -1, null, null)) {
            throw new IOException();
        }

        if (file.isFile()) {
            return file.delete();
        }

        if (file.isDirectory()) {
            File[] childFile = file.listFiles();
            if (childFile == null || childFile.length == 0) {
                return file.delete();
            }

            boolean ret = true;
            for (File f : childFile) {
                if (!deleteLocalFile(f)) {
                    ret = false;
                }
            }

            return file.delete() && ret;
        }

        return true;
    }

    public OperationResult downloadFile(List<DMFile> list) {
        DownloadTask task = new DownloadTask(list);
        int ret = task.run();
        return new OperationResult(ret, getDownloadPath());
    }

    private class DownloadTask {
        private List<DMFile> mList;
        private long mDownloaded = 0;
        private long mTotalSize = 0;
        private int completeNumber = 0;
        private int mSkipNum = 0;
        private int mOperationWhenSaveName = 0;
        private boolean mStopped = false;
        private String newName = null;
        private String headFolderName = null;

        private long lastTime = 0;
        private DownloadInfo[] DownloadInfoStr = new DownloadInfo[5];
        private int index = 0;

        public DownloadTask(List<DMFile> list) {
            mList = list;

            for (int i = 0; i < list.size(); i++) {
                if (mListener.onProgressChanged(null, -1, -1, -1, -1, null, null)) break;
                DMFile file = list.get(i);
                if (!file.isDir()) {
                    mTotalSize += file.mSize;
                } else {
                    mTotalSize += getDropBoxFolderSize(file);
                }
            }
        }

        public int getSkipNumber() {
            return mSkipNum;
        }


        private boolean checkSpace() {

            long ava = SDCardUtil.getAvailableSizeOf(getLocalRootPath());
            if (ava < mTotalSize) {
                return false;
            } else {
                return true;
            }
        }

        public int downloadFile(List<DMFile> list) {

            if (!checkSpace()) {
                //中断直接退出，不回调空间不足
                if (mListener.onProgressChanged(null, -1, -1, -1, -1, null, null))
                    return RET_AFTER_FAIL;
                if (mListener != null) {
                    mListener.onFinished(ERROR_PHONE_NOT_ENOUGH_SPACE, null);
                }

                return RET_AFTER_FAIL;
            }

            List<DMFile> finishedList = new ArrayList<>();
            for (DMFile file : list) {

                int ret = RET_AFTER_FAIL;
                try {
                    ret = downloadFile(file, null);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    //如果是用户取消。则跳出整个循环
                    if (mListener.onProgressChanged(null, -1, -1, -1, -1, null, null)) {
                        break;
                    }
                }

                if (ret == RET_AFTER_SKIP) {
                    mSkipNum++;
                } else if (ret == RET_AFTER_SUCCESS) {
                    finishedList.add(file);

                    if (mListener != null) {
                        mListener.onFileFinished(file);
                    }


                } else {
                    Log.e(TAG, "copy file err. " + file.mPath);
                }
                completeNumber++;
                headFolderName = null;

                if (mStopped) {
                    break;
                }
            }


            newName = null;
            headFolderName = null;

            if (finishedList.size() + mSkipNum == list.size()) {
                System.out.println("download ret ss");
                return RET_AFTER_SUCCESS;
            } else {
                return RET_AFTER_FAIL;
            }

        }


        public int downloadFile(DMFile file, String folder) throws IOException, InterruptedException {
            //递归只能用标志位退出
            if (mListener.onProgressChanged(null, -1, -1, -1, -1, null, null)) {
                throw new IOException();
            }
            int ret = RET_AFTER_FAIL;

            String dstFolder = getDownloadPath();
            if (folder == null) {
                dstFolder = getDownloadPath();
            } else {
                dstFolder = getDownloadPath() + "/" + folder;
            }

            if (file.isDir()) {
                if (headFolderName == null) {
                    headFolderName = file.getName();
                }
                ret = downloadFolderFromUdisk(file, dstFolder);
            } else {

                ret = downLoadFromUdisk(file, dstFolder);
            }

            System.out.println("downloadFile:" + ret);

            return ret;
        }


        private int downloadFolderFromUdisk(DMFile file, String desFolder) throws IOException, InterruptedException {
            if (mListener.onProgressChanged(null, -1, -1, -1, -1, null, null)) {
                throw new IOException();
            }
            int ret = RET_AFTER_SUCCESS;

            File localFile = new File(desFolder + File.separator + file.mName);
            if (localFile.exists()) {
                int op = 0;
                if (mOperationWhenSaveName != 0) {
                    op = -mOperationWhenSaveName;
                } else {
                    op = mListener.onSameFile(file.getName(), 0);
                    //System.out.println("exist 44 op="+op);
                    if (op < 0) {
                        // op小于0表示记住选择，op的正值表示真正的op
                        mOperationWhenSaveName = op;
                        op = -op;
                    }
                }

                if (op == OP_RENAME) {
                    String dir = localFile.getParent();
                    newName = getFileName(dir, localFile.getName(), mLocalFileTest);
                    localFile = new File(dir, newName);
                    localFile.mkdir();
                } else if (op == OP_SKIP) {
                    return RET_AFTER_SKIP;
                }

            } else {
                localFile.mkdir();
            }


            List<DMFile> list = getDropBoxFolderAllFiles(file);

            if (list != null) {

                for (DMFile xlFile : list) {
                    //System.out.println("loc folder pa2:"+xlFile.getDirPath());

                    String folderName = null;

                    if (headFolderName != null) {
                        String p1 = xlFile.getParent();
                        folderName = p1.substring(p1.indexOf(headFolderName));
                        if (newName != null) {
                            folderName = newName + folderName.substring(folderName.indexOf("/"));
                        }
                    }
                    ret = downloadFile(xlFile, folderName);
                }
            }

            System.out.println("downloadFolderFromUdisk:" + ret);

            return ret;
        }

        private int downLoadFromUdisk(final DMFile file, String saveFolder) throws IOException, InterruptedException {
            if (mListener.onProgressChanged(null, -1, -1, -1, -1, null, null)) {
                throw new IOException();
            }

            File localFile = new File(saveFolder + File.separator + file.mName);
            if (localFile.exists()) {
                int op = 0;
                if (mOperationWhenSaveName != 0) {
                    op = -mOperationWhenSaveName;
                } else {
                    op = mListener.onSameFile(file.getName(), 0);
                    //System.out.println("exist 44 op="+op);
                    if (op < 0) {
                        // op小于0表示记住选择，op的正值表示真正的op
                        mOperationWhenSaveName = op;
                        op = -op;
                    }
                }

                if (op == OP_RENAME) {
                    String dir = localFile.getParent();
                    String newName = getFileName(dir, localFile.getName(), mLocalFileTest);
                    localFile = new File(dir, newName);
                } else if (op == OP_SKIP) {
                    return RET_AFTER_SKIP;
                }
            } else {
                File dirFile = new File(saveFolder);
                if (!dirFile.exists()) {
                    if (!dirFile.mkdirs()) {
                        return RET_AFTER_FAIL;
                    }
                }
                if (!localFile.exists()) {
                    if (!localFile.createNewFile()) {
                        return RET_AFTER_FAIL;
                    }
                }

            }
            FileOutputStream mFos;
            try {
                mFos = new FileOutputStream(localFile);
            } catch (FileNotFoundException e) {
                return RET_AFTER_FAIL;
            }
            boolean ret = false;

            try {
                DMDropboxAPI.getInstance().getFileStream(file.mPath, null)
                        .copyStreamToOutput(mFos, new com.dropbox.client2.ProgressListener() {

                            @Override
                            public boolean onProgress(long bytes, long total) {
                                // TODO Auto-generated method stub
                                if (mListener != null) {
                                    long downBytes = mDownloaded + bytes;
                                    if (bytes >= total) {
                                        mDownloaded += total;
                                    }

                                    long tmpTime = System.currentTimeMillis();
                                    String speed = null;
                                    String leftTime = null;
                                    long bytePerSencond = 0;
                                    if (tmpTime - lastTime >= 1000) {
                                        if (DownloadInfoStr[index] == null) {
                                            // 5S前
                                            if (index == 0) {
                                                bytePerSencond = downBytes;
                                            } else {
                                                bytePerSencond = downBytes * 1000 / (tmpTime - DownloadInfoStr[0].curTime + 1000);
                                            }
                                            DownloadInfoStr[index] = new DownloadInfo(tmpTime, downBytes);
                                        } else {
                                            // 5S后
                                            bytePerSencond = (downBytes - DownloadInfoStr[index].downloadBytes) * 1000 / (tmpTime - DownloadInfoStr[index].curTime);
                                            //覆盖掉最久远的一个值
                                            DownloadInfoStr[index].curTime = tmpTime;
                                            DownloadInfoStr[index].downloadBytes = downBytes;
                                        }
                                        //算出速度
                                        speed = FileInfoUtils.getLegibilityFileSize(bytePerSencond) + BrothersApplication.getInstance()
                                                .getString(R.string.DM_File_Operate_Speed_psec);
                                        //算出时间
                                        if (bytePerSencond == 0) {
                                            leftTime = BrothersApplication.getInstance().getString(
                                                    R.string.DM_File_Operate_Remain_Time_unknow);
                                        } else {
                                            leftTime = TimeTool.convertSeconds(BrothersApplication.getInstance(),
                                                    (int) ((mTotalSize - downBytes) / bytePerSencond));
                                        }
                                        index = (++index) % DownloadInfoStr.length;
                                        lastTime = tmpTime;
                                    }

                                    mStopped = mListener.onProgressChanged(file.mName, (double) downBytes * 100 / mTotalSize,
                                            mList.size(), mList.size() - completeNumber, mTotalSize - downBytes, leftTime, speed);
                                }
                                return mStopped;
                            }
                        });
            } catch (DropboxException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                ret = true;
            }


            /**
             * mStopped 表示下载失败，网络，或者用户中断
             */

            if (mStopped || ret) {
                return RET_AFTER_FAIL;
            }
            return RET_AFTER_SUCCESS;
        }


        public int run() {
            // TODO Auto-generated method stub
            return downloadFile(mList);
        }

    }

    public boolean createDropBoxkDir(String path) {
        boolean ret = false;
        if (!DMDropboxAPI.getInstance().isExists(path)) {
            try {
                ret = DMDropboxAPI.getInstance().createFolder(path) != null ? true : false;
            } catch (DropboxException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        return ret;
    }

    public int createDropBoxDir(DMFile folder) {
        // TODO Auto-generated method stub

        CreateDirTask task = new CreateDirTask(folder);
        task.setProgressListener(mListener);
        int ret = task.run();
        return ret;
    }


    private class CreateDirTask {

        private DMFile mFolder;
        private int mOperationWhenSaveName = 0;
        private String newName = null;

        public CreateDirTask(DMFile folder) {
            // TODO Auto-generated constructor stub
            this.mFolder = folder;
        }


        public void setProgressListener(IProgressListener listener) {
            mListener = listener;
        }

        public int run() {
            // TODO Auto-generated method stub

            if (mFolder.isDir) {

                try {

                    if (DMDropboxAPI.getInstance().isExists(mFolder.mPath)) {

                        int op = 0;
                        if (mOperationWhenSaveName != 0) {
                            op = -mOperationWhenSaveName;
                        } else {

                            op = mListener.onSameFile(mFolder.getName(), 0);
                            //System.out.println("exist 44 op="+op);
                            if (op < 0) {
                                // op小于0表示记住选择，op的正值表示真正的op
                                mOperationWhenSaveName = op;
                                op = -op;
                            }
                        }

                        if (op == OP_RENAME) {
                            String dir = mFolder.getParent();
                            newName = getFileName(dir, mFolder.getName(), new FileExistsTest() {

                                @Override
                                public boolean test(String dir, String name) {
                                    // TODO Auto-generated method stub
                                    dir = dir.endsWith("/") ? dir : dir + File.separator;
                                    return DMDropboxAPI.getInstance().isExists(dir + name);
                                }
                            });

                            String newPath = mFolder.mPath.substring(0, mFolder.mPath.lastIndexOf("/")) + "/" + newName;

//							return createUdiskDir(newPath) == true? RET_AFTER_SUCCESS:RET_AFTER_FAIL;
                            return createDropBoxkDir(newPath) == true ? RET_AFTER_SUCCESS : RET_AFTER_FAIL;

                        } else if (op == OP_SKIP) {
                            return RET_AFTER_SKIP;
                        }


                    } else {
                        return createDropBoxkDir(mFolder.mPath) == true ? RET_AFTER_SUCCESS : RET_AFTER_FAIL;
                    }

                } catch (Exception e) {
                    // TODO: handle exception
                }

            }

            return RET_AFTER_FAIL;
        }

    }


    public List<DMFile> getDropBoxFolderAllFiles(DMFile file) {

        return file.isDir() ? getDropBoxFolderAllFiles(file.mPath) : null;
    }

    public List<DMFile> getDropBoxFolderAllFiles(String path) {
        List<DMFile> fileList = null;

        try {
            fileList = DMDropboxAPI.getInstance().listFileDropBox(path, -1, null, true, null);
        } catch (DropboxException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return fileList;
    }


    public List<DMFile> listLocalFolderAllFiles(String path, boolean listHiden) {
        File fileRoot = new File(path);
        if (!fileRoot.isDirectory()) {
            return null;
        }

        FileFilter filter = null;
        if (!listHiden) {
            filter = new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    if (pathname.getName().startsWith(".")) {
                        return false;
                    }

                    return true;
                }
            };
        }

        List<DMFile> fileItemList = new ArrayList<>();
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
                item.mName = file.getName();
                item.mLastModify = file.lastModified();
                item.mPath = file.getAbsolutePath();
                item.mSize = file.length();
                fileItemList.add(item);
            }
        }

        return fileItemList;
    }

    public List<DMFile> getUdiskAllFilesByType(DMFileCategoryType type) {

        List<DMFile> fileList = new ArrayList<>();

        DMFilePage filePage = DMSdk.getInstance().getFileListByType(type);

        if (filePage != null) {

            int pages = filePage.getTotalPage();
            fileList = filePage.getFiles();

            if (pages > 1) {

                for (int i = 1; i < pages; i++) {
                    DMFilePage next = DMSdk.getInstance().getFileListByType(type);
                    if (next != null) {
                        fileList.addAll(next.getFiles());
                    }
                }
            }
        }

        return fileList;
    }

    public long getLocalFolderSize(File file) {
        if (mListener.onProgressChanged(null, -1, -1, -1, -1, null, null) || file == null) return 0;
        if (file.isDirectory()) {
            long size = 0;
            File[] files = file.listFiles();
            int len = files.length;
            for (int j = 0; ; j++) {
                if (j >= len) {
                    return size;
                }
                size += getLocalFolderSize(files[j]);
            }
        }
        return file.length();
    }


    public long getDropBoxFolderSize(DMFile file) {
        if (mListener.onProgressChanged(null, -1, -1, -1, -1, null, null)) return 0;
        long size = 0;

        if (file.isDir()) {

            List<DMFile> fileList = null;
            try {
                fileList = DMDropboxAPI.getInstance().listFileDropBox(file.mPath, -1, null, true, null);
            } catch (DropboxException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            if (fileList != null) {

                for (DMFile dmFile : fileList) {
                    if (dmFile.isDir()) {
                        size += getDropBoxFolderSize(dmFile);
                    } else {
                        size += dmFile.mSize;
                    }
                }
            }
        }

        return size;
    }

    public long getClassicFolderSize(DMFile file) {
        if (mListener.onProgressChanged(null, -1, -1, -1, -1, null, null)) return 0;
        long size = 0;

        DMFilePage filePage = DMSdk.getInstance().getFileListInDirByType(DMFileCategoryType.E_PICTURE_CATEGORY, file.mPath, -1);
        if (filePage != null && filePage.getFiles() != null) {
            List<DMFile> files = filePage.getFiles();
            if (files != null) {

                for (DMFile dmFile : files) {
                    size += dmFile.mSize;
                }
            }
        }

        return size;
    }

    public interface ProgressListener {
        boolean onProgressChange(double progress);

        int onFinished(int err);
    }

    public void doDownload(final DMFile file, final String savePath, final ProgressListener listener) {
        new Thread() {
            public void run() {

                String localFilePath = savePath.endsWith("/") ?
                        savePath + file.mName : savePath + File.separator + file.mName;
                File localFile = new File(localFilePath);
                if (localFile.exists()) {
                    //System.out.println("dw 1 exist");
                    if (listener != null) {
                        listener.onFinished(0);
                        return;
                    }
                }

//				DMDownload task = new DMDownload(file.mPath, savePath,  new OnProgressChangeListener() {
//					
//					@Override
//					public int onProgressChange(int uid, long total, long already) {
//						// TODO Auto-generated method stub
//						int ret = 0;
//						double progress = (double) already * 100 / total;
//						//System.out.println("dodod:"+progress);
//						boolean stop = listener.onProgressChange(progress);
//						if (stop) {
//							ret = -1;
//						}
//						
//						return ret;
//					}
//				});
//			
//				DMSdk.getInstance().download(task);


                FileOutputStream mFos = null;
                try {
                    mFos = new FileOutputStream(localFile);
                } catch (FileNotFoundException e) {
                    listener.onFinished(-1);
                    return;
                }
                int ret = 0;

                try {
                    DMDropboxAPI.getInstance().getFileStream(file.mPath, null)
                            .copyStreamToOutput(mFos, new com.dropbox.client2.ProgressListener() {

                                @Override
                                public boolean onProgress(long bytes, long total) {
                                    // TODO Auto-generated method stub
                                    double progress = (double) bytes * 100 / total;
                                    //System.out.println("dodod:"+progress);
                                    return listener.onProgressChange(progress);

                                }
                            });
                } catch (DropboxException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    ret = -1;
                }

                if (mListener != null) {
                    listener.onFinished(ret);
                }

            }
        }.start();
    }


    public OperationResult uploadFile(List<DMFile> list, String desPath) {
        // TODO Auto-generated method stub
        UploadTask task = new UploadTask(list, desPath);
        int ret = task.run();
        return new OperationResult(ret, desPath);
    }

    private class UploadTask {
        private List<DMFile> mList;
        private long mUploaded = 0;
        private long mTotalSize = 0;
        private int completeNumber = 0;
        private int mSkipNum = 0;
        private int mOperationWhenSaveName = 0;
        private boolean mStopped = false;
        private String mDesPath = null;
        private String headFolderName = null;
        private String newName = null;
        private DMStorageInfo mStorageInfo = null;


        private long lastTime = 0;
        private DownloadInfo[] DownloadInfoStr = new DownloadInfo[5];
        private int index = 0;

        public UploadTask(List<DMFile> list, String desPath) {
            mList = list;
            mDesPath = desPath;

            for (int i = 0; i < list.size(); i++) {
                if (mListener.onProgressChanged(null, -1, -1, -1, -1, null, null)) break;
                DMFile file = list.get(i);
                if (!file.isDir()) {
                    mTotalSize += file.mSize;
                } else {
                    File localFile = new File(file.getPath());
                    mTotalSize += getLocalFolderSize(localFile);
                }
            }
        }

        public int getSkipNumber() {
            return mSkipNum;
        }

        public int uploadFile(List<DMFile> list) {
            if (DMDropboxAPI.getInstance().getFreeSpace() < mTotalSize) {
                //中断直接退出，不回调空间不足
                if (mListener.onProgressChanged(null, -1, -1, -1, -1, null, null))
                    return RET_AFTER_FAIL;
                if (mListener != null) {
                    mListener.onFinished(ERROR_UDISK_NOT_ENOUGH_SPACE, null);
                }

                return RET_AFTER_FAIL;
            }
            List<DMFile> finishedList = new ArrayList<>();
            for (DMFile file : list) {
                headFolderName = null;
                int ret = RET_AFTER_FAIL;
                try {
                    ret = uploadFile(file, null);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    //如果是用户取消。则跳出整个循环
                    if (mListener.onProgressChanged(null, -1, -1, -1, -1, null, null)) {
                        break;
                    }
                }

                if (ret == RET_AFTER_SKIP) {
                    mSkipNum++;
                } else if (ret == RET_AFTER_SUCCESS) {
                    finishedList.add(file);
                    if (mListener != null) {
                        mListener.onFileFinished(file);
                    }
                }

                completeNumber++;
            }


            mDesPath = null;
            newName = null;
            headFolderName = null;

            if (finishedList.size() + mSkipNum == list.size()) {
                return RET_AFTER_SUCCESS;
            } else {
                return RET_AFTER_FAIL;
            }

        }


        public int uploadFile(DMFile file, String folder) throws IOException, InterruptedException {
            //递归只能用标志位退出
            if (mListener.onProgressChanged(null, -1, -1, -1, -1, null, null)) {
                throw new IOException();
            }
            int ret = RET_AFTER_FAIL;

            String dstFolder = null;
            if (folder == null) {
                dstFolder = mDesPath;
            } else {
                dstFolder = mDesPath + "/" + folder;
            }

            if (file.isDir()) {
                if (headFolderName == null) {
                    headFolderName = file.getName();
                }
                ret = uploadFolderFromLocal(file, dstFolder);
            } else {
                ret = upLoadFromLocal(file, dstFolder);
            }

            return ret;
        }


        private int uploadFolderFromLocal(DMFile file, String desFolder) throws IOException, InterruptedException {
            if (mListener.onProgressChanged(null, -1, -1, -1, -1, null, null)) {
                throw new IOException();
            }
            int ret = RET_AFTER_SUCCESS;

            String desFilePath = null;

            if (desFolder.endsWith("/")) {
                desFilePath = desFolder + file.mName;
            } else {
                desFilePath = desFolder + File.separator + file.mName;
            }

            if (DMSdk.getInstance().isExisted(desFilePath)) {
                int op = 0;
                if (mOperationWhenSaveName != 0) {
                    op = -mOperationWhenSaveName;
                } else {
                    op = mListener.onSameFile(file.getName(), 0);
                    //System.out.println("exist 44 op="+op);
                    if (op < 0) {
                        // op小于0表示记住选择，op的正值表示真正的op
                        mOperationWhenSaveName = op;
                        op = -op;
                    }
                }

                if (op == OP_RENAME) {

                    File dstFile = new File(desFilePath);
                    String dir = dstFile.getParent();

                    newName = getFileName(dir, dstFile.getName(), new FileExistsTest() {

                        @Override
                        public boolean test(String dir, String name) throws IOException {
                            // TODO Auto-generated method stub
                            dir = dir.endsWith("/") ? dir : dir + File.separator;
                            return DMDropboxAPI.getInstance().isExists(dir + name);

                        }

                    });

                    //System.out.println("folder newname:"+newName);

                    String newPath = desFilePath.substring(0, desFilePath.lastIndexOf("/")) + "/" + newName;


                    //System.out.println("folder new :"+newPath);
                    createDropBoxkDir(newPath);


                } else if (op == OP_SKIP) {
                    return RET_AFTER_SKIP;
                }
            }
            List<DMFile> list = null;
            try {
                list = listLocalFolderAllFiles(file.mPath, false);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                if (mListener.onProgressChanged(null, -1, -1, -1, -1, null, null)) {
                    throw new IOException();
                }
            }

            if (list != null && list.size() == 0) {

                return createDropBoxkDir(desFilePath) == true ? RET_AFTER_SUCCESS : RET_AFTER_FAIL;
            }

            for (DMFile xlFile : list) {
                //System.out.println("loc folder pa2:"+xlFile.getDirPath());

                String folderName = null;

                if (headFolderName != null) {
                    String p1 = xlFile.getParent();
                    System.out.println("p1:" + p1);

                    folderName = p1.substring(p1.indexOf(headFolderName));
                    System.out.println("loc folder pa3:" + folderName);

                    if (newName != null) {
                        folderName = folderName.replace(headFolderName, newName);
                    }
                }

                ret = uploadFile(xlFile, folderName);
            }

            return ret;
        }


        private int upLoadFromLocal(final DMFile file, String saveFolder) throws IOException, InterruptedException {
            if (mListener.onProgressChanged(null, -1, -1, -1, -1, null, null)) {
                throw new IOException();
            }

//			long size_m = 4 * 1024;
//			float size_f_m = file.mSize / 1024 / 1024;
//			
//			if (size_f_m  > size_m && mStorageInfo!= null && mStorageInfo.getStorages().size() > 0 && 
//					mStorageInfo.getStorages().get(0).fsType != null && mStorageInfo.getStorages().get(0).fsType.equals("msdos") ) {
//				int op = mListener.onFileSystemUnSopport(file.getName());
//				mUploaded += file.mSize;
//				return RET_AFTER_SKIP;
//			}

            String desUrl = null;

            if (saveFolder != null && saveFolder.length() > 1 && saveFolder.endsWith("/")) {
                desUrl = saveFolder + file.mName;
            } else {
                desUrl = saveFolder + File.separator + file.mName;
            }

            DMFile testFile = new DMFile();
            testFile.mName = file.mName;
            testFile.mPath = desUrl;
            if (DMDropboxAPI.getInstance().isExists(desUrl)) {
                int op = 0;
                if (mOperationWhenSaveName != 0) {
                    op = -mOperationWhenSaveName;
                } else {
                    op = mListener.onSameFile(file.getName(), 0);
                    //System.out.println("exist 44 op="+op);
                    if (op < 0) {
                        // op小于0表示记住选择，op的正值表示真正的op
                        mOperationWhenSaveName = op;
                        op = -op;
                    }
                }

                if (op == OP_RENAME) {

                    String dir = testFile.getParent();
                    newName = getFileName(dir, testFile.getName(), new FileExistsTest() {

                        @Override
                        public boolean test(String dir, String name) {
                            // TODO Auto-generated method stub
                            dir = dir.endsWith("/") ? dir : dir + File.separator;
                            return DMDropboxAPI.getInstance().isExists(dir + name);
                        }

                    });

                    desUrl = saveFolder + File.separator + newName;

                } else if (op == OP_SKIP) {
                    mUploaded += file.mSize;
                    return RET_AFTER_SKIP;
                }
            }
//				
//			DMUpload task = new DMUpload(file.mPath, desUrl, new OnProgressChangeListener() {
//				
//				@Override
//				public int onProgressChange(int uid, long total, long already) {
//					// TODO Auto-generated method stub
//					int ret = 0;
//					if (mListener != null) {
//						long uploadBytes = mUploaded + already;
//						//System.out.println("uploadBytes:"+uploadBytes + ",mTotalSize="+mTotalSize);
//						mStopped = mListener.onProgressChanged(file.mName,(double) uploadBytes * 100 / mTotalSize);
//						if (mStopped) {
//							ret = -1;
//						}
//					}
//					
//					if (already >= total) {
//						mUploaded += total;
//					}
//					return ret;
//					
//				}
//			});

            FileInputStream mFis = null;
            try {
                mFis = new FileInputStream(file.mPath);
            } catch (FileNotFoundException e) {
                return RET_AFTER_FAIL;
            }


            Entry ret = null;
            try {
                ret = DMDropboxAPI.getInstance().putFileOverwrite(desUrl, mFis, file.mSize,
                        new com.dropbox.client2.ProgressListener() {

                            @Override
                            public boolean onProgress(long bytes, long total) {
                                // TODO Auto-generated method stub
                                if (mListener != null) {
                                    long uploadBytes = mUploaded + bytes;
                                    if (bytes >= total) {
                                        mUploaded += total;
                                    }
                                    long tmpTime = System.currentTimeMillis();
                                    String speed = null;
                                    String leftTime = null;
                                    long bytePerSencond = 0;
                                    if (tmpTime - lastTime >= 1000) {
                                        if (DownloadInfoStr[index] == null) {
                                            // 5S前
                                            if (index == 0) {
                                                bytePerSencond = uploadBytes;
                                            } else {
                                                bytePerSencond = uploadBytes * 1000 / (tmpTime - DownloadInfoStr[0].curTime + 1000);
                                            }
                                            DownloadInfoStr[index] = new DownloadInfo(tmpTime, uploadBytes);
                                        } else {
                                            // 5S后
                                            bytePerSencond = (uploadBytes - DownloadInfoStr[index].downloadBytes) * 1000 / (tmpTime - DownloadInfoStr[index].curTime);
                                            //覆盖掉最久远的一个值
                                            DownloadInfoStr[index].curTime = tmpTime;
                                            DownloadInfoStr[index].downloadBytes = uploadBytes;
                                        }
                                        //算出速度
                                        speed = FileInfoUtils.getLegibilityFileSize(bytePerSencond) + BrothersApplication.getInstance()
                                                .getString(R.string.DM_File_Operate_Speed_psec);
                                        //算出时间
                                        if (bytePerSencond == 0) {
                                            leftTime = BrothersApplication.getInstance().getString(
                                                    R.string.DM_File_Operate_Remain_Time_unknow);
                                        } else {
                                            leftTime = TimeTool.convertSeconds(BrothersApplication.getInstance(),
                                                    (int) ((mTotalSize - uploadBytes) / bytePerSencond));
                                        }
                                        index = (++index) % DownloadInfoStr.length;
                                        lastTime = tmpTime;
                                    }


                                    mStopped = mListener.onProgressChanged(file.mName, (double) uploadBytes * 100 / mTotalSize,
                                            mList.size(), mList.size() - completeNumber, mTotalSize - uploadBytes, leftTime, speed);
                                }

                                return mStopped;
                            }
                        });
            } catch (DropboxException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            if (mStopped || ret == null) {

                return RET_AFTER_FAIL;
            }

            return RET_AFTER_SUCCESS;

        }


        public int run() {
            // TODO Auto-generated method stub

            return uploadFile(mList);
        }

    }

    public OperationResult copyTo(List<DMFile> list, String desPath) {
        // TODO Auto-generated method stub
        CopyTask task = new CopyTask(list, desPath);
        int ret = task.run();
        return new OperationResult(ret, desPath);
    }

    private class CopyTask {
        private List<DMFile> mList;
        private long mCopyed = 0;
        private int mSkipNum = 0;
        private long mTotalSize = 0;
        private int mOperationWhenSaveName = 0;
        private boolean mStopped = false;
        private String mDesPath = null;
        private String newName = null;

        public CopyTask(List<DMFile> list, String desPath) {
            mList = list;
            mDesPath = desPath;

            for (int i = 0; i < list.size(); i++) {
                if (mListener.onProgressChanged(null, -1, -1, -1, -1, null, null)) break;
                DMFile file = list.get(i);
                if (!file.isDir()) {
                    mTotalSize += file.mSize;
                } else {
                    mTotalSize += getDropBoxFolderSize(file);
                }
            }
        }

        public int copyFiles(List<DMFile> list) {

            if (DMDropboxAPI.getInstance().getFreeSpace() < mTotalSize) {
                //中断直接退出，不回调空间不足
                if (mListener.onProgressChanged(null, -1, -1, -1, -1, null, null))
                    return RET_AFTER_FAIL;
                if (mListener != null) {
                    mListener.onFinished(ERROR_UDISK_NOT_ENOUGH_SPACE, null);
                }

                return RET_AFTER_FAIL;
            }

            List<DMFile> finishedList = new ArrayList<>();
            for (DMFile file : list) {
                int ret = RET_AFTER_FAIL;
                try {
                    ret = copyFiles(file);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    //如果是用户取消。则跳出整个循环
                    if (mListener.onProgressChanged(null, -1, -1, -1, -1, null, null)) {
                        break;
                    }
                }

                if (ret == RET_AFTER_SKIP) {
                    mSkipNum++;
                } else if (ret == RET_AFTER_SUCCESS) {
                    finishedList.add(file);

                    if (mListener != null) {
                        mListener.onFileFinished(file);
                    }


                } else {
                    Log.e(TAG, "cpto file err. " + file.mPath);
                }

                if (mStopped) {
                    break;
                }
            }


            mDesPath = null;
            newName = null;

            if (finishedList.size() + mSkipNum == list.size()) {
                return RET_AFTER_SUCCESS;
            } else {
                return RET_AFTER_FAIL;
            }

        }

        public int copyFiles(final DMFile file) throws IOException, InterruptedException {
            //递归只能用标志位退出
            if (mListener.onProgressChanged(null, -1, -1, -1, -1, null, null)) {
                throw new IOException();
            }

            String desUrl = mDesPath + File.separator + file.mName;

            DMFile testFile = new DMFile();
            testFile.mName = file.mName;
            testFile.mPath = desUrl;
            if (DMDropboxAPI.getInstance().isExists(desUrl)) {
                int op = 0;
                if (mOperationWhenSaveName != 0) {
                    op = -mOperationWhenSaveName;
                } else {
                    op = mListener.onSameFile(file.getName(), 0);
                    //System.out.println("exist 44 op="+op);
                    if (op < 0) {
                        // op小于0表示记住选择，op的正值表示真正的op
                        mOperationWhenSaveName = op;
                        op = -op;
                    }
                }

                if (op == OP_RENAME) {

                    String dir = testFile.getParent();
                    newName = getFileName(dir, testFile.getName(), new FileExistsTest() {

                        @Override
                        public boolean test(String dir, String name) {
                            // TODO Auto-generated method stub
                            dir = dir.endsWith("/") ? dir : dir + File.separator;
                            return DMDropboxAPI.getInstance().isExists(dir + name);
                        }

                    });

                    desUrl = mDesPath + File.separator + newName;

                } else if (op == OP_SKIP) {
                    return RET_AFTER_SKIP;
                }
            }

            Entry ret = null;
            try {
                ret = DMDropboxAPI.getInstance().copy(file.mPath, desUrl);
            } catch (DropboxException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            if (mStopped || ret == null) {

                return RET_AFTER_FAIL;
            }

            return RET_AFTER_SUCCESS;

        }

        public int run() {
            // TODO Auto-generated method stub
            return copyFiles(mList);
        }
    }


    public FileExistsTest mLocalFileTest = new FileExistsTest() {
        @Override
        public boolean test(String dir, String name) {
            // TODO Auto-generated method stub
            File file = new File(dir, name);
            return file.exists();
        }
    };

    public interface FileExistsTest {
        boolean test(String dir, String name) throws IOException;
    }

    private class RenameTask {
        private DMFile mFile;
        private String mNewName;

        public RenameTask(DMFile list, String name) {
            mFile = list;
            this.mNewName = name;
        }


        public boolean run() {
            return renameFile(mFile, mNewName);
        }

        boolean renameFile(DMFile file, String name) {
            boolean bOK = false;
            String testPath = file.getParent() + name;
            //外面已经做了判断，这里可以适当去掉 节约时间
//			if(isNameExist(testPath, file.mLocation))
//			{
//				mListener.onFinished(ERROR_RENAME_NAME_EXIST);
//				return false;
//			}

            bOK = renameFileOperation(file, name);
            if (bOK) {
                if (AodPlayer.getInstance().getIsPlaying()) {

                    String filePath = getFullPath(file);

                    if (filePath.equals(AodPlayer.getInstance().getCurPlayPath())) {
                        AodPlayer.getInstance().playNext();
                    }
                }

            } else {
                return false;
            }


            return bOK;
        }

        private boolean renameFileOperation(DMFile file, String desName) {
            boolean bOK = false;
            if (file.mLocation == DMFile.LOCATION_LOCAL) {
                File rawFile = new File(file.getPath());
                String newName = file.getParent() + desName;
                bOK = rawFile.renameTo(new File(newName));
            } else {
                String newUrl = file.getParent() + desName;

                try {
                    bOK = DMDropboxAPI.getInstance().move(file.mPath, newUrl) != null ? true : false;
                } catch (DropboxException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            return bOK;
        }

    }

    ;

    public boolean renameFile(List<DMFile> list, String newName) {
        RenameTask renameTask = new RenameTask(list.get(0), newName);
        boolean ret = renameTask.run();
        return ret;
    }

    public boolean isNameExist(String path, int location) {
        if (location == DMFile.LOCATION_LOCAL) {
            File file = new File(path);
            if (file.exists())
                return true;
            else
                return false;
        } else if (location == DMFile.LOCATION_UDISK) {
            return DMSdk.getInstance().isExisted(path);
        } else {
            return DMDropboxAPI.getInstance().isExists(path);
        }

    }

    public void stop() {
        isUserStop = true;
    }

    public String getLocalRootPath() {

        String slaverSDPath = SDCardUtil.getPrimarySDCard();
        if (slaverSDPath.length() > 0 && Build.VERSION.SDK_INT < 19) {
            return slaverSDPath;
        }

        return SDCardUtil.getPrimarySDCard();
    }


    public String getDownloadPath() {
        return getLocalRootPath() + "My Downloads";
    }

    public void initDownloadFolder() {
        // TODO Auto-generated method stub
        mkdir(getLocalRootPath() + "My Downloads");
    }

    public void clearCache() {
        try {
            Application application = BrothersApplication.getInstance();
            deleteLocalFile(new File(mHelper.getCachePath()));
            deleteLocalFile(application.getCacheDir());
            deleteLocalFile(application.getExternalCacheDir());
            deleteLocalFile(application.getFilesDir());
            // deleteLocalFile(new File(TransTools.SDCARD_PATH + TransTools.DISK_BU));
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }

    }

    public long getCacheSize() {
        Application application = BrothersApplication.getInstance();
        File file1 = new File(getCachePath());
        File file2 = application.getCacheDir();
        File file3 = application.getExternalCacheDir();
        File file4 = application.getFilesDir();

        long size1 = getLocalFolderSize(file1);
        long size2 = getLocalFolderSize(file2);
        long size3 = getLocalFolderSize(file3);
        long size4 = getLocalFolderSize(file4);
        System.out.println("cache:" + size1 + " + " + size2 + " + " + size3 + " + " + size4);
        return size1 + size2 + size3 + size4;
    }

    public String getCachePath() {
        File file = new File(this.getLocalRootPath(), ".airdisk-cache");
        if (!file.exists()) {
            file.mkdir();
        }

        return file.getPath();
    }

    private void mkdir(String path) {
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdir();
        }
    }

    public String generateFileId(DMFile file) {

        String src = file.mPath + file.mLastModify + AndroidConfig.getIMEI();

        return MD5Tools.toMD5(src);
    }


}
