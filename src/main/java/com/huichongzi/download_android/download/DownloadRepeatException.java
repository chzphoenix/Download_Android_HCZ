package com.huichongzi.download_android.download;

/**
 * 重复下载异常类
 * Created by cuihz on 2014/7/3.
 */
public class DownloadRepeatException extends Exception{
    public DownloadRepeatException(String msg){
        super(msg);
    }
}
