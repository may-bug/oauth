<script setup lang="ts">
interface Props {
  icon?: string
  title?: string
  description?: string
}
const props = withDefaults(defineProps<Props>(), {
  icon: 'mdi:inbox-outline',
})
</script>

<template>
  <div class="flex flex-col items-center justify-center py-14 text-center">
    <div class="w-16 h-16 rounded-2xl bg-surface-secondary flex items-center justify-center mb-4">
      <Icon :name="icon" size="28" class="text-text-tertiary" />
    </div>
    <h3 v-if="title" class="text-[15px] font-medium text-text-primary mb-1">{{ title }}</h3>
    <p v-if="description" class="text-sm text-text-secondary max-w-xs">{{ description }}</p>
    <div v-if="$slots.default" class="mt-4">
      <slot />
    </div>
  </div>
</template>
