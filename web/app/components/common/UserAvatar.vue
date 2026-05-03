<script setup lang="ts">
interface Props {
  src?: string
  name?: string
  size?: 'sm' | 'md' | 'lg'
}
const props = withDefaults(defineProps<Props>(), { size: 'md' })

const sizeClasses = computed(() => {
  switch (props.size) {
    case 'sm': return 'w-7 h-7 text-[11px]'
    case 'md': return 'w-9 h-9 text-sm'
    case 'lg': return 'w-14 h-14 text-xl'
  }
})

const initials = computed(() => {
  if (!props.name) return '?'
  return props.name.slice(0, 1).toUpperCase()
})
</script>

<template>
  <div :class="['rounded-full bg-accent-light flex items-center justify-center overflow-hidden shrink-0 ring-1 ring-border/40', sizeClasses]">
    <img v-if="src" :src="src" :alt="name" class="w-full h-full object-cover" />
    <span v-else class="font-semibold text-accent">{{ initials }}</span>
  </div>
</template>
