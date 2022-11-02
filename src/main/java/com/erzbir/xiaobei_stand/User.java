package com.erzbir.xiaobei_stand;

import lombok.Getter;
import lombok.Setter;

/**
 * @Author: Erzbir
 * @Date: 2022/9/5 12:18
 * @<code> 用户信息解析到这个类 </code>
 */

@Getter
@Setter
public class User {
    private String username; // 用户名
    private String password; // 密码
    private String location; // 坐标基准点
    private String place; // 具体位置信息, 格式为: 中国-四川省-成都市-成华区
    private String email; // 收件邮箱

    @Override
    public String toString() {
        return "User{" +
                "USERNAME='" + username + '\'' +
                ", PASSWORD='" + password + '\'' +
                ", LOCATION='" + location + '\'' +
                ", PLACE='" + place + '\'' +
                ", ACCEPT_EMAIL='" + email + '\'' +
                '}';
    }
}
