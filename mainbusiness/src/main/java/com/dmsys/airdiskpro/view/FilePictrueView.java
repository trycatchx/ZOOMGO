package com.dmsys.airdiskpro.view;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dmsys.airdiskpro.filemanager.EditState;
import com.dmsys.airdiskpro.filemanager.FileManager;
import com.dmsys.airdiskpro.filemanager.FileOperationHelper;
import com.dmsys.airdiskpro.filemanager.IItemLoader;
import com.dmsys.airdiskpro.filemanager.PictrueGroupLoader;
import com.dmsys.airdiskpro.filemanager.PictrueLoader;
import com.dmsys.airdiskpro.model.DirViewStateChangeEvent;
import com.dmsys.airdiskpro.model.MulPictrueGroup;
import com.dmsys.airdiskpro.model.PicsUnit;
import com.dmsys.airdiskpro.model.PictrueGroup;
import com.dmsys.airdiskpro.ui.FolderImageActivity;
import com.dmsys.airdiskpro.ui.imagereader.ImagePagerActivity;
import com.dmsys.airdiskpro.utils.DipPixelUtil;
import com.dmsys.airdiskpro.utils.FileInfoUtils;
import com.dmsys.airdiskpro.utils.HandlerUtil;
import com.dmsys.dmsdk.model.DMDir;
import com.dmsys.dmsdk.model.DMFile;
import com.dmsys.dmsdk.model.DMFileCategoryType;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.dmsys.mainbusiness.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import cn.dm.longsys.library.imageloader.core.DMImageLoader;
import cn.dm.longsys.library.imageloader.core.DisplayImageOptions;
import cn.dm.longsys.library.imageloader.core.assist.FailReason;
import cn.dm.longsys.library.imageloader.core.listener.ImageLoadingListener;
import cn.dm.longsys.library.imageloader.core.listener.PauseOnScrollListener;
import de.greenrobot.event.EventBus;

public class FilePictrueView  extends FrameLayout implements IFileExplorer{

	public static final int MSG_NOTIFY_DATA_SET_CHANGED = HandlerUtil.generateId();
	public static final int REQUEST_CODE_PICTRUE = 0;
	private static final String TAG = FilePictrueView.class.getSimpleName();
	public static boolean picScrolling = false;
	private List<MulPictrueGroup> mMulPictrueGroupList = new ArrayList<>(); 
	private ArrayList<String> mSelectedFolderPathList = new ArrayList<String>();
	
	//没有划分成4个为一列表的数据
	private List<ArrayList<DMFile>> groupDatas = new ArrayList<>();
	private List<PicsUnit> mPicsUnitList = new ArrayList<>();
	private ArrayList<String> mSelectedIDList = new ArrayList<>();
	
	private IItemLoader mItemLoader;

	// private PictureGroup groupView;
	public interface EditChangedListener {
		public void editChanged(List<DMFile> datas);
	}

	public interface LoadItemCompleteListener {
		public void onLoadItemComplete(List<DMFile> datas);
	}

	public interface OnFileLongClickListener {
		public boolean onItemClick(AdapterView<?> parent, View view, int position, long id);

		public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id);
	}

	/** 是否为编辑模式 */
	private int mState = EditState.STATE_NORMAL;
	
	public static final int UI_MODE_LIST = 0;
	public static final int UI_MODE_GRID = 1;
	public static final int UI_MODE_DATE = 2; // 照片时间轴
	
	/** 界面模式：ListView或者GridView */
	private int mCurUiMode = UI_MODE_GRID;
	
	List<DMFile> mDatas = new ArrayList<>();
	
	private Activity mContext;
	private LayoutInflater mInflater;
	private FileExplorerAdapter mAdapterList,mAdapterGrid;
	private PullToRefreshListView mList;
	private PullToRefreshListView mGrid;
	private TextView text_album;
	private TextView text_date;
	private View mEmptyLayout;
	private View mLoadingView;

	private DisplayImageOptions mLoaderOptions;

	public final static int UnitSize = 4;

	private EditChangedListener mEditChangedListener;
	private LoadItemCompleteListener mLoadItemCompleteListener;
	private OnFileLongClickListener mOnFileLongClickListener;
	private HandlerUtil.StaticHandler mHandler;
	private MyMessageListener mMessageListener;

	private PicTitleView tvPicTitle;
	private int picTitleHeight = 0;
	private int mListTop = -1;
	private int curFirstChildIndex = 0;
	private int oldFirstItemId = 0;
	private boolean imageloaderPaused = false;

	private DMFileCategoryType mType;

	private int mScreenWidth = 480;
	private int imageRLWIdth;
	private int gridItemWidth;
	

	public FilePictrueView(Activity context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		init();
	}

	public FilePictrueView(Activity context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		init();
	}

	public FilePictrueView(Activity context) {
		super(context);
		mContext = context;
		init();
	}

	private void initLoaderOptions() {
		
		mLoaderOptions = new DisplayImageOptions.Builder()
		  .cacheInMemory(true)
		  .showImageOnFail(R.drawable.filemanager_photo_fail)
		  .useThumb(true)
		  .cacheOnDisk(true)
		  .showImageOnLoading(R.drawable.ready_to_loading_image)
		  .showImageForEmptyUri(R.drawable.filemanager_photo_fail)
		  .considerExifParams(true)
		  .build();
		
	}

	public int getImageRLWidth(int screenWith) {
		return (screenWith - DipPixelUtil.dip2px(getContext(), 24)) / UnitSize;
	}

	public void initialize(DMFileCategoryType fileType, IItemLoader itemloader,int screenWidth) {

		mItemLoader = itemloader;
		mScreenWidth = screenWidth;
		imageRLWIdth = getImageRLWidth(mScreenWidth);
		gridItemWidth = (mScreenWidth - DipPixelUtil.dip2px(getContext(), 30)) / 2;
		mType = fileType;
		initLoaderOptions();

		mList.getRefreshableView().setDivider(null);
		mList.getRefreshableView().setDividerHeight(DipPixelUtil.dip2px(mContext, 2));

	}


	
	public void reloadItems(boolean showLoadingView) {
		
		mMulPictrueGroupList.clear();
		mPicsUnitList.clear();
		
		if(mCurUiMode == UI_MODE_DATE) {
			mItemLoader = new PictrueLoader();
		} else if(mCurUiMode == UI_MODE_GRID){
			mItemLoader = new PictrueGroupLoader();
		}
		
		mItemLoader.loadItems(mHandler, null);
		if (showLoadingView == true) {
			mLoadingView.setVisibility(View.VISIBLE);
		}
	}


	public void showAdapterView() {
		mEmptyLayout.setVisibility(View.GONE);
		setUIMode(mCurUiMode);
	}

	private void setUIMode(int uiMode) {
		mCurUiMode = uiMode;
		if (mCurUiMode == UI_MODE_LIST || mCurUiMode == UI_MODE_DATE) {
			mList.setVisibility(View.VISIBLE);
			mGrid.setVisibility(View.GONE);
		} else if (mCurUiMode == UI_MODE_GRID) {
			mList.setVisibility(View.GONE);
			mGrid.setVisibility(View.VISIBLE);
		}
	}

	private void init() {
		
		mInflater = LayoutInflater.from(mContext);
		
		View view = mInflater.inflate(R.layout.filemanager_typer_pictrue_explorer_view, null);
		
		mEmptyLayout = view.findViewById(R.id.emptyRl);
		mLoadingView = view.findViewById(R.id.loading);
		tvPicTitle = (PicTitleView) view.findViewById(R.id.tv_pic_title);

		/**
		 * 初始化第一个ListView
		 */
		mList = (PullToRefreshListView) view.findViewById(R.id.list);
		mAdapterList = new FileExplorerAdapter();
		mList.setMode(Mode.PULL_FROM_START);
		mList.setAdapter(mAdapterList);
		mList.setOnScrollListener(new PauseOnScrollListener(DMImageLoader.getInstance(), false, true,new PicScrollListener()));
		mList.setOnRefreshListener(new OnRefreshListener<ListView>() {

			@Override
			public void onRefresh(PullToRefreshBase<ListView> refreshView) {
				// TODO Auto-generated method stub
				reloadItems(false);
			}
		});
		
		/**
		 * 初始化第二个ListView
		 */
		mGrid = (PullToRefreshListView) view.findViewById(R.id.grid);
		mAdapterGrid = new FileExplorerAdapter();
		mGrid.setAdapter(mAdapterGrid);
		mGrid.setOnScrollListener(new PauseOnScrollListener(DMImageLoader.getInstance(), false, true));
		mGrid.setOnRefreshListener(new OnRefreshListener<ListView>() {

			@Override
			public void onRefresh(PullToRefreshBase<ListView> refreshView) {
				// TODO Auto-generated method stub
				reloadItems(false);
			}
		});
		
		text_album = (TextView) view.findViewById(R.id.text_album);
		text_date = (TextView) view.findViewById(R.id.text_date);
		text_album.setSelected(true);
		
		text_album.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (!text_album.isSelected()) {
					text_album.setSelected(true);
					text_date.setSelected(false);
					
					setUIMode(UI_MODE_GRID);
					
					notifyDataSetChanged();
					
					reloadItems(true);
				}
			}
		});
		
		text_date.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (!text_date.isSelected()) {
					text_date.setSelected(true);
					text_album.setSelected(false);
					
					setUIMode(UI_MODE_DATE);
					
					notifyDataSetChanged();
					
					reloadItems(true);
				}
			}
		});
		
		mMessageListener = new MyMessageListener();
		mHandler = new HandlerUtil.StaticHandler(mMessageListener);
		addView(view);

	}
	

	public void setEditChangedListener(EditChangedListener l) {
		this.mEditChangedListener = l;
	}

	public void setLoadItemCompleteListener(LoadItemCompleteListener l) {
		mLoadItemCompleteListener = l;
	}

	public void setOnFileLongClickListener(OnFileLongClickListener l) {
		mOnFileLongClickListener = l;
	}


	private class EmptyHolder {

	}

	private class ViewHolderBase extends EmptyHolder {
		public ImageView mFileIcon;
		public View mOperationView;
	}

	private class ViewHolderList extends ViewHolderBase {
		public TextView mFileName;
		public TextView mFileSize;
		public TextView mFileDate;
	}


	private final class ViewHolderGrid{
		ImageView grid_item_icon_0,grid_item_icon_1,grid_item_icon_2,grid_item_icon_3;
		ImageView grid_item_icon_4,grid_item_icon_5,grid_item_icon_6,grid_item_icon_7;
		ImageView grid_item_operatinobtn,grid_item_operatinobtn_1;
		PicFolderFrameLayout flyt_grid_item,flyt_grid_item_1;
		LinearLayout grid_item_llyt,grid_item_llyt_1,llyt_grid_sub_item,llyt_grid_sub_item_1;
		TextView grid_item_folder_name,grid_item_folder_name_1,tv_grid_item_folder_count,tv_grid_item_folder_count_1;

	}

	private class FileExplorerAdapter extends BaseAdapter {
		private LayoutInflater inflater = null;
		private DMImageLoader imageLoader;

		public FileExplorerAdapter() {
			super();
			inflater = LayoutInflater.from(mContext);
			imageLoader = DMImageLoader.getInstance();
		}

		@Override
		public int getCount() {
			if (mCurUiMode == UI_MODE_DATE) {
				return mPicsUnitList.size();
			} else {
				return mMulPictrueGroupList.size();
			}
		}

		@Override
		public Object getItem(int position) {
			if (mCurUiMode == UI_MODE_GRID) {
				return mMulPictrueGroupList.get(position);
			} else {
				return mPicsUnitList.get(position).picGroup;
			}
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			System.out.println("getView position :"+position + ",mCurUiMode:"+mCurUiMode);
			
			if (mCurUiMode == UI_MODE_GRID) {
				 
				MulPictrueGroup mMulPictrueGroup = mMulPictrueGroupList.get(position);
				// 图片 grid
				ViewHolderGrid viewHolder;
				if (convertView == null) {
					convertView = mInflater.inflate(R.layout.fileexplorer_type_grid_item, null);
					viewHolder = new ViewHolderGrid();
					initGridHoder(viewHolder,convertView);
					convertView.setTag(viewHolder);
				} else {
					viewHolder = (ViewHolderGrid) convertView.getTag();
				}
				//根据你的父类是什么布局，就需要什么布局的LayoutParams
		        LayoutParams lp =  new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
				lp.width=gridItemWidth;
				lp.height=gridItemWidth; 
				
				LinearLayout.LayoutParams lp1 =  new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT); 
				lp1.width=gridItemWidth;
				lp1.height=gridItemWidth; 
				
				
				viewHolder.llyt_grid_sub_item.setLayoutParams(lp);
				viewHolder.llyt_grid_sub_item_1.setLayoutParams(lp);
				
				viewHolder.flyt_grid_item.setLayoutParams(lp1);
				viewHolder.flyt_grid_item_1.setLayoutParams(lp1);
				
				
				updateView(viewHolder, mMulPictrueGroup,imageLoader, mLoaderOptions,gridItemWidth,position);
				
			} else if (mCurUiMode == UI_MODE_DATE) {
				
				// 这个是目前使用的图片分类
				PicHolder holder = null;

				if (convertView == null) {
					holder = new PicHolder();
					convertView = inflater.inflate(R.layout.pictures_line, null);
					initHolder(holder, convertView);
					convertView.setTag(holder);
				} else {
					holder = (PicHolder) convertView.getTag();
				}
				loadData2Holder(mPicsUnitList.get(position), imageLoader, mLoaderOptions, holder, imageRLWIdth);
			}

			return convertView;
		}

		private class PicHolder {
			public TextView tvDate;
			public RelativeLayout rlPicItem0;
			public PicImageView ivIcon0;
			public ImageView ivOperation0;
			public RelativeLayout rlPicItem1;
			public PicImageView ivIcon1;
			public ImageView ivOperation1;
			public RelativeLayout rlPicItem2;
			public PicImageView ivIcon2;
			public ImageView ivOperation2;
			public RelativeLayout rlPicItem3;
			public PicImageView ivIcon3;
			public ImageView ivOperation3;
			
//			public ImageView ivDivide;
			public PicLineLayout llLine;
		}
		
		private class PicItemClickListener implements OnClickListener {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub

				if (mState == EditState.STATE_NORMAL) {
					int positionInGroup = ((PicImageView) arg0).getUnitId() * FolderImageActivity.UnitSize + ((PicImageView) arg0).getIdInLine();
                    int countInPreGroup = getPositionByGroupId(((PicImageView) arg0).getUnitGroupId());
                    int positionInAll = positionInGroup + countInPreGroup;
					FileOperationHelper.getInstance().openPicture(mContext,groupDatas, positionInAll,ImagePagerActivity.IS_FROM_FileExplorerView);
				} else {
					FileManager.changeSelectState(((PicImageView) arg0).getDMFile()); // 反选
					//被选中的保存起来
					updateSelectedList(((PicImageView) arg0).getDMFile());
					ImageView icon = ((PicImageView) arg0).getIcon();
					if(icon != null) {
						icon.setVisibility(View.VISIBLE);
						icon.setSelected(((PicImageView) arg0).getDMFile().selected);
					}
					
					
					EventBus.getDefault().post(new DirViewStateChangeEvent(mState, "", getSelectedFiles()));

				}
			}

		}
		
		private void initHolder(PicHolder holder, View convertView) {
//			holder.ivDivide = (ImageView) convertView.findViewById(R.id.iv_line_divide);
			holder.ivIcon0 = (PicImageView) convertView.findViewById(R.id.piv_line_icon0);
			holder.ivIcon1 = (PicImageView) convertView.findViewById(R.id.piv_line_icon1);
			holder.ivIcon2 = (PicImageView) convertView.findViewById(R.id.piv_line_icon2);
			holder.ivIcon3 = (PicImageView) convertView.findViewById(R.id.piv_line_icon3);
			holder.ivOperation0 = (ImageView) convertView.findViewById(R.id.iv_line_operatinobtn0);
			holder.ivOperation1 = (ImageView) convertView.findViewById(R.id.iv_line_operatinobtn1);
			holder.ivOperation2 = (ImageView) convertView.findViewById(R.id.iv_line_operatinobtn2);
			holder.ivOperation3 = (ImageView) convertView.findViewById(R.id.iv_line_operatinobtn3);
			holder.rlPicItem0 = (RelativeLayout) convertView.findViewById(R.id.rl_line_picitem0);
			holder.rlPicItem1 = (RelativeLayout) convertView.findViewById(R.id.rl_line_picitem1);
			holder.rlPicItem2 = (RelativeLayout) convertView.findViewById(R.id.rl_line_picitem2);
			holder.rlPicItem3 = (RelativeLayout) convertView.findViewById(R.id.rl_line_picitem3);
			holder.tvDate = (TextView) convertView.findViewById(R.id.tv_line_date);
			holder.llLine = (PicLineLayout) convertView.findViewById(R.id.ll_line);
			
			//这一步就是为了把引用给保存进去，在onclick可以拿出来使用
			holder.ivIcon0.setIcon(holder.ivOperation0);
			holder.ivIcon1.setIcon(holder.ivOperation1);
			holder.ivIcon2.setIcon(holder.ivOperation2);
			holder.ivIcon3.setIcon(holder.ivOperation3);
			

			holder.ivIcon0.setOnClickListener(new PicItemClickListener());
			holder.ivIcon1.setOnClickListener(new PicItemClickListener());
			holder.ivIcon2.setOnClickListener(new PicItemClickListener());
			holder.ivIcon3.setOnClickListener(new PicItemClickListener());
			
			/*holder.ivIcon0.setOnLongClickListener(new PicItemLongClickListener());
			holder.ivIcon1.setOnLongClickListener(new PicItemLongClickListener());
			holder.ivIcon2.setOnLongClickListener(new PicItemLongClickListener());
			holder.ivIcon3.setOnLongClickListener(new PicItemLongClickListener());*/
		}

		private void loadData2Holder(PicsUnit unit, DMImageLoader imageLoader, DisplayImageOptions options, PicHolder holder, int width) {

			int unitGroupId = unit.unitGroupId;
			int unitId = unit.unitId;
			ArrayList<DMFile> fileList = unit.picGroup;

			updateDateDivideView(holder, unit);

			resetImageRLSize(holder.rlPicItem0, width);
			resetImageRLSize(holder.rlPicItem1, width);
			resetImageRLSize(holder.rlPicItem2, width);
			resetImageRLSize(holder.rlPicItem3, width);

			if (fileList.size() == 1) {
				holder.ivIcon0.setVisibility(View.VISIBLE);
				holder.ivIcon1.setVisibility(View.INVISIBLE);
				holder.ivIcon2.setVisibility(View.INVISIBLE);
				holder.ivIcon3.setVisibility(View.INVISIBLE);
				holder.ivOperation0.setVisibility(View.VISIBLE);
				holder.ivOperation1.setVisibility(View.INVISIBLE);
				holder.ivOperation2.setVisibility(View.INVISIBLE);
				holder.ivOperation3.setVisibility(View.INVISIBLE);

				loadImageDate(holder.ivIcon0, unitId, unitGroupId, fileList.get(0), 0);
				loadOperatorDate(holder.ivOperation0, fileList.get(0).selected);
				String uri0 = FileOperationHelper.getInstance().getFullPath(fileList.get(0));
				loadImage(imageLoader, options, holder.ivIcon0, uri0);
				if (fileList.get(0).mHidden) {
					holder.ivIcon0.setAlpha(50);
				}else {
					holder.ivIcon0.setAlpha(255);
				}

			} else if (fileList.size() == 2) {
				holder.ivIcon0.setVisibility(View.VISIBLE);
				holder.ivIcon1.setVisibility(View.VISIBLE);
				holder.ivIcon2.setVisibility(View.INVISIBLE);
				holder.ivIcon3.setVisibility(View.INVISIBLE);
				holder.ivOperation0.setVisibility(View.VISIBLE);
				holder.ivOperation1.setVisibility(View.VISIBLE);
				holder.ivOperation2.setVisibility(View.INVISIBLE);
				holder.ivOperation3.setVisibility(View.INVISIBLE);

				loadImageDate(holder.ivIcon0, unitId, unitGroupId, fileList.get(0), 0);
				loadOperatorDate(holder.ivOperation0, fileList.get(0).selected);
				String uri0 = FileOperationHelper.getInstance().getFullPath(fileList.get(0));
				loadImage(imageLoader, options, holder.ivIcon0, uri0);
				if (fileList.get(0).mHidden) {
					holder.ivIcon0.setAlpha(50);
				}else {
					holder.ivIcon0.setAlpha(255);
				}
				
				
				loadImageDate(holder.ivIcon1, unitId, unitGroupId, fileList.get(1), 1);
				loadOperatorDate(holder.ivOperation1, fileList.get(1).selected);
				String uri1 = FileOperationHelper.getInstance().getFullPath(fileList.get(1));
				loadImage(imageLoader, options, holder.ivIcon1, uri1);
				if (fileList.get(1).mHidden) {
					holder.ivIcon0.setAlpha(50);
				}else {
					holder.ivIcon0.setAlpha(255);
				}

			} else if (fileList.size() == 3) {
				holder.ivIcon0.setVisibility(View.VISIBLE);
				holder.ivIcon1.setVisibility(View.VISIBLE);
				holder.ivIcon2.setVisibility(View.VISIBLE);
				holder.ivIcon3.setVisibility(View.GONE);
				holder.ivOperation0.setVisibility(View.VISIBLE);
				holder.ivOperation1.setVisibility(View.VISIBLE);
				holder.ivOperation2.setVisibility(View.VISIBLE);
				holder.ivOperation3.setVisibility(View.GONE);

				loadImageDate(holder.ivIcon0, unitId, unitGroupId, fileList.get(0), 0);
				loadOperatorDate(holder.ivOperation0, fileList.get(0).selected);
				String uri0 = FileOperationHelper.getInstance().getFullPath(fileList.get(0));
				loadImage(imageLoader, options, holder.ivIcon0, uri0);
				if (fileList.get(0).mHidden) {
					holder.ivIcon0.setAlpha(50);
				}else {
					holder.ivIcon0.setAlpha(255);
				}

				loadImageDate(holder.ivIcon1, unitId, unitGroupId, fileList.get(1), 1);
				loadOperatorDate(holder.ivOperation1, fileList.get(1).selected);
				String uri1 = FileOperationHelper.getInstance().getFullPath(fileList.get(1));
				loadImage(imageLoader, options, holder.ivIcon1, uri1);
				if (fileList.get(1).mHidden) {
					holder.ivIcon1.setAlpha(50);
				}else {
					holder.ivIcon1.setAlpha(255);
				}

				loadImageDate(holder.ivIcon2, unitId, unitGroupId, fileList.get(2), 2);
				loadOperatorDate(holder.ivOperation2, fileList.get(2).selected);
				String uri2 = FileOperationHelper.getInstance().getFullPath(fileList.get(2));
				loadImage(imageLoader, options, holder.ivIcon2, uri2);
				if (fileList.get(2).mHidden) {
					holder.ivIcon2.setAlpha(50);
				}else {
					holder.ivIcon2.setAlpha(255);
				}
			} else if(fileList.size() == 4) {
				holder.ivIcon0.setVisibility(View.VISIBLE);
				holder.ivIcon1.setVisibility(View.VISIBLE);
				holder.ivIcon2.setVisibility(View.VISIBLE);
				holder.ivIcon3.setVisibility(View.VISIBLE);
				holder.ivOperation0.setVisibility(View.VISIBLE);
				holder.ivOperation1.setVisibility(View.VISIBLE);
				holder.ivOperation2.setVisibility(View.VISIBLE);
				holder.ivOperation3.setVisibility(View.VISIBLE);

				loadImageDate(holder.ivIcon0, unitId, unitGroupId, fileList.get(0), 0);
				loadOperatorDate(holder.ivOperation0, fileList.get(0).selected);
				String uri0 = FileOperationHelper.getInstance().getFullPath(fileList.get(0));
				loadImage(imageLoader, options, holder.ivIcon0, uri0);
				if (fileList.get(0).mHidden) {
					holder.ivIcon0.setAlpha(50);
				}else {
					holder.ivIcon0.setAlpha(255);
				}

				loadImageDate(holder.ivIcon1, unitId, unitGroupId, fileList.get(1), 1);
				loadOperatorDate(holder.ivOperation1, fileList.get(1).selected);
				String uri1 = FileOperationHelper.getInstance().getFullPath(fileList.get(1));
				loadImage(imageLoader, options, holder.ivIcon1, uri1);
				if (fileList.get(1).mHidden) {
					holder.ivIcon1.setAlpha(50);
				}else {
					holder.ivIcon1.setAlpha(255);
				}

				loadImageDate(holder.ivIcon2, unitId, unitGroupId, fileList.get(2), 2);
				loadOperatorDate(holder.ivOperation2, fileList.get(2).selected);
				String uri2 = FileOperationHelper.getInstance().getFullPath(fileList.get(2));
				loadImage(imageLoader, options, holder.ivIcon2, uri2);
				if (fileList.get(2).mHidden) {
					holder.ivIcon2.setAlpha(50);
				}else {
					holder.ivIcon2.setAlpha(255);
				}
				
				loadImageDate(holder.ivIcon3, unitId, unitGroupId, fileList.get(3), 3);
				loadOperatorDate(holder.ivOperation3, fileList.get(3).selected);
				String uri3 = FileOperationHelper.getInstance().getFullPath(fileList.get(3));
				loadImage(imageLoader, options, holder.ivIcon3, uri3);
				if (fileList.get(3).mHidden) {
					holder.ivIcon3.setAlpha(50);
				}else {
					holder.ivIcon3.setAlpha(255);
				}
			}
		}
		
		private void updateDateDivideView(PicHolder holder, PicsUnit picUnit) {

			if (picUnit.type == PicsUnit.Head) {
				holder.llLine.setTail(false);
				holder.tvDate.setVisibility(View.VISIBLE);
//				holder.ivDivide.setVisibility(View.VISIBLE);
				holder.llLine.setHead(true);
			} else if (picUnit.type == PicsUnit.Mid) {
				holder.llLine.setTail(false);
				holder.tvDate.setVisibility(View.GONE);
//				holder.ivDivide.setVisibility(View.VISIBLE);
				holder.llLine.setHead(false);
			} else if (picUnit.type == PicsUnit.Tail) {
				holder.llLine.setTail(true);
				holder.tvDate.setVisibility(View.GONE);
//				holder.ivDivide.setVisibility(View.VISIBLE);
				holder.llLine.setHead(false);
			}
			if (picUnit.isAlsoTail) {
				holder.llLine.setTail(true);
//				holder.ivDivide.setVisibility(View.GONE);
			}

			if (holder.llLine.isHead()) {
				holder.tvDate.setText(picUnit.date);
			}

			holder.llLine.setDate(formatPicDate(picUnit.picGroup.get(0).mLastModify));

			if (tvPicTitle.getText() == null || tvPicTitle.getText().equals("")) {
				tvPicTitle.setText(formatPicDate(picUnit.picGroup.get(0).mLastModify));
				tvPicTitle.setVisibility(View.VISIBLE);
			}
		}
		
		private void loadImageDate(PicImageView imageView, int unitId, int unitGroupId, DMFile file, int idInLine) {
			imageView.setUnitId(unitId);
			imageView.setUnitGroupId(unitGroupId);
			imageView.setDMFile(file);
			imageView.setIdInLine(idInLine);
		}
		
		/*
		 * 获取指定的groupId之前有多少个图片
		 */
		private int getPositionByGroupId(int groupID) {
			int lineCount = 0;
			for (int i = 0; i < mPicsUnitList.size(); i++) {
				if (mPicsUnitList.get(i).unitGroupId == groupID) {
					break;
				} else {
					lineCount = lineCount + mPicsUnitList.get(i).picGroup.size();
				}
			}
			return lineCount;
		}
		
		private void updateSelectedList(DMFile fileItem) {
			if (fileItem.selected == true)
				mSelectedIDList.add(fileItem.getPath());
			else
				mSelectedIDList.remove(fileItem.getPath());
		}

		private class PicFolderClickListener implements OnClickListener {

			@Override
			public void onClick(View view) {
				// TODO Auto-generated method stub
				//行
				int row = ((PicFolderFrameLayout) view).getRow();
				//列
				int columns = ((PicFolderFrameLayout) view).getColumns();
				if(mMulPictrueGroupList.size() <= row || mMulPictrueGroupList.get(row).PictrueGroupList.size() <= columns) return;
				//得到选中的文件夹
				PictrueGroup p = mMulPictrueGroupList.get(row).PictrueGroupList.get(columns);
				
				if (mState == EditState.STATE_NORMAL) {
					Intent mIntent = new Intent(mContext,FolderImageActivity.class);
					mIntent.putExtra("PATH", p.folderPath);
					mContext.startActivity(mIntent);
				} else {
					ImageView iv = null;
					if(columns == 0) {
						 iv = (ImageView) view.findViewById(R.id.fileexplorer_grid_item_operatinobtn);
					} else {
						 iv = (ImageView) view.findViewById(R.id.fileexplorer_grid_item_operatinobtn_1);
					}
					boolean isSelected = !p.isSelected();
					//反选
					p.setSelected(isSelected);
					if(isSelected) {
						mSelectedFolderPathList.add(p.getFolderPath());
					} else {
						mSelectedFolderPathList.remove(p.getFolderPath());
					}
					if(iv != null) {
						iv.setVisibility(View.VISIBLE);
						iv.setSelected(isSelected);
					}
					
					List<DMFile> dirs = new ArrayList<>();
					for(String folder:mSelectedFolderPathList){
						DMFile dir = new DMFile();
						dir.isDir = true;
						dir.mPath = folder;
						dir.selected = true;
						dirs.add(dir);
					}
					
					EventBus.getDefault().post(new DirViewStateChangeEvent(mState, "", dirs));
				}
			}

		}
		
		private void initGridHoder(ViewHolderGrid holder, View convertView) { 
			holder.grid_item_icon_0 = (ImageView) convertView.findViewById(R.id.fileexplorer_grid_item_icon_0);
			holder.grid_item_icon_1 = (ImageView) convertView.findViewById(R.id.fileexplorer_grid_item_icon_1);
			holder.grid_item_icon_2 = (ImageView) convertView.findViewById(R.id.fileexplorer_grid_item_icon_2);
			holder.grid_item_icon_3 = (ImageView) convertView.findViewById(R.id.fileexplorer_grid_item_icon_3);
			holder.grid_item_operatinobtn = (ImageView) convertView.findViewById(R.id.fileexplorer_grid_item_operatinobtn);
			holder.grid_item_llyt = (LinearLayout) convertView.findViewById(R.id.llyt_grid_item);
			holder.llyt_grid_sub_item = (LinearLayout) convertView.findViewById(R.id.llyt_grid_sub_item);
			holder.grid_item_folder_name = (TextView) convertView.findViewById(R.id.tv_grid_item_folder_name);
			holder.tv_grid_item_folder_count = (TextView) convertView.findViewById(R.id.tv_grid_item_folder_count);
			holder.flyt_grid_item = (PicFolderFrameLayout) convertView.findViewById(R.id.flyt_grid_item);
			
			
			
			holder.grid_item_icon_4 = (ImageView) convertView.findViewById(R.id.fileexplorer_grid_item_icon_4);
			holder.grid_item_icon_5 = (ImageView) convertView.findViewById(R.id.fileexplorer_grid_item_icon_5);
			holder.grid_item_icon_6 = (ImageView) convertView.findViewById(R.id.fileexplorer_grid_item_icon_6);
			holder.grid_item_icon_7 = (ImageView) convertView.findViewById(R.id.fileexplorer_grid_item_icon_7);
			holder.grid_item_operatinobtn_1 = (ImageView) convertView.findViewById(R.id.fileexplorer_grid_item_operatinobtn_1);
			holder.grid_item_llyt_1 = (LinearLayout) convertView.findViewById(R.id.llyt_grid_item_1);
			holder.llyt_grid_sub_item_1 = (LinearLayout) convertView.findViewById(R.id.llyt_grid_sub_item_1);

			holder.grid_item_folder_name_1 = (TextView) convertView.findViewById(R.id.tv_grid_item_folder_name_1);
			holder.tv_grid_item_folder_count_1 = (TextView) convertView.findViewById(R.id.tv_grid_item_folder_count_1);
			holder.flyt_grid_item_1 = (PicFolderFrameLayout) convertView.findViewById(R.id.flyt_grid_item_1);
			
			holder.flyt_grid_item.setOnClickListener(new PicFolderClickListener());
			holder.flyt_grid_item_1.setOnClickListener(new PicFolderClickListener());
			
		}
		
		/**
		 * 更新gridView 的内容

		 */
		private void updateView(ViewHolderGrid holder, MulPictrueGroup mMulPictrueGroup,
				DMImageLoader imageLoader, DisplayImageOptions mLoaderOptions,int width,int position) {
			
			//第一个相册必须有
			 ImageView[] iv = new ImageView[4];
			 iv[0] = holder.grid_item_icon_0;
			 iv[1] = holder.grid_item_icon_1;
			 iv[2] = holder.grid_item_icon_2;
			 iv[3] = holder.grid_item_icon_3;
			updateSeqView(iv,holder.grid_item_operatinobtn, 
					holder.grid_item_folder_name,holder.tv_grid_item_folder_count,mMulPictrueGroup.PictrueGroupList.get(0), imageLoader, mLoaderOptions, width);
			holder.grid_item_llyt.setVisibility(View.VISIBLE);

			//
			if(mMulPictrueGroup.PictrueGroupList.size() == 1) {
				holder.grid_item_llyt_1.setVisibility(View.INVISIBLE);
				holder.flyt_grid_item.setRow(position);
				holder.flyt_grid_item.setColumns(0);
				
				if (mMulPictrueGroup.PictrueGroupList.get(0).mHidden) {
					holder.grid_item_llyt.setAlpha(0.19f);
				}else {
					holder.grid_item_llyt.setAlpha(1);
				}
				
			} else if(mMulPictrueGroup.PictrueGroupList.size() == 2) {
				//第二个相册
				ImageView[] iv1 = new ImageView[4];
				 iv1[0] = holder.grid_item_icon_4;
				 iv1[1] = holder.grid_item_icon_5;
				 iv1[2] = holder.grid_item_icon_6;
				 iv1[3] = holder.grid_item_icon_7;
				
				updateSeqView(iv1,holder.grid_item_operatinobtn_1, 
						holder.grid_item_folder_name_1, holder.tv_grid_item_folder_count_1,mMulPictrueGroup.PictrueGroupList.get(1), imageLoader, mLoaderOptions, width);
				holder.grid_item_llyt_1.setVisibility(View.VISIBLE);
				holder.flyt_grid_item.setRow(position);
				holder.flyt_grid_item.setColumns(0);
				
				holder.flyt_grid_item_1.setRow(position);
				holder.flyt_grid_item_1.setColumns(1);
				
				if (mMulPictrueGroup.PictrueGroupList.get(0).mHidden) {
					holder.grid_item_llyt.getBackground().setAlpha(50);
				}else {
					holder.grid_item_llyt.getBackground().setAlpha(255);
				}
				
				if (mMulPictrueGroup.PictrueGroupList.get(1).mHidden) {
					holder.grid_item_llyt_1.getBackground().setAlpha(50);
				}else {
					holder.grid_item_llyt_1.getBackground().setAlpha(255);
				}
			}
		}
		
		private void updateSeqView(ImageView [] iv, ImageView operatinoBtn,TextView folderName,TextView folderCount,PictrueGroup mPictrueGroup,
				DMImageLoader imageLoader, DisplayImageOptions mLoaderOptions,int width) {
			int size = mPictrueGroup.getCount();
			if(mPictrueGroup != null) {
				folderName.setText(mPictrueGroup.getFolderName());
				folderCount.setText(String.format(mContext.getString(R.string.DM_Disk_Backup_Media_Folder_Num), String.valueOf(size)));
			}
			//更新编辑选中的图标
			loadOperatorDate(operatinoBtn,mPictrueGroup.isSelected());
			if(size == 1) {
				iv[0].setVisibility(View.VISIBLE);
				iv[1].setVisibility(View.INVISIBLE);
				iv[2].setVisibility(View.INVISIBLE);
				iv[3].setVisibility(View.INVISIBLE);
				
				String uri0 = FileOperationHelper.getInstance().getFullPath(mPictrueGroup.picGroup.get(0));
				loadImage(imageLoader, mLoaderOptions, iv[0], uri0);
				
			} else if (size == 2) {
				iv[0].setVisibility(View.VISIBLE);
				iv[1].setVisibility(View.VISIBLE);
				iv[2].setVisibility(View.INVISIBLE);
				iv[3].setVisibility(View.INVISIBLE);
				
				
				String uri0 = FileOperationHelper.getInstance().getFullPath(mPictrueGroup.picGroup.get(0));
				loadImage(imageLoader, mLoaderOptions, iv[0], uri0);
				
				String uri1 = FileOperationHelper.getInstance().getFullPath(mPictrueGroup.picGroup.get(1));
				loadImage(imageLoader, mLoaderOptions, iv[1], uri1);
				
			} else if(size ==3) {
				iv[0].setVisibility(View.VISIBLE);
				iv[1].setVisibility(View.VISIBLE);
				iv[2].setVisibility(View.VISIBLE);
				iv[3].setVisibility(View.INVISIBLE);
				
				String uri0 = FileOperationHelper.getInstance().getFullPath(mPictrueGroup.picGroup.get(0));
				loadImage(imageLoader, mLoaderOptions, iv[0], uri0);
				
				String uri1 = FileOperationHelper.getInstance().getFullPath(mPictrueGroup.picGroup.get(1));
				loadImage(imageLoader, mLoaderOptions, iv[1], uri1);
				
				String uri2 = FileOperationHelper.getInstance().getFullPath(mPictrueGroup.picGroup.get(2));
				loadImage(imageLoader, mLoaderOptions, iv[2], uri2);
				
			} else if(size > 3)  {
				iv[0].setVisibility(View.VISIBLE);
				iv[1].setVisibility(View.VISIBLE);
				iv[2].setVisibility(View.VISIBLE);
				iv[3].setVisibility(View.VISIBLE);
				
				String uri0 = FileOperationHelper.getInstance().getFullPath(mPictrueGroup.picGroup.get(0));
				loadImage(imageLoader, mLoaderOptions, iv[0], uri0);
				
				String uri1 = FileOperationHelper.getInstance().getFullPath(mPictrueGroup.picGroup.get(1));
				loadImage(imageLoader, mLoaderOptions, iv[1], uri1);
				
				String uri2 = FileOperationHelper.getInstance().getFullPath(mPictrueGroup.picGroup.get(2));
				loadImage(imageLoader, mLoaderOptions, iv[2], uri2);
				
				String uri3 = FileOperationHelper.getInstance().getFullPath(mPictrueGroup.picGroup.get(3));
				loadImage(imageLoader, mLoaderOptions, iv[3], uri3);
			}
			
		}
		

		private void loadOperatorDate(ImageView operView, boolean isSelected) {
			if (mState == EditState.STATE_NORMAL) {
				operView.setVisibility(View.GONE);
				operView.setSelected(false);
			} else {
				//这里进行了一个显示。但是要根据operView 的一个选中状态进行显示图片，如果没有被选中的话是显示一张透明的图片
				operView.setVisibility(View.VISIBLE);
				operView.setSelected(isSelected);
			}
		}
		
		private void resetImageRLSize(RelativeLayout rl, int width) {
			LinearLayout.LayoutParams param = (LinearLayout.LayoutParams) rl.getLayoutParams();
			param.height = width;
			param.width = width;
			rl.setLayoutParams(param);
		}
		private void resetImageRLSize(ImageView rl, int width) {
			LinearLayout.LayoutParams param = (LinearLayout.LayoutParams) rl.getLayoutParams();
			param.height = width;
			param.width = width;
			rl.setLayoutParams(param);
		}


		private void loadImage(DMImageLoader imageLoader, DisplayImageOptions options, final ImageView iconview, String uri) {
			
			uri = FileInfoUtils.encodeUri(uri);
			
			imageLoader.displayImage(uri, iconview,options,new ImageLoadingListener() {
				
				@Override
				public void onLoadingStarted(String imageUri, View view) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void onLoadingFailed(String imageUri, View view,FailReason failReason) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
					// TODO Auto-generated method stub
				}
				
				@Override
				public void onLoadingCancelled(String imageUri, View view) {
					// TODO Auto-generated method stub
					
				}
			});
		}
	}


	private class MyMessageListener implements HandlerUtil.MessageListener {
		@Override
		public void handleMessage(Message msg) {

			if (msg.what == IItemLoader.MSG_LOAD_ITEM_COMPLETE) {
				boolean bEmpty = false;
				tvPicTitle.setText("");
				tvPicTitle.setVisibility(View.INVISIBLE);
				//时间list图片列表
				if (mCurUiMode == UI_MODE_DATE) {
					mList.onRefreshComplete();
					try{
						groupDatas = (List<ArrayList<DMFile>>) msg.obj;
					if (groupDatas != null && groupDatas.size() > 0) {
						mPicsUnitList.clear();
						mPicsUnitList = getPicsUnitList(groupDatas);
						addSelectedState(mPicsUnitList);
					} else {
						bEmpty = true;
					}
					}catch(Exception e) {
						groupDatas = null;
						bEmpty = true;
					}
					
					
					//文件夹grid图片的处理
				} else if(mCurUiMode == UI_MODE_GRID) {
					List<MulPictrueGroup> list;
					mGrid.onRefreshComplete();
					try{
						 list = (List<MulPictrueGroup>) msg.obj;
						 System.out.println("ggrid kkk:"+list.size());
						if (null != list && list.size() > 0) {
							if (MulPictrueGroup.class.isInstance(list.get(0))) {
								 System.out.println("ggrid data right");
								mMulPictrueGroupList.clear();
								mMulPictrueGroupList.addAll(list);
								addSelectedState0(mMulPictrueGroupList);
							} else {
								return;
							}
							bEmpty = true;
						}
					}catch(Exception e) {
						list = null;
						bEmpty = true;
					}
				}
				if (bEmpty) {
					setUIMode(mCurUiMode);
					mLoadingView.setVisibility(View.GONE);
					notifyDataSetChanged();
				} else {
					setUIMode(mCurUiMode);
					notifyDataSetChanged();
					mLoadingView.setVisibility(View.GONE);
				}

			} else if (msg.what == MSG_NOTIFY_DATA_SET_CHANGED) {
				if (mType == DMFileCategoryType.E_PICTURE_CATEGORY)
					notifyDataSetChanged();
			}
		}
	}

	private void addSelectedState(List<PicsUnit> list) {
		for (int i = 0; i < list.size(); i++) {
			for (int k = 0; k < list.get(i).picGroup.size(); k++) {
				for (int j = 0; j < mSelectedIDList.size(); j++) {
					if (list.get(i).picGroup.get(k).getPath().equals(mSelectedIDList.get(j))) {
						list.get(i).picGroup.get(k).selected = true;
						break;
					}
				}

			}
		}
	}

	private void addSelectedState0(List<MulPictrueGroup> list) {
		
		for (int i = 0; i < list.size(); i++) {
			
			for(PictrueGroup m:list.get(i).PictrueGroupList) {
				for (int j = 0; j < mSelectedFolderPathList.size(); j++) {
					if (m.getFolderPath().equals(mSelectedFolderPathList.get(j))) {
						m.selected = true;
						break;
					}
				}
			}
		}
	}
	
	private List<PicsUnit> getPicsUnitList(List<ArrayList<DMFile>> groupList) {
		ArrayList<PicsUnit> unitList = new ArrayList<PicsUnit>();
		for (int i = 0; i < groupList.size(); i++) {
			addSameUnit(i, unitList, groupList.get(i));
		}
		return unitList;
	}
	
	private void addSameUnit(int groupid, ArrayList<PicsUnit> units, ArrayList<DMFile> group) {
		ArrayList<ArrayList<DMFile>> pieces = new ArrayList<ArrayList<DMFile>>();
		pieces = separateGroup(group);
		ArrayList<PicsUnit> unitsTemp = buildUnits(groupid, pieces);
		units.addAll(unitsTemp);
	}
	
	private static ArrayList<ArrayList<DMFile>> separateGroup(ArrayList<DMFile> group) {

		ArrayList<ArrayList<DMFile>> pieceList = new ArrayList<ArrayList<DMFile>>();
		ArrayList<DMFile> piece = new ArrayList<DMFile>();
		for (int i = 0; i < group.size(); i++) {
			int temp = i + 1;
			if ((temp) % UnitSize != 0) {
				piece.add(group.get(i));
			} else if ((temp) % UnitSize == 0) {
				piece.add(group.get(i));
				pieceList.add(piece);
				piece = new ArrayList<DMFile>();
			} else {
			}
		}
		if (piece.size() != 0)
			pieceList.add(piece);
		return pieceList;
	}

	private ArrayList<PicsUnit> buildUnits(int groupid, ArrayList<ArrayList<DMFile>> pieces) {
		ArrayList<PicsUnit> res = new ArrayList<PicsUnit>();
		for (int i = 0; i < pieces.size(); i++) {
			if (i == 0) {
				PicsUnit unit = new PicsUnit();
				unit.unitGroupId = groupid;
				unit.unitId = i;
				unit.picGroup = pieces.get(i);
				unit.type = PicsUnit.Head;
//				formatPicDate(picUnit.picGroup.get(0).mLastModify)
				unit.date = formatPicDate(pieces.get(i).get(0).mLastModify);
				res.add(unit);
			} else {
				PicsUnit unit = new PicsUnit();
				unit.unitGroupId = groupid;
				unit.unitId = i;
				unit.picGroup = pieces.get(i);
				unit.type = PicsUnit.Mid;
				unit.date = formatPicDate(pieces.get(i).get(0).mLastModify);
				res.add(unit);
			}
		}
		if (res.get(res.size() - 1).type != PicsUnit.Head)
			res.get(res.size() - 1).type = PicsUnit.Tail;

		res.get(res.size() - 1).isAlsoTail = true;
		return res;
	}
	
	
	
	public void notifyDataSetChanged() {
		mAdapterList.notifyDataSetChanged();
		mAdapterGrid.notifyDataSetChanged();
		if ((mMulPictrueGroupList.size() == 0 && mCurUiMode == UI_MODE_GRID) || (mPicsUnitList.size() == 0 && mCurUiMode == UI_MODE_DATE)) { // 如果无数据
			mEmptyLayout.setVisibility(View.VISIBLE);
		} else {
			mEmptyLayout.setVisibility(View.GONE);
			setUIMode(mCurUiMode);
		}
	}
	
	private String formatPicDate(long mLastModify) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(mLastModify);
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		int month = calendar.get(Calendar.MONTH);
		int year = calendar.get(Calendar.YEAR);
		int mWay = calendar.get(Calendar.DAY_OF_WEEK);  
		String week = "";
		switch(mWay) {
		case 1:week = mContext.getString(R.string.DM_Sunday);break;
		case 2:week = mContext.getString(R.string.DM_Monday);break;
		case 3:week = mContext.getString(R.string.DM_Tuesday);break;
		case 4:week = mContext.getString(R.string.DM_Wednesday);break;
		case 5:week = mContext.getString(R.string.DM_Thursday);break;
		case 6:week = mContext.getString(R.string.DM_Friday);break;
		case 7:week = mContext.getString(R.string.DM_Saturday);break;
		}
		String date = year + "-" + (month + 1) + "-" + day + " "+week;
		return date;
	}

	public void resetUiMode() {
		// TODO Auto-generated method stub
		mCurUiMode = UI_MODE_GRID;
		setUIMode(mCurUiMode);
		reloadItems(false);
				
	}

	@Override
	public void switchMode(int mode) {
		// TODO Auto-generated method stub

		if(mState == mode) return;
		mState =mode;
		notifyDataSetChanged();
	}

	@Override
	public void selectAll() {
		// TODO Auto-generated method stub
		if (mCurUiMode != UI_MODE_DATE) {
			mSelectedFolderPathList.clear();
			for(MulPictrueGroup m :mMulPictrueGroupList) {
				Iterator<PictrueGroup> iter = m.PictrueGroupList.iterator();
				while (iter.hasNext()) {
					PictrueGroup p = iter.next();
					p.setSelected(true);
					mSelectedFolderPathList.add(p.getFolderPath());
				}
			}
			
			List<DMFile> dirs = new ArrayList<>();
			for(String folder:mSelectedFolderPathList){
				DMFile dir = new DMFile();
				dir.isDir = true;
				dir.mPath = folder;
				dir.selected = true;
				dirs.add(dir);
			}
			
			EventBus.getDefault().post(new DirViewStateChangeEvent(mState, "", dirs));
			
		}else {
			Iterator<PicsUnit> iter = mPicsUnitList.iterator();
			mSelectedIDList.clear();
			while (iter.hasNext()) {
				FileManager.selectAll(iter.next().picGroup,mSelectedIDList);
			}
			EventBus.getDefault().post(new DirViewStateChangeEvent(mState, "", getSelectedFiles()));
		}
		//System.out.println("test:noti3");
		notifyDataSetChanged();
	}

	@Override
	public void unselectAll() {
		// TODO Auto-generated method stub
		if (mCurUiMode != UI_MODE_DATE) {
			
			mSelectedFolderPathList.clear();
			for(MulPictrueGroup m :mMulPictrueGroupList) {
				Iterator<PictrueGroup> iter = m.PictrueGroupList.iterator();
				while (iter.hasNext()) {
					iter.next().setSelected(false);
				}
			}
			
		} else {
			Iterator<PicsUnit> iter = mPicsUnitList.iterator();
			while (iter.hasNext()) {
				FileManager.unselectAll(iter.next().picGroup);
			}
			mSelectedIDList.clear();
		}
		//System.out.println("test:noti4");
		notifyDataSetChanged();
		EventBus.getDefault().post(new DirViewStateChangeEvent(mState, "", getSelectedFiles()));
	}

	@Override
	public List<DMFile> getSelectedFiles() {
		// TODO Auto-generated method stub
		List<DMFile> list = new ArrayList<>();
		if (mCurUiMode != UI_MODE_DATE) {
			for(MulPictrueGroup m :mMulPictrueGroupList) {
				for (int i = 0; i < m.PictrueGroupList.size(); i++) {
					if(m.PictrueGroupList.get(i).isSelected()) {
						//list.addAll(m.PictrueGroupList.get(i).picGroup);
						DMDir dir  = new DMDir();
						dir.mName = m.PictrueGroupList.get(i).folderName;
						dir.mPath = m.PictrueGroupList.get(i).folderPath;
						dir.mSize = m.PictrueGroupList.get(i).floderSize;
						dir.isDir = true;
						dir.mHidden = m.PictrueGroupList.get(i).mHidden;
						dir.mLastModify = m.PictrueGroupList.get(i).mLastModify;
						dir.mLocation = DMFile.LOCATION_UDISK;
						list.add(dir);
					}
				}
			}
		}else {
			for (int i = 0; i < mPicsUnitList.size(); i++) {
				List<DMFile> group = mPicsUnitList.get(i).picGroup;
				for (int j = 0; j < group.size(); j++) {
					DMFile file = group.get(j);
					if (file.selected) {
						list.add(file);
					}
				}
			}
		}

		return list;
	}

	@Override
	public void reloadItems() {
		// TODO Auto-generated method stub
		reloadItems(true);
	}
	
	private int picTitleY;

	private class PicScrollListener implements OnScrollListener {

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
			// TODO Auto-generated method stub
			
			if (mCurUiMode == UI_MODE_DATE) {
				
				if (picTitleHeight == 0 || mListTop == -1) {
					picTitleHeight = tvPicTitle.getHeight();
					mListTop = mList.getTop();
				} else {
					if (mList.getChildAt(0) == null) {
						return;
					}
						
					//下拉头
					if(mList.getRefreshableView().getFirstVisiblePosition() == 0) {
						tvPicTitle.setVisibility(View.INVISIBLE);
						return;
					} else {
						tvPicTitle.setVisibility(View.VISIBLE);
						curFirstChildIndex = mList.getRefreshableView().getFirstVisiblePosition() - 1;
					}
					if ((mPicsUnitList.get(curFirstChildIndex).type == PicsUnit.Tail || mPicsUnitList.get(curFirstChildIndex).isAlsoTail) && (mList.getChildAt(0).getBottom() - mListTop < picTitleHeight)) {
							picTitleY = mList.getChildAt(0).getBottom() - mListTop - picTitleHeight;
							tvPicTitle.layout(tvPicTitle.getLeft(), mListTop + picTitleY, tvPicTitle.getRight(), picTitleHeight + mListTop + picTitleY);
							tvPicTitle.invalidate();
					} else {
						picTitleY = 0;
						tvPicTitle.layout(tvPicTitle.getLeft(), mListTop, tvPicTitle.getRight(), picTitleHeight + mListTop);
						tvPicTitle.invalidate();
					}

					if (((mPicsUnitList.get(curFirstChildIndex).type == PicsUnit.Head) ||((mPicsUnitList.get(curFirstChildIndex).type == PicsUnit.Tail)
							|| (mPicsUnitList.get(curFirstChildIndex).isAlsoTail))) && mList.getRefreshableView().getFirstVisiblePosition() != oldFirstItemId) {
						oldFirstItemId = mList.getRefreshableView().getFirstVisiblePosition();
						tvPicTitle.setFocusable(true);
						tvPicTitle.requestFocus();
						tvPicTitle.setText(mPicsUnitList.get(curFirstChildIndex).date);
					}
				}
			}
			
		}

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			// TODO Auto-generated method stub
			if (scrollState == OnScrollListener.SCROLL_STATE_TOUCH_SCROLL || scrollState == OnScrollListener.SCROLL_STATE_FLING) {
				picScrolling = true;
				if (imageloaderPaused == false) {
//					DMImageLoader.getInstance().pause();
					imageloaderPaused = true;
				}
			} else {
				picScrolling = false;
				if (imageloaderPaused == true) {
//					DMImageLoader.getInstance().resume();
					imageloaderPaused = false;
//					DMImageLoader.getInstance().stop();
//					Log.d("ra_noti", "1571");
					//System.out.println("test:noti14");
//					mAdapter.notifyDataSetChanged();

					if (mCurUiMode == UI_MODE_DATE) {
						RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, DipPixelUtil.dip2px(getContext(), 36));
						lp.setMargins(DipPixelUtil.dip2px(getContext(), 8), picTitleY, DipPixelUtil.dip2px(getContext(), 8), 0);
						tvPicTitle.setLayoutParams(lp);
					}
					
				}
			}
		}

	}

}
