package com.github.catvod.spider;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import com.github.catvod.crawler.Spider;
import com.github.catvod.crawler.SpiderDebug;
import com.github.catvod.net.OkHttpUtil;
import com.github.catvod.utils.Misc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import rxhttp.wrapper.annotations.NonNull;

public class IQIYI extends Spider {
    private HashMap<String, String> header;
    private JSONObject ext;
    private String extend;

    private String getCookie(String cookie) {
        if (TextUtils.isEmpty(cookie))
            return "";
        if (cookie.startsWith("http")) return OkHttpUtil.string(cookie).replace("\n", "");
        return cookie;
    }

    private void setHeader() throws Exception {
        header.put("cookie", getCookie(ext.getString("cookie")));
        header.put("User-Agent", Misc.CHROME);
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


    public String categoryContent(String str, String str2, boolean z, HashMap<String, String> hashMap) {
        try {
            StringBuilder str3 = new StringBuilder("https://pcw-api.iqiyi.com/search/video/videolists?channel_id=" + str + "&is_purchase=&mode=24&pageNum=" + str2 + "&pageSize=24");
            if (hashMap != null) {
                Set<String> keySet = hashMap.keySet();
                ArrayList<String> arrayList = new ArrayList<>();
                for (String str4 : keySet) {
                    String trim = Objects.requireNonNull(hashMap.get(str4)).trim();
                    if (str4.matches("\\d+")) {
                        arrayList.add(trim + ";must");
                    } else {
                        str3.append("&").append(str4).append("=").append(trim);
                    }
                }
                str3.append("&three_category_id=").append(join(",", arrayList));
            }
            String h = OkHttpUtil.string(str3.toString(), header);
            JSONObject jSONObject = new JSONObject();
            try {
                JSONArray optJSONArray = Objects.requireNonNull(new JSONObject(h).optJSONObject("data")).optJSONArray("list");
                JSONArray jSONArray = new JSONArray();
                int i = 0;
                while (i < optJSONArray.length()) {
                    JSONObject optJSONObject = optJSONArray.optJSONObject(i);
                    String optString = optJSONObject.optString("name");
                    String q = Misc.fixUrl(str3.toString(), optJSONObject.optString("imageUrl"));
                    String optString2 = optJSONObject.optString("score");
                    String optString3 = optJSONObject.optString("playUrl");
                    if (!optJSONObject.optString("albumId").equals("")) {
                        if (optJSONObject.optInt("sourceId") != 0) {
                            optString3 = "/video/video/baseinfo/" + optJSONObject.optString("tvId") + "?userInfo=verify&jsonpCbName=videoInfo39";
                        } else {
                            optString3 = "/albums/album/avlistinfo?aid=" + optJSONObject.optString("albumId") + "&size=5000&page=1&url=" + optString3;
                        }
                    } else if (optJSONObject.optLong("tvId") != 0) {
                        optString3 = "/video/video/baseinfo/" + optJSONObject.optString("tvId") + "?userInfo=verify&jsonpCbName=videoInfo39";
                    }
                    JSONObject jSONObject2 = new JSONObject();
                    jSONObject2.put("vod_id", optString3);
                    jSONObject2.put("vod_name", optString);
                    jSONObject2.put("vod_pic", q);
                    jSONObject2.put("vod_remarks", optString2);
                    jSONArray.put(jSONObject2);
                    i++;
                }
                jSONObject.put("page", str2);
                jSONObject.put("pagecount", Integer.MAX_VALUE);
                jSONObject.put("limit", 90);
                jSONObject.put("total", Integer.MAX_VALUE);
                jSONObject.put("list", jSONArray);
            } catch (Exception e) {
                SpiderDebug.log(e);
            }
            return jSONObject.toString(4);
        } catch (Exception e2) {
            SpiderDebug.log(e2);
            return "";
        }
    }

    public String detailContent(List<String> list) {
        try {
            String url = "https://pcw-api.iqiyi.com" + list.get(0);
            JSONObject jSONObject = new JSONObject(OkHttpUtil.string(url, header)).getJSONObject("data");
            JSONObject jSONObject2 = new JSONObject();
            JSONObject optJSONObject = jSONObject.optJSONArray("epsodelist") != null ? Objects.requireNonNull(jSONObject.optJSONArray("epsodelist")).optJSONObject(0) : jSONObject;
            jSONObject2.put("vod_id", list.get(0));
            jSONObject2.put("vod_name", optJSONObject.optString("name").replaceAll("第\\d+(?:集|期)", ""));
            jSONObject2.put("vod_pic", Misc.fixUrl(url, optJSONObject.optString("imageUrl")));
            JSONObject optJSONObject2 = optJSONObject.optJSONObject("people");
            JSONArray optJSONArray = optJSONObject2.optJSONArray("main_charactor");
            if (optJSONArray == null) {
                optJSONArray = optJSONObject2.optJSONArray("producer");
            }
            if (optJSONArray == null) {
                optJSONArray = optJSONObject2.optJSONArray("guest");
            }
            if (optJSONArray == null) {
                optJSONArray = optJSONObject2.optJSONArray("singer");
            }
            ArrayList<String> arrayList = new ArrayList<>();
            if (optJSONArray != null) {
                for (int i = 0; i < optJSONArray.length(); i++) {
                    arrayList.add(optJSONArray.optJSONObject(i).optString("name"));
                }
            }
            JSONArray optJSONArray2 = optJSONObject2.optJSONArray("director");
            if (optJSONArray2 == null) {
                optJSONArray2 = optJSONObject2.optJSONArray("screen_writer");
            }
            if (optJSONArray2 == null) {
                optJSONArray2 = optJSONObject2.optJSONArray("host");
            }
            ArrayList<String> arrayList2 = new ArrayList<>();
            if (optJSONArray2 != null) {
                for (int i2 = 0; i2 < optJSONArray2.length(); i2++) {
                    arrayList2.add(optJSONArray2.optJSONObject(i2).optString("name"));
                }
            }
            jSONObject2.put("vod_remarks", optJSONObject.optString("duration"));
            jSONObject2.put("vod_actor", join(",", arrayList));
            jSONObject2.put("vod_director", join(",", arrayList2));
            jSONObject2.put("vod_content", optJSONObject.optString("description"));
            JSONArray optJSONArray3 = jSONObject.optJSONArray("epsodelist");
            if (optJSONArray3 == null) {
                optJSONArray3 = new JSONArray();
                optJSONArray3.put(jSONObject);
            }
            ArrayList<String> arrayList3 = new ArrayList<>();
            for (int i3 = 0; i3 < optJSONArray3.length(); i3++) {
                JSONObject optJSONObject3 = optJSONArray3.optJSONObject(i3);
                arrayList3.add(optJSONObject3.optString("order") + " " + optJSONObject3.optString("subtitle") + "$" + optJSONObject3.optString("playUrl"));
            }
            jSONObject2.put("vod_play_from", "iqiyi");
            jSONObject2.put("vod_play_url", join("#", arrayList3));
            JSONObject jSONObject3 = new JSONObject();
            JSONArray jSONArray = new JSONArray();
            jSONArray.put(jSONObject2);
            jSONObject3.put("list", jSONArray);
            return jSONObject3.toString(4);
        } catch (Exception e) {
            SpiderDebug.log(e);
            return "";
        }
    }

    public String homeContent(boolean filter) {
        try {
            String str = "https://pcw-api.iqiyi.com/search/video/videolists?channel_id=2&is_purchase=&mode=24&pageNum=1&pageSize=24&data_type=1&site=iqiyi";
            String h = OkHttpUtil.string(str, header);
            JSONObject jSONObject = new JSONObject();
            jSONObject.put("class", ext.getJSONArray("classes").toString());
            if (filter) {
                jSONObject.put("filters", ext.getJSONObject("filter"));
            }
            try {
                JSONArray optJSONArray = Objects.requireNonNull(new JSONObject(h).optJSONObject("data")).optJSONArray("list");
                JSONArray jSONArray = new JSONArray();
                int i = 0;
                while (i < optJSONArray.length()) {
                    JSONObject optJSONObject = optJSONArray.optJSONObject(i);
                    String optString = optJSONObject.optString("name");
                    String q = Misc.fixUrl(str, optJSONObject.optString("imageUrl"));
                    String optString2 = optJSONObject.optString("score");
                    String optString3 = optJSONObject.optString("playUrl");
                    if (!optJSONObject.optString("albumId").equals("")) {
                        if (optJSONObject.optInt("sourceId") != 0) {
                            optString3 = "/video/video/baseinfo/" + optJSONObject.optString("tvId") + "?userInfo=verify&jsonpCbName=videoInfo39";
                        } else {
                            optString3 = "/albums/album/avlistinfo?aid=" + optJSONObject.optString("albumId") + "&size=5000&page=1&url=" + optString3;
                        }
                    } else if (optJSONObject.optLong("tvId") != 0) {
                        optString3 = "/video/video/baseinfo/" + optJSONObject.optString("tvId") + "?userInfo=verify&jsonpCbName=videoInfo39";
                    }
                    JSONObject jSONObject2 = new JSONObject();
                    jSONObject2.put("vod_id", optString3);
                    jSONObject2.put("vod_name", optString);
                    jSONObject2.put("vod_pic", q);
                    jSONObject2.put("vod_remarks", optString2);
                    jSONArray.put(jSONObject2);
                    i++;
                }
                jSONObject.put("list", jSONArray);
            } catch (Exception e) {
                SpiderDebug.log(e);
            }
            return jSONObject.toString(4);
        } catch (Exception e2) {
            SpiderDebug.log(e2);
            return "";
        }
    }

    public String homeVideoContent() {
        try {
            JSONArray jSONArray = new JSONObject(OkHttpUtil.string("https://pcw-api.iqiyi.com/api.php/app/index_video?token=", header)).getJSONArray("list");
            JSONArray jSONArray2 = new JSONArray();
            for (int i = 0; i < jSONArray.length(); i++) {
                JSONArray jSONArray3 = jSONArray.getJSONObject(i).getJSONArray("vlist");
                int i2 = 0;
                while (i2 < jSONArray3.length() && i2 < 6) {
                    JSONObject jSONObject = jSONArray3.getJSONObject(i2);
                    JSONObject jSONObject2 = new JSONObject();
                    jSONObject2.put("vod_id", jSONObject.optString("vod_id"));
                    jSONObject2.put("vod_name", jSONObject.optString("vod_name"));
                    jSONObject2.put("vod_pic", jSONObject.optString("vod_pic"));
                    jSONObject2.put("vod_remarks", jSONObject.optString("vod_remarks"));
                    jSONArray2.put(jSONObject2);
                    i2++;
                }
            }
            JSONObject jSONObject3 = new JSONObject();
            jSONObject3.put("list", jSONArray2);
            return jSONObject3.toString();
        } catch (Exception e) {
            SpiderDebug.log(e);
            return "";
        }
    }

    public void init(Context context, String extend) {
        try {
            this.extend = extend;
            this.header = new HashMap<>();
            fetchRule();
        } catch (Exception e) {
            SpiderDebug.log(e);
        }
    }

    public String join(@NonNull CharSequence charSequence, @NonNull Iterable iterable) {
        Iterator it = iterable.iterator();
        if (!it.hasNext()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(it.next());
        while (it.hasNext()) {
            sb.append(charSequence);
            sb.append(it.next());
        }
        return sb.toString();
    }

    public String playerContent(String str, String str2, List<String> list) {
        try {
            JSONObject jSONObject = new JSONObject();
            try {
                jSONObject.put("parse", 1);
                jSONObject.put("jx", "1");
                jSONObject.put("url", str2);
                return jSONObject.toString();
            } catch (Exception e) {
                SpiderDebug.log(e);
                return jSONObject.toString();
            }
        } catch (Exception e2) {
            SpiderDebug.log(e2);
            return "";
        }
    }

    public String searchContent(String str, boolean quick) {
        try {
            String url = "https://search.video.iqiyi.com/o?if=html5&key=" + str + "&pageNum=1&pos=1&pageSize=20";
            String json = OkHttpUtil.string(url, header);
            JSONArray optJSONArray = Objects.requireNonNull(new JSONObject(json).optJSONObject("data")).optJSONArray("docinfos");

            JSONArray videos = new JSONArray();
            for (int i = 0; i < optJSONArray.length(); i++) {
                JSONObject optJSONObject = optJSONArray.getJSONObject(i).optJSONObject("albumDocInfo");
                JSONObject video = new JSONObject();
                String optString = optJSONObject.optString("albumLink");
                video.put("vod_id", "/albums/album/avlistinfo?aid=" + optJSONObject.optString("qipu_id") + "&size=5000&page=1&url=" + optString);
                video.put("vod_name", optJSONObject.optString("albumTitle"));
                video.put("vod_pic", optJSONObject.optString("albumImg"));
                video.put("vod_remarks", optJSONObject.optString("releaseDate"));
                videos.put(video);
            }
            JSONObject jSONObject2 = new JSONObject();
            jSONObject2.put("list", videos);
            return jSONObject2.toString();
        } catch (Exception e) {
            SpiderDebug.log(e);
            return "";
        }
    }
}
