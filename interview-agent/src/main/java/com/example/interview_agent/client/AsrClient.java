package com.example.interview_agent.client;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Component
public class AsrClient {

    private static final Logger logger = LoggerFactory.getLogger(AsrClient.class);

    @Value("${asr.host-url}")
    private String hostUrl;
    @Value("${asr.appid}")
    private String appId;
    @Value("${asr.api-key}")
    private String apiKey;
    @Value("${asr.api-secret}")
    private String apiSecret;

    private final OkHttpClient client = new OkHttpClient.Builder().build();
    private final Gson gson = new Gson();

    private static final int STATUS_FIRST_FRAME = 0;
    private static final int STATUS_CONTINUE_FRAME = 1;
    private static final int STATUS_LAST_FRAME = 2;

    public String transcribe(File audioFile) throws ExecutionException, InterruptedException, IOException {
        String authUrl;
        try {
            authUrl = getAuthUrl();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate ASR auth URL", e);
        }

        CompletableFuture<String> futureResult = new CompletableFuture<>();
        StringBuilder finalTranscript = new StringBuilder();

        Request request = new Request.Builder().url(authUrl).build();

        WebSocketListener listener = new WebSocketListener() {
            @Override
            public void onOpen(@NotNull WebSocket webSocket, @NotNull Response response) {
                logger.info("ASR: WebSocket opened, starting to send audio data...");
                new Thread(() -> sendAudioData(webSocket, audioFile, futureResult)).start();
            }

            @Override
            public void onMessage(@NotNull WebSocket webSocket, @NotNull String text) {
                AsrResponse response = gson.fromJson(text, AsrResponse.class);
                if (response.getCode() != 0) {
                    logger.error("ASR: Received error response: code={}, message={}", response.getCode(), response.getMessage());
                    futureResult.completeExceptionally(new RuntimeException("ASR API Error: " + response.getMessage()));
                    webSocket.close(1001, "API Error");
                    return;
                }

                if (response.getData() != null && response.getData().getResult() != null) {
                    response.getData().getResult().getWs().forEach(ws -> ws.getCw().forEach(cw -> finalTranscript.append(cw.getW())));
                }

                if (response.getData() != null && response.getData().getStatus() == 2) {
                    logger.info("ASR: Transcription finished.");
                    futureResult.complete(finalTranscript.toString());
                    webSocket.close(1000, "Transcription finished");
                }
            }

            @Override
            public void onFailure(@NotNull WebSocket webSocket, @NotNull Throwable t, Response response) {
                logger.error("ASR: WebSocket failed", t);
                futureResult.completeExceptionally(t);
            }
        };

        client.newWebSocket(request, listener);
        return futureResult.get();
    }

    private void sendAudioData(WebSocket webSocket, File audioFile, CompletableFuture<String> futureResult) {

        try (FileInputStream fis = new FileInputStream(audioFile)) {

            JsonObject frame = new JsonObject();

            JsonObject common = new JsonObject();
            common.addProperty("app_id", appId);
            frame.add("common", common);

            JsonObject business = new JsonObject();
            business.addProperty("language", "zh_cn");
            business.addProperty("domain", "iat");
            business.addProperty("accent", "mandarin");
            business.addProperty("dwa", "wpgs"); // 动态修正，让结果更智能
            frame.add("business", business);

            JsonObject data = new JsonObject();
            data.addProperty("status", STATUS_FIRST_FRAME);
            data.addProperty("format", "audio/L16;rate=16000"); // 明确指定我们发送的是PCM
            data.addProperty("encoding", "raw");
            frame.add("data", data);

            webSocket.send(frame.toString());
            logger.info("ASR: 第一帧（业务参数）已发送。");


            byte[] buffer = new byte[1280];
            int len;
            while ((len = fis.read(buffer)) != -1) {
                JsonObject audioFrame = new JsonObject();
                JsonObject audioData = new JsonObject();
                audioData.addProperty("status", STATUS_CONTINUE_FRAME);
                audioData.addProperty("audio", Base64.getEncoder().encodeToString(Arrays.copyOf(buffer, len)));
                audioFrame.add("data", audioData);

                webSocket.send(audioFrame.toString());

                Thread.sleep(40); // 每次发送后停顿40毫秒
            }

            JsonObject lastFrame = new JsonObject();
            JsonObject lastData = new JsonObject();
            lastData.addProperty("status", STATUS_LAST_FRAME);
            lastFrame.add("data", lastData);

            webSocket.send(lastFrame.toString());
            logger.info("ASR: 所有音频数据及结束帧已发送。");

        } catch (IOException | InterruptedException e) {
            logger.error("ASR: 发送音频数据时发生错误", e);
            futureResult.completeExceptionally(e);
        }
    }

    private String getAuthUrl() throws Exception {
        URL url = new URL(this.hostUrl.replace("wss://", "https://").replace("ws://", "http://"));
        SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        String date = format.format(new Date());
        String authorization_origin = String.format("host: %s\ndate: %s\nGET %s HTTP/1.1", url.getHost(), date, url.getPath());
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec spec = new SecretKeySpec(apiSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(spec);
        byte[] macBytes = mac.doFinal(authorization_origin.getBytes(StandardCharsets.UTF_8));
        String signature = Base64.getEncoder().encodeToString(macBytes);
        String authorization = String.format("api_key=\"%s\", algorithm=\"hmac-sha256\", headers=\"host date request-line\", signature=\"%s\"", apiKey, signature);
        String finalAuthorization = Base64.getEncoder().encodeToString(authorization.getBytes(StandardCharsets.UTF_8));
        return this.hostUrl + "?authorization=" + finalAuthorization + "&date=" + date.replace(" ", "%20") + "&host=" + url.getHost();
    }

    private static class AsrResponse {
        private int code;
        private String message;
        private Data data;
        public int getCode() { return code; }
        public String getMessage() { return message; }
        public Data getData() { return data; }
    }
    private static class Data {
        private int status;
        private Result result;
        public int getStatus() { return status; }
        public Result getResult() { return result; }
    }
    private static class Result {
        private List<Ws> ws;
        public List<Ws> getWs() { return ws; }
    }
    private static class Ws {
        private List<Cw> cw;
        public List<Cw> getCw() { return cw; }
    }
    private static class Cw {
        private String w;
        public String getW() { return w; }
    }
}