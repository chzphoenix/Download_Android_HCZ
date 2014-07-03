package com.huichongzi.download_android.download;

import java.io.File;
import java.util.Hashtable;
import java.util.Iterator;

import android.content.Context;
import android.util.Log;


/**
 * 下载主类
 * Created by cuihz on 2014/7/3.
 */
public class Downloader {
    private static Context mContext = null;
    private DownloaderListener downloadListener = null;
    // 最大允许启动下载的个数
    private static final int Max_Allow_Download = 3;
    // 当前下载的存储表
    protected static Hashtable<String, DownloadTask> Download_Map = new Hashtable<String, DownloadTask>();
    private DownloadInfo downloadInfo;
    //下载中断是否重连
    private static boolean isReconnect = false;
    private static DownloadTaskCheckThread downloadTaskCheck = new DownloadTaskCheckThread();
    private static boolean checkTaskIsOn = false;


    private Downloader(Context context, DownloadInfo di, DownloaderListener listener) {
        mContext = context.getApplicationContext();
        this.downloadInfo = di;
        this.downloadListener = listener;
    }


    /**
     * 检查存储情况。通过不同的回调处理相应的事件。
     * 1、有异常或错误
     * 2、未下载或未下载完开启下载线程
     */
    private void tryStorage() {
        // 新建存储线程（存储可能需要3-5s，所以以线程方式）
        StorageHandleTask sh = new StorageHandleTask(mContext, downloadInfo, new StorageListener() {
            public void onAlreadyDownload(String path) {
                Log.d("onAlreadyDownload", downloadInfo.getName() + " already exists in "
                        + path);
                if (downloadListener != null) {
                    downloadListener.onDownloadRepeat(downloadInfo.getName() + "文件已经下载过");
                }
            }
            public void onDownloadNotFinished(String path) {
                Log.e("onDownloadNotFinished", downloadInfo.getName()
                        + " is download but not finished in " + path);
                download(false);
            }
            public void onNotDownload(String path) {
                Log.e("onNotDownload", downloadInfo.getName()
                        + " is  not download,it will download in "
                        + path);
                download(true);
            }
            public void onStorageNotEnough(long softSize, long avilableSize) {
                Log.e("onStorageNotEnough", downloadInfo.getName()
                        + "not enough size sdsize=" + avilableSize);
                String msg = "空间不足";
                if (downloadListener != null) {
                    downloadListener.onCreateFailed(msg);
                }
            }
            public void onStorageNotMount(String path) {
                Log.e("onStorageNotMount", downloadInfo.getName() + "rom can't chmod");
                String msg = "sd卡不存在";
                if (downloadListener != null) {
                    downloadListener.onCreateFailed(msg);
                }
            }
            public void onDownloadPathConnectError(String msg) {
                Log.e("onDownloadPathConnectError", downloadInfo.getName() + "无法连接到下载地址" + downloadInfo.getUrl());
                if (downloadListener != null) {
                    downloadListener.onConnectFailed(downloadInfo.getName() + msg);
                }
            }
            public void onFileSizeError() {
                Log.e("onDownloadPathConnectError", downloadInfo.getName() + "文件大小与服务器不符" + downloadInfo.getUrl());
                if (downloadListener != null) {
                    downloadListener.onCheckFailed(downloadInfo.getName() + "文件大小与服务器不符");
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
        DownloadTask downloadTask = new DownloadTask(mContext, downloadInfo, downloadListener, isNew);
        downloadTask.start();
        Download_Map.put(downloadInfo.getId(), downloadTask);
    }


    /**
     * 暂停下载
     * @param id 下载唯一id
     */
    public static void pauseDownload(String id) {
        if(Download_Map.containsKey(id)){
            DownloadTask task = Download_Map.get(id);
            if(task != null){
                task.pauseDownload();
                return;
            }
            else{
                Download_Map.remove(id);
            }
        }
    }


    /**
     * 恢复下载
     * @param id 下载唯一id
     * @throws DownloadOverFlowException 超过最大下载数异常
     * @throws DownloadNotExistException 下载任务不存在
     */
    public static void resumeDownload(String id) throws DownloadOverFlowException, DownloadNotExistException {
        if(getDowningSize() >= Max_Allow_Download){
            throw new DownloadOverFlowException();
        }
        if(Download_Map.containsKey(id)){
            DownloadTask task = Download_Map.get(id);
            if(task != null){
                task.resumeDownload();
                return;
            }
            else{
                Download_Map.remove(id);
            }
        }
        throw new DownloadNotExistException();
    }


    /**
     * 下载是否暂停
     * @param id
     * @return
     */
    public static boolean isDownloadPause(String id){
        if(Download_Map.containsKey(id)){
            DownloadTask task = Download_Map.get(id);
            if(task != null && !task.isStop()){
                return task.isPause();
            }
            else{
                Download_Map.remove(id);
            }
        }
        return true;
    }


    /**
     * 取消下载
     * @param id
     */
    public static void cancelDownload(String id){
        if(Download_Map.containsKey(id)){
            DownloadTask task = Download_Map.get(id);
            if(task != null){
                task.stopDownload();
            }
            Download_Map.remove(id);
        }
    }


    /**
     * 添加下载任务
     * @param context
     * @param di 下载信息
     * @param listener 下载事件回调
     * @throws DownloadRepeatException 重复下载异常
     * @throws DownloadOverFlowException 下载数超出异常
     */
    public static void downloadEvent(Context context, DownloadInfo di, DownloaderListener listener) throws DownloadRepeatException, DownloadOverFlowException {
        //检查是否已下载完成
        File file = new File(di.getPath());
        if (file.exists() && file.isFile()) {
            throw new DownloadRepeatException("已经下载过了");
        } else {
            file.getParentFile().mkdirs();
        }
        //未下载则下载
        if (Download_Map.containsKey(di.getId())) {
            Log.d("download", di.getName() + " is downloading");
            throw new DownloadRepeatException("正在下载");
        }
        if (getDowningSize() >= Max_Allow_Download) {
            Log.d("download", "max download is " + Max_Allow_Download);
            throw new DownloadOverFlowException();
        }
        Downloader downloader = new Downloader(context, di, listener);
        // 下载存储应用
        downloader.tryStorage();
        //给服务端发消息，通知点击下载
        listener.onStartDownload();
    }


    public static void setReconnect(boolean flag) {
        isReconnect = flag;
        if (isReconnect) {
            if (!downloadTaskCheck.isAlive() && !checkTaskIsOn) {
                downloadTaskCheck.start();
                checkTaskIsOn = true;
            }
        } else {
        }
    }

    protected static class DownloadTaskCheckThread extends Thread {
        public void run() {
            while (isReconnect) {
                try {
                    sleep(3000);
                } catch (InterruptedException e) {
                }

                Log.e("", "下载监控线程运行中....,Download_Map size=" + Download_Map.size());
                if (Download_Map.size() == 0) {
                    Downloader.setReconnect(false);
                    continue;
                }
                if (!DownloadUtils.isNetworkAvailable(Downloader.mContext)) {
                    continue;
                }
                for (Iterator<DownloadTask> iter = Download_Map.values().iterator(); iter.hasNext(); ) {
                    DownloadTask task = iter.next();
                    if (!task.isFinished() && task.isPause()) {
                        task.resumeDownload();
                    }
                }

            }
        }
    }


    private static int getDowningSize(){
        int size = 0;
        for (Iterator<DownloadTask> iter = Download_Map.values().iterator(); iter.hasNext(); ) {
            DownloadTask task = iter.next();
            if (!task.isFinished() && !task.isPause()) {
                size++;
            }
        }
        return size;
    }


}
