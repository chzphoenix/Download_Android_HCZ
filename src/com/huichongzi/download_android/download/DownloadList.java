package com.huichongzi.download_android.download;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

/**
 * 下载列表类
 * Created by cuihz on 2014/7/4.
 */
class DownloadList {
    // 最大允许启动下载的个数
    protected static final int Max_Allow_Download = 3;
    // 当前下载的存储表
    protected static Hashtable<String, Downloader> downloadMap = new Hashtable<String, Downloader>();


    protected static void add(Downloader down){
        downloadMap.put(down.di.getId(), down);
    }

    protected static boolean has(String id){
        return downloadMap.containsKey(id);
    }

    protected static void remove(String id){
        if(downloadMap.contains(id)){
            Downloader down = downloadMap.get(id);
            if(down != null && down.di != null){
                down.di.setState(DownloadOrder.STATE_STOP);
            }
            downloadMap.remove(id);
        }
    }

    protected static Downloader getFromMap(String id){
        return downloadMap.get(id);
    }



    protected static int getSize(){
        return downloadMap.size();
    }


    /**
     * 刷新下载列表，未到下载上限切有等待下载时自动下载
     */
    protected static void refresh(){
        
    }


    /**
     * 获取正在下载的文件数
     * @return
     */
    protected static int getDowningTaskSize(){
        int size = 0;
        for (Iterator<Downloader> iter = downloadMap.values().iterator(); iter.hasNext(); ) {
            Downloader down = iter.next();
            if (down.di.getState() == DownloadOrder.STATE_DOWNING) {
                size++;
            }
        }
        return size;
    }

}
