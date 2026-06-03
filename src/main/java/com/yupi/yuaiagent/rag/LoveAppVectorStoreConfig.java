package com.yupi.yuaiagent.rag;

import jakarta.annotation.Resource;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class LoveAppVectorStoreConfig {

    @Resource
    private LoveAppDocumentLoader loveAppDocumentLoader;
    @Resource
    private MyTokenTextSplitter  myTokenTextSplitter;
    @Resource
    private MyKeyWordEnricher   myKeyWordEnricher;

    @Bean
    VectorStore loveAppVectorStore(@Qualifier("dashscopeEmbeddingModel") EmbeddingModel embeddingModel) {
        //构建一个普通向量存储对象
        SimpleVectorStore simpleVectorStore = SimpleVectorStore.builder(embeddingModel).build();
        //获取document对象集合
        List<Document> documentList = loveAppDocumentLoader.loadMarkdown();
        //将加载的文档用自定义的Token切词器再次进行切分
//        List<Document> SplitDocuments = myTokenTextSplitter.splitCustomized(documentList);
        //基于AI的自动补充关键词元信息
        List<Document> EnrichDocuments = myKeyWordEnricher.enrichDocuments(documentList);
        //将文档写入到向量数据库中
        simpleVectorStore.add(EnrichDocuments);
        return simpleVectorStore;
    }
}
