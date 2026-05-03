<script setup lang="ts">
const { locale, setLocale } = useI18n()

function toggle() {
  setLocale(locale.value === 'zh-CN' ? 'en' : 'zh-CN')
}
</script>

<template>
  <button
    class="h-8 px-2.5 flex items-center gap-1.5 rounded-lg hover:bg-surface-secondary transition-colors duration-150 text-xs font-medium text-text-secondary"
    @click="toggle"
  >
    <Icon name="mdi:translate" size="16" />
    {{ locale === 'zh-CN' ? '中文' : 'EN' }}
  </button>
</template>
