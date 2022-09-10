package com.erzbir.xiaobei;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @Author: Erzbir
 * @Date: 2022/8/31 23:19
 */
public class Main {

    static ExecutorService executor = Executors.newCachedThreadPool(); // 缓存线程池


    public static void main(String[] args) {
        try {
            JsonObject jsonObject;
            JsonArray userJsonArray = null;
            JsonArray headJsonArray = null;
            try {
                jsonObject = JsonParser.parseReader(new FileReader("config.json")).getAsJsonObject();
                //jsonObject = JsonParser.parseString(System.getenv("yourKey")).getAsJsonObject(); // 如果通过环境变量获取请解除此行注释并注释掉上一行, 再将"youKey"改成你设置的环境变量名
                userJsonArray = jsonObject.get("user").getAsJsonArray();
                headJsonArray = jsonObject.get("head").getAsJsonArray();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            if (userJsonArray == null || headJsonArray == null) {
                return;
            }
            JsonObject headJson;
            JsonObject userJson;
            // 这里用传参的形式创建并开启线程, 因为线程自己获取风险更大
            for (int i = 0; i < Objects.requireNonNull(userJsonArray).size(); i++) {
                userJson = userJsonArray.get(i).getAsJsonObject();
                headJson = headJsonArray.get(i).getAsJsonObject();
                // 如果用户数量超出请求头数量则随机选head中的一个请求头
                if (i > headJsonArray.size() - 1) {
                    headJson = headJsonArray.get((int) (Math.random() * headJsonArray.size())).getAsJsonObject();
                }
                executor.submit(new RunThread(userJson, headJson));
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            executor.shutdown();
        }


    }

    /**
     * @<code> 线程类, 每个用户的操作都是一个线程 </code>
     */
    private static class RunThread implements Runnable {
        private User user; // 一个线程绑定一个用户
        private Head header; // 一个线程绑定一个head

        public RunThread() {

        }

        public RunThread(JsonObject userJson, JsonObject headerJson) {
            this.user = new User(userJson);
            this.header = new Head(headerJson);
        }

        @Override
        public void run() {
            Action action = null;
            try {
                action = new Action(user, header);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            if (action == null) {
                return;
            }
            if (action.begin()) {
                System.out.println("上报成功");
            }
        }
    }
}
