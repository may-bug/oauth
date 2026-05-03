<script setup lang="ts">
import PageHeader from "~/components/common/PageHeader.vue";
import LoadingSpinner from "~/components/ui/LoadingSpinner.vue";
import AppCard from "~/components/ui/AppCard.vue";
import AppInput from "~/components/ui/AppInput.vue";
import AppToggle from "~/components/ui/AppToggle.vue";
import AppButton from "~/components/ui/AppButton.vue";
import AppSelect from "~/components/ui/AppSelect.vue";
import ConfirmDialog from "~/components/ui/ConfirmDialog.vue";

definePageMeta({ layout: 'admin', middleware: 'admin' })

const route = useRoute()
const { t } = useI18n()
const api = useApi()
const { success, error: showError } = useToast()

const provider = computed(() => route.params.provider as string)
const isNew = computed(() => provider.value === 'new')
const loading = ref(!isNew.value)
const saving = ref(false)
const showDeleteConfirm = ref(false)

const form = reactive({
  provider: '',
  displayName: '',
  enabled: false,
  clientId: '',
  clientSecret: '',
  authorizeUrl: '',
  tokenUrl: '',
  tokenMethod: 'POST',
  tokenResponseFormEncoded: true,
  tokenFieldName: 'access_token',
  scope: '',
  defaultRedirectUri: '',
  userinfoUrl: '',
  userinfoMethod: 'GET',
  userinfoAuthStyle: 'header',
  userIdField: 'id',
  nicknameField: 'name',
  avatarField: 'avatar_url',
  emailField: 'email',
  openidEnabled: false,
  openidUrl: '',
  openidFieldName: 'id_token',
  openidResponseField: 'id_token',
  extraAuthorizeParams: '',
  sortOrder: 0,
})

const baseUrl = computed(() => import.meta.client ? window.location.origin : '')

onMounted(async () => {
  if (isNew.value) {
    loading.value = false
    return
  }
  try {
    const data = await api.get<any>(`/admin/config/social-providers/${provider.value}`)
    Object.keys(form).forEach(k => {
      if (data[k] !== undefined) (form as any)[k] = data[k]
    })
    if (form.clientSecret === '******') form.clientSecret = ''
  } catch (e: any) {
    showError(e.message)
  } finally {
    loading.value = false
  }
})

async function toggleEnabled(enabled: boolean) {
  form.enabled = enabled
  try {
    await api.put(`/admin/config/social-providers/${provider.value}/toggle`, { enabled })
  } catch (e: any) {
    form.enabled = !enabled
    showError(e.message)
  }
}

async function handleSave() {
  saving.value = true
  try {
    const payload = { ...form }
    if (!payload.clientSecret) delete (payload as any).clientSecret
    if (isNew.value) {
      await api.post('/admin/config/social-providers', payload)
    } else {
      await api.put(`/admin/config/social-providers/${provider.value}`, payload)
    }
    success('配置已保存')
    if (isNew.value) navigateTo('/admin/config/social')
  } catch (e: any) {
    showError(e.message)
  } finally {
    saving.value = false
  }
}

async function handleDelete() {
  try {
    await api.delete(`/admin/config/social-providers/${provider.value}`)
    success('已删除')
    navigateTo('/admin/config/social')
  } catch (e: any) {
    showError(e.message)
  } finally {
    showDeleteConfirm.value = false
  }
}
</script>

<template>
  <div>
    <PageHeader :title="isNew ? '新建提供商' : (form.displayName || provider)">
      <template #breadcrumb>
        <NuxtLink to="/admin" class="hover:text-text-primary">{{ t('admin.dashboard') }}</NuxtLink>
        <span>/</span>
        <NuxtLink to="/admin/config/social" class="hover:text-text-primary">社交登录配置</NuxtLink>
        <span>/</span>
        <span class="text-text-primary">{{ isNew ? '新建' : (form.displayName || provider) }}</span>
      </template>
    </PageHeader>

    <LoadingSpinner v-if="loading" />

    <template v-else>
      <div class="space-y-6 max-w-3xl">
        <!-- Basic -->
        <AppCard>
          <h3 class="text-sm font-semibold text-text-primary mb-4">基本信息</h3>
          <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <AppInput v-model="form.provider" label="Provider 标识" :disabled="!isNew" placeholder="github / gitee / qq" />
            <AppInput v-model="form.displayName" label="显示名称" />
            <AppInput v-model="form.sortOrder" type="number" label="排序" />
            <div class="flex items-end pb-1">
              <AppToggle v-if="!isNew" :model-value="form.enabled" @update:model-value="toggleEnabled" :label="form.enabled ? '已启用' : '未启用'" />
            </div>
          </div>
        </AppCard>

        <!-- OAuth Credentials -->
        <AppCard>
          <h3 class="text-sm font-semibold text-text-primary mb-4">OAuth 凭证</h3>
          <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <AppInput v-model="form.clientId" label="Client ID" />
            <AppInput v-model="form.clientSecret" type="password" label="Client Secret" :placeholder="isNew ? '' : '留空则不修改'" />
            <div class="sm:col-span-2">
              <AppInput v-model="form.scope" label="Scope" placeholder="多个 scope 用空格分隔" />
            </div>
            <div class="sm:col-span-2">
              <AppInput v-model="form.defaultRedirectUri" label="默认回调地址" />
            </div>
          </div>
        </AppCard>

        <!-- OAuth Endpoints -->
        <AppCard>
          <h3 class="text-sm font-semibold text-text-primary mb-4">OAuth 端点</h3>
          <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div class="sm:col-span-2">
              <AppInput v-model="form.authorizeUrl" label="授权地址 (Authorize URL)" />
            </div>
            <AppInput v-model="form.tokenUrl" label="Token URL" />
            <AppSelect v-model="form.tokenMethod" label="Token Method" :options="[{label:'POST',value:'POST'},{label:'GET',value:'GET'}]" />
            <AppToggle v-model="form.tokenResponseFormEncoded" label="Token 响应 Form-Encoded" />
            <AppInput v-model="form.tokenFieldName" label="Token 字段名" />
          </div>
        </AppCard>

        <!-- UserInfo -->
        <AppCard>
          <h3 class="text-sm font-semibold text-text-primary mb-4">用户信息接口</h3>
          <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div class="sm:col-span-2">
              <AppInput v-model="form.userinfoUrl" label="UserInfo URL" />
            </div>
            <AppSelect v-model="form.userinfoMethod" label="请求方式" :options="[{label:'GET',value:'GET'},{label:'POST',value:'POST'}]" />
            <AppSelect v-model="form.userinfoAuthStyle" label="认证方式" :options="[{label:'Header (Bearer)',value:'header'},{label:'Query Param',value:'query'},{label:'Form Body',value:'body'}]" />
            <AppInput v-model="form.userIdField" label="用户 ID 字段" />
            <AppInput v-model="form.nicknameField" label="昵称字段" />
            <AppInput v-model="form.avatarField" label="头像字段" />
            <AppInput v-model="form.emailField" label="邮箱字段" />
          </div>
        </AppCard>

        <!-- OpenID -->
        <AppCard>
          <div class="flex items-center justify-between mb-4">
            <h3 class="text-sm font-semibold text-text-primary">OpenID Connect</h3>
            <AppToggle v-model="form.openidEnabled" label="启用" />
          </div>
          <template v-if="form.openidEnabled">
            <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div class="sm:col-span-2">
                <AppInput v-model="form.openidUrl" label="OpenID URL" />
              </div>
              <AppInput v-model="form.openidFieldName" label="OpenID 字段名" />
              <AppInput v-model="form.openidResponseField" label="OpenID 响应字段" />
            </div>
          </template>
        </AppCard>

        <!-- Extra -->
        <AppCard>
          <h3 class="text-sm font-semibold text-text-primary mb-4">扩展参数</h3>
          <AppInput v-model="form.extraAuthorizeParams" label="额外授权参数" placeholder="JSON 格式，如 {&quot;prompt&quot;: &quot;consent&quot;}" />
        </AppCard>

        <!-- Actions -->
        <div class="flex items-center justify-between">
          <div class="flex items-center gap-3">
            <AppButton :loading="saving" @click="handleSave">{{ t('common.save') }}</AppButton>
            <NuxtLink to="/admin/config/social">
              <AppButton variant="secondary">{{ t('common.cancel') }}</AppButton>
            </NuxtLink>
          </div>
          <AppButton v-if="!isNew" variant="danger" size="sm" @click="showDeleteConfirm = true">{{ t('common.delete') }}</AppButton>
        </div>
      </div>
    </template>

    <ConfirmDialog
      v-model="showDeleteConfirm"
      title="删除提供商"
      :message="`确定要删除「${form.displayName || provider}」配置吗？`"
      danger
      @confirm="handleDelete"
    />
  </div>
</template>
