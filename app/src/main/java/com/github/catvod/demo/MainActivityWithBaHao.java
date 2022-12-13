package com.github.catvod.demo;

import android.app.Activity;
import android.os.Bundle;

import com.github.catvod.js.Md5;
import com.github.catvod.js.wyb52.Mark;
import com.github.catvod.spider.BaHao;

import org.json.JSONException;

import java.util.ArrayList;

public class MainActivityWithBaHao extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        new Thread(() -> {
            Mark.setContent(this);
            Md5.setMd5Content(this);
            BaHao bili = new BaHao();
            bili.init(this, "https://ghproxy.com/https://raw.githubusercontent.com/ronghuaxueleng/my_tv_soft/filter_interface/sties/json/8hysw.json");
            try {
                String s = bili.playerContent("1", "/play/37163-0-0.html", new ArrayList<>());
                System.out.println();
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }).start();
    }
}