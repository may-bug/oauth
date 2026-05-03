<script setup lang="ts">
import PageHeader from "~/components/common/PageHeader.vue";
import LoadingSpinner from "~/components/ui/LoadingSpinner.vue";
import AppCard from "~/components/ui/AppCard.vue";
import AppInput from "~/components/ui/AppInput.vue";
import AppToggle from "~/components/ui/AppToggle.vue";
import AppButton from "~/components/ui/AppButton.vue";

definePageMeta({ layout: 'admin', middleware: 'admin' })

const { t } = useI18n()
const api = useApi()
const { success, error: showError } = useToast()

const config = ref<any>(null)
const loading = ref(true)

onMounted(async () => {
  try {
    config.value = await api.get('/admin/config/email')
  } catch (e: any) {
    showError(e.message)
  } finally {
    loading.value = false
  }
})

async function handleSave() {
  try {
    await api.put('/admin/config/email', config.value)
    success(t('common.success'))
  } catch (e: any) {
    showError(e.message)
  }
}
</script>

<template>
  <div>
    <PageHeader :title="t('admin.emailConfig')" />
    <LoadingSpinner v-if="loading" />
    <AppCard v-else-if="config">
      <form @submit.prevent="handleSave" class="space-y-4 max-w-lg">
        <AppInput v-model="config.host" label="SMTP 服务器" />
        <AppInput v-model="config.port" type="number" label="端口" />
        <AppInput v-model="config.username" label="用户名" />
        <AppInput v-model="config.password" type="password" label="密码" />
        <AppInput v-model="config.from" label="发件人地址" />
        <AppToggle v-model="config.ssl" label="SSL" />
        <AppButton type="submit">{{ t('common.save') }}</AppButton>
      </form>
    </AppCard>
  </div>
</template>
