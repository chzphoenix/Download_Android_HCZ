package com.huichongzi.download_android.download;


import android.content.Context;
import android.os.Handler;
import android.os.Message;
import com.huichongzi.download_android.exception.DownloadDBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 单条下载任务
 * Created by cuihz on 2014/7/3.
 */
public class Downloader {
    protected DownloadInfo di;
    private Context context;
    private StorageHandleTask storageHandleTask;
    private DownloadTask downloadTask;
    private DownloadQueue queue;
    private static final Logger logger = LoggerFactory.getLogger(Downloader.class);


    protected Downloader(Context context, DownloadInfo di, DownloadQueue queue) {
        this.di = di;
        this.queue = queue;
        this.context = context;
    }



    /**
     * 检查存储情况。通过不同的回调处理相应的事件。
     * 1、有异常或错误
     * 2、未下载或未下载完开启下载线程
     */
    protected void tryStorage() {
        if(storageHandleTask != null && storageHandleTask.isAlive()){
            logger.debug("storageHandleTask has running");
            return;
        }
        if(downloadTask != null && downloadTask.isAlive()){
            logger.debug("downloadTask has running");
            return;
        }
        // 新建存储线程（存储可能需要3-5s，所以以线程方式）
        storageHandleTask = new StorageHandleTask(di, new StorageListener() {
            @Override
            public void onStartPre() {
                changeState(DownloadOrder.STATE_DOWNING, di.getProgress(), null, false, true);
            }

            public void onAlreadyDownload(String path) {
                logger.warn("{} already exists in {}", di.getName(), path);
                changeState(DownloadOrder.STATE_SUCCESS, 0, null, true, true);
            }

            public void onDownloadNotFinished(String path) {
                logger.info("{} is startDownload but not finished in {}", di.getName(), path);
                startDownload(false);
            }

            public void onNotDownload(String path) {
                logger.info("{} is not startDownload,it will startDownload in {}", di.getName(), path);
                startDownload(true);
            }

            public void onStorageNotEnough(long softSize, long avilableSize) {
                logger.error("{} not enough size, sdsize={}", di.getName(), avilableSize);
                String msg = "storage not enough";
                changeState(DownloadOrder.STATE_FAILED, DownloadOrder.FAILED_STORAGE_NOT_ENOUPH, msg, true, true);
            }

            public void onStorageNotMount(String path) {
                logger.error("{} sdcard not mounted {}", di.getName(), path);
                String msg = "sdcard not mounted";
                changeState(DownloadOrder.STATE_FAILED, DownloadOrder.FAILED_SDCARD_UNMOUNT, msg, true, true);
            }

            public void onDownloadUrlConnectError(String msg) {
                logger.error("{} not connect to {}", di.getName(), di.getUrl());
                changeState(DownloadOrder.STATE_FAILED, DownloadOrder.FAILED_URL_UNCONNECT, msg, true, true);
            }

            public void onFileSizeError() {
                logger.error("{} file size is diff of {}", di.getName(), di.getUrl());
                changeState(DownloadOrder.STATE_FAILED, DownloadOrder.FAILED_SIZE_ERROR, "file size is diff", true, true);
            }

            public void onCreateFailed(String msg) {
                logger.error("{} create tmp file failed", di.getName());
                changeState(DownloadOrder.STATE_FAILED, DownloadOrder.FAILED_CREATE_TMPFILE, msg, true, true);
            }
        });
        storageHandleTask.start();
    }


    /**
     * 开启下载线程
     *
     * @param isNew 是否为新的下载
     */
    private void startDownload(boolean isNew) {
        // 启动文件下载线程
        if(downloadTask != null && downloadTask.isAlive()){
            return;
        }
        if(di.getState() != DownloadOrder.STATE_DOWNING){
            return;
        }
        downloadTask = new DownloadTask(context, di, this, isNew);
        downloadTask.start();
    }


    /**
     * 改变下载状态
     * @param state 状态
     * @param code 如果状态为失败，则为失败码；如果状态为下载中，则为进度值。其他无意义
     * @param msg 如果状态为失败，则为失败信息，其他无意义
     * @param isNeedRefresh 是否刷新下载列表，启动其他等待的任务
     * @param isNeedSave 是否保存数据
     */
    protected synchronized void changeState(int state, int code , String msg, boolean isNeedRefresh, boolean isNeedSave) {
        if(state == DownloadOrder.STATE_DOWNING && di.getState() > DownloadOrder.STATE_WAIT_DOWN){
            return;
        }
        di.setState(state);
        if(isNeedSave && context != null) {
            if(state == DownloadOrder.STATE_DOWNING){
                di.setPriority(DownloadOrder.PRIORITY_MIN);
                di.setProgress(code);
            }
            else {
                queue.downloadings.remove(this);
            }
            if(state == DownloadOrder.STATE_SUCCESS){
                di.setDownOverTime(System.currentTimeMillis());
            }
            DownloadDao.saveIgnoreException(context, di);
        }
        if(isNeedRefresh && context != null){
            try {
                queue.refresh(context);
            } catch (DownloadDBException e) {
                logger.error(e.toString(), e);
            }
        }
        if(msg == null){
            msg = "";
        }
        Handler downloadHandler = queue.handlers.get(di.getId());
        if(downloadHandler != null){
            Message message = downloadHandler.obtainMessage(state, code, 0, msg);
            downloadHandler.sendMessage(message);
        }
        if(state == DownloadOrder.STATE_SUCCESS){
            queue.handlers.remove(downloadHandler);
        }
    }

}
