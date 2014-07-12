package com.huichongzi.download_android.download;

import java.util.List;

import android.content.Context;

import android.content.Intent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 下载主类
 * Created by cuihz on 2014/7/3.
 */
public class Downloader {
    private DownloaderListener downloadListener = null;
    protected static DownServiceListener serviceListener= null;
    protected DownloadInfo di;
    private Context context;
    private static final Logger logger = LoggerFactory.getLogger(Downloader.class);


    protected Downloader(DownloadInfo di, DownloaderListener listener) {
        this.downloadListener = listener;
        this.di = di;
    }


    public void setContext(Context context) {
        this.context = context;
    }

    /**
     * 检查存储情况。通过不同的回调处理相应的事件。
     * 1、有异常或错误
     * 2、未下载或未下载完开启下载线程
     */
    protected void tryStorage() {
        // 新建存储线程（存储可能需要3-5s，所以以线程方式）
        StorageHandleTask sh = new StorageHandleTask(di, new StorageListener() {
            public void onAlreadyDownload(String path) {
                logger.warn("{} already exists in {}", di.getName(), path);
                if (!DownloadList.has(di.getId())) {
                    di.setState(DownloadOrder.STATE_SUCCESS);
                    DownloadList.add(Downloader.this);
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
                    downloadListener.onProFailed(DownloadOrder.FAILED_STORAGE_NOT_ENOUPH, msg);
                }
            }

            public void onStorageNotMount(String path) {
                logger.error("{} sdcard not mounted", di.getName());
                String msg = "sd卡不存在";
                if (downloadListener != null) {
                    downloadListener.onProFailed(DownloadOrder.FAILED_SDCARD_UNMOUNT, msg);
                }
            }

            public void onDownloadUrlConnectError(String msg) {
                logger.error("{} not connect to {}", di.getName(), di.getUrl());
                if (downloadListener != null) {
                    downloadListener.onProFailed(DownloadOrder.FAILED_URL_UNCONNECT, msg);
                }
            }

            public void onFileSizeError() {
                logger.error("{} file size is diff of {}", di.getName(), di.getUrl());
                if (downloadListener != null) {
                    downloadListener.onProFailed(DownloadOrder.FAILED_SIZE_ERROR, "文件大小与服务器不符");
                }
            }

            public void onCreateFailed(String msg) {
                logger.error("{} create tmp file failed", di.getName());
                if (downloadListener != null) {
                    downloadListener.onProFailed(DownloadOrder.FAILED_CREATE_TMPFILE, msg);
                }
            }
        });
        sh.start();
    }


    /**
     * 开启下载线程
     *
     * @param isNew 是否为新的下载
     */
    private void download(boolean isNew) {
        // 启动文件下载线程
        DownloadTask downloadTask = new DownloadTask(context, di, downloadListener, isNew);
        downloadTask.start();
    }





    /**
     * 暂停下载
     *
     * @param context
     * @param id      下载唯一id
     * @throws DownloadNotExistException 下载任务不存在或id不正确
     * @throws IllegalParamsException di参数不合法
     */
    public static void pauseDownload(Context context, int id) throws DownloadNotExistException, IllegalParamsException {
        if(context == null){
            throw new IllegalParamsException("context", "不能为空");
        }
        if(id <= 0){
            throw new IllegalParamsException("id", "不能为负或0");
        }
        if (DownloadList.has(id)) {
            Intent intent = new Intent(context, DownloadService.class);
            intent.putExtra("action", DownloadOrder.ACTION_PAUSE);
            intent.putExtra("id", id);
            context.startService(intent);
        } else {
            throw new DownloadNotExistException();
        }
    }


    /**
     * 批量暂停下载，根据id列表
     *
     * @param context
     * @param ids
     * @throws IllegalParamsException di参数不合法
     */
    public static void pauseDownloadForIds(Context context, List<Integer> ids) throws IllegalParamsException {
        if(context == null){
            throw new IllegalParamsException("context", "不能为空");
        }
        if(ids == null || ids.size() <= 0){
            throw new IllegalParamsException("ids", "不能为空");
        }
            Intent intent = new Intent(context, DownloadService.class);
            intent.putExtra("action", DownloadOrder.ACTION_PAUSE_IDS);
            intent.putExtra("ids", ids.toArray());
            context.startService(intent);
    }


    /**
     * 批量暂停下载，根据组
     *
     * @param context
     * @param group
     * @throws IllegalParamsException di参数不合法
     */
    public static void pauseDownloadForGroup(Context context, String group) throws IllegalParamsException {
        if(context == null){
            throw new IllegalParamsException("context", "不能为空");
        }
        if(group == null || group.equals("")){
            throw new IllegalParamsException("group", "不能为空");
        }
        Intent intent = new Intent(context, DownloadService.class);
        intent.putExtra("action", DownloadOrder.ACTION_PAUSE_GROUP);
        intent.putExtra("group", group);
        context.startService(intent);
    }


    /**
     * 恢复下载
     *
     * @param context
     * @param id      下载唯一id
     * @throws DownloadNotExistException 下载任务不存在或id不正确
     * @throws IllegalParamsException di参数不合法
     */
    public static void resumeDownload(Context context, int id) throws DownloadNotExistException, IllegalParamsException {
        if(context == null){
            throw new IllegalParamsException("context", "不能为空");
        }
        if(id <= 0){
            throw new IllegalParamsException("id", "不能为负或0");
        }
        if (DownloadList.has(id)) {
            Intent intent = new Intent(context, DownloadService.class);
            intent.putExtra("action", DownloadOrder.ACTION_RESUME);
            intent.putExtra("id", id);
            context.startService(intent);
        } else {
            throw new DownloadNotExistException();
        }
    }


    /**
     * 批量恢复下载，根据id列表
     *
     * @param context
     * @param ids
     * @throws IllegalParamsException di参数不合法
     */
    public static void resumeDownloadForIds(Context context, List<Integer> ids) throws IllegalParamsException {
        if(context == null){
            throw new IllegalParamsException("context", "不能为空");
        }
        if(ids == null || ids.size() <= 0){
            throw new IllegalParamsException("ids", "不能为空");
        }
        Intent intent = new Intent(context, DownloadService.class);
        intent.putExtra("action", DownloadOrder.ACTION_RESUME_IDS);
        intent.putExtra("ids", ids.toArray());
        context.startService(intent);
    }

    /**
     * 批量恢复下载，根据组
     *
     * @param context
     * @param group
     * @throws IllegalParamsException di参数不合法
     */
    public static void resumeDownloadForGroup(Context context, String group) throws IllegalParamsException {
        if(context == null){
            throw new IllegalParamsException("context", "不能为空");
        }
        if(group == null || group.equals("")){
            throw new IllegalParamsException("group", "不能为空");
        }
        Intent intent = new Intent(context, DownloadService.class);
        intent.putExtra("action", DownloadOrder.ACTION_RESUME_GROUP);
        intent.putExtra("group", group);
        context.startService(intent);
    }


    /**
     * 取消下载/删除已下载文件
     *
     * @param context
     * @param id
     * @throws DownloadNotExistException 下载任务不存在或id不正确
     * @throws IllegalParamsException di参数不合法
     */
    public static void cancelDownload(Context context, int id) throws DownloadNotExistException, IllegalParamsException {
        if(context == null){
            throw new IllegalParamsException("context", "不能为空");
        }
        if(id <= 0){
            throw new IllegalParamsException("id", "不能为负或0");
        }
        if (DownloadList.has(id)) {
            Intent intent = new Intent(context, DownloadService.class);
            intent.putExtra("action", DownloadOrder.ACTION_CANCEL);
            intent.putExtra("id", id);
            context.startService(intent);
        } else {
            throw new DownloadNotExistException();
        }
    }


    /**
     * 批量取消/删除下载，根据id列表
     *
     * @param context
     * @param ids
     * @throws IllegalParamsException di参数不合法
     */
    public static void cancelDownloadForIds(Context context, List<Integer> ids) throws IllegalParamsException {
        if(context == null){
            throw new IllegalParamsException("context", "不能为空");
        }
        if(ids == null || ids.size() <= 0){
            throw new IllegalParamsException("ids", "不能为空");
        }
        Intent intent = new Intent(context, DownloadService.class);
        intent.putExtra("action", DownloadOrder.ACTION_CANCEL_IDS);
        intent.putExtra("ids", ids.toArray(new Integer[ids.size()]));
        context.startService(intent);
    }


    /**
     * 批量取消/删除下载，根据组和是否已完成
     *
     * @param context
     * @param group
     * @param isDowned
     * @throws IllegalParamsException di参数不合法
     */
    public static void cancelDownloadForGroup(Context context, String group, boolean isDowned) throws IllegalParamsException {
        if(context == null){
            throw new IllegalParamsException("context", "不能为空");
        }
        if(group == null || group.equals("")){
            throw new IllegalParamsException("group", "不能为空");
        }
        Intent intent = new Intent(context, DownloadService.class);
        intent.putExtra("action", DownloadOrder.ACTION_CANCEL_GROUP);
        intent.putExtra("group", group);
        intent.putExtra("isDowned", isDowned ? DownloadOrder.STATE_SUCCESS : 0);
        context.startService(intent);
    }


    /**
     * 获取数据库中的下载列表。
     * 用于程序启动时
     *
     * @param group    下载组
     * @param isDowned 是否已下载完
     * @return
     */
    public static List<DownloadInfo> getDownloadList(String group, boolean isDowned) {
        return DownloadList.getDownloadList(group, isDowned);
    }


    /**
     * 添加下载任务
     *
     * @param context
     * @param di      下载信息
     * @throws IllegalParamsException di参数不合法
     */
    public static void downloadEvent(Context context, DownloadInfo di, DownloaderListener listener) throws IllegalParamsException {
        di.checkIllegal();
        //检查列表中是否已存在
        if (DownloadList.has(di.getId())) {
            throw new IllegalParamsException("di", "任务已经存在");
        }
        if(context == null){
            throw new IllegalParamsException("context", "不能为空");
        }
        Downloader down = new Downloader(di, listener);
        DownloadList.add(down);
        Intent intent = new Intent(context, DownloadService.class);
        intent.putExtra("action", DownloadOrder.ACTION_ADD);
        intent.putExtra("id", di.getId());
        context.startService(intent);
    }

    /**
     * 停止后台的下载服务
     * @param context
     */
    public static void stopDownService(Context context){
        Intent intent = new Intent(context, DownloadService.class);
        context.stopService(intent);
    }


    /**
     * 设置下载监听器
     * @param id
     * @param listener
     * @throws DownloadNotExistException
     */
    public static void setDownloadListener(int id, DownloaderListener listener) throws DownloadNotExistException {
        if (DownloadList.has(id)) {
            Downloader down = DownloadList.get(id);
            down.downloadListener = listener;
        } else {
            throw new DownloadNotExistException();
        }
    }

    /**
     * 监听下载的后台服务
     * @param listener
     */
    public static void setDownServiceListener(DownServiceListener listener){
        serviceListener = listener;
    }


}
