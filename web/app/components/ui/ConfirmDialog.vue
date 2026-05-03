<script setup lang="ts">
import AppModal from "~/components/ui/AppModal.vue";
import AppButton from "~/components/ui/AppButton.vue";

interface Props {
  modelValue: boolean
  title: string
  message: string
  confirmText?: string
  cancelText?: string
  danger?: boolean
  loading?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  confirmText: '确认',
  cancelText: '取消',
  danger: false,
})

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  confirm: []
}>()

function onConfirm() {
  emit('confirm')
}
</script>

<template>
  <AppModal :model-value="modelValue" :title="title" max-width="380px" @update:model-value="emit('update:modelValue', $event)">
    <p class="text-sm text-text-secondary leading-relaxed">{{ message }}</p>
    <template #footer>
      <AppButton variant="secondary" size="sm" @click="emit('update:modelValue', false)">
        {{ cancelText }}
      </AppButton>
      <AppButton :variant="danger ? 'danger' : 'primary'" size="sm" :loading="loading" @click="onConfirm">
        {{ confirmText }}
      </AppButton>
    </template>
  </AppModal>
</template>
