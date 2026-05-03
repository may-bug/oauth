import type { LoginRequest, EmailLoginRequest, PhoneLoginRequest, RegisterRequest, TokenResponse, AuthConfig, PermissionsResponse } from '~/types/auth'
import type { User } from '~/types/user'

export function useAuth() {
  const api = useApi()
  const authStore = useAuthStore()
  const { success, error: showError } = useToast()

  async function loginWithPassword(data: LoginRequest): Promise<void> {
    const tokens = await api.post<TokenResponse>('/auth/login', data)
    authStore.setTokens(tokens.accessToken, tokens.refreshToken)
    await fetchUser()
    await fetchPermissions()
  }

  async function loginWithEmail(data: EmailLoginRequest): Promise<void> {
    const tokens = await api.post<TokenResponse>('/auth/login/email', data)
    authStore.setTokens(tokens.accessToken, tokens.refreshToken)
    await fetchUser()
    await fetchPermissions()
  }

  async function loginWithPhone(data: PhoneLoginRequest): Promise<void> {
    const tokens = await api.post<TokenResponse>('/auth/login/phone', data)
    authStore.setTokens(tokens.accessToken, tokens.refreshToken)
    await fetchUser()
    await fetchPermissions()
  }

  async function sendEmailCode(email: string): Promise<void> {
    await api.post('/auth/login/email/send', { email })
    success('验证码已发送')
  }

  async function sendPhoneCode(phone: string): Promise<void> {
    await api.post('/auth/login/phone/send', { phone })
    success('验证码已发送')
  }

  async function register(data: RegisterRequest): Promise<void> {
    const tokens = await api.post<TokenResponse>('/auth/register', data)
    authStore.setTokens(tokens.accessToken, tokens.refreshToken)
    await fetchUser()
    await fetchPermissions()
  }

  async function sendRegisterEmailCode(email: string): Promise<void> {
    await api.post('/auth/register/email/send', { email })
    success('验证码已发送')
  }

  async function logout(): Promise<void> {
    try {
      await api.post('/auth/logout')
    } catch {
      // ignore logout errors
    }
    authStore.clearAuth()
    await navigateTo('/auth/login')
  }

  async function fetchUser(): Promise<User> {
    const data = await api.get<{ authenticated: boolean; userId: number; username: string; nickname: string }>('/auth/status')
    const user: User = {
      id: data.userId,
      username: data.username,
      nickname: data.nickname,
    } as User
    authStore.setUser(user)
    return user
  }

  async function fetchPermissions(): Promise<PermissionsResponse> {
    const permissions = await api.get<PermissionsResponse>('/auth/permissions')
    authStore.setPermissions(permissions)
    return permissions
  }

  async function fetchAuthConfig(): Promise<AuthConfig> {
    return api.get<AuthConfig>('/auth/config')
  }

  async function getSocialAuthUrl(provider: string, redirectUri?: string): Promise<{ auth_url: string; state: string }> {
    return api.get(`/auth/social/${provider}`, { params: { redirectUri } })
  }

  async function handleSocialCallback(provider: string, code: string, state?: string): Promise<string | undefined> {
    const tokens = await api.get<TokenResponse>(`/auth/social/${provider}/callback`, { params: { code, state } })
    authStore.setTokens(tokens.accessToken, tokens.refreshToken)
    await fetchUser()
    await fetchPermissions()
    return tokens.redirectUri
  }

  return {
    loginWithPassword,
    loginWithEmail,
    loginWithPhone,
    sendEmailCode,
    sendPhoneCode,
    register,
    sendRegisterEmailCode,
    logout,
    fetchUser,
    fetchPermissions,
    fetchAuthConfig,
    getSocialAuthUrl,
    handleSocialCallback,
    isAuthenticated: computed(() => authStore.isAuthenticated),
    user: computed(() => authStore.user),
  }
}
