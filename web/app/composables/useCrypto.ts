// Backend-compatible hybrid encryption using Web Crypto API
// Format: [4B AES-key-len(BE)][RSA-OAEP encrypted AES key][12B IV][AES-256-GCM ciphertext]
// All Base64 encoded

let publicKeyCrypto: CryptoKey | null = null

export function useCrypto() {

  async function setPublicKey(base64: string): Promise<void> {
    const keyData = base64ToUint8Array(base64)
    publicKeyCrypto = await crypto.subtle.importKey(
      'spki', keyData, { name: 'RSA-OAEP', hash: 'SHA-256' }, false, ['encrypt']
    )
  }

  function base64ToUint8Array(base64: string): Uint8Array {
    const binary = atob(base64)
    const bytes = new Uint8Array(binary.length)
    for (let i = 0; i < binary.length; i++) bytes[i] = binary.charCodeAt(i)
    return bytes
  }

  function uint8ArrayToBase64(bytes: Uint8Array): string {
    let binary = ''
    for (let i = 0; i < bytes.length; i++) binary += String.fromCharCode(bytes[i])
    return btoa(binary)
  }

  function concat(...arrays: Uint8Array[]): Uint8Array {
    const totalLength = arrays.reduce((sum, a) => sum + a.length, 0)
    const result = new Uint8Array(totalLength)
    let offset = 0
    for (const arr of arrays) { result.set(arr, offset); offset += arr.length }
    return result
  }

  function intToUint8ArrayBE(value: number): Uint8Array {
    const arr = new Uint8Array(4)
    arr[0] = (value >> 24) & 0xFF; arr[1] = (value >> 16) & 0xFF
    arr[2] = (value >> 8) & 0xFF; arr[3] = value & 0xFF
    return arr
  }

  async function encrypt(text: string): Promise<string> {
    if (!publicKeyCrypto) throw new Error('Public key not set')

    const aesKey = await crypto.subtle.generateKey(
      { name: 'AES-GCM', length: 256 }, true, ['encrypt']
    )
    const iv = crypto.getRandomValues(new Uint8Array(12))

    const encodedText = new TextEncoder().encode(text)
    const ciphertext = new Uint8Array(
      await crypto.subtle.encrypt({ name: 'AES-GCM', iv, tagLength: 128 }, aesKey, encodedText)
    )

    const rawAesKey = new Uint8Array(await crypto.subtle.exportKey('raw', aesKey))
    const encryptedAesKey = new Uint8Array(
      await crypto.subtle.encrypt({ name: 'RSA-OAEP' }, publicKeyCrypto!, rawAesKey)
    )

    return uint8ArrayToBase64(concat(
      intToUint8ArrayBE(encryptedAesKey.length), encryptedAesKey, iv, ciphertext
    ))
  }

  async function encryptPassword(password: string, cryptoEnabled: boolean): Promise<string> {
    if (!cryptoEnabled) return password
    return encrypt(password)
  }

  return { setPublicKey, encrypt, encryptPassword }
}
