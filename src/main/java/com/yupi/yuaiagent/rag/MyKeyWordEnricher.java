package com.yupi.yuaiagent.rag;


import jakarta.annotation.Resource;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.model.transformer.KeywordMetadataEnricher;
import org.springframework.ai.model.transformer.SummaryMetadataEnricher;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 基于Ai的文档元信息增强器（为文档补充元信息）
 */
@Component
public class MyKeyWordEnricher {

    //引入阿里的大模型
    @Resource(name = "dashScopeChatModel")
    private ChatModel dashscopeChatModel;

    /**
     * 关键词元信息增强器
     *
     * @param documents 文档
     * @return {@link List }<{@link Document }>
     */
    public List<Document> enrichDocuments(List<Document> documents) {
        KeywordMetadataEnricher keywordMetadataEnricher = new KeywordMetadataEnricher(dashscopeChatModel, 5);
        return keywordMetadataEnricher.apply(documents);
    }

    /**
     * 摘要元信息增强器
     *
     * @param documents 文档
     * @return {@link List }<{@link Document }>
     */
    List<Document> enrichDocumentsBySummary(List<Document> documents) {
        SummaryMetadataEnricher enricher = new SummaryMetadataEnricher(dashscopeChatModel,
                List.of(SummaryMetadataEnricher.SummaryType.PREVIOUS, SummaryMetadataEnricher.SummaryType.CURRENT, SummaryMetadataEnricher.SummaryType.NEXT));
        return enricher.apply(documents);
    }
}
