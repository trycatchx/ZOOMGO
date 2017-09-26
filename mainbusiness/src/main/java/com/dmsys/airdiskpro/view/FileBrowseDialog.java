package com.dmsys.airdiskpro.view;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.dmsys.mainbusiness.R;

public class FileBrowseDialog extends Dialog implements View.OnClickListener{
private Context mContext;
	
	private LinearLayout layout_pic,layout_music,layout_video,layout_file;
	
	private ImageView iv_file_browse_dialog_picture,iv_file_browse_dialog_video,
	iv_file_browse_dialog_music,iv_file_browse_dialog_doc;
	
	private LinearLayout llyt_file_browse_dialog_cancle;
	private FileBrowseDialogOnClickListener mFileBrowseDialogOnClickListener;
	
	private LinearLayout layout;
	
	public interface FileBrowseDialogOnClickListener {
		public void FirstImageViewOnClick();
		public void SecondImageViewOnClick();
		public void ThirdImageViewOnClick();
		public void FourthImageViewOnClick();
		public void CloseOnClick();
	}
	
	

	public FileBrowseDialogOnClickListener getFileBrowseDialogOnClickListener() {
		return mFileBrowseDialogOnClickListener;
	}

	public void setFileBrowseDialogOnClickListener(
			FileBrowseDialogOnClickListener mFileBrowseDialogOnClickListener) {
		this.mFileBrowseDialogOnClickListener = mFileBrowseDialogOnClickListener;
	}

	protected FileBrowseDialog(Context context, boolean cancelable,
			OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);
		// TODO Auto-generated constructor stub
		mContext = context;
		initViews();
	}

	public FileBrowseDialog(Context context, int themeResId) {
		super(context, R.style.fullscreen_dialog);
		// TODO Auto-generated constructor stub
		mContext = context;
		initViews();
	}

	public FileBrowseDialog(Context context) {
		this(context, R.style.fullscreen_dialog);
		// TODO Auto-generated constructor stub
		mContext = context;
		initViews();
	}

	private void initViews() {
		
		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		RelativeLayout rootView = (RelativeLayout) inflater.inflate(R.layout.file_browse_dialog, null);
		
		layout_pic = (LinearLayout) rootView.findViewById(R.id.layout_pic);
		layout_video = (LinearLayout) rootView.findViewById(R.id.layout_video);
		layout_music = (LinearLayout) rootView.findViewById(R.id.layout_music);
		layout_file = (LinearLayout) rootView.findViewById(R.id.layout_file);
		llyt_file_browse_dialog_cancle = (LinearLayout) rootView.findViewById(R.id.llyt_file_browse_dialog_cancle);
		
		layout = (LinearLayout) rootView.findViewById(R.id.layout);
		
		iv_file_browse_dialog_picture = (ImageView) rootView.findViewById(R.id.iv_file_browse_dialog_picture);
		iv_file_browse_dialog_video = (ImageView) rootView.findViewById(R.id.iv_file_browse_dialog_video);
		iv_file_browse_dialog_music = (ImageView) rootView.findViewById(R.id.iv_file_browse_dialog_music);
		iv_file_browse_dialog_doc = (ImageView) rootView.findViewById(R.id.iv_file_browse_dialog_doc);
		llyt_file_browse_dialog_cancle = (LinearLayout) rootView.findViewById(R.id.llyt_file_browse_dialog_cancle);
		
		iv_file_browse_dialog_picture.setOnClickListener(this);
		iv_file_browse_dialog_video.setOnClickListener(this);
		iv_file_browse_dialog_music.setOnClickListener(this);
		iv_file_browse_dialog_doc.setOnClickListener(this);
		llyt_file_browse_dialog_cancle.setOnClickListener(this);
		
		// 初始化布局的参数
		this.setContentView(rootView);
	}

	private void attachAnimation() {
		// TODO Auto-generated method stub
		
		PropertyValuesHolder valuesHolder = PropertyValuesHolder.ofFloat("scaleX", 0.0f, 1.0f);
		PropertyValuesHolder valuesHolder1 = PropertyValuesHolder.ofFloat("scaleY", 0.0f, 1.0f);
		PropertyValuesHolder valuesHolder2 = PropertyValuesHolder.ofFloat("alpha", 0.0f, 1.0f);
		 
		ObjectAnimator pic = ObjectAnimator.ofPropertyValuesHolder(layout_pic, valuesHolder, valuesHolder1, valuesHolder2);
		pic.setInterpolator(new AccelerateDecelerateInterpolator());
		pic.setDuration(500).start();
		
		ObjectAnimator video = ObjectAnimator.ofPropertyValuesHolder(layout_video, valuesHolder, valuesHolder1, valuesHolder2);
		video.setInterpolator(new AccelerateDecelerateInterpolator());
		video.setDuration(500).start();
		
		ObjectAnimator music = ObjectAnimator.ofPropertyValuesHolder(layout_music, valuesHolder, valuesHolder1, valuesHolder2);
		music.setInterpolator(new AccelerateDecelerateInterpolator());
		music.setDuration(500).start();
		
		ObjectAnimator file = ObjectAnimator.ofPropertyValuesHolder(layout_file, valuesHolder, valuesHolder1, valuesHolder2);
		file.setInterpolator(new AccelerateDecelerateInterpolator());
		file.setDuration(500).start();
	}
	
	@Override
	public void show() {
		// TODO Auto-generated method stub
		super.show();
		attachAnimation();
	}
	
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		dismiss();
		int i = v.getId();
		if (i == R.id.iv_file_browse_dialog_picture) {
			if (mFileBrowseDialogOnClickListener != null) {
				mFileBrowseDialogOnClickListener.FirstImageViewOnClick();
			}

		} else if (i == R.id.iv_file_browse_dialog_video) {
			if (mFileBrowseDialogOnClickListener != null) {
				mFileBrowseDialogOnClickListener.SecondImageViewOnClick();
			}

		} else if (i == R.id.iv_file_browse_dialog_music) {
			if (mFileBrowseDialogOnClickListener != null) {
				mFileBrowseDialogOnClickListener.ThirdImageViewOnClick();
			}

		} else if (i == R.id.iv_file_browse_dialog_doc) {
			if (mFileBrowseDialogOnClickListener != null) {
				mFileBrowseDialogOnClickListener.FourthImageViewOnClick();
			}

		} else if (i == R.id.llyt_file_browse_dialog_cancle) {
			if (mFileBrowseDialogOnClickListener != null) {
				mFileBrowseDialogOnClickListener.CloseOnClick();
			}

		} else {
		}
	}

}
