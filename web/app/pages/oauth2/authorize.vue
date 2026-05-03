<script setup lang="ts">
import OAuthConsentCard from "~/components/auth/OAuthConsentCard.vue";

definePageMeta({ layout: 'oauth' })

const route = useRoute()
const api = useApi()
const auth = useAuth()
const { error: showError } = useToast()

const clientId = route.query.client_id as string
const redirectUri = route.query.redirect_uri as string
const scope = (route.query.scope as string) || 'openid'
const state = route.query.state as string | undefined

const clientName = ref(clientId)
const scopes = computed(() => scope.split(' '))
const loading = ref(false)

onMounted(async () => {
  // Could fetch client details if endpoint exists
})

async function authorize() {
  loading.value = true
  try {
    const params = new URLSearchParams({
      client_id: clientId,
      redirect_uri: redirectUri,
      scope,
      response_type: 'code',
    })
    if (state) params.set('state', state)

    const data = await api.get<{ code: string; state?: string }>(`/oauth2/authorize?${params.toString()}`)
    const url = new URL(redirectUri)
    url.searchParams.set('code', data.code)
    if (data.state) url.searchParams.set('state', data.state)
    navigateTo(url.toString(), { external: true })
  } catch (e: any) {
    showError(e.message || 'Authorization failed')
  } finally {
    loading.value = false
  }
}

function deny() {
  const url = new URL(redirectUri)
  url.searchParams.set('error', 'access_denied')
  if (state) url.searchParams.set('state', state)
  navigateTo(url.toString(), { external: true })
}
</script>

<template>
  <OAuthConsentCard
    :client-name="clientName"
    :scopes="scopes"
    :username="auth.user?.value?.nickname || auth.user?.value?.username"
    :avatar="auth.user?.value?.avatar"
    @authorize="authorize"
    @deny="deny"
  />
</template>
