<script setup lang="ts">

import AppInput from "~/components/ui/AppInput.vue";
import AppButton from "~/components/ui/AppButton.vue";

const { t } = useI18n()
const api = useApi()
const { encryptPassword } = useCrypto()
const { isSubmitting, submit } = useForm()
const { success, error: showError } = useToast()

const form = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})

async function handleChange() {
  if (form.newPassword !== form.confirmPassword) {
    showError(t('validation.passwordMatch'))
    return
  }
  await submit(async () => {
    try {
      const encOld = await encryptPassword(form.oldPassword)
      const encNew = await encryptPassword(form.newPassword)
      await api.post('/profile/password', { oldPassword: encOld, newPassword: encNew })
      success(t('profile.passwordChanged'))
      form.oldPassword = ''
      form.newPassword = ''
      form.confirmPassword = ''
    } catch (e: any) {
      showError(e.message)
    }
  })
}
</script>

<template>
  <form @submit.prevent="handleChange" class="space-y-4">
    <AppInput v-model="form.oldPassword" type="password" :label="t('profile.oldPassword')" icon="mdi:lock" />
    <AppInput v-model="form.newPassword" type="password" :label="t('profile.newPassword')" icon="mdi:lock-plus" />
    <AppInput v-model="form.confirmPassword" type="password" :label="t('profile.confirmPassword')" icon="mdi:lock-check" />
    <AppButton type="submit" :loading="isSubmitting">{{ t('profile.changePassword') }}</AppButton>
  </form>
</template>
