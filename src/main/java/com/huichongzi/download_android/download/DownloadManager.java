package com.huichongzi.download_android.download;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cuihz on 2014/8/11.
 */
public class DownloadManager {

    private static final Logger logger = LoggerFactory.getLogger(DownloadManager.class);
    protected static DownServiceListener serviceListener= null;
    static boolean isDownloadServiceOn;

    /**
     * 暂停下载
     *
     * @param context
     * @param id      下载唯一id
     * @throws DownloadDBException
     * @throws DownloadNotExistException 下载任务不存在或id不正确
     * @throws IllegalParamsException
     */
    public static void pauseDownload(Context context, int id) throws DownloadNotExistException, IllegalParamsException, DownloadDBException {
        if(context == null){
            throw new IllegalParamsException("context", "must not null");
        }
        if(id == 0){
            throw new IllegalParamsException("id", "must <> 0");
        }
        if (DownloadList.has(id)) {
            Downloader down = getDownloader(id);
            if (down.di.getState() != DownloadOrder.STATE_SUCCESS) {
                down.changeState(DownloadOrder.STATE_PAUSE, 0, "", false, false);
            }
            Intent intent = new Intent(context, DownloadService.class);
            context.startService(intent);
            DownloadDao.save(context, down.di);
        } else {
            throw new DownloadNotExistException();
        }
    }


    /**
     * 批量暂停下载，根据id列表
     *
     * @param context
     * @param ids
     * @throws DownloadDBException
     * @throws IllegalParamsException
     */
    public static void pauseDownloadForIds(Context context, List<Integer> ids) throws IllegalParamsException, DownloadDBException {
        if(context == null){
            throw new IllegalParamsException("context", "must not null");
        }
        if(ids == null || ids.size() <= 0){
            throw new IllegalParamsException("ids", "must not null and size > 0");
        }
        List<DownloadInfo> downloadInfos = new ArrayList<DownloadInfo>();
        for(int downId : ids){
            Downloader downloader = getDownloader(downId);
            if(downloader == null || downloader.di == null){
                continue;
            }
            if (downloader.di.getState() != DownloadOrder.STATE_SUCCESS) {
                downloader.changeState(DownloadOrder.STATE_PAUSE, 0, null, false, false);
                downloadInfos.add(downloader.di);
            }
        }
       Intent intent = new Intent(context, DownloadService.class);
        context.startService(intent);

        DownloadDao.saveList(context, downloadInfos);
    }


    /**
     * 批量暂停下载，根据组
     *
     * @param context
     * @param group 下载组
     * @throws DownloadDBException
     * @throws IllegalParamsException
     */
    public static void pauseDownloadForGroup(Context context, String group) throws IllegalParamsException, DownloadDBException {
        if(context == null){
            throw new IllegalParamsException("context", "must not null");
        }
        if(group == null || group.equals("")){
            throw new IllegalParamsException("group", "must not null");
        }
        List<DownloadInfo> downloadInfos = new ArrayList<DownloadInfo>();
        for(Downloader downloader : DownloadList.downloadMap.values()){
            if(downloader.di != null && downloader.di.getGroup().equals(group)){
                if (downloader.di.getState() != DownloadOrder.STATE_SUCCESS) {
                    downloader.changeState(DownloadOrder.STATE_PAUSE, 0, null, false, false);
                    downloadInfos.add(downloader.di);
                }
            }
        }
        Intent intent = new Intent(context, DownloadService.class);
        context.startService(intent);

        DownloadDao.saveList(context, downloadInfos);
    }


    /**
     * 恢复下载
     *
     * @param context
     * @param id      下载唯一id
     * @throws DownloadDBException
     * @throws DownloadNotExistException 下载任务不存在或id不正确
     * @throws IllegalParamsException
     */
    public static void resumeDownload(Context context, int id) throws DownloadNotExistException, IllegalParamsException, DownloadDBException {
        if(context == null){
            throw new IllegalParamsException("context", "must not null");
        }
        if(id == 0){
            throw new IllegalParamsException("id", "must <> 0");
        }
        if (DownloadList.has(id)) {
            Downloader down = getDownloader(id);
            if (down.di.getState() != DownloadOrder.STATE_SUCCESS && down.di.getState() != DownloadOrder.STATE_DOWNING && down.di.getState() != DownloadOrder.STATE_WAIT_RECONN) {
                down.changeState(DownloadOrder.STATE_WAIT_DOWN, 0, "", false, false);
            }
            Intent intent = new Intent(context, DownloadService.class);
            context.startService(intent);
            DownloadDao.save(context, down.di);
        } else {
            throw new DownloadNotExistException();
        }
    }


    /**
     * 批量恢复下载，根据id列表
     *
     * @param context
     * @param ids
     * @throws DownloadDBException
     * @throws IllegalParamsException
     */
    public static void resumeDownloadForIds(Context context, List<Integer> ids) throws IllegalParamsException, DownloadDBException {
        if(context == null){
            throw new IllegalParamsException("context", "must not null");
        }
        if(ids == null || ids.size() <= 0){
            throw new IllegalParamsException("ids", "must not null and size > 0");
        }
        List<DownloadInfo> downloadInfos = new ArrayList<DownloadInfo>();
        for(int downId : ids){
            Downloader downloader = getDownloader(downId);
            if(downloader == null || downloader.di == null){
                continue;
            }
            if (downloader.di.getState() != DownloadOrder.STATE_SUCCESS && downloader.di.getState() != DownloadOrder.STATE_DOWNING && downloader.di.getState() != DownloadOrder.STATE_WAIT_RECONN) {
                downloader.changeState(DownloadOrder.STATE_WAIT_DOWN, 0, null, false, false);
                downloadInfos.add(downloader.di);
            }
        }
        Intent intent = new Intent(context, DownloadService.class);
        context.startService(intent);

        DownloadDao.saveList(context, downloadInfos);
    }

    /**
     * 批量恢复下载，根据组
     *
     * @param context
     * @param group 下载组
     * @throws DownloadDBException
     * @throws IllegalParamsException
     */
    public static void resumeDownloadForGroup(Context context, String group) throws IllegalParamsException, DownloadDBException {
        if(context == null){
            throw new IllegalParamsException("context", "must not null");
        }
        if(group == null || group.equals("")){
            throw new IllegalParamsException("group", "must not null");
        }
        List<DownloadInfo> downloadInfos = new ArrayList<DownloadInfo>();
        for(Downloader downloader : DownloadList.downloadMap.values()){
            if(downloader.di != null && downloader.di.getGroup().equals(group)){
                if (downloader.di.getState() != DownloadOrder.STATE_SUCCESS && downloader.di.getState() != DownloadOrder.STATE_DOWNING && downloader.di.getState() != DownloadOrder.STATE_WAIT_RECONN) {
                    downloader.changeState(DownloadOrder.STATE_WAIT_DOWN, 0, null, false, false);
                    downloadInfos.add(downloader.di);
                }
            }
        }
        Intent intent = new Intent(context, DownloadService.class);
        context.startService(intent);

        DownloadDao.saveList(context, downloadInfos);
    }


    /**
     * 取消下载/删除已下载文件
     *
     * @param context
     * @param id  下载唯一id
     * @throws DownloadDBException
     * @throws DownloadNotExistException 下载任务不存在或id不正确
     * @throws IllegalParamsException
     */
    public static void cancelDownload(Context context, int id) throws DownloadNotExistException, IllegalParamsException, DownloadDBException {
        if(context == null){
            throw new IllegalParamsException("context", "must not null");
        }
        if(id == 0){
            throw new IllegalParamsException("id", "must <> 0");
        }
        if (DownloadList.has(id)) {
            DownloadList.remove(context, id);
            Intent intent = new Intent(context, DownloadService.class);
            context.startService(intent);
        } else {
            throw new DownloadNotExistException();
        }
    }


    /**
     * 批量取消/删除下载，根据id列表
     *
     * @param context
     * @param ids
     * @throws DownloadDBException
     * @throws IllegalParamsException
     */
    public static void cancelDownloadForIds(Context context, List<Integer> ids) throws IllegalParamsException, DownloadDBException {
        if(context == null){
            throw new IllegalParamsException("context", "must not null");
        }
        if(ids == null || ids.size() <= 0){
            throw new IllegalParamsException("ids", "must not null and size > 0");
        }
        List<DownloadInfo> infos = new ArrayList<DownloadInfo>();
        for(int downId : ids){
            Downloader downloader = getDownloader(downId);
            if(downloader == null || downloader.di == null){
                DownloadDao.delete(context, downId);
                continue;
            }
            infos.add(downloader.di);
        }
        DownloadList.removeList(context, infos);

        Intent intent = new Intent(context, DownloadService.class);
        context.startService(intent);
    }


    /**
     * 批量取消/删除下载，根据组和是否已完成
     *
     * @param context
     * @param group 下载组
     * @param type 见DownloadOrder
     * @throws DownloadDBException
     * @throws IllegalParamsException
     */
    public static void cancelDownloadForGroup(Context context, String group, int type) throws IllegalParamsException, DownloadDBException {
        if(context == null){
            throw new IllegalParamsException("context", "must not null");
        }
        if(group == null || group.equals("")){
            throw new IllegalParamsException("group", "must not null");
        }
        if(type < DownloadOrder.GROUP_ALL || type > DownloadOrder.GROUP_DOWNLOADING){
            throw new IllegalParamsException("type", "must GROUP_ALL/GROUP_DOWNLOADED/GROUP_DOWNLOADING");
        }
        List<DownloadInfo> infos = new ArrayList<DownloadInfo>();
        for(Downloader downloader : DownloadList.downloadMap.values()){
            if(downloader.di.getGroup().equals(group)){
                if(type == DownloadOrder.GROUP_ALL || (type == DownloadOrder.GROUP_DOWNLOADED) == (downloader.di.getState() == DownloadOrder.STATE_SUCCESS)){
                    if(downloader.di != null){
                        infos.add(downloader.di);
                    }
                }
            }
        }
        DownloadList.removeList(context, infos);

        Intent intent = new Intent(context, DownloadService.class);
        context.startService(intent);
    }


    /**
     * 列表中是否已经存在此任务
     * @param id
     * @return boolean
     */
    public static boolean isInList(int id){
        return DownloadList.has(id);
    }


    /**
     * 获取数据库中的下载列表。
     * 用于程序启动时
     * @param context
     * @param group    下载组
     * @param isDowned 是否已下载完
     * @return List<DownloadInfo>
     * @throws DownloadDBException
     */
    public static List<DownloadInfo> getDownloadList(Context context, String group, boolean isDowned) throws DownloadDBException {
        return DownloadList.getDownloadList(context, group, isDowned);
    }


    /**
     * 添加下载任务
     *
     * @param context
     * @param downloadInfo      下载信息
     * @param handler   当下载状态改变时会发送Message。Message.what为当前下载状态；Message.arg1在downloading状态下为进度值，在failed状态下为失败代码，其他状态无意义；Message.obj为字符串，在failed状态下为失败信息，其他无意义
     * @throws IllegalParamsException
     * @throws DownloadDBException
     */
    public static void addDownload(Context context, DownloadInfo downloadInfo, Handler handler) throws IllegalParamsException, DownloadDBException {
        downloadInfo.checkIllegal();
        //检查列表中是否已存在
        if (DownloadList.has(downloadInfo.getId())) {
            throw new IllegalParamsException(downloadInfo.getName(), "already exist");
        }
        if(context == null){
            throw new IllegalParamsException("context", "must not null");
        }
        if (downloadInfo.getState() <= DownloadOrder.STATE_DOWNING || downloadInfo.getState() >= DownloadOrder.STATE_SUCCESS) {
            downloadInfo.setState(DownloadOrder.STATE_WAIT_DOWN);
        }
        DownloadDao.save(context, downloadInfo);
        Downloader down = new Downloader(downloadInfo, handler);
        DownloadList.add(down);

        Intent intent = new Intent(context, DownloadService.class);
        context.startService(intent);
    }



    /**
     * 添加下载任务列表
     *
     * @param context
     * @param downloadInfos      下载信息列表
     * @param handlers   当下载状态改变时会发送Message。Message.what为当前下载状态；Message.arg1在downloading状态下为进度值，在failed状态下为失败代码，其他状态无意义；Message.obj为字符串，在failed状态下为失败信息，其他无意义
     * @throws IllegalParamsException
     * @throws DownloadDBException
     */
    public static void addDownloadForList(Context context, List<DownloadInfo> downloadInfos, List<Handler> handlers) throws IllegalParamsException, DownloadDBException {
        if(context == null){
            throw new IllegalParamsException("context", "must not null");
        }
        DownloadDao.saveList(context, downloadInfos);

        for(int i = 0; i < downloadInfos.size(); i++) {
            DownloadInfo downloadInfo = downloadInfos.get(i);
            downloadInfo.checkIllegal();
            //检查列表中是否已存在
            if (DownloadList.has(downloadInfo.getId())) {
                if(handlers != null && handlers.size() > i && handlers.get(i) != null) {
                    Message msg = handlers.get(i).obtainMessage(DownloadOrder.STATE_FAILED, DownloadOrder.FAILED_ADD_EXIST, 0, downloadInfo.getName());
                    handlers.get(i).handleMessage(msg);
                }
            }
            else{
                if (downloadInfo.getState() <= DownloadOrder.STATE_DOWNING || downloadInfo.getState() >= DownloadOrder.STATE_SUCCESS) {
                    downloadInfo.setState(DownloadOrder.STATE_WAIT_DOWN);
                }
                Downloader down;
                if(handlers != null && handlers.size() > i && handlers.get(i) != null) {
                    down = new Downloader(downloadInfo, handlers.get(i));
                }
                else{
                    down = new Downloader(downloadInfo, null);
                }
                DownloadList.add(down);
            }
        }

        Intent intent = new Intent(context, DownloadService.class);
        context.startService(intent);
    }


    /**
     * 用于老数据迁移，实际上只是数据库批量保存操作的代理
     * @param context
     * @param downloadInfoList   保存的列表
     * @throws DownloadDBException
     */
    public static void saveForMoveData(Context context, List<DownloadInfo> downloadInfoList) throws DownloadDBException {
        DownloadDao.saveList(context, downloadInfoList);
    }



    /**
     * 停止后台的下载服务
     * @param context
     */
    public static void stopDownService(Context context){
        Intent intent = new Intent(context, DownloadService.class);
        context.stopService(intent);
    }


    /**
     * 设置下载监听器
     * @param id      下载唯一id
     * @param handler  当下载状态改变时会发送Message。Message.what为当前下载状态；Message.arg1在downloading状态下为进度值，在failed状态下为失败代码，其他状态无意义；Message.obj为字符串，在failed状态下为失败信息，其他无意义
     * @throws DownloadNotExistException
     */
    public static void setDownloadListener(int id, Handler handler) throws DownloadNotExistException {
        if (DownloadList.has(id)) {
            Downloader down = DownloadList.get(id);
            down.downloadHandler = handler;
        } else {
            throw new DownloadNotExistException();
        }
    }

    /**
     * 监听下载的后台服务
     * @param listener
     */
    public static void setDownServiceListener(DownServiceListener listener){
        serviceListener = listener;
    }




    private static Downloader getDownloader(int id){
        if (!DownloadList.has(id)) {
            logger.error("{} is not exist.", id);
            return null;
        }
        Downloader down = DownloadList.get(id);
        return down;
    }


}
