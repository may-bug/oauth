<script setup lang="ts">
import type { NuxtError } from '#app'

const props = defineProps<{ error: NuxtError }>()

const info = computed(() => {
  const code = props.error?.statusCode || 500
  switch (code) {
    case 404: return { icon: 'mdi:map-search-outline', title: '页面未找到', desc: '请检查 URL 或返回首页' }
    case 403: return { icon: 'mdi:shield-lock-outline', title: '无权访问', desc: '您没有权限访问此页面' }
    case 500: return { icon: 'mdi:server-off', title: '服务器错误', desc: '请稍后重试' }
    default:  return { icon: 'mdi:alert-circle-outline', title: '出错了', desc: props.error?.message || '未知错误' }
  }
})
</script>

<template>
  <div class="min-h-screen bg-bg flex items-center justify-center p-4">
    <div class="text-center max-w-sm">
      <div class="text-[80px] font-bold text-accent/10 leading-none">{{ error?.statusCode || 500 }}</div>
      <div class="w-14 h-14 rounded-2xl bg-surface-secondary flex items-center justify-center mx-auto mt-2 mb-5">
        <Icon :name="info.icon" size="28" class="text-text-tertiary" />
      </div>
      <h1 class="text-lg font-semibold text-text-primary mb-1.5">{{ info.title }}</h1>
      <p class="text-sm text-text-secondary mb-7">{{ info.desc }}</p>
      <NuxtLink to="/" class="inline-flex h-10 px-6 items-center rounded-xl text-sm font-semibold bg-accent text-white hover:bg-accent-hover active:scale-[0.97] shadow-sm transition-all duration-150">
        返回首页
      </NuxtLink>
    </div>
  </div>
</template>
