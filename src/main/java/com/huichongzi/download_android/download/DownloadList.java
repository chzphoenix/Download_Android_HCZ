package com.huichongzi.download_android.download;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.File;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

/**
 * 下载列表类
 * Created by cuihz on 2014/7/4.
 */
class DownloadList {
    private static final Logger logger = LoggerFactory.getLogger(DownloadList.class);
    // 最大允许启动下载的个数
    protected static final int Max_Allow_Download = 3;
    // 当前下载的存储表
    protected static Hashtable<Integer, Downloader> downloadMap = new Hashtable<Integer, Downloader>();


    protected static void add(Downloader down){
        downloadMap.put(down.di.getId(), down);
        DownloadDB.add(down.di);
    }

    protected static boolean has(int id){
        if(downloadMap.containsKey(id)){
            Downloader down = DownloadList.get(id);
            //清除无效的部分
            if(down == null || down.di == null || down.di.getState() == DownloadOrder.STATE_STOP){
                downloadMap.remove(id);
                return false;
            }
            //清除文件被删除的无效di
            if(down.di.getState() == DownloadOrder.STATE_SUCCESS){
                File file = new File(down.di.getPath());
                if(!file.exists() || !file.isFile()){
                    downloadMap.remove(id);
                    return false;
                }
            }
            return true;
        }
        else{
            return false;
        }
    }

    protected static void remove(int id){
        if(downloadMap.contains(id)){
            Downloader down = downloadMap.get(id);
            if(down != null && down.di != null){
                down.di.setStateAndRefresh(DownloadOrder.STATE_STOP);
            }
            downloadMap.remove(id);
        }
        DownloadDB.delete(id);
        refresh(0);
    }

    protected static Downloader get(int id){
        return downloadMap.get(id);
    }




    protected static List<DownloadInfo> getDownloadList(String group, boolean isDowned) {
        List<DownloadInfo> list = DownloadDB.getList(group, isDowned);
        if(isDowned){
            //如果是下载完成的，需要检查文件是否已被删除
            for(DownloadInfo di : list){
                File file = new File(di.getPath());
                if(!file.exists() || !file.isFile()){
                    list.remove(di);
                    DownloadDB.delete(di.getId());
                }
            }
        }
        return list;
    }


    /**
     * 刷新下载列表，未到下载上限且有等待下载时自动下载
     * @param mode 重连模式.如果不为0，则只启动该模式的重连任务
     */
    protected static void refresh(int mode){
        int count = 0;
        for (Iterator<Downloader> iter = downloadMap.values().iterator(); iter.hasNext(); ) {
            Downloader down = iter.next();
            if (down.di.getState() == DownloadOrder.STATE_DOWNING && !down.di.isUnlimite()) {
                count++;
            }
            else if(down.di.isUnlimite() || count < Max_Allow_Download){              //如果不受限直接继续，受限则检查最大下载
                if(((down.di.getReconnMode() & mode) != 0 && down.di.getState() == DownloadOrder.STATE_WAIT_RECONN)   //断网或sd卡卸载重连情况
                        || (mode == 0 && down.di.getState() == DownloadOrder.STATE_WAIT_DOWN)){                       //刷新列表的情况
                    down.tryStorage();
                    down.di.setState(DownloadOrder.STATE_DOWNING);
                    if(!down.di.isUnlimite()){
                        count++;
                    }
                }
            }
            DownloadDB.update(down.di);
        }
        logger.debug("download task count: {}", count);
    }


    /**
     * 下载中的任务全部变为等待重连。
     * sd卡卸载、网络中断时
     */
    protected static void waitAllForReconn(){
        for (Iterator<Downloader> iter = downloadMap.values().iterator(); iter.hasNext(); ) {
            Downloader down = iter.next();
            if (down.di.getState() == DownloadOrder.STATE_DOWNING) {
                down.di.setState(DownloadOrder.STATE_WAIT_RECONN);
            }
        }
    }




}
