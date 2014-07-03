package com.huichongzi.download_android.download;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.StatFs;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by cuihz on 2014/7/2.
 */
class DownloadUtils {



    protected static boolean isNetworkAvailable(Context context) {
        if(context != null){
            ConnectivityManager manager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            if (manager != null) {
                NetworkInfo[] info = manager.getAllNetworkInfo();
                if (info != null) {
                    for (int i = 0; i < info.length; i++) {
                        if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }




    // 文件下载出错，删除文件及临时文件
    protected static void removeFile(String path) {
        File file = new File(path);
        File tempFile = new File(path + StorageHandlerTask.Unfinished_Sign);
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


    protected static long getAvailableSize(String path){
        StatFs statfs = new StatFs(path);
        // 获取block的SIZE
        long blocSize = statfs.getBlockSize();
        // 己使用的Block的数量
        long availaBlock = statfs.getAvailableBlocks();
        return availaBlock * blocSize;
    }


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
