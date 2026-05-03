<script setup lang="ts">
import type { UserProfile } from '~/types/user'
import { GenderLabel } from '~/utils/constants'
import AppTextarea from "~/components/ui/AppTextarea.vue";
import AppButton from "~/components/ui/AppButton.vue";
import AppSelect from "~/components/ui/AppSelect.vue";
import AppInput from "~/components/ui/AppInput.vue";

interface Props {
  profile: UserProfile
}
const props = defineProps<Props>()
const emit = defineEmits<{
  save: [data: Partial<UserProfile>]
}>()

const { t } = useI18n()

const form = reactive({
  nickname: props.profile.nickname || '',
  realName: props.profile.realName || '',
  gender: props.profile.gender,
  birthday: props.profile.birthday || '',
  bio: props.profile.bio || '',
})

const genderOptions = computed(() => [
  { label: GenderLabel[0], value: 0 },
  { label: GenderLabel[1], value: 1 },
  { label: GenderLabel[2], value: 2 },
])

function handleSave() {
  emit('save', { ...form })
}
</script>

<template>
  <form @submit.prevent="handleSave" class="space-y-4">
    <AppInput v-model="form.nickname" :label="t('profile.nickname')" />
    <AppInput v-model="form.realName" :label="t('profile.realName')" />
    <AppSelect v-model="form.gender" :label="t('profile.gender')" :options="genderOptions" />
    <AppInput v-model="form.birthday" type="date" :label="t('profile.birthday')" />
    <AppTextarea v-model="form.bio" :label="t('profile.bio')" :max-length="500" :rows="3" />
    <AppButton type="submit">{{ t('common.save') }}</AppButton>
  </form>
</template>
