<script setup lang="ts">
interface Props {
  modelValue: string
  label?: string
  placeholder?: string
  error?: string
  maxLength?: number
  rows?: number
  disabled?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  rows: 3,
  modelValue: '',
})

const emit = defineEmits<{
  'update:modelValue': [value: string]
}>()
</script>

<template>
  <div class="space-y-1.5">
    <div v-if="label" class="flex items-center justify-between">
      <label class="text-[13px] font-medium text-text-primary">{{ label }}</label>
      <span v-if="maxLength" class="text-[11px] text-text-tertiary tabular-nums">{{ modelValue.length }}/{{ maxLength }}</span>
    </div>
    <textarea
      :value="modelValue"
      :placeholder="placeholder"
      :rows="rows"
      :disabled="disabled"
      :maxlength="maxLength"
      :class="[
        'w-full bg-surface-secondary/50 border rounded-xl px-3 py-2.5 text-sm text-text-primary resize-none',
        'placeholder:text-text-tertiary',
        'focus:outline-none focus:ring-2 focus:ring-accent/20 focus:border-accent focus:bg-surface',
        'transition-all duration-150 ease-out',
        'disabled:opacity-40 disabled:cursor-not-allowed',
        error ? 'border-danger focus:ring-danger/20' : 'border-border',
      ]"
      @input="emit('update:modelValue', ($event.target as HTMLTextAreaElement).value)"
    />
    <p v-if="error" class="text-xs text-danger">{{ error }}</p>
  </div>
</template>
