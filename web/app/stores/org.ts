// @ts-ignore
import { defineStore } from 'pinia'

export const useOrgStore = defineStore('org', () => {
  const currentOrgId = ref<number | null>(null)

  const orgHeader = computed(() => currentOrgId.value ? String(currentOrgId.value) : undefined)

  function setOrgId(id: number | null) {
    currentOrgId.value = id
  }

  return { currentOrgId, orgHeader, setOrgId }
}, { persist: true })
