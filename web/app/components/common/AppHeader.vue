<script setup lang="ts">
import AppIcon from "~/components/AppIcon.vue";
import LanguageSwitch from "~/components/common/LanguageSwitch.vue";
import ThemeToggle from "~/components/common/ThemeToggle.vue";
import UserMenu from "~/components/common/UserMenu.vue";

const { t } = useI18n()
const authStore = useAuthStore()

const isScrolled = ref(false)

onMounted(() => {
  if (typeof window !== 'undefined') {
    window.addEventListener('scroll', () => {
      isScrolled.value = window.scrollY > 10
    }, { passive: true })
  }
})

onUnmounted(() => {
  if (typeof window !== 'undefined') {
    window.removeEventListener('scroll', () => {})
  }
})
</script>

<template>
  <header
    :class="[
      'fixed top-0 left-0 right-0 z-50 transition-all duration-300 ease-out',
      isScrolled ? 'glass shadow-sm' : 'bg-transparent',
    ]"
  >
    <div class="max-w-6xl mx-auto px-4 sm:px-6 h-14 flex items-center justify-between">
      <!-- Left: Logo + Nav -->
      <div class="flex items-center gap-8">
        <NuxtLink to="/" class="flex items-center gap-2">
          <div class="text-accent">
            <AppIcon src="/icons/logo.svg" name="mdi:shield-key" size="24" />
          </div>
          <span class="text-[17px] font-semibold text-text-primary tracking-tight hidden sm:block">Auth Center</span>
        </NuxtLink>
        <nav class="hidden md:flex items-center gap-1">
          <NuxtLink
            to="/"
            class="px-3 py-1.5 rounded-lg text-[13px] font-medium text-text-secondary hover:text-text-primary hover:bg-surface-secondary/50 transition-all duration-150"
          >
            {{ t('common.home') || '首页' }}
          </NuxtLink>
          <NuxtLink
            to="/docs"
            class="px-3 py-1.5 rounded-lg text-[13px] font-medium text-text-secondary hover:text-text-primary hover:bg-surface-secondary/50 transition-all duration-150"
          >
            {{ t('common.docs') || '文档' }}
          </NuxtLink>
        </nav>
      </div>

      <!-- Right: Actions -->
      <div class="flex items-center gap-1.5">
        <LanguageSwitch />
        <ThemeToggle />
          <template v-if="!authStore.isAuthenticated">
            <NuxtLink
              to="/auth/login"
              class="h-8 px-3 flex items-center rounded-lg text-[13px] font-medium text-text-secondary hover:text-text-primary hover:bg-surface-secondary/50 transition-all duration-150"
            >
              {{ t('auth.login') }}
            </NuxtLink>
            <NuxtLink
              to="/auth/register"
              class="h-8 px-4 flex items-center rounded-xl text-[13px] font-semibold bg-accent text-white hover:bg-accent-hover active:scale-[0.97] shadow-sm transition-all duration-150"
            >
              {{ t('auth.register') }}
            </NuxtLink>
          </template>
          <template v-else>
            <UserMenu />
          </template>
      </div>
    </div>
  </header>
</template>
