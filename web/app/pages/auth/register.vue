<script setup lang="ts">
import type { AuthConfig } from '~/types/auth'
import GlassCard from "~/components/ui/GlassCard.vue";
import AppLogo from "~/components/common/AppLogo.vue";
import LoadingSpinner from "~/components/ui/LoadingSpinner.vue";
import RegisterForm from "~/components/auth/RegisterForm.vue";
import SocialLoginButtons from "~/components/auth/SocialLoginButtons.vue";

definePageMeta({ layout: 'auth', middleware: 'guest' })

const route = useRoute()
const auth = useAuth()
const { t } = useI18n()

const config = ref<AuthConfig | null>(null)

const redirectPath = computed(() => {
  const r = route.query.redirect
  if (typeof r === 'string' && r.startsWith('/') && !r.startsWith('//')) return r
  return undefined
})

onMounted(async () => {
  config.value = await auth.fetchAuthConfig()
  if (config.value?.crypto?.enabled && config.value.crypto.publicKey) {
    const { setPublicKey } = useCrypto()
    await setPublicKey(config.value.crypto.publicKey)
  }
})

async function handleSocialLogin(provider: string) {
  const { auth_url } = await auth.getSocialAuthUrl(provider, redirectPath.value)
  navigateTo(auth_url, { external: true })
}
</script>

<template>
  <div>
    <GlassCard class="p-8">
      <!-- Card header -->
      <div class="text-center mb-7">
        <AppLogo size="sm" class="justify-center mb-3" />
        <h2 class="text-xl font-semibold text-text-primary">{{ t('auth.register') }}</h2>
        <p class="text-xs text-text-tertiary mt-1">{{ t('auth.createAccount') || '创建您的账户以开始使用' }}</p>
      </div>

      <LoadingSpinner v-if="!config" />

      <template v-else>
        <RegisterForm :config="config" />
        <SocialLoginButtons
          v-if="config.socialLogin.some(s => s.enabled)"
          :social-login="config.socialLogin"
          class="mt-5"
          @login="handleSocialLogin"
        />
      </template>
    </GlassCard>

    <!-- Bottom links -->
    <p class="text-center mt-5 text-[11px] text-text-tertiary">
      {{ t('auth.agreement') || '注册即表示同意' }}
      <a href="#" class="text-accent hover:underline">{{ t('auth.terms') || '服务条款' }}</a>
      {{ t('common.and') || '和' }}
      <a href="#" class="text-accent hover:underline">{{ t('auth.privacy') || '隐私政策' }}</a>
    </p>
  </div>
</template>
