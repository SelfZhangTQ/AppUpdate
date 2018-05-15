package com.appupdate.manager;

import com.appupdate.listener.OnDownloadListener;

/**
 * @author：zhangtianqiu on 18/5/9 14:10
 */
public abstract class BaseHttpDownloadManager {
    /**
     * 下载apk
     *
     * @param apkUrl   apk下载地址
     * @param apkName  apk名字
     * @param listener 回调
     */
    public abstract void download(String apkUrl, String apkName, OnDownloadListener listener);

}
