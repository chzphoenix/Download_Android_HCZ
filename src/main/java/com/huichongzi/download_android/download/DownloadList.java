package com.huichongzi.download_android.download;

import android.content.Context;
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
    protected static final int Max_Allow_Download = 1;
    // 当前下载的存储表
    protected static Hashtable<Integer, Downloader> downloadMap = new Hashtable<Integer, Downloader>();


    protected static void add(Context context, Downloader down){
        downloadMap.put(down.di.getId(), down);
        DownloadDao.save(context, down.di);
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

    protected static void remove(Context context, int id){
        if(downloadMap.contains(id)){
            Downloader down = downloadMap.get(id);
            if(down != null && down.di != null){
                down.changeState(DownloadOrder.STATE_STOP, 0, null, false);
                DownloadUtils.removeFile(down.di.getPath());
            }
            downloadMap.remove(id);
        }
        DownloadDao.delete(context, id);
    }

    protected static Downloader get(int id){
        return downloadMap.get(id);
    }




    protected static List<DownloadInfo> getDownloadList(Context context, String group, boolean isDowned) throws DownloadDBException{
        List<DownloadInfo> list = DownloadDao.getList(context, group, isDowned);
        if(isDowned){
            //如果是下载完成的，需要检查文件是否已被删除
            for(DownloadInfo di : list){
                File file = new File(di.getPath());
                if(!file.exists() || !file.isFile()){
                    list.remove(di);
                    DownloadDao.delete(context, di.getId());
                }
            }
        }
        return list;
    }


    /**
     * 刷新下载列表，未到下载上限且有等待下载时自动下载
     * @param mode 重连模式.如果不为0，则先启动该模式的重连任务;为0，则先启动所有断连任务。再启动其他任务
     */
    protected static void refresh(Context context, int mode){
        if(!DownloadUtils.isNetAlive(context) || !DownloadUtils.isSdcardMount()){
            return;
        }
        int count = 0;
        //先遍历重启断连的任务
        for (Iterator<Downloader> iter = downloadMap.values().iterator(); iter.hasNext(); ) {
            Downloader down = iter.next();
            if (down.di.getState() == DownloadOrder.STATE_DOWNING && !down.di.isUnlimite()) {
                count++;
            }
            else if(down.di.isUnlimite() || count < Max_Allow_Download){              //如果不受限直接继续，受限则检查最大下载
                if(((down.di.getReconnMode() & mode) != 0 && down.di.getState() == DownloadOrder.STATE_WAIT_RECONN)   //内部自动断连情况
                        || (mode == 0 && down.di.getState() == DownloadOrder.STATE_WAIT_RECONN)){                     //外部情况，先重启所有断连
                    down.tryStorage();
                    down.di.setState(DownloadOrder.STATE_DOWNING);
                    if(!down.di.isUnlimite()){
                        count++;
                    }
                }
            }
            DownloadDao.save(context, down.di);
        }
        //如果未达到下载限制，则遍历启动等待任务
        if(count < Max_Allow_Download) {
            for (Iterator<Downloader> iter = downloadMap.values().iterator(); iter.hasNext(); ) {
                Downloader down = iter.next();
                if (down.di.isUnlimite() || count < Max_Allow_Download) {              //如果不受限直接继续，受限则检查最大下载
                    if (down.di.getState() == DownloadOrder.STATE_WAIT_DOWN) {
                        down.tryStorage();
                        down.di.setState(DownloadOrder.STATE_DOWNING);
                        if (!down.di.isUnlimite()) {
                            count++;
                        }
                    }
                }
                DownloadDao.save(context, down.di);
            }
        }
        logger.debug("download task count: {}", count);
    }







}
