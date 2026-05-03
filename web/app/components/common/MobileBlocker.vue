<script setup lang="ts">

import AppButton from "~/components/ui/AppButton.vue";

const { t } = useI18n()
</script>

<template>
  <div class="fixed inset-0 bg-bg flex flex-col items-center justify-center p-8 text-center">
    <Icon name="mdi:monitor" size="80" class="text-text-tertiary mb-6" />
    <h2 class="text-xl font-semibold text-text-primary mb-2">{{ t('admin.mobileBlocker') }}</h2>
    <NuxtLink to="/user" class="mt-6">
      <AppButton variant="secondary">
        {{ t('admin.backToProfile') }}
      </AppButton>
    </NuxtLink>
  </div>
</template>
