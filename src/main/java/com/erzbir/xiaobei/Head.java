package com.erzbir.xiaobei;

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
    private String user_agent;
    private String accept;
    private String accept_language;
    private String accept_encoding;
    private String content_type;

    @Override
    public String toString() {
        return "Head{" +
                "userAgent='" + user_agent + '\'' +
                ", accept='" + accept + '\'' +
                ", acceptLanguage='" + accept_language + '\'' +
                ", acceptEncoding='" + accept_encoding + '\'' +
                ", contentType='" + content_type + '\'' +
                '}';
    }
}
