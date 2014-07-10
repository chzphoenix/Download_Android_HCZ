package com.huichongzi.download_android.download;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by cuihz on 2014/7/10.
 */
public class IllegalParamsException extends  Exception{

    private static final Logger logger = LoggerFactory.getLogger(IllegalParamsException.class);
    public IllegalParamsException(String param, String msg){
        super(param + "参数不合法：" + msg);
    }

}
