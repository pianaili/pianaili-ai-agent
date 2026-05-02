package com.yupi.yuaiagent.rag;



import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;

/**
 * 创建上下文查询增强的工厂
 */
public class LoveAppContextualQueryAugmenterFactory {
    /**
     * 创建上下文查询增强器
     * @return
     */
    public static ContextualQueryAugmenter createInstance(){
        PromptTemplate empContextPromptTemplate = new PromptTemplate("""
                你应该输出下面的内容：
                抱歉，我只能回答恋爱相关的问题，别的没办法帮到您哦，
                有问题可以联系编程导航客服 https://codefather.cn
                """);
        return ContextualQueryAugmenter.builder()
                .allowEmptyContext(false)//false是开启自定义回复，改成true会继续查询
                .emptyContextPromptTemplate(empContextPromptTemplate)
                .build();
    }
}
