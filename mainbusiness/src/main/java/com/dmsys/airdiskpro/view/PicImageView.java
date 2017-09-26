package com.dmsys.airdiskpro.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.dmsys.dmsdk.model.DMFile;

public class PicImageView extends ImageView {
	private DMFile dmFile = null;
	private long positionInAll = -1;
	private int idInLine;
	private int unitGroupId;
	private int unitId;
	private ImageView icon;

	public int getUnitId() {
		return unitId;
	}
	
	public void setUnitId(int unitId) {
		this.unitId = unitId;
	}

	public int getUnitGroupId() {
		return unitGroupId;
	}

	public void setUnitGroupId(int unitGroupId) {
		this.unitGroupId = unitGroupId;
	}

	public PicImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	public PicImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public PicImageView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public DMFile getDMFile() {
		return dmFile;
	}

	public void setDMFile(DMFile xlFile) {
		this.dmFile = xlFile;
	}

	public int getIdInLine() {
		return idInLine;
	}

	public void setIdInLine(int idInLine) {
		this.idInLine = idInLine;
	}

	public ImageView getIcon() {
		return icon;
	}

	public void setIcon(ImageView icon) {
		this.icon = icon;
	}
	

}
