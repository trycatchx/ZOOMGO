package com.dmsys.airdiskpro.view;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.dmairdisk.aodplayer.util.CommonAsync;
import com.dmsys.dmsdk.DMSdk;
import com.dmsys.dmsdk.model.DMFileSort;
import com.dmsys.mainbusiness.R;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DMSortDialog extends UDiskBaseDialog implements View.OnClickListener{

	private ImageView cb_name_aesc,cb_name_desc;
	private ImageView cb_size_aesc,cb_size_desc;
	private ImageView cb_time_aesc,cb_time_desc;
	
	private RelativeLayout layout_name,layout_date,layout_size;
	
	private int mType,mOrder;
	
	public DMSortDialog(Context context, int type) {
		// TODO Auto-generated constructor stub
		super(context);
		super.initView(type, R.layout.dialog_sort);
		this.setCancelable(true);
		initViews();
	}

	private void initViews() {
		// TODO Auto-generated method stub
		View rootView = getCustomView();
		cb_name_aesc = (ImageView) rootView.findViewById(R.id.cb_name_aesc);
		cb_name_desc = (ImageView) rootView.findViewById(R.id.cb_name_desc);
		cb_size_aesc = (ImageView) rootView.findViewById(R.id.cb_size_aesc);
		cb_size_desc = (ImageView) rootView.findViewById(R.id.cb_size_desc);
		cb_time_aesc = (ImageView) rootView.findViewById(R.id.cb_time_aesc);
		cb_time_desc = (ImageView) rootView.findViewById(R.id.cb_time_desc);
		cb_name_aesc.setOnClickListener(this);
		cb_name_desc.setOnClickListener(this);
		cb_size_aesc.setOnClickListener(this);
		cb_size_desc.setOnClickListener(this);
		cb_time_aesc.setOnClickListener(this);
		cb_time_desc.setOnClickListener(this);
		
		layout_date = (RelativeLayout) rootView.findViewById(R.id.layout_time);
		layout_name = (RelativeLayout) rootView.findViewById(R.id.layout_name);
		layout_size = (RelativeLayout) rootView.findViewById(R.id.layout_size);
		
		getCurrentSortInfo();
	}

	
	private void getCurrentSortInfo() {
		// TODO Auto-generated method stub
		CommonAsync.Runnable runnable = new CommonAsync.Runnable() {
			
			@Override
			public void stop() {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public Object run() {
				// TODO Auto-generated method stub
				mType = DMSdk.getInstance().getFileSortType();
				mOrder = DMSdk.getInstance().getFileSortOrder();
				return 0;
			}
		};
		
		CommonAsync.CommonAsyncListener listener = new CommonAsync.CommonAsyncListener() {
			
			@Override
			public void onResult(Object ret) {
				// TODO Auto-generated method stub
				
				cb_name_aesc.setSelected(false);
				cb_name_desc.setSelected(false);
				cb_size_aesc.setSelected(false);
				cb_size_desc.setSelected(false);
				cb_time_aesc.setSelected(false);
				cb_time_desc.setSelected(false);
				
				if (mType == DMFileSort.SORT_TYPE_NAME) {
					if (mOrder == DMFileSort.SORT_ORDER_DOWN) {
						cb_name_desc.setSelected(true);
					}else if (mOrder == DMFileSort.SORT_ORDER_UP) {
						cb_name_aesc.setSelected(true);
					}
				}else if (mType == DMFileSort.SORT_TYPE_SIZE) {
					if (mOrder == DMFileSort.SORT_ORDER_DOWN) {
						cb_size_desc.setSelected(true);
					}else if (mOrder == DMFileSort.SORT_ORDER_UP) {
						cb_size_aesc.setSelected(true);
					}
				}else if (mType == DMFileSort.SORT_TYPE_TIME) {
					if (mOrder == DMFileSort.SORT_ORDER_DOWN) {
						cb_time_desc.setSelected(true);
					}else if (mOrder == DMFileSort.SORT_ORDER_UP) {
						cb_time_aesc.setSelected(true);
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
		
		CommonAsync task = new CommonAsync(runnable, listener);
		ExecutorService FULL_TASK_EXECUTOR = (ExecutorService) Executors.newCachedThreadPool();
		task.executeOnExecutor(FULL_TASK_EXECUTOR);
	}
	
	public int getCurrentSortType(){
		if (cb_name_desc.isSelected() || cb_name_aesc.isSelected()) {
			return DMFileSort.SORT_TYPE_NAME;
		}else if (cb_time_desc.isSelected() || cb_time_aesc.isSelected()) {
			return DMFileSort.SORT_TYPE_TIME;
		}else if (cb_size_desc.isSelected() || cb_size_aesc.isSelected()) {
			return DMFileSort.SORT_TYPE_SIZE;
		}
		return -1;
	}
	
	public int getCurrentSortOrder(){
		if (cb_name_desc.isSelected() || cb_time_desc.isSelected() || cb_size_desc.isSelected()) {
			return DMFileSort.SORT_ORDER_DOWN;
		}else if (cb_name_aesc.isSelected() || cb_time_aesc.isSelected() || cb_size_aesc.isSelected()) {
			return DMFileSort.SORT_ORDER_UP;
		}
		return -1;
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		cb_name_aesc.setSelected(false);
		cb_name_desc.setSelected(false);
		cb_size_aesc.setSelected(false);
		cb_size_desc.setSelected(false);
		cb_time_aesc.setSelected(false);
		cb_time_desc.setSelected(false);

		int i = v.getId();
		if (i == R.id.cb_name_aesc) {
			cb_name_aesc.setSelected(true);

		} else if (i == R.id.cb_name_desc) {
			cb_name_desc.setSelected(true);

		} else if (i == R.id.cb_size_aesc) {
			cb_size_aesc.setSelected(true);

		} else if (i == R.id.cb_size_desc) {
			cb_size_desc.setSelected(true);

		} else if (i == R.id.cb_time_aesc) {
			cb_time_aesc.setSelected(true);

		} else if (i == R.id.cb_time_desc) {
			cb_time_desc.setSelected(true);

		} else {
		}
	}
	
}
