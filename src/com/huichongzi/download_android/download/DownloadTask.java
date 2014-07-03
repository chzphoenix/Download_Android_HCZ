package com.huichongzi.download_android.download;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import android.content.Context;
import android.util.Log;

import java.security.NoSuchAlgorithmException;


/**
 * 下载线程
 * 1、将文件分多个线程下载
 * 2、监视每个下载子线程，保存下载状态等
 * Created by cuihz on 2014/7/3.
 */
class DownloadTask extends Thread {
	private int fileSize = 0;
	// 每一个线程分担的下载量
	private int blockSize;
	// 平均分配后下载量后多余的部分
	private int downloadSizeMore;
	// 分几个线程下载该应用
	private int threadNum = 5;
	private int downloadedSize = 0;
	private DownloaderListener downloadListener = null;
	private boolean finished;
	private boolean isDownMidle = false;
	String tmpPath;
    private DownloadInfo di;
	private TaskState taskState = new TaskState();
	private Context mContext = null;
	private UnFinishedConfFile unFinishConf;

    /**
     * 构造函数
     * @param context
     * @param di 下载信息
     * @param listener 下载事件监听回调
     * @param isNew 是否为新下载文件
     */
    protected DownloadTask(Context context, DownloadInfo di, DownloaderListener listener, boolean isNew) {
		this.tmpPath = di.getPath() + StorageHandleTask.Unfinished_Sign;
		this.downloadListener = listener;
        this.di = di;
		mContext = context;
        //初始化下载状态文件
		unFinishConf = new UnFinishedConfFile(di.getPath());
		//如果是断点续传，获取下载的线程数
		if(isNew && !unFinishConf.isConfNull()){
			threadNum = Integer.parseInt(unFinishConf.getValue("threadNum"));
			isDownMidle = true;
		}
		Log.d("DownloadTask", "build DownloadTask url=" + di.getUrl()
                + ",tmpPath=" + tmpPath);
	}

	@Override
	public void run() {
		if(unFinishConf.isLock()){
            downloadListener.onDownloadRepeat(di.getName() + "文件已在下载");
			return;
		}
		taskState.isPause = false;
        taskState.isStop = false;
		SingleDownloadThread[] fds = new SingleDownloadThread[threadNum];
		//记录下载线程数，以便断点续传时能使用正确的线程数下载
		unFinishConf.put("threadNum", threadNum + "");
		try {
			fileSize = di.getSize();
             URL url = new URL(di.getUrl());
			File file = new File(tmpPath);

			//如果是断点续传，首先判断记录的大小与下载信息中大小是否一致，不一致则删除重下
			if(isDownMidle && fileSize != Integer.parseInt(unFinishConf.getValue("fileSize"))){
				file.delete();
				file.getParentFile().mkdirs();
				isDownMidle = false;
			}
			//记录文件大小，以便上面判断使用
			unFinishConf.put("fileSize", fileSize + "");
			// 计算每个线程要下载的数据量
			blockSize = fileSize / threadNum;
			// 解决整除后百分比计算误差
			downloadSizeMore = (fileSize % threadNum);	
			
			//开始分线程下载，判断是否断点续传，如果是一半读取保存的下载信息为每个线程配置
			for (int i = 0; i < threadNum; i++) {
				SingleDownloadThread fdt = null;
				if (i != (threadNum - 1)) {
					long beginPos = isDownMidle ? Integer.parseInt(unFinishConf.getValue(i + "start")) : i * blockSize;
					//long endPos = isDownMidle ? Integer.parseInt(unFinishConf.getValue(i + "end")) : (i + 1) * blockSize - 1;
					long endPos = isDownMidle ? Integer.parseInt(unFinishConf.getValue(i + "end")) : (i + 1) * blockSize;
					// 启动线程，分别下载自己需要下载的部分
					fdt = new SingleDownloadThread(i, url, file, beginPos, endPos,
							taskState, downloadListener, mContext);
				} else {
					long beginPos = isDownMidle ? Integer.parseInt(unFinishConf.getValue(i + "start")) : i * blockSize;
					//long endPos = isDownMidle ? Integer.parseInt(unFinishConf.getValue(i + "end")) : (i + 1) * blockSize - 1 + downloadSizeMore;
					long endPos = isDownMidle ? Integer.parseInt(unFinishConf.getValue(i + "end")) : (i + 1) * blockSize + downloadSizeMore;
					// 最后一个线程下载字节=平均+多余字节
					fdt = new SingleDownloadThread(i, url, file, beginPos, endPos,
							taskState, downloadListener, mContext);
				}
				// 处理上次下完但因取消未及时更改文件名的情况
				if (fdt.isFinished()) {
					downloadListener.onDownloadProgressChanged(100);
					return;
				}
				fdt.setName("Thread" + i);
				fdt.start();
				fds[i] = fdt;
			}
			finished = false;
			int count = 1;
			long alreadyDownloadSize = 0;
			//如果是断点续传，读取已下载的大小
			if(isDownMidle){
				alreadyDownloadSize = Long.parseLong(unFinishConf.getValue("downloadedSize"));
			}
            //循环监视各个子线程
			while (!finished && !taskState.isStop) {
                if (taskState.isPause) {
                    Thread.sleep(1000);
                    continue;
                }
                // 先把整除的余数搞定
                downloadedSize = downloadSizeMore;
                finished = true;
                for (int i = 0; i < fds.length; i++) {
                    downloadedSize += fds[i].getDownloadSize();
                    //为每个线程记录下载信息
                    fds[i].saveProgress(unFinishConf);
                    if (!fds[i].isFinished()) {
                        finished = false;
                    }
                }
                if (downloadListener != null && !taskState.isPause && !taskState.isStop) {
                    int progress = (int) ((downloadedSize * 1.0 + alreadyDownloadSize) / fileSize * 100);
                    downloadListener.onDownloadProgressChanged(progress);
                    // 下载完毕
                    if (progress == 100) {
                        downloadOver();
                    }
                    //当下载大小超过2M，记录已下载大小，将记录的下载信息保持至文件
                    if (downloadedSize / (1024 * 1024 * 2 * count) >= 1) {
                        unFinishConf.put("downloadedSize", downloadedSize + alreadyDownloadSize + "");
                        unFinishConf.write();
                        count++;
                    }
                    // 休息1秒后再读取下载进度
                    sleep(1000);
                }
            }

		} catch (Exception e) {
			e.printStackTrace();
			downloadListener.onDownloadFailed();
		}

	}



	protected boolean isFinished() {
		return finished;
	}

    /**
     * 暂停下载
     */
	protected void pauseDownload() {
		taskState.isPause = true;
	}

    /**
     * 回复下载
     */
    protected void resumeDownload() {
        taskState.isPause = false;
    }

	protected boolean isPause(){
		return taskState.isPause;
	}

    /**
     * 取消下载
     * 删除文件
     */
    protected void stopDownload() {
        taskState.isStop = true;
        DownloadUtils.removeFile(di.getPath());
    }


    protected boolean isStop(){
        return taskState.isStop;
    }


    /**
     * 下载线程的状态类
     * 暂停和停止
     */
	protected class TaskState {
		protected boolean isPause = false;
        protected boolean isStop = false;
	}


    /**
     * 下载完成
     * 进行校验，改名，删除状态文件
     */
    private void downloadOver(){
        File downloadFile = new File(tmpPath);
//        //验证md5
//        if (!checkMd5(downloadFile)) {
//            DownloadUtils.removeFile(di.getPath());
//            downloadListener.onCheckFailed(DownloadConfig.Is_Md5_Error, "文件MD5不同！");
//            return;
//        }
//        //验证大小
//        if (downloadFile.length() != di.getSize()) {
//            DownloadUtils.removeFile(di.getPath());
//            downloadListener.onCheckFailed(DownloadConfig.Is_Size_Error, "文件大小不同！");
//            return;
//        }
        if (tmpPath.endsWith(StorageHandleTask.Unfinished_Sign)) {
            // 删除下载中标志
            new UnFinishedConfFile(di.getPath()).delete();
            File toFile = new File(di.getPath());
            // 更改文件名
            downloadFile.renameTo(toFile);
        }
        // 从下载列表中删除已下载成功的应用
        Downloader.Download_Map.remove(di.getId());
        if (downloadListener != null) {
            downloadListener.onDownloadSuccess(di.getPath());
        }
    }


    /**
     * 校验文件的md5
     * @param file
     * @return
     */
    private boolean checkMd5(File file){
        if (di.getMd5() != null) {
            // md5验证失败后不走下载流程
            try {
                if (!di.getMd5().equalsIgnoreCase(DownloadUtils.getFileMD5(file))) {
                    return false;
                }
                else{
                    return true;
                }
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
                return false;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        else{
            return true;
        }
    }
}