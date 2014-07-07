package com.huichongzi.download_android.download;

import java.util.Hashtable;
import java.util.Iterator;

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
        DownloadDB.add(down.di);
        refresh();
    }

    protected static boolean has(String id){
        return downloadMap.containsKey(id);
    }

    protected static void remove(String id){
        if(downloadMap.contains(id)){
            Downloader down = downloadMap.get(id);
            if(down != null && down.di != null){
                down.di.setStateAndRefresh(DownloadOrder.STATE_STOP);
            }
            downloadMap.remove(id);
        }
        DownloadDB.delete(id);
        refresh();
    }

    protected static Downloader get(String id){
        return downloadMap.get(id);
    }

    protected static int getSize(){
        return downloadMap.size();
    }


    /**
     * 刷新下载列表，未到下载上限且有等待下载时自动下载
     */
    protected static void refresh(){
        int count = 0;
        for (Iterator<Downloader> iter = downloadMap.values().iterator(); iter.hasNext(); ) {
            Downloader down = iter.next();
            if (down.di.getState() == DownloadOrder.STATE_DOWNING) {
                count++;
            }
            else if(count < Max_Allow_Download && down.di.getState() == DownloadOrder.STATE_WAIT){
                down.tryStorage();
                count++;
            }
            DownloadDB.update(down.di);
        }
    }


    /**
     * 下载中的任务全部变为等待。
     * sd卡卸载、网络中断时
     */
    protected static void waitAll(){
        for (Iterator<Downloader> iter = downloadMap.values().iterator(); iter.hasNext(); ) {
            Downloader down = iter.next();
            if (down.di.getState() == DownloadOrder.STATE_DOWNING) {
                down.di.setState(DownloadOrder.STATE_WAIT);
            }
        }
    }




}
