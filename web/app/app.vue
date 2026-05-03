<script setup lang="ts">
import FullLoading from "~/components/common/FullLoading.vue";
import AppToast from "~/components/ui/AppToast.vue";
const nuxtApp = useNuxtApp()

const colorMode = useColorMode()
const themeStore = useThemeStore()
// 是否首次加载
const isFullLoading = ref(true)

nuxtApp.hook('page:start', () => {
  isFullLoading.value = true
})
nuxtApp.hook('page:finish', () => {
  isFullLoading.value = false
})

onMounted(async () => {
  if (themeStore.isDark) {
    colorMode.preference = 'dark'
  }
  const authStore = useAuthStore()
  try {
    const { $api } = useNuxtApp()
    const data = await ($api as any).get('/auth/status')
    if (data) {
      authStore.setStatus(data)
    }
  } catch(err) {
    authStore.clearAuth()
    console.error(err)
  }
})
</script>

<template>
  <div>
    <FullLoading v-if="isFullLoading"/>
    <NuxtRouteAnnouncer />
    <NuxtLayout>
      <NuxtPage />
    </NuxtLayout>
    <AppToast />
  </div>
</template>

<style>
@import '~/assets/css/main.css';
@import '~/assets/css/apple.css';
</style>
