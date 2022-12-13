package com.github.catvod.net;

import com.github.catvod.crawler.Spider;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Dns;
import okhttp3.OkHttpClient;
import okhttp3.Response;

public class OkResponse {

    public static final String METHOD_GET = "GET";
    public static final String METHOD_POST = "POST";

    private static final Object lockO = new Object();
    private static OkHttpClient defaultClient = null;
    private static final int DEFAULT_TIMEOUT = 15;

    public static OkHttpClient defaultClient() {
        synchronized (lockO) {
            if (defaultClient == null) {
                OkHttpClient.Builder builder = new OkHttpClient.Builder().dns(safeDns()).readTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS).writeTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS).connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS).retryOnConnectionFailure(true).sslSocketFactory(new SSLSocketFactoryCompat(), SSLSocketFactoryCompat.trustAllCert);
//                Interceptor loggingInterceptor = new HttpLoggingInterceptor();
//                builder.addInterceptor(loggingInterceptor);
                defaultClient = builder.build();
            }
            return defaultClient;
        }
    }

    public static Dns safeDns() {
        try {
            return (Dns) Spider.class.getMethod("safeDns").invoke(null);
        } catch (Exception e) {
            return Dns.SYSTEM;
        }
    }

    public static Response get(OkHttpClient client, String url, String tag, Map<String, String> paramsMap, Map<String, String> headerMap, Map<String, List<String>> respHeaderMap, String httpMethod) {
        OKCallBack.OKCallBackDefault callback = new OKCallBack.OKCallBackDefault() {
            @Override
            public Response onParseResponse(Call call, Response response) {
                return response;
            }
        };
        OKRequest req = new OKRequest(httpMethod, url, paramsMap, headerMap, callback);
        req.setTag(tag);
        req.execute(client);
        return callback.getResult();
    }

    public static Response get(String url) {
        return get(url, null);
    }

    public static Response get(String url, Map<String, String> headerMap) {
        return get(url, headerMap, null);
    }

    public static Response get(String url, Map<String, String> headerMap, Map<String, List<String>> respHeaderMap) {
        return get(url, null, headerMap, respHeaderMap);
    }

    public static Response get(String url, Map<String, String> paramsMap, Map<String, String> headerMap, Map<String, List<String>> respHeaderMap) {
        return get(url, null, paramsMap, headerMap, respHeaderMap);
    }

    public static Response get(String url, String tag, Map<String, String> headerMap) {
        return get(url, tag, null, headerMap, null);
    }

    public static Response get(String url, String tag, Map<String, String> paramsMap, Map<String, String> headerMap, Map<String, List<String>> respHeaderMap) {
        return get(defaultClient(), url, tag, paramsMap, headerMap, respHeaderMap, OkResponse.METHOD_GET);
    }

    public static Response post(String url) {
        return post(url, null);
    }

    public static Response post(String url, Map<String, String> paramsMap) {
        return post(url, paramsMap, null);
    }

    public static Response post(String url, Map<String, String> paramsMap, Map<String, String> headerMap) {
        return post(url, paramsMap, headerMap, null);
    }

    public static Response post(String url, Map<String, String> paramsMap, Map<String, String> headerMap, Map<String, List<String>> respHeaderMap) {
        return get(defaultClient(), url, null, paramsMap, headerMap, respHeaderMap, METHOD_POST);
    }

    public static String postJson(String url, String json) {
        return postJson(url, json, null);
    }

    public static String postJson(String url, String json, Map<String, String> headerMap) {
        OKCallBack.OKCallBackString callback = new OKCallBack.OKCallBackString();
        new OKRequest(METHOD_POST, url, json, headerMap, callback).execute(defaultClient());
        return callback.getResult();
    }

    public static void cancel(OkHttpClient client, Object tag) {
        if (client == null || tag == null) return;
        for (Call call : client.dispatcher().queuedCalls()) {
            if (tag.equals(call.request().tag())) {
                call.cancel();
            }
        }
        for (Call call : client.dispatcher().runningCalls()) {
            if (tag.equals(call.request().tag())) {
                call.cancel();
            }
        }
    }

    public static void cancel(Object tag) {
        cancel(defaultClient(), tag);
    }
}
