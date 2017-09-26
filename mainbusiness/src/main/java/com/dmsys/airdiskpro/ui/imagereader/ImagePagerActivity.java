package com.dmsys.airdiskpro.ui.imagereader;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.dm.baselib.BaseValue;
import com.dmsys.airdiskpro.event.DisconnectEvent;
import com.dmsys.airdiskpro.event.PasswordChangeEvent;
import com.dmsys.airdiskpro.filemanager.FileOperationHelper;
import com.dmsys.airdiskpro.filemanager.FileOperationService;
import com.dmsys.airdiskpro.ui.BaseActionActivity;
import com.dmsys.airdiskpro.ui.imagereader.Constants.Extra;
import com.dmsys.airdiskpro.utils.FileInfoUtils;
import com.dmsys.airdiskpro.utils.HandlerUtil;
import com.dmsys.airdiskpro.view.MessageDialog;
import com.dmsys.dmsdk.DMSdk;
import com.dmsys.dmsdk.model.DMFile;
import com.dmsys.dmsdk.model.DMStorageInfo;
import com.dmsys.dropbox.api.DMDropboxAPI;
import com.dropbox.client2.exception.DropboxException;
import com.dmsys.mainbusiness.R;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.dm.longsys.library.imageloader.core.DMImageLoader;
import de.greenrobot.event.EventBus;

/**
 * 获取toString显示统一个文件路径：
 * file："file:///storage/sdcard0/Music/%E7%A7%8B%E6%84%8F%E6%B5%93.mp3"
 * http："http://storage/sdcard0/Music/%E7%A7%8B%E6%84%8F%E6%B5%93.mp3"
 * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
 */
public class ImagePagerActivity extends BaseActionActivity implements OnClickListener{
	
	public Context mContext;
	private long  activityStartTime;
	private static final String STATE_POSITION = "STATE_POSITION";
	public static final String EXIT_WHEN_DETACHED = "exit_when_detached";
	private boolean bExit;
	public static String CUR_IMAGE_POSITION = "current.image.position";
	
	public static final int IS_FROM_FileExplorerView = 0;
	public static final int IS_FROM_FileExplorerDirView = 1;
	public static final int IS_FROM_FolderImageActivity = 2;
	public static final int IS_FROM_DropBox = 3;
	
	ImageViewPager pager;
	List<String> imageUrls = new ArrayList<String>();
	int pagerPosition = 0;
	private ImageView ivBack,iv_copy,iv_share,iv_delete;
	private TextView tvImageCount,tvImageName;
	private View rlImageInfoBar,llyt_image_bottom_bar;
	public  List<ArrayList<DMFile>> mDatas;
	public int isFrom = 0;
	private HandlerUtil.StaticHandler mHandler;
	private ImagePagerAdapter adapter;
	private Animation AnimTop,AnimBottom;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_image_pager);
		
		mHandler = new HandlerUtil.StaticHandler();
		mContext = this;
		//获取参数
		Bundle bundle = getIntent().getExtras();
		bExit = getIntent().getBooleanExtra(EXIT_WHEN_DETACHED, false);
		boolean paramIsILLegal = true;
		if(bundle != null) {
			isFrom = bundle.getInt(Extra.IMAGE_FROM,0);
		} 
		if(checkParam()) {
			fillData();
			paramIsILLegal = false;
		}
		if(paramIsILLegal || imageUrls == null) {
			finish();
			//Toast.makeText(this, R.string.DM_ImageReader_Parameter_Is_Empty, Toast.LENGTH_LONG).show();
			return;
		} 
		
		if (savedInstanceState != null) {
			pagerPosition = savedInstanceState.getInt(STATE_POSITION);
		}

		initViews();
		
		setImageinfo(imageUrls.get(pagerPosition),calcPositionInCurGroup(pagerPosition)+ 1,calcTotalInCurGroup(pagerPosition));
	}
	
	
	private void initViews() {
		
		EventBus.getDefault().register(this);
		
		ivBack = (ImageView)findViewById(R.id.ivBack);
		tvImageCount = (TextView)findViewById(R.id.tvImageCount);
		tvImageName = (TextView)findViewById(R.id.tvImageName);
		rlImageInfoBar = (View)findViewById(R.id.rlImageInfoBar);
		llyt_image_bottom_bar = (View)findViewById(R.id.llyt_image_bottom_bar);
		//退出
		ivBack.setOnClickListener(this);
		
		pager = (ImageViewPager) findViewById(R.id.pager);
		adapter = new ImagePagerAdapter(imageUrls);
		pager.setAdapter(adapter);
		pager.setCurrentItem(pagerPosition);
		pager.setOnPageChangeListener(new OnPageChangeListener() {
			
			@Override
			public void onPageSelected(int arg0) {
				pagerPosition = arg0;
				if(isFrom == 1) {
					EventBus.getDefault().post(new FileManagerDirViewCallBackPosition(pagerPosition));
				} else if(isFrom == 0){
					EventBus.getDefault().post(new FileExplorerViewCallBackPosition(pagerPosition));
				} else if(isFrom == 2) {
					EventBus.getDefault().post(new FolderImageActivityCallBackPosition(pagerPosition));
				}
				setImageinfo(imageUrls.get(arg0),calcPositionInCurGroup(arg0)+ 1,calcTotalInCurGroup(arg0));
			}
			
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				
				
			}
			
			@Override
			public void onPageScrollStateChanged(int arg0) {
				
			}
		});
		
		iv_copy = (ImageView) findViewById(R.id.iv_copy);
		iv_share = (ImageView) findViewById(R.id.iv_share);
		iv_delete = (ImageView) findViewById(R.id.iv_delete);
		iv_copy.setOnClickListener(this);
		iv_share.setOnClickListener(this);
		iv_delete.setOnClickListener(this);
		
		if (imageUrls != null && imageUrls.size() > 0 && imageUrls.get(0).startsWith("http://")) {
			iv_copy.setImageResource(R.drawable.image_copy_selector);
		}else{
			iv_copy.setImageResource(R.drawable.image_upload_selector);
		}
		
	}
	
	private int calcPositionInCurGroup(int pagerPosition) {
		int count = 0;
		
		for(List<DMFile> l:mDatas) {
			for(int i = 0;i<l.size();i++) {
				if(count < pagerPosition) {
					count++;
				} else {
					return i;
				}
			}
		}
		return 0;
	}
	private int calcTotalInCurGroup(int pagerPosition) {
		int count = 0;
		
		for(List<DMFile> l:mDatas) {
			for(int i = 0;i<l.size();i++) {
				if(count < pagerPosition) {
					count++;
				} else {
					return l.size();
				}
			}
		}
		return 0;
	}
	
	
	private boolean checkParam() {
		boolean ret = false;
		mDatas = FileOperationHelper.mGroupDatas;
		pagerPosition = FileOperationHelper.imgPostionInAll;
		if(mDatas != null && pagerPosition >= 0) {
			int count = 0;
			for(ArrayList<DMFile> l:mDatas) {
				count+=l.size();
			}
			if(pagerPosition<count) {
				ret = true;
			}
		}
		return ret;
	}
	/**
	 * 图片分类的初始化
	 */
	private void fillData() {
		//统计图片数量
		imageUrls.clear();
		for (List<DMFile> l : mDatas) {
			for (DMFile f : l) {
				if (f.mLocation == DMFile.LOCATION_UDISK) {
					imageUrls.add(FileInfoUtils.encodeUri("http://" +  BaseValue.Host + File.separator + f.getPath()));
				}else if (f.mLocation == DMFile.LOCATION_LOCAL){
					imageUrls.add("file://" + f.getPath());
				} else if(f.mLocation == DMFile.LOCATION_DROPBOX) {
					//不改变
					imageUrls.add(f.getPath());
				}
			}
		}
	}
	
	
	private void setImageinfo(String imageUrl,int position ,int total)
	{
		tvImageName.setText(getImageName(imageUrl));
		tvImageCount.setText(position + "/" + total);
	}
	
	private String getImageName(String url)
	{
		String filename = FileInfoUtils.fileName(url);
		return FileInfoUtils.decode(filename);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putInt(STATE_POSITION, pager.getCurrentItem());
	}

	
	
	private GlideUrl getGlideRequestUrl(String path) {
		LazyHeaders headers= new LazyHeaders.Builder().build();
		String fullPath = path;
		if(isFrom == IS_FROM_DropBox) {
			HashMap<String, String> tmp = new HashMap<String, String>();
			DMDropboxAPI.getInstance().getSession().sign(tmp);
			if(tmp.size() > 0) {
				LazyHeaders.Builder mBuilder = new LazyHeaders.Builder();
				for(Map.Entry<String, String> entry: tmp.entrySet()) {
					mBuilder.addHeader(entry.getKey(), entry.getValue());
				}
				headers = mBuilder.build();
			}
			fullPath = getFullPath(path);
		} 
		
		return new GlideUrl(fullPath,headers);
	}
	
	private String getFullPath(String path) {
		String ret = null;
		try {
			ret = DMDropboxAPI.getInstance().getRequestUr(path, null);
		} catch (DropboxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ret;

	}
	
	private class ImagePagerAdapter extends PagerAdapter {

		private List<String> images;
		private LayoutInflater inflater;

		ImagePagerAdapter(List<String> images) {
			this.images = images;
			inflater = getLayoutInflater();
		}
		
		private int mChildCount = 0;
		 
	     @Override
	     public void notifyDataSetChanged() {         
	           mChildCount = getCount();
	           super.notifyDataSetChanged();
	     }
	 
	     @Override
	     public int getItemPosition(Object object)   {          
	           if ( mChildCount > 0) {
	           mChildCount --;
	           return POSITION_NONE;
	           }
	           return super.getItemPosition(object);
	     }

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View) object);
		}

		@Override
		public int getCount() {
			return images.size();
		}

		@Override
		public Object instantiateItem(ViewGroup view, int position) {
			View imageLayout = inflater.inflate(R.layout.image_item_pager_image, view, false);
			assert imageLayout != null;
			String path = "";
			final ZoomView imageView = (ZoomView) imageLayout.findViewById(R.id.image);
			final LinearLayout llytLoadingBg = (LinearLayout) imageLayout.findViewById(R.id.llyt_image_loading);
			final Button reloading = (Button)imageLayout.findViewById(R.id.btn_image_reloading);
//			if (images.get(position) != null && images.get(position).startsWith("http://")) {
////				path = FileInfoUtils.encodeUri(images.get(position));
//			}else {
				path = images.get(position);
//			}
			imageView.setOnSingleTapConfirmedListener(new ZoomView.SingleTapConfirmedListener() {
				
				@Override
				public void onSingleTapConfirmed() {
					// TODO Auto-generated method stub
				 	toggleTitleInfoBar();
				}
			});
			
			reloading.setTag(path);
			reloading.setOnTouchListener(new OnTouchListener() {
				
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					// TODO Auto-generated method stub
//					mGestureDetector.onTouchEvent(event);
					return false;
				}
			});
			
			reloading.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					imageView.setBackgroundColor(Color.TRANSPARENT);
					String path = v.getTag().toString();
					
					if(isFrom == IS_FROM_DropBox) {
						displayImage(path, imageView, llytLoadingBg, reloading);
					} else {
						displayImageWithNoHeaders(path, imageView, llytLoadingBg, reloading);
					}
				}
			});

			llytLoadingBg.setVisibility(View.VISIBLE);
			reloading.setVisibility(View.GONE);
			
			if(isFrom == IS_FROM_DropBox) {
				displayImage(path, imageView, llytLoadingBg, reloading);
			} else {
				displayImageWithNoHeaders(path, imageView, llytLoadingBg, reloading);
				
			}
		
			view.addView(imageLayout, 0);
			return imageLayout;
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view.equals(object);
		}

		@Override
		public void restoreState(Parcelable state, ClassLoader loader) {
		}

		@Override
		public Parcelable saveState() {
			return null;
		}
		
		public void displayImageWithNoHeaders(String path ,final ZoomView imageView,final LinearLayout llytLoadingBg,final Button reloading){
			Glide.with(ImagePagerActivity.this)
			.load(path)
			.fitCenter()
			.thumbnail(0.1f)
			.dontAnimate()
			.listener(new RequestListener<String, GlideDrawable>() {  
				@Override
				public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
					llytLoadingBg.setVisibility(View.GONE);
					reloading.setVisibility(View.VISIBLE);
					return false;
				}
				
				@Override
				public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
					llytLoadingBg.setVisibility(View.GONE);
					reloading.setVisibility(View.GONE);
					if((imageView != null) && (imageView instanceof ZoomView))
					{
						((ZoomView)imageView).setIsZoomEnabled(true);
						((ZoomView)imageView).setCanDoubleTap(true);
						((ZoomView)imageView).setCanSingleTap(true);
						((ZoomView)imageView).setCanScale(true);
					}
					return false;
				}
			})
			.crossFade()
			.into(imageView);
		}
		public void displayImage(String path ,final ZoomView imageView,final LinearLayout llytLoadingBg,final Button reloading){
			Glide.with(ImagePagerActivity.this)
	        .load(getGlideRequestUrl(path))
	        .fitCenter()
	        .thumbnail(0.1f)
	        .dontAnimate()
	        .listener(new RequestListener<GlideUrl, GlideDrawable>() {  
	    	    @Override
	    	    public boolean onException(Exception e, GlideUrl model, Target<GlideDrawable> target, boolean isFirstResource) {
	    	    	llytLoadingBg.setVisibility(View.GONE);
	    			reloading.setVisibility(View.VISIBLE);
	    	        return false;
	    	    }
	    	    
	    	    @Override
	    	    public boolean onResourceReady(GlideDrawable resource, GlideUrl model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
	    	    	llytLoadingBg.setVisibility(View.GONE);
					reloading.setVisibility(View.GONE);
					if((imageView != null) && (imageView instanceof ZoomView))
					{
						((ZoomView)imageView).setIsZoomEnabled(true);
						((ZoomView)imageView).setCanDoubleTap(true);
						((ZoomView)imageView).setCanSingleTap(true);
						((ZoomView)imageView).setCanScale(true);
					}
	    	    	return false;
	    	    }
	    	})
	        .crossFade()
	        .into(imageView);
		}
		
		
		
	}
	
	
	/**
	 * 切换titleinfobar显示状态
	 */
	private void toggleTitleInfoBar()
	{
		if(rlImageInfoBar.getVisibility() == View.VISIBLE)
		{
			AnimBottom = AnimationUtils.loadAnimation(mContext,
					R.anim.buttom_slide_hide_bar_hide);
			AnimTop = AnimationUtils.loadAnimation(mContext,
					R.anim.top_slide_hide_bar_hide);
			AnimBottom.setAnimationListener(new MyAnimationListener());
			
			llyt_image_bottom_bar.startAnimation(AnimBottom);
			rlImageInfoBar.startAnimation(AnimTop);
		}
		else
		{
			rlImageInfoBar.setVisibility(View.VISIBLE);
			llyt_image_bottom_bar.setVisibility(View.VISIBLE);
			AnimBottom = AnimationUtils.loadAnimation(mContext, R.anim.buttom_slide_hide_bar_show); 
			AnimTop = AnimationUtils.loadAnimation(mContext,R.anim.top_slide_hide_bar_show);
			llyt_image_bottom_bar.startAnimation(AnimBottom);
			rlImageInfoBar.startAnimation(AnimTop);
			
		}
	}
	/**
	 * 动画结束隐藏控件
	 * @author Administrator
	 *
	 */
	
	class MyAnimationListener implements AnimationListener {

		@Override
		public void onAnimationStart(Animation arg0) {}

		@Override
		public void onAnimationRepeat(Animation arg0) {}

		@Override
		public void onAnimationEnd(Animation arg0) {
			// TODO Auto-generated method stub
			llyt_image_bottom_bar.setVisibility(View.GONE);
			rlImageInfoBar.setVisibility(View.GONE);
		}
	}
	
	/**
	 * 数组中的格式为编码过的格式
	 * @param
	 * @return
	 */
	public static String[] getUriDecodeStringArray(String[] encodeStrings)
	{
		int index = 0;
		String[] uriString = new String[encodeStrings.length];
		
		for(String path : encodeStrings)
		{
			uriString[index] = FileInfoUtils.decode(path);
			////System.out.println("ima url:"+uriString[index]);
			index ++;
		}
		
		return uriString;
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		init(this);
		DMImageLoader.getInstance().stop();
		activityStartTime = System.currentTimeMillis();
	}
	
	
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		unInit();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		long dur = System.currentTimeMillis() - activityStartTime;
		dur = (dur/1000)/60;
//		DMStatistics.onDuration(this,"ImageReader",String.valueOf(dur)+"min");
	}
	/**
	 * 
	 * @author Administrator
	 * 自定义的一个event类，通知到图片分类那里进行定位
	 */
	public class FileExplorerViewCallBackPosition {
		public int position;

		public FileExplorerViewCallBackPosition(int position) {
			this.position = position;
		}
	}
	/**
	 * 自定义的一个event类，通知到文件列表那里进行定位
	 */
	public class FileManagerDirViewCallBackPosition {
		public int position;

		public FileManagerDirViewCallBackPosition(int position) {
			this.position = position;
		}
	}
	
	public class FolderImageActivityCallBackPosition {
		public int position;
		
		public FolderImageActivityCallBackPosition(int position) {
			this.position = position;
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		int i = v.getId();
		if (i == R.id.ivBack) {
			finish();

		} else if (i == R.id.iv_copy) {
			if (imageUrls.get(0).startsWith("http://")) {
				doFileOperation(FileOperationService.FILE_OP_DOWNLOAD, getSelectedFiles());
			} else {

				if (BaseValue.Host == null || BaseValue.Host == "") {
					Toast.makeText(mContext, R.string.DM_Remind_Notconnect_Device, Toast.LENGTH_SHORT).show();
				} else {
					DMStorageInfo info = DMSdk.getInstance().getStorageInfo();
					if (info != null && info.getStorages() != null && info.getStorages().size() > 0) {
						String mDesPath = info.getStorages().get(0).mPath;
						doFileUploadOperation(FileOperationService.FILE_OP_UPLOAD, getSelectedFiles(), mDesPath);
					} else {
						Toast.makeText(mContext, R.string.DM_Remind_Operate_No_Disk, Toast.LENGTH_SHORT).show();
					}
				}
			}


		} else if (i == R.id.iv_share) {
			List<DMFile> l = getSelectedFiles();
			if (l != null && l.size() > 0) {
				shareFile(l.get(0));
			}

		} else if (i == R.id.iv_delete) {
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
					doFileOperation(FileOperationService.FILE_OP_DELETE, getSelectedFiles());
				}
			});

			builder.show();


		} else {
		}
		
	}
	/**
	 *  返回选中的文件
	 * @return
	 */
	private List<DMFile> getSelectedFiles() {
		int count = 0;
		for(List<DMFile> l:mDatas) {
			for(int i = 0;i<l.size();i++) {
				if(count < pagerPosition) {
					count++;
				} else {
					List<DMFile> list = new ArrayList<DMFile>();
					list.add(l.get(i));
					return list;
				}
			}
		}
		return null;
	}
	
	
	
	private void updateView() {
		removeImage(mDatas,pagerPosition);
		//没有图片了就finish
		if(mDatas.size()<=0) {
			finish();
			return;
		}
		fillData();
		if(pagerPosition >= imageUrls.size()) {
			pagerPosition = imageUrls.size() - 1;
		}
		setImageinfo(imageUrls.get(pagerPosition),calcPositionInCurGroup(pagerPosition)+ 1,calcTotalInCurGroup(pagerPosition));
		adapter.notifyDataSetChanged();
	}
	
	
	private void removeImage(List<ArrayList<DMFile>> list,int positionInAll) {
		int count = 0;
		for(int i = 0;i< list.size();i++) {
			for(int j = 0;j<list.get(i).size();j++) {
				if(count < positionInAll) {
					count++;
				} else {
					if(list.get(i).size()<=1) {
						list.remove(i);
					} else {
						list.get(i).remove(j);
					}
					return;
				}
			}
		}
		return;
	}
	
	public void onEventMainThread(DisconnectEvent event){
		finish();
	}
	
	public void onEventMainThread(PasswordChangeEvent event){
		finish();
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		EventBus.getDefault().unregister(this);
		DMImageLoader.getInstance().resume();
	}


	@Override
	public void onOperationEnd(String opt) {
		// TODO Auto-generated method stub
		if (opt != null && opt.equals(getString(R.string.DM_Task_Delete))) {
			updateView();
		}
	}
	
}