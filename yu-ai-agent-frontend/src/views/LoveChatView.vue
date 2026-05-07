<script setup>
import { ref, computed, onMounted } from 'vue'
import ChatRoom from '../components/ChatRoom.vue'
import { loveAppChatSseUrl } from '../api/http.js'
import { useSseChat } from '../composables/useSseChat.js'

const chatId = ref('')

const { messages, loading, error, send } = useSseChat((message) =>
  loveAppChatSseUrl(message, chatId.value),
)

/** 装饰用心形位置（固定种子，避免 hydration/布局乱跳） */
const heartSpots = [
  { t: '7%', l: '5%', s: 0.95, d: 0, r: -12 },
  { t: '18%', l: '90%', s: 0.72, d: 0.8, r: 8 },
  { t: '42%', l: '8%', s: 0.55, d: 1.6, r: -6 },
  { t: '58%', l: '92%', s: 0.68, d: 2.1, r: 14 },
  { t: '78%', l: '12%', s: 0.5, d: 0.4, r: -18 },
  { t: '88%', l: '78%', s: 0.82, d: 1.1, r: 6 },
  { t: '30%', l: '48%', s: 0.35, d: 2.8, r: 22 },
  { t: '65%', l: '52%', s: 0.4, d: 1.9, r: -10 },
]

const subtitleText = computed(() => {
  if (!chatId.value) return '正在为你布置一场温柔对话…'
  return `心事要说给懂的人听 · 会话 ${chatId.value}`
})

onMounted(() => {
  chatId.value = typeof crypto !== 'undefined' && crypto.randomUUID ? crypto.randomUUID() : String(Date.now())
})
</script>

<template>
  <div class="love-page">
    <div class="love-page__aurora" aria-hidden="true" />
    <div class="love-page__decor" aria-hidden="true">
      <span
        v-for="(h, i) in heartSpots"
        :key="i"
        class="love-page__heart"
        :style="{
          top: h.t,
          left: h.l,
          transform: `rotate(${h.r}deg) scale(${h.s})`,
          animationDelay: `${h.d}s`,
        }"
      >
        ♥
      </span>
    </div>

    <div class="love-page__main">
      <ChatRoom
        title="💗 AI 恋爱大师"
        :subtitle="subtitleText"
        theme-class="theme-love"
        ai-avatar-variant="love"
        :messages="messages"
        :loading="loading"
        :error="error"
        placeholder="写下心情、暧昧烦恼或想对 TA 说的话——我会温柔地陪你梳理💕"
        @send="send"
      />
    </div>
  </div>
</template>

<style>
.love-page {
  position: relative;
  min-height: 100dvh;
  min-height: 100vh;
  overflow-x: hidden;
  isolation: isolate;
  background: radial-gradient(900px 520px at 50% -20%, rgba(244, 114, 182, 0.22), transparent 55%),
    radial-gradient(700px 480px at 100% 40%, rgba(192, 132, 252, 0.14), transparent 50%),
    radial-gradient(600px 420px at 0% 80%, rgba(251, 113, 133, 0.12), transparent 50%),
    #0d0812;
}

.love-page__aurora {
  position: fixed;
  inset: 0;
  z-index: 0;
  pointer-events: none;
  background: radial-gradient(ellipse 120% 70% at 50% 110%, rgba(244, 114, 182, 0.09), transparent 45%);
  opacity: 0.95;
}

.love-page__decor {
  position: fixed;
  inset: 0;
  z-index: 0;
  pointer-events: none;
}

.love-page__heart {
  position: absolute;
  color: rgba(249, 168, 212, 0.45);
  font-size: clamp(1.35rem, 4.5vw, 2.1rem);
  line-height: 1;
  text-shadow: 0 0 28px rgba(244, 114, 182, 0.35);
  animation: love-float 9s ease-in-out infinite;
  user-select: none;
}

@keyframes love-float {
  0%,
  100% {
    translate: 0 0;
    opacity: 0.42;
  }
  50% {
    translate: 0 -10px;
    opacity: 0.72;
  }
}

.love-page__main {
  position: relative;
  z-index: 1;
}

/* ChatRoom 根节点：恋爱主题色与玻璃质感 */
.theme-love.chat-shell {
  --room-bg: linear-gradient(
    165deg,
    rgba(36, 16, 38, 0.92) 0%,
    rgba(18, 10, 22, 0.96) 42%,
    rgba(12, 8, 16, 0.98) 100%
  );
  --room-header-bg: linear-gradient(180deg, rgba(46, 20, 48, 0.55), rgba(26, 12, 30, 0.25));
  --room-footer-bg: linear-gradient(180deg, rgba(28, 12, 32, 0.2), rgba(18, 8, 22, 0.96));
  --room-border: rgba(251, 182, 206, 0.22);
  --room-accent: #fda4d0;
  --room-fg: #fff1f7;
  --user-bubble: linear-gradient(135deg, #fb7185 0%, #e879f9 48%, #c084fc 100%);
  --ai-bubble: linear-gradient(180deg, rgba(255, 255, 255, 0.1), rgba(255, 240, 248, 0.05));
}

.theme-love .chat-frame {
  filter: drop-shadow(0 18px 42px rgba(0, 0, 0, 0.35));
}

.theme-love .chat-header {
  border-bottom-color: rgba(251, 182, 206, 0.22);
}

.theme-love .head-text h1 {
  background: linear-gradient(92deg, #ffe4f3, #fda4d0 45%, #f0abfc);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}

.theme-love .sub {
  color: rgba(255, 241, 247, 0.78);
}

.theme-love .banner-error {
  border-bottom-color: rgba(251, 182, 206, 0.22);
}

.theme-love .bubble-ai {
  border-color: rgba(251, 182, 206, 0.22);
  box-shadow: 0 8px 28px rgba(244, 63, 132, 0.12);
}

.theme-love .bubble-user {
  box-shadow: 0 10px 32px rgba(236, 72, 153, 0.28);
}

.theme-love .back {
  color: #fbcfe8;
}

.theme-love .ai-meta {
  color: rgba(255, 228, 240, 0.72);
}

.theme-love .typewriter-caret {
  border-left-color: var(--room-accent);
}

.theme-love .chat-input {
  border-top-color: rgba(251, 182, 206, 0.2);
}
</style>
