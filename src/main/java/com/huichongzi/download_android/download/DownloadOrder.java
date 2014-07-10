package com.huichongzi.download_android.download;

/**
 * 各种下载指令及状态集
 * Created by cuihz on 2014/7/4.
 */
public class DownloadOrder {
    /** 不做任何校验  */
    public static final int MODE_NONE = 0;
    /** 下载前校验文件大小，校验用户设置的大小是否等于请求url返回的大小  */
    public static final int MODE_SIZE_START = 1;
    /** 下载后校验文件大小，校验下载文件大小是否等于预设定的文件大小  */
    public static final int MODE_SIZE_END = 2;
    /** 下载后校验文件MD5，校验下载文件MD5是否等于预设定的文件MD5  */
    public static final int MODE_MD5_END = 4;


    /** 正在下载  */
    public static final int STATE_DOWNING = 100;
    /** 等待状态，包括下载数已到上限、网络中断、sd卡卸载等情况。当下载文件数小于限制数，将按顺序自动下载等待状态的文件  */
    public static final int STATE_WAIT = 101;
    /** 暂停状态，用户进行暂停操作后的状态，不能自动下载，必须等用户手动恢复下载 */
    public static final int STATE_PAUSE = 102;
    /** 停止状态，用户取消下载，此状态一般不会出现，用于异常情况  */
    public static final int STATE_STOP = 103;
    /** 下载失败。包括下载完成后校验失败，校验失败时会删除相关文件。  */
    public static final int STATE_FAILED = 104;
    /** 下载成功  */
    public static final int STATE_SUCCESS = 105;


    /** 空间不足  */
    public static final int FAILED_STORAGE_NOT_ENOUPH = 10001;
    /** SD卡不存在  */
    public static final int FAILED_SDCARD_UNMOUNT = 10002;
    /** url无法连接  */
    public static final int FAILED_URL_UNCONNECT = 10003;
    /** 创建临时文件失败  */
    public static final int FAILED_CREATE_TMPFILE = 10004;
    /** 下载前验证大小失败  */
    public static final int FAILED_SIZE_ERROR = 10005;




    protected static final int ACTION_ADD = 1001;
    protected static final int ACTION_PAUSE = 1002;
    protected static final int ACTION_RESUME = 1003;
    protected static final int ACTION_CANCEL = 1004;
}
