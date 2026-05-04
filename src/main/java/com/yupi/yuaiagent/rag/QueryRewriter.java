package com.yupi.yuaiagent.rag;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yupi.yuaiagent.utils.youdao.AuthV3Util;
import com.yupi.yuaiagent.utils.youdao.HttpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.preretrieval.query.transformation.QueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import static com.yupi.yuaiagent.utils.youdao.AppInfo.APP_KEY;
import static com.yupi.yuaiagent.utils.youdao.AppInfo.APP_SECRET;

@Component
@Slf4j
public class QueryRewriter {

    private final QueryTransformer queryTransformer;

    /**
     * 初始化查询重写器
     *
     * @param dashscopeChatModel
     */
    public QueryRewriter(ChatModel dashscopeChatModel) {
        ChatClient.Builder builder = ChatClient.builder(dashscopeChatModel);
        //构建查询重写转换器
        queryTransformer = RewriteQueryTransformer.builder()
                .chatClientBuilder(builder)
                .build();
    }

    /**
     * 执行查询重写
     *
     * @param queryStr
     * @return
     */
    public String doQueryRewrite(String queryStr) {
        Query query = new Query(queryStr);
        Query transform = queryTransformer.transform(query);
        return transform.text();
    }

    public String doQueryTransform(String queryStr) {
        // 添加请求参数
        Map<String, String[]> params = createRequestParams(queryStr);
        // 添加鉴权相关参数
        try {
            AuthV3Util.addAuthParams(APP_KEY, APP_SECRET, params);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("连接有道翻译服务失败", e);
        }
        // 请求api服务
        byte[] result = HttpUtil.doPost("https://openapi.youdao.com/api", null, params, "application/json");
        String jsonStr = new String(result, StandardCharsets.UTF_8);
        JSONObject resObj = JSON.parseObject(jsonStr);

        // ==========================================
        // 【有道翻译API固定返回结构】👇 这是核心！
        // ==========================================
        String errorCode = resObj.getString("errorCode");
        String translation =null;
        if ("0".equals(errorCode)) {
            // 翻译成功 → 取出翻译结果
            translation = resObj.getJSONArray("translation").getString(0);
        } else {
            // 翻译失败
            log.error("调用失败，错误码：{}" ,errorCode);
        }
        return translation;
    }


    private Map<String, String[]> createRequestParams(String q) {
        /*
         * note: 将下列变量替换为需要请求的参数
         * 取值参考文档: https://ai.youdao.com/DOCSIRMA/html/%E8%87%AA%E7%84%B6%E8%AF%AD%E8%A8%80%E7%BF%BB%E8%AF%91/API%E6%96%87%E6%A1%A3/%E6%96%87%E6%9C%AC%E7%BF%BB%E8%AF%91%E6%9C%8D%E5%8A%A1/%E6%96%87%E6%9C%AC%E7%BF%BB%E8%AF%91%E6%9C%8D%E5%8A%A1-API%E6%96%87%E6%A1%A3.html
         */
//        String q = "待翻译文本";
        String from = "auto";
        String to = "en";
        String vocabId = "";

        return new HashMap<String, String[]>() {{
            put("q", new String[]{q});
            put("from", new String[]{from});
            put("to", new String[]{to});
            put("vocabId", new String[]{vocabId});
        }};
    }
}
