<script setup lang="ts">
interface Props {
  items: Array<{ label: string; icon?: string; action: string; danger?: boolean }>
}

defineProps<Props>()
const emit = defineEmits<{
  select: [action: string]
}>()

const isOpen = ref(false)
const dropdownRef = ref<HTMLElement>()

function handleClickOutside(e: Event) {
  if (dropdownRef.value && !dropdownRef.value.contains(e.target as Node)) {
    isOpen.value = false
  }
}

onMounted(() => document.addEventListener('click', handleClickOutside))
onUnmounted(() => document.removeEventListener('click', handleClickOutside))

function select(action: string) {
  emit('select', action)
  isOpen.value = false
}
</script>

<template>
  <div ref="dropdownRef" class="relative">
    <button class="w-8 h-8 flex items-center justify-center rounded-lg hover:bg-surface-secondary transition-colors" @click="isOpen = !isOpen">
      <slot name="trigger">
        <Icon name="mdi:dots-vertical" size="18" class="text-text-secondary" />
      </slot>
    </button>
    <Transition name="dropdown">
      <div
        v-if="isOpen"
        class="absolute right-0 mt-1.5 w-48 bg-surface border border-border/60 rounded-xl shadow-lg py-1 z-50 overflow-hidden"
      >
        <button
          v-for="item in items"
          :key="item.action"
          :class="[
            'w-full flex items-center gap-2.5 px-4 py-2.5 text-[13px] text-left transition-colors duration-100',
            item.danger
              ? 'text-danger hover:bg-danger/6'
              : 'text-text-primary hover:bg-surface-secondary',
          ]"
          @click="select(item.action)"
        >
          <Icon v-if="item.icon" :name="item.icon" size="15" />
          {{ item.label }}
        </button>
      </div>
    </Transition>
  </div>
</template>

<style scoped>
.dropdown-enter-active { transition: all 0.15s var(--ease-out); }
.dropdown-leave-active { transition: all 0.1s var(--ease-in); }
.dropdown-enter-from,
.dropdown-leave-to { opacity: 0; transform: translateY(-6px) scale(0.96); }
</style>
