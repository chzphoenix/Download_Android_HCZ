package com.huichongzi.download_android.download;

/**
 * 下载事件监听接口
 * Created by cuihz on 2014/7/3.
 */
public interface DownloaderListener {

    /**
     * 重复下载
     */
    public void onDownloadRepeat(String msg);


    /**
     * 连接失败
     * @param msg
     */
    public void onConnectFailed(String msg);

    /**
     * 创建文件失败，包括sd卡不存在，空间不足等
     * @param msg
     */
    public void onCreateFailed(String msg);


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
     * 下载失败
     */
    public void onDownloadFailed();


    /**
     * 校验失败，包括md5、文件大小等
     * @param msg
     */
    public void onCheckFailed(String msg);

    /**
     * 下载成功
     */
    public void onDownloadSuccess(String md5);



}
