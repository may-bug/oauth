<script setup lang="ts">
import LanguageSwitch from "~/components/common/LanguageSwitch.vue";
import ThemeToggle from "~/components/common/ThemeToggle.vue";
import UserMenu from "~/components/common/UserMenu.vue";

defineEmits<{
  toggleSidebar: []
}>()
</script>

<template>
  <header class="h-12 border-b border-separator flex items-center justify-between px-5 glass sticky top-0 z-40">
    <button class="w-8 h-8 flex items-center justify-center rounded-lg hover:bg-surface-secondary transition-colors" @click="$emit('toggleSidebar')">
      <Icon name="mdi:menu" size="18" class="text-text-secondary" />
    </button>
    <div class="flex items-center gap-1.5">
      <LanguageSwitch />
      <ThemeToggle />
      <UserMenu />
    </div>
  </header>
</template>
