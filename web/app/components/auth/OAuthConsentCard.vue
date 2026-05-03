<script setup lang="ts">
import AppCard from "~/components/ui/AppCard.vue";
import AppLogo from "~/components/common/AppLogo.vue";
import UserAvatar from "~/components/common/UserAvatar.vue";
import AppButton from "~/components/ui/AppButton.vue";

interface Props {
  clientName: string
  scopes: string[]
  username?: string
  avatar?: string
}

defineProps<Props>()
const emit = defineEmits<{
  authorize: []
  deny: []
}>()

const { t } = useI18n()

const scopeLabels: Record<string, string> = {
  openid: t('oauth.scope.openid'),
  profile: t('oauth.scope.profile'),
  email: t('oauth.scope.email'),
  phone: t('oauth.scope.phone'),
  address: t('oauth.scope.address'),
  offline_access: t('oauth.scope.offline_access'),
}

const scopeIcons: Record<string, string> = {
  openid: 'mdi:identifier',
  profile: 'mdi:account-circle-outline',
  email: 'mdi:email-outline',
  phone: 'mdi:phone-outline',
  address: 'mdi:map-marker-outline',
  offline_access: 'mdi:cloud-off-outline',
}
</script>

<template>
  <AppCard class="p-8">
    <div class="text-center mb-8">
      <AppLogo size="md" class="justify-center mb-4" />
      <div class="accent-bar mx-auto mb-5" />
      <h2 class="text-xl font-semibold text-text-primary leading-snug">
        {{ t('oauth.requestAccess', { app: clientName }) }}
      </h2>
    </div>

    <div class="mb-6">
      <h3 class="app-label mb-3">{{ t('oauth.scopes') }}</h3>
      <ul class="space-y-2">
        <li v-for="scope in scopes" :key="scope" class="flex items-center gap-3 text-sm text-text-primary py-2.5 px-3.5 rounded-xl bg-surface-secondary/50">
          <Icon :name="scopeIcons[scope] || 'mdi:check-circle-outline'" size="18" class="text-accent shrink-0" />
          <span>{{ scopeLabels[scope] || scope }}</span>
        </li>
      </ul>
    </div>

    <div v-if="username" class="mb-8 p-3.5 bg-surface-secondary/50 rounded-xl flex items-center gap-3 border border-border/60">
      <UserAvatar :src="avatar" :name="username" size="sm" />
      <span class="text-sm font-medium text-text-primary">{{ username }}</span>
    </div>

    <div class="flex gap-3">
      <AppButton variant="secondary" block @click="emit('deny')">
        {{ t('oauth.deny') }}
      </AppButton>
      <AppButton variant="primary" block @click="emit('authorize')">
        {{ t('oauth.authorize') }}
      </AppButton>
    </div>
  </AppCard>
</template>
