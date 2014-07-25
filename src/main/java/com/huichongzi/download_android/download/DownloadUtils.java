package com.huichongzi.download_android.download;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.os.Message;
import android.os.StatFs;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 工具类
 * Created by cuihz on 2014/7/2.
 */
class DownloadUtils {


    /**
     * 网络是否有效
     * @param context
     * @return boolean
     */
    protected static boolean isNetAlive(Context context){
        ConnectivityManager conn = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = conn.getActiveNetworkInfo();
        if(info != null && info.isAvailable()){
            return true;
        }
        else{
            return false;
        }
    }


    /**
     * 检查sd卡是否存在
     * @return boolean
     */
    protected static boolean isSdcardMount(){
        return Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);
    }


    /**
     * 创建下载临时文件
     * @param di 下载文件信息
     * @return
     */
    protected static void createTmpFile(DownloadInfo di) throws IOException{
        File file = new File(di.getPath() + StorageHandleTask.Unfinished_Sign);
        // 创建下载目录
        if (!file.exists()) {
            file.getParentFile().mkdirs();
        }
        FileOutputStream fos = new FileOutputStream(file);
        int bufferSize = 4 * 1024 * 1024;
        long fileSize = di.getSize();
        int num = (int)(fileSize / bufferSize);
        int ext = (int)(fileSize % bufferSize);
        byte[] buffer;
        for(int i = 0; i < num; i++){
            buffer = new byte[bufferSize];
            fos.write(buffer);
        }
        fos.write(new byte[ext]);
        fos.flush();
        fos.close();
    }


    /**
     * 删除文件。包括临时文件和配置文件
     * 用于下载出错，下载取消等
     * @param path
     */
    protected static void removeFile(String path) {
        File file = new File(path);
        File tempFile = new File(path + StorageHandleTask.Unfinished_Sign);
        // 下载完成
        if (file.exists()) {
            file.deleteOnExit();
            // 下载带但没有完成
        } else if (tempFile.exists()) {
            tempFile.deleteOnExit();
        }
        new UnFinishedConfFile(file.getAbsolutePath()).delete();
    }


    /**
     * 获取指定文件的MD5值
     * @param file 需要获取MD5值的文件
     * @return 以字符串形式返回的MD5值
     * @throws java.security.NoSuchAlgorithmException
     * @throws java.io.IOException
     */
    protected static String getFileMD5(File file) throws NoSuchAlgorithmException, IOException {
        InputStream fis = null;
        MessageDigest md5 = null;
        try {
            fis = new FileInputStream(file);
            md5 = MessageDigest.getInstance("MD5");
            int numRead = 0;
            byte[] buffer = new byte[1024];
            while ((numRead = fis.read(buffer)) > 0) {
                md5.update(buffer, 0, numRead);
            }
        } finally {
            if (fis != null) fis.close();
        }
        return md5 != null ? toHexString(md5.digest()) : "";
    }


    /**
     * 把byte[]数据转换成以16进制的字符串
     * @param b 转换的数据
     * @return 16进制的字符串
     */
    protected static String toHexString(byte[] b) {
        char[] hexChar = { '0', '1', '2', '3', '4', '5', '6', '7','8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
        StringBuilder sb = new StringBuilder(b.length * 2);
        for (int i = 0; i < b.length; i++) {
            sb.append(hexChar[(b[i] & 0xf0) >>> 4]);
            sb.append(hexChar[b[i] & 0x0f]);
        }
        return sb.toString();
    }


    /**
     * 获取本路径的可用空间
     * @param path long
     * @return
     */
    protected static long getAvailableSize(String path){
        StatFs statfs = new StatFs(path);
        // 获取block的SIZE
        long blocSize = statfs.getBlockSize();
        // 己使用的Block的数量
        long availaBlock = statfs.getAvailableBlocks();
        return availaBlock * blocSize;
    }


    /**
     * 通过url获取下载文件大小
     * @param urlStr 下载url
     * @return long
     * @throws Exception
     */
    protected static long getFileSize(String urlStr) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = null;
        conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("HEAD");
        conn.setRequestProperty("Connection", "close");
        int downFileCode = conn.getResponseCode();
        if (downFileCode >= 200 && downFileCode < 300) {
        } else {
            throw new Exception(downFileCode + "");
        }
        // 获取下载文件的总大小
        int fileSize = conn.getContentLength();
        return fileSize;
    }


}
