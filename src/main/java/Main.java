import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalTime;
import java.util.Base64;
import java.util.Random;

/**
 * @Author: Erzbir
 * @Date: 2022/8/31 23:19
 */
public class Main {
    private static final String Url = "https://xiaobei.yinghuaonline.com/xiaobei-api/";
    static Thread thread1;
    static Thread thread2;
    static Thread thread3;

    public static void main(String[] args) {
        if (!Reference.init(0)) {
            // 先初始化一遍
            SavaLogs.ERR = "配置文件异常";
            System.out.println("配置文件异常");
            return;
        }
        {
            // 发送消息的线程
            thread1 = new Thread(() -> {
                while (true) {
                    synchronized (Main.class) {
                        System.out.println("推送线程运行");
                        System.out.println("保存日志进程运行");
                        SavaLogs savaLogs = SavaLogs.getInstance();
                        SendMessage sendMessage = SendMessage.getInstance();
                        sendMessage.setReceiver(Reference.ACCEPT_EMAIL);
                        if (Reference.MSG != null && Reference.MSG.length() != 0) {
                            sendMessage.send_email(Reference.MSG);
                            Reference.MSG = null;
                        } else if (SavaLogs.ERR != null) {
                            savaLogs.save();
                            SavaLogs.ERR = null;
                        } else {
                            try {
                                Main.class.wait();
                                Main.class.notify();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            });
        }
        {
            // 保存日志的线程
            thread2 = new Thread(() -> {
                while (true) {
                    synchronized (Main.class) {
                        if (Reference.MSG != null && SavaLogs.ERR != null) {
                            try {
                                Main.class.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            Main.class.notify();
                        }
                    }
                }
            });
        }
        {
            thread3 = new Thread(() -> {
                System.out.println("线程运行");
                while (true) {
                    synchronized (Main.class) {
                        if (SavaLogs.ERR != null && Reference.MSG != null) {
                            Main.class.notify();
                            try {
                                Main.class.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            });
            thread3.start();
        }
        thread1.start();
        thread2.start();
        int count1 = 0;
        int count2 = 0;
        Random random = new Random();
        for (int i = 0; i < Reference.LENGTH; i++) {
            SendMessage sendMessage = SendMessage.getInstance();
            String s;
            if (!xiaobei(i)) {
                count2++;
                s = "用户 " + Reference.USERNAME + " 失败!";
                SavaLogs.ERR = s;
                sendMessage.setReceiver(Reference.ACCEPT_EMAIL);
                sendMessage.send_email(s);
                System.out.println(s);
                System.out.printf("已失败 %d 人, 还剩 %d 人\n", count2, Reference.LENGTH - 1 - i);
            } else {
                count1++;
                s = "用户 " + Reference.USERNAME + " 成功!";
                System.out.println(s);
                System.out.printf("已成功 {%d 人, 还剩 %d 人\n", count1, Reference.LENGTH - 1 - i);
            }
            if (i == Reference.LENGTH - 1) {
                thread1.stop();
                thread2.stop();
                thread3.stop();
                return;
            }
        }
    }

    public static boolean xiaobei(int index) {
        if (!Reference.init(index)) {
            // 先初始化一遍
            SavaLogs.ERR = "配置文件异常";
            System.out.println("配置文件异常");
            return false;
        }
        String username = Reference.USERNAME;
        String password = Reference.PASSWORD;
        if (username == null || username.length() == 0 || password == null || password.length() == 0) {
            return false;
        }
        //URL
        // 滑动验证
        String captchaUrl = Url + "captchaImage";
        // 登录
        String loginUrl = Url + "login";
        // 打卡
        String healthUrl = Url + "student/health";
        // 报文信息
        // data:   {"msg":"操作成功","img":"xxxxxx","code":200,"showCode":"NM6B","uuid":"12udb12ud810sbc0w"}

        // 开始网络请求(想起python是多么的简单, 当然有第三方包我没下)
        HttpURLConnection connection = null;
        InputStream in = null;
        BufferedReader httpIn = null;
        ByteArrayOutputStream out = null;
        OutputStream httpOut = null;
        String jsonString = "";
        StringBuilder res = new StringBuilder();
        JsonObject jsonObject = new JsonParser().parse(Reference.HEADER)
                .getAsJsonObject();
        // 请求头数据
        String userAgent = jsonObject.get("user-agent").getAsString();
        String accept = jsonObject.get("accept").getAsString();
        String acceptLanguage = jsonObject.get("accept-language").getAsString();
        String acceptEncoding = jsonObject.get("accept-encoding").getAsString();
        String contentType = jsonObject.get("content-type").getAsString();
        String authorization;
        try {
            connection = (HttpURLConnection) new URL(captchaUrl).openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("user-agent", userAgent);
            connection.setRequestProperty("accept", accept);
            connection.setRequestProperty("acceptLanguage", acceptLanguage);
            connection.setRequestProperty("acceptEncoding", acceptEncoding);
            connection.connect();
            if (connection.getResponseCode() != 200) {
                String temp = "验证码获取失败";
                SavaLogs.ERR = LocalTime.now() + "-----" + temp + "\n";
                Reference.MSG = temp;
                System.out.println(temp);

                return false;
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
            String temp = "网络或服务器问题";
            SavaLogs.ERR = LocalTime.now() + "-----" + temp + "\n";
            Reference.MSG = temp;
            System.out.println(temp);

        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
                if (connection != null) {
                    connection.disconnect();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (jsonString == null) {
            return false;
        }
        // 获取uuid
        String uuid;
        try {
            jsonObject = new JsonParser().parse(jsonString).getAsJsonObject();
            uuid = jsonObject.get("uuid").getAsString();
        } catch (Exception e) {
            e.printStackTrace();
            String temp = "uuid获取失败, 应该是帐号不存在的问题";
            SavaLogs.ERR = LocalTime.now() + "-----" + temp + "\n";
            Reference.MSG = temp;
            System.out.println(temp);
            System.out.println(temp);

            return false;
        }
        String showCode = jsonObject.get("showCode").getAsString();
        // 小北对密码只做了一个base64加密, 所以我们只需要加密这个就行了
        // 在抓到的报文中, 除了密码没看到其他加密的......
        Base64.Encoder encoder = Base64.getEncoder();
        StringBuilder m = new StringBuilder();
        byte[] buff = encoder.encode(password.getBytes());
        for (byte i : buff) {
            char c = (char) i;
            m.append(c);
        }
        password = m.toString();
        // 创建报文数据, 没错, 小北的报文就是如此简单
        String response =
                "{" +
                        "\"username\":\"" + username + "\"," +
                        "\"password\":\"" + password + "\"," +
                        "\"showCode\":\"" + showCode + "\"," +
                        "\"uuid\":\"" + uuid + "\"" + "}";

        // 登录
        try {
            connection = (HttpURLConnection) new URL(loginUrl).openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("user-agent", userAgent);
            connection.setRequestProperty("accept", accept);
            connection.setRequestProperty("acceptLanguage", acceptLanguage);
            connection.setRequestProperty("acceptEncoding", acceptEncoding);
            connection.setRequestProperty("content-type", contentType);
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.connect();
            httpOut = connection.getOutputStream();
            httpOut.write(response.getBytes());
            InputStream inputStream;
            try {
                inputStream = connection.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("网络输入流获取失败");
                return false;
            }
            httpIn = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = httpIn.readLine()) != null) {
                res.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
            String temp = "网络问题导致登录失败";
            SavaLogs.ERR = LocalTime.now() + "-----" + temp + "\n";
            Reference.MSG = temp;
            System.out.println(temp);

            return false;
        } finally {
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
        // 成功 {"msg":"操作成功","code":200,"token":"xxxx"}
        // 失败 {"msg":"用户不存在/密码错误","code":500}
        String code;
        String msg;
        {
            jsonObject = new JsonParser().parse(res.toString()).getAsJsonObject();
            code = String.valueOf(jsonObject.get("code"));
            msg = String.valueOf(jsonObject.get("msg"));
            res = new StringBuilder();
        }
        if (!code.equals("200")) {
            String temp = "登录失败, 原因: " + msg;
            SavaLogs.ERR = LocalTime.now() + "-----" + temp + "\n";
            Reference.MSG = temp;
            System.out.println(temp);
            return false;
        }
        authorization = jsonObject.get("token").getAsString();
        if (Reference.PLACE == null || Reference.PLACE.length() == 0) {
            if (!Action.getLocation()) {
                String temp = "登录成功但是位置获取失败";
                SavaLogs.ERR = LocalTime.now() + "-----" + temp + "\n";
                Reference.MSG = temp;
                System.out.println(temp);
                return false;
            }
        }
        String healthJson = Action.getHealth();
        // 又要开始烦人的连接了, 我目前还不知道有什么简便的工具类, 我应该花时间封装的...
        // 那就等一个大佬来优化代码吧!
        // 上报健康信息
        try {
            connection = (HttpURLConnection) new URL(healthUrl).openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("user-agent", userAgent);
            connection.setRequestProperty("accept", accept);
            connection.setRequestProperty("acceptLanguage", acceptLanguage);
            connection.setRequestProperty("acceptEncoding", acceptEncoding);
            connection.setRequestProperty("authorization", authorization);
            connection.setRequestProperty("content-type", contentType);
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.connect();
            httpOut = connection.getOutputStream();
            httpOut.write(healthJson.getBytes());
            httpIn = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            while ((line = httpIn.readLine()) != null) {
                res.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
            String temp = "体温上报失败";
            System.out.println(temp);
            SavaLogs.ERR = LocalTime.now() + "-----" + temp + "\n";
            Reference.MSG = temp;
            return false;
        } finally {
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
        {
            jsonObject = new JsonParser().parse(res.toString()).getAsJsonObject();
            code = jsonObject.get("code").getAsString();
            msg = jsonObject.get("msg").getAsString();
        }
        // 成功 return {'msg': '操作成功', 'code': 200}
        // 失败 {'msg': "xxxxx", 'code': 500}
        if (!code.equals("200")) {
            String temp = "打卡失败, 失败原因: " + msg;
            SavaLogs.ERR = LocalTime.now() + "-----" + temp + "\n";
            Reference.MSG = temp;
            System.out.println(temp);
            return false;
        }
        String temp = "打卡成功!!!" + msg;
        SavaLogs.ERR = LocalTime.now() + "-----" + temp + "\n";
        Reference.MSG = temp;
        Reference.clear();
        System.out.println(temp);
        return true;
    }
}
