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
import android.util.Log;

/**
 * 单个线程下载类
 * 下载文件的一部分
 * Created by cuihz on 2014/7/3.
 */
class SingleDownloadThread extends Thread {

    private static final int BUFFER_SIZE = 1024 * 10;
    private URL url;
    //线程id，用来区分多线程中的每个线程
    private int threadId;
    private File file;
    private long startPosition;
    private long endPosition;
    private long curPosition;
    // 用于标识当前线程是否下载完成
    private boolean finished = false;
    private long downloadSize = 0;
    private DownloadTask.TaskState taskState;
    private DownloaderListener downloadListener;
    private static final int connTimeout = 1000 * 40;
    private static final int readTimeout = 1000 * 60;
    // 下载中停止一段时间，以防过于耗费资源
    private static final int Sleep_Time = 30;
    private Context mContext = null;


    protected SingleDownloadThread(int threadId, URL url, File file, long startPosition,
                                   long endPosition, DownloadTask.TaskState taskState,
                                   DownloaderListener downloadListener, Context context) {
        this.threadId = threadId;
        this.url = url;
        this.file = file;
        this.startPosition = startPosition;
        this.curPosition = startPosition;
        this.endPosition = endPosition;
        this.taskState = taskState;
        this.downloadListener = downloadListener;
        mContext = context;
        // 添加判断，以防下载完成但用户取消导致文件名未更改
        if (startPosition > endPosition && endPosition > 0) {
            Log.e(this.toString(), "Start postition is larger than end position, set finished to true");
            finished = true;
        }
    }

    @Override
    public void run() {
        BufferedInputStream bis = null;
        RandomAccessFile fos = null;
        byte[] buf = new byte[BUFFER_SIZE];
        HttpURLConnection con = null;
        try {
            con = (HttpURLConnection) url.openConnection();
            con.setConnectTimeout(connTimeout);
            con.setReadTimeout(readTimeout);
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

            // 开始循环以流的形式读写文件
            while (curPosition < endPosition && !taskState.isStop) {
                try {
                    //如果下载暂停，则休眠1秒后循环
                    if (taskState.isPause) {
                        Thread.sleep(1000);
                        continue;
                    }
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

                    Thread.sleep(Sleep_Time);
                } catch (InterruptedException e) {
                    Log.e("SingleDownloadThread run",
                            "download Interrupt error:" + e.getMessage());
                    downloadListener.onDownloadFailed();
                }
            }
            Log.e(toString(), "Total downloadsize: " + downloadSize);
            // 下载完成设为true
            this.finished = true;
            bis.close();
            fos.close();
        } catch (SocketTimeoutException e) {
            Log.e("SingleDownloadThread run", "download SocketTimeoutException ");
            downloadListener.onDownloadFailed();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("SingleDownloadThread run", "download IOException " + e.getMessage());
            downloadListener.onDownloadFailed();
        } catch (Exception e) {
            Log.e("SingleDownloadThread run", "download error " + e.getMessage());
            downloadListener.onDownloadFailed();
            e.printStackTrace();
        }
    }

    /**
     * 判断本线程是否结束
     * @return
     */
    protected boolean isFinished() {
        return finished;
    }

    /**
     * 获取本线程已下载的大小
     * @return
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
