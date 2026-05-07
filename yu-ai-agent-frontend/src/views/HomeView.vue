<script setup>
import AiAvatar from '../components/AiAvatar.vue'

const apps = [
  {
    name: 'AI 恋爱大师',
    desc: '恋爱场景对话，独立会话 ID，单气泡流式回复（SSE + 打字机）',
    to: '/love',
    className: 'card-love',
    avatar: 'love',
  },
  {
    name: 'AI 超级智能体',
    desc: 'Manus 多步骤智能体：每一步单独气泡，流式 SSE',
    to: '/manus',
    className: 'card-manus',
    avatar: 'manus',
  },
]
</script>

<template>
  <div class="home">
    <!-- AI 氛围背景 -->
    <div class="home-ai-bg" aria-hidden="true">
      <div class="home-ai-bg__mesh" />
      <div class="home-ai-bg__grid" />
      <div class="home-ai-bg__glow home-ai-bg__glow--a" />
      <div class="home-ai-bg__glow home-ai-bg__glow--b" />
      <div class="home-ai-bg__nodes">
        <span v-for="n in 12" :key="n" class="home-ai-bg__node" :style="{ '--d': `${n * 0.7}s`, '--x': `${(n * 17) % 100}%`, '--y': `${(n * 23) % 85}%` }" />
      </div>
      <div class="home-ai-bg__scan" />
    </div>

    <div class="home-inner">
      <header class="hero">
        <div class="hero-chip">
          <span class="hero-chip__dot" />
          <span class="hero-chip__label">Neural · SSE Stream</span>
        </div>
        <p class="eyebrow">YU AI Agent</p>
        <h1 class="hero-title">
          <span class="hero-title__text">应用中心</span>
        </h1>
        <p class="lede">
          统一界面，自适应电脑、平板与手机。后端默认前缀：
          <code>http://localhost:8123/api</code>
        </p>
      </header>

      <section class="grid" aria-label="应用列表">
        <RouterLink
          v-for="app in apps"
          :key="app.to"
          :to="app.to"
          class="card"
          :class="app.className"
        >
          <div class="card__shine" aria-hidden="true" />

          <div class="card-top">
            <div
              class="card-icon-hub"
              :class="app.avatar === 'love' ? 'card-icon-hub--love' : 'card-icon-hub--manus'"
            >
              <!-- 恋爱：漂浮爱心 -->
              <template v-if="app.avatar === 'love'">
                <span class="love-spark love-spark--1">♥</span>
                <span class="love-spark love-spark--2">♥</span>
                <span class="love-spark love-spark--3">💗</span>
                <span class="love-ring" />
              </template>
              <!-- 智能体：电路环与节点 -->
              <template v-else>
                <svg class="manus-ring" viewBox="0 0 100 100" aria-hidden="true">
                  <circle
                    class="manus-ring__track"
                    cx="50"
                    cy="50"
                    r="44"
                    fill="none"
                    stroke="rgba(56, 189, 248, 0.25)"
                    stroke-width="1"
                  />
                  <circle
                    class="manus-ring__dash"
                    cx="50"
                    cy="50"
                    r="44"
                    fill="none"
                    stroke="rgba(34, 211, 238, 0.55)"
                    stroke-width="1.5"
                    stroke-dasharray="10 18"
                    stroke-linecap="round"
                  />
                </svg>
                <span class="manus-node manus-node--1" />
                <span class="manus-node manus-node--2" />
                <span class="manus-node manus-node--3" />
                <span class="manus-corner manus-corner--tl" />
                <span class="manus-corner manus-corner--br" />
              </template>

              <div class="card-avatar-wrap">
                <AiAvatar :variant="app.avatar" />
              </div>
            </div>

            <div class="card-headings">
              <h2>{{ app.name }}</h2>
              <p>{{ app.desc }}</p>
            </div>
          </div>
          <span class="cta">进入应用 →</span>
        </RouterLink>
      </section>
    </div>
  </div>
</template>

<style scoped>
.home {
  position: relative;
  min-height: 100dvh;
  padding: var(--yu-page-pad, clamp(0.75rem, 4vw, 1.5rem));
  color: var(--yu-text, #e8eaef);
  overflow-x: hidden;
}

/* —— AI 风格全局背景 —— */
.home-ai-bg {
  position: fixed;
  inset: 0;
  z-index: 0;
  pointer-events: none;
  overflow: hidden;
}

.home-ai-bg__mesh {
  position: absolute;
  inset: -20%;
  background:
    radial-gradient(ellipse 55% 45% at 20% 20%, rgba(99, 102, 241, 0.22), transparent 55%),
    radial-gradient(ellipse 50% 40% at 85% 15%, rgba(56, 189, 248, 0.18), transparent 50%),
    radial-gradient(ellipse 45% 50% at 50% 95%, rgba(244, 114, 182, 0.12), transparent 55%),
    radial-gradient(ellipse 40% 35% at 70% 60%, rgba(34, 211, 238, 0.08), transparent 50%);
  animation: mesh-shift 18s ease-in-out infinite alternate;
}

.home-ai-bg__grid {
  position: absolute;
  inset: 0;
  opacity: 0.35;
  background-image:
    linear-gradient(rgba(125, 211, 252, 0.06) 1px, transparent 1px),
    linear-gradient(90deg, rgba(125, 211, 252, 0.06) 1px, transparent 1px);
  background-size: 56px 56px;
  mask-image: radial-gradient(ellipse 85% 70% at 50% 40%, black 10%, transparent 72%);
  animation: grid-drift 22s linear infinite;
}

.home-ai-bg__glow {
  position: absolute;
  width: min(520px, 80vw);
  height: min(520px, 80vw);
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.5;
  animation: glow-pulse 10s ease-in-out infinite;
}

.home-ai-bg__glow--a {
  top: -12%;
  left: -8%;
  background: rgba(56, 189, 248, 0.35);
}

.home-ai-bg__glow--b {
  bottom: -18%;
  right: -10%;
  background: rgba(167, 139, 250, 0.32);
  animation-delay: -4s;
}

.home-ai-bg__nodes {
  position: absolute;
  inset: 0;
}

.home-ai-bg__node {
  position: absolute;
  left: var(--x);
  top: var(--y);
  width: 3px;
  height: 3px;
  border-radius: 50%;
  background: rgba(186, 230, 253, 0.65);
  box-shadow: 0 0 12px rgba(56, 189, 248, 0.45);
  animation: node-twinkle 3.2s ease-in-out infinite;
  animation-delay: var(--d);
}

.home-ai-bg__scan {
  position: absolute;
  inset: 0;
  background: linear-gradient(
    transparent 0%,
    rgba(56, 189, 248, 0.03) 48%,
    rgba(255, 255, 255, 0.02) 50%,
    transparent 52%
  );
  background-size: 100% 220%;
  animation: scan-sweep 7s ease-in-out infinite;
  opacity: 0.85;
}

.home-inner {
  position: relative;
  z-index: 1;
  max-width: min(1040px, 100%);
  margin: 0 auto;
  padding: clamp(1.25rem, 4vw, 2.75rem) 0 clamp(2rem, 6vw, 3.5rem);
}

.hero {
  max-width: 42rem;
  margin: 0 auto clamp(1.75rem, 5vw, 2.75rem);
  text-align: center;
}

.hero-chip {
  display: inline-flex;
  align-items: center;
  gap: 0.45rem;
  padding: 0.35rem 0.75rem;
  margin-bottom: 0.85rem;
  border-radius: 999px;
  font-size: 0.72rem;
  font-weight: 600;
  letter-spacing: 0.06em;
  text-transform: uppercase;
  color: rgba(186, 230, 253, 0.92);
  border: 1px solid rgba(56, 189, 248, 0.28);
  background: linear-gradient(135deg, rgba(56, 189, 248, 0.12), rgba(99, 102, 241, 0.1));
  box-shadow: 0 0 24px rgba(56, 189, 248, 0.12);
}

.hero-chip__dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #22d3ee;
  box-shadow: 0 0 10px #38bdf8;
  animation: chip-pulse 1.8s ease-in-out infinite;
}

.hero-chip__label {
  opacity: 0.95;
}

.eyebrow {
  margin: 0 0 0.45rem;
  font-size: clamp(0.72rem, 2vw, 0.8rem);
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: rgba(165, 243, 252, 0.85);
}

.hero-title {
  margin: 0 0 0.65rem;
  line-height: 1.15;
}

.hero-title__text {
  font-size: clamp(1.65rem, 5vw, 2.2rem);
  font-weight: 800;
  letter-spacing: -0.03em;
  background: linear-gradient(105deg, #e0f2fe 0%, #a5f3fc 35%, #c4b5fd 72%, #f9a8d4 100%);
  background-size: 200% auto;
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
  animation: title-shimmer 8s ease-in-out infinite;
}

.lede {
  margin: 0;
  color: var(--yu-text-muted, rgba(232, 234, 239, 0.72));
  line-height: 1.65;
  font-size: clamp(0.88rem, 2.6vw, 0.98rem);
}

.lede code {
  font-size: 0.88em;
  padding: 0.12rem 0.38rem;
  border-radius: var(--yu-radius-sm, 10px);
  background: rgba(56, 189, 248, 0.08);
  border: 1px solid rgba(56, 189, 248, 0.15);
  word-break: break-all;
}

.grid {
  display: grid;
  gap: clamp(0.85rem, 3vw, 1.35rem);
  grid-template-columns: repeat(auto-fit, minmax(min(100%, 280px), 1fr));
}

.card {
  position: relative;
  display: flex;
  flex-direction: column;
  gap: 1rem;
  padding: clamp(1.1rem, 3.5vw, 1.45rem);
  border-radius: var(--yu-radius-lg, 18px);
  text-decoration: none;
  color: inherit;
  border: 1px solid var(--yu-border, rgba(255, 255, 255, 0.1));
  background: rgba(15, 18, 26, 0.55);
  backdrop-filter: blur(12px);
  transition:
    transform 0.22s ease,
    border-color 0.22s ease,
    box-shadow 0.22s ease;
  min-height: 100%;
  overflow: hidden;
}

.card__shine {
  position: absolute;
  inset: 0;
  background: linear-gradient(
    125deg,
    transparent 40%,
    rgba(255, 255, 255, 0.06) 48%,
    transparent 56%
  );
  transform: translateX(-100%);
  transition: transform 0.65s ease;
  pointer-events: none;
}

.card:hover .card__shine {
  transform: translateX(100%);
}

.card:hover {
  transform: translateY(-5px);
  border-color: rgba(255, 255, 255, 0.22);
  box-shadow: 0 22px 56px rgba(0, 0, 0, 0.42);
}

.card:focus-visible {
  outline-offset: 4px;
}

.card-top {
  display: flex;
  gap: 0.95rem;
  align-items: flex-start;
}

/* 图标区：统一尺寸，承载各应用装饰 */
.card-icon-hub {
  position: relative;
  flex-shrink: 0;
  width: 4.25rem;
  height: 4.25rem;
  display: grid;
  place-items: center;
}

.card-avatar-wrap {
  position: relative;
  z-index: 2;
  transform: scale(1.08);
}

/* —— 恋爱大师：柔光环 + 漂浮爱心 —— */
.card-icon-hub--love .love-ring {
  position: absolute;
  inset: 2px;
  border-radius: 50%;
  border: 1px solid rgba(251, 182, 206, 0.45);
  box-shadow:
    0 0 20px rgba(244, 114, 182, 0.35),
    inset 0 0 18px rgba(244, 114, 182, 0.12);
  animation: love-ring-pulse 2.8s ease-in-out infinite;
  z-index: 0;
}

.love-spark {
  position: absolute;
  z-index: 1;
  font-size: 0.68rem;
  line-height: 1;
  pointer-events: none;
  opacity: 0.85;
  text-shadow: 0 0 10px rgba(244, 114, 182, 0.55);
  animation: love-spark-float 3s ease-in-out infinite;
}

.love-spark--1 {
  top: 2%;
  right: 8%;
  color: #fda4af;
  animation-delay: 0s;
}

.love-spark--2 {
  bottom: 12%;
  left: 4%;
  color: #f0abfc;
  font-size: 0.55rem;
  animation-delay: 0.6s;
}

.love-spark--3 {
  top: 38%;
  left: -2%;
  font-size: 0.75rem;
  animation-delay: 1.1s;
}

.card-love {
  --glow: rgba(244, 114, 182, 0.42);
  border-color: rgba(251, 182, 206, 0.16);
}

.card-love:hover {
  border-color: rgba(251, 182, 206, 0.32);
  box-shadow: 0 22px 56px var(--glow);
}

/* —— 超级智能体：轨道线 + 数据节点 —— */
.card-icon-hub--manus .manus-ring {
  position: absolute;
  inset: -2px;
  width: calc(100% + 4px);
  height: calc(100% + 4px);
  z-index: 0;
  animation: manus-spin 14s linear infinite;
}

.card-icon-hub--manus .manus-ring__dash {
  animation: manus-dash 3s ease-in-out infinite;
}

.manus-node {
  position: absolute;
  width: 5px;
  height: 5px;
  border-radius: 1px;
  background: #22d3ee;
  box-shadow: 0 0 10px rgba(34, 211, 238, 0.7);
  z-index: 1;
  animation: manus-blink 2s ease-in-out infinite;
}

.manus-node--1 {
  top: 6%;
  right: 10%;
  animation-delay: 0s;
}

.manus-node--2 {
  bottom: 14%;
  left: 12%;
  animation-delay: 0.5s;
}

.manus-node--3 {
  top: 42%;
  right: -2%;
  animation-delay: 1s;
}

.manus-corner {
  position: absolute;
  width: 10px;
  height: 10px;
  border-color: rgba(56, 189, 248, 0.45);
  border-style: solid;
  z-index: 1;
  opacity: 0.85;
}

.manus-corner--tl {
  top: 0;
  left: 0;
  border-width: 1px 0 0 1px;
}

.manus-corner--br {
  bottom: 0;
  right: 0;
  border-width: 0 1px 1px 0;
}

.card-manus {
  --glow: rgba(56, 189, 248, 0.42);
  border-color: rgba(56, 189, 248, 0.18);
}

.card-manus:hover {
  border-color: rgba(56, 189, 248, 0.38);
  box-shadow: 0 22px 56px var(--glow);
}

.card-headings {
  min-width: 0;
}

.card h2 {
  margin: 0 0 0.35rem;
  font-size: clamp(1.05rem, 3vw, 1.22rem);
  font-weight: 650;
}

.card p {
  margin: 0;
  flex: 1;
  font-size: clamp(0.86rem, 2.5vw, 0.93rem);
  line-height: 1.55;
  color: var(--yu-text-muted, rgba(232, 234, 239, 0.78));
}

.cta {
  margin-top: auto;
  font-weight: 650;
  font-size: 0.92rem;
  background: linear-gradient(90deg, #7dd3fc, #a5b4fc);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}

.card-love .cta {
  background: linear-gradient(90deg, #fda4af, #e879f9);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}

@keyframes mesh-shift {
  0% {
    transform: translate(0, 0) scale(1);
  }
  100% {
    transform: translate(-2%, 2%) scale(1.03);
  }
}

@keyframes grid-drift {
  0% {
    background-position: 0 0;
  }
  100% {
    background-position: 56px 56px;
  }
}

@keyframes glow-pulse {
  0%,
  100% {
    opacity: 0.42;
    transform: scale(1);
  }
  50% {
    opacity: 0.58;
    transform: scale(1.06);
  }
}

@keyframes node-twinkle {
  0%,
  100% {
    opacity: 0.2;
    transform: scale(0.9);
  }
  50% {
    opacity: 1;
    transform: scale(1.15);
  }
}

@keyframes scan-sweep {
  0%,
  100% {
    background-position: 0 -40%;
  }
  50% {
    background-position: 0 140%;
  }
}

@keyframes chip-pulse {
  0%,
  100% {
    opacity: 1;
    transform: scale(1);
    box-shadow: 0 0 10px #38bdf8;
  }
  50% {
    opacity: 0.65;
    transform: scale(0.92);
    box-shadow: 0 0 16px #67e8f9;
  }
}

@keyframes title-shimmer {
  0%,
  100% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
}

@keyframes love-ring-pulse {
  0%,
  100% {
    transform: scale(1);
    opacity: 0.95;
  }
  50% {
    transform: scale(1.04);
    opacity: 0.75;
  }
}

@keyframes love-spark-float {
  0%,
  100% {
    transform: translateY(0);
    opacity: 0.55;
  }
  50% {
    transform: translateY(-4px);
    opacity: 1;
  }
}

@keyframes manus-spin {
  to {
    transform: rotate(360deg);
  }
}

@keyframes manus-dash {
  0%,
  100% {
    stroke-opacity: 0.75;
  }
  50% {
    stroke-opacity: 1;
  }
}

@keyframes manus-blink {
  0%,
  100% {
    opacity: 0.35;
  }
  50% {
    opacity: 1;
  }
}

@media (max-width: 380px) {
  .card-top {
    flex-direction: column;
    align-items: center;
    text-align: center;
  }

  .card-headings {
    text-align: center;
  }

  .card-icon-hub {
    margin-bottom: 0.15rem;
  }
}

@media (prefers-reduced-motion: reduce) {
  .home-ai-bg__mesh,
  .home-ai-bg__grid,
  .home-ai-bg__glow,
  .home-ai-bg__node,
  .home-ai-bg__scan,
  .hero-chip__dot,
  .hero-title__text,
  .love-ring,
  .love-spark,
  .manus-ring,
  .manus-ring__dash,
  .manus-node,
  .card__shine {
    animation: none !important;
  }

  .manus-ring {
    transform: none;
  }
}
</style>
