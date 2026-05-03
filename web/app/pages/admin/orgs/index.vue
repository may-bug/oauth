<script setup lang="ts">
import type { Organization } from '~/types/org'
import PageHeader from "~/components/common/PageHeader.vue";
import LoadingSpinner from "~/components/ui/LoadingSpinner.vue";
import AppCard from "~/components/ui/AppCard.vue";
import EmptyState from "~/components/ui/EmptyState.vue";

definePageMeta({ layout: 'admin', middleware: 'admin' })

const { t } = useI18n()
const api = useApi()
const { error: showError } = useToast()

const orgs = ref<Organization[]>([])
const loading = ref(true)

onMounted(async () => {
  try {
    orgs.value = await api.get<Organization[]>('/admin/orgs')
  } catch (e: any) {
    showError(e.message)
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div>
    <PageHeader :title="t('admin.orgs_title')" />
    <LoadingSpinner v-if="loading" />
    <AppCard v-else>
      <div class="space-y-2">
        <div v-for="org in orgs" :key="org.id" class="flex items-center justify-between p-3 rounded-lg hover:bg-surface">
          <div>
            <p class="font-medium text-text-primary">{{ org.name }}</p>
            <p class="text-sm text-text-secondary">{{ org.code }}</p>
          </div>
          <NuxtLink :to="`/admin/orgs/${org.id}`" class="text-accent text-sm hover:underline">
            {{ t('common.edit') }}
          </NuxtLink>
        </div>
        <EmptyState v-if="orgs.length === 0" />
      </div>
    </AppCard>
  </div>
</template>
