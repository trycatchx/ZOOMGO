package com.dmsys.airdiskpro.ui.imagereader;

import android.view.View;
import android.view.ViewTreeObserver.OnPreDrawListener;

public abstract class MyPreDrawFilter implements OnPreDrawListener{

	private View mView;
	
	public MyPreDrawFilter(View view){
		mView = view;
	}
	
	@Override
	public boolean onPreDraw() {
		mView.getViewTreeObserver().removeOnPreDrawListener(this);
		doBeforeDraw();
		return true;
	}

	public abstract void doBeforeDraw();
}
