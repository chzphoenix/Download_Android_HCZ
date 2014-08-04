package com.huichongzi.download_android.download;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;

import android.content.Context;
import android.os.Handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 单个线程下载类
 * 下载文件的一部分
 * Created by cuihz on 2014/7/3.
 */
class SingleDownloadThread extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(SingleDownloadThread.class);
    private static final int BUFFER_SIZE = 1024 * 10;
    private DownloadInfo di;
    //线程id，用来区分多线程中的每个线程
    private int threadId;
    private File file;
    private long startPosition;
    private long endPosition;
    private long curPosition;
    private long downloadSize = 0;
    private Downloader downloader;
    private static final int CONN_TIMEOUT = 1000 * 40;
    private static final int READ_TIMEOUT = 1000 * 60;
    // 下载中停止一段时间，以防过于耗费资源
    private static final int SLEEP_TIME = 50;
    private Context context;



    protected SingleDownloadThread(Context context, int threadId, DownloadInfo di, File file, long startPosition,
                                   long endPosition, Downloader downloader) {
        this.context = context;
        this.threadId = threadId;
        this.di = di;
        this.file = file;
        this.startPosition = startPosition;
        this.curPosition = startPosition;
        this.endPosition = endPosition;
        this.downloader = downloader;
    }

    @Override
    public void run() {
        BufferedInputStream bis = null;
        RandomAccessFile fos = null;
        byte[] buf = new byte[BUFFER_SIZE];
        HttpURLConnection con = null;
        try {
            URL url = new URL(di.getUrl());
            con = (HttpURLConnection) url.openConnection();
            con.setConnectTimeout(CONN_TIMEOUT);
            con.setReadTimeout(READ_TIMEOUT);
            con.setAllowUserInteraction(true);

            // 设置当前线程下载的起点，终点
            String property = "bytes=" + startPosition + "-" + endPosition;
            con.setRequestProperty("RANGE", property);

            // 使用java中的RandomAccessFile对文件进行随机读写操作
            fos = new RandomAccessFile(file, "rw");

            // 设置开始写文件的位置
            fos.seek(startPosition);
            InputStream is = con.getInputStream();
            bis = new BufferedInputStream(is);

            logger.debug("{} thread {} -> {} , {}", di.getName(), threadId, curPosition, endPosition);
            // 开始循环以流的形式读写文件
            while (curPosition < endPosition && di.getState() == DownloadOrder.STATE_DOWNING) {
                try {
                    int len = bis.read(buf, 0, BUFFER_SIZE);
                    if (len == -1) {
                        break;
                    }
                    fos.write(buf, 0, len);
                    curPosition = curPosition + len;
                    if (curPosition > endPosition) {
                        downloadSize += len - (curPosition - endPosition) + 1;
                        curPosition = endPosition;
                    } else {
                        downloadSize += len;
                    }
                    Thread.sleep(SLEEP_TIME);
                } catch (InterruptedException e) {
                    logger.error("{} thread {} -> download Interrupt error:{}", di.getName(), threadId, e.getMessage());
                    downloader.changeState(DownloadOrder.STATE_FAILED, DownloadOrder.FAILED_DOWNLOADING, e.getMessage(), true, true);
                }
            }
            logger.info("{} thread {} -> Total downloadsize: {}", di.getName(), threadId, downloadSize);
            bis.close();
            fos.close();
        } catch (SocketTimeoutException e) {
            logger.error("{} thread {} -> download SocketTimeoutException:{}", di.getName(), threadId, e.getMessage());
            downloader.changeState(DownloadOrder.STATE_FAILED, DownloadOrder.FAILED_DOWNLOADING, e.getMessage(), true, true);
        } catch (IOException e) {
            if(context != null && (!DownloadUtils.isNetAlive(context) || !DownloadUtils.isSdcardMount())){
                downloader.changeState(DownloadOrder.STATE_WAIT_RECONN, 0, null, false, true);
            }
            else{
                logger.error("{} thread {} -> download IOException:{}", di.getName(), threadId, e.getMessage());
                downloader.changeState(DownloadOrder.STATE_FAILED, DownloadOrder.FAILED_DOWNLOADING, e.getMessage(), true, true);
            }
        } catch (Exception e) {
            logger.error("{} thread {} -> download error:{}", di.getName(), threadId, e.getMessage());
            downloader.changeState(DownloadOrder.STATE_FAILED, DownloadOrder.FAILED_DOWNLOADING, e.getMessage(), true, true);
        }
    }



    /**
     * 获取本线程已下载的大小
     * @return long
     */
    protected long getDownloadSize() {
        return downloadSize;
    }

    /**
     * 保存本线程的下载进度
     * @param conf
     */
    protected void saveProgress(UnFinishedConfFile conf) {
        conf.put(threadId + "start", curPosition + "");
        conf.put(threadId + "end", endPosition + "");
    }
}
