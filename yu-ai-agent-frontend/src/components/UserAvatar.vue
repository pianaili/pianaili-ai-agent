<template>
  <div class="user-avatar" role="img" aria-label="我">
    <span class="txt">我</span>
  </div>
</template>

<style scoped>
.user-avatar {
  flex-shrink: 0;
  width: clamp(2.25rem, 5.5vw, 2.75rem);
  height: clamp(2.25rem, 5.5vw, 2.75rem);
  border-radius: 50%;
  display: grid;
  place-items: center;
  background: linear-gradient(145deg, rgba(255, 255, 255, 0.14), rgba(255, 255, 255, 0.06));
  border: 1px solid rgba(255, 255, 255, 0.18);
  color: var(--room-fg, #e8eaef);
  box-shadow: 0 4px 14px rgba(0, 0, 0, 0.28);
}

.txt {
  font-size: clamp(0.72rem, 2.4vw, 0.82rem);
  font-weight: 700;
  letter-spacing: 0.02em;
}
</style>
