<script setup lang="ts">
interface Props {
  status: number
  type?: 'user' | 'generic'
}

const props = withDefaults(defineProps<Props>(), {
  type: 'generic',
})

const config = computed(() => {
  if (props.type === 'user') {
    switch (props.status) {
      case 0: return { label: '禁用', class: 'bg-danger/8 text-danger border-danger/15' }
      case 1: return { label: '正常', class: 'bg-success/8 text-success border-success/15' }
      case 2: return { label: '锁定', class: 'bg-warning/8 text-warning border-warning/15' }
      default: return { label: '未知', class: 'bg-surface-secondary text-text-secondary border-border' }
    }
  }
  switch (props.status) {
    case 0: return { label: '禁用', class: 'bg-danger/8 text-danger border-danger/15' }
    case 1: return { label: '启用', class: 'bg-success/8 text-success border-success/15' }
    default: return { label: '未知', class: 'bg-surface-secondary text-text-secondary border-border' }
  }
})
</script>

<template>
  <span :class="['inline-flex items-center px-2 py-0.5 rounded-full text-[11px] font-medium border', config.class]">
    {{ config.label }}
  </span>
</template>
