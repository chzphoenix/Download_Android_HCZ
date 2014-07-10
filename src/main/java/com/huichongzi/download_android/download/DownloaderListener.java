package com.huichongzi.download_android.download;


/**
 * 下载事件监听接口
 * Created by cuihz on 2014/7/3.
 */
public interface DownloaderListener{

    /**
     * 准备下载时失败
     * @param code 见DownloadOrder
     * @param msg
     */
    public void onProFailed(int code, String msg);


	/**
	 * 开始下载时调用
	 */
	public void onStartDownload();


    /**
     * 当下载进度改变时
     * @param currentProgress 当前进度
     */
    public void onDownloadProgressChanged(int currentProgress);

    /**
     * 下载中失败
     * @param msg
     */
    public void onDownloadFailed(String msg);


    /**
     * 校验失败，包括md5、文件大小等
     * @param msg
     */
    public void onCheckFailed(String msg);

    /**
     * 下载成功
     */
    public void onDownloadSuccess();



}
