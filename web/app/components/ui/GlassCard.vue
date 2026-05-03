<script setup lang="ts">
interface Props {
  class?: string
}
const props = defineProps<Props>()
</script>

<template>
  <div
    :class="[
      'glass rounded-2xl shadow-lg p-6 border border-white/20',
      'animate-scale-in',
      props.class,
    ]"
  >
    <slot />
  </div>
</template>
