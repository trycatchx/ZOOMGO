package com.dmsys.airdiskpro.filemanager;

import com.dmsys.airdiskpro.utils.HandlerUtil;
import com.dmsys.dmsdk.model.SelectableItem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;


public class FileManager {

	/** 删除文件：单步消息 */
	public static final int MSG_DELETE_FILES_STEP = HandlerUtil.generateId();
	/** 删除文件失败：单步消息 */
	public static final int MSG_DELETE_FILES_STEP_FAILURE = HandlerUtil.generateId();
	/** 删除文件：删除完成 */
	public static final int MSG_DELETE_FILES_COMPLETE = HandlerUtil.generateId();
	/** 重命名文件：完成 */
	public static final int MSG_RENAME_FILE_COMPLETE = HandlerUtil.generateId();

	
	public static int getSelectedCount(Collection collection) {
		if(null == collection) {
			return 0;
		}
		int count = 0;
		Iterator<SelectableItem> iter = collection.iterator();
		while (iter.hasNext()) {
			SelectableItem item = iter.next();
			if (item.selected) {
				count++;
			}
		}
		return count;
	}

	public static boolean isAllSelected(Collection collection) {
		return collection.size() == getSelectedCount(collection);
	}

	public static void selectAll(Collection collection,List<String> l) {
		if(null == collection) {
			return;
		}
		Iterator<SelectableItem> iter = collection.iterator();
		if(l!= null) {
			while (iter.hasNext()) {
				SelectableItem s = iter.next();
				s.selected = true;
				l.add(s.mPath);
			}
		} else {
			while (iter.hasNext()) {
				iter.next().selected = true;
			}
		}
		
	}

	public static void unselectAll(Collection collection) {
		if(null == collection) {
			return;
		}
		Iterator<SelectableItem> iter = collection.iterator();
		while (iter.hasNext()) {
			iter.next().selected = false;
		}
	}

	public static void changeSelectState(SelectableItem selectable) {
		if(selectable!=null)
			selectable.selected = !selectable.selected;
	}

	public static List findSelectedFiles(Collection collection) {
		List<SelectableItem> selected = new ArrayList<SelectableItem>();
		Iterator<SelectableItem> iter = collection.iterator();
		while (iter.hasNext()) {
			SelectableItem item = iter.next();
			if (item.selected) {
				selected.add(item);
			}
		}
		return selected;
	}
	
}
