package com.github.catvod.demo;

import android.app.Activity;
import android.os.Bundle;

import com.github.catvod.crawler.Spider;
import com.github.catvod.loader.JarLoader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

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
                Spider bili = jarLoader.getSpider("Bili");
                bili.init(MainActivityWithJar.this, "https://ghproxy.com/https://raw.githubusercontent.com/ronghuaxueleng/my_tv_soft/filter_interface/sties/json/%E5%84%BF%E7%AB%A5%E4%B9%90%E5%9B%AD.json");
                try {
                    String json = bili.homeContent(false);
                    System.out.println("首页数据内容");
                    System.out.println(json);
                    // 首页最近更新数据
                    JSONObject homeContent = new JSONObject(bili.homeVideoContent());
                    System.out.println("首页最近更新数据");
                    System.out.println(homeContent);
                    JSONObject data1 = new JSONObject(bili.categoryContent("儿童早教", "1", false, null));
                    JSONArray list = data1.getJSONArray("list");
                    if (list.length() > 0) {
                        JSONObject jsonObject = list.getJSONObject(0);
                        String vod_id = jsonObject.getString("vod_id");
                        // 视频详情
                        System.out.println(bili.detailContent(new ArrayList<String>() {{
                            add(vod_id);
                        }}));
                    }
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