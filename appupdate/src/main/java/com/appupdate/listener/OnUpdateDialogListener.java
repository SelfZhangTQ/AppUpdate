package com.appupdate.listener;

/**
 * @author：zhangtianqiu on 18/5/9 18:42
 */
public interface OnUpdateDialogListener {
    /**
     * 下载进度
     *
     * @param max      总进度
     * @param progress 当前进度
     */
    void onDownloadProgress(int max, int progress);

    void onDownloadError();

}
