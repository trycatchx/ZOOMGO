package com.dmsys.dropbox.model;

import com.dmsys.dmsdk.model.DMFile;
import com.dmsys.dmsdk.util.DMFileTypeUtil;
import com.dropbox.client2.RESTUtility;

import java.util.Map;

/**
 * 这里只是一个Holder类，里氏替换原则
 * @author Administrator
 *
 */

public class DMDropBoxFileHodler extends DMFile{
	
	public DMDropBoxFileHodler(Map<String, Object> map) {
		
		this.mPath = (String) map.get("path");
		this.mName = mPath.substring(mPath.lastIndexOf('/') + 1, mPath.length());
		this.isDir = getFromMapAsBoolean(map, "is_dir");
		this.mSize =  getFromMapAsLong(map, "bytes");
		this.mLastModify = RESTUtility.parseDate(
				 (String) map.get("modified")).getTime();
		this.mType = DMFileTypeUtil
				.getFileCategoryTypeByName(this.mName);
		this.mLocation = DMFile.LOCATION_DROPBOX;
	}
	
	
	protected  long getFromMapAsLong(Map<String, Object> map, String name) {
		Object val = map.get(name);
		long ret = 0;
		if (val != null) {
			if (val instanceof Number) {
				ret = ((Number) val).longValue();
			} else if (val instanceof String) {
				// To parse cases where JSON can't represent a Long, so
				// it's stored as a string
				ret = Long.parseLong((String) val, 16);
			}
		}
		return ret;
	}
	
	protected  boolean getFromMapAsBoolean(Map<String, Object> map,
			String name) {
		Object val = map.get(name);
		if (val != null && val instanceof Boolean) {
			return ((Boolean) val).booleanValue();
		} else {
			return false;
		}
	}
	
}
