package com.dmsys.airdiskpro.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dm.baselib.BaseValue;
import com.dm.xunlei.udisk.Network.Dialog.AlertDmDialogDefault;
import com.dm.xunlei.udisk.wificonnect.UDiskWiFiInternetSettingsActivity;
import com.dmsys.airdiskpro.event.BackupRefreshEvent;
import com.dmsys.airdiskpro.event.BackupStateEvent;
import com.dmsys.airdiskpro.event.NewFwEvent;
import com.dmsys.airdiskpro.event.PasswordChangeEvent;
import com.dmsys.airdiskpro.event.StorageEvent;
import com.dmsys.airdiskpro.event.SupportFunctionEvent;
import com.dmsys.airdiskpro.service.BackupService;
import com.dmsys.airdiskpro.setting.SettingsActivity;
import com.dmsys.airdiskpro.setting.VaultSettingActivity;
import com.dmsys.airdiskpro.utils.AndroidConfig;
import com.dmsys.airdiskpro.utils.HandlerUtil;
import com.dmsys.airdiskpro.view.MessageDialog;
import com.dmsys.airdiskpro.view.UDiskBaseDialog;
import com.dmsys.dmsdk.DMSdk;
import com.dmsys.dmsdk.DMSdk.DMDeviceStatusChangeListener;
import com.dmsys.dmsdk.model.DMIsOpeningVault;
import com.dmsys.dmsdk.model.DMPower;
import com.dmsys.dmsdk.model.DMRemoteAP;
import com.dmsys.dmsdk.model.DMRet;
import com.dmsys.dmsdk.model.DMStatusType;
import com.dmsys.dmsdk.model.DMStorage;
import com.dmsys.dmsdk.model.DMStorageInfo;
import com.dmsys.dmsdk.model.DMSupportFunction;
import com.dmsys.dmsdk.model.DMWifiSetting;
import com.dmsys.dropbox.activity.MyDropBoxActivity;
import com.dmsys.txtviewer.util.DisplayUtils;
import com.dmsys.mainbusiness.R;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.greenrobot.event.EventBus;

public class MenuFragment extends Fragment implements View.OnClickListener {

	private View rootView;
	private TextView tv_layout_menu_wifi_status;
	private TextView tv_layout_menu_network_status;
	private TextView tv_layout_menu_no_devive;
	private TextView tv_set_app_ver, tv_set_fw_ver,tv_set_device_ip;
	private ImageView iv_layout_menu_device_icon, iv_layout_new_tips;
	private RelativeLayout rlyt_layout_menu_network, rlyt_layout_menu_wifi,
			rlyt_layout_menu_backup_auto, rlyt_layout_menu_download,
			rlyt_layout_menu_setting, rlyt_layout_menu_backup_manual,
			layout_info, rlyt_layout_menu_dropbox, rlyt_layout_menu_vault;

	private LinearLayout layout_device_info;
	private ProgressBar storageProgressBar, baking;
	private ImageView iv_power;
	private TextView text_curDevice;
	private TextView text_storage;

	private MainActivity mActivity;
	private HandlerUtil.StaticHandler mHandler;

	private long mStateCookie;

	private static final int REQUEST_VERSION = 0;
	private static final int REQUEST_REMOTEAP = 1;
	private static final int REQUEST_WIFISETTING = 2;
	private static final int REQUEST_STORAGE = 3;
	private static final int REQUEST_POWER = 4;
	private static final int REQUEST_SYNCTIME = 5;
	private static final int REQUEST_PRIVATEVERSION = 6;
	private static final int REQUEST_CLIENT = 7;
	private static final int REQUEST_GET_SAFETYEXIT = 8;
	private static final int REQUEST_SET_SAFETYEXIT = 9;
	private static final int REQUEST_VAULT_STATE = 10;

	// 避免多次跳出连接成功提示；
	private boolean alreadyConnected;

	private ReceiveBroadCast receiver;

	private long[] mHits = new long[4];
	private String privateVersion;

	private DMStorageInfo mStorage;
	private boolean suppotRemoteAp;

	private WakeLock mWakeLock;
	private Dialog promptDialog;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.fragment_menu, null);
		initViews();
		return rootView;
	}

	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
		mActivity = (MainActivity) activity;
		System.out.println("menu onAttach");
	}

	public void refreshSSID(String ssid) {
		if (ssid != null && ssid.length() > 1) {
			tv_layout_menu_wifi_status.setText(ssid);
		} else if (ssid == null) {
			tv_layout_menu_wifi_status
					.setText(getString(R.string.DM_Sidebar_Unconnect));
			refreshConnectInfo(false);
			resetUI();
		} else if (ssid.equals("")) {
			refreshConnectInfo(false);
			resetUI();
		}

	}

	public void refreshConnectInfo(boolean connected) {
		// TODO Auto-generated method stub
		if (connected) {
			if (!alreadyConnected) {
				getWifiInfo();
				getFwVersion();
				getStorageInfo();
				syncTime();
				getPrivateVersion();
			}
			
			rlyt_layout_menu_dropbox.setVisibility(View.GONE);
			rlyt_layout_menu_network.setEnabled(true);
		} else {
			alreadyConnected = false;
			rlyt_layout_menu_network.setEnabled(false);
			tv_layout_menu_network_status.setText("");
			rlyt_layout_menu_dropbox.setVisibility(View.GONE);
			rlyt_layout_menu_vault.setVisibility(View.GONE);
		}
		iv_layout_menu_device_icon.setClickable(connected);
	}

	class RequestTask extends AsyncTask<Void, Void, Object> {

		private int request;

		public RequestTask(int type) {
			// TODO Auto-generated constructor stub
			request = type;
		}

		@Override
		protected Object doInBackground(Void... params) {
			// TODO Auto-generated method stub
			Object result = null;
			switch (request) {
			case REQUEST_VERSION:
				result = DMSdk.getInstance().getFWVersion();
				break;

			case REQUEST_REMOTEAP:
				result = DMSdk.getInstance().getDeviceRemoteAPInfo();
				break;

			case REQUEST_WIFISETTING:
				result = DMSdk.getInstance().getDeviceWifiSettingInfo();
				break;

			case REQUEST_POWER:
				result = DMSdk.getInstance().getPowerInfo();
				break;

			case REQUEST_STORAGE:
				result = DMSdk.getInstance().getStorageInfo();
				break;

			case REQUEST_SYNCTIME:
				result = DMSdk.getInstance().syncTime();
				break;

			case REQUEST_PRIVATEVERSION:
				privateVersion = DMSdk.getInstance().getPrivateVersion();
				break;

			case REQUEST_CLIENT:
				result = DMSdk.getInstance().getClientStatus();
				break;

			case REQUEST_GET_SAFETYEXIT:
				result = DMSdk.getInstance().getSafetyExit();
				break;

			case REQUEST_SET_SAFETYEXIT:
				result = DMSdk.getInstance().safetyExit();
				break;
			case REQUEST_VAULT_STATE:
				result = DMSdk.getInstance().isOpeningVault();
				break;
			default:
				break;
			}

			return result;
		}

		@Override
		protected void onPostExecute(Object result) {
			// TODO Auto-generated method stub
			if (result != null) {

				switch (request) {
				case REQUEST_VERSION:
					String version = (String) result;
					System.out.println("REQUEST_VERSION:" + version);
					String fwVerShow = String
							.format(mActivity.getResources().getString(
									R.string.DM_Sidebar_FW_Version), version);
					if (isAdded()) {
						tv_set_fw_ver.setText(fwVerShow);
					}
					break;
				
				case REQUEST_REMOTEAP:
					DMRemoteAP remote = (DMRemoteAP) result;
					if (remote != null) {
						boolean connect = remote.getIsConnect();
						System.out.println("remoteap connected:" + connect);
						if (isAdded()) {
							if (connect) {
								if (BaseValue.Host != null) {
									tv_set_device_ip.setVisibility(View.VISIBLE);
									tv_set_device_ip.setText(String.format(getString(R.string.DM_Device_ip), remote.getIp()));
								}
								tv_layout_menu_network_status.setText(remote
										.getSsid());
							} else {
								tv_set_device_ip.setVisibility(View.GONE);
								tv_layout_menu_network_status
										.setText(getString(R.string.DM_Sidebar_Unconnect));
							}
						}
					}
					break;
				case REQUEST_WIFISETTING:
					DMWifiSetting wifi = (DMWifiSetting) result;
					if (wifi != null && isAdded()) {
						String ssid = wifi.getSsid().replace("\"", "");
						text_curDevice.setText(ssid);
						tv_layout_menu_no_devive.setVisibility(View.GONE);
						layout_device_info.setVisibility(View.VISIBLE);
						Toast.makeText(
								mActivity,
								String.format(
										getString(R.string.DM_Device_Connected_To),
										ssid), Toast.LENGTH_SHORT).show();
					}
					break;
				case REQUEST_STORAGE:
					DMStorageInfo info = mStorage = (DMStorageInfo) result;
					if (info != null && info.getMountStatus() == 1
							&& info.getStorages() != null
							&& info.getStorages().size() > 0 && isAdded()) {

						storageProgressBar.setVisibility(View.VISIBLE);
						layout_info.setVisibility(View.VISIBLE);

						refreshStorageInfo(info.getStorages());
					} else if (info != null && info.getMountStatus() == 0) {
						storageProgressBar.setVisibility(View.INVISIBLE);
						layout_info.setVisibility(View.INVISIBLE);
					}
					break;
				case REQUEST_POWER:
					DMPower power = (DMPower) result;
					System.out.println("power:" + power.getPower());
					System.out.println("status:" + power.getStatus());
					if (isAdded()) {
						refreshPowerInfo(power);
					}
					break;
				case REQUEST_CLIENT:
					int status = (int) result;
					if (status == 0) {
						getRemoteAP();
					} else if (status == 1) {
						tv_layout_menu_network_status
								.setText(getString(R.string.DM_SetUI_Device_Wireless_Closed));
					}
					break;
				case REQUEST_SET_SAFETYEXIT:
					int ret = (int) result;
					if (ret == DMRet.ACTION_SUCCESS) {
						Toast.makeText(mActivity, "success", Toast.LENGTH_SHORT)
								.show();
						
					} else {
						Toast.makeText(mActivity, "fail", Toast.LENGTH_SHORT)
						.show();
					}
					break;
				case REQUEST_VAULT_STATE:

					if (result != null) {
						DMIsOpeningVault tmp = (DMIsOpeningVault) result;
						if(tmp.errorCode == DMRet.ACTION_SUCCESS && tmp.isOpen) {
							//进入保险库
							Intent mIntent = new Intent (mActivity,VaultAllFileActivity.class);
							mActivity.startActivity(mIntent);
						} else {
							showVaultSwitchDialog();
						}
					} else {
						showVaultSwitchDialog();
					}
					break;

				default:
					break;
				}
			}
		}
	};


	private void getFwVersion() {
		// TODO Auto-generated method stub
		RequestTask task = new RequestTask(REQUEST_VERSION);
		task.executeOnExecutor((ExecutorService) Executors
				.newCachedThreadPool());
	}

	private void getPrivateVersion() {
		// TODO Auto-generated method stub
		RequestTask task = new RequestTask(REQUEST_PRIVATEVERSION);
		task.executeOnExecutor((ExecutorService) Executors
				.newCachedThreadPool());
	}

	private void syncTime() {
		RequestTask task = new RequestTask(REQUEST_SYNCTIME);
		task.executeOnExecutor((ExecutorService) Executors
				.newCachedThreadPool());
	}

	public void refreshStorageInfo(List<DMStorage> storages) {
		// TODO Auto-generated method stub
		long total = 0, used = 0;
		for (DMStorage dmStorage : storages) {
			total += dmStorage.total;
			used += dmStorage.total - dmStorage.free;
		}

		String usedInfo = String.format("%.2f", (used * 1.0) / 1024 / 1024)
				+ "GB";
		String totolInfo = String.format("%.2f", (total * 1.0) / 1024 / 1024)
				+ "GB";
		text_storage.setText(usedInfo + " / " + totolInfo);
		try {
			int progress = (int) (100 * used / total);
			storageProgressBar.setProgress(progress);
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	private void getRemoteAP() {
		// TODO Auto-generated method stub
		RequestTask task = new RequestTask(REQUEST_REMOTEAP);
		task.executeOnExecutor((ExecutorService) Executors
				.newCachedThreadPool());
	}

	private void getClientStatus() {
		// TODO Auto-generated method stub
		RequestTask task = new RequestTask(REQUEST_CLIENT);
		task.executeOnExecutor((ExecutorService) Executors
				.newCachedThreadPool());
	}

	private void getWifiInfo() {
		// TODO Auto-generated method stub
		RequestTask task = new RequestTask(REQUEST_WIFISETTING);
		task.executeOnExecutor((ExecutorService) Executors
				.newCachedThreadPool());
	}

	private void getStorageInfo() {
		// TODO Auto-generated method stub
		RequestTask task = new RequestTask(REQUEST_STORAGE);
		task.executeOnExecutor((ExecutorService) Executors
				.newCachedThreadPool());
	}

	private void getPowerInfo() {
		// TODO Auto-generated method stub
		RequestTask task = new RequestTask(REQUEST_POWER);
		task.executeOnExecutor((ExecutorService) Executors
				.newCachedThreadPool());
	}

	private void getSafetyExit() {
		// TODO Auto-generated method stub
		RequestTask task = new RequestTask(REQUEST_GET_SAFETYEXIT);
		task.executeOnExecutor((ExecutorService) Executors
				.newCachedThreadPool());
	}

	private void setSafetyExit() {
		// TODO Auto-generated method stub
		RequestTask task = new RequestTask(REQUEST_SET_SAFETYEXIT);
		task.executeOnExecutor((ExecutorService) Executors
				.newCachedThreadPool());
	}

	private void getVaultStatu() {
		RequestTask task = new RequestTask(REQUEST_VAULT_STATE);
		task.executeOnExecutor((ExecutorService) Executors
				.newCachedThreadPool());
	}

	public void refreshPowerInfo(DMPower power) {
		// TODO Auto-generated method stub
		if (power.getStatus() == DMPower.CHARGING) {

			if (power.getPower() > 90) {
				iv_power.setImageResource(R.drawable.power_5_charging);
			} else if (power.getPower() > 70) {
				iv_power.setImageResource(R.drawable.power_4_charging);
			} else if (power.getPower() > 50) {
				iv_power.setImageResource(R.drawable.power_3_charging);
			} else if (power.getPower() > 30) {
				iv_power.setImageResource(R.drawable.power_2_charging);
			} else if (power.getPower() > 10) {
				iv_power.setImageResource(R.drawable.power_1_charging);
			} else {
				iv_power.setImageResource(R.drawable.power_0_charging);
			}

		} else if (power.getStatus() == DMPower.LOW_POWER) {
			iv_power.setImageResource(R.drawable.power_0);
		} else {
			if (power.getPower() > 90) {
				iv_power.setImageResource(R.drawable.power_5);
			} else if (power.getPower() > 70) {
				iv_power.setImageResource(R.drawable.power_4);
			} else if (power.getPower() > 50) {
				iv_power.setImageResource(R.drawable.power_3);
			} else if (power.getPower() > 30) {
				iv_power.setImageResource(R.drawable.power_2);
			} else if (power.getPower() > 10) {
				iv_power.setImageResource(R.drawable.power_1);
			} else {
				iv_power.setImageResource(R.drawable.power_0);
			}
		}
	}

	private void fillData() {
		// 設置版本號
		if (isAdded()) {
			String appVerShow = String.format(mActivity.getResources()
					.getString(R.string.DM_Sidebar_APP_Version), AndroidConfig
					.getVersionName(mActivity));
			tv_set_app_ver.setText(appVerShow);

			WifiManager wifiMgr = (WifiManager) mActivity.getApplicationContext()
					.getSystemService(Context.WIFI_SERVICE);
			WifiInfo info = wifiMgr.getConnectionInfo();
			String ssid = info != null ? info.getSSID() : null;

			if (ssid != null && ssid.length() > 1) {
				tv_layout_menu_wifi_status.setText(ssid);
			} else {
				tv_layout_menu_wifi_status
						.setText(getString(R.string.DM_Sidebar_Unconnect));
				resetUI();
			}

		}

	}

	private void initViews() {

		EventBus.getDefault().register(this);

		tv_layout_menu_wifi_status = (TextView) rootView
				.findViewById(R.id.tv_layout_menu_wifi_status);
		tv_layout_menu_network_status = (TextView) rootView
				.findViewById(R.id.tv_layout_menu_network_status);
		tv_layout_menu_no_devive = (TextView) rootView
				.findViewById(R.id.tv_layout_menu_cur_devive_ssid);
		tv_set_app_ver = (TextView) rootView.findViewById(R.id.tv_set_app_ver);
		tv_set_fw_ver = (TextView) rootView.findViewById(R.id.tv_set_fw_ver);
		tv_set_device_ip = (TextView) rootView.findViewById(R.id.tv_set_device_ip);
		tv_set_fw_ver.setOnClickListener(this);

		iv_layout_menu_device_icon = (ImageView) rootView
				.findViewById(R.id.iv_layout_menu_device_icon);
		int height = DisplayUtils.getScreenHeightPixels(getActivity());
		if(height >0) {
			height = height/10;
			ViewGroup.LayoutParams param = iv_layout_menu_device_icon.getLayoutParams();
			param.height = height;
			iv_layout_menu_device_icon.setLayoutParams(param);
		}


		iv_layout_new_tips = (ImageView) rootView
				.findViewById(R.id.iv_layout_new_tips);
		rlyt_layout_menu_network = (RelativeLayout) rootView
				.findViewById(R.id.rlyt_layout_menu_network);
		rlyt_layout_menu_wifi = (RelativeLayout) rootView
				.findViewById(R.id.rlyt_layout_menu_wifi);
		rlyt_layout_menu_setting = (RelativeLayout) rootView
				.findViewById(R.id.rlyt_layout_menu_setting);
		rlyt_layout_menu_download = (RelativeLayout) rootView
				.findViewById(R.id.rlyt_layout_menu_download);
		rlyt_layout_menu_backup_manual = (RelativeLayout) rootView
				.findViewById(R.id.rlyt_layout_menu_backup_manual);
		rlyt_layout_menu_backup_auto = (RelativeLayout) rootView
				.findViewById(R.id.rlyt_layout_menu_backup_auto);
		layout_info = (RelativeLayout) rootView.findViewById(R.id.layout_info);
		rlyt_layout_menu_dropbox = (RelativeLayout) rootView
				.findViewById(R.id.rlyt_layout_menu_dropbox);
		rlyt_layout_menu_vault = (RelativeLayout) rootView
				.findViewById(R.id.rlyt_layout_menu_vault);

		layout_device_info = (LinearLayout) rootView
				.findViewById(R.id.layout_device_info);
		storageProgressBar = (ProgressBar) rootView
				.findViewById(R.id.device_storage_progress);
		iv_power = (ImageView) rootView.findViewById(R.id.device_power);
		text_curDevice = (TextView) rootView.findViewById(R.id.device_ssid);
		text_storage = (TextView) rootView
				.findViewById(R.id.device_storage_info);

		iv_layout_menu_device_icon.setOnClickListener(this);
		rlyt_layout_menu_wifi.setOnClickListener(this);
		rlyt_layout_menu_network.setOnClickListener(this);
		rlyt_layout_menu_setting.setOnClickListener(this);
		rlyt_layout_menu_download.setOnClickListener(this);
		rlyt_layout_menu_backup_manual.setOnClickListener(this);
		rlyt_layout_menu_backup_auto.setOnClickListener(this);
		rlyt_layout_menu_dropbox.setOnClickListener(this);
		rlyt_layout_menu_vault.setOnClickListener(this);

		baking = (ProgressBar) rootView.findViewById(R.id.baking);

		mHandler = new HandlerUtil.StaticHandler() {
			@Override
			public void dispatchMessage(Message msg) {
				// TODO Auto-generated method stub
				super.dispatchMessage(msg);
			}
		};

		fillData();

		mStateCookie = DMSdk.getInstance().attachListener(
				new DMDeviceStatusChangeListener() {

					@Override
					public void onDeviceStatusChanged(int type) {
						// TODO Auto-generated method stub
						if (DMStatusType.isPowerChange(type)) {
							getPowerInfo();
						} else if (DMStatusType.isPasswordChange(type)) {
							EventBus.getDefault().post(
									new PasswordChangeEvent());
						} else if (DMStatusType.isDiskChange(type)) {
							getStorageInfo();
						}

					}
				});

		initBrocastReceiver();

	}

	private void initBrocastReceiver() {
		// TODO Auto-generated method stub
		receiver = new ReceiveBroadCast();
		IntentFilter filter = new IntentFilter();
		filter.addAction("com.dmsys.REFRESH_REMOTEAP"); // 只有持有相同的action的接受者才能接收此广播
		getActivity().registerReceiver(receiver, filter);
	}

	public class ReceiveBroadCast extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			System.out.println("receive broadcast");
			getRemoteAP();
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		int i = v.getId();
		if (i == R.id.rlyt_layout_menu_wifi) {// mActivity.startActivity(new Intent(mActivity,
			// WiFiConnectActivity.class));
			mActivity
					.startActivity(new Intent("android.settings.WIFI_SETTINGS"));

		} else if (i == R.id.iv_layout_menu_device_icon) {
			showTipDiaog();

		} else if (i == R.id.rlyt_layout_menu_network) {
			mActivity.startActivity(new Intent(mActivity,
					UDiskWiFiInternetSettingsActivity.class));

		} else if (i == R.id.rlyt_layout_menu_download) {
			mActivity.startActivity(new Intent(mActivity,
					MyDownloadActivity.class));

		} else if (i == R.id.rlyt_layout_menu_backup_manual) {
			if (mStorage != null && mStorage.getMountStatus() == 1
					&& mStorage.getStorages() != null
					&& mStorage.getStorages().size() > 0) {
				mActivity.startActivity(new Intent(mActivity,
						ManualBackupActivity.class));
			} else if (mStorage != null && mStorage.getMountStatus() == 0) {
				Toast.makeText(mActivity, R.string.DM_MDNS_Disk_Connect_PC_2,
						Toast.LENGTH_SHORT).show();
			}

		} else if (i == R.id.rlyt_layout_menu_backup_auto) {
			if (mStorage != null && mStorage.getMountStatus() == 1
					&& mStorage.getStorages() != null
					&& mStorage.getStorages().size() > 0) {
				mActivity.startActivity(new Intent(mActivity,
						AutoBackupActivity.class));
			} else if (mStorage != null && mStorage.getMountStatus() == 0) {
				Toast.makeText(mActivity, R.string.DM_MDNS_Disk_Connect_PC_2,
						Toast.LENGTH_SHORT).show();
			}

		} else if (i == R.id.rlyt_layout_menu_setting) {
			mActivity.startActivity(new Intent(mActivity,
					SettingsActivity.class));

		} else if (i == R.id.tv_set_fw_ver) {
			judgeDevelopMode();

		} else if (i == R.id.rlyt_layout_menu_dropbox) {
			mActivity.startActivity(new Intent(mActivity,
					MyDropBoxActivity.class));

		} else if (i == R.id.rlyt_layout_menu_vault) {
			getVaultStatu();

		} else {
		}
	}

	protected void showTipDiaog() {
		// TODO Auto-generated method stub
		final MessageDialog dialog = new MessageDialog(mActivity,
				UDiskBaseDialog.TYPE_TWO_BTN);
		dialog.setTitleContent(getString(R.string.DM_setting_getotaupgrade_successful_tips));
		dialog.setMessage(getString(R.string.DM_Disk_Dialog_Rescan_Device));
		dialog.setLeftBtn(getString(R.string.DM_Control_Cancel),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dlg, int which) {
						// TODO Auto-generated method stub
					}
				});

		dialog.setRightBtn(getString(R.string.DM_Control_Definite),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dlg, int which) {
						// TODO Auto-generated method stub
						EventBus.getDefault().post(new PasswordChangeEvent());
					}
				});

		dialog.show();
	}

	protected void showVaultSwitchDialog() {

		String continueStr = getString(R.string.DM_Access_Vault_Notset_Open_Toset);
		String cancelStr = getString(R.string.DM_Access_Vault_Notset_Open_Giveup);
		String[] array = new String[] { cancelStr, continueStr };

		String message = getString(R.string.DM_Access_Vault_Notset_Open);

		promptDialog = AlertDmDialogDefault.prompt(mActivity, message, null,
				new AlertDmDialogDefault.OnPromptListener() {

					@Override
					public void onPromptPositive() {
						// TODO Auto-generated method stub
						promptDialog.cancel();
						Intent mIntent = new Intent(mActivity,
								VaultSettingActivity.class);
						mActivity.startActivity(mIntent);
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

	protected void judgeDevelopMode() {
		// TODO Auto-generated method stub
		System.arraycopy(mHits, 1, mHits, 0, mHits.length - 1);
		mHits[mHits.length - 1] = SystemClock.uptimeMillis();
		if (mHits[0] >= (SystemClock.uptimeMillis() - 800)) {
			if (privateVersion != null) {
				Toast.makeText(mActivity, privateVersion, Toast.LENGTH_SHORT)
						.show();
			}
		}
	}

	public void resetUI() {
		tv_layout_menu_network_status.setText("");

		tv_layout_menu_no_devive.setVisibility(View.VISIBLE);
		layout_device_info.setVisibility(View.GONE);

		iv_layout_new_tips.setVisibility(View.GONE);
		tv_set_fw_ver.setText("");
		
		tv_set_device_ip.setVisibility(View.GONE);

	}

	public void onEventMainThread(SupportFunctionEvent event) {

		System.out.println("func type:" + event.mType);

		if (isAdded()) {

			if (event.mType == -1) {
				iv_power.setVisibility(View.GONE);
				rlyt_layout_menu_network.setVisibility(View.GONE);
				rootView.findViewById(R.id.devided_network).setVisibility(
						View.GONE);

				rlyt_layout_menu_backup_manual.setVisibility(View.GONE);
				rlyt_layout_menu_backup_auto.setVisibility(View.GONE);

				return;
			}

			if (DMSupportFunction.isSupportPower(event.mType)) {
				iv_power.setVisibility(View.GONE);
				getPowerInfo();
			} else {
				iv_power.setVisibility(View.GONE);
			}

			if (DMSupportFunction.isSupportRemoteAP(event.mType)) {
				suppotRemoteAp = true;
				rlyt_layout_menu_network.setVisibility(View.VISIBLE);
				rootView.findViewById(R.id.devided_network).setVisibility(
						View.VISIBLE);
				getClientStatus();
			} else {
				suppotRemoteAp = false;
				rlyt_layout_menu_network.setVisibility(View.GONE);
				rootView.findViewById(R.id.devided_network).setVisibility(
						View.GONE);
			}

			if (DMSupportFunction.isSupportBackup(event.mType)) {
				rlyt_layout_menu_backup_auto.setVisibility(View.GONE);
				rlyt_layout_menu_backup_manual.setVisibility(View.GONE);
			} else {
				rlyt_layout_menu_backup_manual.setVisibility(View.GONE);
				rlyt_layout_menu_backup_auto.setVisibility(View.GONE);
			}

//			if (DMSupportFunction.isSupportVault(event.mType)) {
//				rlyt_layout_menu_vault.setVisibility(View.VISIBLE);
//			} else{
//				rlyt_layout_menu_vault.setVisibility(View.GONE);
//			}

		}
	}

	public void onEventMainThread(StorageEvent event) {
		if (BaseValue.Host != null && !BaseValue.Host.equals("")) {
			getStorageInfo();
			getPowerInfo();
			if (suppotRemoteAp) {
				getClientStatus();
			}
		}
	}

	public void onEventMainThread(NewFwEvent event) {
		if (event.getOta() != null) {
			iv_layout_new_tips.setVisibility(View.VISIBLE);
		} else {
			iv_layout_new_tips.setVisibility(View.GONE);
		}
	}

	public void onEventMainThread(BackupStateEvent event) {
		if (event.mState == BackupStateEvent.BACKING) {
			baking.setVisibility(View.VISIBLE);
			lockScreen();
		} else {
			baking.setVisibility(View.GONE);
			releaseScreen();
		}
	}

	private void lockScreen() {
		mWakeLock = ((PowerManager) mActivity
				.getSystemService(Context.POWER_SERVICE)).newWakeLock(
				PowerManager.SCREEN_BRIGHT_WAKE_LOCK
						| PowerManager.ON_AFTER_RELEASE, "wakelock");
		mWakeLock.acquire();
	}

	private void releaseScreen() {
		if (mWakeLock != null) {
			mWakeLock.release();
			mWakeLock = null;
		}
	}

	public void onEventMainThread(BackupRefreshEvent event) {

		if (isResumed()) {
			if (event.type == 0) {
				if (event.message.what == BackupService.MSG_BACKUP_COMPLETE) {
					boolean backup_contacts = mActivity.getSharedPreferences(
							"BACKUP", Context.MODE_PRIVATE).getBoolean(
							"CONTACTS", false);
					if (!backup_contacts) {
						baking.setVisibility(View.GONE);
					}
					Bundle bundle = event.message.getData();
					if (bundle != null) {
						boolean ret = bundle.getBoolean(
								BackupService.RESULT_BACKUP, false);
						if (ret) {
							Toast.makeText(
									mActivity,
									getString(R.string.DM_Disk_backup_success_Picture),
									Toast.LENGTH_LONG).show();
						} else {
							int errorCode = bundle.getInt(
									BackupService.ERROR_CODE, -1);
							switch (errorCode) {
							case BackupService.ERROR_BACKUP_NO_STORAGE:
								Toast.makeText(
										mActivity,
										getString(R.string.DM_Remind_Operate_No_Disk),
										Toast.LENGTH_LONG).show();
								break;
							case BackupService.CODE_BACKUP_EXCEPTION:
								Toast.makeText(
										mActivity,
										getString(R.string.DM_Disk_backup_exception),
										Toast.LENGTH_LONG).show();
								break;
							case BackupService.CODE_BACKUP_UPLOAD_FAILED:
								long num = bundle
										.getLong(BackupService.RESULT_BACKEDUP_NUMBER);
								Toast.makeText(
										mActivity,
										String.format(
												getString(R.string.DM_Disk_backup_fail_with_num),
												String.valueOf(num)), Toast.LENGTH_LONG).show();
								break;
							case BackupService.CODE_BACKEDUP_FILE:
								Toast.makeText(
										mActivity,
										getString(R.string.DM_Disk_the_selected_file_has_been_backup),
										Toast.LENGTH_LONG).show();
								break;
							case BackupService.CODE_BACKUP_IS_USER_STOP:
								Toast.makeText(
										mActivity,
										getString(R.string.DM_Remind_Operate_Stop),
										Toast.LENGTH_LONG).show();
								break;

							default:
								break;
							}
						}
					}
				}
			} else if (event.type == 1) {
				if (event.message.what == BackupService.MSG_BACKUP_COMPLETE) {
					baking.setVisibility(View.GONE);
					Bundle bundle = event.message.getData();
					if (bundle != null) {
						boolean ret = bundle.getBoolean(
								BackupService.RESULT_BACKUP, false);
						if (ret) {
							System.out
									.println("menu DM_Disk_backup_success_Contacts");
							Toast.makeText(
									mActivity,
									getString(R.string.DM_Disk_backup_success_Contacts),
									Toast.LENGTH_LONG).show();
						} else {
							int errorCode = bundle.getInt(
									BackupService.ERROR_CODE, -1);
							switch (errorCode) {
							case BackupService.ERROR_BACKUP_NO_STORAGE:
								Toast.makeText(
										mActivity,
										getString(R.string.DM_Remind_Operate_No_Disk),
										Toast.LENGTH_LONG).show();
								break;
							case BackupService.CODE_BACKUP_EXCEPTION:
								Toast.makeText(
										mActivity,
										getString(R.string.DM_Disk_backup_exception),
										Toast.LENGTH_LONG).show();
								break;
							case BackupService.CODE_BACKUP_UPLOAD_FAILED:
								Toast.makeText(
										mActivity,
										getString(R.string.DM_Disk_backup_fail_Contacts),
										Toast.LENGTH_LONG).show();
								break;
							// 已经备好了
							case BackupService.CODE_BACKEDUP_FILE:
								Toast.makeText(
										mActivity,
										getString(R.string.DM_Disk_contacts_has_been_backup),
										Toast.LENGTH_LONG).show();
								break;
							case BackupService.CODE_BACKUP_NO_FILE:
								Toast.makeText(
										mActivity,
										getString(R.string.DM_Disk_have_no_contacts),
										Toast.LENGTH_LONG).show();
								break;
							case BackupService.CODE_BACKUP_IS_USER_STOP:
								Toast.makeText(
										mActivity,
										getString(R.string.DM_Remind_Operate_Stop),
										Toast.LENGTH_LONG).show();
								break;

							default:
								break;
							}
						}
					}
				}
			}
		}

	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		System.out.println("menu onDestroy");
		super.onDestroy();
		EventBus.getDefault().unregister(this);
		// DMSdk.getInstance().removeListener(mDeviceCookie);
		DMSdk.getInstance().removeListener(mStateCookie);
		getActivity().unregisterReceiver(receiver);
		privateVersion = null;
	}

}
