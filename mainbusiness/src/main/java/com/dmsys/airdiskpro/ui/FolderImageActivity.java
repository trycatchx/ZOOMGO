package com.dmsys.airdiskpro.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dm.baselib.BaseValue;
import com.dmairdisk.aodplayer.util.CommonAsync;
import com.dmsoftwareupgrade.util.UDiskBaseDialog;
import com.dmsys.airdiskpro.AttributeTask;
import com.dmsys.airdiskpro.adapter.PopupAdapter;
import com.dmsys.airdiskpro.event.DisconnectEvent;
import com.dmsys.airdiskpro.filemanager.EditState;
import com.dmsys.airdiskpro.filemanager.FileManager;
import com.dmsys.airdiskpro.filemanager.FileOperationHelper;
import com.dmsys.airdiskpro.filemanager.FileOperationService;
import com.dmsys.airdiskpro.model.PicsUnit;
import com.dmsys.airdiskpro.ui.imagereader.ImagePagerActivity;
import com.dmsys.airdiskpro.utils.DipPixelUtil;
import com.dmsys.airdiskpro.utils.FileInfoUtils;
import com.dmsys.airdiskpro.utils.FileUtil;
import com.dmsys.airdiskpro.utils.TimeTool;
import com.dmsys.airdiskpro.view.DMPopup;
import com.dmsys.airdiskpro.view.DMSortDialog;
import com.dmsys.airdiskpro.view.IFileExplorer;
import com.dmsys.airdiskpro.view.MessageDialog;
import com.dmsys.airdiskpro.view.PicImageView;
import com.dmsys.airdiskpro.view.PicLineLayout;
import com.dmsys.airdiskpro.view.PicTitleView;
import com.dmsys.dmsdk.DMSdk;
import com.dmsys.dmsdk.model.DMFile;
import com.dmsys.dmsdk.model.DMFileCategoryType;
import com.dmsys.dmsdk.model.DMFilePage;
import com.dmsys.dmsdk.model.DMRet;
import com.dmsys.dmsdk.model.DMSupportFunction;
import com.dmsys.mainbusiness.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.dm.longsys.library.imageloader.core.DMImageLoader;
import cn.dm.longsys.library.imageloader.core.DisplayImageOptions;
import cn.dm.longsys.library.imageloader.core.listener.PauseOnScrollListener;
import de.greenrobot.event.EventBus;

public class FolderImageActivity extends BaseActionActivity implements IFileExplorer, OnClickListener{

	private Context mContext;
	
	private String mFolderPath;
	
	private DisplayImageOptions mLoaderOptions;
	public final static int UnitSize = 4;
	private int mMode = EditState.STATE_NORMAL;
	private ListView mList;
	private View mEmptyLayout;
	private View mLoadingView;
	private ImageView titlebar_left;
	private RelativeLayout normalLayout;
	private RelativeLayout layout_edit;
	private TextView selectAllText;
	private TextView mainText;
	
	/**底部action栏**/
	private View bottomView;
	private TextView downloadText;
	private TextView copyText;
	private TextView deleteText;
	private View moreImage;
	
	private FolderImageAdapter mAdapter;
	private int imageRLWIdth;

	// 划分为4个为一组的数据
	private List<PicsUnit> mPicsUnitList = new ArrayList<>();
	// 没有划分成4个为一列表的数据
	private List<ArrayList<DMFile>> groupDatas = new ArrayList<>();
	
	private RelativeLayout layout_title_more;
	
	private DMPopup mPopup,titlePopup;
	private PopupAdapter mPopupAdapter;
	private WindowManager mWindowManager;
	
	private int picTitleHeight = 0;
	private int mListTop = -1;
	private int curFirstChildIndex = 0;
	private int picTitleY;
	private int oldFirstItemId = 0;
	public static boolean picScrolling = false;
	private boolean imageloaderPaused = false;
	private PicTitleView tvPicTitle;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_folder_image);
		
		mFolderPath = getIntent().getStringExtra("PATH");
		System.out.println("tttttt:"+mFolderPath);
		initViews();
		
	}


	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		//停止剪切赋值的service
		super.onStop();
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		init(this);
		reloadItems();
	}
	
	public void onEventMainThread(DisconnectEvent event){
		finish();
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		unInit();
		EventBus.getDefault().unregister(this);
	}
	
	private void initViews() {
		
		mContext = this;
		
		EventBus.getDefault().register(this);
		tvPicTitle = (PicTitleView) findViewById(R.id.tv_floder_image_title);
		mList = (ListView) findViewById(R.id.folder_image_list);
		mEmptyLayout = findViewById(R.id.emptyRl);
		mLoadingView = findViewById(R.id.loading);
		mAdapter = new FolderImageAdapter();
		mList.setAdapter(mAdapter);
		
		mList.setOnScrollListener(new PauseOnScrollListener(DMImageLoader.getInstance(), false, true, new PicScrollListener()));

		DisplayMetrics dpy = new DisplayMetrics();
		getWindow().getWindowManager().getDefaultDisplay().getMetrics(dpy);
		imageRLWIdth = getImageRLWidth(dpy.widthPixels);

		mPopupAdapter = new PopupAdapter(this);
		mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		
		tvPicTitle.setVisibility(View.VISIBLE);
		initLoaderOptions();
		initTitleBar();
		initBottomBar();
	}
	
	private void initTitleBar() {
		// TODO Auto-generated method stub
		FrameLayout back = (FrameLayout) findViewById(R.id.layout_back);
		titlebar_left = (ImageView) findViewById(R.id.titlebar_left);
		titlebar_left.setImageResource(R.drawable.dm_lib_wifi_back_btn_bg_selector);
		back.setOnClickListener(this);
		normalLayout = (RelativeLayout) findViewById(R.id.layout_normal);
		normalLayout.setVisibility(View.VISIBLE);
		selectAllText = (TextView) findViewById(R.id.text_selectall);
		selectAllText.setOnClickListener(this);

		mainText = (TextView) findViewById(R.id.titlebar_title);
		mainText.setText(mFolderPath.substring(mFolderPath.lastIndexOf("/") + 1));
		
		findViewById(R.id.layout_search).setVisibility(View.GONE);
		layout_title_more = (RelativeLayout) findViewById(R.id.layout_title_more);
		layout_title_more.setOnClickListener(this);
	}
	
	private void initBottomBar() {
		// TODO Auto-generated method stub
		
		bottomView = findViewById(R.id.bottom);
		
		downloadText = (TextView) findViewById(R.id.op_download);
		downloadText.setOnClickListener(this);
		copyText = (TextView) findViewById(R.id.op_cpTo);
		copyText.setOnClickListener(this);
		deleteText = (TextView) findViewById(R.id.op_delete);
		deleteText.setOnClickListener(this);
		moreImage = findViewById(R.id.op_more);
		moreImage.setOnClickListener(this);
	}
	
	private void fillData(List<DMFile> files) {
		if (files != null && files.size() > 0) {
			groupDatas.clear();

			long curDate = -1;
			ArrayList<DMFile> curList = null;
			/*
			 * 按照时间获取之后，吧所有的的文件同一个时间放到一个列表里面
			 */
			for (DMFile pic : files) {
				long date = pic.mLastModify;

				if (curDate == -1) {
					curDate = date;
				}
				if (curList == null) {
					curList = new ArrayList<>();
					groupDatas.add(curList);
				}

				if (TimeTool.isSameDayOfMillis(date, curDate)) {
					curList.add(pic);
				} else {
					if (curList != null) {
						curList = new ArrayList<>();
						groupDatas.add(curList);
					}
					curList.add(pic);
					curDate = date;
				}
			}
		}

		if (groupDatas.size() > 0) {
			
			Collections.sort(groupDatas, fileLastModifyComparator);
			
			mPicsUnitList.clear();
			mPicsUnitList = getPicsUnitList(groupDatas);
			Log.d("ra_checkS", "end addPPPS~~~");
		}
	}
	
	Comparator<ArrayList<DMFile>> fileLastModifyComparator = new Comparator<ArrayList<DMFile>>() {

		@Override
		public int compare(ArrayList<DMFile> f1, ArrayList<DMFile> f2) {
			if (f1 == null || f2 == null) {
				if (f1 == null) {
					return -1;
				} else if(f2 == null) {
					return 1;
				} else {
					return 0;
				}
			} else {
				
				Calendar calendar = Calendar.getInstance();
				calendar.setTimeInMillis(f1.get(0).mLastModify);
				int day1 = calendar.get(Calendar.DAY_OF_MONTH);
				int month1 = calendar.get(Calendar.MONTH);
				int year1 = calendar.get(Calendar.YEAR);
				calendar.clear();
				
				calendar.setTimeInMillis(f2.get(0).mLastModify);
				int day2 = calendar.get(Calendar.DAY_OF_MONTH);
				int month2 = calendar.get(Calendar.MONTH);
				int year2 = calendar.get(Calendar.YEAR);
				
				if (year1 > year2) {
					return -1;
				} else if (year1 == year2) {
					if (month1 > month2) {
						return -1;
					}else if(month1 == month2) {
						if (day1 > day2) {
							return -1;
						}else if(day1 == day2){
							return 0;
						} else {
							return 1;
						}
					} else {
						return 1;
					}
				} else {
					return 1;
				}
				
			}
		}
	};
	
	private List<PicsUnit> getPicsUnitList(List<ArrayList<DMFile>> groupList) {
		ArrayList<PicsUnit> unitList = new ArrayList<PicsUnit>();
		for (int i = 0; i < groupList.size(); i++) {
			addSameUnit(i, unitList, groupList.get(i));
		}
		return unitList;
	}

	private void addSameUnit(int groupid, ArrayList<PicsUnit> units,
			ArrayList<DMFile> group) {
		ArrayList<ArrayList<DMFile>> pieces = new ArrayList<ArrayList<DMFile>>();
		pieces = separateGroup(group);
		ArrayList<PicsUnit> unitsTemp = buildUnits(groupid, pieces);
		units.addAll(unitsTemp);
	}

	private static ArrayList<ArrayList<DMFile>> separateGroup(
			ArrayList<DMFile> group) {

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

	private ArrayList<PicsUnit> buildUnits(int groupid,
			ArrayList<ArrayList<DMFile>> pieces) {
		ArrayList<PicsUnit> res = new ArrayList<PicsUnit>();
		for (int i = 0; i < pieces.size(); i++) {
			if (i == 0) {
				PicsUnit unit = new PicsUnit();
				unit.unitGroupId = groupid;
				unit.unitId = i;
				unit.picGroup = pieces.get(i);
				unit.type = PicsUnit.Head;
				res.add(unit);
			} else {
				PicsUnit unit = new PicsUnit();
				unit.unitGroupId = groupid;
				unit.unitId = i;
				unit.picGroup = pieces.get(i);
				unit.type = PicsUnit.Mid;
				res.add(unit);
			}
		}
		if (res.get(res.size() - 1).type != PicsUnit.Head)
			res.get(res.size() - 1).type = PicsUnit.Tail;

		res.get(res.size() - 1).isAlsoTail = true;
		return res;
	}

	private int getImageRLWidth(int screenWith) {
		return (screenWith - DipPixelUtil.dip2px(this, 24)) / UnitSize;
	}
	
	private void initLoaderOptions() {

		mLoaderOptions = new DisplayImageOptions.Builder().cacheInMemory(true)
				.showImageOnFail(R.drawable.filemanager_photo_fail)
				.useThumb(true)
				.cacheOnDisk(true)
				.showImageOnLoading(R.drawable.ready_to_loading_image)
				.showImageForEmptyUri(R.drawable.filemanager_photo_fail)
				.considerExifParams(true).build();

	}
	
	@Override
	public void onOperationEnd(String opt) {
		// TODO Auto-generated method stub
		switchMode(false);
		if (opt != null && opt.equals(getString(R.string.DM_Task_Delete))) {
			reloadItems();
		}
	}

	@Override
	public void switchMode(int mode) {
		// TODO Auto-generated method stub
		if (mMode == mode)
			return;
		if (mode == EditState.STATE_NORMAL) {
			mMode = EditState.STATE_NORMAL;
		} else if (mode == EditState.STATE_EDIT) {
			mMode = EditState.STATE_EDIT;
		}
		mAdapter.notifyDataSetChanged();
	}

	@Override
	public void selectAll() {
		// TODO Auto-generated method stub
		Iterator<PicsUnit> iter = mPicsUnitList.iterator();
		while (iter.hasNext()) {
			FileManager.selectAll(iter.next().picGroup, null);
		}
		mAdapter.notifyDataSetChanged();
		mainText.setText(String.format(getResources().getString(R.string.DM_Navigation_Upload_Num), String.valueOf(getSelectedFiles().size())));
	}

	@Override
	public void unselectAll() {
		// TODO Auto-generated method stub
		Iterator<PicsUnit> iter = mPicsUnitList.iterator();
		while (iter.hasNext()) {
			FileManager.unselectAll(iter.next().picGroup);
		}
		mAdapter.notifyDataSetChanged();
	}

	@Override
	public List<DMFile> getSelectedFiles() {
		// TODO Auto-generated method stub
		List<DMFile> list = new ArrayList<>();
		for (int i = 0; i < mPicsUnitList.size(); i++) {
			List<DMFile> group = mPicsUnitList.get(i).picGroup;
			for (int j = 0; j < group.size(); j++) {
				DMFile file = group.get(j);
				if (file.selected) {
					list.add(file);
				}
			}
		}
		return list;
	}

	@Override
	public void reloadItems() {
		// TODO Auto-generated method stub
		mLoadingView.setVisibility(View.VISIBLE);
		new LoadImageTask().execute();
	}
	
	class LoadImageTask extends AsyncTask<Void, Void, List<DMFile>>{

		@Override
		protected List<DMFile> doInBackground(Void... params) {
			// TODO Auto-generated method stub
			List<DMFile> files = null;
			DMFilePage filePage = DMSdk.getInstance().getFileListInDirByType(DMFileCategoryType.E_PICTURE_CATEGORY, mFolderPath, 0);
			if (filePage != null && filePage.getFiles() != null) {
				int pp = filePage.getTotalPage();
				files = filePage.getFiles();
			}
			
			return files ;
		}
		
		@Override
		protected void onPostExecute(List<DMFile> result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			if (result != null && result.size() > 0) {
				fillData(result);
				//设置title为文件夹名字
				mAdapter.notifyDataSetChanged();
				mLoadingView.setVisibility(View.GONE);
				mEmptyLayout.setVisibility(View.GONE);
			} else {
				mLoadingView.setVisibility(View.GONE);
				mEmptyLayout.setVisibility(View.VISIBLE);
				mList.setVisibility(View.GONE);
				mPicsUnitList.clear();
				groupDatas.clear();
			}
		}
		
	}
	
	private class FolderImageAdapter extends BaseAdapter {
		private int gridWidth;
		private LayoutInflater inflater = null;
		private DMImageLoader imageLoader;

		public FolderImageAdapter() {
			super();
			inflater = LayoutInflater.from(FolderImageActivity.this);
			imageLoader = DMImageLoader.getInstance();
		}

		@Override
		public int getCount() {
			return mPicsUnitList.size();
		}

		@Override
		public Object getItem(int position) {
			return mPicsUnitList.get(position).picGroup;
		}

		public int getPositionType(int position) {
			return mPicsUnitList.get(position).type;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
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
			loadData2Holder(mPicsUnitList.get(position), imageLoader,
					mLoaderOptions, holder, imageRLWIdth);

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

			// public ImageView ivDivide;
			public PicLineLayout llLine;
		}

		/*
		 * 这里就是选中的一个事件
		 */
		private class FolderImageItemClickListener implements OnClickListener {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if (mMode == EditState.STATE_NORMAL) {
					// XLFile file = mDatas.get(position);
					int positionInGroup = ((PicImageView) arg0).getUnitId() * UnitSize + ((PicImageView) arg0).getIdInLine();
					int countInPreGroup = getPositionByGroupId(((PicImageView) arg0).getUnitGroupId());
					int positionInAll = positionInGroup + countInPreGroup;
					FileOperationHelper.getInstance().openPicture(mContext, groupDatas,
							positionInAll,ImagePagerActivity.IS_FROM_FolderImageActivity);
				} else {
					FileManager.changeSelectState(((PicImageView) arg0).getDMFile()); // 反选
					// 被选中的保存起来
					ImageView icon = ((PicImageView) arg0).getIcon();
					if (icon != null) {
						icon.setVisibility(View.VISIBLE);
						icon.setSelected(((PicImageView) arg0).getDMFile().selected);
					}
					
					mainText.setText(String.format(getResources().getString(R.string.DM_Navigation_Upload_Num), String.valueOf(getSelectedFiles().size())));
					
				}
			}

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
					lineCount = lineCount
							+ mPicsUnitList.get(i).picGroup.size();
				}
			}
			return lineCount;
		}


		private void initHolder(PicHolder holder, View convertView) {
			// holder.ivDivide = (ImageView) convertView
			// .findViewById(R.id.iv_line_divide);
			holder.ivIcon0 = (PicImageView) convertView
					.findViewById(R.id.piv_line_icon0);
			holder.ivIcon1 = (PicImageView) convertView
					.findViewById(R.id.piv_line_icon1);
			holder.ivIcon2 = (PicImageView) convertView
					.findViewById(R.id.piv_line_icon2);
			holder.ivIcon3 = (PicImageView) convertView
					.findViewById(R.id.piv_line_icon3);
			holder.ivOperation0 = (ImageView) convertView
					.findViewById(R.id.iv_line_operatinobtn0);
			holder.ivOperation1 = (ImageView) convertView
					.findViewById(R.id.iv_line_operatinobtn1);
			holder.ivOperation2 = (ImageView) convertView
					.findViewById(R.id.iv_line_operatinobtn2);
			holder.ivOperation3 = (ImageView) convertView
					.findViewById(R.id.iv_line_operatinobtn3);
			holder.rlPicItem0 = (RelativeLayout) convertView
					.findViewById(R.id.rl_line_picitem0);
			holder.rlPicItem1 = (RelativeLayout) convertView
					.findViewById(R.id.rl_line_picitem1);
			holder.rlPicItem2 = (RelativeLayout) convertView
					.findViewById(R.id.rl_line_picitem2);
			holder.rlPicItem3 = (RelativeLayout) convertView
					.findViewById(R.id.rl_line_picitem3);
			holder.tvDate = (TextView) convertView
					.findViewById(R.id.tv_line_date);
			holder.llLine = (PicLineLayout) convertView
					.findViewById(R.id.ll_line);

			// 这一步就是为了把引用给保存进去，在onclick可以拿出来使用
			holder.ivIcon0.setIcon(holder.ivOperation0);
			holder.ivIcon1.setIcon(holder.ivOperation1);
			holder.ivIcon2.setIcon(holder.ivOperation2);
			holder.ivIcon3.setIcon(holder.ivOperation3);

			holder.ivIcon0
					.setOnClickListener(new FolderImageItemClickListener());
			holder.ivIcon1
					.setOnClickListener(new FolderImageItemClickListener());
			holder.ivIcon2
					.setOnClickListener(new FolderImageItemClickListener());
			holder.ivIcon3
					.setOnClickListener(new FolderImageItemClickListener());
		}

		private void loadData2Holder(PicsUnit unit, DMImageLoader imageLoader,
				DisplayImageOptions options, PicHolder holder, int width) {

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

				loadImageDate(holder.ivIcon0, unitId, unitGroupId,
						fileList.get(0), 0);
				loadOperatorDate(holder.ivOperation0, fileList.get(0));
				String uri0 = FileOperationHelper.getInstance().getFullPath(fileList.get(0));
				loadImage(imageLoader, options, holder.ivIcon0, uri0);

			} else if (fileList.size() == 2) {
				holder.ivIcon0.setVisibility(View.VISIBLE);
				holder.ivIcon1.setVisibility(View.VISIBLE);
				holder.ivIcon2.setVisibility(View.INVISIBLE);
				holder.ivIcon3.setVisibility(View.INVISIBLE);
				holder.ivOperation0.setVisibility(View.VISIBLE);
				holder.ivOperation1.setVisibility(View.VISIBLE);
				holder.ivOperation2.setVisibility(View.INVISIBLE);
				holder.ivOperation3.setVisibility(View.INVISIBLE);

				loadImageDate(holder.ivIcon0, unitId, unitGroupId,
						fileList.get(0), 0);
				loadOperatorDate(holder.ivOperation0, fileList.get(0));
				String uri0 = FileOperationHelper.getInstance().getFullPath(fileList.get(0));
				loadImage(imageLoader, options, holder.ivIcon0, uri0);

				loadImageDate(holder.ivIcon1, unitId, unitGroupId,
						fileList.get(1), 1);
				loadOperatorDate(holder.ivOperation1, fileList.get(1));
				String uri1 = FileOperationHelper.getInstance().getFullPath(fileList.get(1));
				loadImage(imageLoader, options, holder.ivIcon1, uri1);

			} else if (fileList.size() == 3) {
				holder.ivIcon0.setVisibility(View.VISIBLE);
				holder.ivIcon1.setVisibility(View.VISIBLE);
				holder.ivIcon2.setVisibility(View.VISIBLE);
				holder.ivIcon3.setVisibility(View.GONE);
				holder.ivOperation0.setVisibility(View.VISIBLE);
				holder.ivOperation1.setVisibility(View.VISIBLE);
				holder.ivOperation2.setVisibility(View.VISIBLE);
				holder.ivOperation3.setVisibility(View.GONE);

				loadImageDate(holder.ivIcon0, unitId, unitGroupId,
						fileList.get(0), 0);
				loadOperatorDate(holder.ivOperation0, fileList.get(0));
				String uri0 = FileOperationHelper.getInstance().getFullPath(fileList.get(0));
				loadImage(imageLoader, options, holder.ivIcon0, uri0);

				loadImageDate(holder.ivIcon1, unitId, unitGroupId,
						fileList.get(1), 1);
				loadOperatorDate(holder.ivOperation1, fileList.get(1));
				String uri1 = FileOperationHelper.getInstance().getFullPath(fileList.get(1));
				loadImage(imageLoader, options, holder.ivIcon1, uri1);

				loadImageDate(holder.ivIcon2, unitId, unitGroupId,
						fileList.get(2), 2);
				loadOperatorDate(holder.ivOperation2, fileList.get(2));
				String uri2 = FileOperationHelper.getInstance().getFullPath(fileList.get(2));
				loadImage(imageLoader, options, holder.ivIcon2, uri2);
			} else if (fileList.size() == 4) {
				holder.ivIcon0.setVisibility(View.VISIBLE);
				holder.ivIcon1.setVisibility(View.VISIBLE);
				holder.ivIcon2.setVisibility(View.VISIBLE);
				holder.ivIcon3.setVisibility(View.VISIBLE);
				holder.ivOperation0.setVisibility(View.VISIBLE);
				holder.ivOperation1.setVisibility(View.VISIBLE);
				holder.ivOperation2.setVisibility(View.VISIBLE);
				holder.ivOperation3.setVisibility(View.VISIBLE);

				loadImageDate(holder.ivIcon0, unitId, unitGroupId,
						fileList.get(0), 0);
				loadOperatorDate(holder.ivOperation0, fileList.get(0));
				String uri0 = FileOperationHelper.getInstance().getFullPath(fileList.get(0));
				loadImage(imageLoader, options, holder.ivIcon0, uri0);

				loadImageDate(holder.ivIcon1, unitId, unitGroupId,
						fileList.get(1), 1);
				loadOperatorDate(holder.ivOperation1, fileList.get(1));
				String uri1 = FileOperationHelper.getInstance().getFullPath(fileList.get(1));
				loadImage(imageLoader, options, holder.ivIcon1, uri1);

				loadImageDate(holder.ivIcon2, unitId, unitGroupId,
						fileList.get(2), 2);
				loadOperatorDate(holder.ivOperation2, fileList.get(2));
				String uri2 = FileOperationHelper.getInstance().getFullPath(fileList.get(2));
				loadImage(imageLoader, options, holder.ivIcon2, uri2);

				loadImageDate(holder.ivIcon3, unitId, unitGroupId,
						fileList.get(3), 3);
				loadOperatorDate(holder.ivOperation3, fileList.get(3));
				String uri3 = FileOperationHelper.getInstance().getFullPath(fileList.get(3));
				loadImage(imageLoader, options, holder.ivIcon3, uri3);
			}
		}

		private void resetImageRLSize(RelativeLayout rl, int width) {
			LinearLayout.LayoutParams param = (LinearLayout.LayoutParams) rl
					.getLayoutParams();
			param.height = width;
			param.width = width;
			rl.setLayoutParams(param);
		}

		private void updateDateDivideView(PicHolder holder, PicsUnit picUnit) {

			if (picUnit.type == PicsUnit.Head) {
				holder.llLine.setTail(false);
				holder.tvDate.setVisibility(View.VISIBLE);
				// holder.ivDivide.setVisibility(View.VISIBLE);
				holder.llLine.setHead(true);
			} else if (picUnit.type == PicsUnit.Mid) {
				holder.llLine.setTail(false);
				holder.tvDate.setVisibility(View.GONE);
				// holder.ivDivide.setVisibility(View.VISIBLE);
				holder.llLine.setHead(false);
			} else if (picUnit.type == PicsUnit.Tail) {
				holder.llLine.setTail(true);
				holder.tvDate.setVisibility(View.GONE);
				// holder.ivDivide.setVisibility(View.VISIBLE);
				holder.llLine.setHead(false);
			}
			if (picUnit.isAlsoTail) {
				holder.llLine.setTail(true);
				// holder.ivDivide.setVisibility(View.GONE);
			}

			if (holder.llLine.isHead()) {
				holder.tvDate
						.setText(formatPicDate(picUnit.picGroup.get(0).mLastModify));
			}

			holder.llLine
					.setDate(formatPicDate(picUnit.picGroup.get(0).mLastModify));
			
			if (tvPicTitle.getText() == null
					|| tvPicTitle.getText().equals("")) {
				tvPicTitle
						.setText(formatPicDate(picUnit.picGroup.get(0).mLastModify));
				tvPicTitle.setVisibility(View.VISIBLE);
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
			switch (mWay) {
			case 1:
				week = mContext.getString(R.string.DM_Sunday);
				break;
			case 2:
				week = mContext.getString(R.string.DM_Monday);
				break;
			case 3:
				week = mContext.getString(R.string.DM_Tuesday);
				break;
			case 4:
				week = mContext.getString(R.string.DM_Wednesday);
				break;
			case 5:
				week = mContext.getString(R.string.DM_Thursday);
				break;
			case 6:
				week = mContext.getString(R.string.DM_Friday);
				break;
			case 7:
				week = mContext.getString(R.string.DM_Saturday);
				break;
			}
			String date = year
					+ "-"
					+ (month + 1)
					+ "-" + day
					+  " "
					+ week;
			return date;
		}

		private void loadImageDate(PicImageView imageView, int unitId,
				int unitGroupId, DMFile DMFile, int idInLine) {
			imageView.setUnitId(unitId);
			imageView.setUnitGroupId(unitGroupId);
			imageView.setDMFile(DMFile);
			imageView.setIdInLine(idInLine);
		}

		private void loadOperatorDate(ImageView operView, DMFile file) {
			if (mMode == EditState.STATE_NORMAL) {
				operView.setVisibility(View.GONE);
				operView.setSelected(false);
			} else {
				// 这里进行了一个显示。但是要根据operView 的一个选中状态进行显示图片，如果没有被选中的话是显示一张透明的图片
				operView.setVisibility(View.VISIBLE);
				operView.setSelected(file.selected);
			}
		}


		private void loadImage(DMImageLoader imageLoader,DisplayImageOptions options, final ImageView iconview,String uri) {
			uri = FileInfoUtils.encodeUri(uri);
			imageLoader.displayImage(uri, iconview, options);
		}

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		int i = v.getId();
		if (i == R.id.layout_back) {
			if (mMode == EditState.STATE_NORMAL) {
				finish();
			} else {
				switchMode(false);
			}

		} else if (i == R.id.text_selectall) {
			if (selectAllText.getText().equals(getString(R.string.DM_Control_Select))) {
				selectAllText.setText(R.string.DM_Control_Uncheck_All);
				selectAll();
			} else {
				selectAllText.setText(R.string.DM_Control_Select);
				unselectAll();
			}

		} else if (i == R.id.layout_title_more) {
			showTitleDialog();

		} else if (i == R.id.item_edit) {
			titlePopup.dismiss();
			switchMode(true);

		} else if (i == R.id.item_sort) {
			titlePopup.dismiss();
			showSortDialog();

		} else if (i == R.id.op_download) {
			doFileOperation(FileOperationService.FILE_OP_DOWNLOAD);

		} else if (i == R.id.op_cpTo) {
			final List<DMFile> files = getSelectedFiles();
			if (files.size() == 0) {
				Toast.makeText(this, R.string.DM_FileOP_Warn_Select_File, Toast.LENGTH_SHORT).show();
				return;
			}
			FileOperationService.selectedList = files;
			Intent cpintent = new Intent(this, PathSelectActivity.class);
			cpintent.putExtra(PathSelectActivity.EXTRA_OP, PathSelectActivity.COPYTO);
			startActivity(cpintent);

		} else if (i == R.id.op_delete) {
			doFileOperation(FileOperationService.FILE_OP_DELETE);

		} else if (i == R.id.op_more) {
			showMoreDialog();

		} else {
		}
	}
	
	private void showTitleDialog() {
		// TODO Auto-generated method stub
		if (titlePopup != null && titlePopup.isShowing()) {
			titlePopup.dismiss();
			return;
		}
		titlePopup = new DMPopup(this,DMPopup.VERTICAL);
		View contentView = LayoutInflater.from(this).inflate(R.layout.popup_operation, null);
		TextView editText = (TextView) contentView.findViewById(R.id.item_edit);
		TextView newText = (TextView) contentView.findViewById(R.id.item_newfolder);
		TextView sortText = (TextView) contentView.findViewById(R.id.item_sort);
		editText.setOnClickListener(this);
		sortText.setOnClickListener(this);
		
		newText.setVisibility(View.GONE);
		
		LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		contentView.setLayoutParams(params);
		titlePopup.addView(contentView);
		titlePopup.show(layout_title_more);
	}

	private void showSortDialog(){
		final DMSortDialog sortDialog = new DMSortDialog(this, UDiskBaseDialog.TYPE_TWO_BTN);
		sortDialog.setTitleContent(getString(R.string.DM_File_Sort));
		sortDialog.setLeftBtn(getString(R.string.DM_SetUI_Cancel), new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				
			}
		});
		sortDialog.setRightBtn(getString(R.string.DM_SetUI_Confirm), new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				setFileSortInfo(sortDialog.getCurrentSortType(),sortDialog.getCurrentSortOrder());
			}
		});
		sortDialog.show();
		
	}

	protected void setFileSortInfo(final int currentSortType, final int currentSortOrder) {
		// TODO Auto-generated method stub
		CommonAsync.Runnable runnable = new CommonAsync.Runnable() {
			
			@Override
			public void stop() {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public Object run() {
				// TODO Auto-generated method stub
				DMSdk.getInstance().setFileSortType(currentSortType);
				DMSdk.getInstance().setFileSortOrder(currentSortOrder);
				return 0;
			}
		};
		
		CommonAsync.CommonAsyncListener listener = new CommonAsync.CommonAsyncListener() {
			
			@Override
			public void onResult(Object ret) {
				// TODO Auto-generated method stub
				reloadItems();
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

	public void doFileOperation(final int op) {

		final List<DMFile> list = getSelectedFiles();
		if (op != FileOperationService.FILE_OP_NEWFOLDER && list.size() == 0) {
			Toast.makeText(this, R.string.DM_FileOP_Warn_Select_File, Toast.LENGTH_SHORT).show();
			return;
		}

		if (op == FileOperationService.FILE_OP_DELETE) {
			MessageDialog builder = new MessageDialog(this);
			builder.setTitleContent(getString(R.string.DM_Task_Delete));
			builder.setMessage(getString(R.string.DM_Remind_Operate_Delete_File));
			builder.setLeftBtn(getString(R.string.DM_Control_Cancel), new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
				}
			});

			builder.setRightBtn(getString(R.string.DM_Control_Definite), new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					doFileOperation(op, list);
				}
			});

			builder.show();

		} else {
			doFileOperation(op, list);
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (mMode == EditState.STATE_EDIT) {
				switchMode(false);
				return true;
			}
		}

		return super.onKeyDown(keyCode, event);
	}
	
	private void switchMode(boolean bEdit) {
		if (bEdit) {
			bottomView.setVisibility(View.VISIBLE);
			selectAllText.setVisibility(View.VISIBLE);

			titlebar_left.setImageResource(R.drawable.sel_upload_close);
			normalLayout.setVisibility(View.GONE);

			mainText.setText(String.format(getResources().getString(R.string.DM_Navigation_Upload_Num), "0"));
			
			switchMode(EditState.STATE_EDIT);
		} else {
			bottomView.setVisibility(View.GONE);
			selectAllText.setVisibility(View.GONE);

			titlebar_left.setImageResource(R.drawable.dm_lib_wifi_back_btn_bg_selector);
			normalLayout.setVisibility(View.VISIBLE);

			mainText.setText(mFolderPath.substring(mFolderPath.lastIndexOf("/") +1));
			
			unselectAll();
			switchMode(EditState.STATE_NORMAL);
		}
	}
	
	private void showMoreDialog() {
		// TODO Auto-generated method stub
		
		if (mPopup != null && mPopup.isShowing()) {
			mPopup.dismiss();
			return;
		}
		
		final List<DMFile> files = getSelectedFiles();
		if (files.size() == 0) {
			Toast.makeText(this, R.string.DM_FileOP_Warn_Select_File, Toast.LENGTH_SHORT).show();
			return;
		}
		
		mPopup = new DMPopup(this,DMPopup.VERTICAL);
		
		View contentView = LayoutInflater.from(this).inflate(R.layout.popup_content, null);
		
		ListView listView = (ListView) contentView.findViewById(R.id.pop_list);
		final List<String> mdata = getPopData(files);
		mPopupAdapter.setData(mdata);
		listView.setAdapter(mPopupAdapter);
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// TODO Auto-generated method stub
				if (mdata.get(position).equals(getString(R.string.DM_Task_Open_By))) {
					onClickThirdParty(files.get(0));
				}else if (mdata.get(position).equals(getString(R.string.DM_Task_Share))) {
					onClickShare(files.get(0));
				}else if (mdata.get(position).equals(getString(R.string.DM_Task_Rename))) {
					onClickRename(files.get(0));
				}else if (mdata.get(position).equals(getString(R.string.DM_Task_Details))) {
					onClickDetail(files);
				}else if (mdata.get(position).equals(getString(R.string.DM_Task_File_Hide))) {
					onClickHide(true,files.get(0));
				}else if (mdata.get(position).equals(getString(R.string.DM_Task_File_Unhide))) {
					onClickHide(false,files.get(0));
				}
				
				mPopup.dismiss();
			}
		});

		LayoutParams params = new LayoutParams((mWindowManager.getDefaultDisplay().getWidth()*1)/2, LayoutParams.WRAP_CONTENT);
		contentView.setLayoutParams(params);
		mPopup.addView(contentView);
		
		mPopup.show(findViewById(R.id.bottom));
		
	}
	
	protected void onClickHide(final boolean hide,final DMFile file) {
		// TODO Auto-generated method stub
		CommonAsync.Runnable runnable = new CommonAsync.Runnable() {
			
			@Override
			public void stop() {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public Object run() {
				// TODO Auto-generated method stub
				return DMSdk.getInstance().setFileHide(file.mPath, hide);
			}
		};
		
		CommonAsync.CommonAsyncListener listener = new CommonAsync.CommonAsyncListener() {
			
			@Override
			public void onResult(Object result) {
				// TODO Auto-generated method stub
				int ret = (int) result;
				System.out.println("sethide ret:"+ret);
				if (ret == DMRet.ACTION_SUCCESS) {
					switchMode(false);
					reloadItems();
				}else {
					if (ret == 10262) {
						Toast.makeText(FolderImageActivity.this, R.string.DM_Task_Filesystem_Not_Surpport, Toast.LENGTH_SHORT).show();
					}else {
						Toast.makeText(FolderImageActivity.this, R.string.DM_SetUI_Failed_Operation, Toast.LENGTH_SHORT).show();
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
		
		CommonAsync async = new CommonAsync(runnable, listener);
		async.executeOnExecutor((ExecutorService) Executors.newCachedThreadPool());
	}

	protected void onClickThirdParty(DMFile file) {
		// TODO Auto-generated method stub
		if (file.getType() == DMFileCategoryType.E_BOOK_CATEGORY && file.mLocation == DMFile.LOCATION_UDISK && !file.mPath.toLowerCase().endsWith(".txt")) {
			downloadFileToDO(this,file,DOWN_TO_OPEN);
		}else if (file.getType() == DMFileCategoryType.E_PICTURE_CATEGORY && file.mLocation == DMFile.LOCATION_UDISK) {
			downloadFileToDO(this,file,DOWN_TO_OPEN);
		}else
			FileUtil.thirdPartOpen(file,this);
	}

	protected void onClickShare(DMFile file) {
		// TODO Auto-generated method stub
		shareFile(file);
	}
	
	private void onClickRename(DMFile file) {
		// TODO Auto-generated method stub
		renameFile(file);
	}
	
	protected void onClickDetail(List<DMFile> files) {
		// TODO Auto-generated method stub
		AttributeTask mAttributeTask = null;
		
		if (files.size() == 1) {
			mAttributeTask = new AttributeTask(this, files.get(0));
		}else {
			mAttributeTask = new AttributeTask(this, files);
		}
		mAttributeTask.execute();
	}

	private List<String> getPopData(List<DMFile> files) {
		// TODO Auto-generated method stub
		List<String> data = new ArrayList<>();
		if (files.size() == 1) {
			
			if (DMSupportFunction.isSupportFileHide(BaseValue.supportFucntion)) {
				if (files.get(0).mHidden) {
					data.add(getString(R.string.DM_Task_File_Unhide));
				}else {
					data.add(getString(R.string.DM_Task_File_Hide));
				}
			}
			
			if (files.get(0).isDir == true) {
				data.add(getString(R.string.DM_Task_Rename));
				data.add(getString(R.string.DM_Task_Details));
			}else {
				data.add(getString(R.string.DM_Task_Open_By));
				data.add(getString(R.string.DM_Task_Share));
				data.add(getString(R.string.DM_Task_Rename));
				data.add(getString(R.string.DM_Task_Details));
			}
		}else if (files.size() > 1) {
			data.add(getString(R.string.DM_Task_Details));
		}
		return data;
	}
	
	private class PicScrollListener implements OnScrollListener {

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int totalItemCount) {
			// TODO Auto-generated method stub
			if (picTitleHeight == 0 || mListTop == -1) {
				picTitleHeight = tvPicTitle.getHeight();
				mListTop = mList.getTop();

			} else {
				curFirstChildIndex = 0;

				if (mList.getChildAt(curFirstChildIndex) == null)
					return;
				if ((((PicLineLayout) (mList.getChildAt(curFirstChildIndex)))
						.isTail())
						&& (mList.getChildAt(curFirstChildIndex).getBottom()
								- mListTop < picTitleHeight)) {
					picTitleY = mList.getChildAt(curFirstChildIndex)
							.getBottom() - mListTop - picTitleHeight;
					tvPicTitle.layout(tvPicTitle.getLeft(), mListTop
							+ picTitleY, tvPicTitle.getRight(), picTitleHeight
							+ mListTop + picTitleY);
					tvPicTitle.invalidate();
				} else {
					picTitleY = 0;
					tvPicTitle.layout(tvPicTitle.getLeft(), mListTop,
							tvPicTitle.getRight(), picTitleHeight + mListTop);
					tvPicTitle.invalidate();
					tvPicTitle.setVisibility(View.VISIBLE);
				}

				if (((((PicLineLayout) (mList.getChildAt(curFirstChildIndex))).isHead()) || (((PicLineLayout) (mList.getChildAt(curFirstChildIndex))).isTail()))
						&& mList.getFirstVisiblePosition() != oldFirstItemId) {
					oldFirstItemId = mList.getFirstVisiblePosition();
					tvPicTitle.setText(((PicLineLayout) mList
							.getChildAt(curFirstChildIndex)).getDate());
				}
			}
		}

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			// TODO Auto-generated method stub
			if (scrollState == OnScrollListener.SCROLL_STATE_TOUCH_SCROLL
					|| scrollState == OnScrollListener.SCROLL_STATE_FLING) {
				picScrolling = true;
				if (imageloaderPaused == false) {
					// DMImageLoader.getInstance().pause();
					imageloaderPaused = true;
				}
			} else {
				picScrolling = false;
				if (imageloaderPaused == true) {
					// DMImageLoader.getInstance().resume();
					imageloaderPaused = false;
					// DMImageLoader.getInstance().stop();
					// Log.d("ra_noti", "1571");
					// System.out.println("test:noti14");
					// mAdapter.notifyDataSetChanged();

					RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
							RelativeLayout.LayoutParams.MATCH_PARENT,
							DipPixelUtil.dip2px(mContext, 36));
					lp.setMargins(DipPixelUtil.dip2px(mContext, 8), picTitleY,
							DipPixelUtil.dip2px(mContext, 8), 0);
					tvPicTitle.setLayoutParams(lp);
				}
			}
		}

	}

}
