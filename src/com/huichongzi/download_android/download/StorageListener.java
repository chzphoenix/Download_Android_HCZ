package com.huichongzi.download_android.download;

/**
 * 存储事件监听
 * Created by cuihz on 2014/7/3.
 */
interface StorageListener {

	/**
	 * 当该应用下载存在时触发
	 * @param path 该应用存储的位置
	 */
	void onAlreadyDownload(String path);
	
	/**
	 * 当该应用没有下载过触发
	 * @param path 该应用存储的位置
	 */
	void onNotDownload(String path);
	
	/**
	 * 当该应用下载没有完成后触发
	 * @param path 该应用存储的位置
	 */
	void onDownloadNotFinished(String path);



    /**
     * 当空间不足时触发
     * @param softSize 软件大小
     * @param avilableSize 可用空间
     */
    void onStorageNotEnough(long softSize, long avilableSize);


    /**
	 * 当该应用没有权限后触发，sd卡不存在
	 * @param path 该应用存储的位置
	 */
	void onStorageNotMount(String path);
	
	/**
	 * 下载地址连接错误
	 * @param msg 错误信息
	 */
	void onDownloadPathConnectError(String msg);

    /**
     * 下载前校验文件大小失败
     */
    void onFileSizeError();

    /**
     * 创建下载文件失败
     */
    void onCreateFailed();
}
