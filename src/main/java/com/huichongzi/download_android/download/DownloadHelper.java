package com.huichongzi.download_android.download;

import android.content.Context;
import com.huichongzi.download_android.utils.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * 下载管理，主要负责下载队列池管理和新建下载队列
 * Created by cuihz on 2014/11/4.
 */
public class DownloadHelper {

    private static final Logger logger = LoggerFactory.getLogger(DownloadHelper.class);

    private static List<DownloadQueue> queues = new ArrayList<DownloadQueue>();

    static boolean isServiceOn = false;

    public static DownloadQueue initDownloadQueue(Context context, String group, int max){
        DownloadQueue queue = new DownloadQueue(context, group, max);
        int index = queues.indexOf(queue);
        if(index != -1){
            queue = queues.get(index);
            queue.maxDownload = max;
        }
        else{
            queues.add(queue);
        }
        return queue;
    }

    public static int getMaxDownloaderOfGroup(String group){
        for(DownloadQueue queue : queues){
            if(queue.group.equals(group)){
                return queue.maxDownload;
            }
        }
        return 1;
    }

    public static DownloadQueue getDownloadQueue(String group){
        for(DownloadQueue queue : queues){
            if(queue.group.equals(group)){
                return queue;
            }
        }
        return null;
    }


    public static boolean isDownloadServiceOn(){
        return isServiceOn;
    }

    public static boolean isFileDownloaded(String path){
        return FileUtils.isFileExist(path);
    }

    public static boolean isFileDownloading(String path){
        return FileUtils.isFileExist(path + StorageHandleTask.Unfinished_Sign);
    }



}
