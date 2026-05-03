<script setup lang="ts">
import type { User } from '~/types/user'
import PageHeader from "~/components/common/PageHeader.vue";
import LoadingSpinner from "~/components/ui/LoadingSpinner.vue";
import AppCard from "~/components/ui/AppCard.vue";
import AppInput from "~/components/ui/AppInput.vue";
import AppSelect from "~/components/ui/AppSelect.vue";
import AppTextarea from "~/components/ui/AppTextarea.vue";
import AppButton from "~/components/ui/AppButton.vue";

definePageMeta({ layout: 'admin', middleware: 'admin' })

const route = useRoute()
const { t } = useI18n()
const api = useApi()
const { success, error: showError } = useToast()
const { hasPermission } = usePermission()
const { isSubmitting, submit } = useForm()

const user = ref<User | null>(null)
const loading = ref(true)

const form = reactive({
  nickname: '',
  realName: '',
  gender: 0 as 0 | 1 | 2,
  bio: '',
})

onMounted(async () => {
  try {
    user.value = await api.get<User>(`/admin/users/${route.params.id}`)
    form.nickname = user.value.nickname || ''
    form.realName = user.value.realName || ''
    form.gender = user.value.gender
    form.bio = user.value.bio || ''
  } catch (e: any) {
    showError(e.message)
  } finally {
    loading.value = false
  }
})

async function handleSave() {
  await submit(async () => {
    try {
      await api.put(`/admin/users/${route.params.id}`, form)
      success(t('common.success'))
    } catch (e: any) {
      showError(e.message)
    }
  })
}
</script>

<template>
  <div>
    <PageHeader :title="t('admin.users_detail')">
      <template #breadcrumb>
        <NuxtLink to="/admin/users" class="hover:text-text-primary">{{ t('admin.users') }}</NuxtLink>
        <span>/</span>
        <span class="text-text-primary">{{ user?.username }}</span>
      </template>
    </PageHeader>

    <LoadingSpinner v-if="loading" />

    <template v-else-if="user">
      <AppCard>
        <form @submit.prevent="handleSave" class="space-y-4 max-w-lg">
          <AppInput :model-value="user.username" label="用户名" disabled />
          <AppInput v-model="form.nickname" :label="t('profile.nickname')" />
          <AppInput v-model="form.realName" :label="t('profile.realName')" />
          <AppSelect
            v-model="form.gender"
            :label="t('profile.gender')"
            :options="[{ label: '未知', value: 0 }, { label: '男', value: 1 }, { label: '女', value: 2 }]"
          />
          <AppTextarea v-model="form.bio" :label="t('profile.bio')" :max-length="500" />
          <AppButton v-if="hasPermission('user:write')" type="submit" :loading="isSubmitting">
            {{ t('common.save') }}
          </AppButton>
        </form>
      </AppCard>
    </template>
  </div>
</template>
