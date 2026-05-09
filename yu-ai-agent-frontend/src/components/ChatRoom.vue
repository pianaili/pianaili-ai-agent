<script setup>
import { ref, watch, computed } from 'vue'
import AiAvatar from './AiAvatar.vue'
import UserAvatar from './UserAvatar.vue'

const props = defineProps({
  title: { type: String, required: true },
  subtitle: { type: String, default: '' },
  themeClass: { type: String, default: '' },
  /** love | manus | default — 决定默认 AI 头像样式 */
  aiAvatarVariant: {
    type: String,
    default: 'default',
    validator: (v) => ['love', 'manus', 'default'].includes(v),
  },
  messages: { type: Array, default: () => [] },
  loading: { type: Boolean, default: false },
  error: { type: String, default: '' },
  placeholder: { type: String, default: '输入消息后按 Enter 发送（Shift+Enter 换行）' },
  appendPendingIndicator: { type: Boolean, default: false },
})

const emit = defineEmits(['send'])

const input = ref('')
const listEl = ref(null)
const isNearBottom = ref(true)
const SCROLL_THRESHOLD = 80

// 导航按钮：手动滚动时出现，3 秒无操作后消失
const showNavButtons = ref(false)
let navTimer = null
let isProgrammaticScroll = false

function showNavWithTimer() {
  showNavButtons.value = true
  clearTimeout(navTimer)
  navTimer = setTimeout(() => {
    showNavButtons.value = false
  }, 3000)
}

// wheel / touchstart 一定是用户主动操作，直接显示按钮
function handleWheel() {
  showNavWithTimer()
}

function handleTouchStart() {
  showNavWithTimer()
}

function handleScroll() {
  const el = listEl.value
  if (!el) return
  isNearBottom.value = el.scrollHeight - el.scrollTop - el.clientHeight < SCROLL_THRESHOLD

  // 兜底：非程序触发的 scroll 也显示按钮
  if (isProgrammaticScroll) return
  showNavWithTimer()
}

function scrollToLatest() {
  if (!isNearBottom.value) return
  const el = listEl.value
  if (!el || !el.lastElementChild) return
  isProgrammaticScroll = true
  el.lastElementChild.scrollIntoView({ block: 'end' })
  requestAnimationFrame(() => {
    requestAnimationFrame(() => {
      isProgrammaticScroll = false
    })
  })
}

function scrollToTop() {
  const el = listEl.value
  if (!el) return
  isProgrammaticScroll = true
  el.scrollTo({ top: 0, behavior: 'smooth' })
  setTimeout(() => {
    isProgrammaticScroll = false
  }, 600)
}

function scrollToBottom() {
  const el = listEl.value
  if (!el || !el.lastElementChild) return
  isProgrammaticScroll = true
  el.lastElementChild.scrollIntoView({ block: 'end' })
  requestAnimationFrame(() => {
    requestAnimationFrame(() => {
      isProgrammaticScroll = false
    })
  })
}

// 监听到有新气泡或内容变化时，DOM 已更新完毕再滚动 (flush: 'post')
watch(
  () => props.messages,
  () => scrollToLatest(),
  { deep: true, flush: 'post' },
)

// loading 启动时强制回到底部（用户发新消息）
watch(
  () => props.loading,
  (loading) => {
    if (loading) {
      isNearBottom.value = true
      scrollToLatest()
    }
  },
  { flush: 'post' },
)

function onSubmit() {
  const t = input.value.trim()
  if (!t || props.loading) return
  emit('send', input.value)
  input.value = ''
}

function onKeydown(e) {
  if (e.key !== 'Enter' || e.shiftKey) return
  e.preventDefault()
  onSubmit()
}

function showTypewriterCaret(m, index) {
  if (m.role !== 'assistant' || m.targetContent == null) return false
  const t = m.targetContent.length
  const c = (m.content ?? '').length
  if (c >= t) return false
  for (let i = 0; i < props.messages.length; i++) {
    const row = props.messages[i]
    if (row.role !== 'assistant' || row.targetContent == null) continue
    const rt = row.targetContent.length
    const rc = (row.content ?? '').length
    if (rc < rt) return i === index
  }
  return false
}

const showStepPendingIndicator = computed(() => {
  if (!props.appendPendingIndicator || !props.loading) return false
  for (const m of props.messages) {
    if (m.role !== 'assistant' || m.targetContent == null) continue
    if ((m.content ?? '').length < (m.targetContent ?? '').length) return false
  }
  return true
})

function messageKey(m, i) {
  return m.id ?? i
}
</script>

<template>
  <div class="chat-shell" :class="themeClass">
    <div class="chat-frame">
      <header class="chat-header">
        <RouterLink class="back" to="/">← 返回应用中心</RouterLink>
        <div class="head-text">
          <h1>{{ title }}</h1>
          <p v-if="subtitle" class="sub">{{ subtitle }}</p>
        </div>
      </header>

      <p v-if="error" class="banner-error" role="alert">{{ error }}</p>

      <main
        ref="listEl"
        class="chat-list"
        role="log"
        aria-live="polite"
        @scroll="handleScroll"
        @wheel="handleWheel"
        @touchstart="handleTouchStart"
      >
        <div
          v-for="(m, i) in messages"
          :key="messageKey(m, i)"
          class="row"
          :class="m.role === 'user' ? 'row-user' : 'row-ai'"
        >
          <template v-if="m.role === 'user'">
            <div class="msg-stack msg-stack--user">
              <div class="bubble bubble-user">
                <span class="meta meta-in-bubble">我</span>
                <div class="content content-user">{{ m.content }}</div>
              </div>
            </div>
            <UserAvatar />
          </template>

          <template v-else>
            <AiAvatar :variant="aiAvatarVariant" />
            <div class="msg-stack msg-stack--ai">
              <span class="ai-meta">{{ aiMetaLabel }}</span>
              <div class="bubble bubble-ai">
                <div
                  v-if="
                    !m.content &&
                    !(m.targetContent?.length) &&
                    loading &&
                    i === messages.length - 1
                  "
                  class="typing-inline"
                >
                  <span class="dots"><i /><i /><i /></span>
                </div>
                <div v-else class="content content-ai">
                  {{ m.content }}<span
                    v-if="showTypewriterCaret(m, i)"
                    class="typewriter-caret"
                    aria-hidden="true"
                  />
                </div>
              </div>
            </div>
          </template>
        </div>

        <div v-if="showStepPendingIndicator" class="row row-ai row-pending">
          <AiAvatar :variant="aiAvatarVariant" />
          <div class="msg-stack msg-stack--ai">
            <span class="ai-meta">下一步</span>
            <div class="bubble bubble-ai bubble-pending">
              <div class="typing-inline">
                <span class="dots"><i /><i /><i /></span>
              </div>
            </div>
          </div>
        </div>
      </main>
    </div>

    <footer class="chat-input">
      <Transition name="nav-fade">
        <div v-show="showNavButtons" class="nav-buttons">
          <button
            type="button"
            class="nav-btn"
            aria-label="滚动到顶部"
            title="回到顶部"
            @click="scrollToTop"
          >
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
          </button>
          <button
            type="button"
            class="nav-btn"
            aria-label="滚动到底部"
            title="回到底部"
            @click="scrollToBottom"
          >
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"><polyline points="6 9 12 15 18 9"/></svg>
          </button>
        </div>
      </Transition>
      <div class="chat-input-inner">
        <textarea
          v-model="input"
          rows="2"
          :placeholder="placeholder"
          :disabled="loading"
          :aria-busy="loading"
          @keydown="onKeydown"
        />
        <button type="button" class="send" :disabled="loading || !input.trim()" @click="onSubmit">
          发送
        </button>
      </div>
    </footer>
  </div>
</template>

<style scoped>
.chat-shell {
  min-height: 100dvh;
  display: flex;
  flex-direction: column;
  background: var(--room-bg, var(--yu-bg-mid, #0f1218));
  color: var(--room-fg, var(--yu-text, #e8eaef));
}

.chat-frame {
  flex: 1;
  width: 100%;
  max-width: var(--yu-chat-max, min(900px, 100%));
  margin-inline: auto;
  display: flex;
  flex-direction: column;
  min-height: 0;
  padding-inline: var(--yu-page-pad, clamp(0.75rem, 4vw, 1.5rem));
}

.chat-header {
  display: flex;
  align-items: flex-start;
  gap: clamp(0.65rem, 2.5vw, 1rem);
  padding: clamp(0.85rem, 3vw, 1.15rem) 0;
  border-bottom: 1px solid var(--room-border, rgba(255, 255, 255, 0.08));
  background: var(--room-header-bg, transparent);
}

.back {
  flex-shrink: 0;
  margin-top: 0.25rem;
  color: var(--room-accent, var(--yu-accent, #7dd3fc));
  text-decoration: none;
  font-size: 0.875rem;
  white-space: nowrap;
}

.back:hover {
  text-decoration: underline;
}

.head-text {
  min-width: 0;
  flex: 1;
}

.head-text h1 {
  margin: 0;
  font-size: clamp(1.1rem, 2.8vw, 1.35rem);
  font-weight: 650;
  letter-spacing: -0.02em;
  line-height: 1.25;
}

.sub {
  margin: 0.35rem 0 0;
  font-size: clamp(0.78rem, 2.2vw, 0.85rem);
  opacity: 0.78;
  word-break: break-word;
  line-height: 1.45;
}

.banner-error {
  margin: 0;
  padding: 0.65rem 0;
  background: transparent;
  color: #fecaca;
  font-size: 0.9rem;
  border-bottom: 1px solid rgba(248, 113, 113, 0.25);
}

.chat-list {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  padding: clamp(0.85rem, 3vw, 1.15rem) 0
    calc(5.75rem + env(safe-area-inset-bottom, 0px));
  display: flex;
  flex-direction: column;
  gap: clamp(0.65rem, 2.5vw, 1rem);
  -webkit-overflow-scrolling: touch;
}

.row {
  display: flex;
  align-items: flex-start;
  gap: clamp(0.45rem, 2vw, 0.65rem);
  max-width: 100%;
}

.row-user {
  flex-direction: row;
  justify-content: flex-end;
}

.row-ai {
  flex-direction: row;
  justify-content: flex-start;
}

.msg-stack {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 0.28rem;
}

.msg-stack--user {
  flex: 1 1 auto;
  align-items: flex-end;
  min-width: 0;
}

.msg-stack--ai {
  flex: 1;
  align-items: flex-start;
  max-width: min(720px, calc(100% - 3rem));
}

.ai-meta {
  font-size: 0.68rem;
  font-weight: 600;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  opacity: 0.62;
  padding-left: 0.1rem;
}

.row-pending .bubble-pending {
  opacity: 0.92;
  border-style: dashed;
}

.bubble {
  padding: 0.62rem 0.82rem;
  border-radius: var(--yu-radius-md, 14px);
  line-height: 1.55;
  white-space: pre-wrap;
  word-break: break-word;
  max-width: 100%;
}

.bubble-user {
  width: fit-content;
  max-width: min(720px, 100%);
  background: var(--user-bubble, linear-gradient(135deg, #38bdf8, #818cf8));
  color: #0b1020;
  border-bottom-right-radius: 5px;
  box-shadow: 0 8px 26px rgba(0, 0, 0, 0.22);
}

.bubble-ai {
  background: var(--ai-bubble, rgba(255, 255, 255, 0.06));
  border: 1px solid var(--room-border, rgba(255, 255, 255, 0.08));
  border-bottom-left-radius: 5px;
  box-shadow: 0 6px 22px rgba(0, 0, 0, 0.18);
}

.meta-in-bubble {
  display: block;
  font-size: 0.66rem;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  opacity: 0.55;
  margin-bottom: 0.28rem;
}

.content {
  font-size: clamp(0.88rem, 2.4vw, 0.95rem);
}

.content-ai {
  text-align: left;
  width: 100%;
}

.content-user {
  text-align: left;
}

.typewriter-caret {
  display: inline-block;
  width: 0.55ch;
  margin-left: 1px;
  border-left: 2px solid var(--room-accent, #7dd3fc);
  height: 1em;
  vertical-align: -0.12em;
  animation: caret-blink 0.95s step-end infinite;
}

@keyframes caret-blink {
  50% {
    opacity: 0;
  }
}

.typing-inline {
  min-height: 1.25rem;
}

.typing-inline .dots {
  display: inline-flex;
  gap: 4px;
  align-items: center;
  padding: 0.2rem 0;
}

.typing-inline .dots i {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--room-accent, #7dd3fc);
  opacity: 0.35;
  animation: pulse 1s ease-in-out infinite;
}

.typing-inline .dots i:nth-child(2) {
  animation-delay: 0.15s;
}

.typing-inline .dots i:nth-child(3) {
  animation-delay: 0.3s;
}

@keyframes pulse {
  0%,
  100% {
    opacity: 0.35;
    transform: translateY(0);
  }
  50% {
    opacity: 1;
    transform: translateY(-2px);
  }
}

.chat-input {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  border-top: 1px solid var(--room-border, rgba(255, 255, 255, 0.08));
  background: var(--room-footer-bg, rgba(10, 12, 18, 0.92));
  backdrop-filter: blur(12px);
  padding-bottom: env(safe-area-inset-bottom, 0px);
}

.chat-input-inner {
  max-width: var(--yu-chat-max, min(900px, 100%));
  margin: 0 auto;
  width: 100%;
  padding: clamp(0.55rem, 2.5vw, 0.85rem) var(--yu-page-pad, clamp(0.75rem, 4vw, 1.5rem));
  display: flex;
  gap: clamp(0.45rem, 2vw, 0.75rem);
  align-items: flex-end;
}

.chat-input textarea {
  flex: 1;
  min-height: 2.75rem;
  resize: none;
  border-radius: var(--yu-radius-sm, 10px);
  border: 1px solid var(--room-border, rgba(255, 255, 255, 0.12));
  background: rgba(0, 0, 0, 0.35);
  color: inherit;
  padding: 0.62rem 0.78rem;
  font: inherit;
  line-height: 1.45;
}

.chat-input textarea:focus {
  outline: 2px solid var(--room-accent, #7dd3fc);
  outline-offset: 0;
}

.chat-input textarea:disabled {
  opacity: 0.58;
}

.send {
  flex-shrink: 0;
  border: none;
  border-radius: var(--yu-radius-sm, 10px);
  padding: 0.62rem clamp(1rem, 4vw, 1.25rem);
  min-height: 2.75rem;
  font-weight: 650;
  cursor: pointer;
  background: var(--room-accent, #38bdf8);
  color: #0b1020;
}

.send:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}

/* 导航按钮：相对固定底栏绝对定位，紧贴输入区上方，不随消息列表或页面滚动 */
.nav-buttons {
  position: absolute;
  right: clamp(0.75rem, 4vw, 1.5rem);
  bottom: 100%;
  margin-bottom: 0.45rem;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  z-index: 20;
}

.nav-btn {
  width: 2.5rem;
  height: 2.5rem;
  border-radius: 50%;
  border: 1px solid var(--room-border, rgba(255, 255, 255, 0.12));
  background: var(--room-footer-bg, rgba(10, 12, 18, 0.88));
  color: var(--room-fg, rgba(255, 255, 255, 0.75));
  backdrop-filter: blur(8px);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.2s, color 0.2s, transform 0.2s;
}

.nav-btn:hover {
  background: var(--room-accent, #38bdf8);
  color: #0b1020;
  border-color: transparent;
  transform: scale(1.08);
}

.nav-btn:active {
  transform: scale(0.95);
}

/* Vue Transition */
.nav-fade-enter-active {
  transition: opacity 0.3s ease;
}
.nav-fade-leave-active {
  transition: opacity 0.35s ease;
}
.nav-fade-enter-from,
.nav-fade-leave-to {
  opacity: 0;
}

@media (max-width: 720px) {
  .chat-header {
    flex-wrap: wrap;
  }

  .back {
    width: 100%;
    margin-top: 0;
    padding-bottom: 0.15rem;
    border-bottom: 1px solid rgba(255, 255, 255, 0.06);
  }

  .head-text {
    flex-basis: 100%;
  }
}

@media (max-width: 520px) {
  .chat-input-inner {
    flex-direction: column;
    align-items: stretch;
  }

  .send {
    width: 100%;
    min-height: 2.85rem;
  }

  .msg-stack--ai {
    max-width: calc(100% - 3rem);
  }
}

@media (min-width: 1024px) {
  .chat-list {
    padding-bottom: calc(6rem + env(safe-area-inset-bottom, 0px));
  }
}
</style>
