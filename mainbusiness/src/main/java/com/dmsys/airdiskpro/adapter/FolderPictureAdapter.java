package com.dmsys.airdiskpro.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dmsys.airdiskpro.model.MediaFolder;
import com.dmsys.dmsdk.model.DMFile;
import com.dmsys.mainbusiness.R;

import java.util.ArrayList;
import java.util.List;

import cn.dm.longsys.library.imageloader.core.DMImageLoader;
import cn.dm.longsys.library.imageloader.core.DisplayImageOptions;


public class FolderPictureAdapter extends BaseAdapter {

	private LayoutInflater inflater = null;
	List<MediaFolder> mGroupDatas = new ArrayList<>();
	DMImageLoader mDMImageLoader;
	DisplayImageOptions mLoaderOptions;
	int imageRLWIdth;
	Context mContext;


	public FolderPictureAdapter(Context mContext,List<MediaFolder> mGroupDatas, int imageRLWIdth) {
		super();
		this.mContext = mContext;
		inflater = LayoutInflater.from(mContext);
		this.mGroupDatas = mGroupDatas;
		this.imageRLWIdth = imageRLWIdth;
		mDMImageLoader = DMImageLoader.getInstance();
		initLoaderOptions();
	}

	private void initLoaderOptions() {
		/**
		 * imageloader的新包导入
		 */
		mLoaderOptions = new DisplayImageOptions.Builder()
				.cacheInMemory(true)
				.showImageOnFail(R.drawable.filemanager_photo_fail)
				.useThumb(true)
				.cacheOnDisk(true)
				.showImageOnLoading(R.drawable.ready_to_loading_image)
				.showImageForEmptyUri(R.drawable.filemanager_photo_fail)
				.build();
	}

	@Override
	public int getCount() {

		return mGroupDatas.size();
	}

	@Override
	public Object getItem(int position) {
		return mGroupDatas.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		PicHolder holder = null;

		if (convertView == null) {
			holder = new PicHolder();
			convertView = inflater.inflate(R.layout.file_browse_folder_item, null);
			initHolder(holder, convertView);
			convertView.setTag(holder);
		} else {
			holder = (PicHolder) convertView.getTag();
		}

		loadData2Holder(mGroupDatas.get(position), mDMImageLoader,mLoaderOptions, holder, imageRLWIdth);

		return convertView;
	}

	private class PicHolder {
		public LinearLayout llyt_grid_item;
		public ImageView fileexplorer_grid_item_icon_0;
		public ImageView fileexplorer_grid_item_icon_1;
		public ImageView fileexplorer_grid_item_icon_2;
		public ImageView fileexplorer_grid_item_icon_3;
		public TextView tv_grid_item_folder_name;
		public TextView tv_grid_item_folder_count;
	}

	private boolean hasIllegalChar(String str) {
		String pwdLegalChar = "*|:\"<>?/\\";
		// String pwdLegalChar = "s";
		for (int i = 0; i < str.length(); i++) {
			if ((pwdLegalChar.indexOf(str.charAt(i)) >= 0)) {
				return true;
			}
		}
		return false;
	}

	private void initHolder(PicHolder holder, View convertView) {
		holder.llyt_grid_item = (LinearLayout) convertView
				.findViewById(R.id.llyt_grid_item);
		holder.fileexplorer_grid_item_icon_0 = (ImageView) convertView
				.findViewById(R.id.fileexplorer_grid_item_icon_0);
		holder.fileexplorer_grid_item_icon_1 = (ImageView) convertView
				.findViewById(R.id.fileexplorer_grid_item_icon_1);
		holder.fileexplorer_grid_item_icon_2 = (ImageView) convertView
				.findViewById(R.id.fileexplorer_grid_item_icon_2);
		holder.fileexplorer_grid_item_icon_3 = (ImageView) convertView
				.findViewById(R.id.fileexplorer_grid_item_icon_3);
		holder.tv_grid_item_folder_name = (TextView) convertView
				.findViewById(R.id.tv_grid_item_folder_name);
		holder.tv_grid_item_folder_count = (TextView) convertView
				.findViewById(R.id.tv_grid_item_folder_count);
	}

	private void loadData2Holder(MediaFolder folder, DMImageLoader imageLoader,
			DisplayImageOptions options, PicHolder holder, int width) {
		resetImageRLayoutSize(holder, width);
		// loadOperatorDate(holder.iv_line_operatinobtn, folder.isSelected());
		loadFolderInfo(holder, folder.getFolderName(), folder.getMediaInfoList().size(), width);

		// holder.mrl_media_set_item.setFolder(folder);
		if (folder.getMediaInfoList().size() == 1) {
			holder.fileexplorer_grid_item_icon_0.setVisibility(View.VISIBLE);
			holder.fileexplorer_grid_item_icon_1.setVisibility(View.INVISIBLE);
			holder.fileexplorer_grid_item_icon_2.setVisibility(View.INVISIBLE);
			holder.fileexplorer_grid_item_icon_3.setVisibility(View.INVISIBLE);
			loadMedia(imageLoader, options,holder.fileexplorer_grid_item_icon_0, folder
							.getMediaInfoList().get(0));

		} else if (folder.getMediaInfoList().size() == 2) {
			holder.fileexplorer_grid_item_icon_0.setVisibility(View.VISIBLE);
			holder.fileexplorer_grid_item_icon_1.setVisibility(View.VISIBLE);
			holder.fileexplorer_grid_item_icon_2.setVisibility(View.INVISIBLE);
			holder.fileexplorer_grid_item_icon_3.setVisibility(View.INVISIBLE);
			loadMedia(imageLoader, options,
					holder.fileexplorer_grid_item_icon_0, folder
							.getMediaInfoList().get(0));
			loadMedia(imageLoader, options,
					holder.fileexplorer_grid_item_icon_1, folder
							.getMediaInfoList().get(1));
		} else if (folder.getMediaInfoList().size() == 3) {
			holder.fileexplorer_grid_item_icon_0.setVisibility(View.VISIBLE);
			holder.fileexplorer_grid_item_icon_1.setVisibility(View.VISIBLE);
			holder.fileexplorer_grid_item_icon_2.setVisibility(View.VISIBLE);
			holder.fileexplorer_grid_item_icon_3.setVisibility(View.INVISIBLE);
			loadMedia(imageLoader, options,
					holder.fileexplorer_grid_item_icon_0, folder
							.getMediaInfoList().get(0));
			loadMedia(imageLoader, options,
					holder.fileexplorer_grid_item_icon_1, folder
							.getMediaInfoList().get(1));
			loadMedia(imageLoader, options,
					holder.fileexplorer_grid_item_icon_2, folder
							.getMediaInfoList().get(2));
		} else if (folder.getMediaInfoList().size() >= 4) {
			holder.fileexplorer_grid_item_icon_0.setVisibility(View.VISIBLE);
			holder.fileexplorer_grid_item_icon_1.setVisibility(View.VISIBLE);
			holder.fileexplorer_grid_item_icon_2.setVisibility(View.VISIBLE);
			holder.fileexplorer_grid_item_icon_3.setVisibility(View.VISIBLE);
			loadMedia(imageLoader, options,
					holder.fileexplorer_grid_item_icon_0, folder
							.getMediaInfoList().get(0));
			loadMedia(imageLoader, options,
					holder.fileexplorer_grid_item_icon_1, folder
							.getMediaInfoList().get(1));
			loadMedia(imageLoader, options,
					holder.fileexplorer_grid_item_icon_2, folder
							.getMediaInfoList().get(2));
			loadMedia(imageLoader, options,
					holder.fileexplorer_grid_item_icon_3, folder
							.getMediaInfoList().get(3));
		}
	}

	private void resetImageRLayoutSize(PicHolder holder, int width) {
		LinearLayout.LayoutParams param = (LinearLayout.LayoutParams) holder.llyt_grid_item
				.getLayoutParams();
		param.height = width;
		param.width = width;
		holder.llyt_grid_item.setLayoutParams(param);
	}

	private void reSetImageSize(ImageView iv, int width, int height) {
		RelativeLayout.LayoutParams param = (RelativeLayout.LayoutParams) iv
				.getLayoutParams();
		param.height = height;
		param.width = width;
		iv.setLayoutParams(param);
	}

	private void loadFolderInfo(PicHolder holder, String folderName, int num,
			int RLWidth) {
		String numToShow = String.format(
				mContext.getResources().getString(
						R.string.DM_Disk_Backup_Media_Folder_Num),
				String.valueOf(num));
		holder.tv_grid_item_folder_name.setText(folderName);
		holder.tv_grid_item_folder_name.setMaxWidth(RLWidth * 2 / 3);
		holder.tv_grid_item_folder_count.setText(numToShow);
	}

	private void loadOperatorDate(ImageView operView, boolean selected) {
		// operView.setVisibility(View.VISIBLE);
		operView.setSelected(selected);
	}

	private void loadImage(DMImageLoader imageLoader,
			DisplayImageOptions options, final ImageView iconview,
			final String uri) {
		iconview.setScaleType(ScaleType.CENTER_CROP);
		String uriToUse = "file://" + uri;
		imageLoader.displayImage(uriToUse, iconview, options);
	}

	private void loadMedia(DMImageLoader imageLoader,
			DisplayImageOptions options, ImageView iconView, DMFile info) {
		loadImage(imageLoader, options, iconView, info.getPath());

	}

	private MediaFolder findItemUseHash(ArrayList<MediaFolder> folderList,long hash) {
		for (int i = 0; i < folderList.size(); i++) {
			if (folderList.get(i).getParentHash() == hash) {
				return folderList.get(i);
			} else {
				continue;
			}
		}
		return null;
	}

	public static int dip2px(Context context, float dpValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}

}
