<script setup lang="ts">

import PageHeader from "~/components/common/PageHeader.vue";
import AppCard from "~/components/ui/AppCard.vue";

definePageMeta({ layout: 'admin', middleware: 'admin' })

const { t } = useI18n()
const auth = useAuth()
</script>

<template>
  <div>
    <PageHeader :title="t('admin.dashboard')" />

    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
      <AppCard>
        <div class="flex items-center gap-3">
          <div class="w-10 h-10 rounded-xl bg-accent/10 flex items-center justify-center">
            <Icon name="mdi:account-group" size="24" class="text-accent" />
          </div>
          <div>
            <p class="text-sm text-text-secondary">{{ t('admin.users') }}</p>
            <NuxtLink to="/admin/users" class="text-accent text-sm hover:underline">管理 →</NuxtLink>
          </div>
        </div>
      </AppCard>

      <AppCard>
        <div class="flex items-center gap-3">
          <div class="w-10 h-10 rounded-xl bg-success/10 flex items-center justify-center">
            <Icon name="mdi:shield-account" size="24" class="text-success" />
          </div>
          <div>
            <p class="text-sm text-text-secondary">{{ t('admin.roles') }}</p>
            <NuxtLink to="/admin/roles" class="text-accent text-sm hover:underline">管理 →</NuxtLink>
          </div>
        </div>
      </AppCard>

      <AppCard>
        <div class="flex items-center gap-3">
          <div class="w-10 h-10 rounded-xl bg-warning/10 flex items-center justify-center">
            <Icon name="mdi:application-braces" size="24" class="text-warning" />
          </div>
          <div>
            <p class="text-sm text-text-secondary">{{ t('admin.clients') }}</p>
            <NuxtLink to="/admin/clients" class="text-accent text-sm hover:underline">管理 →</NuxtLink>
          </div>
        </div>
      </AppCard>
    </div>
  </div>
</template>
