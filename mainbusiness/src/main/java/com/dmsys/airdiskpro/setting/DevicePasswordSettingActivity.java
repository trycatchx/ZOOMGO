package com.dmsys.airdiskpro.setting;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.dmsys.airdiskpro.setting.BaseOnOffSettingFragment.OnStartResetPasswordViewListener;
import com.dmsys.airdiskpro.setting.VaultResetPasswordFragment.OnStartVaultSettingViewListener;
import com.dmsys.mainbusiness.R;

import rx.subscriptions.CompositeSubscription;

public class DevicePasswordSettingActivity extends FragmentActivity implements
		OnStartResetPasswordViewListener, OnStartVaultSettingViewListener,
		OnClickListener {

	DevicePasswordSettingFragment devicePasswordSettingFragment;
	VaultResetPasswordFragment vaultResetPasswordFragment;
	private TextView titlebar_title;
	public CompositeSubscription mSubscriptions = new CompositeSubscription();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_vault);
		initView();
	}

	private void initView() {
		FrameLayout back = (FrameLayout) findViewById(R.id.layout_back);
		ImageView titlebar_left = (ImageView) findViewById(R.id.titlebar_left);
		titlebar_left
				.setImageResource(R.drawable.dm_lib_wifi_back_btn_bg_selector);
		back.setOnClickListener(this);
		titlebar_title = (TextView) findViewById(R.id.titlebar_title);
		titlebar_title.setText(getString(R.string.DM_Set_SecureVault));

		devicePasswordSettingFragment = DevicePasswordSettingFragment.newInstance(this);
		vaultResetPasswordFragment = VaultResetPasswordFragment
				.newInstance(this);
		showDevicePasswordSettingFragment();
	}

	private void showDevicePasswordSettingFragment() {

		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction transaction = fragmentManager.beginTransaction();
		if (devicePasswordSettingFragment.isAdded()) {
			transaction.show(devicePasswordSettingFragment);
		} else {
			transaction.add(R.id.llyt_valut_base, devicePasswordSettingFragment);
			// 建立mvp
			new DevicePasswordSettingPresenter(devicePasswordSettingFragment,
					new ParamCheckUserCase());
		}

		if (vaultResetPasswordFragment.isAdded()) {
			transaction.hide(vaultResetPasswordFragment);
		}
		transaction.commit();
	}

	private void showResetVaultPasswordFragment() {

		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction transaction = fragmentManager.beginTransaction();
		if (vaultResetPasswordFragment.isAdded()) {
			transaction.show(vaultResetPasswordFragment);
		} else {
			transaction.add(R.id.llyt_valut_base, vaultResetPasswordFragment);
			// 建立mvp
			new VaultResetPasswordPresenter(vaultResetPasswordFragment,
					new ParamCheckUserCase(),mSubscriptions);
		}
		if (devicePasswordSettingFragment.isAdded()) {
			transaction.hide(devicePasswordSettingFragment);
		}
		transaction.commit();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		boolean ret = false;
		if (keyCode == KeyEvent.KEYCODE_BACK
				&& vaultResetPasswordFragment.isVisible()) {
			showDevicePasswordSettingFragment();
			ret = true;
		}
		return ret ? true : super.onKeyDown(keyCode, event);
	}

	@Override
	public void onStartResetPasswordView() {
		// TODO Auto-generated method stub
		showResetVaultPasswordFragment();
	}

	@Override
	public void onStartVaultSettingView() {
		// TODO Auto-generated method stub
		showDevicePasswordSettingFragment();
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		int i = v.getId();
		if (i == R.id.layout_back) {
			if (vaultResetPasswordFragment.isVisible()) {
				showDevicePasswordSettingFragment();
			} else {
				finish();
			}

		} else {
		}
	}


	@Override
	public void onDestroy() {
		super.onDestroy();
		mSubscriptions.unsubscribe();
	}

}
