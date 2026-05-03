<script setup lang="ts">
interface Props {
  modelValue: string | number
  value: string | number
  label?: string
  disabled?: boolean
}
const props = defineProps<Props>()
const emit = defineEmits<{ 'update:modelValue': [value: string | number] }>()
</script>

<template>
  <label class="inline-flex items-center gap-2.5 cursor-pointer select-none group" :class="{ 'opacity-40 cursor-not-allowed': disabled }">
    <div
      :class="[
        'w-5 h-5 rounded-full border-2 flex items-center justify-center transition-all duration-150 ease-out',
        'group-active:scale-90',
        modelValue === value ? 'border-accent' : 'border-border hover:border-accent/40',
      ]"
      @click.prevent="!disabled && emit('update:modelValue', value)"
    >
      <div v-if="modelValue === value" class="w-2.5 h-2.5 rounded-full bg-accent" />
    </div>
    <span v-if="label" class="text-sm text-text-primary">{{ label }}</span>
  </label>
</template>
