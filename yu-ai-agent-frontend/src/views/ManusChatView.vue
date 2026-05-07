<script setup>
import ChatRoom from '../components/ChatRoom.vue'
import { manusChatSseUrl } from '../api/http.js'
import { useSseChat } from '../composables/useSseChat.js'

const { messages, loading, error, send } = useSseChat(
  (message) => manusChatSseUrl(message),
  { separateAssistantBubblePerSsePayload: true },
)
</script>

<template>
  <ChatRoom
    title="AI 超级智能体"
    subtitle="Manus · 每一步单独气泡；步骤之间自动插入换行分隔"
    theme-class="theme-manus"
    ai-avatar-variant="manus"
    :messages="messages"
    :loading="loading"
    :error="error"
    append-pending-indicator
    placeholder="描述任务或问题；回复按步骤逐条出现…"
    @send="send"
  />
</template>

<style>
.theme-manus {
  --room-bg: linear-gradient(180deg, #0c1628 0%, #0a0e14 50%);
  --room-header-bg: rgba(12, 22, 40, 0.72);
  --room-footer-bg: rgba(10, 14, 20, 0.94);
  --room-border: rgba(56, 189, 248, 0.2);
  --room-accent: #38bdf8;
  --room-fg: #e8eaef;
  --ai-bubble: rgba(56, 189, 248, 0.08);
}
</style>
