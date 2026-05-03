<script setup lang="ts">
import PageHeader from "~/components/common/PageHeader.vue";
import LoadingSpinner from "~/components/ui/LoadingSpinner.vue";
import AppCard from "~/components/ui/AppCard.vue";
import AppToggle from "~/components/ui/AppToggle.vue";
import AppInput from "~/components/ui/AppInput.vue";
import AppButton from "~/components/ui/AppButton.vue";

definePageMeta({ layout: 'admin', middleware: 'admin' })

const { t } = useI18n()
const api = useApi()
const { success, error: showError } = useToast()

const config = ref<any>(null)
const loading = ref(true)

onMounted(async () => {
  try {
    config.value = await api.get('/admin/config/crypto')
  } catch (e: any) {
    showError(e.message)
  } finally {
    loading.value = false
  }
})

async function handleSave() {
  try {
    await api.put('/admin/config/crypto', config.value)
    success(t('common.success'))
  } catch (e: any) {
    showError(e.message)
  }
}
</script>

<template>
  <div>
    <PageHeader :title="t('admin.cryptoConfig')" />
    <LoadingSpinner v-if="loading" />
    <AppCard v-else-if="config">
      <form @submit.prevent="handleSave" class="space-y-4 max-w-lg">
        <AppToggle v-model="config.enabled" label="启用加密" />
        <AppInput v-model="config.expireSeconds" type="number" label="密钥过期时间 (秒)" />
        <AppButton type="submit">{{ t('common.save') }}</AppButton>
      </form>
    </AppCard>
  </div>
</template>
