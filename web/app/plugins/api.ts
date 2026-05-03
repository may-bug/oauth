import { createRequestInstance } from '~/utils/request'

export default defineNuxtPlugin(() => {
  const api = createRequestInstance()

  return {
    provide: {
      api,
    },
  }
})
