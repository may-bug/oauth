<script setup lang="ts">

import AppInput from "~/components/ui/AppInput.vue";
import AppButton from "~/components/ui/AppButton.vue";

interface Props {
  type: 'email' | 'phone'
  currentValue?: string
}
const props = defineProps<Props>()
const { t } = useI18n()
const api = useApi()
const { isSubmitting, submit } = useForm()
const { success, error: showError } = useToast()

const newValue = ref('')
const code = ref('')
const step = ref<'input' | 'verify'>('input')
const cooldown = ref(0)
let timer: ReturnType<typeof setInterval>

async function sendCode() {
  try {
    const endpoint = props.type === 'email' ? '/profile/email/change' : '/profile/phone/change'
    await api.post(endpoint, { [props.type]: newValue.value })
    step.value = 'verify'
    cooldown.value = 60
    timer = setInterval(() => {
      cooldown.value--
      if (cooldown.value <= 0) clearInterval(timer)
    }, 1000)
  } catch (e: any) {
    showError(e.message)
  }
}

async function verify() {
  await submit(async () => {
    try {
      const endpoint = props.type === 'email' ? '/profile/email/verify' : '/profile/phone/verify'
      await api.post(endpoint, { code: code.value })
      success(t('common.success'))
      step.value = 'input'
      newValue.value = ''
      code.value = ''
    } catch (e: any) {
      showError(e.message)
    }
  })
}
</script>

<template>
  <div class="space-y-4">
    <div v-if="currentValue" class="text-sm text-text-secondary">
      当前{{ type === 'email' ? '邮箱' : '手机' }}：{{ currentValue }}
    </div>

    <template v-if="step === 'input'">
      <AppInput v-model="newValue" :type="type" :label="type === 'email' ? t('auth.email') : t('auth.phone')" />
      <AppButton @click="sendCode" :disabled="!newValue">{{ t('auth.sendCode') }}</AppButton>
    </template>

    <template v-else>
      <AppInput v-model="code" :label="t('profile.verifyCode')" />
      <div class="flex gap-2">
        <AppButton @click="verify" :loading="isSubmitting">{{ t('common.confirm') }}</AppButton>
        <AppButton variant="secondary" :disabled="cooldown > 0" @click="sendCode">
          {{ cooldown > 0 ? `${cooldown}s` : t('auth.sendCode') }}
        </AppButton>
      </div>
    </template>
  </div>
</template>
