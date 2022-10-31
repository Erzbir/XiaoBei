package com.erzbir.xiaobei;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalTime;
import java.util.Base64;
import java.util.Random;
import java.util.concurrent.FutureTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Author: Erzbir
 * @Date: 2022/8/31 23:10
 * 获取地理位置, 上报信息, 登录等各种操作
 */

// 获取坐标: https://api.xiaobaibk.com/api/map/
@Getter
@Setter
public class Action {
    private static final String Url = "https://xiaobei.yinghuaonline.com/xiaobei-api/";
    private static final String captchaUrl = Url + "captchaImage"; // 验证码
    private static final String loginUrl = Url + "login"; // 登录
    private static final String healthUrl = Url + "student/health"; // 打卡
    public FutureTask<Boolean> futureTask;
    public String temp; // 反馈信息
    private User user; // 用户
    private Head header; // 请求头
    // 以下是服务器返回内容, 用于POST操作
    private String showCode; // 通过verify获得
    private String uuid; // 通过verify获得
    private String healthJson;  // 建康信息报文
    private String authorization; // 验证码通过login获得
    private SendMessage sendMessage; // 发送消息的类

    public Action(User user, Head header) {
        this.user = user;
        this.header = header;
        this.sendMessage = new SendMessage(user, Main.jsonObject);
        this.healthJson = null;
        this.showCode = null;
        this.authorization = null;
        this.uuid = null;

    }


    // 将关流抽取成方法
    private static void closeStream(HttpURLConnection connection, OutputStream httpOut, Closeable httpIn) {
        try {
            if (httpOut != null) {
                httpOut.close();
            }
            if (httpIn != null) {
                httpIn.close();
            }
            if (connection != null) {
                connection.disconnect();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @<code> 坐标为空则不执行, 如果位置为空则根据坐标获取地址 </code>
     */
    void getPlace() {
        StringBuilder result = new StringBuilder();
        String jsonString;
        ByteArrayOutputStream out = null;
        InputStream in = null;
        String[] lc = user.getLocation().split(",");
        String location = lc[1] + "," + lc[0];
        URL url;
        HttpURLConnection connection;
        try {
            url = new URL("https://api.xiaobaibk.com/api/location/?location=" + location);
            connection = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        try {
            connection.setRequestMethod("GET");
            connection.setRequestProperty("user-agent", header.getUser_agent());
            connection.setRequestProperty("accept", header.getAccept());
            connection.connect();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        try {
            if (connection.getResponseCode() != 200) {
                return;
            }
            in = connection.getInputStream();
            byte[] buff = new byte[1024];
            out = new ByteArrayOutputStream();
            int len;
            while ((len = in.read(buff)) != -1) {
                out.write(buff, 0, len);
            }
            jsonString = out.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        } finally {
            closeStream(connection, out, in);
        }
        if (jsonString == null || jsonString.length() == 0) {
            return;
        }
        // 这里用正则匹配json而不是JsonObject, 只是练手, 其他类用了工具
        Pattern pattern = Pattern.compile("\"country\":\"(.*?)\".*?province\":\"(.*?)\",\"city\":\"(.*?)\".*?district\":\"(.*?)\"");
        Matcher matcher = pattern.matcher(jsonString);
        int i = 1;
        while (matcher.find()) {
            while (i < 5) {
                String s = matcher.group(i++);
                result.append(s).append("-");
            }
        }
        result.replace(result.length() - 1, result.length(), "");
        user.setPlace(result.toString());
    }

    /**
     * @<code> 需要注意的是坐标和位置不为空才开始执行 </code>
     */
    void getHealth() {
        String place = user.getPlace();
        if (place == null || place.isEmpty()) {
            // System.out.println("获取位置信息失败");
            temp = LocalTime.now() + "\t" + user.getUsername() + "获取位置信息失败";
            sendMessage.setMsg(temp);
            return;
        }
        Random random = new Random();
        String[] lc = user.getLocation().split(",");
        BigDecimal bigDecimal = BigDecimal.valueOf(random.nextDouble(367, 372) / 10).setScale(1, RoundingMode.DOWN);
        String temperature = String.valueOf(bigDecimal);
        int rand = random.nextInt(1111, 9999);
        String location_x = String.valueOf((double) rand / 100000 + Double.parseDouble(lc[0]));
        String location_y = String.valueOf((double) rand / 100000 + Double.parseDouble(lc[1]));
        String location = location_x + ',' + location_y;
        healthJson = "{" +
                "\"temperature\":\"" + temperature + "\"," +
                "\"coordinates\":\"" + place + "\"," +
                "\"location\":\"" + location + "\"," +
                "\"healthState\":" + "\"1\"" + "," +
                "\"dangerousRegion\":" + "\"2\"" + "," +
                "\"dangerousRegionRemark\":" + "\"\"" + "," +
                "\"contactSituation\":" + "\"2\"" + "," +
                "\"goOut\":" + "\"1\"" + "," +
                "\"goOutRemark\":" + "\"\"" + "," +
                "\"remark\":" + "\"无\"" + "," +
                "\"familySituation\":" + "\"1\"" +
                "}";
    }

    /**
     * @<code> 滑动验证, 没有用户名和密码则不运行 </code>
     * @GET请求头 {"user-agent": "xxxxx", "accept": "xxxxx", "acceptLanguage": "xxxx", "acceptEncoding": "xxxxx", "content-type": "xxxx"}
     * @返回的报文 data: {"msg":"操作成功","img":"xxxxxx","code":200,"showCode":"NM6B","uuid":"12udb12ud810sbc0w"}
     */

    void verify() {
        // 滑动验证
        // 开始网络请求(想起python是多么的简单, 当然有第三方包我没下)
        HttpURLConnection connection = null;
        InputStream in = null;
        ByteArrayOutputStream out = null;
        String jsonString = "";
        JsonObject jsonObject;
        // 请求头数据
        try {
            // 设置请求头, java设置请求头非常麻烦, 害
            connection = (HttpURLConnection) new URL(captchaUrl).openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("user-agent", header.getUser_agent());
            connection.setRequestProperty("accept", header.getAccept());
            connection.setRequestProperty("acceptLanguage", header.getAccept_language());
            connection.setRequestProperty("acceptEncoding", header.getAccept_encoding());
            connection.connect();
            if (connection.getResponseCode() != 200) {
                temp = LocalTime.now() + "\t" + user.getUsername() + "验证码获取失败";
                // System.out.println(temp);
                return;
            }
            in = connection.getInputStream();
            out = new ByteArrayOutputStream();
            byte[] buff = new byte[1024];
            int len;
            while ((len = in.read(buff)) != -1) {
                out.write(buff, 0, len);
            }
            jsonString = out.toString();
        } catch (IOException e) {
            e.printStackTrace();
            temp = LocalTime.now() + "\t" + user.getUsername() + "网络或服务器问题";
            // System.out.println(temp);

        } finally {
            sendMessage.setMsg(temp);
            closeStream(connection, out, in);
        }
        if (jsonString == null) {
            return;
        }
        // 获取uuid
        try {
            jsonObject = JsonParser.parseString(jsonString).getAsJsonObject();
            uuid = jsonObject.get("uuid").getAsString();
        } catch (Exception e) {
            e.printStackTrace();
            temp = LocalTime.now() + "\t" + user.getUsername() + "uuid获取失败, 应该是帐号不存在的问题";
            // System.out.println(temp);
            return;
        } finally {
            sendMessage.setMsg(temp);
        }
        showCode = jsonObject.get("showCode").getAsString();
        // 小北对密码只做了一个base64加密, 所以我们只需要加密这个就行了
        // 在抓到的报文中, 除了密码没看到其他加密的......
    }

    /**
     * @POST请求头 {"user-agent": "xxxxx", "accept": "xxxxx", "acceptLanguage": "xxxx", "acceptEncoding": "xxxxx", "content-type": "xxxx"}
     */
    void logIn() {
        try {
            if (showCode == null || showCode.isEmpty() || uuid == null || uuid.isEmpty()) {
                return;
            }
            HttpURLConnection connection;
            //密码做Base64加密, 小北唯一加密的就是这个
            Base64.Encoder encoder = Base64.getEncoder();
            String password = encoder.encodeToString(user.getPassword().getBytes());
            // 创建报文数据, 没错, 小北的报文就是如此简单
            String response =
                    "{" +
                            "\"username\":\"" + user.getUsername() + "\"," +
                            "\"password\":\"" + password + "\"," +
                            "\"showCode\":\"" + showCode + "\"," +
                            "\"uuid\":\"" + uuid + "\"" + "}";
            String res;
            try {
                connection = (HttpURLConnection) new URL(loginUrl).openConnection();
                res = post(connection, response);
                // {"username": "xxxxx", "password": "xxxxx", "uuid": "xxxxx", "showCode": xxxxx}
            } catch (IOException e) {
                e.printStackTrace();
                temp = LocalTime.now() + "\t" + user.getUsername() + "网络问题导致登录失败";
                // System.out.println(temp);
                return;
            }
            // 成功 {"msg":"操作成功","code":200,"token":"xxxx"}
            // 失败 {"msg":"用户不存在/密码错误","code":500}
            String code;
            String msg;
            JsonObject jsonObject;
            {
                jsonObject = JsonParser.parseString(res).getAsJsonObject();
                code = String.valueOf(jsonObject.get("code"));
                msg = String.valueOf(jsonObject.get("msg"));
            }
            if (!code.equals("200")) {
                temp = LocalTime.now() + "\t" + user.getUsername() + "登录失败, 原因: " + msg;
                // System.out.println(temp);
                return;
            }
            authorization = jsonObject.get("token").getAsString();
        } finally {
            sendMessage.setMsg(temp);
        }
    }


    // 将post操作抽取成方法, get操作比较少就不抽取了

    /**
     * @return boolean
     * @<code> 用于上报的的函数, 如果健康信息为空就不运行 </code>
     * @POST请求头 {"user-agent": "xxxxx", "accept": "xxxxx", "acceptLanguage": "xxxx", "acceptEncoding": "xxxxx", "authorization: "xxxxx", "content-type": "xxxx"}
     */
    boolean report() {
        try {
            if (healthJson == null || healthJson.isEmpty()) {
                // System.out.println(user.getUSERNAME() + "健康信息获取失败");
                temp = LocalTime.now() + "\t" + user.getUsername() + "健康信息获取失败";
                return false;
            }
            // System.out.println(user.getPLACE());
            HttpURLConnection connection;
            String res;
            try {
                connection = (HttpURLConnection) new URL(healthUrl).openConnection();
                res = post(connection, healthJson);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            String code;
            String msg;
            {
                JsonObject jsonObject = JsonParser.parseString(res).getAsJsonObject();
                code = jsonObject.get("code").getAsString();
                msg = jsonObject.get("msg").getAsString();
            }
            // 成功 return {'msg': '操作成功', 'code': 200}
            // 失败 {'msg': "xxxxx", 'code': 500}
            if (!code.equals("200")) {
                temp = LocalTime.now() + "\t" + user.getUsername() + "打卡失败, 失败原因: " + msg;
                // System.out.println(temp);
                return false;
            }
            temp = LocalTime.now() + "\t" + user.getUsername() + "打卡成功!!!" + msg;
        } finally {
            sendMessage.setMsg(temp);
        }

        // System.out.println(temp);
        return true;

    }

    /**
     * @param connection -HttpURLConnection对象
     * @param data       -要发送的数据
     * @return boolean
     * @throws IOException -如果请求失败
     */
    private String post(@NotNull HttpURLConnection connection, @NotNull String data) throws IOException {
        StringBuilder res = new StringBuilder(); // 用于返回得到的数据
        OutputStream httpOut = null;
        BufferedReader httpIn = null;
        try {
            // 设置请求头, java设置请求头非常麻烦, 害
            connection.setRequestMethod("POST");
            connection.setRequestProperty("user-agent", header.getUser_agent());
            connection.setRequestProperty("accept", header.getAccept());
            connection.setRequestProperty("acceptLanguage", header.getAccept_language());
            connection.setRequestProperty("acceptEncoding", header.getAccept_encoding());
            if (authorization != null && !authorization.isEmpty()) {
                connection.setRequestProperty("authorization", authorization);
            }
            connection.setRequestProperty("content-type", header.getContent_type());
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.connect();
            httpOut = connection.getOutputStream();
            httpOut.write(data.getBytes());
            httpIn = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            while ((line = httpIn.readLine()) != null) {
                res.append(line);
            }
        } catch (IOException e) {
            throw new IOException();
        } finally {
            closeStream(connection, httpOut, httpIn);
        }
        return res.toString();
    }

    public void saveLog() {
        if (temp == null || temp.isEmpty()) {
            return;
        }
        FileWriter fw = null;
        try {
            File f = new File("./ErrLog.log");
            fw = new FileWriter(f, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (fw == null) {
            return;
        }
        PrintWriter writer = new PrintWriter(fw);
        try (writer) {
            writer.println(temp);
            writer.flush();
            fw.flush();
            writer.close();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            temp = null;
        }
        System.out.println("日志保存成功");
    }

}

