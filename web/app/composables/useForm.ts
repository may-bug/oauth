export function useForm() {
  const isSubmitting = ref(false)
  const errors = ref<Record<string, string>>({})

  async function submit<T>(fn: () => Promise<T>): Promise<T | undefined> {
    isSubmitting.value = true
    try {
      return await fn()
    } finally {
      isSubmitting.value = false
    }
  }

  function validate(): boolean {
    // Overridden by the component that calls this composable
    return true
  }

  return {
    isSubmitting,
    errors,
    submit,
    validate,
  }
}
