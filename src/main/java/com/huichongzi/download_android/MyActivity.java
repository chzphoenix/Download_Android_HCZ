package com.huichongzi.download_android;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import com.huichongzi.download_android.download.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyActivity extends Activity {

    private static final Logger logger = LoggerFactory.getLogger(MyActivity.class);
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        DownloadInfo info = new DownloadInfo();
        info.setId(-1147909057);
        info.setPath(android.os.Environment.getExternalStorageDirectory().getPath() + "/Android/data/" + getPackageName() + "/jjlggg.mp4");
        info.setCheckMode(0);
        info.setState(DownloadOrder.STATE_WAIT_DOWN);
        info.setName("jjlggg");
        info.setGroup("test");
        info.setUrl("http://ips.ifeng.com/video19.ifeng.com/video09/2014/07/31/2156868-102-067-1450.mp4");

        logger.error(info.toString());
        DownloadQueue queue = DownloadHelper.initDownloadQueue(this, "test", 1);
        try {
            queue.addDownload(info, new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    logger.error(msg.what + "," + msg.arg1 + "," + msg.obj);
                }
            });
        }
        catch (Exception e){
            logger.error("", e);
        }

    }
}
