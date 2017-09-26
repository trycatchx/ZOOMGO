package com.dmsys.airdiskpro.setting;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dmsys.airdiskpro.setting.DeviceVaultContract.IDeviceVaultPresenter;
import com.dmsys.mainbusiness.R;

public class DeviceVaultActivity extends Activity implements
		DeviceVaultContract.IDeviceVaultView, OnClickListener {

	private RelativeLayout rlty_sett_info, rlyt_vault_setting;
	private LinearLayout llyt_vault,llyt_password;
	private TextView tv_device_statu,tv_vault_statu,setting_title_text;
	private ImageView backButton,ibRefresh;

	private DeviceVaultContract.IDeviceVaultPresenter mPresenter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_device_vault);
		// 初始化应有的布局还有params
		initView();
		initVars();
	}
	
	

	private void initVars() {
		mPresenter = new DeviceVaultPresenter(this);
		
	}

	private void initView() {
		rlty_sett_info = (RelativeLayout) findViewById(R.id.rlty_sett_info);
		rlyt_vault_setting = (RelativeLayout) findViewById(R.id.rlyt_vault_setting);
		llyt_vault = (LinearLayout) findViewById(R.id.llyt_vault);
		llyt_password = (LinearLayout) findViewById(R.id.llyt_password);
		
		tv_device_statu = (TextView) findViewById(R.id.tv_device_statu);
		tv_vault_statu = (TextView) findViewById(R.id.tv_vault_statu);
		setting_title_text = (TextView) findViewById(R.id.setting_title_text);
		backButton = (ImageView)findViewById(R.id.setting_title_button);
		ibRefresh = (ImageView)findViewById(R.id.ibRefresh);
		ibRefresh.setVisibility(View.GONE);
		
		setting_title_text.setText(getString(R.string.DM_Setting_Secure));
		rlty_sett_info.setOnClickListener(this);
		rlyt_vault_setting.setOnClickListener(this);
		backButton.setOnClickListener(this);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		mPresenter.start();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		mPresenter.stop();
		super.onDestroy();
	}

	/**
	 * 这里无需实现，在initVars 已经有保留一份引用。 这里设计的初衷是 假若view
	 * 是fragment，那么Presenter的引用是在activity。。
	 */
	@Override
	public void setPresenter(IDeviceVaultPresenter presenter) {
		// TODO Auto-generated method stub

	}


	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		int i = v.getId();
		if (i == R.id.rlty_sett_info) {
			Intent intent = new Intent(this, PasswordModifyActivity.class);
			intent.putExtra("pwd_state", tv_device_statu.getText().toString());
			startActivity(intent);

		} else if (i == R.id.rlyt_vault_setting) {
			Intent intent1 = new Intent(this, VaultSettingActivity.class);
			intent1.putExtra("pwd_state", tv_device_statu.getText().toString());
			startActivity(intent1);

		} else if (i == R.id.setting_title_button) {
			finish();

		}
	}

	@Override
	public void updatePasswordModifyView(boolean isSupport, boolean isSetting) {
		// TODO Auto-generated method stub
		
		if(isSupport) {
			llyt_password.setVisibility(View.VISIBLE);
			if(isSetting) {
				tv_device_statu.setText(R.string.DM_Setting_Access_Password_Set);
			} else {
				tv_device_statu.setText(R.string.DM_Setting_Access_Password_Notset);
			}
		} else {
			llyt_password.setVisibility(View.GONE);
		}
		
	}

	
	@Override
	public void updateVaultView(boolean isSupport, boolean isSetting) {
		// TODO Auto-generated method stub
		if(isSupport) {
			if(isSetting) {
				tv_vault_statu.setText(R.string.DM_Setting_Access_Password_Set);
			} else {
				tv_vault_statu.setText(R.string.DM_Setting_Access_Password_Notset);
			}
		} else {
			llyt_vault.setVisibility(View.GONE);
		}
	}
	
	
}
