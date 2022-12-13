package com.github.catvod.js.wyb52;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.github.catvod.js.Base;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class Mark extends Base {
    protected static String content = "";
    static String fileName = "wyb52/mark.js";

    public static String getContent() {
        return content;
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
//        if (!file.exists()) {
            downLoadJs(baseUrl + fileName, file);
//        }
            content = getFileConten(file);
        }, 1000);
    }
}
