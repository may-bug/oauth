<script setup lang="ts">
interface Props {
  modelValue: boolean
  label?: string
  indeterminate?: boolean
  disabled?: boolean
}
const props = defineProps<Props>()
const emit = defineEmits<{ 'update:modelValue': [value: boolean] }>()
</script>

<template>
  <label class="inline-flex items-center gap-2.5 cursor-pointer select-none group" :class="{ 'opacity-40 cursor-not-allowed': disabled }">
    <div
      :class="[
        'w-5 h-5 rounded-md border-2 flex items-center justify-center transition-all duration-150 ease-out',
        'group-active:scale-90',
        modelValue || indeterminate
          ? 'bg-accent border-accent'
          : 'bg-surface border-border hover:border-accent/40',
      ]"
      @click.prevent="!disabled && emit('update:modelValue', !modelValue)"
    >
      <Icon v-if="modelValue" name="mdi:check-bold" size="12" class="text-white" />
      <Icon v-else-if="indeterminate" name="mdi:minus-bold" size="12" class="text-white" />
    </div>
    <span v-if="label" class="text-sm text-text-primary">{{ label }}</span>
  </label>
</template>
