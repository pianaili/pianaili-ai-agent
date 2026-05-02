package com.yupi.yuaiagent.demo.rag;


import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.preretrieval.query.expansion.MultiQueryExpander;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 多查询拓展器，使用大模型将用户输入的查询语句拓展多个维度查询，就能在RAG中查询到更多的相关文档，
 * 建议使用便宜的大模型，降本增效，因为多查询会增加成本和不确定因素，慎用
 */
@Component
public class MultiQueryExpanderDemo {

    private final ChatClient.Builder chatClientBuilder;

    public MultiQueryExpanderDemo(ChatModel dashscopeChatModel) {
        chatClientBuilder = ChatClient.builder(dashscopeChatModel);
    }

    public List<Query> expand(String queryStr) {
        MultiQueryExpander queryExpander = MultiQueryExpander.builder()
                .chatClientBuilder(chatClientBuilder)
                .numberOfQueries(3)
                .build();
        List<Query> queries = queryExpander.expand(new Query(queryStr));
        return queries;
    }
}
