package com.huichongzi.download_android.download;

import android.content.Context;

/**
 * 下载后台服务的监听器
 * Created by cuihz on 2014/7/11.
 */
public interface DownServiceListener {
    /**
     * 服务创建
     */
    public void onServiceCreate(Context context);


    /**
     * 服务开始
     */
    public void onServiceStart(Context context);

    /**
     * 服务停止
     */
    public void onServiceDestroy(Context context);
}
