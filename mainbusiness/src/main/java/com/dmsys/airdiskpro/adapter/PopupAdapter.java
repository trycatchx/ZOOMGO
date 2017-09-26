package com.dmsys.airdiskpro.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.dmsys.mainbusiness.R;

import java.util.List;

public class PopupAdapter extends BaseAdapter {

    private Context mContext;
    private List<String> mData;

    public PopupAdapter(Context context) {
        mContext = context;
    }

    public void setData(List<String> data) {
        this.mData = data;
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        ViewHolder mHolder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.popup_item, null);
            mHolder = new ViewHolder();
            mHolder.mTextView = (TextView) convertView.findViewById(R.id.item_name);
            convertView.setTag(mHolder);
        } else {
            mHolder = (ViewHolder) convertView.getTag();
        }

        mHolder.mTextView.setText(mData.get(position));

        return convertView;
    }

    public class ViewHolder {
        public TextView mTextView;
    }


}
