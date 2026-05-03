<script setup lang="ts">
import type { Role, PermissionNode } from '~/types/role'
import PageHeader from "~/components/common/PageHeader.vue";
import LoadingSpinner from "~/components/ui/LoadingSpinner.vue";
import AppCard from "~/components/ui/AppCard.vue";
import AppInput from "~/components/ui/AppInput.vue";
import AppTextarea from "~/components/ui/AppTextarea.vue";
import AppSelect from "~/components/ui/AppSelect.vue";
import PermissionTree from "~/components/admin/PermissionTree.vue";
import AppButton from "~/components/ui/AppButton.vue";

definePageMeta({ layout: 'admin', middleware: 'admin' })

const route = useRoute()
const { t } = useI18n()
const api = useApi()
const { success, error: showError } = useToast()
const { isSubmitting, submit } = useForm()

const isNew = computed(() => route.params.id === 'new')
const role = ref<Role | null>(null)
const permissions = ref<PermissionNode[]>([])
const selectedPermissionIds = ref<number[]>([])
const loading = ref(true)

const form = reactive({
  code: '',
  name: '',
  description: '',
  orgId: null as number | null,
  status: 1 as 0 | 1,
})

onMounted(async () => {
  try {
    if (!isNew.value) {
      role.value = await api.get<Role>(`/admin/roles/${route.params.id}`)
      form.code = role.value.code
      form.name = role.value.name
      form.description = role.value.description || ''
      form.orgId = role.value.orgId || null
      form.status = role.value.status
    }
    // Load all permissions (assuming endpoint returns tree)
    permissions.value = await api.get<PermissionNode[]>('/admin/roles/permissions')
  } catch (e: any) {
    showError(e.message)
  } finally {
    loading.value = false
  }
})

async function handleSave() {
  await submit(async () => {
    try {
      if (isNew.value) {
        await api.post('/admin/roles', { ...form, permissionIds: selectedPermissionIds.value })
      } else {
        await api.put(`/admin/roles/${route.params.id}`, { ...form, permissionIds: selectedPermissionIds.value })
      }
      success(t('common.success'))
      navigateTo('/admin/roles')
    } catch (e: any) {
      showError(e.message)
    }
  })
}
</script>

<template>
  <div>
    <PageHeader :title="isNew ? t('admin.roles_create') : t('admin.roles_detail')">
      <template #breadcrumb>
        <NuxtLink to="/admin/roles" class="hover:text-text-primary">{{ t('admin.roles') }}</NuxtLink>
        <span>/</span>
        <span class="text-text-primary">{{ isNew ? t('admin.roles_create') : role?.name }}</span>
      </template>
    </PageHeader>

    <LoadingSpinner v-if="loading" />

    <template v-else>
      <AppCard class="mb-6">
        <form @submit.prevent="handleSave" class="space-y-4 max-w-lg">
          <AppInput v-model="form.code" label="角色编码" :disabled="!isNew && role?.isSystem" />
          <AppInput v-model="form.name" label="角色名称" />
          <AppTextarea v-model="form.description" label="描述" :rows="2" />
          <AppSelect
            v-model="form.status"
            label="状态"
            :options="[{ label: '启用', value: 1 }, { label: '禁用', value: 0 }]"
          />
        </form>
      </AppCard>

      <AppCard>
        <h3 class="text-lg font-semibold text-text-primary mb-4">权限配置</h3>
        <PermissionTree v-model="selectedPermissionIds" :nodes="permissions" />
      </AppCard>

      <div class="mt-6 flex gap-3">
        <AppButton @click="handleSave" :loading="isSubmitting">{{ t('common.save') }}</AppButton>
        <NuxtLink to="/admin/roles">
          <AppButton variant="secondary">{{ t('common.cancel') }}</AppButton>
        </NuxtLink>
      </div>
    </template>
  </div>
</template>
