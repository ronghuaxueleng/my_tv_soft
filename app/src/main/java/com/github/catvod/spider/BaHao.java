package com.github.catvod.spider;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import com.github.catvod.bean.Class;
import com.github.catvod.bean.Result;
import com.github.catvod.bean.Vod;
import com.github.catvod.crawler.Spider;
import com.github.catvod.crawler.SpiderDebug;
import com.github.catvod.js.Md5;
import com.github.catvod.js.wyb52.Ck;
import com.github.catvod.js.wyb52.Mark;
import com.github.catvod.net.OkHttpUtil;
import com.github.catvod.parser.AAencode;
import com.github.catvod.utils.Misc;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

/**
 * 8号影院
 * www.8hysw.com
 */
public class BaHao extends Spider {

    private static final String url = "http://www.8hysw.com";
    private HashMap<String, String> header;
    private JSONObject ext;
    private String extend;

    private final ScriptEngineManager manager = new ScriptEngineManager();
    private final ScriptEngine engine = manager.getEngineByName("javascript");

    // js注释正则
    String jsNotes = "[^:]//.*|/\\*(\\s|.)*?\\*\\/";
    Pattern urlPattern = Pattern.compile("var now=base64decode\\(\"(?<url>\\S+)\"\\);");
    Pattern pnPattern = Pattern.compile("var pn=\"(?<pn>\\S+)\";");
    Pattern targetPattern = Pattern.compile("src=\"(?<targetUrl>https://\\S+url=\\S+)\"");
    Pattern evalPattern = Pattern.compile("eval\\((?<function>.*)\\)");
    Pattern vdPattern = Pattern.compile("vd='(?<vd>\\S+)'");
    Pattern keyPattern = Pattern.compile("key\\s?=\\s?'(?<key>\\S+)'");
    Pattern texPattern = Pattern.compile("tex=(?<tex>\\S+);");
    Pattern pageTimePattern = Pattern.compile("var\\spageTime\\s=\\s(?<pageTime>\\S+);");
    Pattern cvidpPattern = Pattern.compile("\\$\\('#cvid'\\).val\\('(?<cvid>\\S+)'\\);");
    Pattern cvkPattern = Pattern.compile("document\\.getElementById\\(\"cvk\"\\)\\.value=\\$\\(\"#cvid\"\\)\\.val\\(\\)\\+\"(?<cvk>\\S+)\";");
    String func1 = "function(p,a,c,k,e,d){e=function(c){return c};if(!''.replace(/^/,String)){while(c--){d[c]=k[c]||c}k=[function(e){return d[e]}];e=function(){return'\\\\w+'};c=1};while(c--){if(k[c]){p=p.replace(new RegExp('\\\\b'+e(c)+'\\\\b','g'),k[c])}}return p}";
    String func2 = "function(p,a,c,k,e,d){e=function(c){return(c<a?\"\":e(parseInt(c/a)))+((c=c%a)>35?String.fromCharCode(c+29):c.toString(36))};if(!''.replace(/^/,String)){while(c--)d[e(c)]=k[c]||e(c);k=[function(e){return d[e]}];e=function(){return'\\\\w+'};c=1;};while(c--)if(k[c])p=p.replace(new RegExp('\\\\b'+e(c)+'\\\\b','g'),k[c]);return p;}";

    private String getCookie(String cookie) {
        if (TextUtils.isEmpty(cookie))
            return "PHPSESSID=qah2k2fasbb25pm5qumkdn99r1";
        if (cookie.startsWith("http")) return OkHttpUtil.string(cookie).replace("\n", "");
        return cookie;
    }

    private void setHeader() throws Exception {
        header.put("cookie", getCookie(ext.getString("cookie")));
        header.put("User-Agent", Misc.ANDROID);
        header.put("Connection", "keep-alive");
        header.put("Accept-Language", "zh-CN,zh;q=0.9,en-US;q=0.8,en;q=0.7");
        header.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
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

    @TargetApi(Build.VERSION_CODES.O)
    @Override
    public String playerContent(String flag, String id, List<String> vipFlags) throws Exception {
        String url = "http://www.8hysw.com" + id;
        String html = OkHttpUtil.string(url, header);
        Document doc = Jsoup.parse(html);
        Elements playerFrame = doc.select(".stui-player__iframe");
        Elements scripts = playerFrame.select("script");
        String pn = null;
        String playUrl = null;
        for (Element script : scripts) {
            Matcher urlMatcher = urlPattern.matcher(script.html());
            if (urlMatcher.find()) {
                byte[] decodedBytes = Base64.getDecoder().decode(urlMatcher.group("url"));
                playUrl = new String(decodedBytes);
            }
            Matcher pnMatcher = pnPattern.matcher(script.html());
            if (pnMatcher.find()) {
                pn = pnMatcher.group("pn");
            }
        }
        String playerUrl = "http://www.8hysw.com/js/player/" + pn + ".html";
        html = OkHttpUtil.string(playerUrl, header);
        html = html.replaceAll(jsNotes, "");
        Matcher targetMatcher = targetPattern.matcher(html);
        String targetUrl = null;
        if (targetMatcher.find()) {
            targetUrl = targetMatcher.group("targetUrl");
            if (targetUrl != null) {
                targetUrl = targetUrl.replaceAll("\\s", "").replace("'+parent.now+'", "%s");
            }
        }

        if (targetUrl != null && playUrl != null) {
            url = String.format(targetUrl, playUrl);
            Map<String, String> newHeader = new HashMap<>(header);
            newHeader.put("Referer", playerUrl);
            newHeader.put("Host", Misc.getHost(url));
            html = OkHttpUtil.string(url, newHeader);
            Document palyerHtmldoc = Jsoup.parse(html);
            String vd = "";
            String key = "";
            String tex = "";
            String ckey = "";
            String pageTime = "";
            String cvk = Objects.requireNonNull(palyerHtmldoc.getElementById("cvk")).val();
            String cvid = Objects.requireNonNull(palyerHtmldoc.getElementById("cvid")).val();
            String guid = Objects.requireNonNull(palyerHtmldoc.getElementById("guid")).val();
            scripts = palyerHtmldoc.select("script");
            for (Element script : scripts) {
                String src = script.attr("src");
                if ("".equals(src)) {
                    String[] splitScripts = script.html().split("\n");
                    for (String splitScript : splitScripts) {
                        String s = splitScript.replaceAll("\\t", "");
                        boolean ifEval = s.startsWith("eval");
                        if (ifEval) {
                            Matcher evalMatcher = evalPattern.matcher(s);
                            if (evalMatcher.find()) {
                                String function = evalMatcher.group("function");
                                if (function.startsWith(func1)) {
                                    String s1 = function.replace(func1, "var func1=" + func1 + ";func1");
                                    String execute = execute(s1);
                                    String[] evals = execute.split("eval");
                                    StringJoiner evalJoiner = new StringJoiner(";\n");
                                    evalJoiner.add(Md5.getMd5Content());
                                    evalJoiner.add(Ck.content);
                                    evalJoiner.add("var vd = '" + vd + "'");
                                    evalJoiner.add("var cache =" + execute(evals[0] + execute(evals[1])));
                                    evalJoiner.add(execute(evals[2]).replace("document.domain", "'" + Misc.getHost(url) + "'"));
                                    ckey = execute(evalJoiner.toString());
                                } else if (function.startsWith(func2)) {
                                    String s1 = function.replace(func2, "var func2=" + func2 + ";func2");
                                    String execute = execute(s1);
                                    Matcher cvkMatcher = cvkPattern.matcher(execute);
                                    if (cvkMatcher.find()) {
                                        cvk = cvid + cvkMatcher.group("cvk");
                                    }
                                }
                            }
                        } else if (s.contains("ﾟωﾟﾉ= /｀ｍ´）ﾉ ~┻━┻")) {
                            String decode = AAencode.decode(s);
                            Matcher cvidMatcher = cvidpPattern.matcher(decode);
                            if (cvidMatcher.find()) {
                                cvid = cvidMatcher.group("cvid");
                            }
                            Matcher vdMatcher = vdPattern.matcher(decode);
                            if (vdMatcher.find()) {
                                vd = vdMatcher.group("vd");
                            }
                            Matcher keyMatcher = keyPattern.matcher(decode);
                            if (keyMatcher.find()) {
                                key = keyMatcher.group("key");
                            }
                            Matcher texMatcher = texPattern.matcher(decode);
                            if (texMatcher.find()) {
                                tex = texMatcher.group("tex");
                            }
                        } else {
                            Matcher pageTimeMatcher = pageTimePattern.matcher(s);
                            if (pageTimeMatcher.find()) {
                                pageTime = pageTimeMatcher.group("pageTime");
                            }
                        }
                    }
                }
            }
            TreeMap<String, String> postData = new TreeMap<>();
            postData.put("vid", playUrl);
            postData.put("cvid", execute(Md5.getMd5Content() + "\n" + "md5('" + cvk + "')") + "0");
            postData.put("type", "auto");
            postData.put("guid", guid);
            postData.put("mode", "phone");
            postData.put("err", "0");
            postData.put("rms", "0");
            postData.put("ptm", String.valueOf(Math.abs((double) System.currentTimeMillis() / 1000 - Long.parseLong(pageTime))));
            postData.put("key", key);
            String markContent = Md5.getMd5Content() + "\n" + Mark.getContent();
            postData.put("ckey", execute(markContent + "\n" + "mark('" + key + ckey + "','" + tex + "')"));
            String authKey = execute(markContent + "\n" + "gkt('" + key + "')");
            BigDecimal bd1 = new BigDecimal(authKey);
            String s = bd1.toPlainString();
            String encData = execute(markContent + "\n" + "gdk('" + key + "',0x1)");
            newHeader = new HashMap<>();
            newHeader.put("Host", "api.52wyb.com");
            newHeader.put("authkey", s);
            newHeader.put("encdata", encData);
            newHeader.put("sec-ch-ua-mobile", "?0");
            newHeader.put("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/95.0.4638.69 Safari/537.36");
            newHeader.put("content-type", "application/x-www-form-urlencoded; charset=UTF-8");
            newHeader.put("accept", "application/json, text/javascript, */*; q=0.01");
            newHeader.put("x-requested-with", "XMLHttpRequest");
            newHeader.put("sec-ch-ua", "\"Chromium\";v=\"21\", \" Not;A Brand\";v=\"99\"");
            newHeader.put("sec-ch-ua-platform", "\"Windows\"");
            newHeader.put("version", "0.4.2");
            newHeader.put("origin", "https://api.52wyb.com");
            newHeader.put("sec-fetch-site", "same-origin");
            newHeader.put("sec-fetch-mode", "cors");
            newHeader.put("sec-fetch-dest", "empty");
            newHeader.put("accept-language", "zh-CN,zh;q=0.9");
            String post = OkHttpUtil.post("https://api.52wyb.com/apis/get.json", postData, newHeader);
            JSONObject res = new JSONObject(post);
            if (res.getInt("success") == 1) {
                JSONObject streams = res.getJSONObject("streams");
                JSONArray segs = streams.getJSONArray("segs");
                if (segs.length() > 0) {
                    JSONObject jsonObject = segs.getJSONObject(0);
                    return Result.get().url(jsonObject.getString("url")).header(header).string();
                }
            }
        }

        return Result.get().url(url).header(header).string();
    }

    public String execute(String script) {
        Object result = null;
        try {
            //预编译
            result = engine.eval(script);
            return result.toString();
        } catch (Exception e) {
            Log.e("e", e.toString());
            throw new RuntimeException(e);
        }
    }
}
