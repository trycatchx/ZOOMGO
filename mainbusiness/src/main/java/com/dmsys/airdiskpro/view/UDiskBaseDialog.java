package com.dmsys.airdiskpro.view;

import android.app.Dialog;
import android.content.Context;
import android.os.PowerManager.WakeLock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dmsys.airdiskpro.utils.DipPixelUtil;
import com.dmsys.mainbusiness.R;


public class UDiskBaseDialog extends DMBaseDialog {
	private Context mCtx;
	private View mCustomView;
	private WakeLock mWakeLock;
	
	private OnClickListener mLeftBtnListener;
	private OnClickListener mRightBtnListener;
	private OnClickListener mRightOfRightBtnListener;
	
	public static final int TYPE_NO_BTN = 0;
	public static final int TYPE_ONE_BTN = 1;
	public static final int TYPE_TWO_BTN = 2;
	public static final int TYPE_THREE_BTN = 3;
	private boolean clickButtonDismiss = true;
	public UDiskBaseDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);
		mCtx = context;
	}

	public UDiskBaseDialog(Context context, int theme) {
		super(context, theme);
		mCtx = context;
	}

	public UDiskBaseDialog(Context context) {
		super(context, R.style.Password_Dialog);// 透明背景对话框
		mCtx = context;
	}	
	
	public void setLeftBtn(CharSequence text, OnClickListener listener) {
		mLeftBtnListener = listener;
		
		TextView btnLeft = (TextView) findViewById(R.id.dialog_btn_left);
		if (btnLeft != null) {
			btnLeft.setText(text);
		}
	}
	
	public void setRightBtn(CharSequence text, OnClickListener listener) {
		mRightBtnListener = listener;
		
		TextView btnRight = (TextView) findViewById(R.id.dialog_btn_right);
		if (btnRight != null) {
			btnRight.setText(text);
		}
	}
	
	public void setRightOfRightBtn(CharSequence text, OnClickListener listener) {
		mRightOfRightBtnListener = listener;
		
		TextView btnRightOfRight = (TextView) findViewById(R.id.dialog_btn_right_of_right);
		if (btnRightOfRight != null) {
			btnRightOfRight.setText(text);
		}
	}
	
	
	public void setTitleContent(String content)
	{
		TextView dialogTitle = (TextView) findViewById(R.id.dialog_title);
		if (dialogTitle != null) {
			dialogTitle.setText(content);
		}
	}
	
	public LinearLayout getTitleLinearLayout() {
		return (LinearLayout) findViewById(R.id.ll_dialog_title);
	}
	
	public View getCustomView() {
		return mCustomView;
	}
	
	public void initView(int type, int customViewId) {
		clickButtonDismiss = true;
		LayoutInflater inflater = (LayoutInflater)mCtx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		LinearLayout rootView = null;
		View.OnClickListener listener = new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				int i = v.getId();
				if (i == R.id.dialog_btn_left) {
					if (mLeftBtnListener != null) {
						mLeftBtnListener.onClick(UDiskBaseDialog.this, Dialog.BUTTON_POSITIVE);
					}
					if (clickButtonDismiss)
						dismiss();

				} else if (i == R.id.dialog_btn_right) {
					if (mRightBtnListener != null) {
						mRightBtnListener.onClick(UDiskBaseDialog.this, Dialog.BUTTON_NEGATIVE);
					}
					if (clickButtonDismiss)
						dismiss();

				} else if (i == R.id.dialog_btn_right_of_right) {
					if (mRightOfRightBtnListener != null) {
						mRightOfRightBtnListener.onClick(UDiskBaseDialog.this, Dialog.BUTTON_NEUTRAL);
					}
					if (clickButtonDismiss)
						dismiss();

				} else {
				}
			}
		};
		if (type == TYPE_ONE_BTN) {
			rootView = (LinearLayout)inflater.inflate(R.layout.udisk_dialog_one_btn, null);
			
			TextView btnLeft = (TextView) rootView.findViewById(R.id.dialog_btn_left);
			btnLeft.setOnClickListener(listener);
			
		} else if (type == TYPE_TWO_BTN) {
			rootView = (LinearLayout)inflater.inflate(R.layout.udisk_dialog_two_btns, null);
			
			TextView btnLeft = (TextView) rootView.findViewById(R.id.dialog_btn_left);
			btnLeft.setOnClickListener(listener);
			
			TextView btnRight = (TextView) rootView.findViewById(R.id.dialog_btn_right);
			btnRight.setOnClickListener(listener);
		} else if (type == TYPE_THREE_BTN) {
			rootView = (LinearLayout)inflater.inflate(R.layout.udisk_dialog_three_btns, null);
			
			TextView btnLeft = (TextView) rootView.findViewById(R.id.dialog_btn_left);
			btnLeft.setOnClickListener(listener);
			
			TextView btnRight = (TextView) rootView.findViewById(R.id.dialog_btn_right);
			btnRight.setOnClickListener(listener);
			
			TextView dialog_btn_right_of_right = (TextView) rootView.findViewById(R.id.dialog_btn_right_of_right);
			dialog_btn_right_of_right.setOnClickListener(listener);
		} else if (type == TYPE_NO_BTN) {
			rootView = (LinearLayout)inflater.inflate(R.layout.udisk_dialog_no_btn, null);
		}
		

		RelativeLayout dlgContent = (RelativeLayout)rootView.findViewById(R.id.dialog_content);
		View customView = inflater.inflate(customViewId, dlgContent,false);
		

		
		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, 
				RelativeLayout.LayoutParams.WRAP_CONTENT);
		//layoutParams.leftMargin = 25;
		//layoutParams.rightMargin = 25;
		customView.setLayoutParams(layoutParams);
		
		mCustomView = customView;
		
		dlgContent.addView(customView);
		this.setContentView(rootView);

		Window dialogWindow = getWindow();
		dialogWindow.setLayout(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		dialogWindow.getDecorView().setPadding(60, 0, 60, 0);
	}
	
	public void setWidthDip(int w)
	{
		int width = DipPixelUtil.dip2px(mCtx, w); // 宽度
		Window dialogWindow = getWindow();
		dialogWindow.setLayout(width, LayoutParams.WRAP_CONTENT);
	}
	
	public void setClickButtonDismiss(boolean dismiss)
	{
		clickButtonDismiss = dismiss;
	}
	
	public void setNumber(String number) {
		TextView textView = (TextView)findViewById(R.id.dialog_title_number);
		if(textView != null) {
			textView.setText(number);
		}
		
	}
	
}
