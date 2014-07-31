package com.huichongzi.download_android;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import com.huichongzi.download_android.download.*;
import org.omg.CORBA.Environment;

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
        info.setId(-1147909057);
        info.setDownDir(android.os.Environment.getExternalStorageDirectory().getPath() + "/Android/data/com.ifeng.video.cache/");
        info.setCheckMode(0);
        info.setName("jjlggg");
        info.setType("mp4");
        info.setUrl("http://ips.ifeng.com/video19.ifeng.com/video09/2014/07/31/2156868-102-067-1450.mp4");

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
