package com.erzbir.xiaobei;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.FileReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @Author: Erzbir
 * @Date: 2022/8/31 23:19
 */
public class Main {
    public static JsonObject jsonObject;
    static ExecutorService executor = Executors.newCachedThreadPool(); // 缓存线程池


    public static void main(String[] args) {
        try {
            JsonArray userJsonArray;
            JsonArray headJsonArray;
            String s = System.getenv("YOUR_KEY");
            if (s == null || s.isEmpty()) {
                jsonObject = JsonParser.parseReader(new FileReader("config.json")).getAsJsonObject();
            } else {
                jsonObject = JsonParser.parseString(s).getAsJsonObject();
            }
            userJsonArray = jsonObject.getAsJsonArray("user");
            headJsonArray = jsonObject.getAsJsonArray("head");
            if (userJsonArray == null || headJsonArray == null) {
                return;
            }
            JsonObject headJson;
            JsonObject userJson;
            // 这里用传参的形式创建并开启线程, 因为线程自己获取风险更大
            for (int i = 0; i < userJsonArray.size(); i++) {
                userJson = userJsonArray.get(i).getAsJsonObject();
                // 如果用户数量超出请求头数量则随机选head中的一个请求头
                if (i >= headJsonArray.size()) {
                    headJson = headJsonArray.get((int) (Math.random() * headJsonArray.size())).getAsJsonObject();
                } else {
                    headJson = headJsonArray.get(i).getAsJsonObject();
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
        private final User user; // 一个线程绑定一个用户
        private final Head header; // 一个线程绑定一个head

        public RunThread(JsonObject userJson, JsonObject headerJson) {
            Gson gson = new Gson();
            this.user = gson.fromJson(userJson, User.class);
            this.header = gson.fromJson(headerJson, Head.class);
        }

        @Override
        public void run() {
            Threads threads;
            threads = new Threads(user, header);
            try {
                if (threads.begin()) {
                    System.out.println("上报成功");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
