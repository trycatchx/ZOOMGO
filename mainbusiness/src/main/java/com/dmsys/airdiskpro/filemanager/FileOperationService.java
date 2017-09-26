package com.dmsys.airdiskpro.filemanager;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.dmsys.airdiskpro.utils.FileInfoUtils;
import com.dmsys.airdiskpro.utils.HandlerUtil;
import com.dmsys.dmsdk.DMSdk;
import com.dmsys.dmsdk.model.DMFile;
import com.dmsys.dropbox.utils.DropBoxFileOperationHelper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Semaphore;

import static com.dmsys.airdiskpro.filemanager.FileOperationService.ModuleType.UDisk;

public class FileOperationService extends Service {
    private static final String TAG = FileOperationService.class.getSimpleName();
    private Handler mHandler = new Handler();
    private final IBinder mBinder = new OperationBinder();
    private boolean mStopped = false;

    public static final String Rename_Key = "rename_Key";

    public static final int OP_SUCCESSED = 0;
    public static final int OP_FAILED = 1;
    public static final int OP_CANCELED = 2;
    public static final int OP_SKIPED = 3;

    public static final int FILE_OP_DOWNLOAD = 0;
    public static final int FILE_OP_UPLOAD = 1;
    public static final int FILE_OP_DELETE = 2;
    public static final int FILE_OP_RENAME = 3;
    public static final int FILE_OP_COPYTO = 4;
    public static final int FILE_OP_NEWFOLDER = 5;
    public static final int FILE_OP_BACKUP = 6;
    public static final int FILE_OP_DECRYPTED = 7;
    public static final int FILE_OP_ENCRYPTED = 8;
    public static final int FILE_OP_ENCRYPRED_DELETE = 9;

    public static final int MSG_DELETE_FINISHED = HandlerUtil.generateId();
    public static final int MSG_RENAME_FINISHED = HandlerUtil.generateId();
    public static final int MSG_NEWFOLDER_FINISHED = HandlerUtil.generateId();
    public static final int MSG_DOWNLOAD_FINISHED = HandlerUtil.generateId();
    public static final int MSG_UPLOAD_FINISHED = HandlerUtil.generateId();
    public static final int MSG_COPY_FINISHED = HandlerUtil.generateId();
    public static final int MSG_BACKUP_FINISHED = HandlerUtil.generateId();
    public static final int MSG_DECRYPTED_FINISHED = HandlerUtil.generateId();
    public static final int MSG_ENCRYPTED_FINISHED = HandlerUtil.generateId();
    public static final int MSG_ENCRYPTED_DELETE_FINISHED = HandlerUtil.generateId();

    public static final int MSG_PROGRESS_CHANGED = HandlerUtil.generateId();
    public static final int MSG_FILE_FINISHED = HandlerUtil.generateId();
    public static final int MSG_ERROR = HandlerUtil.generateId();
    public static final int MSG_SAME_FILE = HandlerUtil.generateId();
    public static final int MSG_CONTAIN_SPECIAL_SYMBOLS = HandlerUtil.generateId();
    public static final int MSG_FILESYSTEM_UNSUPPORT = HandlerUtil.generateId();

    public static final String EXTRA_MODULE_TYPE = "FileOperationService.extra_module_type";

    public static final String EXTRA_TYPE = "FileOperationService.extra_type";
    public static final String EXTRA_OP = "FileOperationService.extra_op";
    public static final String EXTRA_STORAGE = "FileOperationService.extra_storage";
    public static final String EXTRA_LIST = "FileOperationService.extra_list";
    public static final String EXTRA_BUNDLE = "FileOperationService.extra_bundle";
    public static final String EXTRA_NEWFOLDER = "FileOperationService.new_folder";


    public static List<DMFile> selectedList = new ArrayList<DMFile>();
    private DMFile mNewFolder;

    // 目前同时只能有一个任务存在
    private TaskInfo mCurrentTaskInfo;

    private double mPreProgress = 0;
    private long lastTime = 0;
    private FileOperationThread thread;


    public enum ModuleType {
        UDisk, DropBox
    }

    ModuleType mModuleType = UDisk;

    public class SameNameInfo {
        public String name;
        public int tag;
        public Semaphore semp;
        public int operation;
        public int ret;
    }

    public static class OperationResult {
        public int ret;
        public String desPath;
        public List<DMFile> list;

        public OperationResult(int ret, String desPath) {
            this.ret = ret;
            this.desPath = desPath;
        }

        public OperationResult(int ret, String desPath, List<DMFile> list) {
            this.ret = ret;
            this.desPath = desPath;
            this.list = list;
        }
    }

    private final class TaskInfo {
        List<DMFile> list;
        int op;
        double progress = 0.0;
        int finished = 0;
    }

    public class ProgressInfo {
        public String path;
        public double progress;
        public int numberLeft;
        public int count;
        public String sizeLeft;
        public String timeLeft;
        public String speed;

    }


    public interface IProgressListener {
        boolean onProgressChanged(String path, double progress, int count, int numberLeft
                , long sizeLeft, String timeLeft, String speed); // 进度, 已经转换成百分数

        void onFinished(int err, List<DMFile> unOperaFiles);
//		List<DMFile> unSuccessFiles

        void onFileFinished(DMFile file);

        int onSameFile(String name, int tag) throws InterruptedException;

        void onContainSpecialSymbols(String name);

        int onFileSystemUnSopport(String name) throws InterruptedException;
    }

    private IProgressListener mListener = new IProgressListener() {
        @Override
        public boolean onProgressChanged(String path, double progress, int count, int numberLeft
                , long sizeLeft, String timeLeft, String speed) {
            // TODO Auto-generated method stub
            //中断
            if (mStopped) {
                return true;
            }
            //继续执行
            if (path == null && progress == -1) {
                return false;
            }

            ProgressInfo info = null;

            long tmpTime = System.currentTimeMillis();
            //300mS 更新一次
            if (tmpTime - lastTime > 300 || timeLeft != null) {
                ////System.out.println("percent->"+ mPreProgress + ":" + progress);

                info = new ProgressInfo();
                info.path = path;
                info.progress = progress;

                info.numberLeft = numberLeft;
                info.count = count;
                info.sizeLeft = sizeLeft < 0 ? null : FileInfoUtils.getLegibilityFileSize(sizeLeft);
                info.timeLeft = timeLeft;
                info.speed = speed;
                lastTime = tmpTime;

                sendMessageByHandler(MSG_PROGRESS_CHANGED, 0, 0, info);
            }


            if (mStopped) {
                mPreProgress = 0;
            }

            // 返回true停止操作
            return mStopped;
        }

        @Override
        public void onFinished(int err, List<DMFile> unOperaFiles) {
            // TODO Auto-generated method stub
            Log.d(TAG, "file transfer ok! err = " + err);

            if (err != FileOperationHelper.ERROR_SUCESS) {
                sendMessageByHandler(MSG_ERROR, err, 0, unOperaFiles);
            }
        }

        @Override
        public void onFileFinished(DMFile file) {
            // TODO Auto-generated method stub
            mCurrentTaskInfo.finished += 1;
        }

        @Override
        public int onSameFile(String name, int tag) throws InterruptedException {
            // TODO Auto-generated method stub
            final Semaphore semp = new Semaphore(1);
            semp.acquire();
            SameNameInfo info = new SameNameInfo();
            info.name = name;
            info.tag = tag;
            info.semp = semp;
            info.operation = mCurrentTaskInfo.op;

            sendMessageByHandler(MSG_SAME_FILE, 0, 0, info);

            semp.acquire();

            return info.ret;
        }


        @Override
        public void onContainSpecialSymbols(String name) {
            // TODO Auto-generated method stub
            sendMessageByHandler(MSG_CONTAIN_SPECIAL_SYMBOLS, 0, 0, name);
        }

        @Override
        public int onFileSystemUnSopport(String name) throws InterruptedException {
            // TODO Auto-generated method stub
            final Semaphore semp = new Semaphore(1);
            semp.acquire();
            SameNameInfo info = new SameNameInfo();
            info.name = name;
            info.tag = 0;
            info.semp = semp;
            info.operation = mCurrentTaskInfo.op;

            sendMessageByHandler(MSG_FILESYSTEM_UNSUPPORT, 0, 0, info);

            semp.acquire();

            return info.ret;
        }
    };

    public class OperationBinder extends Binder {
        public FileOperationService getService() {
            return FileOperationService.this;
        }
    }

    public void setHandler(Handler handler) {
        this.mHandler = handler;
    }

    public void sendMessageByHandler(int what, int arg1, int arg2, Object obj) {
        if (mHandler == null) {
            return;
        }
        mHandler.obtainMessage(what, arg1, arg2, obj).sendToTarget();
    }


    public void cancelCurOperation() {
        mStopped = true;
        System.out.println("ppppppp cancelCurOperation mStopped:" + mStopped);
        if (thread != null) {
            thread.interrupt();
            thread = null;
        }
    }

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        FileOperationHelper.getInstance().setProgressListener(mListener);
        DropBoxFileOperationHelper.getInstance().setProgressListener(mListener);
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        mStopped = true;
        setHandler(null);
        super.onDestroy();
        cancelCurOperation();
        Log.d(TAG, "service stopped!");
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        Log.d(TAG, "onBind()");
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO Auto-generated method stub
        if (intent == null) return START_NOT_STICKY;
        Bundle bundle = intent.getBundleExtra(EXTRA_BUNDLE);
        int op = bundle.getInt(EXTRA_OP);
        boolean operateType = bundle.getBoolean(EXTRA_TYPE);
        String uDiskName = bundle.getString(EXTRA_STORAGE);
        String rename_new = bundle.getString(Rename_Key);

        int indexModuleType = bundle.getInt(EXTRA_MODULE_TYPE, UDisk.ordinal());
        if (indexModuleType < ModuleType.values().length) {
            mModuleType = ModuleType.values()[indexModuleType];
        }

        Serializable newFolder = bundle.getSerializable(EXTRA_NEWFOLDER);
        if (newFolder != null) {
            mNewFolder = (DMFile) newFolder;
        }

        List<DMFile> list = new ArrayList<>();

        if (op == FileOperationService.FILE_OP_NEWFOLDER && mNewFolder != null) {
            list.add(mNewFolder);
        } else {
            list = selectedList;
        }

        mPreProgress = 0;
        mStopped = false;
        FileOperationHelper.isUserStop = false;
        if ((list != null && list.size() > 0)) {
            thread = new FileOperationThread(op, operateType, uDiskName, list, rename_new, mModuleType);
            thread.start();
        } else {
            System.out.println("SERVICE STARTTED file null");
        }

        System.out.println("SERVICE STARTTED!!!");

        return START_NOT_STICKY;
    }


    private void doFileOperation(int op, boolean type,
                                 String uDiskName, final List<DMFile> list, String newName, ModuleType mModuleType) {
        if (list.size() == 0) {
            return;
        }
        mCurrentTaskInfo = new TaskInfo();
        mCurrentTaskInfo.list = list;
        mCurrentTaskInfo.op = op;

        int msg = 0;

        //保存操作不成功的文件
        HashMap<String, List> trans = new HashMap<String, List>();

        if (op == FILE_OP_DELETE) {
            int ret = 0;
            switch (mModuleType) {
                case DropBox:
                    ret = DropBoxFileOperationHelper.getInstance().deleteFile(type, list);
                    break;
                case UDisk:
                    ret = FileOperationHelper.getInstance().deleteFile(type, list);
                    DMSdk.getInstance().syncSystem();
                    break;
            }
            msg = MSG_DELETE_FINISHED;
            if (ret == 0) {
                sendMessageByHandler(msg, OP_SUCCESSED, 0, trans);

            } else {
                if (mStopped) {
                    sendMessageByHandler(msg, OP_CANCELED, 0, list);
                } else {
                    sendMessageByHandler(msg, OP_FAILED, 0, ret);
                }
            }

        } else if (op ==FILE_OP_RENAME) {
            boolean bOK = false;
            switch (mModuleType) {
                case DropBox:
                    bOK = DropBoxFileOperationHelper.getInstance().renameFile(list, newName);
                    break;
                case UDisk:
                    bOK = FileOperationHelper.getInstance().renameFile(list, newName);
                    DMSdk.getInstance().syncSystem();
                    break;
            }
            msg = MSG_RENAME_FINISHED;
            if (bOK) {
                sendMessageByHandler(msg, OP_SUCCESSED, 0, trans);
            } else {
                sendMessageByHandler(msg, OP_FAILED, 0, list);
            }
        } else if (op == FILE_OP_DOWNLOAD) {
            OperationResult result = null;
            switch (mModuleType) {
                case DropBox:
                    result = DropBoxFileOperationHelper.getInstance().downloadFile(list);
                    break;
                case UDisk:
                    result = FileOperationHelper.getInstance().downloadFile(this, list);
                    break;
            }
            msg = MSG_DOWNLOAD_FINISHED;

            if (result.ret == FileOperationHelper.RET_AFTER_FAIL) {
                if (mStopped) {
                    sendMessageByHandler(msg, OP_CANCELED, 0, list);
                } else {
                    sendMessageByHandler(msg, OP_FAILED, 0, list);
                }
            } else if (result.ret == FileOperationHelper.RET_AFTER_SUCCESS) {
                trans.put(result.desPath, null);
                sendMessageByHandler(msg, OP_SUCCESSED, mCurrentTaskInfo.finished, trans);
            }

        } else if (op == FILE_OP_NEWFOLDER) {

            int ret = -1;
            switch (mModuleType) {
                case DropBox:
                    ret = DropBoxFileOperationHelper.getInstance().createDropBoxDir(list.get(0));
                    break;
                case UDisk:
                    ret = FileOperationHelper.getInstance().createUdiskDir(list.get(0));
                    DMSdk.getInstance().syncSystem();
                    break;
            }
            msg = MSG_NEWFOLDER_FINISHED;
            if (ret == FileOperationHelper.RET_AFTER_SUCCESS) {
                sendMessageByHandler(msg, OP_SUCCESSED, 0, trans);
            } else if (ret == FileOperationHelper.RET_AFTER_FAIL) {
                sendMessageByHandler(msg, OP_FAILED, 0, list);
            }
        } else if (op == FILE_OP_UPLOAD) {
            OperationResult result = null;
            switch (mModuleType) {
                case DropBox:
                    result = DropBoxFileOperationHelper.getInstance().uploadFile(list, newName);
                    break;
                case UDisk:
                    result = FileOperationHelper.getInstance().uploadFile(list, newName);
                    DMSdk.getInstance().syncSystem();
                    break;
            }
            msg = MSG_UPLOAD_FINISHED;
            if (result.ret == FileOperationHelper.RET_AFTER_FAIL) {
                if (mStopped) {
                    sendMessageByHandler(msg, OP_CANCELED, 0, list);
                } else {
                    sendMessageByHandler(msg, OP_FAILED, 0, list);
                }
            } else if (result.ret == FileOperationHelper.RET_AFTER_SUCCESS) {
                trans.put(result.desPath, null);
                sendMessageByHandler(msg, OP_SUCCESSED, mCurrentTaskInfo.finished, trans);
            }

        } else if (op == FILE_OP_COPYTO) {
            OperationResult result = null;
            switch (mModuleType) {
                case DropBox:
                    result = DropBoxFileOperationHelper.getInstance().copyTo(list, newName);
                    break;
                case UDisk:
                    result = FileOperationHelper.getInstance().copyTo(list, newName);
                    break;
            }
            msg = MSG_COPY_FINISHED;

            if (result.ret == FileOperationHelper.RET_AFTER_FAIL) {
                if (mStopped) {
                    sendMessageByHandler(msg, OP_CANCELED, 0, list);
                } else {
                    sendMessageByHandler(msg, OP_FAILED, 0, list);
                }
            } else if (result.ret == FileOperationHelper.RET_AFTER_SUCCESS) {
                trans.put(result.desPath, null);
                sendMessageByHandler(msg, OP_SUCCESSED, mCurrentTaskInfo.finished, trans);
            }

        } else if (op == FILE_OP_DECRYPTED) {
            OperationResult result = FileOperationHelper.getInstance().decryptedTo(list, newName);

            msg = MSG_DECRYPTED_FINISHED;

            if (result.ret == FileOperationHelper.RET_AFTER_FAIL) {
                if (mStopped) {
                    sendMessageByHandler(msg, OP_CANCELED, 0, list);
                } else {
                    sendMessageByHandler(msg, OP_FAILED, 0, list);
                }
            } else if (result.ret == FileOperationHelper.RET_AFTER_SUCCESS) {
                trans.put(result.desPath, null);
                sendMessageByHandler(msg, OP_SUCCESSED, mCurrentTaskInfo.finished, trans);
            }

        } else if (op == FILE_OP_ENCRYPTED) {
            OperationResult result = FileOperationHelper.getInstance().encryptedTo(list, newName);
            msg = MSG_ENCRYPTED_FINISHED;

            if (result.ret == FileOperationHelper.RET_AFTER_FAIL) {
                if (mStopped) {
                    sendMessageByHandler(msg, OP_CANCELED, 0, list);
                } else {
                    sendMessageByHandler(msg, OP_FAILED, 0, list);
                }
            } else if (result.ret == FileOperationHelper.RET_AFTER_SUCCESS) {
                trans.put(result.desPath, result.list);
                sendMessageByHandler(msg, OP_SUCCESSED, mCurrentTaskInfo.finished, trans);
            }
        } else if (op == FILE_OP_ENCRYPRED_DELETE) {

            int ret = FileOperationHelper.getInstance().enctypredDelete(list);
            DMSdk.getInstance().syncSystem();
            if (ret == 0) {
                sendMessageByHandler(MSG_ENCRYPTED_DELETE_FINISHED, OP_SUCCESSED, 0, trans);
            } else {
                if (mStopped) {
                    sendMessageByHandler(MSG_ENCRYPTED_DELETE_FINISHED, OP_CANCELED, 0, list);
                } else {
                    sendMessageByHandler(MSG_ENCRYPTED_DELETE_FINISHED, OP_FAILED, 0, ret);
                }
            }
        } else {
            return;
        }

        mStopped = false;
        //用户停止后动作处理完，标志要重置为false，
        FileOperationHelper.isUserStop = false;
        //selectedList = null;
        if (mNewFolder != null) {
            mNewFolder = null;
        }
//		stopSelf();

    }


    private class FileOperationThread extends Thread {
        private List<DMFile> mList;
        private int mOp;
        private boolean mType;
        private String uDiskName;
        private String newName;
        private ModuleType mModuleType;

        public FileOperationThread(int op, boolean type, String uDiskName, List<DMFile> list, String newName, ModuleType mModuleType) {
            mOp = op;
            mType = type;
            mList = list;
            this.uDiskName = uDiskName;
            this.newName = newName;
            this.mModuleType = mModuleType;
        }

        public void run() {
            if (mList == null)
                return;
            doFileOperation(mOp, mType, uDiskName, mList, newName, mModuleType);
        }
    }

    ;

    private static final int NOTIIFY_ID_FILE_FINISHED = 1;

}
