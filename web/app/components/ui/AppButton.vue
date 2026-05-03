<script setup lang="ts">
interface Props {
  variant?: 'primary' | 'secondary' | 'danger' | 'ghost'
  size?: 'sm' | 'md' | 'lg'
  loading?: boolean
  disabled?: boolean
  icon?: string
  block?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  variant: 'primary',
  size: 'md',
  loading: false,
  disabled: false,
  block: false,
})

const variantClasses = computed(() => {
  switch (props.variant) {
    case 'primary': return 'bg-accent text-white hover:bg-accent-hover active:bg-accent-hover/90 shadow-sm'
    case 'secondary': return 'bg-surface text-text-primary border border-border hover:bg-surface-secondary active:bg-surface-secondary/80'
    case 'danger': return 'bg-danger text-white hover:bg-danger/90 active:bg-danger/80 shadow-sm'
    case 'ghost': return 'bg-transparent text-accent hover:bg-accent-light active:bg-accent-light/60'
  }
})

const sizeClasses = computed(() => {
  switch (props.size) {
    case 'sm': return 'h-7 px-3 text-xs font-medium rounded-lg gap-1.5'
    case 'md': return 'h-9 px-4 text-sm font-medium rounded-xl gap-2'
    case 'lg': return 'h-11 px-6 text-[15px] font-semibold rounded-xl gap-2.5'
  }
})
</script>

<template>
  <button
    :class="[
      'inline-flex items-center justify-center select-none',
      'transition-all duration-150 ease-out',
      'disabled:opacity-40 disabled:cursor-not-allowed',
      'active:scale-[0.97] disabled:active:scale-100',
      'focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-accent',
      variantClasses,
      sizeClasses,
      block ? 'w-full' : '',
    ]"
    :disabled="disabled || loading"
  >
    <Icon
      v-if="loading"
      name="mdi:loading"
      class="animate-spin"
      :size="size === 'sm' ? '14' : size === 'lg' ? '20' : '16'"
    />
    <Icon
      v-else-if="icon"
      :name="icon"
      :size="size === 'sm' ? '14' : size === 'lg' ? '20' : '16'"
    />
    <slot />
  </button>
</template>
