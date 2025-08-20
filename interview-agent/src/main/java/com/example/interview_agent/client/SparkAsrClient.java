////
//package com.example.interview_agent.client;
//
//import com.google.gson.Gson;
//import com.google.gson.JsonObject;
//import okhttp3.*;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Component;
//
//import javax.crypto.Mac;
//import javax.crypto.spec.SecretKeySpec;
//import java.io.File;
//import java.io.IOException;
//import java.net.URL;
//import java.nio.charset.StandardCharsets;
//import java.nio.file.Files;
//import java.text.SimpleDateFormat;
//import java.util.*;
//
//@Component
//public class SparkAsrClient {
//    private static final Logger logger = LoggerFactory.getLogger(SparkAsrClient.class);
//
//    @Value("${spark.asr.host-url}")
//    private String hostUrl;
//    @Value("${spark.app-id}")
//    private String appId;
//    @Value("${spark.api-key}")
//    private String apiKey;
//    @Value("${spark.api-secret}")
//    private String apiSecret;
//
//    private final OkHttpClient client = new OkHttpClient.Builder().build();
//    private final Gson gson = new Gson();
//
//    public String transcribeAndEvaluate(File audioFile, String currentQuestion) throws Exception {
//        String requestBody = buildRequestBody(audioFile, currentQuestion);
//        Headers headers = buildAuthHeaders(requestBody);
//
//        Request request = new Request.Builder()
//                .url(hostUrl)
//                .headers(headers)
//                .post(RequestBody.create(requestBody, MediaType.get("application/json")))
//                .build();
//
//        logger.info("向大模型识别API发送请求...");
//        try (Response response = client.newCall(request).execute()) {
//            String responseBody = response.body() != null ? response.body().string() : "{}";
//            if (!response.isSuccessful()) {
//                logger.error("大模型识别API请求失败: code={}, body={}", response.code(), responseBody);
//                throw new IOException("Unexpected code " + response);
//            }
//            logger.info("收到大模型识别API的响应: {}", responseBody);
//
//            JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);
//            if (jsonResponse.get("header").getAsJsonObject().get("code").getAsInt() != 0) {
//                throw new IOException("API returned error: " + jsonResponse.get("header").getAsJsonObject().get("message").getAsString());
//            }
//            return jsonResponse.getAsJsonObject("payload").getAsJsonObject("result").get("text").getAsString();
//        }
//    }
//
//    private String buildRequestBody(File audioFile, String currentQuestion) throws IOException {
//        String audioBase64 = Base64.getEncoder().encodeToString(Files.readAllBytes(audioFile.toPath()));
//        JsonObject root = new JsonObject();
//        JsonObject header = new JsonObject();
//        header.addProperty("app_id", appId);
//        header.addProperty("task_id", UUID.randomUUID().toString());
//        root.add("header", header);
//        JsonObject parameter = new JsonObject();
//        JsonObject audio = new JsonObject();
//        audio.addProperty("encoding", "webm"); // 直接告诉API是webm格式
//        audio.addProperty("sample_rate", 16000);
//        audio.addProperty("channels", 1);
//        audio.addProperty("bit_depth", 16);
//        parameter.add("audio", audio);
//        JsonObject recognize = new JsonObject();
//        recognize.addProperty("domain", "part-nlu");
//        JsonObject extractRule = new JsonObject();
//        String rule = String.format("{\"evaluation\": {\"role\": \"user\",\"text\": \"作为一名面试官，请对以下问题的回答进行简短、专业、一针见血的评价（50字以内）。问题: '%s' 候选人回答: [AUDIO]\"}}", currentQuestion);
//        extractRule.addProperty("rule", rule);
//        extractRule.addProperty("result_key", "evaluation");
//        recognize.add("extract_rule", extractRule);
//        parameter.add("recognize", recognize);
//        root.add("parameter", parameter);
//        JsonObject payload = new JsonObject();
//        JsonObject data = new JsonObject();
//        data.addProperty("audio", audioBase64);
//        data.addProperty("status", 3);
//        payload.add("data", data);
//        root.add("payload", payload);
//        return gson.toJson(root);
//    }
//
//    private Headers buildAuthHeaders(String body) throws Exception {
//        URL url = new URL(this.hostUrl);
//        SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
//        format.setTimeZone(TimeZone.getTimeZone("GMT"));
//        String date = format.format(new Date());
//
//        String digest = "SHA-256=" + Base64.getEncoder().encodeToString(java.security.MessageDigest.getInstance("SHA-256").digest(body.getBytes(StandardCharsets.UTF_8)));
//
//        String authorization_origin = String.format("host: %s\ndate: %s\nPOST %s HTTP/1.1\ndigest: %s", url.getHost(), date, url.getPath(), digest);
//        Mac mac = Mac.getInstance("HmacSHA256");
//        SecretKeySpec spec = new SecretKeySpec(apiSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
//        mac.init(spec);
//        byte[] macBytes = mac.doFinal(authorization_origin.getBytes(StandardCharsets.UTF_8));
//        String signature = Base64.getEncoder().encodeToString(macBytes);
//
//        String authorization = String.format("api_key=\"%s\", algorithm=\"hmac-sha256\", headers=\"host date request-line digest\", signature=\"%s\"", apiKey, signature);
//
//        return new Headers.Builder()
//                .add("Host", url.getHost())
//                .add("Date", date)
//                .add("Authorization", authorization)
//                .add("Digest", digest)
//                .build();
//    }
//}