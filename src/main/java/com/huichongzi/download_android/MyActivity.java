package com.huichongzi.download_android;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import com.huichongzi.download_android.download.*;

public class MyActivity extends Activity {

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Downloader.stopDownService(this);
        DownloadInfo info = new DownloadInfo();
        info.setId(12134);
        info.setDownDir("/storage/sdcard1");
        info.setCheckMode(0);
        info.setName("wre");
        info.setType("apk");
        info.setUrl("http://gdown.baidu.com/data/wisegame/ec9b80a18f931952/lanlingwang_1.apk");

        try {
            Downloader.addDownload(this, info, new Handler(){
                @Override
                public void handleMessage(Message msg){
                    System.out.println(msg.what);
                }
            });
        }
        catch (Exception e){
            e.printStackTrace();
        }

      }
}
