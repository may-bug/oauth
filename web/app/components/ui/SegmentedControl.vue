<script setup lang="ts">
interface Props {
  modelValue: string
  options: Array<{ label: string; value: string }>
}

const props = defineProps<Props>()
const emit = defineEmits<{
  'update:modelValue': [value: string]
}>()

const selectedIndex = computed(() => props.options.findIndex((o) => o.value === props.modelValue))
</script>

<template>
  <div class="relative flex bg-surface-secondary/60 rounded-xl p-0.5 border border-border/50">
    <!-- Active indicator — iOS-style sliding pill -->
    <div
      class="absolute top-0.5 bottom-0.5 bg-surface rounded-[11px] shadow-sm border border-border/30 transition-all duration-250 ease-out"
      :style="{
        left: `calc(${selectedIndex} * (100% - 4px) / ${options.length} + 2px)`,
        width: `calc((100% - 4px) / ${options.length})`,
      }"
    />
    <button
      v-for="option in options"
      :key="option.value"
      type="button"
      :class="[
        'relative z-10 flex-1 py-1.5 text-[13px] font-medium text-center rounded-[11px] transition-colors duration-150',
        option.value === modelValue ? 'text-text-primary' : 'text-text-secondary hover:text-text-primary',
      ]"
      @click="emit('update:modelValue', option.value)"
    >
      {{ option.label }}
    </button>
  </div>
</template>
