package com.huichongzi.download_android.download;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 下载广播接收类
 * Created by cuihz on 2014/7/7.
 */
class DownloadReceiver extends BroadcastReceiver{
    private static final Logger logger = LoggerFactory.getLogger(DownloadReceiver.class);
    private static DownloadReceiver downloadReceiver;
    private boolean isSdcardMounted = true;
    private boolean isNetAlive = true;
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if(action.equals(Intent.ACTION_MEDIA_EJECT)){
            logger.debug("sdcard unmounted");
            DownloadList.waitAllForReconn();
            isSdcardMounted = false;
        }
        if(action.equals(Intent.ACTION_MEDIA_MOUNTED)){
            logger.debug("sdcard mounted");
            if(isNetAlive){
                DownloadList.refresh(DownloadOrder.RECONNMODE_SDCARD);
                isSdcardMounted = true;
            }
        }
        if(action.equals(ConnectivityManager.CONNECTIVITY_ACTION)){
            if(DownloadUtils.isNetAlive(context)){
                logger.debug("net connected");
                if(isSdcardMounted){
                    DownloadList.refresh(DownloadOrder.RECONNMODE_NET);
                    isNetAlive = true;
                }
            }
            else{
                logger.debug("net unconnected");
                DownloadList.waitAllForReconn();
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
        logger.info("registe receiver");
    }


    /**
     * 移除广播
     * @param context
     */
    protected static void removeReceiver(Context context){
        try {
            if (downloadReceiver != null) {
                context.getApplicationContext().unregisterReceiver(downloadReceiver);
                logger.info("unregiste receiver");
            }
        }
        catch(IllegalArgumentException e){
        }
    }
}
