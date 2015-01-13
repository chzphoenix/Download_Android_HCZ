package com.huichongzi.download_android.utils;


import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.os.StatFs;
import android.text.TextUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by cuihz on 2014/7/25.
 */
public class FileUtils {

    private static final Logger logger = LoggerFactory.getLogger(FileUtils.class);




    /**
     * 获取指定文件的MD5值
     *
     * @param file 需要获取MD5值的文件
     * @return 以字符串形式返回的MD5值
     * @throws java.security.NoSuchAlgorithmException
     * @throws java.io.IOException
     */
    public static String getFileMD5(File file) throws NoSuchAlgorithmException, IOException {
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
     *
     * @param b 转换的数据
     * @return 16进制的字符串
     */
    public static String toHexString(byte[] b) {
        char[] hexChar = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        StringBuilder sb = new StringBuilder(b.length * 2);
        for (int i = 0; i < b.length; i++) {
            sb.append(hexChar[(b[i] & 0xf0) >>> 4]);
            sb.append(hexChar[b[i] & 0x0f]);
        }
        return sb.toString();
    }


    /**
     * 获取本路径的可用空间
     *
     * @param path long
     * @return
     */
    public static long getAvailableSize(String path) throws Exception {
        StatFs statfs = new StatFs(path);
        // 获取block的SIZE
        long blocSize = statfs.getBlockSize();
        // 己使用的Block的数量
        long availaBlock = statfs.getAvailableBlocks();
        return availaBlock * blocSize;
    }

    /**
     * delete dir or files
     * @param file
     */
    public static void delete(File file) {
        if (file == null || !file.exists())
            return ;
        if (file.isDirectory()) {
            for (File subfile : file.listFiles()) {
                delete(subfile);
            }
        } else {
            file.delete();
        }
    }

    /**
     * 判断文件是否存在
     *
     * @param path 文件路径
     * @return
     */
    public static boolean isFileExist(String path){
        File file = new File(path);
        return file.exists();
    }

    /**
     * 复制文件
     *
     * @param is 输入流
     * @param os 输出流
     * @throws java.io.IOException
     */
    public static void copyFile(InputStream is, OutputStream os) throws IOException {
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        byte[] buffer = new byte[512];
        try {
            bis = new BufferedInputStream(is, 8192);
            bos = new BufferedOutputStream(os, 8192);
            int len;
            while ((len = bis.read(buffer)) != -1) {
                bos.write(buffer, 0, len);
            }
            bos.flush();
        } catch (IOException e) {
            logger.warn(e.toString(), e);
            throw e;
        } finally {
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException e) {
                    logger.warn(e.toString(), e);
                }
            }
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {
                    logger.warn(e.toString(), e);
                }
            }
        }
    }


    /**
     * 得到文件的大小
     *
     * @param path 文件路径
     * @return 文件大小
     */
    public static long getFileSize(String path) {
        File filedir = new File(path);
        long totoalLength = 0;
        File[] files = filedir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    totoalLength = totoalLength + getFileSize(file.getAbsolutePath());
                } else {
                    totoalLength += file.length();
                }
            }
        }
        return totoalLength;
    }


    /**
     * 通过url获取下载文件大小
     *
     * @param urlStr 下载url
     * @return long
     * @throws Exception
     */
    public static long getFileSizeFromUrl(String urlStr) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        conn.setRequestMethod("HEAD");
        conn.setRequestProperty("Connection", "close");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);
        conn.setDoInput(true);
        conn.connect();
        int downFileCode = conn.getResponseCode();
        if (!(downFileCode >= 200 && downFileCode < 300)) {
            throw new Exception(downFileCode + "");
        }
        // 获取下载文件的总大小
        int fileSize = conn.getContentLength();
        return fileSize;
    }

}
