package com.yupi.yuaiagent.agent;


import cn.hutool.core.util.StrUtil;
import com.yupi.yuaiagent.agent.model.AgentState;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 抽象基础代理类，用于管理代理状态和执行流程。
 * 提供状态转换、内存管理和基于步骤的执行循环的基础功能。
 * 子类必须实现step方法。
 */
@Data
@Slf4j
public abstract class BaseAgent {

    //核心属性
    private String name;

    //提示词
    private String systemPrompt;
    private String nextStepPrompt;

    //代理状态，默认是空闲状态
    private AgentState state = AgentState.IDLE;

    //执行步骤控制
    private int currentStep = 0;
    private int maxSteps = 10;

    //LLM 大模型
    private ChatClient chatClient;

    //Memory 记忆（需要自主维护对话上下文）
    private List<Message>  messageList = new ArrayList<>();

    /**
     * 运行代理
     *
     * @param userPrompt  用户提示词
     * @return  执行的结果
     */
    public String run(String userPrompt){
        //1.基础校验
        if (this.state != AgentState.IDLE) {
            throw new RuntimeException("Cannot run agent from state: " + this.state);
        }
        if (StrUtil.isBlank(userPrompt)) {
            throw new RuntimeException("Cannot run agent with empty user prompt");
        }
        //2.执行，更改状态
        this.state = AgentState.RUNNING;
        //记录消息上下文
        messageList.add(new UserMessage(userPrompt));
        //保存结果列表
        List<String> results = new ArrayList<>();
        try {
            //执行循环
            for (int i = 0; i < maxSteps && state != AgentState.FINISHED; i++) {
                int stepNumber = i + 1;
                currentStep = stepNumber;
                log.info("Executing step {}/{}", stepNumber, maxSteps);
                //单步执行
                String stepResult = step();
                results.add(stepResult);
                if (currentStep >= maxSteps) {
                    state = AgentState.FINISHED;
                    results.add("Terminated: Reached max steps (" + maxSteps + ")");
                }
            }
            return String.join("\n", results);
        } catch (Exception e) {
            state = AgentState.ERROR;
            log.error("Error executing agent", e);
            return "执行错误" + e.getMessage();
        }finally {
            //3.清理资源
            cleanup();
        }
    }
    /**
     * 运行代理,流式输出
     *
     * @param userPrompt  用户提示词
     * @return  执行的结果
     */
    public SseEmitter runWithStream(String userPrompt){
        //创建一个超时时间较长的SseEmitter对象
        SseEmitter sseEmitter = new SseEmitter(300000L);
        //使用线程异步处理，避免主线程阻塞
        CompletableFuture.runAsync(() -> {
            //1.基础校验
            if (this.state != AgentState.IDLE) {
                safeSend(sseEmitter, "Cannot run agent from state: " + this.state);
                sseEmitter.complete();
                return;
            }
            if (StrUtil.isBlank(userPrompt)) {
                safeSend(sseEmitter, "Cannot run agent with empty user prompt");
                sseEmitter.complete();
                return;
            }
            //2.执行，更改状态
            this.state = AgentState.RUNNING;
            //记录消息上下文
            messageList.add(new UserMessage(userPrompt));
            //保存结果列表
            List<String> results = new ArrayList<>();
            try {
                //执行循环
                for (int i = 0; i < maxSteps && state != AgentState.FINISHED; i++) {
                    int stepNumber = i + 1;
                    currentStep = stepNumber;
                    log.info("Executing step {}/{}", stepNumber, maxSteps);
                    //单步执行
                    String stepResult = step();
                    results.add(stepResult);
                    //输出当前每一步的结果
                    if (!safeSend(sseEmitter, stepResult)) {
                        break;
                    }
                    if (currentStep >= maxSteps) {
                        state = AgentState.FINISHED;
                        results.add("Terminated: Reached max steps (" + maxSteps + ")");
                        safeSend(sseEmitter, "执行结束：达到最大步骤（" + maxSteps + "）");
                    }
                }
                //正常完成
                sseEmitter.complete();
            } catch (Exception e) {
                state = AgentState.ERROR;
                log.error("Error executing agent", e);
                safeSend(sseEmitter, "执行错误：" + e.getMessage());
                sseEmitter.completeWithError(e);
//                return "执行错误" + e.getMessage();
            }finally {
                //3.清理资源
                this.cleanup();
            }
        });
        //设置超时回调
        sseEmitter.onTimeout(() -> {
            this.state = AgentState.ERROR;
            this.cleanup();
            log.warn("SSE connect timed out");
        });
        //设置超时回调
        sseEmitter.onCompletion(() -> {
            if (this.state == AgentState.RUNNING){
                this.state = AgentState.FINISHED;
            }
            this.cleanup();
            log.info("SSE connect completed");
        });
        return sseEmitter;
    }

    /**
     * 定义单个步骤
     * @return
     */
    public abstract String step();

    /**
     * 清理资源
     */
    protected void cleanup(){
        //子类可以重写此方法来清理资源
    }

    /**
     * 安全发送SSE消息，如果emitter已完成则返回false
     */
    private boolean safeSend(SseEmitter emitter, String message) {
        try {
            emitter.send(message);
            return true;
        } catch (IllegalStateException e) {
            log.warn("SSE emitter already completed, cannot send: {}", message);
            return false;
        } catch (IOException e) {
            log.error("SSE send failed", e);
            emitter.completeWithError(e);
            return false;
        }
    }
}
