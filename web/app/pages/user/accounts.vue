<script setup lang="ts">
import { SocialProviderLabel, SocialProviderIconPath } from '~/utils/constants'
import PageHeader from "~/components/common/PageHeader.vue";
import AppCard from "~/components/ui/AppCard.vue";
import AppButton from "~/components/ui/AppButton.vue";
import ConfirmDialog from "~/components/ui/ConfirmDialog.vue";

definePageMeta({ layout: 'profile' })

const { t } = useI18n()
const api = useApi()
const auth = useAuth()
const { success, error: showError } = useToast()

const profile = ref<any>(null)
const showUnbindConfirm = ref(false)
const unbindProvider = ref('')

onMounted(async () => {
  profile.value = await api.get('/profile')
})

const boundProviders = computed(() => {
  return profile.value?.socialAccounts || []
})

function isBound(provider: string) {
  return boundProviders.value.some((a: any) => a.provider === provider)
}

function getBoundAccount(provider: string) {
  return boundProviders.value.find((a: any) => a.provider === provider)
}

async function bindProvider(provider: string) {
  try {
    const { auth_url } = await api.get<{ auth_url: string; state: string }>(`/auth/social/${provider}/bind`)
    navigateTo(auth_url, { external: true })
  } catch (e: any) {
    showError(e.message)
  }
}

async function unbind() {
  try {
    await api.post(`/auth/social/${unbindProvider.value}/unbind`)
    success(t('common.success'))
    profile.value = await api.get('/profile')
  } catch (e: any) {
    showError(e.message)
  } finally {
    showUnbindConfirm.value = false
  }
}
</script>

<template>
  <div>
    <PageHeader :title="t('profile.accounts')" />

    <div class="space-y-3">
      <AppCard v-for="provider in SocialProviders" :key="provider" padding>
        <div class="flex items-center justify-between">
          <div class="flex items-center gap-4">
            <div class="w-10 h-10 rounded-xl bg-surface-secondary flex items-center justify-center">
              <img
                :src="SocialProviderIconPath[provider]"
                :alt="SocialProviderLabel[provider]"
                class="w-5 h-5"
              />
            </div>
            <div>
              <p class="text-[15px] font-medium text-text-primary">{{ SocialProviderLabel[provider] }}</p>
              <p v-if="isBound(provider)" class="text-[13px] text-text-secondary">
                {{ getBoundAccount(provider)?.username || getBoundAccount(provider)?.email || '已绑定' }}
              </p>
              <p v-else class="text-[13px] text-text-tertiary">未绑定</p>
            </div>
          </div>
          <AppButton
            v-if="isBound(provider)"
            variant="danger"
            size="sm"
            @click="unbindProvider = provider; showUnbindConfirm = true"
          >
            {{ t('profile.unbindSocial') }}
          </AppButton>
          <AppButton
            v-else
            variant="secondary"
            size="sm"
            @click="bindProvider(provider)"
          >
            {{ t('profile.bindSocial') }}
          </AppButton>
        </div>
      </AppCard>
    </div>

    <ConfirmDialog
      v-model="showUnbindConfirm"
      :title="t('profile.unbindSocial')"
      :message="`确定要解绑 ${SocialProviderLabel[unbindProvider]} 吗？`"
      danger
      @confirm="unbind"
    />
  </div>
</template>
