package com.huichongzi.download_android.download;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import com.huichongzi.download_android.exception.*;
import com.huichongzi.download_android.utils.NetUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 下载队列，用于添加任务，改变任务状态等
 * Created by cuihz on 2014/8/11.
 */
public class DownloadQueue {

    private static final Logger logger = LoggerFactory.getLogger(DownloadQueue.class);

    private Context context;
    private int downloadCount;
    String group;
    /** 此队列中可同时进行下载的任务上限 **/
    int maxDownload;
    Map<Integer, Handler> handlers = new HashMap<Integer, Handler>();
    List<Downloader> downloadings = new ArrayList<Downloader>();

    protected DownloadQueue(Context context, String group, int maxDownload){
        this.context = context;
        this.group = group;
        this.maxDownload = maxDownload;
        if(this.maxDownload <= 0){
            this.maxDownload = 1;
        }
    }



    public void changeStateById(int id, int state) throws IllegalParamsException, DownloadDBException {
        if(context == null){
            throw new IllegalParamsException("context", "must not null");
        }
        if(id == 0){
            throw new IllegalParamsException("id", "must <> 0");
        }
        if(TextUtils.isEmpty(group)){
            throw new IllegalParamsException("group", "must not null");
        }
        if(state <= DownloadOrder.STATE_DOWNING || state > DownloadOrder.STATE_STOP){
            throw new IllegalParamsException("state", "error");
        }
        List<Integer> ids = new ArrayList<Integer>();
        ids.add(id);
        changeStateByIds(ids, state);
    }



    public void changeStateByIds(List<Integer> ids, int state) throws IllegalParamsException, DownloadDBException {
        if(context == null){
            throw new IllegalParamsException("context", "must not null");
        }
        if(ids == null || ids.size() < 0){
            throw new IllegalParamsException("ids", "size must > 0");
        }
        if(TextUtils.isEmpty(group)){
            throw new IllegalParamsException("group", "must not null");
        }
        if(state <= DownloadOrder.STATE_DOWNING || state > DownloadOrder.STATE_STOP){
            throw new IllegalParamsException("state", "error");
        }
        for(int i = 0; i < downloadings.size(); i++){
            Downloader downloader = downloadings.get(i);
            DownloadInfo info = downloader.di;
            if(ids.contains(info.getId())) {
                downloader.changeState(state, 0, "", false, true);
                ids.remove((Integer)info.getId());
                i--;
            }
        }
        if(state != DownloadOrder.STATE_STOP) {
            DownloadDao.updateByIds(context, group, ids, state);
            for(int id : ids){
                Handler handler = handlers.get(id);
                Message message = handler.obtainMessage(state, 0, 0, "");
                handler.sendMessage(message);
            }
        }
        else{
            List<DownloadInfo> list = DownloadDao.getByIds(context, ids);
            for(DownloadInfo info : list){
                StorageHandleTask.removeFile(info.getPath());
            }
            DownloadDao.deleteByIds(context, ids);
        }
        startService();
    }




    public void changeStateByGroup(int state) throws IllegalParamsException, DownloadDBException {
        if(context == null){
            throw new IllegalParamsException("context", "must not null");
        }
        if(TextUtils.isEmpty(group)){
            throw new IllegalParamsException("group", "must not null");
        }
        if(state <= DownloadOrder.STATE_DOWNING || state > DownloadOrder.STATE_STOP){
            throw new IllegalParamsException("state", "error");
        }
        for(int i = 0; i < downloadings.size(); i++){
            Downloader downloader = downloadings.get(i);
            downloader.changeState(state, 0, "", false, true);
        }
        downloadings.clear();
        List<DownloadInfo> list = DownloadDao.getByGroup(context, group);
        if(state != DownloadOrder.STATE_STOP) {
            DownloadDao.updateByGroup(context, group, state);
            for(DownloadInfo downloadInfo : list){
                if(downloadInfo.getState() == DownloadOrder.STATE_SUCCESS || downloadInfo.getState() == DownloadOrder.STATE_STOP) {
                    handlers.remove(downloadInfo.getId());
                }
                else {
                    Handler handler = handlers.get(downloadInfo.getId());
                    Message message = handler.obtainMessage(state, 0, 0, "");
                    handler.sendMessage(message);
                }
            }
        }
        else{
            for(DownloadInfo info : list){
                StorageHandleTask.removeFile(info.getPath());
            }
            DownloadDao.deleteList(context, list);
        }
        startService();
    }


    /**
     * 保持/恢复下载状态
     * @param toHold  为true是保持下载状态，为false时恢复下载状态
     */
    public void hold(boolean toHold){
        if(toHold){
            for(Downloader downloader : downloadings){
                DownloadInfo info = downloader.di;
                info.setState(DownloadOrder.STATE_WAIT_DOWN);
                info.setPriority(DownloadOrder.PRIORITY_DOWNING);
                try {
                    DownloadDao.save(context, info);
                } catch (DownloadDBException e) {
                    logger.error(e.toString(), e);
                }
                Handler handler = handlers.get(info.getId());
                Message message = handler.obtainMessage(DownloadOrder.STATE_WAIT_DOWN, 0, 0, "");
                handler.sendMessage(message);
            }
        }
        else{
            startService();
        }
    }




    public void addDownload(DownloadInfo downloadInfo, Handler handler) throws IllegalParamsException, DownloadDBException {
        if(context == null){
            throw new IllegalParamsException("context", "must not null");
        }
        handlers.put(downloadInfo.getId(), handler);
        List<DownloadInfo> infos = new ArrayList<DownloadInfo>();
        infos.add(downloadInfo);
        addDownloadByList(infos);
    }



    public void addDownloadByList(List<DownloadInfo> downloadInfos) throws IllegalParamsException, DownloadDBException {
        if(context == null){
            throw new IllegalParamsException("context", "must not null");
        }
        DownloadDao.saveList(context, downloadInfos);
        startService();
    }

    public boolean hasDownload(int id) throws DownloadDBException {
        return DownloadDao.has(context, id);
    }


    public void changeDownloadInfo(DownloadInfo info) throws DownloadDBException {
        List<DownloadInfo> infos = new ArrayList<DownloadInfo>();
        infos.add(info);
        changeDownloadInfoByList(infos);
    }

    public void changeDownloadInfoByList(List<DownloadInfo> infos) throws DownloadDBException {
        DownloadDao.saveList(context, infos);
    }


    private void startService(){
        Intent intent = new Intent(context, DownloadService.class);
        intent.putExtra(DownloadInfo.GROUP, group);
        context.startService(intent);
    }

    public void setMaxDownload(int max){
        this.maxDownload = max;
    }


    public void registerDownloadHandler(int id, Handler handler){
        handlers.put(id, handler);
    }

    public Handler getDownloadHandlerById(int id){
        return handlers.get(id);
    }





    public void setDownloaderPriority(int id, int priority) throws IllegalParamsException, DownloadDBException {
        if(context == null){
            throw new IllegalParamsException("context", "must not null");
        }
        if(id == 0){
            throw new IllegalParamsException("id", "must <> 0");
        }
        if(TextUtils.isEmpty(group)){
            throw new IllegalParamsException("group", "must not null");
        }
        DownloadDao.updatePriority(context, group, id, priority);
    }


    public int getDownloaderPriority(int id) throws IllegalParamsException, DownloadDBException {
        if(context == null){
            throw new IllegalParamsException("context", "must not null");
        }
        if(id == 0){
            throw new IllegalParamsException("id", "must <> 0");
        }
        if(TextUtils.isEmpty(group)){
            throw new IllegalParamsException("group", "must not null");
        }
        DownloadInfo info = DownloadDao.getById(context, id);
        if(info != null){
            return info.getPriority();
        }
        else{
            throw new IllegalParamsException("id", "not exits");
        }
    }

    synchronized void refresh(Context context) throws DownloadDBException {
        this.context = context;
        if(!NetUtils.isNetAvailable(context)){
            return;
        }
        List<DownloadInfo> list = DownloadDao.getRefreshList(context, group);
        downloadCount = 0;
        for(DownloadInfo info : list){
            if(downloadCount >= maxDownload){
                return;
            }
            else if(info.getState() == DownloadOrder.STATE_DOWNING){
                downloadCount++;
            }
            else if(info.getState() == DownloadOrder.STATE_WAIT_DOWN){
                Downloader down = new Downloader(context, info, this);
                downloadings.add(down);
                info.setState(DownloadOrder.STATE_DOWNING);
                DownloadDao.save(context, info);
                down.tryStorage();
                downloadCount++;
            }
        }
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DownloadQueue that = (DownloadQueue) o;

        if (group != null ? !group.equals(that.group) : that.group != null) return false;

        return true;
    }
}
