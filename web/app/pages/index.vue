<script setup lang="ts">

import AppHeader from "~/components/common/AppHeader.vue";
import AppFooter from "~/components/common/AppFooter.vue";

const authStore = useAuthStore()
const { t } = useI18n()
useHead({ title: t('index.pageTitle') })

// Scroll-driven 3D transforms
const scrollY = ref(0)
const heroRef = ref<HTMLElement>()
const featuresRef = ref<HTMLElement>()
const flowRef = ref<HTMLElement>()

onMounted(() => {
  window.addEventListener('scroll', () => scrollY.value = window.scrollY, { passive: true })
})
onUnmounted(() => window.removeEventListener('scroll', () => {}))

// Intersection observer for 3D entrance animations
const visibleSections = ref<Set<string>>(new Set())
onMounted(() => {
  const observer = new IntersectionObserver((entries) => {
    entries.forEach(e => {
      if (e.isIntersecting) visibleSections.value.add(e.target.id)
    })
  }, { threshold: 0.2 })
  document.querySelectorAll('[data-section]').forEach(el => observer.observe(el))
  setTimeout(() => visibleSections.value.add('hero'), 100)
})

const features = [
  { key: 'oauth', icon: 'mdi:shield-check', img: null },
  { key: 'multiLogin', icon: 'mdi:account-multiple', img: null },
  { key: 'security', icon: 'mdi:shield-lock', img: null },
  { key: 'management', icon: 'mdi:cog-outline', img: null },
  { key: 'i18n', icon: 'mdi:earth', img: null },
  { key: 'darkMode', icon: 'mdi:weather-night', img: null },
]

const steps = [
  { step: '01', key: 'register', icon: 'mdi:application-outline' },
  { step: '02', key: 'integrate', icon: 'mdi:code-tags' },
  { step: '03', key: 'authorize', icon: 'mdi:account-check-outline' },
  { step: '04', key: 'complete', icon: 'mdi:check-decagram-outline' },
]
</script>

<template>
  <div class="bg-bg">
    <AppHeader />

    <!-- ==================== HERO ==================== -->
    <section id="hero" data-section class="min-h-screen flex items-center justify-center relative overflow-hidden" ref="heroRef">
      <!-- Ambient -->
      <div class="absolute inset-0 pointer-events-none">
        <div class="absolute w-[600px] h-[600px] rounded-full bg-accent/[0.03] blur-[150px]"
          :style="{ transform: `translate(-50%,-50%) translate(${scrollY*0.02}px,${-scrollY*0.01}px)`, left:'55%', top:'50%' }" />
        <div class="absolute w-[400px] h-[400px] rounded-full bg-accent/[0.02] blur-[120px]"
          :style="{ transform: `translate(-50%,-50%) translate(${-scrollY*0.03}px,${scrollY*0.02}px)`, left:'35%', top:'60%' }" />
        <div class="absolute inset-0 opacity-[0.02] dark:opacity-[0.04]"
          style="background-image: radial-gradient(circle, var(--color-text-primary) 1px, transparent 1px); background-size: 32px 32px;"
          :style="{ transform: `translateY(${scrollY*-0.02}px)` }" />

        <!-- Decorative SVG curves -->
        <svg class="absolute inset-0 w-full h-full opacity-[0.03] dark:opacity-[0.05]" viewBox="0 0 1440 900" fill="none">
          <path d="M-100 350 Q 500 100 900 400 T 1600 300" stroke="var(--color-accent)" stroke-width="0.4" stroke-dasharray="3 6" />
          <path d="M-100 600 Q 400 800 800 600 T 1600 650" stroke="var(--color-accent)" stroke-width="0.3" stroke-dasharray="2 5" />
          <circle cx="250" cy="200" r="1.5" fill="var(--color-accent)" opacity="0.25" />
          <circle cx="650" cy="700" r="2" fill="var(--color-accent)" opacity="0.2" />
          <circle cx="1050" cy="280" r="1" fill="var(--color-accent)" opacity="0.3" />
          <circle cx="400" cy="500" r="1.5" fill="var(--color-accent)" opacity="0.2" />
        </svg>
      </div>

      <div class="max-w-6xl mx-auto px-4 sm:px-6 py-32 w-full relative z-10">
        <div class="flex flex-col lg:flex-row items-center gap-12 lg:gap-8">
          <!-- Left -->
          <div class="flex-1 text-center lg:text-left"
            :style="{ transform: `translateY(${scrollY*-0.02}px)` }">
            <div class="inline-flex items-center gap-2 px-3 py-1.5 rounded-full bg-accent-light text-accent text-xs font-medium mb-8">
              <Icon name="mdi:shield-check" size="13" />
              {{ $t('auth.badge') }}
            </div>
            <h1 class="text-[36px] sm:text-[44px] lg:text-[50px] font-bold text-text-primary leading-[1.08] tracking-[-0.015em] mb-5 max-w-lg">
              {{ $t('index.heroTitle') }}<br />
              <span class="text-accent">{{ $t('index.heroHighlight') }}</span>
            </h1>
            <p class="text-[15px] sm:text-base text-text-secondary leading-relaxed max-w-md mx-auto lg:mx-0 mb-9">
              {{ $t('index.heroDesc') }}
            </p>
            <div class="flex flex-col sm:flex-row items-center gap-3 justify-center lg:justify-start">
                <template v-if="authStore.isAuthenticated">
                  <NuxtLink to="/admin" class="h-11 px-7 flex items-center gap-2 rounded-xl text-sm font-semibold bg-accent text-white hover:bg-accent-hover active:scale-[0.97] shadow-sm shadow-accent/15 transition-all duration-200">
                    {{ $t('auth.goAdmin') }} <Icon name="mdi:arrow-right" size="17" />
                  </NuxtLink>
                  <NuxtLink to="/user" class="h-11 px-7 flex items-center rounded-xl text-sm font-medium text-text-primary border border-border hover:bg-surface-secondary active:scale-[0.97] transition-all duration-200">
                    {{ $t('profile.title') }}
                  </NuxtLink>
                </template>
                <template v-else>
                  <NuxtLink to="/auth/register" class="h-11 px-7 flex items-center gap-2 rounded-xl text-sm font-semibold bg-accent text-white hover:bg-accent-hover active:scale-[0.97] shadow-sm shadow-accent/15 transition-all duration-200">
                    {{ $t('auth.getStarted') }} <Icon name="mdi:arrow-right" size="17" />
                  </NuxtLink>
                  <NuxtLink to="/auth/login" class="h-11 px-7 flex items-center rounded-xl text-sm font-medium text-text-primary border border-border hover:bg-surface-secondary active:scale-[0.97] transition-all duration-200">
                    {{ $t('auth.login') }}
                  </NuxtLink>
                </template>
            </div>
          </div>

          <!-- Right: Floating visual — spread out with proper spacing -->
          <div class="hidden lg:block flex-1 relative h-[480px]">
            <!-- Card 1: Auth dialog — top center-right -->
            <div class="absolute top-0 right-8 w-56 glass-heavy rounded-2xl p-3.5 shadow-xl"
              :style="{ transform: `translateY(${scrollY*-0.04}px)` }">
              <div class="flex items-center gap-2 mb-2.5">
                <div class="w-7 h-7 rounded-lg bg-accent/10 flex items-center justify-center"><Icon name="mdi:shield-key" size="14" class="text-accent" /></div>
                <div>
                  <div class="text-[11px] font-semibold text-text-primary">{{ $t('index.heroCardTitle') }}</div>
                  <div class="text-[9px] text-text-tertiary">{{ $t('index.heroCardRequestAccess') }}</div>
                </div>
              </div>
              <div class="space-y-1 mb-2.5">
                <div class="flex items-center gap-1.5 text-[10px] text-text-secondary"><Icon name="mdi:check-circle" size="10" class="text-success" /> {{ $t('oauth.scope.profile') }}</div>
                <div class="flex items-center gap-1.5 text-[10px] text-text-secondary"><Icon name="mdi:check-circle" size="10" class="text-success" /> {{ $t('oauth.scope.email') }}</div>
                <div class="flex items-center gap-1.5 text-[10px] text-text-secondary"><Icon name="mdi:check-circle" size="10" class="text-success" /> {{ $t('oauth.scope.offline_access') }}</div>
              </div>
              <div class="flex gap-1.5">
                <div class="flex-1 h-6 rounded-lg bg-surface-secondary text-[10px] flex items-center justify-center text-text-secondary font-medium">{{ $t('oauth.deny') }}</div>
                <div class="flex-1 h-6 rounded-lg bg-accent text-[10px] flex items-center justify-center text-white font-medium">{{ $t('oauth.authorize') }}</div>
              </div>
            </div>

            <!-- Card 2: User badge — middle left -->
            <div class="absolute top-[140px] left-2 w-44 bg-surface rounded-2xl p-3 shadow-lg border border-border/30"
              :style="{ transform: `translateY(${scrollY*0.03}px)` }">
              <div class="flex items-center gap-2.5">
                <div class="w-8 h-8 rounded-full bg-accent-light flex items-center justify-center"><Icon name="mdi:account" size="16" class="text-accent" /></div>
                <div>
                  <div class="text-[11px] font-semibold text-text-primary">user@example</div>
                  <div class="text-[9px] text-success">● {{ $t('index.heroCardAuthenticated') }}</div>
                </div>
              </div>
            </div>

            <!-- Card 3: Token response — bottom right -->
            <div class="absolute bottom-[80px] right-4 w-48 bg-surface rounded-2xl p-3 shadow-lg border border-border/30"
              :style="{ transform: `translateY(${scrollY*-0.02}px)` }">
              <div class="flex items-center gap-1.5 mb-2">
                <div class="w-1.5 h-1.5 rounded-full bg-success" /><span class="text-[9px] font-semibold text-text-secondary uppercase tracking-wider">Token</span>
              </div>
              <div class="space-y-1 font-mono text-[10px]">
                <div class="flex items-center gap-1"><span class="text-success bg-success/8 px-1 rounded font-bold">200</span><span class="text-text-primary">eyJhbGciOi...</span></div>
                <div class="h-0.5 w-2/3 bg-surface-secondary rounded-full opacity-40" />
                <div class="flex items-center gap-1"><span class="text-accent bg-accent/8 px-1 rounded font-bold">JWT</span><span class="text-text-primary">dGhpcyBp...</span></div>
              </div>
            </div>

            <!-- Badge: TLS — bottom left -->
            <div class="absolute bottom-[140px] left-14 w-14 h-14 rounded-2xl bg-surface border border-border/40 shadow-sm flex flex-col items-center justify-center gap-0.5"
              :style="{ transform: `translateY(${scrollY*0.05}px)` }">
              <Icon name="mdi:lock" size="16" class="text-accent" /><span class="text-[8px] font-bold text-accent">TLS</span>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- ==================== FEATURES — 3D perspective section ==================== -->
    <section id="features" data-section class="min-h-screen flex items-center relative overflow-hidden border-t border-separator"
      style="perspective: 1200px;">
      <!-- Background -->
      <div class="absolute inset-0 pointer-events-none bg-surface-secondary/20">
        <div class="absolute w-[500px] h-[500px] rounded-full bg-accent/[0.02] blur-[130px]"
          :style="{ transform: `translate(-50%,-50%) translate(${scrollY*0.01}px,${-scrollY*0.02}px)`, left:'50%', top:'60%' }" />
      </div>

      <div class="max-w-6xl mx-auto px-4 sm:px-6 py-24 w-full relative z-10">
        <div class="text-center mb-14"
          :style="{ opacity: visibleSections.has('features') ? 1 : 0, transform: `translateY(${visibleSections.has('features') ? 0 : 30}px)`, transition: 'all 0.7s cubic-bezier(0.16,1,0.3,1)' }">
          <div class="accent-bar mx-auto mb-5" />
          <h2 class="text-[30px] font-bold text-text-primary tracking-[-0.01em] mb-2">{{ $t('index.sectionFeatures') }}</h2>
          <p class="text-sm text-text-secondary">{{ $t('index.sectionFeaturesDesc') }}</p>
        </div>

        <!-- 3D Card grid -->
        <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-5 max-w-5xl mx-auto">
          <div
            v-for="(f, i) in features"
            :key="f.key"
            :style="{
              opacity: visibleSections.has('features') ? 1 : 0,
              transform: visibleSections.has('features')
                ? 'rotateX(0deg) rotateY(0deg) translateZ(0) scale(1)'
                : `rotateX(${15 - i*2}deg) translateZ(-${(5-i)*8}px) scale(0.85)`,
              transition: `all 0.8s cubic-bezier(0.16,1,0.3,1) ${i*0.08}s`,
              transformStyle: 'preserve-3d',
            }"
            class="group bg-surface border border-border/40 rounded-2xl p-6 hover:shadow-lg hover:-translate-y-1 transition-all duration-300"
          >
            <div class="w-10 h-10 rounded-xl bg-accent-light flex items-center justify-center mb-4 group-hover:scale-110 transition-transform duration-300">
              <Icon :name="f.icon" size="20" class="text-accent" />
            </div>
            <h3 class="text-[15px] font-semibold text-text-primary mb-2">{{ $t(`index.features.${f.key}.title`) }}</h3>
            <p class="text-[13px] text-text-secondary leading-relaxed">{{ $t(`index.features.${f.key}.desc`) }}</p>
            <!-- Image placeholder -->
            <div v-if="f.img" class="mt-4 rounded-xl overflow-hidden bg-surface-secondary h-32 flex items-center justify-center">
              <img :src="f.img" :alt="$t(`index.features.${f.key}.title`)" class="w-full h-full object-cover" />
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- ==================== HOW IT WORKS — 3D flip cards ==================== -->
    <section id="flow" data-section class="min-h-screen flex items-center relative overflow-hidden border-t border-separator"
      style="perspective: 1200px;">
      <div class="absolute inset-0 pointer-events-none">
        <div class="absolute w-[600px] h-[600px] rounded-full bg-accent/[0.02] blur-[140px]"
          :style="{ transform: `translate(-50%,-50%) translate(${-scrollY*0.02}px,${scrollY*0.01}px)`, left:'40%', top:'45%' }" />
      </div>

      <div class="max-w-6xl mx-auto px-4 sm:px-6 py-24 w-full relative z-10">
        <div class="text-center mb-16"
          :style="{ opacity: visibleSections.has('flow') ? 1 : 0, transform: `translateY(${visibleSections.has('flow') ? 0 : 24}px)`, transition: 'all 0.6s cubic-bezier(0.16,1,0.3,1)' }">
          <div class="accent-bar mx-auto mb-5" />
          <h2 class="text-[30px] font-bold text-text-primary tracking-[-0.01em] mb-2">{{ $t('index.sectionFlow') }}</h2>
          <p class="text-sm text-text-secondary">{{ $t('index.sectionFlowDesc') }}</p>
        </div>

        <!-- 3D step cards -->
        <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-5 max-w-5xl mx-auto">
          <div
            v-for="(s, i) in steps"
            :key="s.step"
            :style="{
              opacity: visibleSections.has('flow') ? 1 : 0,
              transform: visibleSections.has('flow')
                ? 'rotateY(0deg) translateZ(0)'
                : `rotateY(${30 - i*8}deg) translateZ(-${(4-i)*25}px)`,
              transition: `all 0.7s cubic-bezier(0.16,1,0.3,1) ${i*0.1}s`,
              transformStyle: 'preserve-3d',
            }"
            class="group relative bg-surface border border-border/40 rounded-2xl p-6 hover:shadow-lg hover:-translate-y-1 transition-all duration-300"
          >
            <div class="w-10 h-10 rounded-xl bg-accent-light flex items-center justify-center mb-4 group-hover:scale-110 transition-transform duration-300">
              <Icon :name="s.icon" size="20" class="text-accent" />
            </div>
            <div class="absolute top-4 right-4 text-[40px] font-bold text-accent/6 leading-none">{{ s.step }}</div>
            <h3 class="text-[15px] font-semibold text-text-primary mb-2">{{ $t(`index.steps.${s.key}.title`) }}</h3>
            <p class="text-[13px] text-text-secondary leading-relaxed">{{ $t(`index.steps.${s.key}.desc`) }}</p>
          </div>
        </div>
      </div>
    </section>

    <AppFooter />
  </div>
</template>
