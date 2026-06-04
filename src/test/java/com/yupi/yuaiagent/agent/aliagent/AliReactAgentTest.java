package com.yupi.yuaiagent.agent.aliagent;

import cn.hutool.core.lang.UUID;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AliReactAgentTest {

    @Resource
    private AliReactAgent aliReactAgent;

    @Test
    void agentCall() {
        String chatId = UUID.randomUUID().toString();
        String result = aliReactAgent.agentCall("你好，我是偏爱黎", chatId);
        Assertions.assertNotNull(result);

        result = aliReactAgent.agentCall("我想增进和另一半（蕾姆）的关系", chatId);
        Assertions.assertNotNull(result);

        result = aliReactAgent.agentCall("我的另一半叫什么名字，我刚刚告诉了你，帮我回忆一下", chatId);
        Assertions.assertNotNull(result);
    }

    @Test
    void doChatWithRAG() {
        String chatId = UUID.randomUUID().toString();
        String message = "我已经结婚了，但是婚后关系不太亲密，该怎么办？";
        String answer = aliReactAgent.agentCall(message, chatId);
        Assertions.assertNotNull(answer);
    }

    @Test
    void doChatWithTools() {
        String chatId = UUID.randomUUID().toString();
        String message = "我和对象吵架了，你能帮我搜索一些哄女孩子开心的图片吗？";
        String answer = aliReactAgent.agentCall(message, chatId);
        Assertions.assertNotNull(answer);
    }
}