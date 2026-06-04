package com.yupi.yuaiagent.agent;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.yupi.yuaiagent.agent.model.AgentState;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Data
@EqualsAndHashCode(callSuper = true)
public class ToolCallAgent extends ReActAgent{

    // 可用的工具
    private final ToolCallback[] availableTools;
    private  final ToolCallbackProvider toolCallbackProvider;

    // 保存了工具调用信息的响应
    private ChatResponse toolCallChatResponse;

    // 工具调用管理者
    private final ToolCallingManager toolCallingManager;

    // 禁用内置的工具调用机制，自己维护上下文
    private final ChatOptions chatOptions;

    public ToolCallAgent(ToolCallback[] availableTools,ToolCallbackProvider toolCallbackProvider) {
        super();
        this.availableTools = availableTools;
        this.toolCallbackProvider = toolCallbackProvider;
        this.toolCallingManager = ToolCallingManager.builder().build();
        // 禁用 Spring AI 内置的工具调用机制，自己维护选项和消息上下文
        this.chatOptions = DashScopeChatOptions.builder()
//                .withProxyToolCalls(true)
                .internalToolExecutionEnabled(false)
                .build();
    }
    /**
     * 处理当前状态并决定下一步行动
     *
     * @return 是否需要执行行动，true表示需要执行，false表示不需要执行
     */
    @Override
    public boolean think() {
        //1.校验提示词，拼接用户提示词
        if (StrUtil.isNotBlank(getNextStepPrompt())) {
            UserMessage userMessage = new UserMessage(getNextStepPrompt());
            getMessageList().add(userMessage);
        }
        try {
            //2.调用AI大模型，获取工具调用结果
            List<Message> messageList = getMessageList();
            Prompt prompt = new Prompt(messageList, this.chatOptions);
            ChatResponse chatResponse = getChatClient().prompt(prompt)
                    .system(getSystemPrompt())
                    .toolCallbacks(availableTools)
                    .toolCallbacks(toolCallbackProvider)
                    .call()
                    .chatResponse();
            //记录响应，用于act
            this.toolCallChatResponse = chatResponse;
            //3.解析响应结果，获取调用工具的信息
            //助手消息
            AssistantMessage assistantMessage = chatResponse.getResult().getOutput();
            //获取要调用的工具列表
            List<AssistantMessage.ToolCall> toolCallList = assistantMessage.getToolCalls();
            //得到提示消息
            String result = assistantMessage.getText();
            log.info(getName()+"的思考结果:"+result);
            log.info(getName()+"选择了"+toolCallList.size()+"个工具要调用");
            String toolCallInfo = toolCallList.stream()
                    .map(toolCall -> String.format("工具的名称:%S,参数:%S", toolCall.name(), toolCall.arguments()))
                    .collect(Collectors.joining("\n"));
            log.info(toolCallInfo);
            //如果不需要调用工具，返回false
            if (toolCallList.isEmpty()) {
                //只有不需要调用工具的时候才需要手动记录助手消息
                getMessageList().add(assistantMessage);
                return false;
            }else {
                //需要调用工具时，无需记录助手消息，因为调用工具时会自动记录
                return  true;
            }
        } catch (Exception e) {
            log.error(getName()+"的思考过程遇到了问题："+e.getMessage());
            getMessageList().add(new AssistantMessage("处理时遇到了错误："+e.getMessage()));
            return  false;
        }
    }
    /**
     * 执行单个步骤：思考和行动
     * 执行工具调用并处理返回结果
     * @return 步骤执行结果
     */
    @Override
    public String act() {
        if (!toolCallChatResponse.hasToolCalls()){
            return "没有工具需要调用";
        }
        Prompt prompt = new Prompt(getMessageList(), this.chatOptions);
        //调用工具
        ToolExecutionResult toolExecutionResult = toolCallingManager.executeToolCalls(prompt, toolCallChatResponse);
        //记录消息上下文,conversationHistory里面已经包含了助手消息和工具调用返回结果
        setMessageList(toolExecutionResult.conversationHistory());
        //获取工具调用返回结果
        ToolResponseMessage toolResponseMessage = (ToolResponseMessage) CollUtil.getLast(toolExecutionResult.conversationHistory());
        //判断是否调用了终止工具
        boolean doTerminateToolCalled = toolResponseMessage.getResponses().stream()
                .anyMatch(toolResponse -> toolResponse.name().equals("doTerminate"));
        if (doTerminateToolCalled) {
            //结束任务，更改状态
            setState(AgentState.FINISHED);
        }
        //将工具调用结果转换成String类型
        String results = toolResponseMessage.getResponses().stream()
                .map(response -> "工具:" + response.name() + "返回的结果:" + response.responseData())
                .collect(Collectors.joining("\n"));
        log.info(results);
        return results;
    }

    /**
     * 清理资源，重置代理到初始状态以便复用。
     * 在父类清理基础上，额外清除工具调用响应引用。
     */
    @Override
    protected void cleanup() {
        // 清除工具调用响应，释放内存并避免脏状态影响下次运行
        this.toolCallChatResponse = null;
        // 调用父类清理（重置状态、步骤计数器、消息列表）
        super.cleanup();
    }
}
