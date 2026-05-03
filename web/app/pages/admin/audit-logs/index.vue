<script setup lang="ts">

import AppModal from "~/components/ui/AppModal.vue";
import Pagination from "~/components/ui/Pagination.vue";
import StatusBadge from "~/components/ui/StatusBadge.vue";
import PageHeader from "~/components/common/PageHeader.vue";
import AppCard from "~/components/ui/AppCard.vue";
import DataTable from "~/components/ui/DataTable.vue";

definePageMeta({ layout: 'admin', middleware: 'admin' })

const { t } = useI18n()
const api = useApi()
const { error: showError } = useToast()
const { page, size, total, setTotal } = usePagination()

const logs = ref<any[]>([])
const loading = ref(true)
const selectedLog = ref<any>(null)
const showModal = computed({
  get: () => !!selectedLog.value,
  set: (val) => { if (!val) selectedLog.value = null },
})

const columns = [
  { key: 'username', label: t('admin.audit_user') },
  { key: 'action', label: t('admin.audit_action') },
  { key: 'resourceType', label: t('admin.audit_resource') },
  { key: 'status', label: t('admin.audit_status'), width: '80px' },
  { key: 'createdAt', label: t('admin.audit_time'), width: '180px' },
  { key: 'actions', label: '', width: '80px' },
]

async function fetchLogs() {
  loading.value = true
  try {
    const data = await api.get<any>('/admin/audit-logs', { params: { page: page.value, size: size.value } })
    logs.value = data.records
    setTotal(data.total)
  } catch (e: any) {
    showError(e.message)
  } finally {
    loading.value = false
  }
}

onMounted(fetchLogs)
watch(page, fetchLogs)
</script>

<template>
  <div>
    <PageHeader :title="t('admin.audit_title')" />

    <AppCard :padding="false">
      <DataTable :columns="columns" :data="logs" :loading="loading">
        <template #status="{ row }">
          <StatusBadge :status="row.status" />
        </template>
        <template #actions="{ row }">
          <button class="text-accent text-sm hover:underline" @click="selectedLog = row">
            {{ t('admin.audit_detail') }}
          </button>
        </template>
      </DataTable>
      <div class="px-4">
        <Pagination :page="page" :total="total" :size="size" @update:page="page = $event" />
      </div>
    </AppCard>

    <AppModal v-model="showModal" title="审计日志详情">
      <div v-if="selectedLog" class="space-y-3">
        <div><strong>用户：</strong>{{ selectedLog.username }}</div>
        <div><strong>操作：</strong>{{ selectedLog.action }}</div>
        <div><strong>资源：</strong>{{ selectedLog.resourceType }} / {{ selectedLog.resourceId }}</div>
        <div><strong>IP：</strong>{{ selectedLog.ip }}</div>
        <div><strong>详情：</strong></div>
        <pre class="bg-surface rounded-lg p-3 text-xs overflow-auto max-h-60">{{ selectedLog.detail }}</pre>
      </div>
    </AppModal>
  </div>
</template>
