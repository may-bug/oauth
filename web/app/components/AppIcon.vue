<script setup lang="ts">
interface Props {
  name?: string
  src?: string
  size?: string | number
}

const props = withDefaults(defineProps<Props>(), {
  size: '24',
})

const imgError = ref(false)
</script>

<template>
  <Icon v-if="name && !src" :name="name" :size="size" />
  <img
    v-else-if="src && !imgError"
    :src="src"
    :width="size"
    :height="size"
    class="inline-block"
    @error="imgError = true"
  />
  <Icon v-else-if="name" :name="name" :size="size" />
</template>
