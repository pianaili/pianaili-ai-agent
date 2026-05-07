import { createRouter, createWebHistory } from 'vue-router'

const HomeView = () => import('../views/HomeView.vue')
const LoveChatView = () => import('../views/LoveChatView.vue')
const ManusChatView = () => import('../views/ManusChatView.vue')

export const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/', name: 'home', component: HomeView, meta: { title: '应用中心' } },
    { path: '/love', name: 'love', component: LoveChatView, meta: { title: 'AI 恋爱大师' } },
    { path: '/manus', name: 'manus', component: ManusChatView, meta: { title: 'AI 超级智能体' } },
  ],
})

router.afterEach((to) => {
  const title = to.meta?.title
  document.title = title ? `${title} · YU AI` : 'YU AI Agent'
})
