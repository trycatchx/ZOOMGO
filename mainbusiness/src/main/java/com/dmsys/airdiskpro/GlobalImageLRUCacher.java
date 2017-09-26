package com.dmsys.airdiskpro;

import android.app.ActivityManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Video;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;

import com.dmsys.airdiskpro.utils.BitmapUtil;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * 全局图片缓存
 * 
 * @author YangQiang
 * 
 */
public class GlobalImageLRUCacher {

	private final static String TAG = "GlobalImageLRUCacher";

	public final int TYPE_MINI = 0; // 表示缩略图，各种规格
	public final int TYPE_FULL = 2; // 大图浏览模式时，大图，

	private final int MSG_DECODE_BMP = 0x1000; //解码图片
	private final int MSG_DECODE_APK_ICON = 0x1010; //获取未安装APK的logo
	private final int MSG_DECODE_INSTALLED_APK_ICOM = 0x1011;
	private final int MSG_DECODE_COMPLETE = 0x1004; //一个任务解析完成
	private final int MSG_DECODE_VIDEO_THUMBNAIL = 0x1012; //视频缩略图

	private Context mContext;
	private ContentResolver mResolver;
	private HandlerThread mHandlerThread = new HandlerThread("GlobalImageLRUCacher-Thread");
	private Handler mSubHandler;
	private Handler mUIThreadHandler;
	private List<String> mq = new ArrayList<String>();

	private int mMaxSize; // 最大值动态计算
	private final int M_MIN_SIZE = 1024 * 1024 * 4;// 设置6M的最小硬缓存空间
	private final int M_MAX_SIZE = 1024 * 1024 * 10;
	private final int MIN_ELEMENTS_COUNT = 21;
	private long mMaxMemery;
	private LruCache<String, Bitmap> mHardBitmapCache;
	private int mAndroidApiVersion;
	private boolean mUseSoftCache = false;

	private static GlobalImageLRUCacher mInstance;
	private static final Object mlock = new Object();

	// 软引用池的最大文件个数。
	private static int SOFT_CACHE_CAPACITY = 40;
	private final LinkedHashMap<String, SoftReference<Bitmap>> mSoftBitmapCache = new LinkedHashMap<String, SoftReference<Bitmap>>(SOFT_CACHE_CAPACITY, 0.75f,
			true) {
		private static final long serialVersionUID = 4256086969964818921L;

		protected boolean removeEldestEntry(Entry<String, SoftReference<Bitmap>> eldest) {
			if (size() > SOFT_CACHE_CAPACITY) {
				return true;
			}
			return false;

		};
	};

	private GlobalImageLRUCacher(Context context) {
		mContext = context;
		mResolver = mContext.getContentResolver();
		inti();
	}

	public static GlobalImageLRUCacher getInstance(Context context) {
		if (null == mInstance) {
			synchronized (mlock) {
				if (null == mInstance) {
					mInstance = new GlobalImageLRUCacher(context);
				}
			}
		}
		return mInstance;
	}

	private void inti() {

		Runtime rt = Runtime.getRuntime();
		long freeMemory = rt.freeMemory(); // 当前已分配的堆，剩余的内存 。
											// 堆空间不是一层不变的，但是系统会为其设置最大上限，系统为App按需分配。
		mMaxMemery = rt.maxMemory(); // 可分配的堆空间的最大容量，HTC
										// G1为16M，现在很多手机都已经修改为32M，64M，甚至128M或以上
		ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
		ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
		am.getMemoryInfo(mi);
		if (mMaxMemery > mi.availMem) {
			// 将缓存设置为可用内存的1/3，这时内存已经极度缺乏了.但是系统可能会回收一些其他地方的资源来供我们使用。
			mMaxSize = (int) (mi.availMem / 3);
			// 如果这是最大值小于我们规定的最小值，则放弃这个最小值，采用最小值，以防我们的缓存太小了。
			mMaxSize = mMaxSize < M_MIN_SIZE ? M_MIN_SIZE : mMaxSize;
		} else {
			mMaxSize = (int) (mMaxMemery / 5);// 将缓存设置为最大可分配的堆空间的1/4
		}

		mMaxSize = mMaxSize > M_MAX_SIZE ? M_MAX_SIZE : mMaxSize;

		mAndroidApiVersion = Build.VERSION.SDK_INT;

		mHardBitmapCache = new LruCache<String, Bitmap>(mMaxSize) {
			@Override
			public int sizeOf(String key, Bitmap value) {
				int size = value.getRowBytes() * value.getHeight();
				return size;
			}

			@Override
			protected void entryRemoved(boolean evicted, String key, Bitmap oldValue, Bitmap newValue) {
				if (mAndroidApiVersion >= 9 && mUseSoftCache) {
					// 硬引用缓存区满，将一个最不经常使用的oldvalue推入到软引用缓存区
					mSoftBitmapCache.put(key, new SoftReference<Bitmap>(oldValue));
				}
			}
		};
		mHardBitmapCache.setMinElemetns(MIN_ELEMENTS_COUNT);

		mHandlerThread.start();

		// 主线程中的Handler。
		mUIThreadHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				if (msg.what == MSG_DECODE_COMPLETE) {
					MyData data = (MyData) msg.obj;
					if (null != data && null != data.callback) {
						data.callback.callback(data.bmp, data.flag);
					}
				}
			}
		};
		mHandlerThread.setPriority(2);

		// 接收消息的子线程，用于处理查找缓存和解码图片
		mSubHandler = new Handler(mHandlerThread.getLooper()) {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				if (msg.what == MSG_DECODE_BMP) {

					MyData data = (MyData) msg.obj;
					Bitmap bmp = null;
					if (null != (bmp = create(data.path, data.type, data.width, data.height))) { // decode
																									// bitmap，并且成功
						mHardBitmapCache.put(data.key, bmp); // 缓存
					}
					data.bmp = bmp;
					mq.remove(data.key);
					mUIThreadHandler.obtainMessage(MSG_DECODE_COMPLETE, data).sendToTarget();
				} else if (msg.what == MSG_DECODE_APK_ICON) {
//					MyData data = (MyData) msg.obj;
//					if(null == data){
//						return;
//					}
//					Bitmap bmp = null;
//					ApkInfo apkInfo = AppHelper.getApkInfo(mContext, data.path);
//					if(null != apkInfo) {
//						Drawable drawable = apkInfo.getApkIcon();
//						if(drawable != null) {
//							bmp = BitmapUtil.drawToBitmap(drawable, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
//							data.bmp = bmp;
//							mHardBitmapCache.put(data.key, bmp); // 缓存
//						}
//					}
//					
//					mq.remove(data.key);
//					mUIThreadHandler.obtainMessage(MSG_DECODE_COMPLETE, data).sendToTarget();
				} else if (msg.what == MSG_DECODE_VIDEO_THUMBNAIL) {
					MyData data = (MyData) msg.obj;
					if(null == data || TextUtils.isEmpty(data.path)){
						return;
					}
					Bitmap bmp = ThumbnailUtils.createVideoThumbnail(data.path, Video.Thumbnails.MICRO_KIND);
					Log.d("ra_videiT", "path = "+data.path+"  bitmap = "+bmp);
					if(null != bmp) {
						data.bmp = bmp;
						mHardBitmapCache.put(data.key, bmp); // 缓存
					}
					mq.remove(data.key);
					mUIThreadHandler.obtainMessage(MSG_DECODE_COMPLETE, data).sendToTarget();
				} else if (msg.what == MSG_DECODE_INSTALLED_APK_ICOM) {
					MyData data = (MyData) msg.obj;
					Drawable icon = data.packageInfo.applicationInfo.loadIcon(mContext.getPackageManager());
					if(null != icon) {
						Bitmap bmp = BitmapUtil.drawToBitmap(icon, icon.getIntrinsicWidth(), icon.getIntrinsicHeight());
						if(null != bmp) {
							data.bmp = bmp;
							mHardBitmapCache.put(data.key, bmp); // 缓存
						}
					}
					mq.remove(data.key);
					mUIThreadHandler.obtainMessage(MSG_DECODE_COMPLETE, data).sendToTarget();
				}
				 
			}
		};
	}


	public Bitmap getBitmap(String path, Object attachment, DecodeBitmapCallBack callback) {
		if (TextUtils.isEmpty(path)) {
			return null;
		}

		MyData data = new MyData();
		data.flag = attachment;
		data.path = path;
		data.callback = callback;
		data.type = TYPE_FULL;
		data.key = path + "-FXF";
		Bitmap bmp = null;

		synchronized (mHardBitmapCache) {
			final Bitmap bitmap = mHardBitmapCache.get(data.key);
			if (bitmap != null)
				return bitmap;
		}

		// 硬引用缓存区间中读取失败，从软引用缓存区间读取
		synchronized (mSoftBitmapCache) {
			SoftReference<Bitmap> bitmapReference = mSoftBitmapCache.get(data.key);
			if (bitmapReference != null) {
				final Bitmap bitmap2 = bitmapReference.get();
				if (bitmap2 != null) {
					return bitmap2;
				}
			}
			if (mq.contains(data.key)) {
				return null;
			}
			mq.add(data.key);
			mSoftBitmapCache.remove(data.key);
			Message msg = mSubHandler.obtainMessage(MSG_DECODE_BMP, 0, 0, data);
			mSubHandler.sendMessageDelayed(msg,getDelayTime());
		}

		return bmp;
	}
	
	public Bitmap getOnlyBitmap(String path,int width , int height) {
		if (TextUtils.isEmpty(path)) {
			return null;
		}		
		String key = path + "-" + width + "X" + height;
		Bitmap bmp = null;

		synchronized (mHardBitmapCache) {
			final Bitmap bitmap = mHardBitmapCache.get(key);
			if (bitmap != null)
				return bitmap;
		}

		// 硬引用缓存区间中读取失败，从软引用缓存区间读取
		synchronized (mSoftBitmapCache) {
			SoftReference<Bitmap> bitmapReference = mSoftBitmapCache.get(key);
			if (bitmapReference != null) {
				final Bitmap bitmap2 = bitmapReference.get();
				if (bitmap2 != null) {
					return bitmap2;
				}
			}			
		}
		return bmp;
	}

	public Bitmap getApkIcon(String path, Object attachment, DecodeBitmapCallBack callback) {
		if (TextUtils.isEmpty(path)) {
			return null;
		}

		MyData data = new MyData();
		data.flag = attachment;
		data.path = path;
		data.callback = callback;
		data.type = TYPE_FULL;
		data.key = path + "-APK_ICON";
		Bitmap bmp = null;

		synchronized (mHardBitmapCache) {
			final Bitmap bitmap = mHardBitmapCache.get(data.key);
			if (bitmap != null)
				return bitmap;
		}

		// 硬引用缓存区间中读取失败，从软引用缓存区间读取
		synchronized (mSoftBitmapCache) {
			SoftReference<Bitmap> bitmapReference = mSoftBitmapCache.get(data.key);
			if (bitmapReference != null) {
				final Bitmap bitmap2 = bitmapReference.get();
				if (bitmap2 != null) {
					return bitmap2;
				}
			}
			if (mq.contains(data.key)) {
				return null;
			}
			mq.add(data.key);
			mSoftBitmapCache.remove(data.key);
			mSubHandler.obtainMessage(MSG_DECODE_APK_ICON, 0, 0, data).sendToTarget();
		}

		return bmp;
	}
	
	public Bitmap getInstalledSoftwareThumbnail(PackageInfo pinfo, Object attachment, DecodeBitmapCallBack callback) {

		MyData data = new MyData();
		data.flag = attachment;
		data.packageInfo = pinfo;
		data.callback = callback;
		data.type = TYPE_FULL;
		data.key = pinfo.packageName + "-INSTALLED_APK_ICON";
		Bitmap bmp = null;

		synchronized (mHardBitmapCache) {
			final Bitmap bitmap = mHardBitmapCache.get(data.key);
			if (bitmap != null)
				return bitmap;
		}

		// 硬引用缓存区间中读取失败，从软引用缓存区间读取
		synchronized (mSoftBitmapCache) {
			SoftReference<Bitmap> bitmapReference = mSoftBitmapCache.get(data.key);
			if (bitmapReference != null) {
				final Bitmap bitmap2 = bitmapReference.get();
				if (bitmap2 != null) {
					return bitmap2;
				}
			}
			if (mq.contains(data.key)) {
				return null;
			}
			mq.add(data.key);
			mSoftBitmapCache.remove(data.key);
			mSubHandler.obtainMessage(MSG_DECODE_INSTALLED_APK_ICOM, 0, 0, data).sendToTarget();
		}

		return bmp;
	}
	
	public Bitmap getVideoThumbnail(String path, Object attachment, DecodeBitmapCallBack callback) {
		if (TextUtils.isEmpty(path)) {
			return null;
		}

		MyData data = new MyData();
		data.flag = attachment;
		data.path = path;
		data.callback = callback;
		data.type = TYPE_FULL;
		data.key = path + "-video-thumbnail";
		Bitmap bmp = null;

		synchronized (mHardBitmapCache) {
			final Bitmap bitmap = mHardBitmapCache.get(data.key);
			if (bitmap != null)
			{
				return bitmap;
			}
		}

		// 硬引用缓存区间中读取失败，从软引用缓存区间读取
		synchronized (mSoftBitmapCache) {
			SoftReference<Bitmap> bitmapReference = mSoftBitmapCache.get(data.key);
			if (bitmapReference != null) {
				final Bitmap bitmap2 = bitmapReference.get();
				if (bitmap2 != null) {
					return bitmap2;
				}
			}
			if (mq.contains(data.key)) {
				return null;
			}
			mq.add(data.key);
			mSoftBitmapCache.remove(data.key);
			mSubHandler.obtainMessage(MSG_DECODE_VIDEO_THUMBNAIL, 0, 0, data).sendToTarget();
		}
		return bmp;
	}
	
	public boolean runningOut(int comparison){
		int adjustMax = mHardBitmapCache.maxSize()-comparison;
		return mHardBitmapCache.size() >= adjustMax;
	}

	/**
	 * 从缓存中直接获取Bitmap 该接口用于对接@朱娟同学首页的缓存接口
	 * 
	 * @param key
	 *            路径拼接图片尺寸,因为同一张图片在不同的地方使用，尺寸不一样,这样就能够标示不同分辨率的图
	 * @return 返回对应的Bitmap，如果在缓存中没有找到，则返回null。
	 * @deprecated
	 */
	public Bitmap getBitmap(String key) {
		synchronized (mHardBitmapCache) {
			final Bitmap bitmap = mHardBitmapCache.get(key);
			if (bitmap != null)
				return bitmap;
		}

		// 硬引用缓存区间中读取失败，从软引用缓存区间读取
		synchronized (mSoftBitmapCache) {
			SoftReference<Bitmap> bitmapReference = mSoftBitmapCache.get(key);
			if (bitmapReference != null) {
				final Bitmap bitmap2 = bitmapReference.get();
				if (bitmap2 != null) {
					return bitmap2;
				}
			}
		}
		return null;
	}

	/**
	 * @deprecated
	 * @param key
	 * @param value
	 */
	public void putBitmap(String key, Bitmap value) {
		if (key != null && value != null) {
			if (getBitmap(key) == null) {
				mHardBitmapCache.put(key, value); // 缓存
			}
		}
	}

	/**
	 * 从缓冲池中获取Bitmap，如果缓存池中存在，则立即回调；如果缓存池中不存在，则先解码，再回调。
	 * 注意：回调操作在主线程中的，客户端无需使用Handler来处理消息。
	 * 
	 * @param path
	 *            图片的完整路径，包含名字
	 * @param attachment
	 *            回调附带参数，可以为null
	 * @param callback
	 *            回调对象,不能为null,否则客户端无法接收解码后的bitmap
	 */
	public Bitmap getBitmap(String path, int width, int height, Object attachment, DecodeBitmapCallBack callback) {

		if (TextUtils.isEmpty(path)) {
			return null;
		}
		
		if(width <= 0){
			width = 96;
		}
		if(height <= 0){
			height = 96;
		}

		MyData data = new MyData(); 
		data.flag = attachment;
		data.path = path;
		data.width = width;
		data.height = height;
		data.callback = callback;
		data.type = TYPE_MINI;
		data.key = path + "-" + width + "X" + height;

		synchronized (mHardBitmapCache) {
			final Bitmap bitmap = mHardBitmapCache.get(data.key);
			if (bitmap != null)
				return bitmap;
		}

		// 硬引用缓存区间中读取失败，从软引用缓存区间读取
		synchronized (mSoftBitmapCache) {
			SoftReference<Bitmap> bitmapReference = mSoftBitmapCache.get(data.key);
			if (bitmapReference != null) {
				final Bitmap bitmap2 = bitmapReference.get();
				if (bitmap2 != null) {
					return bitmap2;
				}
			}
			if (mq.contains(data.key)) {
				return null;
			}
			mq.add(data.key);
			mSoftBitmapCache.remove(data.key);
			mSubHandler.obtainMessage(MSG_DECODE_BMP, 0, 0, data).sendToTarget();
		}
		return null;
	}

	private Bitmap create(String key, int type, int width, int height) {
		Bitmap bmp = null;

		switch (type) {
		case TYPE_MINI:
			bmp = decodeThumbnailBitmap(key, width, height);
			break;
		case TYPE_FULL:
			bmp = decodeFullSizeImg(key);
			break;
		}
		return bmp;
	}

	// private Bitmap decodeMiniBitmap(String path, int width, int height) {
	// int fileType = UtilWalkBox.getFileType(path);
	// Bitmap bmp = null;
	// switch (fileType) {
	// case UtilWalkBox.FI_IMAGE:
	// bmp = decodeThumbnailBitmap(path, width, height, mResolver);
	// break;
	// case UtilWalkBox.FI_VIDEO:
	// bmp = ThumbnailHelper.getVideoBitmap(path, width, height, mResolver);
	// break;
	// default:
	// bmp = ThumbnailHelper.getImageThumbnailBitmap(path, width, height,
	// mResolver);
	// }
	// return bmp;
	// }

	/**
	 * 从媒体库中查询缩略图，如果没有查到，则去SD卡上解码
	 * 
	 * @param path
	 * @param width
	 * @param height
	 * @return
	 */
	private Bitmap decodeThumbnailBitmap(String path, int width, int height) {
		long id = -1;
		id = getImageIndex(path);
		Bitmap bmp = null;
		if (id != -1) {
			bmp = Images.Thumbnails.getThumbnail(mResolver, id, Images.Thumbnails.MINI_KIND, null);
		}
		
		if(null == bmp) {
			bmp = decodeThumbnailBitmapFromSDCard(path, width, height);
		}
		
		if(null != bmp) {
			Bitmap temp = BitmapUtil.createBitmapCenterCorp(bmp, width, height);
			bmp.recycle();
			return temp;
		}
		
		return bmp;
	}

	/**
	 * 从SD卡解码图片
	 * 
	 * @param imagePath
	 * @param width
	 * @param height
	 * @return
	 */
	private Bitmap decodeThumbnailBitmapFromSDCard(String imagePath, int width, int height) {
		Bitmap bitmap = null;
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		// 获取这个图片的宽和高，注意此处的bitmap为null
		BitmapFactory.decodeFile(imagePath, options);

		// 计算缩放比
		int h = options.outHeight;
		int w = options.outWidth;

		int be = 1;
		if (w < width && h < height) {
			be = 1;
		} else {
//			int xScale = (w % width == 0) ? w / width : (w / width) + 1;
//			int yScale = (h % height == 0) ? h / height : (h / height) + 1;
			int xScale = w / width;
			int yScale = h / height;
			be = Math.min(xScale, yScale);
			be = be < 1 ? 1 : be;
		}

		options.inSampleSize = be;
		options.inJustDecodeBounds = false; // 设为 false
		options.inPreferredConfig = Bitmap.Config.RGB_565;
		options.inPurgeable = true;
		options.inInputShareable = true;
		// 重新读入图片，读取缩放后的bitmap，注意这次要把options.inJustDecodeBounds 设为 false

		// 估计一下新采样的图的长宽，然后计算这个需要占用的内存
		int length = width * height * 2; // RGB565每个像素2字节

		// 清理缓存
		trimCache(length);

		bitmap = BitmapFactory.decodeFile(imagePath, options);
		return bitmap;
	}

	private Bitmap decodeFullSizeImg(String path) {
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inSampleSize = 1;
		opts.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, opts);
		int imageWidth = opts.outWidth;
		int imageHeith = opts.outHeight;
		int base = 2;

		DisplayMetrics dm = mContext.getResources().getDisplayMetrics();
		int mScreenWidth = dm.widthPixels;
		int mScreenHeight = dm.heightPixels;

		if (mScreenHeight < mScreenWidth) {
			int t = mScreenHeight;
			mScreenHeight = mScreenWidth;
			mScreenWidth = t;
		}
		if (mScreenWidth > 480) {
			mScreenWidth = 480;
		}
		if (mScreenHeight > 800) {
			mScreenHeight = 480;
		}

		int wx = mScreenWidth * base;
		int hx = mScreenHeight * base;

		if (imageWidth < wx && imageHeith < hx) {
			opts.inSampleSize = 1;
		} else {
			int xScale = (imageWidth % wx == 0) ? imageWidth / wx : (imageWidth / wx) + 1;
			int yScale = (imageHeith % hx == 0) ? imageHeith / hx : (imageHeith / hx) + 1;
			opts.inSampleSize = Math.max(xScale, yScale);
		}

		opts.inJustDecodeBounds = false;
		opts.inPreferredConfig = Bitmap.Config.RGB_565;
		opts.inPurgeable = true;
		opts.inInputShareable = true;

		// 估计一下新采样的图的长宽，然后计算这个需要占用的内存
		int newWidth = imageWidth / opts.inSampleSize;
		int newHeight = imageHeith / opts.inSampleSize;
		int length = newWidth * newHeight * 2; // RGB565每个像素2字节
		trimCache(length);
		Bitmap bitmap = BitmapFactory.decodeFile(path, opts);
		return bitmap;
	}
	
	/**
	 * 释放缓存，
	 * 
	 * @param trim
	 *            要释放的空间大小。
	 */
	public void trimCache(int trim) {

		if (trim > mHardBitmapCache.maxSize()) {
			trim = mHardBitmapCache.maxSize();
		}

		synchronized (mHardBitmapCache) {
			// 如果剩余的缓存 小于 decode这个图所需的内存，则进行一次回收
			int release = mHardBitmapCache.maxSize() - mHardBitmapCache.size();
			if (trim > release) { //
				int trimToSize = mHardBitmapCache.maxSize() - trim; // 回收最多只剩余这么多。
				mHardBitmapCache.trimToSize(trimToSize);
			}
		}
	}

	/**
	 * 从系统中的媒体库中查询这个图片的缩略图的id
	 * 

	 */
	private long getImageIndex(String path) {
		String[] columns = new String[] { Images.Media._ID, Images.Media.DATA };

		long id = -1;
		Cursor cursor = null;
		try {
		    cursor = mResolver.query(Images.Media.EXTERNAL_CONTENT_URI, columns, Images.Media.DATA + "=?", new String[] { path }, null);
			if (cursor != null && cursor.moveToFirst()) {
				id = cursor.getLong(cursor.getColumnIndex(columns[0]));
			}
		} catch (Exception e) {
			e.printStackTrace();
			return id;
		} finally {
			if (cursor != null) {
				cursor.close();
				cursor = null;
			}
		}
		return id;
	}
	

	/**
	 * 停止缓存相关的所有资源
	 */
	public void release() {
		if (null != mUIThreadHandler) {
			mUIThreadHandler.removeMessages(MSG_DECODE_COMPLETE);
		}
		if (null != mSubHandler) {
			mSubHandler.removeMessages(MSG_DECODE_BMP);
		}
		if (null != mHandlerThread) {
			mHandlerThread.quit();
		}
		if (null != mSoftBitmapCache) {
			mSoftBitmapCache.clear();
		}
		if (null != mHardBitmapCache) {
			mHardBitmapCache.evictAll();
		}
	}

	/**
	 * 清空缓存
	 */
	public void clear() {
		mHardBitmapCache.trimToSize(-1);
	}

	public interface DecodeBitmapCallBack {
		public void callback(Bitmap bmp, Object flag);
	}

	class MyData {
		String path;
		String key;
		int type;
		int width;
		int height;
		Object flag;
		Bitmap bmp;
		PackageInfo packageInfo;
		DecodeBitmapCallBack callback;
	}
	
	// ----------------
		private static long lastCreateTaskTime = 0;
		private static long curCreateTaskTime = 0;
		private static long delayTime;

		/**
		 * @return 获取delay时间,凡是两次长假任务间隔小雨阈值，则延时一段时间再去创建
		 */
		public static long getDelayTime() {
			lastCreateTaskTime = curCreateTaskTime;
			curCreateTaskTime = System.currentTimeMillis();

			long temp = curCreateTaskTime - lastCreateTaskTime;
			if (temp < 1000) {
				delayTime += 30;
			} else {
				delayTime = 0;
			}
			return delayTime;
		}
}
