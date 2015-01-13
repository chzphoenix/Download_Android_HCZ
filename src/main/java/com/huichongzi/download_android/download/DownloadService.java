package com.huichongzi.download_android.download;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.text.TextUtils;
import com.huichongzi.download_android.exception.DownloadDBException;
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
    }


    @Override
    public void onDestroy() {
        DownloadHelper.isServiceOn = false;
        super.onDestroy();
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        DownloadHelper.isServiceOn = true;
        String group = intent.getStringExtra(DownloadInfo.GROUP);
        if(!TextUtils.isEmpty(group)) {
            try {
                DownloadQueue queue = DownloadHelper.getDownloadQueue(group);
                if(queue != null){
                    queue.refresh(this);
                }
            } catch (DownloadDBException e) {
                logger.error(e.toString(), e);
            }
        }
        return START_NOT_STICKY;
    }

}
