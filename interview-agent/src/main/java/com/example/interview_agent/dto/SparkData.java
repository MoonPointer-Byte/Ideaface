package com.example.interview_agent.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class SparkData {


    public record SparkRequest(Header header, Parameter parameter, Payload payload) {}


    public record Header(@SerializedName("app_id") String appId, String uid) {

        public Header(String appId) {
            this(appId, null);
        }
    }


    public record Parameter(Chat chat) {}


    public record Chat(String domain, Double temperature, @SerializedName("max_tokens") Integer max_Tokens) {}


    public record Payload(Message message) {}


    public record Message(List<Text> text) {}


    public record Text(String role, String content) {}

    public record SparkResponse(ResponseHeader header, ResponsePayload payload) {}


    public record ResponseHeader(int code, String message, String sid, int status) {}


    public record ResponsePayload(Choices choices) {}


    public record Choices(int status, int seq, List<Text> text) {}
}