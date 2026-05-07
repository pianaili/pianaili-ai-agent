<script setup>
import { computed } from 'vue'

const props = defineProps({
  /** love | manus | default */
  variant: {
    type: String,
    default: 'default',
    validator: (v) => ['love', 'manus', 'default'].includes(v),
  },
})

const label = computed(() => {
  if (props.variant === 'love') return '恋爱导师'
  if (props.variant === 'manus') return '超级智能体'
  return 'AI 助手'
})
</script>

<template>
  <div class="ai-avatar" :data-variant="variant" role="img" :aria-label="label">
    <svg v-if="variant === 'love'" class="ico" viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <path
        fill="currentColor"
        d="M12 20.5c-.42 0-.82-.11-1.17-.33C7.65 18.28 3.25 14.7 3.25 9.75c0-2.55 2.07-4.62 4.62-4.62 1.55 0 2.98.76 3.85 2.02a4.62 4.62 0 0 1 6.05 7.02c-1.43 1.51-3.18 2.78-4.5 3.68-.35.22-.75.33-1.17.33Z"
        opacity=".92"
      />
    </svg>
    <svg v-else-if="variant === 'manus'" class="ico" viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <path
        fill="currentColor"
        d="M8 4h8v2H8V4Zm-1 4h10a2 2 0 0 1 2 2v7a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2v-7c0-1.1.9-2 2-2Zm2 3v2h4V11h-4Zm0 4v2h4v-2h-4Z"
        opacity=".92"
      />
      <path fill="currentColor" d="M10 18h4v2h-4v-2Z" opacity=".55" />
    </svg>
    <svg v-else class="ico" viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <path
        fill="currentColor"
        d="M12 2a2 2 0 0 1 2 2c0 .74-.4 1.39-1 1.73V7h1a7 7 0 0 1 7 7h1v2h-1v1a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-1H2v-2h1a7 7 0 0 1 7-7h1V5.73c-.6-.34-1-.99-1-1.73a2 2 0 0 1 2-2Z"
        opacity=".88"
      />
    </svg>
  </div>
</template>

<style scoped>
.ai-avatar {
  flex-shrink: 0;
  width: clamp(2.25rem, 5.5vw, 2.75rem);
  height: clamp(2.25rem, 5.5vw, 2.75rem);
  border-radius: 50%;
  display: grid;
  place-items: center;
  color: #fff;
  box-shadow: 0 4px 14px rgba(0, 0, 0, 0.35);
  border: 1px solid rgba(255, 255, 255, 0.14);
}

.ai-avatar[data-variant='love'] {
  background: linear-gradient(145deg, #f472b6, #a855f7);
}

.ai-avatar[data-variant='manus'] {
  background: linear-gradient(145deg, #22d3ee, #2563eb);
}

.ai-avatar[data-variant='default'] {
  background: linear-gradient(145deg, #94a3b8, #475569);
}

.ico {
  width: 58%;
  height: 58%;
}
</style>
