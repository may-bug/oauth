<script setup lang="ts">
import type { Organization } from '~/types/org'
import PageHeader from "~/components/common/PageHeader.vue";
import LoadingSpinner from "~/components/ui/LoadingSpinner.vue";
import AppCard from "~/components/ui/AppCard.vue";
import AppInput from "~/components/ui/AppInput.vue";
import AppSelect from "~/components/ui/AppSelect.vue";
import AppButton from "~/components/ui/AppButton.vue";

definePageMeta({ layout: 'admin', middleware: 'admin' })

const route = useRoute()
const { t } = useI18n()
const api = useApi()
const { success, error: showError } = useToast()

const org = ref<Organization | null>(null)
const loading = ref(true)

const form = reactive({
  name: '',
  code: '',
  status: 1 as 0 | 1,
})

onMounted(async () => {
  try {
    org.value = await api.get<Organization>(`/admin/orgs/${route.params.id}`)
    form.name = org.value.name
    form.code = org.value.code
    form.status = org.value.status
  } catch (e: any) {
    showError(e.message)
  } finally {
    loading.value = false
  }
})

async function handleSave() {
  try {
    await api.put(`/admin/orgs/${route.params.id}`, form)
    success(t('common.success'))
  } catch (e: any) {
    showError(e.message)
  }
}
</script>

<template>
  <div>
    <PageHeader :title="t('admin.orgs_detail')">
      <template #breadcrumb>
        <NuxtLink to="/admin/orgs" class="hover:text-text-primary">{{ t('admin.orgs') }}</NuxtLink>
        <span>/</span>
        <span class="text-text-primary">{{ org?.name }}</span>
      </template>
    </PageHeader>

    <LoadingSpinner v-if="loading" />
    <AppCard v-else>
      <form @submit.prevent="handleSave" class="space-y-4 max-w-lg">
        <AppInput v-model="form.name" label="组织名称" />
        <AppInput v-model="form.code" label="组织编码" disabled />
        <AppSelect v-model="form.status" label="状态" :options="[{ label: '启用', value: 1 }, { label: '禁用', value: 0 }]" />
        <AppButton type="submit">{{ t('common.save') }}</AppButton>
      </form>
    </AppCard>
  </div>
</template>
