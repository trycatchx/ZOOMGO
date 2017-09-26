package com.dmsys.vlcplayer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.dmsys.vlcplayer.R;
import com.dmsys.vlcplayer.subtitle.ChooseSrtBean;

import java.util.List;


public class ChooseSrtAdapter extends BaseAdapter {

	private Context mContext;
	private List<ChooseSrtBean> mData;
	private LayoutInflater inflater = null;
	public int selectedPosition = -1;

	public ChooseSrtAdapter(Context mContext, List<ChooseSrtBean> mData) {
		super();
		this.mContext = mContext;
		this.mData = mData;
		inflater = LayoutInflater.from(mContext);
	}

	public void clear() {
		this.mData.clear();
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mData.size();
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return mData.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return arg0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ChooseSrtAdapterHolder holder = null;
		if (convertView == null) {
			holder = new ChooseSrtAdapterHolder();
			convertView = inflater.inflate(R.layout.popuwindow_choose_srt,
					parent, false);
			initHolder(holder, convertView);
			convertView.setTag(holder);
		} else {
			holder = (ChooseSrtAdapterHolder) convertView.getTag();
		}
		holder.tv_choose_item.setText(mData.get(position).fileName);

		if (selectedPosition >= 0 && selectedPosition < getCount()
				&& position == selectedPosition) {

			holder.tv_choose_item.setTextColor(mContext.getResources()
					.getColor(R.color.vod_player_srt_selected));

		} else {
			holder.tv_choose_item.setTextColor(mContext.getResources()
					.getColor(R.color.vod_player_text_white));

		}

		return convertView;
	}

	private void initHolder(ChooseSrtAdapterHolder holder, View convertView) {
		holder.tv_choose_item = (TextView) convertView;
	}

	class ChooseSrtAdapterHolder {
		TextView tv_choose_item;
	}

}
