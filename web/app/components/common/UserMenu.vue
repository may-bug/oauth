<script setup lang="ts">

import UserAvatar from "~/components/common/UserAvatar.vue";

const auth = useAuth()
const { t } = useI18n()

const isOpen = ref(false)
const menuRef = ref<HTMLElement>()

function handleClickOutside(e: Event) {
  if (menuRef.value && !menuRef.value.contains(e.target as Node)) {
    isOpen.value = false
  }
}

onMounted(() => document.addEventListener('click', handleClickOutside))
onUnmounted(() => document.removeEventListener('click', handleClickOutside))
</script>

<template>
  <div ref="menuRef" class="relative">
    <button
      class="flex items-center gap-2 p-1 pr-2 rounded-lg hover:bg-surface-secondary transition-colors duration-150"
      @click="isOpen = !isOpen"
    >
      <UserAvatar :src="auth.user?.value?.avatar" :name="auth.user?.value?.nickname || auth.user?.value?.username" size="sm" />
      <span class="text-[13px] font-medium text-text-primary hidden sm:block">
        {{ auth.user?.value?.nickname || auth.user?.value?.username }}
      </span>
      <Icon name="mdi:chevron-down" size="14" class="text-text-tertiary" />
    </button>

    <Transition name="dropdown">
      <div v-if="isOpen" class="absolute right-0 mt-1.5 w-48 bg-surface border border-border/60 rounded-xl shadow-lg py-1 z-50 overflow-hidden">
        <NuxtLink to="/user" class="flex items-center gap-2.5 px-4 py-2.5 text-[13px] text-text-primary hover:bg-surface-secondary transition-colors duration-100" @click="isOpen = false">
          <Icon name="mdi:account-circle-outline" size="17" />
          {{ t('profile.title') }}
        </NuxtLink>
        <NuxtLink to="/admin" class="flex items-center gap-2.5 px-4 py-2.5 text-[13px] text-text-primary hover:bg-surface-secondary transition-colors duration-100" @click="isOpen = false">
          <Icon name="mdi:cog-outline" size="17" />
          {{ t('admin.title') }}
        </NuxtLink>
        <div class="my-1 border-t border-separator" />
        <button class="w-full flex items-center gap-2.5 px-4 py-2.5 text-[13px] text-danger hover:bg-danger/6 transition-colors duration-100" @click="auth.logout()">
          <Icon name="mdi:logout" size="17" />
          {{ t('auth.logout') }}
        </button>
      </div>
    </Transition>
  </div>
</template>

<style scoped>
.dropdown-enter-active { transition: all 0.15s var(--ease-out); }
.dropdown-leave-active { transition: all 0.1s var(--ease-in); }
.dropdown-enter-from,
.dropdown-leave-to { opacity: 0; transform: translateY(-6px) scale(0.96); }
</style>
