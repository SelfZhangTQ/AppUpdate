package com.appupdate.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.appupdate.R;
import com.appupdate.listener.OnButtonClickListener;
import com.appupdate.listener.OnUpdateDialogListener;
import com.appupdate.manager.DownloadManager;
import com.appupdate.service.DownloadService;
import com.appupdate.utils.ApkUtil;
import com.appupdate.utils.ScreenUtil;
import com.appupdate.utils.SharePreUtil;

import java.io.File;


/**
 * @author：zhangtianqiu on 18/5/9 14:29
 */
public class UpdateDialog extends Dialog implements View.OnClickListener, OnUpdateDialogListener {

    private Context context;
    private DownloadManager manager;
    private boolean forcedUpgrade;
    private TextView update;
    private OnButtonClickListener buttonClickListener;


    private String appDownDoneFile;
    private String appDownDoneCode;
    private String appNetVersionCode;
    private String appLength;


    private int max;
    private int progress;

    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1) {

                float pressent = (float) progress / max * 100;
                update.setText("正在下载" + (int) pressent + "%");
                if ((int) pressent == 100) {
                    update.setText("新版本下载完成");
                    appDownDoneFile = SharePreUtil.getString(context, "APK_DOWN_DONE", "");
                    appLength = SharePreUtil.getString(context, "APK_LENGTH", "");
                    appDownDoneCode = SharePreUtil.getString(context, "APK_DOWN_DONE_CODE", "");
                }
            } else if (msg.what == 2) {
                update.setText("更新失败");
            }


        }
    };

    public UpdateDialog(@NonNull Context context) {
        super(context, R.style.UpdateDialog);
        init(context);
    }

    /**
     * 初始化布局
     */
    private void init(Context context) {
        this.context = context;
        manager = DownloadManager.getInstance();
        forcedUpgrade = manager.getConfiguration().isForcedUpgrade();
        manager.setOnUpdateDialogListener(this);
        buttonClickListener = manager.getConfiguration().getOnButtonClickListener();
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_update, null);
        setContentView(view);
        setWindowSize(context);
        initView(view);
    }

    private void initView(View view) {
        View ibClose = view.findViewById(R.id.ib_close);
        TextView title = view.findViewById(R.id.tv_title);
        TextView size = view.findViewById(R.id.tv_size);
        TextView description = view.findViewById(R.id.tv_description);
        update = view.findViewById(R.id.btn_update);
        View line = view.findViewById(R.id.line);
        update.setOnClickListener(this);
        ibClose.setOnClickListener(this);
        //强制升级
        if (forcedUpgrade) {
            line.setVisibility(View.GONE);
            ibClose.setVisibility(View.GONE);
            setOnKeyListener(new OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    //屏蔽返回键
                    return keyCode == KeyEvent.KEYCODE_BACK;
                }
            });
        }
        //设置界面数据
        if (!TextUtils.isEmpty(manager.getApkVersionName())) {
            title.setText(String.format("发现新版啦！v%s", manager.getApkVersionName()));
        }
        if (!TextUtils.isEmpty(manager.getApkSize())) {
            size.setText(String.format("新版本大小：%sM", manager.getApkSize()));
            size.setVisibility(View.VISIBLE);
        }
        description.setText(manager.getApkDescription());

        appDownDoneFile = SharePreUtil.getString(context, "APK_DOWN_DONE", "");
        appLength = SharePreUtil.getString(context, "APK_LENGTH", "");
        appDownDoneCode = SharePreUtil.getString(context, "APK_DOWN_DONE_CODE", "");
        appNetVersionCode = DownloadManager.getInstance().getApkVersionCode();
        if (!TextUtils.isEmpty(appDownDoneFile) && !TextUtils.isEmpty(appLength) && !TextUtils.isEmpty(appNetVersionCode) && !TextUtils.isEmpty(appLength)) {
            if (new File(appDownDoneFile).length() == Long.valueOf(appLength)) {
                if (appNetVersionCode.equals(appDownDoneCode)) {
                    update.setText("立即安装");
                }
            }

        }
    }

    private void setWindowSize(Context context) {
        Window dialogWindow = this.getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.width = (int) (ScreenUtil.getWith(context) * 0.65f);
        lp.height = (int) ((ScreenUtil.getWith(context) * 0.65f) * 1.5);
        lp.gravity = Gravity.CENTER;
        dialogWindow.setAttributes(lp);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.ib_close) {
            if (!forcedUpgrade) {
                onDismiss();
            }
            //回调点击事件
            if (buttonClickListener != null) {
                buttonClickListener.onButtonClick(OnButtonClickListener.CANCEL);
            }
        } else if (id == R.id.btn_update) {
            if (!forcedUpgrade) {
                onDismiss();

            }
            //回调点击事件
            if (buttonClickListener != null) {
                buttonClickListener.onButtonClick(OnButtonClickListener.UPDATE);
            }
            if (!TextUtils.isEmpty(appDownDoneFile) && !TextUtils.isEmpty(appDownDoneCode) && !TextUtils.isEmpty(appNetVersionCode) && !TextUtils.isEmpty(appLength)) {
                if (appNetVersionCode.equals(appDownDoneCode)) {
                    long fileLength = new File(appDownDoneFile).length();
                    if (fileLength == Long.valueOf(appLength)) {
                        if (appNetVersionCode.equals(appDownDoneCode)) {
                            ApkUtil.installApk(context, new File(appDownDoneFile));
                            update.setText("立即安装");
                        } else {
                            context.startService(new Intent(context, DownloadService.class));
                            update.setText("正在下载");
                        }
                    } else {
                        context.startService(new Intent(context, DownloadService.class));
                        update.setText("正在下载");
                    }

                } else {
                    context.startService(new Intent(context, DownloadService.class));
                    update.setText("正在下载");
                }
            } else {
                context.startService(new Intent(context, DownloadService.class));
                update.setText("正在下载");
            }

        }
    }

    @Override
    public void onDownloadProgress(final int max, final int progress) {
        this.max = max;
        this.progress = progress;
        handler.sendEmptyMessageDelayed(1, 500);
    }

    @Override
    public void onDownloadError() {
        handler.sendEmptyMessageDelayed(2, 100);
    }

    public void onDismiss() {
        dismiss();
        DownloadManager.getInstance().release();
    }
}
