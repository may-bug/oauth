<script setup lang="ts">
interface Props {
  collapsed: boolean
}
defineProps<Props>()
const route = useRoute()
const { t } = useI18n()

const navItems = computed(() => [
  { path: '/admin', label: t('admin.dashboard'), icon: 'mdi:view-dashboard-outline' },
  { path: '/admin/users', label: t('admin.users'), icon: 'mdi:account-group-outline' },
  { path: '/admin/roles', label: t('admin.roles'), icon: 'mdi:shield-account-outline' },
  { path: '/admin/orgs', label: t('admin.orgs'), icon: 'mdi:office-building-outline' },
  { path: '/admin/clients', label: t('admin.clients'), icon: 'mdi:application-braces-outline' },
  { path: '/admin/audit-logs', label: t('admin.auditLogs'), icon: 'mdi:file-document-outline' },
])

const configItems = computed(() => [
  { path: '/admin/config/captcha', label: t('admin.captchaConfig'), icon: 'mdi:image-size-select-actual' },
  { path: '/admin/config/crypto', label: t('admin.cryptoConfig'), icon: 'mdi:key-chain' },
  { path: '/admin/config/email', label: t('admin.emailConfig'), icon: 'mdi:email-outline' },
])

function isActive(path: string) {
  return route.path === path || (path !== '/admin' && route.path.startsWith(path))
}
</script>

<template>
  <nav class="py-3 px-2.5 space-y-0.5">
    <NuxtLink
      v-for="item in navItems"
      :key="item.path"
      :to="item.path"
      :class="[
        'flex items-center gap-2.5 rounded-lg text-[13px] font-medium transition-colors duration-150',
        collapsed ? 'justify-center h-10' : 'px-3 py-2',
        isActive(item.path)
          ? 'bg-accent-light text-accent'
          : 'text-text-secondary hover:bg-surface-secondary hover:text-text-primary',
      ]"
      :title="collapsed ? item.label : undefined"
    >
      <Icon :name="item.icon" size="19" />
      <span v-if="!collapsed">{{ item.label }}</span>
    </NuxtLink>

    <div v-if="!collapsed" class="pt-4 mt-4 border-t border-separator">
      <p class="px-3 text-[10px] font-semibold uppercase tracking-widest text-text-tertiary mb-1.5">系统配置</p>
    </div>

    <NuxtLink
      v-for="item in configItems"
      :key="item.path"
      :to="item.path"
      :class="[
        'flex items-center gap-2.5 rounded-lg text-[13px] font-medium transition-colors duration-150',
        collapsed ? 'justify-center h-10' : 'px-3 py-2',
        isActive(item.path)
          ? 'bg-accent-light text-accent'
          : 'text-text-secondary hover:bg-surface-secondary hover:text-text-primary',
      ]"
      :title="collapsed ? item.label : undefined"
    >
      <Icon :name="item.icon" size="19" />
      <span v-if="!collapsed">{{ item.label }}</span>
    </NuxtLink>
  </nav>
</template>
