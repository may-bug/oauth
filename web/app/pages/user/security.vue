<script setup lang="ts">
import type { UserProfile } from '~/types/user'
import PageHeader from "~/components/common/PageHeader.vue";
import AppCard from "~/components/ui/AppCard.vue";
import PasswordChange from "~/components/profile/PasswordChange.vue";
import CredentialBind from "~/components/profile/CredentialBind.vue";

definePageMeta({ layout: 'profile' })

const { t } = useI18n()
const api = useApi()

const profile = ref<UserProfile | null>(null)

onMounted(async () => {
  profile.value = await api.get<UserProfile>('/profile')
})
</script>

<template>
  <div>
    <PageHeader :title="t('profile.security')" />

    <div class="space-y-6">
      <AppCard>
        <h3 class="text-lg font-semibold text-text-primary mb-4">{{ t('profile.changePassword') }}</h3>
        <PasswordChange />
      </AppCard>

      <AppCard v-if="profile">
        <h3 class="text-lg font-semibold text-text-primary mb-4">{{ t('profile.changeEmail') }}</h3>
        <CredentialBind type="email" :current-value="profile.email" />
      </AppCard>

      <AppCard v-if="profile">
        <h3 class="text-lg font-semibold text-text-primary mb-4">{{ t('profile.changePhone') }}</h3>
        <CredentialBind type="phone" :current-value="profile.phone" />
      </AppCard>
    </div>
  </div>
</template>
