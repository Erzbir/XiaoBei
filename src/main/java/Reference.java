import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.FileReader;
import java.util.Collections;

/**
 * @Author: Erzbir
 * @Date: 2022/8/31 23:11
 * <p>
 * 配置文件解析到这个类中
 */

public class Reference {
    public volatile static String MSG = null; // 待发送信息
    public static String USERNAME = null; // 用户名
    public static String PASSWORD = null; // 密码
    public static String LOCATION = null; // 坐标基准点
    public static String EMAIL = null; // 发送邮件使用的邮箱
    public static String PLACE = null; // 具体位置信息, 格式为: 中国-四川省-成都市-成华区
    public static int LENGTH = 0; // 人数
    public static String ACCEPT_EMAIL = null; // 发送信息的目标邮箱
    public static String HEADER = "{" +
            "\"user-agent\": \"iPhone10,3(iOS/14.4) Uninview(Uninview/1.0.0) Weex/0.26.0 1125x2436\"," + "\"accept\": \"*/*\"," + "\"accept-language\": \"zh-cn\"," + "\"accept-encoding\": \"gzip, deflate, br\"" + "}"; // 请求头


    public static String valueToString() {
        return USERNAME + PASSWORD + PLACE + EMAIL + ACCEPT_EMAIL + LENGTH + HEADER;
    }

    // 初始化变量, 因为是单例所以没有写在构造函数中
    public static boolean init(int index) {
        try {
            JsonElement json = new JsonParser().parse(new FileReader("config.json"));
            JsonObject jsonObject = json.getAsJsonObject();
            JsonObject userJson = jsonObject.get("user").getAsJsonArray().get(index).getAsJsonObject();
            USERNAME = userJson.get("username").getAsString();
            PASSWORD = userJson.get("password").getAsString();
            LOCATION = userJson.get("location").getAsString();
            LENGTH = Collections.singletonList(jsonObject.get("user").getAsJsonArray()).size();
            PLACE = userJson.get("place").getAsString();
            ACCEPT_EMAIL = userJson.get("email").getAsString();
            EMAIL = jsonObject.get("sender_email").getAsString();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 为了保证正常运行, 每次一定调用重置到默认
     */
    public static void clear() {
        USERNAME = null;
        PASSWORD = null;
        LOCATION = null;
        EMAIL = null;
        PLACE = null;
        LENGTH = 0;
        ACCEPT_EMAIL = null;
        HEADER = null;
    }
}
