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
    config.value = await api.get('/admin/config/captcha')
  } catch (e: any) {
    showError(e.message)
  } finally {
    loading.value = false
  }
})

async function handleSave() {
  try {
    await api.put('/admin/config/captcha', config.value)
    success(t('common.success'))
  } catch (e: any) {
    showError(e.message)
  }
}
</script>

<template>
  <div>
    <PageHeader :title="t('admin.captchaConfig')" />
    <LoadingSpinner v-if="loading" />
    <AppCard v-else-if="config">
      <form @submit.prevent="handleSave" class="space-y-4 max-w-lg">
        <AppInput v-model="config.length" type="number" label="验证码长度" />
        <AppInput v-model="config.expireSeconds" type="number" label="过期时间 (秒)" />
        <AppToggle v-model="config.noise" label="噪点" />
        <AppToggle v-model="config.rotation" label="旋转" />
        <AppToggle v-model="config.wave" label="波浪" />
        <AppButton type="submit">{{ t('common.save') }}</AppButton>
      </form>
    </AppCard>
  </div>
</template>
