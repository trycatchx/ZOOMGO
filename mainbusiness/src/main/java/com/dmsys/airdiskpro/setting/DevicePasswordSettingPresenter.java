package com.dmsys.airdiskpro.setting;

import com.dmairdisk.aodplayer.util.CommonAsync;
import com.dmsys.airdiskpro.setting.DeviceVaultContract.IVaultView;
import com.dmsys.dmsdk.DMSdk;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.dmsys.airdiskpro.utils.Preconditions.checkNotNull;

public class DevicePasswordSettingPresenter implements
		DeviceVaultContract.IVaultPresenter {

	IVaultView mVaultView;
	ParamCheckUserCase mParamCheckUserCase;
	
	public DevicePasswordSettingPresenter(IVaultView mVaultView,ParamCheckUserCase mParamCheckUserCase) {
		super();
		// TODO Auto-generated constructor stub
		
		this.mVaultView = checkNotNull(mVaultView);
		this.mVaultView.setPresenter(this);
		this.mParamCheckUserCase = mParamCheckUserCase;
	}


	@Override
	public void start() {
		// TODO Auto-generated method stub
		getDeviceVaultStatus();
	}

	@Override
	public void stop() {

	}

	public void getDeviceVaultStatus() {
		// TODO Auto-generated method stub
		 new CommonAsync(new CommonAsync.Runnable() {
				@Override
				public Object run() {
					// TODO Auto-generated method stub
					return DMSdk.getInstance().getPasswordState();
				}
			}, new CommonAsync.CommonAsyncListener() {
				
				@Override
				public void onResult(Object ret) {
					// TODO Auto-generated method stub
					int type = (int) ret;
					mVaultView.updateVaultView(type == 1 ? true:false);
				}
				
			}).executeOnExecutor((ExecutorService) Executors.newCachedThreadPool());

	}
	
	

	@Override
	public void createPassword(final String password, final String password1,
			final String contentTips) {
		// TODO Auto-generated method stub
		 new CommonAsync(new CommonAsync.Runnable() {
				@Override
				public Object run() {
					// TODO Auto-generated method stub
					int ret  = mParamCheckUserCase.checkParameter(password,password1);
					if(ret == 0) {
						ret = DMSdk.getInstance().setPassword(password);
					}
					return ret;
				}
			}, new CommonAsync.CommonAsyncListener() {
				
				@Override
				public void onResult(Object ret) {
					// TODO Auto-generated method stub
					mVaultView.onCreatePasswordResult((int) ret);
				}
				
			}).executeOnExecutor((ExecutorService) Executors.newCachedThreadPool());
	}

	@Override
	public void closeVault(String password) {
		// TODO Auto-generated method stub
		 new CommonAsync(new CommonAsync.Runnable() {
				@Override
				public Object run() {
					// TODO Auto-generated method stub
					return 	0;
				}
			}, new CommonAsync.CommonAsyncListener() {
				
				@Override
				public void onResult(Object ret) {
					// TODO Auto-generated method stub
					mVaultView.onCloseVaultResult((int )ret);
				}
			}).executeOnExecutor((ExecutorService) Executors.newCachedThreadPool());
	}

	@Override
	public void checkVault() {
		// TODO Auto-generated method stub
		mVaultView.onCheckVaultResult(true);
	}

	@Override
	public void getPasswordTips() {
		// TODO Auto-generated method stub
		mVaultView.onGetPasswordTipsResult(null);
	}

}
