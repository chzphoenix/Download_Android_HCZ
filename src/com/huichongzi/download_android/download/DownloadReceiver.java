package com.huichongzi.download_android.download;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * 下载广播接收类
 * Created by cuihz on 2014/7/7.
 */
public class DownloadReceiver extends BroadcastReceiver{
    private static boolean isAdded = false;
    private boolean isSdcardMounted = true;
    private boolean isNetAlive = true;
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if(action.equals(Intent.ACTION_MEDIA_EJECT)){
            DownloadList.waitAll();
            isSdcardMounted = false;
            return;
        }
        if(action.equals(Intent.ACTION_MEDIA_MOUNTED) && isNetAlive){
            DownloadList.refresh();
            isSdcardMounted = true;
            return;
        }
        if(action.equals(ConnectivityManager.CONNECTIVITY_ACTION)){
            ConnectivityManager conn = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo info = conn.getActiveNetworkInfo();
            if(info != null && info.isAvailable() && isSdcardMounted){
                DownloadList.refresh();
                isNetAlive = true;
            }
            else{
                DownloadList.waitAll();
                isNetAlive = false;
            }
        }
    }

    /**
     * 添加广播接收，如果已经添加过则不再添加
     */
    protected static void addReceiver(Context context){
        if(isAdded){
            return;
        }
        IntentFilter filter = new IntentFilter();
        filter.setPriority(1000);
        filter.addAction(Intent.ACTION_MEDIA_EJECT);
        filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        filter.addDataScheme("file");
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        DownloadReceiver receiver = new DownloadReceiver();
        context.registerReceiver(receiver, filter);
        isAdded = true;
    }
}
