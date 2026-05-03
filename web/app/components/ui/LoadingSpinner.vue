<script setup lang="ts">
interface Props {
  size?: 'sm' | 'md' | 'lg'
}
const props = withDefaults(defineProps<Props>(), { size: 'md' })

const sizeMap = { sm: '18', md: '28', lg: '42' }
</script>

<template>
  <div class="flex items-center justify-center py-10">
    <Icon name="mdi:loading" :size="sizeMap[size]" class="animate-spin text-accent" />
  </div>
</template>
