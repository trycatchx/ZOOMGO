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

public class VaultSettingActivity extends FragmentActivity implements
		OnStartResetPasswordViewListener, OnStartVaultSettingViewListener,
		OnClickListener {

	VaultSettingFragment vaultSettingFragment;
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

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mSubscriptions.unsubscribe();
	}

	private void initView() {
		FrameLayout back = (FrameLayout) findViewById(R.id.layout_back);
		ImageView titlebar_left = (ImageView) findViewById(R.id.titlebar_left);
		titlebar_left
				.setImageResource(R.drawable.dm_lib_wifi_back_btn_bg_selector);
		back.setOnClickListener(this);
		titlebar_title = (TextView) findViewById(R.id.titlebar_title);
		titlebar_title.setText(getString(R.string.DM_Set_SecureVault));

		vaultSettingFragment = VaultSettingFragment.newInstance(this);
		vaultResetPasswordFragment = VaultResetPasswordFragment
				.newInstance(this);
		showVaultSettingFragment();
	}

	private void showVaultSettingFragment() {

		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction transaction = fragmentManager.beginTransaction();
		if (vaultSettingFragment.isAdded()) {
			transaction.show(vaultSettingFragment);
		} else {
			transaction.add(R.id.llyt_valut_base, vaultSettingFragment);
			// 建立mvp
			new VaultSettingPresenter(vaultSettingFragment,
					new ParamCheckUserCase(),mSubscriptions);
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
		if (vaultSettingFragment.isAdded()) {
			transaction.hide(vaultSettingFragment);
		}
		transaction.commit();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		boolean ret = false;
		if (keyCode == KeyEvent.KEYCODE_BACK
				&& vaultResetPasswordFragment.isVisible()) {
			showVaultSettingFragment();
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
		showVaultSettingFragment();
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		int i = v.getId();
		if (i == R.id.layout_back) {
			if (vaultResetPasswordFragment.isVisible()) {
				showVaultSettingFragment();
			} else {
				finish();
			}

		} else {
		}
	}

}
