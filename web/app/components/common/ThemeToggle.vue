<script setup lang="ts">
const colorMode = useColorMode()
const themeStore = useThemeStore()

const isDark = computed({
  get: () => colorMode.value === 'dark',
  set: (value: boolean) => {
    colorMode.preference = value ? 'dark' : 'light'
    themeStore.setDark(value)
  },
})

onMounted(() => {
  if (themeStore.isDark) {
    colorMode.preference = 'dark'
  }
})
</script>

<template>
  <button
    class="w-8 h-8 flex items-center justify-center rounded-lg hover:bg-surface-secondary text-text-secondary hover:text-text-primary transition-all duration-150"
    :title="isDark ? '切换到浅色模式' : '切换到深色模式'"
    @click="isDark = !isDark"
  >
    <Icon :name="isDark ? 'mdi:weather-sunny' : 'mdi:weather-night'" size="17" />
  </button>
</template>
