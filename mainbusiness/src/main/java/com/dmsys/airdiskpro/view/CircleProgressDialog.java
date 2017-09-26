package com.dmsys.airdiskpro.view;


import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import com.dmsys.mainbusiness.R;


/**
 * Dialog to configure the SSID and security settings for Access Point operation
 */
public class CircleProgressDialog extends AlertDialog {

	private View mView;
	public static final int HorizontalStyle = 0;
	private ImageView ivCirclePro = null;
	private Context mContext;
	
	public CircleProgressDialog(Context context, int style) {
		super(context, style);
		mContext = context;
	}

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mView = getLayoutInflater().inflate(R.layout.circle_progress_dialog,null);
		ivCirclePro = (ImageView)mView.findViewById(R.id.iv_circlepro);
		Animation operatingAnim = AnimationUtils.loadAnimation(mContext, R.anim.circle_progress_anim);
		LinearInterpolator lin = new LinearInterpolator();  
		operatingAnim.setInterpolator(lin);  
		ivCirclePro.startAnimation(operatingAnim);  
		setContentView(mView);
	}
}
