package com.huichongzi.download_android.download;



/**
 * Created by cuihz on 2014/8/11.
 */
public class BaseException extends Exception {
    public BaseException() {
        super();
    }
    public BaseException(String detailMessage) {
        super(detailMessage);
    }
    public BaseException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }
    public BaseException(Throwable throwable) {
        super(throwable);
    }
}
