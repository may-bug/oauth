import type { CaptchaResponse } from '~/types/auth'
import { captchaToken, captchaCode, clearCaptcha as clearSharedCaptcha } from './captchaState'

export function useCaptcha() {
  const api = useApi()
  const captchaSvg = ref('')
  const captchaExpire = ref(0)

  async function fetchCaptcha(): Promise<void> {
    captchaCode.value = ''
    captchaToken.value = ''
    const data = await api.get<CaptchaResponse>('/auth/captcha')
    captchaSvg.value = data.svg
    captchaToken.value = data.token
    captchaExpire.value = data.expireSeconds
  }

  function clearCaptcha() {
    captchaSvg.value = ''
    clearSharedCaptcha()
  }

  return {
    captchaSvg,
    captchaToken,
    captchaCode,
    captchaExpire,
    fetchCaptcha,
    clearCaptcha,
  }
}
