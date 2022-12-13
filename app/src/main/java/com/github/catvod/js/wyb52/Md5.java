package com.github.catvod.js.wyb52;

import android.content.Context;

import com.github.catvod.js.Base;

import java.io.File;

public class Md5 extends Base {
    protected static String content = "";
    static String fileName = "wyb52/md5.js";

    public static String getContent() {
        return content;
    }

    public static void setContent(Context context) {
        File filesDir = context.getFilesDir();
        File wyb52Dir = new File(filesDir, "wyb52");
        if (!wyb52Dir.isDirectory()) {
            wyb52Dir.mkdirs();
        }

        File file = new File(filesDir, fileName);
//        if (!file.exists()) {
            downLoadJs(baseUrl + fileName, file);
//        }
        content = getFileConten(file);
    }
}
