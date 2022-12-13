package com.github.catvod.js;

import android.os.Handler;
import android.os.HandlerThread;

import com.github.catvod.crawler.SpiderDebug;
import com.lzy.okgo.OkGo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.Response;

public class Base {
    protected static String baseUrl = "https://ghproxy.com/https://raw.githubusercontent.com/ronghuaxueleng/my_tv_soft/tvjar/";

    public static String getFileConten(File file) {
        try {
            //获取输入流
            InputStream mAssets = new FileInputStream(file);

            //获取文件的字节数
            int lenght = mAssets.available();
            //创建byte数组
            byte[] buffer = new byte[lenght];
            //将文件中的数据写入到字节数组中
            mAssets.read(buffer);
            mAssets.close();
            return new String(buffer);
        } catch (IOException e) {
            e.printStackTrace();
            SpiderDebug.log(e);
            SpiderDebug.log("fuck 读取" + file.getName() + "失败");
        }
        return "";
    }

    public static void downLoadJs(String jsPath, File cache) {
        HandlerThread thread = new HandlerThread("NetWork");
        thread.start();
        Handler handler = new Handler(thread.getLooper());
        handler.postDelayed(() -> {
            try {
                Response response = OkGo.<File>get(jsPath).execute();
                InputStream is = response.body().byteStream();
                OutputStream os = new FileOutputStream(cache);
                try {
                    byte[] buffer = new byte[2048];
                    int length;
                    while ((length = is.read(buffer)) > 0) {
                        os.write(buffer, 0, length);
                    }
                } finally {
                    try {
                        is.close();
                        os.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (Throwable e) {
                e.printStackTrace();
                SpiderDebug.log(e);
                SpiderDebug.log("fuck 下载" + jsPath + "失败");
            }
        }, 10);
    }

}
