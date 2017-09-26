package com.dmsys.airdiskpro.ui;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.widget.Toast;

import com.dm.baselib.BaseValue;
import com.dm.share.IntentShare;
import com.dmsys.airdiskpro.filemanager.FileOperationHelper;
import com.dmsys.airdiskpro.filemanager.FileOperationService;
import com.dmsys.airdiskpro.filemanager.FileOperationService.SameNameInfo;
import com.dmsys.airdiskpro.utils.ConvertUtil;
import com.dmsys.airdiskpro.utils.FileInfoUtils;
import com.dmsys.airdiskpro.view.MessageDialog;
import com.dmsys.airdiskpro.view.MsgWidthCheckBoxDialog;
import com.dmsys.airdiskpro.view.ProgressDialog;
import com.dmsys.airdiskpro.view.UDiskBaseDialog;
import com.dmsys.airdiskpro.view.UDiskEditTextDialog;
import com.dmsys.dmsdk.DMSdk;
import com.dmsys.dmsdk.model.DMFile;
import com.dmsys.dmsdk.model.DMFileCategoryType;
import com.dmsys.mainbusiness.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class BaseActionFragment extends Fragment {

    private Context mContext;
    private FileOperationService mOpService;
    private Handler mHandler;
    public ProgressDialog mProgressDialog;
    public SharedPreferences mPreferences;
    public Editor mEditor;
    private String PREFERENCE_NAME = "PREFERENCE_NAME";
    private String KEY_SHOWNOMORE = "SHOWNOMORE";

    private boolean mCancelCache = false;
    public int DOWN_TO_OPEN = 0;
    public int DOWN_TO_SHARE = 1;
    boolean mIsBound;

    public void init(Context context, Handler mHandler) {
        this.mContext = context;
        this.mHandler = mHandler;
        this.mPreferences = getActivity().getSharedPreferences(PREFERENCE_NAME,
                Context.MODE_PRIVATE);
        this.mEditor = mPreferences.edit();
        mIsBound = true;
        Intent intent = new Intent(getActivity(), FileOperationService.class);
        mContext.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // TODO Auto-generated method stub
            mOpService = ((FileOperationService.OperationBinder) service)
                    .getService();
            mOpService.setHandler(mHandler);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            // TODO Auto-generated method stub
            mOpService.setHandler(null);
            mOpService = null;
        }
    };

    public void unInit() {
        if (mIsBound) {
            mContext.unbindService(mConnection);
            mIsBound = false;
        }
    }

    public void doFileOperation(int op, List<DMFile> list) {

        FileOperationService.selectedList = list;

        Resources resource = this.getResources();

        Intent intent = new Intent(mContext, FileOperationService.class);
        Bundle bundle = new Bundle();
        bundle.putInt(FileOperationService.EXTRA_OP, op);
        intent.putExtra(FileOperationService.EXTRA_BUNDLE, bundle);
        mContext.startService(intent);

        String title = opCode2String(op);

        boolean isShowAllConfig = (op != FileOperationService.FILE_OP_DELETE);

        mProgressDialog = new ProgressDialog.Builder(mContext)
                .setNumberVisiable(true)
                .setNameVisiable(isShowAllConfig)
                .setSpeedVisiable(isShowAllConfig)
                .setTimeVisiable(isShowAllConfig)
                .setSum("(" + list.size() + ")")
                .setTitler(title)
                .setLeftNumber(String.valueOf(list.size()))
                .setFileName(
                        getString(R.string.DM_File_Operate_Remain_Time_unknow))
                .setLeftTime(
                        getString(R.string.DM_File_Operate_Remain_Time_calc))
                .setSpeed(getString(R.string.DM_File_Operate_Remain_Time_calc))
                .setLeftBtn(resource.getString(R.string.DM_Control_Cancel),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                // TODO Auto-generated method stub
                                mOpService.cancelCurOperation();
                                FileOperationHelper.getInstance().stop();
                                Toast.makeText(mContext,
                                        R.string.DM_Remind_Operate_Stop,
                                        Toast.LENGTH_SHORT).show();
                            }
                        }).build();

        mProgressDialog.show();
    }

    public void doFileOperation(int op, boolean type, List<DMFile> list) {

        FileOperationService.selectedList = list;

        Resources resource = this.getResources();

        Intent intent = new Intent(mContext, FileOperationService.class);
        Bundle bundle = new Bundle();
        bundle.putInt(FileOperationService.EXTRA_OP, op);
        bundle.putBoolean(FileOperationService.EXTRA_TYPE, type);
        intent.putExtra(FileOperationService.EXTRA_BUNDLE, bundle);
        mContext.startService(intent);

        String message = "";
        String title = opCode2String(op);
        String opName = opCode2String(op);
        mProgressDialog = new ProgressDialog(mContext);
        mProgressDialog.setTitleContent(title);
        mProgressDialog.setNumber("(" + list.size() + ")");
        mProgressDialog.setLeftBtn(
                resource.getString(R.string.DM_Control_Cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        mOpService.cancelCurOperation();
                        System.out
                                .println("ppppppp mProgressDialog mStopped:" + true);
                        FileOperationHelper.getInstance().stop();
                        Toast.makeText(mContext,
                                R.string.DM_Remind_Operate_Stop,
                                Toast.LENGTH_SHORT).show();
                    }
                });

        mProgressDialog.show();

    }

    public void doFileOperation(final int op, List<DMFile> list, String desPath) {

        FileOperationService.selectedList = list;

        Intent intent = new Intent(mContext, FileOperationService.class);
        Bundle bundle = new Bundle();
        bundle.putInt(FileOperationService.EXTRA_OP, op);
        bundle.putString(FileOperationService.Rename_Key, desPath);
        intent.putExtra(FileOperationService.EXTRA_BUNDLE, bundle);
        mContext.startService(intent);

        boolean isShowAllConfig = true;
        if (op == FileOperationService.FILE_OP_COPYTO
                || op == FileOperationService.FILE_OP_DECRYPTED
                || op == FileOperationService.FILE_OP_ENCRYPTED) {
            isShowAllConfig = false;
        }

        String title = opCode2String(op);

        mProgressDialog = new ProgressDialog.Builder(mContext)
                .setNumberVisiable(true)
                .setNameVisiable(isShowAllConfig)
                .setSpeedVisiable(isShowAllConfig)
                .setTimeVisiable(isShowAllConfig)
                .setSum("(" + list.size() + ")")
                .setTitler(title)
                .setLeftBtn(getString(R.string.DM_Control_Cancel),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                // TODO Auto-generated method stub
                                mOpService.cancelCurOperation();
                                FileOperationHelper.getInstance().stop();
                                Toast.makeText(mContext,
                                        R.string.DM_Remind_Operate_Stop,
                                        Toast.LENGTH_SHORT).show();

                                if (op == FileOperationService.FILE_OP_COPYTO) {
                                    getActivity().finish();
                                }

                            }
                        }).build();

        mProgressDialog.show();

    }

    public void doNewFolderOperation(int op, DMFile file) {

        Intent intent = new Intent(getActivity(), FileOperationService.class);
        Bundle bundle = new Bundle();
        bundle.putInt(FileOperationService.EXTRA_OP, op);
        bundle.putSerializable(FileOperationService.EXTRA_NEWFOLDER, file);
        intent.putExtra(FileOperationService.EXTRA_BUNDLE, bundle);
        mContext.startService(intent);

    }

    public void onSameFile(final SameNameInfo info) {
        final MsgWidthCheckBoxDialog dialog = new MsgWidthCheckBoxDialog(
                mContext);
        dialog.setCheckText(getString(R.string.DM_Task_Confirm_Operate));
        dialog.setMessage(String.format(
                getString(R.string.DM_Remind_Operate_SameFile), info.name));
        dialog.setTitleContent(getString(R.string.DM_Remind_Tips));
        dialog.setLeftBtn(getString(R.string.DM_Task_Confirm_Rename),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dlg, int which) {
                        // TODO Auto-generated method stub
                        if (dialog.getChecked()) {
                            info.ret = -FileOperationHelper.OP_RENAME;
                        } else {
                            info.ret = FileOperationHelper.OP_RENAME;
                        }
                        info.semp.release();
                    }
                });

        dialog.setRightBtn(getString(R.string.DM_Task_Confirm_Jump),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dlg, int which) {
                        // TODO Auto-generated method stub
                        // // System.out.println("exist ret0:"+info.ret);
                        if (dialog.getChecked()) {
                            info.ret = -FileOperationHelper.OP_SKIP;
                        } else {
                            info.ret = FileOperationHelper.OP_SKIP;
                        }

                        info.semp.release();
                    }
                });

        dialog.show();
    }

    public void onSuccess(final String strOp, String dstPath,
                          List<DMFile> list, int size) {
        if (strOp.equals(getString(R.string.DM_Task_Delete))
                || strOp.equals(getString(R.string.DM_Task_Rename))) {
            return;
        }

        boolean show = mPreferences.getBoolean(KEY_SHOWNOMORE, true);
        if (!show) {
            if (strOp.equals(getString(R.string.DM_Task_Copy))
                    || strOp.equals(getString(R.string.DM_Access_Vault_Decrypt_Note_Title))) {
                getActivity().finish();
            }
            return;
        }

        final MessageDialog dialog = new MessageDialog(mContext,
                UDiskBaseDialog.TYPE_ONE_BTN);
        dialog.setTitleContent(opToString(strOp));

        String message = "";
        if (strOp.equals(getString(R.string.DM_Task_Download))) {
            message = getString(R.string.DM_Remind_Operate_Download_Done);
            message = String.format(message, String.valueOf(size), dstPath);
        } else if (strOp.equals(getString(R.string.DM_Task_Copy))) {
            message = getString(R.string.DM_Remind_Operate_Copy_Done);
            message = String.format(message, String.valueOf(size), dstPath);
        } else if (strOp
                .equals(getString(R.string.DM_Access_Vault_Decrypt_Note_Title))) {
            message = getString(R.string.DM_Access_Vault_Decrypt_Success);
            message = String.format(message, String.valueOf(size), dstPath);
            dialog.setTitleContent(getString(R.string.DM_Access_Vault_Decrypt_Success_Note_Title));
        } else if (strOp
                .equals(getString(R.string.DM_Access_Vault_Encrypt_Note_Title))) {

            //当有部分过滤了的文件，显示另一个对话框
            if (list.size() > 0) {

                message = String.format(getString(R.string.DM_Encrypt_file_isexit),String.valueOf(list.size()));
                StringBuffer content = new StringBuffer("");
                for (DMFile d : list) {
                    content.append(d.getPath()).append("\n");
                }
                dialog.setSubContent(content.toString());
            } else {

                message = String.format( getString(R.string.DM_Access_Vault_Encrypt_Suceess_Caption), String.valueOf(size));
            }

            dialog.setTitleContent(getString(R.string.DM_Access_Vault_Encrypt_Suceess_Note_Title));
        }


        dialog.setMessage(message);
        dialog.setLeftBtn(getString(R.string.DM_Control_Definite),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dlg, int which) {
                        // TODO Auto-generated method stub

                        if (strOp.equals(getString(R.string.DM_Task_Copy))
                                || strOp.equals(getString(R.string.DM_Access_Vault_Decrypt_Note_Title))) {
                            getActivity().finish();
                        }


                    }
                });

        dialog.show();
    }

    private String opToString(String strOp) {
        String retString = null;
        if (strOp.equals(getString(R.string.DM_Task_Download))) {
            retString = getString(R.string.DM_Remind_Operate_Download_Success);
        } else if (strOp.equals(getString(R.string.DM_Task_Copy))) {
            retString = getString(R.string.DM_Remind_Operate_Copy_Success);
        } else if (strOp
                .equals(getString(R.string.DM_Access_Vault_Decrypt_Note_Title))) {
            retString = getString(R.string.DM_Access_Vault_Decrypt_Success_Note_Title);
        } else if (strOp
                .equals(getString(R.string.DM_Access_Vault_Encrypt_Note_Title))) {
            retString = getString(R.string.DM_Access_Vault_Encrypt_Suceess_Note_Title);
        }
        return retString;
    }


    public void onError(int err, List<DMFile> files) {



            MessageDialog builder = new MessageDialog(mContext,
                    UDiskBaseDialog.TYPE_ONE_BTN);
            builder.setTitleContent(getString(R.string.DM_Fileexplore_Operation_Warn_Error));
            builder.setLeftBtn(getString(R.string.DM_SetUI_Dialog_Button_Sure),
                    null);
            builder.setMessage(strerr(getActivity(), err));
            builder.show();


    }

    private String strerr(Context context, int err) {
        String str = null;
        switch (err) {
            case FileOperationHelper.ERROR_UDISK_NOT_ENOUGH_SPACE:
                str = String
                        .format(context
                                        .getString(R.string.DM_Fileexplore_Operation_Warn_Airdisk_No_Space),
                                ConvertUtil.convertFileSize(
                                        BaseValue.taskTotalSize, 2), ConvertUtil
                                        .convertFileSize(BaseValue.diskFreeSize, 2));
                break;
            case FileOperationHelper.ERROR_PHONE_NOT_ENOUGH_SPACE:
                str = context.getString(R.string.DM_Remind_Operate_Local_NoSize);
                break;
            case FileOperationHelper.ERROR_NOT_CONNECTED:
                str = context
                        .getString(R.string.DM_Fileexplore_Operation_Warn_Connect_Fail);
                break;
            case FileOperationHelper.ERROR_NOT_FOUND_STORAGE:
                str = context.getString(R.string.DM_Remind_Operate_No_Disk);
                break;
            default:
                break;
        }
        return str;
    }

    // 复写一个
    public String opCode2String(int op) {
        switch (op) {
            case FileOperationService.FILE_OP_DOWNLOAD:
                return getString(R.string.DM_Task_Download_Mobile);
            case FileOperationService.FILE_OP_DELETE:
                return getString(R.string.DM_Task_Delete);
            case FileOperationService.FILE_OP_UPLOAD:
                return getString(R.string.DM_Task_upload);
            case FileOperationService.FILE_OP_COPYTO:
                return getString(R.string.DM_Task_Copy);
            case FileOperationService.FILE_OP_DECRYPTED:
                return getString(R.string.DM_Access_Vault_Decrypt_Note_Title);
            case FileOperationService.FILE_OP_ENCRYPTED:
                return getString(R.string.DM_Access_Vault_Encrypt_Note_Title);
        }
        return null;
    }

    public void downloadFileToDO(Context context, DMFile file, int type) {
        mCancelCache = false;

        final ProgressDialog dialog = new ProgressDialog(context);
        dialog.setProgress(0);
        dialog.setMessage(context
                .getString(R.string.DM_Fileexplore_Loading_File));
        dialog.setTitleContent(context.getString(R.string.DM_Task_Download));
        dialog.setLeftBtn(context.getString(R.string.DM_Control_Cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        mCancelCache = true;
                    }
                });
        doDownload(context, file, dialog, type);
        dialog.show();
    }

    private double mProgress;

    public void doDownload(final Context context, final DMFile file,
                           final ProgressDialog dialog, final int type) {

        File directory = new File(FileOperationHelper.getInstance()
                .getCachePath());
        if (!directory.exists()) {
            directory.mkdir();
        }

        final File dstFile = new File(FileOperationHelper.getInstance()
                .getCachePath(), file.getName());
        if (dstFile.exists()) {

            if (dstFile.length() < file.mSize) {
                dstFile.delete();
            } else {
                mHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        dialog.setProgress(100);
                        dialog.dismiss();
                    }

                });

                if (type == DOWN_TO_OPEN) {
                    Uri uri = Uri.fromFile(dstFile);
                    FileOperationHelper.getInstance().openDefault(uri,
                            file.mType);
                } else {
                    int shareType = getIntentShareType(file.getType());
                    IntentShare.shareFile(dstFile.getPath(), shareType,
                            mContext);
                }
                return;
            }
        }

        FileOperationHelper.getInstance().doDownload(file, dstFile.getParent(),
                new FileOperationHelper.ProgressListener() {

                    @Override
                    public boolean onProgressChange(final double progress) {
                        // TODO Auto-generated method stub
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
                                    if (type == DOWN_TO_OPEN) {
                                        Uri uri = Uri.fromFile(dstFile);
                                        FileOperationHelper.getInstance()
                                                .openDefault(uri, file.mType);
                                    } else {
                                        int shareType = getIntentShareType(file
                                                .getType());
                                        IntentShare.shareFile(
                                                dstFile.getPath(), shareType,
                                                mContext);
                                    }

                                } else {
                                    Toast.makeText(
                                            mContext,
                                            mContext.getString(R.string.DM_Disk_Buffer_Fail),
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                        return 0;
                    }
                });
    }

    public void shareFile(final DMFile xlfile) {
        if (!shareFormatSupport(xlfile.getType())) {
            Toast.makeText(mContext,
                    mContext.getString(R.string.DM_Share_Format_Not_Support),
                    Toast.LENGTH_SHORT).show();
            return;
        }
        if (xlfile.mLocation == DMFile.LOCATION_LOCAL) {
            shareLocFile(xlfile);
        } else {
            shareUdiskFile(xlfile);
        }

    }

    public void shareLocFile(DMFile xlfile) {

        int shareType = getIntentShareType(xlfile.getType());
        IntentShare.shareFile(xlfile.mPath, shareType, mContext);
    }

    public void shareUdiskFile(DMFile xlfile) {
        downloadFileToDO(mContext, xlfile, DOWN_TO_SHARE);
    }

    public int getIntentShareType(DMFileCategoryType EType) {

        int shareType;
        if (EType == DMFileCategoryType.E_VIDEO_CATEGORY) {
            shareType = IntentShare.VIDEO;
        } else if (EType == DMFileCategoryType.E_PICTURE_CATEGORY) {
            shareType = IntentShare.IMAGE;
        } else if (EType == DMFileCategoryType.E_BOOK_CATEGORY) {
            shareType = IntentShare.TEXT;
        } else if (EType == DMFileCategoryType.E_MUSIC_CATEGORY) {
            shareType = IntentShare.MUSIC;
        } else {
            shareType = IntentShare.OTHER;
        }
        return shareType;
    }

    public boolean shareFormatSupport(DMFileCategoryType EType) {
        if (EType == DMFileCategoryType.E_VIDEO_CATEGORY) {
            return true;
        } else if (EType == DMFileCategoryType.E_PICTURE_CATEGORY) {
            return true;
        } else if (EType == DMFileCategoryType.E_BOOK_CATEGORY) {
            return false;
        } else if (EType == DMFileCategoryType.E_MUSIC_CATEGORY) {
            return true;
        } else {
            return false;
        }
    }

    public void renameFile(final DMFile file) {

        final String namePart = FileInfoUtils.mainName(file.getName());
        final String extendPart = FileInfoUtils.extension(file.getName());
        final UDiskEditTextDialog editTextDialog = new UDiskEditTextDialog(
                getActivity(), UDiskEditTextDialog.TYPE_TWO_BTN);
        editTextDialog.setTitleContent(getActivity().getString(
                R.string.DM_Task_Rename));
        editTextDialog.setLeftBtn(getString(R.string.DM_Control_Cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        editTextDialog.releaseDialog();
                    }
                });

        editTextDialog.setRightBtn(getString(R.string.DM_Control_Definite),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        String newNamePart = editTextDialog.getEditContent();
                        String newName = null;
                        if (file.isDir || extendPart.equals("")) {
                            newName = newNamePart;
                        } else {
                            newName = newNamePart + "." + extendPart;
                        }

                        if (newName == null || newName.equals("")
                                || newNamePart == null
                                || newNamePart.equals("")) {
                            editTextDialog
                                    .showWarnText(R.string.DM_More_Rename_No_Enpty);
                            editTextDialog.setEditContent(namePart);
                            editTextDialog.lockDialog();
                            return;
                        } else if (!FileInfoUtils.isValidFileName(newName)) {
                            editTextDialog
                                    .showWarnText(R.string.DM_More_Rename_Name_Error);
                            editTextDialog.lockDialog();
                            return;
                        } else if (file.getName().equals(newName)) {
                            editTextDialog
                                    .showWarnText(R.string.DM_More_Rename_Placeholder);
                            editTextDialog.lockDialog();
                            return;
                        } else if (DMSdk.getInstance().isExisted(
                                file.getParent() + File.separator + newName)) {
                            editTextDialog
                                    .showWarnText(R.string.DM_More_Rename_BeUsed);
                            editTextDialog.lockDialog();
                            return;
                        } else {
                            editTextDialog.releaseDialog();
                            ArrayList<DMFile> xlfileList = new ArrayList<>();
                            xlfileList.add(file);
                            newName = newName.trim();
                            doFileRenameOperation(
                                    FileOperationService.FILE_OP_RENAME,
                                    xlfileList, newName);
                        }
                    }
                });
        editTextDialog.setEditContent(namePart);
        editTextDialog.show();
        editTextDialog.getEditTextView().setFocusable(true);
        editTextDialog.getEditTextView().setFocusableInTouchMode(true);
        editTextDialog.getEditTextView().requestFocus();
        editTextDialog.getEditTextView().pullUpKeyboard();
    }

    public void doFileRenameOperation(int op, List<DMFile> list, String newName) {

        FileOperationService.selectedList = list;

        Intent intent = new Intent(getActivity(), FileOperationService.class);
        Bundle bundle = new Bundle();
        bundle.putInt(FileOperationService.EXTRA_OP, op);
        bundle.putString(FileOperationService.Rename_Key, newName);
        intent.putExtra(FileOperationService.EXTRA_BUNDLE, bundle);
        getActivity().startService(intent);

    }

    // private static final String LegalChar =
    // "~!@#$%^&*()_+-={}|[]\\:'\"<>?,/._0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLIMNOPQRSTUVWXYZ";
    private static final String LegalChar = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLIMNOPQRSTUVWXYZ(`-=[]\\;',./~!@#$%^&*()_+{}|:<>?\")";

    // ssid暂时不支持"这个字符
    private boolean hasIllegalChar(String str) {
        for (int i = 0; i < str.length(); i++) {
            if (("\"".indexOf(str.charAt(i)) >= 0)) {
                return true;
            }
        }
        return false;
    }


    private static final String STATE_SAVE_IS_HIDDEN = "STATE_SAVE_IS_HIDDEN";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            boolean isSupportHidden = savedInstanceState.getBoolean(STATE_SAVE_IS_HIDDEN);

            FragmentTransaction ft = getFragmentManager().beginTransaction();
            if (isSupportHidden) {
                ft.hide(this);
            } else {
                ft.show(this);
            }
            ft.commit();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_SAVE_IS_HIDDEN, isHidden());
    }


}
