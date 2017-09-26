package com.dmsys.airdiskpro.setting;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dm.xunlei.udisk.Network.Dialog.AlertDmDialogDefault;
import com.dm.xunlei.udisk.Network.Dialog.AlertDmDialogDefault.OnInputListener;
import com.dm.xunlei.udisk.Network.Dialog.AlertDmDialogDefault.OnTipsListener;
import com.dm.xunlei.udisk.Network.View.CustomButtonView1;
import com.dm.xunlei.udisk.Network.View.CustomButtonView1.onToogleClickListener;
import com.dm.xunlei.udisk.Network.View.EditTextButtonView;
import com.dm.xunlei.udisk.Network.View.EditTextButtonView.onEditTextContentListener;
import com.dmsys.mainbusiness.R;

public abstract class BaseOnOffSettingFragment extends Fragment implements
        OnClickListener {

    public interface OnStartResetPasswordViewListener {
        void onStartResetPasswordView();
    }

    OnStartResetPasswordViewListener listener;
    Activity activity;

    View parent;
    RelativeLayout rlyt_vault_password_reset, rl_errornote;
    LinearLayout llyt_vault_reset_password, llyt_vault_create_password,
            tmp_dialog_llyt_tips, tmp_dialog_rlyt_error;
    CustomButtonView1 cbv_vault_switch;
    TextView tv_vault_switch_tips, tv_errornote, tmp_dialog_tv_error_msg,
            tmp_dialog_tv_password_tips_content, tmp_dialog_tv_password_tips;
    EditTextButtonView etbv_edittext, etbv_password, etbv_password_tips;
    Button btn_vault_save, okButton;
    public Dialog promptDialog, adPassword;
    EditTextButtonView mETBV;




    /*
     * Deprecated on API 23
     * Use onAttachToContext instead
     */
    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        onAttachToContext(activity);
    }

    private void onAttachToContext(Activity context) {

        activity = context;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        parent = inflater.inflate(R.layout.fragment_vault, container, false);

        return parent;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onActivityCreated(savedInstanceState);

        initViews();

        // loadData();
    }

    private void initViews() {

        rlyt_vault_password_reset = (RelativeLayout) parent
                .findViewById(R.id.rlyt_vault_password_reset);
        rl_errornote = (RelativeLayout) parent.findViewById(R.id.rl_errornote);
        llyt_vault_reset_password = (LinearLayout) parent
                .findViewById(R.id.llyt_vault_reset_password);
        llyt_vault_create_password = (LinearLayout) parent
                .findViewById(R.id.llyt_vault_create_password);
        cbv_vault_switch = (CustomButtonView1) parent
                .findViewById(R.id.cbv_vault_switch);
        rlyt_vault_password_reset = (RelativeLayout) parent
                .findViewById(R.id.rlyt_vault_password_reset);

        tv_vault_switch_tips = (TextView) parent
                .findViewById(R.id.tv_vault_switch_tips);
        tv_errornote = (TextView) parent.findViewById(R.id.tv_errornote);
        etbv_edittext = (EditTextButtonView) parent
                .findViewById(R.id.etbv_edittext);
        etbv_password = (EditTextButtonView) parent
                .findViewById(R.id.etbv_password);
        etbv_password_tips = (EditTextButtonView) parent
                .findViewById(R.id.etbv_password_tips);
        cbv_vault_switch.setTitle(getString(R.string.DM_Set_SecureVault));
        etbv_edittext.setStyle(EditTextButtonView.OptionalPasswordStyle);
        etbv_edittext
                .setEditTextHint(getString(R.string.DM_Set_SecureVault_Password_Enter));

        etbv_password.setStyle(EditTextButtonView.OptionalPasswordStyle);
        etbv_password_tips.setStyle(EditTextButtonView.EditTextStyle);
        etbv_password
                .setEditTextHint(getString(R.string.DM_Set_SecureVault_Password_Again));

        btn_vault_save = (Button) parent.findViewById(R.id.btn_vault_save);
        llyt_vault_create_password.setVisibility(View.GONE);
        cbv_vault_switch.setToToogle();
        cbv_vault_switch.setOnToogleClickListener(new onToogleClickListener() {

            @Override
            public void onClick(boolean toogleOn) {

                if (!toogleOn
                        && llyt_vault_create_password.getVisibility() == View.VISIBLE) {
                    // 用户还没有开启保险库
                    llyt_vault_reset_password.setVisibility(View.GONE);
                    llyt_vault_create_password.setVisibility(View.GONE);
                    tv_vault_switch_tips.setVisibility(View.VISIBLE);
                    tv_vault_switch_tips
                            .setText(getString(R.string.DM_Set_SecureVault_Disabled_Caption));
                    cbv_vault_switch.setToogleState(toogleOn, false);
                    //把软键盘关闭
                    etbv_edittext.hideKeyboard();

                } else {
                    cbv_vault_switch.setToogleState(!toogleOn, false);
                    showVaultSwitchDialog(toogleOn);
                }
            }
        });
        btn_vault_save.setOnClickListener(this);
        llyt_vault_reset_password.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        int i = v.getId();
        if (i == R.id.btn_vault_save) {
            createPassword(etbv_edittext.getContentText(),
                    etbv_password.getContentText(),
                    etbv_password_tips.getContentText());

        } else if (i == R.id.llyt_vault_reset_password) {
            if (listener != null) {
                listener.onStartResetPasswordView();
            }


        } else {
        }
    }

    @Override
    public void onDestroyView() {
        // TODO Auto-generated method stub
        if (promptDialog != null && promptDialog.isShowing()) {
            promptDialog.dismiss();
            promptDialog = null;
        }
        if (adPassword != null && adPassword.isShowing()) {
            adPassword.dismiss();
            adPassword = null;
        }

        super.onDestroyView();
    }

    /**
     * 以下都是 Dialog 的代码
     *
     * @param
     */

    private void showCreatePasswordView() {
        tv_vault_switch_tips.setVisibility(View.GONE);
        llyt_vault_reset_password.setVisibility(View.GONE);
        llyt_vault_create_password.setVisibility(View.VISIBLE);
    }

    protected void showVaultSwitchDialog(final boolean flag) {

        String continueStr = getString(R.string.DM_SetUI_Confirm);
        String cancelStr = getString(R.string.DM_SetUI_Ap_Info_Cancel);
        String[] array = new String[]{cancelStr, continueStr};

        String message = null;
        if (flag) {
            message = getString(R.string.DM_Set_SecureVault_enabled_Note);
        } else {
            message = getString(R.string.DM_Set_SecureVault_Disabled_Empty_Note);
        }
        getString(R.string.DM_Set_SecureVault_enabled_Note);

        promptDialog = AlertDmDialogDefault.prompt(activity, message, null,
                new AlertDmDialogDefault.OnPromptListener() {

                    @Override
                    public void onPromptPositive() {
                        // TODO Auto-generated method stub
                        promptDialog.cancel();
                        if (flag) {
                            cbv_vault_switch.setToogleState(true, false);
                            showCreatePasswordView();
                            etbv_edittext.beFocus();
                            etbv_edittext.pullUpKeyboard();


                        } else {
                            // 检查保险库是否为空
                            checkIsEmpty();
                        }
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

    public void showCanNoCloseVaultDialog() {
        String cancelStr = getString(R.string.DM_Control_Know);
        String[] array = new String[]{cancelStr};

        String message = getString(R.string.DM_Set_SecureVault_Disabled_Not_Empty_Note);

        promptDialog = AlertDmDialogDefault.prompt(activity, message, null,
                new AlertDmDialogDefault.OnPromptListener() {

                    @Override
                    public void onPromptPositive() {
                        // TODO Auto-generated method stub
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
                }, array, 1);
    }

    public void showRealCloseVaultDialog() {
        adPassword = AlertDmDialogDefault.popPasswordWindos3(activity,
                getString(R.string.DM_Set_SecureVault_Accesss_Enter),
                new OnInputListener() {

                    @Override
                    public void onInputPositive(DialogInterface dialog,
                                                String content) {
                        // TODO Auto-generated method stub
                        close(content);
                    }

                    @Override
                    public void onInputNegative(DialogInterface dialog) {
                        adPassword.cancel();
                    }
                }, null, new OnTipsListener() {

                    @Override
                    public void onTips(LinearLayout l1, LinearLayout l2,
                                       TextView tv, TextView tv1, TextView tv2) {
                        // TODO Auto-generated method stub
                        tmp_dialog_rlyt_error = l1;
                        tmp_dialog_llyt_tips = l2;
                        tmp_dialog_tv_error_msg = tv;
                        tmp_dialog_tv_password_tips_content = tv1;
                        tmp_dialog_tv_password_tips = tv2;
                    }
                });

        okButton = (Button) adPassword.findViewById(R.id.dialog_ok_two);
        okButton.setEnabled(false);
        mETBV = (EditTextButtonView) adPassword
                .findViewById(R.id.etbv_dialog_password);

        mETBV.beFocus();
        mETBV.pullUpKeyboard();

        mETBV.getEditTextView().setFilters(
                new InputFilter[]{new InputFilter.LengthFilter(32)});
        mETBV.setOnEditTextContentListener(new onEditTextContentListener() {
            @Override
            public void onChange(String curContent) {
                // TODO Auto-generated method stub
                validate(mETBV.getContentText().toString());
            }
        });
        tmp_dialog_tv_password_tips.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                getTips();
            }
        });

    }

    private void validate(String str) {
        if ((str != null && str.length() == 0) || (str.length() < 8)) {
            okButton.setEnabled(false);
        } else
            okButton.setEnabled(true);

    }

    public abstract void createPassword(String a, String b, String c);

    public abstract void checkIsEmpty();

    public abstract void close(String c);

    public abstract void getTips();

}
