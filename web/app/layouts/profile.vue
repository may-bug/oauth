<script setup lang="ts">

import LanguageSwitch from "~/components/common/LanguageSwitch.vue";
import ThemeToggle from "~/components/common/ThemeToggle.vue";
import UserMenu from "~/components/common/UserMenu.vue";

const { t } = useI18n()
const route = useRoute()

const navItems = computed(() => [
  { path: '/user', label: t('profile.personalInfo'), icon: 'mdi:account-outline' },
  { path: '/user/security', label: t('profile.security'), icon: 'mdi:lock-outline' },
  { path: '/user/accounts', label: t('profile.accounts'), icon: 'mdi:link-variant' },
])
</script>

<template>
  <div class="min-h-screen bg-bg flex flex-col">
    <!-- Minimal header — no homepage nav links -->
    <header class="h-12 border-b border-separator flex items-center justify-between px-5 glass sticky top-0 z-40 shrink-0">
      <NuxtLink to="/" class="flex items-center gap-2 text-text-secondary hover:text-text-primary transition-colors">
        <Icon name="mdi:shield-key" size="20" class="text-accent" />
        <span class="text-[15px] font-semibold text-text-primary">Auth Center</span>
      </NuxtLink>
      <div class="flex items-center gap-1.5">
        <LanguageSwitch />
        <ThemeToggle />
        <UserMenu />
      </div>
    </header>

    <div class="flex flex-1 max-w-5xl mx-auto w-full">
      <!-- Sidebar (desktop) -->
      <aside class="hidden md:block w-56 shrink-0 border-r border-separator py-6 px-3 sticky top-12 h-[calc(100vh-3rem)]">
        <nav class="space-y-0.5">
          <NuxtLink
            v-for="item in navItems"
            :key="item.path"
            :to="item.path"
            :class="[
              'flex items-center gap-2.5 px-3 py-2 rounded-lg text-sm font-medium transition-colors duration-150',
              route.path === item.path
                ? 'bg-accent-light text-accent'
                : 'text-text-secondary hover:bg-surface-secondary hover:text-text-primary',
            ]"
          >
            <Icon :name="item.icon" size="18" />
            {{ item.label }}
          </NuxtLink>
        </nav>
        <div class="mt-6 pt-6 border-t border-separator space-y-0.5">
          <NuxtLink to="/" class="flex items-center gap-2.5 px-3 py-2 rounded-lg text-sm text-text-secondary hover:bg-surface-secondary hover:text-text-primary transition-colors duration-150">
            <Icon name="mdi:arrow-left" size="18" />
            {{ t('common.back') }}
          </NuxtLink>
          <button class="w-full flex items-center gap-2.5 px-3 py-2 rounded-lg text-sm text-danger hover:bg-danger/6 transition-colors duration-150" @click="useAuth().logout()">
            <Icon name="mdi:logout" size="18" />
            {{ t('auth.logout') }}
          </button>
        </div>
      </aside>

      <!-- Content -->
      <main class="flex-1 py-8 px-6 md:px-10 max-w-2xl">
        <slot />
      </main>
    </div>

    <!-- Bottom Tab Bar (mobile) -->
    <nav class="md:hidden fixed bottom-0 left-0 right-0 h-14 bg-surface/90 backdrop-blur-xl border-t border-separator flex items-center justify-around z-40">
      <NuxtLink
        v-for="item in navItems"
        :key="item.path"
        :to="item.path"
        :class="[
          'flex flex-col items-center gap-0.5 text-[10px] font-medium transition-colors',
          route.path === item.path ? 'text-accent' : 'text-text-tertiary',
        ]"
      >
        <Icon :name="item.icon" size="20" />
        <span>{{ item.label }}</span>
      </NuxtLink>
    </nav>
    <div class="md:hidden h-14" />
  </div>
</template>
