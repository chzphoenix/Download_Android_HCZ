package com.huichongzi.download_android.download;

import java.io.File;
import android.content.Context;
import android.os.Environment;
import android.util.Log;

class StorageHandlerTask extends Thread {
	private StorageListener storageListener = null;
	private Context mContext = null;
    private DownloadInfo di;
	protected static final String Unfinished_Sign = ".temp";
	// sd卡最少保留空间
	final static int miniSdSize = 2 * 1024 * 1024;


	protected StorageHandlerTask(Context context, DownloadInfo di, StorageListener listener) {
		storageListener = listener;
		mContext = context;
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
        if (!createStorageDir()) {
            // 建立目录失败
            return;
        }
	}

	private boolean createStorageDir() {
		Log.e("", "begin createStorageDir,downloadUrl=" + di.getUrl());
		long availableSize = 0;
        long softSize = 0;
		try {
			softSize = DownloadUtils.getFileSize(di.getUrl());
            if(softSize != di.getSize() || di.getSize() <= 0){
                storageListener.onFileSizeError();
                return false;
            }
		} catch (Exception e) {
			e.printStackTrace();
			storageListener.onDownloadPathConnectError(di.getUrl() + "连接下载地址错误" + e.getMessage());
			return false;
		}

		// 判断sd卡是否存在
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
				storageListener.onStorageNotEnough(softSize, availableSize);
			}
		}
        else{
            storageListener.onStorageNotMount(di.getPath());
        }
		return true;
	}



	// 检查是否存在，并根据是否下载完成触发不同事件
	private boolean isDownloadExist(String downloadDir) {
		File file = new File(downloadDir);
		File tempFile = new File(downloadDir + Unfinished_Sign);
		if (file.exists()) {
            // 下载完成
			if (storageListener != null) {
				storageListener.onAlreadyDownload(downloadDir);
			}
			return true;
		}
        else if (tempFile.exists()) {
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
