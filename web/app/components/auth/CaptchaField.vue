<script setup lang="ts">
interface Props {
  modelValue: string
  svg: string
  error?: string
}

defineProps<Props>()
const emit = defineEmits<{
  'update:modelValue': [value: string]
  refresh: []
}>()
</script>

<template>
  <div class="space-y-1.5">
    <label class="block text-[13px] font-medium text-text-primary">{{ $t('auth.captcha') }}</label>
    <div class="flex gap-2">
      <input
        type="text"
        :value="modelValue"
        :placeholder="$t('auth.captcha')"
        :class="[
          'flex-1 h-9 bg-surface-secondary/50 border rounded-xl px-3 text-sm text-text-primary placeholder:text-text-tertiary focus:outline-none focus:ring-2 focus:ring-accent/20 focus:border-accent focus:bg-surface transition-all duration-150',
          error ? 'border-danger focus:ring-danger/20 focus:border-danger' : 'border-border',
        ]"
        @input="emit('update:modelValue', ($event.target as HTMLInputElement).value)"
      />
      <button
        type="button"
        class="h-9 w-24 rounded-xl border border-border overflow-hidden cursor-pointer bg-white flex items-center justify-center hover:border-text-tertiary/30 transition-colors"
        @click="emit('refresh')"
        v-html="svg"
      />
    </div>
    <p v-if="error" class="text-xs text-danger">{{ error }}</p>
  </div>
</template>
