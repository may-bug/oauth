<script setup lang="ts">
interface Props {
  modelValue: boolean
  label?: string
  disabled?: boolean
}

const props = defineProps<Props>()
const emit = defineEmits<{
  'update:modelValue': [value: boolean]
}>()
</script>

<template>
  <label class="inline-flex items-center gap-3 cursor-pointer select-none" :class="{ 'opacity-40 cursor-not-allowed': disabled }">
    <button
      type="button"
      role="switch"
      :aria-checked="modelValue"
      :disabled="disabled"
      :class="[
        'relative w-12 h-7 rounded-full transition-colors duration-200 ease-out shrink-0',
        modelValue ? 'bg-accent' : 'bg-border',
      ]"
      @click="emit('update:modelValue', !modelValue)"
    >
      <span
        :class="[
          'absolute top-0.5 w-6 h-6 bg-white rounded-full shadow-sm transition-all duration-200 ease-out',
          modelValue ? 'left-[calc(100%-1.625rem)]' : 'left-0.5',
        ]"
      />
    </button>
    <span v-if="label" class="text-sm text-text-primary">{{ label }}</span>
  </label>
</template>
