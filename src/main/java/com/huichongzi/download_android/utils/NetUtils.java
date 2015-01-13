package com.huichongzi.download_android.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by helloqi on 2014/7/16.
 */
public class NetUtils {

    private static final Logger logger = LoggerFactory.getLogger(NetUtils.class);
    public static final String NET_WORK_INVAILABLE = "netInvailable";

    /**
     * @description 检查整个网络连接状态
     * return true  网路OK
     *        false 网路Poor
     */
    public static boolean isNetAvailable(Context context) {
        if (context == null) {
            logger.warn("context is null!");
            throw new IllegalArgumentException();
        }
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            return networkInfo != null && networkInfo.isAvailable();
        } catch (Exception e) {
            logger.error("", e);
            return false;
        }

    }


    /**
     * make true current connect service is wifi
     *
     * @param mContext
     * @return true  网络是wifi  false 不是wifi网络
     */
    public static boolean isWifi(Context mContext) {
        ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
        return  activeNetInfo != null && activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI;
    }

    /**
     * 判断当前网络是否是手机网络
     *
     * @param context
     * @return true  是手机网络 false 不是手机网络
     */
    public static boolean isMobile(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetInfo != null && activeNetInfo.getType() == ConnectivityManager.TYPE_MOBILE;
    }

    /**
     * 检测当前网络是否为Net接入方式
     *
     * @param context
     * @return true是除wap的手机网络，false不是手机网
     */
    public static boolean isMobileNet(Context context) {
        return isMobile(context) && !isMobileWap(context);
    }

    /**
     * 检测当前网络是否为wap接入方式
     *
     * @param context
     * @return true是wap网，false 不是
     */
    public static boolean isMobileWap(Context context) {
        String proxyHost = android.net.Proxy.getDefaultHost();
        return !TextUtils.isEmpty(proxyHost) && isMobile(context);
    }


    private static final String UNKNOW_TYPE = "unknow";
    private static final String WIFI = "wifi";
    private static final String MOBILE_2G = "2g";
    private static final String MOBILE_3G = "3g";
    private static final String MOBILE_4G = "4g";
    private static final String OFFLINE = "offline";

    /**
     * 当前网络类型
     *
     * @return wifi 2g 3g offline unknow
     */
    public static String getNetType(Context context) {
        ConnectivityManager conn = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (conn == null)
            return OFFLINE;
        NetworkInfo info = conn.getActiveNetworkInfo();
        if (info == null)
            return OFFLINE;
        if (info.getType() == ConnectivityManager.TYPE_WIFI || info.getType() == ConnectivityManager.TYPE_WIMAX)
            return WIFI;
        else if (info.getType() == ConnectivityManager.TYPE_MOBILE)
            return getMoblieNetType(context);
        else
            return UNKNOW_TYPE;
    }
    /**
     * 区分移动网络是2G是3G还是4G
     * @return
     */
    private static String getMoblieNetType(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        if (info == null)
            return OFFLINE;
        switch (info.getSubtype()) {
            case TelephonyManager.NETWORK_TYPE_GPRS:
            case TelephonyManager.NETWORK_TYPE_CDMA:
            case TelephonyManager.NETWORK_TYPE_EDGE:
                return MOBILE_2G;
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
            case TelephonyManager.NETWORK_TYPE_HSUPA:
            case TelephonyManager.NETWORK_TYPE_HSDPA:
            case TelephonyManager.NETWORK_TYPE_HSPAP:
            case TelephonyManager.NETWORK_TYPE_HSPA:
            case TelephonyManager.NETWORK_TYPE_UMTS:
                return MOBILE_3G;
            case TelephonyManager.NETWORK_TYPE_LTE:
                return MOBILE_4G;
            default:
                return UNKNOW_TYPE;
        }
    }
}
