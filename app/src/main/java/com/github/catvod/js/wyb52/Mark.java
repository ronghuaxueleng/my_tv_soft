package com.github.catvod.js.wyb52;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;

public class Mark {

    static String content = "";

    public static String getContent() {
        return content;
    }

    public static void setContent(Context context) {
        try {
            //获取输入流
            InputStream mAssets = context.getAssets().open("mark.js");

            //获取文件的字节数
            int lenght = mAssets.available();
            //创建byte数组
            byte[] buffer = new byte[lenght];
            //将文件中的数据写入到字节数组中
            mAssets.read(buffer);
            mAssets.close();
            content = new String(buffer);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("fuck", "error");
        }
    }

}
