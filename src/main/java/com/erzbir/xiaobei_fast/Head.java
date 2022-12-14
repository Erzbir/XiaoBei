package com.erzbir.xiaobei_fast;

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
                "user_agent='" + user_agent + '\'' +
                ", accept='" + accept + '\'' +
                ", accept_language='" + accept_language + '\'' +
                ", accept_encoding='" + accept_encoding + '\'' +
                ", content_type='" + content_type + '\'' +
                '}';
    }
}
