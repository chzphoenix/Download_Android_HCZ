package com.huichongzi.download_android.download;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    }


    @Override
    public void onDestroy() {
        DownloadReceiver.removeReceiver(this);
        super.onDestroy();
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        int action = intent.getIntExtra("action", 0);
        int id = intent.getIntExtra("id", 0);
        if (action == 0) {
            logger.error("start service failed, action is wrong");
            return START_NOT_STICKY;
        }
        if (!DownloadList.has(id)) {
            logger.error("{} is not exist.", id);
        }
        Downloader down = DownloadList.get(id);
        down.setContext(this);
        int state = down.di.getState();
        switch (action) {
            case DownloadOrder.ACTION_ADD:
                if (state <= DownloadOrder.STATE_DOWNING || state >= DownloadOrder.STATE_SUCCESS) {
                    down.di.setStateAndRefresh(DownloadOrder.STATE_WAIT);
                } else {
                    DownloadList.refresh();
                }
                break;
            case DownloadOrder.ACTION_PAUSE:
                if (state != DownloadOrder.STATE_SUCCESS) {
                    down.di.setStateAndRefresh(DownloadOrder.STATE_PAUSE);
                    return START_NOT_STICKY;
                }
                break;
            case DownloadOrder.ACTION_RESUME:
                if (state != DownloadOrder.STATE_SUCCESS) {
                    down.di.setStateAndRefresh(DownloadOrder.STATE_WAIT);
                    return START_NOT_STICKY;
                }
                break;
            case DownloadOrder.ACTION_CANCEL:
                down.stopDownload();
                break;
        }
        return START_NOT_STICKY;
    }

}
