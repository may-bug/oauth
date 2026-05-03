<script setup lang="ts">
import AppIcon from "~/components/AppIcon.vue";
</script>

<template>
  <div class="
    fixed inset-0 z-[9999] flex items-center justify-center
    bg-auth-gradient
  ">
    <div class="flex flex-col items-center gap-6 animate-scale-in-center">
      <!-- Logo -->
      <div class="p-4 rounded-2xl bg-accent-light">
        <AppIcon src="/icons/logo.svg" name="mdi:shield-key" :size="56" />
      </div>

      <!-- Brand -->
      <span class="text-[22px] font-semibold tracking-[-0.01em] text-text-primary">
        Auth Center
      </span>

      <!-- Loading bar -->
      <div class="w-40 h-[3px] bg-surface-secondary rounded-full overflow-hidden">
        <div class="h-full w-2/5 bg-accent rounded-full loading-fill" />
      </div>
    </div>
  </div>
</template>

<style scoped>
.loading-fill {
  animation: loading-slide 1.4s var(--ease-in-out, cubic-bezier(0.4, 0, 0.2, 1)) infinite;
}

@keyframes loading-slide {
  0% {
    transform: translateX(-100%);
  }
  100% {
    transform: translateX(350%);
  }
}
</style>
