package com.huichongzi.download_android.download;

import java.io.File;
import java.util.Hashtable;
import java.util.Iterator;

import android.content.Context;
import android.util.Log;


public class Downloader {

    private static Context mContext = null;
    private DownloaderListener downloadListener = null;

    // 最大允许启动下载的个数
    private static final int Max_Allow_Download = 3;

    // 当前下载的存储表
    protected static Hashtable<String, DownloadTask> Download_Map = new Hashtable<String, DownloadTask>();


    private DownloadInfo downloadInfo;

    private static DownloadTaskCheckThread downloadTaskCheck = new DownloadTaskCheckThread();
    private static boolean checkTaskIsOn = false;
    //下载中断是否重连
    private static boolean isReconnect = false;

    private DownloadTask downloadTask;


    private Downloader(Context context, DownloadInfo di, DownloaderListener listener) {
        mContext = context.getApplicationContext();
        this.downloadInfo = di;
        this.downloadListener = listener;
    }


    private void tryStorage() {
        // 新建存储线程（存储可能需要3-5s，所以以线程方式）
        StorageHandlerTask sh = new StorageHandlerTask(mContext, downloadInfo, new StorageListener() {

            public void onAlreadyDownload(String path) {
                Log.d("onAlreadyDownload", downloadInfo.getName() + " already exists in "
                        + path);
                if (downloadListener != null) {
                    downloadListener.onDownloaded();
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
                    downloadListener.onProFailed(DownloadConfig.Storage_Not_Enough, msg);
                }
            }


            public void onStorageNotMount(String path) {
                Log.e("onStorageNotMount", downloadInfo.getName() + "rom can't chmod");
                String msg = "sd卡不存在";
                if (downloadListener != null) {
                    downloadListener.onProFailed(DownloadConfig.Storage_Cannot_Chmod, msg);
                }

            }

            public void onDownloadPathConnectError(String msg) {
                Log.e("onDownloadPathConnectError", downloadInfo.getName() + "无法连接到下载地址" + downloadInfo.getUrl());
                if (downloadListener != null) {
                    downloadListener.onProFailed(DownloadConfig.Url_Connect_Error, downloadInfo.getName() + msg);
                }
            }

            public void onFileSizeError() {
                Log.e("onDownloadPathConnectError", downloadInfo.getName() + "文件大小与服务器不符" + downloadInfo.getUrl());
                if (downloadListener != null) {
                    downloadListener.onProFailed(DownloadConfig.Is_Size_Error, downloadInfo.getName() + "文件大小与服务器不符");
                }
            }
        });
        sh.start();
    }


    private void download(boolean isNew) {
        // 启动文件下载线程
        downloadTask = new DownloadTask(mContext, downloadInfo, downloadListener, isNew);
        downloadTask.start();
        Download_Map.put(downloadInfo.getId(), downloadTask);

    }


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


    public static void resumeDownload(String id) {
        if(getDowningSize() >= Max_Allow_Download){

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

    }


    public static boolean isDownloadPause(String id) throws Exception {
        if(Download_Map.containsKey(id)){
            DownloadTask task = Download_Map.get(id);
            if(task != null && !task.isStop()){
                return task.isPause();
            }
            else{
                Download_Map.remove(id);
            }
        }
            throw new Exception();
    }



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
     * 添加下载事件
     * @param context
     * @param di 下载信息
     * @param listener 下载监听回调
     */
    public static void downloadEvent(Context context, DownloadInfo di, DownloaderListener listener) {
        //检查是否已下载
        File file = new File(di.getPath());
        if (file.exists() && file.isFile()) {

            return;
        } else {
            file.getParentFile().mkdirs();
        }


        //未下载则下载
        if (Download_Map.containsKey(di.getId())) {
            listener.onProFailed(DownloadConfig.Is_Download_Repeat, "当前文件已在下载");
            Log.d("download", di.getName() + " is downloading");
            return;
        }
        if (getDowningSize() >= Max_Allow_Download) {
            listener.onProFailed(DownloadConfig.Is_Download_OverFlow, "最大下载数为"
                    + Max_Allow_Download + "个");
            Log.d("download", "max download is " + Max_Allow_Download);
            return;
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
