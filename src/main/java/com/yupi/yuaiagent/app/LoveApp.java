package com.yupi.yuaiagent.app;


import com.yupi.yuaiagent.advisor.MyLoggerAdvisor;
import com.yupi.yuaiagent.advisor.ReReadingAdvisor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class LoveApp {

    private static final String SYSTEM_PROMPT = "扮演深耕恋爱心理领域的专家。开场向用户表明身份，告知用户可倾诉恋爱难题。" +
            "围绕单身、恋爱、已婚三种状态提问：单身状态询问社交圈拓展及追求心仪对象的困扰；" +
            "恋爱状态询问沟通、习惯差异引发的矛盾；已婚状态询问家庭责任与亲属关系处理的问题。" +
            "引导用户详述事情经过、对方反应及自身想法，以便给出专属解决方案。";

    private final ChatClient chatClient;

    /**
     * 初始化ChatClient客户端
     * @param dashscopeChatModel
     */
    public LoveApp(ChatModel dashscopeChatModel) {
        // 修改1：使用MessageWindowChatMemory替代InMemoryChatMemory
        ChatMemory chatMemory = MessageWindowChatMemory.builder()
                .maxMessages(10)
                .build();
        ChatClient.Builder builder = ChatClient.builder(dashscopeChatModel);
        builder.defaultSystem(SYSTEM_PROMPT);
        builder.defaultAdvisors(
                //修改2：MessageChatMemoryAdvisor构造函数私有化了，现在只能使用建造器模式构建
                MessageChatMemoryAdvisor.builder(chatMemory).build(),
                //自定义Advisor日志拦截器，按需开启
                new MyLoggerAdvisor(),
                //自定义增强Advisor，按需开启
                new ReReadingAdvisor().withOrder(0)
        );
        this.chatClient = builder.build();
    }

    /**
     * AI 基础对话，支持多轮对话记忆
     * @param message
     * @param chatId
     * @return
     */
    public String doChat(String message,String chatId) {
        ChatResponse response = chatClient
                .prompt()
                .user(message)
                //以前的两个常量不存在了，换成ChatMemory.CONVERSATION_ID
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                .call()
                .chatResponse();
        String content = response.getResult().getOutput().getText();
        log.info("content:{}", content);
        return content;
    }

    record LoveReport(String title, List<String> suggestions) {

    }

    /**
     * AI恋爱报告功能，支持结构化输出
     * @param message
     * @param chatId
     * @return
     */
    public LoveReport doChatWithReport(String message,String chatId) {
        LoveReport loveReport = chatClient
                .prompt()
                .system(SYSTEM_PROMPT+"每次对话后都要生成恋爱结果,标题为{用户名}的恋爱报告,内容为建议列表")
                .user(message)
                //以前的两个常量不存在了，换成ChatMemory.CONVERSATION_ID
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                .call()
                .entity(LoveReport.class);
        log.info("loveReport:{}", loveReport);
        return loveReport;
    }
}
