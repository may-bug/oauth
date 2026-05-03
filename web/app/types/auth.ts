export interface LoginRequest {
  username: string
  password: string
}

export interface EmailLoginRequest {
  email: string
  code: string
}

export interface PhoneLoginRequest {
  phone: string
  code: string
}

export interface RegisterRequest {
  username: string
  password: string
  nickname?: string
  email?: string
  phone?: string
}

export interface TokenResponse {
  accessToken: string
  refreshToken: string
  expiresIn?: number
  tokenType?: string
  redirectUri?: string
}

export interface SocialProvider {
  provider: string
  name: string
  enabled: boolean
}

export interface CaptchaEndpointConfig {
  endpoint: string
  enabled: boolean
}

export interface CaptchaConfigDetail {
  enabled: boolean
  type: string
  length: number
  expireSeconds: number
  endpoints: CaptchaEndpointConfig[]
}

export interface CryptoEndpointConfig {
  endpoint: string
  enabled: boolean
  encryptedFields: string[]
}

export interface CryptoConfigDetail {
  enabled: boolean
  keySize: number
  publicKey: string
  endpoints: CryptoEndpointConfig[]
}

export interface AuthConfig {
  passwordLogin: boolean
  emailLogin: boolean
  phoneLogin: boolean
  socialLogin: SocialProvider[]
  captcha: CaptchaConfigDetail
  crypto: CryptoConfigDetail
}

export interface CaptchaResponse {
  svg: string
  token: string
  expireSeconds: number
}

export interface RefreshTokenRequest {
  refreshToken: string
}

export interface AuthStatus{
  authenticated:boolean
  username:string | undefined
  nickname:string | undefined
  userId:string| undefined
}

export interface PermissionsResponse {
  menus: Permission[]
  buttons: Permission[]
  apis: string[]
}

export interface Permission {
  id: number
  code: string
  name: string
  type: 1 | 2 | 3
  parentId?: number
  path?: string
  icon?: string
  sortOrder?: number
}
