<script setup lang="ts">
interface Props {
  modelValue: string | number | undefined
  options: Array<{ label: string; value: string | number }>
  label?: string
  placeholder?: string
  error?: string
  disabled?: boolean
}

const props = defineProps<Props>()
const emit = defineEmits<{
  'update:modelValue': [value: string | number]
}>()
</script>

<template>
  <div class="space-y-1.5">
    <label v-if="label" class="block text-[13px] font-medium text-text-primary">{{ label }}</label>
    <select
      :value="modelValue"
      :disabled="disabled"
      :class="[
        'w-full h-9 bg-surface-secondary/50 border rounded-xl px-3 text-sm text-text-primary',
        'focus:outline-none focus:ring-2 focus:ring-accent/20 focus:border-accent focus:bg-surface',
        'transition-all duration-150 ease-out',
        'disabled:opacity-40 disabled:cursor-not-allowed',
        'appearance-none bg-no-repeat select-chevron',
        error ? 'border-danger focus:ring-danger/20' : 'border-border',
      ]"
      @change="emit('update:modelValue', ($event.target as HTMLSelectElement).value)"
    >
      <option v-if="placeholder" value="" disabled>{{ placeholder }}</option>
      <option v-for="opt in options" :key="opt.value" :value="opt.value">{{ opt.label }}</option>
    </select>
    <p v-if="error" class="text-xs text-danger">{{ error }}</p>
  </div>
</template>

<style scoped>
.select-chevron {
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='12' height='12' fill='%23AEAEB2' viewBox='0 0 24 24'%3E%3Cpath d='M7.41 8.58L12 13.17l4.59-4.59L18 10l-6 6-6-6z'/%3E%3C/svg%3E");
  background-position: right 10px center;
}
</style>
