export function usePagination(defaultSize = 20) {
  const page = ref(1)
  const size = ref(defaultSize)
  const total = ref(0)

  const totalPages = computed(() => Math.ceil(total.value / size.value))

  function onPageChange(newPage: number) {
    page.value = newPage
  }

  function onSizeChange(newSize: number) {
    size.value = newSize
    page.value = 1
  }

  function setTotal(value: number) {
    total.value = value
  }

  function reset() {
    page.value = 1
  }

  return {
    page,
    size,
    total,
    totalPages,
    onPageChange,
    onSizeChange,
    setTotal,
    reset,
  }
}
