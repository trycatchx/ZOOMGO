package com.dmsys.dropbox.api;

import com.dmsys.dmsdk.model.DMFile;
import com.dmsys.dropbox.activity.MyDropBoxAllFileFragment;

import java.util.List;

/**
 * Created by jiong103 on 2017/3/9.
 */

public interface OnDropBoxDirViewStateChangeListener {
    public void onChange(MyDropBoxAllFileFragment.DropBoxEditState state, String currentPath,
                         List<DMFile> fileList);
}
