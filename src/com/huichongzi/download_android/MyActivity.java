package com.huichongzi.download_android;

import android.app.Activity;
import android.os.Bundle;
import com.huichongzi.download_android.download.*;

public class MyActivity extends Activity {
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        DownloadInfo info = new DownloadInfo();
        info.setId("12134");
        info.setHome("/storage/sdcard1");
        info.setMode(0);
        info.setName("wre");
        info.setType("apk");
        info.setUrl("http://gdown.baidu.com/data/wisegame/ec9b80a18f931952/lanlingwang_1.apk");
        try {
            Downloader.downloadEvent(this, info, new DownloaderListener() {
                @Override
                public void onDownloadRepeat(String msg) {
                    System.out.println(msg);
                }

                @Override
                public void onConnectFailed(String msg) {
                    System.out.println(msg);
                }

                @Override
                public void onCreateFailed(String msg) {
                    System.out.println(msg);
                }

                @Override
                public void onStartDownload() {
                    System.out.println("start");
                }

                @Override
                public void onDownloadProgressChanged(int currentProgress) {
                    System.out.println(currentProgress);
                }

                @Override
                public void onDownloadFailed() {
                    System.out.println("failed");
                }

                @Override
                public void onCheckFailed(String msg) {
                    System.out.println(msg);
                }

                @Override
                public void onDownloadSuccess() {
                    System.out.println("success");
                }
            });
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
