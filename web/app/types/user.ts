export interface User {
  id: number
  username: string
  password?: string
  nickname?: string
  realName?: string
  avatar?: string
  gender: 0 | 1 | 2
  birthday?: string
  bio?: string
  status: 0 | 1 | 2
  emailVerified?: boolean
  phoneVerified?: boolean
  lastLoginAt?: string
  lastLoginIp?: string
  createdAt?: string
  updatedAt?: string
}

export interface UserProfile {
  id: number
  username: string
  nickname?: string
  realName?: string
  avatar?: string
  gender: 0 | 1 | 2
  birthday?: string
  bio?: string
  email?: string
  phone?: string
  emailVerified?: boolean
  phoneVerified?: boolean
  socialAccounts?: SocialAccount[]
}

export interface SocialAccount {
  provider: string
  providerUid: string
  providerUsername?: string
  boundAt?: string
}
