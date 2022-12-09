package com.github.catvod.demo;

import android.app.Activity;
import android.os.Bundle;

import com.github.catvod.spider.BaHao;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivityWithBaHao extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        new Thread(() -> {
            BaHao bili = new BaHao();
            bili.init(this, "https://ghproxy.com/https://raw.githubusercontent.com/ronghuaxueleng/my_tv_soft/filter_interface/sties/json/8hysw.json");
            try {
                String json = bili.homeContent(false);
                System.out.println("首页数据内容");
                System.out.println(json);
                // 首页最近更新数据
//                JSONObject homeContent = new JSONObject(bili.homeVideoContent());
//                System.out.println("首页最近更新数据");
//                System.out.println(homeContent);
                JSONObject data = new JSONObject(bili.categoryContent("2", "1", false, new HashMap<>()));
                JSONArray list = data.getJSONArray("list");
                if (list.length() > 0) {
                    JSONObject jsonObject = list.getJSONObject(0);
                    String vod_id = jsonObject.getString("vod_id");
                    // 视频详情
                    JSONObject detailContent = new JSONObject(bili.detailContent(new ArrayList<String>() {{
                        add(vod_id);
                    }}));
                    JSONArray playList = detailContent.getJSONArray("list");
                    String vod_play_url = playList.getJSONObject(0).getString("vod_play_url");
                    String s = vod_play_url.split("#")[0];
                    String playUrl = s.split("\\$")[1];
                    bili.playerContent("1", playUrl, new ArrayList<>());
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }).start();
    }
}