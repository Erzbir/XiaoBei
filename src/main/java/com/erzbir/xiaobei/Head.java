package com.erzbir.xiaobei;

import com.google.gson.JsonObject;

/**
 * @Author: Erzbir
 * @Date: 2022/9/5 12:16
 * @<code> 请求头解析到这个类 </code>
 */
public class Head {
    private String userAgent = null;
    private String accept = null;
    private String acceptLanguage = null;
    private String acceptEncoding = null;
    private String contentType = null;

    public Head() {

    }

    public Head(JsonObject headJson) {
        userAgent = headJson.get("user-agent").getAsString();
        accept = headJson.get("accept").getAsString();
        acceptLanguage = headJson.get("accept-language").getAsString();
        acceptEncoding = headJson.get("accept-encoding").getAsString();
        contentType = headJson.get("content-type").getAsString();

    }


    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getAccept() {
        return accept;
    }

    public void setAccept(String accept) {
        this.accept = accept;
    }

    public String getAcceptLanguage() {
        return acceptLanguage;
    }

    public void setAcceptLanguage(String acceptLanguage) {
        this.acceptLanguage = acceptLanguage;
    }

    public String getAcceptEncoding() {
        return acceptEncoding;
    }

    public void setAcceptEncoding(String acceptEncoding) {
        this.acceptEncoding = acceptEncoding;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    @Override
    public String toString() {
        return "Head{" +
                "userAgent='" + userAgent + '\'' +
                ", accept='" + accept + '\'' +
                ", acceptLanguage='" + acceptLanguage + '\'' +
                ", acceptEncoding='" + acceptEncoding + '\'' +
                ", contentType='" + contentType + '\'' +
                '}';
    }
}
