package com.huichongzi.download_android.download;

import java.io.File;
import java.util.List;

import android.content.Context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 下载主类
 * Created by cuihz on 2014/7/3.
 */
public class Downloader {
    private DownloaderListener downloadListener = null;
    protected DownloadInfo di;
    private Context context;
    private static final Logger logger = LoggerFactory.getLogger(Downloader.class);


    private Downloader(Context context, DownloadInfo di, DownloaderListener listener) {
        this.context = context;
        this.di = di;
        this.downloadListener = listener;
    }


    /**
     * 检查存储情况。通过不同的回调处理相应的事件。
     * 1、有异常或错误
     * 2、未下载或未下载完开启下载线程
     */
    public void tryStorage() {
        // 新建存储线程（存储可能需要3-5s，所以以线程方式）
        StorageHandleTask sh = new StorageHandleTask(di, new StorageListener() {
            public void onAlreadyDownload(String path) {
                logger.warn("{} already exists in {}", di.getName(), path);
                if (downloadListener != null) {
                    downloadListener.onDownloadRepeat(di.getName() + "文件已经下载过");
                }
            }
            public void onDownloadNotFinished(String path) {
                logger.info("{} is download but not finished in {}", di.getName(), path);
                download(false);
            }
            public void onNotDownload(String path) {
                logger.info("{} is not download,it will download in {}", di.getName(), path);
                download(true);
            }
            public void onStorageNotEnough(long softSize, long avilableSize) {
                logger.error("{} not enough size, sdsize={}", di.getName(), avilableSize);
                String msg = "空间不足";
                if (downloadListener != null) {
                    downloadListener.onCreateFailed(msg);
                }
            }
            public void onStorageNotMount(String path) {
                logger.error("{} sdcard not mounted", di.getName());
                String msg = "sd卡不存在";
                if (downloadListener != null) {
                    downloadListener.onCreateFailed(msg);
                }
            }
            public void onDownloadPathConnectError(String msg) {
                logger.error("{} not connect to {}", di.getName(), di.getUrl());
                if (downloadListener != null) {
                    downloadListener.onConnectFailed(di.getName() + msg);
                }
            }
            public void onFileSizeError() {
                logger.error("{} file size is diff of {}", di.getName(), di.getUrl());
                if (downloadListener != null) {
                    downloadListener.onCheckFailed(di.getName() + "文件大小与服务器不符");
                }
            }
            public void onCreateFailed(){
                logger.error("{} create tmp file failed", di.getName());
                if (downloadListener != null) {
                    downloadListener.onCreateFailed("创建临时文件失败");
                }
            }
        });
        sh.start();
    }


    /**
     * 开启下载线程
     * @param isNew 是否为新的下载
     */
    private void download(boolean isNew) {
        // 启动文件下载线程
        DownloadTask downloadTask = new DownloadTask(context, di, downloadListener, isNew);
        downloadTask.start();
    }

    /**
     * 取消下载
     * 删除文件
     */
    protected void stopDownload() {
        di.setStateAndRefresh(DownloadOrder.STATE_STOP);
        DownloadUtils.removeFile(di.getPath());
    }




    /**
     * 暂停下载
     * @param id 下载唯一id
     */
    public static void pauseDownload(String id) {
        if(DownloadList.has(id)){
            Downloader down = DownloadList.get(id);
            if(downloaderIsUsable(down)){
                down.di.setStateAndRefresh(DownloadOrder.STATE_PAUSE);
                return;
            }
            DownloadList.remove(id);
        }
    }


    /**
     * 恢复下载
     * @param id 下载唯一id
     * @throws DownloadNotExistException 下载任务不存在
     */
    public static void resumeDownload(String id) throws DownloadNotExistException {
        if(DownloadList.has(id)){
            Downloader down = DownloadList.get(id);
            if(downloaderIsUsable(down)){
                down.di.setStateAndRefresh(DownloadOrder.STATE_WAIT);
                return;
            }
            DownloadList.remove(id);
        }
        throw new DownloadNotExistException();
    }





    /**
     * 取消下载/删除已下载文件
     * @param id
     */
    public static void cancelDownload(String id){
        if(DownloadList.has(id)){
            Downloader down = DownloadList.get(id);
            if(downloaderIsUsable(down)){
                down.stopDownload();
            }
            DownloadList.remove(id);
        }
    }


    /**
     * 获取数据库中的下载列表。
     * 用于程序启动时
     * @param group 下载组
     * @param isDowned 是否已下载完
     * @return
     */
    public static List<DownloadInfo> getDownloadList(String group, boolean isDowned){
        return DownloadDB.getList(group, isDowned);
    }





    /**
     * 添加下载任务
     * @param context
     * @param di 下载信息
     * @param listener 下载事件回调
     * @throws DownloadRepeatException 重复下载异常
     */
    public static void downloadEvent(Context context, DownloadInfo di, DownloaderListener listener) throws DownloadRepeatException{
        Downloader downloader = new Downloader(context, di, listener);
        //检查是否已下载完成
        File file = new File(di.getPath());
        if (file.exists() && file.isFile()) {
            logger.debug("{} is downloaded", di.getName());
            di.setStateAndRefresh(DownloadOrder.STATE_SUCCESS);
            DownloadList.add(context, downloader);
            throw new DownloadRepeatException("已经下载过了");
        }
        //检查是否已经在任务列表中
        if (DownloadList.has(di.getId()) && downloaderIsUsable(DownloadList.get(di.getId()))) {
            logger.debug("{} is downloading", di.getName());
            throw new DownloadRepeatException("已经在任务列表中");
        }
        DownloadList.add(context, downloader);
        di.setStateAndRefresh(DownloadOrder.STATE_WAIT);
    }





    /**
     *  判断一个下载者是否可用
     * @param down
     * @return
     */
    private static boolean downloaderIsUsable(Downloader down){
        if(down == null || down.di == null || down.downloadListener == null){
            return false;
        }
        int state = down.di.getState();
        if(state != DownloadOrder.STATE_PAUSE || state != DownloadOrder.STATE_FAILED || state != DownloadOrder.STATE_WAIT){
            return false;
        }
        return true;
    }



}
