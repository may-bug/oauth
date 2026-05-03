<script setup lang="ts">
import PageHeader from "~/components/common/PageHeader.vue";
import LoadingSpinner from "~/components/ui/LoadingSpinner.vue";
import AppButton from "~/components/ui/AppButton.vue";
import AppToggle from "~/components/ui/AppToggle.vue";
import { SocialProviderIconPath, SocialProviderIcon } from '~/utils/constants'

definePageMeta({ layout: 'admin', middleware: 'admin' })

const { t } = useI18n()
const api = useApi()
const { success, error: showError } = useToast()

interface SocialConfig {
  provider: string
  name: string
  enabled: boolean
  clientId: string
  clientSecret: string
  redirectUri: string
  scopes: string
}

const providers = ref<SocialConfig[]>([])
const loading = ref(true)
const iconErrors = ref<Record<string, boolean>>({})

onMounted(async () => {
  try {
    providers.value = await api.get<SocialConfig[]>('/admin/config/social-providers')
  } catch (e: any) {
    showError(e.message)
  } finally {
    loading.value = false
  }
})

async function toggleProvider(provider: string, enabled: boolean) {
  try {
    await api.put(`/admin/config/social-providers/${provider}/toggle`, { enabled })
  } catch (e: any) {
    // Revert on failure
    const p = providers.value.find(s => s.provider === provider)
    if (p) p.enabled = !enabled
    showError(e.message)
  }
}
</script>

<template>
  <div>
    <PageHeader :title="'社交登录配置'">
      <template #breadcrumb>
        <NuxtLink to="/admin" class="hover:text-text-primary">{{ t('admin.dashboard') }}</NuxtLink>
        <span>/</span>
        <span class="text-text-primary">社交登录配置</span>
      </template>
    </PageHeader>

    <LoadingSpinner v-if="loading" />

    <template v-else>
      <div class="flex flex-wrap gap-4">
        <div
          v-for="item in providers"
          :key="item.provider"
          class="w-[200px] bg-surface border border-border/40 rounded-2xl p-5 flex flex-col items-center text-center hover:shadow-md hover:border-border transition-all duration-200"
        >
          <AppToggle :model-value="item.enabled" @update:model-value="toggleProvider(item.provider, $event)" class="mb-3" />
          <NuxtLink :to="`/admin/config/social/${item.provider}`" class="flex flex-col items-center">
            <div class="w-12 h-12 rounded-xl bg-surface-secondary flex items-center justify-center overflow-hidden mb-3">
              <img
                v-if="SocialProviderIconPath[item.provider] && !iconErrors[item.provider]"
                :src="SocialProviderIconPath[item.provider]"
                :alt="item.name"
                class="w-6 h-6"
                @error="iconErrors[item.provider] = true"
              />
              <Icon v-else :name="SocialProviderIcon[item.provider] || 'mdi:shield-account'" size="24" class="text-text-tertiary" />
            </div>
            <h3 class="text-sm font-semibold text-text-primary">{{ item.name }}</h3>
            <span class="text-[11px] text-text-tertiary mt-0.5">{{ item.provider }}</span>
          </NuxtLink>
        </div>
      </div>
    </template>
  </div>
</template>
