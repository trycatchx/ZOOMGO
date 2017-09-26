package com.dmsys.airdiskpro.setting;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.dmairdisk.aodplayer.util.CommonAsync;
import com.dmsys.dmsdk.DMSdk;
import com.dmsys.dmsdk.model.DMRemoteAP;
import com.dmsys.dmsdk.model.DMWifiSetting;
import com.dmsys.mainbusiness.R;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DeviceInfoActivity extends Activity implements OnClickListener {

	private TextView wifi_ssid;
	private TextView wifi_ip;
	private TextView wifi_mac;
	
	private TextView net_ssid;
	private TextView net_ip;
	private TextView net_mac;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_info);
		initViews();
	}

	private void initViews() {
		// TODO Auto-generated method stub
		((TextView) findViewById(R.id.titlebar_title)).setText(R.string.DM_Device_Info);
		FrameLayout back = (FrameLayout) findViewById(R.id.layout_back);
		ImageView titlebar_left = (ImageView) findViewById(R.id.titlebar_left);
		titlebar_left.setImageResource(R.drawable.dm_lib_wifi_back_btn_bg_selector);
		back.setOnClickListener(this);
		
		wifi_ssid = (TextView) findViewById(R.id.text_wifi_ssid);
		wifi_ip = (TextView) findViewById(R.id.text_wifi_ip);
		wifi_mac = (TextView) findViewById(R.id.text_wifi_mac);
		
		net_ssid = (TextView) findViewById(R.id.text_remote_ssid);
		net_ip = (TextView) findViewById(R.id.text_remote_ip);
		net_mac = (TextView) findViewById(R.id.text_remote_mac);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		getWifiSetting();
		getRemoteApInfo();
	}
	
	private void getWifiSetting() {
		// TODO Auto-generated method stub
		CommonAsync.Runnable runnable = new CommonAsync.Runnable() {
			
			@Override
			public void stop() {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public Object run() {
				// TODO Auto-generated method stub
				System.out.println("getDeviceWifiSettingInfo");
				return  DMSdk.getInstance().getDeviceWifiSettingInfo();
			}
		};
		
		CommonAsync.CommonAsyncListener listener = new CommonAsync.CommonAsyncListener() {
			
			@Override
			public void onResult(Object ret) {
				// TODO Auto-generated method stub
				DMWifiSetting wifi =  (DMWifiSetting) ret;
				if (wifi != null) {
					wifi_ssid.setText(wifi.getSsid());
					wifi_ip.setText(wifi.getIp());
					wifi_mac.setText(wifi.getMac());
				}else {
					wifi_ssid.setText(R.string.DM_Sidebar_Unconnect);
					wifi_ip.setText("0.0.0.0");
					wifi_mac.setText("----");
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
		
		CommonAsync task = new CommonAsync(runnable, listener);
		ExecutorService FULL_TASK_EXECUTOR = (ExecutorService) Executors.newCachedThreadPool();
		task.executeOnExecutor(FULL_TASK_EXECUTOR);
	}

	protected void getRemoteApInfo() {
		// TODO Auto-generated method stub
		CommonAsync.Runnable runnable = new CommonAsync.Runnable() {
			
			@Override
			public void stop() {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public Object run() {
				// TODO Auto-generated method stub
				System.out.println("setSafetyExit");
				return  DMSdk.getInstance().getDeviceRemoteAPInfo();
			}
		};
		
		CommonAsync.CommonAsyncListener listener = new CommonAsync.CommonAsyncListener() {
			
			@Override
			public void onResult(Object ret) {
				// TODO Auto-generated method stub
				DMRemoteAP remoteAP =  (DMRemoteAP) ret;
				if (remoteAP != null && remoteAP.getIsConnect()) {
					net_ssid.setText(remoteAP.getSsid());
					net_ip.setText(remoteAP.getIp());
					net_mac.setText(remoteAP.getMac());
				}else {
					net_ssid.setText(R.string.DM_Sidebar_Unconnect);
					net_ip.setText("0.0.0.0");
					net_mac.setText("----");
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
		
		CommonAsync task = new CommonAsync(runnable, listener);
		ExecutorService FULL_TASK_EXECUTOR = (ExecutorService) Executors.newCachedThreadPool();
		task.executeOnExecutor(FULL_TASK_EXECUTOR);
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		int i = v.getId();
		if (i == R.id.layout_back) {
			finish();

		} else {
		}
	}
	
}
