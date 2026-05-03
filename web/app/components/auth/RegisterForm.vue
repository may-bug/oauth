<script setup lang="ts">
import type { AuthConfig } from '~/types/auth'
import AppInput from "~/components/ui/AppInput.vue";
import CaptchaField from "~/components/auth/CaptchaField.vue";
import AppButton from "~/components/ui/AppButton.vue";

interface Props {
  config: AuthConfig
}

const props = defineProps<Props>()
const { t } = useI18n()
const route = useRoute()
const auth = useAuth()
const { fetchCaptcha, captchaSvg, captchaCode } = useCaptcha()
const { encryptPassword } = useCrypto()
const { isSubmitting, submit, errors } = useForm()
const { success, error: showError } = useToast()

const showOptional = ref(false)

const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  nickname: '',
  email: '',
})

const redirectPath = computed(() => {
  const r = route.query.redirect
  if (typeof r === 'string' && r.startsWith('/') && !r.startsWith('//')) return r
  return '/'
})

onMounted(() => {
  if (props.config.captcha?.enabled) fetchCaptcha()
})

function validateForm(): boolean {
  errors.value = {}

  if (!form.username || form.username.trim().length < 3) {
    errors.value.username = t('validation.minLength', { min: 3 })
  } else if (!/^[a-zA-Z0-9_]+$/.test(form.username.trim())) {
    errors.value.username = t('validation.username')
  }

  if (!form.password || form.password.length < 6) {
    errors.value.password = t('validation.minLength', { min: 6 })
  }

  if (form.password !== form.confirmPassword) {
    errors.value.confirmPassword = t('validation.passwordMatch')
  }

  if (form.email && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)) {
    errors.value.email = t('validation.invalidEmail')
  }

  if (props.config.captcha?.enabled && !captchaCode.value) {
    errors.value.captcha = t('validation.required')
  }

  return Object.keys(errors.value).length === 0
}

async function handleRegister() {
  if (!validateForm()) return

  await submit(async () => {
    try {
      const encryptedPw = await encryptPassword(form.password, props.config.crypto?.enabled)
      await auth.register({
        username: form.username,
        password: encryptedPw,
        nickname: form.nickname || undefined,
        email: form.email || undefined,
      })
      success(t('auth.registerSuccess'))
      navigateTo(redirectPath.value)
    } catch (e: any) {
      showError(e.message || t('common.error'))
      if (props.config.captcha?.enabled) fetchCaptcha()
    }
  })
}
</script>

<template>
  <form @submit.prevent="handleRegister" class="space-y-4">
    <!-- Required: Username -->
    <AppInput
      v-model="form.username"
      :label="t('auth.username')"
      icon="mdi:account-outline"
      :placeholder="t('auth.username')"
      :error="errors.username"
    />

    <!-- Required: Password group -->
    <div class="space-y-3">
      <AppInput
        v-model="form.password"
        type="password"
        :label="t('auth.password')"
        icon="mdi:lock-outline"
        :placeholder="$t('auth.passwordHint') || '至少6位字符'"
        :error="errors.password"
      />
      <AppInput
        v-model="form.confirmPassword"
        type="password"
        :label="t('profile.confirmPassword')"
        icon="mdi:lock-check-outline"
        :placeholder="$t('profile.confirmPassword')"
        :error="errors.confirmPassword"
      />
    </div>

    <!-- Captcha (if enabled) -->
    <CaptchaField
      v-if="config.captcha?.enabled"
      v-model="captchaCode"
      :svg="captchaSvg"
      :error="errors.captcha"
      @refresh="fetchCaptcha"
    />

    <!-- Optional fields toggle -->
    <button
      v-if="!showOptional"
      type="button"
      class="w-full flex items-center justify-center gap-1.5 py-2 text-xs text-text-tertiary hover:text-text-secondary transition-colors"
      @click="showOptional = true"
    >
      <Icon name="mdi:plus-circle-outline" size="14" />
      {{ $t('auth.moreInfo') || '补充更多信息（选填）' }}
    </button>

    <!-- Optional fields -->
    <template v-if="showOptional">
      <div class="pt-1 space-y-4 border-t border-separator">
        <AppInput
          v-model="form.nickname"
          :label="t('auth.nickname')"
          icon="mdi:card-account-details-outline"
          :placeholder="$t('auth.nicknameOptional') || '选填'"
        />
        <AppInput
          v-model="form.email"
          type="email"
          :label="t('auth.email')"
          icon="mdi:email-outline"
          :placeholder="$t('auth.emailOptional') || '选填'"
          :error="errors.email"
        />
      </div>
    </template>

    <AppButton type="submit" block size="lg" :loading="isSubmitting">
      {{ t('auth.register') }}
    </AppButton>

    <p class="text-center text-[13px] text-text-secondary">
      {{ t('auth.hasAccount') }}
      <NuxtLink
        :to="redirectPath !== '/' ? `/auth/login?redirect=${encodeURIComponent(redirectPath)}` : '/auth/login'"
        class="text-accent font-medium hover:underline underline-offset-2"
      >{{ t('auth.login') }}</NuxtLink>
    </p>
  </form>
</template>
