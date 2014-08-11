package com.huichongzi.download_android.download;

/**
 * 下载任务不存在异常
 * Created by cuihz on 2014/7/3.
 */
public class DownloadNotExistException extends BaseException{
    public DownloadNotExistException(){
        super("the download is not exist!");
    }
    public DownloadNotExistException(String msg){
        super(msg);
    }
    public DownloadNotExistException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }
    public DownloadNotExistException(Throwable throwable) {
        super(throwable);
    }
}
