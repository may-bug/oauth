<script setup lang="ts">

import AppHeader from "~/components/common/AppHeader.vue";
import AppFooter from "~/components/common/AppFooter.vue";

const { t } = useI18n()
useHead({ title: computed(() => `${t('docs.title')} — Auth Center`) })

const activeSection = ref('overview')

const sections = [
  { id: 'overview' },
  { id: 'auth' },
  { id: 'oauth2' },
  { id: 'social' },
  { id: 'user' },
  { id: 'client' },
]

const authApis = [
  { method: 'POST', path: '/v1/auth/login', desc: 'docs.apis.auth.loginPassword', body: { username: 'string', password: 'string (RSA加密)', captcha: 'string?', captchaToken: 'string?' }, response: '{ accessToken, refreshToken, expiresIn }' },
  { method: 'POST', path: '/v1/auth/login/email', desc: 'docs.apis.auth.loginEmail', body: { email: 'string', code: 'string' }, response: '{ accessToken, refreshToken, expiresIn }' },
  { method: 'POST', path: '/v1/auth/login/phone', desc: 'docs.apis.auth.loginPhone', body: { phone: 'string', code: 'string' }, response: '{ accessToken, refreshToken, expiresIn }' },
  { method: 'POST', path: '/v1/auth/register', desc: 'docs.apis.auth.register', body: { username: 'string', password: 'string (RSA加密)', nickname: 'string?', email: 'string?' }, response: '{ accessToken, refreshToken, expiresIn }' },
  { method: 'POST', path: '/v1/auth/refresh', desc: 'docs.apis.auth.refresh', body: { refreshToken: 'string' }, response: '{ accessToken, refreshToken }' },
  { method: 'GET', path: '/v1/auth/config', desc: 'docs.apis.auth.config', response: '{ passwordLogin, emailLogin, phoneLogin, socialLogin[], captcha{}, crypto{} }' },
  { method: 'GET', path: '/v1/auth/captcha', desc: 'docs.apis.auth.captcha', response: '{ svg, token, expireSeconds }', headers: { 'X-Captcha-Token': 'string' } },
  { method: 'POST', path: '/v1/auth/login/email/send', desc: 'docs.apis.auth.sendEmailCode', body: { email: 'string' } },
  { method: 'POST', path: '/v1/auth/login/phone/send', desc: 'docs.apis.auth.sendPhoneCode', body: { phone: 'string' } },
  { method: 'POST', path: '/v1/auth/register/email/send', desc: 'docs.apis.auth.sendRegisterEmailCode', body: { email: 'string' } },
  { method: 'GET', path: '/v1/auth/me', desc: 'docs.apis.auth.me', auth: true, response: '{ id, username, nickname, email, phone, avatar, status }' },
  { method: 'POST', path: '/v1/auth/logout', desc: 'docs.apis.auth.logout', auth: true },
]

const oauthApis = [
  { method: 'GET', path: '/v1/oauth2/authorize', desc: 'docs.apis.oauth.authorize', params: { client_id: 'string', redirect_uri: 'string', response_type: '"code"', scope: 'string', state: 'string?' }, response: 'HTTP 302 Redirect to redirect_uri?code=xxx&state=xxx', auth: true },
  { method: 'POST', path: '/v1/oauth2/token', desc: 'docs.apis.oauth.token', body: { grant_type: '"authorization_code"', code: 'string', redirect_uri: 'string', client_id: 'string', client_secret: 'string?' }, response: '{ access_token, token_type, expires_in, refresh_token, scope }' },
  { method: 'POST', path: '/v1/oauth2/introspect', desc: 'docs.apis.oauth.introspect', body: { token: 'string' }, response: '{ active, scope, client_id, username, exp }' },
  { method: 'POST', path: '/v1/oauth2/revoke', desc: 'docs.apis.oauth.revoke', body: { token: 'string' } },
]

const socialApis = [
  { method: 'GET', path: '/v1/auth/social/{provider}', desc: 'docs.apis.social.entry', params: { provider: '"github"|"gitee"|"qq"', redirectUri: 'string?' }, response: '{ auth_url, state }' },
  { method: 'GET', path: '/v1/auth/social/{provider}/callback', desc: 'docs.apis.social.callback', params: { code: 'string', state: 'string?' }, response: '{ accessToken, refreshToken }' },
  { method: 'GET', path: '/v1/auth/social/{provider}/bind', desc: 'docs.apis.social.bind', params: { provider: 'string' }, response: '{ auth_url, state }', auth: true },
  { method: 'POST', path: '/v1/auth/social/{provider}/unbind', desc: 'docs.apis.social.unbind', auth: true },
]

const supportedScopes = ['openid', 'profile', 'email', 'phone', 'address', 'offline_access']
const supportedGrantTypes = ['authorization_code', 'refresh_token', 'client_credentials']

function scrollTo(id: string) {
  activeSection.value = id
  document.getElementById(id)?.scrollIntoView({ behavior: 'smooth' })
}
</script>

<template>
  <div class="min-h-screen bg-bg flex flex-col">
    <AppHeader />

    <div class="flex flex-1 max-w-6xl mx-auto w-full pt-14">
      <!-- Sidebar -->
      <aside class="hidden lg:block w-56 shrink-0 border-r border-separator sticky top-14 h-[calc(100vh-3.5rem)] py-8 px-3 overflow-y-auto">
        <nav class="space-y-0.5">
          <button
            v-for="s in sections"
            :key="s.id"
            :class="[
              'w-full text-left px-3 py-2 rounded-lg text-[13px] font-medium transition-colors duration-150',
              activeSection === s.id ? 'bg-accent-light text-accent' : 'text-text-secondary hover:bg-surface-secondary hover:text-text-primary',
            ]"
            @click="scrollTo(s.id)"
          >{{ $t(`docs.sections.${s.id}`) }}</button>
        </nav>
      </aside>

      <!-- Content -->
      <main class="flex-1 min-w-0 py-8 px-6 md:px-12 max-w-4xl">
        <!-- Overview -->
        <section id="overview" class="mb-20">
          <h1 class="text-[32px] font-bold text-text-primary tracking-tight mb-3">{{ $t('docs.title') }}</h1>
          <p class="text-text-secondary text-[15px] leading-relaxed mb-8">
            <i18n-t keypath="docs.description" tag="span">
              <template #prefix>
                <code class="px-1.5 py-0.5 bg-surface-secondary rounded text-[13px] text-accent">/v1</code>
              </template>
            </i18n-t>
          </p>

          <div class="grid grid-cols-1 sm:grid-cols-2 gap-4 mb-8">
            <div class="bg-surface border border-border/40 rounded-2xl p-6">
              <div class="w-10 h-10 rounded-xl bg-accent-light flex items-center justify-center mb-4">
                <Icon name="mdi:lock-outline" size="20" class="text-accent" />
              </div>
              <h3 class="text-[15px] font-semibold text-text-primary mb-2">{{ $t('docs.overview.auth.title') }}</h3>
              <p class="text-[13px] text-text-secondary leading-relaxed">
                <i18n-t keypath="docs.overview.auth.desc" tag="span">
                  <template #header>
                    <code class="px-1 bg-surface-secondary rounded text-[12px]">Authorization: Bearer &lt;token&gt;</code>
                  </template>
                </i18n-t>
              </p>
            </div>
            <div class="bg-surface border border-border/40 rounded-2xl p-6">
              <div class="w-10 h-10 rounded-xl bg-success/10 flex items-center justify-center mb-4">
                <Icon name="mdi:code-json" size="20" class="text-success" />
              </div>
              <h3 class="text-[15px] font-semibold text-text-primary mb-2">{{ $t('docs.overview.format.title') }}</h3>
              <p class="text-[13px] text-text-secondary leading-relaxed">
                <i18n-t keypath="docs.overview.format.desc" tag="span">
                  <template #header>
                    <code class="px-1 bg-surface-secondary rounded text-[12px]">Content-Type: application/json</code>
                  </template>
                </i18n-t>
              </p>
            </div>
            <div class="bg-surface border border-border/40 rounded-2xl p-6">
              <div class="w-10 h-10 rounded-xl bg-warning/10 flex items-center justify-center mb-4">
                <Icon name="mdi:alert-circle-outline" size="20" class="text-warning" />
              </div>
              <h3 class="text-[15px] font-semibold text-text-primary mb-2">{{ $t('docs.overview.error.title') }}</h3>
              <p class="text-[13px] text-text-secondary leading-relaxed">
                <i18n-t keypath="docs.overview.error.desc" tag="span">
                  <template #format>
                    <code class="px-1 bg-surface-secondary rounded text-[12px]">{ code: number, message: string, data?: any }</code>
                  </template>
                </i18n-t>
              </p>
            </div>
            <div class="bg-surface border border-border/40 rounded-2xl p-6">
              <div class="w-10 h-10 rounded-xl bg-[#5856D6]/10 flex items-center justify-center mb-4">
                <Icon name="mdi:shield-key" size="20" class="text-[#5856D6]" />
              </div>
              <h3 class="text-[15px] font-semibold text-text-primary mb-2">{{ $t('docs.overview.crypto.title') }}</h3>
              <p class="text-[13px] text-text-secondary leading-relaxed">
                <i18n-t keypath="docs.overview.crypto.desc" tag="span">
                  <template #path>
                    <code class="px-1 bg-surface-secondary rounded text-[12px]">/auth/config</code>
                  </template>
                </i18n-t>
              </p>
            </div>
          </div>
        </section>

        <!-- Auth APIs -->
        <section id="auth" class="mb-20">
          <h2 class="text-[24px] font-bold text-text-primary mb-2">{{ $t('docs.auth.title') }}</h2>
          <p class="text-text-secondary text-[14px] mb-8">{{ $t('docs.auth.desc') }}</p>

          <div class="space-y-6">
            <div v-for="api in authApis" :key="api.path" class="bg-surface border border-border/40 rounded-2xl overflow-hidden">
              <div class="flex items-center gap-3 px-5 py-3.5 border-b border-separator bg-surface-secondary/20">
                <span :class="['px-2.5 py-1 rounded-md text-[11px] font-bold uppercase tracking-wider', api.method === 'GET' ? 'bg-success/10 text-success' : 'bg-accent/10 text-accent']">
                  {{ api.method }}
                </span>
                <code class="text-[14px] font-mono text-text-primary">{{ api.path }}</code>
                <span class="text-[13px] text-text-secondary ml-auto">{{ $t(api.desc) }}</span>
              </div>
              <div class="p-5 space-y-3 text-[13px]">
                <div v-if="api.auth" class="flex items-center gap-2 text-warning">
                  <Icon name="mdi:shield-lock" size="14" /> {{ $t('docs.label.requiresAuth') }}
                </div>
                <div v-if="api.headers">
                  <span class="font-semibold text-text-primary">{{ $t('docs.label.headers') }}</span>
                  <code class="ml-2 px-1.5 py-0.5 bg-surface-secondary rounded text-[12px]">{{ JSON.stringify(api.headers) }}</code>
                </div>
                <div v-if="api.params">
                  <span class="font-semibold text-text-primary">{{ $t('docs.label.params') }}</span>
                  <code class="ml-2 px-1.5 py-0.5 bg-surface-secondary rounded text-[12px]">{{ JSON.stringify(api.params) }}</code>
                </div>
                <div v-if="api.body">
                  <span class="font-semibold text-text-primary">{{ $t('docs.label.body') }}</span>
                  <code class="ml-2 px-1.5 py-0.5 bg-surface-secondary rounded text-[12px]">{{ JSON.stringify(api.body) }}</code>
                </div>
                <div v-if="api.response">
                  <span class="font-semibold text-text-primary">{{ $t('docs.label.response') }}</span>
                  <code class="ml-2 px-1.5 py-0.5 bg-surface-secondary rounded text-[12px]">{{ api.response }}</code>
                </div>
              </div>
            </div>
          </div>
        </section>

        <!-- OAuth 2.0 -->
        <section id="oauth2" class="mb-20">
          <h2 class="text-[24px] font-bold text-text-primary mb-2">{{ $t('docs.oauth.title') }}</h2>
          <p class="text-text-secondary text-[14px] mb-6">{{ $t('docs.oauth.desc') }}</p>

          <div class="grid grid-cols-1 sm:grid-cols-2 gap-4 mb-8">
            <div class="bg-surface border border-border/40 rounded-2xl p-5">
              <h4 class="text-sm font-semibold text-text-primary mb-2">{{ $t('docs.oauth.supportedScopes') }}</h4>
              <div class="flex flex-wrap gap-1.5">
                <span v-for="s in supportedScopes" :key="s" class="px-2 py-0.5 rounded-md bg-surface-secondary text-[12px] font-mono text-text-secondary">{{ s }}</span>
              </div>
            </div>
            <div class="bg-surface border border-border/40 rounded-2xl p-5">
              <h4 class="text-sm font-semibold text-text-primary mb-2">{{ $t('docs.oauth.supportedGrantTypes') }}</h4>
              <div class="flex flex-wrap gap-1.5">
                <span v-for="g in supportedGrantTypes" :key="g" class="px-2 py-0.5 rounded-md bg-surface-secondary text-[12px] font-mono text-text-secondary">{{ g }}</span>
              </div>
            </div>
          </div>

          <div class="space-y-6">
            <div v-for="api in oauthApis" :key="api.path" class="bg-surface border border-border/40 rounded-2xl overflow-hidden">
              <div class="flex items-center gap-3 px-5 py-3.5 border-b border-separator bg-surface-secondary/20">
                <span :class="['px-2.5 py-1 rounded-md text-[11px] font-bold uppercase tracking-wider', api.method === 'GET' ? 'bg-success/10 text-success' : 'bg-accent/10 text-accent']">
                  {{ api.method }}
                </span>
                <code class="text-[14px] font-mono text-text-primary">{{ api.path }}</code>
                <span class="text-[13px] text-text-secondary ml-auto">{{ $t(api.desc) }}</span>
              </div>
              <div class="p-5 space-y-3 text-[13px]">
                <div v-if="api.auth" class="flex items-center gap-2 text-warning">
                  <Icon name="mdi:shield-lock" size="14" /> {{ $t('docs.label.requiresAuth') }}
                </div>
                <div v-if="api.params">
                  <span class="font-semibold text-text-primary">{{ $t('docs.label.params') }}</span>
                  <code class="ml-2 px-1.5 py-0.5 bg-surface-secondary rounded text-[12px]">{{ JSON.stringify(api.params) }}</code>
                </div>
                <div v-if="api.body">
                  <span class="font-semibold text-text-primary">{{ $t('docs.label.body') }}</span>
                  <code class="ml-2 px-1.5 py-0.5 bg-surface-secondary rounded text-[12px]">{{ JSON.stringify(api.body) }}</code>
                </div>
                <div v-if="api.response">
                  <span class="font-semibold text-text-primary">{{ $t('docs.label.response') }}</span>
                  <code class="ml-2 px-1.5 py-0.5 bg-surface-secondary rounded text-[12px]">{{ api.response }}</code>
                </div>
              </div>
            </div>
          </div>
        </section>
      </main>
    </div>

    <AppFooter />
  </div>
</template>
