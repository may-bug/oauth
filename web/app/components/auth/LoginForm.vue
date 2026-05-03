<script setup lang="ts">
import type { AuthConfig } from '~/types/auth'
import SegmentedControl from "~/components/ui/SegmentedControl.vue";
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

const loginMode = ref('password')

const form = reactive({
  username: '',
  password: '',
  email: '',
  emailCode: '',
  phone: '',
  phoneCode: '',
})

const loginOptions = computed(() => {
  const options = []
  if (props.config.passwordLogin) options.push({ label: t('auth.passwordLogin'), value: 'password' })
  if (props.config.emailLogin) options.push({ label: t('auth.emailLogin'), value: 'email' })
  if (props.config.phoneLogin) options.push({ label: t('auth.phoneLogin'), value: 'phone' })
  return options
})

const redirectPath = computed(() => {
  const r = route.query.redirect
  if (typeof r === 'string' && r.startsWith('/') && !r.startsWith('//')) return r
  return '/'
})

onMounted(() => {
  if (props.config.captcha?.enabled) fetchCaptcha()
  if (loginOptions.value.length > 0) loginMode.value = loginOptions.value[0].value
})

watch(loginMode, () => {
  Object.keys(errors.value).forEach(k => delete errors.value[k])
})

const emailCooldown = ref(0)
let emailTimer: ReturnType<typeof setInterval>
async function sendEmailCode() {
  if (!form.email || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)) {
    showError(t('validation.invalidEmail') || '请输入有效的邮箱地址')
    return
  }
  try {
    await auth.sendEmailCode(form.email)
    emailCooldown.value = 60
    emailTimer = setInterval(() => {
      emailCooldown.value--
      if (emailCooldown.value <= 0) clearInterval(emailTimer)
    }, 1000)
  } catch (e: any) {
    showError(e.message)
  }
}

const phoneCooldown = ref(0)
let phoneTimer: ReturnType<typeof setInterval>
async function sendPhoneCode() {
  if (!form.phone || !/^1[3-9]\d{9}$/.test(form.phone)) {
    showError(t('validation.invalidPhone') || '请输入有效的手机号码')
    return
  }
  try {
    await auth.sendPhoneCode(form.phone)
    phoneCooldown.value = 60
    phoneTimer = setInterval(() => {
      phoneCooldown.value--
      if (phoneCooldown.value <= 0) clearInterval(phoneTimer)
    }, 1000)
  } catch (e: any) {
    showError(e.message)
  }
}

function validateForm(): boolean {
  errors.value = {}

  if (loginMode.value === 'password') {
    if (!form.username || form.username.trim().length === 0) {
      errors.value.username = t('validation.required')
    } else if (form.username.trim().length < 3) {
      errors.value.username = t('validation.minLength', { min: 3 })
    }
    if (!form.password || form.password.length === 0) {
      errors.value.password = t('validation.required')
    }
    if (props.config.captcha?.enabled && !captchaCode.value) {
      errors.value.captcha = t('validation.required')
    }
  } else if (loginMode.value === 'email') {
    if (!form.email || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)) {
      errors.value.email = t('validation.invalidEmail') || '请输入有效的邮箱地址'
    }
    if (!form.emailCode || form.emailCode.trim().length === 0) {
      errors.value.emailCode = t('validation.required')
    }
  } else if (loginMode.value === 'phone') {
    if (!form.phone || !/^1[3-9]\d{9}$/.test(form.phone)) {
      errors.value.phone = t('validation.invalidPhone') || '请输入有效的手机号码'
    }
    if (!form.phoneCode || form.phoneCode.trim().length === 0) {
      errors.value.phoneCode = t('validation.required')
    }
  }

  return Object.keys(errors.value).length === 0
}

async function handleLogin() {
  if (!validateForm()) return

  await submit(async () => {
    try {
      if (loginMode.value === 'password') {
        const encryptedPw = await encryptPassword(form.password, props.config.crypto?.enabled)
        await auth.loginWithPassword({
          username: form.username,
          password: encryptedPw,
        })
      } else if (loginMode.value === 'email') {
        await auth.loginWithEmail({ email: form.email, code: form.emailCode })
      } else {
        await auth.loginWithPhone({ phone: form.phone, code: form.phoneCode })
      }
      success(t('auth.loginSuccess'))
      navigateTo(redirectPath.value)
    } catch (e: any) {
      showError(e.message || t('common.error'))
      if (props.config.captcha?.enabled) fetchCaptcha()
    }
  })
}
</script>

<template>
  <form @submit.prevent="handleLogin" class="space-y-4">
    <SegmentedControl v-if="loginOptions.length > 1" v-model="loginMode" :options="loginOptions" />

    <!-- Password Login -->
    <template v-if="loginMode === 'password'">
      <AppInput v-model="form.username" :label="t('auth.username')" icon="mdi:account-outline" :placeholder="t('auth.username')" :error="errors.username" />
      <AppInput v-model="form.password" type="password" :label="t('auth.password')" icon="mdi:lock-outline" :placeholder="t('auth.password')" :error="errors.password" />
      <CaptchaField v-if="config.captcha?.enabled" v-model="captchaCode" :svg="captchaSvg" :error="errors.captcha" @refresh="fetchCaptcha" />
    </template>

    <!-- Email Login -->
    <template v-if="loginMode === 'email'">
      <AppInput v-model="form.email" type="email" :label="t('auth.email')" icon="mdi:email-outline" :placeholder="t('auth.email')" :error="errors.email" />
      <div class="flex gap-2.5">
        <AppInput v-model="form.emailCode" :label="t('auth.captcha')" :placeholder="t('auth.captcha')" :error="errors.emailCode" class="flex-1" />
        <div class="flex items-end">
          <AppButton variant="secondary" size="sm" :disabled="emailCooldown > 0" @click="sendEmailCode">
            {{ emailCooldown > 0 ? `${emailCooldown}s` : t('auth.sendCode') }}
          </AppButton>
        </div>
      </div>
    </template>

    <!-- Phone Login -->
    <template v-if="loginMode === 'phone'">
      <AppInput v-model="form.phone" type="tel" :label="t('auth.phone')" icon="mdi:phone-outline" :placeholder="t('auth.phone')" :error="errors.phone" />
      <div class="flex gap-2.5">
        <AppInput v-model="form.phoneCode" :label="t('auth.captcha')" :placeholder="t('auth.captcha')" :error="errors.phoneCode" class="flex-1" />
        <div class="flex items-end">
          <AppButton variant="secondary" size="sm" :disabled="phoneCooldown > 0" @click="sendPhoneCode">
            {{ phoneCooldown > 0 ? `${phoneCooldown}s` : t('auth.sendCode') }}
          </AppButton>
        </div>
      </div>
    </template>

    <AppButton type="submit" block size="lg" :loading="isSubmitting" class="mt-2">
      {{ t('auth.login') }}
    </AppButton>

    <p class="text-center text-[13px] text-text-secondary">
      {{ t('auth.noAccount') }}
      <NuxtLink :to="redirectPath !== '/' ? `/auth/register?redirect=${encodeURIComponent(redirectPath)}` : '/auth/register'" class="text-accent font-medium hover:underline underline-offset-2">{{ t('auth.register') }}</NuxtLink>
    </p>
  </form>
</template>
