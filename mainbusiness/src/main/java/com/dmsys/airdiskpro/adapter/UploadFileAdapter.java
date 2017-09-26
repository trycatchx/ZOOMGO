package com.dmsys.airdiskpro.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.dmsys.airdiskpro.GlobalImageLRUCacher;
import com.dmsys.airdiskpro.GlobalImageLRUCacher.DecodeBitmapCallBack;
import com.dmsys.airdiskpro.ui.UploadBaseActivity.Mode;
import com.dmsys.airdiskpro.utils.ConvertUtil;
import com.dmsys.airdiskpro.utils.FileUtil;
import com.dmsys.dmsdk.model.DMFile;
import com.dmsys.dmsdk.model.DMFileCategoryType;
import com.dmsys.mainbusiness.R;

import java.util.List;

public class UploadFileAdapter extends BaseAdapter {

	private List<DMFile> list ;
	private Context mContext;
	private LayoutInflater inflater = null;
	private Mode mMode = Mode.MODE_NORMAL;
	private Handler mHandler;

	
	public Mode getmMode() {
		return mMode;
	}

	public void setmMode(Mode mMode) {
		this.mMode = mMode;
	}

	public UploadFileAdapter(List<DMFile> list, Context mContext) {
		super();
		inflater = LayoutInflater.from(mContext);
		this.list = list;
		this.mContext = mContext;
		mHandler = new Handler();
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return list.get(position);
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
			convertView = inflater.inflate(R.layout.file_browse_upload_file_list_item, null);
			initHolder(holder, convertView);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		loadData2Holder(holder,list.get(position).getType(),position,list.get(position)) ;

		return convertView;
	}
	
	private void loadData2Holder(ViewHolder holder,DMFileCategoryType mType,int position,DMFile file) {
		
		Bitmap b = null;
		
		if (file.mType == DMFileCategoryType.E_VIDEO_CATEGORY) {
			b = GlobalImageLRUCacher.getInstance(mContext).getVideoThumbnail(file.mPath, holder.ivIcon, new DecodeBitmapCallBack() {
				public void callback(Bitmap bmp, Object flag) {
					if (null != bmp) {
						//mHandler.obtainMessage(MSG_NOTIFY_DATA_SET_CHANGED).sendToTarget();
						System.out.println("gggget bitmap");
						mHandler.post(new Runnable() {
							
							@Override
							public void run() {
								// TODO Auto-generated method stub
								notifyDataSetChanged();
							}
						});
					}
				}
			});
		}
		
		if (b == null) {
			System.out.println("gggget null");
			holder.ivIcon.setImageResource(FileUtil.getFileLogo(file.getType()));
		}else {
			holder.ivIcon.setImageBitmap(b);
		}
		
		
		holder.tvName.setText(file.getName());
		holder.tvSize.setText(ConvertUtil.byteConvert(file.mSize, false));
		holder.tvDate.setText(file.getLastModified("yyyy-MM-dd"));
		
		if (mMode == Mode.MODE_EDIT) {
			holder.ivChoose.setSelected(file.selected);
			holder.ivChoose.setVisibility(View.VISIBLE);
		} else {
			holder.ivChoose.setSelected(false);
			holder.ivChoose.setVisibility(View.INVISIBLE);
		}
	}
	
	
	private void initHolder(ViewHolder holder,View convertView) { 
		holder.tvName = (TextView) convertView.findViewById(R.id.fileexplorer_list_item_name);
		holder.tvSize = (TextView) convertView.findViewById(R.id.fileexplorer_list_item_size);
		holder.tvDate = (TextView) convertView.findViewById(R.id.fileexplorer_list_item_date);
		
		holder.ivIcon = (ImageView) convertView.findViewById(R.id.fileexplorer_list_item_icon);
		holder.ivChoose = (ImageView) convertView.findViewById(R.id.fileexplorer_list_item_operatinobtn);
	}
	
	
	private class ViewHolder {
		public TextView tvDate;	
		public TextView tvName;	
		public TextView tvSize;	
		public ImageView ivIcon;
		public ImageView ivChoose;
		
	}

}
