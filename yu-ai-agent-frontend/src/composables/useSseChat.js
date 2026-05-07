import { ref, onUnmounted } from 'vue'

/** 打字机速度：每秒展示的字符数（中英混合按字符计） */
const TYPEWRITER_CPS = 36

/** 去掉 BOM / 零宽字符后再 trim，避免「肉眼空白」仍创建气泡 */
function normalizeSsePayload(text) {
  return String(text)
    .replace(/\uFEFF/g, '')
    .replace(/[\u200B-\u200D\u2060]/g, '')
    .trim()
}

function shouldEmitPayload(normalized) {
  return normalized.length > 0 && normalized !== '[DONE]'
}

/**
 * 解析 SSE：同一事件内多行 `data:` 合并为一条（用 \\n 连接），空事件不回调。
 * 事件之间以空行分隔（RFC 8895）。
 */
async function readSseStream(url, signal, onChunk) {
  const res = await fetch(url, {
    signal,
    headers: { Accept: 'text/event-stream' },
  })
  if (!res.ok) {
    const text = await res.text().catch(() => '')
    throw new Error(text || `请求失败 (${res.status})`)
  }
  const reader = res.body?.getReader()
  if (!reader) throw new Error('无法读取响应流')

  const decoder = new TextDecoder()
  let buffer = ''
  /** @type {string[]} */
  const pendingDataLines = []

  function flushEvent() {
    if (pendingDataLines.length === 0) return
    const joined = pendingDataLines.join('\n')
    pendingDataLines.length = 0
    const normalized = normalizeSsePayload(joined)
    if (!shouldEmitPayload(normalized)) return
    onChunk(normalized)
  }

  while (true) {
    const { done, value } = await reader.read()
    if (done) break
    buffer += decoder.decode(value, { stream: true })

    const lines = buffer.split('\n')
    buffer = lines.pop() ?? ''

    for (const rawLine of lines) {
      const line = rawLine.replace(/\r$/, '')
      if (line === '') {
        flushEvent()
        continue
      }
      if (line.startsWith(':')) continue
      if (line.startsWith('data:')) {
        let field = line.slice(5)
        if (field.startsWith(' ')) field = field.slice(1)
        pendingDataLines.push(field)
        continue
      }
    }
  }

  if (buffer.replace(/\r$/, '').length) {
    const line = buffer.replace(/\r$/, '')
    if (line.startsWith('data:')) {
      let field = line.slice(5)
      if (field.startsWith(' ')) field = field.slice(1)
      pendingDataLines.push(field)
    }
  }
  flushEvent()
}

function snapAssistantToTarget(a) {
  if (a?.role === 'assistant' && a.targetContent != null) {
    a.content = a.targetContent
  }
}

/** 找到第一个尚未打完字的 AI 气泡（从上到下，保证步骤按顺序打字） */
function findFirstIncompleteAssistantIndex(list) {
  for (let i = 0; i < list.length; i++) {
    const m = list[i]
    if (m.role !== 'assistant' || m.targetContent == null) continue
    const t = m.targetContent
    const c = m.content ?? ''
    if (c.length < t.length) return i
  }
  return -1
}

function nextMessageId() {
  if (typeof crypto !== 'undefined' && crypto.randomUUID) return crypto.randomUUID()
  return `m-${Date.now()}-${Math.random().toString(16).slice(2)}`
}

/**
 * @typedef {object} UseSseChatOptions
 * @property {boolean} [separateAssistantBubblePerSsePayload] 为 true 时：每次 SSE `data` 单独一条 AI 气泡（Manus）；每条气泡内仍为打字机。默认 false：单气泡（恋爱大师）。
 */

/**
 * @param {(message: string) => string} buildUrl 根据用户本轮输入构造 SSE 完整 URL
 * @param {UseSseChatOptions} [options]
 */
export function useSseChat(buildUrl, options = {}) {
  const separateAssistantBubblePerSsePayload = options.separateAssistantBubblePerSsePayload === true

  const messages = ref([])
  const loading = ref(false)
  const error = ref(null)

  let abortController = null
  /** SSE 仍未结束时，允许打字机在「步与步之间」空转等待下一包 */
  let sseOpen = false

  let rafId = null
  let lastFrameTime = 0

  function stopTypingLoop() {
    if (rafId != null) {
      cancelAnimationFrame(rafId)
      rafId = null
    }
    lastFrameTime = 0
  }

  function typingLoop(ts) {
    const list = messages.value
    const idx = findFirstIncompleteAssistantIndex(list)

    if (idx < 0) {
      if (!sseOpen) {
        stopTypingLoop()
        return
      }
      rafId = requestAnimationFrame(typingLoop)
      return
    }

    const a = list[idx]
    if (!lastFrameTime) lastFrameTime = ts
    const elapsed = ts - lastFrameTime
    lastFrameTime = ts

    const target = a.targetContent
    const cur = a.content ?? ''
    const behind = target.length - cur.length

    if (behind <= 0) {
      rafId = requestAnimationFrame(typingLoop)
      return
    }

    const add = Math.max(1, Math.floor((TYPEWRITER_CPS * elapsed) / 1000))
    const n = Math.min(behind, add)
    a.content += target.slice(cur.length, cur.length + n)

    rafId = requestAnimationFrame(typingLoop)
  }

  function ensureTypingLoop() {
    if (rafId == null) {
      lastFrameTime = 0
      rafId = requestAnimationFrame(typingLoop)
    }
  }

  function waitUntilAllTyped() {
    return new Promise((resolve) => {
      const tick = () => {
        if (findFirstIncompleteAssistantIndex(messages.value) < 0) {
          resolve()
          return
        }
        requestAnimationFrame(tick)
      }
      tick()
    })
  }

  function abortStream() {
    if (abortController) {
      abortController.abort()
      abortController = null
    }
    for (const m of messages.value) {
      snapAssistantToTarget(m)
    }
    sseOpen = false
    stopTypingLoop()
  }

  /**
   * @param {string} text
   */
  async function send(text) {
    const trimmed = text?.trim()
    if (!trimmed || loading.value) return

    error.value = null
    loading.value = true
    abortStream()

    messages.value.push({ role: 'user', content: trimmed, id: nextMessageId() })

    if (separateAssistantBubblePerSsePayload) {
      abortController = new AbortController()
      sseOpen = true
      ensureTypingLoop()

      try {
        const url = buildUrl(trimmed)
        /** 第二步及以后的 SSE 前增加一个换行，便于区分智能体步骤 */
        let manusStepIndex = 0
        await readSseStream(url, abortController.signal, (chunk) => {
          const normalized = normalizeSsePayload(typeof chunk === 'string' ? chunk : String(chunk))
          if (!shouldEmitPayload(normalized)) return
          const targetText = manusStepIndex++ === 0 ? normalized : `\n${normalized}`
          messages.value.push({
            role: 'assistant',
            content: '',
            targetContent: targetText,
            id: nextMessageId(),
          })
          ensureTypingLoop()
        })
      } catch (e) {
        if (e.name !== 'AbortError') {
          error.value = e.message || '流式请求失败'
          const last = messages.value[messages.value.length - 1]
          if (last?.role === 'user') {
            const errText = `（错误：${error.value}）`
            messages.value.push({
              role: 'assistant',
              content: errText,
              targetContent: errText,
              id: nextMessageId(),
            })
          }
        }
      } finally {
        sseOpen = false
        abortController = null
        ensureTypingLoop()
        await waitUntilAllTyped()
        loading.value = false
      }
      return
    }

    messages.value.push({
      role: 'assistant',
      content: '',
      targetContent: '',
      id: nextMessageId(),
    })
    const assistantIndex = messages.value.length - 1

    abortController = new AbortController()
    sseOpen = true
    ensureTypingLoop()

    try {
      const url = buildUrl(trimmed)
      await readSseStream(url, abortController.signal, (chunk) => {
        const row = messages.value[assistantIndex]
        if (row) row.targetContent += chunk
        ensureTypingLoop()
      })
    } catch (e) {
      if (e.name !== 'AbortError') {
        error.value = e.message || '流式请求失败'
        const row = messages.value[assistantIndex]
        if (row && !row.targetContent) {
          const errText = `（错误：${error.value}）`
          row.targetContent = errText
          row.content = errText
        }
      }
    } finally {
      sseOpen = false
      abortController = null
      ensureTypingLoop()
      await waitUntilAllTyped()
      loading.value = false
    }
  }

  onUnmounted(abortStream)

  return { messages, loading, error, send, abortStream }
}
