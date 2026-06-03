package com.yupi.yuaiagent.rag;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PgVectorVectorStoreConfigTest {

    @Resource
    private VectorStore pgVectorVectorStore;
    @Resource
    private LoveAppDocumentLoader loveAppDocumentLoader;
    @Resource
    private MyKeyWordEnricher myKeyWordEnricher;

    @Test
    void pgVectorVectorStore() {
//        List<Document> documents = List.of(
//                new Document("Spring AI rocks!! Spring AI rocks!! Spring AI rocks!! Spring AI rocks!! Spring AI rocks!!", Map.of("meta1", "meta1")),
//                new Document("The World is Big and Salvation Lurks Around the Corner"),
//                new Document("You walk forward facing the past and you turn back toward the future.", Map.of("meta2", "meta2")));
        List<Document> documentList = loveAppDocumentLoader.loadMarkdown();
        documentList = myKeyWordEnricher.enrichDocuments(documentList);
        if (documentList.size() > 10) {
            List<Document> documents = new ArrayList<>(10);
            for (int i = 1; i <= documentList.size(); i++) {
                documents.add(documentList.get(i - 1));
                if (i % 10 == 0 || i == documentList.size()) {
                    pgVectorVectorStore.add(documents);
                    documents.clear();
                }
            }
        } else {
            pgVectorVectorStore.add(documentList);
        }

        // 添加文档
//        pgVectorVectorStore.add(documents);

        // 相似度查询
        List<Document> results = this.pgVectorVectorStore
                .similaritySearch(SearchRequest.builder()
                        .query("我已经结婚了，但是婚后关系不太亲密，该怎么办？")
                        .topK(3)
                        .build());
        Assertions.assertNotNull(results);
    }
}