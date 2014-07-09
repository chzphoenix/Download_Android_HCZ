package com.huichongzi.download_android.download;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;


/**
 * 存储检查操作线程类
 * 下载前检查sd卡是否存在，空间是否足够等
 * Created by cuihz on 2014/7/3.
 */
class StorageHandleTask extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(Downloader.class);
    //监听存储检查线程，用于各种事件回调
	private StorageListener storageListener = null;
    private DownloadInfo di;
    //临时文件的扩展名
	protected static final String Unfinished_Sign = ".temp";
	// sd卡最少保留空间
	final static int miniSdSize = 2 * 1024 * 1024;


    /**
     * 构造函数
     * @param di 下载信息
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
        // 首先检查是否下载完成
        if (isDownloadExist(di.getPath())) {
            return;
        }
        // 检查路径并创建文件
        if (!createStorageDir()) {
            // 建立目录失败
            return;
        }
	}


    /**
     * 检查是否文件下载一半；sd卡是否存在、空间是否足够等，并创建文件。
     * @return
     */
	private boolean createStorageDir() {
        logger.debug("{} begin createStorageDir,downloadUrl= {}", di.getName(), di.getUrl());
		long availableSize = 0;
        long softSize = 0;

        //通过url获取文件大小，并与传入的downloadinfo中的文件大小比较
		try {
			softSize = DownloadUtils.getFileSize(di.getUrl());
            if((di.getMode() & DownloadOrder.MODE_SIZE_START) == 0){
                di.setSize(softSize);
            }
            else if(softSize != di.getSize() || di.getSize() <= 0){
                storageListener.onFileSizeError();
                di.setStateAndRefresh(DownloadOrder.STATE_FAILED);
                DownloadList.remove(di.getId());
                return false;
            }
		} catch (Exception e) {
            di.setStateAndRefresh(DownloadOrder.STATE_FAILED);
            DownloadList.remove(di.getId());
			storageListener.onDownloadPathConnectError(di.getUrl() + "连接下载地址错误" + e.getMessage());
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




		// 判断sd卡是否存在，存储空间是否足够
        try {
            if (DownloadUtils.isSdcardMount()) {
                logger.debug("{} sdcard exist", di.getName());
                File file = new File(di.getPath());
                availableSize = DownloadUtils.getAvailableSize(file.getParentFile().getPath());
                logger.debug("{} sd availaSize= {}, softsize=", di.getName(), availableSize, softSize);
                // 如果sd卡空间足够
                if (availableSize > softSize + miniSdSize) {
                    // 正常下载
                    DownloadUtils.createTmpFile(di);
                    storageListener.onNotDownload(di.getPath());
                } else {
                    di.setStateAndRefresh(DownloadOrder.STATE_FAILED);
                    DownloadList.remove(di.getId());
                    storageListener.onStorageNotEnough(softSize, availableSize);
                }
            } else {
                di.setStateAndRefresh(DownloadOrder.STATE_FAILED);
                DownloadList.remove(di.getId());
                storageListener.onStorageNotMount(di.getPath());
            }
            return true;
        }
        catch (Exception e){
            e.printStackTrace();
            storageListener.onCreateFailed();
            return false;
        }
	}


    /**
     * 检查是否下载完成
     * @param downloadDir 下载路径
     * @return
     */
	private boolean isDownloadExist(String downloadDir) {
		File file = new File(downloadDir);
		if (file.exists() && file.isFile()) {
            di.setStateAndRefresh(DownloadOrder.STATE_SUCCESS);
            // 下载完成
			if (storageListener != null) {
				storageListener.onAlreadyDownload(downloadDir);
			}
			return true;
		}
        else{
            return false;
        }
	}

	
	



}
