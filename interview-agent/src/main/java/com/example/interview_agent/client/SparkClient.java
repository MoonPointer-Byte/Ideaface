package com.example.interview_agent.client;

import com.example.interview_agent.dto.SparkData.*;
import com.google.gson.Gson;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Component
public class SparkClient {

    private static final Logger logger = LoggerFactory.getLogger(SparkClient.class);

    @Value("${spark.host-url}")
    private String hostUrl;

    @Value("${spark.app-id}")
    private String appId;

    @Value("${spark.api-key}")
    private String apiKey;

    @Value("${spark.api-secret}")
    private String apiSecret;

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();

    private final Gson gson = new Gson();

    public String askQuestion(String question) throws ExecutionException, InterruptedException {
        // 1. 获取鉴权URL
        String authUrl;
        try {
            authUrl = getAuthUrl();
        } catch (Exception e) {
            logger.error("Error generating authentication URL", e);
            throw new RuntimeException("Failed to generate auth URL", e);
        }

        CompletableFuture<String> futureResponse = new CompletableFuture<>();
        StringBuilder fullResponse = new StringBuilder();

        Request request = new Request.Builder().url(authUrl).build();

        WebSocketListener listener = new WebSocketListener() {
            @Override
            public void onOpen(@NotNull WebSocket webSocket, @NotNull Response response) {
                logger.info("Spark WebSocket opened, sending question...");
                var requestDto = createRequest(question);
                var requestJson = gson.toJson(requestDto);
                webSocket.send(requestJson);
            }

            @Override
            public void onMessage(@NotNull WebSocket webSocket, @NotNull String text) {
                var responseDto = gson.fromJson(text, SparkResponse.class);

                if (responseDto.header().code() != 0) {
                    logger.error("Received error from Spark API: code={}, message={}",
                            responseDto.header().code(), responseDto.header().message());
                    webSocket.close(1001, "API Error");
                    futureResponse.completeExceptionally(new RuntimeException("Spark API Error: " + responseDto.header().message()));
                    return;
                }

                responseDto.payload().choices().text().forEach(t -> fullResponse.append(t.content()));

                if (responseDto.header().status() == 2) {
                    logger.info("Spark response finished.");
                    webSocket.close(1000, "Normal closure");
                    futureResponse.complete(fullResponse.toString());
                }
            }

            @Override
            public void onFailure(@NotNull WebSocket webSocket, @NotNull Throwable t, Response response) {
                logger.error("Spark WebSocket failed", t);
                futureResponse.completeExceptionally(t);
            }

            @Override
            public void onClosing(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
                logger.info("Spark WebSocket closing: code={}, reason={}", code, reason);
                if(!futureResponse.isDone()) {
                    futureResponse.complete(fullResponse.toString());
                }
            }


        };

        client.newWebSocket(request, listener);

        return futureResponse.get();


    }


    public String evaluateTranscript(String transcript, String positionId) throws Exception {
        String jobDescription = getJobDescriptionFor(positionId);

        String prompt = String.format("""
            你是一位资深的AI面试评估官，你的任务是基于候选人的面试回答和目标岗位要求，进行全面、客观、专业的多维度评估。

            【目标岗位】
            %s

            【面试回答实录】
            "%s"

            【评估指令】
            请严格按照以下要求完成评估，并以一个完整的、可以被程序直接解析的JSON对象格式返回。不要在JSON代码块前后添加任何额外的解释、介绍或总结性文字。
            1.  **专业知识 (professionalKnowledge):** 评估候选人对岗位相关知识的掌握程度。
            2.  **技能匹配度 (skillMatch):** 评估候选人展示的技能与岗位要求的契合度。
            3.  **语言表达能力 (expressionAbility):** 评估候选人的表达是否清晰、流畅、有条理。
            4.  **逻辑思维能力 (logicalThinking):** 评估候选人回答问题时的逻辑性和结构性，是否能使用STAR法则等。
            5.  **应变抗压能力 (stressResistance):** 从回答中推断候选人面对压力和意外问题时的反应。

            【输出格式】
            请严格遵循以下JSON结构：
            {
              "overallComment": "对候选人的一个简短、全面的总体评价（不超过50字）",
              "scores": {
                "professionalKnowledge": <1-5之间的浮点数>,
                "skillMatch": <1-5之间的浮点数>,
                "expressionAbility": <1-5之间的浮点数>,
                "logicalThinking": <1-5之间的浮点数>,
                "stressResistance": <1-5之间的浮点数>
              },
              "suggestions": [
                {
                  "dimension": "专业知识",
                  "suggestion": "具体的、可操作的改进建议..."
                },
                {
                  "dimension": "语言表达能力",
                  "suggestion": "具体的、可操作的改进建议..."
                }
              ]
            }
            """, jobDescription, transcript);
        return this.askQuestion(prompt);
    }

    private String getJobDescriptionFor(String positionId) {
        return switch (positionId) {
            case "ai-engineer" -> "要求：熟悉常见的机器学习算法（如决策树、SVM、神经网络），至少掌握一种深度学习框架（PyTorch或TensorFlow），有实际的项目经验，能够清晰地阐述模型原理和项目细节。";
            case "backend-developer" -> "要求：精通Java语言，熟悉Spring Boot框架，了解数据库设计和SQL优化，有微服务或分布式系统开发经验，熟悉Docker和Linux常用命令。";
            case "product-manager" -> "要求：具备优秀的用户洞察和市场分析能力，能够独立完成需求文档（PRD）撰写，逻辑思维清晰，沟通协调能力强，对数据敏感。";
            default -> "通用岗位要求：要求具备良好的沟通能力、学习能力和团队合作精神。";
        };
    }

    private SparkRequest createRequest(String question) {
        var userMessage = new Text("user", question);
        var message = new Message(List.of(userMessage));
        var payload = new Payload(message);
        // 此处可根据不同场景（如不同岗位面试）动态调整domain, temperature等参数
        var chat = new Chat("4.0Ultra", 0.5, 1024); // 使用v3.5模型，并设置max_tokens
        var parameter = new Parameter(chat);
        var header = new Header(appId, UUID.randomUUID().toString()); // 为每个会话生成一个唯一的uid
        return new SparkRequest(header, parameter, payload);
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
}