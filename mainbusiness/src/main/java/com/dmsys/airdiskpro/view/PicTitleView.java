package com.dmsys.airdiskpro.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

public class PicTitleView extends TextView{
	private boolean moving = false;
	public PicTitleView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	public PicTitleView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}
	

	public PicTitleView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		// TODO Auto-generated method stub
		/*if(top == 0)
		{
			Log.d("ra_frssss", "onlayout_ "+left+" "+top+" "+right+" "+bottom+" ");
		}else
			Log.d("ra_frssss", "onlayout_ "+left+" "+top+" "+right+" "+bottom+" ");*/
//		super.onLayout(changed, left, top, right, bottom);
	}
	
}
