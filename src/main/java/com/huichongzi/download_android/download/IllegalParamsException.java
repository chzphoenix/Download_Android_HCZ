package com.huichongzi.download_android.download;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by cuihz on 2014/7/10.
 */
public class IllegalParamsException extends  BaseException{

    private static final Logger logger = LoggerFactory.getLogger(IllegalParamsException.class);
    public IllegalParamsException(String param, String msg){
        super("'" + param + "' param illegal：" + msg);
    }
    public IllegalParamsException(String msg){
        super(msg);
    }
    public IllegalParamsException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }
    public IllegalParamsException(Throwable throwable) {
        super(throwable);
    }

}
