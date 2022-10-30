package com.erzbir.xiaobei;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;

/**
 * @Author: Erzbir
 * @Date: 2022/9/5 12:16
 * @<code> 请求头解析到这个类 </code>
 */
@Setter
@Getter
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
