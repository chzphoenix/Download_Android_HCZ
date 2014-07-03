package com.huichongzi.download_android.download;

public class DownloadConfig {
	
	// 已经在下载不能下载
	public static final int Is_Download_Repeat = 101;
	// 超过最大下载限制
	public static final int Is_Download_OverFlow = 102;
	// 空间不足
	public static final int Storage_Not_Enough = 103;
	// 没有sd卡
	public static final int Storage_Cannot_Chmod = 104;
	// 下载地址无法连接
	public static final int Url_Connect_Error = 105;
	// 文件大小不符。
	public static final int Is_Size_Error = 201;
	// MD5不符。
	public static final int Is_Md5_Error = 202;


}
