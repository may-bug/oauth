<script setup lang="ts">
interface Props {
  modelValue: boolean
  title?: string
  maxWidth?: string
}

const props = withDefaults(defineProps<Props>(), {
  maxWidth: '440px',
})

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
}>()

function close() {
  emit('update:modelValue', false)
}

function onKeydown(e: KeyboardEvent) {
  if (e.key === 'Escape') close()
}

onMounted(() => {
  document.addEventListener('keydown', onKeydown)
})

onUnmounted(() => {
  document.removeEventListener('keydown', onKeydown)
})
</script>

<template>
  <Teleport to="body">
    <Transition name="modal">
      <div
        v-if="modelValue"
        class="fixed inset-0 z-50 flex items-center justify-center p-4"
      >
        <!-- Backdrop — dark with subtle blur -->
        <div class="absolute inset-0 bg-black/30 backdrop-blur-sm" @click="close" />

        <!-- Content — elevated surface -->
        <div
          class="relative bg-surface rounded-2xl shadow-xl animate-scale-in w-full border border-border/40"
          :style="{ maxWidth }"
        >
          <!-- Header -->
          <div v-if="title || $slots.header" class="flex items-center justify-between px-5 py-4 border-b border-separator">
            <slot name="header">
              <h3 class="text-[17px] font-semibold text-text-primary leading-none">{{ title }}</h3>
            </slot>
            <button class="w-7 h-7 flex items-center justify-center rounded-full text-text-tertiary hover:text-text-primary hover:bg-surface-secondary transition-all" @click="close">
              <Icon name="mdi:close" size="18" />
            </button>
          </div>

          <!-- Body -->
          <div class="px-5 py-4">
            <slot />
          </div>

          <!-- Footer -->
          <div v-if="$slots.footer" class="flex justify-end gap-3 px-5 py-4 border-t border-separator bg-surface-secondary/30 rounded-b-2xl">
            <slot name="footer" />
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.modal-enter-active { transition: opacity 0.2s var(--ease-out); }
.modal-leave-active { transition: opacity 0.15s var(--ease-in); }
.modal-enter-from,
.modal-leave-to { opacity: 0; }
</style>
