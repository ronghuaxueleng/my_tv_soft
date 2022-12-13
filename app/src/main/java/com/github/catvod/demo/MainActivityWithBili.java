package com.github.catvod.demo;

import android.app.Activity;
import android.os.Bundle;

import com.github.catvod.spider.BaHao;
import com.github.catvod.spider.Bili;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivityWithBili extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        new Thread(() -> {
            Bili bili = new Bili();
            bili.init(this, "https://ghproxy.com/https://raw.githubusercontent.com/ronghuaxueleng/my_tv_soft/filter_interface/sties/json/child/%E5%84%BF%E7%AB%A5%E4%B9%90%E5%9B%AD.json");
            try {
                String json = bili.homeContent(false);
                System.out.println("首页数据内容");
                System.out.println(json);
                // 首页最近更新数据
                JSONObject homeContent = new JSONObject(bili.homeVideoContent());
                System.out.println("首页最近更新数据");
                System.out.println(homeContent);
                JSONObject data1 = new JSONObject(bili.categoryContent("儿童早教", "1", false, new HashMap<>()));
                JSONArray list = data1.getJSONArray("list");
                if (list.length() > 0) {
                    JSONObject jsonObject = list.getJSONObject(0);
                    String vod_id = jsonObject.getString("vod_id");
                    // 视频详情
                    JSONObject jsonObject1 = new JSONObject(bili.detailContent(new ArrayList<String>() {{
                        add(vod_id);
                    }}));
                    JSONArray list1 = jsonObject1.getJSONArray("list");
                    String vodPlayUrl = list1.getJSONObject(0).getString("vod_play_url");
                    System.out.println(vodPlayUrl);
                    String s = bili.playerContent("1", vodPlayUrl.split("\\$")[1], new ArrayList<>());
                    System.out.println(s);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }).start();
    }
}