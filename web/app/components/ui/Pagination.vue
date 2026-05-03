<script setup lang="ts">
interface Props {
  page: number
  total: number
  size: number
}

const props = defineProps<Props>()
const emit = defineEmits<{
  'update:page': [value: number]
  'update:size': [value: number]
}>()

const totalPages = computed(() => Math.ceil(props.total / props.size))

const visiblePages = computed(() => {
  const pages: number[] = []
  const start = Math.max(1, props.page - 2)
  const end = Math.min(totalPages.value, props.page + 2)
  for (let i = start; i <= end; i++) pages.push(i)
  return pages
})
</script>

<template>
  <div v-if="totalPages > 1" class="flex items-center justify-between gap-4 py-3">
    <span class="text-xs text-text-secondary">共 {{ total }} 条</span>
    <div class="flex items-center gap-1">
      <!-- Prev -->
      <button
        :disabled="page <= 1"
        class="w-8 h-8 flex items-center justify-center rounded-lg text-text-secondary hover:bg-surface-secondary disabled:opacity-30 disabled:cursor-not-allowed transition-colors duration-150"
        @click="emit('update:page', page - 1)"
      >
        <Icon name="mdi:chevron-left" size="18" />
      </button>
      <!-- Page numbers -->
      <button
        v-for="p in visiblePages"
        :key="p"
        :class="[
          'w-8 h-8 flex items-center justify-center rounded-lg text-xs font-medium transition-all duration-150',
          p === page
            ? 'bg-accent text-white shadow-sm'
            : 'text-text-secondary hover:bg-surface-secondary',
        ]"
        @click="emit('update:page', p)"
      >
        {{ p }}
      </button>
      <!-- Next -->
      <button
        :disabled="page >= totalPages"
        class="w-8 h-8 flex items-center justify-center rounded-lg text-text-secondary hover:bg-surface-secondary disabled:opacity-30 disabled:cursor-not-allowed transition-colors duration-150"
        @click="emit('update:page', page + 1)"
      >
        <Icon name="mdi:chevron-right" size="18" />
      </button>
    </div>
  </div>
</template>
