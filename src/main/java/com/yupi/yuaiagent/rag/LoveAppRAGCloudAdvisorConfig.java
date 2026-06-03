package com.yupi.yuaiagent.rag;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.rag.DashScopeDocumentRetriever;
import com.alibaba.cloud.ai.dashscope.rag.DashScopeDocumentRetrieverOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 自定义基于阿里云知识库服务的RAG增强顾问
 */
@Configuration
@Slf4j
public class LoveAppRAGCloudAdvisorConfig {

    @Value("${spring.ai.dashscope.api-key}")
    private String dashscopeApiKey;

    @Bean
    public Advisor loveAppRAGCloudAdvisor() {
        final String KnowledgeBase = "恋爱大师";
        DashScopeApi dashScopeApi = DashScopeApi.builder().apiKey(dashscopeApiKey).build();
        //获取阿里云RAG向量知识库检索到的文档，文档召回
        DocumentRetriever retriever = new DashScopeDocumentRetriever(dashScopeApi,
                DashScopeDocumentRetrieverOptions.builder()
                        .indexName(KnowledgeBase)
                        .sparseSimilarityTopK(3) //稠密检索，根据语义匹配
                        .sparseSimilarityTopK(3) //稀疏检索，根据关键词匹配
                        .build());
        //通过召回的文档构建检索增强Advisor
        return RetrievalAugmentationAdvisor.builder()
                .documentRetriever(retriever)
                .build();

    }
}
