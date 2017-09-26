package com.dmsys.airdiskpro.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class PicLineLayout extends LinearLayout {

	private boolean isTail = false;
	private String date;
	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public boolean isTail() {
		return isTail;
	}

	public void setTail(boolean isTail) {
		this.isTail = isTail;
	}

	public boolean isHead() {
		return isHead;
	}

	public void setHead(boolean isHead) {
		this.isHead = isHead;
	}

	private boolean isHead = false;

	public PicLineLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	public PicLineLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public PicLineLayout(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

}
