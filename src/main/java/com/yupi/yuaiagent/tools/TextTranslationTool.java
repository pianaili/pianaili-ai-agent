package com.yupi.yuaiagent.tools;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yupi.yuaiagent.utils.youdao.AuthV3Util;
import com.yupi.yuaiagent.utils.youdao.HttpUtil;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import static com.yupi.yuaiagent.utils.youdao.AppInfo.APP_KEY;
import static com.yupi.yuaiagent.utils.youdao.AppInfo.APP_SECRET;

/**
 * 文本翻译工具
 */
public class TextTranslationTool {


    @Tool(description = "Text translation")
    public String textTranslation(@ToolParam(description = "The text to be translated") String text) {

        try {
            // 添加请求参数
            Map<String, String[]> params = createRequestParams(text);
            // 添加鉴权相关参数
            AuthV3Util.addAuthParams(APP_KEY, APP_SECRET, params);
            // 请求api服务
            byte[] result = HttpUtil.doPost("https://openapi.youdao.com/api", null, params, "application/json");
            String jsonStr = new String(result, StandardCharsets.UTF_8);
            JSONObject resObj = JSON.parseObject(jsonStr);

            // ==========================================
            // 【有道翻译API固定返回结构】👇 这是核心！
            // ==========================================
            String errorCode = resObj.getString("errorCode");
            if (!"0".equals(errorCode)){
                throw new RuntimeException();
            }
            return resObj.getJSONArray("translation").getString(0);
        } catch (NoSuchAlgorithmException e) {
            return "Error translating text: " + e.getMessage();
        }
    }

    private Map<String, String[]> createRequestParams(String q) {
        /*
         * note: 将下列变量替换为需要请求的参数
         * 取值参考文档: https://ai.youdao.com/DOCSIRMA/html/%E8%87%AA%E7%84%B6%E8%AF%AD%E8%A8%80%E7%BF%BB%E8%AF%91/API%E6%96%87%E6%A1%A3/%E6%96%87%E6%9C%AC%E7%BF%BB%E8%AF%91%E6%9C%8D%E5%8A%A1/%E6%96%87%E6%9C%AC%E7%BF%BB%E8%AF%91%E6%9C%8D%E5%8A%A1-API%E6%96%87%E6%A1%A3.html
         */
//        String q = "待翻译文本";
        String from = "auto";
        String to = "zh-CHS";
        String vocabId = "您的用户词表ID";

        return new HashMap<String, String[]>() {{
            put("q", new String[]{q});
            put("from", new String[]{from});
            put("to", new String[]{to});
            put("vocabId", new String[]{vocabId});
        }};
    }
}
