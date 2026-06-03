package com.yupi.yuaiagent.app;


import com.yupi.yuaiagent.advisor.MyLoggerAdvisor;
import com.yupi.yuaiagent.chatmemory.FileBasedChatMemory;
import com.yupi.yuaiagent.rag.LoveAppRAGCustomAdvisorFactory;
import com.yupi.yuaiagent.rag.QueryRewriter;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.preretrieval.query.expansion.MultiQueryExpander;
import org.springframework.ai.rag.preretrieval.query.expansion.QueryExpander;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

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
    public LoveApp(@Qualifier("dashScopeChatModel") ChatModel dashscopeChatModel) {
        // 初始化基于文件的对话记忆
        String fileDir = System.getProperty("user.dir") + "/tmp/chat-memory";
        ChatMemory chatMemory = new FileBasedChatMemory(fileDir);
         //修改1：使用MessageWindowChatMemory替代InMemoryChatMemory
//        ChatMemory chatMemory = MessageWindowChatMemory.builder()
//                .maxMessages(10)
//                .build();
        ChatClient.Builder builder = ChatClient.builder(dashscopeChatModel);
        builder.defaultSystem(SYSTEM_PROMPT);
        builder.defaultAdvisors(
                //修改2：MessageChatMemoryAdvisor构造函数私有化了，现在只能使用建造器模式构建
                MessageChatMemoryAdvisor.builder(chatMemory).build(),
                //自定义Advisor日志拦截器，按需开启
                new MyLoggerAdvisor()
                //自定义增强Advisor，按需开启
//                new ReReadingAdvisor().withOrder(0)
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

    /**
     * AI 基础对话，支持多轮对话记忆
     * 流式传输，响应式编程
     * @param message
     * @param chatId
     * @return
     */
    public Flux<String> doChatByStream(String message,String chatId) {
        return chatClient
                .prompt()
                .user(message)
                //以前的两个常量不存在了，换成ChatMemory.CONVERSATION_ID
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                .stream()
                .content();
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

    @Resource
    private VectorStore loveAppVectorStore;
    @Resource
    private Advisor loveAppRAGCloudAdvisor;
    @Resource
    private VectorStore pgVectorVectorStore;
    @Resource
    private QueryRewriter queryRewriter;

    public String doChatWithRAG(String message,String chatId) {
        //重写用户的message
//        String rewriteMessage = queryRewriter.doQueryRewrite(message);

        // 1. 构建 VectorStoreDocumentRetriever
        DocumentRetriever retriever = VectorStoreDocumentRetriever.builder()
                .vectorStore(loveAppVectorStore)      // 必填：向量存储
                .similarityThreshold(0.75)     // 可选：相似度阈值
                .topK(3)                       // 可选：返回 top K 条文档
                .build();

        // 2. 构建 RetrievalAugmentationAdvisor（Advisor 对象）
        Advisor loveAppRAGLocalAdvisor = RetrievalAugmentationAdvisor.builder()
                .documentRetriever(retriever)
                .build();

        //构建pgVectorStoreDocumentRetriever对象
        DocumentRetriever pgVectorStoreDocumentRetriever = VectorStoreDocumentRetriever.builder()
//                .filterExpression(new FilterExpressionBuilder().eq("key","value").build()) //元数据标签过滤
                .vectorStore(pgVectorVectorStore)
                .similarityThreshold(0.75)
                .topK(3)
                .build();
        //构建检索增强顾问
        Advisor pgVectorStoreAdvisor = RetrievalAugmentationAdvisor.builder()
                .documentRetriever(pgVectorStoreDocumentRetriever)
//                .queryExpander() //还可以添加查询拓展器
                .queryAugmenter(ContextualQueryAugmenter.builder()
                        .allowEmptyContext(true)
                        .build()) //当检索内容为空时，没有知识库支持，也要回答用户的问题
                .build();
        ChatResponse response = chatClient
                .prompt()
                //输入重写后的用户消息
                .user(message)
                //以前的两个常量不存在了，换成ChatMemory.CONVERSATION_ID
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                //添加本地的RAG向量知识库
                .advisors(loveAppRAGLocalAdvisor)
//                .advisors(new QuestionAnswerAdvisor(loveAppVectorStore))//该advisor已经不不存在了
                //添加阿里云的RAG向量知识库
//                .advisors(loveAppRAGCloudAdvisor)
                //应用RAG检索增强服务，基于PgVectorStore的向量存储
//                .advisors(pgVectorStoreAdvisor)
                //自定义的RAG 检索增强服务（文档查询器+上下文增强）
//                .advisors(
//                        LoveAppRAGCustomAdvisorFactory.createLoveAppRAGCustomAdvisor(
//                                loveAppVectorStore,"健康"
//                        )
//                )
                .call()
                .chatResponse();
        String content = response.getResult().getOutput().getText();
        log.info("content:{}", content);
        return content;
    }

    //AI 调用工具的能力
    @Resource
    private ToolCallback[] allTools;

    /**
     * 调用工具的能力
     * @param message
     * @param chatId
     * @return
     */
    public String doChatWithTools(String message,String chatId) {
        ChatResponse response = chatClient
                .prompt()
                .user(message)
                //以前的两个常量不存在了，换成ChatMemory.CONVERSATION_ID
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                .toolCallbacks(allTools)
                .call()
                .chatResponse();
        String content = response.getResult().getOutput().getText();
        log.info("content:{}", content);
        return content;
    }

    @Resource
    private ToolCallbackProvider toolCallbackProvider;

    /**
     * AI调用Mcp服务
     * @param message
     * @param chatId
     * @return
     */
    public String doChatWithMcp(String message,String chatId) {
        ChatResponse response = chatClient
                .prompt()
                .user(message)
                //以前的两个常量不存在了，换成ChatMemory.CONVERSATION_ID
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                .toolCallbacks(toolCallbackProvider) //调用Mcp服务
                .call()
                .chatResponse();
        String content = response.getResult().getOutput().getText();
        log.info("content:{}", content);
        return content;
    }
}
