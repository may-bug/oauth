<script setup lang="ts">
interface Toast {
  id: number
  type: 'success' | 'error' | 'warning' | 'info'
  message: string
}

const toasts = ref<Toast[]>([])
let nextId = 0

function addToast(type: string, message: string) {
  const id = nextId++
  toasts.value.push({ id, type: type as Toast['type'], message })
  setTimeout(() => removeToast(id), type === 'error' ? 5000 : 3000)
}

function removeToast(id: number) {
  toasts.value = toasts.value.filter((t) => t.id !== id)
}

onMounted(() => {
  window.addEventListener('toast', ((e: CustomEvent) => {
    addToast(e.detail.type, e.detail.message)
  }) as EventListener)
})

const typeStyles: Record<string, string> = {
  success: 'bg-success/10 text-success border-success/20',
  error: 'bg-danger/10 text-danger border-danger/20',
  warning: 'bg-warning/10 text-warning border-warning/20',
  info: 'bg-accent/10 text-accent border-accent/20',
}

const typeIcons: Record<string, string> = {
  success: 'mdi:check-circle',
  error: 'mdi:alert-circle',
  warning: 'mdi:alert',
  info: 'mdi:information',
}
</script>

<template>
  <Teleport to="body">
    <div class="fixed top-4 right-4 z-[100] flex flex-col gap-2 w-80 max-w-[calc(100vw-2rem)]">
      <TransitionGroup name="toast">
        <div
          v-for="toast in toasts"
          :key="toast.id"
          :class="[
            'flex items-start gap-3 p-3.5 rounded-xl border shadow-lg bg-surface animate-slide-up',
            typeStyles[toast.type],
          ]"
        >
          <Icon :name="typeIcons[toast.type]" size="18" class="shrink-0 mt-px" />
          <p class="text-[13px] flex-1 leading-relaxed">{{ toast.message }}</p>
          <button class="text-current opacity-40 hover:opacity-100 transition-opacity shrink-0" @click="removeToast(toast.id)">
            <Icon name="mdi:close" size="14" />
          </button>
        </div>
      </TransitionGroup>
    </div>
  </Teleport>
</template>

<style scoped>
.toast-enter-active { transition: all 0.3s var(--ease-spring); }
.toast-leave-active { transition: all 0.15s var(--ease-in); }
.toast-enter-from { opacity: 0; transform: translateX(80%) scale(0.95); }
.toast-leave-to { opacity: 0; transform: translateX(80%) scale(0.95); }
</style>
