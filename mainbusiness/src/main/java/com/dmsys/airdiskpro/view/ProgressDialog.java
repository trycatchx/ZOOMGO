package com.dmsys.airdiskpro.view;

import android.content.Context;
import android.content.DialogInterface;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dmsys.mainbusiness.R;


public class ProgressDialog extends UDiskBaseDialog {

	private WakeLock mWakeLock;

	public ProgressDialog(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		initView(TYPE_ONE_BTN, R.layout.dialog_progress);
		this.setCancelable(false);
		bindListener(context);
	}

	public ProgressDialog(Context context, int style) {
		super(context);
		// TODO Auto-generated constructor stub
		if (style == TYPE_NO_BTN) {
			initView(style, R.layout.dialog_progress_no_btn);
		} else {
			initView(style, R.layout.dialog_progress);
		}

		this.setCancelable(false);
		bindListener(context);
	}

	public ProgressDialog(Context context, int style, boolean name,
			boolean time, boolean numberVisiable, boolean speedVisiable,
			boolean progress, String sum, String titler, String leftBtn,
			String number, String speed, String leftTime,String fileName,
			DialogInterface.OnClickListener l) {
		super(context);
		// TODO Auto-generated constructor stub
		if (style == TYPE_NO_BTN) {
			initView(style, R.layout.dialog_progress_no_btn);
		} else {
			initView(style, R.layout.dialog_progress);
		}

		this.setCancelable(false);
		bindListener(context);

		View customView = this.getCustomView();

		((LinearLayout) customView.findViewById(R.id.llyt_file_name))
				.setVisibility(name ? View.VISIBLE : View.GONE);
		((TextView) customView.findViewById(R.id.tv_pg_dg_name))
		.setText(fileName);

		((LinearLayout) customView.findViewById(R.id.llyt_left_time))
				.setVisibility(time ? View.VISIBLE : View.GONE);
		((TextView) customView.findViewById(R.id.tv_pg_dg_time_left))
		.setText(leftTime);;

		((LinearLayout) customView.findViewById(R.id.llyt_left_number))
				.setVisibility(numberVisiable ? View.VISIBLE : View.GONE);
		((TextView) customView.findViewById(R.id.tv_pg_dg_number_left))
		.setText(number);
		
		
		((LinearLayout) customView.findViewById(R.id.llyt_speed))
				.setVisibility(speedVisiable ? View.VISIBLE : View.GONE);
		((TextView) customView.findViewById(R.id.tv_pg_dg_speed))
		.setText(speed);

		((RelativeLayout) customView.findViewById(R.id.rlyt_dg_pg))
				.setVisibility(progress ? View.VISIBLE : View.GONE);

		setNumber(sum);

		setTitleContent(titler);
		setLeftBtn(leftBtn, l);
	}

	private void bindListener(final Context context) {
		// TODO Auto-generated method stub
		setOnShowListener(new OnShowListener() {

			@Override
			public void onShow(DialogInterface dialog) {
				// TODO Auto-generated method stub
				mWakeLock = ((PowerManager) context
						.getSystemService(Context.POWER_SERVICE)).newWakeLock(
						PowerManager.SCREEN_BRIGHT_WAKE_LOCK
								| PowerManager.ON_AFTER_RELEASE, "wakelock");
				mWakeLock.acquire();
			}
		});

		setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss(DialogInterface dialog) {
				// TODO Auto-generated method stub
				if (mWakeLock != null) {
					mWakeLock.release();
					mWakeLock = null;
				}
			}
		});
	}

	public void setProgress(int progress) {
		View customView = this.getCustomView();
		DMProgress progressBar = (DMProgress) customView
				.findViewById(R.id.dialog_progress);
		progressBar.setProgress(progress);

		TextView progressNumView = (TextView) findViewById(R.id.dialog_progress_num);
		progressNumView.setText(progress + "%");
	}

	public void setProgress(double progress) {
		progress = progress > 100? 100:progress;
		progress = progress < 0? 0:progress;
		
		View customView = this.getCustomView();
		DMProgress progressBar = (DMProgress) customView
				.findViewById(R.id.dialog_progress);
		progressBar.setProgress((int) progress);

		TextView progressNumView = (TextView) findViewById(R.id.dialog_progress_num);
		String value = new java.text.DecimalFormat("#.0").format(progress);
		if (value.startsWith(".")) {
			value = "0" + value;
		}
		progressNumView.setText(value + "%");
	}

	public void setMessage(CharSequence text) {
		View customView = this.getCustomView();
		TextView textView = (TextView) customView
				.findViewById(R.id.tv_pg_dg_name);
		textView.setText(text);
	}

	public void setTimeLeft(CharSequence text) {
		View customView = this.getCustomView();
		TextView textView = (TextView) customView
				.findViewById(R.id.tv_pg_dg_time_left);
		textView.setText(text);
	}

	public void setNumberLeft(CharSequence text) {
		View customView = this.getCustomView();
		TextView textView = (TextView) customView
				.findViewById(R.id.tv_pg_dg_number_left);
		textView.setText(text);
	}

	public void setSpeed(CharSequence text) {
		View customView = this.getCustomView();
		TextView textView = (TextView) customView
				.findViewById(R.id.tv_pg_dg_speed);
		textView.setText(text);
	}

	public String getMessage() {
		View customView = this.getCustomView();
		TextView textView = (TextView) customView
				.findViewById(R.id.tv_pg_dg_name);
		return textView.getText().toString();
	}

	public static class Builder {
		Context context;
		boolean nameVisiable = true;
		boolean timeVisiable = false;
		boolean numberVisiable = false;
		boolean speedVisiable = false;
		boolean progressVisiable = true;
		int style = TYPE_ONE_BTN;
		String sum;
		String number;
		String speed;
		String leftTime;
		String titler;
		String fileName;
		String leftBtnString;
		DialogInterface.OnClickListener l;

		public Builder(Context context) {
			this.context = context;
		}

		public Builder setStyle(int v) {
			style = v;
			return this;
		}

		public Builder setNameVisiable(boolean v) {
			nameVisiable = v;
			return this;
		}

		public Builder setTimeVisiable(boolean v) {
			timeVisiable = v;
			return this;
		}

		public Builder setNumberVisiable(boolean v) {
			numberVisiable = v;
			return this;
		}

		public Builder setSpeedVisiable(boolean v) {
			speedVisiable = v;
			return this;
		}

		public Builder setProgressVisiable(boolean v) {
			progressVisiable = v;
			return this;
		}

		public Builder setLeftNumber(String v) {
			number = v;
			return this;
		}

		public Builder setSpeed(String v) {
			speed = v;
			return this;
		}

		public Builder setLeftTime(String v) {
			leftTime = v;
			return this;
		}

		public Builder setSum(String v) {
			sum = v;
			return this;
		}
		public Builder setFileName(String v) {
			fileName = v;
			return this;
		}

		public Builder setTitler(String v) {
			titler = v;
			return this;
		}

		public Builder setLeftBtn(String v, DialogInterface.OnClickListener v1) {
			leftBtnString = v;
			l = v1;
			return this;
		}

		public ProgressDialog build() {
			return new ProgressDialog(context, style, nameVisiable,
					timeVisiable, numberVisiable, speedVisiable,
					progressVisiable, sum, titler, leftBtnString, number,
					speed, leftTime,fileName, l);
		}

	}

}
