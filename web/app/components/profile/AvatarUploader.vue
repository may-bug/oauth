<script setup lang="ts">

import UserAvatar from "~/components/common/UserAvatar.vue";
import AppButton from "~/components/ui/AppButton.vue";

interface Props {
  src?: string
  name?: string
}
defineProps<Props>()
const emit = defineEmits<{
  upload: [file: File]
}>()

const { t } = useI18n()
const { error: showError } = useToast()
const fileInput = ref<HTMLInputElement>()

function onFileChange(e: Event) {
  const file = (e.target as HTMLInputElement).files?.[0]
  if (!file) return
  if (!file.type.startsWith('image/')) {
    showError(t('profile.avatarInvalidType'))
    return
  }
  if (file.size > 2 * 1024 * 1024) {
    showError(t('profile.avatarTooLarge'))
    return
  }
  emit('upload', file)
}
</script>

<template>
  <div class="flex items-center gap-4">
    <UserAvatar :src="src" :name="name" size="lg" />
    <div>
      <AppButton variant="secondary" size="sm" @click="fileInput?.click()">
        {{ t('profile.uploadAvatar') }}
      </AppButton>
      <input ref="fileInput" type="file" accept="image/*" class="hidden" @change="onFileChange" />
      <p class="text-xs text-text-tertiary mt-1">JPG, PNG, 最大 2MB</p>
    </div>
  </div>
</template>
