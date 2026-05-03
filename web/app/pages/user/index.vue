<script setup lang="ts">
import type { UserProfile } from '~/types/user'
import PageHeader from "~/components/common/PageHeader.vue";
import LoadingSpinner from "~/components/ui/LoadingSpinner.vue";
import AppCard from "~/components/ui/AppCard.vue";
import AvatarUploader from "~/components/profile/AvatarUploader.vue";

definePageMeta({ layout: 'profile' })

const { t } = useI18n()
const api = useApi()
const { success, error: showError } = useToast()

const profile = ref<UserProfile | null>(null)
const loading = ref(true)

onMounted(async () => {
  try {
    profile.value = await api.get<UserProfile>('/profile')
  } catch (e: any) {
    showError(e.message)
  } finally {
    loading.value = false
  }
})

async function handleAvatarUpload(file: File) {
  try {
    const form = new FormData()
    form.append('file', file)
    const data = await api.postForm<{ avatar: string }>('/profile/avatar', form)
    if (profile.value) profile.value.avatar = data.avatar
    success(t('common.success'))
  } catch (e: any) {
    showError(e.message)
  }
}

async function handleSave(data: Partial<UserProfile>) {
  try {
    await api.put('/profile', data)
    success(t('profile.saveSuccess'))
  } catch (e: any) {
    showError(e.message)
  }
}
</script>

<template>
  <div>
    <PageHeader :title="t('profile.personalInfo')" />

    <LoadingSpinner v-if="loading" />

    <template v-else-if="profile">
      <AppCard class="mb-6">
        <AvatarUploader :src="profile.avatar" :name="profile.nickname || profile.username" @upload="handleAvatarUpload" />
      </AppCard>

      <AppCard>
        <ProfileForm :profile="profile" @save="handleSave" />
      </AppCard>
    </template>
  </div>
</template>
