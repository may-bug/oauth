<script setup lang="ts">

import MobileBlocker from "~/components/common/MobileBlocker.vue";
import AppLogo from "~/components/common/AppLogo.vue";
import LanguageSwitch from "~/components/common/LanguageSwitch.vue";
import ThemeToggle from "~/components/common/ThemeToggle.vue";
import UserMenu from "~/components/common/UserMenu.vue";
import AppFooter from "~/components/common/AppFooter.vue";

const { t } = useI18n()
const route = useRoute()
const sidebarCollapsed = ref(false)

// Device detection by input capability (pointer), not window size
const isDesktop = ref(false)

function checkDevice() {
  const hasFinePointer = window.matchMedia('(pointer: fine)').matches
  const hasCoarseOnly = window.matchMedia('(pointer: coarse)').matches && !hasFinePointer
  const isMobileUA = /Android|iPhone|iPad|iPod|webOS/i.test(navigator.userAgent)
  isDesktop.value = hasFinePointer || (!hasCoarseOnly && !isMobileUA)
}

onMounted(() => {
  checkDevice()
  window.matchMedia('(pointer: fine)').addEventListener('change', checkDevice)
})

const mainNav = computed(() => [
  { path: '/admin', label: t('admin.dashboard'), icon: 'mdi:view-dashboard-outline' },
  { path: '/admin/users', label: t('admin.users'), icon: 'mdi:account-group-outline' },
  { path: '/admin/roles', label: t('admin.roles'), icon: 'mdi:shield-account-outline' },
  { path: '/admin/orgs', label: t('admin.orgs'), icon: 'mdi:office-building-outline' },
  { path: '/admin/clients', label: t('admin.clients'), icon: 'mdi:application-braces-outline' },
  { path: '/admin/audit-logs', label: t('admin.auditLogs'), icon: 'mdi:file-document-outline' },
])

const configNav = computed(() => [
  { path: '/admin/config/social', label: '社交登录', icon: 'mdi:login-variant' },
  { path: '/admin/config/captcha', label: t('admin.captchaConfig'), icon: 'mdi:image-size-select-actual' },
  { path: '/admin/config/crypto', label: t('admin.cryptoConfig'), icon: 'mdi:key-chain' },
  { path: '/admin/config/email', label: t('admin.emailConfig'), icon: 'mdi:email-outline' },
])
</script>

<template>
  <div class="min-h-screen bg-bg flex flex-col">
    <!-- Mobile Blocker -->
    <MobileBlocker v-if="!isDesktop" />

    <!-- Desktop Layout -->
    <div v-if="isDesktop" class="flex flex-1">
      <!-- Sidebar -->
      <aside
        :class="[
          'shrink-0 border-r border-separator bg-surface-secondary/50 h-screen sticky top-0 transition-all duration-300 ease-out flex flex-col',
          sidebarCollapsed ? 'w-[64px]' : 'w-60',
        ]"
      >
        <div class="h-14 flex items-center px-4 border-b border-separator shrink-0">
          <AppLogo v-if="!sidebarCollapsed" size="sm" />
          <div v-else class="mx-auto w-8 h-8 rounded-lg bg-accent/10 flex items-center justify-center">
            <Icon name="mdi:shield-key" size="18" class="text-accent" />
          </div>
        </div>

        <nav class="flex-1 py-3 px-2.5 space-y-0.5 overflow-y-auto">
          <NuxtLink
            v-for="item in mainNav"
            :key="item.path"
            :to="item.path"
            :class="[
              'flex items-center gap-2.5 rounded-lg text-[13px] font-medium transition-colors duration-150',
              sidebarCollapsed ? 'justify-center h-10' : 'px-3 py-2',
              route.path === item.path || (item.path !== '/admin' && route.path.startsWith(item.path))
                ? 'bg-accent-light text-accent'
                : 'text-text-secondary hover:bg-surface-secondary hover:text-text-primary',
            ]"
            :title="sidebarCollapsed ? item.label : undefined"
          >
            <Icon :name="item.icon" size="19" />
            <span v-if="!sidebarCollapsed">{{ item.label }}</span>
          </NuxtLink>

          <div class="pt-4 mt-4 border-t border-separator">
            <p v-if="!sidebarCollapsed" class="px-3 text-[10px] font-semibold uppercase tracking-widest text-text-tertiary mb-1.5">
              {{ t('admin.config') || '系统配置' }}
            </p>
          </div>

          <NuxtLink
            v-for="item in configNav"
            :key="item.path"
            :to="item.path"
            :class="[
              'flex items-center gap-2.5 rounded-lg text-[13px] font-medium transition-colors duration-150',
              sidebarCollapsed ? 'justify-center h-10' : 'px-3 py-2',
              route.path === item.path
                ? 'bg-accent-light text-accent'
                : 'text-text-secondary hover:bg-surface-secondary hover:text-text-primary',
            ]"
            :title="sidebarCollapsed ? item.label : undefined"
          >
            <Icon :name="item.icon" size="19" />
            <span v-if="!sidebarCollapsed">{{ item.label }}</span>
          </NuxtLink>
        </nav>
      </aside>

      <!-- Main Area -->
      <div class="flex-1 min-w-0 flex flex-col">
        <!-- Header — sidebar toggle + global controls -->
        <header class="h-14 border-b border-separator flex items-center justify-between px-5 glass sticky top-0 z-40 shrink-0">
          <div class="flex items-center gap-3">
            <button class="w-8 h-8 flex items-center justify-center rounded-lg hover:bg-surface-secondary transition-colors" @click="sidebarCollapsed = !sidebarCollapsed">
              <Icon name="mdi:menu" size="18" class="text-text-secondary" />
            </button>
          </div>
          <div class="flex items-center gap-1.5">
            <LanguageSwitch />
            <ThemeToggle />
            <UserMenu />
          </div>
        </header>

        <main class="flex-1 p-8">
          <slot />
        </main>
      </div>
    </div>
  </div>
</template>
