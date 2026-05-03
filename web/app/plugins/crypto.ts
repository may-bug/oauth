export default defineNuxtPlugin(async () => {
  if (import.meta.client) {
    // Pre-fetch public key on client side
    try {
      const { fetchPublicKey } = useCrypto()
      await fetchPublicKey()
    } catch {
      // Will retry on first use
    }
  }
})
