package com.dmsys.airdiskpro.setting;

import android.annotation.SuppressLint;
import android.view.View;
import android.widget.Toast;

import com.dmsys.airdiskpro.setting.DeviceVaultContract.IVaultPresenter;
import com.dmsys.mainbusiness.R;

public class VaultSettingFragment extends BaseOnOffSettingFragment implements
		DeviceVaultContract.IVaultView {

	IVaultPresenter mPresenter;
	

	public VaultSettingFragment() {
		super();
		// TODO Auto-generated constructor stub
	}

	@SuppressLint("ValidFragment")
	public VaultSettingFragment(OnStartResetPasswordViewListener l) {
		super();
		listener = l;// TODO Auto-generated constructor stub
	}

	public static VaultSettingFragment newInstance(
			OnStartResetPasswordViewListener l) {
		return new VaultSettingFragment(l);
	}

	@Override
	public void onStart() {
		super.onStart();
		mPresenter.start();
	};

	@Override
	public void setPresenter(IVaultPresenter presenter) {
		// TODO Auto-generated method stub
		mPresenter = presenter;
	}

	@Override
	public void updateVaultView(boolean isOpen) {
		// TODO Auto-generated method stub
		cbv_vault_switch.setToogleState(isOpen, false);
		tv_vault_switch_tips.setVisibility(View.VISIBLE);
		if (isOpen) {
			tv_vault_switch_tips
					.setText(getString(R.string.DM_Set_SecureVault_Caption));
			llyt_vault_reset_password.setVisibility(View.VISIBLE);
		} else {
			tv_vault_switch_tips
					.setText(getString(R.string.DM_Set_SecureVault_Disabled_Caption));
			llyt_vault_reset_password.setVisibility(View.GONE);
		}
	}

	

	

	/**
	 * 点击保存键之后的回调
	 * 
	 * @param errorCode
	 */
	@Override
	public void onCreatePasswordResult(int errorCode) {
		// TODO Auto-generated method stub
		rl_errornote.setVisibility(errorCode == 0 ? View.GONE : View.VISIBLE);

		switch (errorCode) {
		case ParamCheckUserCase.NORMAL_CODE:
			llyt_vault_create_password.setVisibility(View.GONE);
			llyt_vault_reset_password.setVisibility(View.VISIBLE);
			tv_vault_switch_tips
					.setText(getString(R.string.DM_Set_SecureVault_Caption));
			tv_vault_switch_tips.setVisibility(View.VISIBLE);
			Toast.makeText(activity,
					getString(R.string.DM_Set_SecureVault_Password_Success),
					Toast.LENGTH_LONG).show();
			etbv_edittext.hideKeyboard();
			break;
		case ParamCheckUserCase.ERROR_PASSWORD_UNMATCHED:
			tv_errornote
					.setText(R.string.DM_Set_SecureVault_Password_Not_Match);
			break;
		case ParamCheckUserCase.ERROR_PASSWORD_TOOSHORT:
			tv_errornote.setText(R.string.DM_Error_PWD_Short);
			break;
		case ParamCheckUserCase.ERROR_PASSWORD_EMPTY:
			tv_errornote.setText(R.string.DM_Error_PWD_empty);
			break;
		case ParamCheckUserCase.ERROR_NOT_CHANGED:
			tv_errornote.setText(R.string.DM_Error_No_Change);
			break;
		case ParamCheckUserCase.ERROR_CHAR_ILLEGAL:
			tv_errornote.setText(R.string.DM_SetUI_Char_Illegal);
			break;
		case ParamCheckUserCase.ERROR_PASSWORD_TOOLONG:
			tv_errornote
					.setText(R.string.DM_SetUI_Credentials_Password_Too_Long);
			break;
		default:
			Toast.makeText(activity,
					getString(R.string.DM_Set_SecureVault_Password_Fail),
					Toast.LENGTH_LONG).show();
			break;

		}

	}

	@Override
	public void onCloseVaultResult(int erroCode) {
		// TODO Auto-generated method stub
		switch (erroCode) {
		case 0:
			if (adPassword != null && adPassword.isShowing()) {
				adPassword.dismiss();
				adPassword = null;
			}

			llyt_vault_reset_password.setVisibility(View.GONE);
			llyt_vault_create_password.setVisibility(View.GONE);
			tv_vault_switch_tips.setVisibility(View.VISIBLE);
			tv_vault_switch_tips
					.setText(getString(R.string.DM_Set_SecureVault_Disabled_Caption));
			cbv_vault_switch.setToogleState(false, false);
			Toast.makeText(activity,
					getString(R.string.DM_Set_SecureVault_Disabled_Success),
					Toast.LENGTH_LONG).show();
			break;

		case 1:
			// 密码错误
			if (adPassword != null && adPassword.isShowing()) {
				if (tmp_dialog_rlyt_error != null
						&& tmp_dialog_tv_error_msg != null) {
					tmp_dialog_rlyt_error.setVisibility(View.VISIBLE);
					tmp_dialog_tv_error_msg.setVisibility(View.VISIBLE);
					tmp_dialog_tv_error_msg
							.setText(getString(R.string.DM_Access_Password_Wrong));
				}
			}

			break;
		default:
			Toast.makeText(activity,
					getString(R.string.DM_Set_SecureVault_Disabled_Fail),
					Toast.LENGTH_LONG).show();
			break;
		}

	}

	@Override
	public void onCheckVaultResult(boolean isEmpty) {
		// TODO Auto-generated method stub
		if (isEmpty) {
			showRealCloseVaultDialog();
		} else {
			showCanNoCloseVaultDialog();
		}
	}

	@Override
	public void onGetPasswordTipsResult(String content) {
		// TODO Auto-generated method stub
		if (adPassword != null && adPassword.isShowing()) {
			tmp_dialog_tv_password_tips.setVisibility(View.GONE);
			tmp_dialog_llyt_tips.setVisibility(View.VISIBLE);
			if(content == null || content.equals("")) {
				tmp_dialog_tv_password_tips_content.setVisibility(View.GONE);
			} else {
				tmp_dialog_tv_password_tips_content.setVisibility(View.VISIBLE);
				tmp_dialog_tv_password_tips_content
						.setText(content);
			}
		}
	}

	
	
	// 基类 的回调，也就是本界面的请求
	
	@Override
	public void createPassword(String a, String b, String c) {
		// TODO Auto-generated method stub

		mPresenter.createPassword(a,b,c);
	}

	@Override
	public void checkIsEmpty() {
		// TODO Auto-generated method stub
		mPresenter.checkVault();
	}

	@Override
	public void close(String content) {
		// TODO Auto-generated method stub
		mPresenter.closeVault(content);
	}

	@Override
	public void getTips() {
		// TODO Auto-generated method stub
		mPresenter.getPasswordTips();
	}

}
