<script setup lang="ts">
import AppIcon from "~/components/AppIcon.vue";

interface Props {
  size?: 'sm' | 'md' | 'lg'
}
const props = withDefaults(defineProps<Props>(), { size: 'md' })

const sizeClasses = computed(() => {
  switch (props.size) {
    case 'sm': return 'text-[17px]'
    case 'md': return 'text-2xl'
    case 'lg': return 'text-[34px]'
  }
})

const iconSize = computed(() => {
  switch (props.size) {
    case 'sm': return '22'
    case 'md': return '30'
    case 'lg': return '44'
  }
})
</script>

<template>
  <div class="flex items-center gap-2">
    <div class="text-accent">
      <AppIcon src="/icons/logo.svg" name="mdi:shield-key" :size="iconSize" />
    </div>
    <span :class="['font-semibold text-text-primary tracking-tight', sizeClasses]">Auth Center</span>
  </div>
</template>
