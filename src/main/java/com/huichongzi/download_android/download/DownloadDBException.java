package com.huichongzi.download_android.download;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by cuihz on 2014/7/31.
 */
public class DownloadDBException extends Exception {

    private static final Logger logger = LoggerFactory.getLogger(DownloadDBException.class);

    public DownloadDBException(String msg){
        super(msg);
    }

}
