package com.github.catvod.demo;

import android.app.Activity;
import android.os.Bundle;

import com.github.catvod.crawler.Spider;
import com.github.catvod.js.wyb52.Mark;
import com.github.catvod.js.wyb52.Md5;
import com.github.catvod.loader.JarLoader;

import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class MainActivityWithJar extends Activity {
    JarLoader jarLoader = new JarLoader();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            InputStream is = getAssets().open("custom_spider.jar");
            int len = is.available();
            byte[] data = new byte[len];
            is.read(data);
            is.close();
            jarLoader.load(this, data);

            new Thread(() -> {
                Mark.setContent(this);
                Md5.setContent(this);
                Spider bili = jarLoader.getSpider("BaHao");
                bili.init(MainActivityWithJar.this, "https://ghproxy.com/https://raw.githubusercontent.com/ronghuaxueleng/my_tv_soft/filter_interface/sties/json/8hysw.json");
                try {
                    String s = bili.playerContent("1", "/play/37163-0-0.html", new ArrayList<>());
                    System.out.println(s);
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}