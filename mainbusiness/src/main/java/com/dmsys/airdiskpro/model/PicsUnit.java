package com.dmsys.airdiskpro.model;

import com.dmsys.dmsdk.model.DMFile;

import java.util.ArrayList;

public class PicsUnit {
	public static int Head = 0;
	public static int Mid = 1;
	public static int Tail = 2;
	public boolean isAlsoTail = false;
	public ArrayList<DMFile> picGroup;
	public String date = "";
	public int unitId;//在同一组中的每个PicsUnit的unitId不同
	
	public int unitGroupId;//在同一组中的PicsUnit有相同的unitGroupId
	
	/**
	 * 本组是这个日期的图片的
	 * 头
	 * 中间
	 * 尾部
	 */
	public int type = -1;
}
