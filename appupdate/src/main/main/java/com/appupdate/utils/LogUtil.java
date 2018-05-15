package com.appupdate.utils;

import android.util.Log;

import com.appupdate.manager.DownloadManager;


public final class LogUtil {

    private static boolean b = true;

    static {
        b = DownloadManager.getInstance().getConfiguration().isEnableLog();
    }

    public static void e(String tag, String msg) {
        if (b) {
            Log.e(tag, msg);
        }
    }

    public static void e(String tag, int msg) {
        if (b) {
            Log.e(tag, String.valueOf(msg));
        }
    }

    public static void e(String tag, float msg) {
        if (b) {
            Log.e(tag, String.valueOf(msg));
        }
    }

    public static void e(String tag, Long msg) {
        if (b) {
            Log.e(tag, String.valueOf(msg));
        }
    }

    public static void e(String tag, double msg) {
        if (b) {
            Log.e(tag, String.valueOf(msg));
        }
    }

    public static void e(String tag, boolean msg) {
        if (b) {
            Log.e(tag, String.valueOf(msg));
        }
    }
}
