<script setup lang="ts">
import type { User } from '~/types/user'
import PageHeader from "~/components/common/PageHeader.vue";
import SearchField from "~/components/ui/SearchField.vue";
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
const { page, size, total, setTotal, reset } = usePagination()

const users = ref<User[]>([])
const loading = ref(true)
const search = ref('')

const columns = computed(() => [
  { key: 'id', label: 'ID', width: '120px' },
  { key: 'username', label: t('auth.username') },
  { key: 'nickname', label: t('profile.nickname') },
  { key: 'status', label: '状态', width: '100px' },
  { key: 'actions', label: '', width: '120px' },
])

async function fetchUsers() {
  loading.value = true
  try {
    const data = await api.get<any>('/admin/users', { params: { page: page.value, size: size.value } })
    users.value = data.records
    setTotal(data.total)
  } catch (e: any) {
    showError(e.message)
  } finally {
    loading.value = false
  }
}

onMounted(fetchUsers)
watch(page, fetchUsers)

const showDisableConfirm = ref(false)
const showEnableConfirm = ref(false)
const selectedUser = ref<User | null>(null)

async function toggleUserStatus(user: User, newStatus: number) {
  try {
    await api.post(`/admin/users/${user.id}/${newStatus === 0 ? 'disable' : 'enable'}`)
    success(t('common.success'))
    fetchUsers()
  } catch (e: any) {
    showError(e.message)
  }
}
</script>

<template>
  <div>
    <PageHeader :title="t('admin.users_title')">
      <template #actions>
        <SearchField v-model="search" placeholder="搜索用户..." class="w-64" />
      </template>
    </PageHeader>

    <AppCard :padding="false">
      <DataTable :columns="columns" :data="users" :loading="loading">
        <template #status="{ row }">
          <StatusBadge :status="row.status" type="user" />
        </template>
        <template #actions="{ row }">
          <div class="flex items-center gap-2">
            <NuxtLink :to="`/admin/users/${row.id}`" class="text-accent text-sm hover:underline">
              {{ t('common.edit') }}
            </NuxtLink>
            <button
              v-if="row.status === 1 && hasPermission('user:manage')"
              class="text-danger text-sm hover:underline"
              @click="selectedUser = row; showDisableConfirm = true"
            >
              {{ t('common.disabled') }}
            </button>
            <button
              v-if="row.status === 0 && hasPermission('user:manage')"
              class="text-success text-sm hover:underline"
              @click="selectedUser = row; showEnableConfirm = true"
            >
              {{ t('common.enabled') }}
            </button>
          </div>
        </template>
      </DataTable>
      <div class="px-4">
        <Pagination :page="page" :total="total" :size="size" @update:page="page = $event" />
      </div>
    </AppCard>

    <ConfirmDialog
      v-model="showDisableConfirm"
      :title="t('admin.users_disable')"
      :message="`确定要禁用用户 ${selectedUser?.username} 吗？`"
      danger
      @confirm="selectedUser && toggleUserStatus(selectedUser, 0)"
    />
    <ConfirmDialog
      v-model="showEnableConfirm"
      :title="t('admin.users_enable')"
      :message="`确定要启用用户 ${selectedUser?.username} 吗？`"
      @confirm="selectedUser && toggleUserStatus(selectedUser, 1)"
    />
  </div>
</template>
