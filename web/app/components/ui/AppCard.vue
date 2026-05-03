<script setup lang="ts">
interface Props {
  class?: string
  padding?: boolean
  hover?: boolean
}
const props = withDefaults(defineProps<Props>(), {
  padding: true,
  hover: false,
})
</script>

<template>
  <div
    :class="[
      'bg-surface border border-border/60 rounded-2xl shadow-sm',
      hover ? 'transition-all duration-200 ease-out hover:shadow-md hover:-translate-y-px' : '',
      padding ? 'p-6' : '',
      props.class,
    ]"
  >
    <slot />
  </div>
</template>
