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
        if(Downloader.serviceListener != null){
            Downloader.serviceListener.onServiceCreate(this);
        }
    }


    @Override
    public void onDestroy() {
        DownloadReceiver.removeReceiver(this);
        if(Downloader.serviceListener != null){
            Downloader.serviceListener.onServiceDestroy(this);
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
        if(Downloader.serviceListener != null){
            Downloader.serviceListener.onServiceStart(this);
        }
        int action = intent.getIntExtra("action", 0);
        if (action == 0) {
            logger.error("start service failed, action is wrong");
            return START_NOT_STICKY;
        }

        int id = intent.getIntExtra("id", 0);
        Downloader down = getDownloader(id);

        String group = intent.getStringExtra("group");

        int[] ids = intent.getIntArrayExtra("ids");

        switch (action) {
            case DownloadOrder.ACTION_ADD:
                if(down.di == null){
                    break;
                }
                if (down.di.getState() <= DownloadOrder.STATE_DOWNING || down.di.getState() >= DownloadOrder.STATE_SUCCESS) {
                    down.di.setState(DownloadOrder.STATE_WAIT_DOWN);
                }
                down.changeState(down.di.getState(), 0, null, false);
                break;
            case DownloadOrder.ACTION_PAUSE:
                pauseDown(down);
                break;
            case DownloadOrder.ACTION_RESUME:
                resumeDown(down);
                break;
            case DownloadOrder.ACTION_CANCEL:
                cancelDown(down);
                break;
            case DownloadOrder.ACTION_PAUSE_GROUP:
                if(group == null || group.equals("")){
                    break;
                }
                for(Downloader downloader : DownloadList.downloadMap.values()){
                    if(downloader.di.getGroup().equals(group)){
                        pauseDown(downloader);
                    }
                }
                break;
            case DownloadOrder.ACTION_RESUME_GROUP:
                if(group == null || group.equals("")){
                    break;
                }
                for(Downloader downloader : DownloadList.downloadMap.values()){
                    if(downloader.di.getGroup().equals(group)){
                        resumeDown(downloader);
                    }
                }
                break;
            case DownloadOrder.ACTION_CANCEL_GROUP:
                int isDowned = intent.getIntExtra("isDowned", -1);
                if(group == null || group.equals("") || isDowned == -1){
                    break;
                }
                for(Downloader downloader : DownloadList.downloadMap.values()){
                    if(downloader.di.getGroup().equals(group)){
                        if((isDowned == DownloadOrder.STATE_SUCCESS) == (downloader.di.getState() == DownloadOrder.STATE_SUCCESS)){
                            cancelDown(downloader);
                        }
                    }
                }
                break;
            case DownloadOrder.ACTION_PAUSE_IDS:
                if(ids == null || ids.length <= 0){
                    break;
                }
                for(int downId : ids){
                    pauseDown(getDownloader(downId));
                }
                break;
            case DownloadOrder.ACTION_RESUME_IDS:
                if(ids == null || ids.length <= 0){
                    break;
                }
                for(int downId : ids){
                    resumeDown(getDownloader(downId));
                }
                break;
            case DownloadOrder.ACTION_CANCEL_IDS:
                if(ids == null || ids.length <= 0){
                    break;
                }
                for(int downId : ids){
                    cancelDown(getDownloader(downId));
                }
                break;
        }
        DownloadList.refresh(this, 0);
        return START_NOT_STICKY;
    }


    private Downloader getDownloader(int id){
        if (!DownloadList.has(id)) {
            logger.error("{} is not exist.", id);
            return null;
        }
        Downloader down = DownloadList.get(id);
        down.setContext(this);
        return down;
    }

    private void pauseDown(Downloader down){
        if(down.di == null){
            return;
        }
        if (down.di.getState() != DownloadOrder.STATE_SUCCESS) {
            down.changeState(DownloadOrder.STATE_PAUSE, 0, null, false);
        }
    }


    private void resumeDown(Downloader down){
        if(down.di == null){
            return;
        }
        if (down.di.getState() != DownloadOrder.STATE_SUCCESS && down.di.getState() != DownloadOrder.STATE_DOWNING && down.di.getState() != DownloadOrder.STATE_WAIT_RECONN) {
            down.changeState(DownloadOrder.STATE_WAIT_DOWN, 0, null, false);
        }
    }


    private void cancelDown(Downloader down){
        if(down.di == null){
            return;
        }
        DownloadList.remove(down.di.getId());
    }

}
