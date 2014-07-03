package com.huichongzi.download_android.download;

public interface DownloaderListener {
	/**
	 * 下载成功
	 */
	 public void onDownloadSuccess(String md5);


    /**
     * 准备下载失败，包括sd卡不存在，空间不足等
     * @param errCode
     * @param msg
     */
    public void onProFailed(int errCode, String msg);
	
	 /**
	 * 下载失败
	 */
	public void onDownloadFailed();

    /**
     * 下载完成后校验失败，包括md5、文件大小等
     * @param errCode
     * @param msg
     */
    public void onCheckFailed(int errCode, String msg);

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
     * 文件已经下载过
     */
    public void onDownloaded();
	

}
