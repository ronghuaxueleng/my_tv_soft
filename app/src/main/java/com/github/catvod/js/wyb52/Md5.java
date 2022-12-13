package com.github.catvod.js.wyb52;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;

import com.github.catvod.js.Base;

import java.io.File;

public class Md5 extends Base {
    static String fileName = "wyb52/md5.js";

    public static String getContent(Context context) {
        File filesDir = context.getCacheDir();
        File file = new File(filesDir, fileName);
        return getFileConten(file);
    }

    public static void setContent(Context context) {
        HandlerThread thread = new HandlerThread("NetWork");
        thread.start();
        Handler handler = new Handler(thread.getLooper());
        handler.postDelayed(() -> {
            File filesDir = context.getCacheDir();
            File wyb52Dir = new File(filesDir, "wyb52");
            if (!wyb52Dir.isDirectory()) {
                wyb52Dir.mkdirs();
            }

            File file = new File(filesDir, fileName);
            if (!file.exists()) {
                downLoadJs(baseUrl + fileName, file);
            }
        }, 1000);
    }
}
