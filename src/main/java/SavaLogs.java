import java.io.IOException;
import java.io.PrintWriter;

/**
 * @Author: Erzbir
 * @Date: 2022/9/1 18:38
 */

/**
 * 用于保存失败日志的类, 单例
 */
public class SavaLogs {
    public volatile static String ERR; // 失败日志信息
    private static SavaLogs savaLogs;

    private SavaLogs() {

    }

    public static SavaLogs getInstance() {
        if (savaLogs == null) {
            return new SavaLogs();
        }
        return savaLogs;
    }

    public void save() {
        if (ERR == null) {
            return;
        }
        try (PrintWriter writer = new PrintWriter("ErrLog.log")) {
            writer.println(ERR);
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
