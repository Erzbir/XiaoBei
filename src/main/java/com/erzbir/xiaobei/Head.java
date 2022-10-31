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
    private String user_agent = null;
    private String accept = null;
    private String accept_language = null;
    private String accept_encoding = null;
    private String content_type = null;


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
