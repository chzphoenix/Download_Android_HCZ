package com.huichongzi.download_android.download;

import java.io.File;
import android.os.Environment;
import android.util.Log;

/**
 * 存储检查操作线程类
 * 下载前检查sd卡是否存在，空间是否足够等
 * Created by cuihz on 2014/7/3.
 */
class StorageHandleTask extends Thread {
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
        // 首先检查是否下载过，如果下载过则不再建立空间
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
     * 检查sd卡是否存在、空间是否足够等，并创建文件。（目前创建目录，打算改为创建满大小的假文件）
     * @return
     */
	private boolean createStorageDir() {
		Log.e("", "begin createStorageDir,downloadUrl=" + di.getUrl());
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
                di.setState(DownloadOrder.STATE_FAILED);
                DownloadList.remove(di.getId());
                return false;
            }
		} catch (Exception e) {
            di.setState(DownloadOrder.STATE_FAILED);
            DownloadList.remove(di.getId());
			storageListener.onDownloadPathConnectError(di.getUrl() + "连接下载地址错误" + e.getMessage());
			return false;
		}

		// 判断sd卡是否存在，存储空间是否足够
		if (isSdPresent()) {
			Log.d("", "sdcard exist");
            File file = new File(di.getPath());
            availableSize = DownloadUtils.getAvailableSize(file.getParentFile().getPath());
			Log.d("createStorageDir", "sd availaSize=" + availableSize
					+ "softsize=" + softSize);
			// 如果sd卡空间足够
			if (availableSize > softSize + miniSdSize) {
                // 正常下载
                mkdir(di.getPath());
                storageListener.onNotDownload(di.getPath());
			}
			else{
                di.setState(DownloadOrder.STATE_FAILED);
                DownloadList.remove(di.getId());
				storageListener.onStorageNotEnough(softSize, availableSize);
			}
		}
        else{
            di.setState(DownloadOrder.STATE_FAILED);
            DownloadList.remove(di.getId());
            storageListener.onStorageNotMount(di.getPath());
        }
		return true;
	}


    /**
     * 检查是否下载过或正在下载
     * @param downloadDir 下载路径
     * @return
     */
	private boolean isDownloadExist(String downloadDir) {
		File file = new File(downloadDir);
		File tempFile = new File(downloadDir + Unfinished_Sign);
		if (file.exists() && file.isFile()) {
            di.setState(DownloadOrder.STATE_SUCCESS);
            // 下载完成
			if (storageListener != null) {
				storageListener.onAlreadyDownload(downloadDir);
			}
			return true;
		}
        else if (tempFile.exists() && tempFile.isFile()) {
            // 下载中但没有完成
			Log.d("checkIsCompleteForOutPackage", downloadDir
					+ "　download size=" + tempFile.length());
			if (storageListener != null) {
				storageListener.onDownloadNotFinished(downloadDir
						+ Unfinished_Sign);
			}
            return true;
		}
        else{
            return false;
        }
	}

	
	

	
	// sd卡是否存在
	private static boolean isSdPresent() {
		return Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED);
	}

	// 建立下载目录
	private boolean mkdir(String dir) {
		File file = new File(dir);
		// 创建下载目录
		if (!file.exists()) {
			return file.mkdirs();
		}
		return true;
	}
}
