package com.yupi.springbootinit.xfAI;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class SparkAIChatExample {

    public static void main(String[] args) throws Exception {
        String sparkApiUrl = "wss://spark-api.xf-yun.com/v3.5/chat"; // 这里应该是HTTP API的URL
        String sparkAppId = "b8903084"; // 你的讯飞开放平台的APP_ID
        String sparkApiKey = "MzVkYTY5N2ExNDJhZTMyZWMwZmRkYTJl"; // 你的讯飞开放平台的API_KEY
        String sparkApiSecret = "53b2f6259b6f305ab9e70de9cf4d8029"; // 你的讯飞开放平台的API_SECRET
        String sparkLlmDomain = "generalv3.5";

        // 构建请求体，这里只是一个示例，实际的请求体格式需要根据API文档来构造
        String jsonBody = "{\"messages\": [{\"role\": \"user\", \"content\": \"你好呀\"}]}";

        // 创建URL对象
        URL url = new URL(sparkApiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("AppId", sparkAppId);
        connection.setRequestProperty("ApiKey", sparkApiKey);
        connection.setRequestProperty("ApiSecret", sparkApiSecret);

        // 发送请求体
        OutputStream os = connection.getOutputStream();
        os.write(jsonBody.getBytes());
        os.flush();
        os.close();

        // 读取响应
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        // 打印结果
        System.out.println(response.toString());
    }
}
