package com.dmsys.airdiskpro.view;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dm.xunlei.udisk.Network.View.EditTextButtonView;
import com.dm.xunlei.udisk.Network.View.EditTextButtonView.onEditTextContentListener;
import com.dmsoftwareupgrade.util.UDiskBaseDialog;
import com.dmsys.airdiskpro.RxBus;
import com.dmsys.airdiskpro.event.DeviceValutEvent;
import com.dmsys.airdiskpro.utils.AndroidConfig;
import com.dmsys.airdiskpro.utils.FileInfoUtils;
import com.dmsys.dmsdk.DMSdk;
import com.dmsys.dmsdk.model.DMLoginVault;
import com.dmsys.dmsdk.model.DMRet;
import com.dmsys.dmsdk.model.DMVaultPwTips;
import com.dmsys.mainbusiness.R;

import java.util.concurrent.Callable;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class VaultPasswordDialog extends Activity implements
        View.OnClickListener {

    View tips;

    LinearLayout rl_errornote;
    LinearLayout llyt_tips;
    TextView tv_errornote, tv_tips, tx_password_tips, dialogTitle;
    EditTextButtonView etbv_dialog_password;
    Button button_ok, button_cancel;

    public final static String TypeFlag = "VaultPasswordDialogFlag";
    public final static String DeviceNameFlag = "VaultPasswordDialogDeviceNameFlag";
    public final static int FLAG_VALUT_PASSWORD = 0;
    public final static int FLAG_DEVICE_PASSWORD = 1;

    public int flag = FLAG_VALUT_PASSWORD;
    public String name = "";
    public Activity mActivity;
    public CompositeSubscription mSubscriptions = new CompositeSubscription();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.dm_lib_wifi_password_alertdialog3);
        mActivity = this;
        if (getIntent() != null) {
            flag = getIntent().getIntExtra(TypeFlag, FLAG_VALUT_PASSWORD);
            name = getIntent().getStringExtra(DeviceNameFlag);
        }

        setFinishOnTouchOutside(false);

        initView();
    }

    private void initView() {
        rl_errornote = (LinearLayout) findViewById(R.id.rl_errornote);
        llyt_tips = (LinearLayout) findViewById(R.id.llyt_tips);
        tv_errornote = (TextView) findViewById(R.id.tv_errornote);
        tv_tips = (TextView) findViewById(R.id.tv_tips);
        tx_password_tips = (TextView) findViewById(R.id.tx_password_tips);

        etbv_dialog_password = (EditTextButtonView) findViewById(R.id.etbv_dialog_password);
        etbv_dialog_password.setStyle(EditTextButtonView.OptionalPasswordStyle);

        dialogTitle = (TextView) findViewById(R.id.dialog_title);

        TextView tv_dialog = (TextView) findViewById(R.id.tv_dialog);

        button_ok = (Button) findViewById(R.id.dialog_ok_two);
        button_cancel = (Button) findViewById(R.id.dialog_cancel_two);

        button_ok.setOnClickListener(this);
        button_cancel.setOnClickListener(this);
        tx_password_tips.setOnClickListener(this);
        button_ok.setEnabled(false);
        tv_dialog.setVisibility(View.GONE);

        tx_password_tips.setVisibility(View.VISIBLE);
        rl_errornote.setVisibility(View.GONE);
        llyt_tips.setVisibility(View.GONE);

        etbv_dialog_password.getEditTextView().requestFocus();
        etbv_dialog_password.pullUpKeyboard();

        etbv_dialog_password.getEditTextView().setFilters(
                new InputFilter[]{new InputFilter.LengthFilter(32)});
        etbv_dialog_password
                .setOnEditTextContentListener(new onEditTextContentListener() {
                    @Override
                    public void onChange(String curContent) {
                        // TODO Auto-generated method stub
                        validate(etbv_dialog_password.getContentText()
                                .toString());
                    }
                });

        if (flag == FLAG_DEVICE_PASSWORD) {
            String message = String.format(
                    getString(R.string.DM_SetUI_Input_Password_Dialog), name);
            dialogTitle.setText(message);
            tx_password_tips
                    .setText(getString(R.string.DM_Access_Password_Forgot));
        } else {
            String message = String.format(
                    getString(R.string.DM_SetUI_Input_Password_Dialog), getString(R.string.DM_Set_SecureVault));
            dialogTitle.setText(message);

        }
        tx_password_tips.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG); // 下划线
        tx_password_tips.getPaint().setAntiAlias(true);

    }

    private void validate(String str) {
        if ((str != null && str.length() == 0) || (str.length() < 8)) {
            button_ok.setEnabled(false);
        } else
            button_ok.setEnabled(true);

    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        int i = v.getId();
        if (i == R.id.dialog_ok_two) {
            loginCheck(etbv_dialog_password.getContentText().toString());

        } else if (i == R.id.dialog_cancel_two) {
            if (flag == FLAG_DEVICE_PASSWORD) {
                RxBus.getDefault().send(new DeviceValutEvent(DeviceValutEvent.DEVICE_PASSWORD, -1));
            } else {
                RxBus.getDefault().send(new DeviceValutEvent(DeviceValutEvent.VAULT_PASSWORD, -1));
            }
            finish();

        } else if (i == R.id.tx_password_tips) {
            if (flag == FLAG_DEVICE_PASSWORD) {
                showTipDiaog();
            } else {
                getPasswordTips();
            }


        } else {
        }
    }

    MessageDialog dialog;

    protected void showTipDiaog() {
        // TODO Auto-generated method stub
        dialog = new MessageDialog(this, UDiskBaseDialog.TYPE_ONE_BTN);
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

    public void loginVault(final String password) {
        // TODO Auto-generated method stub


        Subscription subscription = Observable.fromCallable(new Callable<DMLoginVault>() {

            @Override
            public DMLoginVault call() {
                return DMSdk.getInstance().loginVault(password);
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<DMLoginVault>() {
                    @Override
                    public void call(DMLoginVault ret) {

                        if (ret != null && ret.errorCode == DMRet.ACTION_SUCCESS) {
                            // 跳去保险库
//                            Intent mIntent = new Intent(VaultPasswordDialog.this,
//                                    VaultAllFileActivity.class);
//                            VaultPasswordDialog.this.startActivity(mIntent);

                            finish();
                        } else {
                            // 显示密码错误
                            if (rl_errornote != null && tv_errornote != null) {
                                rl_errornote.setVisibility(View.VISIBLE);
                                tv_errornote
                                        .setText(getString(R.string.DM_Access_Password_Wrong));
                            }
                        }
                    }
                });


        mSubscriptions.add(subscription);


    }


    private void loginCheck(String password) {
        if (password == null || password.equals("")) {
            showPasswordError(getString(R.string.DM_More_Rename_No_Enpty));
        } else if (password.length() < 8) {
            showPasswordError(getString(R.string.DM_Error_PWD_Short));
        } else if (password.length() > 32) {
            showPasswordError(getString(R.string.DM_SetUI_Credentials_Password_Too_Long));
        } else if (!FileInfoUtils.isValidFileName(password)) {
            showPasswordError(getString(R.string.DM_More_Rename_Name_Error));
        } else {
            if (flag == FLAG_VALUT_PASSWORD) {
                loginVault(password);
            } else {
                loginDevice(password);
            }


        }
    }


    protected void loginDevice(final String password) {

        Subscription subscription = Observable.fromCallable(new Callable<Integer>() {

            @Override
            public Integer call() {
                return DMSdk.getInstance().loginDevice(password,
                        AndroidConfig.getPhoneModel(),
                        AndroidConfig.getIMEI(mActivity));
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer ret) {
                        if (ret == 0) {
                            RxBus.getDefault().send(new DeviceValutEvent(DeviceValutEvent.DEVICE_PASSWORD, ret));
                            finish();
                        } else if (ret == DMRet.ERROR_SESSTION_INVALID) {
                            showPasswordError(getString(R.string.DM_SetUI_Connect_Error_Authenticating));
                        } else {
                            RxBus.getDefault().send(new DeviceValutEvent(DeviceValutEvent.DEVICE_PASSWORD, ret));
                            finish();
                        }
                    }
                });


        mSubscriptions.add(subscription);


    }

    private void showPasswordError(String content) {

        tv_errornote.setText(content);
        rl_errornote.setVisibility(View.VISIBLE);
    }


    public void getPasswordTips() {
        // TODO Auto-generated method stub

        Subscription subscription = Observable.fromCallable(new Callable<DMVaultPwTips>() {

            @Override
            public DMVaultPwTips call() {
                return DMSdk.getInstance().getPwTips();
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<DMVaultPwTips>() {
                    @Override
                    public void call(DMVaultPwTips ret) {

                        tx_password_tips.setVisibility(View.GONE);
                        llyt_tips.setVisibility(View.VISIBLE);
                        if (ret != null && ret.errorCode == DMRet.ACTION_SUCCESS) {
                            tv_tips.setText(ret.tips != null ? ret.tips : "");
                            tv_tips.setVisibility(View.VISIBLE);
                        } else {
                            tv_tips.setVisibility(View.GONE);
                        }
                    }
                });


        mSubscriptions.add(subscription);


    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
            dialog = null;
        }
        mSubscriptions.unsubscribe();
        super.onDestroy();
    }

}
