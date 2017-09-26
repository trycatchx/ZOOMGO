package com.dmsys.dropbox.api;

/**
 * Created by jiong103 on 2017/3/9.
 */

public interface OnDropBoxFileItemClickListener {
    public boolean onFileClick(int position);

    public boolean onFileLongClick(int position);
}