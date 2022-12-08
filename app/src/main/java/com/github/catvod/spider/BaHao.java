package com.github.catvod.spider;

import android.content.Context;
import android.text.TextUtils;

import com.github.catvod.bean.Class;
import com.github.catvod.bean.Result;
import com.github.catvod.bean.Vod;
import com.github.catvod.crawler.Spider;
import com.github.catvod.crawler.SpiderDebug;
import com.github.catvod.net.OkHttpUtil;
import com.github.catvod.utils.Misc;
import com.github.catvod.utils.Trans;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.seimicrawler.xpath.JXDocument;
import org.seimicrawler.xpath.JXNode;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * 8号影院
 * www.8hysw.com
 */
public class BaHao extends Spider {

    private static final String url = "http://www.8hysw.com";
    private HashMap<String, String> header;
    private JSONObject ext;
    private String extend;

    private String getCookie(String cookie) {
        if (TextUtils.isEmpty(cookie))
            return "PHPSESSID=qah2k2fasbb25pm5qumkdn99r1";
        if (cookie.startsWith("http")) return OkHttpUtil.string(cookie).replace("\n", "");
        return cookie;
    }

    private void setHeader() throws Exception {
        header.put("cookie", getCookie(ext.getString("cookie")));
        header.put("User-Agent", Misc.CHROME);
        header.put("Referer", url);
    }

    private void fetchExt() {
        String result = OkHttpUtil.string(extend);
        if (!TextUtils.isEmpty(result)) extend = result;
    }

    private void fetchRule() throws Exception {
        if (header.containsKey("cookie") && Objects.requireNonNull(header.get("cookie")).length() > 0)
            return;
        if (extend.startsWith("http")) fetchExt();
        ext = new JSONObject(extend);
        setHeader();
    }

    @Override
    public void init(Context context, String extend) {
        try {
            this.extend = extend;
            this.header = new HashMap<>();
            fetchRule();
        } catch (Exception e) {
            SpiderDebug.log(e);
        }
    }

    @Override
    public String homeContent(boolean filter) throws Exception {
        fetchRule();
        return Result.string(Class.arrayFrom(ext.getJSONArray("classes").toString()), ext.getJSONObject("filter"));
    }

    @Override
    public String homeVideoContent() throws Exception {
        fetchRule();
        return categoryContent("2", "1", true, new HashMap<>());
    }

    @Override
    public String categoryContent(String tid, String pg, boolean filter, HashMap<String, String> extend) throws Exception {
        String url = String.format("http://www.8hysw.com/frim/%s-%s.html", tid, pg);
        String html = OkHttpUtil.string(url, header);
        List<Vod> list = new ArrayList<>();
        Document doc = Jsoup.parse(html);
        Elements vodlist = doc.getElementsByClass("stui-vodlist__box");

        if (!vodlist.isEmpty()) {
            for (Element element : vodlist) {
                Vod vod = new Vod();
                Elements thumb = element.select("a.stui-vodlist__thumb");
                String pic = thumb.attr("data-original");
                vod.setVodPic(pic.startsWith("//") ? "http:" + pic : pic);
                String title = thumb.attr("title");
                vod.setVodName(title);
                String bvid = thumb.attr("href");
                vod.setVodRemarks(thumb.select("span.pic-text").text());
                vod.setVodId(bvid);
                list.add(vod);
                System.out.println();
            }
        }
        return Result.string(list);
    }

    @Override
    public String detailContent(List<String> ids) throws Exception {
        String bvid = ids.get(0);
        String url = "http://www.8hysw.com" + bvid;
        String html = OkHttpUtil.string(url, header);
        Document doc = Jsoup.parse(html);
        Elements content = doc.select(".stui-content");
        Elements detail = content.select(".stui-content__detail");
        Elements thumb = content.select(".stui-vodlist__thumb");
        Elements playlist = doc.select("#playlist");
        List<Vod> vodList = new ArrayList<>();
        for (Element element : playlist) {
            Vod vod = new Vod();
            vod.setVodId(bvid);
            vod.setVodName(detail.select("h1.title").text());
            vod.setVodPic(thumb.attr("data-original"));
            vod.setVodRemarks(thumb.select(".pic-text").text());
            vod.setVodPlayFrom(element.select(".title").text());
            Elements datas = detail.select("p.data");
            for (Element data : datas) {
                Elements muteds = data.select(".text-muted");

                for (Element muted : muteds) {
                    if (muted.text().contains("主演")) {
                        Elements actors = data.select("a");
                        List<String> list = new ArrayList<>();
                        for (Element actor : actors) {
                            String text = actor.text();
                            list.add(text);
                        }
                        vod.setVodActor(String.join(" ", list));
                    }

                    if (muted.text().contains("导演")) {
                        Elements actors = data.select("a");
                        List<String> list = new ArrayList<>();
                        for (Element actor : actors) {
                            String text = actor.text();
                            list.add(text);
                        }
                        vod.setVodDirector(String.join(" ", list));
                    }

                    if (muted.text().contains("地区")) {
                        vod.setVodArea(muted.nextElementSibling().text());
                    }

                    if (muted.text().contains("年份")) {
                        vod.setVodYear(muted.nextElementSibling().text());
                    }

                    if (muted.text().contains("类型")) {
                        vod.setTypeName(muted.nextElementSibling().text());
                    }

                    if (muted.text().contains("简介")) {
                        vod.setVodContent(data.select(".detail-content").text());
                    }

                }
            }
            Elements contentPlaylist = element.select(".stui-content__playlist li a");
            List<String> playUrllist = new ArrayList<>();
            for (Element li : contentPlaylist) {
                String title = li.attr("title");
                String href = li.attr("href");
                playUrllist.add(title + "$" + href);
            }
            vod.setVodPlayUrl(TextUtils.join("#", playUrllist));
            vodList.add(vod);
        }
        return Result.string(vodList);
    }

    @Override
    public String searchContent(String key, boolean quick) throws Exception {
        return categoryContent(key, "1", true, new HashMap<>());
    }

    @Override
    public String playerContent(String flag, String id, List<String> vipFlags) throws Exception {
        String[] ids = id.split("\\+");
        String aid = ids[0];
        String cid = ids[1];
        String url = "https://api.bilibili.com/x/player/playurl?avid=" + aid + "&cid=" + cid + "&qn=120&fourk=1";
        JSONObject resp = new JSONObject(OkHttpUtil.string(url, header));
        url = resp.getJSONObject("data").getJSONArray("durl").getJSONObject(0).getString("url");
        return Result.get().url(url).header(header).string();
    }
}
