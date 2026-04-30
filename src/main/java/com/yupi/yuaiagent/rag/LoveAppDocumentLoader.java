package com.yupi.yuaiagent.rag;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 恋爱大师应用文档加载器
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class LoveAppDocumentLoader {

    //注入资源解析器
    private final ResourcePatternResolver resourcePatternResolver;

    /**
     * //加载多篇Markdown文档
     * @return
     */
    public List<Document> loadMarkdown(){
        List<Document> documents = new ArrayList<>();
        try {
            Resource[] resources = resourcePatternResolver.getResources("classpath:document/*.md");
            for (Resource resource : resources) {
                String filename = resource.getFilename();
                //Markdown文档加载器配置
                MarkdownDocumentReaderConfig config = MarkdownDocumentReaderConfig.builder()
                        .withHorizontalRuleCreateDocument(true)
                        .withIncludeCodeBlock(false)
                        .withIncludeBlockquote(false)
                        .withAdditionalMetadata("filename", filename)
                        .build();
                //new一个Markdown文档加载器
                MarkdownDocumentReader markdownDocumentReader = new MarkdownDocumentReader(resource, config);
                //得到所有的文档资源
                documents.addAll(markdownDocumentReader.get());
            }

        } catch (IOException e) {
            log.error("加载Markdown文件失败",e);
        }
        return documents;
    }

}
