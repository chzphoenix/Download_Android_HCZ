package com.huichongzi.download_android.download;

import com.huichongzi.download_android.utils.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;


/**
 * 存储检查操作线程类
 * 下载前检查sd卡是否存在，空间是否足够等
 * Created by cuihz on 2014/7/3.
 */
class StorageHandleTask extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(StorageHandleTask.class);
    //监听存储检查线程，用于各种事件回调
    private StorageListener storageListener = null;
    private DownloadInfo di;
    //临时文件的扩展名
    protected static final String Unfinished_Sign = ".temp";
    // sd卡最少保留空间
    final static int miniSdSize = 2 * 1024 * 1024;


    /**
     * 构造函数
     *
     * @param di       下载信息
     * @param listener 存储检查事件监听回调
     */
    protected StorageHandleTask(DownloadInfo di, StorageListener listener) {
        storageListener = listener;
        this.di = di;
    }


    /**
     * 开始检查空间和下载情况的线程，并通过事件返回检查情况
     */
    @Override
    public void run() {
        logger.debug("storage download thread start");
        storageListener.onStartPre();
        // 首先检查是否下载完成
        if (isDownloadExist(di.getPath())) {
            logger.debug("storage download thread end");
            return;
        }
        // 检查路径并创建文件
        if (!createStorageDir()) {
            // 建立目录失败
            logger.debug("storage download thread end");
            return;
        }
        logger.debug("storage download thread end");
    }


    /**
     * 检查是否文件下载一半；sd卡是否存在、空间是否足够等，并创建文件。
     *
     * @return boolean
     */
    private boolean createStorageDir() {
        logger.debug("{} begin createStorageDir,downloadUrl= {}", di.getName(), di.getUrl());
        long availableSize = 0;
        long softSize = 0;


        //通过url获取文件大小，并与传入的downloadinfo中的文件大小比较
        try {
            softSize = FileUtils.getFileSizeFromUrl(di.getUrl());
            if ((di.getCheckMode() & DownloadOrder.CHECKMODE_SIZE_START) == 0) {
                di.setTotalSize(softSize);
            } else if (softSize != di.getTotalSize() || di.getTotalSize() <= 0) {
                storageListener.onFileSizeError();
                return false;
            }
        } catch (Exception e) {
            storageListener.onDownloadUrlConnectError(di.getUrl() + "连接下载地址错误" + e.getMessage());
            return false;
        }


        //如果文件为下载一半，不必创建存储空间
        File tempFile = new File(di.getPath() + Unfinished_Sign);
        if (tempFile.exists() && tempFile.isFile()) {
            // 下载中但没有完成
            if (storageListener != null) {
                storageListener.onDownloadNotFinished(di.getPath()
                        + Unfinished_Sign);
            }
            return true;
        }


        // 存储空间是否可用、足够
        try {
            logger.debug("{} sdcard exist", di.getName());
            File file = new File(di.getPath());
            file.getParentFile().mkdirs();
            availableSize = FileUtils.getAvailableSize(file.getParentFile().getPath());
            logger.debug("{} sd availaSize= {}, softsize=", di.getName(), availableSize, softSize);
            // 如果sd卡空间足够
            if (availableSize > softSize + miniSdSize) {
                // 正常下载
                try {
                    StorageHandleTask.createTmpFile(di);
                } catch (IOException e) {
                    storageListener.onCreateFailed(e.getMessage());
                    return false;
                }
                storageListener.onNotDownload(di.getPath());
            } else {
                storageListener.onStorageNotEnough(softSize, availableSize);
            }
        }
        catch (Exception e) {
            logger.error("", e);
            if (e.getMessage() != null && e.getMessage().contains("Invalid path")) {
                storageListener.onStorageNotMount(di.getPath());
            }
        }
        return true;
    }


    /**
     * 检查是否下载完成
     *
     * @param downloadDir 下载路径
     * @return boolean
     */
    private boolean isDownloadExist(String downloadDir) {
        File file = new File(downloadDir);
        if (file.exists() && file.isFile()) {
            // 下载完成
            if (storageListener != null) {
                storageListener.onAlreadyDownload(downloadDir);
            }
            return true;
        } else {
            return false;
        }
    }



    /**
     * 创建下载临时文件
     * @param di 下载文件信息
     * @return
     */
    protected static void createTmpFile(DownloadInfo di) throws IOException {
        File file = new File(di.getPath() + StorageHandleTask.Unfinished_Sign);
        // 创建下载目录
        if (!file.exists()) {
            file.getParentFile().mkdirs();
        }
        RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
        randomAccessFile.seek(di.getTotalSize() - 1);
        randomAccessFile.write(0);
        randomAccessFile.close();
    }


    /**
     * 删除文件。包括临时文件和配置文件
     * 用于下载出错，下载取消等
     * @param path
     */
    protected static void removeFile(String path) {
        File file = new File(path);
        File tempFile = new File(path + StorageHandleTask.Unfinished_Sign);
        // 下载完成
        if (file.exists()) {
            file.delete();
        }
        // 下载但没有完成
        if (tempFile.exists()) {
            tempFile.delete();
        }
        new UnFinishedConfFile(file.getAbsolutePath()).delete();
    }


}
