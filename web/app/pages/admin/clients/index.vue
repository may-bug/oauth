<script setup lang="ts">
import type { OAuth2Client } from '~/types/client'
import { ClientTypeLabel } from '~/utils/constants'
import PageHeader from "~/components/common/PageHeader.vue";
import AppButton from "~/components/ui/AppButton.vue";
import AppCard from "~/components/ui/AppCard.vue";
import DataTable from "~/components/ui/DataTable.vue";
import StatusBadge from "~/components/ui/StatusBadge.vue";
import Pagination from "~/components/ui/Pagination.vue";
import AppModal from "~/components/ui/AppModal.vue";

definePageMeta({ layout: 'admin', middleware: 'admin' })

const { t } = useI18n()
const api = useApi()
const { success, error: showError } = useToast()
const { hasPermission } = usePermission()
const { page, size, total, setTotal } = usePagination()

const clients = ref<OAuth2Client[]>([])
const loading = ref(true)

const columns = [
  { key: 'clientId', label: 'Client ID', width: '200px' },
  { key: 'clientName', label: '名称' },
  { key: 'clientType', label: '类型', width: '100px' },
  { key: 'status', label: '状态', width: '100px' },
  { key: 'actions', label: '', width: '200px' },
]

async function fetchClients() {
  loading.value = true
  try {
    const data = await api.get<any>('/admin/clients', { params: { page: page.value, size: size.value } })
    clients.value = data.records
    setTotal(data.total)
  } catch (e: any) {
    showError(e.message)
  } finally {
    loading.value = false
  }
}

onMounted(fetchClients)
watch(page, fetchClients)

const showCreateSuccess = ref(false)
const newClientSecret = ref('')

async function createClient() {
  try {
    const data = await api.post<{ clientId: string; clientSecret: string }>('/admin/clients', {
      clientName: 'New Client',
      clientType: 2,
      redirectUris: '[]',
      allowedScopes: '["openid"]',
      accessTokenTtl: 3600,
      refreshTokenTtl: 86400,
    })
    newClientSecret.value = data.clientSecret
    showCreateSuccess.value = true
    fetchClients()
  } catch (e: any) {
    showError(e.message)
  }
}
</script>

<template>
  <div>
    <PageHeader :title="t('admin.clients_title')">
      <template #actions>
        <AppButton v-if="hasPermission('client:write')" icon="mdi:plus" @click="createClient">
          {{ t('admin.clients_create') }}
        </AppButton>
      </template>
    </PageHeader>

    <AppCard :padding="false">
      <DataTable :columns="columns" :data="clients" :loading="loading">
        <template #clientType="{ row }">
          {{ ClientTypeLabel[row.clientType] }}
        </template>
        <template #status="{ row }">
          <StatusBadge :status="row.status" />
        </template>
        <template #actions="{ row }">
          <div class="flex items-center gap-2">
            <NuxtLink :to="`/admin/clients/${row.id}`" class="text-accent text-sm hover:underline">
              {{ t('common.edit') }}
            </NuxtLink>
          </div>
        </template>
      </DataTable>
      <div class="px-4">
        <Pagination :page="page" :total="total" :size="size" @update:page="page = $event" />
      </div>
    </AppCard>

    <AppModal v-model="showCreateSuccess" title="客户端已创建">
      <p class="text-text-secondary mb-4">{{ t('admin.clients_secretShown') }}</p>
      <div class="bg-surface rounded-lg p-3 font-mono text-sm break-all">{{ newClientSecret }}</div>
      <template #footer>
        <AppButton @click="showCreateSuccess = false">{{ t('common.close') }}</AppButton>
      </template>
    </AppModal>
  </div>
</template>
