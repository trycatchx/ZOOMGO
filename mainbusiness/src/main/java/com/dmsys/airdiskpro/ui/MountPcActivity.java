package com.dmsys.airdiskpro.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.dmairdisk.aodplayer.util.CommonAsync;
import com.dmsys.airdiskpro.event.DisconnectEvent;
import com.dmsys.dmsdk.DMSdk;
import com.dmsys.dmsdk.DMSdk.DMDeviceListChangeListener;
import com.dmsys.dmsdk.DMSdk.DMDeviceStatusChangeListener;
import com.dmsys.dmsdk.model.DMDevice;
import com.dmsys.dmsdk.model.DMStatusType;
import com.dmsys.dmsdk.model.DMStorageInfo;
import com.dmsys.dmsdk.model.DMWifiSetting;
import com.dmsys.mainbusiness.R;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.greenrobot.event.EventBus;

public class MountPcActivity extends Activity {

	private TextView text_message;
	private long cookie_disk,deviceCookie;
	private long mExitTime = 0;
	private BroadcastReceiver mReceiver ;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mountpc);
		text_message = (TextView) findViewById(R.id.text_message);
		EventBus.getDefault().register(this);
		
		attachDiskListener();
		attachDeviceChangeListener();
		
		initBroadcaseReceiver();
		
	}
	
	private void attachDiskListener() {
		// TODO Auto-generated method stub
		cookie_disk = DMSdk.getInstance().attachListener(new DMDeviceStatusChangeListener() {

			@Override
			public void onDeviceStatusChanged(int type) {
				// TODO Auto-generated method stub
				System.out.println("cookie_disk:"+type);
				if (DMStatusType.isDiskChange(type)) {
					getDiskInfo();
				}
			}
		});
	}
	
	private void attachDeviceChangeListener() {
		deviceCookie = DMSdk.getInstance().attachListener(new DMDeviceListChangeListener() {
			@Override
			public void onDeviceListChanged(int type,DMDevice device) {
				if (type == 1) {
					finish();
				}
			}
		});
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		getWifiSettings();
	}
	
	
	private void getWifiSettings() {
		// TODO Auto-generated method stub
		CommonAsync.Runnable runnable = new CommonAsync.Runnable() {
			
			@Override
			public void stop() {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public Object run() {
				// TODO Auto-generated method stub
				return DMSdk.getInstance().getDeviceWifiSettingInfo();
			}
		};
		
		CommonAsync.CommonAsyncListener listener = new CommonAsync.CommonAsyncListener() {
			
			@Override
			public void onResult(Object ret) {
				// TODO Auto-generated method stub
				DMWifiSetting setting = (DMWifiSetting) ret;
				if (setting != null && setting.getSsid() != null) {
					String content = String.format(getString(R.string.DM_MDNS_Disk_Connect_PC), setting.getSsid());
					text_message.setText(content);
				}
			}
			
			@Override
			public void onError() {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onDestory() {
				// TODO Auto-generated method stub
				
			}
		};
		
		CommonAsync async = new CommonAsync(runnable, listener);
		async.executeOnExecutor( (ExecutorService) Executors.newCachedThreadPool());
	}
	
	private void getDiskInfo(){
		
		CommonAsync.Runnable runnable = new CommonAsync.Runnable() {
			
			@Override
			public void stop() {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public Object run() {
				// TODO Auto-generated method stub
				return DMSdk.getInstance().getStorageInfo();
			}
		};
		
		CommonAsync.CommonAsyncListener listener = new CommonAsync.CommonAsyncListener() {
			
			@Override
			public void onResult(Object ret) {
				// TODO Auto-generated method stub
				DMStorageInfo storageInfo = (DMStorageInfo) ret;
				if (storageInfo != null) {
					if (storageInfo.getMountStatus() == 1) {
						finish();
					}
				}
			}
			
			@Override
			public void onError() {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onDestory() {
				// TODO Auto-generated method stub
				
			}
		};
		
		CommonAsync async = new CommonAsync(runnable, listener);
		async.executeOnExecutor( (ExecutorService) Executors.newCachedThreadPool());
		
	}
	
	
	public void onEventMainThread(DisconnectEvent event){
		finish();
	}
	
	public void onClickMyDownload(View view){
		Intent intent = new Intent(this, MyDownloadActivity.class);
		startActivity(intent);
	}
	
	private void initBroadcaseReceiver() {
		// TODO Auto-generated method stub
		if (mReceiver == null) {
			
			mReceiver = new BroadcastReceiver() {

				@Override
				public void onReceive(final Context context, Intent intent) {
					// TODO Auto-generated method stub
					if (intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
						
						NetworkInfo info=intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
						WifiInfo mWifiInfo0 =intent.getParcelableExtra(WifiManager.EXTRA_WIFI_INFO);
						State wifi = info.getState();
						if (wifi != null) {
							if (wifi == State.DISCONNECTED) {
								finish();
							}
						} 
					}
				}
			};
			
			IntentFilter filter = new IntentFilter();
			filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
			registerReceiver(mReceiver, filter);
			
		}
		
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			
			if (System.currentTimeMillis() - mExitTime > 2000) {
				Toast.makeText(this, R.string.DM_MainAc_Toast_Key_Back_Quit, Toast.LENGTH_SHORT).show();
				mExitTime = System.currentTimeMillis();
				return false;
			} else {
				//EventBus.getDefault().post(new ExitEvent());
				
				Intent intent = new Intent(this,MainActivity.class); 
				intent.putExtra(MainActivity.TAG_EXIT, true); 
				startActivity(intent);
				
				this.finish();
			}
		}
		
		return super.onKeyDown(keyCode, event);
	}
	
	public void unregisterReceiver() {
		if (mReceiver != null) {
			System.out.println("ununununregister!!!");
			unregisterReceiver(mReceiver);
			mReceiver = null;
		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		EventBus.getDefault().unregister(this);
		unregisterReceiver();
		DMSdk.getInstance().removeListener(cookie_disk);
		DMSdk.getInstance().removeListener(deviceCookie);
	}
	
	
}
