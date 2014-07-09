package com.huichongzi.download_android.download;

/**
 * 下载任务不存在异常
 * Created by cuihz on 2014/7/3.
 */
public class DownloadNotExistException extends Exception{
    public DownloadNotExistException(){
        super("下载任务不存在，请重建下载任务");
    }
}
