<script setup lang="ts">
import type { OAuth2Client } from '~/types/client'
import { ClientTypeLabel } from '~/utils/constants'
import LoadingSpinner from "~/components/ui/LoadingSpinner.vue";
import AppCard from "~/components/ui/AppCard.vue";
import AppInput from "~/components/ui/AppInput.vue";
import AppSelect from "~/components/ui/AppSelect.vue";
import AppTextarea from "~/components/ui/AppTextarea.vue";
import AppButton from "~/components/ui/AppButton.vue";
import AppModal from "~/components/ui/AppModal.vue";
import PageHeader from "~/components/common/PageHeader.vue";

definePageMeta({ layout: 'admin', middleware: 'admin' })

const route = useRoute()
const { t } = useI18n()
const api = useApi()
const { success, error: showError } = useToast()
const { isSubmitting, submit } = useForm()

const client = ref<OAuth2Client | null>(null)
const loading = ref(true)
const newSecret = ref('')
const showSecretModal = ref(false)

const form = reactive({
  clientName: '',
  clientType: 2 as 1 | 2 | 3,
  redirectUris: '',
  allowedScopes: '',
  accessTokenTtl: 3600,
  refreshTokenTtl: 86400,
  status: 1 as 0 | 1,
})

onMounted(async () => {
  try {
    client.value = await api.get<OAuth2Client>(`/admin/clients/${route.params.id}`)
    form.clientName = client.value.clientName
    form.clientType = client.value.clientType
    form.redirectUris = client.value.redirectUris
    form.allowedScopes = client.value.allowedScopes
    form.accessTokenTtl = client.value.accessTokenTtl
    form.refreshTokenTtl = client.value.refreshTokenTtl
    form.status = client.value.status
  } catch (e: any) {
    showError(e.message)
  } finally {
    loading.value = false
  }
})

async function handleSave() {
  await submit(async () => {
    try {
      await api.put(`/admin/clients/${route.params.id}`, form)
      success(t('common.success'))
    } catch (e: any) {
      showError(e.message)
    }
  })
}

async function resetSecret() {
  try {
    const data = await api.post<{ clientSecret: string }>(`/admin/clients/${route.params.id}/reset-secret`)
    newSecret.value = data.clientSecret
    showSecretModal.value = true
  } catch (e: any) {
    showError(e.message)
  }
}
</script>

<template>
  <div>
    <PageHeader :title="t('admin.clients_detail')">
      <template #breadcrumb>
        <NuxtLink to="/admin/clients" class="hover:text-text-primary">{{ t('admin.clients') }}</NuxtLink>
        <span>/</span>
        <span class="text-text-primary">{{ client?.clientName }}</span>
      </template>
    </PageHeader>

    <LoadingSpinner v-if="loading" />
    <template v-else-if="client">
      <AppCard>
        <form @submit.prevent="handleSave" class="space-y-4 max-w-lg">
          <AppInput :model-value="client.clientId" label="Client ID" disabled />
          <AppInput v-model="form.clientName" label="客户端名称" />
          <AppSelect
            v-model="form.clientType"
            label="类型"
            :options="Object.entries(ClientTypeLabel).map(([value, label]) => ({ value: Number(value), label }))"
          />
          <AppTextarea v-model="form.redirectUris" label="重定向 URI (JSON 数组)" :rows="3" />
          <AppTextarea v-model="form.allowedScopes" label="允许的作用域 (JSON 数组)" :rows="3" />
          <div class="grid grid-cols-2 gap-4">
            <AppInput v-model="form.accessTokenTtl" type="number" label="Access Token TTL (秒)" />
            <AppInput v-model="form.refreshTokenTtl" type="number" label="Refresh Token TTL (秒)" />
          </div>
          <AppSelect v-model="form.status" label="状态" :options="[{ label: '启用', value: 1 }, { label: '禁用', value: 0 }]" />
          <div class="flex gap-3">
            <AppButton type="submit" :loading="isSubmitting">{{ t('common.save') }}</AppButton>
            <AppButton variant="secondary" @click="resetSecret">{{ t('admin.clients_resetSecret') }}</AppButton>
          </div>
        </form>
      </AppCard>
    </template>

    <AppModal v-model="showSecretModal" title="密钥已重置">
      <p class="text-text-secondary mb-4">{{ t('admin.clients_secretShown') }}</p>
      <div class="bg-surface rounded-lg p-3 font-mono text-sm break-all">{{ newSecret }}</div>
      <template #footer>
        <AppButton @click="showSecretModal = false">{{ t('common.close') }}</AppButton>
      </template>
    </AppModal>
  </div>
</template>
