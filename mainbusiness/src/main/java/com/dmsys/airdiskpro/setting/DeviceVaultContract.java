package com.dmsys.airdiskpro.setting;

import com.dmsys.airdiskpro.BasePresenter;
import com.dmsys.airdiskpro.BaseView;

/**
 * 此mvp 遵循v和P 一对一 （标准）
 * 
 * @author jiong103
 *
 */

public interface DeviceVaultContract {

	
	/**
	 * 设备密码和保险库的view 接口还有P层的接口 用在activity
	 * @author jiong103
	 *
	 */
	public interface IDeviceVaultView extends BaseView<IDeviceVaultPresenter> {

		void updatePasswordModifyView(boolean isSupport, boolean isSetting);

		void updateVaultView(boolean isSupport, boolean isSetting);

	}
	public interface IDeviceVaultPresenter extends BasePresenter {
		void getDeviceVaultStatus();
	}
	
	
	/**
	 * 保险库的View 接口以及 P层的接口 用在Fragment
	 * @author jiong103
	 *
	 */

	public interface IVaultView extends BaseView<IVaultPresenter> {

		void updateVaultView(boolean isOpen);
		void onCreatePasswordResult(int errorCode);
		void onCloseVaultResult(int erroCode);
		void onCheckVaultResult(boolean isEmpty);
		void onGetPasswordTipsResult(String content);
	}

	public interface IVaultPresenter extends BasePresenter {
		void createPassword(String password, String password1, String contentTips);
		void closeVault(String password);
		void checkVault();
		void getPasswordTips();

	}
	
	/**
	 * reset保险库的 密码
	 */
	public interface IVaultReSetPasswordView extends BaseView<IVaultReSetPasswordPresenter> {
		void onResetPasswordResult(int errorCode);
	}
	
	public interface IVaultReSetPasswordPresenter extends BasePresenter {
		void resetPassword(String oldPassword, String password, String password1, String contentTips);
	}
	
	

}
