<script setup lang="ts">
import Breadcrumb from "~/components/common/Breadcrumb.vue";

interface Props {
  title: string
}
defineProps<Props>()
</script>

<template>
  <div class="flex items-center justify-between mb-8">
    <div>
      <h1 class="app-title-lg text-text-primary">{{ title }}</h1>
      <Breadcrumb v-if="$slots.breadcrumb" class="mt-1.5">
        <slot name="breadcrumb" />
      </Breadcrumb>
    </div>
    <div v-if="$slots.actions" class="flex items-center gap-2">
      <slot name="actions" />
    </div>
  </div>
</template>
