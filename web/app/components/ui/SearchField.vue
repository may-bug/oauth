<script setup lang="ts">
interface Props {
  modelValue: string
  placeholder?: string
}
const props = withDefaults(defineProps<Props>(), { placeholder: '搜索...' })
const emit = defineEmits<{ 'update:modelValue': [value: string] }>()

let timer: ReturnType<typeof setTimeout>
function onInput(value: string) {
  clearTimeout(timer)
  timer = setTimeout(() => emit('update:modelValue', value), 300)
}
</script>

<template>
  <div class="relative">
    <Icon name="mdi:magnify" size="16" class="absolute left-3 top-1/2 -translate-y-1/2 text-text-tertiary pointer-events-none" />
    <input
      type="text"
      :value="modelValue"
      :placeholder="placeholder"
      class="w-full h-9 bg-surface-secondary/50 border border-border rounded-xl pl-9 pr-3 text-sm text-text-primary placeholder:text-text-tertiary focus:outline-none focus:ring-2 focus:ring-accent/20 focus:border-accent focus:bg-surface transition-all duration-150 ease-out"
      @input="onInput(($event.target as HTMLInputElement).value)"
    />
  </div>
</template>
