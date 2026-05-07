import axios from 'axios'

/** 开发环境通过 Vite 代理 /api；生产环境可在 .env.production 中改为真实网关地址 */
export const baseURL = import.meta.env.VITE_API_BASE ?? '/api'

export const http = axios.create({
  baseURL,
  timeout: 60_000,
})

/** SSE 仍用浏览器 fetch 读取流；此处用 Axios 生成带查询参数的完整请求 URL */
export function loveAppChatSseUrl(message, chatId) {
  return http.getUri({
    url: '/ai/love_app/chat/sse',
    params: { message, chatId },
  })
}

export function manusChatSseUrl(message) {
  return http.getUri({
    url: '/ai/manus/chat',
    params: { message },
  })
}
