// Shared captcha state — used by useCaptcha composable and request interceptor
export const captchaToken = ref('')
export const captchaCode = ref('')

export function setCaptcha(token: string, code: string) {
  captchaToken.value = token
  captchaCode.value = code
}

export function clearCaptcha() {
  captchaToken.value = ''
  captchaCode.value = ''
}
