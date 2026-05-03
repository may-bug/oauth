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

    <!-- Header -->
    <AppHeader />

    <!-- Centered card -->
    <div class="flex-1 flex items-center justify-center p-4 sm:p-6 relative z-10">
      <div class="w-full max-w-[460px]">
        <slot />
      </div>
    </div>
  </div>
</template>
