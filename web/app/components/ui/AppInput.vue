<script setup lang="ts">
interface Props {
  modelValue?: string
  type?: string
  label?: string
  placeholder?: string
  error?: string
  helpText?: string
  disabled?: boolean
  icon?: string
}

const props = withDefaults(defineProps<Props>(), {
  type: 'text',
  modelValue: '',
})

const emit = defineEmits<{
  'update:modelValue': [value: string]
}>()

const showPassword = ref(false)
const inputType = computed(() => {
  if (props.type === 'password') return showPassword.value ? 'text' : 'password'
  return props.type
})
</script>

<template>
  <div class="space-y-1.5">
    <label v-if="label" class="block text-[13px] font-medium text-text-primary">
      {{ label }}
    </label>
    <div class="relative">
      <div v-if="icon" class="absolute left-3 top-1/2 -translate-y-1/2 text-text-tertiary pointer-events-none">
        <Icon :name="icon" size="16" />
      </div>
      <input
        :type="inputType"
        :value="modelValue"
        :placeholder="placeholder"
        :disabled="disabled"
        :class="[
          'w-full h-9 bg-surface-secondary/50 border rounded-xl px-3 text-sm text-text-primary',
          'placeholder:text-text-tertiary',
          'focus:outline-none focus:ring-2 focus:ring-accent/20 focus:border-accent focus:bg-surface',
          'transition-all duration-150 ease-out',
          'disabled:opacity-40 disabled:cursor-not-allowed',
          icon ? 'pl-9' : '',
          error ? 'border-danger focus:ring-danger/20 focus:border-danger' : 'border-border',
        ]"
        @input="emit('update:modelValue', ($event.target as HTMLInputElement).value)"
      />
      <button
        v-if="type === 'password'"
        type="button"
        class="absolute right-3 top-1/2 -translate-y-1/2 text-text-tertiary hover:text-text-secondary transition-colors"
        @click="showPassword = !showPassword"
      >
        <Icon :name="showPassword ? 'mdi:eye-off-outline' : 'mdi:eye-outline'" size="16" />
      </button>
    </div>
    <p v-if="error" class="text-xs text-danger">{{ error }}</p>
    <p v-else-if="helpText" class="text-xs text-text-tertiary">{{ helpText }}</p>
  </div>
</template>
