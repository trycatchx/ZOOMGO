package com.dmsys.airdiskpro.view;

import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.dmsys.mainbusiness.R;

public class UDiskListViewDialog extends UDiskBaseDialog {

	public interface MyItemClickListener{
		public void onClick(DialogInterface dialog, int id);
	};
	private MyItemClickListener myItemClickListener;

	public void setMyItemClickListener(MyItemClickListener myItemClickListener) {
		this.myItemClickListener = myItemClickListener;
	}

	public UDiskListViewDialog(Context context, int type, ListAdapter adapter,MyItemClickListener clickListener) {
		super(context);
		// TODO Auto-generated constructor stub
		super.initView(type, R.layout.dialog_listview);
		setMyItemClickListener(clickListener);
		setWidthDip(300);
		((ListView) getCustomView().findViewById(R.id.lv_content)).setAdapter(adapter);
		((ListView) getCustomView().findViewById(R.id.lv_content)).setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				// TODO Auto-generated method stub
				myItemClickListener.onClick(UDiskListViewDialog.this, arg2);
			}
		});
		this.setCancelable(true);
	}

}
