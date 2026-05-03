<script setup lang="ts">

import LoadingSpinner from "~/components/ui/LoadingSpinner.vue";

definePageMeta({ layout: 'auth' })

const route = useRoute()
const auth = useAuth()
const { error: showError } = useToast()

onMounted(async () => {
  const provider = route.params.provider as string
  const code = route.query.code as string
  const state = route.query.state as string | undefined
  const redirect = route.query.redirect as string | undefined

  if (!code) {
    showError('Missing authorization code')
    await navigateTo('/auth/login')
    return
  }

  try {
    const redirectUri = await auth.handleSocialCallback(provider, code, state)
    // Backend returns redirect target in state, fallback to home
    await navigateTo(redirectUri || '/')
  } catch (e: any) {
    showError(e.message || 'Social login failed')
    await navigateTo('/auth/login')
  }
})
</script>

<template>
  <div class="text-center">
    <LoadingSpinner size="lg" />
    <p class="mt-4 text-text-secondary">{{ $t('common.loading') }}</p>
  </div>
</template>
