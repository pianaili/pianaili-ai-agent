package com.yupi.yuaiagent.tools;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;
//@SpringBootTest
class TextTranslationToolTest {

    @Test
    void textTranslation() {
        TextTranslationTool textTranslationTool = new TextTranslationTool();
        String result = textTranslationTool.textTranslation("hello world");
        System.out.println(result);
        assertNotNull(result);

    }
}