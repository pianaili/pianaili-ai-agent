package com.yupi.yuaiagent.demo.invoke;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

import java.util.List;

/**
 * 阿里云百炼 AI Hutool HTTP 调用
 */
public class HutoolAiInvoke {

    private static final String URL = "https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation";

    public static String callWithMessage() {
        JSONObject body = JSONUtil.createObj()
                .set("model", "qwen-plus")
                .set("input", JSONUtil.createObj()
                        .set("messages", List.of(
                                JSONUtil.createObj()
                                        .set("role", "system")
                                        .set("content", "You are a helpful assistant."),
                                JSONUtil.createObj()
                                        .set("role", "user")
                                        .set("content", "你是谁？")
                        ))
                )
                .set("parameters", JSONUtil.createObj()
                        .set("result_format", "message")
                );

        try (HttpResponse response = HttpRequest.post(URL)
                .header("Authorization", "Bearer " + TestApiKey.API_KEY)
                .header("Content-Type", "application/json")
                .body(body.toString())
                .execute()) {
            return response.body();
        }
    }

    public static void main(String[] args) {
        String result = callWithMessage();
        System.out.println(JSONUtil.formatJsonStr(result));
    }
}
