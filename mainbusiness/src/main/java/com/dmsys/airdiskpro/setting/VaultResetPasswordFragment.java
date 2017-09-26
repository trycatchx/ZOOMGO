package com.dmsys.airdiskpro.setting;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dm.xunlei.udisk.Network.View.EditTextButtonView;
import com.dmsys.airdiskpro.setting.DeviceVaultContract.IVaultReSetPasswordPresenter;
import com.dmsys.mainbusiness.R;

public class VaultResetPasswordFragment extends Fragment implements
		DeviceVaultContract.IVaultReSetPasswordView, View.OnClickListener {

	private View parent;

	IVaultReSetPasswordPresenter mPresenter;
	Activity activity;
	EditTextButtonView etbv_edittext, etbv_password, etbv_password1,
			etbv_password_tips;
	RelativeLayout rl_errornote;
	TextView tv_errornote;
	Button btn_vault_save;

	OnStartVaultSettingViewListener listener ;
	public interface OnStartVaultSettingViewListener{
		void onStartVaultSettingView();
	}
	public VaultResetPasswordFragment() {
		super();
	}
	
	public VaultResetPasswordFragment(OnStartVaultSettingViewListener l) {
		super();
		listener = l;// TODO Auto-generated constructor stub
	}
	
	public static VaultResetPasswordFragment newInstance(OnStartVaultSettingViewListener l) {
		return new VaultResetPasswordFragment(l);
	}





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
		parent = inflater.inflate(R.layout.fragment_vault_reset_password,
				container, false);

		return parent;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);

		initViews();

		// loadData();
	}

	@Override
	public void onStart() {
		super.onStart();
		mPresenter.start();
	};

	private void initViews() {
		etbv_edittext = (EditTextButtonView) parent
				.findViewById(R.id.etbv_edittext);
		etbv_password = (EditTextButtonView) parent
				.findViewById(R.id.etbv_password);
		etbv_password1 = (EditTextButtonView) parent
				.findViewById(R.id.etbv_password1);
		etbv_password_tips = (EditTextButtonView) parent
				.findViewById(R.id.etbv_password_tips);
		rl_errornote = (RelativeLayout) parent.findViewById(R.id.rl_errornote);
		tv_errornote = (TextView) parent.findViewById(R.id.tv_errornote);
		btn_vault_save = (Button) parent.findViewById(R.id.btn_vault_save);

		etbv_edittext.setStyle(EditTextButtonView.OptionalPasswordStyle);
		etbv_password.setStyle(EditTextButtonView.OptionalPasswordStyle);
		etbv_password1.setStyle(EditTextButtonView.OptionalPasswordStyle);
		etbv_password_tips.setStyle(EditTextButtonView.EditTextStyle);

		etbv_edittext
				.setEditTextHint(getString(R.string.DM_Set_SecureVault_Password_Reset_Enter));
		etbv_password
				.setEditTextHint(getString(R.string.DM_Set_SecureVault_Password_Reset_Enter_New));
		etbv_password1
				.setEditTextHint(getString(R.string.DM_Set_SecureVault_Password_Reset_Enter_Again));

		btn_vault_save.setOnClickListener(this);

		etbv_edittext.beFocus();
		etbv_edittext.pullUpKeyboard();

	}

	// resetPassword

	@Override
	public void setPresenter(IVaultReSetPasswordPresenter presenter) {
		// TODO Auto-generated method stub
		this.mPresenter = presenter;
	}

	@Override
	public void onResetPasswordResult(int errorCode) {
		// TODO Auto-generated method stub
		rl_errornote.setVisibility(errorCode == 0 ? View.GONE : View.VISIBLE);

		switch (errorCode) {
		case ParamCheckUserCase.NORMAL_CODE:
			listener.onStartVaultSettingView();
			Toast.makeText(activity,
					getString(R.string.DM_Set_SecureVault_Password_Reset_Success),
					Toast.LENGTH_LONG).show();
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
			tv_errornote.setText(R.string.DM_Set_SecureVault_Password_Reset_Repeat);
			break;
		case ParamCheckUserCase.ERROR_CHAR_ILLEGAL:
			tv_errornote.setText(R.string.DM_SetUI_Char_Illegal);
			break;
		case ParamCheckUserCase.ERROR_PASSWORD_TOOLONG:
			tv_errornote
					.setText(R.string.DM_SetUI_Credentials_Password_Too_Long);
			break;
		case ParamCheckUserCase.ERROR_PASSWORD_INCORRECT:
			tv_errornote
			.setText(R.string.DM_Set_SecureVault_Password_Original_Wrong);
			break;
		default:
			Toast.makeText(activity,
					getString(R.string.DM_Set_SecureVault_Password_Reset_Fail),
					Toast.LENGTH_LONG).show();
			break;
		}

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		int i = v.getId();
		if (i == R.id.btn_vault_save) {
			mPresenter.resetPassword(etbv_edittext.getContentText(),
					etbv_password.getContentText(),
					etbv_password1.getContentText(),
					etbv_password_tips.getContentText());

		} else {
		}
	}

}
