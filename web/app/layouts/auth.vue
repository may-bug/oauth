<script setup lang="ts">
import AppHeader from "~/components/common/AppHeader.vue";
</script>

<template>
  <div class="min-h-screen bg-auth-gradient flex flex-col relative overflow-hidden">
    <!-- Subtle dot grid -->
    <div
      class="absolute inset-0 pointer-events-none opacity-[0.03] dark:opacity-[0.04]"
      style="background-image: radial-gradient(circle, var(--color-text-primary) 1px, transparent 1px); background-size: 32px 32px;"
    />

    <!-- Animated gradient orbs -->
    <div class="absolute inset-0 overflow-hidden pointer-events-none" aria-hidden="true">
      <div class="absolute rounded-full opacity-[0.06] dark:opacity-[0.04] animate-float"
        style="left: 65%; top: 25%; width: 320px; height: 320px; background: radial-gradient(circle, var(--color-accent) 0%, transparent 70%); animation-duration: 25s; transform: translate(-50%, -50%);" />
      <div class="absolute rounded-full opacity-[0.06] dark:opacity-[0.04] animate-float"
        style="left: 25%; top: 70%; width: 240px; height: 240px; background: radial-gradient(circle, #5856D6 0%, transparent 70%); animation-duration: 22s; animation-delay: 5s; transform: translate(-50%, -50%);" />
    </div>

    <!-- Header -->
    <AppHeader />

    <!-- Centered card -->
    <div class="flex-1 flex items-center justify-center p-4 sm:p-6 relative z-10">
      <div class="w-full max-w-[400px]">
        <slot />
      </div>
    </div>
  </div>
</template>
