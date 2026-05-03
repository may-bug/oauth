<script setup lang="ts">
import type { Role } from '~/types/role'
import PageHeader from "~/components/common/PageHeader.vue";
import AppButton from "~/components/ui/AppButton.vue";
import AppCard from "~/components/ui/AppCard.vue";
import DataTable from "~/components/ui/DataTable.vue";
import StatusBadge from "~/components/ui/StatusBadge.vue";
import Pagination from "~/components/ui/Pagination.vue";
import ConfirmDialog from "~/components/ui/ConfirmDialog.vue";

definePageMeta({ layout: 'admin', middleware: 'admin' })

const { t } = useI18n()
const api = useApi()
const { success, error: showError } = useToast()
const { hasPermission } = usePermission()
const { page, size, total, setTotal } = usePagination()

const roles = ref<Role[]>([])
const loading = ref(true)

const columns = [
  { key: 'code', label: '编码' },
  { key: 'name', label: '名称' },
  { key: 'description', label: '描述' },
  { key: 'status', label: '状态', width: '100px' },
  { key: 'actions', label: '', width: '150px' },
]

async function fetchRoles() {
  loading.value = true
  try {
    const data = await api.get<any>('/admin/roles', { params: { page: page.value, size: size.value } })
    roles.value = data.records
    setTotal(data.total)
  } catch (e: any) {
    showError(e.message)
  } finally {
    loading.value = false
  }
}

onMounted(fetchRoles)
watch(page, fetchRoles)

const showDeleteConfirm = ref(false)
const selectedRole = ref<Role | null>(null)

async function deleteRole() {
  if (!selectedRole.value) return
  try {
    await api.delete(`/admin/roles/${selectedRole.value.id}`)
    success(t('common.success'))
    fetchRoles()
  } catch (e: any) {
    showError(e.message)
  } finally {
    showDeleteConfirm.value = false
  }
}
</script>

<template>
  <div>
    <PageHeader :title="t('admin.roles_title')">
      <template #actions>
        <NuxtLink v-if="hasPermission('role:create')" to="/admin/roles/new">
          <AppButton icon="mdi:plus">{{ t('admin.roles_create') }}</AppButton>
        </NuxtLink>
      </template>
    </PageHeader>

    <AppCard :padding="false">
      <DataTable :columns="columns" :data="roles" :loading="loading">
        <template #status="{ row }">
          <StatusBadge :status="row.status" />
        </template>
        <template #actions="{ row }">
          <div class="flex items-center gap-2">
            <NuxtLink :to="`/admin/roles/${row.id}`" class="text-accent text-sm hover:underline">
              {{ t('common.edit') }}
            </NuxtLink>
            <button
              v-if="hasPermission('role:delete') && !row.isSystem"
              class="text-danger text-sm hover:underline"
              @click="selectedRole = row; showDeleteConfirm = true"
            >
              {{ t('common.delete') }}
            </button>
            <span v-if="row.isSystem" class="text-xs text-text-tertiary">{{ t('admin.roles_systemRole') }}</span>
          </div>
        </template>
      </DataTable>
      <div class="px-4">
        <Pagination :page="page" :total="total" :size="size" @update:page="page = $event" />
      </div>
    </AppCard>

    <ConfirmDialog
      v-model="showDeleteConfirm"
      :title="t('admin.roles_delete')"
      :message="t('admin.roles_deleteConfirm', { name: selectedRole?.name })"
      danger
      @confirm="deleteRole"
    />
  </div>
</template>
