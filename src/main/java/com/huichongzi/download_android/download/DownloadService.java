package com.huichongzi.download_android.download;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * 下载后台服务
 * Created by cuihz on 2014/7/9.
 */
public class DownloadService extends Service {

    private static final Logger logger = LoggerFactory.getLogger(DownloadService.class);

    @Override
    public void onCreate() {
        super.onCreate();
        DownloadReceiver.addReceiver(this);
        if(DownloadManager.serviceListener != null){
            DownloadManager.serviceListener.onServiceCreate(this);
        }
    }


    @Override
    public void onDestroy() {
        DownloadReceiver.removeReceiver(this);
        if(DownloadManager.serviceListener != null){
            DownloadManager.serviceListener.onServiceDestroy(this);
        }
        super.onDestroy();
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        if(DownloadManager.serviceListener != null){
            DownloadManager.serviceListener.onServiceStart(this);
        }
        int mode = intent.getIntExtra("mode", 0);
        DownloadList.refresh(this, mode);
        return START_NOT_STICKY;
    }
}
