package com.huichongzi.download_android.download;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.util.Log;

/**
 * 下载广播接收类
 * Created by cuihz on 2014/7/7.
 */
public class DownloadReceiver extends BroadcastReceiver{
    private static DownloadReceiver downloadReceiver;
    private boolean isSdcardMounted = true;
    private boolean isNetAlive = true;
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if(action.equals(Intent.ACTION_MEDIA_EJECT)){
            Log.i("DownloadReceiver", "sd卡卸载");
            DownloadList.waitAll();
            isSdcardMounted = false;
        }
        if(action.equals(Intent.ACTION_MEDIA_MOUNTED)){
            Log.i("DownloadReceiver", "sd卡装载");
            if(isNetAlive){
                DownloadList.refresh();
                isSdcardMounted = true;
            }
        }
        if(action.equals(ConnectivityManager.CONNECTIVITY_ACTION)){
            if(DownloadUtils.isNetAlive(context)){
                Log.i("DownloadReceiver", "网络已连接");
                if(isSdcardMounted){
                    DownloadList.refresh();
                    isNetAlive = true;
                }
            }
            else{
                Log.i("DownloadReceiver", "网络断开");
                DownloadList.waitAll();
                isNetAlive = false;
            }
        }
    }

    /**
     * 添加广播，如果已经存在则不再添加
     * @param context
     */
    protected static void addReceiver(Context context){
        if(downloadReceiver != null){
            return;
        }
        IntentFilter filter = new IntentFilter();
        filter.setPriority(1000);
        filter.addAction(Intent.ACTION_MEDIA_EJECT);
        filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        filter.addDataScheme("file");
        downloadReceiver = new DownloadReceiver();
        context.getApplicationContext().registerReceiver(downloadReceiver, filter);
        IntentFilter filter2 = new IntentFilter();
        filter2.setPriority(1000);
        filter2.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        context.getApplicationContext().registerReceiver(downloadReceiver, filter2);
        Log.i("DownloadReceiver", "注册");
    }


    /**
     * 移除广播
     * @param context
     */
    protected static void removeReceiver(Context context){
        try {
            if (downloadReceiver != null) {
                context.getApplicationContext().unregisterReceiver(downloadReceiver);
                Log.i("DownloadReceiver", "注销");
            }
        }
        catch(IllegalArgumentException e){
        }
    }
}
