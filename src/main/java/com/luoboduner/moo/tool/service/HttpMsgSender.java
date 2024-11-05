package com.luoboduner.moo.tool.service;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import com.luoboduner.moo.tool.App;
import com.luoboduner.moo.tool.bean.HttpMsg;
import lombok.extern.slf4j.Slf4j;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;

import java.net.HttpCookie;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * <pre>
 * Http消息发送器
 * </pre>
 *
 * @author <a href="https://github.com/rememberber">Zhou Bo</a>
 * @since 2019/7/16.
 */
@Slf4j
public class HttpMsgSender {

    private HttpMsgMaker httpMsgMaker;

    public volatile static OkHttpClient okHttpClient;

    public volatile static Proxy proxy;

    public HttpMsgSender() {
        httpMsgMaker = new HttpMsgMaker();
        okHttpClient = getOkHttpClient();
    }

    public HttpSendResult send() {
        return sendUseOkHttp();
    }

    public HttpSendResult sendUseHutool() {
        HttpSendResult sendResult = new HttpSendResult();
        HttpResponse httpResponse;
        try {
            HttpMsg httpMsg = httpMsgMaker.makeMsg();
            HttpRequest httpRequest;
            switch (HttpMsgMaker.method) {
                case "GET":
                    httpRequest = HttpRequest.get(httpMsg.getUrl());
                    break;
                case "POST":
                    httpRequest = HttpRequest.post(httpMsg.getUrl());
                    break;
                case "PUT":
                    httpRequest = HttpRequest.put(httpMsg.getUrl());
                    break;
                case "PATCH":
                    httpRequest = HttpRequest.patch(httpMsg.getUrl());
                    break;
                case "DELETE":
                    httpRequest = HttpRequest.delete(httpMsg.getUrl());
                    break;
                case "HEAD":
                    httpRequest = HttpRequest.head(httpMsg.getUrl());
                    break;
                case "OPTIONS":
                    httpRequest = HttpRequest.options(httpMsg.getUrl());
                    break;
                default:
                    httpRequest = HttpRequest.get(httpMsg.getUrl()).form(httpMsg.getParamMap());
            }
            if (httpMsg.getParamMap() != null && !httpMsg.getParamMap().isEmpty()) {
                httpRequest.form(httpMsg.getParamMap());
            }
            if (httpMsg.getHeaderMap() != null && !httpMsg.getHeaderMap().isEmpty()) {
                for (Map.Entry<String, Object> entry : httpMsg.getHeaderMap().entrySet()) {
                    httpRequest.header(entry.getKey(), (String) entry.getValue());
                }
            }
            if (httpMsg.getCookies() != null && !httpMsg.getCookies().isEmpty()) {
                HttpCookie[] cookies = ArrayUtil.toArray(httpMsg.getCookies(), HttpCookie.class);
                httpRequest.cookie(cookies);
            }
            if (StringUtils.isNotEmpty(httpMsg.getBody())) {
                httpRequest.body(httpMsg.getBody());
            }
            if (App.config.isHttpUseProxy()) {
                httpRequest.setProxy(getProxy());
            }

            httpResponse = httpRequest.execute();
            if (!httpResponse.isOk()) {
                sendResult.setSuccess(false);
                sendResult.setInfo(httpResponse.toString());
                return sendResult;
            }
        } catch (Exception e) {
            sendResult.setSuccess(false);
            sendResult.setInfo(e.getMessage());
            log.error(e.toString());
            return sendResult;
        }
        StringBuilder headerBuilder = StrUtil.builder();
        for (Map.Entry<String, List<String>> entry : httpResponse.headers().entrySet()) {
            headerBuilder.append(entry).append(StrUtil.CRLF);
        }
        sendResult.setHeaders(headerBuilder.toString());

        String body = httpResponse.body();
        sendResult.setInfo(body);
        if (body != null && body.startsWith("{") && body.endsWith("}")) {
            try {
                body = JSONUtil.toJsonPrettyStr(body);
            } catch (Exception e) {
                log.error(e.toString());
            }
        }
        sendResult.setBody(body);

        StringBuilder cookiesBuilder = StrUtil.builder();
        List<String> headerList = httpResponse.headerList(Header.SET_COOKIE.toString());
        if (headerList != null) {
            for (String cookieStr : headerList) {
                cookiesBuilder.append(cookieStr).append(StrUtil.CRLF);
            }
        }

        sendResult.setCookies(cookiesBuilder.toString());

        sendResult.setSuccess(true);
        return sendResult;
    }

    public HttpSendResult sendUseOkHttp() {
        HttpSendResult sendResult = new HttpSendResult();
        try {
            HttpMsg httpMsg = httpMsgMaker.makeMsg();

            Request.Builder requestBuilder = new Request.Builder();

            RequestBody requestBody = RequestBody.create("", MediaType.get("text/plain"));
            if (!"GET".equals(HttpMsgMaker.method) && httpMsg.getParamMap() != null && !httpMsg.getParamMap().isEmpty()) {
                FormBody.Builder formBodyBuilder = new FormBody.Builder();
                for (Map.Entry<String, Object> paramEntry : httpMsg.getParamMap().entrySet()) {
                    formBodyBuilder.add(paramEntry.getKey(), (String) paramEntry.getValue());
                }
                requestBody = formBodyBuilder.build();
            } else if (!"GET".equals(HttpMsgMaker.method) && StringUtils.isNotEmpty(httpMsg.getBody())) {
                String bodyType = HttpMsgMaker.bodyType;
                MediaType mediaType = MediaType.get(bodyType + "; charset=utf-8");
                requestBody = RequestBody.create(httpMsg.getBody(), mediaType);
            }

            if (httpMsg.getHeaderMap() != null && !httpMsg.getHeaderMap().isEmpty()) {
                for (Map.Entry<String, Object> headerEntry : httpMsg.getHeaderMap().entrySet()) {
                    requestBuilder.addHeader(headerEntry.getKey(), (String) headerEntry.getValue());
                }
            }
            if (httpMsg.getCookies() != null && !httpMsg.getCookies().isEmpty()) {
                requestBuilder.addHeader(Header.COOKIE.toString(), cookieHeader(httpMsg.getCookies()));
            }
            switch (HttpMsgMaker.method) {
                case "GET":
                    HttpUrl.Builder urlBuilder = HttpUrl.parse(httpMsg.getUrl()).newBuilder();
                    if (httpMsg.getParamMap() != null && !httpMsg.getParamMap().isEmpty()) {
                        for (Map.Entry<String, Object> paramEntry : httpMsg.getParamMap().entrySet()) {
                            urlBuilder.addQueryParameter(paramEntry.getKey(), (String) paramEntry.getValue());
                        }
                    }
                    requestBuilder.url(urlBuilder.build()).get();
                    break;
                case "POST":
                    requestBuilder.url(httpMsg.getUrl()).post(requestBody);
                    break;
                case "PUT":
                    requestBuilder.url(httpMsg.getUrl()).put(requestBody);
                    break;
                case "PATCH":
                    requestBuilder.url(httpMsg.getUrl()).patch(requestBody);
                    break;
                case "DELETE":
                    requestBuilder.url(httpMsg.getUrl()).delete(requestBody);
                    break;
                case "HEAD":
                    requestBuilder.url(httpMsg.getUrl()).head();
                    break;
                case "OPTIONS":
                    return sendUseHutool();
                default:
                    requestBuilder.url(httpMsg.getUrl());
            }

            Request request = requestBuilder.build();

            Response response = okHttpClient.newCall(request).execute();
            if (!response.isSuccessful()) {
                sendResult.setSuccess(false);
                sendResult.setInfo(response.toString());
                return sendResult;
            }

            String responseBody = "";
            if (response.body() != null) {
                responseBody = response.body().string();
            }
            sendResult.setInfo(responseBody);
            if (responseBody.startsWith("{") && responseBody.endsWith("}")) {
                try {
                    responseBody = JSONUtil.toJsonPrettyStr(responseBody);
                } catch (Exception e) {
                    log.error(e.toString());
                }
            }
            sendResult.setBody(responseBody);

            sendResult.setHeaders(response.headers().toString());

            StringBuilder cookiesBuilder = StrUtil.builder();
            List<String> headerList = response.headers(Header.SET_COOKIE.toString());
            for (String cookieStr : headerList) {
                cookiesBuilder.append(cookieStr).append(StrUtil.CRLF);
            }

            sendResult.setCookies(cookiesBuilder.toString());

            sendResult.setSuccess(true);
            return sendResult;
        } catch (Exception e) {
            sendResult.setSuccess(false);
            sendResult.setInfo(e.toString());
            log.error(e.toString());
            return sendResult;
        }
    }

    private String cookieHeader(List<HttpCookie> cookies) {
        StringBuilder cookieHeader = new StringBuilder();
        for (int i = 0, size = cookies.size(); i < size; i++) {
            if (i > 0) {
                cookieHeader.append("; ");
            }
            HttpCookie cookie = cookies.get(i);
            cookieHeader.append(cookie.getName()).append('=').append(cookie.getValue());
        }
        return cookieHeader.toString();
    }

    private static Proxy getProxy() {
        if (proxy == null) {
            synchronized (HttpMsgSender.class) {
                if (proxy == null) {
                    proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(App.config.getHttpProxyHost(), Integer.parseInt(App.config.getHttpProxyPort())));
                }
            }
        }
        return proxy;
    }

    public static OkHttpClient getOkHttpClient() {
        if (okHttpClient == null) {
            synchronized (HttpMsgSender.class) {
                if (okHttpClient == null) {
                    OkHttpClient.Builder builder = new OkHttpClient.Builder();
                    builder.connectTimeout(3, TimeUnit.MINUTES);
                    if (App.config.isHttpUseProxy()) {
                        builder.proxy(getProxy());
                    }

                    okHttpClient = builder.build();
                }
            }
        }
        return okHttpClient;
    }
}
