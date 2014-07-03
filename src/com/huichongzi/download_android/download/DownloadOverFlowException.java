package com.huichongzi.download_android.download;

/**
 * 超出最大下载数异常
 * Created by cuihz on 2014/7/3.
 */
public class DownloadOverFlowException extends Exception{
    public DownloadOverFlowException(){
        super("超出最大下载数，请等待");
    }
}
