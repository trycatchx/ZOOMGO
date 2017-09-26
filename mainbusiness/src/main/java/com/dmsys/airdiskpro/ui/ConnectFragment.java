package com.dmsys.airdiskpro.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dm.NetWork.NetWorkUtils;
import com.dmsys.mainbusiness.R;

public class ConnectFragment extends Fragment implements OnClickListener {


    private View parent;

    private Activity activity;

    private LinearLayout layout_searching;
    private LinearLayout layout_noDevice;
    private LinearLayout layout_tip;

    private Button bt_connect;
    private TextView messageText;
    private ImageView img_guide;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        parent = inflater.inflate(R.layout.fragment_connect, null);
        initViews();
        return parent;
    }


    @Override
    public void onAttach(Activity activity) {
        // TODO Auto-generated method stub
        super.onAttach(activity);
        this.activity = (MainActivity) activity;
    }

    private void initViews() {
        // TODO Auto-generated method stub
        layout_searching = (LinearLayout) parent.findViewById(R.id.searching);
        layout_noDevice = (LinearLayout) parent.findViewById(R.id.nodevice);

        bt_connect = (Button) parent.findViewById(R.id.connect);
        bt_connect.setOnClickListener(this);

        layout_tip = (LinearLayout) parent.findViewById(R.id.layout_tip);

        messageText = (TextView) parent.findViewById(R.id.message);

        img_guide = (ImageView) parent.findViewById(R.id.img_guide);
    }

    public void showNoDeviceView() {
        System.out.println("showNoDeviceView");
        layout_searching.setVisibility(View.GONE);
        layout_noDevice.setVisibility(View.VISIBLE);

        String msg = null;

        boolean connected = NetWorkUtils.isWiFiConnected(this.activity);
        System.out.println("showNoDeviceView connected:" + connected);

        if (isAdded()) {

            if (connected) {

                msg = getString(R.string.DM_MDNS_Scan_None);

                bt_connect.setText(R.string.DM_MDNS_rescan_Button);

                img_guide.setVisibility(View.GONE);
                layout_tip.setVisibility(View.VISIBLE);

            } else {
                msg = getString(R.string.DM_MDNS_Disconect_WiFi);

                bt_connect.setText(R.string.DM_MDNS_Connect_WiFi_Button);

                img_guide.setVisibility(View.VISIBLE);
                layout_tip.setVisibility(View.GONE);
            }

            messageText.setText(msg);

        }

    }

    public void showCheckingView() {
        System.out.println("showCheckingView");
        layout_searching.setVisibility(View.VISIBLE);
        layout_noDevice.setVisibility(View.GONE);
    }

    public boolean isChecking() {
        if (layout_searching.getVisibility() == View.VISIBLE) {
            return true;
        }
        return false;
    }

    @Override
    public void onClick(View v) {

        int i = v.getId();
        if (i == R.id.connect) {
            onclickConnect();

        } else {
        }
    }

    private void onclickConnect() {
        // TODO Auto-generated method stub
        if (bt_connect.getText().equals(getString(R.string.DM_MDNS_Connect_WiFi_Button))) {
            //startActivity(new Intent(activity, WiFiConnectActivity.class));
            startActivity(new Intent("android.settings.WIFI_SETTINGS"));
        } else {
            if (MainActivity.class.isInstance(getActivity())) {
                ((MainActivity) getActivity()).manualCheckDevice(false);
            }
        }
    }


    @Override
    public void onDestroyView() {
        // TODO Auto-generated method stub
        super.onDestroyView();
    }

    private static final String STATE_SAVE_IS_HIDDEN = "STATE_SAVE_IS_HIDDEN";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            boolean isSupportHidden = savedInstanceState.getBoolean(STATE_SAVE_IS_HIDDEN);

            FragmentTransaction ft = getFragmentManager().beginTransaction();
            if (isSupportHidden) {
                ft.hide(this);
            } else {
                ft.show(this);
            }
            ft.commit();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_SAVE_IS_HIDDEN, isHidden());
    }


}
