package com.dmsys.airdiskpro.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

public class PicFolderFrameLayout extends FrameLayout {
	public int row;
	public int columns;

	public int getRow() {
		return row;
	}

	public void setRow(int row) {
		this.row = row;
	}

	public int getColumns() {
		return columns;
	}

	public void setColumns(int columns) {
		this.columns = columns;
	}

	public PicFolderFrameLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	public PicFolderFrameLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public PicFolderFrameLayout(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

}
