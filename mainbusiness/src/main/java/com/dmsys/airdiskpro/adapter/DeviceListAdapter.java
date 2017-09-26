package com.dmsys.airdiskpro.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.dmsys.dmsdk.model.DMDevice;
import com.dmsys.mainbusiness.R;

import java.util.List;

public class DeviceListAdapter extends BaseAdapter {

	private List<DMDevice> devices;
	private LayoutInflater inflater;
	
	public DeviceListAdapter(Context context, List<DMDevice> deviceList) {
		this.devices = deviceList;
		inflater = LayoutInflater.from(context);
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return devices.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return devices.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder holder = null;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = inflater.inflate(R.layout.device_dialog_listview_item, null);
			holder.textviewSsid = (TextView) convertView.findViewById(R.id.tv_device_dialog_listview_item_ssid);
			holder.textviewIp = (TextView) convertView.findViewById(R.id.tv_device_dialog_listview_item_ip);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		String ssid = devices.get(position).getName();
		if(ssid != null && !ssid.equals("")) {
			holder.textviewSsid.setText((String) devices.get(position).getName());
		} else {
			holder.textviewSsid.setText("--");
		}
		
		holder.textviewIp.setText((String) devices.get(position).getIp());
		return convertView;
	}

	public final class ViewHolder {
		public TextView textviewSsid;
		public TextView textviewIp;
	}
}
