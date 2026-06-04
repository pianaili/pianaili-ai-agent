package com.yupi.yuaiagent.agent.aliagent;

import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import com.alibaba.cloud.ai.graph.checkpoint.savers.redis.RedisSaver;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.yupi.yuaiagent.agent.aliagent.rag.RAGMessagesHook;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AliReactAgent {

    private static final String SYSTEM_PROMPT = "扮演深耕恋爱心理领域的专家。开场向用户表明身份，告知用户可倾诉恋爱难题。" +
            "围绕单身、恋爱、已婚三种状态提问：单身状态询问社交圈拓展及追求心仪对象的困扰；" +
            "恋爱状态询问沟通、习惯差异引发的矛盾；已婚状态询问家庭责任与亲属关系处理的问题。" +
            "引导用户详述事情经过、对方反应及自身想法，以便给出专属解决方案。";

    private final ReactAgent reactAgent;

    public AliReactAgent(ChatModel dashScopeChatModel,
                         ToolCallback[] allTools,
                         ToolCallbackProvider mcpAsyncToolCallbacks,
                         VectorStore pgVectorVectorStore) {
        //生产环境可以使用 Redis Checkpointer
//        RedisSaver redisSaver = RedisSaver.builder().redisson(redissionClient).build();

        this.reactAgent = ReactAgent.builder()
                .name("LoveAgent")
                .model(dashScopeChatModel)
                .saver(new MemorySaver()) //基于内存的对话记忆
                .tools(allTools)
                .toolCallbackProviders(mcpAsyncToolCallbacks)
                .hooks(new RAGMessagesHook(pgVectorVectorStore))
                .systemPrompt(SYSTEM_PROMPT)
                .build();
    }

    public String agentCall(String userMessage,String chatId) {
        RunnableConfig config = RunnableConfig.builder()
                .threadId(chatId)
                .build();
        try {
            AssistantMessage response = reactAgent.call(userMessage,config);
            String result = response.getText();
            log.info("智能体调用结果：{}", result);
            return result;
        } catch (GraphRunnerException e) {
            log.error("智能体调用失败",e);
            throw new RuntimeException(e);
        }
    }
}
