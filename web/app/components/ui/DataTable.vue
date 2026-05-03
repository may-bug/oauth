<script setup lang="ts" generic="T">
interface Column {
  key: string
  label: string
  width?: string
  align?: 'left' | 'center' | 'right'
}

interface Props {
  columns: Column[]
  data: T[]
  loading?: boolean
  rowKey?: string
}

const props = withDefaults(defineProps<Props>(), {
  loading: false,
  rowKey: 'id',
})
</script>

<template>
  <div class="overflow-x-auto rounded-xl border border-border/60 bg-surface">
    <table class="w-full">
      <thead>
        <tr class="border-b border-separator bg-surface-secondary/30">
          <th
            v-for="col in columns"
            :key="col.key"
            :class="[
              'py-2.5 px-4 text-[11px] font-semibold uppercase tracking-wider text-text-tertiary select-none',
              col.align === 'center' ? 'text-center' : col.align === 'right' ? 'text-right' : 'text-left',
            ]"
            :style="col.width ? { width: col.width } : {}"
          >
            {{ col.label }}
          </th>
        </tr>
      </thead>
      <tbody>
        <template v-if="loading">
          <tr v-for="i in 5" :key="i" class="border-b border-separator last:border-0">
            <td v-for="col in columns" :key="col.key" class="py-3 px-4">
              <div class="skeleton h-4 w-full" />
            </td>
          </tr>
        </template>
        <template v-else-if="data.length === 0">
          <tr>
            <td :colspan="columns.length" class="py-16">
              <EmptyState icon="mdi:table-large" title="暂无数据" />
            </td>
          </tr>
        </template>
        <template v-else>
          <tr
            v-for="row in data"
            :key="(row as any)[rowKey]"
            class="border-b border-separator last:border-0 hover:bg-surface-secondary/20 transition-colors duration-100"
          >
            <td
              v-for="col in columns"
              :key="col.key"
              :class="[
                'py-3 px-4 text-sm text-text-primary',
                col.align === 'center' ? 'text-center' : col.align === 'right' ? 'text-right' : 'text-left',
              ]"
            >
              <slot :name="col.key" :row="row">
                {{ (row as any)[col.key] }}
              </slot>
            </td>
          </tr>
        </template>
      </tbody>
    </table>
  </div>
</template>
