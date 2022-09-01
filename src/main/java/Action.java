import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Author: Erzbir
 * @Date: 2022/8/31 23:10
 * <p>
 * 获取地理位置, 上报信息等
 */
public class Action {
    public static boolean getLocation() {
        if (Reference.PLACE != null) {
            System.out.println("已有地址");
            return false;
        }
        if (Reference.LOCATION == null) {
            return false;
        }
        StringBuilder result = new StringBuilder();
        String jsonString;
        ByteArrayOutputStream out = null;
        InputStream in = null;
        String[] lc = Reference.LOCATION.split(",");
        String location = lc[1] + "," + lc[0];
        URL url;
        HttpURLConnection httpsUrl;
        try {
            url = new URL("https://api.xiaobaibk.com/api/location/?location=" + location);
            httpsUrl = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        try {
            httpsUrl.setRequestMethod("GET");
            httpsUrl.setRequestProperty("Accept", "*/*");
            httpsUrl.setRequestProperty("User-Agent", "Mozilla/5.0 (compatible; MSIE 11; Windows NT 5.1)");
            httpsUrl.connect();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (httpsUrl != null) {
                httpsUrl.disconnect();
            }
        }
        try {
            if (httpsUrl.getResponseCode() != 200) {
                return false;
            }
            in = httpsUrl.getInputStream();
            byte[] buff = new byte[1024];
            out = new ByteArrayOutputStream();
            int len;
            while ((len = in.read(buff)) != -1) {
                out.write(buff, 0, len);
            }
            jsonString = out.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (in != null) {

                    in.close();

                }
                if (out != null) {
                    out.close();
                }
                httpsUrl.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
            httpsUrl.disconnect();
            httpsUrl = null;
            url = null;
        }
        if (jsonString == null || jsonString.length() == 0) {
            return false;
        }
        // 这里用正则匹配json而不是JsonObject, 只是练手, 其他类用了工具, 好吧其实是我写这里的时候没学这个工具类
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
        // {"status":0,"result":{"location":{"lng":103.3488111352991,"lat":29.90908670030837},"formatted_address":"四川省眉山市洪雅县瓦屋山大道辅路","business":"","addressComponent":{"country":"中国","country_code":0,"country_code_iso":"CHN","country_code_iso2":"CN","province":"四川省","city":"眉山市","city_level":2,"district":"洪雅县","town":"","town_code":"","distance":"","direction":"","adcode":"511423","street":"瓦屋
        Reference.PLACE = result.toString();
        return true;
    }

    /**
     * @return 生成上报内容并返回以此构建的json格式字符串
     */
    public static String getHealth() {
        Random random = new Random();
        String[] lc = Reference.LOCATION.split(",");
        String temperature = String.valueOf(random.nextInt(357, 367) / 10);
        int rand = random.nextInt(1111, 9999);
        String location_x = String.valueOf((double) rand / 100000 + Double.parseDouble(lc[0]));
        String location_y = String.valueOf((double) rand / 100000 + Double.parseDouble(lc[1]));
        String location = location_x + ',' + location_y;
        // 这里是手撕json, 工具不会用, 害
        return "{" +
                "\"temperature\":\"" + temperature + "\"," +
                "\"coordinates\":\"" + Reference.PLACE + "\"," +
                "\"location\":\"" + location + "\"," +
                "\"healthState\":" + "\"1\"" + "," +
                "\"dangerousRegion\":" + "\"2\"" + "," +
                "\"dangerousRegionRemark\":" + "\"\"" + "," +
                "\"contactSituation\":" + "\"2\"" + "," +
                "\"goOut\":" + "\"1\"" + "," +
                "\"goOutRemark\":" + "\"1\"" + "," +
                "\"remark\":" + "\"无\"" + "," +
                "\"goOutRemark\":" + "\"1\"" + "," +
                "\"familySituation\":" + "\"1\"" +
                "}";
    }

}

