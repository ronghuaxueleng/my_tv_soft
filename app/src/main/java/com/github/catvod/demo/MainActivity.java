package com.github.catvod.demo;

import android.app.Activity;
import android.os.Bundle;

import com.github.catvod.spider.Bili;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        new Thread(() -> {
            Bili bili = new Bili();
            bili.init(this, "https://ghproxy.com/https://raw.githubusercontent.com/ronghuaxueleng/my_tv_soft/filter_interface/sties/json/%E5%84%BF%E7%AB%A5%E4%B9%90%E5%9B%AD.json");
            String json = bili.homeContent(false);
            System.out.println("首页数据内容");
            System.out.println(json);
            try {
                // 首页最近更新数据
                JSONObject homeContent = new JSONObject(bili.homeVideoContent());
                System.out.println("首页最近更新数据");
                System.out.println(homeContent);
                JSONObject data = new JSONObject(bili.categoryContent("儿童早教", "1", false, null));
                JSONArray list = data.getJSONArray("list");
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
            }

        }).start();
    }
}