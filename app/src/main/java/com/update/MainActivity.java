package com.update;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.appupdate.config.UpdateConfiguration;
import com.appupdate.manager.DownloadManager;
import com.permission.PermissionCallback;
import com.permission.PermissionItem;
import com.permission.PermissionManager;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.tv_update).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<PermissionItem> permissionItems = new ArrayList<PermissionItem>();
                permissionItems.add(new PermissionItem(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, "手机文件读写权限"));
                permissionItems.add(new PermissionItem(android.Manifest.permission.READ_EXTERNAL_STORAGE, "手机文件读写权限"));
                PermissionManager.create(MainActivity.this)
                        .permissions(permissionItems)
                        .animStyle(R.style.PermissionAnimScale)
                        .checkArrayPermission(new PermissionCallback() {
                            @Override
                            public void onClose() {
                            }

                            @Override
                            public void onSuccess() {
                                DownloadManager.getInstance(MainActivity.this)
                                        .setApkName("hxq.apk")
                                        .setApkUrl("http://download.huoli666.com/upgrade/app-AliBB-release.apk")
                                        .setDownloadPath(Environment.getExternalStorageDirectory() + "/AppUpdate")
                                        .setConfiguration(new UpdateConfiguration().setEnableLog(false).setForcedUpgrade(false).setDefaultDialogStyle(false))
                                        .setDialogStyle("1")
                                        .setSmallIcon(R.mipmap.ic_launcher)
                                        .setApkVersionCode("6")
                                        .setApkVersionName("1.1.5")
                                        .setApkDescription("1.首页优化\n2.新增邀请活动\n3.新增随机掉金币\n4.修复已知bug")
                                        .download();
                            }

                            @Override
                            public void onDeny(String permission, int position) {
                            }

                            @Override
                            public void onGuarantee(String permission, int position) {
                            }
                        });

            }
        });
    }
}
