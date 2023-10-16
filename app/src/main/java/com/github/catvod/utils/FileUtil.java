package com.github.catvod.utils;

import com.github.catvod.spider.Init;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileUtil {

    public static File getCacheDir() {
        return Init.context().getCacheDir();
    }

    public static File getCacheFile(String fileName) {
        return new File(getCacheDir(), fileName);
    }

    public static void write(File file, String data) {
        write(file, data.getBytes());
    }

    public static void write(File file, byte[] data) {
        try {
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(data);
            fos.flush();
            fos.close();
            chmod(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static File chmod(File file) {
        try {
            Process process = Runtime.getRuntime().exec("chmod 777 " + file);
            process.waitFor();
            return file;
        } catch (Exception e) {
            e.printStackTrace();
            return file;
        }
    }

    public static String read(File file) {
        try {
            return read(new FileInputStream(file));
        } catch (Exception e) {
            return "";
        }
    }

    public static String read(InputStream is) {
        try {
            byte[] data = new byte[is.available()];
            is.read(data);
            is.close();
            return new String(data, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }
}
