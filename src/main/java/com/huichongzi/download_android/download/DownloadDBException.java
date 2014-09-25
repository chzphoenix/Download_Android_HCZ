package com.huichongzi.download_android.download;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by cuihz on 2014/7/31.
 */
public class DownloadDBException extends BaseException {

    private static final Logger logger = LoggerFactory.getLogger(DownloadDBException.class);
    public DownloadDBException(String msg){
        super(msg);
    }
    public DownloadDBException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }
    public DownloadDBException(Throwable throwable) {
        super(throwable);
    }

}
