package com.appupdate.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.appupdate.dialog.UpdateDialog;
import com.appupdate.listener.OnDownloadListener;
import com.appupdate.listener.OnUpdateDialogListener;
import com.appupdate.manager.BaseHttpDownloadManager;
import com.appupdate.manager.DownloadManager;
import com.appupdate.manager.HttpDownloadManager;
import com.appupdate.utils.ApkUtil;
import com.appupdate.utils.FileUtil;
import com.appupdate.utils.LogUtil;
import com.appupdate.utils.NotificationUtil;
import com.appupdate.utils.SharePreUtil;

import java.io.File;

/**
 * @author：zhangtianqiu on 18/5/9 14:08
 */
public class DownloadService extends Service implements OnDownloadListener {

    private static final String TAG = "DownloadService";
    private int smallIcon;
    private String apkUrl;
    private String apkName;
    private String downloadPath;
    private String versionCode;
    private OnDownloadListener listener;
    private OnUpdateDialogListener onUpdateDialogListener;
    private boolean showNotification;
    private boolean jumpInstallPage;
    private int lastProgress;


    /**
     * 是否正在下载，防止重复点击
     */
    private boolean downloading;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (null == intent) {
            return START_STICKY;
        }
        init();
        return super.onStartCommand(intent, flags, startId);
    }


    private void init() {
        apkUrl = DownloadManager.getInstance().getApkUrl();
        apkName = DownloadManager.getInstance().getApkName();
        downloadPath = DownloadManager.getInstance().getDownloadPath();
        smallIcon = DownloadManager.getInstance().getSmallIcon();
        versionCode = DownloadManager.getInstance().getApkVersionCode();
        //创建apk文件存储文件夹
        FileUtil.createDirDirectory(downloadPath);
        listener = DownloadManager.getInstance().getConfiguration().getOnDownloadListener();
        showNotification = DownloadManager.getInstance().getConfiguration().isShowNotification();
        jumpInstallPage = DownloadManager.getInstance().getConfiguration().isJumpInstallPage();
        //获取app通知开关是否打开
        boolean enable = NotificationUtil.notificationEnable(this);
        LogUtil.e(TAG, enable ? "应用的通知栏开关状态：已打开" : "应用的通知栏开关状态：已关闭");
        download();

    }

    /**
     * 获取下载管理者
     */
    private synchronized void download() {
        if (downloading) {
            LogUtil.e(TAG, "download: 当前正在下载，请务重复下载！");
            return;
        }
        BaseHttpDownloadManager manager = DownloadManager.getInstance().getConfiguration().getHttpManager();
        //如果用户自己定义了下载过程
        if (manager != null) {
            manager.download(apkUrl, apkName, this);
        } else {
            //使用自己的下载
            new HttpDownloadManager(this, downloadPath).download(apkUrl, apkName, this);
        }
        downloading = true;
    }


    @Override
    public void start() {
        if (showNotification) {
            handler.sendEmptyMessage(0);
            NotificationUtil.showNotification(this, smallIcon, "开始下载", "可稍后查看下载进度");
        }
        if (listener != null) {
            listener.start();
        }
        SharePreUtil.putString(DownloadManager.getContext(), "APK_DOWN_DONE", "");
        SharePreUtil.putString(DownloadManager.getContext(), "APK_DOWN_DONE_CODE", "");

    }

    @Override
    public void downloading(int max, int progress) {
        if (showNotification) {
            //优化通知栏更新，减少通知栏更新次数
            int curr = (int) (progress / (double) max * 100.0);
            if (curr != lastProgress) {
                lastProgress = curr;
                NotificationUtil.showProgressNotification(this, smallIcon, "正在下载新版本", "", max, progress);
            }
        }
        if (listener != null) {
            listener.downloading(max, progress);
        }

        if (onUpdateDialogListener == null) {
            onUpdateDialogListener = DownloadManager.getInstance().getOnUpdateDialogListener();
        }
        onUpdateDialogListener.onDownloadProgress(max, progress);

    }

    @Override
    public void done(File apk) {
        downloading = false;
        if (showNotification) {
            NotificationUtil.showDoneNotification(this, smallIcon, "下载完成", "点击进行安装", apk);
        }
        //如果用户设置了回调 则先处理用户的事件 在执行自己的
        if (listener != null) {
            listener.done(apk);
        }
        if (jumpInstallPage) {
            ApkUtil.installApk(this, apk);
        }
        releaseResources();
        SharePreUtil.putString(DownloadManager.getContext(), "APK_DOWN_DONE_CODE", versionCode);

    }

    @Override
    public void error(Exception e) {
        LogUtil.e(TAG, "error: " + e);
        downloading = false;
        if (showNotification) {
            NotificationUtil.showErrorNotification(this, smallIcon, "下载出错", "点击继续下载");
        }
        if (listener != null) {
            listener.error(e);
        }
    }

    /**
     * 下载完成释放资源
     */

    private void releaseResources() {
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
        stopSelf();
        DownloadManager.getInstance().release();
    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Toast.makeText(DownloadService.this, "正在后台下载新版本...", Toast.LENGTH_SHORT).show();

        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
