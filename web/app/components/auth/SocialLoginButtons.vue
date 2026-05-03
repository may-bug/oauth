<script setup lang="ts">
import type { SocialProvider } from '~/types/auth'
import { SocialProviderIconPath, SocialProviderIcon } from '~/utils/constants'

interface Props {
  socialLogin: SocialProvider[]
}

defineProps<Props>()
const emit = defineEmits<{
  login: [provider: string]
}>()

const { t } = useI18n()
const iconErrors = ref<Record<string, boolean>>({})
</script>

<template>
  <div v-if="socialLogin.length > 0" class="space-y-4">
    <div class="flex items-center gap-3">
      <div class="flex-1 h-px bg-separator" />
      <span class="text-xs text-text-tertiary">{{ t('auth.or') }}</span>
      <div class="flex-1 h-px bg-separator" />
    </div>
    <div class="flex justify-center gap-3">
      <button
        v-for="item in socialLogin.filter(s => s.enabled)"
        :key="item.provider"
        :title="item.name"
        :class="[
          'w-11 h-11 flex items-center justify-center rounded-xl border text-sm font-medium transition-all duration-150',
          'hover:bg-surface-secondary hover:border-text-tertiary/30 active:scale-[0.95]',
          'disabled:opacity-30 disabled:cursor-not-allowed disabled:active:scale-100',
          'border-border bg-surface',
        ]"
        @click="emit('login', item.provider)"
      >
        <img
          v-if="SocialProviderIconPath[item.provider] && !iconErrors[item.provider]"
          :src="SocialProviderIconPath[item.provider]"
          :alt="item.name"
          class="w-5 h-5"
          @error="iconErrors[item.provider] = true"
        />
        <Icon v-else-if="SocialProviderIcon[item.provider]" :name="SocialProviderIcon[item.provider]" size="20" />
        <span v-else class="text-xs font-semibold">{{ item.name.charAt(0) }}</span>
      </button>
    </div>
  </div>
</template>
