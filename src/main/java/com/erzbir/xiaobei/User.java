package com.erzbir.xiaobei;

import com.google.gson.JsonObject;

/**
 * @Author: Erzbir
 * @Date: 2022/9/5 12:18
 * @<code> 用户信息解析到这个类 </code>
 */
public class User {
    private String USERNAME; // 用户名
    private String PASSWORD; // 密码
    private String LOCATION; // 坐标基准点
    private String PLACE; // 具体位置信息, 格式为: 中国-四川省-成都市-成华区
    private String ACCEPT_EMAIL; // 收件邮箱

    public User() {

    }

    public User(JsonObject userObject) {
        USERNAME = userObject.get("username").getAsString();
        PASSWORD = userObject.get("password").getAsString();
        LOCATION = userObject.get("location").getAsString();
        PLACE = userObject.get("place").getAsString();
        ACCEPT_EMAIL = userObject.get("email").getAsString();
        USERNAME = userObject.get("username").getAsString();
    }

    public String getUSERNAME() {
        return USERNAME;
    }

    public void setUSERNAME(String USERNAME) {
        this.USERNAME = USERNAME;
    }

    public String getPASSWORD() {
        return PASSWORD;
    }

    public void setPASSWORD(String PASSWORD) {
        this.PASSWORD = PASSWORD;
    }

    public String getLOCATION() {
        return LOCATION;
    }

    public void setLOCATION(String LOCATION) {
        this.LOCATION = LOCATION;
    }

    public String getPLACE() {
        return PLACE;
    }

    public void setPLACE(String PLACE) {
        this.PLACE = PLACE;
    }

    public String getACCEPT_EMAIL() {
        return ACCEPT_EMAIL;
    }

    public void setACCEPT_EMAIL(String ACCEPT_EMAIL) {
        this.ACCEPT_EMAIL = ACCEPT_EMAIL;
    }

    @Override
    public String toString() {
        return "User{" +
                "USERNAME='" + USERNAME + '\'' +
                ", PASSWORD='" + PASSWORD + '\'' +
                ", LOCATION='" + LOCATION + '\'' +
                ", PLACE='" + PLACE + '\'' +
                ", ACCEPT_EMAIL='" + ACCEPT_EMAIL + '\'' +
                '}';
    }
}
